/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.net;

import static android.net.NetworkPolicy.CYCLE_NONE;
import static android.net.NetworkPolicy.LIMIT_DISABLED;
import static android.net.NetworkPolicy.SNOOZE_NEVER;
import static android.net.NetworkPolicy.WARNING_DISABLED;
import static android.net.NetworkTemplate.MATCH_MOBILE_3G_LOWER;
import static android.net.NetworkTemplate.MATCH_MOBILE_4G;
import static android.net.NetworkTemplate.MATCH_WIFI;
import static android.net.NetworkTemplate.buildTemplateMobile3gLower;
import static android.net.NetworkTemplate.buildTemplateMobile4g;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static com.android.internal.util.Preconditions.checkNotNull;

import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.text.format.Time;

import com.android.internal.util.Objects;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Utility class to modify list of {@link NetworkPolicy}. Specifically knows
 * about which policies can coexist. This editor offers thread safety when
 * talking with {@link NetworkPolicyManager}.
 */
public class NetworkPolicyEditor {
    // TODO: be more robust when missing policies from service

    public static final boolean ENABLE_SPLIT_POLICIES = false;

    private NetworkPolicyManager mPolicyManager;
    private ArrayList<NetworkPolicy> mPolicies = Lists.newArrayList();

    public NetworkPolicyEditor(NetworkPolicyManager policyManager) {
        mPolicyManager = checkNotNull(policyManager);
    }

    public void read() {
        final NetworkPolicy[] policies = mPolicyManager.getNetworkPolicies();

        boolean modified = false;
        mPolicies.clear();
        for (NetworkPolicy policy : policies) {
            // TODO: find better place to clamp these
            if (policy.limitBytes < -1) {
                policy.limitBytes = LIMIT_DISABLED;
                modified = true;
            }
            if (policy.warningBytes < -1) {
                policy.warningBytes = WARNING_DISABLED;
                modified = true;
            }

            mPolicies.add(policy);
        }

        // force combine any split policies when disabled
        if (!ENABLE_SPLIT_POLICIES) {
            modified |= forceMobilePolicyCombined();
        }

        // when we cleaned policies above, write back changes
        if (modified) writeAsync();
    }

    public void writeAsync() {
        // TODO: consider making more robust by passing through service
        final NetworkPolicy[] policies = mPolicies.toArray(new NetworkPolicy[mPolicies.size()]);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                write(policies);
                return null;
            }
        }.execute();
    }

    public void write(NetworkPolicy[] policies) {
        mPolicyManager.setNetworkPolicies(policies);
    }

    public boolean hasLimitedPolicy(NetworkTemplate template) {
        final NetworkPolicy policy = getPolicy(template);
        return policy != null && policy.limitBytes != LIMIT_DISABLED && policy.active;
    }

    public NetworkPolicy getOrCreatePolicy(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        if (policy == null) {
            policy = buildDefaultPolicy(template);
            mPolicies.add(policy);
        }
        return policy;
    }

    public NetworkPolicy getPolicy(NetworkTemplate template) {
        for (NetworkPolicy policy : mPolicies) {
            if (policy.template.equals(template)) {
                return policy;
            }
        }
        return null;
    }

    @Deprecated
    private static NetworkPolicy buildDefaultPolicy(NetworkTemplate template) {
        // TODO: move this into framework to share with NetworkPolicyManagerService
        final int cycleDay;
        final String cycleTimezone;
        final boolean metered;

        if (template.getMatchRule() == MATCH_WIFI) {
            cycleDay = CYCLE_NONE;
            cycleTimezone = Time.TIMEZONE_UTC;
            metered = false;
        } else {
            final Time time = new Time();
            time.setToNow();
            cycleDay = time.monthDay;
            cycleTimezone = time.timezone;
            metered = true;
        }

        return new NetworkPolicy(template, cycleDay, cycleTimezone, WARNING_DISABLED,
                LIMIT_DISABLED, SNOOZE_NEVER, SNOOZE_NEVER, metered, true);
    }

    public int getPolicyCycleDay(NetworkTemplate template) {
        return getPolicy(template).cycleDay;
    }

    public void setPolicyCycleDay(NetworkTemplate template, int cycleDay, String cycleTimezone) {
        final NetworkPolicy policy = getOrCreatePolicy(template);
        cleanPolicyActive();
        policy.cycleDay = cycleDay;
        policy.cycleTimezone = cycleTimezone;
        policy.inferred = false;
        policy.active = true;
        policy.clearSnooze();
        writeAsync();
    }
    
    private void cleanPolicyActive() {
        for (NetworkPolicy policy : mPolicies) {
            policy.active = false;
        }
    }

    public void setPolicyActive(NetworkPolicy policy) {
        cleanPolicyActive();
        policy.active = true;
    }
    
    public long getPolicyWarningBytes(NetworkTemplate template) {
        return getPolicy(template).warningBytes;
    }

    public void setPolicyWarningBytes(NetworkTemplate template, long warningBytes) {
        final NetworkPolicy policy = getOrCreatePolicy(template);
        cleanPolicyActive();
        policy.warningBytes = warningBytes;
        policy.inferred = false;
        policy.clearSnooze();
        policy.active = true;
        writeAsync();
    }

    public long getPolicyLimitBytes(NetworkTemplate template) {
        return getPolicy(template).limitBytes;
    }

    public void setPolicyLimitBytes(NetworkTemplate template, long limitBytes) {
        final NetworkPolicy policy = getOrCreatePolicy(template);
        cleanPolicyActive();
        policy.limitBytes = limitBytes;
        policy.inferred = false;
        policy.clearSnooze();
        policy.active = true;
        writeAsync();
    }

    public boolean getPolicyMetered(NetworkTemplate template) {
        final NetworkPolicy policy = getPolicy(template);
        if (policy != null) {
            return policy.metered;
        } else {
            return false;
        }
    }

    public void setPolicyMetered(NetworkTemplate template, boolean metered) {
        boolean modified = false;

        NetworkPolicy policy = getPolicy(template);
        if (metered) {
            if (policy == null) {
                policy = buildDefaultPolicy(template);
                policy.metered = true;
                policy.inferred = false;
                mPolicies.add(policy);
                modified = true;
            } else if (!policy.metered) {
                policy.metered = true;
                policy.inferred = false;
                modified = true;
            }

        } else {
            if (policy == null) {
                // ignore when policy doesn't exist
            } else if (policy.metered) {
                policy.metered = false;
                policy.inferred = false;
                modified = true;
            }
        }

        if (modified) writeAsync();
    }

    /**
     * Remove any split {@link NetworkPolicy}.
     */
    private boolean forceMobilePolicyCombined() {
        final HashSet<String> subscriberIds = Sets.newHashSet();
        for (NetworkPolicy policy : mPolicies) {
            subscriberIds.add(policy.template.getSubscriberId());
        }

        boolean modified = false;
        for (String subscriberId : subscriberIds) {
            modified |= setMobilePolicySplitInternal(subscriberId, false);
        }
        return modified;
    }

    @Deprecated
    public boolean isMobilePolicySplit(String subscriberId) {
        boolean has3g = false;
        boolean has4g = false;
        for (NetworkPolicy policy : mPolicies) {
            final NetworkTemplate template = policy.template;
            if (Objects.equal(subscriberId, template.getSubscriberId())) {
                switch (template.getMatchRule()) {
                    case MATCH_MOBILE_3G_LOWER:
                        has3g = true;
                        break;
                    case MATCH_MOBILE_4G:
                        has4g = true;
                        break;
                }
            }
        }
        return has3g && has4g;
    }

    @Deprecated
    public void setMobilePolicySplit(String subscriberId, boolean split) {
        if (setMobilePolicySplitInternal(subscriberId, split)) {
            writeAsync();
        }
    }

    /**
     * Mutate {@link NetworkPolicy} for given subscriber, combining or splitting
     * the policy as requested.
     *
     * @return {@code true} when any {@link NetworkPolicy} was mutated.
     */
    @Deprecated
    private boolean setMobilePolicySplitInternal(String subscriberId, boolean split) {
        final boolean beforeSplit = isMobilePolicySplit(subscriberId);

        final NetworkTemplate template3g = buildTemplateMobile3gLower(subscriberId);
        final NetworkTemplate template4g = buildTemplateMobile4g(subscriberId);
        final NetworkTemplate templateAll = buildTemplateMobileAll(subscriberId);

        if (split == beforeSplit) {
            // already in requested state; skip
            return false;

        } else if (beforeSplit && !split) {
            // combine, picking most restrictive policy
            final NetworkPolicy policy3g = getPolicy(template3g);
            final NetworkPolicy policy4g = getPolicy(template4g);

            final NetworkPolicy restrictive = policy3g.compareTo(policy4g) < 0 ? policy3g
                    : policy4g;
            mPolicies.remove(policy3g);
            mPolicies.remove(policy4g);
            mPolicies.add(new NetworkPolicy(templateAll, restrictive.cycleDay,
                    restrictive.cycleTimezone, restrictive.warningBytes, restrictive.limitBytes,
                    SNOOZE_NEVER, SNOOZE_NEVER, restrictive.metered, restrictive.inferred));
            return true;

        } else if (!beforeSplit && split) {
            // duplicate existing policy into two rules
            final NetworkPolicy policyAll = getPolicy(templateAll);
            mPolicies.remove(policyAll);
            mPolicies.add(new NetworkPolicy(template3g, policyAll.cycleDay, policyAll.cycleTimezone,
                    policyAll.warningBytes, policyAll.limitBytes, SNOOZE_NEVER, SNOOZE_NEVER,
                    policyAll.metered, policyAll.inferred));
            mPolicies.add(new NetworkPolicy(template4g, policyAll.cycleDay, policyAll.cycleTimezone,
                    policyAll.warningBytes, policyAll.limitBytes, SNOOZE_NEVER, SNOOZE_NEVER,
                    policyAll.metered, policyAll.inferred));
            return true;
        } else {
            return false;
        }
    }
}
