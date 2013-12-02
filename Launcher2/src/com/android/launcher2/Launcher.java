
/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
//renxinquan add start page count
import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.view.Gravity;
import android.graphics.drawable.BitmapDrawable;
//renxinquan add end page count
import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
//liyang sence_feature 0701 (on)
import android.content.pm.ResolveInfo;
//liyang sence_feature 0701 (off)
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.ImageView;
import android.widget.IMTKWidget;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
//GPBYB-314 liyang 20130911 add start
import android.widget.ImageButton;
//GPBYB-314 liyang 20130911 add end

import com.android.common.Search;
import com.android.launcher.R;
import com.android.launcher2.DropTarget.DragObject;
import com.mediatek.common.featureoption.FeatureOption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
//add GPBLY-657 20130702 start
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
//add GPBLY-657 20130702 end
import android.app.ProgressDialog;
import android.app.KeyguardManager;
import android.content.pm.ResolveInfo;
import android.telephony.TelephonyManager;
/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        implements View.OnClickListener, OnLongClickListener, LauncherModel.Callbacks,
                   View.OnTouchListener, MTKUnreadLoader.UnreadCallbacks {
    private static final String TAG = "Launcher";
    static final String TAG_SURFACEWIDGET = "MTKWidgetView";
    static final boolean LOGD = false;

    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = true;
    static final boolean DEBUG_STRICT_MODE = false;

    private static final int MENU_GROUP_WALLPAPER = 1;
    private static final int MENU_WALLPAPER_SETTINGS = Menu.FIRST + 1;
    private static final int MENU_MANAGE_APPS = MENU_WALLPAPER_SETTINGS + 1;
    private static final int MENU_SYSTEM_SETTINGS = MENU_MANAGE_APPS + 1;
    private static final int MENU_HELP = MENU_SYSTEM_SETTINGS + 1;

    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    private static final int REQUEST_BIND_APPWIDGET = 11;
    
    //zhuwei add
    private static final int MENU_UNINSTALL = 10;
    private static final int MENU_UNINSTALL_CANCEL = 11;
    private static final int MENU_GROUP_UNINSTALL = MENU_GROUP_WALLPAPER + 1;
    public static boolean isAppsItemUninstallState = false;
    private MenuItem mUnInstallItem = null;
    private MenuItem mCancelUnstallItem = null;
    Object appsItemUninstallObj = new Object();
    Object workspaceItemEditObj = new Object();
    private boolean mPopupMenuShowing = false;
    private PopupMenu mPopupMenu = null;
    private boolean mHasMenukey;
    private View mOverflowMenuButton;
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	if (!FeatureOption.FEATURE_GP811_UNINSTALL) {
    		return;
        }
        Log.i("zhuwei","onConfigurationChanged isAppsItemUninstallState->"+isAppsItemUninstallState);
        if (isAppsItemUninstallState) {
        	cancelUninstallAppsItem(true);

			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					startUninstallAppsItem();
				}
			};
			mHandler.postDelayed(r, 0);
        }
 
    	 if (!mHasMenukey) {
             boolean popupMenuShowing = mPopupMenuShowing;
             if (popupMenuShowing && mPopupMenu != null) {
                 mPopupMenu.dismiss();
             }
             createFakeMenu();
             if (popupMenuShowing && mOverflowMenuButton != null) {
                 mOverflowMenuButton.performClick();
             }
         }

         
    }
    
    private void createFakeMenu() {
    	if (!mHasMenukey) {
    		final View menu = findViewById(R.id.gp811_menu);
    		mOverflowMenuButton = menu;
    		menu.setVisibility(View.VISIBLE);
    		menu.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					final PopupMenu popupMenu = new PopupMenu(
							Launcher.this, menu);
					mPopupMenu = popupMenu;
					final Menu menu = popupMenu.getMenu();
					boolean b = onCreateOptionsMenu(menu);
                                        Log.i("zhuwei","onCreateOptionsMenu->"+b);
					popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
								public boolean onMenuItemClick(MenuItem item) {
									return onOptionsItemSelected(item);
								}
						      });
					popupMenu.setOnDismissListener(new OnDismissListener() {
						public void onDismiss(PopupMenu menu) {
							mPopupMenuShowing = false;
							return;
						}
					});
					onPrepareOptionsMenu(menu);
					if (popupMenu != null) {
						mPopupMenuShowing = true;
						popupMenu.show();
					}
				}
			});
    	}
	}
    
    //end
    


	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
//renxinquan add start page count
    //static final int SCREEN_COUNT = 5;
    //static final int DEFAULT_SCREEN = 2;
    
    static int SCREEN_COUNT = -1;//Utilities.defaultPageViewCount;
    static int DEFAULT_SCREEN = 2;//Utilities.defaultPageIndex;
    
//renxinquan add end page count
    private static final String PREFERENCES = "launcher.preferences";
    // To turn on these properties, type
    // adb shell setprop log.tag.PROPERTY_NAME [VERBOSE | SUPPRESS]
    static final String FORCE_ENABLE_ROTATION_PROPERTY = "launcher_force_rotate";
    static final String DUMP_STATE_PROPERTY = "launcher_dump_state";

    // The Intent extra that defines whether to ignore the launch animation
    static final String INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION =
            "com.android.launcher.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: int
    private static final String RUNTIME_STATE = "launcher.state";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CONTAINER = "launcher.add_container";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cell_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cell_y";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_span_x";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_span_y";
    // Type: parcelable
    private static final String RUNTIME_STATE_PENDING_ADD_WIDGET_INFO = "launcher.add_widget_info";

    private static final String TOOLBAR_ICON_METADATA_NAME = "com.android.launcher.toolbar_icon";
    private static final String TOOLBAR_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_search_icon";
    private static final String TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME =
            "com.android.launcher.toolbar_voice_search_icon";
//add zhaojiangwei test
    private Handler mDelayHandler = new Handler();//zhaojiangwei
    public ProgressDialog mProgressDialog = null; //zhaojiangwei
    private static final int MAX_TASKS = 21;
    private int mCallState = TelephonyManager.CALL_STATE_IDLE;
    private boolean inPowerSwitchPowerMode = false;
    private boolean inMenuSwitchPowerMode = false;
//add zhaojiangwei test
//add GPBLY-657 20130702 start
    private static final String BOOL_SHOW_NOTICE = "is_notice_show";
//add GPBLY-657 20130702 end
    /** The different states that Launcher can be in. */
    private enum State { NONE, WORKSPACE, APPS_CUSTOMIZE, APPS_CUSTOMIZE_SPRING_LOADED };
    private State mState = State.WORKSPACE;
    private AnimatorSet mStateAnimation;
    private AnimatorSet mDividerAnimator;

    static final int APPWIDGET_HOST_ID = 1024;
    private static final int EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT = 300;
    private static final int EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT = 600;
    private static final int SHOW_CLING_DURATION = 550;
    private static final int DISMISS_CLING_DURATION = 250;

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREEN;

    // How long to wait before the new-shortcut animation automatically pans the workspace
    private static int NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS = 10;

    private final BroadcastReceiver mCloseSystemDialogsReceiver
            = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();

    private LayoutInflater mInflater;

    private Workspace mWorkspace;
    private View mQsbDivider;
    private View mDockDivider;
    private View mLauncherView;
    private DragLayer mDragLayer;
    private DragController mDragController;

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;

    private ItemInfo mPendingAddInfo = new ItemInfo();
    private AppWidgetProviderInfo mPendingAddWidgetInfo;

    private int[] mTmpAddItemCellCoordinates = new int[2];

    private FolderInfo mFolderInfo;

    private Hotseat mHotseat;
    private View mAllAppsButton;

    private SearchDropTargetBar mSearchDropTargetBar;
    private AppsCustomizeTabHost mAppsCustomizeTabHost;
    private AppsCustomizePagedView mAppsCustomizeContent;
    private boolean mAutoAdvanceRunning = false;

    private Bundle mSavedState;
    // We set the state in both onCreate and then onNewIntent in some cases, which causes both
    // scroll issues (because the workspace may not have been measured yet) and extra work.
    // Instead, just save the state that we need to restore Launcher to, and commit it in onResume.
    private State mOnResumeState = State.NONE;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mWorkspaceLoading = true;

    private boolean mPaused = true;
    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;

    // Keep track of whether the user has left launcher
    private static boolean sPausedFromUserAction = false;

    private Bundle mSavedInstanceState;

    private LauncherModel mModel;
    private IconCache mIconCache;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mAttached = false;
//add zhaojiangwei test
    private CheckServer mCheckServer;	
    private boolean inPowerMode;
    private Context mContext;
//add zhaojiangwei test
    private static LocaleConfiguration sLocaleConfiguration = null;

    private static HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();

    private Intent mAppMarketIntent = null;

    // Related to the auto-advancing of widgets
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
        new HashMap<View, AppWidgetProviderInfo>();

    // Determines how long to wait after a rotation before restoring the screen orientation to
    // match the sensor state.
    private final int mRestoreScreenOrientationDelay = 500;

    // External icons saved in case of resource changes, orientation, etc.
    private static Drawable.ConstantState[] sGlobalSearchIcon = new Drawable.ConstantState[2];
    private static Drawable.ConstantState[] sVoiceSearchIcon = new Drawable.ConstantState[2];
    private static Drawable.ConstantState[] sAppMarketIcon = new Drawable.ConstantState[2];

    private Drawable mWorkspaceBackgroundDrawable;
    private Drawable mBlackBackgroundDrawable;

    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<Integer>();

    static final ArrayList<String> sDumpLogs = new ArrayList<String>();

    // We only want to get the SharedPreferences once since it does an FS stat each time we get
    // it from the context.
    private SharedPreferences mSharedPrefs;

    // Holds the page that we need to animate to, and the icon views that we need to animate up
    // when we scroll to that page on resume.
    private int mNewShortcutAnimatePage = -1;
    private ArrayList<View> mNewShortcutAnimateViews = new ArrayList<View>();
    private ImageView mFolderIconImageView;
    private Bitmap mFolderIconBitmap;
    private Canvas mFolderIconCanvas;
    private Rect mRectForFolderAnimation = new Rect();

    private BubbleTextView mWaitingForResume;

    private HideFromAccessibilityHelper mHideFromAccessibilityHelper
        = new HideFromAccessibilityHelper();

    private Runnable mBuildLayersRunnable = new Runnable() {
        public void run() {
            if (mWorkspace != null) {
                mWorkspace.buildPageHardwareLayers();
            }
        }
    };

    private static ArrayList<PendingAddArguments> sPendingAddList
            = new ArrayList<PendingAddArguments>();

    private static boolean sForceEnableRotation = isPropertyEnabled(FORCE_ENABLE_ROTATION_PROPERTY);

    private static class PendingAddArguments {
        int requestCode;
        Intent intent;
        long container;
        int screen;
        int cellX;
        int cellY;
    }

    private static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    /// M: Static variable to record whether locale has been changed.
    private static boolean sLocaleChanged = false;

    /// M: Add for launch specified applications in landscape. @{
    private static final int ORIENTATION_0 = 0;
    private static final int ORIENTATION_90 = 90;
    private static final int ORIENTATION_180 = 180;
    private static final int ORIENTATION_270 = 270;

    private OrientationEventListener mOrientationListener;
    private int mLastOrientation = ORIENTATION_0;
    /// @}

    /// M: Add for measure launcher performance. @{
    private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
        @Override
        public boolean onPostDraw() {
            if (LauncherLog.DEBUG_DRAW) {
                LauncherLog.i("AppLaunch", "[AppLaunch] Launcher2 onPostDraw.");
            }
            return true;
        }
    }
    private final PostDrawListener mPostDrawListener = new PostDrawListener();
    /// @}

    /// M: Add for launcher unread shortcut feature. @{
    static final int MAX_UNREAD_COUNT = 99;
    private MTKUnreadLoader mUnreadLoader;

    private boolean mUnreadLoadCompleted = false;
    private boolean mBindingWorkspaceFinished = false;
    private boolean mBindingAppsFinished = false;
    /// @}

    /// M: Save current CellLayout bounds before workspace.changeState(CellLayout will be scaled).
    private Rect mCurrentBounds = new Rect();
    
    /// M: Used to popup long press widget to add message.
    private Toast mLongPressWidgetToAddToast;

    /// M: Used to force reload when loading workspace
    private boolean mIsLoadingWorkspace;

    /// M: flag to indicate whether the orientation has changed.
    private boolean mOrientationChanged;

    /// M: flag to indicate whether the pages in app customized pane were recreated.
    private boolean mPagesWereRecreated;
//add GPBLY-657 20130702 start
    private static boolean hasShow = false;
//add GPBLY-657 20130702 end


//renxinquan add start -----SuNing applist sort

// 0:default
// 1:abc
// 2:time
static public void setSortType(int type,Context context){
		final SharedPreferences prefs = context.getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), Context.MODE_PRIVATE);
				prefs.edit().putInt("SuNing",type).commit();
}
static public int getSortType(Context context){
	final SharedPreferences prefs = context.getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), Context.MODE_PRIVATE);
		return prefs.getInt("SuNing",0);
}
//renxinquan add end  -----SuNing applist sort


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        //renxinquan add start page count
        if(FeatureOption.RGT_PAGECOUNT_EDIT_SUPPORT){
	        if(SCREEN_COUNT == -1)
	        	SCREEN_COUNT = Utilities.getPageViewCount(this);
        }else{
        	SCREEN_COUNT = 5;
        }
        //renxinquan add end page count
        LauncherApplication app = ((LauncherApplication)getApplication());
        mSharedPrefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(),
                Context.MODE_PRIVATE);
        mModel = app.setLauncher(this);
        /// M: added for unread feature, load and bind unread info.
        if (FeatureOption.MTK_LAUNCHER_UNREAD_SUPPORT) {
            mUnreadLoader = app.getUnreadLoader();
            mUnreadLoader.loadAndInitUnreadShortcuts();    
        }
//add GPBLY-657 20130702 start
	final SharedPreferences prefs = getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), Context.MODE_PRIVATE);
//add GPBYL-837 20130731
if ((FeatureOption.RLK_GP810H_F1_ZX_SUPPORT) || (FeatureOption.RLK_GP810H_J1_ZX_SUPPORT) || (FeatureOption.RLK_GP811H_A1_SUPPORT)) {
        if (!hasShow && prefs.getBoolean(BOOL_SHOW_NOTICE, true)) {
            // GLLYSW-614 chenbo 20120126 (end)
            final AlertDialog alert = new AlertDialog.Builder(this).setTitle(R.string.title_bool_show_notice)
                    .setPositiveButton(R.string.title_bool_show_nitice_one, new OnClickListener() {


                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Editor editor = prefs.edit();
                            editor.putBoolean(BOOL_SHOW_NOTICE, false);
                            editor.commit();
                        }
                    }).setNegativeButton(R.string.title_bool_show_nitice_once, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }).setIcon(android.R.drawable.ic_dialog_info).setMessage(R.string.title_bool_show_nitice_info).create();
            alert.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    hasShow = true;
                }
            });
            // add
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }
}
//add GPBLY-657 20130702 end
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mInflater = getLayoutInflater();
//add zhaojiangwei test
	mContext= getApplicationContext();
	mCheckServer = new CheckServer(this);	
	inPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.POWER_MODE_ON, 0) != 0;
//add zhaojiangwei test
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onCreate: savedInstanceState = " + savedInstanceState
                    + ", mModel = " + mModel + ", mIconCache = " + mIconCache + ", this = " + this
                    + ", sLocaleChanged = " + sLocaleChanged);
        }
        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        // If we are getting an onCreate, we can actually preempt onResume and unset mPaused here,
        // this also ensures that any synchronous binding below doesn't re-trigger another
        // LauncherModel load.
        mPaused = false;

        if (PROFILE_STARTUP) {
            android.os.Debug.startMethodTracing(
                    Environment.getExternalStorageDirectory() + "/launcher");
        }
        
        checkForLocaleChange();
        setContentView(R.layout.launcher);
        setupViews();
        showFirstRunWorkspaceCling();

        registerContentObservers();

        lockAllApps();

        mSavedState = savedInstanceState;
        restoreState(mSavedState);

        // Update customization drawer _after_ restoring the states
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated();
        }

        if (PROFILE_STARTUP) {
            android.os.Debug.stopMethodTracing();
        }

        if (!mRestoring) {
            /// M: Reset load state if locale changed before.
            if (sLocaleChanged) {
                mModel.resetLoadedState(true, true);
                sLocaleChanged = false;
            }
            mIsLoadingWorkspace = true;
            if (sPausedFromUserAction) {
                // If the user leaves launcher, then we should just load items asynchronously when
                // they return.
                mModel.startLoader(true, -1);
            } else {
                // We only load the page synchronously if the user rotates (or triggers a
                // configuration change) while launcher is in the foreground
                mModel.startLoader(true, mWorkspace.getCurrentPage());
            }
        }

        if (!mModel.isAllAppsLoaded()) {
            ViewGroup appsCustomizeContentParent = (ViewGroup) mAppsCustomizeContent.getParent();
            mInflater.inflate(R.layout.apps_customize_progressbar, appsCustomizeContentParent);
        }

        // For handling default keys
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);

        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);

        updateGlobalIcons();

        // On large interfaces, we want the screen to auto-rotate based on the current orientation
        unlockScreenOrientation(true);
        
        // M: Register orientation listener.
        registerOrientationListener();
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        sPausedFromUserAction = true;
    }

    private void updateGlobalIcons() {
        boolean searchVisible = false;
        boolean voiceVisible = false;
        // If we have a saved version of these external icons, we load them up immediately
        int coi = getCurrentOrientationIndexForGlobalIcons();
        if (sGlobalSearchIcon[coi] == null || sVoiceSearchIcon[coi] == null ||
                sAppMarketIcon[coi] == null) {
            updateAppMarketIcon();
            searchVisible = updateGlobalSearchIcon();
            voiceVisible = updateVoiceSearchIcon(searchVisible);
        }
        if (sGlobalSearchIcon[coi] != null) {
             updateGlobalSearchIcon(sGlobalSearchIcon[coi]);
             searchVisible = true;
        }
        if (sVoiceSearchIcon[coi] != null) {
            updateVoiceSearchIcon(sVoiceSearchIcon[coi]);
            voiceVisible = true;
        }
        if (sAppMarketIcon[coi] != null) {
            updateAppMarketIcon(sAppMarketIcon[coi]);
        }
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange();  // recursive, but now with a locale configuration
                }
            }.execute();
            return;
        }

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "checkForLocaleChange: previousLocale = " + previousLocale
                    + ", locale = " + locale + ", previousMcc = " + previousMcc + ", mcc = " + mcc
                    + ", previousMnc = " + previousMnc + ", mnc = " + mnc + ", localeChanged = "
                    + localeChanged + ", this = " + this);
        }

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            /// M: When locale changed, reset collator and flush caches.
            sLocaleChanged = localeChanged;
            mModel.setFlushCache();
            mIconCache.flush();

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            new Thread("WriteLocaleConfiguration") {
                @Override
                public void run() {
                    writeConfiguration(Launcher.this, localeConfiguration);
                }
            }.start();
        }
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            LauncherLog.d(TAG, "FileNotFoundException when read configuration.");
        } catch (IOException e) {
            LauncherLog.d(TAG, "IOException when read configuration.");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LauncherLog.d(TAG, "IOException when close file.");
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            LauncherLog.d(TAG, "FileNotFoundException when write configuration.");
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LauncherLog.d(TAG, "IOException when close file.");
                }
            }
        }
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    boolean isDraggingEnabled() {
        // We prevent dragging when we are loading the workspace as it is possible to pick up a view
        // that is subsequently removed from the workspace in startBinding().
        return !mModel.isLoadingWorkspace();
    }

    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
        synchronized (sLock) {
            sScreen = screen;
        }
    }

    /**
     * Returns whether we should delay spring loaded mode -- for shortcuts and widgets that have
     * a configuration step, this allows the proper animations to run after other transitions.
     */
    private boolean completeAdd(PendingAddArguments args) {
        boolean result = false;
        switch (args.requestCode) {
            case REQUEST_PICK_APPLICATION:
                completeAddApplication(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                break;
            case REQUEST_PICK_SHORTCUT:
                processShortcut(args.intent);
                break;
            case REQUEST_CREATE_SHORTCUT:
                completeAddShortcut(args.intent, args.container, args.screen, args.cellX,
                        args.cellY);
                result = true;
                break;
            case REQUEST_CREATE_APPWIDGET:
                int appWidgetId = args.intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                completeAddAppWidget(appWidgetId, args.container, args.screen, null, null);
                result = true;
                break;
            case REQUEST_PICK_WALLPAPER:
                // We just wanted the activity result here so we can clear mWaitingForResult
                break;
        }
        // Before adding this resetAddInfo(), after a shortcut was added to a workspace screen,
        // if you turned the screen off and then back while in All Apps, Launcher would not
        // return to the workspace. Clearing mAddInfo.container here fixes this issue
        resetAddInfo();
        return result;
    }

    @Override
    protected void onActivityResult(
            final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_CANCELED) {
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, mPendingAddInfo, null, mPendingAddWidgetInfo);
            }
            return;
        }
        boolean delayExitSpringLoadedMode = false;
        boolean isWidgetDrop = (requestCode == REQUEST_PICK_APPWIDGET ||
                requestCode == REQUEST_CREATE_APPWIDGET);
        mWaitingForResult = false;

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onActivityResult: requestCode = " + requestCode 
                    + ", resultCode = " + resultCode + ", data = " + data 
                    + ", mPendingAddInfo = " + mPendingAddInfo);
        }

        // We have special handling for widgets
        if (isWidgetDrop) {
            int appWidgetId = data != null ?
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (appWidgetId < 0) {
                Log.e(TAG, "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the \\" +
                        "widget configuration activity.");
                completeTwoStageWidgetDrop(RESULT_CANCELED, appWidgetId);
            } else {
                completeTwoStageWidgetDrop(resultCode, appWidgetId);
            }
            return;
        }

        // The pattern used here is that a user PICKs a specific application,
        // which, depending on the target, might need to CREATE the actual target.

        // For example, the user would PICK_SHORTCUT for "Music playlist", and we
        // launch over to the Music app to actually CREATE_SHORTCUT.
        if (resultCode == RESULT_OK && mPendingAddInfo.container != ItemInfo.NO_ID) {
            final PendingAddArguments args = new PendingAddArguments();
            args.requestCode = requestCode;
            args.intent = data;
            args.container = mPendingAddInfo.container;
            args.screen = mPendingAddInfo.screen;
            args.cellX = mPendingAddInfo.cellX;
            args.cellY = mPendingAddInfo.cellY;
            if (isWorkspaceLocked()) {
                sPendingAddList.add(args);
            } else {
                delayExitSpringLoadedMode = completeAdd(args);
            }
        }
        mDragLayer.clearAnimatedView();
        // Exit spring loaded mode if necessary after cancelling the configuration of a widget
        exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), delayExitSpringLoadedMode,
                null);
    }
    
    @Override 
    protected void onStart() {
        super.onStart();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onStart: this = " + this);
        }
        // Launch performance debug log.
        getWindow().getDecorView().getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);

        if (isAllAppsVisible() && mAppsCustomizeTabHost.getVisibility() == View.VISIBLE
                && mAppsCustomizeTabHost.getContentVisibility() == View.GONE) {
            mAppsCustomizeTabHost.setContentVisibility(View.VISIBLE);
        }
    }

    @Override 
    protected void onStop() {
        super.onStop();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onStop: this = " + this);
        }
        
        //zhuwei add
        if (isAppsItemUninstallState) {
        	cancelUninstallAppsItem(false);
        }
        //end

        // Launch performance debug log.
        getWindow().getDecorView().getViewTreeObserver().removeOnPostDrawListener(mPostDrawListener);
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeTwoStageWidgetDrop resultCode = " + resultCode + ", appWidgetId = " + appWidgetId
                    + ", mPendingAddInfo.screen = " + mPendingAddInfo.screen);
        }

        CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(mPendingAddInfo.screen);
        Runnable onCompleteRunnable = null;
        int animationType = 0;

        AppWidgetHostView boundWidget = null;
        if (resultCode == RESULT_OK) {
            animationType = Workspace.COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION;
            final AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                    mPendingAddWidgetInfo);
            boundWidget = layout;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    completeAddAppWidget(appWidgetId, mPendingAddInfo.container,
                            mPendingAddInfo.screen, layout, null);
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        } else if (resultCode == RESULT_CANCELED) {
            animationType = Workspace.CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION;
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    exitSpringLoadedDragModeDelayed((resultCode != RESULT_CANCELED), false,
                            null);
                }
            };
        }
        if (mDragLayer.getAnimatedView() != null) {
            mWorkspace.animateWidgetDrop(mPendingAddInfo, cellLayout,
                    (DragView) mDragLayer.getAnimatedView(), onCompleteRunnable,
                    animationType, boundWidget, true);
        } else {
            // The animated view may be null in the case of a rotation during widget configuration
            onCompleteRunnable.run();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onResume: mRestoring = " + mRestoring
                    + ", mOnResumeNeedsLoad = " + mOnResumeNeedsLoad + ",mOrientationChanged = "
                    + mOrientationChanged + ",mPagesAreRecreated = " + mPagesWereRecreated
                    + ", this = " + this);
        }
        
        //zhuwei add
        if (isAppsItemUninstallState) {
        	cancelUninstallAppsItem(true);

			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					startUninstallAppsItem();
				}
			};
			mHandler.postDelayed(r, 0);
        }
        //end
        

	//liyang sence_feature 0701 (on)
        if (FeatureOption.RLK_SCENE_MODE) {
                 //setDefaultLauncher();
        	 Intent setDefaultIntent = new Intent();
        	 setDefaultIntent.setAction("com.android.launcher.CLASSICS_LAUNCHER");
        	 sendBroadcast(setDefaultIntent);
        	 Log.d("liyang", "Launcher.onResume()--setDefaultLauncher");
	}
	//liyang sence_feature 0701 (off)

        /// M: if the orientation changed and remove views happened during Launcher
        /// activity paused, we need to re-sync all apps pages, because the views
        /// recreated after view removed would use landscape resources.
        if (mOrientationChanged && mPagesWereRecreated) {
            LauncherLog.d(TAG, "(Launcher)onResume: mOrientationChanged && mPagesWereRecreated");
            mAppsCustomizeContent.invalidateAppPages(mAppsCustomizeContent.getCurrentPage(), true);
        }
        resetReSyncFlags();

        /// M: Call the appropriate callback for the IMTKWidget on the current page when we resume Launcher.
        mWorkspace.onResumeWhenShown(mWorkspace.getCurrentPage());
        
        // Restore the previous launcher state
        if (mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (mOnResumeState == State.APPS_CUSTOMIZE) {
            showAllApps(false);
        }
        mOnResumeState = State.NONE;

        // Background was set to gradient in onPause(), restore to black if in all apps.
	//modify BUG_ID:GPBYL-141 20130527 yukai start
        //setWorkspaceBackground(mState == State.WORKSPACE);
	setWorkspaceBackground(true);
	//modify BUG_ID:GPBYL-141 20130527 yukai end
        // Process any items that were added while Launcher was away
        InstallShortcutReceiver.flushInstallQueue(this);

        mPaused = false;
        sPausedFromUserAction = false;
        if (mRestoring || mOnResumeNeedsLoad) {
            mWorkspaceLoading = true;
            mIsLoadingWorkspace = true;
            mModel.startLoader(true, -1);
            mRestoring = false;
            mOnResumeNeedsLoad = false;
        }

        // Reset the pressed state of icons that were locked in the press state while activities
        // were launching
        if (mWaitingForResume != null) {
            // Resets the previous workspace icon press state
            mWaitingForResume.setStayPressed(false);
        }
        if (mAppsCustomizeContent != null) {
            // Resets the previous all apps icon press state
            mAppsCustomizeContent.resetDrawableState();
        }
        // It is possible that widgets can receive updates while launcher is not in the foreground.
        // Consequently, the widgets will be inflated in the orientation of the foreground activity
        // (framework issue). On resuming, we ensure that any widgets are inflated for the current
        // orientation.
        getWorkspace().reinflateWidgetsIfNecessary();

        // Again, as with the above scenario, it's possible that one or more of the global icons
        // were updated in the wrong orientation.
        updateGlobalIcons();

        /// M: Enable orientation listener when we resume Launcher.
        enableOrientationListener();
    }

    @Override
    protected void onPause() {
        // NOTE: We want all transitions from launcher to act as if the wallpaper were enabled
        // to be consistent.  So re-enable the flag here, and we will re-disable it as necessary
        // when Launcher resumes and we are still in AllApps.
        updateWallpaperVisibility(true);

        super.onPause();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onPause: this = " + this);
        }

        /// M: Call the appropriate callback for the IMTKWidget on the current page when we pause Launcher.
        mWorkspace.onPauseWhenShown(mWorkspace.getCurrentPage());
        resetReSyncFlags();

        mPaused = true;
        mDragController.cancelDrag();
        mDragController.resetLastGestureUpTime();

        /// M: Disable the orientation listener when we pause Launcher.
        disableOrientationListener();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onRetainNonConfigurationInstance: mSavedState = "
                    + mSavedState + ", mSavedInstanceState = " + mSavedInstanceState);
        }

        // Flag the loader to stop early before switching
        mModel.stopLoader();
        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.surrender();
        }
        return Boolean.TRUE;
    }

    // We can't hide the IME if it was forced open.  So don't bother
    /*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            final InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            inputManager.hideSoftInputFromWindow(lp.token, 0, new android.os.ResultReceiver(new
                        android.os.Handler()) {
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Log.d(TAG, "ResultReceiver got resultCode=" + resultCode);
                        }
                    });
            Log.d(TAG, "called hideSoftInputFromWindow from onWindowFocusChanged");
        }
    }
    */

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int uniChar = event.getUnicodeChar();
        final boolean handled = super.onKeyDown(keyCode, event);
        final boolean isKeyNotWhitespace = uniChar > 0 && !Character.isWhitespace(uniChar);
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d(TAG, " onKeyDown: KeyCode = " + keyCode + ", KeyEvent = " + event
                    + ", uniChar = " + uniChar + ", handled = " + handled + ", isKeyNotWhitespace = "
                    + isKeyNotWhitespace);
        }

        if (!handled && acceptFilter() && isKeyNotWhitespace) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        }

        // Eat the long press event so the keyboard doesn't come up.
        if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            /// M: invalidate the option menu before menu pop ups. Since the
            // menu item count differs between workspace and app list, if we
            // press menu key in workspace, and then do it in app list,the
            // menu window will animate because window size changed. We add this
            // step to force re-create menu decor view, this would lower the
            // time duration of option menu pop ups. Also we could do it only
            // when the menu pop switch between workspace and app list.
            invalidateOptionsMenu();
        }

        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Given the integer (ordinal) value of a State enum instance, convert it to a variable of type
     * State
     */
    private static State intToState(int stateOrdinal) {
        State state = State.WORKSPACE;
        final State[] stateValues = State.values();
        for (int i = 0; i < stateValues.length; i++) {
            if (stateValues[i].ordinal() == stateOrdinal) {
                state = stateValues[i];
                break;
            }
        }
        return state;
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "restoreState: savedState = " + savedState);
        }

        if (savedState == null) {
            return;
        }

        State state = intToState(savedState.getInt(RUNTIME_STATE, State.WORKSPACE.ordinal()));
        if (state == State.APPS_CUSTOMIZE) {
            mOnResumeState = State.APPS_CUSTOMIZE;
        }

        int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if (currentScreen > -1) {
            mWorkspace.setCurrentPage(currentScreen);
        }

        final long pendingAddContainer = savedState.getLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, -1);
        final int pendingAddScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);

        if (pendingAddContainer != ItemInfo.NO_ID && pendingAddScreen > -1) {
            mPendingAddInfo.container = pendingAddContainer;
            mPendingAddInfo.screen = pendingAddScreen;
            mPendingAddInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            mPendingAddInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            mPendingAddInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            mPendingAddInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            mPendingAddWidgetInfo = savedState.getParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO);
            mWaitingForResult = true;
            mRestoring = true;
        }


        boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
        if (renameFolder) {
            long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            mFolderInfo = mModel.getFolderById(this, sFolders, id);
            mRestoring = true;
        }

        // Restore the AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String curTab = savedState.getString("apps_customize_currentTab");
            if (curTab != null) {
                mAppsCustomizeTabHost.setContentTypeImmediate(
                        mAppsCustomizeTabHost.getContentTypeForTabTag(curTab));
                mAppsCustomizeContent.loadAssociatedPages(
                        mAppsCustomizeContent.getCurrentPage());
            }

            int currentIndex = savedState.getInt("apps_customize_currentIndex");
            mAppsCustomizeContent.restorePageForIndex(currentIndex);
        }
    }

    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
        final DragController dragController = mDragController;

        mLauncherView = findViewById(R.id.launcher);
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mQsbDivider = findViewById(R.id.qsb_divider);
        mDockDivider = findViewById(R.id.dock_divider);

        mLauncherView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mWorkspaceBackgroundDrawable = getResources().getDrawable(R.drawable.workspace_bg);
        mBlackBackgroundDrawable = new ColorDrawable(Color.BLACK);

        // Setup the drag layer
        mDragLayer.setup(this, dragController);

        // Setup the hotseat
        mHotseat = (Hotseat) findViewById(R.id.hotseat);
        if (mHotseat != null) {
            mHotseat.setup(this);
        }

        // Setup the workspace
        mWorkspace.setHapticFeedbackEnabled(false);
        mWorkspace.setOnLongClickListener(this);
        mWorkspace.setup(dragController);
        dragController.addDragListener(mWorkspace);

        // Get the search/delete bar
        mSearchDropTargetBar = (SearchDropTargetBar) mDragLayer.findViewById(R.id.qsb_bar);

        // Setup AppsCustomize
        mAppsCustomizeTabHost = (AppsCustomizeTabHost) findViewById(R.id.apps_customize_pane);
        mAppsCustomizeContent = (AppsCustomizePagedView)
                mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
        mAppsCustomizeContent.setup(this, dragController);

	//GPBYB-314 liyang 20130911 add start
        if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
	    ImageButton imagebutton = (ImageButton)findViewById(R.id.suning_market);
	    imagebutton.setVisibility(View.VISIBLE);
	    imagebutton.setOnClickListener(this);
        }
	//GPBYB-314 liyang 20130911 add end
        
        //zhuwei add
        if (com.mediatek.common.featureoption.FeatureOption.FEATURE_GP811_UNINSTALL) {
        	mHasMenukey = ViewConfiguration.get(this).hasPermanentMenuKey();
        	createFakeMenu();
        }
        //end

        // Setup the drag controller (drop targets have to be added in reverse order in priority)
        dragController.setDragScoller(mWorkspace);
        dragController.setScrollView(mDragLayer);
        dragController.setMoveTarget(mWorkspace);
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
        }
    }
    

    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.mtk_application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        /// M: modified for unread feature, the icon is playced in a RelativeLayout,
        /// we should set click/longClick listener for the icon not the RelativeLayout.
        MTKShortcut shortcut = (MTKShortcut)mInflater.inflate(layoutResId, parent, false);
        shortcut.applyFromShortcutInfo(info, mIconCache);
        shortcut.mFavorite.setOnClickListener(this);
        shortcut.mFavorite.setOnLongClickListener(this);
        return shortcut;
    }

    /**
     * Add an application shortcut to the workspace.
     *
     * @param data The intent describing the application.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    void completeAddApplication(Intent data, long container, int screen, int cellX, int cellY) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeAddApplication: Intent = " + data
                    + ", container = " + container + ", screen = " + screen + ", cellX = " + cellX
                    + ", cellY = " + cellY);
        }    

        final int[] cellXY = mTmpAddItemCellCoordinates;
        final CellLayout layout = getCellLayout(container, screen);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
        } else if (!layout.findCellForSpan(cellXY, 1, 1)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        final ShortcutInfo info = mModel.getShortcutInfo(getPackageManager(), data, this);

        if (info != null) {
            info.setActivity(data.getComponent(), Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            info.container = ItemInfo.NO_ID;
            mWorkspace.addApplicationShortcut(info, layout, container, screen, cellXY[0], cellXY[1],
                    isWorkspaceLocked(), cellX, cellY);
        } else {
            Log.e(TAG, "Couldn't find ActivityInfo for selected application: " + data);
        }
    }

    /**
     * Add a shortcut to the workspace.
     *
     * @param data The intent describing the shortcut.
     * @param cellInfo The position on screen where to create the shortcut.
     */
    private void completeAddShortcut(Intent data, long container, int screen, int cellX,
            int cellY) {
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        CellLayout layout = getCellLayout(container, screen);

        boolean foundCellSpan = false;

        ShortcutInfo info = mModel.infoFromShortcutIntent(this, data, null);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeAddShortcut: info = " + info + ", data = " + data
                    + ", container = " + container + ", screen = " + screen + ", cellX = "
                    + cellX + ", cellY = " + cellY);
        }

        if (info == null) {
            return;
        }
        final View view = createShortcut(info);

        // First we check if we already know the exact location where we want to add this item.
        if (cellX >= 0 && cellY >= 0) {
            cellXY[0] = cellX;
            cellXY[1] = cellY;
            foundCellSpan = true;

            // If appropriate, either create a folder or add to an existing folder
            if (mWorkspace.createUserFolderIfNecessary(view, container, layout, cellXY, 0,
                    true, null,null)) {
                return;
            }
            DragObject dragObject = new DragObject();
            dragObject.dragInfo = info;
            if (mWorkspace.addToExistingFolderIfNecessary(view, layout, cellXY, 0, dragObject,
                    true)) {
                return;
            }
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, cellXY);
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, 1, 1);
        }

        if (!foundCellSpan) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        LauncherModel.addItemToDatabase(this, info, container, screen, cellXY[0], cellXY[1], false);

        if (mIsLoadingWorkspace) {
        	mModel.forceReload();
        }
        
        if (!mRestoring) {
            mWorkspace.addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1,
                    isWorkspaceLocked());
        }
    }

    static int[] getSpanForWidget(Context context, ComponentName component, int minWidth,
            int minHeight) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, component, null);
        // We want to account for the extra amount of padding that we are adding to the widget
        // to ensure that it gets the full amount of space that it has requested
        int requiredWidth = minWidth + padding.left + padding.right;
        int requiredHeight = minHeight + padding.top + padding.bottom;
        return CellLayout.rectToCell(context.getResources(), requiredWidth, requiredHeight, null);
    }

    static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo info) {
        return getSpanForWidget(context, info.provider, info.minResizeWidth, info.minResizeHeight);
    }

    static int[] getSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minWidth, info.minHeight);
    }

    static int[] getMinSpanForWidget(Context context, PendingAddWidgetInfo info) {
        return getSpanForWidget(context, info.componentName, info.minResizeWidth,
                info.minResizeHeight);
    }

    /**
     * Add a widget to the workspace.
     *
     * @param appWidgetId The app widget id
     * @param cellInfo The position on screen where to create the widget.
     */
    private void completeAddAppWidget(final int appWidgetId, long container, int screen,
            AppWidgetHostView hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "completeAddAppWidget: appWidgetId = " + appWidgetId
                    + ", container = " + container + ", screen = " + screen);
        }

        // Calculate the grid spans needed to fit this widget
        CellLayout layout = getCellLayout(container, screen);
        
        /// M: If screen is -1, layout will be null, replaced with currentDropLayout.
        if (layout == null) {
            layout = mWorkspace.getCurrentDropLayout();
        }

        int[] minSpanXY = getMinSpanForWidget(this, appWidgetInfo);
        int[] spanXY = getSpanForWidget(this, appWidgetInfo);

        // Try finding open space on Launcher screen
        // We have saved the position to which the widget was dragged-- this really only matters
        // if we are placing widgets on a "spring-loaded" screen
        int[] cellXY = mTmpAddItemCellCoordinates;
        int[] touchXY = mPendingAddInfo.dropPos;
        int[] finalSpan = new int[2];
        boolean foundCellSpan = false;
        if (mPendingAddInfo.cellX >= 0 && mPendingAddInfo.cellY >= 0) {
            cellXY[0] = mPendingAddInfo.cellX;
            cellXY[1] = mPendingAddInfo.cellY;
            spanXY[0] = mPendingAddInfo.spanX;
            spanXY[1] = mPendingAddInfo.spanY;
            foundCellSpan = true;
        } else if (touchXY != null) {
            // when dragging and dropping, just find the closest free spot
            int[] result = layout.findNearestVacantArea(
                    touchXY[0], touchXY[1], minSpanXY[0], minSpanXY[1], spanXY[0],
                    spanXY[1], cellXY, finalSpan);
            spanXY[0] = finalSpan[0];
            spanXY[1] = finalSpan[1];
            foundCellSpan = (result != null);
        } else {
            foundCellSpan = layout.findCellForSpan(cellXY, minSpanXY[0], minSpanXY[1]);
        }

        if (!foundCellSpan) {
            if (appWidgetId != -1) {
                // Deleting an app widget ID is a void call but writes to disk before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                    }
                }.start();
            }
            showOutOfSpaceMessage(isHotseatLayout(layout));
            return;
        }

        // Build Launcher-specific widget info and save to database
        LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId,
                appWidgetInfo.provider);
        launcherInfo.spanX = spanXY[0];
        launcherInfo.spanY = spanXY[1];
        launcherInfo.minSpanX = mPendingAddInfo.minSpanX;
        launcherInfo.minSpanY = mPendingAddInfo.minSpanY;

        LauncherModel.addItemToDatabase(this, launcherInfo,
                container, screen, cellXY[0], cellXY[1], false);

        if (mIsLoadingWorkspace) {
        	LauncherLog.d(TAG, "Just Loading Workspace, force reload");
        	mModel.forceReload();
        }

        if (!mRestoring) {
            if (hostView == null) {
                // Perform actual inflation because we're live
                launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            } else {
                // The AppWidgetHostView has already been inflated and instantiated
                launcherInfo.hostView = hostView;
            }

            launcherInfo.hostView.setTag(launcherInfo);
            launcherInfo.hostView.setVisibility(View.VISIBLE);
            launcherInfo.notifyWidgetSizeChanged(this);

            mWorkspace.addInScreen(launcherInfo.hostView, container, screen, cellXY[0], cellXY[1],
                    launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());

            addWidgetToAutoAdvanceIfNeeded(launcherInfo.hostView, appWidgetInfo);
        }
        resetAddInfo();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                LauncherLog.d(TAG, "ACTION_SCREEN_OFF: mPendingAddInfo = " + mPendingAddInfo
                        + ", mAppsCustomizeTabHost = " + mAppsCustomizeTabHost + ", this = " + this);
                mUserPresent = false;
                mDragLayer.clearAllResizeFrames();
                updateRunning();

                // Reset AllApps to its initial state only if we are not in the middle of
                // processing a multi-step drop
                if (mAppsCustomizeTabHost != null && mPendingAddInfo.container == ItemInfo.NO_ID) {
                    mAppsCustomizeTabHost.reset();
		    //GPBYL- liyang 20130521 (on)
                    //showWorkspace(false);
		    //GPBYL- liyang 20130521 (off)
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            }
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAttachedToWindow.");
        }

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mReceiver, filter);

        mAttached = true;
        mVisible = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onDetachedFromWindow.");
        }

        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
        // The following code used to be in onResume, but it turns out onResume is called when
        // you're in All Apps and click home to go to the workspace. onWindowVisibilityChanged
        // is a more appropriate event to handle
        if (mVisible) {
            mAppsCustomizeTabHost.onWindowVisible();
            if (!mWorkspaceLoading) {
                final ViewTreeObserver observer = mWorkspace.getViewTreeObserver();
                // We want to let Launcher draw itself at least once before we force it to build
                // layers on all the workspace pages, so that transitioning to Launcher from other
                // apps is nice and speedy. Usually the first call to preDraw doesn't correspond to
                // a true draw so we wait until the second preDraw call to be safe
                observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        // We delay the layer building a bit in order to give
                        // other message processing a time to run.  In particular
                        // this avoids a delay in hiding the IME if it was
                        // currently shown, because doing that may involve
                        // some communication back with the app.
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);

                        observer.removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }
            // When Launcher comes back to foreground, a different Activity might be responsible for
            // the app market intent, so refresh the icon
            updateAppMarketIcon();
            clearTypedText();
        }
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(ADVANCE_MSG);
        Message msg = mHandler.obtainMessage(ADVANCE_MSG);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                            (System.currentTimeMillis() - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(ADVANCE_MSG);
                mHandler.removeMessages(0); // Remove messages sent using postDelayed()
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ADVANCE_MSG) {
                int i = 0;
                for (View key: mWidgetsToAdvance.keySet()) {
                    final View v = key.findViewById(mWidgetsToAdvance.get(key).autoAdvanceViewId);
                    final int delay = mAdvanceStagger * i;
                    if (v instanceof Advanceable) {
                       postDelayed(new Runnable() {
                           public void run() {
                               ((Advanceable) v).advance();
                           }
                       }, delay);
                    }
                    i++;
                }
                sendAdvanceMessage(mAdvanceInterval);
            }
        }
    };

    void addWidgetToAutoAdvanceIfNeeded(View hostView, AppWidgetProviderInfo appWidgetInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addWidgetToAutoAdvanceIfNeeded hostView = " + hostView + ", appWidgetInfo = "
                    + appWidgetInfo);
        }

        if (appWidgetInfo == null || appWidgetInfo.autoAdvanceViewId == -1) {
            return;
        }
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    void removeWidgetToAutoAdvance(View hostView) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeWidgetToAutoAdvance hostView = " + hostView);
        }

        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "removeAppWidget launcherInfo = " + launcherInfo);
        }

        removeWidgetToAutoAdvance(launcherInfo.hostView);
        launcherInfo.hostView = null;
    }

    void showOutOfSpaceMessage(boolean isHotseatLayout) {
        int strId = (isHotseatLayout ? R.string.hotseat_out_of_space : R.string.out_of_space);
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT).show();
    }

    /**
     * M: Pop up message allows to you add only one IMTKWidget for the given AppWidgetInfo.
     *
     * @param info The information of the IMTKWidget.
     */
    void showOnlyOneWidgetMessage(PendingAddWidgetInfo info) {
        try {
            PackageManager pm = getPackageManager();
            String label = pm.getApplicationLabel(pm.getApplicationInfo(info.componentName.getPackageName(), 0)).toString();
            Toast.makeText(this, getString(R.string.one_video_widget, label), Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {
            LauncherLog.e(TAG, "Got NameNotFounceException when showOnlyOneWidgetMessage.", e);
        }
        // Exit spring loaded mode if necessary after adding the widget.
        exitSpringLoadedDragModeDelayed(false, false, null);
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();

        // Whatever we were doing is hereby canceled.
        mWaitingForResult = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onNewIntent: intent = " + intent);
        }
        
        //zhuwei add
        if (isAppsItemUninstallState) {
        	cancelUninstallAppsItem(true);
        }
        //end

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // also will cancel mWaitingForResult.
            closeSystemDialogs();

            final boolean alreadyOnHome =
                    ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                        != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            Runnable processIntent = new Runnable() {
                public void run() {
                    if (mWorkspace == null) {
                        // Can be cases where mWorkspace is null, this prevents a NPE
                        return;
                    }
                    Folder openFolder = mWorkspace.getOpenFolder();
                    // In all these cases, only animate if we're already on home
                    mWorkspace.exitWidgetResizeMode();
                    if (alreadyOnHome && mState == State.WORKSPACE && !mWorkspace.isTouchActive() &&
                            openFolder == null) {
                        /// M: Call the appropriate callback for the IMTKWidget on the current page
                        /// when press "Home" key move to default screen.
                        mWorkspace.moveOutAppWidget(mWorkspace.getCurrentPage());
                        mWorkspace.moveToDefaultScreen(true);
                    }

                    closeFolder();
                    exitSpringLoadedDragMode();

                    // If we are already on home, then just animate back to the workspace,
                    // otherwise, just wait until onResume to set the state back to Workspace
                    if (alreadyOnHome) {
                        showWorkspace(true);
                    } else {
                        mOnResumeState = State.WORKSPACE;
                    }

                    final View v = getWindow().peekDecorView();
                    if (v != null && v.getWindowToken() != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(
                                INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                    // Reset AllApps to its initial state
                    if (!alreadyOnHome && mAppsCustomizeTabHost != null) {
                        mAppsCustomizeTabHost.reset();
                    }
                }
            };

            if (alreadyOnHome && !mWorkspace.hasWindowFocus()) {
                // Delay processing of the intent to allow the status bar animation to finish
                // first in order to avoid janky animations.
                mWorkspace.postDelayed(processIntent, 350);
            } else {
                // Process the intent immediately.
                processIntent.run();
            }

        }
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onRestoreInstanceState: state = " + state
                    + ", mSavedInstanceState = " + mSavedInstanceState);
        }
        
        for (int page: mSynchronouslyBoundPages) {
            mWorkspace.restoreInstanceStateForChild(page);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, mWorkspace.getNextPage());
        super.onSaveInstanceState(outState);

        outState.putInt(RUNTIME_STATE, mState.ordinal());
        // We close any open folder since it will not be re-opened, and we need to make sure
        // this state is reflected.
        closeFolder();

        if (mPendingAddInfo.container != ItemInfo.NO_ID && mPendingAddInfo.screen > -1 &&
                mWaitingForResult) {
            outState.putLong(RUNTIME_STATE_PENDING_ADD_CONTAINER, mPendingAddInfo.container);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, mPendingAddInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, mPendingAddInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, mPendingAddInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, mPendingAddInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, mPendingAddInfo.spanY);
            outState.putParcelable(RUNTIME_STATE_PENDING_ADD_WIDGET_INFO, mPendingAddWidgetInfo);
        }

        if (mFolderInfo != null && mWaitingForResult) {
            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
        }

        // Save the current AppsCustomize tab
        if (mAppsCustomizeTabHost != null) {
            String currentTabTag = mAppsCustomizeTabHost.getCurrentTabTag();
            if (currentTabTag != null) {
                outState.putString("apps_customize_currentTab", currentTabTag);
            }
            int currentIndex = mAppsCustomizeContent.getSaveInstanceStateIndex();
            outState.putInt("apps_customize_currentIndex", currentIndex);
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, " onSaveInstanceState: outState = " + outState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "(Launcher)onDestroy: this = " + this);
        }
        
        //zhuwei add 
        if (isAppsItemUninstallState) {
			cancelUninstallAppsItem(true);
		}
        //end

        // Remove all pending runnables
        mHandler.removeMessages(ADVANCE_MSG);
        mHandler.removeMessages(0);
        mWorkspace.removeCallbacks(mBuildLayersRunnable);

        // Stop callbacks from LauncherModel
        LauncherApplication app = ((LauncherApplication) getApplication());
        mModel.stopLoader();
        app.setLauncher(null);

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        mAppWidgetHost = null;

        mWidgetsToAdvance.clear();

        TextKeyListener.getInstance().release();

        // Disconnect any of the callbacks and drawables associated with ItemInfos on the workspace
        // to prevent leaking Launcher activities on orientation change.
        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        getContentResolver().unregisterContentObserver(mWidgetObserver);
        unregisterReceiver(mCloseSystemDialogsReceiver);

        mDragLayer.clearAllResizeFrames();
        ((ViewGroup) mWorkspace.getParent()).removeAllViews();
        mWorkspace.removeAllViews();
        mWorkspace = null;
        mDragController = null;

        LauncherAnimUtils.onDestroyActivity();
        
        /// M: Disable orientation listener when launcher is destroyed.
        disableOrientationListener();
    }

    public DragController getDragController() {
        return mDragController;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) {
            mWaitingForResult = true;
        }
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startSearch.");
        }
        showWorkspace(true);

        if (initialQuery == null) {
            // Use any text typed in the launcher as the initial query
            initialQuery = getTypedText();
        }
        if (appSearchData == null) {
            appSearchData = new Bundle();
            appSearchData.putString(Search.SOURCE, "launcher-search");
        }
        Rect sourceBounds = new Rect();
        if (mSearchDropTargetBar != null) {
            sourceBounds = mSearchDropTargetBar.getSearchBarBounds();
        }

        startGlobalSearch(initialQuery, selectInitialQuery,
            appSearchData, sourceBounds);
    }

    /**
     * Starts the global search activity. This code is a copied from SearchManager
     */
    public void startGlobalSearch(String initialQuery,
            boolean selectInitialQuery, Bundle appSearchData, Rect sourceBounds) {
        final SearchManager searchManager =
            (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w(TAG, "No global search activity found.");
            return;
        }
        Intent intent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(globalSearchActivity);
        // Make sure that we have a Bundle to put source in
        if (appSearchData == null) {
            appSearchData = new Bundle();
        } else {
            appSearchData = new Bundle(appSearchData);
        }
        // Set source to package name of app that starts global search, if not set already.
        if (!appSearchData.containsKey("source")) {
            appSearchData.putString("source", getPackageName());
        }
        intent.putExtra(SearchManager.APP_DATA, appSearchData);
        if (!TextUtils.isEmpty(initialQuery)) {
            intent.putExtra(SearchManager.QUERY, initialQuery);
        }
        if (selectInitialQuery) {
            intent.putExtra(SearchManager.EXTRA_SELECT_QUERY, selectInitialQuery);
        }
        intent.setSourceBounds(sourceBounds);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Global search activity not found: " + globalSearchActivity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//renxinquan add start
		if(FeatureOption.RLK_GP818H_A1_SN_SUPPORT || FeatureOption.FEATURE_GP811_UNINSTALL){
			if(isWorkspaceLocked() && !(mAppsCustomizeTabHost.getVisibility() == View.VISIBLE)){
				return false;
			}
		} else {
			if (isWorkspaceLocked()) {
            	            return false;
        	        }
                }
//renxinquan add end
        super.onCreateOptionsMenu(menu);

        Intent manageApps = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
        manageApps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        String helpUrl = getString(R.string.help_url);
        Intent help = new Intent(Intent.ACTION_VIEW, Uri.parse(helpUrl));
        help.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        
        //zhuwei add 
        if (com.mediatek.common.featureoption.FeatureOption.FEATURE_GP811_UNINSTALL) {
        	 mUnInstallItem = menu.add(MENU_GROUP_UNINSTALL, MENU_UNINSTALL, 0, R.string.delete_target_uninstall_label);
             mCancelUnstallItem = menu.add(MENU_GROUP_UNINSTALL, MENU_UNINSTALL_CANCEL, 0, R.string.cancel_action);
             mCancelUnstallItem.setVisible(false);
        }
        //end
        

        menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
            .setIcon(android.R.drawable.ic_menu_gallery)
            .setAlphabeticShortcut('W');
        menu.add(0, MENU_MANAGE_APPS, 0, R.string.menu_manage_apps)
            .setIcon(android.R.drawable.ic_menu_manage)
            .setIntent(manageApps)
            .setAlphabeticShortcut('M');
        menu.add(0, MENU_SYSTEM_SETTINGS, 0, R.string.menu_settings)
            .setIcon(android.R.drawable.ic_menu_preferences)
            .setIntent(settings)
            .setAlphabeticShortcut('P');

      //liyang sence_feature 0701 (on)
        if (FeatureOption.RLK_SCENE_MODE) {
        	menu.add(2, MENU_SYSTEM_SETTINGS+2, 0, R.string.menu_scene)
        	.setAlphabeticShortcut('P');
		}
      //liyang sence_feature 0701 (off)
//add zhaojiangwei test
	if (FeatureOption.RLK_POWER_MODE){
		inPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.POWER_MODE_ON, 0) != 0;
		if(inPowerMode){
		menu.add(4, MENU_SYSTEM_SETTINGS+4, 0, R.string.menu_normal_mode)
		    .setAlphabeticShortcut('P');
		}else{
		menu.add(4, MENU_SYSTEM_SETTINGS+4, 0, R.string.menu_power_mode)
		    .setAlphabeticShortcut('P');		
		}
		}
//add zhaojiangwei test
//renxinquan add start page count
        if(FeatureOption.RGT_PAGECOUNT_EDIT_SUPPORT){
        	menu.add(3, MENU_SYSTEM_SETTINGS+3, 0, R.string.screen_edit)
        	.setAlphabeticShortcut('P');
        }
//renxinquan add end page count
        if (!helpUrl.isEmpty()) {
            menu.add(0, MENU_HELP, 0, R.string.menu_help)
                .setIcon(android.R.drawable.ic_menu_help)
                .setIntent(help)
                .setAlphabeticShortcut('H');
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mAppsCustomizeTabHost.isTransitioning()) {
            return false;
        }
        boolean allAppsVisible = (mAppsCustomizeTabHost.getVisibility() == View.VISIBLE);
        menu.setGroupVisible(MENU_GROUP_WALLPAPER, !allAppsVisible);
        
        //zhuwei add 
        boolean isAppsTab = mAppsCustomizeTabHost.getCurrentTabTag().equalsIgnoreCase("APPS");
        int menuGroupId = -1;
        if (menu.size() != 0) {
            menuGroupId = menu.getItem(0).getGroupId();
        }
        if (isAppsTab && allAppsVisible) {
        	if (menuGroupId == MENU_GROUP_UNINSTALL) {
        		menu.setGroupVisible(MENU_GROUP_UNINSTALL, true);
        		if (isAppsItemUninstallState) {
        			if (mCancelUnstallItem != null) {
        				mCancelUnstallItem.setVisible(true);
        			}
        			if (mUnInstallItem != null) {
        				mUnInstallItem.setVisible(false);
        			}
        		} else {
        			if (mCancelUnstallItem != null) {
        				mCancelUnstallItem.setVisible(false);
        			}
        			if (mUnInstallItem != null) {
        				mUnInstallItem.setVisible(true);
        			}
        		}
        	}
        } else {
        	if (menuGroupId == MENU_GROUP_UNINSTALL) {
        		menu.setGroupVisible(MENU_GROUP_UNINSTALL, false);
        	}
        }
        //end
        

      //liyang sence_feature 0701 (on)
//add zhaojiangwei test
	if (FeatureOption.RLK_SCENE_MODE) {
		if (FeatureOption.RLK_POWER_MODE){
			inPowerMode = Settings.Global.getInt(mContext.getContentResolver(),Settings.Global.POWER_MODE_ON, 0) != 0;
			if(inPowerMode){
				menu.setGroupVisible(2, false);
				menu.setGroupVisible(3, false);
			}else{
				menu.setGroupVisible(2, !allAppsVisible);
	      		}	
	   	}else{
			menu.setGroupVisible(2, !allAppsVisible);
		}
	}
	if (FeatureOption.RLK_POWER_MODE){
	menu.setGroupVisible(4, !allAppsVisible);
	}
//add zhaojiangwei test
      //liyang sence_feature 0701 (off)
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
      //zhuwei add 
        case MENU_UNINSTALL:
        	if (canShowUninstallMode()) {
        		startUninstallAppsItem();
        		if (mUnInstallItem != null) {
        			mUnInstallItem.setVisible(false);
        		}
        		if (mCancelUnstallItem != null) {
        			mCancelUnstallItem.setVisible(true);
        		}
        	}
        	return true;
        case MENU_UNINSTALL_CANCEL:
        	if (canShowUninstallMode()) {
        		cancelUninstallAppsItem(true);
        		if (mUnInstallItem != null) {
        			mUnInstallItem.setVisible(true);
        		}
        		if (mCancelUnstallItem != null) {
        			mCancelUnstallItem.setVisible(false);
        		}
        	}
        	return true;
        //end
        
        case MENU_WALLPAPER_SETTINGS:
            startWallpaper();
            return true;
        //liyang sence_feature 0701 (on)
        case MENU_SYSTEM_SETTINGS+2:
	if (FeatureOption.RLK_SCENE_MODE) {
		Intent sceneMode = new Intent();
        	sceneMode.setAction(Intent.ACTION_MAIN);
        	sceneMode.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	sceneMode.setComponent(new ComponentName("com.rlk.scene","com.rlk.scene.MainActivity"));
		startActivity(sceneMode);
	}	
            return true;
        //liyang sence_feature 0701 (off)
	//add zhaojiangwei test
        case MENU_SYSTEM_SETTINGS+4:
		if (FeatureOption.RLK_POWER_MODE){
		inPowerSwitchPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.SWITCH_POWER_MODE, 0) != 0;
		inMenuSwitchPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.SWITCH_POWER_MODE2, 0) != 0;
		if((inMenuSwitchPowerMode && (mProgressDialog!=null && mProgressDialog.isShowing()))||  inPowerSwitchPowerMode){
		Toast.makeText(mContext, R.string.is_switch_warning, Toast.LENGTH_SHORT).show();
		}else{
		inPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.POWER_MODE_ON, 0) != 0;
		TelephonyManager telephonyManager =
		(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		if(telephonyManager!=null){
	    	mCallState = telephonyManager.getCallState();
		}
		if(mCallState != TelephonyManager.CALL_STATE_OFFHOOK){
	       	 	Settings.Global.putInt(
			mContext.getContentResolver(),
			Settings.Global.SWITCH_POWER_MODE2,
			1);
		    //add by zhuwei 20130730 BUG_ID:GESYSW-885
			closeFolder();
			//end	
			if(inPowerMode) {
				mCheckServer.restore_setting();
			} else 
		                mCheckServer.start_setting();
	
       	 	Settings.Global.putInt(
	        mContext.getContentResolver(),
	        Settings.Global.POWER_MODE_ON,
	        !inPowerMode ? 1 : 0);

		/*GESYSW-897 zhaojiangwei 0730 +++ */
		removeAllRecentTasksList();
		/*GESYSW-897 zhaojiangwei 0730 --- */		
		}else{
		Toast.makeText(mContext, R.string.incall_warning, Toast.LENGTH_SHORT).show();
		}
		}
		}
            return true;
	//add zhaojiangwei test
	//renxinquan add start page count
	case MENU_SYSTEM_SETTINGS+3:
		show_add_del_pagecount_dialog();
		return true;
	//show_add_del_pagecount_dialog
	//renxinquan add send page count
	
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        // Use a custom animation for launching search
        return true;
    }

    public boolean isWorkspaceLocked() {
        return mWorkspaceLoading || mWaitingForResult;
    }
//add zhaojiangwei test
    public void createProgressDialog() {
	if(mProgressDialog ==null){
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage(getResources().getString(R.string.is_switch_warning));
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
	}

    public void showProgressDialog(boolean show) {
	if (mProgressDialog != null) {
		if (show) {
			if (!mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}
		} else {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
		       	 	Settings.Global.putInt(
				mContext.getContentResolver(),
				Settings.Global.SWITCH_POWER_MODE2,
				0);
			}
		}
	}
	}

    public void removeAllRecentTasksList(){
	final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
	final List<ActivityManager.RecentTaskInfo> recentTasks =
                am.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        boolean isRemoveSuccess = false;
		//Log.i("zjw", "zjw removeAllRecentTasksList");
	for(int i = 0; i < recentTasks.size(); i++){
		Intent intent = new Intent(recentTasks.get(i).baseIntent);
		boolean islauncherApp = intent.getComponent().getPackageName().equals("com.android.launcher");
		boolean isAlarmApp = intent.getComponent().getPackageName().equals("com.android.deskclock");

		if (!islauncherApp && !isAlarmApp){
			am.removeTask(recentTasks.get(i).persistentId,ActivityManager.REMOVE_TASK_KILL_PROCESS);
		}
	}

	createProgressDialog();

	showProgressDialog(true);

	mDelayHandler.postDelayed(new Runnable() {
		public void run() {
			showProgressDialog(false);
		}
	}, 5000);
		
    }
//add zhaojiangwei test
    private void resetAddInfo() {
        mPendingAddInfo.container = ItemInfo.NO_ID;
        mPendingAddInfo.screen = -1;
        mPendingAddInfo.cellX = mPendingAddInfo.cellY = -1;
        mPendingAddInfo.spanX = mPendingAddInfo.spanY = -1;
        mPendingAddInfo.minSpanX = mPendingAddInfo.minSpanY = -1;
        mPendingAddInfo.dropPos = null;
    }

    void addAppWidgetImpl(final int appWidgetId, ItemInfo info, AppWidgetHostView boundWidget,
            AppWidgetProviderInfo appWidgetInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addAppWidgetImpl: appWidgetId = " + appWidgetId
                    + ", info = " + info + ", boundWidget = " + boundWidget 
                    + ", appWidgetInfo = " + appWidgetInfo);
        }

        if (appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;

            // Launch over to configure widget, if needed
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            // Otherwise just add it
            completeAddAppWidget(appWidgetId, info.container, info.screen, boundWidget,
                    appWidgetInfo);
            // Exit spring loaded mode if necessary after adding the widget
            exitSpringLoadedDragModeDelayed(true, false, null);
        }
    }

    /**
     * Process a shortcut drop.
     *
     * @param componentName The name of the component
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void processShortcutFromDrop(ComponentName componentName, long container, int screen,
            int[] cell, int[] loc) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "processShortcutFromDrop componentName = " + componentName + ", container = " + container
                    + ", screen = " + screen);
        }

        resetAddInfo();
        mPendingAddInfo.container = container;
        mPendingAddInfo.screen = screen;
        mPendingAddInfo.dropPos = loc;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }

        Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        createShortcutIntent.setComponent(componentName);
        processShortcut(createShortcutIntent);
    }

    /**
     * Process a widget drop.
     *
     * @param info The PendingAppWidgetInfo of the widget being added.
     * @param screen The screen where it should be added
     * @param cell The cell it should be added to, optional
     * @param position The location on the screen where it was dropped, optional
     */
    void addAppWidgetFromDrop(PendingAddWidgetInfo info, long container, int screen,
            int[] cell, int[] span, int[] loc) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addAppWidgetFromDrop: info = " + info + ", container = " + container + ", screen = "
                    + screen);
        }

        resetAddInfo();
        mPendingAddInfo.container = info.container = container;
        mPendingAddInfo.screen = info.screen = screen;
        mPendingAddInfo.dropPos = loc;
        mPendingAddInfo.minSpanX = info.minSpanX;
        mPendingAddInfo.minSpanY = info.minSpanY;

        if (cell != null) {
            mPendingAddInfo.cellX = cell[0];
            mPendingAddInfo.cellY = cell[1];
        }
        if (span != null) {
            mPendingAddInfo.spanX = span[0];
            mPendingAddInfo.spanY = span[1];
        }

        AppWidgetHostView hostView = info.boundWidget;
        int appWidgetId;
        if (hostView != null) {
            appWidgetId = hostView.getAppWidgetId();
            addAppWidgetImpl(appWidgetId, info, hostView, info.info);
        } else {
            // In this case, we either need to start an activity to get permission to bind
            // the widget, or we need to start an activity to configure the widget, or both.
            appWidgetId = getAppWidgetHost().allocateAppWidgetId();
            Bundle options = info.bindOptions;

            boolean success = false;
            if (options != null) {
                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                        info.componentName, options);
            } else {
                success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                        info.componentName);
            }
            if (success) {
                addAppWidgetImpl(appWidgetId, info, null, info.info);
            } else {
                mPendingAddWidgetInfo = info.info;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.componentName);
                // TODO: we need to make sure that this accounts for the options bundle.
                // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
                startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    void processShortcut(Intent intent) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "processShortcut: applicationName = " + applicationName
                    + ", shortcutName = " + shortcutName + ", intent = " + intent);
        }

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_application));
            startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
        } else {
            startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
        }
    }

    void processWallpaper(Intent intent) {
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    FolderIcon addFolder(CellLayout layout, long container, final int screen, int cellX,
            int cellY) {
        final FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);

        // Update the model
        LauncherModel.addItemToDatabase(Launcher.this, folderInfo, container, screen, cellX, cellY,
                false);
        sFolders.put(folderInfo.id, folderInfo);

        // Create the view
        FolderIcon newFolder =
            FolderIcon.fromXml(R.layout.folder_icon, this, layout, folderInfo, mIconCache);
        mWorkspace.addInScreen(newFolder, container, screen, cellX, cellY, 1, 1,
                isWorkspaceLocked());
        return newFolder;
    }

    void removeFolder(FolderInfo folder) {
        sFolders.remove(folder.id);
    }

    private void startWallpaper() {
        showWorkspace(true);
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        // NOTE: Adds a configure option to the chooser if the wallpaper supports it
        //       Removed in Eclair MR1
//        WallpaperManager wm = (WallpaperManager)
//                getSystemService(Context.WALLPAPER_SERVICE);
//        WallpaperInfo wi = wm.getWallpaperInfo();
//        if (wi != null && wi.getSettingsActivity() != null) {
//            LabeledIntent li = new LabeledIntent(getPackageName(),
//                    R.string.configure_wallpaper, 0);
//            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
//            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
//        }
        startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                true, mWidgetObserver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (LauncherLog.DEBUG_KEY) {
            LauncherLog.d(TAG, "dispatchKeyEvent: keyEvent = " + event);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (isPropertyEnabled(DUMP_STATE_PROPERTY)) {
                        dumpState();
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "Back key pressed, mState = " + mState + ", mOnResumeState = " + mOnResumeState);
        }
        
        //zhuwei add 
        if (isAppsItemUninstallState) {
        	cancelUninstallAppsItem(true);
        	return;
        }
        //end
        
    	if (isAllAppsVisible()) {
    		showWorkspace(true);
        } else if (mWorkspace.getOpenFolder() != null) {
            Folder openFolder = mWorkspace.getOpenFolder();
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            } else {
                closeFolder();
            }
        } else {
            mWorkspace.exitWidgetResizeMode();

            // Back button is a no-op here, but give at least some feedback for the button press
            mWorkspace.showOutlinesTemporarily();
        }
        /// M: Cancel long press widget to add message.
        cancelLongPressWidgetToAddMessage();
    }

    /**
     * Re-listen when widgets are reset.
     */
    private void onAppWidgetReset() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onAppWidgetReset.");
        }

        if (mAppWidgetHost != null) {
            mAppWidgetHost.startListening();
        }
    }

    /**
     * Launches the intent referred by the clicked shortcut.
     *
     * @param v The view representing the clicked shortcut.
     */
    public void onClick(View v) {
        // Make sure that rogue clicks don't get through while allapps is launching, or after the
        // view has detached (it's possible for this to happen if the view is removed mid touch).
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "Click on view " + v);
        }

        if (v.getWindowToken() == null) {
            LauncherLog.d(TAG, "Click on a view with no window token, directly return.");
            return;
        }

        if (!mWorkspace.isFinishedSwitchingState()) {
            LauncherLog.d(TAG, "The workspace is in switching state when clicking on view, directly return.");
            return;
        }

	//GPBYB-314 liyang 20130911 add start
	if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
	    if (v.getId() == R.id.suning_market){
			try {
				Intent intent = new Intent();
	            intent.setComponent(new ComponentName("com.suning.markethd", "com.suning.markethd.activity.StartActivity"));
	            //GPBYB-357 liyang 20130927 add start
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//GPBYB-357 liyang 20130927 add end
		        startActivity(intent);
			}catch (Exception e) {
				Toast.makeText(getApplicationContext(), getString(R.string.suning_application), Toast.LENGTH_LONG).show();
			}
        }
	}
       //GPBYB-314 liyang 20130911 add end

        Object tag = v.getTag();
        //renxinquan add start page count
        if(FeatureOption.RGT_PAGECOUNT_EDIT_SUPPORT){
          if(tag instanceof myTag){
          	//remove_page_add_del_count(((myTag) tag).id);
          	if(((myTag) tag).id == -1)
          		addCellLayoutToWorkspace();
          	else
          		remove_page_screen(((myTag) tag).id);
          }
        }
        //renxinquan add end page count
        if (tag instanceof ShortcutInfo) {
            // Open shortcut
            final Intent intent = ((ShortcutInfo) tag).intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));

            boolean success = startActivitySafely(v, intent, tag);

            if (success && v instanceof BubbleTextView) {
                mWaitingForResume = (BubbleTextView) v;
                mWaitingForResume.setStayPressed(true);
            }
        } else if (tag instanceof FolderInfo) {
            if (v instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) v;
                handleFolderClick(fi);
            }
        } else if (v == mAllAppsButton) {
            if (isAllAppsVisible()) {
                showWorkspace(true);
            } else {
                onClickAllAppsButton(v);
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        // this is an intercepted event being forwarded from mWorkspace;
        // clicking anywhere on the workspace causes the customization drawer to slide down
        showWorkspace(true);
        return false;
    }

    /**
     * Event handler for the search button
     *
     * @param v The view that was clicked.
     */
    public void onClickSearchButton(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onClickSearchButton v = " + v);
        }

        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        onSearchRequested();
    }

    /**
     * Event handler for the voice button
     *
     * @param v The view that was clicked.
     */
    public void onClickVoiceButton(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onClickVoiceButton v = " + v);
        }

        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        try {
            final SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            ComponentName activityName = searchManager.getGlobalSearchActivity();
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (activityName != null) {
                intent.setPackage(activityName.getPackageName());
            }
            startActivity(null, intent, "onClickVoiceButton");
            overridePendingTransition(R.anim.fade_in_fast, R.anim.fade_out_fast);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivitySafely(null, intent, "onClickVoiceButton");
        }
    }

    /**
     * Event handler for the "grid" button that appears on the home screen, which
     * enters all apps mode.
     *
     * @param v The view that was clicked.
     */
    public void onClickAllAppsButton(View v) {
        showAllApps(true);
    }

    public void onTouchDownAllAppsButton(View v) {
        // Provide the same haptic feedback that the system offers for virtual keys.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public void onClickAppMarketButton(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onClickAppMarketButton v = " + v + ", mAppMarketIntent = " + mAppMarketIntent);
        }

        if (mAppMarketIntent != null) {
            startActivitySafely(v, mAppMarketIntent, "app market");
        } else {
            Log.e(TAG, "Invalid app market intent.");
        }
    }

    void startApplicationDetailsActivity(ComponentName componentName) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startApplicationDetailsActivity: componentName = " + componentName);
        }

        String packageName = componentName.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivitySafely(null, intent, "startApplicationDetailsActivity");
    }

    void startApplicationUninstallActivity(ApplicationInfo appInfo) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startApplicationUninstallActivity: appInfo = " + appInfo);
        }

        if ((appInfo.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
            // System applications cannot be installed. For now, show a toast explaining that.
            // We may give them the option of disabling apps this way.
            int messageId = R.string.uninstall_system_app_text;
            Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
        } else {
            String packageName = appInfo.componentName.getPackageName();
            String className = appInfo.componentName.getClassName();
            Intent intent = new Intent(
                    Intent.ACTION_DELETE, Uri.fromParts("package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivity v = " + v + ", intent = " + intent + ", tag = " + tag);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            boolean useLaunchAnimation = (v != null) &&
                    !intent.hasExtra(INTENT_EXTRA_IGNORE_LAUNCH_ANIMATION);
//add zhaojiangwei test
	if(false){
	ComponentName componentName = intent.getComponent();
        //add componentName != null
        if (componentName != null) {
            String packageName =  componentName.getPackageName();
	    inPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.POWER_MODE_ON, 0) != 0;
	    if ("com.android.browser".equals(packageName) && inPowerMode){
		Toast.makeText(this, R.string.power_mode_warning, Toast.LENGTH_SHORT).show();
		return false;
	}
        }
	}
//add zhaojiangwei test
            if (useLaunchAnimation) {
                ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                        v.getMeasuredWidth(), v.getMeasuredHeight());

                startActivity(intent, opts.toBundle());
            } else {
                startActivity(intent);
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag=" + tag + " intent=" + intent, e);
        }
        return false;
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivitySafely v = " + v + ", intent = " + intent + ", tag = " + tag);
        }

        boolean success = false;
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }

    void startActivityForResultSafely(Intent intent, int requestCode) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startActivityForResultSafely: intent = " + intent
                    + ", requestCode = " + requestCode);
        }

        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    private void handleFolderClick(FolderIcon folderIcon) {
        final FolderInfo info = folderIcon.getFolderInfo();
        Folder openFolder = mWorkspace.getFolderForTag(info);

        // If the folder info reports that the associated folder is open, then verify that
        // it is actually opened. There have been a few instances where this gets out of sync.
        if (info.opened && openFolder == null) {
            Log.d(TAG, "Folder info marked as open, but associated folder is not open. Screen: "
                    + info.screen + " (" + info.cellX + ", " + info.cellY + ")");
            info.opened = false;
        }

        if (!info.opened && !folderIcon.getFolder().isDestroyed()) {
            // Close any open folder
            closeFolder();
            // Open the requested folder
            openFolder(folderIcon);
        } else {
            // Find the open folder...
            int folderScreen;
            if (openFolder != null) {
                folderScreen = mWorkspace.getPageForView(openFolder);
                // .. and close it
                closeFolder(openFolder);
                if (folderScreen != mWorkspace.getCurrentPage()) {
                    // Close any folder open on the current screen
                    closeFolder();
                    // Pull the folder onto this screen
                    openFolder(folderIcon);
                }
            }
        }
    }

    /**
     * This method draws the FolderIcon to an ImageView and then adds and positions that ImageView
     * in the DragLayer in the exact absolute location of the original FolderIcon.
     */
    private void copyFolderIconToImage(FolderIcon fi) {
        final int width = fi.getMeasuredWidth();
        final int height = fi.getMeasuredHeight();

        // Lazy load ImageView, Bitmap and Canvas
        if (mFolderIconImageView == null) {
            mFolderIconImageView = new ImageView(this);
        }
        if (mFolderIconBitmap == null || mFolderIconBitmap.getWidth() != width ||
                mFolderIconBitmap.getHeight() != height) {
            mFolderIconBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFolderIconCanvas = new Canvas(mFolderIconBitmap);
        }

        DragLayer.LayoutParams lp;
        if (mFolderIconImageView.getLayoutParams() instanceof DragLayer.LayoutParams) {
            lp = (DragLayer.LayoutParams) mFolderIconImageView.getLayoutParams();
        } else {
            lp = new DragLayer.LayoutParams(width, height);
        }

        // The layout from which the folder is being opened may be scaled, adjust the starting
        // view size by this scale factor.
        float scale = mDragLayer.getDescendantRectRelativeToSelf(fi, mRectForFolderAnimation);
        lp.customPosition = true;
        lp.x = mRectForFolderAnimation.left;
        lp.y = mRectForFolderAnimation.top;
        lp.width = (int) (scale * width);
        lp.height = (int) (scale * height);

        mFolderIconCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        fi.draw(mFolderIconCanvas);
        mFolderIconImageView.setImageBitmap(mFolderIconBitmap);
        if (fi.getFolder() != null) {
            mFolderIconImageView.setPivotX(fi.getFolder().getPivotXForIconAnimation());
            mFolderIconImageView.setPivotY(fi.getFolder().getPivotYForIconAnimation());
        }
        // Just in case this image view is still in the drag layer from a previous animation,
        // we remove it and re-add it.
        if (mDragLayer.indexOfChild(mFolderIconImageView) != -1) {
            mDragLayer.removeView(mFolderIconImageView);
        }
        mDragLayer.addView(mFolderIconImageView, lp);
        if (fi.getFolder() != null) {
            fi.getFolder().bringToFront();
        }
    }

    private void growAndFadeOutFolderIcon(FolderIcon fi) {
        if (fi == null) {
            return;
        }
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.5f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.5f);

        FolderInfo info = (FolderInfo) fi.getTag();
        if (info.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            CellLayout cl = (CellLayout) fi.getParent().getParent();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) fi.getLayoutParams();
            cl.setFolderLeaveBehindCell(lp.cellX, lp.cellY);
        }

        // Push an ImageView copy of the FolderIcon into the DragLayer and hide the original
        copyFolderIconToImage(fi);
        fi.setVisibility(View.INVISIBLE);

        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.start();
    }

    private void shrinkAndFadeInFolderIcon(final FolderIcon fi) {
        if (fi == null) return;
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);

        final CellLayout cl = (CellLayout) fi.getParent().getParent();

        // We remove and re-draw the FolderIcon in-case it has changed
        mDragLayer.removeView(mFolderIconImageView);
        copyFolderIconToImage(fi);
        ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderIconImageView, alpha,
                scaleX, scaleY);
        oa.setDuration(getResources().getInteger(R.integer.config_folderAnimDuration));
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (cl != null) {
                    cl.clearFolderLeaveBehind();
                    // Remove the ImageView copy of the FolderIcon and make the original visible.
                    mDragLayer.removeView(mFolderIconImageView);
                    fi.setVisibility(View.VISIBLE);
                }
            }
        });
        oa.start();
    }

    /**
     * Opens the user folder described by the specified tag. The opening of the folder
     * is animated relative to the specified View. If the View is null, no animation
     * is played.
     *
     * @param folderInfo The FolderInfo describing the folder to open.
     */
    public void openFolder(FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        FolderInfo info = folder.mInfo;

        info.opened = true;

        // Just verify that the folder hasn't already been added to the DragLayer.
        // There was a one-off crash where the folder had a parent already.
        if (folder.getParent() == null) {
            mDragLayer.addView(folder);
            mDragController.addDropTarget((DropTarget) folder);
        } else {
            Log.w(TAG, "Opening folder (" + folder + ") which already has a parent (" +
                    folder.getParent() + ").");
        }
        folder.animateOpen();
        growAndFadeOutFolderIcon(folderIcon);
    }

    public void closeFolder() {
        Folder folder = mWorkspace.getOpenFolder();
        if (folder != null) {
            if (folder.isEditingName()) {
                folder.dismissEditingName();
            }
            closeFolder(folder);

            // Dismiss the folder cling
            dismissFolderCling(null);
        }
    }

    void closeFolder(Folder folder) {
        folder.getInfo().opened = false;

        ViewGroup parent = (ViewGroup) folder.getParent().getParent();
        if (parent != null) {
            FolderIcon fi = (FolderIcon) mWorkspace.getViewForTag(folder.mInfo);
            shrinkAndFadeInFolderIcon(fi);
        }
        folder.animateClosed();
    }

    public boolean onLongClick(View v) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onLongClick: View = " + v + ", v.getTag() = " + v.getTag()
                    + ", mState = " + mState);
        }

        if (!isDraggingEnabled()) {
            LauncherLog.d(TAG, "onLongClick: isDraggingEnabled() = " + isDraggingEnabled());
            return false;
        }

        if (isWorkspaceLocked()) {
            LauncherLog.d(TAG, "onLongClick: isWorkspaceLocked() mWorkspaceLoading " + mWorkspaceLoading
                    + ", mWaitingForResult = " + mWaitingForResult);
            return false;
        }

        if (mState != State.WORKSPACE) {
            LauncherLog.d(TAG, "onLongClick: mState != State.WORKSPACE: = " + mState);
            return false;
        }

        /// M: modidfied for Unread feature, to find CellLayout through while loop.
        while (!(v instanceof CellLayout)) {
            v = (View) v.getParent();
        }

        resetAddInfo();
        CellLayout.CellInfo longClickCellInfo = (CellLayout.CellInfo) v.getTag();
        // This happens when long clicking an item with the dpad/trackball
        if (longClickCellInfo == null) {
            return true;
        }

        // The hotseat touch handling does not go through Workspace, and we always allow long press
        // on hotseat items.
        final View itemUnderLongClick = longClickCellInfo.cell;
        boolean allowLongPress = isHotseatLayout(v) || mWorkspace.allowLongPress();
        if (allowLongPress && !mDragController.isDragging()) {
            if (itemUnderLongClick == null) {
                // User long pressed on empty space
                mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                startWallpaper();
            } else {
                if (!(itemUnderLongClick instanceof Folder)) {
                    /// M: Call the appropriate callback for the IMTKWidget on the current page
                    /// when long click and begin to drag IMTKWidget.
                    mWorkspace.startDragAppWidget(mWorkspace.getCurrentPage());
                    // User long pressed on an item
                    mWorkspace.startDrag(longClickCellInfo);
                }
            }
        }
        return true;
    }

    boolean isHotseatLayout(View layout) {
        return mHotseat != null && layout != null &&
                (layout instanceof CellLayout) && (layout == mHotseat.getLayout());
    }
    
    Hotseat getHotseat() {
        return mHotseat;
    }
    
    SearchDropTargetBar getSearchBar() {
        return mSearchDropTargetBar;
    }

    /**
     * Returns the CellLayout of the specified container at the specified screen.
     */
    CellLayout getCellLayout(long container, int screen) {
        if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            if (mHotseat != null) {
                return mHotseat.getLayout();
            } else {
                return null;
            }
        } else {
            return (CellLayout) mWorkspace.getChildAt(screen);
        }
    }

    Workspace getWorkspace() {
        return mWorkspace;
    }

    // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
    @Override
    public boolean isAllAppsVisible() {
        return (mState == State.APPS_CUSTOMIZE) || (mOnResumeState == State.APPS_CUSTOMIZE);
    }

    @Override
    public boolean isAllAppsButtonRank(int rank) {
        return mHotseat.isAllAppsButtonRank(rank);
    }

    /**
     * Helper method for the cameraZoomIn/cameraZoomOut animations
     * @param view The view being animated
     * @param scaleFactor The scale factor used for the zoom
     */
    private void setPivotsForZoom(View view, float scaleFactor) {
        view.setPivotX(view.getWidth() / 2.0f);
        view.setPivotY(view.getHeight() / 2.0f);
    }

    void disableWallpaperIfInAllApps() {
        // Only disable it if we are in all apps
        if (isAllAppsVisible()) {
            if (mAppsCustomizeTabHost != null &&
                    !mAppsCustomizeTabHost.isTransitioning()) {
				//GPBYB-243 liyang 20130829 add start
				if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
					updateWallpaperVisibility(true);
				}else{
					updateWallpaperVisibility(false);
				}
				//GPBYB-243 liyang 20130829 add end
            }
        }
    }
/*GPBYL-873 shuchenlin remark the code begin*/
    private void setWorkspaceBackground(boolean workspace) {
       // mLauncherView.setBackground(workspace ?
       //         mWorkspaceBackgroundDrawable : mBlackBackgroundDrawable);
    }
/*GPBYL-873 shuchenlin end*/
    void updateWallpaperVisibility(boolean visible) {
	//liyang sence_feature 0701 (on)
	if(!FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }
        setWorkspaceBackground(visible);
	}
	//liyang sence_feature 0701 (off)
    }

    private void dispatchOnLauncherTransitionPrepare(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionPrepare(this, animated, toWorkspace);
        }
    }

    private void dispatchOnLauncherTransitionStart(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStart(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    private void dispatchOnLauncherTransitionStep(View v, float t) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionStep(this, t);
        }
    }

    private void dispatchOnLauncherTransitionEnd(View v, boolean animated, boolean toWorkspace) {
        if (v instanceof LauncherTransitionable) {
            ((LauncherTransitionable) v).onLauncherTransitionEnd(this, animated, toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Things to test when changing the following seven functions.
     *   - Home from workspace
     *          - from center screen
     *          - from other screens
     *   - Home from all apps
     *          - from center screen
     *          - from other screens
     *   - Back from all apps
     *          - from center screen
     *          - from other screens
     *   - Launch app from workspace and quit
     *          - with back
     *          - with home
     *   - Launch app from all apps and quit
     *          - with back
     *          - with home
     *   - Go to a screen that's not the default, then all
     *     apps, and launch and app, and go back
     *          - with back
     *          -with home
     *   - On workspace, long press power and go back
     *          - with back
     *          - with home
     *   - On all apps, long press power and go back
     *          - with back
     *          - with home
     *   - On workspace, power off
     *   - On all apps, power off
     *   - Launch an app and turn off the screen while in that app
     *          - Go back with home key
     *          - Go back with back key  TODO: make this not go to workspace
     *          - From all apps
     *          - From workspace
     *   - Enter and exit car mode (becuase it causes an extra configuration changed)
     *          - From all apps
     *          - From the center workspace
     *          - From another workspace
     */

    /**
     * Zoom the camera out from the workspace to reveal 'toView'.
     * Assumes that the view to show is anchored at either the very top or very bottom
     * of the screen.
     */
    private void showAppsCustomizeHelper(final boolean animated, final boolean springLoaded) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "showAppsCustomizeHelper animated = " + animated + ", springLoaded = " + springLoaded);
        }

        if (mStateAnimation != null) {
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        final Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomInTime);
        final int fadeDuration = res.getInteger(R.integer.config_appsCustomizeFadeInTime);
        final float scale = (float) res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mWorkspace;
        final AppsCustomizeTabHost toView = mAppsCustomizeTabHost;
        final int startDelay =
                res.getInteger(R.integer.config_workspaceAppsCustomizeAnimationStagger);

        setPivotsForZoom(toView, scale);

        // Shrink workspaces away if going to AppsCustomize from workspace
        Animator workspaceAnim =
                mWorkspace.getChangeStateAnimation(Workspace.State.SMALL, animated);

		//GPBYB-243 liyang 20130829 add start
		if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
			//and hide hotseat and dock divider.
			hideHotseat(false);
			if (mDockDivider != null)
				mDockDivider.setVisibility(View.INVISIBLE);
			if (mWorkspace != null)
				mWorkspace.setVisibility(View.INVISIBLE);
		}
		//GPBYB-243 liyang 20130829 add end
        if (animated) {
            toView.setScaleX(scale);
            toView.setScaleY(scale);
            final LauncherViewPropertyAnimator scaleAnim = new LauncherViewPropertyAnimator(toView);
            scaleAnim.
                scaleX(1f).scaleY(1f).
                setDuration(duration).
                setInterpolator(new Workspace.ZoomOutInterpolator());

            toView.setVisibility(View.VISIBLE);
            toView.setAlpha(0f);
            final ObjectAnimator alphaAnim = ObjectAnimator
                .ofFloat(toView, "alpha", 0f, 1f)
                .setDuration(fadeDuration);
            alphaAnim.setInterpolator(new DecelerateInterpolator(1.5f));
            alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation == null) {
                        throw new RuntimeException("animation is null");
                    }
                    float t = (Float) animation.getAnimatedValue();
                    dispatchOnLauncherTransitionStep(fromView, t);
                    dispatchOnLauncherTransitionStep(toView, t);
                }
            });

            // toView should appear right at the end of the workspace shrink
            // animation
            mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            mStateAnimation.play(scaleAnim).after(startDelay);
            mStateAnimation.play(alphaAnim).after(startDelay);

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                boolean animationCancelled = false;

                @Override
                public void onAnimationStart(Animator animation) {
                    updateWallpaperVisibility(true);
                    // Prepare the position
                    toView.setTranslationX(0.0f);
                    toView.setTranslationY(0.0f);
                    toView.setVisibility(View.VISIBLE);
                    toView.bringToFront();
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromView, animated, false);
                    dispatchOnLauncherTransitionEnd(toView, animated, false);

                    if (mWorkspace != null && !springLoaded && !LauncherApplication.isScreenLarge()) {
                        // Hide the workspace scrollbar
                        mWorkspace.hideScrollingIndicator(true);
                        hideDockDivider();
                    }
                    if (!animationCancelled) {
						//GPBYB-243 liyang 20130829 add start
						if (!FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
							updateWallpaperVisibility(false);
						}
						//GPBYB-243 liyang 20130829 add end
                    }

                    // Hide the search bar
                    if (mSearchDropTargetBar != null) {
                        mSearchDropTargetBar.hideSearchBar(false);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationCancelled = true;
                }
            });

            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }

            boolean delayAnim = false;
            final ViewTreeObserver observer;

            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);

            // If any of the objects being animated haven't been measured/laid out
            // yet, delay the animation until we get a layout pass
            if ((((LauncherTransitionable) toView).getContent().getMeasuredWidth() == 0) ||
                    (mWorkspace.getMeasuredWidth() == 0) ||
                    (toView.getMeasuredWidth() == 0)) {
                observer = mWorkspace.getViewTreeObserver();
                delayAnim = true;
            } else {
                observer = null;
            }

            final AnimatorSet stateAnimation = mStateAnimation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mStateAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mStateAnimation != stateAnimation)
                        return;
                    setPivotsForZoom(toView, scale);
                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);
                    toView.post(new Runnable() {
                        public void run() {
                            // Check that mStateAnimation hasn't changed while
                            // we waited for a layout/draw pass
                            if (mStateAnimation != stateAnimation)
                                return;
                            mStateAnimation.start();
                        }
                    });
                }
            };
            if (delayAnim) {
                final OnGlobalLayoutListener delayedStart = new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        toView.post(startAnimRunnable);
                        observer.removeOnGlobalLayoutListener(this);
                    }
                };
                observer.addOnGlobalLayoutListener(delayedStart);
            } else {
                startAnimRunnable.run();
            }
        } else {
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setVisibility(View.VISIBLE);
            toView.bringToFront();

            if (!springLoaded && !LauncherApplication.isScreenLarge()) {
                // Hide the workspace scrollbar
                mWorkspace.hideScrollingIndicator(true);
                hideDockDivider();

                // Hide the search bar
                if (mSearchDropTargetBar != null) {
                    mSearchDropTargetBar.hideSearchBar(false);
                }
            }
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
			//GPBYB-243 liyang 20130829 add start
			if (!FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
				updateWallpaperVisibility(false);
			}
			//GPBYB-243 liyang 20130829 add end
        }
    }

    /**
     * Zoom the camera back into the workspace, hiding 'fromView'.
     * This is the opposite of showAppsCustomizeHelper.
     * @param animated If true, the transition will be animated.
     */
    private void hideAppsCustomizeHelper(State toState, final boolean animated,
            final boolean springLoaded, final Runnable onCompleteRunnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "hideAppsCustomzieHelper toState = " + toState + ", animated = " + animated
                    + ", springLoaded = " + springLoaded);
        }
        
         //zhuwei add 
        if (isAppsItemUninstallState) {
        	Log.i("zhuwei", "hideAppsCustomizeHelper->cancelUninstallAppsItem");
			cancelUninstallAppsItem(true);
		}
        //end
        

        if (mStateAnimation != null) {
            mStateAnimation.cancel();
            mStateAnimation = null;
        }
        Resources res = getResources();

        final int duration = res.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        final int fadeOutDuration =
                res.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        final float scaleFactor = (float)
                res.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final View fromView = mAppsCustomizeTabHost;
        final View toView = mWorkspace;
        Animator workspaceAnim = null;

        if (toState == State.WORKSPACE) {
            int stagger = res.getInteger(R.integer.config_appsCustomizeWorkspaceAnimationStagger);
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.NORMAL, animated, stagger);
        } else if (toState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            workspaceAnim = mWorkspace.getChangeStateAnimation(
                    Workspace.State.SPRING_LOADED, animated);
        }

        setPivotsForZoom(fromView, scaleFactor);
        updateWallpaperVisibility(true);
        showHotseat(animated);
		//GPBYB-243 liyang 20130829 add start
		if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
			if (mDockDivider != null)
			mDockDivider.setVisibility(View.VISIBLE);
		}
		//GPBYB-243 liyang 20130829 add end
        if (animated) {
            final LauncherViewPropertyAnimator scaleAnim =
                    new LauncherViewPropertyAnimator(fromView);
            scaleAnim.
                scaleX(scaleFactor).scaleY(scaleFactor).
                setDuration(duration).
                setInterpolator(new Workspace.ZoomInInterpolator());

            final ObjectAnimator alphaAnim = ObjectAnimator
                .ofFloat(fromView, "alpha", 1f, 0f)
                .setDuration(fadeOutDuration);
            alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = 1f - (Float) animation.getAnimatedValue();
                    dispatchOnLauncherTransitionStep(fromView, t);
                    dispatchOnLauncherTransitionStep(toView, t);
                }
            });

            mStateAnimation = LauncherAnimUtils.createAnimatorSet();

            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            mAppsCustomizeContent.pauseScrolling();

            mStateAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    updateWallpaperVisibility(true);
                    fromView.setVisibility(View.GONE);
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    if (mWorkspace != null) {
                        mWorkspace.hideScrollingIndicator(false);
                    }
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }
                    mAppsCustomizeContent.updateCurrentPageScroll();
                    mAppsCustomizeContent.resumeScrolling();
                }
            });

            mStateAnimation.playTogether(scaleAnim, alphaAnim);
            if (workspaceAnim != null) {
                mStateAnimation.play(workspaceAnim);
            }
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            final Animator stateAnimation = mStateAnimation;
            mWorkspace.post(new Runnable() {
                public void run() {
                    if (stateAnimation != mStateAnimation)
                        return;
                    mStateAnimation.start();
                }
            });
        } else {
            fromView.setVisibility(View.GONE);
            dispatchOnLauncherTransitionPrepare(fromView, animated, true);
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
            dispatchOnLauncherTransitionPrepare(toView, animated, true);
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
            mWorkspace.hideScrollingIndicator(false);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "onTrimMemory: level = " + level);
        }

        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            mAppsCustomizeTabHost.onTrimMemory();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            // When another window occludes launcher (like the notification shade, or recents),
            // ensure that we enable the wallpaper flag so that transitions are done correctly.
            updateWallpaperVisibility(true);
        } else {
            // When launcher has focus again, disable the wallpaper if we are in AllApps
            mWorkspace.postDelayed(new Runnable() {
                @Override
                public void run() {
                    disableWallpaperIfInAllApps();
                }
            }, 500);
        }
    }

    void showWorkspace(boolean animated) {
        /// M: Call the appropriate callback for the IMTKWidget on the current page when leave all apps list back to
        /// workspace.
        mWorkspace.stopCovered(mWorkspace.getCurrentPage());
        showWorkspace(animated, null);
    }

    void showWorkspace(boolean animated, Runnable onCompleteRunnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "showWorkspace: animated = " + animated + ", mState = " + mState);
        }

        if (mState != State.WORKSPACE) {
            boolean wasInSpringLoadedMode = (mState == State.APPS_CUSTOMIZE_SPRING_LOADED);
            mWorkspace.setVisibility(View.VISIBLE);
            hideAppsCustomizeHelper(State.WORKSPACE, animated, false, onCompleteRunnable);

            // Show the search bar (only animate if we were showing the drop target bar in spring
            // loaded mode)
            if (mSearchDropTargetBar != null) {
                mSearchDropTargetBar.showSearchBar(wasInSpringLoadedMode);
            }

            // We only need to animate in the dock divider if we're going from spring loaded mode
            showDockDivider(animated && wasInSpringLoadedMode);

            // Set focus to the AppsCustomize button
            if (mAllAppsButton != null) {
                mAllAppsButton.requestFocus();
            }
        }

        mWorkspace.flashScrollingIndicator(animated);

        // Change the state *after* we've called all the transition code
        mState = State.WORKSPACE;

        // Resume the auto-advance of widgets
        mUserPresent = true;
        updateRunning();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void showAllApps(boolean animated) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "showAllApps: animated = " + animated + ", mState = " + mState
                    + ", mCurrentBounds = " + mCurrentBounds);
        }
        if (mState != State.WORKSPACE) return;
        
        /// M: Recorder current bounds of current cellLayout.
        if (mWorkspace != null) {
            mDragLayer.getDescendantRectRelativeToSelf(mWorkspace.getCurrentDropLayout(), mCurrentBounds);
        }

        /// M: Call the appropriate callback for the IMTKWidget on the current page when enter all apps list.
        mWorkspace.startCovered(mWorkspace.getCurrentPage());
        showAppsCustomizeHelper(animated, false);
        mAppsCustomizeTabHost.requestFocus();

        // Change the state *after* we've called all the transition code
        mState = State.APPS_CUSTOMIZE;

        // Pause the auto-advance of widgets until we are out of AllApps
        mUserPresent = false;
        updateRunning();
        closeFolder();

        // Send an accessibility event to announce the context change
        getWindow().getDecorView()
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void enterSpringLoadedDragMode() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "enterSpringLoadedDragMode mState = " + mState + ", mOnResumeState = " + mOnResumeState);
        }
        
    	if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(State.APPS_CUSTOMIZE_SPRING_LOADED, true, true, null);
            hideDockDivider();
            mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    void exitSpringLoadedDragModeDelayed(final boolean successfulDrop, boolean extendedDelay,
            final Runnable onCompleteRunnable) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "exitSpringLoadedDragModeDelayed successfulDrop = " + successfulDrop + ", extendedDelay = "
                    + extendedDelay + ", mState = " + mState);
        }

        if (mState != State.APPS_CUSTOMIZE_SPRING_LOADED) {
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (successfulDrop) {
                    // Before we show workspace, hide all apps again because
                    // exitSpringLoadedDragMode made it visible. This is a bit hacky; we should
                    // clean up our state transition functions
                    mAppsCustomizeTabHost.setVisibility(View.GONE);
                    showWorkspace(true, onCompleteRunnable);
                } else {
                    exitSpringLoadedDragMode();
                }
            }
        }, (extendedDelay ?
                EXIT_SPRINGLOADED_MODE_LONG_TIMEOUT :
                EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT));
    }

    void exitSpringLoadedDragMode() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "exitSpringLoadedDragMode mState = " + mState);
        }

        if (mState == State.APPS_CUSTOMIZE_SPRING_LOADED) {
            final boolean animated = true;
            final boolean springLoaded = true;
            showAppsCustomizeHelper(animated, springLoaded);
            mState = State.APPS_CUSTOMIZE;
        }
        // Otherwise, we are not in spring loaded mode, so don't do anything.
    }

    void hideDockDivider() {
        if (mQsbDivider != null && mDockDivider != null) {
            mQsbDivider.setVisibility(View.INVISIBLE);
            mDockDivider.setVisibility(View.INVISIBLE);
        }
    }

    void showDockDivider(boolean animated) {
        if (mQsbDivider != null && mDockDivider != null) {
        	//GPBYB-276 liyang 20130903 add start
			if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
				mQsbDivider.setVisibility(View.GONE);
			}else{
				mQsbDivider.setVisibility(View.VISIBLE);
			}
			//GPBYB-276 liyang 20130903 add end
            mDockDivider.setVisibility(View.VISIBLE);
            if (mDividerAnimator != null) {
                mDividerAnimator.cancel();
                mQsbDivider.setAlpha(1f);
                mDockDivider.setAlpha(1f);
                mDividerAnimator = null;
            }
            if (animated) {
                mDividerAnimator = LauncherAnimUtils.createAnimatorSet();
                mDividerAnimator.playTogether(LauncherAnimUtils.ofFloat(mQsbDivider, "alpha", 1f),
                        LauncherAnimUtils.ofFloat(mDockDivider, "alpha", 1f));
                int duration = 0;
                if (mSearchDropTargetBar != null) {
                    duration = mSearchDropTargetBar.getTransitionInDuration();
                }
                mDividerAnimator.setDuration(duration);
                mDividerAnimator.start();
            }
        }
    }

    void lockAllApps() {
        // TODO
    }

    void unlockAllApps() {
        // TODO
    }

    /**
     * Shows the hotseat area.
     */
    void showHotseat(boolean animated) {
        if (!LauncherApplication.isScreenLarge()) {
            if (animated) {
                if (mHotseat.getAlpha() != 1f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionInDuration();
                    }
                    mHotseat.animate().alpha(1f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(1f);
            }
        }
    }

    /**
     * Hides the hotseat area.
     */
    void hideHotseat(boolean animated) {
        if (!LauncherApplication.isScreenLarge()) {
            if (animated) {
                if (mHotseat.getAlpha() != 0f) {
                    int duration = 0;
                    if (mSearchDropTargetBar != null) {
                        duration = mSearchDropTargetBar.getTransitionOutDuration();
                    }
                    mHotseat.animate().alpha(0f).setDuration(duration);
                }
            } else {
                mHotseat.setAlpha(0f);
            }
        }
    }

    /**
     * Add an item from all apps or customize onto the given workspace screen.
     * If layout is null, add to the current screen.
     */
    void addExternalItemToScreen(ItemInfo itemInfo, final CellLayout layout) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "addExternalItemToScreen itemInfo = " + itemInfo + ", layout = " + layout);
        }

        if (!mWorkspace.addExternalItemToScreen(itemInfo, layout)) {
            showOutOfSpaceMessage(isHotseatLayout(layout));
        }
    }

    /** Maps the current orientation to an index for referencing orientation correct global icons */
    private int getCurrentOrientationIndexForGlobalIcons() {
        // default - 0, landscape - 1
        switch (getResources().getConfiguration().orientation) {
        case Configuration.ORIENTATION_LANDSCAPE:
            return 1;
        default:
            return 0;
        }
    }

    private Drawable getExternalPackageToolbarIcon(ComponentName activityName, String resourceName) {
        try {
            PackageManager packageManager = getPackageManager();
            // Look for the toolbar icon specified in the activity meta-data
            Bundle metaData = packageManager.getActivityInfo(
                    activityName, PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                int iconResId = metaData.getInt(resourceName);
                if (iconResId != 0) {
                    Resources res = packageManager.getResourcesForActivity(activityName);
                    return res.getDrawable(iconResId);
                }
            }
        } catch (NameNotFoundException e) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon; " + activityName.flattenToShortString() +
                    " not found", e);
        } catch (Resources.NotFoundException nfe) {
            // This can happen if the activity defines an invalid drawable
            Log.w(TAG, "Failed to load toolbar icon from " + activityName.flattenToShortString(),
                    nfe);
        }
        return null;
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateTextButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);
        Resources r = getResources();
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);

        TextView button = (TextView) findViewById(buttonId);
        // If we were unable to find the icon via the meta-data, use a generic one
        if (toolbarIcon == null) {
            toolbarIcon = r.getDrawable(fallbackDrawableId);
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return null;
        } else {
            toolbarIcon.setBounds(0, 0, w, h);
            if (button != null) {
                button.setCompoundDrawables(toolbarIcon, null, null, null);
            }
            return toolbarIcon.getConstantState();
        }
    }

    // if successful in getting icon, return it; otherwise, set button to use default drawable
    private Drawable.ConstantState updateButtonWithIconFromExternalActivity(
            int buttonId, ComponentName activityName, int fallbackDrawableId,
            String toolbarResourceName) {
        ImageView button = (ImageView) findViewById(buttonId);
        Drawable toolbarIcon = getExternalPackageToolbarIcon(activityName, toolbarResourceName);

        if (button != null) {
            // If we were unable to find the icon via the meta-data, use a
            // generic one
            if (toolbarIcon == null) {
                button.setImageResource(fallbackDrawableId);
            } else {
                button.setImageDrawable(toolbarIcon);
            }
        }

        return toolbarIcon != null ? toolbarIcon.getConstantState() : null;

    }

    private void updateTextButtonWithDrawable(int buttonId, Drawable d) {
        TextView button = (TextView) findViewById(buttonId);
        button.setCompoundDrawables(d, null, null, null);
    }

    private void updateButtonWithDrawable(int buttonId, Drawable.ConstantState d) {
        ImageView button = (ImageView) findViewById(buttonId);
        button.setImageDrawable(d.newDrawable(getResources()));
    }

    private void invalidatePressedFocusedStates(View container, View button) {
        if (container instanceof HolographicLinearLayout) {
            HolographicLinearLayout layout = (HolographicLinearLayout) container;
            layout.invalidatePressedFocusedStates();
        } else if (button instanceof HolographicImageView) {
            HolographicImageView view = (HolographicImageView) button;
            view.invalidatePressedFocusedStates();
        }
    }

    private boolean updateGlobalSearchIcon() {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);

        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName activityName = searchManager.getGlobalSearchActivity();
        if (activityName != null) {
            /// M: only show search engine name on non-OP01 projects.
            final boolean needUpdate = LauncherExtPlugin.getSearchButtonExt(this).needUpdateSearchButtonResource();
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "updateGlobalSearchIcon: needUpdate = " + needUpdate + ",activityName = " + activityName);
            }
            if (needUpdate) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                    TOOLBAR_SEARCH_ICON_METADATA_NAME);
                if (sGlobalSearchIcon[coi] == null) {
                    sGlobalSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                            R.id.search_button, activityName, R.drawable.ic_home_search_normal_holo,
                            TOOLBAR_ICON_METADATA_NAME);
                }
            } else {
                searchButton.setImageResource(R.drawable.ic_home_search_normal_holo);
            }

            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            invalidatePressedFocusedStates(searchButtonContainer, searchButton);
            return true;
        } else {
            // We disable both search and voice search when there is no global search provider
            if (searchButtonContainer != null) searchButtonContainer.setVisibility(View.GONE);
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private void updateGlobalSearchIcon(Drawable.ConstantState d) {
        final View searchButtonContainer = findViewById(R.id.search_button_container);
        final View searchButton = (ImageView) findViewById(R.id.search_button);
        updateButtonWithDrawable(R.id.search_button, d);
        invalidatePressedFocusedStates(searchButtonContainer, searchButton);
    }

    private boolean updateVoiceSearchIcon(boolean searchVisible) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        final View voiceButtonProxy = findViewById(R.id.voice_button_proxy);

        // We only show/update the voice search icon if the search icon is enabled as well
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName globalSearchActivity = searchManager.getGlobalSearchActivity();

        ComponentName activityName = null;
        if (globalSearchActivity != null) {
            // Check if the global search activity handles voice search
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            intent.setPackage(globalSearchActivity.getPackageName());
            activityName = intent.resolveActivity(getPackageManager());
        }

        if (activityName == null) {
            // Fallback: check if an activity other than the global search activity
            // resolves this
            Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            activityName = intent.resolveActivity(getPackageManager());
        }
        if (searchVisible && activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                    R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                    TOOLBAR_VOICE_SEARCH_ICON_METADATA_NAME);
            if (sVoiceSearchIcon[coi] == null) {
                sVoiceSearchIcon[coi] = updateButtonWithIconFromExternalActivity(
                        R.id.voice_button, activityName, R.drawable.ic_home_voice_search_holo,
                        TOOLBAR_ICON_METADATA_NAME);
            }
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.VISIBLE);
            voiceButton.setVisibility(View.VISIBLE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.VISIBLE);
            }
            invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
            return true;
        } else {
            if (voiceButtonContainer != null) voiceButtonContainer.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);
            if (voiceButtonProxy != null) {
                voiceButtonProxy.setVisibility(View.GONE);
            }
            return false;
        }
    }

    private void updateVoiceSearchIcon(Drawable.ConstantState d) {
        final View voiceButtonContainer = findViewById(R.id.voice_button_container);
        final View voiceButton = findViewById(R.id.voice_button);
        updateButtonWithDrawable(R.id.voice_button, d);
        invalidatePressedFocusedStates(voiceButtonContainer, voiceButton);
    }

    /**
     * Sets the app market icon.
     */
    private void updateAppMarketIcon() {
        final View marketButton = findViewById(R.id.market_button);
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MARKET);
        // Find the app market activity by resolving an intent.
        // (If multiple app markets are installed, it will return the ResolverActivity.)
        ComponentName activityName = intent.resolveActivity(getPackageManager());
        if (activityName != null) {
            int coi = getCurrentOrientationIndexForGlobalIcons();
            mAppMarketIntent = intent;
            sAppMarketIcon[coi] = updateTextButtonWithIconFromExternalActivity(
                    R.id.market_button, activityName, R.drawable.ic_launcher_market_holo,
                    TOOLBAR_ICON_METADATA_NAME);
			//GPBYB- liyang 20130918 start
			if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
				marketButton.setVisibility(View.GONE);
				marketButton.setEnabled(false);
			}else{
				marketButton.setVisibility(View.VISIBLE);
			}
			//GPBYB- liyang 20130918 end
        } else {
            // We should hide and disable the view so that we don't try and restore the visibility
            // of it when we swap between drag & normal states from IconDropTarget subclasses.
            marketButton.setVisibility(View.GONE);
            marketButton.setEnabled(false);
        }
    }

    private void updateAppMarketIcon(Drawable.ConstantState d) {
        // Ensure that the new drawable we are creating has the approprate toolbar icon bounds
        Resources r = getResources();
        Drawable marketIconDrawable = d.newDrawable(r);
        int w = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_width);
        int h = r.getDimensionPixelSize(R.dimen.toolbar_external_icon_height);
        marketIconDrawable.setBounds(0, 0, w, h);

        updateTextButtonWithDrawable(R.id.market_button, marketIconDrawable);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        final boolean result = super.dispatchPopulateAccessibilityEvent(event);
        final List<CharSequence> text = event.getText();
        text.clear();
        // Populate event with a fake title based on the current state.
        if (mState == State.APPS_CUSTOMIZE) {
            text.add(getString(R.string.all_apps_button_label));
        } else {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return result;
    }

    /**
     * Receives notifications when system dialogs are to be closed.
     */
    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "Close system dialogs: intent = " + intent);
            }
            closeSystemDialogs();
        }
    }

    /**
     * Receives notifications whenever the appwidgets are reset.
     */
    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            onAppWidgetReset();
        }
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader
     * in onResume.
     *
     * This needs to be called from incoming places where resources might have been loaded
     * while we are paused.  That is becaues the Configuration might be wrong
     * when we're not running, and if it comes back to what it was when we
     * were paused, we are not restarted.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused.  The caller might be able to
     * skip some work in that case since we will come back again.
     */
    public boolean setLoadOnResume() {
        if (mPaused) {
            LauncherLog.i(TAG, "setLoadOnResume: this = " + this);
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public int getCurrentWorkspaceScreen() {
        if (mWorkspace != null) {
            return mWorkspace.getCurrentPage();
        } else {
            return SCREEN_COUNT / 2;
        }
    }

    /**
     * Refreshes the shortcuts shown on the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void startBinding() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startBinding: this = " + this);
        }

        /// M: Cancel Drag when reload to avoid dragview lost parent and JE @{
        if (mDragController != null) {
            mDragController.cancelDrag();
        }
        /// M: }@

        final Workspace workspace = mWorkspace;

        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        mWorkspace.clearDropTargets();
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
            final CellLayout layoutParent = (CellLayout) workspace.getChildAt(i);
            layoutParent.removeAllViewsInLayout();
            layoutParent.requestChildLayout();  
        }
        workspace.invalidate();
        mWidgetsToAdvance.clear();
        if (mHotseat != null) {
            mHotseat.resetLayout();
        }
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "startBinding: mIsLoadingWorkspace = " + mIsLoadingWorkspace);
        }
        mIsLoadingWorkspace = false;
    }

    /**
     * Bind the items start-end from the list.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
        setLoadOnResume();

        // Get the list of added shortcuts and intersect them with the set of shortcuts here
        Set<String> newApps = new HashSet<String>();
        newApps = mSharedPrefs.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);

        Workspace workspace = mWorkspace;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "bindItems: start = " + start + ", end = " + end 
                        + "item = " + item + ", this = " + this);
            }

            // Short circuit if we are loading dock items for a configuration which has no dock
            if (item.container == LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                    mHotseat == null) {
                continue;
            }

            switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    String uri = info.intent.toUri(0).toString();
                    View shortcut = createShortcut(info);
                    workspace.addInScreen(shortcut, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    boolean animateIconUp = false;
                    synchronized (newApps) {
                        if (newApps.contains(uri)) {
                            animateIconUp = newApps.remove(uri);
                        }
                    }
                    if (animateIconUp) {
                        // Prepare the view to be animated up
                        shortcut.setAlpha(0f);
                        shortcut.setScaleX(0f);
                        shortcut.setScaleY(0f);
                        mNewShortcutAnimatePage = item.screen;
                        if (!mNewShortcutAnimateViews.contains(shortcut)) {
                            mNewShortcutAnimateViews.add(shortcut);
                        }
                    }
                    break;
                case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                    FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
                            (ViewGroup) workspace.getChildAt(workspace.getCurrentPage()),
                            (FolderInfo) item, mIconCache);
                    workspace.addInScreen(newFolder, item.container, item.screen, item.cellX,
                            item.cellY, 1, 1, false);
                    break;
            }
        }

        workspace.requestLayout();
    }

    /**
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindFolders(HashMap<Long, FolderInfo> folders) {
        setLoadOnResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindFolders: this = " + this);
        }
        sFolders.clear();
        sFolders.putAll(folders);
    }

    /**
     * Add the views for a widget to the workspace.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidget(LauncherAppWidgetInfo item) {
        setLoadOnResume();

        final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: " + item);
        }
        final Workspace workspace = mWorkspace;

        final int appWidgetId = item.appWidgetId;
        final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component "
                    + (appWidgetInfo == null ? "" : appWidgetInfo.provider));
        }
        
        /// M: If appWidgetInfo is null, appWidgetHostView will be error view,
        /// don't add in homescreen.
        if (appWidgetInfo == null) {
            return;
        }

        item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);

        item.hostView.setTag(item);
        item.onBindAppWidget(this);

        /// M: Call the appropriate callback for the IMTKWidget will be bound to workspace.
        mWorkspace.setAppWidgetIdAndScreen(item.hostView, mWorkspace.getCurrentPage(), appWidgetId);

        workspace.addInScreen(item.hostView, item.container, item.screen, item.cellX,
                item.cellY, item.spanX, item.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(item.hostView, appWidgetInfo);

        workspace.requestLayout();

        if (DEBUG_WIDGETS) {
            Log.d(TAG, "bound widget id=" + item.appWidgetId + " in " + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }

    public void onPageBoundSynchronously(int page) {
        mSynchronouslyBoundPages.add(page);
    }

    /**
     * Callback saying that there aren't any more items to bind.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void finishBindingItems() {
        setLoadOnResume();
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "finishBindingItems: mSavedState = " + mSavedState + ", mSavedInstanceState = "
                    + mSavedInstanceState + ", this = " + this);
        }

        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentPage()).requestFocus();
            }
            mSavedState = null;
        }

        mWorkspace.restoreInstanceStateForRemainingPages();

        // If we received the result of any pending adds while the loader was running (e.g. the
        // widget configuration forced an orientation change), process them now.
        for (int i = 0; i < sPendingAddList.size(); i++) {
            completeAdd(sPendingAddList.get(i));
        }
        sPendingAddList.clear();

        // Update the market app icon as necessary (the other icons will be managed in response to
        // package changes in bindSearchablesChanged()
        updateAppMarketIcon();

        // Animate up any icons as necessary
        if (mVisible || mWorkspaceLoading) {
            Runnable newAppsRunnable = new Runnable() {
                @Override
                public void run() {
                    runNewAppsAnimation(false);
                }
            };

            boolean willSnapPage = mNewShortcutAnimatePage > -1 &&
                    mNewShortcutAnimatePage != mWorkspace.getCurrentPage();
            if (canRunNewAppsAnimation()) {
                // If the user has not interacted recently, then either snap to the new page to show
                // the new-apps animation or just run them if they are to appear on the current page
                if (willSnapPage) {
                    mWorkspace.snapToPage(mNewShortcutAnimatePage, newAppsRunnable);
                } else {
                    runNewAppsAnimation(false);
                }
            } else {
                // If the user has interacted recently, then just add the items in place if they
                // are on another page (or just normally if they are added to the current page)
                runNewAppsAnimation(willSnapPage);
            }
        }

        mWorkspaceLoading = false;

        /// M: If unread information load completed, we need to bind it to workspace.        
        if (mUnreadLoadCompleted) {
            bindWorkspaceUnreadInfo();
        }
        mBindingWorkspaceFinished = true;
    }

    private boolean canRunNewAppsAnimation() {
        long diff = System.currentTimeMillis() - mDragController.getLastGestureUpTime();
        return diff > (NEW_APPS_ANIMATION_INACTIVE_TIMEOUT_SECONDS * 1000);
    }

    /**
     * Runs a new animation that scales up icons that were added while Launcher was in the
     * background.
     *
     * @param immediate whether to run the animation or show the results immediately
     */
    private void runNewAppsAnimation(boolean immediate) {
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        Collection<Animator> bounceAnims = new ArrayList<Animator>();

        // Order these new views spatially so that they animate in order
        Collections.sort(mNewShortcutAnimateViews, new Comparator<View>() {
            @Override
            public int compare(View a, View b) {
                CellLayout.LayoutParams alp = (CellLayout.LayoutParams) a.getLayoutParams();
                CellLayout.LayoutParams blp = (CellLayout.LayoutParams) b.getLayoutParams();
                int cellCountX = LauncherModel.getCellCountX();
                return (alp.cellY * cellCountX + alp.cellX) - (blp.cellY * cellCountX + blp.cellX);
            }
        });

        // Animate each of the views in place (or show them immediately if requested)
        if (immediate) {
            for (View v : mNewShortcutAnimateViews) {
                v.setAlpha(1f);
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
        } else {
            for (int i = 0; i < mNewShortcutAnimateViews.size(); ++i) {
                View v = mNewShortcutAnimateViews.get(i);
                ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat("alpha", 1f),
                        PropertyValuesHolder.ofFloat("scaleX", 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f));
                bounceAnim.setDuration(InstallShortcutReceiver.NEW_SHORTCUT_BOUNCE_DURATION);
                bounceAnim.setStartDelay(i * InstallShortcutReceiver.NEW_SHORTCUT_STAGGER_DELAY);
                bounceAnim.setInterpolator(new SmoothPagedView.OvershootInterpolator());
                bounceAnims.add(bounceAnim);
            }
            anim.playTogether(bounceAnims);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mWorkspace != null) {
                        mWorkspace.postDelayed(mBuildLayersRunnable, 500);
                    }
                }
            });
            anim.start();
        }

        // Clean up
        mNewShortcutAnimatePage = -1;
        mNewShortcutAnimateViews.clear();
        new Thread("clearNewAppsThread") {
            public void run() {
                mSharedPrefs.edit()
                            .putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1)
                            .putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, null)
                            .commit();
            }
        }.start();
    }

    @Override
    public void bindSearchablesChanged() {
        boolean searchVisible = updateGlobalSearchIcon();
        boolean voiceVisible = updateVoiceSearchIcon(searchVisible);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.onSearchPackagesChanged(searchVisible, voiceVisible);
        }
    }

    /**
     * Add the icons for all apps.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAllApplications(final ArrayList<ApplicationInfo> apps) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAllApplications: apps = " + apps);
        }
        
    	Runnable setAllAppsRunnable = new Runnable() {
            public void run() {
                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.setApps(apps);
                }
            }
        };

        /// M: If unread information load completed, we need to update information in app list.
        if (mUnreadLoadCompleted) {
            AppsCustomizePagedView.updateUnreadNumInAppInfo(apps);
        }
        // Remove the progress bar entirely; we could also make it GONE
        // but better to remove it since we know it's not going to be used
        View progressBar = mAppsCustomizeTabHost.
            findViewById(R.id.apps_customize_progress_bar);
        if (progressBar != null) {
            ((ViewGroup)progressBar.getParent()).removeView(progressBar);

            // We just post the call to setApps so the user sees the progress bar
            // disappear-- otherwise, it just looks like the progress bar froze
            // which doesn't look great
            mAppsCustomizeTabHost.post(setAllAppsRunnable);
        } else {
            // If we did not initialize the spinner in onCreate, then we can directly set the
            // list of applications without waiting for any progress bars views to be hidden.
            setAllAppsRunnable.run();
        }
        mBindingAppsFinished = true;
    }

    /**
     * A package was installed.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppsUpdated: apps = " + apps);
        }
        setLoadOnResume();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.addApps(apps);
        }
    }

    /**
     * A package was updated.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppsUpdated: apps = " + apps);
        }

        setLoadOnResume();
        if (mWorkspace != null) {
            mWorkspace.updateShortcuts(apps);
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.updateApps(apps);
        }
    }

    /**
     * A package was uninstalled.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppsRemoved(ArrayList<String> packageNames, boolean permanent) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppsRemoved: packageNames = " + packageNames + ", permanent = " + permanent);
        }     
   
        if (permanent) {
            mWorkspace.removeItems(packageNames);
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.removeApps(packageNames);
        }

        // Notify the drag controller
        mDragController.onAppsRemoved(packageNames, this);
    }

    /**
     * A number of packages were updated.
     */
    public void bindPackagesUpdated() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindPackagesUpdated.");
        }

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.onPackagesUpdated();
        }
    }

    private int mapConfigurationOriActivityInfoOri(int configOri) {
        final Display d = getWindowManager().getDefaultDisplay();
        int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
        switch (d.getRotation()) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
            // We are currently in the same basic orientation as the natural orientation
            naturalOri = configOri;
            break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
            // We are currently in the other basic orientation to the natural orientation
            naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE) ?
                    Configuration.ORIENTATION_PORTRAIT : Configuration.ORIENTATION_LANDSCAPE;
            break;
        }

        int[] oriMap = {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        };
        // Since the map starts at portrait, we need to offset if this device's natural orientation
        // is landscape.
        int indexOffset = 0;
        if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
            indexOffset = 1;
        }
        return oriMap[(d.getRotation() + indexOffset) % 4];
    }

    public boolean isRotationEnabled() {
        boolean enableRotation = sForceEnableRotation ||
                getResources().getBoolean(R.bool.allow_rotation);
        return enableRotation;
    }
    public void lockScreenOrientation() {
        if (isRotationEnabled()) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources()
                    .getConfiguration().orientation));
        }
    }
    public void unlockScreenOrientation(boolean immediate) {
        if (isRotationEnabled()) {
            if (immediate) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }, mRestoreScreenOrientationDelay);
            }
        }
    }

    /* Cling related */
    private boolean isClingsEnabled() {
        // disable clings when running in a test harness
        if(ActivityManager.isRunningInTestHarness()) return false;

        return true;
    }

    private Cling initCling(int clingId, int[] positionData, boolean animate, int delay) {
        final Cling cling = (Cling) findViewById(clingId);
        if (cling != null) {
            cling.init(this, positionData);
            cling.setVisibility(View.VISIBLE);
            cling.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if (animate) {
                cling.buildLayer();
                cling.setAlpha(0f);
                cling.animate()
                    .alpha(1f)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(SHOW_CLING_DURATION)
                    .setStartDelay(delay)
                    .start();
            } else {
                cling.setAlpha(1f);
            }
            cling.setFocusableInTouchMode(true);
            cling.post(new Runnable() {
                public void run() {
                    cling.setFocusable(true);
                    cling.requestFocus();
                }
            });
            mHideFromAccessibilityHelper.setImportantForAccessibilityToNo(
                    mDragLayer, clingId == R.id.all_apps_cling);
        }
        return cling;
    }

    private void dismissCling(final Cling cling, final String flag, int duration) {
        // To catch cases where siblings of top-level views are made invisible, just check whether
        // the cling is directly set to GONE before dismissing it.
        if (cling != null && cling.getVisibility() != View.GONE) {
            ObjectAnimator anim = LauncherAnimUtils.ofFloat(cling, "alpha", 0f);
            anim.setDuration(duration);
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    cling.setVisibility(View.GONE);
                    cling.cleanup();
                    // We should update the shared preferences on a background thread
                    new Thread("dismissClingThread") {
                        public void run() {
                            SharedPreferences.Editor editor = mSharedPrefs.edit();
                            editor.putBoolean(flag, true);
                            editor.commit();
                        }
                    }.start();
                };
            });
            anim.start();
            mHideFromAccessibilityHelper.restoreImportantForAccessibility(mDragLayer);
        }
    }

    private void removeCling(int id) {
        final View cling = findViewById(id);
        if (cling != null) {
            final ViewGroup parent = (ViewGroup) cling.getParent();
            parent.post(new Runnable() {
                @Override
                public void run() {
                    parent.removeView(cling);
                }
            });
            mHideFromAccessibilityHelper.restoreImportantForAccessibility(mDragLayer);
        }
    }

    private boolean skipCustomClingIfNoAccounts() {
        Cling cling = (Cling) findViewById(R.id.workspace_cling);
        boolean customCling = cling.getDrawIdentifier().equals("workspace_custom");
        if (customCling) {
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType("com.google");
            return accounts.length == 0;
        }
        return false;
    }

    public void showFirstRunWorkspaceCling() {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.WORKSPACE_CLING_DISMISSED_KEY, false) &&
                !skipCustomClingIfNoAccounts()) {
            /// M: modified for theme feature, set different workspace cling color through different themes.
            Cling workspaceCling = initCling(R.id.workspace_cling, null, false, 0);
            setClingTitleWithThemeColor(workspaceCling, R.id.workspace_cling_title);
        } else {
            removeCling(R.id.workspace_cling);
        }
    }

    public void showFirstRunAllAppsCling(int[] position) {
        // Enable the clings only if they have not been dismissed before
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.ALLAPPS_CLING_DISMISSED_KEY, false)) {
            /// M: modified for theme feature, set different all apps cling color through different themes.
            Cling appsCling = initCling(R.id.all_apps_cling, position, true, 0);
            setClingTitleWithThemeColor(appsCling, R.id.all_apps_cling_title);
        } else {
            removeCling(R.id.all_apps_cling);
        }
    }

    public Cling showFirstRunFoldersCling() {
        // Enable the clings only if they have not been dismissed before
        /// M: modified for theme feature, set different folder cling color through different themes.
        Cling cling = null;
        if (isClingsEnabled() &&
                !mSharedPrefs.getBoolean(Cling.FOLDER_CLING_DISMISSED_KEY, false)) {
            cling = initCling(R.id.folder_cling, null, true, 0);
            setClingTitleWithThemeColor(cling, R.id.folder_cling_title);
        } else {
            removeCling(R.id.folder_cling);
        }
        return cling;
    }

    public boolean isFolderClingVisible() {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        if (cling != null) {
            return cling.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    public void dismissWorkspaceCling(View v) {
        Cling cling = (Cling) findViewById(R.id.workspace_cling);
        dismissCling(cling, Cling.WORKSPACE_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }

    public void dismissAllAppsCling(View v) {
        Cling cling = (Cling) findViewById(R.id.all_apps_cling);
        dismissCling(cling, Cling.ALLAPPS_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }

    public void dismissFolderCling(View v) {
        Cling cling = (Cling) findViewById(R.id.folder_cling);
        dismissCling(cling, Cling.FOLDER_CLING_DISMISSED_KEY, DISMISS_CLING_DURATION);
    }

    /**
     * Prints out out state for debugging.
     */
    public void dumpState() {
        Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
        Log.d(TAG, "mSavedState=" + mSavedState);
        Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
        Log.d(TAG, "mRestoring=" + mRestoring);
        Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
        Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
        Log.d(TAG, "sFolders.size=" + sFolders.size());
        mModel.dumpState();

        if (mAppsCustomizeContent != null) {
            mAppsCustomizeContent.dumpState();
        }
        Log.d(TAG, "END launcher2 dump state");
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.println(" ");
        writer.println("Debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            writer.println("  " + sDumpLogs.get(i));
        }
    }

    public static void dumpDebugLogsToConsole() {
        Log.d(TAG, "");
        Log.d(TAG, "*********************");
        Log.d(TAG, "Launcher debug logs: ");
        for (int i = 0; i < sDumpLogs.size(); i++) {
            Log.d(TAG, "  " + sDumpLogs.get(i));
        }
        Log.d(TAG, "*********************");
        Log.d(TAG, "");
    }

    /**
     * M: Change cling color while theme changed.
     *
     * @param cling The cling will to be set color.
     * @param id The title of the cling.
     */
    private void setClingTitleWithThemeColor(final View cling, int id) {
        if (cling != null) {
            final TextView titleView = (TextView) cling.findViewById(id);
            if (titleView != null) {
                titleView.setTextColor(getThemeColor(getResources(), R.color.cling_title_text_color));
            }
        }
    }

    /**
     * M: Get theme main color.
     *
     * @param res resources object.
     * @param id the default color resource id.
     * @return theme main color, if non, return the default color from given id.
     */   
    public static int getThemeColor(Resources res, int id) {
        int color = 0;
        if (FeatureOption.MTK_THEMEMANAGER_APP) {
            color = res.getThemeMainColor();
        }
        if (color == 0) {
            color = res.getColor(id);
        }
        return color;
    }
    
    /**
     * M: Get current CellLayout bounds.
     * 
     * @return mCurrentBounds.
     */
    Rect getCurrentBounds() {
        return mCurrentBounds;
    }

    /**
     * M: Register OrientationListerner when onCreate.
     */
    private void registerOrientationListener() {
        mOrientationListener = new OrientationEventListener(Launcher.this) {
            @Override
            public void onOrientationChanged(int orientation) {
                orientation = roundOrientation(orientation);
                if (orientation != mLastOrientation) {
                    if (mLastOrientation == Launcher.ORIENTATION_0 || mLastOrientation == Launcher.ORIENTATION_180) {
                        if (orientation == Launcher.ORIENTATION_270 || orientation == Launcher.ORIENTATION_90) {
                            boolean isRotateEnabled = Settings.System.getInt(getContentResolver(),
                                    Settings.System.ACCELEROMETER_ROTATION, 1) != 0;
                            if (isRotateEnabled) {
                                String cmpName = Settings.System.getString(getContentResolver(),
                                        Settings.System.LANDSCAPE_LAUNCHER);
                                if (cmpName != null && !cmpName.equals("none")) {
                                    fireAppRotated(cmpName);
                                }
                            }
                        }
                    }
                    mLastOrientation = orientation;
                }
            }
        };
        final String cmpName = Settings.System.getString(getContentResolver(), Settings.System.LANDSCAPE_LAUNCHER);
        if (cmpName != null && !cmpName.equals("none")) {
            mOrientationListener.enable();
        }
    }
    
    /**
     * M: Calculate orientation.
     *
     * @param orientation
     * @return
     */
    private int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 360;
    }
    
    /**
     * M: Launch the specified app with name of "cmpName" and intent action is intent.ACTION_ROTATED_MAIN.
     *
     * @param cmpName
     */
    private void fireAppRotated(String cmpName) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "fireAppRotated: cmpName = " + cmpName);
        }

        String name[] = cmpName.split("/");
        Intent intent = new Intent(Intent.ACTION_ROTATED_MAIN);
        intent.setComponent(new ComponentName(name[0], name[1]));
        startActivitySafely(null, intent, null);
    }
    
    /**
     * M: Enable OrientationListener when onResume.
     */
    private void enableOrientationListener() {
        final String cmpName = Settings.System.getString(getContentResolver(), Settings.System.LANDSCAPE_LAUNCHER);
        if (cmpName != null && !cmpName.equals("none")) {
            if (mOrientationListener.canDetectOrientation()) {
                mOrientationListener.enable();
                mLastOrientation = Launcher.ORIENTATION_270;
            } else {
                Toast.makeText(this, R.string.orientation, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * M: Disable OrientationListener when onPause/onDestory.
     */
    private void disableOrientationListener() {
        final String cmpName = Settings.System.getString(getContentResolver(), Settings.System.LANDSCAPE_LAUNCHER);
        if (cmpName != null && !cmpName.equals("none")) {
            mLastOrientation = Launcher.ORIENTATION_0;
            mOrientationListener.disable();
        }
    }

    /**
     * M: scene switched, reset views and states for UI update.
     * 
     * @param scene the name of the scene will be switched to.
     */
    public void switchScene() {
        for (int i = 0; i < SCREEN_COUNT; i++) {
            final CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(i);
            cellLayout.removeAllViews();
        }

        // wipe any previous widgets
        final Context context = getApplicationContext();
        AppWidgetHost appWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
        appWidgetHost.deleteHost();
        final ContentResolver resolver = context.getContentResolver();
        resolver.notifyChange(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, null);

        DragController dragController = mDragController;
        dragController.resetDropTarget();
        // The order here is bottom to top.
        dragController.addDropTarget(mWorkspace);
        if (mSearchDropTargetBar != null) {
            mSearchDropTargetBar.setup(this, dragController);
        }
    }

    /**
     * M: Bind component unread information in workspace and all apps list.
     *
     * @param component the component name of the app.
     * @param unreadNum the number of the unread message.
     */
    public void bindComponentUnreadChanged(final ComponentName component, final int unreadNum) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "bindComponentUnreadChanged: component = " + component
                    + ", unreadNum = " + unreadNum + ", this = " + this);
        }
        // Post to message queue to avoid possible ANR.
        mHandler.post(new Runnable() {
            public void run() {
                final long start = System.currentTimeMillis();
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindComponentUnreadChanged begin: component = " + component
                            + ", unreadNum = " + unreadNum + ", start = " + start);
                }
                if (mWorkspace != null) {
                    mWorkspace.updateComponentUnreadChanged(component, unreadNum);
                }

                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.updateAppsUnreadChanged(component, unreadNum);
                }
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindComponentUnreadChanged end: current time = "
                            + System.currentTimeMillis() + ", time used = "
                            + (System.currentTimeMillis() - start));
                }
            }
        });
    }
    
   /**
     * M: Bind shortcuts unread number if binding process has finished.
     */
    public void bindUnreadInfoIfNeeded() {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "bindUnreadInfoIfNeeded: mBindingWorkspaceFinished = "
                    + mBindingWorkspaceFinished + ", thread = " + Thread.currentThread());
        }
        if (mBindingWorkspaceFinished) {
            bindWorkspaceUnreadInfo();
        }
        
        if (mBindingAppsFinished) {
            bindAppsUnreadInfo();
        }
        mUnreadLoadCompleted = true;
    }
    
    /**
     * M: Bind unread number to shortcuts with data in MTKUnreadLoader.
     */
    private void bindWorkspaceUnreadInfo() {
        mHandler.post(new Runnable() {
            public void run() {
                final long start = System.currentTimeMillis();
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindWorkspaceUnreadInfo begin: start = " + start);
                }
                if (mWorkspace != null) {
                    mWorkspace.updateShortcutsAndFoldersUnread();
                }
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindWorkspaceUnreadInfo end: current time = "
                            + System.currentTimeMillis() + ",time used = "
                            + (System.currentTimeMillis() - start));
                }
            }
        });
    }
    
    /**
     * M: Bind unread number to shortcuts with data in MTKUnreadLoader.
     */
    private void bindAppsUnreadInfo() {
        mHandler.post(new Runnable() {
            public void run() {
                final long start = System.currentTimeMillis();
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindAppsUnreadInfo begin: start = " + start);
                }
                if (mAppsCustomizeContent != null) {
                    mAppsCustomizeContent.updateAppsUnread();
                }
                if (LauncherLog.DEBUG_PERFORMANCE) {
                    LauncherLog.d(TAG, "bindAppsUnreadInfo end: current time = "
                            + System.currentTimeMillis() + ",time used = "
                            + (System.currentTimeMillis() - start));
                }
            }
        });
    }
    
    /**
     * M: Show long press widget to add message, avoid duplication of message.
     */
    public void showLongPressWidgetToAddMessage() {
        if (mLongPressWidgetToAddToast == null) {
            mLongPressWidgetToAddToast = Toast.makeText(getApplicationContext(), R.string.long_press_widget_to_add,
                    Toast.LENGTH_SHORT);
        } else {
            mLongPressWidgetToAddToast.setText(R.string.long_press_widget_to_add);
            mLongPressWidgetToAddToast.setDuration(Toast.LENGTH_SHORT);
        }
        mLongPressWidgetToAddToast.show();
    }

    /**
     * M: Cancel long press widget to add message when press back key.
     */
    private void cancelLongPressWidgetToAddMessage() {
        if (mLongPressWidgetToAddToast != null) {
            mLongPressWidgetToAddToast.cancel();
        }
    }
    
    /**
     * M: A widget was uninstalled/disabled.
     *
     * Implementation of the method from LauncherModel.Callbacks.
     */
    public void bindAppWidgetRemoved(ArrayList<String> appWidget, boolean permanent) {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "bindAppWidgetRemoved: appWidget = " + appWidget + ", permanent = " + permanent);
        }        
        if (permanent) {
            mWorkspace.removeItems(appWidget);
        }
    }

    /**
     * M: set orientation changed flag, this would make the apps customized pane
     * recreate views in certain condition.
     */
    public void notifyOrientationChanged() {
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "notifyOrientationChanged: mOrientationChanged = "
                    + mOrientationChanged + ", mPaused = " + mPaused);
        }
        mOrientationChanged = true;
    }

    /**
     * M: tell Launcher that the pages in app customized pane were recreated.
     */
    void notifyPagesWereRecreated() {
        mPagesWereRecreated = true;
    }

    /**
     * M: reset re-sync apps pages flags.
     */
    private void resetReSyncFlags() {
        mOrientationChanged = false;
        mPagesWereRecreated = false;
    }

    //liyang sence_feature 0701 (on)
    void setDefaultLauncher(){
 		Intent intent = new Intent(Intent.ACTION_MAIN);
 		intent.addCategory(Intent.CATEGORY_HOME);
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
 		Log.d("liyang", "Launcher.setDefaultLauncher()--resolveInfoList.size="+resolveInfoList.size());
 		if (resolveInfoList != null) {
 			int size = resolveInfoList.size();
 			for (int j = 0; j < size; j++) {
 				final ResolveInfo r = resolveInfoList.get(j);
 				if (r.activityInfo.packageName.equals("com.android.provision")
 						|| r.activityInfo.name.equals("com.android.settings.CryptKeeper")) {
 					resolveInfoList.remove(j);
 					size -= 1;
 					break;
 				}
 			}
 			ComponentName[] set = new ComponentName[size];
 			ComponentName defaultLauncher = new ComponentName(
 					"com.android.launcher", "com.android.launcher2.Launcher");
 			int defaultMatch = 0;
 			if (size <= 2) {
 				Log.d("liyang", "Launcher.setDefaultLauncher()()--<=2");
 				for (int i = 0; i < size; i++) {
 					final ResolveInfo resolveInfo = resolveInfoList.get(i);
 					set[i] = new ComponentName(
 							resolveInfo.activityInfo.packageName,
 							resolveInfo.activityInfo.name);
 						if (resolveInfo.match > defaultMatch)defaultMatch = resolveInfo.match;
 				
 				}
 				
 				IntentFilter filter = new IntentFilter();
 				filter.addAction(Intent.ACTION_MAIN);
 				filter.addCategory(Intent.CATEGORY_HOME);
 				filter.addCategory(Intent.CATEGORY_DEFAULT);
 	                        pm.clearPackagePreferredActivities("com.rlk.scene");
				pm.clearPackagePreferredActivities(defaultLauncher.getPackageName());
 				pm.addPreferredActivity(filter, defaultMatch, set,
 						defaultLauncher);
 			}
 		}
    }
  //liyang sence_feature 0701 (off)
//renxinquan add start page_count

    
    private void addCellLayoutToWorkspace(){
    
      int screen_count=mWorkspace.getChildCount();
  	  if (screen_count < Utilities.maxPageViewCount) {	
  		  CellLayout newCellLayout=(CellLayout)mInflater.inflate(R.layout.workspace_screen, mWorkspace, false);	
  	
  		  mWorkspace.addView(newCellLayout);  		
  		  updateWorkspaceScreenCounts();
  		  mWorkspace.setOnLongClickListener(this);
  		  Utilities.setPageViewCount(this, mWorkspace.getChildCount());
  		loadThumbnail();
  		
  	  }else{
  		  Toast mLauncherToast;
  		  mLauncherToast =Toast.makeText(this, R.string.message_cannot_add_desktop_screen, Toast.LENGTH_LONG);
  		  mLauncherToast.show();
  	  }
  	  
    }
    
    
    private void remove_page_screen(int screen){
    	mWorkspace.removePage(screen);
    }
    
   
    public void setLauncher_SCREEN_COUNT(int count){
    	  SCREEN_COUNT = count;
    	  DEFAULT_SCREEN = count/2;
      }
      
    public void updateWorkspaceScreenCounts(){
  	  int screenCount = mWorkspace.getChildCount();

  	  Utilities.setPageViewCount(this, screenCount);
      mWorkspace.setWorkspaceScreenCounts(screenCount);
    	  //addPageState();
    	  //setLevel(mWorkspace.getCurrentPage());
    }
    
    private ArrayList<Bitmap> mThumbnailBitmaps;
    float mThumbCellWidth = -1;
    float mThumbCellHeight = -1;
    private void loadThumbnailBitmaps(int countX , float myScale) {
 
        final Resources resources = getResources();
        final Workspace workspace = mWorkspace;

        CellLayout cell = ((CellLayout) workspace.getChildAt(0));
        
//        float max = workspace.getChildCount();
        float max = countX;
        
        final Rect r = new Rect();
        resources.getDrawable(R.drawable.thumb_bg_normal).getPadding(r);
        int extraW = (int) ((r.left + r.right) * max);
        int extraH = r.top + r.bottom;

        int aW = cell.getWidth() - extraW;
        float w = aW / max;

        int width = cell.getWidth();
        int height = cell.getHeight();
        int x = cell.getPaddingLeft();
        int y = cell.getPaddingTop();
        width -= (x + cell.getPaddingRight());
        height -= (y + cell.getPaddingBottom());

        float scale = w / width;
        scale*=myScale;
        final int workspaceScreens = mWorkspace.getChildCount();

        final float sWidth = width * scale;
        float sHeight = height * scale;
        
      //zsc tecno 0704 +++
        if (mThumbCellWidth == -1 || mThumbCellHeight == -1) {
      	  mThumbCellWidth = sWidth/4f;
      	  mThumbCellHeight = sHeight/4f;
  	  }
      //zsc tecno 0704 ---

//        LinearLayout preview = new LinearLayout(this);
        mThumbnailBitmaps = new ArrayList<Bitmap>(workspaceScreens);

        for (int i = 0; i < workspaceScreens; i++) {
            cell = (CellLayout) workspace.getChildAt(i);
            final Bitmap bitmap = Bitmap.createBitmap((int) sWidth, (int) sHeight,
                    Bitmap.Config.ARGB_8888);

            final Canvas c = new Canvas(bitmap);
            c.scale(scale, scale);
            c.translate(-cell.getPaddingLeft(), -cell.getPaddingTop());
            cell.dispatchDraw(c);
            mThumbnailBitmaps.add(bitmap);            
        }
    }
    
 
    class myTag{
    	public int id;
    	myTag(int i){
    		id = i;
    	}
    };
    
    LinearLayout mThumbnailCellLayout = null;
    HorizontalScrollView mMyScrollView = null;
    public void loadThumbnail(){

  	  
  	  loadThumbnailBitmaps(3,0.8f);
  	  
  	 
  	  
  	  FrameLayout thumbParent = null;
  	  ImageView thumbnailView = null;

  	  ImageView thumbnailDeleteBtn = null;

  
  	  mThumbnailCellLayout.removeAllViewsInLayout();
  	  int childCount = mWorkspace.getChildCount();
  	  for (int i = 0; i < childCount; i++) {
		  
  			//zsc tecno 0614 +++

  			//zsc tecno 0614 ---
			  thumbParent = (FrameLayout) mInflater.inflate(R.layout.thumbnail, null);
			  thumbnailView = (ImageView) thumbParent.findViewById(R.id.thumbnail_screenView);
			 // thumbnailViewBg = (ImageView) thumbParent.findViewById(R.id.thumbnail_screenView_bg);
			//zsc tecno 0613 +++
			  thumbnailDeleteBtn = (ImageView) thumbParent.findViewById(R.id.thumbnail_deleteIconView);
			 // thumbnailSetHomeBtn = (ImageView) thumbParent.findViewById(R.id.thumbnail_homeIconView);
			//zsc tecno 0613 ---
			  thumbnailView.setImageBitmap(mThumbnailBitmaps.get(i));
//			  thumbnailView.setScaleType(ScaleType.CENTER_INSIDE);
			  thumbnailDeleteBtn.setTag(new myTag(i));
			  
			  //lp = new ViewGroup.LayoutParams
			  //lp = (LayoutParams) new ViewGroup.LayoutParams(cellX, cellY,1,1);
			//  childId = LauncherModel.getCellLayoutChildId(LauncherSettings.Favorites.CONTAINER_THUMBNAIL, 0, cellX, cellY, 1, 1);
			 // mThumbnailCellLayout.addViewToCellLayout(thumbParent, -1, childId, lp, true);
			  mThumbnailCellLayout.addView(thumbParent);
			//  thumbParent.setHapticFeedbackEnabled(false);
//			  thumbParent.setOnLongClickListener(this);
			//  thumbParent.setOnClickListener(this);
			//zsc tecno 0613 +++
			  thumbnailView.setOnClickListener(this);

			  thumbnailDeleteBtn.setOnClickListener(this);
			 
			  //zsc tecno 0613 ---
			  
			  mThumbnailCellLayout.requestLayout();
  	  }
  	  if(childCount < Utilities.maxPageViewCount){
		  thumbParent = (FrameLayout) mInflater.inflate(R.layout.thumbnail, null);
		  thumbnailView = (ImageView) thumbParent.findViewById(R.id.thumbnail_screenView);
		  thumbnailDeleteBtn = (ImageView) thumbParent.findViewById(R.id.thumbnail_deleteIconView);
		//  thumbnailView.setImageBitmap());
		  thumbnailDeleteBtn.setVisibility(View.GONE);
		  thumbnailView.setImageResource(R.drawable.thumb_add_icon);
		  mThumbnailCellLayout.addView(thumbParent);
		  thumbnailView.setOnClickListener(this);
		  mThumbnailCellLayout.requestLayout();
		  thumbnailView.setTag(new myTag(-1));
  	  }
    }

    public void remove_page_add_del_count(int id){
    	mThumbnailCellLayout.removeViewAt(id);
    	mThumbnailCellLayout.requestLayout();
    }
    
    void show_add_del_pagecount_dialog(){
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
        builder.setIcon(0);
        builder.setCancelable(true);
       // builder.setView(mMyScrollView);
       // mMyScrollView.requestFocus();
        final AlertDialog dialog = builder.create();
       // dialog.getLayoutInflater().
    	  mMyScrollView = (HorizontalScrollView) dialog.getLayoutInflater().inflate(R.layout.main_page_setting, null);
    	  mThumbnailCellLayout = (LinearLayout) mMyScrollView.findViewById(R.id.linearlayout_my);
    	  loadThumbnail();
    	  dialog.setView(mMyScrollView);
        dialog.show();
        //mMyScrollView.fullScroll(View.FOCUS_DOWN);
    }
    
    
    
//renxinquan add end page_count
    
    //zhuwei add
    public boolean canShowUninstallMode() {
  	  if (null == mAppsCustomizeTabHost) {
  		  return false;
  	  }
  	  
  	  boolean allAppsVisible = (mAppsCustomizeTabHost.getVisibility() == View.VISIBLE);
        boolean isAppsTab = mAppsCustomizeTabHost.getCurrentTabTag().equalsIgnoreCase("APPS");
        
        return allAppsVisible && isAppsTab;
    }
    
    public void setIsAppsItemUninstallState(boolean bool){
  	  synchronized (workspaceItemEditObj) {
  		  if (bool != isAppsItemUninstallState) {
  			  isAppsItemUninstallState = bool;
  		  }
  	  }
    }
    
    List<Integer> getAnimWorkspacePageList(int currentPage,boolean isIdle) {
  		List<Integer> needAnimWorkspacePage = new ArrayList<Integer>();
  		int leftPage = 0;
  		int rightPage = 0;
  		if (isIdle) {
  			if (currentPage == 0) {
  				leftPage = mWorkspace.getChildCount() - 1;
  				rightPage = currentPage + 1;
  			}else if(currentPage == mWorkspace.getChildCount() - 1){
  				leftPage = currentPage - 1;
  				rightPage = 0;
  			}else{
  				leftPage = currentPage - 1;
  				rightPage = currentPage + 1;
  			}
  		}else{
  			if (currentPage == 0) {
  				leftPage = - 1;
  				rightPage = currentPage + 1;
  			}else if(currentPage == mAppsCustomizeContent.getNumAppsPages() - 1){
  				leftPage = currentPage - 1;
  				rightPage = -1;
  			}else{
  				leftPage = currentPage - 1;
  				rightPage = currentPage + 1;
  		    }
  		
  		}
  		
  		needAnimWorkspacePage.add(currentPage);
  		needAnimWorkspacePage.add(leftPage);
  		needAnimWorkspacePage.add(rightPage);
  		Log.d("zhuwei", "Launcher.getAnimWorkspacePageList--needAnimWorkspacePage="+needAnimWorkspacePage.toString());
  		return needAnimWorkspacePage;
  	}
    
    private void changeWidgetButtonAndMenu(boolean enable) {
  	  if (mAppsCustomizeTabHost == null) {
  		  Log.e("zhuwei", "mAppsCustomizeTabHost-->!!! null!");
  	  }
  	  /*if (mCancelUnstallItem != null) {
  		  mCancelUnstallItem.setVisible(enable); 
  	  }
  	  if (mUnInstallItem != null) {
  		  mUnInstallItem.setVisible(!enable);
  	  }*/
  	  if (mAppsCustomizeTabHost != null) {
  		  mAppsCustomizeTabHost.onUninstallStateChanged(enable);
  	  }
  	  
    }
    
    private void startUninstallAppsItem() {
  	  if (!isAppsItemUninstallState) {
  		  changeWidgetButtonAndMenu(true);
  		  setIsAppsItemUninstallState(true);
  		  List<Integer> animWorkspacePageList = getAnimWorkspacePageList(mAppsCustomizeContent.getCurrentPage(),false);
  		  for (int i = 0; i < animWorkspacePageList.size(); i++) {
  			  int currentPage = animWorkspacePageList.get(i);
  			  View child = mAppsCustomizeContent.getPageAt(currentPage);
                          
  			  if (child instanceof PagedViewCellLayout) {
  				  PagedViewCellLayout pagedViewCellLayout = (PagedViewCellLayout) child;
  				  PagedViewCellLayoutChildren childrenLayout = pagedViewCellLayout.getChildrenLayout();
  				  int childCount = childrenLayout.getChildCount();
  				  PagedViewIcon appItem = null;
  				  for (int j = 0; j < childCount; j++) {
  					  View v = childrenLayout.getChildAt(j);
  					  if (v instanceof PagedViewIcon) {
  						  appItem = (PagedViewIcon) v;
  						  if (!appItem.getIsAnimating()) {
  							  float randomValue = getRandomValueForAnim();
  							  appItem.initIsScaleUpValue(randomValue);
  							  appItem.startRotateAnimation();
  						  }
  					  } else if (v instanceof MTKAppIcon) {
                                              MTKAppIcon mtkAppItem = (MTKAppIcon)v;
                                              if (mtkAppItem != null) {
  							appItem = mtkAppItem.getPagedViewIcon();
  							 if (appItem != null && !appItem.getIsAnimating()) {
  	  							  float randomValue = getRandomValueForAnim();
  	  							  appItem.initIsScaleUpValue(randomValue);
  	  							  appItem.startRotateAnimation();
  	  						  } else {
  	  							  Log.e("zhuwei", "get from MtkAppIcon appItem --> null");
  	  						  }
  						}
                                          }
  					  else{
  						  v.invalidate();
  					  }
  				  }
  			  }
  		  }
  	  }
    }
    
    public float getRandomValueForAnim(){
  		double random = Math.random() * 300f + 450f;
  		return (float) random;
    }
    
    private void cancelUninstallAppsItem(boolean bool) {
  	  if (isAppsItemUninstallState) {
  		  if (bool) {
  			  setIsAppsItemUninstallState(false);
  		  }
  		  changeWidgetButtonAndMenu(false);
  		  int count = mAppsCustomizeContent.getChildCount();
  		  for (int i = 0; i < count; i++) {
  			  View child = mAppsCustomizeContent.getPageAt(i);
  			  if (child instanceof PagedViewCellLayout) {
  				  PagedViewCellLayout pagedViewCellLayout = (PagedViewCellLayout) child;
  				  PagedViewCellLayoutChildren childrenLayout = pagedViewCellLayout.getChildrenLayout();
  				  int childCount = childrenLayout.getChildCount();
  				  PagedViewIcon appItem = null;
  				  for (int j = 0; j < childCount; j++) {
  					  View v = childrenLayout.getChildAt(j);
  					  if (v instanceof PagedViewIcon) {
  						  appItem = (PagedViewIcon) v;
  						  appItem.cancelRotateAnimation();
  					  }  else if (v instanceof MTKAppIcon) {
    						MTKAppIcon mtkAppItem = (MTKAppIcon)v;
      						if (mtkAppItem != null) {
      							appItem = mtkAppItem.getPagedViewIcon();
      							 if (appItem != null) {
      								 appItem.cancelRotateAnimation();
      	  						  } else {
      	  							  Log.e("zhuwei", "get from MtkAppIcon appItem --> null");
      	  						  }
      						}
  					  } else{
  						  v.invalidate();
  					  }
  				  }
  			  }
  		  }
  	  }
    }
    
    public void refreshArrangeAnimWhenSnapScreen(PagedView child, boolean isNotSnap){
  	  boolean isAppsCustomizePagedView = false;
  	  if (child instanceof AppsCustomizePagedView) {
  		  isAppsCustomizePagedView = true;
  	  }
  	 /* if (isWorkspaceItemEditState && !isNotSnap && !isAppsCustomizePagedView) {
  		  cancelEditWorkspaceItemForsnap(child);
  		  editWorkspaceItem(true);
  	  }else*/ if (isAppsItemUninstallState && !isNotSnap && isAppsCustomizePagedView) {
  		  cancelUninstallAppsItem(true);
  		  startUninstallAppsItem();
  	  }
    }
    
    //end
    
}

interface LauncherTransitionable {
    View getContent();
    void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace);
    void onLauncherTransitionStep(Launcher l, float t);
    void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace);
} 