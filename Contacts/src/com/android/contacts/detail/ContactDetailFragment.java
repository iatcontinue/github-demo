/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.contacts.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity.NamedContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.DisplayNameSources;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.StatusUpdates;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.android.contacts.Collapser;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.ContactPresenceIconUtil;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GroupMetaData;
import com.android.contacts.R;
import com.android.contacts.TypePrecedence;
import com.android.contacts.activities.ContactDetailActivity.FragmentKeyListener;
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDelta.ValuesDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.dataitem.EmailDataItem;
import com.android.contacts.model.dataitem.EventDataItem;
import com.android.contacts.model.dataitem.GroupMembershipDataItem;
import com.android.contacts.model.dataitem.ImDataItem;
import com.android.contacts.model.dataitem.NicknameDataItem;
import com.android.contacts.model.dataitem.NoteDataItem;
import com.android.contacts.model.dataitem.OrganizationDataItem;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.model.dataitem.RelationDataItem;
import com.android.contacts.model.dataitem.SipAddressDataItem;
import com.android.contacts.model.dataitem.StructuredNameDataItem;
import com.android.contacts.model.dataitem.StructuredPostalDataItem;
import com.android.contacts.model.dataitem.WebsiteDataItem;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.ClipboardUtils;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.StreamItemEntry;
import com.android.contacts.util.StructuredPostalUtils;
import com.android.internal.telephony.ITelephony;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/** M: New Feature xxx @{ */
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo; 
import android.telephony.PhoneNumberUtils;
import android.view.Menu;

import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.ext.ContactDetailExtension;
import com.android.contacts.ext.ContactPluginDefault;
import com.android.contacts.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.util.NotifyingAsyncQueryHandler;
import com.android.contacts.util.ContactBadgeUtil;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.detail.AssociationSimActivity;
import com.mediatek.contacts.detail.AssociationSimActivity.ContactDetailInfo;
import com.mediatek.contacts.extension.aassne.SimUtils;
import com.mediatek.phone.SIMInfoWrapper;
/** @} */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// GPBYL-72 chenbo add 20130517 (start)
import android.graphics.drawable.Drawable;
// GPBYL-72 chenbo add 20130517 (end)

/** M: New Feature xxx @{ */
/* orignal code
public class ContactDetailFragment extends Fragment implements FragmentKeyListener,
        SelectAccountDialogFragment.Listener, OnItemClickListener {
*/
public class ContactDetailFragment extends Fragment implements
        FragmentKeyListener, SelectAccountDialogFragment.Listener,
        OnItemClickListener, NotifyingAsyncQueryHandler.AsyncQueryListener,
        NotifyingAsyncQueryHandler.AsyncUpdateListener {
/** @} */

    private static final String TAG = "ContactDetailFragment";

    private interface ContextMenuIds {
        static final int COPY_TEXT = 0;
        static final int CLEAR_DEFAULT = 1;
        static final int SET_DEFAULT = 2;

        /** M: New Feature xxx @{ */
        static final int NEW_ASSOCIATION_SIM = 3;
        static final int DEL_ASSOCIATION_SIM = 4;
        static final int COPY_TO_DIALER = 5;
        static final int DIAL_WITH_COUNTRY_CODE = 6;
        /** @} */
    }

    private static final String KEY_CONTACT_URI = "contactUri";
    private static final String KEY_LIST_STATE = "liststate";

    private Context mContext;
    private View mView;
    private OnScrollListener mVerticalScrollListener;
    private Uri mLookupUri;
    private Listener mListener;

    private Contact mContactData;
    private ViewGroup mStaticPhotoContainer;
    private View mPhotoTouchOverlay;
    private ListView mListView;
    private ViewAdapter mAdapter;
    private Uri mPrimaryPhoneUri = null;
    private ViewEntryDimensions mViewEntryDimensions;

    private final ContactDetailPhotoSetter mPhotoSetter = new ContactDetailPhotoSetter();

    private Button mQuickFixButton;
    private QuickFix mQuickFix;
    private String mDefaultCountryIso;
    private boolean mContactHasSocialUpdates;
    private boolean mShowStaticPhoto = true;
	// GPBYL-72 chenbo add 20130517 (start)
	private Drawable mOverLayBg;
	// GPBYL-72 chenbo add 20130517 (end)
    private final QuickFix[] mPotentialQuickFixes = new QuickFix[] {
            new MakeLocalCopyQuickFix(),
            new AddToMyContactsQuickFix()
    };

    /**
     * Device capability: Set during buildEntries and used in the long-press context menu
     */
    private boolean mHasPhone;

    /**
     * Device capability: Set during buildEntries and used in the long-press context menu
     */
    private boolean mHasSms;

    /**
     * Device capability: Set during buildEntries and used in the long-press context menu
     */
    private boolean mHasSip;

    /**
     * The view shown if the detail list is empty.
     * We set this to the list view when first bind the adapter, so that it won't be shown while
     * we're loading data.
     */
    private View mEmptyView;

    /**
     * Saved state of the {@link ListView}. This must be saved and applied to the {@ListView} only
     * when the adapter has been populated again.
     */
    private Parcelable mListState;

    /**
     * Lists of specific types of entries to be shown in contact details.
     */
    private ArrayList<DetailViewEntry> mPhoneEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mSmsEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mEmailEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mPostalEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mImEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mNicknameEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mGroupEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mRelationEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mNoteEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mWebsiteEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mSipEntries = new ArrayList<DetailViewEntry>();
    private ArrayList<DetailViewEntry> mEventEntries = new ArrayList<DetailViewEntry>();
    private final Map<AccountType, List<DetailViewEntry>> mOtherEntriesMap =
            new HashMap<AccountType, List<DetailViewEntry>>();
    private ArrayList<ViewEntry> mAllEntries = new ArrayList<ViewEntry>();
    private LayoutInflater mInflater;

    private boolean mIsUniqueNumber;
    private boolean mIsUniqueEmail;

    private ListPopupWindow mPopup;

    /**
     * This is to forward touch events to the list view to enable users to scroll the list view
     * from the blank area underneath the static photo when the layout with static photo is used.
     */
    private OnTouchListener mForwardTouchToListView = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mListView != null) {
                mListView.dispatchTouchEvent(event);
                return true;
            }
            return false;
        }
    };

    /**
     * This is to forward drag events to the list view to enable users to scroll the list view
     * from the blank area underneath the static photo when the layout with static photo is used.
     */
    private OnDragListener mForwardDragToListView = new OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (mListView != null) {
                mListView.dispatchDragEvent(event);
                return true;
            }
            return false;
        }
    };

    public ContactDetailFragment() {
        // Explicit constructor for inflation
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** M: New Feature xxx @{ */
        setHasOptionsMenu(true);
        /** @} */
        if (savedInstanceState != null) {
            mLookupUri = savedInstanceState.getParcelable(KEY_CONTACT_URI);
            mListState = savedInstanceState.getParcelable(KEY_LIST_STATE);
        }

        mHandler = new NotifyingAsyncQueryHandler(this.mContext, this);
        mHandler.setUpdateListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CONTACT_URI, mLookupUri);
        if (mListView != null) {
            outState.putParcelable(KEY_LIST_STATE, mListView.onSaveInstanceState());
        }
    }

    /** M:AAS update Phone type label @ { */
    public void onStart() {
        super.onStart();
        if (mContactData != null) {
            final String accountType = SimUtils.getAccountTypeBySlot(mContactData.getSlot());
            if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(accountType,
                    ExtensionManager.COMMD_FOR_AAS)) {
                Log.d(TAG, "rebind data for Datakind changed");
                bindData();
            }
        }
    }

    /** M: @ } */

    @Override
    public void onPause() {
        dismissPopupIfShown();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(mContext);
        mViewEntryDimensions = new ViewEntryDimensions(mContext.getResources());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        mView = inflater.inflate(R.layout.contact_detail_fragment, container, false);
        // Set the touch and drag listener to forward the event to the mListView so that
        // vertical scrolling can happen from outside of the list view.
        mView.setOnTouchListener(mForwardTouchToListView);
        mView.setOnDragListener(mForwardDragToListView);

        mInflater = inflater;

        mStaticPhotoContainer = (ViewGroup) mView.findViewById(R.id.static_photo_container);
        mPhotoTouchOverlay = mView.findViewById(R.id.photo_touch_intercept_overlay);
		// GPBYL-72 chenbo add 20130517 (start)
		if (mPhotoTouchOverlay != null) {
			mOverLayBg = mPhotoTouchOverlay.getBackground();
		}
		// GPBYL-72 chenbo add 20130517 (end)
        mListView = (ListView) mView.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setItemsCanFocus(true);
        mListView.setOnScrollListener(mVerticalScrollListener);

        // Don't set it to mListView yet.  We do so later when we bind the adapter.
        mEmptyView = mView.findViewById(android.R.id.empty);

        mQuickFixButton = (Button) mView.findViewById(R.id.contact_quick_fix);
        mQuickFixButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuickFix != null) {
                    mQuickFix.execute();
                }
            }
        });

        mView.setVisibility(View.INVISIBLE);

        if (mContactData != null) {
            bindData();
        }

        return mView;
    }

    public void setListener(Listener value) {
        mListener = value;
    }

    protected Context getContext() {
        return mContext;
    }

    protected Listener getListener() {
        return mListener;
    }

    protected Contact getContactData() {
        return mContactData;
    }

    public void setVerticalScrollListener(OnScrollListener listener) {
        mVerticalScrollListener = listener;
    }

    public Uri getUri() {
        return mLookupUri;
    }

    /**
     * Sets whether the static contact photo (that is not in a scrolling region), should be shown
     * or not.
     */
    public void setShowStaticPhoto(boolean showPhoto) {
        mShowStaticPhoto = showPhoto;
    }

    /**
     * Shows the contact detail with a message indicating there are no contact details.
     */
    public void showEmptyState() {
        setData(null, null);
    }

    public void setData(Uri lookupUri, Contact result) {
        mLookupUri = lookupUri;
        mContactData = result;
        bindData();
    }

    /**
     * Reset the list adapter in this {@link Fragment} to get rid of any saved scroll position
     * from a previous contact.
     */
    public void resetAdapter() {
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    /**
     * Returns the top coordinate of the first item in the {@link ListView}. If the first item
     * in the {@link ListView} is not visible or there are no children in the list, then return
     * Integer.MIN_VALUE. Note that the returned value will be <= 0 because the first item in the
     * list cannot have a positive offset.
     */
    public int getFirstListItemOffset() {
        return ContactDetailDisplayUtils.getFirstListItemOffset(mListView);
    }

    /**
     * Tries to scroll the first item to the given offset (this can be a no-op if the list is
     * already in the correct position).
     * @param offset which should be <= 0
     */
    public void requestToMoveToOffset(int offset) {
        ContactDetailDisplayUtils.requestToMoveToOffset(mListView, offset);
    }

    protected void bindData() {
        if (mView == null) {
            return;
        }

        if (isAdded()) {
            getActivity().invalidateOptionsMenu();
        }

        if (mContactData == null) {
            mView.setVisibility(View.INVISIBLE);
            if (mStaticPhotoContainer != null) {
                mStaticPhotoContainer.setVisibility(View.GONE);
            }
            mAllEntries.clear();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        /** M:AAS @ { */
        int slotId = SIMInfo.getSlotById(mContext, (long) mContactData.getIndicate());
        ExtensionManager.getInstance().getContactAccountExtension().setCurrentSlot(slotId, ExtensionManager.COMMD_FOR_AAS);
        /** M: @ } */
        // Figure out if the contact has social updates or not
        mContactHasSocialUpdates = !mContactData.getStreamItems().isEmpty();

        // Setup the photo if applicable
        if (mStaticPhotoContainer != null) {
            // The presence of a static photo container is not sufficient to determine whether or
            // not we should show the photo. Check the mShowStaticPhoto flag which can be set by an
            // outside class depending on screen size, layout, and whether the contact has social
            // updates or not.
            if (mShowStaticPhoto) {
                mStaticPhotoContainer.setVisibility(View.VISIBLE);
                final ImageView photoView = (ImageView) mStaticPhotoContainer.findViewById(
                        R.id.photo);
                final boolean expandPhotoOnClick = mContactData.getPhotoUri() != null;
                final OnClickListener listener = mPhotoSetter.setupContactPhotoForClick(
                        mContext, mContactData, photoView, expandPhotoOnClick);
                if (mPhotoTouchOverlay != null) {
                    mPhotoTouchOverlay.setVisibility(View.VISIBLE);
                    /** M: New Feature @{ */
                    /*
                     * Original Code if (expandPhotoOnClick ||
                     * mContactData.isWritableContact(mContext)) {
                     * mPhotoTouchOverlay.setOnClickListener(listener); } else {
                     * mPhotoTouchOverlay.setClickable(false); }
                     */
                    Log.i(TAG, "[bindData] mContactData.getIndicate() : "
                            + mContactData.getIndicate());
					// GPBYL-72 chenbo add 20130517 (start) // we should catch the touch event even it has no use
                    if (mContactData.getIndicate() >= 0) {
                        mPhotoTouchOverlay.setOnClickListener(null);
        				mPhotoTouchOverlay.setBackgroundResource(android.R.color.transparent);
                    } else if (expandPhotoOnClick || mContactData.isWritableContact(mContext)) {
                        mPhotoTouchOverlay.setOnClickListener(listener);
						mPhotoTouchOverlay.setBackground(mOverLayBg);
                    } else {
                        mPhotoTouchOverlay.setOnClickListener(null);
        				mPhotoTouchOverlay.setBackgroundResource(android.R.color.transparent);
                    }
					// GPBYL-72 chenbo add 20130517 (end)
                    /** @} */
                }
            } else {
                mStaticPhotoContainer.setVisibility(View.GONE);
            }
        }

        // Build up the contact entries
        buildEntries();

        // Collapse similar data items for select {@link DataKind}s.
        if (ExtensionManager.getInstance().getContactDetailExtension().collapsePhoneEntries(
                ContactPluginDefault.COMMD_FOR_OP01)) {
            Collapser.collapseList(mPhoneEntries);
        }
        /** M: New Feature xxx @{ */
        getShowingPhoneEntries();
        /** @} */
        Collapser.collapseList(mSmsEntries);
        Collapser.collapseList(mEmailEntries);
        Collapser.collapseList(mPostalEntries);
        Collapser.collapseList(mImEntries);
        Collapser.collapseList(mEventEntries);

        mIsUniqueNumber = mPhoneEntries.size() == 1;
        mIsUniqueEmail = mEmailEntries.size() == 1;

        // Make one aggregated list of all entries for display to the user.
        setupFlattenedList();

        if (mAdapter == null) {
            mAdapter = new ViewAdapter();
            mListView.setAdapter(mAdapter);
        }

        // Restore {@link ListView} state if applicable because the adapter is now populated.
        if (mListState != null) {
            mListView.onRestoreInstanceState(mListState);
            mListState = null;
        }

        mAdapter.notifyDataSetChanged();

        mListView.setEmptyView(mEmptyView);

        configureQuickFix();

        mView.setVisibility(View.VISIBLE);
    }

    /*
     * Sets {@link #mQuickFix} to a useful action and configures the visibility of
     * {@link #mQuickFixButton}
     */
    private void configureQuickFix() {
        mQuickFix = null;

        for (QuickFix fix : mPotentialQuickFixes) {
            if (fix.isApplicable()) {
                mQuickFix = fix;
                break;
            }
        }

        // Configure the button
        if (mQuickFix == null) {
            mQuickFixButton.setVisibility(View.GONE);
        } else {
            mQuickFixButton.setVisibility(View.VISIBLE);
            mQuickFixButton.setText(mQuickFix.getTitle());
        }
    }

    /** @return default group id or -1 if no group or several groups are marked as default */
    private long getDefaultGroupId(List<GroupMetaData> groups) {
        long defaultGroupId = -1;
        for (GroupMetaData group : groups) {
            if (group.isDefaultGroup()) {
                // two default groups? return neither
                if (defaultGroupId != -1) return -1;
                defaultGroupId = group.getGroupId();
            }
        }
        return defaultGroupId;
    }

    /**
     * Build up the entries to display on the screen.
     */
    private final void buildEntries() {
        mHasPhone = PhoneCapabilityTester.isPhone(mContext);
        mHasSms = PhoneCapabilityTester.isSmsIntentRegistered(mContext);
        mHasSip = PhoneCapabilityTester.isSipPhone(mContext);

        // Clear out the old entries
        mAllEntries.clear();

        mPrimaryPhoneUri = null;

        // Build up method entries
        if (mContactData == null) {
            return;
        }

        ArrayList<String> groups = new ArrayList<String>();
        for (RawContact rawContact: mContactData.getRawContacts()) {
            final long rawContactId = rawContact.getId();
            for (DataItem dataItem : rawContact.getDataItems()) {
                dataItem.setRawContactId(rawContactId);

                if (dataItem.getMimeType() == null) continue;

                if (dataItem instanceof GroupMembershipDataItem) {
                    GroupMembershipDataItem groupMembership =
                            (GroupMembershipDataItem) dataItem;
                    Long groupId = groupMembership.getGroupRowId();
                    if (groupId != null) {
                        handleGroupMembership(groups, mContactData.getGroupMetaData(), groupId);
                    }
                    continue;
                }
                /** M: New Feature  @{ */
                String mimeType = dataItem.getMimeType();
                String accountType = dataItem.getAccountType().accountType;
                AccountType type1 = dataItem.getAccountType();
                /** @} */
                
                /*
                 * New Feature by Mediatek Begin.
                 *   Original Android's code:
                 *     final DataKind kind = dataItem.getDataKind();
                 *   CR ID: ALPS00308657
                 *   Descriptions: RCS
                 */
                DataKind kind = dataItem.getDataKind();
                /** M: New Feature RCS @{ */
                boolean result = ExtensionManager.getInstance().getContactDetailExtension()
                        .getExtentionKind(mimeType, false, null, ExtensionManager.COMMD_FOR_RCS);
                if (result) {
                    kind = new DataKind(mimeType, 0, 10, false, R.layout.text_fields_editor_view);
                    kind.actionBody = new SimpleInflater(Data.DATA1);
                    kind.titleRes = 0;

                } 
                String mExtentionMimeType = null;
                boolean pluginStatus = ExtensionManager.getInstance().getContactDetailExtension()
                        .checkPluginSupport(ExtensionManager.COMMD_FOR_RCS);
                if (pluginStatus) {
                    mExtentionMimeType = ExtensionManager.getInstance().getContactDetailExtension()
                            .getExtentionMimeType(ExtensionManager.COMMD_FOR_RCS);
                    Log.i(TAG, " THE mExtentionMimeType : " + mExtentionMimeType);
                }
    /** @} */
                ///M: for SNS plugin @{
                if (ExtensionManager
                        .getInstance()
                        .getContactDetailExtension()
                        .isMimeTypeSupported(mimeType,
                                ExtensionManager.COMMD_FOR_SNS,
                                ExtensionManager.COMMD_FOR_SNS)) {
                    kind.resourcePackageName = type1.getDisplayLabel(mContext)
                            .toString();
                }
                ///@}

                /*
                 * New Feature by Mediatek End.
                 */
                if (kind == null) {
                    continue;
                }

                final DetailViewEntry entry = DetailViewEntry.fromValues(mContext, dataItem,
                        mContactData.isDirectoryEntry(), mContactData.getDirectoryId());
                entry.maxLines = kind.maxLinesForDisplay;
                /*
                 * New Feature by Mediatek Begin.            
                 * set this telephone number's sim id bound to sim card        
                 */
                if (!mContactData.isDirectoryEntry()) {
                    entry.simId = dataItem.getContentValues().getAsInteger(Data.SIM_ASSOCIATION_ID);
                }
                /*
                 * New Feature  by Mediatek End.
                */
                final boolean hasData = !TextUtils.isEmpty(entry.data);
                final boolean isSuperPrimary = dataItem.isSuperPrimary();

                if (dataItem instanceof StructuredNameDataItem) {
                    // Always ignore the name. It is shown in the header if set
                } else if (dataItem instanceof PhoneDataItem && hasData) {
                    PhoneDataItem phone = (PhoneDataItem) dataItem;
                    // Build phone entries
                    entry.data = phone.getFormattedPhoneNumber();
                    final Intent phoneIntent = mHasPhone ?
                            ContactsUtils.getCallIntent(entry.data) : null;
                    final Intent smsIntent = mHasSms ? new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts(Constants.SCHEME_SMSTO, entry.data, null)) : null;

                    // Configure Icons and Intents.
                    if (mHasPhone && mHasSms) {
                        entry.intent = phoneIntent;
                        entry.secondaryIntent = smsIntent;
                        entry.secondaryActionIcon = kind.iconAltRes;
                        entry.secondaryActionDescription = kind.iconAltDescriptionRes;
                    } else if (mHasPhone) {
                        entry.intent = phoneIntent;
                    } else if (mHasSms) {
                        entry.intent = smsIntent;
                    } else {
                        entry.intent = null;
                    }

                    // Remember super-primary phone
                    if (isSuperPrimary) mPrimaryPhoneUri = entry.uri;

                    entry.isPrimary = isSuperPrimary;

                    /** M:AAS @ { */
                    entry.mAccountType = accountType;
                    if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(accountType,
                            ExtensionManager.COMMD_FOR_AAS)
                            && SimUtils.isAdditionalNumber(dataItem.getContentValues())) {
                        entry.mIsAnr = true;
                        mAnrEntries.add(entry);
                    } else {
                        entry.mIsAnr = false;
                        /** M: @ } */
                        // If the entry is a primary entry, then render it first in the view.
                        if (entry.isPrimary) {
                            // add to beginning of list so that this phone number shows up first
                            mPhoneEntries.add(0, entry);
                        } else {
                            // add to end of list
                            mPhoneEntries.add(entry);
                        }
                    }
                } else if (dataItem instanceof EmailDataItem && hasData) {
                    // Build email entries
                    entry.intent = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts(Constants.SCHEME_MAILTO, entry.data, null));
                    entry.isPrimary = isSuperPrimary;
                    // If entry is a primary entry, then render it first in the view.
                    if (entry.isPrimary) {
                        mEmailEntries.add(0, entry);
                    } else {
                        mEmailEntries.add(entry);
                    }

                    // When Email rows have status, create additional Im row
                    final DataStatus status = mContactData.getStatuses().get(entry.id);
                    if (status != null) {
                        EmailDataItem email = (EmailDataItem) dataItem;
                        ImDataItem im = ImDataItem.createFromEmail(email);

                        final DetailViewEntry imEntry = DetailViewEntry.fromValues(mContext, im,
                                mContactData.isDirectoryEntry(), mContactData.getDirectoryId());
                        buildImActions(mContext, imEntry, im);
                        imEntry.setPresence(status.getPresence());
                        imEntry.maxLines = kind.maxLinesForDisplay;
                        mImEntries.add(imEntry);
                    }
                } else if (dataItem instanceof StructuredPostalDataItem && hasData) {
                    // Build postal entries
                    entry.intent = StructuredPostalUtils.getViewPostalAddressIntent(entry.data);
                    mPostalEntries.add(entry);
                } else if (dataItem instanceof ImDataItem && hasData) {
                    // Build IM entries
                    buildImActions(mContext, entry, (ImDataItem) dataItem);

                    // Apply presence when available
                    final DataStatus status = mContactData.getStatuses().get(entry.id);
                    if (status != null) {
                        entry.setPresence(status.getPresence());
                    }
                    mImEntries.add(entry);
                } else if (dataItem instanceof OrganizationDataItem) {
                    // Organizations are not shown. The first one is shown in the header
                    // and subsequent ones are not supported anymore
                } else if (dataItem instanceof NicknameDataItem && hasData) {
                    // Build nickname entries
                    final boolean isNameRawContact =
                        (mContactData.getNameRawContactId() == rawContactId);

                    final boolean duplicatesTitle =
                        isNameRawContact
                        && mContactData.getDisplayNameSource() == DisplayNameSources.NICKNAME;

                    if (!duplicatesTitle) {
                        entry.uri = null;
                        mNicknameEntries.add(entry);
                    }
                } else if (dataItem instanceof NoteDataItem && hasData) {
                    // Build note entries
                    entry.uri = null;
                    mNoteEntries.add(entry);
                } else if (dataItem instanceof WebsiteDataItem && hasData) {
                    // Build Website entries
                    entry.uri = null;
                    try {
                        WebAddress webAddress = new WebAddress(entry.data);
                        entry.intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(webAddress.toString()));
                    } catch (ParseException e) {
                        Log.e(TAG, "Couldn't parse website: " + entry.data);
                    }
                    mWebsiteEntries.add(entry);
                } else if (dataItem instanceof SipAddressDataItem && hasData) {
                    // Build SipAddress entries
                    entry.uri = null;
                    if (mHasSip) {
                        entry.intent = ContactsUtils.getCallIntent(
                                Uri.fromParts(Constants.SCHEME_SIP, entry.data, null));
                    } else {
                        entry.intent = null;
                    }
                    mSipEntries.add(entry);
                    // TODO: Now that SipAddress is in its own list of entries
                    // (instead of grouped in mOtherEntries), consider
                    // repositioning it right under the phone number.
                    // (Then, we'd also update FallbackAccountType.java to set
                    // secondary=false for this field, and tweak the weight
                    // of its DataKind.)
                } else if (dataItem instanceof EventDataItem && hasData) {
                    entry.data = DateUtils.formatDate(mContext, entry.data);
                    entry.uri = null;
                    mEventEntries.add(entry);
                } else if (dataItem instanceof RelationDataItem && hasData) {
                    entry.intent = new Intent(Intent.ACTION_SEARCH);
                    entry.intent.putExtra(SearchManager.QUERY, entry.data);
                    entry.intent.setType(Contacts.CONTENT_TYPE);
                    mRelationEntries.add(entry);
                } 
                
                /*
                 * New Feature by Mediatek Begin. Original Android's code: CR
                 * ID: ALPS00308657 Descriptions: RCS
                 */
                else if (mExtentionMimeType != null
                        && mExtentionMimeType.equals(mimeType)
                        && ExtensionManager.getInstance().getContactDetailExtension()
                                .checkPluginSupport(ExtensionManager.COMMD_FOR_RCS)) {
                    int im = dataItem.getContentValues().getAsInteger(Data.DATA5);
                    int ft = dataItem.getContentValues().getAsInteger(Data.DATA6);
                    Log.i(TAG, "im,ft : " + im + " | " + ft);
                    Intent intent = ExtensionManager.getInstance().getContactDetailExtension()
                            .getExtentionIntent(im, ft, ExtensionManager.COMMD_FOR_RCS);
                    if (intent != null) {
                        String name = null;
                        if (mContactData != null) {
                            name = mContactData.getDisplayName();
                        }
                        intent.putExtra(RCS_DISPLAY_NAME, name);
                        intent.putExtra(RCS_PHONE_NUMBER, entry.data);
                        Log.i(TAG, "entry.data : " + entry.data + " | name :" + name
                                + " | mContactData : " + mContactData);
                        if (entry.data != null) {
                            entry.intent = intent;
                        }
                    }
                    mExtentionEntries.add(entry);
                }
                ///M: for SNS plugin @{
                else if (ExtensionManager
                        .getInstance()
                        .getContactDetailExtension()
                        .isMimeTypeSupported(mimeType,
                                ExtensionManager.COMMD_FOR_SNS,
                                ExtensionManager.COMMD_FOR_SNS)
                        && hasData) {
                    buildSNSEntity(rawContact.getValues(), dataItem.getContentValues(), entry, type1, kind,
                            accountType, dataItem);
                }
                ///@}
                /*
                 * New Feature by Mediatek End.
                 */
                
                else {
                    // Handle showing custom rows
                    entry.intent = new Intent(Intent.ACTION_VIEW);
                    entry.intent.setDataAndType(entry.uri, entry.mimetype);

                    entry.data = dataItem.buildDataString();

                    if (!TextUtils.isEmpty(entry.data)) {
                        // If the account type exists in the hash map, add it as another entry for
                        // that account type
                        AccountType type = dataItem.getAccountType();
                        if (mOtherEntriesMap.containsKey(type)) {
                            List<DetailViewEntry> listEntries = mOtherEntriesMap.get(type);
                            listEntries.add(entry);
                        } else {
                            // Otherwise create a new list with the entry and add it to the hash map
                            List<DetailViewEntry> listEntries = new ArrayList<DetailViewEntry>();
                            listEntries.add(entry);
                            mOtherEntriesMap.put(type, listEntries);
                        }
                    }
                }
            }
        }

        if (!groups.isEmpty()) {
            DetailViewEntry entry = new DetailViewEntry();
            Collections.sort(groups);
            StringBuilder sb = new StringBuilder();
            int size = groups.size();
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(groups.get(i));
            }
            entry.mimetype = GroupMembership.MIMETYPE;
            entry.kind = mContext.getString(R.string.groupsLabel);
            /** M: New Feature Easy Poring @{ */
            // original code entry.data = sb.toString();
            entry.data = ExtensionManager.getInstance().getContactDetailExtension().setSPChar(sb.toString(),
                    ContactPluginDefault.COMMD_FOR_OP01);
    /** @} */
            mGroupEntries.add(entry);
        }
    }

    /**
     * Collapse all contact detail entries into one aggregated list with a {@link HeaderViewEntry}
     * at the top.
     */
    private void setupFlattenedList() {
        // All contacts should have a header view (even if there is no data for the contact).
        mAllEntries.add(new HeaderViewEntry());

        addPhoneticName();

        /** M:AAS @ { */
        mIsPhoneEntriesNull = true;
        flattenList(mPhoneEntries);
        flattenList(mAnrEntries);
        /** M: @ } */
        flattenList(mSmsEntries);
        flattenList(mEmailEntries);
        flattenList(mImEntries);
        flattenList(mNicknameEntries);
        flattenList(mWebsiteEntries);

        addNetworks();

        flattenList(mSipEntries);
        flattenList(mPostalEntries);
        flattenList(mEventEntries);
        flattenList(mGroupEntries);
        flattenList(mRelationEntries);
        flattenList(mNoteEntries);
        
        /*
         * New Feature by Mediatek Begin.
         *   Original Android's code:
         *     
         *   CR ID: ALPS00308657
         *   Descriptions: RCS
         */
        flattenList(mExtentionEntries);
        /*
         * New Feature by Mediatek End.
         */

        ///M: for SNS plugin @{
        flattenList(mSNSEntries);
        ///@}
    }

    /**
     * Add phonetic name (if applicable) to the aggregated list of contact details. This has to be
     * done manually because phonetic name doesn't have a mimetype or action intent.
     */
    private void addPhoneticName() {
        String phoneticName = ContactDetailDisplayUtils.getPhoneticName(mContext, mContactData);
        if (TextUtils.isEmpty(phoneticName)) {
            return;
        }

        // Add a title
        String phoneticNameKindTitle = mContext.getString(R.string.name_phonetic);
        mAllEntries.add(new KindTitleViewEntry(phoneticNameKindTitle.toUpperCase()));

        // Add the phonetic name
        final DetailViewEntry entry = new DetailViewEntry();
        entry.kind = phoneticNameKindTitle;
        entry.data = phoneticName;
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: xxx CR ID:
         * ALPS00246021 Descriptions:
         */
        /** M: Change Feature Easy Porting @{ */
        entry.data = ExtensionManager.getInstance().getContactDetailExtension().setSPChar(
                phoneticName, ContactPluginDefault.COMMD_FOR_OP01);
        /** @} */
        /*
         * Bug Fix by Mediatek End.
         */
        mAllEntries.add(entry);
    }

    /**
     * Add attribution and other third-party entries (if applicable) under the "networks" section
     * of the aggregated list of contact details. This has to be done manually because the
     * attribution does not have a mimetype and the third-party entries don't have actually belong
     * to the same {@link DataKind}.
     */
    private void addNetworks() {
        String attribution = ContactDetailDisplayUtils.getAttribution(mContext, mContactData);
        boolean hasAttribution = !TextUtils.isEmpty(attribution);
        int networksCount = mOtherEntriesMap.keySet().size();

        // Note: invitableCount will always be 0 for me profile.  (ContactLoader won't set
        // invitable types for me profile.)
        int invitableCount = mContactData.getInvitableAccountTypes().size();
        if (!hasAttribution && networksCount == 0 && invitableCount == 0) {
            return;
        }

        // Add a title
        String networkKindTitle = mContext.getString(R.string.connections);
        mAllEntries.add(new KindTitleViewEntry(networkKindTitle.toUpperCase()));

        // Add the attribution if applicable
        if (hasAttribution) {
            final DetailViewEntry entry = new DetailViewEntry();
            entry.kind = networkKindTitle;
            entry.data = attribution;
            mAllEntries.add(entry);

            // Add a divider below the attribution if there are network details that will follow
            if (networksCount > 0) {
                mAllEntries.add(new SeparatorViewEntry());
            }
        }

        // Add the other entries from third parties
        for (AccountType accountType : mOtherEntriesMap.keySet()) {

            // Add a title for each third party app
            mAllEntries.add(new NetworkTitleViewEntry(mContext, accountType));

            for (DetailViewEntry detailEntry : mOtherEntriesMap.get(accountType)) {
                // Add indented separator
                SeparatorViewEntry separatorEntry = new SeparatorViewEntry();
                separatorEntry.setIsInSubSection(true);
                mAllEntries.add(separatorEntry);

                // Add indented detail
                detailEntry.setIsInSubSection(true);
                mAllEntries.add(detailEntry);
            }
        }

        mOtherEntriesMap.clear();

        // Add the "More networks" button, which opens the invitable account type list popup.
        if (invitableCount > 0) {
            addMoreNetworks();
        }
    }

    /**
     * Add the "More networks" entry.  When clicked, show a popup containing a list of invitable
     * account types.
     */
    private void addMoreNetworks() {
        // First, prepare for the popup.

        // Adapter for the list popup.
        final InvitableAccountTypesAdapter popupAdapter = new InvitableAccountTypesAdapter(mContext,
                mContactData);

        // Listener called when a popup item is clicked.
        final AdapterView.OnItemClickListener popupItemListener
                = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                if (mListener != null && mContactData != null) {
                    mListener.onItemClicked(ContactsUtils.getInvitableIntent(
                            popupAdapter.getItem(position) /* account type */,
                            mContactData.getLookupUri()));
                }
            }
        };

        // Then create the click listener for the "More network" entry.  Open the popup.
        View.OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showListPopup(v, popupAdapter, popupItemListener);
            }
        };

        // Finally create the entry.
        mAllEntries.add(new AddConnectionViewEntry(mContext, onClickListener));
    }

    /**
     * Iterate through {@link DetailViewEntry} in the given list and add it to a list of all
     * entries. Add a {@link KindTitleViewEntry} at the start if the length of the list is not 0.
     * Add {@link SeparatorViewEntry}s as dividers as appropriate. Clear the original list.
     */
    private void flattenList(ArrayList<DetailViewEntry> entries) {
        int count = entries.size();

        // Add a title for this kind by extracting the kind from the first entry
        if (count > 0) {
            String kind = entries.get(0).kind;

            /** M:AAS update views data @ { original code:
            mAllEntries.add(new KindTitleViewEntry(kind.toUpperCase()));*/
            DetailViewEntry entry = entries.get(0);
            String mimeType = entry.mimetype;

            if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(
                    entry.mAccountType, ExtensionManager.COMMD_FOR_AAS)
                    && ExtensionManager.getInstance().getContactAccountExtension().isPhone(
                            mimeType, ExtensionManager.COMMD_FOR_AAS)) {
                String separatorTitle;
                if (entry.mIsAnr) {
                    if (mIsPhoneEntriesNull) {
                        mAllEntries.add(new KindTitleViewEntry(kind.toUpperCase()));
                    }
                    separatorTitle = ExtensionManager.getInstance().getContactDetailExtension().repChar(null, (char) 0,
                            (char) 0, (char) 0, ContactDetailExtension.STRING_ADDITINAL, ExtensionManager.COMMD_FOR_AAS);
                    mAllEntries.add(new KindSubTitleViewEntry(separatorTitle));
                } else {
                    mIsPhoneEntriesNull = entries.isEmpty() ? true : false;
                    separatorTitle = ExtensionManager.getInstance().getContactDetailExtension().repChar(null, (char) 0,
                            (char) 0, (char) 0, ContactDetailExtension.STRING_PRIMART, ExtensionManager.COMMD_FOR_AAS);
                    mAllEntries.add(new KindTitleViewEntry(kind.toUpperCase()));
                    mAllEntries.add(new KindSubTitleViewEntry(separatorTitle));
                }
            } else {
                mAllEntries.add(new KindTitleViewEntry(kind.toUpperCase()));
            }
            /** M: @ } */
        }

        // Add all the data entries for this kind
        for (int i = 0; i < count; i++) {
            // For all entries except the first one, add a divider above the entry
            if (i != 0) {
                mAllEntries.add(new SeparatorViewEntry());
            }
            mAllEntries.add(entries.get(i));
        }

        // Clear old list because it's not needed anymore.
        entries.clear();
    }

    /**
     * Maps group ID to the corresponding group name, collapses all synonymous groups.
     * Ignores default groups (e.g. My Contacts) and favorites groups.
     */
    private void handleGroupMembership(
            ArrayList<String> groups, List<GroupMetaData> groupMetaData, long groupId) {
        if (groupMetaData == null) {
            return;
        }

        for (GroupMetaData group : groupMetaData) {
            if (group.getGroupId() == groupId) {
                if (!group.isDefaultGroup() && !group.isFavorites()) {
                    String title = group.getTitle();
                    if (!TextUtils.isEmpty(title) && !groups.contains(title)) {
                        groups.add(title);
                    }
                }
                break;
            }
        }
    }

    private static String buildDataString(DataKind kind, ContentValues values,
            Context context) {
        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(context, values);
        return actionBody == null ? null : actionBody.toString();
    }

    /**
     * Writes the Instant Messaging action into the given entry value.
     */
    @VisibleForTesting
    public static void buildImActions(Context context, DetailViewEntry entry,
            ImDataItem im) {
        final boolean isEmail = im.isCreatedFromEmail();

        if (!isEmail && !im.isProtocolValid()) {
            return;
        }

        final String data = im.getData();
        if (TextUtils.isEmpty(data)) {
            return;
        }

        final int protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK : im.getProtocol();

        if (protocol == Im.PROTOCOL_GOOGLE_TALK) {
            final int chatCapability = im.getChatCapability();
            entry.chatCapability = chatCapability;
            entry.typeString = Im.getProtocolLabel(context.getResources(), Im.PROTOCOL_GOOGLE_TALK,
                    null).toString();
            /*
             * Bug Fix by Mediatek Begin. Original Android's code: xxx CR ID:
             * ALPS00246021 Descriptions:
             */
    /** M: New Feature Easy Porting @{ */
            entry.typeString = ExtensionManager.getInstance().getContactDetailExtension().setSPChar(Im
                    .getProtocolLabel(context.getResources(), Im.PROTOCOL_GOOGLE_TALK,
                            null).toString(), ContactPluginDefault.COMMD_FOR_OP01);

    /** @} */       
            /*
             * Bug Fix by Mediatek End.
             */

            if ((chatCapability & Im.CAPABILITY_HAS_CAMERA) != 0) {
                entry.intent =
                        new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?message"));
                entry.secondaryIntent =
                        new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?call"));
            } else if ((chatCapability & Im.CAPABILITY_HAS_VOICE) != 0) {
                // Allow Talking and Texting
                entry.intent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?message"));
                entry.secondaryIntent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?call"));
            } else {
                entry.intent =
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("xmpp:" + data + "?message"));
            }
        } else {
            // Build an IM Intent
            String host = im.getCustomProtocol();

            if (protocol != Im.PROTOCOL_CUSTOM) {
                // Try bringing in a well-known host for specific protocols
                host = ContactsUtils.lookupProviderNameFromId(protocol);
            }

            if (!TextUtils.isEmpty(host)) {
                final String authority = host.toLowerCase();
                final Uri imUri = new Uri.Builder().scheme(Constants.SCHEME_IMTO).authority(
                        authority).appendPath(data).build();
                entry.intent = new Intent(Intent.ACTION_SENDTO, imUri);
            }
        }
    }

    /**
     * Show a list popup.  Used for "popup-able" entry, such as "More networks".
     */
    private void showListPopup(View anchorView, ListAdapter adapter,
            final AdapterView.OnItemClickListener onItemClickListener) {
        dismissPopupIfShown();
        mPopup = new ListPopupWindow(mContext, null);
        mPopup.setAnchorView(anchorView);
        mPopup.setWidth(anchorView.getWidth());
        mPopup.setAdapter(adapter);
        mPopup.setModal(true);

        // We need to wrap the passed onItemClickListener here, so that we can dismiss() the
        // popup afterwards.  Otherwise we could directly use the passed listener.
        mPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                onItemClickListener.onItemClick(parent, view, position, id);
                dismissPopupIfShown();
            }
        });
        mPopup.show();
    }

    private void dismissPopupIfShown() {
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
        mPopup = null;
    }

    /**
     * Base class for an item in the {@link ViewAdapter} list of data, which is
     * supplied to the {@link ListView}.
     */
    static class ViewEntry {
        private final int viewTypeForAdapter;
        protected long id = -1;
        /** Whether or not the entry can be focused on or not. */
        protected boolean isEnabled = false;

        ViewEntry(int viewType) {
            viewTypeForAdapter = viewType;
        }

        int getViewType() {
            return viewTypeForAdapter;
        }

        long getId() {
            return id;
        }

        boolean isEnabled() {
            return isEnabled;
        }

        /**
         * Called when the entry is clicked.  Only {@link #isEnabled} entries can get clicked.
         *
         * @param clickedView  {@link View} that was clicked  (Used, for example, as the anchor view
         *        for a popup.)
         * @param fragmentListener  {@link Listener} set to {@link ContactDetailFragment}
         */
        public void click(View clickedView, Listener fragmentListener) {
        }
    }

    /**
     * Header item in the {@link ViewAdapter} list of data.
     */
    private static class HeaderViewEntry extends ViewEntry {

        HeaderViewEntry() {
            super(ViewAdapter.VIEW_TYPE_HEADER_ENTRY);
        }

    }

    /**
     * Separator between items of the same {@link DataKind} in the
     * {@link ViewAdapter} list of data.
     */
    private static class SeparatorViewEntry extends ViewEntry {

        /**
         * Whether or not the entry is in a subsection (if true then the contents will be indented
         * to the right)
         */
        private boolean mIsInSubSection = false;

        SeparatorViewEntry() {
            super(ViewAdapter.VIEW_TYPE_SEPARATOR_ENTRY);
        }

        public void setIsInSubSection(boolean isInSubSection) {
            mIsInSubSection = isInSubSection;
        }

        public boolean isInSubSection() {
            return mIsInSubSection;
        }

    }

    /**
     * Title entry for items of the same {@link DataKind} in the
     * {@link ViewAdapter} list of data.
     */
    private static class KindTitleViewEntry extends ViewEntry {

        private final String mTitle;

        KindTitleViewEntry(String titleText) {
            super(ViewAdapter.VIEW_TYPE_KIND_TITLE_ENTRY);
            mTitle = titleText;
        }

        public String getTitle() {
            return mTitle;
        }
    }

    /**
     * M:AAS Title entry for items of the same {@link DataKind} in the {@link ViewAdapter} list of
     * data. @ {
     */
    static class KindSubTitleViewEntry extends ViewEntry {

        private final String mTitle;

        KindSubTitleViewEntry(String titleText) {
            super(ViewAdapter.VIEW_TYPE_SUB_KIND_TITLE_ENTRY);
            mTitle = titleText;
        }

        public String getTitle() {
            return mTitle;
        }
    }
    /** M: @ } */

    /**
     * A title for a section of contact details from a single 3rd party network.
     */
    private static class NetworkTitleViewEntry extends ViewEntry {
        private final Drawable mIcon;
        private final CharSequence mLabel;

        public NetworkTitleViewEntry(Context context, AccountType type) {
            super(ViewAdapter.VIEW_TYPE_NETWORK_TITLE_ENTRY);
            this.mIcon = type.getDisplayIcon(context);
            this.mLabel = type.getDisplayLabel(context);
            this.isEnabled = false;
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public CharSequence getLabel() {
            return mLabel;
        }
    }

    /**
     * This is used for the "Add Connections" entry.
     */
    private static class AddConnectionViewEntry extends ViewEntry {
        private final Drawable mIcon;
        private final CharSequence mLabel;
        private final View.OnClickListener mOnClickListener;

        private AddConnectionViewEntry(Context context, View.OnClickListener onClickListener) {
            super(ViewAdapter.VIEW_TYPE_ADD_CONNECTION_ENTRY);
            this.mIcon = context.getResources().getDrawable(
                    R.drawable.ic_menu_add_field_holo_light);
            this.mLabel = context.getString(R.string.add_connection_button);
            this.mOnClickListener = onClickListener;
            this.isEnabled = true;
        }

        @Override
        public void click(View clickedView, Listener fragmentListener) {
            if (mOnClickListener == null) return;
            mOnClickListener.onClick(clickedView);
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public CharSequence getLabel() {
            return mLabel;
        }
    }

    /**
     * An item with a single detail for a contact in the {@link ViewAdapter}
     * list of data.
     */
    static class DetailViewEntry extends ViewEntry implements Collapsible<DetailViewEntry> {
        // TODO: Make getters/setters for these fields
        public int type = -1;
        public String kind;
        public String typeString;
        public String data;
        public Uri uri;
        public int maxLines = 1;
        public String mimetype;

        public Context context = null;
        public boolean isPrimary = false;
        public int secondaryActionIcon = -1;
        public int secondaryActionDescription = -1;
        public Intent intent;
        public Intent secondaryIntent = null;
        public ArrayList<Long> ids = new ArrayList<Long>();
        public int collapseCount = 0;

        public int presence = -1;
        public int chatCapability = 0;

        private boolean mIsInSubSection = false;

        ///M: for SNS plugin @{
        private boolean mIsSNSStatus = false;
        private Bundle mData = null;
        /// @}

        /*
         * New Feature by Mediatek Begin.            
         * save the association's sim id 
         */
        public int simId = -1;
        /*
         * New Feature  by Mediatek End.
         */ 
        /*
         * New Feature by Mediatek Begin.            
         * save the association's sim id 
         */
        public boolean mIsAnr = false;
        public String mAccountType = "";
        /*
         * New Feature  by Mediatek End.
         */ 

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("== DetailViewEntry ==\n");
            sb.append("  type: " + type + "\n");
            sb.append("  kind: " + kind + "\n");
            sb.append("  typeString: " + typeString + "\n");
            sb.append("  data: " + data + "\n");
            sb.append("  uri: " + uri.toString() + "\n");
            sb.append("  maxLines: " + maxLines + "\n");
            sb.append("  mimetype: " + mimetype + "\n");
            sb.append("  isPrimary: " + (isPrimary ? "true" : "false") + "\n");
            sb.append("  secondaryActionIcon: " + secondaryActionIcon + "\n");
            sb.append("  secondaryActionDescription: " + secondaryActionDescription + "\n");
            if (intent == null) {
                sb.append("  intent: " + intent.toString() + "\n");
            } else {
                sb.append("  intent: " + intent.toString() + "\n");
            }
            if (secondaryIntent == null) {
                sb.append("  secondaryIntent: (null)\n");
            } else {
                sb.append("  secondaryIntent: " + secondaryIntent.toString() + "\n");
            }
            sb.append("  ids: " + Iterables.toString(ids) + "\n");
            sb.append("  collapseCount: " + collapseCount + "\n");
            sb.append("  presence: " + presence + "\n");
            sb.append("  chatCapability: " + chatCapability + "\n");
            sb.append("  mIsInSubsection: " + (mIsInSubSection ? "true" : "false") + "\n");
            return sb.toString();
        }

        DetailViewEntry() {
            super(ViewAdapter.VIEW_TYPE_DETAIL_ENTRY);
            isEnabled = true;
        }

        /**
         * Build new {@link DetailViewEntry} and populate from the given values.
         */
        public static DetailViewEntry fromValues(Context context, DataItem item,
                boolean isDirectoryEntry, long directoryId) {
            final DetailViewEntry entry = new DetailViewEntry();
            entry.id = item.getId();
            entry.context = context;
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            if (isDirectoryEntry) {
                entry.uri = entry.uri.buildUpon().appendQueryParameter(
                        ContactsContract.DIRECTORY_PARAM_KEY, String.valueOf(directoryId)).build();
            }
            entry.mimetype = item.getMimeType();
            entry.kind = item.getKindString();
            /** M: New Feature Easy Porting @{ */
            // original code entry.data = buildDataString(kind, values,
            // context);
            String tempData = item.buildDataString();
            Log.d(TAG, "fromValues(), kind.mimeType= " + item.getMimeType());
            Log.i(TAG, "fromValues(),tempData = " + tempData);
            if (item.getMimeType() == SipAddress.CONTENT_ITEM_TYPE && tempData != null) {
                Log.i(TAG, "SipAddress.CONTENT_ITEM_TYPE tempData = " + tempData);
                entry.data = ExtensionManager.getInstance().getContactDetailExtension().setSPChar(
                        tempData, ContactPluginDefault.COMMD_FOR_OP01);
            } else if (item.getMimeType() == Nickname.CONTENT_ITEM_TYPE && tempData != null) {
                Log.i(TAG, "Nickname.CONTENT_ITEM_TYPE tempData = " + tempData);
                entry.data = ExtensionManager.getInstance().getContactDetailExtension().setSPChar(
                        tempData, ContactPluginDefault.COMMD_FOR_OP01);
            } else {
                entry.data = tempData;
            }
            /** @} */

            if (item.hasKindTypeColumn()) {
                entry.type = item.getKindTypeColumn();

                // get type string
                entry.typeString = "";
                /** M:AAS @ { */
                if (SimUtils.isAasPhoneType(entry.type)) {
                    int slotId = ExtensionManager.getInstance().getContactAccountExtension()
                            .getCurrentSlot(ExtensionManager.COMMD_FOR_AAS);
                    entry.typeString = (String) ExtensionManager.getInstance()
                            .getContactAccountExtension().getTypeLabel(
                                    context.getResources(), entry.type,
                                    item.getContentValues().getAsString(Data.DATA3), slotId,
                                    ExtensionManager.COMMD_FOR_AAS);
                } else {
                    /** M: @ } */
                    for (EditType type : item.getDataKind().typeList) {
                        if (type.rawValue == entry.type) {
                            /** M: New Feature Easy Porting @{ */
                            /*
                             * orignal code if (type.customColumn == null) { //
                             * Non-custom type. Get its description from the
                             * resource entry.typeString =
                             * context.getString(type.labelRes); } else { //
                             * Custom type. Read it from the database
                             * entry.typeString =
                             * values.getAsString(type.customColumn); }
                             */
                            if (type.customColumn == null) {
                                // Non-custom type. Get its description from the
                                // resource
                                if (isUnSync(item.getMimeType(), item.getContentValues())) {
                                    Log.i(TAG, "isUnSync is true item.getMimeType() : " + item.getMimeType()
                                            + " | context.getString(type.labelRes) : "
                                            + context.getString(type.labelRes));

                                    entry.typeString = ExtensionManager.getInstance()
                                            .getContactDetailExtension().setSPChar(
                                                    context.getString(type.labelRes),
                                                    ContactPluginDefault.COMMD_FOR_OP01);

                                } else {
                                    Log.i(TAG, "isUnSync is false kind.mimeType : " + item.getMimeType()
                                            + " | context.getString(type.labelRes) : "
                                            + context.getString(type.labelRes));
                                    entry.typeString = context.getString(type.labelRes);
                                }
                            } else {
                                // Custom type. Read it from the database
                                Log.i(TAG, "type.customColumn is not null");

                                entry.typeString = ExtensionManager.getInstance()
                                        .getContactDetailExtension().setSPChar(
                                                item.getContentValues().getAsString(type.customColumn),
                                                ContactPluginDefault.COMMD_FOR_OP01);
                            }
                            break;
                        }
                    }
                    /** @} */
                }
            } else {
                /*
                 * New Feature by Mediatek Begin.
                 *   Original Android's code:
                 *     
                 *   CR ID: ALPS00308657
                 *   Descriptions: RCS
                 */
                boolean pluginStatus = ExtensionManager.getInstance().getContactDetailExtension()
                        .checkPluginSupport(ExtensionManager.COMMD_FOR_RCS);
                entry.typeString = null;
                if (pluginStatus) {
                    entry.typeString = ExtensionManager.getInstance().getContactDetailExtension()
                            .getExtensionTitles(entry.data, item.getMimeType(), null,
                                    mPhoneAndSubtitle, ExtensionManager.COMMD_FOR_RCS);
                    entry.kind = ExtensionManager.getInstance().getContactDetailExtension()
                            .getExtensionTitles(null, item.getMimeType(), entry.kind, null,
                                    ExtensionManager.COMMD_FOR_RCS);
                }
                /*
                 * New Feature by Mediatek End.
                 */

                ///M: for SNS plugin @{
                if (ExtensionManager
                        .getInstance()
                        .getContactDetailExtension()
                        .isMimeTypeSupported(item.getMimeType(),
                                ExtensionManager.COMMD_FOR_SNS,
                                ExtensionManager.COMMD_FOR_SNS)) {
                    entry.kind = item.getDataKind().resourcePackageName;
                }
                ///@}
            }
            
            /*
             * New Feature by Mediatek Begin.
             *   Original Android's code:
             *     
             *   CR ID: ALPS00308657
             *   Descriptions: RCS
             */
            if (item.getMimeType().equals(Phone.CONTENT_ITEM_TYPE)) {
                mPhoneAndSubtitle.put(entry.data, entry.typeString);
            }
            /*
             * New Feature by Mediatek End.
             */
            
            
            Log.i(TAG, "return entry = " + entry.typeString);
            Log.i(TAG, "return entry.data = " + entry.data);
            return entry;
        }

        public void setPresence(int presence) {
            this.presence = presence;
        }

        public void setIsInSubSection(boolean isInSubSection) {
            mIsInSubSection = isInSubSection;
        }

        public boolean isInSubSection() {
            return mIsInSubSection;
        }

        @Override
        public boolean collapseWith(DetailViewEntry entry) {
            // assert equal collapse keys
            if (!shouldCollapseWith(entry)) {
                return false;
            }

            // Choose the label associated with the highest type precedence.
            if (TypePrecedence.getTypePrecedence(mimetype, type)
                    > TypePrecedence.getTypePrecedence(entry.mimetype, entry.type)) {
                type = entry.type;
                kind = entry.kind;
                typeString = entry.typeString;
            }

            // Choose the max of the maxLines and maxLabelLines values.
            maxLines = Math.max(maxLines, entry.maxLines);

            // Choose the presence with the highest precedence.
            if (StatusUpdates.getPresencePrecedence(presence)
                    < StatusUpdates.getPresencePrecedence(entry.presence)) {
                presence = entry.presence;
            }

            // If any of the collapsed entries are primary make the whole thing primary.
            isPrimary = entry.isPrimary ? true : isPrimary;

            // uri, and contactdId, shouldn't make a difference. Just keep the original.

            // Keep track of all the ids that have been collapsed with this one.
            ids.add(entry.getId());
            collapseCount++;
            return true;
        }

        @Override
        public boolean shouldCollapseWith(DetailViewEntry entry) {
            if (entry == null) {
                return false;
            }

            if (!ContactsUtils.shouldCollapse(mimetype, data, entry.mimetype, entry.data)) {
                return false;
            }

            if (!TextUtils.equals(mimetype, entry.mimetype)
                    || !ContactsUtils.areIntentActionEqual(intent, entry.intent)
                    || !ContactsUtils.areIntentActionEqual(
                            secondaryIntent, entry.secondaryIntent)) {
                return false;
            }

            return true;
        }

        @Override
        public void click(View clickedView, Listener fragmentListener) {
            /// M: for SNS plugin @{
            if (mIsSNSStatus && null != mUpdateClickListener) {
                mUpdateClickListener.scrollUpdateStatus();
                return;
            }
            /// @}

            if (fragmentListener == null || intent == null) return;
            fragmentListener.onItemClicked(intent);
        }
    }

    /**
     * Cache of the children views for a view that displays a header view entry.
     */
    private static class HeaderViewCache {
        public final TextView displayNameView;
        public final TextView companyView;
        public final ImageView photoView;
        public final View photoOverlayView;
        public final ImageView starredView;
        public final int layoutResourceId;

        public HeaderViewCache(View view, int layoutResourceInflated) {
            displayNameView = (TextView) view.findViewById(R.id.name);
            companyView = (TextView) view.findViewById(R.id.company);
            photoView = (ImageView) view.findViewById(R.id.photo);
            photoOverlayView = view.findViewById(R.id.photo_touch_intercept_overlay);
            starredView = (ImageView) view.findViewById(R.id.star);
            layoutResourceId = layoutResourceInflated;
        }

        public void enablePhotoOverlay(OnClickListener listener) {
            if (photoOverlayView != null) {
                photoOverlayView.setOnClickListener(listener);
                photoOverlayView.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class KindTitleViewCache {
        public final TextView titleView;

        public KindTitleViewCache(View view) {
            titleView = (TextView)view.findViewById(R.id.title);
        }
    }

    /**
     * Cache of the children views for a view that displays a {@link NetworkTitleViewEntry}
     */
    private static class NetworkTitleViewCache {
        public final TextView name;
        public final ImageView icon;

        public NetworkTitleViewCache(View view) {
            name = (TextView) view.findViewById(R.id.network_title);
            icon = (ImageView) view.findViewById(R.id.network_icon);
        }
    }

    /**
     * Cache of the children views for a view that displays a {@link AddConnectionViewEntry}
     */
    private static class AddConnectionViewCache {
        public final TextView name;
        public final ImageView icon;
        public final View primaryActionView;

        public AddConnectionViewCache(View view) {
            name = (TextView) view.findViewById(R.id.add_connection_label);
            icon = (ImageView) view.findViewById(R.id.add_connection_icon);
            primaryActionView = view.findViewById(R.id.primary_action_view);
        }
    }

    /**
     * Cache of the children views of a contact detail entry represented by a
     * {@link DetailViewEntry}
     */
    private static class DetailViewCache {
        public final TextView type;
        public final TextView data;
        public final ImageView presenceIcon;
        public final ImageView secondaryActionButton;
        public final View actionsViewContainer;
        public final View primaryActionView;
        public final View secondaryActionViewContainer;
        public final View secondaryActionDivider;
        public final View primaryIndicator;

        /*
         * New Feature by Mediatek Begin.            
         * save the newly view's object 
         */
        public final ImageView imgAssociationSimIcon;
        public final TextView  txtAssociationSimName;       
        public final View      vewVtCallDivider;
        public final View vtcallActionViewContainer;
        public final ImageView btnVtCallAction;

        public final LinearLayout mAssociationSimLayout;
        /*
         * New Feature  by Mediatek End.
        */
        
        /*
         * New Feature by Mediatek Begin. 
         * Original Android's code:
        public DetailViewCache(View view,
                OnClickListener primaryActionClickListener,
                OnClickListener secondaryActionClickListener) {
         * add the vtcall button's onClickListener           
         */
        public DetailViewCache(View view, OnClickListener primaryActionClickListener,
                OnClickListener secondaryActionClickListener,
                OnClickListener vtCallActionClickListener) {
        /*
         * New Feature  by Mediatek End.
         */            
            type = (TextView) view.findViewById(R.id.type);
            data = (TextView) view.findViewById(R.id.data);
            primaryIndicator = view.findViewById(R.id.primary_indicator);
            presenceIcon = (ImageView) view.findViewById(R.id.presence_icon);

            actionsViewContainer = view.findViewById(R.id.actions_view_container);
            actionsViewContainer.setOnClickListener(primaryActionClickListener);
            primaryActionView = view.findViewById(R.id.primary_action_view);

            secondaryActionViewContainer = view.findViewById(
                    R.id.secondary_action_view_container);
            secondaryActionViewContainer.setOnClickListener(
                    secondaryActionClickListener);
            secondaryActionButton = (ImageView) view.findViewById(
                    R.id.secondary_action_button);

            secondaryActionDivider = view.findViewById(R.id.vertical_divider);
            
            /*
             * New Feature by Mediatek Begin.            
             * get the newly view's object  
             */
            vtcallActionViewContainer = view.findViewById(R.id.vtcall_action_view_container);
            /** M: New Feature Phone Landscape UI @{ */
            if (vtcallActionViewContainer != null) {
            /** @ }*/
                vtcallActionViewContainer.setOnClickListener(vtCallActionClickListener); 
            /** M: New Feature Phone Landscape UI @{ */
            }
            /** @ }*/
            imgAssociationSimIcon = (ImageView) view.findViewById(R.id.association_sim_icon);
            txtAssociationSimName = (TextView) view.findViewById(R.id.association_sim_text);   
            vewVtCallDivider = view.findViewById(R.id.vertical_divider_vtcall);
            btnVtCallAction = (ImageView) view.findViewById(R.id.vtcall_action_button);
            mAssociationSimLayout = (LinearLayout)view.findViewById(R.id.association_sim_layout);
            /*
             * New Feature  by Mediatek End.
            */

        }
    }

    private final class ViewAdapter extends BaseAdapter {

        public static final int VIEW_TYPE_DETAIL_ENTRY = 0;
        public static final int VIEW_TYPE_HEADER_ENTRY = 1;
        public static final int VIEW_TYPE_KIND_TITLE_ENTRY = 2;
        public static final int VIEW_TYPE_NETWORK_TITLE_ENTRY = 3;
        public static final int VIEW_TYPE_ADD_CONNECTION_ENTRY = 4;
        public static final int VIEW_TYPE_SEPARATOR_ENTRY = 5;
        /** M:AAS @ { original code: 
        private static final int VIEW_TYPE_COUNT = 6; */
        public static final int VIEW_TYPE_SUB_KIND_TITLE_ENTRY = 6;
        private static final int VIEW_TYPE_COUNT = 7;
        /** M: @ } */

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_HEADER_ENTRY:
                    return getHeaderEntryView(convertView, parent);
                case VIEW_TYPE_SEPARATOR_ENTRY:
                    return getSeparatorEntryView(position, convertView, parent);
                case VIEW_TYPE_KIND_TITLE_ENTRY:
                    return getKindTitleEntryView(position, convertView, parent);
                case VIEW_TYPE_DETAIL_ENTRY:
                    return getDetailEntryView(position, convertView, parent);
                case VIEW_TYPE_NETWORK_TITLE_ENTRY:
                    return getNetworkTitleEntryView(position, convertView, parent);
                case VIEW_TYPE_ADD_CONNECTION_ENTRY:
                    return getAddConnectionEntryView(position, convertView, parent);
                case VIEW_TYPE_SUB_KIND_TITLE_ENTRY:
                    return getSubKindTitleEntryView(position, convertView, parent);
                default:
                    throw new IllegalStateException("Invalid view type ID " +
                            getItemViewType(position));
            }
        }

        private View getHeaderEntryView(View convertView, ViewGroup parent) {
            final int desiredLayoutResourceId = mContactHasSocialUpdates ?
                    R.layout.detail_header_contact_with_updates :
                    R.layout.detail_header_contact_without_updates;
            View result = null;
            HeaderViewCache viewCache = null;

            // Only use convertView if it has the same layout resource ID as the one desired
            // (the two can be different on wide 2-pane screens where the detail fragment is reused
            // for many different contacts that do and do not have social updates).
            if (convertView != null) {
                viewCache = (HeaderViewCache) convertView.getTag();
                if (viewCache.layoutResourceId == desiredLayoutResourceId) {
                    result = convertView;
                }
            }

            // Otherwise inflate a new header view and create a new view cache.
            if (result == null) {
                result = mInflater.inflate(desiredLayoutResourceId, parent, false);
                viewCache = new HeaderViewCache(result, desiredLayoutResourceId);
                result.setTag(viewCache);
            }

            ContactDetailDisplayUtils.setDisplayName(mContext, mContactData,
                    viewCache.displayNameView);
            ContactDetailDisplayUtils.setCompanyName(mContext, mContactData, viewCache.companyView);

            // Set the photo if it should be displayed
            if (viewCache.photoView != null) {
                final boolean expandOnClick = mContactData.getPhotoUri() != null;
                final OnClickListener listener = mPhotoSetter.setupContactPhotoForClick(mContext,
                        mContactData, viewCache.photoView, expandOnClick);
                /** M: New Feature @{ */
                /*
                 * Original Code if (expandOnClick ||
                 * mContactData.isWritableContact(mContext)) {
                 * viewCache.enablePhotoOverlay(listener); }
                 */
                Log.i(TAG, "[getHeaderEntryView] mContactData.getIndicate() : "
                        + mContactData.getIndicate());
                if (mContactData.getIndicate() >= 0) {
                    viewCache.photoView.setClickable(false);
                } else if (expandOnClick || mContactData.isWritableContact(mContext)) {
                    viewCache.enablePhotoOverlay(listener);
                }
                /** @} */

            }

            // Set the starred state if it should be displayed
            final ImageView favoritesStar = viewCache.starredView;
            /** M: New Feature xxx @{ */
            // The favorite star shown or not for tablet should be decided here for ALPS00242811
            if (mContactData.getIndicate() < 0) {
                /** @} */
                if (favoritesStar != null) {
                    favoritesStar.setVisibility(View.VISIBLE);
                    ContactDetailDisplayUtils.configureStarredImageView(favoritesStar, mContactData
                            .isDirectoryEntry(), mContactData.isUserProfile(), mContactData
                            .getStarred());
                    final Uri lookupUri = mContactData.getLookupUri();
                    favoritesStar.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Toggle "starred" state
                            // Make sure there is a contact
                            if (lookupUri != null) {
                                // Read the current starred value from the UI
                                // instead of using the last
                                // loaded state. This allows rapid tapping
                                // without writing the same
                                // value several times
                                final Object tag = favoritesStar.getTag();
                                final boolean isStarred = tag == null ? false
                                        : (Boolean) favoritesStar.getTag();

                                // To improve responsiveness, swap out the
                                // picture (and tag) in the UI
                                // already
                                ContactDetailDisplayUtils.configureStarredImageView(favoritesStar,
                                        mContactData.isDirectoryEntry(), mContactData
                                                .isUserProfile(), !isStarred);

                                // Now perform the real save
                                Intent intent = ContactSaveService.createSetStarredIntent(
                                        getContext(), lookupUri, !isStarred);
                                getContext().startService(intent);
                            }
                        }
                    });
                }
                /** M: New Feature xxx @{ */
            } else {
                // sim detail needn't to show favorite star icon
                if (favoritesStar != null) {
                    favoritesStar.setVisibility(View.GONE);
                }
            }
            /** @} */

            return result;
        }

        private View getSeparatorEntryView(int position, View convertView, ViewGroup parent) {
            final SeparatorViewEntry entry = (SeparatorViewEntry) getItem(position);
            final View result = (convertView != null) ? convertView :
                    mInflater.inflate(R.layout.contact_detail_separator_entry_view, parent, false);

            result.setPadding(entry.isInSubSection() ? mViewEntryDimensions.getWidePaddingLeft() :
                    mViewEntryDimensions.getPaddingLeft(), 0,
                    mViewEntryDimensions.getPaddingRight(), 0);

            return result;
        }

        private View getKindTitleEntryView(int position, View convertView, ViewGroup parent) {
            final KindTitleViewEntry entry = (KindTitleViewEntry) getItem(position);
            final View result;
            final KindTitleViewCache viewCache;

            if (convertView != null) {
                result = convertView;
                viewCache = (KindTitleViewCache)result.getTag();
            } else {
                result = mInflater.inflate(R.layout.list_separator, parent, false);
                viewCache = new KindTitleViewCache(result);
                result.setTag(viewCache);
            }
            /** M: New Feature for ThemeManager @{ */
            // MTK_THEMEMANAGER_APP
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                Resources res = mContext.getResources();
                int textColor = res.getThemeMainColor();
                if (textColor != 0) {
                    viewCache.titleView.setTextColor(textColor);
                }
            }
            // MTK_THEMEMANAGER_APP
            /** @} */
            viewCache.titleView.setText(entry.getTitle());

            return result;
        }

        /** M:AAS @ { */
        private View getSubKindTitleEntryView(int position, View convertView, ViewGroup parent) {
            final KindSubTitleViewEntry entry = (KindSubTitleViewEntry) getItem(position);

            final View result = (convertView != null) ? convertView : mInflater.inflate(
                    R.layout.list_sub_separator, parent, false);
            final TextView titleTextView = (TextView) result.findViewById(R.id.sub_title);

            // MTK_THEMEMANAGER_APP
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                Resources res = mContext.getResources();
                int textColor = res.getThemeMainColor();
                if (textColor != 0) {
                    titleTextView.setTextColor(textColor);
                }
            }
            // MTK_THEMEMANAGER_APP

            titleTextView.setText(entry.getTitle());
            return result;
        }
        /** M: @ } */

        private View getNetworkTitleEntryView(int position, View convertView, ViewGroup parent) {
            final NetworkTitleViewEntry entry = (NetworkTitleViewEntry) getItem(position);
            final View result;
            final NetworkTitleViewCache viewCache;

            if (convertView != null) {
                result = convertView;
                viewCache = (NetworkTitleViewCache) result.getTag();
            } else {
                result = mInflater.inflate(R.layout.contact_detail_network_title_entry_view,
                        parent, false);
                viewCache = new NetworkTitleViewCache(result);
                result.setTag(viewCache);
            }

            viewCache.name.setText(entry.getLabel());
            viewCache.icon.setImageDrawable(entry.getIcon());

            return result;
        }

        private View getAddConnectionEntryView(int position, View convertView, ViewGroup parent) {
            final AddConnectionViewEntry entry = (AddConnectionViewEntry) getItem(position);
            final View result;
            final AddConnectionViewCache viewCache;

            if (convertView != null) {
                result = convertView;
                viewCache = (AddConnectionViewCache) result.getTag();
            } else {
                result = mInflater.inflate(R.layout.contact_detail_add_connection_entry_view,
                        parent, false);
                viewCache = new AddConnectionViewCache(result);
                result.setTag(viewCache);
            }
            viewCache.name.setText(entry.getLabel());
            viewCache.icon.setImageDrawable(entry.getIcon());
            viewCache.primaryActionView.setOnClickListener(entry.mOnClickListener);

            return result;
        }

        private View getDetailEntryView(int position, View convertView, ViewGroup parent) {
            final DetailViewEntry entry = (DetailViewEntry) getItem(position);
            final View v;
            final DetailViewCache viewCache;

            // Check to see if we can reuse convertView
            if (convertView != null) {
                v = convertView;
                viewCache = (DetailViewCache) v.getTag();
            } else {
                // Create a new view if needed
                v = mInflater.inflate(R.layout.contact_detail_list_item, parent, false);

                // Cache the children
                /*
                 * New Feature by Mediatek Begin. 
                 * Original Android's code:
                viewCache = new DetailViewCache(v,
                        mPrimaryActionClickListener, mSecondaryActionClickListener);
                 * add the vtcall button's onClickListener    
                 */
                viewCache = new DetailViewCache(v, mPrimaryActionClickListener,
                        mSecondaryActionClickListener, mVtCallActionClickListener);
                /*
                 * New Feature  by Mediatek End.
                */
                /*
                 * New Feature by Mediatek Begin.
                 *   Original Android's code:
                 *     
                 *   CR ID: ALPS00308657
                 *   Descriptions: RCS
                 */
                mPluginView = v;
                /*
                 * New Feature by Mediatek End.
                 */

                ///M: for SNS plugin @{
                // Used by SNS to customize its display content
                mSNSDetailEntryView = v;
                ///@}
                v.setTag(viewCache);
            }

            bindDetailView(position, v, entry);
            return v;
        }

        private void bindDetailView(int position, View view, DetailViewEntry entry) {
            final Resources resources = mContext.getResources();
            DetailViewCache views = (DetailViewCache) view.getTag();

            if (!TextUtils.isEmpty(entry.typeString)) {
                views.type.setText(entry.typeString.toUpperCase());
                views.type.setVisibility(View.VISIBLE);
            } else {
                views.type.setVisibility(View.GONE);
            }

            views.data.setText(entry.data);
            setMaxLines(views.data, entry.maxLines);

            // Set the default contact method
            views.primaryIndicator.setVisibility(entry.isPrimary ? View.VISIBLE : View.GONE);

            // Set the presence icon
            final Drawable presenceIcon = ContactPresenceIconUtil.getPresenceIcon(
                    mContext, entry.presence);
            final ImageView presenceIconView = views.presenceIcon;
            if (presenceIcon != null) {
                presenceIconView.setImageDrawable(presenceIcon);
                presenceIconView.setVisibility(View.VISIBLE);
            } else {
                presenceIconView.setVisibility(View.GONE);
            }

            final ActionsViewContainer actionsButtonContainer =
                    (ActionsViewContainer) views.actionsViewContainer;
            actionsButtonContainer.setTag(entry);
            actionsButtonContainer.setPosition(position);
            registerForContextMenu(actionsButtonContainer);

            // Set the secondary action button
            final ImageView secondaryActionView = views.secondaryActionButton;
            Drawable secondaryActionIcon = null;
            String secondaryActionDescription = null;
            if (entry.secondaryActionIcon != -1) {
                secondaryActionIcon = resources.getDrawable(entry.secondaryActionIcon);
                secondaryActionDescription = resources.getString(entry.secondaryActionDescription);
            } else if ((entry.chatCapability & Im.CAPABILITY_HAS_CAMERA) != 0) {
                secondaryActionIcon =
                        resources.getDrawable(R.drawable.sym_action_videochat_holo_light);
                secondaryActionDescription = resources.getString(R.string.video_chat);
            } else if ((entry.chatCapability & Im.CAPABILITY_HAS_VOICE) != 0) {
                secondaryActionIcon =
                        resources.getDrawable(R.drawable.sym_action_audiochat_holo_light);
                secondaryActionDescription = resources.getString(R.string.audio_chat);
            }

            final View secondaryActionViewContainer = views.secondaryActionViewContainer;
            if (entry.secondaryIntent != null && secondaryActionIcon != null) {
                secondaryActionView.setImageDrawable(secondaryActionIcon);
                secondaryActionView.setContentDescription(secondaryActionDescription);
                secondaryActionViewContainer.setTag(entry);
                secondaryActionViewContainer.setVisibility(View.VISIBLE);
                views.secondaryActionDivider.setVisibility(View.VISIBLE);
            } else {
                secondaryActionViewContainer.setVisibility(View.GONE);
                views.secondaryActionDivider.setVisibility(View.GONE);
            }

            // Right and left padding should not have "pressed" effect.
            view.setPadding(
                    entry.isInSubSection()
                            ? mViewEntryDimensions.getWidePaddingLeft()
                            : mViewEntryDimensions.getPaddingLeft(),
                    0, mViewEntryDimensions.getPaddingRight(), 0);
            // Top and bottom padding should have "pressed" effect.
            final View primaryActionView = views.primaryActionView;
            primaryActionView.setPadding(
                    primaryActionView.getPaddingLeft(),
                    mViewEntryDimensions.getPaddingTop(),
                    primaryActionView.getPaddingRight(),
                    mViewEntryDimensions.getPaddingBottom());
            secondaryActionViewContainer.setPadding(
                    secondaryActionViewContainer.getPaddingLeft(),
                    mViewEntryDimensions.getPaddingTop(),
                    secondaryActionViewContainer.getPaddingRight(),
                    mViewEntryDimensions.getPaddingBottom());

            /** M: New Feature xxx @{ */
            showNewAddWidget(views, entry);
            /** @} */          

            /*
             * New Feature by Mediatek Begin.
             *   Original Android's code:
             *     
             *   CR ID: ALPS00308657
             *   Descriptions: RCS
             */

            ExtensionManager.getInstance().getContactDetailExtension().setViewVisible(mPluginView,
                    getActivity(), entry.mimetype, entry.data, mContactData.getDisplayName(),
                    ExtensionManager.COMMD_FOR_RCS, R.id.vtcall_action_view_container,
                    R.id.vertical_divider_vtcall, R.id.vtcall_action_button,
                    R.id.secondary_action_view_container, R.id.secondary_action_button,
                    R.id.vertical_divider);
            /*
             * New Feature by Mediatek End.
             */

            ///M: for SNS plugin @{
            if (ExtensionManager
                    .getInstance()
                    .getContactDetailExtension()
                    .isMimeTypeSupported(entry.mimetype,
                            ExtensionManager.COMMD_FOR_SNS,
                            ExtensionManager.COMMD_FOR_SNS)) {
                ExtensionManager
                        .getInstance()
                        .getContactDetailExtension()
                        .setViewVisibleWithBundle(
                                mSNSDetailEntryView,
                                getActivity(),
                                entry.mData,
                                R.id.vtcall_action_view_container,
                                R.id.vertical_divider_vtcall,
                                R.id.vtcall_action_button,
                                R.id.secondary_action_view_container,
                                R.id.secondary_action_button,
                                R.id.vertical_divider,
                                R.id.data,
                                mContext.getResources().getDimensionPixelSize(
                                        R.dimen.detail_network_icon_size),
                                ExtensionManager.COMMD_FOR_SNS);
            }
            ///@}
        }

        private void setMaxLines(TextView textView, int maxLines) {
            if (maxLines == 1) {
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
            } else {
                textView.setSingleLine(false);
                textView.setMaxLines(maxLines);
                textView.setEllipsize(null);
            }
        }

        private final OnClickListener mPrimaryActionClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener == null) return;
                final ViewEntry entry = (ViewEntry) view.getTag();
                if (entry == null) return;
                entry.click(view, mListener);
            }
        };

        private final OnClickListener mSecondaryActionClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener == null) return;
                if (view == null) return;
                final ViewEntry entry = (ViewEntry) view.getTag();
                if (entry == null || !(entry instanceof DetailViewEntry)) return;
                final DetailViewEntry detailViewEntry = (DetailViewEntry) entry;
                final Intent intent = detailViewEntry.secondaryIntent;
                if (intent == null) return;
                mListener.onItemClicked(intent);
            }
        };

        
        /*
         * New Feature by Mediatek Begin.              
         * handle vtcall action           
         */
        private final OnClickListener mVtCallActionClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener == null)
                    return;

                final DetailViewEntry entry = (DetailViewEntry) view.getTag();
                if (entry != null) {
                    final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts(
                            "tel", entry.data, null));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
                    intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) entry.simId);

                    mListener.onItemClicked(intent);
                }
            }
        };
        /*
         * New Feature  by Mediatek End.
        */

        @Override
        public int getCount() {
            return mAllEntries.size();
        }

        @Override
        public ViewEntry getItem(int position) {
            return mAllEntries.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return mAllEntries.get(position).getViewType();
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public long getItemId(int position) {
            final ViewEntry entry = mAllEntries.get(position);
            if (entry != null) {
                return entry.getId();
            }
            return -1;
        }

        @Override
        public boolean areAllItemsEnabled() {
            // Header will always be an item that is not enabled.
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }
    }

    @Override
    public void onAccountSelectorCancelled() {
    }

    @Override
    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        createCopy(account);
    }

    private void createCopy(AccountWithDataSet account) {
        if (mListener != null) {
            /**
             * M: Change Feature ALPS00426671 <br>
             * To fix mismatch issue between search view and list view. <br>
             * One case is that the display name is specified by exchange server. <br>
             * This display name would be inserted to Search Index table for Contact Search. <br>
             * But this display name specified by exchange server is wrong. @{
             */
            final ArrayList<ContentValues> values = mContactData.getContentValues();
            for (ContentValues cv : values) {
                if (StructuredName.CONTENT_ITEM_TYPE.equals(cv.getAsString(Data.MIMETYPE))) {
                    Log.i(TAG, "Before change values :" + cv.toString());
                    cv.put(StructuredName.DISPLAY_NAME,
                            cv.getAsString(StructuredName.FAMILY_NAME) + cv.getAsString(StructuredName.GIVEN_NAME));
                    Log.i(TAG, "Before change values :" + cv.toString());
                }
            }
            /** @} */
            mListener.onCreateRawContactRequested(mContactData.getContentValues(), account);
        }
    }

    /**
     * Default (fallback) list item click listener.  Note the click event for DetailViewEntry is
     * caught by individual views in the list item view to distinguish the primary action and the
     * secondary action, so this method won't be invoked for that.  (The listener is set in the
     * bindview in the adapter)
     * This listener is used for other kind of entries.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener == null) return;
        final ViewEntry entry = mAdapter.getItem(position);
        if (entry == null) return;
        entry.click(view, mListener);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        DetailViewEntry selectedEntry = (DetailViewEntry) mAllEntries.get(info.position);

        menu.setHeaderTitle(selectedEntry.data);

        /** M: New Feature Copy number to dialer @{ */
        // change string:
        // menu.add(ContextMenu.NONE, ContextMenuIds.COPY_TEXT,
        // ContextMenu.NONE, getString(R.string.copy_text));
        menu.add(ContextMenu.NONE, ContextMenuIds.COPY_TEXT, ContextMenu.NONE,
                getString(R.string.menu_copy));
        /** @} */

        // Don't allow setting or clearing of defaults for directory contacts
        if (mContactData.isDirectoryEntry()) {
            return;
        }

        String selectedMimeType = selectedEntry.mimetype;

        // Defaults to true will only enable the detail to be copied to the clipboard.
        boolean isUniqueMimeType = true;

        // Only allow primary support for Phone and Email content types
        if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
            isUniqueMimeType = mIsUniqueNumber;
        } else if (Email.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
            isUniqueMimeType = mIsUniqueEmail;
        }

        // Checking for previously set default
        if (selectedEntry.isPrimary) {
            menu.add(ContextMenu.NONE, ContextMenuIds.CLEAR_DEFAULT,
                    ContextMenu.NONE, getString(R.string.clear_default));
        } else if (!isUniqueMimeType) {
            menu.add(ContextMenu.NONE, ContextMenuIds.SET_DEFAULT,
                    ContextMenu.NONE, getString(R.string.set_default));
        }

        /** M: New Feature Copy number to dialer @{ */
        if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {
            menu.add(ContextMenu.NONE, ContextMenuIds.COPY_TO_DIALER, ContextMenu.NONE,
                    getString(R.string.copy_text_to_dialer));
            /** M: New Feature dial with country code @{ */
            String phoneNumber = selectedEntry.data;
            int insertedSIMCount = SIMInfo.getInsertedSIMCount(mContext);
            // if emergency number and start with "+" ,then not show the menu item.
            if (!phoneNumber.startsWith("+")
                    && !PhoneNumberUtils.isEmergencyNumber(phoneNumber)
                    && insertedSIMCount > 0) {
                menu.add(ContextMenu.NONE, ContextMenuIds.DIAL_WITH_COUNTRY_CODE, ContextMenu.NONE,
                        getString(R.string.international_dialing_dial_with_country_code));
            }
            /** @} */
        }
        /** @} */

        
        /*
         * New Feature by Mediatek Begin.            
         * create new association menu and del association menu        
         */
        /*
         * Bug Fix by Mediatek Begin.
         *   Original Android's code:
         *     if (Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType)) {  
         *   CR ID: ALPS00116397
         */

        boolean result = ExtensionManager.getInstance().getContactDetailExtension().checkMenuItem(
                FeatureOption.MTK_GEMINI_SUPPORT, Phone.CONTENT_ITEM_TYPE.equals(selectedMimeType),
                !isMe(), ContactPluginDefault.COMMD_FOR_OP01);

        int simInfoSize = 0;
        if (null != SIMInfoWrapper.getDefault()) {
            simInfoSize = SIMInfoWrapper.getDefault().getInsertedSimInfoList().size();
        }
        if (result && SIMInfoWrapper.getDefault().getInsertedSimCount() > 1) {
            ExtensionManager.getInstance().getContactDetailExtension().setMenu(menu,
                    (!this.mContactData.isDirectoryEntry()), selectedEntry.simId,
                    (simInfoSize > 0), ContextMenuIds.DEL_ASSOCIATION_SIM,
                    ContextMenuIds.NEW_ASSOCIATION_SIM, getActivity(),
                    R.string.menu_remove_association, R.string.menu_association,
                    ContactPluginDefault.COMMD_FOR_OP01);
        }

        /*
         * New Feature  by Mediatek End.
        */
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        try {
            menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
            case ContextMenuIds.DIAL_WITH_COUNTRY_CODE:
                dialingWithCountryCode(menuInfo.position);
                return true;
            case ContextMenuIds.COPY_TEXT:
                copyToClipboard(menuInfo.position);
                return true;
            case ContextMenuIds.SET_DEFAULT:
                setDefaultContactMethod(mListView.getItemIdAtPosition(menuInfo.position));
                return true;
            case ContextMenuIds.CLEAR_DEFAULT:
                clearDefaultContactMethod(mListView.getItemIdAtPosition(menuInfo.position));
                return true;
            
            /*
             * New Feature by Mediatek Begin.            
             * handle new and del association menu click event        
             */
            case ContextMenuIds.NEW_ASSOCIATION_SIM:                
                handleNewAssociationSimMenu((DetailViewEntry) mAllEntries.get(menuInfo.position));
                return true;
                
            case ContextMenuIds.DEL_ASSOCIATION_SIM:
                handleDelAssociationSimMenu((DetailViewEntry) mAllEntries.get(menuInfo.position));                
                return true;  

                /** M: New Feature Copy number to dialer @{ */
            case ContextMenuIds.COPY_TO_DIALER:
                copyToDialer(menuInfo.position);
                Log.i("TAG","copy to dialer");
                return true;
                /** @} */

            /*
             * New Feature  by Mediatek End.
             */
                
            default:
                throw new IllegalArgumentException("Unknown menu option " + item.getItemId());
        }
    }

    /*
     * New Feature by Mediatek Begin.            
     * add menu action of dialing with country code       
     */
    private void dialingWithCountryCode(int position) {
        DetailViewEntry detailViewEntry = (DetailViewEntry) mAllEntries.get(position);
        final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts(
                "tel", detailViewEntry.data, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.EXTRA_INTERNATIONAL_DIAL_OPTION, 1);
        intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) detailViewEntry.simId);
        mListener.onItemClicked(intent);
    }
    /*
     * New Feature  by Mediatek End.
     */
    public void handleAssociationSimOptionMenu() {
        DetailViewEntry detail = getFirstDetailViewEntry();
        if (detail != null) {
            handleNewAssociationSimMenu(detail);
        }
    }
    
    public DetailViewEntry getFirstDetailViewEntry() {
        for (ViewEntry viewEntry : mAllEntries) {
            if (viewEntry instanceof DetailViewEntry) {
                DetailViewEntry detail = (DetailViewEntry) viewEntry;
                /*
                 * Bug Fix by Mediatek Begin. Original code: return detail; CR
                 * ID: ALPS000113770 ­
                 */
                if (detail.mimetype != null && detail.mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
                    return detail;
                }
                /*
                 * Bug Fix by Mediatek End.
                 */
            }
        }
        return null;
    }
    
    private void setDefaultContactMethod(long id) {
        Intent setIntent = ContactSaveService.createSetSuperPrimaryIntent(mContext, id);
        mContext.startService(setIntent);
    }

    private void clearDefaultContactMethod(long id) {
        Intent clearIntent = ContactSaveService.createClearPrimaryIntent(mContext, id);
        mContext.startService(clearIntent);
    }

    private void copyToClipboard(int viewEntryPosition) {
        // Getting the text to copied
        DetailViewEntry detailViewEntry = (DetailViewEntry) mAllEntries.get(viewEntryPosition);
        CharSequence textToCopy = detailViewEntry.data;

        // Checking for empty string
        if (TextUtils.isEmpty(textToCopy)) return;

        ClipboardUtils.copyText(getActivity(), detailViewEntry.typeString, textToCopy, true);
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                try {
                    ITelephony phone = ITelephony.Stub.asInterface(
                            ServiceManager.checkService("phone"));
                    if (phone != null && !phone.isIdle()) {
                        // Skip out and let the key be handled at a higher level
                        break;
                    }
                } catch (RemoteException re) {
                    // Fall through and try to call the contact
                }

                int index = mListView.getSelectedItemPosition();
                if (index != -1) {
                    final DetailViewEntry entry = (DetailViewEntry) mAdapter.getItem(index);
                    if (entry != null && entry.intent != null &&
                            entry.intent.getAction() == Intent.ACTION_CALL_PRIVILEGED) {
                        mContext.startActivity(entry.intent);
                        return true;
                    }
                } else if (mPrimaryPhoneUri != null) {
                    // There isn't anything selected, call the default number
                    mContext.startActivity(ContactsUtils.getCallIntent(mPrimaryPhoneUri));
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Base class for QuickFixes. QuickFixes quickly fix issues with the Contact without
     * requiring the user to go to the editor. Example: Add to My Contacts.
     */
    private static abstract class QuickFix {
        public abstract boolean isApplicable();
        public abstract String getTitle();
        public abstract void execute();
    }

    private class AddToMyContactsQuickFix extends QuickFix {
        @Override
        public boolean isApplicable() {
            // Only local contacts
            if (mContactData == null || mContactData.isDirectoryEntry()) return false;

            // User profile cannot be added to contacts
            if (mContactData.isUserProfile()) return false;

            // Only if exactly one raw contact
            if (mContactData.getRawContacts().size() != 1) return false;

            // test if the default group is assigned
            final List<GroupMetaData> groups = mContactData.getGroupMetaData();

            // For accounts without group support, groups is null
            if (groups == null) return false;

            // remember the default group id. no default group? bail out early
            final long defaultGroupId = getDefaultGroupId(groups);
            if (defaultGroupId == -1) return false;

            final RawContact rawContact = (RawContact) mContactData.getRawContacts().get(0);
            final AccountType type = rawContact.getAccountType();
            // Offline or non-writeable account? Nothing to fix
            
            /** M: Change Feature not show button when it is null accountType @{ */
            /*
             * Original Code if (type == null || !type.areContactsWritable())
             * return false;
             */
            if (type == null || !type.areContactsWritable() || type.accountType == null) {
                Log.i(TAG, "[isApplicable] type, accountType : " + type + " , " + type.accountType);
                return false;
            }
            /** @} */

            // Check whether the contact is in the default group
            boolean isInDefaultGroup = false;
            for (DataItem dataItem : Iterables.filter(
                    rawContact.getDataItems(), GroupMembershipDataItem.class)) {
                GroupMembershipDataItem groupMembership = (GroupMembershipDataItem) dataItem;
                final Long groupId = groupMembership.getGroupRowId();
                if (groupId == defaultGroupId) {
                    isInDefaultGroup = true;
                    break;
                }
            }

            return !isInDefaultGroup;
        }

        @Override
        public String getTitle() {
            return getString(R.string.add_to_my_contacts);
        }

        @Override
        public void execute() {
            final long defaultGroupId = getDefaultGroupId(mContactData.getGroupMetaData());
            // there should always be a default group (otherwise the button would be invisible),
            // but let's be safe here
            if (defaultGroupId == -1) return;

            // add the group membership to the current state
            final RawContactDeltaList contactDeltaList = mContactData.createRawContactDeltaList();
            final RawContactDelta rawContactEntityDelta = contactDeltaList.get(0);

            final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
            final AccountType type = rawContactEntityDelta.getAccountType(accountTypes);
            final DataKind groupMembershipKind = type.getKindForMimetype(
                    GroupMembership.CONTENT_ITEM_TYPE);
            final ValuesDelta entry = RawContactModifier.insertChild(rawContactEntityDelta,
                    groupMembershipKind);
            entry.setGroupRowId(defaultGroupId);

            // and fire off the intent. we don't need a callback, as the database listener
            // should update the ui
            final Intent intent = ContactSaveService.createSaveContactIntent(getActivity(),
                    contactDeltaList, "", 0, false, getActivity().getClass(),
                    Intent.ACTION_VIEW, null);
            getActivity().startService(intent);
        }
    }

    private class MakeLocalCopyQuickFix extends QuickFix {
        @Override
        public boolean isApplicable() {
            // Not a directory contact? Nothing to fix here
            if (mContactData == null || !mContactData.isDirectoryEntry()) return false;

            // No export support? Too bad
            if (mContactData.getDirectoryExportSupport() == Directory.EXPORT_SUPPORT_NONE) {
                return false;
            }

            return true;
        }

        @Override
        public String getTitle() {
            return getString(R.string.menu_copyContact);
        }

        @Override
        public void execute() {
            if (mListener == null) {
                return;
            }

            int exportSupport = mContactData.getDirectoryExportSupport();
            switch (exportSupport) {
                case Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY: {
                    createCopy(new AccountWithDataSet(mContactData.getDirectoryAccountName(),
                                    mContactData.getDirectoryAccountType(), null));
                    break;
                }
                case Directory.EXPORT_SUPPORT_ANY_ACCOUNT: {
                    final List<AccountWithDataSet> accounts =
                            AccountTypeManager.getInstance(mContext).getAccounts(true);
                    if (accounts.isEmpty()) {
                        createCopy(null);
                        return;  // Don't show a dialog.
                    }

                    // In the common case of a single writable account, auto-select
                    // it without showing a dialog.
                    if (accounts.size() == 1) {
                        createCopy(accounts.get(0));
                        return;  // Don't show a dialog.
                    }

                    SelectAccountDialogFragment.show(getFragmentManager(),
                            ContactDetailFragment.this, R.string.dialog_new_contact_account,
                            AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, null);
                    break;
                }
            }
        }
    }

    /**
     * This class loads the correct padding values for a contact detail item so they can be applied
     * dynamically. For example, this supports the case where some detail items can be indented and
     * need extra padding.
     */
    private static class ViewEntryDimensions {

        private final int mWidePaddingLeft;
        private final int mPaddingLeft;
        private final int mPaddingRight;
        private final int mPaddingTop;
        private final int mPaddingBottom;

        public ViewEntryDimensions(Resources resources) {
            mPaddingLeft = resources.getDimensionPixelSize(
                    R.dimen.detail_item_side_margin);
            mPaddingTop = resources.getDimensionPixelSize(
                    R.dimen.detail_item_vertical_margin);
            mWidePaddingLeft = mPaddingLeft +
                    resources.getDimensionPixelSize(R.dimen.detail_item_icon_margin) +
                    resources.getDimensionPixelSize(R.dimen.detail_network_icon_size);
            mPaddingRight = mPaddingLeft;
            mPaddingBottom = mPaddingTop;
        }

        public int getWidePaddingLeft() {
            return mWidePaddingLeft;
        }

        public int getPaddingLeft() {
            return mPaddingLeft;
        }

        public int getPaddingRight() {
            return mPaddingRight;
        }

        public int getPaddingTop() {
            return mPaddingTop;
        }

        public int getPaddingBottom() {
            return mPaddingBottom;
        }
    }

    public static interface Listener {
        /**
         * User clicked a single item (e.g. mail). The intent passed in could be null.
         */
        public void onItemClicked(Intent intent);

        /**
         * User requested creation of a new contact with the specified values.
         *
         * @param values ContentValues containing data rows for the new contact.
         * @param account Account where the new contact should be created.
         */
        public void onCreateRawContactRequested(ArrayList<ContentValues> values,
                AccountWithDataSet account);
    }

    /**
     * Adapter for the invitable account types; used for the invitable account type list popup.
     */
    private final static class InvitableAccountTypesAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ArrayList<AccountType> mAccountTypes;

        public InvitableAccountTypesAdapter(Context context, Contact contactData) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            final List<AccountType> types = contactData.getInvitableAccountTypes();
            mAccountTypes = new ArrayList<AccountType>(types.size());

            for (int i = 0; i < types.size(); i++) {
                mAccountTypes.add(types.get(i));
            }

            Collections.sort(mAccountTypes, new AccountType.DisplayLabelComparator(mContext));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View resultView =
                    (convertView != null) ? convertView
                    : mInflater.inflate(R.layout.account_selector_list_item, parent, false);

            final TextView text1 = (TextView)resultView.findViewById(android.R.id.text1);
            final TextView text2 = (TextView)resultView.findViewById(android.R.id.text2);
            final ImageView icon = (ImageView)resultView.findViewById(android.R.id.icon);

            final AccountType accountType = mAccountTypes.get(position);

            CharSequence action = accountType.getInviteContactActionLabel(mContext);
            CharSequence label = accountType.getDisplayLabel(mContext);
            if (TextUtils.isEmpty(action)) {
                text1.setText(label);
                text2.setVisibility(View.GONE);
            } else {
                text1.setText(action);
                text2.setVisibility(View.VISIBLE);
                text2.setText(label);
            }
            icon.setImageDrawable(accountType.getDisplayIcon(mContext));

            return resultView;
        }

        @Override
        public int getCount() {
            return mAccountTypes.size();
        }

        @Override
        public AccountType getItem(int position) {
            return mAccountTypes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
    /** M: New Feature xxx @{ */    
    /*
     * New Feature by Mediatek Begin.              
     * show newly view for association and vtcall                          
     */
    private ArrayList<DetailViewEntry> mAnrEntries = new ArrayList<DetailViewEntry>();
    private boolean mIsPhoneEntriesNull = true;///M:AAS
    private NotifyingAsyncQueryHandler mHandler = null;
    private DetailViewEntry tempDetailViewEntry = null;
   
    private ArrayList<DetailViewEntry> mShowingPhoneEntries = null;

    public boolean isMe() {
        if (mContactData != null) {
            String lookupKey = mContactData.getLookupKey();
            if (lookupKey != null) {
                return lookupKey.equals("profile");
            }
        }
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem associationMenuItem = menu.findItem(R.id.menu_association_sim);
        if (associationMenuItem != null) {

            boolean result = ExtensionManager.getInstance().getContactDetailExtension()
                    .checkMenuItem(FeatureOption.MTK_GEMINI_SUPPORT,
                            hasPhoneEntry(this.mContactData), !isMe(), ContactPluginDefault.COMMD_FOR_OP01);
            Log.i(TAG,
                    "onPrepareOptionsMenu result, SIMInfoWrapper.getDefault().getInsertedSimCount()  : "
                            + result + " , " + SIMInfoWrapper.getDefault().getInsertedSimCount());
            int simInfoSize = 0;
            if (null != SIMInfoWrapper.getDefault()) {
                simInfoSize = SIMInfoWrapper.getDefault().getInsertedSimInfoList().size();
            }
            if (result && SIMInfoWrapper.getDefault().getInsertedSimCount() > 1) {
                ExtensionManager.getInstance().getContactDetailExtension().setMenuVisible(
                        associationMenuItem, (!this.mContactData.isDirectoryEntry()),
                        (simInfoSize > 0), ContactPluginDefault.COMMD_FOR_OP01);
            } else {
                associationMenuItem.setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_association_sim:
                handleAssociationSimOptionMenu();
                return true;
            default:
                break;
        }
        return false;
    }

    public void showNewAddWidget(DetailViewCache views, DetailViewEntry entry) {

        final int simId = entry.simId;
        int associVisible = View.GONE;
        int showVTAction = View.GONE;
        if (null != entry && Phone.CONTENT_ITEM_TYPE.equals(entry.mimetype)) {
            if (simId > -1) {
                associVisible = View.VISIBLE;
                views.imgAssociationSimIcon.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.ic_association));
                views.mAssociationSimLayout.setVisibility(View.VISIBLE);

                SIMInfo simInfo = SIMInfo.getSIMInfoById(mContext, simId);
                if (simInfo != null) {
                    Log.i(TAG, "[showNewAddWidget]: simInfo.mDisplayName is "
                            + simInfo.mDisplayName);
                    Log.i(TAG, "[showNewAddWidget]: simInfo.mColor is " + simInfo.mColor);
                    views.txtAssociationSimName.setText(simInfo.mDisplayName);
                    int slotId = SIMInfo.getSlotById(mContext, simId);
                    Log.d(TAG, "slotId = " + slotId);
                    if (slotId >= 0) {
                        Log.d(TAG, "[showNewAddWidget]: slotId >= 0 ");
                        // fix bug for Consistent UI by mediatek start
                        views.txtAssociationSimName
                                .setBackgroundResource(Telephony.SIMBackgroundLightRes[simInfo.mColor]);
                     // fix bug for Consistent UI by mediatek end
                    } else {
                        Log.d(TAG, "[showNewAddWidget]: slotId < 0 ");
                        views.txtAssociationSimName
                                .setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_locked);
                    }
                 // fix bug for Consistent UI by mediatek start
                    int paddingLeft = this.getResources().getDimensionPixelOffset(
                            R.dimen.dialpad_operator_horizontal_padding_left);
                    int paddingRight = this.getResources().getDimensionPixelOffset(
                            R.dimen.dialpad_operator_horizontal_padding_right);
                    views.txtAssociationSimName.setPadding(paddingLeft, 0, paddingRight, 0);
                 // fix bug for Consistent UI by mediatek end
                } else {
                    Log.i(TAG, "[showNewAddWidget]: not find siminfo");
                }
            }
            views.btnVtCallAction.setImageDrawable(mContext.getResources().getDrawable(
                    R.drawable.ic_video_call));
            views.vtcallActionViewContainer.setTag(entry);
            showVTAction = View.VISIBLE;
            // views.btnVtCallAction.setTag(entry);
        }
        views.imgAssociationSimIcon.setVisibility(associVisible);
        views.txtAssociationSimName.setVisibility(associVisible);
        if (FeatureOption.MTK_VT3G324M_SUPPORT) {
            views.vewVtCallDivider.setVisibility(showVTAction);
            views.btnVtCallAction.setVisibility(showVTAction);
            views.vtcallActionViewContainer.setVisibility(showVTAction);
            if (showVTAction == View.GONE) {
                views.vewVtCallDivider.setClickable(false);
                views.btnVtCallAction.setClickable(false);
            }
        } else {
            views.vewVtCallDivider.setVisibility(View.GONE);
            views.btnVtCallAction.setVisibility(View.GONE);
            views.vtcallActionViewContainer.setVisibility(View.GONE);
            views.vtcallActionViewContainer.setClickable(false);
            /** M: Bug Fix for ALPS00363177 @{ */
            views.vewVtCallDivider.setClickable(false);
            views.btnVtCallAction.setClickable(false);
            /** @} */
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * get if has phone number item        
     */
    public boolean hasPhoneEntry(Contact contactData) {
        if (contactData == null) {
            Log.d(TAG, "[hasPhoneEntry]: contactData = null");
            return false;
        }
        String phoneNumber = null;
        for (RawContact rawContact : mContactData.getRawContacts()) {
            for (DataItem dataItem : rawContact.getDataItems()) {
                if (null != dataItem && dataItem instanceof PhoneDataItem
                        && null != dataItem.getContentValues()) {
                    String number = dataItem.getContentValues().getAsString(Data.DATA1);
                    Log.i(TAG, "hasPhoneEntry number : " + number);
                    if (!TextUtils.isEmpty(number)) {
                        phoneNumber = number;
                    }
                }
            }
        }
        return !TextUtils.isEmpty(phoneNumber);
    }
    /*
     * New Feature  by Mediatek End.
    */


    /*
     * New Feature by Mediatek Begin.            
     * handle new association menu click event        
     */
    public void handleNewAssociationSimMenu(DetailViewEntry detailViewEntry) {
        if (detailViewEntry == null) {
            Log.d(TAG, "[handleNewAssociationSimMenu]: detailViewEntry = null");
            return;
        }
        tempDetailViewEntry = detailViewEntry;

        if (this.mContactData.getIndicate() > -1) {// RawContacts.INDICATE_SIM)
            new AlertDialog.Builder(this.mContext).setTitle(this.mContactData.getDisplayName())
            // .setIcon(android.R.drawable.ic_menu_more)
                    .setMessage(R.string.warning_detail).setPositiveButton(android.R.string.ok,
                            mOnNewAssociationSimListener).setNegativeButton(
                            android.R.string.cancel, null).setCancelable(true).create().show();
        } else {
            startAssociationActivity(this.mContactData.getDisplayName(), ContactDetailDisplayUtils
                    .getCompany(this.mContext, this.mContactData), this.mContactData
                    .getNameRawContactId(), tempDetailViewEntry.id, this.mContactData
                    .getLookupUri(), getNumberContentValuesFromDataTable(mShowingPhoneEntries));
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * handle del association menu click event        
     */
    public void handleDelAssociationSimMenu(DetailViewEntry detailViewEntry) {
        if (detailViewEntry == null) {
            Log.d(TAG, "[handleDelAssociationSimMenu]: detailViewEntry = null");
            return;
        }
        tempDetailViewEntry = detailViewEntry;

        new AlertDialog.Builder(this.mContext).setTitle(R.string.remove_number_title).setIcon(
                android.R.drawable.ic_menu_more).setMessage(R.string.remove_association_message)
                .setPositiveButton(R.string.remove_number_title, mOnDelSimAssociationListener)
                .setNegativeButton(android.R.string.cancel, null).setCancelable(true).create()
                .show();
    }
    /*
     * New Feature  by Mediatek End.
    */
    
 
    /*
     * New Feature by Mediatek Begin.            
     * handle dialog's ok button event        
     */
    private DialogInterface.OnClickListener mOnDelSimAssociationListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            if (tempDetailViewEntry == null) {
                Log.d(TAG, "[mDelSimAssociationListener.onClick]: tempDetailViewEntry = null");
                return;
            }

            ContentValues values = new ContentValues();
            values.put(Data.SIM_ASSOCIATION_ID, -1);
            mHandler.startUpdate(0, null, Data.CONTENT_URI, values, Data._ID + "=? ", new String[] {
                String.valueOf(tempDetailViewEntry.id)
            });
        }
    };
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * handle dialog's ok button event        
     */
    private DialogInterface.OnClickListener mOnNewAssociationSimListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            if (tempDetailViewEntry == null) {
                Log.d(TAG, "[mNewAssociationSimListener.onClick]: tempDetailViewEntry = null");
                return;
            }

            if (isUSimType(mContactData.getSlot())) {
                Log.d(TAG, "[mNewAssociationSimListener.onClick]: is USIM card");
                // importOneUSimContact(name, type, number, email,
                // additional_number, grpIdSet);

                String email = null;
                String additionNumber = null;
                int additionNumberType = -1;
                
                for (RawContact rawContact : mContactData.getRawContacts()) {
                    for (DataItem dataItem : rawContact.getDataItems()) {
                        if (null != dataItem && null != dataItem.getContentValues()) {
                            Log.i(TAG, "mOnNewAssociationSimListener mimeType : "
                                    + dataItem.getMimeType());
                            if (dataItem instanceof EmailDataItem) {
                                email = dataItem.getContentValues().getAsString(Data.DATA1);
                                Log.i(TAG, "mOnNewAssociationSimListener email is : " + email);
                            } else if (dataItem instanceof PhoneDataItem) {
                                Log.i(TAG,
                                        "mOnNewAssociationSimListener tempDetailViewEntry.getId() and dataId : "
                                                + tempDetailViewEntry.getId() + " , "
                                                + dataItem.getContentValues().getAsLong(Data._ID));
                                if (tempDetailViewEntry.getId() != dataItem.getContentValues()
                                        .getAsLong(Data._ID)) {
                                    additionNumber = dataItem.getContentValues().getAsString(
                                            Data.DATA1);
                                    additionNumberType = dataItem.getContentValues().getAsInteger(
                                            Data.DATA2);
                                    Log.i(TAG,
                                            "mOnNewAssociationSimListener additionNumber, additionNumberType : "
                                                    + additionNumber + " , " + additionNumberType);
                                }
                            }
                        }
                    }
                }
                importOneUSimContact(mContactData.getDisplayName(), tempDetailViewEntry.type,
                        tempDetailViewEntry.data, email, additionNumber, additionNumberType,
                        null);
                
            } else {
                Log.d(TAG, "[mNewAssociationSimListener.onClick]: is USIM card");
                importOneSimContact(mContactData.getDisplayName(), tempDetailViewEntry.type,
                        tempDetailViewEntry.data);
            }

        }
    };
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * new add one phone contact from one USIM contacts        
     */
    private void importOneUSimContact(String name, int phoneType, String phoneNumber, String email,
            String additional_number, int additional_type, Set<Long> grpAddIds) {

        ContentValues sEmptyContentValues = new ContentValues();

        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        // add by mediatek
        ContentValues contactvalues = new ContentValues();
        String ProductCharacteristic = SystemProperties.get("ro.build.characteristics");

        if ((ProductCharacteristic != null) && (ProductCharacteristic.equals("tablet"))) {
            contactvalues.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_TABLET);
        } else {
            contactvalues.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
        }
        contactvalues.put(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);

        contactvalues.put(RawContacts.INDICATE_PHONE_SIM,
                ContactsContract.RawContacts.INDICATE_PHONE);
        // builder.withValues(sEmptyContentValues);
        builder.withValues(contactvalues);
        /** M: Bug Fix for ALPS00345510 @{ */
        builder.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        /** @} */
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builder.withValue(Phone.TYPE, phoneType);
        if (!TextUtils.isEmpty(phoneNumber)) {
            builder.withValue(Phone.NUMBER, phoneNumber);
            builder.withValue(Data.IS_PRIMARY, 1);
        } else {
            builder.withValue(Phone.NUMBER, null);
            builder.withValue(Data.IS_PRIMARY, 1);
        }
        operationList.add(builder.build());

        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(StructuredName.DISPLAY_NAME, name);
        operationList.add(builder.build());

        if (!TextUtils.isEmpty(email)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
            builder.withValue(Email.DATA, email);
            operationList.add(builder.build());
        }

        if (!TextUtils.isEmpty(additional_number)) {
            Log.i(TAG, "additionalNumber is " + additional_number);
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            if (additional_type > -1) {
                builder.withValue(Phone.TYPE, additional_type);
            } else {
                builder.withValue(Phone.TYPE, phoneType);
            }
            builder.withValue(Phone.NUMBER, additional_number);
            // builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
            operationList.add(builder.build());
        }

        // USIM Group begin
        /*
        if (grpAddIds != null && grpAddIds.size() > 0) {
            Long[] grpIdArray = grpAddIds.toArray(new Long[0]);
            for (Long grpId : grpIdArray) {
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
                operationList.add(builder.build());
            }
        }
        */
        // USIM Group end

        try {
            startAssociationActivity(mContext.getContentResolver().applyBatch(
                    ContactsContract.AUTHORITY, operationList));
        } catch (Exception e) {
            Log.e(TAG, "[importOneUSimContact]: "
                    + String.format("%s: %s", e.toString(), e.getMessage()));
        }
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * new add one phone contact from one SIM contacts        
     */
    private  void importOneSimContact(String name, int phoneType, String phoneNumber) {
//      NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
//              cursor.getString(NAME_COLUMN));
        ContentValues sEmptyContentValues = new ContentValues();
        
//      String emailAddresses = "";//cursor.getString(EMAILS_COLUMN);

        final ArrayList<ContentProviderOperation> operationListForSim = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builderForSim = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        // add by mediatek
        ContentValues contactvalues = new ContentValues();
        String ProductCharacteristic = SystemProperties.get("ro.build.characteristics");
        
        if ((ProductCharacteristic != null) && (ProductCharacteristic.equals("tablet"))) {
          contactvalues.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_TABLET);
        } else {
          contactvalues.put(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
        }
        contactvalues.put(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, ContactsContract.RawContacts.INDICATE_PHONE);
//        builder.withValues(sEmptyContentValues);
        builderForSim.withValues(contactvalues);
        /** M: Bug Fix for ALPS00345510 @{ */
        builderForSim.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        /** @} */
        operationListForSim.add(builderForSim.build());

        builderForSim = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builderForSim.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
        builderForSim.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        builderForSim.withValue(Phone.TYPE, phoneType);
        if (!TextUtils.isEmpty(phoneNumber)) {
            builderForSim.withValue(Phone.NUMBER, phoneNumber);
            builderForSim.withValue(Data.IS_PRIMARY, 1);
        } else {
            builderForSim.withValue(Phone.NUMBER, null);
            builderForSim.withValue(Data.IS_PRIMARY, 1);
        }
        operationListForSim.add(builderForSim.build());

        builderForSim = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builderForSim.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
        builderForSim.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        builderForSim.withValue(StructuredName.DISPLAY_NAME, name);
        operationListForSim.add(builderForSim.build());

        try {
            startAssociationActivity(mContext.getContentResolver().applyBatch(
                    ContactsContract.AUTHORITY, operationListForSim));
        } catch (Exception e) {
            Log.e(TAG, "[importOneSimContact]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        } 
    }
    /*
     * New Feature  by Mediatek End.
    */

    /*
     * New Feature by Mediatek Begin.            
     * start association sim activity         
     */
    public void startAssociationActivity(ContentProviderResult[] r) {
        ContentProviderResult r1 = r[0];
        String raw_contact_id = r1.uri.getPath();
        raw_contact_id = raw_contact_id.substring(raw_contact_id.lastIndexOf("/") + 1);

        ContentProviderResult r2 = r[1];
        String data_id = r2.uri.getPath();
        data_id = data_id.substring(data_id.lastIndexOf("/") + 1);

        startAssociationActivity(this.mContactData.getDisplayName(), "", this.mContactData
                .getNameRawContactId(), Long.parseLong(data_id), this.mContactData.getLookupUri(),
                getNumberContentValuesFromDataTable(raw_contact_id));
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    /*
     * New Feature by Mediatek Begin.            
     * get if usim type    
     */
    public boolean isUSimType(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        if (iTel == null) {
            Log.d(TAG, "[isUSimType]: iTel = null");
            return false;
        }

        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                return iTel.getIccCardTypeGemini(slot).equals("USIM");
            } else {
                return iTel.getIccCardType().equals("USIM");
            }
        } catch (Exception e) {
            Log.e(TAG, "[isUSimType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    
    
    
    
    /*
     * New Feature by Mediatek Begin.            
     * handle query return result        
     */
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * start association sim activity        
     */
    public void startAssociationActivity(String displayTitle, String displaySubtitle,
            long rawContactId, long selDataId, Uri lookupUri,
            List<NamedContentValues> numberInfoList) {
        /** M: Bug Fix for ALPS00407311 @{ */
        AssociationSimActivity.sContactDetailInfo = new ContactDetailInfo(displayTitle,
                displaySubtitle, lookupUri, numberInfoList, mContactData.getPhotoUri());
        /** @} */
        Intent intent = new Intent(this.mContext, AssociationSimActivity.class);
        intent.putExtra(AssociationSimActivity.INTENT_DATA_KEY_SEL_DATA_ID, selDataId);
        intent.putExtra(AssociationSimActivity.INTENT_DATA_KEY_SEL_SIM_ID,
                tempDetailViewEntry.simId);

        startActivity(intent);
    }
    /*
     * New Feature  by Mediatek End.
    */
    
    /*
     * New Feature by Mediatek Begin.            
     * get the rawContactId Contact's data from data table        
     */
    public List<NamedContentValues> getNumberContentValuesFromDataTable(String rawContactId) {
        // List<NamedContentValues> numberInfoList = new
        // ArrayList<NamedContentValues>();

        Cursor cursor = this.mContext.getContentResolver().query(Data.CONTENT_URI, DATA_PROJECTION,
                Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + "= ?", new String[] {
                        rawContactId, Phone.CONTENT_ITEM_TYPE
                }, null);

        ArrayList<DetailViewEntry> phoneEntries = new ArrayList<DetailViewEntry>();
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    final DetailViewEntry entry = new DetailViewEntry();
                    entry.id = cursor.getLong(DataQuery._ID);
                    entry.data = cursor.getString(DataQuery.DATA1);
                    entry.type = Integer.parseInt(cursor.getString(DataQuery.DATA2));
                    entry.simId = (int) cursor.getLong(DataQuery.SIM_ASSOCIATION_ID);
                    entry.mimetype = Phone.CONTENT_ITEM_TYPE;
                    phoneEntries.add(entry);
                }
                Collapser.collapseList(phoneEntries);
            }
            cursor.close();
        }

        return getNumberContentValuesFromDataTable(phoneEntries);
    }
    
    public List<NamedContentValues> getNumberContentValuesFromDataTable(
            ArrayList<DetailViewEntry> phoneEntries) {
        List<NamedContentValues> numberInfoList = new ArrayList<NamedContentValues>();

        if (phoneEntries != null) {
            for (DetailViewEntry detailEntry : phoneEntries) {
                ContentValues cv = new ContentValues();
                cv.put(Data._ID, detailEntry.getId());
                cv.put(Data.DATA1, detailEntry.data);
                cv.put(Data.DATA2, String.valueOf(detailEntry.type));
                cv.put(Data.SIM_ASSOCIATION_ID, detailEntry.simId);
                NamedContentValues nv = new NamedContentValues(null, cv);
                numberInfoList.add(nv);
            }
        }

        return numberInfoList;
    }
    
    final String[] DATA_PROJECTION = new String[] {
            Data._ID,
            Data.DATA1,
            Data.DATA2,
            Data.SIM_ASSOCIATION_ID,
    };

    private static class DataQuery {
        public final static int _ID = 0;
        public final static int DATA1 = 1;
        public final static int DATA2 = 2;
        public final static int SIM_ASSOCIATION_ID = 3;
    }
    
    /*
     * New Feature  by Mediatek End.
    */
    
    public static boolean isUnSync(String mimeType, ContentValues values) {
        /*
         * Bug Fix by Mediatek Begin. Original Android's code: xxx CR ID:
         * ALPS00246021 Descriptions:
         */
        if ((mimeType == Im.CONTENT_ITEM_TYPE)
                || (mimeType == Phone.CONTENT_ITEM_TYPE
                        && (GetType(values, Phone.TYPE) == Phone.TYPE_PAGER)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_CALLBACK)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_CAR)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_COMPANY_MAIN)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_MAIN)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_RADIO)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_TELEX)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_TTY_TDD)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_WORK_MOBILE)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_WORK_PAGER)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_MMS)
                        || (GetType(values, Phone.TYPE) == Phone.TYPE_ISDN) || (GetType(values,
                        Phone.TYPE) == Phone.TYPE_ASSISTANT))
                || (mimeType == Email.CONTENT_ITEM_TYPE && (GetType(values, Email.TYPE) == Email.TYPE_MOBILE)) ||
                (mimeType == Event.CONTENT_ITEM_TYPE)) {
            Log.d(TAG, "isUnSync(), return true ");
            return true;
        } else {
            return false;
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private static int GetType(ContentValues values, String typeColumn) {
        if (values.containsKey(typeColumn)) {
            return values.getAsInteger(typeColumn);
        }
        return -1;

    }

    private void getShowingPhoneEntries() {
        mShowingPhoneEntries = new ArrayList<DetailViewEntry>();
        for (DetailViewEntry entry : mPhoneEntries) {
            mShowingPhoneEntries.add(entry);
        }
    }

    public void onUpdateComplete(int token, Object cookie, int result) {
        if (token == 0) {
            Activity activity = this.getActivity();
            if (activity != null) {
                activity.sendBroadcast(new Intent("com.android.contacts.associate_changed"));
            }
        }
    }

    /** M: New Feature Copy number to dialer @{ */
    private void copyToDialer(int viewEntryPosition) {
        DetailViewEntry detailViewEntry = (DetailViewEntry) mAllEntries.get(viewEntryPosition);
        CharSequence textToCopy = detailViewEntry.data;
        Log.i(TAG, "[copyToDialer] textToCopy : " + textToCopy);
        startActivity(new Intent(Intent.ACTION_DIAL, ContactsUtils
                .getCallUri(textToCopy.toString())));
    }

    /** @} **/
    
    /*
     * New Feature by Mediatek Begin.
     *   Original Android's code:
     *     
     *   CR ID: ALPS00308657
     *   Descriptions: RCS
     */
    public static final String RCS_DISPLAY_NAME = "rcs_display_name";
    public static final String RCS_PHONE_NUMBER = "rcs_phone_number";

    private static HashMap<String, String> mPhoneAndSubtitle = new HashMap<String, String>();
    private static ArrayList<DetailViewEntry> mExtentionEntries = new ArrayList<DetailViewEntry>();
    private View mPluginView;
    /*
     * New Feature by Mediatek End.
     */
    /** @} */

    ///M: for SNS plugin @{
    private static ArrayList<DetailViewEntry> mSNSEntries = new ArrayList<DetailViewEntry>();
    private View mSNSDetailEntryView;
    private static final String SNS_ACCOUNT_TYPE = "account_type";
    private static final String SNS_USER_ID = "user_id";
    private static final String SNS_PACKAGE_NAME = "res_package_name";
    private static final String SNS_PACKAGE_ICON = "res_package_icon";

    private static OnUpdateClickListener mUpdateClickListener;

    /**
     * Used by SNS entries to response click event.
     */
    public interface OnUpdateClickListener {
        /**
         * Called when a SNS entry is clicked.
         */
        public void scrollUpdateStatus();
    }

    /**
     * Set a click listener for a SNS entry.
     * 
     * @param updateClickListener
     *            a click listener
     */
    public void setUpdateClickListener(OnUpdateClickListener updateClickListener) {
        mUpdateClickListener = updateClickListener;
    }

    /*
     * It builds SNSOne quick action(Post/Album) and status items for
     * DetailViewEntry of one contact.
     */
    private void buildSNSEntity(ContentValues entValues,
            ContentValues entryValues, DetailViewEntry entry, AccountType type,
            DataKind kind, String accountType, DataItem dataItem) {
        final long dataId = entryValues.getAsLong(Data._ID);
        String uid = entValues.getAsString(RawContacts.SOURCE_ID);

        Log.i(TAG, "This is a SNS type " + accountType);

        Bundle data = new Bundle();
        data.putString(SNS_ACCOUNT_TYPE, accountType);
        data.putString(SNS_USER_ID, uid);
        if (null != type) {
            Log.d(TAG, "add resPackageName" + type.resourcePackageName);
            Log.d(TAG, "add iconRes" + type.iconRes);
            data.putString(SNS_PACKAGE_NAME, type.resourcePackageName);
            data.putInt(SNS_PACKAGE_ICON, type.iconRes);
        }
        entry.mData = data;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(entry.uri, entry.mimetype);
        entry.intent = intent;

        if (kind.actionBody != null) {
            CharSequence body = kind.actionBody.inflateUsing(mContext,
                    entryValues);
            entry.data = (body == null) ? null : body.toString();
        }
        mSNSEntries.add(entry);

        ImmutableList<StreamItemEntry> status = mContactData.getStreamItems();
        StreamItemEntry update = status.size() > 0 ? status.get(0) : null;
        DetailViewEntry updateEntry = null;
        if (update != null) {
            Log.i(TAG, "This is a SNS type add status ");
            updateEntry = DetailViewEntry.fromValues(mContext, dataItem, mContactData.isDirectoryEntry(),
                    mContactData.getDirectoryId());
            updateEntry.data = update.getText();
            CharSequence attribute = ContactBadgeUtil.getSocialDate(update,
                    mContext);
            if (null != attribute) {
                updateEntry.typeString = attribute.toString();
            }
            updateEntry.mIsSNSStatus = true;
            Intent updateIntent = new Intent(Intent.ACTION_VIEW);
            updateIntent.setDataAndType(entry.uri, entry.mimetype);
            updateEntry.intent = updateIntent;
            mSNSEntries.add(updateEntry);
        }
    }
    ///@}
}
