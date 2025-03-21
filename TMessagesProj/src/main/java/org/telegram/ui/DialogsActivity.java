/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.Property;
import android.util.SparseArray;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScrollerCustom;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.XiaomiUtilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Cells.AccountSelectCell;
import org.telegram.ui.Cells.ArchiveHintInnerCell;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerActionCheckCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackButtonMenu;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BlurredRecyclerView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.DialogsItemAnimator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.FiltersListBottomSheet;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.FloatingDebug.FloatingDebugController;
import org.telegram.ui.Components.FloatingDebug.FloatingDebugProvider;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.JoinGroupAlert;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MediaActionDrawable;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.PacmanAnimation;
import org.telegram.ui.Components.Premium.LimitReachedBottomSheet;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.ProxyDrawable;
import org.telegram.ui.Components.PullForegroundDrawable;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RadialProgress2;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.Reactions.ReactionsLayoutInBubble;
import org.telegram.ui.Components.RecyclerAnimationScrollHelper;
import org.telegram.ui.Components.RecyclerItemsEnterAnimator;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchViewPager;
import org.telegram.ui.Components.SimpleThemeDescription;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Components.SwipeGestureSettingsView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.Components.ViewPagerFixed;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kotlin.Unit;
import tw.nekomimi.nekogram.helpers.PasscodeHelper;
import tw.nekomimi.nekogram.ui.BottomBuilder;
import tw.nekomimi.nekogram.InternalUpdater;
import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.NekoXConfig;
import tw.nekomimi.nekogram.utils.PrivacyUtil;
import tw.nekomimi.nekogram.utils.ProxyUtil;
import tw.nekomimi.nekogram.utils.UIUtil;
import tw.nekomimi.nekogram.utils.UpdateUtil;
import tw.nekomimi.nekogram.utils.VibrateUtil;
import xyz.nextalone.nagram.NaConfig;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, FloatingDebugProvider {
    public final static int DIALOGS_TYPE_START_ATTACH_BOT = 14;
    public final static boolean DISPLAY_SPEEDOMETER_IN_DOWNLOADS_SEARCH = true;

    private boolean canShowFilterTabsView;
    private boolean filterTabsViewIsVisible;
    private int initialSearchType = -1;

    private final String ACTION_MODE_SEARCH_DIALOGS_TAG = "search_dialogs_action_mode";
    private boolean isFirstTab = true;
    private boolean rightFragmentTransitionInProgress;
    private boolean allowGlobalSearch = true;

    public MessagesStorage.TopicKey getOpenedDialogId() {
        return openedDialogId;
    }

    public class ViewPage extends FrameLayout {
        private DialogsRecyclerView listView;
        private LinearLayoutManager layoutManager;
        private DialogsAdapter dialogsAdapter;
        private ItemTouchHelper itemTouchhelper;
        private SwipeController swipeController;
        private int selectedType;
        private PullForegroundDrawable pullForegroundDrawable;
        private RecyclerAnimationScrollHelper scrollHelper;
        private int dialogsType;
        private int archivePullViewState;
        private FlickerLoadingView progressView;
        private int lastItemsCount;
        private DialogsItemAnimator dialogsItemAnimator;
        private RecyclerItemsEnterAnimator recyclerItemsEnterAnimator;
        private boolean isLocked;

        private RecyclerListView animationSupportListView;
        private DialogsAdapter animationSupportDialogsAdapter;

        public ViewPage(Context context) {
            super(context);
        }

        public boolean isDefaultDialogType() {
            return dialogsType == 0 || dialogsType == 7 || dialogsType == 8;
        }

        boolean updating;
        Runnable updateListRunnable = () -> {
            dialogsAdapter.updateList(listView, dialogsType == 0 && hasHiddenArchive() && archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN, actionBar.getTranslationY());
            listView.updateDialogsOnNextDraw = true;
            updating = false;
        };

        public void updateList(boolean animated) {
            if (isPaused) {
                return;
            }
            if (animated) {
                AndroidUtilities.cancelRunOnUIThread(updateListRunnable);
                listView.setItemAnimator(dialogsItemAnimator);
                updateListRunnable.run();
                return;
            }
            if (updating) {
                return;
            }
            updating = true;
            if (!dialogsItemAnimator.isRunning()) {
                listView.setItemAnimator(null);
            }
            AndroidUtilities.runOnUIThread(updateListRunnable, 36);

        }
    }

    private ViewPagerFixed.TabsView searchTabsView;
    private float contactsAlpha = 1f;
    private ValueAnimator contactsAlphaAnimator;
    private ViewPage[] viewPages;
    private FiltersView filtersView;
    private ActionBarMenuItem passcodeItem;
    private ActionBarMenuItem downloadsItem;
    private boolean passcodeItemVisible;
    private boolean downloadsItemVisible;
    private ActionBarMenuItem proxyItem;
    private boolean proxyItemVisible;
    private ActionBarMenuItem scanItem;
    private ActionBarMenuItem searchItem;
    private ActionBarMenuItem speedItem;
    private AnimatorSet speedAnimator;
    private ActionBarMenuItem doneItem;
    private ProxyDrawable proxyDrawable;
    private RLottieImageView floatingButton;
    private RadialProgressView floatingProgressView;
    private FrameLayout floatingButtonContainer;
    private ChatAvatarContainer avatarContainer;
    private UndoView[] undoView = new UndoView[2];
    private FilterTabsView filterTabsView;
    private boolean askingForPermissions;
    private RLottieDrawable passcodeDrawable;
    private SearchViewPager searchViewPager;

    private View blurredView;

    private Paint scrimPaint;
    private Drawable scrimViewBackground;
    private View scrimView;
    private boolean scrimViewSelected;
    private int[] scrimViewLocation = new int[2];
    private boolean scrimViewAppearing;
    private AnimatorSet scrimAnimatorSet;
    private ActionBarPopupWindow scrimPopupWindow;
    private ActionBarMenuSubItem[] scrimPopupWindowItems;

    private SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow selectAnimatedEmojiDialog;

    private int initialDialogsType;

    private boolean checkingImportDialog;

    private int messagesCount;
    private int hasPoll;
    private boolean hasInvoice;

    private PacmanAnimation pacmanAnimation;

    private DialogCell slidingView;
    private DialogCell movingView;
    private boolean allowMoving;
    private boolean movingWas;
    private ArrayList<MessagesController.DialogFilter> movingDialogFilters = new ArrayList<>();
    private boolean waitingForScrollFinished;
    private boolean allowSwipeDuringCurrentTouch;
    private boolean updatePullAfterScroll;

    private MenuDrawable menuDrawable;
    private BackDrawable backDrawable;

    private Paint actionBarDefaultPaint = new Paint();

    private NumberTextView selectedDialogsCountTextView;
    private ArrayList<View> actionModeViews = new ArrayList<>();
    private ActionBarMenuItem deleteItem;
    private ActionBarMenuItem pinItem;
    private ActionBarMenuItem muteItem;
    private ActionBarMenuItem archive2Item;
    private ActionBarMenuSubItem pin2Item;
    private ActionBarMenuSubItem addToFolderItem;
    private ActionBarMenuSubItem removeFromFolderItem;
    private ActionBarMenuSubItem archiveItem;
    private ActionBarMenuSubItem clearItem;
    private ActionBarMenuSubItem readItem;
    private ActionBarMenuSubItem blockItem;

    private float additionalFloatingTranslation;
    private float additionalFloatingTranslation2;
    private float floatingButtonTranslation;
    private float floatingButtonHideProgress;
    private float floatingButtonPanOffset;

    private AnimatorSet searchAnimator;
    private Animator tabsAlphaAnimator;
    private float searchAnimationProgress;
    private boolean searchAnimationTabsDelayedCrossfade;

    private RecyclerView sideMenu;
    private ChatActivityEnterView commentView;
    private View commentViewBg;
    private final boolean commentViewAnimated = false;
    private ImageView[] writeButton;
    private FrameLayout writeButtonContainer;
    private View selectedCountView;
    private ActionBarMenuItem switchItem;

    private RectF rect = new RectF();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private FragmentContextView fragmentLocationContextView;
    private FragmentContextView fragmentContextView;

    private ArrayList<TLRPC.Dialog> frozenDialogsList;
    private boolean dialogsListFrozen;

    private AlertDialog permissionDialog;
    private boolean askAboutContacts = true;

    private boolean closeSearchFieldOnHide;
    private long searchDialogId;
    private TLObject searchObject;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private boolean floatingForceVisible;
    private boolean floatingProgressVisible;
    private AnimatorSet floatingProgressAnimator;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private int currentConnectionState;

    private boolean disableActionBarScrolling;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private boolean resetDelegate = true;

    public static SparseArray<Boolean> dialogsLoaded = new SparseArray<>();
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private boolean canSelectTopics;
    private String searchString;
    private String initialSearchString;
    private MessagesStorage.TopicKey openedDialogId = new MessagesStorage.TopicKey();
    private boolean cantSendToChannels;
    private boolean allowSwitchAccount;
    private boolean checkCanWrite;
    private boolean afterSignup;
    private boolean showSetPasswordConfirm;
    private int otherwiseReloginDays;
    private boolean allowGroups;
    private boolean allowChannels;
    private boolean allowUsers;
    private boolean allowBots;
    private boolean closeFragment;

//    private FrameLayout updateLayout;
//    private AnimatorSet updateLayoutAnimator;
//    private RadialProgress2 updateLayoutIcon;
//    private TextView updateTextView;

    private DialogsActivityDelegate delegate;

    private ArrayList<Long> selectedDialogs = new ArrayList<>();
    public boolean notify = true;

    private int canReadCount;
    private int canPinCount;
    private int canMuteCount;
    private int canUnmuteCount;
    private int canClearCacheCount;
    private int canReportSpamCount;
    private int canUnarchiveCount;
    private int forumCount;
    private boolean canDeletePsaSelected;

    private int topPadding;
    private int lastMeasuredTopPadding;

    private int folderId;

    private final static int pin = 100;
    private final static int read = 101;
    private final static int delete = 102;
    private final static int clear = 103;
    private final static int mute = 104;
    private final static int archive = 105;
    private final static int block = 106;
    private final static int archive2 = 107;
    private final static int pin2 = 108;
    private final static int add_to_folder = 109;
    private final static int remove_from_folder = 110;


    private final static int nekox_scanqr = 1003;

    private final static int ARCHIVE_ITEM_STATE_PINNED = 0;
    private final static int ARCHIVE_ITEM_STATE_SHOWED = 1;
    private final static int ARCHIVE_ITEM_STATE_HIDDEN = 2;

    private long startArchivePullingTime;
    private boolean scrollingManually;
    private boolean canShowHiddenArchive;

    private AnimatorSet tabsAnimation;
    private boolean tabsAnimationInProgress;
    private boolean animatingForward;
    private float additionalOffset;
    private boolean backAnimation;
    private int maximumVelocity;
    private boolean startedTracking;
    private boolean maybeStartTracking;
    private static final Interpolator interpolator = t -> {
        --t;
        return t * t * t * t * t + 1.0F;
    };

    private Bulletin topBulletin;

    private int animationIndex = -1;
    private boolean searchIsShowed;
    private boolean searchWasFullyShowed;
    public boolean whiteActionBar;
    private boolean searchFiltersWasShowed;
    private float progressToActionMode;
    private ValueAnimator actionBarColorAnimator;

    private ValueAnimator filtersTabAnimator;
    private float filterTabsProgress;
    private float filterTabsMoveFrom;
    private float tabsYOffset;
    private float scrollAdditionalOffset;

    private int debugLastUpdateAction = -1;
    private boolean slowedReloadAfterDialogClick;

    private AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable statusDrawable;
    private DrawerProfileCell.AnimatedStatusView animatedStatusView;
    public RightSlidingDialogContainer rightSlidingDialogContainer;

    public final Property<DialogsActivity, Float> SCROLL_Y = new AnimationProperties.FloatProperty<DialogsActivity>("animationValue") {
        @Override
        public void setValue(DialogsActivity object, float value) {
            object.setScrollY(value);
        }

        @Override
        public Float get(DialogsActivity object) {
            return actionBar.getTranslationY();
        }
    };

    private class ContentView extends SizeNotifierFrameLayout {

        private Paint actionBarSearchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint windowBackgroundPaint = new Paint();
        private int inputFieldHeight;

        public ContentView(Context context) {
            super(context);
            needBlur = true;
            blurBehindViews.add(this);
        }

        private int startedTrackingPointerId;
        private int startedTrackingX;
        private int startedTrackingY;
        private VelocityTracker velocityTracker;
        private boolean globalIgnoreLayout;
        private int[] pos = new int[2];

        private boolean prepareForMoving(MotionEvent ev, boolean forward) {
            int id = filterTabsView.getNextPageId(forward);
            if (id < 0) {
                return false;
            }
            getParent().requestDisallowInterceptTouchEvent(true);
            maybeStartTracking = false;
            startedTracking = true;
            startedTrackingX = (int) (ev.getX() + additionalOffset);
            actionBar.setEnabled(false);
            filterTabsView.setEnabled(false);
            viewPages[1].selectedType = id;
            viewPages[1].setVisibility(View.VISIBLE);
            animatingForward = forward;
            showScrollbars(false);
            switchToCurrentSelectedMode(true);
            if (forward) {
                viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
            } else {
                viewPages[1].setTranslationX(-viewPages[0].getMeasuredWidth());
            }
            return true;
        }

        @Override
        public void setPadding(int left, int top, int right, int bottom) {
            topPadding = top;
            updateContextViewPosition();
            if (rightSlidingDialogContainer != null) {
                rightSlidingDialogContainer.setFragmentViewPadding(topPadding);
            }
            if (whiteActionBar && searchViewPager != null) {
                searchViewPager.setTranslationY(topPadding - lastMeasuredTopPadding);
            } else {
                requestLayout();
            }
        }

        public boolean checkTabsAnimationInProgress() {
            if (tabsAnimationInProgress) {
                boolean cancel = false;
                if (backAnimation) {
                    if (Math.abs(viewPages[0].getTranslationX()) < 1) {
                        viewPages[0].setTranslationX(0);
                        viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? 1 : -1));
                        cancel = true;
                    }
                } else if (Math.abs(viewPages[1].getTranslationX()) < 1) {
                    viewPages[0].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? -1 : 1));
                    viewPages[1].setTranslationX(0);
                    cancel = true;
                }
                if (cancel) {
                    showScrollbars(true);
                    if (tabsAnimation != null) {
                        tabsAnimation.cancel();
                        tabsAnimation = null;
                    }
                    tabsAnimationInProgress = false;
                }
                return tabsAnimationInProgress;
            }
            return false;
        }

        public int getActionBarFullHeight() {
            float h = actionBar.getHeight();
            float filtersTabsHeight = 0;
            if (filterTabsView != null && filterTabsView.getVisibility() != GONE) {
                filtersTabsHeight = filterTabsView.getMeasuredHeight() - (1f - filterTabsProgress) * filterTabsView.getMeasuredHeight();
            }
            float searchTabsHeight = 0;
            if (searchTabsView != null && searchTabsView.getVisibility() != View.GONE) {
                searchTabsHeight = searchTabsView.getMeasuredHeight();
            }
            h += filtersTabsHeight * (1f - searchAnimationProgress) + searchTabsHeight * searchAnimationProgress;
            return (int) h;
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            if (child == fragmentContextView && fragmentContextView.isCallStyle()) {
                return true;
            }
            if (child == blurredView) {
                return true;
            }
            boolean result;
            if (child == viewPages[0] || (viewPages.length > 1 && child == viewPages[1]) || child == fragmentContextView || child == fragmentLocationContextView) {
                canvas.save();
                canvas.clipRect(0, -getY() + actionBar.getY() + getActionBarFullHeight(), getMeasuredWidth(), getMeasuredHeight());
                if (slideFragmentProgress != 1f) {
                    float s = 1f - 0.05f * (1f - slideFragmentProgress);
                    canvas.translate((isDrawerTransition ? AndroidUtilities.dp(4) : -AndroidUtilities.dp(4)) * (1f - slideFragmentProgress), 0);
                    canvas.scale(s, s, isDrawerTransition ? getMeasuredWidth() : 0, -getY() + actionBar.getY() + getActionBarFullHeight());
                }

                result = super.drawChild(canvas, child, drawingTime);
                canvas.restore();
            } else if (child == actionBar && slideFragmentProgress != 1f) {
                canvas.save();
                float s = 1f - 0.05f * (1f - slideFragmentProgress);
                canvas.translate((isDrawerTransition ? AndroidUtilities.dp(4) : -AndroidUtilities.dp(4)) * (1f - slideFragmentProgress), 0);
                canvas.scale(s, s, isDrawerTransition ? getMeasuredWidth() : 0, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2f);
                result = super.drawChild(canvas, child, drawingTime);
                canvas.restore();
            } else {
                result = super.drawChild(canvas, child, drawingTime);
            }
            return result;
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            int actionBarHeight = getActionBarFullHeight();
            int top;
            if (inPreviewMode) {
                top = AndroidUtilities.statusBarHeight;
            } else {
                top = (int) (-getY() + actionBar.getY());
            }
            rightSlidingDialogContainer.setCurrentTop(top + actionBarHeight);
            if (whiteActionBar) {
                if (searchAnimationProgress == 1f) {
                    actionBarSearchPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    if (searchTabsView != null) {
                        searchTabsView.setTranslationY(0);
                        searchTabsView.setAlpha(1f);
                        if (filtersView != null) {
                            filtersView.setTranslationY(0);
                            filtersView.setAlpha(1f);
                        }
                    }
                } else if (searchAnimationProgress == 0) {
                    if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                        filterTabsView.setTranslationY(actionBar.getTranslationY());
                    }
                }
                AndroidUtilities.rectTmp2.set(0, top, getMeasuredWidth(), top + actionBarHeight);
                drawBlurRect(canvas, 0, AndroidUtilities.rectTmp2, searchAnimationProgress == 1f ? actionBarSearchPaint : actionBarDefaultPaint, true);
                if (searchAnimationProgress > 0 && searchAnimationProgress < 1f) {
                    actionBarSearchPaint.setColor(ColorUtils.blendARGB(Theme.getColor(folderId == 0 ? Theme.key_actionBarDefault : Theme.key_actionBarDefaultArchived), Theme.getColor(Theme.key_windowBackgroundWhite), searchAnimationProgress));
                    if (searchIsShowed || !searchWasFullyShowed) {
                        canvas.save();
                        canvas.clipRect(0, top, getMeasuredWidth(), top + actionBarHeight);
                        float cX = getMeasuredWidth() - AndroidUtilities.dp(24);
                        int statusBarH = actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
                        float cY = statusBarH + (actionBar.getMeasuredHeight() - statusBarH) / 2f;
                        drawBlurCircle(canvas, 0, cX, cY, getMeasuredWidth() * 1.3f * searchAnimationProgress, actionBarSearchPaint, true);
                        canvas.restore();
                    } else {
                        AndroidUtilities.rectTmp2.set(0, top, getMeasuredWidth(), top + actionBarHeight);
                        drawBlurRect(canvas, 0, AndroidUtilities.rectTmp2, actionBarSearchPaint, true);
                    }
                    if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                        filterTabsView.setTranslationY(actionBarHeight - (actionBar.getHeight() + filterTabsView.getMeasuredHeight()));
                    }
                    if (searchTabsView != null) {
                        float y = actionBarHeight - (actionBar.getHeight() + searchTabsView.getMeasuredHeight());
                        float alpha;
                        if (searchAnimationTabsDelayedCrossfade) {
                            alpha = searchAnimationProgress < 0.5f ? 0 : (searchAnimationProgress - 0.5f) / 0.5f;
                        } else {
                            alpha = searchAnimationProgress;
                        }

                        searchTabsView.setTranslationY(y);
                        searchTabsView.setAlpha(alpha);
                        if (filtersView != null) {
                            filtersView.setTranslationY(y);
                            filtersView.setAlpha(alpha);
                        }
                    }
                }
            } else if (!inPreviewMode) {
                if (progressToActionMode > 0) {
                    actionBarSearchPaint.setColor(ColorUtils.blendARGB(Theme.getColor(folderId == 0 ? Theme.key_actionBarDefault : Theme.key_actionBarDefaultArchived), Theme.getColor(Theme.key_windowBackgroundWhite), progressToActionMode));
                    AndroidUtilities.rectTmp2.set(0, top, getMeasuredWidth(), top + actionBarHeight);
                    drawBlurRect(canvas, 0, AndroidUtilities.rectTmp2, actionBarSearchPaint, true);
                } else {
                    AndroidUtilities.rectTmp2.set(0, top, getMeasuredWidth(), top + actionBarHeight);
                    drawBlurRect(canvas, 0, AndroidUtilities.rectTmp2, actionBarDefaultPaint, true);
                }
            }
            tabsYOffset = 0;
            if ((filtersTabAnimator != null || rightSlidingDialogContainer.hasFragment()) && filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                tabsYOffset = -(1f - filterTabsProgress) * filterTabsView.getMeasuredHeight();
                filterTabsView.setTranslationY(actionBar.getTranslationY() + tabsYOffset);
                filterTabsView.setAlpha(filterTabsProgress);
                viewPages[0].setTranslationY(-(1f - filterTabsProgress) * filterTabsMoveFrom);
            } else if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                filterTabsView.setTranslationY(actionBar.getTranslationY());
                filterTabsView.setAlpha(1f);
            }
            updateContextViewPosition();
            super.dispatchDraw(canvas);
            if (whiteActionBar && searchAnimationProgress > 0 && searchAnimationProgress < 1f && searchTabsView != null) {
                windowBackgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                windowBackgroundPaint.setAlpha((int) (windowBackgroundPaint.getAlpha() * searchAnimationProgress));
                canvas.drawRect(0, top + actionBarHeight, getMeasuredWidth(), top + actionBar.getMeasuredHeight() + searchTabsView.getMeasuredHeight(), windowBackgroundPaint);
            }

            if (parentLayout != null && actionBar != null && !actionBar.getCastShadows()) {
                int y = (int) (actionBar.getY() + getActionBarFullHeight());
                parentLayout.drawHeaderShadow(canvas, (int) (255 * (1f - searchAnimationProgress)), y);
                if (searchAnimationProgress > 0) {
                    if (searchAnimationProgress < 1) {
                        int a = Theme.dividerPaint.getAlpha();
                        Theme.dividerPaint.setAlpha((int) (a * searchAnimationProgress));
                        canvas.drawLine(0, y, getMeasuredWidth(), y, Theme.dividerPaint);
                        Theme.dividerPaint.setAlpha(a);
                    } else {
                        canvas.drawLine(0, y, getMeasuredWidth(), y, Theme.dividerPaint);
                    }
                }
            }

            if (fragmentContextView != null && fragmentContextView.isCallStyle()) {
                canvas.save();
                canvas.translate(fragmentContextView.getX(), fragmentContextView.getY());
                if (slideFragmentProgress != 1f) {
                    float s = 1f - 0.05f * (1f - slideFragmentProgress);
                    canvas.translate((isDrawerTransition ? AndroidUtilities.dp(4) : -AndroidUtilities.dp(4)) * (1f - slideFragmentProgress), 0);
                    canvas.scale(s, 1f, isDrawerTransition ? getMeasuredWidth() : 0, fragmentContextView.getY());
                }
                fragmentContextView.setDrawOverlay(true);
                fragmentContextView.draw(canvas);
                fragmentContextView.setDrawOverlay(false);
                canvas.restore();
            }
            if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
                if (blurredView.getAlpha() != 1f) {
                    if (blurredView.getAlpha() != 0) {
                        canvas.saveLayerAlpha(blurredView.getLeft(), blurredView.getTop(), blurredView.getRight(), blurredView.getBottom(), (int) (255 * blurredView.getAlpha()), Canvas.ALL_SAVE_FLAG);
                        canvas.translate(blurredView.getLeft(), blurredView.getTop());
                        blurredView.draw(canvas);
                        canvas.restore();
                    }
                } else {
                    blurredView.draw(canvas);
                }
            }
            if (scrimView != null) {
                canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), scrimPaint);
                canvas.save();
                getLocationInWindow(pos);
                canvas.translate(scrimViewLocation[0] - pos[0], scrimViewLocation[1] - (Build.VERSION.SDK_INT < 21 ? AndroidUtilities.statusBarHeight : 0));
                if (scrimViewBackground != null) {
                    scrimViewBackground.setAlpha(scrimViewAppearing ? 255 : (int) (scrimPaint.getAlpha() / 50f * 255f));
                    scrimViewBackground.setBounds(0, 0, scrimView.getWidth(), scrimView.getHeight());
                    scrimViewBackground.draw(canvas);
                }
                Drawable selectorDrawable = filterTabsView.getListView().getSelectorDrawable();
                if (scrimViewAppearing && selectorDrawable != null) {
                    canvas.save();
                    Rect selectorBounds = selectorDrawable.getBounds();
                    canvas.translate(-selectorBounds.left, -selectorBounds.top);
                    selectorDrawable.draw(canvas);
                    canvas.restore();
                }
                scrimView.draw(canvas);
                if (scrimViewSelected) {
                    Drawable drawable = filterTabsView.getSelectorDrawable();
                    canvas.translate(-scrimViewLocation[0], -drawable.getIntrinsicHeight() - 1);
                    drawable.draw(canvas);
                }
                canvas.restore();
            }
        }

        private boolean wasPortrait;

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
            boolean portrait = heightSize > widthSize;

            setMeasuredDimension(widthSize, heightSize);
            heightSize -= getPaddingTop();

            if (doneItem != null) {
                LayoutParams layoutParams = (LayoutParams) doneItem.getLayoutParams();
                layoutParams.topMargin = actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0;
                layoutParams.height = ActionBar.getCurrentActionBarHeight();
            }

            measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);

            int keyboardSize = measureKeyboardHeight();
            int childCount = getChildCount();

            if (commentView != null) {
                measureChildWithMargins(commentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                Object tag = commentView.getTag();
                if (tag != null && tag.equals(2)) {
                    if (keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow) {
                        heightSize -= commentView.getEmojiPadding();
                    }
                    inputFieldHeight = commentView.getMeasuredHeight();
                } else {
                    inputFieldHeight = 0;
                }

                if (SharedConfig.smoothKeyboard && commentView.isPopupShowing()) {
                    fragmentView.setTranslationY(0);
                    for (int a = 0; a < viewPages.length; a++) {
                        if (viewPages[a] != null) {
                            viewPages[a].setTranslationY(0);
                        }
                    }
                    if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                        actionBar.setTranslationY(0);
                        if (topBulletin != null) {
                            topBulletin.updatePosition();
                        }
                    }
                    searchViewPager.setTranslationY(0);
                }
            }

            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child == null || child.getVisibility() == GONE || child == commentView || child == actionBar) {
                    continue;
                }
                if (child instanceof DatabaseMigrationHint) {
                    int contentWidthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY);
                    int h = View.MeasureSpec.getSize(heightMeasureSpec) + keyboardSize;
                    int contentHeightSpec = View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), h - inputFieldHeight + AndroidUtilities.dp(2) - actionBar.getMeasuredHeight()), View.MeasureSpec.EXACTLY);
                    child.measure(contentWidthSpec, contentHeightSpec);
                } else if (child instanceof ViewPage) {
                    int contentWidthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY);
                    int h;
                    if (filterTabsView != null && filterTabsView.getVisibility() == VISIBLE) {
                        h = heightSize - inputFieldHeight + AndroidUtilities.dp(2) - AndroidUtilities.dp(44) - topPadding;
                        if (rightSlidingDialogContainer.hasFragment()) {
                            h += AndroidUtilities.dp(44);
                        }
                    } else {
                        h = heightSize - inputFieldHeight + AndroidUtilities.dp(2) - ((onlySelect && !(initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool())) ? 0 : actionBar.getMeasuredHeight()) - topPadding;
                    }
                    if (filtersTabAnimator != null && filterTabsView != null && filterTabsView.getVisibility() == VISIBLE) {
                        h += filterTabsMoveFrom;
                    } else {
                        child.setTranslationY(0);
                    }
                    int transitionPadding = ((isSlideBackTransition || isDrawerTransition) ? (int) (h * 0.05f) : 0);
                    h += transitionPadding;
                    child.setPadding(child.getPaddingLeft(), child.getPaddingTop(), child.getPaddingRight(), transitionPadding);
                    child.measure(contentWidthSpec, View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), h), View.MeasureSpec.EXACTLY));
                    child.setPivotX(child.getMeasuredWidth() / 2);
                } else if (child == searchViewPager) {
                    searchViewPager.setTranslationY(0);
                    int contentWidthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY);
                    int h = View.MeasureSpec.getSize(heightMeasureSpec) + keyboardSize;
                    int contentHeightSpec = View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), h - inputFieldHeight + AndroidUtilities.dp(2) - (onlySelect && !(initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) ? 0 : actionBar.getMeasuredHeight()) - topPadding) - (searchTabsView == null ? 0 : AndroidUtilities.dp(44)), View.MeasureSpec.EXACTLY);
                    child.measure(contentWidthSpec, contentHeightSpec);
                    child.setPivotX(child.getMeasuredWidth() / 2);
                } else if (commentView != null && commentView.isPopupView(child)) {
                    if (AndroidUtilities.isInMultiwindow) {
                        if (AndroidUtilities.isTablet()) {
                            child.measure(View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320), heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop()), View.MeasureSpec.EXACTLY));
                        } else {
                            child.measure(View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop(), View.MeasureSpec.EXACTLY));
                        }
                    } else {
                        child.measure(View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, View.MeasureSpec.EXACTLY));
                    }
                } else if (child == rightSlidingDialogContainer){
                    int h = View.MeasureSpec.getSize(heightMeasureSpec);
                    int transitionPadding = ((isSlideBackTransition || isDrawerTransition) ? (int) (h * 0.05f) : 0);
                    h += transitionPadding;
                    rightSlidingDialogContainer.setTransitionPaddingBottom(transitionPadding);
                    child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), h), View.MeasureSpec.EXACTLY));
                } else {
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                }
            }

            if (portrait != wasPortrait) {
                post(() -> {
                    if (selectAnimatedEmojiDialog != null) {
                        selectAnimatedEmojiDialog.dismiss();
                        selectAnimatedEmojiDialog = null;
                    }
                });
                wasPortrait = portrait;
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int count = getChildCount();

            int paddingBottom;
            Object tag = commentView != null ? commentView.getTag() : null;
            int keyboardSize = measureKeyboardHeight();
            if (tag != null && tag.equals(2)) {
                paddingBottom = keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow ? commentView.getEmojiPadding() : 0;
            } else {
                paddingBottom = 0;
            }

            setBottomClip(paddingBottom);
            lastMeasuredTopPadding = topPadding;

            for (int i = -1; i < count; i++) {
                final View child = i == -1 ? commentView : getChildAt(i);
                if (child == null || child.getVisibility() == GONE) {
                    continue;
                }
                final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP | Gravity.LEFT;
                }

                final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = r - width - lp.rightMargin;
                        break;
                    case Gravity.LEFT:
                    default:
                        childLeft = lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = lp.topMargin + getPaddingTop();
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = lp.topMargin;
                }


                if (commentView != null && commentView.isPopupView(child)) {
                    if (AndroidUtilities.isInMultiwindow) {
                        childTop = commentView.getTop() - child.getMeasuredHeight() + AndroidUtilities.dp(1);
                    } else {
                        childTop = commentView.getBottom();
                    }
                } else if (child == filterTabsView || child == searchTabsView || child == filtersView) {
                    childTop = actionBar.getMeasuredHeight();
                } else if (child == searchViewPager) {
                    childTop = (onlySelect && !(initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) ? 0 : actionBar.getMeasuredHeight()) + topPadding + (searchTabsView == null ? 0 : AndroidUtilities.dp(44));
                } else if (child instanceof DatabaseMigrationHint) {
                    childTop = actionBar.getMeasuredHeight();
                } else if (child instanceof ViewPage) {
                    if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                        if (filterTabsView != null && filterTabsView.getVisibility() == VISIBLE) {
                            childTop = AndroidUtilities.dp(44);
                        } else {
                            childTop = actionBar.getMeasuredHeight();
                        }
                    }
                    childTop += topPadding;
                } else if (child instanceof FragmentContextView) {
                    childTop += actionBar.getMeasuredHeight();
                } else if (child == floatingButtonContainer && selectAnimatedEmojiDialog != null) {
                    childTop += keyboardSize;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }

            searchViewPager.setKeyboardHeight(keyboardSize);
            notifyHeightChanged();
            updateContextViewPosition();
            updateCommentView();
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (actionBar.isActionModeShowed()) {
                    allowMoving = true;
                }
            }
            return checkTabsAnimationInProgress() || filterTabsView != null && filterTabsView.isAnimatingIndicator() || onTouchEvent(ev);
        }

        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (maybeStartTracking && !startedTracking) {
                onTouchEvent(null);
            }
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (parentLayout != null && filterTabsView != null && !filterTabsView.isEditing() && !searching && !rightSlidingDialogContainer.hasFragment() &&
                    !parentLayout.checkTransitionAnimation() && !parentLayout.isInPreviewMode() && !parentLayout.isPreviewOpenAnimationInProgress() && !parentLayout.getDrawerLayoutContainer().isDrawerOpened() &&
                    (ev == null || startedTracking || ev.getY() > actionBar.getMeasuredHeight() + actionBar.getTranslationY()) && SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_FOLDERS) {
                if (ev != null) {
                    if (velocityTracker == null) {
                        velocityTracker = VelocityTracker.obtain();
                    }
                    velocityTracker.addMovement(ev);
                }
                if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && checkTabsAnimationInProgress()) {
                    startedTracking = true;
                    startedTrackingPointerId = ev.getPointerId(0);
                    startedTrackingX = (int) ev.getX();
                    parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(false);
                    if (animatingForward) {
                        if (startedTrackingX < viewPages[0].getMeasuredWidth() + viewPages[0].getTranslationX()) {
                            additionalOffset = viewPages[0].getTranslationX();
                        } else {
                            ViewPage page = viewPages[0];
                            viewPages[0] = viewPages[1];
                            viewPages[1] = page;
                            animatingForward = false;
                            additionalOffset = viewPages[0].getTranslationX();
                            filterTabsView.selectTabWithId(viewPages[0].selectedType, 1f);
                            filterTabsView.selectTabWithId(viewPages[1].selectedType, additionalOffset / viewPages[0].getMeasuredWidth());
                            switchToCurrentSelectedMode(true);
                            viewPages[0].dialogsAdapter.resume();
                            viewPages[1].dialogsAdapter.pause();
                        }
                    } else {
                        if (startedTrackingX < viewPages[1].getMeasuredWidth() + viewPages[1].getTranslationX()) {
                            ViewPage page = viewPages[0];
                            viewPages[0] = viewPages[1];
                            viewPages[1] = page;
                            animatingForward = true;
                            additionalOffset = viewPages[0].getTranslationX();
                            filterTabsView.selectTabWithId(viewPages[0].selectedType, 1f);
                            filterTabsView.selectTabWithId(viewPages[1].selectedType, -additionalOffset / viewPages[0].getMeasuredWidth());
                            switchToCurrentSelectedMode(true);
                            viewPages[0].dialogsAdapter.resume();
                            viewPages[1].dialogsAdapter.pause();
                        } else {
                            additionalOffset = viewPages[0].getTranslationX();
                        }
                    }
                    tabsAnimation.removeAllListeners();
                    tabsAnimation.cancel();
                    tabsAnimationInProgress = false;
                } else if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
                    additionalOffset = 0;
                }
                if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking && filterTabsView.getVisibility() == VISIBLE) {
                    startedTrackingPointerId = ev.getPointerId(0);
                    maybeStartTracking = true;
                    startedTrackingX = (int) ev.getX();
                    startedTrackingY = (int) ev.getY();
                    velocityTracker.clear();
                } else if (ev != null && ev.getAction() == MotionEvent.ACTION_MOVE && ev.getPointerId(0) == startedTrackingPointerId) {
                    int dx = (int) (ev.getX() - startedTrackingX + additionalOffset);
                    int dy = Math.abs((int) ev.getY() - startedTrackingY);
                    if (startedTracking && (animatingForward && dx > 0 || !animatingForward && dx < 0)) {
                        if (!prepareForMoving(ev, dx < 0)) {
                            maybeStartTracking = true;
                            startedTracking = false;
                            viewPages[0].setTranslationX(0);
                            viewPages[1].setTranslationX(animatingForward ? viewPages[0].getMeasuredWidth() : -viewPages[0].getMeasuredWidth());
                            filterTabsView.selectTabWithId(viewPages[1].selectedType, 0);
                        }
                    }
                    if (maybeStartTracking && !startedTracking) {
                        float touchSlop = AndroidUtilities.getPixelsInCM(0.3f, true);
                        int dxLocal = (int) (ev.getX() - startedTrackingX);
                        if (Math.abs(dxLocal) >= touchSlop && Math.abs(dxLocal) > dy) {
                            prepareForMoving(ev, dx < 0);
                        }
                    } else if (startedTracking) {
                        viewPages[0].setTranslationX(dx);
                        if (animatingForward) {
                            viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() + dx);
                        } else {
                            viewPages[1].setTranslationX(dx - viewPages[0].getMeasuredWidth());
                        }
                        float scrollProgress = Math.abs(dx) / (float) viewPages[0].getMeasuredWidth();
                        if (viewPages[1].isLocked && scrollProgress > 0.3f) {
                            dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0));
                            filterTabsView.shakeLock(viewPages[1].selectedType);
                            AndroidUtilities.runOnUIThread(() -> {
                                showDialog(new LimitReachedBottomSheet(DialogsActivity.this, getContext(), LimitReachedBottomSheet.TYPE_FOLDERS, currentAccount));
                            }, 200);
                            return false;
                        } else {
                            filterTabsView.selectTabWithId(viewPages[1].selectedType, scrollProgress);
                        }
                    }
                } else if (ev == null || ev.getPointerId(0) == startedTrackingPointerId && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_POINTER_UP)) {
                    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                    float velX;
                    float velY;
                    if (ev != null && ev.getAction() != MotionEvent.ACTION_CANCEL) {
                        velX = velocityTracker.getXVelocity();
                        velY = velocityTracker.getYVelocity();
                        if (!startedTracking) {
                            if (Math.abs(velX) >= 3000 && Math.abs(velX) > Math.abs(velY)) {
                                prepareForMoving(ev, velX < 0);
                            }
                        }
                    } else {
                        velX = 0;
                        velY = 0;
                    }
                    if (startedTracking) {
                        float x = viewPages[0].getX();
                        tabsAnimation = new AnimatorSet();
                        if (viewPages[1].isLocked) {
                            backAnimation = true;
                        } else {
                            if (additionalOffset != 0) {
                                if (Math.abs(velX) > 1500) {
                                    backAnimation = animatingForward ? velX > 0 : velX < 0;
                                } else {
                                    if (animatingForward) {
                                        backAnimation = (viewPages[1].getX() > (viewPages[0].getMeasuredWidth() >> 1));
                                    } else {
                                        backAnimation = (viewPages[0].getX() < (viewPages[0].getMeasuredWidth() >> 1));
                                    }
                                }
                            } else {
                                backAnimation = Math.abs(x) < viewPages[0].getMeasuredWidth() / 3.0f && (Math.abs(velX) < 3500 || Math.abs(velX) < Math.abs(velY));
                            }
                        }
                        float dx;
                        if (backAnimation) {
                            dx = Math.abs(x);
                            if (animatingForward) {
                                tabsAnimation.playTogether(
                                        ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
                                        ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, viewPages[1].getMeasuredWidth())
                                );
                            } else {
                                tabsAnimation.playTogether(
                                        ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
                                        ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, -viewPages[1].getMeasuredWidth())
                                );
                            }
                        } else {
                            dx = viewPages[0].getMeasuredWidth() - Math.abs(x);
                            if (animatingForward) {
                                tabsAnimation.playTogether(
                                        ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, -viewPages[0].getMeasuredWidth()),
                                        ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
                                );
                            } else {
                                tabsAnimation.playTogether(
                                        ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, viewPages[0].getMeasuredWidth()),
                                        ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
                                );
                            }
                        }
                        tabsAnimation.setInterpolator(interpolator);

                        int width = getMeasuredWidth();
                        int halfWidth = width / 2;
                        float distanceRatio = Math.min(1.0f, 1.0f * dx / (float) width);
                        float distance = (float) halfWidth + (float) halfWidth * AndroidUtilities.distanceInfluenceForSnapDuration(distanceRatio);
                        velX = Math.abs(velX);
                        int duration;
                        if (velX > 0) {
                            duration = 4 * Math.round(1000.0f * Math.abs(distance / velX));
                        } else {
                            float pageDelta = dx / getMeasuredWidth();
                            duration = (int) ((pageDelta + 1.0f) * 100.0f);
                        }
                        duration = Math.max(150, Math.min(duration, 600));

                        tabsAnimation.setDuration(duration);
                        tabsAnimation.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                tabsAnimation = null;
                                if (!backAnimation) {
                                    ViewPage tempPage = viewPages[0];
                                    viewPages[0] = viewPages[1];
                                    viewPages[1] = tempPage;
                                    filterTabsView.selectTabWithId(viewPages[0].selectedType, 1.0f);
                                    updateCounters(false);
                                    viewPages[0].dialogsAdapter.resume();
                                    viewPages[1].dialogsAdapter.pause();
                                }
                                isFirstTab = viewPages[0].selectedType == filterTabsView.getFirstTabId();
                                updateDrawerSwipeEnabled();
                                viewPages[1].setVisibility(View.GONE);
                                showScrollbars(true);
                                tabsAnimationInProgress = false;
                                maybeStartTracking = false;
                                actionBar.setEnabled(true);
                                filterTabsView.setEnabled(true);
                                checkListLoad(viewPages[0]);
                            }
                        });
                        tabsAnimation.start();
                        tabsAnimationInProgress = true;
                        startedTracking = false;
                    } else {
                        isFirstTab = viewPages[0].selectedType == filterTabsView.getFirstTabId();
                        updateDrawerSwipeEnabled();
                        maybeStartTracking = false;
                        actionBar.setEnabled(true);
                        filterTabsView.setEnabled(true);
                    }
                    if (velocityTracker != null) {
                        velocityTracker.recycle();
                        velocityTracker = null;
                    }
                }
                return startedTracking;
            }
            return false;
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }

        @Override
        protected void drawList(Canvas blurCanvas, boolean top) {
            if (searchIsShowed) {
                if (searchViewPager != null && searchViewPager.getVisibility() == View.VISIBLE) {
                    searchViewPager.drawForBlur(blurCanvas);
                }
            } else {
                for (int i = 0; i < viewPages.length; i++) {
                    if (viewPages[i] != null && viewPages[i].getVisibility() == View.VISIBLE) {
                        for (int j = 0; j < viewPages[i].listView.getChildCount(); j++) {
                            View child = viewPages[i].listView.getChildAt(j);
                            if (child.getY() < viewPages[i].listView.blurTopPadding + AndroidUtilities.dp(100)) {
                                int restore = blurCanvas.save();
                                blurCanvas.translate(viewPages[i].getX(), viewPages[i].getY() + viewPages[i].listView.getY() + child.getY());
                                if (child instanceof DialogCell) {
                                    DialogCell cell = (DialogCell) child;
                                    cell.drawingForBlur = true;
                                    cell.draw(blurCanvas);
                                    cell.drawingForBlur = false;
                                } else {
                                    child.draw(blurCanvas);
                                }
                                blurCanvas.restoreToCount(restore);
                            }
                        }
                    }
                }
            }
        }
    }

    public static float viewOffset = 0.0f;

    public class DialogsRecyclerView extends BlurredRecyclerView {

        public boolean updateDialogsOnNextDraw;
        private boolean firstLayout = true;
        private boolean ignoreLayout;
        private final ViewPage parentPage;
        private int appliedPaddingTop;
        private int lastTop;
        private int lastListPadding;
        private float rightFragmentOpenedProgress;

        Paint paint = new Paint();
        RectF rectF = new RectF();
        private RecyclerListView animationSupportListView;
        LongSparseArray<View> animationSupportViewsByDialogId;
        private Paint selectorPaint;
        float lastDrawSelectorY;
        float selectorPositionProgress = 1f;
        float animateFromSelectorPosition;
        boolean animateSwitchingSelector;

        public DialogsRecyclerView(Context context, ViewPage page) {
            super(context);
            parentPage = page;
            additionalClipBottom = AndroidUtilities.dp(200);
        }

        public void prepareSelectorForAnimation() {
            selectorPositionProgress = 0;
            animateFromSelectorPosition = lastDrawSelectorY;
            animateSwitchingSelector = rightFragmentOpenedProgress != 0;
        }

        @Override
        protected boolean updateEmptyViewAnimated() {
            return true;
        }

        public void setViewsOffset(float viewOffset) {
            DialogsActivity.viewOffset = viewOffset;
            int n = getChildCount();
            for (int i = 0; i < n; i++) {
                getChildAt(i).setTranslationY(viewOffset);
            }

            if (selectorPosition != NO_POSITION) {
                View v = getLayoutManager().findViewByPosition(selectorPosition);
                if (v != null) {
                    selectorRect.set(v.getLeft(), (int) (v.getTop() + viewOffset), v.getRight(), (int) (v.getBottom() + viewOffset));
                    selectorDrawable.setBounds(selectorRect);
                }
            }
            invalidate();
        }

        public float getViewOffset() {
            return viewOffset;
        }

        @Override
        public void addView(View child, int index, ViewGroup.LayoutParams params) {
            super.addView(child, index, params);
            child.setTranslationY(viewOffset);
            child.setTranslationX(0);
            child.setAlpha(1f);
        }

        @Override
        public void removeView(View view) {
            super.removeView(view);
            view.setTranslationY(0);
            view.setTranslationX(0);
            view.setAlpha(1f);
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (parentPage.pullForegroundDrawable != null && viewOffset != 0) {
                int pTop = getPaddingTop();
                if (pTop != 0) {
                    canvas.save();
                    canvas.translate(0, pTop);
                }
                parentPage.pullForegroundDrawable.drawOverScroll(canvas);
                if (pTop != 0) {
                    canvas.restore();
                }
            }
            super.onDraw(canvas);
        }


        @Override
        protected void dispatchDraw(Canvas canvas) {
            canvas.save();
            if (rightFragmentOpenedProgress > 0) {
                canvas.clipRect(0, 0, AndroidUtilities.lerp(getMeasuredWidth(), AndroidUtilities.dp(RightSlidingDialogContainer.getRightPaddingSize()), rightFragmentOpenedProgress), getMeasuredHeight());
                paint.setColor(Theme.getColor(Theme.key_chats_pinnedOverlay));
                paint.setAlpha((int) (paint.getAlpha() * rightFragmentOpenedProgress));
                canvas.drawRect(0, 0, AndroidUtilities.dp(RightSlidingDialogContainer.getRightPaddingSize()), getMeasuredHeight(), paint);


                int alpha = Theme.dividerPaint.getAlpha();
                Theme.dividerPaint.setAlpha((int) (rightFragmentOpenedProgress * alpha));
                canvas.drawRect(AndroidUtilities.dp(RightSlidingDialogContainer.getRightPaddingSize()), 0, AndroidUtilities.dp(RightSlidingDialogContainer.getRightPaddingSize()) - 1, getMeasuredHeight(), Theme.dividerPaint);
                Theme.dividerPaint.setAlpha(alpha);
            }

            int maxSupportedViewsPosition = Integer.MIN_VALUE;
            int minSupportedViewsPosition = Integer.MAX_VALUE;

            if (animationSupportListView != null) {
                if (animationSupportViewsByDialogId == null) {
                    animationSupportViewsByDialogId = new LongSparseArray<>();
                }

                for (int i = 0; i < animationSupportListView.getChildCount(); i++) {
                    View child = animationSupportListView.getChildAt(i);
                    if (child instanceof DialogCell && child.getBottom() > 0) {
                        animationSupportViewsByDialogId.put(((DialogCell) child).getDialogId(), child);
                    }
                }
            }

            float maxTop = Integer.MAX_VALUE;
            float maxBottom = Integer.MIN_VALUE;
            DialogCell selectedCell = null;
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                DialogCell dialogCell = null;
                if (view instanceof DialogCell) {
                    dialogCell = (DialogCell) view;
                    dialogCell.setRightFragmentOpenedProgress(rightFragmentOpenedProgress);
                    if (AndroidUtilities.isTablet()) {
                        dialogCell.setDialogSelected(dialogCell.getDialogId() == openedDialogId.dialogId);
                    }
                    if (animationSupportViewsByDialogId != null && animationSupportListView != null) {
                        View animateToView = animationSupportViewsByDialogId.get(dialogCell.getDialogId());

                        animationSupportViewsByDialogId.delete(dialogCell.getDialogId());
                        if (animateToView != null) {
                            int supportViewPosition = animationSupportListView.getChildLayoutPosition(animateToView);
                            if (supportViewPosition > maxSupportedViewsPosition) {
                                maxSupportedViewsPosition = supportViewPosition;
                            }
                            if (supportViewPosition < minSupportedViewsPosition) {
                                minSupportedViewsPosition = supportViewPosition;
                            }
                            dialogCell.collapseOffset = (animateToView.getTop() - dialogCell.getTop()) * rightFragmentOpenedProgress;

                            if (dialogCell.getTop() + dialogCell.collapseOffset < maxTop) {
                                maxTop = dialogCell.getTop() + dialogCell.collapseOffset;
                            }
                            float bottom = dialogCell.getTop() + AndroidUtilities.lerp(dialogCell.getMeasuredHeight(), animateToView.getMeasuredHeight(), rightFragmentOpenedProgress);
                            if (bottom + dialogCell.collapseOffset > maxBottom) {
                                maxBottom = bottom + dialogCell.collapseOffset;
                            }
                        }
                    }
                    if (updateDialogsOnNextDraw) {
                        if (dialogCell.update(0, true)) {
                            int p = getChildAdapterPosition(dialogCell);
                            if (p >= 0) {
                                getAdapter().notifyItemChanged(p);
                            }
                        }
                    }
                    if (dialogCell.getDialogId() == rightSlidingDialogContainer.getCurrentFragmetDialogId()) {
                        selectedCell = dialogCell;
                    }
                }
                if (animationSupportListView != null) {
                    int restoreCount = canvas.save();
                    canvas.translate(view.getX(), view.getY());
                    if (dialogCell == null) {
                        canvas.saveLayerAlpha(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight(), (int) (255 * (1f - rightFragmentOpenedProgress)), Canvas.ALL_SAVE_FLAG);
                    }
                    view.draw(canvas);

                    if (dialogCell != null && dialogCell != selectedCell) {
                        dialogCell.collapseOffset = 0;
                    }
                    canvas.restoreToCount(restoreCount);
                }
            }


            if (selectedCell != null) {
                canvas.save();
                lastDrawSelectorY = selectedCell.getY() + selectedCell.collapseOffset + selectedCell.avatarImage.getImageY();
                selectedCell.collapseOffset = 0;
                if (selectorPositionProgress != 1f) {
                    selectorPositionProgress += 16 / 200f;
                    selectorPositionProgress = Utilities.clamp(selectorPositionProgress, 1f, 0f);
                    invalidate();
                }
                float selectorPositionProgress = CubicBezierInterpolator.DEFAULT.getInterpolation(this.selectorPositionProgress);
                boolean animateInOut = false;
                if (selectorPositionProgress != 1f && animateFromSelectorPosition != Integer.MIN_VALUE) {
                    if (Math.abs(animateFromSelectorPosition - lastDrawSelectorY) < getMeasuredHeight() * 0.4f) {
                        lastDrawSelectorY = AndroidUtilities.lerp(animateFromSelectorPosition, lastDrawSelectorY, selectorPositionProgress);
                    } else {
                        animateInOut = true;
                    }
                }

                float hideProgrss = animateSwitchingSelector && (animateInOut || animateFromSelectorPosition == Integer.MIN_VALUE) ? (1f - selectorPositionProgress) : (1f - rightFragmentOpenedProgress);
                if (hideProgrss == 1f) {
                    lastDrawSelectorY = Integer.MIN_VALUE;
                }
                float xOffset = -AndroidUtilities.dp(5) * hideProgrss;
                AndroidUtilities.rectTmp.set(-AndroidUtilities.dp(4) + xOffset, lastDrawSelectorY - AndroidUtilities.dp(1), AndroidUtilities.dp(4) + xOffset, lastDrawSelectorY + selectedCell.avatarImage.getImageHeight() + AndroidUtilities.dp(1));
                if (selectorPaint == null) {
                    selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                }
                selectorPaint.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                canvas.drawRoundRect(AndroidUtilities.rectTmp, AndroidUtilities.dp(4), AndroidUtilities.dp(4), selectorPaint);
                canvas.restore();
            } else {
                lastDrawSelectorY = Integer.MIN_VALUE;
            }

            //undrawing views
            if (animationSupportViewsByDialogId != null) {
                float maxUndrawTop = Integer.MIN_VALUE;
                float maxUndrawBottom = Integer.MAX_VALUE;
                for (int i = 0; i < animationSupportViewsByDialogId.size(); i++) {
                    View view = animationSupportViewsByDialogId.valueAt(i);
                    int position = animationSupportListView.getChildLayoutPosition(view);
                    if ( position < minSupportedViewsPosition && view.getTop() > maxUndrawTop) {
                        maxUndrawTop = view.getTop();
                    }
                    if (position > maxSupportedViewsPosition && view.getBottom() < maxUndrawBottom) {
                        maxUndrawBottom = view.getBottom();
                    }
                }
                for (int i = 0; i < animationSupportViewsByDialogId.size(); i++) {
                    View view = animationSupportViewsByDialogId.valueAt(i);
                    if (view instanceof DialogCell) {
                        int position = animationSupportListView.getChildLayoutPosition(view);
                        DialogCell dialogCell = (DialogCell) view;
                        dialogCell.isTransitionSupport = false;
                        dialogCell.buildLayout();
                        dialogCell.isTransitionSupport = true;
                        dialogCell.setRightFragmentOpenedProgress(rightFragmentOpenedProgress);

                        int restoreCount = canvas.save();
                        if (position > maxSupportedViewsPosition) {
                            canvas.translate(view.getX(), maxBottom + view.getBottom() - maxUndrawBottom);
                        } else {
                            canvas.translate(view.getX(), maxBottom + view.getTop() - maxUndrawTop);
                        }
                        view.draw(canvas);

                        canvas.restoreToCount(restoreCount);
                    }
                }
                animationSupportViewsByDialogId.clear();
            }

            updateDialogsOnNextDraw = false;
            if (animationSupportListView != null) {
                invalidate();
            }

            if (animationSupportListView == null) {
                super.dispatchDraw(canvas);
            }

            if (drawMovingViewsOverlayed()) {
                paint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);

                    if ((view instanceof DialogCell && ((DialogCell) view).isMoving()) || (view instanceof DialogsAdapter.LastEmptyView && ((DialogsAdapter.LastEmptyView) view).moving)) {
                        if (view.getAlpha() != 1f) {
                            rectF.set(view.getX(), view.getY(), view.getX() + view.getMeasuredWidth(), view.getY() + view.getMeasuredHeight());
                            canvas.saveLayerAlpha(rectF, (int) (255 * view.getAlpha()), Canvas.ALL_SAVE_FLAG);
                        } else {
                            canvas.save();
                        }
                        canvas.translate(view.getX(), view.getY());
                        canvas.drawRect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight(), paint);
                        view.draw(canvas);
                        canvas.restore();
                    }
                }
                invalidate();
            }
            if (slidingView != null && pacmanAnimation != null) {
                pacmanAnimation.draw(canvas, slidingView.getTop() + slidingView.getMeasuredHeight() / 2);
            }
        }

        private boolean drawMovingViewsOverlayed() {
            return getItemAnimator() != null && getItemAnimator().isRunning();
        }

        @Override
        public boolean drawChild(Canvas canvas, View child, long drawingTime) {
            if (drawMovingViewsOverlayed() && child instanceof DialogCell && ((DialogCell) child).isMoving()) {
                return true;
            }
            return super.drawChild(canvas, child, drawingTime);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        @Override
        public void setAdapter(RecyclerView.Adapter adapter) {
            super.setAdapter(adapter);
            firstLayout = true;
        }

        @Override
        protected void onMeasure(int widthSpec, int heightSpec) {
            int t = 0;
            if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                if (filterTabsView != null && filterTabsView.getVisibility() == VISIBLE) {
                    t = AndroidUtilities.dp(44);
                } else {
                    t = actionBar.getMeasuredHeight();
                }
            }

            int pos = parentPage.layoutManager.findFirstVisibleItemPosition();
            if (pos != RecyclerView.NO_POSITION && parentPage.itemTouchhelper.isIdle()) {
                RecyclerView.ViewHolder holder = parentPage.listView.findViewHolderForAdapterPosition(pos);
                if (holder != null) {
                    int top = holder.itemView.getTop();
                    if (parentPage.dialogsType == 0 && hasHiddenArchive() && parentPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN) {
                        pos = Math.max(1, pos);
                    }
                    ignoreLayout = true;
                    parentPage.layoutManager.scrollToPositionWithOffset(pos, (int) (top - lastListPadding + scrollAdditionalOffset));
                    ignoreLayout = false;
                }
            }
            if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                ignoreLayout = true;
                if (filterTabsView != null && filterTabsView.getVisibility() == VISIBLE) {
                    t = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
                } else {
                    t = inPreviewMode && Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
                }
                setTopGlowOffset(t);
                setPadding(0, t, 0, 0);
                parentPage.progressView.setPaddingTop(t);
                ignoreLayout = false;
            }

            if (firstLayout && getMessagesController().dialogsLoaded) {
                if (parentPage.dialogsType == 0 && hasHiddenArchive()) {
                    ignoreLayout = true;
                    LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                    layoutManager.scrollToPositionWithOffset(1, 0);
                    ignoreLayout = false;
                }
                firstLayout = false;
            }
            super.onMeasure(widthSpec, heightSpec);
            if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                if (appliedPaddingTop != t && viewPages != null && viewPages.length > 1 && !startedTracking && (tabsAnimation == null || !tabsAnimation.isRunning()) && !tabsAnimationInProgress && (filterTabsView == null || !filterTabsView.isAnimatingIndicator())) {
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            lastListPadding = getPaddingTop();
            lastTop = t;
            scrollAdditionalOffset = 0;
        }

        @Override
        public void requestLayout() {
            if (ignoreLayout) {
                return;
            }
            super.requestLayout();
        }

        private void toggleArchiveHidden(boolean action, DialogCell dialogCell) {
            SharedConfig.toggleArchiveHidden();
            if (SharedConfig.archiveHidden) {
                if (dialogCell != null) {
                    disableActionBarScrolling = true;
                    waitingForScrollFinished = true;
                    smoothScrollBy(0, (dialogCell.getMeasuredHeight() + (dialogCell.getTop() - getPaddingTop())), CubicBezierInterpolator.EASE_OUT);
                    if (action) {
                        updatePullAfterScroll = true;
                    } else {
                        updatePullState();
                    }
                }
                getUndoView().showWithAction(0, UndoView.ACTION_ARCHIVE_HIDDEN, null, null);
            } else {
                getUndoView().showWithAction(0, UndoView.ACTION_ARCHIVE_PINNED, null, null);
                updatePullState();
                if (action && dialogCell != null) {
                    dialogCell.resetPinnedArchiveState();
                    dialogCell.invalidate();
                }
            }
        }

        private void updatePullState() {
            parentPage.archivePullViewState = SharedConfig.archiveHidden ? ARCHIVE_ITEM_STATE_HIDDEN : ARCHIVE_ITEM_STATE_PINNED;
            if (parentPage.pullForegroundDrawable != null) {
                parentPage.pullForegroundDrawable.setWillDraw(parentPage.archivePullViewState != ARCHIVE_ITEM_STATE_PINNED);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (fastScrollAnimationRunning || waitingForScrollFinished || rightFragmentTransitionInProgress) {
                return false;
            }
            int action = e.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                setOverScrollMode(View.OVER_SCROLL_ALWAYS);
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (!parentPage.itemTouchhelper.isIdle() && parentPage.swipeController.swipingFolder) {
                    parentPage.swipeController.swipeFolderBack = true;
                    if (parentPage.itemTouchhelper.checkHorizontalSwipe(null, ItemTouchHelper.LEFT) != 0) {
                        if (parentPage.swipeController.currentItemViewHolder != null) {
                            ViewHolder viewHolder = parentPage.swipeController.currentItemViewHolder;
                            if (viewHolder.itemView instanceof DialogCell) {
                                DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                                long dialogId = dialogCell.getDialogId();
                                if (DialogObject.isFolderDialogId(dialogId)) {
                                    toggleArchiveHidden(false, dialogCell);
                                } else {
                                    TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
                                    if (dialog != null) {
                                        if (SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_READ) {
                                            ArrayList<Long> selectedDialogs = new ArrayList<>();
                                            selectedDialogs.add(dialogId);
                                            canReadCount = dialog.unread_count > 0 || dialog.unread_mark ? 1 : 0;
                                            performSelectedDialogsAction(selectedDialogs, read, true, false);
                                        } else if (SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_MUTE) {
                                            if (!getMessagesController().isDialogMuted(dialogId, 0)) {
                                                NotificationsController.getInstance(UserConfig.selectedAccount).setDialogNotificationsSettings(dialogId, 0, NotificationsController.SETTING_MUTE_FOREVER);
                                                if (BulletinFactory.canShowBulletin(DialogsActivity.this)) {
                                                    BulletinFactory.createMuteBulletin(DialogsActivity.this, NotificationsController.SETTING_MUTE_FOREVER).show();
                                                }
                                            } else {
                                                ArrayList<Long> selectedDialogs = new ArrayList<>();
                                                selectedDialogs.add(dialogId);
                                                canMuteCount = MessagesController.getInstance(currentAccount).isDialogMuted(dialogId, 0) ? 0 : 1;
                                                canUnmuteCount = canMuteCount > 0 ? 0 : 1;
                                                performSelectedDialogsAction(selectedDialogs, mute, true, false);
                                            }
                                        } else if (SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_PIN) {
                                            ArrayList<Long> selectedDialogs = new ArrayList<>();
                                            selectedDialogs.add(dialogId);
                                            boolean pinned = isDialogPinned(dialog);
                                            canPinCount = pinned ? 0 : 1;
                                            performSelectedDialogsAction(selectedDialogs, pin, true, false);
                                        } else if (SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_DELETE) {
                                            ArrayList<Long> selectedDialogs = new ArrayList<>();
                                            selectedDialogs.add(dialogId);
                                            performSelectedDialogsAction(selectedDialogs, delete, true, false);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            boolean result = super.onTouchEvent(e);
            if (parentPage.dialogsType == 0 && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) && parentPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN && hasHiddenArchive()) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                int currentPosition = layoutManager.findFirstVisibleItemPosition();
                if (currentPosition == 0) {
                    int pTop = getPaddingTop();
                    View view = layoutManager.findViewByPosition(currentPosition);
                    int height = (int) (AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78 : 72) * PullForegroundDrawable.SNAP_HEIGHT);
                    int diff = (view.getTop() - pTop) + view.getMeasuredHeight();
                    if (view instanceof DialogCell) {
                        long pullingTime = System.currentTimeMillis() - startArchivePullingTime;
                        if (diff < height || pullingTime < PullForegroundDrawable.minPullingTime) {
                            disableActionBarScrolling = true;
                            smoothScrollBy(0, diff, CubicBezierInterpolator.EASE_OUT_QUINT);
                            parentPage.archivePullViewState = ARCHIVE_ITEM_STATE_HIDDEN;
                        } else {
                            if (parentPage.archivePullViewState != ARCHIVE_ITEM_STATE_SHOWED) {
                                if (getViewOffset() == 0) {
                                    disableActionBarScrolling = true;
                                    smoothScrollBy(0, (view.getTop() - pTop), CubicBezierInterpolator.EASE_OUT_QUINT);
                                }
                                if (!canShowHiddenArchive) {
                                    canShowHiddenArchive = true;
                                    if (!NekoConfig.disableVibration.Bool()) {
                                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                                    }
                                    if (parentPage.pullForegroundDrawable != null) {
                                        parentPage.pullForegroundDrawable.colorize(true);
                                    }
                                }
                                ((DialogCell) view).startOutAnimation();
                                parentPage.archivePullViewState = ARCHIVE_ITEM_STATE_SHOWED;

                                if (NekoConfig.openArchiveOnPull.Bool()) {
                                    AndroidUtilities.runOnUIThread(() -> {
                                        // Open the folder.
                                        // Delay was taken from PullForegroundDrawable::startOutAnimation().
                                        Bundle args = new Bundle();
                                        args.putInt("folderId", 1); // 1 is the ID of the archive folder.
                                        args.putBoolean("onlySelect", onlySelect);
                                        DialogsActivity dialogsActivity = new DialogsActivity(args);
                                        dialogsActivity.setDelegate(delegate);
                                        presentFragment(dialogsActivity, onlySelect);
                                    }, 200);
                                }
                            }
                        }

                        if (getViewOffset() != 0) {
                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(getViewOffset(), 0f);
                            valueAnimator.addUpdateListener(animation -> setViewsOffset((float) animation.getAnimatedValue()));

                            valueAnimator.setDuration(Math.max(100, (long) (350f - 120f * (getViewOffset() / PullForegroundDrawable.getMaxOverscroll()))));
                            valueAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                            setScrollEnabled(false);
                            valueAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    setScrollEnabled(true);
                                }
                            });
                            valueAnimator.start();
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent e) {
            if (fastScrollAnimationRunning || waitingForScrollFinished || parentPage.dialogsItemAnimator.isRunning()) {
                return false;
            }
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                allowSwipeDuringCurrentTouch = !actionBar.isActionModeShowed();
            }
            return super.onInterceptTouchEvent(e);
        }

        @Override
        protected boolean allowSelectChildAtPosition(View child) {
            if (child instanceof HeaderCell && !child.isClickable()) {
                return false;
            }
            return true;
        }

        public void setOpenRightFragmentProgress(float progress) {
            rightFragmentOpenedProgress = progress;
            invalidate();
        }

        public void setAnimationSupportView(RecyclerListView animationSupportListView, float scrollOffset, boolean opened) {
            RecyclerListView anchorListView = animationSupportListView == null ? this.animationSupportListView : this;
            DialogCell anchorView = null;
            DialogCell selectedDialogView = null;
            if (anchorListView == null) {
                this.animationSupportListView = animationSupportListView;
                return;
            }
            int maxTop = Integer.MAX_VALUE;
            for (int i = 0; i < anchorListView.getChildCount(); i++) {
                View child = anchorListView.getChildAt(i);
                if (child instanceof DialogCell) {
                    DialogCell dialogCell = (DialogCell) child;
                    if (dialogCell.getDialogId() == rightSlidingDialogContainer.getCurrentFragmetDialogId()) {
                        selectedDialogView = dialogCell;
                    }
                    if (child.getTop() >= getPaddingTop() && dialogCell.getDialogId() != 0 && child.getTop() < maxTop){
                        anchorView = (DialogCell) child;
                        maxTop = anchorView.getTop();
                    }
                }
            }
            if (selectedDialogView != null && getAdapter().getItemCount() * AndroidUtilities.dp(70) > getMeasuredHeight() && (anchorView.getTop() - getPaddingTop()) > (getMeasuredHeight() - getPaddingTop()) / 2f) {
                anchorView = selectedDialogView;
            }
            this.animationSupportListView = animationSupportListView;

            if (anchorView != null) {
                if (animationSupportListView != null) {
                    animationSupportListView.setPadding(getPaddingLeft(), topPadding, getPaddingLeft(), getPaddingBottom());
                    if (anchorView != null) {
                        DialogsAdapter adapter = (DialogsAdapter) animationSupportListView.getAdapter();
                        int p = adapter.findDialogPosition(anchorView.getDialogId());
                        int offset = (int) (anchorView.getTop() - anchorListView.getPaddingTop() + scrollOffset);
                        if (p >= 0) {
                            ((LinearLayoutManager) animationSupportListView.getLayoutManager()).scrollToPositionWithOffset(p, adapter.fixScrollGap(this, p, offset, parentPage.dialogsType == 0 && parentPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN && hasHiddenArchive(), opened));
                        }
                    }
                }
                DialogsAdapter adapter = (DialogsAdapter) getAdapter();
                int p = adapter.findDialogPosition(anchorView.getDialogId());
                int offset = (int) (anchorView.getTop() - getPaddingTop());
                if (p >= 0) {
                    ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(p, offset);
                }
            }
        }
    }

    private class SwipeController extends ItemTouchHelper.Callback {

        private RectF buttonInstance;
        private RecyclerView.ViewHolder currentItemViewHolder;
        private boolean swipingFolder;
        private boolean swipeFolderBack;
        private ViewPage parentPage;

        public SwipeController(ViewPage page) {
            parentPage = page;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (waitingForDialogsAnimationEnd(parentPage) || parentLayout != null && parentLayout.isInPreviewMode() || rightSlidingDialogContainer.hasFragment()) {
                return 0;
            }
            if (swipingFolder && swipeFolderBack) {
                if (viewHolder.itemView instanceof DialogCell) {
                    ((DialogCell) viewHolder.itemView).swipeCanceled = true;
                }
                swipingFolder = false;
                return 0;
            }
            if (!onlySelect && parentPage.isDefaultDialogType() && slidingView == null && viewHolder.itemView instanceof DialogCell) {
                DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                long dialogId = dialogCell.getDialogId();
                if (actionBar.isActionModeShowed(null)) {
                    TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
                    if (!allowMoving || dialog == null || !isDialogPinned(dialog) || DialogObject.isFolderDialogId(dialogId)) {
                        return 0;
                    }
                    movingView = (DialogCell) viewHolder.itemView;
                    movingView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    swipeFolderBack = false;
                    return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
                } else {
                    if ((filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_FOLDERS) || !allowSwipeDuringCurrentTouch || ((dialogId == getUserConfig().clientUserId || dialogId == 777000) && SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_ARCHIVE) || getMessagesController().isPromoDialog(dialogId, false) && getMessagesController().promoDialogType != MessagesController.PROMO_TYPE_PSA) {
                        return 0;
                    }
                    boolean canSwipeBack = folderId == 0 && (SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_MUTE || SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_READ || SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_PIN || SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_DELETE) && !rightSlidingDialogContainer.hasFragment();
                    if (SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_READ) {
                        MessagesController.DialogFilter filter = null;
                        if (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) {
                            filter = getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0];
                        }
                        if (filter != null && (filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ) != 0) {
                            TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
                            if (dialog != null && !filter.alwaysShow(currentAccount, dialog) && (dialog.unread_count > 0 || dialog.unread_mark)) {
                                canSwipeBack = false;
                            }
                        }
                    }
                    swipeFolderBack = false;
                    swipingFolder = (canSwipeBack && !DialogObject.isFolderDialogId(dialogCell.getDialogId())) || (SharedConfig.archiveHidden && DialogObject.isFolderDialogId(dialogCell.getDialogId()));
                    dialogCell.setSliding(true);
                    return makeMovementFlags(0, ItemTouchHelper.LEFT);
                }
            }
            return 0;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (!(target.itemView instanceof DialogCell)) {
                return false;
            }
            DialogCell dialogCell = (DialogCell) target.itemView;
            long dialogId = dialogCell.getDialogId();
            TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
            if (dialog == null || !isDialogPinned(dialog) || DialogObject.isFolderDialogId(dialogId)) {
                return false;
            }
            int fromIndex = source.getAdapterPosition();
            int toIndex = target.getAdapterPosition();
            if (parentPage.listView.getItemAnimator() == null) {
                parentPage.listView.setItemAnimator(parentPage.dialogsItemAnimator);
            }
            parentPage.dialogsAdapter.moveDialogs(parentPage.listView, fromIndex, toIndex);

            if (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) {
                MessagesController.DialogFilter filter = getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0];
                if (!movingDialogFilters.contains(filter)) {
                    movingDialogFilters.add(filter);
                }
            } else {
                movingWas = true;
            }
            return true;
        }

        @Override
        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            if (swipeFolderBack) {
                return 0;
            }
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (viewHolder != null) {
                DialogCell dialogCell = (DialogCell) viewHolder.itemView;
                long dialogId = dialogCell.getDialogId();
                if (DialogObject.isFolderDialogId(dialogId)) {
                    parentPage.listView.toggleArchiveHidden(false, dialogCell);
                    return;
                }
                TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
                if (dialog == null) {
                    return;
                }

                if (!getMessagesController().isPromoDialog(dialogId, false) && folderId == 0 && SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_READ) {
                    ArrayList<Long> selectedDialogs = new ArrayList<>();
                    selectedDialogs.add(dialogId);
                    canReadCount = dialog.unread_count > 0 || dialog.unread_mark ? 1 : 0;
                    performSelectedDialogsAction(selectedDialogs, read, true, false);
                    return;
                }

                slidingView = dialogCell;
                int position = viewHolder.getAdapterPosition();
                int count = parentPage.dialogsAdapter.getItemCount();
                Runnable finishRunnable = () -> {
                    if (frozenDialogsList == null) {
                        return;
                    }
                    frozenDialogsList.remove(dialog);
                    int pinnedNum = dialog.pinnedNum;
                    slidingView = null;
                    parentPage.listView.invalidate();
                    int lastItemPosition = parentPage.layoutManager.findLastVisibleItemPosition();
                    if (lastItemPosition == count - 1) {
                        parentPage.layoutManager.findViewByPosition(lastItemPosition).requestLayout();
                    }
                    if (getMessagesController().isPromoDialog(dialog.id, false)) {
                        getMessagesController().hidePromoDialog();
                        parentPage.dialogsItemAnimator.prepareForRemove();
                        parentPage.updateList(true);
                    } else {
                        int added = getMessagesController().addDialogToFolder(dialog.id, folderId == 0 ? 1 : 0, -1, 0);
                        if (added != 2 || position != 0) {
                            parentPage.dialogsItemAnimator.prepareForRemove();
                            parentPage.updateList(true);
                        }
                        if (folderId == 0) {
                            if (added == 2) {
                                parentPage.dialogsItemAnimator.prepareForRemove();
                                if (position == 0) {
                                    setDialogsListFrozen(true);
                                    parentPage.updateList(true);
                                    checkAnimationFinished();
                                } else {
                                    parentPage.updateList(true);
                                    if (!SharedConfig.archiveHidden && parentPage.layoutManager.findFirstVisibleItemPosition() == 0) {
                                        disableActionBarScrolling = true;
                                        parentPage.listView.smoothScrollBy(0, -AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78 : 72));
                                    }
                                }
                                ArrayList<TLRPC.Dialog> dialogs = getDialogsArray(currentAccount, parentPage.dialogsType, folderId, false);
                                frozenDialogsList.add(0, dialogs.get(0));
                            } else if (added == 1) {
                                RecyclerView.ViewHolder holder = parentPage.listView.findViewHolderForAdapterPosition(0);
                                if (holder != null && holder.itemView instanceof DialogCell) {
                                    DialogCell cell = (DialogCell) holder.itemView;
                                    cell.checkCurrentDialogIndex(true);
                                    cell.animateArchiveAvatar();
                                }
                            }
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            boolean hintShowed = preferences.getBoolean("archivehint_l", false) || SharedConfig.archiveHidden;
                            if (!hintShowed) {
                                preferences.edit().putBoolean("archivehint_l", true).apply();
                            }
                            getUndoView().showWithAction(dialog.id, hintShowed ? UndoView.ACTION_ARCHIVE : UndoView.ACTION_ARCHIVE_HINT, null, () -> {
                                dialogsListFrozen = true;
                                getMessagesController().addDialogToFolder(dialog.id, 0, pinnedNum, 0);
                                dialogsListFrozen = false;
                                ArrayList<TLRPC.Dialog> dialogs = getMessagesController().getDialogs(0);
                                int index = dialogs.indexOf(dialog);
                                if (index >= 0) {
                                    ArrayList<TLRPC.Dialog> archivedDialogs = getMessagesController().getDialogs(1);
                                    if (!archivedDialogs.isEmpty() || index != 1) {
                                        setDialogsListFrozen(true);
                                        parentPage.dialogsItemAnimator.prepareForRemove();
                                        parentPage.updateList(true);
                                        checkAnimationFinished();
                                    }
                                    if (archivedDialogs.isEmpty()) {
                                        dialogs.remove(0);
                                        if (index == 1) {
                                            setDialogsListFrozen(true);
                                            parentPage.updateList(true);
                                            checkAnimationFinished();
                                        } else {
                                            frozenDialogsList.remove(0);
                                            parentPage.dialogsItemAnimator.prepareForRemove();
                                            parentPage.updateList(true);
                                        }
                                    }
                                } else {
                                    parentPage.updateList(false);
                                }
                            });
                        }
                        if (folderId != 0 && frozenDialogsList.isEmpty()) {
                            parentPage.listView.setEmptyView(null);
                            parentPage.progressView.setVisibility(View.INVISIBLE);
                        }
                    }
                };
                setDialogsListFrozen(true);
                if (Utilities.random.nextInt(1000) == 1) {
                    if (pacmanAnimation == null) {
                        pacmanAnimation = new PacmanAnimation(parentPage.listView);
                    }
                    pacmanAnimation.setFinishRunnable(finishRunnable);
                    pacmanAnimation.start();
                } else {
                    finishRunnable.run();
                }
            } else {
                slidingView = null;
            }
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                parentPage.listView.hideSelector(false);
            }
            currentItemViewHolder = viewHolder;
            if (viewHolder != null && viewHolder.itemView instanceof DialogCell) {
                ((DialogCell) viewHolder.itemView).swipeCanceled = false;
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
            if (animationType == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL) {
                return 200;
            } else if (animationType == ItemTouchHelper.ANIMATION_TYPE_DRAG) {
                if (movingView != null) {
                    View view = movingView;
                    AndroidUtilities.runOnUIThread(() -> view.setBackgroundDrawable(null), parentPage.dialogsItemAnimator.getMoveDuration());
                    movingView = null;
                }
            }
            return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            if (viewHolder != null && viewHolder.itemView instanceof DialogCell) {
                if (((DialogCell) viewHolder.itemView).getCurrentDialogFolderId() == 0
                && NaConfig.INSTANCE.getDoNotUnarchiveBySwipe().Bool()) {
                    return 1.0f;
                }
            }
            return 0.45f;
        }

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return 3500;
        }

        @Override
        public float getSwipeVelocityThreshold(float defaultValue) {
            return Float.MAX_VALUE;
        }
    }

    public interface DialogsActivityDelegate {
        void didSelectDialogs(DialogsActivity fragment, ArrayList<MessagesStorage.TopicKey> dids, CharSequence message, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        getConnectionsManager().updateDcSettings();
        getMessagesController().getBlockedPeers(true);

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            canSelectTopics =  arguments.getBoolean("canSelectTopics", false);
            cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);
            initialDialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
            allowSwitchAccount = arguments.getBoolean("allowSwitchAccount");
            checkCanWrite = arguments.getBoolean("checkCanWrite", true);
            afterSignup = arguments.getBoolean("afterSignup", false);
            folderId = arguments.getInt("folderId", 0);
            resetDelegate = arguments.getBoolean("resetDelegate", true);
            messagesCount = arguments.getInt("messagesCount", 0);
            hasPoll = arguments.getInt("hasPoll", 0);
            hasInvoice = arguments.getBoolean("hasInvoice", false);
            showSetPasswordConfirm = arguments.getBoolean("showSetPasswordConfirm", showSetPasswordConfirm);
            otherwiseReloginDays = arguments.getInt("otherwiseRelogin");
            allowGroups = arguments.getBoolean("allowGroups", true);
            allowChannels = arguments.getBoolean("allowChannels", true);
            allowUsers = arguments.getBoolean("allowUsers", true);
            allowBots = arguments.getBoolean("allowBots", true);
            closeFragment = arguments.getBoolean("closeFragment", true);
            allowGlobalSearch = arguments.getBoolean("allowGlobalSearch", true);
        }

        if (initialDialogsType == 0) {
            askAboutContacts = MessagesController.getGlobalNotificationsSettings().getBoolean("askAboutContacts", true);
        }

        if (searchString == null) {
            currentConnectionState = getConnectionsManager().getConnectionState();

            getNotificationCenter().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
            if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
                getNotificationCenter().addObserver(this, NotificationCenter.filterSettingsUpdated);
                getNotificationCenter().addObserver(this, NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().addObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            }
            getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
            getNotificationCenter().addObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.appDidLogout);
            getNotificationCenter().addObserver(this, NotificationCenter.openedChatChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.messageReceivedByAck);
            getNotificationCenter().addObserver(this, NotificationCenter.messageReceivedByServer);
            getNotificationCenter().addObserver(this, NotificationCenter.messageSendError);
            getNotificationCenter().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            getNotificationCenter().addObserver(this, NotificationCenter.replyMessagesDidLoad);
            getNotificationCenter().addObserver(this, NotificationCenter.reloadHints);
            getNotificationCenter().addObserver(this, NotificationCenter.didUpdateConnectionState);
            getNotificationCenter().addObserver(this, NotificationCenter.onDownloadingFilesChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.needDeleteDialog);
            getNotificationCenter().addObserver(this, NotificationCenter.folderBecomeEmpty);
            getNotificationCenter().addObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().addObserver(this, NotificationCenter.fileLoaded);
            getNotificationCenter().addObserver(this, NotificationCenter.fileLoadFailed);
            getNotificationCenter().addObserver(this, NotificationCenter.fileLoadProgressChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.dialogsUnreadReactionsCounterChanged);
            getNotificationCenter().addObserver(this, NotificationCenter.forceImportContactsStart);
            getNotificationCenter().addObserver(this, NotificationCenter.userEmojiStatusUpdated);
            getNotificationCenter().addObserver(this, NotificationCenter.currentUserPremiumStatusChanged);

            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.appUpdateAvailable);
        }
        getNotificationCenter().addObserver(this, NotificationCenter.messagesDeleted);

        getNotificationCenter().addObserver(this, NotificationCenter.onDatabaseMigration);
        getNotificationCenter().addObserver(this, NotificationCenter.onDatabaseOpened);
        getNotificationCenter().addObserver(this, NotificationCenter.didClearDatabase);

        loadDialogs(getAccountInstance());
        getMessagesController().loadPinnedDialogs(folderId, 0, null);
        if (databaseMigrationHint != null && !getMessagesStorage().isDatabaseMigrationInProgress()) {
            View localView = databaseMigrationHint;
            if (localView.getParent() != null) {
                ((ViewGroup) localView.getParent()).removeView(localView);
            }
            databaseMigrationHint = null;
        }
        return true;
    }

    public static void loadDialogs(AccountInstance accountInstance) {
        int currentAccount = accountInstance.getCurrentAccount();
        if (!dialogsLoaded.get(currentAccount, false)) {
            MessagesController messagesController = accountInstance.getMessagesController();
            messagesController.loadGlobalNotificationsSettings();
            messagesController.loadDialogs(0, 0, 100, true);
            messagesController.loadHintDialogs();
            messagesController.loadUserInfo(accountInstance.getUserConfig().getCurrentUser(), false, 0);
            accountInstance.getContactsController().checkInviteText();
            accountInstance.getMediaDataController().chekAllMedia(false);
            AndroidUtilities.runOnUIThread(() -> accountInstance.getDownloadController().loadDownloadingFiles(), 200);
            for (String emoji : messagesController.diceEmojies) {
                accountInstance.getMediaDataController().loadStickersByEmojiOrName(emoji, true, true);
            }
            dialogsLoaded.put(currentAccount, true);
        }
    }


    private Drawable premiumStar;
    public void updateStatus(TLRPC.User user, boolean animated) {
        if (statusDrawable == null || actionBar == null) {
            return;
        }
        if (user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusUntil && ((TLRPC.TL_emojiStatusUntil) user.emoji_status).until > (int) (System.currentTimeMillis() / 1000)) {
            statusDrawable.set(((TLRPC.TL_emojiStatusUntil) user.emoji_status).document_id, animated);
            actionBar.setRightDrawableOnClick(e -> showSelectStatusDialog());
            SelectAnimatedEmojiDialog.preload(currentAccount);
        } else if (user != null && user.emoji_status instanceof TLRPC.TL_emojiStatus) {
            statusDrawable.set(((TLRPC.TL_emojiStatus) user.emoji_status).document_id, animated);
            actionBar.setRightDrawableOnClick(e -> showSelectStatusDialog());
            SelectAnimatedEmojiDialog.preload(currentAccount);
        } else if (user != null && MessagesController.getInstance(currentAccount).isPremiumUser(user)) {
            if (premiumStar == null) {
                premiumStar = getContext().getResources().getDrawable(R.drawable.msg_premium_liststar).mutate();
                premiumStar = new AnimatedEmojiDrawable.WrapSizeDrawable(premiumStar, AndroidUtilities.dp(18), AndroidUtilities.dp(18)) {
                    @Override
                    public void draw(@NonNull Canvas canvas) {
                        canvas.save();
                        canvas.translate(AndroidUtilities.dp(-2), AndroidUtilities.dp(1));
                        super.draw(canvas);
                        canvas.restore();
                    }
                };
            }
            premiumStar.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_profile_verifiedBackground), PorterDuff.Mode.MULTIPLY));
            statusDrawable.set(premiumStar, animated);
            actionBar.setRightDrawableOnClick(e -> showSelectStatusDialog());
            SelectAnimatedEmojiDialog.preload(currentAccount);
        } else {
            statusDrawable.set((Drawable) null, animated);
            actionBar.setRightDrawableOnClick(null);
        }
        statusDrawable.setColor(Theme.getColor(Theme.key_profile_verifiedBackground));
        if (animatedStatusView != null) {
            animatedStatusView.setColor(Theme.getColor(Theme.key_profile_verifiedBackground));
        }
        if (selectAnimatedEmojiDialog != null && selectAnimatedEmojiDialog.getContentView() instanceof SelectAnimatedEmojiDialog) {
            SimpleTextView textView = actionBar.getTitleTextView();
            ((SelectAnimatedEmojiDialog) selectAnimatedEmojiDialog.getContentView()).setScrimDrawable(textView != null && textView.getRightDrawable() == statusDrawable ? statusDrawable : null, textView);
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            getNotificationCenter().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
            if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
                getNotificationCenter().removeObserver(this, NotificationCenter.filterSettingsUpdated);
                getNotificationCenter().removeObserver(this, NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().removeObserver(this, NotificationCenter.dialogsUnreadCounterChanged);
            }
            getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
            getNotificationCenter().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.contactsDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.appDidLogout);
            getNotificationCenter().removeObserver(this, NotificationCenter.openedChatChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageReceivedByAck);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageReceivedByServer);
            getNotificationCenter().removeObserver(this, NotificationCenter.messageSendError);
            getNotificationCenter().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            getNotificationCenter().removeObserver(this, NotificationCenter.replyMessagesDidLoad);
            getNotificationCenter().removeObserver(this, NotificationCenter.reloadHints);
            getNotificationCenter().removeObserver(this, NotificationCenter.didUpdateConnectionState);
            getNotificationCenter().removeObserver(this, NotificationCenter.onDownloadingFilesChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.needDeleteDialog);
            getNotificationCenter().removeObserver(this, NotificationCenter.folderBecomeEmpty);
            getNotificationCenter().removeObserver(this, NotificationCenter.newSuggestionsAvailable);
            getNotificationCenter().removeObserver(this, NotificationCenter.fileLoaded);
            getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadFailed);
            getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadProgressChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.dialogsUnreadReactionsCounterChanged);
            getNotificationCenter().removeObserver(this, NotificationCenter.forceImportContactsStart);
            getNotificationCenter().removeObserver(this, NotificationCenter.userEmojiStatusUpdated);
            getNotificationCenter().removeObserver(this, NotificationCenter.currentUserPremiumStatusChanged);

            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.appUpdateAvailable);
        }
        getNotificationCenter().removeObserver(this, NotificationCenter.messagesDeleted);

        getNotificationCenter().removeObserver(this, NotificationCenter.onDatabaseMigration);
        getNotificationCenter().removeObserver(this, NotificationCenter.onDatabaseOpened);
        getNotificationCenter().removeObserver(this, NotificationCenter.didClearDatabase);
        if (commentView != null) {
            commentView.onDestroy();
        }
        if (undoView[0] != null) {
            undoView[0].hide(true, 0);
        }
        getNotificationCenter().onAnimationFinish(animationIndex);
        delegate = null;
        SuggestClearDatabaseBottomSheet.dismissDialog();
    }

    @Override
    public ActionBar createActionBar(Context context) {
        ActionBar actionBar = new ActionBar(context) {

            @Override
            public void setTranslationY(float translationY) {
                if (translationY != getTranslationY() && fragmentView != null) {
                    fragmentView.invalidate();
                }
                super.setTranslationY(translationY);
            }

            @Override
            protected boolean shouldClipChild(View child) {
                return super.shouldClipChild(child) || child == doneItem;
            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                if (inPreviewMode && avatarContainer != null && child != avatarContainer) {
                    return false;
                }
                return super.drawChild(canvas, child, drawingTime);
            }

            @Override
            public void setTitleOverlayText(String title, int titleId, Runnable action) {
                super.setTitleOverlayText(title, titleId, action);
                if (selectAnimatedEmojiDialog != null && selectAnimatedEmojiDialog.getContentView() instanceof SelectAnimatedEmojiDialog) {
                    SimpleTextView textView = getTitleTextView();
                    ((SelectAnimatedEmojiDialog) selectAnimatedEmojiDialog.getContentView()).setScrimDrawable(textView != null && textView.getRightDrawable() == statusDrawable ? statusDrawable : null, textView);
                }
            }

            @Override
            protected boolean onSearchChangedIgnoreTitles() {
                return rightSlidingDialogContainer != null && rightSlidingDialogContainer.hasFragment();
            }

            @Override
            public void onSearchFieldVisibilityChanged(boolean visible) {
                if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.hasFragment()) {
                    getBackButton().animate().alpha(visible ? 1f : 0f).start();
                }
                super.onSearchFieldVisibilityChanged(visible);
            }
        };
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSelector), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), true);
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);
        if (inPreviewMode || AndroidUtilities.isTablet() && folderId != 0) {
            actionBar.setOccupyStatusBar(false);
        }
        return actionBar;
    }

    private int accounts;

    @Override
    public View createView(final Context context) {
        searching = false;
        searchWas = false;
        pacmanAnimation = null;
        selectedDialogs.clear();

        maximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

        AndroidUtilities.runOnUIThread(() -> Theme.createChatResources(context, false));

        ActionBarMenu menu = actionBar.createMenu();
        doneItem = new ActionBarMenuItem(context, null, Theme.getColor(Theme.key_actionBarDefaultSelector), Theme.getColor(Theme.key_actionBarDefaultIcon), true);
        doneItem.setText(LocaleController.getString("Done", R.string.Done).toUpperCase());
        actionBar.addView(doneItem, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT, 0, 0, 10, 0));
        doneItem.setOnClickListener(v -> {
            filterTabsView.setIsEditing(false);
            showDoneItem(false);
        });
        doneItem.setAlpha(0.0f);
        doneItem.setVisibility(View.GONE);
        if (!onlySelect && searchString == null && folderId == 0) {
            proxyDrawable = new ProxyDrawable(context);
            proxyItem = menu.addItem(2, proxyDrawable);
            proxyItem.setContentDescription(LocaleController.getString("ProxySettings", R.string.ProxySettings));

            passcodeDrawable = new RLottieDrawable(R.raw.passcode_lock_close, "passcode_lock_close", AndroidUtilities.dp(28), AndroidUtilities.dp(28), true, null);
            passcodeItem = menu.addItem(1, passcodeDrawable);
            passcodeItem.setContentDescription(LocaleController.getString("AccDescrPasscodeLock", R.string.AccDescrPasscodeLock));

            downloadsItem = menu.addItem(3, new ColorDrawable(Color.TRANSPARENT));
            downloadsItem.addView(new DownloadProgressIcon(currentAccount, context));
            downloadsItem.setContentDescription(LocaleController.getString("DownloadsTabs", R.string.DownloadsTabs));
            downloadsItem.setVisibility(View.GONE);

            updatePasscodeButton();
            updateProxyButton(false, false);
        }

        scanItem = menu.addItem(nekox_scanqr, R.drawable.msg_qrcode);
        scanItem.setContentDescription(LocaleController.getString("ScanQRCode", R.string.ScanQRCode));
        scanItem.setVisibility(View.GONE);

        searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true, false).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            boolean isSpeedItemCreated = false;

            @Override
            public void onPreToggleSearch() {
                if (!isSpeedItemCreated) {
                    isSpeedItemCreated = true;
                    FrameLayout searchContainer = (FrameLayout) searchItem.getSearchClearButton().getParent();
                    speedItem = new ActionBarMenuItem(context, menu, Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
                    speedItem.setIcon(R.drawable.avd_speed);
                    speedItem.getIconView().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), PorterDuff.Mode.SRC_IN));
                    speedItem.setTranslationX(AndroidUtilities.dp(32));
                    speedItem.setAlpha(0f);
                    speedItem.setOnClickListener(v -> showDialog(new PremiumFeatureBottomSheet(DialogsActivity.this, PremiumPreviewFragment.PREMIUM_FEATURE_DOWNLOAD_SPEED, true)));
                    speedItem.setClickable(false);
                    speedItem.setFixBackground(true);
                    FrameLayout.LayoutParams speedParams = new FrameLayout.LayoutParams(AndroidUtilities.dp(42), ViewGroup.LayoutParams.MATCH_PARENT);
                    speedParams.leftMargin = speedParams.rightMargin = AndroidUtilities.dp(14 + 24);
                    speedParams.gravity = Gravity.RIGHT;
                    searchContainer.addView(speedItem, speedParams);
                    searchItem.setSearchAdditionalButton(speedItem);

                    updateSpeedItem(searchViewPager.getCurrentPosition() == 2);
                }
            }


            @Override
            public void onSearchExpand() {
                searching = true;
                if (switchItem != null) {
                    switchItem.setVisibility(View.GONE);
                }
                if (proxyItem != null && proxyItemVisible) {
                    proxyItem.setVisibility(View.GONE);
                }
                if (downloadsItem != null && downloadsItemVisible) {
                    downloadsItem.setVisibility(View.GONE);
                }
                if (scanItem != null) {
                    scanItem.setVisibility(View.VISIBLE);
                }
                if (viewPages[0] != null) {
                    if (searchString != null) {
                        viewPages[0].listView.hide();
                        if (searchViewPager != null) {
                            searchViewPager.searchListView.show();
                        }
                    }
                    if (!onlySelect) {
                        floatingButtonContainer.setVisibility(View.GONE);
                    }
                }
                setScrollY(0);
                updatePasscodeButton();
                updateProxyButton(false, false);
                actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrGoBack", R.string.AccDescrGoBack));
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors);
                ((SizeNotifierFrameLayout) fragmentView).invalidateBlur();
            }

            @Override
            public boolean canCollapseSearch() {
                if (switchItem != null) {
                    switchItem.setVisibility(View.VISIBLE);
                }
                if (proxyItem != null && proxyItemVisible) {
                    proxyItem.setVisibility(View.VISIBLE);
                }
                if (downloadsItem != null && downloadsItemVisible) {
                    downloadsItem.setVisibility(View.VISIBLE);
                }
                if (scanItem != null) {
                    scanItem.setVisibility(View.GONE);
                }
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                searching = false;
                searchWas = false;
                if (viewPages[0] != null) {
                    viewPages[0].listView.setEmptyView(folderId == 0 ? viewPages[0].progressView : null);
                    if (!onlySelect) {
                        floatingButtonContainer.setVisibility(View.VISIBLE);
                        floatingHidden = true;
                        floatingButtonTranslation = AndroidUtilities.dp(100);
                        floatingButtonHideProgress = 1f;
                        updateFloatingButtonOffset();
                    }
                    showSearch(false, false, true);
                }
                updateProxyButton(false, false);
                updatePasscodeButton();
                if (menuDrawable != null) {
                    if (actionBar.getBackButton().getDrawable() != menuDrawable) {
                        actionBar.setBackButtonDrawable(menuDrawable);
                        menuDrawable.setRotation(0, true);
                    }
                    actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", R.string.AccDescrOpenMenu));
                }
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                ((SizeNotifierFrameLayout) fragmentView).invalidateBlur();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || (searchViewPager.dialogsSearchAdapter != null && searchViewPager.dialogsSearchAdapter.hasRecentSearch()) || searchFiltersWasShowed) {
                    searchWas = true;
                    if (!searchIsShowed) {
                        showSearch(true, false, true);
                    }
                }
                searchViewPager.onTextChanged(text);
            }

            @Override
            public void onSearchFilterCleared(FiltersView.MediaFilterData filterData) {
                if (!searchIsShowed) {
                    return;
                }
                searchViewPager.removeSearchFilter(filterData);
                searchViewPager.onTextChanged(searchItem.getSearchField().getText().toString());

                updateFiltersView(true, null, null, false, true);
            }

            @Override
            public boolean canToggleSearch() {
                return !actionBar.isActionModeShowed() && databaseMigrationHint == null;// && !rightSlidingDialogContainer.hasFragment();
            }
        });

        if (initialDialogsType == 2 || initialDialogsType == DIALOGS_TYPE_START_ATTACH_BOT) {
            searchItem.setVisibility(View.GONE);
        }
        searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
        searchItem.setContentDescription(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            if (initialDialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else if (initialDialogsType == 10) {
                actionBar.setTitle(LocaleController.getString("SelectChats", R.string.SelectChats));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
            actionBar.setOnLongClickListener(v -> {
                if (NekoConfig.hideAllTab.Bool() && filterTabsView != null && filterTabsView.getDefaultTabId() != filterTabsView.getCurrentTabId()) {
                    filterTabsView.toggleAllTabs(true);
                    filterTabsView.selectDefaultTab();
                }
                return false;
            });
        } else {
            if (searchString != null || folderId != 0) {
                actionBar.setBackButtonDrawable(backDrawable = new BackDrawable(false));
            } else {
                actionBar.setBackButtonDrawable(menuDrawable = new MenuDrawable());
                menuDrawable.setRoundCap();
                actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", R.string.AccDescrOpenMenu));
            }
            if (folderId != 0) {
                actionBar.setTitle(LocaleController.getString("ArchivedChats", R.string.ArchivedChats));
            } else {
                statusDrawable = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(null, AndroidUtilities.dp(26));
                statusDrawable.center = true;
                actionBar.setTitle(NaConfig.INSTANCE.getCustomTitle().String(), statusDrawable);
                actionBar.setOnLongClickListener(v -> {
                    if (NekoConfig.hideAllTab.Bool() && filterTabsView != null && filterTabsView.getCurrentTabId() != Integer.MAX_VALUE) {
                        filterTabsView.toggleAllTabs(true);
                        filterTabsView.selectDefaultTab();
                    }
                    return false;
                });
                updateStatus(UserConfig.getInstance(currentAccount).getCurrentUser(), false);
            }
            if (folderId == 0) {
                actionBar.setSupportsHolidayImage(true);
            }
        }
        if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
            actionBar.setAddToContainer(false);
            actionBar.setCastShadows(false);
            actionBar.setClipContent(true);
        }
        actionBar.setTitleActionRunnable(() -> {
            if (initialDialogsType != 10) {
                hideFloatingButton(false);
            }
            scrollToTop();
        });

        if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || initialDialogsType == 0 && folderId == 0 && !onlySelect && TextUtils.isEmpty(searchString)) {
            scrimPaint = new Paint() {
                @Override
                public void setAlpha(int a) {
                    super.setAlpha(a);
                    if (fragmentView != null) {
                        fragmentView.invalidate();
                    }
                }
            };

            filterTabsView = new FilterTabsView(context) {
                @Override
                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    maybeStartTracking = false;
                    return super.onInterceptTouchEvent(ev);
                }

                @Override
                public void setTranslationY(float translationY) {
                    if (getTranslationY() != translationY) {
                        super.setTranslationY(translationY);
                        updateContextViewPosition();
                        if (fragmentView != null) {
                            fragmentView.invalidate();
                        }
                    }
                }

                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    super.onLayout(changed, l, t, r, b);
                    if (scrimView != null) {
                        scrimView.getLocationInWindow(scrimViewLocation);
                        fragmentView.invalidate();
                    }
                }

                @Override
                protected void onDefaultTabMoved() {
                    if (!getMessagesController().premiumLocked) {
                        try {
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                        } catch (Exception ignore) {}
                        topBulletin = BulletinFactory.of(DialogsActivity.this).createSimpleBulletin(R.raw.filter_reorder, AndroidUtilities.replaceTags(LocaleController.formatString("LimitReachedReorderFolder", R.string.LimitReachedReorderFolder, LocaleController.getString(R.string.FilterAllChats))), LocaleController.getString("PremiumMore", R.string.PremiumMore), Bulletin.DURATION_PROLONG, () -> {
                            showDialog(new PremiumFeatureBottomSheet(DialogsActivity.this, PremiumPreviewFragment.PREMIUM_FEATURE_ADVANCED_CHAT_MANAGEMENT, true));
                            filterTabsView.setIsEditing(false);
                            showDoneItem(false);
                        }).show(true);
                    }
                }
            };

            filterTabsView.setVisibility(View.GONE);
            canShowFilterTabsView = false;
            filterTabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {

                private void showDeleteAlert(MessagesController.DialogFilter dialogFilter) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("FilterDelete", R.string.FilterDelete));
                    builder.setMessage(LocaleController.getString("FilterDeleteAlert", R.string.FilterDeleteAlert));
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    builder.setPositiveButton(LocaleController.getString("Delete", R.string.Delete), (dialog2, which2) -> {
                        TLRPC.TL_messages_updateDialogFilter req = new TLRPC.TL_messages_updateDialogFilter();
                        req.id = dialogFilter.id;
                        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {

                        }));
                        getMessagesController().removeFilter(dialogFilter);
                        getMessagesStorage().deleteDialogFilter(dialogFilter);
                    });
                    AlertDialog alertDialog = builder.create();
                    showDialog(alertDialog);
                    TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (button != null) {
                        button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                    }
                }

                @Override
                public void onSamePageSelected() {
                    scrollToTop();
                }

                @Override
                public void onPageReorder(int fromId, int toId) {
                    for (int a = 0; a < viewPages.length; a++) {
                        if (viewPages[a].selectedType == fromId) {
                            viewPages[a].selectedType = toId;
                        } else if (viewPages[a].selectedType == toId) {
                            viewPages[a].selectedType = fromId;
                        }
                    }
                }

                @Override
                public void onPageSelected(FilterTabsView.Tab tab, boolean forward) {
                    if (viewPages[0].selectedType == tab.id) {
                        return;
                    }
                    if (tab.isLocked) {
                        filterTabsView.shakeLock(tab.id);
                        showDialog(new LimitReachedBottomSheet(DialogsActivity.this, context, LimitReachedBottomSheet.TYPE_FOLDERS, currentAccount));
                        return;
                    }

                    ArrayList<MessagesController.DialogFilter> dialogFilters = getMessagesController().dialogFilters;
                    if (!tab.isDefault && (tab.id < 0 || tab.id >= dialogFilters.size())) {
                        return;
                    }
                    isFirstTab = tab.id == filterTabsView.getFirstTabId();
                    updateDrawerSwipeEnabled();

                    viewPages[1].selectedType = tab.id;
                    viewPages[1].setVisibility(View.VISIBLE);
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
                    showScrollbars(false);
                    switchToCurrentSelectedMode(true);
                    animatingForward = forward;
                }

                @Override
                public boolean canPerformActions() {
                    return !searching;
                }

                @Override
                public void onPageScrolled(float progress) {
                    if (progress == 1 && viewPages[1].getVisibility() != View.VISIBLE && !searching) {
                        return;
                    }
                    if (animatingForward) {
                        viewPages[0].setTranslationX(-progress * viewPages[0].getMeasuredWidth());
                        viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() - progress * viewPages[0].getMeasuredWidth());
                    } else {
                        viewPages[0].setTranslationX(progress * viewPages[0].getMeasuredWidth());
                        viewPages[1].setTranslationX(progress * viewPages[0].getMeasuredWidth() - viewPages[0].getMeasuredWidth());
                    }
                    if (progress == 1) {
                        ViewPage tempPage = viewPages[0];
                        viewPages[0] = viewPages[1];
                        viewPages[1] = tempPage;
                        viewPages[1].setVisibility(View.GONE);
                        showScrollbars(true);
                        updateCounters(false);
                        checkListLoad(viewPages[0]);
                        viewPages[0].dialogsAdapter.resume();
                        viewPages[1].dialogsAdapter.pause();
                    }
                }

                @Override
                public int getTabCounter(int tabId) {
                    if (NaConfig.INSTANCE.getIgnoreFolderCount().Bool()) {
                        return 0;
                    }
                    if (tabId == filterTabsView.getDefaultTabId()) {
                        return getMessagesStorage().getMainUnreadCount();
                    }
                    ArrayList<MessagesController.DialogFilter> dialogFilters = getMessagesController().dialogFilters;
                    if (tabId < 0 || tabId >= dialogFilters.size()) {
                        return 0;
                    }
                    return dialogFilters.get(tabId).unreadCount;
                }

                @Override
                public boolean didSelectTab(FilterTabsView.TabView tabView, boolean selected) {
                    if (actionBar.isActionModeShowed()) {
                        return false;
                    }
                    if (scrimPopupWindow != null) {
                        scrimPopupWindow.dismiss();
                        scrimPopupWindow = null;
                        scrimPopupWindowItems = null;
                        return false;
                    }

                    Rect rect = new Rect();
                    MessagesController.DialogFilter dialogFilter;
                    if (tabView.getId() == filterTabsView.getDefaultTabId()) {
                        dialogFilter = null;
                    } else {
                        dialogFilter = getMessagesController().dialogFilters.get(tabView.getId());
                    }

                    ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getParentActivity());
                    popupLayout.setOnTouchListener(new View.OnTouchListener() {

                        private int[] pos = new int[2];

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                                if (scrimPopupWindow != null && scrimPopupWindow.isShowing()) {
                                    View contentView = scrimPopupWindow.getContentView();
                                    contentView.getLocationInWindow(pos);
                                    rect.set(pos[0], pos[1], pos[0] + contentView.getMeasuredWidth(), pos[1] + contentView.getMeasuredHeight());
                                    if (!rect.contains((int) event.getX(), (int) event.getY())) {
                                        scrimPopupWindow.dismiss();
                                    }
                                }
                            } else if (event.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
                                if (scrimPopupWindow != null && scrimPopupWindow.isShowing()) {
                                    scrimPopupWindow.dismiss();
                                }
                            }
                            return false;
                        }
                    });
                    popupLayout.setDispatchKeyEventListener(keyEvent -> {
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && scrimPopupWindow != null && scrimPopupWindow.isShowing()) {
                            scrimPopupWindow.dismiss();
                        }
                    });
                    Rect backgroundPaddings = new Rect();
                    Drawable shadowDrawable = getParentActivity().getResources().getDrawable(R.drawable.popup_fixed_alert).mutate();
                    shadowDrawable.getPadding(backgroundPaddings);
                    popupLayout.setBackgroundDrawable(shadowDrawable);
                    popupLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));

                    LinearLayout linearLayout = new LinearLayout(getParentActivity());
                    ScrollView scrollView;
                    if (Build.VERSION.SDK_INT >= 21) {
                        scrollView = new ScrollView(getParentActivity(), null, 0, R.style.scrollbarShapeStyle) {
                            @Override
                            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                                setMeasuredDimension(linearLayout.getMeasuredWidth(), getMeasuredHeight());
                            }
                        };
                    } else {
                        scrollView = new ScrollView(getParentActivity());
                    }
                    scrollView.setClipToPadding(false);
                    popupLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

                    linearLayout.setMinimumWidth(AndroidUtilities.dp(200));
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    scrimPopupWindowItems = new ActionBarMenuSubItem[4];
                    for (int a = 0, N = (tabView.getId() == filterTabsView.getDefaultTabId() ? 3 : 4); a < 4; a++) {
                        ActionBarMenuSubItem cell = new ActionBarMenuSubItem(getParentActivity(), a == 0, a == N - 1);
                        if (a == 0) {
                            if (getMessagesController().dialogFilters.size() <= 1) continue;
                            cell.setTextAndIcon(LocaleController.getString("FilterReorder", R.string.FilterReorder), R.drawable.tabs_reorder);
                        } else if (a == 1) {
                            if (N == 3) {
                                cell.setTextAndIcon(LocaleController.getString("FilterEditAll", R.string.FilterEditAll), R.drawable.msg_edit);
                            } else {
                                cell.setTextAndIcon(LocaleController.getString("FilterEdit", R.string.FilterEdit), R.drawable.msg_edit);
                            }
                        } else if (a == 2) {
                            if (N == 3) continue;
                            cell.setTextAndIcon(LocaleController.getString("FilterDeleteItem", R.string.FilterDeleteItem), R.drawable.msg_delete);
                        } else {
                            cell.setTextAndIcon(LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead), R.drawable.msg_markread);
                        }
                        scrimPopupWindowItems[a] = cell;
                        linearLayout.addView(cell);
                        final int i = a;
                        cell.setOnClickListener(v1 -> {
                            if (i == 0) {
                                resetScroll();
                                filterTabsView.setIsEditing(true);
                                showDoneItem(true);
                            } else if (i == 1) {
                                if (N == 3) {
                                    presentFragment(new FiltersSetupActivity());
                                } else {
                                    presentFragment(new FilterCreateActivity(dialogFilter));
                                }
                            } else if (i == 2) {
                                showDeleteAlert(dialogFilter);
                            } else {
                                if (dialogFilter == null) {
                                    int folderId = tabView.getId() == Integer.MAX_VALUE ? 0 : -1;
                                    getMessagesStorage().readAllDialogs(folderId);
                                } else {
                                    if (dialogFilter.dialogs.isEmpty()) {
                                        getMessagesController().loadTabDialogs(dialogFilter);
                                    }
                                    for (TLRPC.Dialog dialog : dialogFilter.dialogs) {
                                        if (dialog.unread_count == 0 && dialog.unread_mentions_count == 0)
                                            continue;
                                        if (getMessagesController().isForum(dialog.id)) {
                                            getMessagesController().markAllTopicsAsRead(dialog.id);
                                        }
                                        getMessagesController().markDialogAsRead(dialog.id, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, dialog.unread_count, true, 0);
                                    }
                                }

                            }
                            if (scrimPopupWindow != null) {
                                scrimPopupWindow.dismiss();
                            }
                        });
                    }
                    scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
                    scrimPopupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
                        @Override
                        public void dismiss() {
                            super.dismiss();
                            if (scrimPopupWindow != this) {
                                return;
                            }
                            scrimPopupWindow = null;
                            scrimPopupWindowItems = null;
                            if (scrimAnimatorSet != null) {
                                scrimAnimatorSet.cancel();
                                scrimAnimatorSet = null;
                            }
                            scrimAnimatorSet = new AnimatorSet();
                            scrimViewAppearing = false;
                            ArrayList<Animator> animators = new ArrayList<>();
                            animators.add(ObjectAnimator.ofInt(scrimPaint, AnimationProperties.PAINT_ALPHA, 0));
                            scrimAnimatorSet.playTogether(animators);
                            scrimAnimatorSet.setDuration(220);
                            scrimAnimatorSet.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (scrimView != null) {
                                        scrimView.setBackground(null);
                                        scrimView = null;
                                    }
                                    if (fragmentView != null) {
                                        fragmentView.invalidate();
                                    }
                                }
                            });
                            scrimAnimatorSet.start();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                getParentActivity().getWindow().getDecorView().setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
                            }
                        }
                    };
                    scrimViewBackground = Theme.createRoundRectDrawable(AndroidUtilities.dp(6), 0, Theme.getColor(Theme.key_actionBarDefault));
                    scrimPopupWindow.setDismissAnimationDuration(220);
                    scrimPopupWindow.setOutsideTouchable(true);
                    scrimPopupWindow.setClippingEnabled(true);
                    scrimPopupWindow.setAnimationStyle(R.style.PopupContextAnimation);
                    scrimPopupWindow.setFocusable(true);
                    popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
                    scrimPopupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
                    scrimPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
                    scrimPopupWindow.getContentView().setFocusableInTouchMode(true);
                    tabView.getLocationInWindow(scrimViewLocation);
                    int popupX = scrimViewLocation[0] + backgroundPaddings.left - AndroidUtilities.dp(16);
                    if (popupX < AndroidUtilities.dp(6)) {
                        popupX = AndroidUtilities.dp(6);
                    } else if (popupX > fragmentView.getMeasuredWidth() - AndroidUtilities.dp(6) - popupLayout.getMeasuredWidth()) {
                        popupX = fragmentView.getMeasuredWidth() - AndroidUtilities.dp(6) - popupLayout.getMeasuredWidth();
                    }
                    int popupY = scrimViewLocation[1] + tabView.getMeasuredHeight() - AndroidUtilities.dp(12);

                    scrimPopupWindow.showAtLocation(fragmentView, Gravity.LEFT | Gravity.TOP, popupX, popupY);
                    scrimView = tabView;
                    scrimViewSelected = selected;
                    fragmentView.invalidate();
                    if (scrimAnimatorSet != null) {
                        scrimAnimatorSet.cancel();
                    }
                    scrimAnimatorSet = new AnimatorSet();
                    scrimViewAppearing = true;
                    ArrayList<Animator> animators = new ArrayList<>();
                    animators.add(ObjectAnimator.ofInt(scrimPaint, AnimationProperties.PAINT_ALPHA, 0, 50));
                    scrimAnimatorSet.playTogether(animators);
                    scrimAnimatorSet.setDuration(150);
                    scrimAnimatorSet.start();

                    return true;
                }

                @Override
                public boolean isTabMenuVisible() {
                    return scrimPopupWindow != null && scrimPopupWindow.isShowing();
                }

                @Override
                public void onDeletePressed(int id) {
                    showDeleteAlert(getMessagesController().dialogFilters.get(id));
                }
            });
        }

        if (allowSwitchAccount && UserConfig.getActivatedAccountsCount() > 1) {
            switchItem = menu.addItemWithWidth(1, 0, AndroidUtilities.dp(56));
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setTextSize(AndroidUtilities.dp(12));

            BackupImageView imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(18));
            switchItem.addView(imageView, LayoutHelper.createFrame(36, 36, Gravity.CENTER));

            TLRPC.User user = getUserConfig().getCurrentUser();
            avatarDrawable.setInfo(user);
            imageView.getImageReceiver().setCurrentAccount(currentAccount);
            Drawable thumb = user != null && user.photo != null && user.photo.strippedBitmap != null ? user.photo.strippedBitmap : avatarDrawable;
            imageView.setImage(ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), "50_50", ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_STRIPPED), "50_50", thumb, user);

            int accounts = 0;
            for (int a : SharedConfig.activeAccounts) {
                if (PasscodeHelper.isAccountHidden(a)) continue;
                TLRPC.User u = AccountInstance.getInstance(a).getUserConfig().getCurrentUser();
                if (u != null) {
                    AccountSelectCell cell = new AccountSelectCell(context, false);
                    cell.setAccount(a, true);
                    switchItem.addSubItem(10 + a, cell, AndroidUtilities.dp(230), AndroidUtilities.dp(48));
                    accounts = a > accounts + 1 ? a + 1 : accounts + 1;
                }
            }
            this.accounts = accounts;
        }
        actionBar.setAllowOverlayTitle(true);

        if (sideMenu != null) {
            sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.getAdapter().notifyDataSetChanged();
        }

        createActionMode(null);

        ContentView contentView = new ContentView(context);
        fragmentView = contentView;

        int pagesCount = (initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || (folderId == 0 && initialDialogsType == 0 && !onlySelect) ? 2 : 1;
        viewPages = new ViewPage[pagesCount];
        for (int a = 0; a < pagesCount; a++) {
            final ViewPage viewPage = new ViewPage(context) {
                @Override
                public void setTranslationX(float translationX) {
                    if (getTranslationX() != translationX) {
                        super.setTranslationX(translationX);
                        if (tabsAnimationInProgress) {
                            if (viewPages[0] == this) {
                                float scrollProgress = Math.abs(viewPages[0].getTranslationX()) / (float) viewPages[0].getMeasuredWidth();
                                filterTabsView.selectTabWithId(viewPages[1].selectedType, scrollProgress);
                            }
                        }
                        contentView.invalidateBlur();
                    }
                }
            };
            contentView.addView(viewPage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            viewPage.dialogsType = initialDialogsType;
            viewPages[a] = viewPage;

            viewPage.progressView = new FlickerLoadingView(context);
            viewPage.progressView.setViewType(FlickerLoadingView.DIALOG_CELL_TYPE);
            viewPage.progressView.setVisibility(View.GONE);
            viewPage.addView(viewPage.progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

            viewPage.listView = new DialogsRecyclerView(context, viewPage);
            viewPage.listView.setAllowStopHeaveOperations(true);
            viewPage.listView.setAccessibilityEnabled(false);
            viewPage.listView.setAnimateEmptyView(true, RecyclerListView.EMPTY_VIEW_ANIMATION_TYPE_ALPHA);
            viewPage.listView.setClipToPadding(false);
            viewPage.listView.setPivotY(0);
            viewPage.dialogsItemAnimator = new DialogsItemAnimator(viewPage.listView) {
                @Override
                public void onRemoveStarting(RecyclerView.ViewHolder item) {
                    super.onRemoveStarting(item);
                    if (viewPage.layoutManager.findFirstVisibleItemPosition() == 0) {
                        View v = viewPage.layoutManager.findViewByPosition(0);
                        if (v != null) {
                            v.invalidate();
                        }
                        if (viewPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN) {
                            viewPage.archivePullViewState = ARCHIVE_ITEM_STATE_SHOWED;
                        }
                        if (viewPage.pullForegroundDrawable != null) {
                            viewPage.pullForegroundDrawable.doNotShow();
                        }
                    }
                }
            };
            viewPage.listView.setVerticalScrollBarEnabled(true);
            viewPage.listView.setInstantClick(true);
            viewPage.layoutManager = new LinearLayoutManager(context) {

                private boolean fixOffset;

                @Override
                public void scrollToPositionWithOffset(int position, int offset) {
                    if (fixOffset) {
                        offset -= viewPage.listView.getPaddingTop();
                    }
                    super.scrollToPositionWithOffset(position, offset);
                }

                @Override
                public void prepareForDrop(@NonNull View view, @NonNull View target, int x, int y) {
                    fixOffset = true;
                    super.prepareForDrop(view, target, x, y);
                    fixOffset = false;
                }

                @Override
                public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                    if (hasHiddenArchive() && position == 1) {
                        super.smoothScrollToPosition(recyclerView, state, position);
                    } else {
                        LinearSmoothScrollerCustom linearSmoothScroller = new LinearSmoothScrollerCustom(recyclerView.getContext(), LinearSmoothScrollerCustom.POSITION_MIDDLE);
                        linearSmoothScroller.setTargetPosition(position);
                        startSmoothScroll(linearSmoothScroller);
                    }
                }

                @Override
                public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                    if (viewPage.listView.fastScrollAnimationRunning) {
                        return 0;
                    }
                    boolean isDragging = viewPage.listView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;

                    int measuredDy = dy;
                    int pTop = viewPage.listView.getPaddingTop();
                    if (viewPage.dialogsType == 0 && !onlySelect && folderId == 0 && dy < 0 && getMessagesController().hasHiddenArchive() && viewPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN) {
                        viewPage.listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                        int currentPosition = viewPage.layoutManager.findFirstVisibleItemPosition();
                        if (currentPosition == 0) {
                            View view = viewPage.layoutManager.findViewByPosition(currentPosition);
                            if (view != null && (view.getBottom() - pTop) <= AndroidUtilities.dp(1)) {
                                currentPosition = 1;
                            }
                        }
                        if (!isDragging) {
                            View view = viewPage.layoutManager.findViewByPosition(currentPosition);
                            if (view != null) {
                                int dialogHeight = AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78 : 72) + 1;
                                int canScrollDy = -(view.getTop() - pTop) + (currentPosition - 1) * dialogHeight;
                                int positiveDy = Math.abs(dy);
                                if (canScrollDy < positiveDy) {
                                    measuredDy = -canScrollDy;
                                }
                            }
                        } else if (currentPosition == 0) {
                            View v = viewPage.layoutManager.findViewByPosition(currentPosition);
                            float k = 1f + ((v.getTop() - pTop) / (float) v.getMeasuredHeight());
                            if (k > 1f) {
                                k = 1f;
                            }
                            viewPage.listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                            measuredDy *= PullForegroundDrawable.startPullParallax - PullForegroundDrawable.endPullParallax * k;
                            if (measuredDy > -1) {
                                measuredDy = -1;
                            }
                            if (undoView[0].getVisibility() == View.VISIBLE) {
                                undoView[0].hide(true, 1);
                            }
                        }
                    }

                    if (viewPage.dialogsType == 0 && viewPage.listView.getViewOffset() != 0 && dy > 0 && isDragging) {
                        float ty = (int) viewPage.listView.getViewOffset();
                        ty -= dy;
                        if (ty < 0) {
                            measuredDy = (int) ty;
                            ty = 0;
                        } else {
                            measuredDy = 0;
                        }
                        viewPage.listView.setViewsOffset(ty);
                    }

                    if (viewPage.dialogsType == 0 && viewPage.archivePullViewState != ARCHIVE_ITEM_STATE_PINNED && hasHiddenArchive()) {
                        int usedDy = super.scrollVerticallyBy(measuredDy, recycler, state);
                        if (viewPage.pullForegroundDrawable != null) {
                            viewPage.pullForegroundDrawable.scrollDy = usedDy;
                        }
                        int currentPosition = viewPage.layoutManager.findFirstVisibleItemPosition();
                        View firstView = null;
                        if (currentPosition == 0) {
                            firstView = viewPage.layoutManager.findViewByPosition(currentPosition);
                        }
                        if (currentPosition == 0 && firstView != null && (firstView.getBottom() - pTop) >= AndroidUtilities.dp(4)) {
                            if (startArchivePullingTime == 0) {
                                startArchivePullingTime = System.currentTimeMillis();
                            }
                            if (viewPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN) {
                                if (viewPage.pullForegroundDrawable != null) {
                                    viewPage.pullForegroundDrawable.showHidden();
                                }
                            }
                            float k = 1f + ((firstView.getTop() - pTop) / (float) firstView.getMeasuredHeight());
                            if (k > 1f) {
                                k = 1f;
                            }
                            long pullingTime = System.currentTimeMillis() - startArchivePullingTime;
                            boolean canShowInternal = k > PullForegroundDrawable.SNAP_HEIGHT && pullingTime > PullForegroundDrawable.minPullingTime + 20;
                            if (canShowHiddenArchive != canShowInternal) {
                                canShowHiddenArchive = canShowInternal;
                                if (viewPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN) {
                                    if (!NekoConfig.disableVibration.Bool()) {
                                        viewPage.listView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                                    }
                                    if (viewPage.pullForegroundDrawable != null) {
                                        viewPage.pullForegroundDrawable.colorize(canShowInternal);
                                    }
                                }
                            }
                            if (viewPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN && measuredDy - usedDy != 0 && dy < 0 && isDragging) {
                                float ty;
                                float tk = (viewPage.listView.getViewOffset() / PullForegroundDrawable.getMaxOverscroll());
                                tk = 1f - tk;
                                ty = (viewPage.listView.getViewOffset() - dy * PullForegroundDrawable.startPullOverScroll * tk);
                                viewPage.listView.setViewsOffset(ty);
                            }
                            if (viewPage.pullForegroundDrawable != null) {
                                viewPage.pullForegroundDrawable.pullProgress = k;
                                viewPage.pullForegroundDrawable.setListView(viewPage.listView);
                            }
                        } else {
                            startArchivePullingTime = 0;
                            canShowHiddenArchive = false;
                            viewPage.archivePullViewState = ARCHIVE_ITEM_STATE_HIDDEN;
                            if (viewPage.pullForegroundDrawable != null) {
                                viewPage.pullForegroundDrawable.resetText();
                                viewPage.pullForegroundDrawable.pullProgress = 0f;
                                viewPage.pullForegroundDrawable.setListView(viewPage.listView);
                            }
                        }
                        if (firstView != null) {
                            firstView.invalidate();
                        }
                        return usedDy;
                    }
                    return super.scrollVerticallyBy(measuredDy, recycler, state);
                }

                @Override
                public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                    if (BuildVars.DEBUG_PRIVATE_VERSION) {
                        try {
                            super.onLayoutChildren(recycler, state);
                        } catch (IndexOutOfBoundsException e) {
                            throw new RuntimeException("Inconsistency detected. " + "dialogsListIsFrozen=" + dialogsListFrozen + " lastUpdateAction=" + debugLastUpdateAction);
                        }
                    } else {
                        try {
                            super.onLayoutChildren(recycler, state);
                        } catch (IndexOutOfBoundsException e) {
                            FileLog.e(e);
                            AndroidUtilities.runOnUIThread(() -> viewPage.dialogsAdapter.notifyDataSetChanged());
                        }
                    }
                }
            };
            viewPage.layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            viewPage.listView.setLayoutManager(viewPage.layoutManager);
            viewPage.listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
            viewPage.addView(viewPage.listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            viewPage.listView.setOnItemClickListener((view, position) -> {
                if (initialDialogsType == 10) {
                    onItemLongClick(viewPage.listView, view, position, 0, 0, viewPage.dialogsType, viewPage.dialogsAdapter);
                    return;
                } else if ((initialDialogsType == 11 || initialDialogsType == 13) && position == 1) {
                    Bundle args = new Bundle();
                    args.putBoolean("forImport", true);
                    long[] array = new long[]{getUserConfig().getClientUserId()};
                    args.putLongArray("result", array);
                    args.putInt("chatType", ChatObject.CHAT_TYPE_MEGAGROUP);
                    String title = arguments.getString("importTitle");
                    if (title != null) {
                        args.putString("title", title);
                    }
                    GroupCreateFinalActivity activity = new GroupCreateFinalActivity(args);
                    activity.setDelegate(new GroupCreateFinalActivity.GroupCreateFinalActivityDelegate() {
                        @Override
                        public void didStartChatCreation() {

                        }

                        @Override
                        public void didFinishChatCreation(GroupCreateFinalActivity fragment, long chatId) {
                            ArrayList<MessagesStorage.TopicKey> arrayList = new ArrayList<>();
                            arrayList.add(MessagesStorage.TopicKey.of(-chatId, 0));
                            DialogsActivityDelegate dialogsActivityDelegate = delegate;
                            if (closeFragment) {
                                removeSelfFromStack();
                            }
                            dialogsActivityDelegate.didSelectDialogs(DialogsActivity.this, arrayList, null, true);
                        }

                        @Override
                        public void didFailChatCreation() {

                        }
                    });
                    presentFragment(activity);
                    return;
                }
                onItemClick(view, position, viewPage.dialogsAdapter);
            });
            viewPage.listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListenerExtended() {
                @Override
                public boolean onItemClick(View view, int position, float x, float y) {
                    if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && filterTabsView.isEditing()) {
                        return false;
                    }
                    return onItemLongClick(viewPage.listView, view, position, x, y, viewPage.dialogsType, viewPage.dialogsAdapter);
                }

                @Override
                public void onMove(float dx, float dy) {
                    if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                        movePreviewFragment(dy);
                    }
                }

                @Override
                public void onLongClickRelease() {
                    if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                        finishPreviewFragment();
                    }
                }
            });
            viewPage.swipeController = new SwipeController(viewPage);
            viewPage.recyclerItemsEnterAnimator = new RecyclerItemsEnterAnimator(viewPage.listView, false);

            viewPage.itemTouchhelper = new ItemTouchHelper(viewPage.swipeController);
            viewPage.itemTouchhelper.attachToRecyclerView(viewPage.listView);

            viewPage.listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                private boolean wasManualScroll;

                private boolean stoppedAllHeavyOperations;
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        wasManualScroll = true;
                        scrollingManually = true;
                    } else {
                        scrollingManually = false;
                    }
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                        wasManualScroll = false;
                        disableActionBarScrolling = false;
                        if (waitingForScrollFinished) {
                            waitingForScrollFinished = false;
                            if (updatePullAfterScroll) {
                                viewPage.listView.updatePullState();
                                updatePullAfterScroll = false;
                            }
                            viewPage.dialogsAdapter.notifyDataSetChanged();
                        }

                        if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && viewPages[0].listView == recyclerView) {
                            int scrollY = (int) -actionBar.getTranslationY();
                            int actionBarHeight = ActionBar.getCurrentActionBarHeight();
                            if (scrollY != 0 && scrollY != actionBarHeight) {
                                if (scrollY < actionBarHeight / 2) {
                                    recyclerView.smoothScrollBy(0, -scrollY);
                                } else if (viewPages[0].listView.canScrollVertically(1)) {
                                    recyclerView.smoothScrollBy(0, actionBarHeight - scrollY);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    viewPage.dialogsItemAnimator.onListScroll(-dy);
                    checkListLoad(viewPage);
                    if (initialDialogsType != 10 && wasManualScroll && floatingButtonContainer.getVisibility() != View.GONE && recyclerView.getChildCount() > 0) {
                        int firstVisibleItem = viewPage.layoutManager.findFirstVisibleItemPosition();
                        if (firstVisibleItem != RecyclerView.NO_POSITION) {
                            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(firstVisibleItem);
                            if (!hasHiddenArchive() || holder != null && holder.getAdapterPosition() != 0) {
                                int firstViewTop = 0;
                                if (holder != null) {
                                    firstViewTop = holder.itemView.getTop();
                                }
                                boolean goingDown;
                                boolean changed = true;
                                if (prevPosition == firstVisibleItem) {
                                    final int topDelta = prevTop - firstViewTop;
                                    goingDown = firstViewTop < prevTop;
                                    changed = Math.abs(topDelta) > 1;
                                } else {
                                    goingDown = firstVisibleItem > prevPosition;
                                }
                                if (changed && scrollUpdated && (goingDown || scrollingManually)) {
                                    hideFloatingButton(goingDown);
                                }
                                prevPosition = firstVisibleItem;
                                prevTop = firstViewTop;
                                scrollUpdated = true;
                            }
                        }
                    }
                    if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && recyclerView == viewPages[0].listView && !searching && !actionBar.isActionModeShowed() && !disableActionBarScrolling && filterTabsViewIsVisible && !rightSlidingDialogContainer.hasFragment()) {
                        if (dy > 0 && hasHiddenArchive() && viewPages[0].dialogsType == 0) {
                            View child = recyclerView.getChildAt(0);
                            if (child != null) {
                                RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(child);
                                if (holder.getAdapterPosition() == 0) {
                                    int visiblePartAfterScroll = child.getMeasuredHeight() + (child.getTop() - recyclerView.getPaddingTop());
                                    if (visiblePartAfterScroll + dy > 0) {
                                        if (visiblePartAfterScroll < 0) {
                                            dy = -visiblePartAfterScroll;
                                        } else {
                                            return;
                                        }
                                    }
                                }
                            }
                        }

                        float currentTranslation = actionBar.getTranslationY();
                        float newTranslation = currentTranslation - dy;
                        if (newTranslation < -ActionBar.getCurrentActionBarHeight()) {
                            newTranslation = -ActionBar.getCurrentActionBarHeight();
                        } else if (newTranslation > 0) {
                            newTranslation = 0;
                        }
                        if (newTranslation != currentTranslation) {
                            setScrollY(newTranslation);
                        }
                    }
                    if (fragmentView != null) {
                        ((SizeNotifierFrameLayout) fragmentView).invalidateBlur();
                    }
                    if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.hasFragment()) {
                        viewPage.listView.invalidate();
                    }
                }
            });

            viewPage.archivePullViewState = SharedConfig.archiveHidden ? ARCHIVE_ITEM_STATE_HIDDEN : ARCHIVE_ITEM_STATE_PINNED;
            if (viewPage.pullForegroundDrawable == null && folderId == 0) {
                viewPage.pullForegroundDrawable = new PullForegroundDrawable(LocaleController.getString("AccSwipeForArchive", R.string.AccSwipeForArchive), LocaleController.getString("AccReleaseForArchive", R.string.AccReleaseForArchive)) {
                    @Override
                    protected float getViewOffset() {
                        return viewPage.listView.getViewOffset();
                    }
                };
                if (hasHiddenArchive()) {
                    viewPage.pullForegroundDrawable.showHidden();
                } else {
                    viewPage.pullForegroundDrawable.doNotShow();
                }
                viewPage.pullForegroundDrawable.setWillDraw(viewPage.archivePullViewState != ARCHIVE_ITEM_STATE_PINNED);
            }

            viewPage.dialogsAdapter = new DialogsAdapter(this, context, viewPage.dialogsType, folderId, onlySelect, selectedDialogs, currentAccount) {
                @Override
                public void notifyDataSetChanged() {
                    viewPage.lastItemsCount = getItemCount();
                    try {
                        super.notifyDataSetChanged();
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
@Override
                public void onButtonClicked(DialogCell dialogCell) {
                    if (dialogCell.getMessage() != null) {
                        TLRPC.TL_forumTopic topic = getMessagesController().getTopicsController().findTopic(-dialogCell.getDialogId(), MessageObject.getTopicId(dialogCell.getMessage().messageOwner, true));
                        if (topic != null) {
                            if (onlySelect) {
                                didSelectResult(dialogCell.getDialogId(), topic.id, false, false);
                            } else {
                                ForumUtilities.openTopic(DialogsActivity.this, -dialogCell.getDialogId(), topic, 0);
                            }
                        }
                    }
                }

                @Override
                public void onButtonLongPress(DialogCell dialogCell) {
                    onItemLongClick(viewPage.listView, dialogCell, viewPage.listView.getChildAdapterPosition(dialogCell), 0, 0, viewPage.dialogsType, viewPage.dialogsAdapter);
                }
            };
            viewPage.dialogsAdapter.setRecyclerListView(viewPage.listView);
            viewPage.dialogsAdapter.setForceShowEmptyCell(afterSignup);
            if (AndroidUtilities.isTablet() && openedDialogId.dialogId != 0) {
                viewPage.dialogsAdapter.setOpenedDialogId(openedDialogId.dialogId);
            }
            viewPage.dialogsAdapter.setArchivedPullDrawable(viewPage.pullForegroundDrawable);
            viewPage.listView.setAdapter(viewPage.dialogsAdapter);

            viewPage.listView.setEmptyView(folderId == 0 ? viewPage.progressView : null);
            viewPage.scrollHelper = new RecyclerAnimationScrollHelper(viewPage.listView, viewPage.layoutManager);

            if (a != 0) {
                viewPages[a].setVisibility(View.GONE);
            }
        }

        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        searchViewPager = new SearchViewPager(context, this, type, initialDialogsType, folderId, new SearchViewPager.ChatPreviewDelegate() {
            @Override
            public void startChatPreview(RecyclerListView listView, DialogCell cell) {
                showChatPreview(cell);
            }

            @Override
            public void move(float dy) {
                if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                    movePreviewFragment(dy);
                }
            }

            @Override
            public void finish() {
                if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                    finishPreviewFragment();
                }
            }
        }) {
            @Override
            protected void onTabPageSelected(int position) {
                updateSpeedItem(position == 2);
            }

            @Override
            protected long getDialogId(String query) {
                if (query != null && query.length() > 0 && rightSlidingDialogContainer != null && rightSlidingDialogContainer.getFragment() instanceof TopicsFragment) {
                    return ((TopicsFragment) rightSlidingDialogContainer.getFragment()).getDialogId();
                }
                return 0;
            }

            @Override
            protected boolean onBackProgress(float progress) {
                return false;
//                setSearchAnimationProgress(1f - progress, true);
//                return true;
            }

//            @Override
//            protected void onBack() {
//                actionBar.onSearchFieldVisibilityChanged(searchItem.toggleSearch(false));
//            }

            @Override
            protected boolean includeDownloads() {
                if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.hasFragment()) {
                    return false;
                }
                return true;
            }
        };
        contentView.addView(searchViewPager);

        searchViewPager.dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search, boolean animated) {
                if (searchViewPager.emptyView.getVisibility() == View.VISIBLE) {
                    animated = true;
                }
                if (searching && searchWas && searchViewPager.emptyView != null) {
                    if (search || searchViewPager.dialogsSearchAdapter.getItemCount() != 0) {
                        searchViewPager.emptyView.showProgress(true, animated);
                    } else {
                        searchViewPager.emptyView.showProgress(false, animated);
                    }
                }
                if (search && searchViewPager.dialogsSearchAdapter.getItemCount() == 0) {
                    searchViewPager.cancelEnterAnimation();
                }
            }

            @Override
            public void didPressedOnSubDialog(long did) {
                if (onlySelect) {
                    if (!validateSlowModeDialog(did)) {
                        return;
                    }
                    if (!selectedDialogs.isEmpty()) {
                        boolean checked = addOrRemoveSelectedDialog(did, null);
                        findAndUpdateCheckBox(did, checked);
                        updateSelectedCount();
                        actionBar.closeSearchField();
                    } else {
                        didSelectResult(did, 0,true, false);
                    }
                } else {
                    Bundle args = new Bundle();
                    if (DialogObject.isUserDialog(did)) {
                        args.putLong("user_id", did);
                    } else {
                        args.putLong("chat_id", -did);
                    }
                    closeSearch();
                    if (AndroidUtilities.isTablet() && viewPages != null) {
                        for (int a = 0; a < viewPages.length; a++) {
                            viewPages[a].dialogsAdapter.setOpenedDialogId(openedDialogId.dialogId = did);
                        }
                        updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                    }
                    if (searchString != null) {
                        if (getMessagesController().checkCanOpenChat(args, DialogsActivity.this)) {
                            getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (getMessagesController().checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(long did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = getMessagesController().getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("ChatHintsDeleteAlertTitle", R.string.ChatHintsDeleteAlertTitle));
                builder.setMessage(AndroidUtilities.replaceTags(LocaleController.formatString("ChatHintsDeleteAlert", R.string.ChatHintsDeleteAlert, ContactsController.formatName(user.first_name, user.last_name))));
                builder.setPositiveButton(LocaleController.getString("StickersRemove", R.string.StickersRemove), (dialogInterface, i) -> getMediaDataController().removePeer(did));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
            }

            @Override
            public void needClearList() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                if (searchViewPager.dialogsSearchAdapter.isSearchWas() && searchViewPager.dialogsSearchAdapter.isRecentSearchDisplayed()) {
                    builder.setTitle(LocaleController.getString("ClearSearchAlertPartialTitle", R.string.ClearSearchAlertPartialTitle));
                    builder.setMessage(LocaleController.formatPluralString("ClearSearchAlertPartial", searchViewPager.dialogsSearchAdapter.getRecentResultsCount()));
                    builder.setPositiveButton(LocaleController.getString("Clear", R.string.Clear), (dialogInterface, i) -> {
                        searchViewPager.dialogsSearchAdapter.clearRecentSearch();
                    });
                } else {
                    builder.setTitle(LocaleController.getString("ClearSearchAlertTitle", R.string.ClearSearchAlertTitle));
                    builder.setMessage(LocaleController.getString("ClearSearchAlert", R.string.ClearSearchAlert));
                    builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton), (dialogInterface, i) -> {
                        if (searchViewPager.dialogsSearchAdapter.isRecentSearchDisplayed()) {
                            searchViewPager.dialogsSearchAdapter.clearRecentSearch();
                        } else {
                            searchViewPager.dialogsSearchAdapter.clearRecentHashtags();
                        }
                    });
                }
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
            }

            @Override
            public void runResultsEnterAnimation() {
                if (searchViewPager != null) {
                    searchViewPager.runResultsEnterAnimation();
                }
            }

            @Override
            public boolean isSelected(long dialogId) {
                return selectedDialogs.contains(dialogId);
            }

            @Override
            public long getSearchForumDialogId() {
                if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.getFragment() instanceof TopicsFragment) {
                    return ((TopicsFragment) rightSlidingDialogContainer.getFragment()).getDialogId();
                }
                return 0;
            }
        });

        searchViewPager.searchListView.setOnItemClickListener((view, position) -> {
            if (initialDialogsType == 10) {
                onItemLongClick(searchViewPager.searchListView, view, position, 0, 0, -1, searchViewPager.dialogsSearchAdapter);
                return;
            }
            onItemClick(view, position, searchViewPager.dialogsSearchAdapter);
        });
        searchViewPager.searchListView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListenerExtended() {
            @Override
            public boolean onItemClick(View view, int position, float x, float y) {
                return onItemLongClick(searchViewPager.searchListView, view, position, x, y, -1, searchViewPager.dialogsSearchAdapter);
            }

            @Override
            public void onMove(float dx, float dy) {
                if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                    movePreviewFragment(dy);
                }
            }

            @Override
            public void onLongClickRelease() {
                if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                    finishPreviewFragment();
                }
            }
        });

        searchViewPager.setFilteredSearchViewDelegate((showMediaFilters, users, dates, archive) -> DialogsActivity.this.updateFiltersView(showMediaFilters, users, dates, archive, true));
        searchViewPager.setVisibility(View.GONE);

        filtersView = new FiltersView(getParentActivity(), null);
        filtersView.setOnItemClickListener((view, position) -> {
            filtersView.cancelClickRunnables(true);
            addSearchFilter(filtersView.getFilterAt(position));
        });
        contentView.addView(filtersView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP));
        filtersView.setVisibility(View.GONE);

        floatingButtonContainer = new FrameLayout(context);
        floatingButtonContainer.setVisibility(onlySelect && initialDialogsType != 10 || folderId != 0 ? View.GONE : View.VISIBLE);
        contentView.addView(floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButtonContainer.setOnClickListener(v -> {
            if (parentLayout != null && parentLayout.isInPreviewMode()) {
                finishPreviewFragment();
                return;
            }
            if (initialDialogsType == 10) {
                if (delegate == null || selectedDialogs.isEmpty()) {
                    return;
                }
                ArrayList<MessagesStorage.TopicKey> topicKeys = new ArrayList<>();
                for (int i = 0; i < selectedDialogs.size(); i++) {
                    topicKeys.add(MessagesStorage.TopicKey.of(selectedDialogs.get(i), 0));
                }
                delegate.didSelectDialogs(DialogsActivity.this, topicKeys, null, false);
            } else {
                if (floatingButton.getVisibility() != View.VISIBLE) {
                    return;
                }

                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new ContactsActivity(args));
            }
        });

        floatingButton = new RLottieImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.SRC_IN));
        if (initialDialogsType == 10) {
            floatingButton.setImageResource(R.drawable.floating_check);
            floatingButtonContainer.setContentDescription(LocaleController.getString("Done", R.string.Done));
        } else {
            floatingButton.setAnimation(R.raw.write_contacts_fab_icon, 52, 52);
            floatingButtonContainer.setContentDescription(LocaleController.getString("NewMessageTitle", R.string.NewMessageTitle));
        }
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButtonContainer, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButtonContainer.setStateListAnimator(animator);
            floatingButtonContainer.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        updateFloatingButtonColor();
        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        floatingProgressView = new RadialProgressView(context);
        floatingProgressView.setProgressColor(Theme.getColor(Theme.key_chats_actionIcon));
        floatingProgressView.setScaleX(0.1f);
        floatingProgressView.setScaleY(0.1f);
        floatingProgressView.setAlpha(0f);
        floatingProgressView.setVisibility(View.GONE);
        floatingProgressView.setSize(AndroidUtilities.dp(22));
        floatingButtonContainer.addView(floatingProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        searchTabsView = null;

        if (!onlySelect && initialDialogsType == 0) {
            fragmentLocationContextView = new FragmentContextView(context, this, true);
            fragmentLocationContextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
            contentView.addView(fragmentLocationContextView);

            fragmentContextView = new FragmentContextView(context, this, false) {
                @Override
                protected void playbackSpeedChanged(float value) {
                    if (Math.abs(value - 1.0f) > 0.001f || Math.abs(value - 1.8f) > 0.001f) {
                        getUndoView().showWithAction(0, Math.abs(value - 1.0f) > 0.001f ? UndoView.ACTION_PLAYBACK_SPEED_ENABLED : UndoView.ACTION_PLAYBACK_SPEED_DISABLED, value, null, null);
                    }
                }
            };
            fragmentContextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
            contentView.addView(fragmentContextView);

            fragmentContextView.setAdditionalContextView(fragmentLocationContextView);
            fragmentLocationContextView.setAdditionalContextView(fragmentContextView);
        } else if (initialDialogsType == 3) {
            if (commentView != null) {
                commentView.onDestroy();
            }
            commentView = new ChatActivityEnterView(getParentActivity(), contentView, null, false) {
                @Override
                public boolean dispatchTouchEvent(MotionEvent ev) {
                    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
                    }
                    return super.dispatchTouchEvent(ev);
                }
                @Override
                public void setTranslationY(float translationY) {
                    super.setTranslationY(translationY);
                    if (!commentViewAnimated) {
                        return;
                    }
                    if (commentViewBg != null) {
                        commentViewBg.setTranslationY(translationY);
                    }
                    if (writeButtonContainer != null) {
                        writeButtonContainer.setTranslationY(translationY);
                    }
                    if (selectedCountView != null) {
                        selectedCountView.setTranslationY(translationY);
                    }
                }
            };
            contentView.setClipChildren(false);
            contentView.setClipToPadding(false);
            commentView.allowBlur = false;
            commentView.forceSmoothKeyboard(true);
            commentView.setAllowStickersAndGifs(true, false, false);
            commentView.setForceShowSendButton(true, false);
            commentView.setPadding(0, 0, AndroidUtilities.dp(20), 0);
            commentView.setVisibility(View.GONE);
            commentView.getSendButton().setAlpha(0);
            commentViewBg = new View(getParentActivity());
            commentViewBg.setBackgroundColor(getThemedColor(Theme.key_chat_messagePanelBackground));
            contentView.addView(commentViewBg, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1600, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0, 0, -1600));
            contentView.addView(commentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
            commentView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
                @Override
                public void onMessageSend(CharSequence message, boolean notify, int scheduleDate) {
                    if (delegate == null || selectedDialogs.isEmpty()) {
                        return;
                    }
                    ArrayList<MessagesStorage.TopicKey> topicKeys = new ArrayList<>();
                    for (int i = 0; i < selectedDialogs.size(); i++) {
                        topicKeys.add(MessagesStorage.TopicKey.of(selectedDialogs.get(i), 0));
                    }
                    delegate.didSelectDialogs(DialogsActivity.this, topicKeys, message, false);
                }

                @Override
                public void onSwitchRecordMode(boolean video) {

                }

                @Override
                public void onTextSelectionChanged(int start, int end) {

                }

                @Override
                public void bottomPanelTranslationYChanged(float translation) {
                    if (commentViewAnimated) {
                        if (keyboardAnimator != null) {
                            keyboardAnimator.cancel();
                            keyboardAnimator = null;
                        }
                        if (commentView != null) {
                            commentView.setTranslationY(translation);
                            commentViewIgnoreTopUpdate = true;
                        }
                    }
                }

                @Override
                public void onStickersExpandedChange() {

                }

                @Override
                public void onPreAudioVideoRecord() {

                }

                @Override
                public void onTextChanged(final CharSequence text, boolean bigChange) {

                }

                @Override
                public void onTextSpansChanged(CharSequence text) {

                }

                @Override
                public void needSendTyping() {

                }

                @Override
                public void onAttachButtonHidden() {

                }

                @Override
                public void onAttachButtonShow() {

                }

                @Override
                public void onMessageEditEnd(boolean loading) {

                }

                @Override
                public void onWindowSizeChanged(int size) {

                }

                @Override
                public void onStickersTab(boolean opened) {

                }

                @Override
                public void didPressAttachButton() {

                }

                @Override
                public void needStartRecordVideo(int state, boolean notify, int scheduleDate) {

                }

                @Override
                public void needChangeVideoPreviewState(int state, float seekProgress) {

                }

                @Override
                public void needStartRecordAudio(int state) {

                }

                @Override
                public void needShowMediaBanHint() {

                }

                @Override
                public void onUpdateSlowModeButton(View button, boolean show, CharSequence time) {

                }

                @Override
                public void onSendLongClick() {

                }

                @Override
                public void onAudioVideoInterfaceUpdated() {

                }
            });

            writeButtonContainer = new FrameLayout(context) {
                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    info.setText(LocaleController.formatPluralString("AccDescrShareInChats", selectedDialogs.size()));
                    info.setClassName(Button.class.getName());
                    info.setLongClickable(true);
                    info.setClickable(true);
                }
            };
            writeButtonContainer.setFocusable(true);
            writeButtonContainer.setFocusableInTouchMode(true);
            writeButtonContainer.setVisibility(View.INVISIBLE);
            writeButtonContainer.setScaleX(0.2f);
            writeButtonContainer.setScaleY(0.2f);
            writeButtonContainer.setAlpha(0.0f);
            contentView.addView(writeButtonContainer, LayoutHelper.createFrame(60, 60, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 10));

            textPaint.setTextSize(AndroidUtilities.dp(12));
            textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            selectedCountView = new View(context) {
                @Override
                protected void onDraw(Canvas canvas) {
                    String text = String.format("%d", Math.max(1, selectedDialogs.size()));
                    int textSize = (int) Math.ceil(textPaint.measureText(text));
                    int size = Math.max(AndroidUtilities.dp(16) + textSize, AndroidUtilities.dp(24));
                    int cx = getMeasuredWidth() / 2;
                    int cy = getMeasuredHeight() / 2;

                    textPaint.setColor(getThemedColor(Theme.key_dialogRoundCheckBoxCheck));
                    paint.setColor(getThemedColor(Theme.isCurrentThemeDark() ? Theme.key_voipgroup_inviteMembersBackground : Theme.key_dialogBackground));
                    rect.set(cx - size / 2, 0, cx + size / 2, getMeasuredHeight());
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), paint);

                    paint.setColor(getThemedColor(Theme.key_dialogRoundCheckBox));
                    rect.set(cx - size / 2 + AndroidUtilities.dp(2), AndroidUtilities.dp(2), cx + size / 2 - AndroidUtilities.dp(2), getMeasuredHeight() - AndroidUtilities.dp(2));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(10), AndroidUtilities.dp(10), paint);

                    canvas.drawText(text, cx - textSize / 2, AndroidUtilities.dp(16.2f), textPaint);
                }
            };
            selectedCountView.setAlpha(0.0f);
            selectedCountView.setScaleX(0.2f);
            selectedCountView.setScaleY(0.2f);
            contentView.addView(selectedCountView, LayoutHelper.createFrame(42, 24, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, -8, 9));


            FrameLayout writeButtonBackground = new FrameLayout(context);
            Drawable writeButtonDrawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), getThemedColor(Theme.key_dialogFloatingButton), getThemedColor(Build.VERSION.SDK_INT >= 21 ? Theme.key_dialogFloatingButtonPressed : Theme.key_dialogFloatingButton));
            if (Build.VERSION.SDK_INT < 21) {
                Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
                shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
                CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
                combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                writeButtonDrawable = combinedDrawable;
            }
            writeButtonBackground.setBackgroundDrawable(writeButtonDrawable);
            writeButtonBackground.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                writeButtonBackground.setOutlineProvider(new ViewOutlineProvider() {
                    @SuppressLint("NewApi")
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                    }
                });
            }
            writeButtonBackground.setOnClickListener(v -> {
                if (delegate == null || selectedDialogs.isEmpty()) {
                    return;
                }
                ArrayList<MessagesStorage.TopicKey> topicKeys = new ArrayList<>();
                for (int i = 0; i < selectedDialogs.size(); i++) {
                    topicKeys.add(MessagesStorage.TopicKey.of(selectedDialogs.get(i), 0));
                }
                delegate.didSelectDialogs(DialogsActivity.this, topicKeys, commentView.getFieldText(), false);
            });
            writeButtonBackground.setOnLongClickListener(v -> {
                if (isNextButton) {
                    return false;
                }
                onSendLongClick(writeButtonBackground);
                return true;
            });

            writeButton = new ImageView[2];
            for (int a = 0; a < 2; ++a) {
                writeButton[a] = new ImageView(context);
                writeButton[a].setImageResource(a == 1 ? R.drawable.msg_arrow_forward : R.drawable.attach_send);
                writeButton[a].setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogFloatingIcon), PorterDuff.Mode.MULTIPLY));
                writeButton[a].setScaleType(ImageView.ScaleType.CENTER);
                writeButtonBackground.addView(writeButton[a], LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, Gravity.CENTER));
            }
            AndroidUtilities.updateViewVisibilityAnimated(writeButton[0], true, 0.5f, false);
            AndroidUtilities.updateViewVisibilityAnimated(writeButton[1], false, 0.5f, false);
            writeButtonContainer.addView(writeButtonBackground, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, Gravity.LEFT | Gravity.TOP, Build.VERSION.SDK_INT >= 21 ? 2 : 0, 0, 0, 0));
        }

        if (filterTabsView != null) {
            contentView.addView(filterTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44));
        }
        if ((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) || !onlySelect) {
            final FrameLayout.LayoutParams layoutParams = LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
            if (inPreviewMode && Build.VERSION.SDK_INT >= 21) {
                layoutParams.topMargin = AndroidUtilities.statusBarHeight;
            }
            contentView.addView(actionBar, layoutParams);

            animatedStatusView = new DrawerProfileCell.AnimatedStatusView(context, 20, 60);
            contentView.addView(animatedStatusView, LayoutHelper.createFrame(20, 20, Gravity.LEFT | Gravity.TOP));
        }

        if (searchString == null && initialDialogsType == 0) {
            // NekoX: Remove UPDATE NOW Bottom View in DialogsActivity
        }

        for (int a = 0; a < 2; a++) {
            undoView[a] = new UndoView(context) {
                @Override
                public void setTranslationY(float translationY) {
                    super.setTranslationY(translationY);
                    if (this == undoView[0] && undoView[1].getVisibility() != VISIBLE) {
                        additionalFloatingTranslation = getMeasuredHeight() + AndroidUtilities.dp(8) - translationY;
                        if (additionalFloatingTranslation < 0) {
                            additionalFloatingTranslation = 0;
                        }
                        if (!floatingHidden) {
                            updateFloatingButtonOffset();
                        }
                    }
                }

                @Override
                protected boolean canUndo() {
                    for (int a = 0; a < viewPages.length; a++) {
                        if (viewPages[a].dialogsItemAnimator.isRunning()) {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                protected void onRemoveDialogAction(long currentDialogId, int action) {
                    if (action == UndoView.ACTION_DELETE || action == UndoView.ACTION_DELETE_FEW) {
                        debugLastUpdateAction = 1;
                        setDialogsListFrozen(true);
                        if (frozenDialogsList != null) {
                            int selectedIndex = -1;
                            for (int i = 0; i < frozenDialogsList.size(); i++) {
                                if (frozenDialogsList.get(i).id == currentDialogId) {
                                    selectedIndex = i;
                                    break;
                                }
                            }

                            if (selectedIndex >= 0) {
                                TLRPC.Dialog dialog = frozenDialogsList.remove(selectedIndex);
                                viewPages[0].dialogsAdapter.notifyDataSetChanged();
                                int finalSelectedIndex = selectedIndex;
                                AndroidUtilities.runOnUIThread(() -> {
                                    if (frozenDialogsList != null) {
                                        if (finalSelectedIndex < 0 || finalSelectedIndex >= frozenDialogsList.size()) {
                                            return;
                                        }
                                        frozenDialogsList.add(finalSelectedIndex, dialog);
                                        viewPages[0].updateList(true);
                                    }
                                });
                            } else {
                                setDialogsListFrozen(false);
                            }
                        }
                        checkAnimationFinished();
                    }
                }
            };
            contentView.addView(undoView[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));
        }

        if (folderId != 0) {
            viewPages[0].listView.setGlowColor(Theme.getColor(Theme.key_actionBarDefaultArchived));
            actionBar.setTitleColor(Theme.getColor(Theme.key_actionBarDefaultArchivedTitle));
            actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultArchivedIcon), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultArchivedSelector), false);
            actionBar.setSearchTextColor(Theme.getColor(Theme.key_actionBarDefaultArchivedSearch), false);
            actionBar.setSearchTextColor(Theme.getColor(Theme.key_actionBarDefaultArchivedSearchPlaceholder), true);
        }

        if (!onlySelect && initialDialogsType == 0) {
            blurredView = new View(context) {
                @Override
                public void setAlpha(float alpha) {
                    super.setAlpha(alpha);
                    if (fragmentView != null) {
                        fragmentView.invalidate();
                    }
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                blurredView.setForeground(new ColorDrawable(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhite), 100)));
            }
            blurredView.setFocusable(false);
            blurredView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            blurredView.setOnClickListener(e -> {
                finishPreviewFragment();
            });
            blurredView.setVisibility(View.GONE);
            blurredView.setFitsSystemWindows(true);
            contentView.addView(blurredView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }

        actionBarDefaultPaint.setColor(Theme.getColor(folderId == 0 ? Theme.key_actionBarDefault : Theme.key_actionBarDefaultArchived));
        if (inPreviewMode) {
            final TLRPC.User currentUser = getUserConfig().getCurrentUser();
            avatarContainer = new ChatAvatarContainer(actionBar.getContext(), null, false);
            avatarContainer.setTitle(UserObject.getUserName(currentUser));
            avatarContainer.setSubtitle(LocaleController.formatUserStatus(currentAccount, currentUser));
            avatarContainer.setUserAvatar(currentUser, true);
            avatarContainer.setOccupyStatusBar(false);
            avatarContainer.setLeftPadding(AndroidUtilities.dp(10));
            actionBar.addView(avatarContainer, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 0, 40, 0));
            floatingButton.setVisibility(View.INVISIBLE);
            actionBar.setOccupyStatusBar(false);
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
            if (fragmentContextView != null) {
                contentView.removeView(fragmentContextView);
            }
            if (fragmentLocationContextView != null) {
                contentView.removeView(fragmentLocationContextView);
            }
        }

        searchIsShowed = false;
        updateFilterTabs(true, false);

        if (searchString != null) {
            showSearch(true, false, false);
            actionBar.openSearchField(searchString, false);
        } else if (initialSearchString != null) {
            showSearch(true, false, false);
            actionBar.openSearchField(initialSearchString, false);
            initialSearchString = null;
            if (filterTabsView != null) {
                filterTabsView.setTranslationY(-AndroidUtilities.dp(44));
            }
        } else {
            showSearch(false, false, false);
        }
        View backButton = actionBar.getBackButton();
        backButton.setOnLongClickListener(e -> {
            scrimPopupWindow = BackButtonMenu.showHistory(this, backButton);
            final ActionBarPopupWindow scrimPopupWindowBack = scrimPopupWindow;
            if (scrimPopupWindow != null) {
                scrimPopupWindow.setOnDismissListener(() -> {
                    if (scrimPopupWindow != scrimPopupWindowBack) {
                        return;
                    }
                    if (scrimAnimatorSet != null) {
                        scrimAnimatorSet.cancel();
                        scrimAnimatorSet = null;
                    }
                    scrimAnimatorSet = new AnimatorSet();
                    scrimViewAppearing = false;
                    ArrayList<Animator> animators = new ArrayList<>();
                    animators.add(ObjectAnimator.ofInt(scrimPaint, AnimationProperties.PAINT_ALPHA, 0));
                    scrimAnimatorSet.playTogether(animators);
                    scrimAnimatorSet.setDuration(220);
                    scrimAnimatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (scrimView != null) {
                                scrimView.setBackground(null);
                                scrimView = null;
                            }
                            if (fragmentView != null) {
                                fragmentView.invalidate();
                            }
                        }
                    });
                    scrimAnimatorSet.start();
                    scrimPopupWindow = null;
                    scrimPopupWindowItems = null;
                });
                if (scrimAnimatorSet != null) {
                    scrimAnimatorSet.cancel();
                } else {
                    scrimAnimatorSet = new AnimatorSet();
                }
                fragmentView.invalidate();
                scrimViewAppearing = true;
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(ObjectAnimator.ofInt(scrimPaint, AnimationProperties.PAINT_ALPHA, 0, 50));
                scrimAnimatorSet.playTogether(animators);
                scrimAnimatorSet.setDuration(150);
                scrimAnimatorSet.start();
                return true;
            } else {
                return false;
            }
        });

        updateMenuButton(false);
        actionBar.setDrawBlurBackground(contentView);

        rightSlidingDialogContainer = new RightSlidingDialogContainer(context) {

            boolean anotherFragmentOpened;
            DialogsActivity.ViewPage transitionPage;

            float fromScrollYProperty;

            @Override
            boolean getOccupyStatusbar() {
                return actionBar != null && actionBar.getOccupyStatusBar();
            }

            @Override
            public void openAnimationStarted(boolean open) {
                if (!isArchive()) {
                    actionBar.setBackButtonDrawable(menuDrawable = new MenuDrawable());
                    menuDrawable.setRoundCap();
                }

                rightFragmentTransitionInProgress = true;
                contentView.requestLayout();
                fromScrollYProperty = actionBar.getTranslationY();

                if (canShowFilterTabsView && filterTabsView != null) {
                    filterTabsView.setVisibility(View.VISIBLE);
                }
                transitionPage = viewPages[0];
                if (transitionPage.animationSupportListView == null) {
                    transitionPage.animationSupportListView = new BlurredRecyclerView(context) {

                        @Override
                        protected void dispatchDraw(Canvas canvas) {

                        }

                        @Override
                        public boolean dispatchTouchEvent(MotionEvent ev) {
                            return false;
                        }

                        @Override
                        public boolean onInterceptTouchEvent(MotionEvent e) {
                            return false;
                        }

                        @Override
                        public boolean onTouchEvent(MotionEvent e) {
                            return false;
                        }
                    };
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                    layoutManager.setNeedFixEndGap(false);
                    transitionPage.animationSupportListView.setLayoutManager(layoutManager);
                    transitionPage.animationSupportDialogsAdapter = new DialogsAdapter(DialogsActivity.this, context, transitionPage.dialogsType, folderId, onlySelect, selectedDialogs, currentAccount);
                    transitionPage.animationSupportDialogsAdapter.setIsTransitionSupport();
                    transitionPage.animationSupportListView.setAdapter(transitionPage.animationSupportDialogsAdapter);
                    transitionPage.addView(transitionPage.animationSupportListView);
                }

                transitionPage.animationSupportDialogsAdapter.setDialogsType(transitionPage.dialogsType);
                transitionPage.dialogsAdapter.setCollapsedView(false, transitionPage.listView);
                transitionPage.dialogsAdapter.setDialogsListFrozen(true);
                transitionPage.animationSupportDialogsAdapter.setDialogsListFrozen(true);
                transitionPage.layoutManager.setNeedFixEndGap(false);
                setDialogsListFrozen(true);
                hideFloatingButton(anotherFragmentOpened);
                transitionPage.dialogsAdapter.notifyDataSetChanged();
                transitionPage.animationSupportDialogsAdapter.notifyDataSetChanged();
                transitionPage.listView.setAnimationSupportView(transitionPage.animationSupportListView, -actionBar.getTranslationY(), open);
                transitionPage.listView.setClipChildren(false);
                actionBar.setAllowOverlayTitle(false);
                transitionPage.listView.stopScroll();
                updateDrawerSwipeEnabled();
            }

            @Override
            public void openAnimationFinished() {
                if (!canShowFilterTabsView && filterTabsView != null) {
                    filterTabsView.setVisibility(View.GONE);
                }
                transitionPage.layoutManager.setNeedFixGap(true);
                transitionPage.dialogsAdapter.setCollapsedView(hasFragment(),  transitionPage.listView);
                transitionPage.dialogsAdapter.setDialogsListFrozen(false);
                transitionPage.animationSupportDialogsAdapter.setDialogsListFrozen(false);
                setDialogsListFrozen(false);
                transitionPage.listView.setClipChildren(true);
                transitionPage.listView.invalidate();
                transitionPage.dialogsAdapter.notifyDataSetChanged();
                transitionPage.animationSupportDialogsAdapter.notifyDataSetChanged();
                transitionPage.listView.setAnimationSupportView(null, 0, hasFragment());
                rightFragmentTransitionInProgress = false;
                actionBar.setAllowOverlayTitle(!hasFragment());
                contentView.requestLayout();
                transitionPage.layoutManager.setNeedFixEndGap(!hasFragment());
                DialogsActivity.this.setScrollY(0);
                searchViewPager.updateTabs();
                updateDrawerSwipeEnabled();
            }

            @Override
            void setOpenProgress(float progress) {
                boolean opened = progress > 0f;
                if (anotherFragmentOpened != opened) {
                    anotherFragmentOpened = opened;
                }
                filterTabsMoveFrom = AndroidUtilities.dp(44);
                filterTabsProgress = canShowFilterTabsView ? 1f - progress : 0;
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }

                DialogsActivity.this.setScrollY(AndroidUtilities.lerp(fromScrollYProperty, 0, progress));
                updateDrawerSwipeEnabled();
                if (menuDrawable != null && hasFragment()) {
                    menuDrawable.setRotation(progress, false);
                }
                if (actionBar.getTitleTextView() != null) {
                    actionBar.getTitleTextView().setAlpha(1f - progress);
                    if (actionBar.getTitleTextView().getAlpha() > 0) {
                        actionBar.getTitleTextView().setVisibility(View.VISIBLE);
                    }
                }
                if (proxyItem != null) {
                    proxyItem.setAlpha(1f - progress);
                }
                if (downloadsItem != null) {
                    downloadsItem.setAlpha(1f - progress);
                }
                if (passcodeItem != null) {
                    passcodeItem.setAlpha(1f - progress);
                }
                if (searchItem != null) {
                    searchItem.setAlpha(anotherFragmentOpened ? 0 : 1f);
                }
                if (actionBar.getBackButton() != null) {
                    actionBar.getBackButton().setAlpha(progress == 1f ? 0f : 1f);
                }

                if (folderId != 0) {
                    actionBarDefaultPaint.setColor(
                        ColorUtils.blendARGB(
                            Theme.getColor(Theme.key_actionBarDefaultArchived),
                            Theme.getColor(Theme.key_actionBarDefault),
                            progress
                        )
                    );
                }

                if (transitionPage != null) {
                    transitionPage.listView.setOpenRightFragmentProgress(progress);
                }
            }
        };
        rightSlidingDialogContainer.setOpenProgress(0f);
        contentView.addView(rightSlidingDialogContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        if (new Random().nextInt(100) < 50)
            PrivacyUtil.postCheckAll(getParentActivity(), currentAccount);
        else if (new Random().nextInt(100) < 20)
            UpdateUtil.postCheckFollowChannel(getParentActivity(), currentAccount);

        return fragmentView;
    }

//    @Override
//    public ActionBar getActionBar() {
//        return rightSlidingDialogContainer != null && rightSlidingDialogContainer.currentActionBarView != null && rightSlidingDialogContainer.isOpenned ? rightSlidingDialogContainer.currentActionBarView : super.getActionBar();
//    }

    public void showSelectStatusDialog() {
        if (selectAnimatedEmojiDialog != null || SharedConfig.appLocked) {
            return;
        }
        final SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[] popup = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow[1];
        TLRPC.User user = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
        int xoff = 0, yoff = 0;
        boolean hasEmoji = false;
        SimpleTextView actionBarTitle = actionBar.getTitleTextView();
        if (actionBarTitle != null && actionBarTitle.getRightDrawable() != null) {
            statusDrawable.play();
            hasEmoji = statusDrawable.getDrawable() instanceof AnimatedEmojiDrawable;
            AndroidUtilities.rectTmp2.set(actionBarTitle.getRightDrawable().getBounds());
            AndroidUtilities.rectTmp2.offset((int) actionBarTitle.getX(), (int) actionBarTitle.getY());
            yoff = -(actionBar.getHeight() - AndroidUtilities.rectTmp2.centerY()) - AndroidUtilities.dp(16);
            xoff = AndroidUtilities.rectTmp2.centerX() - AndroidUtilities.dp(16);
            if (animatedStatusView != null) {
                animatedStatusView.translate(AndroidUtilities.rectTmp2.centerX(), AndroidUtilities.rectTmp2.centerY());
            }
        }
        SelectAnimatedEmojiDialog popupLayout = new SelectAnimatedEmojiDialog(this, getContext(), true, xoff, SelectAnimatedEmojiDialog.TYPE_EMOJI_STATUS, getResourceProvider()) {
            @Override
            protected void onEmojiSelected(View emojiView, Long documentId, TLRPC.Document document, Integer until) {
                TLRPC.TL_account_updateEmojiStatus req = new TLRPC.TL_account_updateEmojiStatus();
                if (documentId == null) {
                    req.emoji_status = new TLRPC.TL_emojiStatusEmpty();
                } else if (until != null) {
                    req.emoji_status = new TLRPC.TL_emojiStatusUntil();
                    ((TLRPC.TL_emojiStatusUntil) req.emoji_status).document_id = documentId;
                    ((TLRPC.TL_emojiStatusUntil) req.emoji_status).until = until;
                } else {
                    req.emoji_status = new TLRPC.TL_emojiStatus();
                    ((TLRPC.TL_emojiStatus) req.emoji_status).document_id = documentId;
                }
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                if (user != null) {
                    user.emoji_status = req.emoji_status;
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.userEmojiStatusUpdated, user);
                    getMessagesController().updateEmojiStatusUntilUpdate(user.id, user.emoji_status);
                }
                if (documentId != null) {
                    animatedStatusView.animateChange(ReactionsLayoutInBubble.VisibleReaction.fromCustomEmoji(documentId));
                }
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> {
                    if (!(res instanceof TLRPC.TL_boolTrue)) {
                        // TODO: reject
                    }
                });
                if (popup[0] != null) {
                    selectAnimatedEmojiDialog = null;
                    popup[0].dismiss();
                }
            }
        };
        if (user != null && user.emoji_status instanceof TLRPC.TL_emojiStatusUntil && ((TLRPC.TL_emojiStatusUntil) user.emoji_status).until > (int) (System.currentTimeMillis() / 1000)) {
            popupLayout.setExpireDateHint(((TLRPC.TL_emojiStatusUntil) user.emoji_status).until);
        }
        popupLayout.setSelected(statusDrawable.getDrawable() instanceof AnimatedEmojiDrawable ? ((AnimatedEmojiDrawable) statusDrawable.getDrawable()).getDocumentId() : null);
        popupLayout.setSaveState(1);
        popupLayout.setScrimDrawable(statusDrawable, actionBarTitle);
        popup[0] = selectAnimatedEmojiDialog = new SelectAnimatedEmojiDialog.SelectAnimatedEmojiDialogWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
            @Override
            public void dismiss() {
                super.dismiss();
                selectAnimatedEmojiDialog = null;
            }
        };
        popup[0].showAsDropDown(actionBar, AndroidUtilities.dp(16), yoff, Gravity.TOP);
        popup[0].dimBehind();
    }

    private int commentViewPreviousTop = -1;
    private ValueAnimator keyboardAnimator;
    private boolean commentViewIgnoreTopUpdate = false;

    private void updateCommentView() {
        if (!commentViewAnimated || commentView == null) {
            return;
        }
        int top = commentView.getTop();
        if (commentViewPreviousTop > 0 && Math.abs(top - commentViewPreviousTop) > AndroidUtilities.dp(20) && !commentView.isPopupShowing()) {
            if (commentViewIgnoreTopUpdate) {
                commentViewIgnoreTopUpdate = false;
                commentViewPreviousTop = top;
                return;
            }
            if (keyboardAnimator != null) {
                keyboardAnimator.cancel();
            }
            keyboardAnimator = ValueAnimator.ofFloat(commentViewPreviousTop - top, 0);
            keyboardAnimator.addUpdateListener(a -> {
                commentView.setTranslationY((float) a.getAnimatedValue());
            });
            keyboardAnimator.setDuration(AdjustPanLayoutHelper.keyboardDuration);
            keyboardAnimator.setInterpolator(AdjustPanLayoutHelper.keyboardInterpolator);
            keyboardAnimator.start();
        }
        commentViewPreviousTop = top;
    }

    private void updateAppUpdateViews(boolean animated) {
        // NekoX: Remove UPDATE NOW Bottom View in DialogsActivity
    }

    private void updateContextViewPosition() {
        float filtersTabsHeight = 0;
        if (filterTabsView != null && filterTabsView.getVisibility() != View.GONE) {
            filtersTabsHeight = filterTabsView.getMeasuredHeight();
        }
        float searchTabsHeight = 0;
        if (searchTabsView != null && searchTabsView.getVisibility() != View.GONE) {
            searchTabsHeight = searchTabsView.getMeasuredHeight();
        }
        if (fragmentContextView != null) {
            float from = 0;
            if (fragmentLocationContextView != null && fragmentLocationContextView.getVisibility() == View.VISIBLE) {
                from += AndroidUtilities.dp(36);
            }
            fragmentContextView.setTranslationY(from + fragmentContextView.getTopPadding() + actionBar.getTranslationY() + filtersTabsHeight * (1f - searchAnimationProgress) + searchTabsHeight * searchAnimationProgress + tabsYOffset);
        }
        if (fragmentLocationContextView != null) {
            float from = 0;
            if (fragmentContextView != null && fragmentContextView.getVisibility() == View.VISIBLE) {
                from += AndroidUtilities.dp(fragmentContextView.getStyleHeight()) + fragmentContextView.getTopPadding();
            }
            fragmentLocationContextView.setTranslationY(from + fragmentLocationContextView.getTopPadding() + actionBar.getTranslationY() + filtersTabsHeight * (1f - searchAnimationProgress) + searchTabsHeight * searchAnimationProgress + tabsYOffset);
        }
    }

    private void updateFiltersView(boolean showMediaFilters, ArrayList<Object> users, ArrayList<FiltersView.DateData> dates, boolean archive, boolean animated) {
        if (!searchIsShowed || onlySelect && !(initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool())) {
            return;
        }
        boolean hasMediaFilter = false;
        boolean hasUserFilter = false;
        boolean hasDateFilter = false;
        boolean hasArchiveFilter = false;

        ArrayList<FiltersView.MediaFilterData> currentSearchFilters = searchViewPager.getCurrentSearchFilters();
        for (int i = 0; i < currentSearchFilters.size(); i++) {
            if (currentSearchFilters.get(i).isMedia()) {
                hasMediaFilter = true;
            } else if (currentSearchFilters.get(i).filterType == FiltersView.FILTER_TYPE_CHAT) {
                hasUserFilter = true;
            } else if (currentSearchFilters.get(i).filterType == FiltersView.FILTER_TYPE_DATE) {
                hasDateFilter = true;
            } else if (currentSearchFilters.get(i).filterType == FiltersView.FILTER_TYPE_ARCHIVE) {
                hasArchiveFilter = true;
            }
        }

        if (hasArchiveFilter) {
            archive = false;
        }

        boolean visible = false;
        boolean hasUsersOrDates = (users != null && !users.isEmpty()) || (dates != null && !dates.isEmpty() || archive);
        if (!hasMediaFilter && !hasUsersOrDates && showMediaFilters) {

        } else if (hasUsersOrDates) {
            ArrayList<Object> finalUsers = (users != null && !users.isEmpty() && !hasUserFilter) ? users : null;
            ArrayList<FiltersView.DateData> finalDates = (dates != null && !dates.isEmpty() && !hasDateFilter) ? dates : null;
            if (finalUsers != null || finalDates != null || archive) {
                visible = true;
                filtersView.setUsersAndDates(finalUsers, finalDates, archive);
            }
        }

        if (!visible) {
            filtersView.setUsersAndDates(null, null, false);
        }
        if (!animated) {
            filtersView.getAdapter().notifyDataSetChanged();
        }
        if (searchTabsView != null) {
            searchTabsView.hide(visible, true);
        }
        filtersView.setEnabled(visible);
        filtersView.setVisibility(View.VISIBLE);
    }

    private void addSearchFilter(FiltersView.MediaFilterData filter) {
        if (!searchIsShowed) {
            return;
        }
        ArrayList<FiltersView.MediaFilterData> currentSearchFilters = searchViewPager.getCurrentSearchFilters();
        if (!currentSearchFilters.isEmpty()) {
            for (int i = 0; i < currentSearchFilters.size(); i++) {
                if (filter.isSameType(currentSearchFilters.get(i))) {
                    return;
                }
            }
        }
        currentSearchFilters.add(filter);
        actionBar.setSearchFilter(filter);
        actionBar.setSearchFieldText("");
        updateFiltersView(true, null, null, false, true);
    }

    public void updateSpeedItem(boolean visibleByPosition) {
        if (speedItem == null) {
            return;
        }

        boolean visibleByDownload = false;
        for (MessageObject obj : getDownloadController().downloadingFiles) {
            if (obj.getDocument() != null && obj.getDocument().size >= 150 * 1024 * 1024) {
                visibleByDownload = true;
                break;
            }
        }
        for (MessageObject obj : getDownloadController().recentDownloadingFiles) {
            if (obj.getDocument() != null && obj.getDocument().size >= 150 * 1024 * 1024) {
                visibleByDownload = true;
                break;
            }
        }
        boolean visible = !getUserConfig().isPremium() && !getMessagesController().premiumLocked && visibleByDownload && visibleByPosition && DISPLAY_SPEEDOMETER_IN_DOWNLOADS_SEARCH;

        boolean wasVisible = speedItem.getTag() != null;
        if (visible != wasVisible) {
            speedItem.setTag(visible ? true : null);
            speedItem.setClickable(visible);

            if (speedAnimator != null) {
                speedAnimator.cancel();
            }

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(180);
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(speedItem, View.ALPHA, visible ? 1f : 0f),
                    ObjectAnimator.ofFloat(speedItem, View.SCALE_X, visible ? 1f : 0.5f),
                    ObjectAnimator.ofFloat(speedItem, View.SCALE_Y, visible ? 1f : 0.5f)
            );
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) speedItem.getIconView().getDrawable();
                        if (visible) {
                            drawable.start();

                            if (SharedConfig.getDevicePerformanceClass() != SharedConfig.PERFORMANCE_CLASS_LOW) {
                                TLRPC.TL_help_premiumPromo premiumPromo = MediaDataController.getInstance(currentAccount).getPremiumPromo();
                                String typeString = PremiumPreviewFragment.featureTypeToServerString(PremiumPreviewFragment.PREMIUM_FEATURE_DOWNLOAD_SPEED);
                                if (premiumPromo != null) {
                                    int index = -1;
                                    for (int i = 0; i < premiumPromo.video_sections.size(); i++) {
                                        if (premiumPromo.video_sections.get(i).equals(typeString)) {
                                            index = i;
                                            break;
                                        }
                                    }
                                    if (index != -1) {
                                        FileLoader.getInstance(currentAccount).loadFile(premiumPromo.videos.get(index), premiumPromo, FileLoader.PRIORITY_HIGH, 0);
                                    }
                                }
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                drawable.reset();
                            } else {
                                drawable.setVisible(false, true);
                            }
                        }
                    }
                }
            });
            animatorSet.start();

            speedAnimator = animatorSet;
        }
    }

    private void createActionMode(String tag) {
        if (actionBar.actionModeIsExist(tag)) {
            return;
        }
        final ActionBarMenu actionMode = actionBar.createActionMode(false, tag);
        actionMode.setBackgroundColor(Color.TRANSPARENT);
        actionMode.drawBlur = false;

        selectedDialogsCountTextView = new NumberTextView(actionMode.getContext());
        selectedDialogsCountTextView.setTextSize(18);
        selectedDialogsCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        selectedDialogsCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        actionMode.addView(selectedDialogsCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
        selectedDialogsCountTextView.setOnTouchListener((v, event) -> true);

        pinItem = actionMode.addItemWithWidth(pin, R.drawable.msg_pin, AndroidUtilities.dp(54));
        muteItem = actionMode.addItemWithWidth(mute, R.drawable.msg_mute, AndroidUtilities.dp(54));
        archive2Item = actionMode.addItemWithWidth(archive2, R.drawable.msg_archive, AndroidUtilities.dp(54));
        deleteItem = actionMode.addItemWithWidth(delete, R.drawable.msg_delete, AndroidUtilities.dp(54), LocaleController.getString("Delete", R.string.Delete));
        ActionBarMenuItem otherItem = actionMode.addItemWithWidth(0, R.drawable.ic_ab_other, AndroidUtilities.dp(54), LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        archiveItem = otherItem.addSubItem(archive, R.drawable.msg_archive, LocaleController.getString("Archive", R.string.Archive));
        pin2Item = otherItem.addSubItem(pin2, R.drawable.msg_pin, LocaleController.getString("DialogPin", R.string.DialogPin));
        addToFolderItem = otherItem.addSubItem(add_to_folder, R.drawable.msg_addfolder, LocaleController.getString("FilterAddTo", R.string.FilterAddTo));
        removeFromFolderItem = otherItem.addSubItem(remove_from_folder, R.drawable.msg_removefolder, LocaleController.getString("FilterRemoveFrom", R.string.FilterRemoveFrom));
        readItem = otherItem.addSubItem(read, R.drawable.msg_markread, LocaleController.getString("MarkAsRead", R.string.MarkAsRead));
        clearItem = otherItem.addSubItem(clear, R.drawable.msg_clear, LocaleController.getString("ClearHistory", R.string.ClearHistory));
        blockItem = otherItem.addSubItem(block, R.drawable.msg_block, LocaleController.getString("BlockUser", R.string.BlockUser));

        muteItem.setOnLongClickListener(e -> {
            performSelectedDialogsAction(selectedDialogs, mute, true, true);
            return true;
        });

        actionModeViews.add(pinItem);
        actionModeViews.add(archive2Item);
        actionModeViews.add(muteItem);
        actionModeViews.add(deleteItem);
        actionModeViews.add(otherItem);

        if (tag == null) {
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if ((id == SearchViewPager.forwardItemId || id == SearchViewPager.gotoItemId || id == SearchViewPager.deleteItemId || id == SearchViewPager.speedItemId) && searchViewPager != null) {
                        searchViewPager.onActionBarItemClick(id);
                        return;
                    }
                    if (id == -1) {
                        if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.hasFragment()) {
                            if (actionBar.isActionModeShowed()) {
                                if (searchViewPager != null && searchViewPager.getVisibility() == View.VISIBLE && searchViewPager.actionModeShowing()) {
                                    searchViewPager.hideActionMode();
                                } else {
                                    hideActionMode(true);
                                }
                            } else {
                                rightSlidingDialogContainer.finishPreview();
                                searchViewPager.updateTabs();
                                return;
                            }
                        } else if (filterTabsView != null && filterTabsView.isEditing()) {
                            filterTabsView.setIsEditing(false);
                            showDoneItem(false);
                        } else if (actionBar.isActionModeShowed()) {
                            if (searchViewPager != null && searchViewPager.getVisibility() == View.VISIBLE && searchViewPager.actionModeShowing()) {
                                searchViewPager.hideActionMode();
                            } else {
                                hideActionMode(true);
                            }
                        } else if (onlySelect || folderId != 0) {
                            finishFragment();
                        } else if (parentLayout != null) {
                            parentLayout.getDrawerLayoutContainer().openDrawer(false);
                        }
                    } else if (id == 1) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        SharedConfig.appLocked = true;
                        SharedConfig.saveConfig();
                        int[] position = new int[2];
                        passcodeItem.getLocationInWindow(position);
                        ((LaunchActivity) getParentActivity()).showPasscodeActivity(false, true, position[0] + passcodeItem.getMeasuredWidth() / 2, position[1] + passcodeItem.getMeasuredHeight() / 2, () -> passcodeItem.setAlpha(1.0f), () -> passcodeItem.setAlpha(0.0f));
                        updatePasscodeButton();
                    } else if (id == 2) {
                        presentFragment(new ProxyListActivity());
                    } else if (id == 3) {
                        showSearch(true, true, true);
                        actionBar.openSearchField(true);
                    } else if (id == nekox_scanqr) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (getParentActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 22);
                                return;
                            }
                        }
                    CameraScanActivity.showAsSheet(DialogsActivity.this, false, CameraScanActivity.TYPE_QR, new CameraScanActivity.CameraScanActivityDelegate() {

                        @Override
                        public void didFindQr(String text) {
                            ProxyUtil.showLinkAlert(getParentActivity(), text);
                        }

                        @Override
                        public boolean processQr(String text, Runnable onLoadEnd) {
                            onLoadEnd.run();
                            return false;
                        }
                    });
                } else if (id >= 10 && id < 10 + accounts) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        DialogsActivityDelegate oldDelegate = delegate;
                        LaunchActivity launchActivity = (LaunchActivity) getParentActivity();
                        launchActivity.switchToAccount(id - 10, true);

                        DialogsActivity dialogsActivity = new DialogsActivity(arguments);
                        dialogsActivity.setDelegate(oldDelegate);
                        launchActivity.presentFragment(dialogsActivity, false, true);
                    } else if (id == add_to_folder) {
                        FiltersListBottomSheet sheet = new FiltersListBottomSheet(DialogsActivity.this, selectedDialogs);
                        sheet.setDelegate(filter -> {
                            ArrayList<Long> alwaysShow = FiltersListBottomSheet.getDialogsCount(DialogsActivity.this, filter, selectedDialogs, true, false);
                            int currentCount;
                            if (filter != null) {
                                currentCount = filter.alwaysShow.size();
                            } else {
                                currentCount = 0;
                            }
                            int totalCount = currentCount + alwaysShow.size();
                            if ((totalCount > getMessagesController().dialogFiltersChatsLimitDefault && !getUserConfig().isPremium()) || totalCount > getMessagesController().dialogFiltersChatsLimitPremium) {
                                showDialog(new LimitReachedBottomSheet(DialogsActivity.this, fragmentView.getContext(), LimitReachedBottomSheet.TYPE_CHATS_IN_FOLDER, currentAccount));
                                return;
                            }
                            if (filter != null) {
                                if (!alwaysShow.isEmpty()) {
                                    for (int a = 0; a < alwaysShow.size(); a++) {
                                        filter.neverShow.remove(alwaysShow.get(a));
                                    }
                                    filter.alwaysShow.addAll(alwaysShow);
                                    FilterCreateActivity.saveFilterToServer(filter, filter.flags, filter.emoticon, filter.name, filter.alwaysShow, filter.neverShow, filter.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, null);
                                }
                                long did;
                                if (alwaysShow.size() == 1) {
                                    did = alwaysShow.get(0);
                                } else {
                                    did = 0;
                                }
                                getUndoView().showWithAction(did, UndoView.ACTION_ADDED_TO_FOLDER, alwaysShow.size(), filter, null, null);
                            } else {
                                presentFragment(new FilterCreateActivity(null, alwaysShow));
                            }
                            hideActionMode(true);
                        });
                        showDialog(sheet);
                    } else if (id == remove_from_folder) {
                        MessagesController.DialogFilter filter = getMessagesController().dialogFilters.get(viewPages[0].selectedType);
                        ArrayList<Long> neverShow = FiltersListBottomSheet.getDialogsCount(DialogsActivity.this, filter, selectedDialogs, false, false);

                        int currentCount;
                        if (filter != null) {
                            currentCount = filter.neverShow.size();
                        } else {
                            currentCount = 0;
                        }
                        if (currentCount + neverShow.size() > 100) {
                            showDialog(AlertsCreator.createSimpleAlert(getParentActivity(), LocaleController.getString("FilterAddToAlertFullTitle", R.string.FilterAddToAlertFullTitle), LocaleController.getString("FilterAddToAlertFullText", R.string.FilterAddToAlertFullText)).create());
                            return;
                        }
                        if (!neverShow.isEmpty()) {
                            filter.neverShow.addAll(neverShow);
                            for (int a = 0; a < neverShow.size(); a++) {
                                Long did = neverShow.get(a);
                                filter.alwaysShow.remove(did);
                                filter.pinnedDialogs.delete(did);
                            }
                            FilterCreateActivity.saveFilterToServer(filter, filter.flags, filter.emoticon, filter.name, filter.alwaysShow, filter.neverShow, filter.pinnedDialogs, false, false, true, false, false, DialogsActivity.this, null);
                        }
                        long did;
                        if (neverShow.size() == 1) {
                            did = neverShow.get(0);
                        } else {
                            did = 0;
                        }
                        getUndoView().showWithAction(did, UndoView.ACTION_REMOVED_FROM_FOLDER, neverShow.size(), filter, null, null);
                        hideActionMode(false);
                    } else if (id == pin || id == read || id == delete || id == clear || id == mute || id == archive || id == block || id == archive2 || id == pin2) {
                        performSelectedDialogsAction(selectedDialogs, id, true, false);
                    }
                }
            });
        }
    }

    private void switchToCurrentSelectedMode(boolean animated) {
        for (int a = 0; a < viewPages.length; a++) {
            viewPages[a].listView.stopScroll();
        }
        int a = animated ? 1 : 0;
        RecyclerView.Adapter currentAdapter = viewPages[a].listView.getAdapter();
        if (viewPages[a].selectedType < 0 || viewPages[a].selectedType >= getMessagesController().dialogFilters.size()) {
            return;
        }
        MessagesController.DialogFilter filter = getMessagesController().dialogFilters.get(viewPages[a].selectedType);
        if (filter.isDefault()) {
            viewPages[a].dialogsType = initialDialogsType;
            viewPages[a].listView.updatePullState();
        } else {
            if (viewPages[a == 0 ? 1 : 0].dialogsType == 7) {
                viewPages[a].dialogsType = 8;
            } else {
                viewPages[a].dialogsType = 7;
            }
            viewPages[a].listView.setScrollEnabled(true);
            getMessagesController().selectDialogFilter(filter, viewPages[a].dialogsType == 8 ? 1 : 0);
        }
        viewPages[1].isLocked = filter.locked;

        viewPages[a].dialogsAdapter.setDialogsType(viewPages[a].dialogsType);
        viewPages[a].layoutManager.scrollToPositionWithOffset(viewPages[a].dialogsType == 0 && hasHiddenArchive() && viewPages[a].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN ? 1 : 0, (int) actionBar.getTranslationY());
        checkListLoad(viewPages[a]);
    }

    private boolean scrollBarVisible = true;

    private void showScrollbars(boolean show) {
        if (viewPages == null || scrollBarVisible == show) {
            return;
        }
        scrollBarVisible = show;
        for (int a = 0; a < viewPages.length; a++) {
            if (show) {
                viewPages[a].listView.setScrollbarFadingEnabled(false);
            }
            viewPages[a].listView.setVerticalScrollBarEnabled(show);
            if (show) {
                viewPages[a].listView.setScrollbarFadingEnabled(true);
            }
        }
    }

    private void updateFilterTabs(boolean force, boolean animated) {
        if (filterTabsView == null || inPreviewMode || searchIsShowed) {
            return;
        }
        if (scrimPopupWindow != null) {
            scrimPopupWindow.dismiss();
            scrimPopupWindow = null;
        }
        ArrayList<MessagesController.DialogFilter> filters = getMessagesController().dialogFilters;
        SharedPreferences preferences = MessagesController.getMainSettings(currentAccount);
        if (filters.size() > 1) {
            if (force || filterTabsView.getVisibility() != View.VISIBLE) {
                boolean animatedUpdateItems = animated;
                if (filterTabsView.getVisibility() != View.VISIBLE) {
                    animatedUpdateItems = false;
                }
                canShowFilterTabsView = true;
                updateFilterTabsVisibility(animated);
                int id = filterTabsView.getCurrentTabId();
                int stableId = filterTabsView.getCurrentTabStableId();
                boolean selectWithStableId = false;
                if (id != filterTabsView.getDefaultTabId() && id >= filters.size()) {
                    filterTabsView.resetTabId();
                    selectWithStableId = true;
                }
                filterTabsView.removeTabs();
                for (int a = 0, N = filters.size(); a < N; a++) {
                    MessagesController.DialogFilter dialogFilter = filters.get(a);
                    if (filters.get(a).isDefault()) {
                        if (filterTabsView.showAllChatsTab)
                            filterTabsView.addTab(a, 0, LocaleController.getString("FilterAllChats", R.string.FilterAllChats), "\uD83D\uDCAC", true, filters.get(a).locked);
                    } else {
                        filterTabsView.addTab(a, filters.get(a).localId, filters.get(a).name, filters.get(a).emoticon == null ? "\uD83D\uDCC1" : filters.get(a).emoticon, false,  filters.get(a).locked);
                    }
                }
                boolean updateCurrentTab = NekoConfig.hideAllTab.Bool();
                if (NekoConfig.hideAllTab.Bool() && stableId <= 0) {
                    id = filterTabsView.getFirstTabId();
                    updateCurrentTab = true;
                    viewPages[0].selectedType = id;
                    filterTabsView.selectTabWithStableId(filterTabsView.getStableId(0));
                } else if (stableId >= 0) {
                    if (filterTabsView.getStableId(viewPages[0].selectedType) != stableId) {
                        updateCurrentTab = true;
                        viewPages[0].selectedType = id;
                    }
                    if (selectWithStableId) {
                        filterTabsView.selectTabWithStableId(stableId);
                    }
                }
                for (ViewPage viewPage : viewPages) {
                    if (viewPage.selectedType >= filters.size()) {
                        viewPage.selectedType = filters.size() - 1;
                    }
                    viewPage.listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
                }
                filterTabsView.finishAddingTabs(animatedUpdateItems);
                if (updateCurrentTab) {
                    switchToCurrentSelectedMode(false);
                }
                isFirstTab = id == filterTabsView.getFirstTabId();
                updateDrawerSwipeEnabled();
                if (filterTabsView.isLocked(filterTabsView.getCurrentTabId())) {
                    filterTabsView.selectFirstTab();
                }
            }
        } else {
            if (filterTabsView.getVisibility() != View.GONE) {
                filterTabsView.setIsEditing(false);
                showDoneItem(false);

                maybeStartTracking = false;
                if (startedTracking) {
                    startedTracking = false;
                    viewPages[0].setTranslationX(0);
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
                }
                if (viewPages[0].selectedType != filterTabsView.getDefaultTabId()) {
                    viewPages[0].selectedType = filterTabsView.getDefaultTabId();
                    viewPages[0].dialogsAdapter.setDialogsType(initialDialogsType);
                    viewPages[0].dialogsType = initialDialogsType;
                    viewPages[0].dialogsAdapter.notifyDataSetChanged();
                }
                viewPages[1].setVisibility(View.GONE);
                viewPages[1].selectedType = 0;
                viewPages[1].dialogsAdapter.setDialogsType(initialDialogsType);
                viewPages[1].dialogsType = initialDialogsType;
                viewPages[1].dialogsAdapter.notifyDataSetChanged();
                canShowFilterTabsView = false;
                updateFilterTabsVisibility(animated);
                for (int a = 0; a < viewPages.length; a++) {
                    if (viewPages[a].dialogsType == 0 && viewPages[a].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN && hasHiddenArchive()) {
                        int p = viewPages[a].layoutManager.findFirstVisibleItemPosition();
                        if (p == 0 || p == 1) {
                            viewPages[a].layoutManager.scrollToPositionWithOffset(1, (int) actionBar.getTranslationY());
                        }
                    }
                    viewPages[a].listView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_DEFAULT);
                    viewPages[a].listView.requestLayout();
                    viewPages[a].requestLayout();
                }
            }
            updateDrawerSwipeEnabled();
        }
        updateCounters(false);
    }

    private void updateDrawerSwipeEnabled() {
        if (parentLayout != null && parentLayout.getDrawerLayoutContainer() != null) {
            parentLayout.getDrawerLayoutContainer().setAllowOpenDrawerBySwipe(((isFirstTab && SharedConfig.getChatSwipeAction(currentAccount) == SwipeGestureSettingsView.SWIPE_GESTURE_FOLDERS) || SharedConfig.getChatSwipeAction(currentAccount) != SwipeGestureSettingsView.SWIPE_GESTURE_FOLDERS) && !searchIsShowed && (rightSlidingDialogContainer == null || !rightSlidingDialogContainer.hasFragment()));
        }
    }

    @Override
    protected void onPanTranslationUpdate(float y) {
        if (viewPages == null) {
            return;
        }
        if (commentView != null && commentView.isPopupShowing()) {
            fragmentView.setTranslationY(y);
            for (int a = 0; a < viewPages.length; a++) {
                viewPages[a].setTranslationY(0);
            }
            if (!onlySelect) {
                actionBar.setTranslationY(0);
                if (topBulletin != null) {
                    topBulletin.updatePosition();
                }
            }
            searchViewPager.setTranslationY(0);
        } else {
            for (int a = 0; a < viewPages.length; a++) {
                viewPages[a].setTranslationY(y);
            }
            if (!onlySelect) {
                actionBar.setTranslationY(y);
                if (topBulletin != null) {
                    topBulletin.updatePosition();
                }
            }
            searchViewPager.setTranslationY(y);
        }
    }

    @Override
    public void finishFragment() {
        super.finishFragment();
        if (scrimPopupWindow != null) {
            scrimPopupWindow.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rightSlidingDialogContainer != null) {
            rightSlidingDialogContainer.onResume();
        }
        if (!parentLayout.isInPreviewMode() && blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            blurredView.setVisibility(View.GONE);
            blurredView.setBackground(null);
        }
        updateDrawerSwipeEnabled();
        if (viewPages != null) {
            for (int a = 0; a < viewPages.length; a++) {
                viewPages[a].dialogsAdapter.notifyDataSetChanged();
            }
        }
        if (commentView != null) {
            commentView.onResume();
        }
        if (!onlySelect && folderId == 0) {
            getMediaDataController().checkStickers(MediaDataController.TYPE_EMOJI);
        }
        if (searchViewPager != null) {
            searchViewPager.onResume();
        }
        final boolean tosAccepted;
        if (!afterSignup) {
            tosAccepted = getUserConfig().unacceptedTermsOfService == null;
        } else {
            tosAccepted = true;
        }
        if (tosAccepted && checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                boolean hasNotContactsPermission = activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
                boolean hasNotStoragePermission = (Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
                AndroidUtilities.runOnUIThread(() -> {
                    afterSignup = false;
                    if (hasNotContactsPermission || hasNotStoragePermission) {
                        askingForPermissions = true;
                        if (hasNotContactsPermission && askAboutContacts && getUserConfig().syncContacts && activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                            AlertDialog.Builder builder = AlertsCreator.createContactsPermissionDialog(activity, param -> {
                                askAboutContacts = param != 0;
                                MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts).apply();
                                askForPermissons(false);
                            });
                            showDialog(permissionDialog = builder.create());
                        } else if (hasNotStoragePermission && activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            if (activity instanceof BasePermissionsActivity) {
                                BasePermissionsActivity basePermissionsActivity = (BasePermissionsActivity) activity;
                                showDialog(permissionDialog = basePermissionsActivity.createPermissionErrorAlert(R.raw.permission_request_folder, LocaleController.getString(R.string.PermissionStorageWithHint)));
                            }
                        } else {
                            askForPermissons(true);
                        }
                    }
                }, afterSignup && hasNotContactsPermission ? 4000 : 0);
            }
        } else if (!onlySelect && XiaomiUtilities.isMIUI() && Build.VERSION.SDK_INT >= 19 && !XiaomiUtilities.isCustomPermissionGranted(XiaomiUtilities.OP_SHOW_WHEN_LOCKED)) {
            if (getParentActivity() == null) {
                return;
            }
            if (MessagesController.getGlobalNotificationsSettings().getBoolean("askedAboutMiuiLockscreen", false)) {
                return;
            }
            showDialog(new AlertDialog.Builder(getParentActivity())
                    .setTopAnimation(R.raw.permission_request_apk, AlertsCreator.PERMISSIONS_REQUEST_TOP_ICON_SIZE, false, Theme.getColor(Theme.key_dialogTopBackground))
                    .setMessage(LocaleController.getString("PermissionXiaomiLockscreen", R.string.PermissionXiaomiLockscreen))
                    .setPositiveButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), (dialog, which) -> {
                        Intent intent = XiaomiUtilities.getPermissionManagerIntent();
                        if (intent != null) {
                            try {
                                getParentActivity().startActivity(intent);
                            } catch (Exception x) {
                                try {
                                    intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                                    getParentActivity().startActivity(intent);
                                } catch (Exception xx) {
                                    FileLog.e(xx);
                                }
                            }
                        }
                    })
                    .setNegativeButton(LocaleController.getString("ContactsPermissionAlertNotNow", R.string.ContactsPermissionAlertNotNow), (dialog, which) -> MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askedAboutMiuiLockscreen", true).commit())
                    .create());
        }
        showFiltersHint();
        if (viewPages != null) {
            for (int a = 0; a < viewPages.length; a++) {
                if (viewPages[a].dialogsType == 0 && viewPages[a].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN && viewPages[a].layoutManager.findFirstVisibleItemPosition() == 0 && hasHiddenArchive()) {
                    viewPages[a].layoutManager.scrollToPositionWithOffset(1, (int) actionBar.getTranslationY());
                }
                if (a == 0) {
                    viewPages[a].dialogsAdapter.resume();
                } else {
                    viewPages[a].dialogsAdapter.pause();
                }
            }
        }
        showNextSupportedSuggestion();
        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public void onBottomOffsetChange(float offset) {
                if (undoView[0] != null && undoView[0].getVisibility() == View.VISIBLE) {
                    return;
                }
                additionalFloatingTranslation = offset;
                if (additionalFloatingTranslation < 0) {
                    additionalFloatingTranslation = 0;
                }
                if (!floatingHidden) {
                    updateFloatingButtonOffset();
                }
            }

            @Override
            public void onShow(Bulletin bulletin) {
                if (undoView[0] != null && undoView[0].getVisibility() == View.VISIBLE) {
                    undoView[0].hide(true, 2);
                }
            }

            @Override
            public int getTopOffset(int tag) {
                return (actionBar != null ? actionBar.getMeasuredHeight() + (int) actionBar.getTranslationY() : 0) + (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE ? filterTabsView.getMeasuredHeight() : 0) + (fragmentContextView != null && fragmentContextView.isCallTypeVisible() ? AndroidUtilities.dp(fragmentContextView.getStyleHeight()) : 0);
            }
        });
        if (searchIsShowed) {
            AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        }
        updateVisibleRows(0, false);
        updateProxyButton(false, true);
        checkSuggestClearDatabase();
    }

    @Override
    public boolean presentFragment(BaseFragment fragment) {
        boolean b = super.presentFragment(fragment);
        if (b) {
            if (viewPages != null) {
                for (int a = 0; a < viewPages.length; a++) {
                    viewPages[a].dialogsAdapter.pause();
                }
            }
        }
        return b;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (rightSlidingDialogContainer != null) {
            rightSlidingDialogContainer.onPause();
        }
        if (scrimPopupWindow != null) {
            scrimPopupWindow.dismiss();
        }
        if (commentView != null) {
            commentView.onPause();
        }
        if (undoView[0] != null) {
            undoView[0].hide(true, 0);
        }
        Bulletin.removeDelegate(this);

        if (viewPages != null) {
            for (int a = 0; a < viewPages.length; a++) {
                viewPages[a].dialogsAdapter.pause();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (rightSlidingDialogContainer.hasFragment()) {
            if (rightSlidingDialogContainer.getFragment().onBackPressed()) {
                rightSlidingDialogContainer.finishPreview();
                searchViewPager.updateTabs();
            }
            return false;
        } else if (scrimPopupWindow != null) {
            scrimPopupWindow.dismiss();
            return false;
        } else if (filterTabsView != null && filterTabsView.isEditing()) {
            filterTabsView.setIsEditing(false);
            showDoneItem(false);
            return false;
        } else if (actionBar != null && actionBar.isActionModeShowed()) {
            if (searchViewPager.getVisibility() == View.VISIBLE) {
                searchViewPager.hideActionMode();
                hideActionMode(true);
            } else {
                hideActionMode(true);
            }
            return false;
        } else if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && !tabsAnimationInProgress && !filterTabsView.isAnimatingIndicator()
                && !startedTracking && !filterTabsView.isFirstTabSelected()) {
            if (!NekoConfig.hideAllTab.Bool()){
                filterTabsView.selectFirstTab();
                return false;
            }
        } else if (commentView != null && commentView.isPopupShowing()) {
            commentView.hidePopup(true);
            return false;
        }
        return super.onBackPressed();
    }

    @Override
    public void onBecomeFullyHidden() {
        if (closeSearchFieldOnHide) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            if (searchObject != null) {
                searchViewPager.dialogsSearchAdapter.putRecentSearch(searchDialogId, searchObject);
                searchObject = null;
            }
            closeSearchFieldOnHide = false;
        }
        if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && filterTabsViewIsVisible) {
            int scrollY = (int) -actionBar.getTranslationY();
            int actionBarHeight = ActionBar.getCurrentActionBarHeight();
            if (scrollY != 0 && scrollY != actionBarHeight) {
                if (scrollY < actionBarHeight / 2) {
                    setScrollY(0);
                } else if (viewPages[0].listView.canScrollVertically(1)) {
                    setScrollY(-actionBarHeight);
                }
            }
        }
        if (undoView[0] != null) {
            undoView[0].hide(true, 0);
        }
    }

    @Override
    public void onBecomeFullyVisible() {
        super.onBecomeFullyVisible();
        if (SharedConfig.useLNavigation) {
            if (viewPages != null) {
                for (int a = 0; a < viewPages.length; a++) {
                    if (viewPages[a].dialogsType == 0 && viewPages[a].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN && viewPages[a].layoutManager.findFirstVisibleItemPosition() == 0 && hasHiddenArchive()) {
                        viewPages[a].layoutManager.scrollToPositionWithOffset(1, (int) actionBar.getTranslationY());
                    }
                    if (a == 0) {
                        viewPages[a].dialogsAdapter.resume();
                    } else {
                        viewPages[a].dialogsAdapter.pause();
                    }
                }
            }
            if (searchIsShowed) {
                AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
            }
            updateVisibleRows(0, false);
            updateProxyButton(false, true);
            checkSuggestClearDatabase();
        }
    }

    @Override
    public void setInPreviewMode(boolean value) {
        super.setInPreviewMode(value);
        if (!value && avatarContainer != null) {
            actionBar.setBackground(null);
            ((ViewGroup.MarginLayoutParams) actionBar.getLayoutParams()).topMargin = 0;
            actionBar.removeView(avatarContainer);
            avatarContainer = null;
            updateFilterTabs(false, false);
            floatingButton.setVisibility(View.VISIBLE);
            final ContentView contentView = (ContentView) fragmentView;
            if (fragmentContextView != null) {
                contentView.addView(fragmentContextView);
            }
            if (fragmentLocationContextView != null) {
                contentView.addView(fragmentLocationContextView);
            }
        }
    }

    public boolean addOrRemoveSelectedDialog(long did, View cell) {
        if (onlySelect && getMessagesController().isForum(did)) {
            return false;
        }
        if (selectedDialogs.contains(did)) {
            selectedDialogs.remove(did);
            if (cell instanceof DialogCell) {
                ((DialogCell) cell).setChecked(false, true);
            } else if (cell instanceof ProfileSearchCell) {
                ((ProfileSearchCell) cell).setChecked(false, true);
            }
            return false;
        } else {
            selectedDialogs.add(did);
            if (cell instanceof DialogCell) {
                ((DialogCell) cell).setChecked(true, true);
            } else if (cell instanceof ProfileSearchCell) {
                ((ProfileSearchCell) cell).setChecked(true, true);
            }
            return true;
        }
    }

    public void search(String query, boolean animated) {
        showSearch(true, false, animated);
        actionBar.openSearchField(query, false);
    }

    private void showSearch(boolean show, boolean startFromDownloads, boolean animated) {
        if (!show) {
            updateSpeedItem(false);
        }
        if (initialDialogsType != 0 && initialDialogsType != 3) {
            animated = false;
        }
        if (searchAnimator != null) {
            searchAnimator.cancel();
            searchAnimator = null;
        }
        if (tabsAlphaAnimator != null) {
            tabsAlphaAnimator.cancel();
            tabsAlphaAnimator = null;
        }
        searchIsShowed = show;
        ((SizeNotifierFrameLayout) fragmentView).invalidateBlur();
        if (show) {
            boolean onlyDialogsAdapter;
            if (searchFiltersWasShowed) {
                onlyDialogsAdapter = false;
            } else {
                onlyDialogsAdapter = onlyDialogsAdapter();
            }
            searchViewPager.showOnlyDialogsAdapter(onlyDialogsAdapter);
            whiteActionBar = !onlyDialogsAdapter;
            if (whiteActionBar) {
                searchFiltersWasShowed = true;
            }
            ContentView contentView = (ContentView) fragmentView;
            if (searchTabsView == null && !onlyDialogsAdapter) {
                searchTabsView = searchViewPager.createTabsView(false, 8);
                int filtersViewPosition = -1;
                if (filtersView != null) {
                    for (int i = 0; i < contentView.getChildCount(); i++) {
                        if (contentView.getChildAt(i) == filtersView) {
                            filtersViewPosition = i;
                            break;
                        }
                    }
                }
                if (filtersViewPosition > 0) {
                    contentView.addView(searchTabsView, filtersViewPosition, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44));
                } else {
                    contentView.addView(searchTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44));
                }
            } else if (searchTabsView != null && onlyDialogsAdapter) {
                ViewParent parent = searchTabsView.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(searchTabsView);
                }
                searchTabsView = null;
            }

            EditTextBoldCursor editText = searchItem.getSearchField();
            if (whiteActionBar) {
                editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                editText.setHintTextColor(Theme.getColor(Theme.key_player_time));
                editText.setCursorColor(Theme.getColor(Theme.key_chat_messagePanelCursor));
            } else {
                editText.setCursorColor(Theme.getColor(Theme.key_actionBarDefaultSearch));
                editText.setHintTextColor(Theme.getColor(Theme.key_actionBarDefaultSearchPlaceholder));
                editText.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSearch));
            }
            searchViewPager.setKeyboardHeight(((ContentView) fragmentView).getKeyboardHeight());
            updateDrawerSwipeEnabled();

            searchViewPager.clear();
            if (folderId != 0 && (rightSlidingDialogContainer == null || !rightSlidingDialogContainer.hasFragment())) {
                FiltersView.MediaFilterData filterData = new FiltersView.MediaFilterData(R.drawable.chats_archive, LocaleController.getString("ArchiveSearchFilter", R.string.ArchiveSearchFilter), null, FiltersView.FILTER_TYPE_ARCHIVE);
                addSearchFilter(filterData);
            }
        } else {
            updateDrawerSwipeEnabled();
        }

        if (animated && searchViewPager.dialogsSearchAdapter.hasRecentSearch()) {
            AndroidUtilities.setAdjustResizeToNothing(getParentActivity(), classGuid);
        } else {
            AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        }
        if (!show && filterTabsView != null && canShowFilterTabsView) {
            filterTabsView.setVisibility(View.VISIBLE);
        }
        if (animated) {
            if (show) {
                searchViewPager.setVisibility(View.VISIBLE);
                searchViewPager.reset();
                updateFiltersView(true, null, null, false, false);
                if (searchTabsView != null) {
                    searchTabsView.hide(false, false);
                    searchTabsView.setVisibility(View.VISIBLE);
                }
            } else {
                viewPages[0].listView.setVisibility(View.VISIBLE);
                viewPages[0].setVisibility(View.VISIBLE);
            }

            setDialogsListFrozen(true);
            viewPages[0].listView.setVerticalScrollBarEnabled(false);
            searchViewPager.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            searchAnimator = new AnimatorSet();
            ArrayList<Animator> animators = new ArrayList<>();
            animators.add(ObjectAnimator.ofFloat(viewPages[0], View.ALPHA, show ? 0.0f : 1.0f));
            animators.add(ObjectAnimator.ofFloat(viewPages[0], View.SCALE_X, show ? 0.9f : 1.0f));
            animators.add(ObjectAnimator.ofFloat(viewPages[0], View.SCALE_Y, show ? 0.9f : 1.0f));
            if (rightSlidingDialogContainer != null) {
                rightSlidingDialogContainer.setVisibility(View.VISIBLE);
                animators.add(ObjectAnimator.ofFloat(rightSlidingDialogContainer, View.ALPHA, show ? 0.0f : 1.0f));
            }
            animators.add(ObjectAnimator.ofFloat(searchViewPager, View.ALPHA, show ? 1.0f : 0.0f));
            animators.add(ObjectAnimator.ofFloat(searchViewPager, View.SCALE_X, show ? 1.0f : 1.05f));
            animators.add(ObjectAnimator.ofFloat(searchViewPager, View.SCALE_Y, show ? 1.0f : 1.05f));
            if (passcodeItem != null) {
                animators.add(ObjectAnimator.ofFloat(passcodeItem.getIconView(), View.ALPHA, show ? 0 : 1f));
            }

            if (downloadsItem != null) {
                if (show) {
                    downloadsItem.setAlpha(0f);
                } else {
                    animators.add(ObjectAnimator.ofFloat(downloadsItem, View.ALPHA, 1f));
                }
                updateProxyButton(false, false);
            }

            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                tabsAlphaAnimator = ObjectAnimator.ofFloat(filterTabsView.getTabsContainer(), View.ALPHA, show ? 0.0f : 1.0f).setDuration(100);
                tabsAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        tabsAlphaAnimator = null;
                    }
                });
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(searchAnimationProgress, show ? 1f : 0);
            valueAnimator.addUpdateListener(valueAnimator1 -> setSearchAnimationProgress((float) valueAnimator1.getAnimatedValue(), false));

            animators.add(valueAnimator);
            searchAnimator.playTogether(animators);
            searchAnimator.setDuration(show ? 200 : 180);
            searchAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);

            if (filterTabsViewIsVisible) {
                int backgroundColor1 = Theme.getColor(folderId == 0 ? Theme.key_actionBarDefault : Theme.key_actionBarDefaultArchived);
                int backgroundColor2 = Theme.getColor(Theme.key_windowBackgroundWhite);
                int sum = Math.abs(Color.red(backgroundColor1) - Color.red(backgroundColor2)) + Math.abs(Color.green(backgroundColor1) - Color.green(backgroundColor2)) + Math.abs(Color.blue(backgroundColor1) - Color.blue(backgroundColor2));
                searchAnimationTabsDelayedCrossfade = sum / 255f > 0.3f;
            } else {
                searchAnimationTabsDelayedCrossfade = true;
            }
            if (!show) {
                searchAnimator.setStartDelay(20);
                if (tabsAlphaAnimator != null) {
                    if (searchAnimationTabsDelayedCrossfade) {
                        tabsAlphaAnimator.setStartDelay(80);
                        tabsAlphaAnimator.setDuration(100);
                    } else {
                        tabsAlphaAnimator.setDuration(show ? 200 : 180);

                    }
                }
            }
            searchAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    getNotificationCenter().onAnimationFinish(animationIndex);
                    if (searchAnimator != animation) {
                        return;
                    }
                    setDialogsListFrozen(false);
                    if (show) {
                        viewPages[0].listView.hide();
                        if (filterTabsView != null) {
                            filterTabsView.setVisibility(View.GONE);
                        }
                        searchWasFullyShowed = true;
                        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
                        searchItem.setVisibility(View.GONE);
                        if (rightSlidingDialogContainer != null) {
                            rightSlidingDialogContainer.setVisibility(View.GONE);
                        }
                    } else {
                        searchItem.collapseSearchFilters();
                        whiteActionBar = false;
                        searchViewPager.setVisibility(View.GONE);
                        if (searchTabsView != null) {
                            searchTabsView.setVisibility(View.GONE);
                        }
                        searchItem.clearSearchFilters();
                        searchViewPager.clear();
                        filtersView.setVisibility(View.GONE);
                        viewPages[0].listView.show();
                        if (!onlySelect) {
                            hideFloatingButton(false);
                        }
                        searchWasFullyShowed = false;
                        if (rightSlidingDialogContainer != null) {
                            rightSlidingDialogContainer.setVisibility(View.VISIBLE);
                        }
                    }

                    if (fragmentView != null) {
                        fragmentView.requestLayout();
                    }

                    setSearchAnimationProgress(show ? 1f : 0, false);

                    viewPages[0].listView.setVerticalScrollBarEnabled(true);
                    searchViewPager.setBackground(null);
                    searchAnimator = null;

                    if (downloadsItem != null) {
                        downloadsItem.setAlpha(show ? 0 : 1f);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    getNotificationCenter().onAnimationFinish(animationIndex);
                    if (searchAnimator == animation) {
                        if (show) {
                            viewPages[0].listView.hide();
                        } else {
                            viewPages[0].listView.show();
                        }
                        searchAnimator = null;
                    }
                }
            });
            animationIndex = getNotificationCenter().setAnimationInProgress(animationIndex, null);
            searchAnimator.start();
            if (tabsAlphaAnimator != null) {
                tabsAlphaAnimator.start();
            }
        } else {
            setDialogsListFrozen(false);
            if (show) {
                viewPages[0].listView.hide();
            } else {
                viewPages[0].listView.show();
            }
            viewPages[0].setAlpha(show ? 0.0f : 1.0f);
            viewPages[0].setScaleX(show ? 0.9f : 1.0f);
            viewPages[0].setScaleY(show ? 0.9f : 1.0f);
            searchViewPager.setAlpha(show ? 1.0f : 0.0f);
            filtersView.setAlpha(show ? 1.0f : 0.0f);
            searchViewPager.setScaleX(show ? 1.0f : 1.1f);
            searchViewPager.setScaleY(show ? 1.0f : 1.1f);
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                filterTabsView.setTranslationY(show ? -AndroidUtilities.dp(44) : 0);
                filterTabsView.getTabsContainer().setAlpha(show ? 0.0f : 1.0f);
            }
            if (filterTabsView != null) {
                if (canShowFilterTabsView && !show) {
                    filterTabsView.setVisibility(View.VISIBLE);
                } else {
                    filterTabsView.setVisibility(View.GONE);
                }
            }
            searchViewPager.setVisibility(show ? View.VISIBLE : View.GONE);
            setSearchAnimationProgress(show ? 1f : 0, false);
            fragmentView.invalidate();

            if (downloadsItem != null) {
                downloadsItem.setAlpha(show ? 0 : 1f);
            }
        }
        if (initialSearchType >= 0) {
            searchViewPager.setPosition(searchViewPager.getPositionForType(initialSearchType));
        }
        if (!show) {
            initialSearchType = -1;
        }
        if (show && startFromDownloads) {
            searchViewPager.showDownloads();
            updateSpeedItem(true);
        }
    }

    public boolean onlyDialogsAdapter() {
        int dialogsCount = getMessagesController().getTotalDialogsCount();
        return onlySelect || !searchViewPager.dialogsSearchAdapter.hasRecentSearch() || dialogsCount <= 10;
    }

    private void updateFilterTabsVisibility(boolean animated) {
        if (isPaused || databaseMigrationHint != null) {
            animated = false;
        }
        if (searchIsShowed) {
            if (filtersTabAnimator != null) {
                filtersTabAnimator.cancel();
            }
            filterTabsViewIsVisible = canShowFilterTabsView;
            filterTabsProgress = filterTabsViewIsVisible ? 1f : 0;
            return;
        }
        boolean visible = canShowFilterTabsView;
        if (filterTabsViewIsVisible != visible) {
            if (filtersTabAnimator != null) {
                filtersTabAnimator.cancel();
            }
            filterTabsViewIsVisible = visible;
            if (animated) {
                if (visible) {
                    if (filterTabsView.getVisibility() != View.VISIBLE) {
                        filterTabsView.setVisibility(View.VISIBLE);
                    }
                    filtersTabAnimator = ValueAnimator.ofFloat(0, 1f);
                    filterTabsMoveFrom = AndroidUtilities.dp(44);
                } else {
                    filtersTabAnimator = ValueAnimator.ofFloat(1f, 0f);
                    filterTabsMoveFrom = Math.max(0, AndroidUtilities.dp(44) + actionBar.getTranslationY());
                }
                float animateFromScrollY = actionBar.getTranslationY();
                filtersTabAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        filtersTabAnimator = null;
                        scrollAdditionalOffset = AndroidUtilities.dp(44) - filterTabsMoveFrom;
                        if (!visible) {
                            filterTabsView.setVisibility(View.GONE);
                        }
                        if (fragmentView != null) {
                            fragmentView.requestLayout();
                        }
                        getNotificationCenter().onAnimationFinish(animationIndex);
                    }
                });
                filtersTabAnimator.addUpdateListener(valueAnimator -> {
                    filterTabsProgress = (float) valueAnimator.getAnimatedValue();
                    if (!visible) {
                        setScrollY(animateFromScrollY * filterTabsProgress);
                    }
                    if (fragmentView != null) {
                        fragmentView.invalidate();
                    }
                });
                filtersTabAnimator.setDuration(220);
                filtersTabAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                animationIndex = getNotificationCenter().setAnimationInProgress(animationIndex, null);
                filtersTabAnimator.start();
                fragmentView.requestLayout();
            } else {
                filterTabsProgress = visible ? 1f : 0;
                filterTabsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            }
        }
    }

    public void setSearchAnimationProgress(float progress, boolean full) {
        searchAnimationProgress = progress;
        if (whiteActionBar) {
            int color1 = folderId != 0 ? Theme.getColor(Theme.key_actionBarDefaultArchivedIcon) : Theme.getColor(Theme.key_actionBarDefaultIcon);
            actionBar.setItemsColor(ColorUtils.blendARGB(color1, Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), searchAnimationProgress), false);
            actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), searchAnimationProgress), true);

            color1 = folderId != 0 ? Theme.getColor(Theme.key_actionBarDefaultArchivedSelector) : Theme.getColor(Theme.key_actionBarDefaultSelector);
            int color2 = Theme.getColor(Theme.key_actionBarActionModeDefaultSelector);
            actionBar.setItemsBackgroundColor(ColorUtils.blendARGB(color1, color2, searchAnimationProgress), false);
        }
        if (fragmentView != null) {
            fragmentView.invalidate();
        }
        if (full) {
//
//            if (show) {
//                searchViewPager.setVisibility(View.VISIBLE);
//                searchViewPager.reset();
//                updateFiltersView(true, null, null, false, false);
//                if (searchTabsView != null) {
//                    searchTabsView.hide(false, false);
//                    searchTabsView.setVisibility(View.VISIBLE);
//                }
//            } else {
//                viewPages[0].listView.setVisibility(View.VISIBLE);
//                viewPages[0].setVisibility(View.VISIBLE);
//            }

//            setDialogsListFrozen(true);
//            viewPages[0].listView.setVerticalScrollBarEnabled(false);
//            searchViewPager.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//            searchAnimator = new AnimatorSet();
//            ArrayList<Animator> animators = new ArrayList<>();
            if (viewPages[0] != null) {
                if (progress < 1f) {
                    viewPages[0].setVisibility(View.VISIBLE);
                }
                viewPages[0].setAlpha(1f - progress);
                viewPages[0].setScaleX(.9f + .1f * progress);
                viewPages[0].setScaleY(.9f + .1f * progress);
            }
            if (rightSlidingDialogContainer != null) {
                if (progress >= 1f) {
                    rightSlidingDialogContainer.setVisibility(View.GONE);
                } else {
                    rightSlidingDialogContainer.setVisibility(View.VISIBLE);
                    rightSlidingDialogContainer.setAlpha(1f - progress);
                }
            }
            if (searchViewPager != null) {
                searchViewPager.setAlpha(progress);
                searchViewPager.setScaleX(1f + .05f * (1f - progress));
                searchViewPager.setScaleY(1f + .05f * (1f - progress));
            }
            if (passcodeItem != null) {
                passcodeItem.getIconView().setAlpha(1f - progress);
            }

            if (downloadsItem != null) {
                downloadsItem.setAlpha(1f - progress);
//                updateProxyButton(false, false);
            }

            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                filterTabsView.getTabsContainer().setAlpha(1f - progress);
            }

//            if (filterTabsViewIsVisible) {
//                int backgroundColor1 = Theme.getColor(folderId == 0 ? Theme.key_actionBarDefault : Theme.key_actionBarDefaultArchived);
//                int backgroundColor2 = Theme.getColor(Theme.key_windowBackgroundWhite);
//                int sum = Math.abs(Color.red(backgroundColor1) - Color.red(backgroundColor2)) + Math.abs(Color.green(backgroundColor1) - Color.green(backgroundColor2)) + Math.abs(Color.blue(backgroundColor1) - Color.blue(backgroundColor2));
//                searchAnimationTabsDelayedCrossfade = sum / 255f > 0.3f;
//            } else {
//                searchAnimationTabsDelayedCrossfade = true;
//            }
//            if (!show) {
//                searchAnimator.setStartDelay(20);
//                if (tabsAlphaAnimator != null) {
//                    if (searchAnimationTabsDelayedCrossfade) {
//                        tabsAlphaAnimator.setStartDelay(80);
//                        tabsAlphaAnimator.setDuration(100);
//                    } else {
//                        tabsAlphaAnimator.setDuration(show ? 200 : 180);
//
//                    }
//                }
//            }
//            searchAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    getNotificationCenter().onAnimationFinish(animationIndex);
//                    if (searchAnimator != animation) {
//                        return;
//                    }
//                    setDialogsListFrozen(false);
//                    if (show) {
//                        viewPages[0].listView.hide();
//                        if (filterTabsView != null) {
//                            filterTabsView.setVisibility(View.GONE);
//                        }
//                        searchWasFullyShowed = true;
//                        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
//                        searchItem.setVisibility(View.GONE);
//                        if (rightSlidingDialogContainer != null) {
//                            rightSlidingDialogContainer.setVisibility(View.GONE);
//                        }
//                    } else {
//                        searchItem.collapseSearchFilters();
//                        whiteActionBar = false;
//                        searchViewPager.setVisibility(View.GONE);
//                        if (searchTabsView != null) {
//                            searchTabsView.setVisibility(View.GONE);
//                        }
//                        searchItem.clearSearchFilters();
//                        searchViewPager.clear();
//                        filtersView.setVisibility(View.GONE);
//                        viewPages[0].listView.show();
//                        if (!onlySelect) {
//                            hideFloatingButton(false);
//                        }
//                        searchWasFullyShowed = false;
//                        if (rightSlidingDialogContainer != null) {
//                            rightSlidingDialogContainer.setVisibility(View.VISIBLE);
//                        }
//                    }
//
//                    if (fragmentView != null) {
//                        fragmentView.requestLayout();
//                    }
//
//                    setSearchAnimationProgress(show ? 1f : 0, false);
//
//                    viewPages[0].listView.setVerticalScrollBarEnabled(true);
//                    searchViewPager.setBackground(null);
//                    searchAnimator = null;
//
//                    if (downloadsItem != null) {
//                        downloadsItem.setAlpha(show ? 0 : 1f);
//                    }
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//                    getNotificationCenter().onAnimationFinish(animationIndex);
//                    if (searchAnimator == animation) {
//                        if (show) {
//                            viewPages[0].listView.hide();
//                        } else {
//                            viewPages[0].listView.show();
//                        }
//                        searchAnimator = null;
//                    }
//                }
//            });
//            animationIndex = getNotificationCenter().setAnimationInProgress(animationIndex, null);
//            searchAnimator.start();
//            if (tabsAlphaAnimator != null) {
//                tabsAlphaAnimator.start();
//            }
        }
        updateContextViewPosition();
    }

    private void findAndUpdateCheckBox(long dialogId, boolean checked) {
        if (viewPages == null) {
            return;
        }
        for (int b = 0; b < viewPages.length; b++) {
            int count = viewPages[b].listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = viewPages[b].listView.getChildAt(a);
                if (child instanceof DialogCell) {
                    DialogCell dialogCell = (DialogCell) child;
                    if (dialogCell.getDialogId() == dialogId) {
                        dialogCell.setChecked(checked, true);
                        break;
                    }
                }
            }
        }
    }

    private void checkListLoad(ViewPage viewPage) {
        if (tabsAnimationInProgress || startedTracking || filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && filterTabsView.isAnimatingIndicator()) {
            return;
        }
        int firstVisibleItem = viewPage.layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = viewPage.layoutManager.findLastVisibleItemPosition();
        int visibleItemCount = Math.abs(viewPage.layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
        if (lastVisibleItem != RecyclerView.NO_POSITION) {
            RecyclerView.ViewHolder holder = viewPage.listView.findViewHolderForAdapterPosition(lastVisibleItem);
            if (floatingForceVisible = holder != null && holder.getItemViewType() == 11) {
                hideFloatingButton(false);
            }
        } else {
            floatingForceVisible = false;
        }
        boolean loadArchived = false;
        boolean loadArchivedFromCache = false;
        boolean load = false;
        boolean loadFromCache = false;
        if (viewPage.dialogsType == 7 || viewPage.dialogsType == 8) {
            ArrayList<MessagesController.DialogFilter> dialogFilters = getMessagesController().dialogFilters;
            if (viewPage.selectedType >= 0 && viewPage.selectedType < dialogFilters.size()) {
                MessagesController.DialogFilter filter = getMessagesController().dialogFilters.get(viewPage.selectedType);
                if ((filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED) == 0) {
                    if (visibleItemCount > 0 && lastVisibleItem >= getDialogsArray(currentAccount, viewPage.dialogsType, 1, dialogsListFrozen).size() - 10 ||
                            visibleItemCount == 0 && !getMessagesController().isDialogsEndReached(1)) {
                        loadArchivedFromCache = !getMessagesController().isDialogsEndReached(1);
                        if (loadArchivedFromCache || !getMessagesController().isServerDialogsEndReached(1)) {
                            loadArchived = true;
                        }
                    }
                }
            }
        }
        if (visibleItemCount > 0 && lastVisibleItem >= getDialogsArray(currentAccount, viewPage.dialogsType, folderId, dialogsListFrozen).size() - 10 ||
                visibleItemCount == 0 && (viewPage.dialogsType == 7 || viewPage.dialogsType == 8) && !getMessagesController().isDialogsEndReached(folderId)) {
            loadFromCache = !getMessagesController().isDialogsEndReached(folderId);
            if (loadFromCache || !getMessagesController().isServerDialogsEndReached(folderId)) {
                load = true;
            }
        }
        if (load || loadArchived) {
            boolean loadFinal = load;
            boolean loadFromCacheFinal = loadFromCache;
            boolean loadArchivedFinal = loadArchived;
            boolean loadArchivedFromCacheFinal = loadArchivedFromCache;
            AndroidUtilities.runOnUIThread(() -> {
                if (loadFinal) {
                    getMessagesController().loadDialogs(folderId, -1, 100, loadFromCacheFinal);
                }
                if (loadArchivedFinal) {
                    getMessagesController().loadDialogs(1, -1, 100, loadArchivedFromCacheFinal);
                }
            });
        }
    }

    private void onItemClick(View view, int position, RecyclerListView.Adapter adapter) {
        if (getParentActivity() == null) {
            return;
        }
        long dialogId = 0;
        int topicId = 0;
        int message_id = 0;
        boolean isGlobalSearch = false;
        int folderId = 0;
        int filterId = 0;
        if (adapter instanceof DialogsAdapter) {
            DialogsAdapter dialogsAdapter = (DialogsAdapter) adapter;
            int dialogsType = dialogsAdapter.getDialogsType();
            if (dialogsType == 7 || dialogsType == 8) {
                MessagesController.DialogFilter dialogFilter = getMessagesController().selectedDialogFilter[dialogsType == 7 ? 0 : 1];
                filterId = dialogFilter.id;
            }
            TLObject object = dialogsAdapter.getItem(position);
            if (object instanceof TLRPC.User) {
                dialogId = ((TLRPC.User) object).id;
            } else if (object instanceof TLRPC.Dialog) {
                TLRPC.Dialog dialog = (TLRPC.Dialog) object;
                folderId = dialog.folder_id;
                if (dialog instanceof TLRPC.TL_dialogFolder) {
                    if (actionBar.isActionModeShowed(null)) {
                        return;
                    }
                    TLRPC.TL_dialogFolder dialogFolder = (TLRPC.TL_dialogFolder) dialog;
                    Bundle args = new Bundle();
                    args.putInt("folderId", dialogFolder.folder.id);
                    args.putBoolean("onlySelect", onlySelect);
                    DialogsActivity dialogsActivity = new DialogsActivity(args);
                    dialogsActivity.setDelegate(delegate);
                    presentFragment(dialogsActivity, onlySelect);
                    return;
                }
                dialogId = dialog.id;
                if (actionBar.isActionModeShowed(null)) {
                    showOrUpdateActionMode(dialogId, view);
                    return;
                }
            } else if (object instanceof TLRPC.TL_recentMeUrlChat) {
                dialogId = -((TLRPC.TL_recentMeUrlChat) object).chat_id;
            } else if (object instanceof TLRPC.TL_recentMeUrlUser) {
                dialogId = ((TLRPC.TL_recentMeUrlUser) object).user_id;
            } else if (object instanceof TLRPC.TL_recentMeUrlChatInvite) {
                TLRPC.TL_recentMeUrlChatInvite chatInvite = (TLRPC.TL_recentMeUrlChatInvite) object;
                TLRPC.ChatInvite invite = chatInvite.chat_invite;
                if (invite.chat == null && (!invite.channel || invite.megagroup) || invite.chat != null && (!ChatObject.isChannel(invite.chat) || invite.chat.megagroup)) {
                    String hash = chatInvite.url;
                    int index = hash.indexOf('/');
                    if (index > 0) {
                        hash = hash.substring(index + 1);
                    }
                    showDialog(new JoinGroupAlert(getParentActivity(), invite, hash, DialogsActivity.this, null));
                    return;
                } else {
                    if (invite.chat != null) {
                        dialogId = -invite.chat.id;
                    } else {
                        return;
                    }
                }
            } else if (object instanceof TLRPC.TL_recentMeUrlStickerSet) {
                TLRPC.StickerSet stickerSet = ((TLRPC.TL_recentMeUrlStickerSet) object).set.set;
                TLRPC.TL_inputStickerSetID set = new TLRPC.TL_inputStickerSetID();
                set.id = stickerSet.id;
                set.access_hash = stickerSet.access_hash;
                showDialog(new StickersAlert(getParentActivity(), DialogsActivity.this, set, null, null));
                return;
            } else if (object instanceof TLRPC.TL_recentMeUrlUnknown) {
                return;
            } else {
                return;
            }
        } else if (adapter == searchViewPager.dialogsSearchAdapter) {
            Object obj = searchViewPager.dialogsSearchAdapter.getItem(position);
            isGlobalSearch = searchViewPager.dialogsSearchAdapter.isGlobalSearch(position);
            if (obj instanceof TLRPC.User) {
                dialogId = ((TLRPC.User) obj).id;
                if (!onlySelect) {
                    searchDialogId = dialogId;
                    searchObject = (TLRPC.User) obj;
                }
            } else if (obj instanceof TLRPC.Chat) {
                dialogId = -((TLRPC.Chat) obj).id;
                if (!onlySelect) {
                    searchDialogId = dialogId;
                    searchObject = (TLRPC.Chat) obj;
                }
            } else if (obj instanceof TLRPC.EncryptedChat) {
                dialogId = DialogObject.makeEncryptedDialogId(((TLRPC.EncryptedChat) obj).id);
                if (!onlySelect) {
                    searchDialogId = dialogId;
                    searchObject = (TLRPC.EncryptedChat) obj;
                }
            } else if (obj instanceof MessageObject) {
                MessageObject messageObject = (MessageObject) obj;
                dialogId = messageObject.getDialogId();
                message_id = messageObject.getId();
                TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
                if (ChatObject.isForum(chat)) {
                    topicId = MessageObject.getTopicId(messageObject.messageOwner, true);
                }
                searchViewPager.dialogsSearchAdapter.addHashtagsFromMessage(searchViewPager.dialogsSearchAdapter.getLastSearchString());
            } else if (obj instanceof String) {
                String str = (String) obj;
                if (searchViewPager.dialogsSearchAdapter.isHashtagSearch()) {
                    actionBar.openSearchField(str, false);
                } else if (!str.equals("section")) {
                    NewContactBottomSheet activity = new NewContactBottomSheet(DialogsActivity.this, getContext());
                    activity.setInitialPhoneNumber(str, true);
//                    presentFragment(activity);
                    activity.show();
                }
            } else if (obj instanceof ContactsController.Contact) {
                ContactsController.Contact contact = (ContactsController.Contact) obj;
                AlertsCreator.createContactInviteDialog(DialogsActivity.this, contact.first_name, contact.last_name, contact.phones.get(0));
            } else if (obj instanceof TLRPC.TL_forumTopic && rightSlidingDialogContainer != null && rightSlidingDialogContainer.getFragment() instanceof TopicsFragment) {
                dialogId = ((TopicsFragment) rightSlidingDialogContainer.getFragment()).getDialogId();
                topicId = ((TLRPC.TL_forumTopic) obj).id;
            }

            if (dialogId != 0 && actionBar.isActionModeShowed()) {
                if (actionBar.isActionModeShowed(ACTION_MODE_SEARCH_DIALOGS_TAG) && message_id == 0 && !isGlobalSearch) {
                    showOrUpdateActionMode(dialogId, view);
                }
                return;
            }
        }

        if (dialogId == 0) {
            return;
        }

        if (onlySelect) {
            if (!validateSlowModeDialog(dialogId)) {
                return;
            }
            if (!getMessagesController().isForum(dialogId) && (!selectedDialogs.isEmpty() || (initialDialogsType == 3 && selectAlertString != null))) {
                if (!selectedDialogs.contains(dialogId) && !checkCanWrite(dialogId)) {
                    return;
                }
                boolean checked = addOrRemoveSelectedDialog(dialogId, view);
                if (adapter == searchViewPager.dialogsSearchAdapter) {
                    actionBar.closeSearchField();
                    findAndUpdateCheckBox(dialogId, checked);
                }
                updateSelectedCount();
            } else {
                if (canSelectTopics && getMessagesController().isForum(dialogId)) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("chat_id", -dialogId);
                    bundle.putBoolean("for_select", true);
                    bundle.putBoolean("forward_to", true);
                    TopicsFragment topicsFragment = new TopicsFragment(bundle);
                    topicsFragment.setForwardFromDialogFragment(DialogsActivity.this);
                    presentFragment(topicsFragment);
                } else {
                    didSelectResult(dialogId, 0,true, false);
                }
            }
        } else {
            Bundle args = new Bundle();
            if (DialogObject.isEncryptedDialog(dialogId)) {
                args.putInt("enc_id", DialogObject.getEncryptedChatId(dialogId));
            } else if (DialogObject.isUserDialog(dialogId)) {
                args.putLong("user_id", dialogId);
            } else {
                long did = dialogId;
                if (message_id != 0) {
                    TLRPC.Chat chat = getMessagesController().getChat(-did);
                    if (chat != null && chat.migrated_to != null) {
                        args.putLong("migrated_to", did);
                        did = -chat.migrated_to.channel_id;
                    }
                }
                args.putLong("chat_id", -did);
            }
            if (message_id != 0) {
                args.putInt("message_id", message_id);
            } else if (!isGlobalSearch) {
                closeSearch();
            } else {
                if (searchObject != null) {
                    searchViewPager.dialogsSearchAdapter.putRecentSearch(searchDialogId, searchObject);
                    searchObject = null;
                }
            }
            boolean canOpenInRightSlidingView = !(LocaleController.isRTL || searching || (AndroidUtilities.isTablet() && folderId != 0));
            args.putInt("dialog_folder_id", folderId);
            args.putInt("dialog_filter_id", filterId);
            if (AndroidUtilities.isTablet() && (!getMessagesController().isForum(dialogId) || !canOpenInRightSlidingView)) {
                if (openedDialogId.dialogId == dialogId && adapter != searchViewPager.dialogsSearchAdapter) {
                    if (getParentActivity() instanceof LaunchActivity) {
                        LaunchActivity launchActivity = (LaunchActivity) getParentActivity();
                        List<BaseFragment> rightFragments = launchActivity.getRightActionBarLayout().getFragmentStack();
                        if (!rightFragments.isEmpty()) {
                            if (rightFragments.size() == 1 && rightFragments.get(rightFragments.size() - 1) instanceof ChatActivity) {
                                ((ChatActivity) rightFragments.get(rightFragments.size() - 1)).onPageDownClicked();
                            } else if (rightFragments.size() == 2) {
                                launchActivity.getRightActionBarLayout().closeLastFragment();
                            } else if (getParentActivity() instanceof LaunchActivity) {
                                BaseFragment first = rightFragments.get(0);
                                rightFragments.clear();
                                rightFragments.add(first);
                                launchActivity.getRightActionBarLayout().rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
                            }
                        }
                    }
                    return;
                }
            }
            if (searchViewPager.actionModeShowing()) {
                searchViewPager.hideActionMode();
            }
            if (searchString != null) {
                if (getMessagesController().checkCanOpenChat(args, DialogsActivity.this)) {
                    getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
                    presentFragment(new ChatActivity(args));
                }
            } else {
                slowedReloadAfterDialogClick = true;
                if (getMessagesController().checkCanOpenChat(args, DialogsActivity.this)) {
                    TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
                    if (chat != null && chat.forum && topicId == 0 && !NaConfig.INSTANCE.getShowForumAsNormalChat().Bool()) {
                        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
                            presentFragment(new TopicsFragment(args));
                        } else {
                            if (!canOpenInRightSlidingView) {
                                presentFragment(new TopicsFragment(args));
                            } else if (!searching) {
                                if (rightSlidingDialogContainer.currentFragment != null && ((TopicsFragment) rightSlidingDialogContainer.currentFragment).getDialogId() == dialogId) {
                                    rightSlidingDialogContainer.finishPreview();
                                } else {
                                    viewPages[0].listView.prepareSelectorForAnimation();
                                    TopicsFragment topicsFragment = new TopicsFragment(args);
                                    topicsFragment.parentDialogsActivity = this;
                                    rightSlidingDialogContainer.presentFragment(getParentLayout(), topicsFragment);
                                }
                                searchViewPager.updateTabs();
                            }
                        }
                    } else {
                        ChatActivity chatActivity = new ChatActivity(args);
                        if (topicId != 0) {
                            ForumUtilities.applyTopic(chatActivity, MessagesStorage.TopicKey.of(dialogId, topicId));
                        }
                        if (adapter instanceof DialogsAdapter && DialogObject.isUserDialog(dialogId) && (getMessagesController().dialogs_dict.get(dialogId) == null)) {
                            TLRPC.Document sticker = getMediaDataController().getGreetingsSticker();
                            if (sticker != null) {
                                chatActivity.setPreloadedSticker(sticker, true);
                            }
                        }
                        presentFragment(chatActivity);
                    }
                }
            }
        }
    }

    public void setOpenedDialogId(long dialogId, int topicId) {
        openedDialogId.dialogId = dialogId;
        openedDialogId.topicId = topicId;

        if (viewPages == null) {
            return;
        }
        for (ViewPage viewPage : viewPages) {
            if (viewPage.isDefaultDialogType() && AndroidUtilities.isTablet()) {
                viewPage.dialogsAdapter.setOpenedDialogId(openedDialogId.dialogId);
            }
        }
        updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
    }

    private boolean onItemLongClick(RecyclerListView listView, View view, int position, float x, float y, int dialogsType, RecyclerListView.Adapter adapter) {
        if (getParentActivity() == null) {
            return false;
        }
        if (!actionBar.isActionModeShowed() && !AndroidUtilities.isTablet() && !onlySelect && view instanceof DialogCell && !getMessagesController().isForum(((DialogCell)view).getDialogId())) {
            DialogCell cell = (DialogCell) view;
            if (cell.isPointInsideAvatar(x, y)) {
                return showChatPreview(cell);
            }
        }
        if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.hasFragment()) {
            return false;
        }
        if (adapter == searchViewPager.dialogsSearchAdapter) {
            Object item = searchViewPager.dialogsSearchAdapter.getItem(position);
            if (!searchViewPager.dialogsSearchAdapter.isSearchWas()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("ClearSearchSingleAlertTitle", R.string.ClearSearchSingleAlertTitle));
                long did;
                if (item instanceof TLRPC.Chat) {
                    TLRPC.Chat chat = (TLRPC.Chat) item;
                    builder.setMessage(LocaleController.formatString("ClearSearchSingleChatAlertText", R.string.ClearSearchSingleChatAlertText, chat.title));
                    did = -chat.id;
                } else if (item instanceof TLRPC.User) {
                    TLRPC.User user = (TLRPC.User) item;
                    if (user.id == getUserConfig().clientUserId) {
                        builder.setMessage(LocaleController.formatString("ClearSearchSingleChatAlertText", R.string.ClearSearchSingleChatAlertText, LocaleController.getString("SavedMessages", R.string.SavedMessages)));
                    } else {
                        builder.setMessage(LocaleController.formatString("ClearSearchSingleUserAlertText", R.string.ClearSearchSingleUserAlertText, ContactsController.formatName(user.first_name, user.last_name)));
                    }
                    did = user.id;
                } else if (item instanceof TLRPC.EncryptedChat) {
                    TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat) item;
                    TLRPC.User user = getMessagesController().getUser(encryptedChat.user_id);
                    builder.setMessage(LocaleController.formatString("ClearSearchSingleUserAlertText", R.string.ClearSearchSingleUserAlertText, ContactsController.formatName(user.first_name, user.last_name)));
                    did = DialogObject.makeEncryptedDialogId(encryptedChat.id);
                } else {
                    return false;
                }
                builder.setPositiveButton(LocaleController.getString("ClearSearchRemove", R.string.ClearSearchRemove), (dialogInterface, i) -> searchViewPager.dialogsSearchAdapter.removeRecentSearch(did));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                AlertDialog alertDialog = builder.create();
                showDialog(alertDialog);
                TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
                return true;
            }
        }
        TLRPC.Dialog dialog;
        if (adapter == searchViewPager.dialogsSearchAdapter) {
            if (onlySelect) {
                onItemClick(view, position, adapter);
                return false;
            }
            long dialogId = 0;
            if (view instanceof ProfileSearchCell && !searchViewPager.dialogsSearchAdapter.isGlobalSearch(position)) {
                dialogId = ((ProfileSearchCell) view).getDialogId();
            }
            if (dialogId != 0) {
                showOrUpdateActionMode(dialogId, view);
                return true;
            }
            return false;
        } else {
            DialogsAdapter dialogsAdapter = (DialogsAdapter) adapter;
            ArrayList<TLRPC.Dialog> dialogs = getDialogsArray(currentAccount, dialogsType, folderId, dialogsListFrozen);
            position = dialogsAdapter.fixPosition(position);
            if (position < 0 || position >= dialogs.size()) {
                return false;
            }
            dialog = dialogs.get(position);
        }

        if (dialog == null) {
            return false;
        }

        if (onlySelect) {
            if (initialDialogsType != 3 && initialDialogsType != 10) {
                return false;
            }
            if (!validateSlowModeDialog(dialog.id)) {
                return false;
            }
            addOrRemoveSelectedDialog(dialog.id, view);
            updateSelectedCount();
            return true;
        } else {
            if (dialog instanceof TLRPC.TL_dialogFolder) {
                onArchiveLongPress(view);
                return false;
            }
            if (actionBar.isActionModeShowed() && isDialogPinned(dialog)) {
                return false;
            }
            showOrUpdateActionMode(dialog.id, view);
            return true;
        }
    }

    private void onArchiveLongPress(View view) {
        if (!NekoConfig.disableVibration.Bool()) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
        BottomSheet.NekoXBuilder builder = new BottomSheet.NekoXBuilder(getParentActivity());
        final boolean hasUnread = getMessagesStorage().getArchiveUnreadCount() != 0;

        int[] icons = new int[]{
                hasUnread ? R.drawable.msg_markread : 0,
                SharedConfig.archiveHidden ? R.drawable.chats_pin : R.drawable.chats_unpin,
        };
        String[] items = new String[]{
                hasUnread ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : null,
                SharedConfig.archiveHidden ? LocaleController.getString("PinInTheList", R.string.PinInTheList) : LocaleController.getString("HideAboveTheList", R.string.HideAboveTheList)
        };
        builder.setItems(items, icons, (d, which) -> {
            if (which == 0) {
                getMessagesStorage().readAllDialogs(1);
            } else if (which == 1 && viewPages != null) {
                for (int a = 0; a < viewPages.length; a++) {
                    if (viewPages[a].dialogsType != 0 || viewPages[a].getVisibility() != View.VISIBLE) {
                        continue;
                    }
                    View child = viewPages[a].listView.getChildAt(0);
                    DialogCell dialogCell = null;
                    if (child instanceof DialogCell && ((DialogCell) child).isFolderCell()) {
                        dialogCell = (DialogCell) child;
                    }
                    viewPages[a].listView.toggleArchiveHidden(true, dialogCell);
                }
            }
        });
        showDialog(builder.create());
    }


    public boolean showChatPreview(DialogCell cell) {
        long dialogId = cell.getDialogId();
        Bundle args = new Bundle();
        int message_id = cell.getMessageId();
        if (DialogObject.isEncryptedDialog(dialogId)) {
            return false;
        } else {
            if (DialogObject.isUserDialog(dialogId)) {
                args.putLong("user_id", dialogId);
            } else {
                long did = dialogId;
                if (message_id != 0) {
                    TLRPC.Chat chat = getMessagesController().getChat(-did);
                    if (chat != null && chat.migrated_to != null) {
                        args.putLong("migrated_to", did);
                        did = -chat.migrated_to.channel_id;
                    }
                }
                args.putLong("chat_id", -did);
            }
        }
        if (message_id != 0) {
            args.putInt("message_id", message_id);
        }

        final ArrayList<Long> dialogIdArray = new ArrayList<>();
        dialogIdArray.add(dialogId);

//        boolean hasFolders = getMessagesController().filtersEnabled && getMessagesController().dialogFiltersLoaded && getMessagesController().dialogFilters != null && getMessagesController().dialogFilters.size() > 0;
        final ActionBarPopupWindow.ActionBarPopupWindowLayout[] previewMenu = new ActionBarPopupWindow.ActionBarPopupWindowLayout[1];
//
//        LinearLayout foldersMenuView = null;
//        int[] foldersMenu = new int[1];
//        if (hasFolders) {
//            foldersMenuView = new LinearLayout(getParentActivity());
//            foldersMenuView.setOrientation(LinearLayout.VERTICAL);
//
//            ScrollView scrollView = new ScrollView(getParentActivity()) {
//                @Override
//                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
//                            (int) Math.min(
//                                    MeasureSpec.getSize(heightMeasureSpec),
//                                    Math.min(AndroidUtilities.displaySize.y * 0.35f, AndroidUtilities.dp(400))
//                            ),
//                            MeasureSpec.getMode(heightMeasureSpec)
//                    ));
//                }
//            };
//            LinearLayout linearLayout = new LinearLayout(getParentActivity());
//            linearLayout.setOrientation(LinearLayout.VERTICAL);
//            scrollView.addView(linearLayout);
//            final boolean backButtonAtTop = true;
//
//            final int foldersCount = getMessagesController().dialogFilters.size();
//            ActionBarMenuSubItem lastItem = null;
//            for (int i = 0; i < foldersCount; ++i) {
//                MessagesController.DialogFilter folder = getMessagesController().dialogFilters.get(i);
//                if (folder.includesDialog(AccountInstance.getInstance(currentAccount), dialogId)) {
//                    continue;
//                }
//                final ArrayList<Long> alwaysShow = FiltersListBottomSheet.getDialogsCount(DialogsActivity.this, folder, dialogIdArray, true, false);
//                int currentCount = folder.alwaysShow.size();
//                if (currentCount + alwaysShow.size() > 100) {
//                    continue;
//                }
//                ActionBarMenuSubItem folderItem = lastItem = new ActionBarMenuSubItem(getParentActivity(), !backButtonAtTop && linearLayout.getChildCount() == 0, false);
//                folderItem.setTextAndIcon(folder.name, R.drawable.msg_folders);
//                folderItem.setMinimumWidth(160);
//                folderItem.setOnClickListener(e -> {
//                    if (!alwaysShow.isEmpty()) {
//                        for (int a = 0; a < alwaysShow.size(); a++) {
//                            folder.neverShow.remove(alwaysShow.get(a));
//                        }
//                        folder.alwaysShow.addAll(alwaysShow);
//                        FilterCreateActivity.saveFilterToServer(folder, folder.flags, folder.name, folder.alwaysShow, folder.neverShow, folder.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, null);
//                    }
//                    long did;
//                    if (alwaysShow.size() == 1) {
//                        did = alwaysShow.get(0);
//                    } else {
//                        did = 0;
//                    }
//                    getUndoView().showWithAction(did, UndoView.ACTION_ADDED_TO_FOLDER, alwaysShow.size(), folder, null, null);
//                    hideActionMode(true);
//                    finishPreviewFragment();
//                });
//                linearLayout.addView(folderItem);
//            }
//            if (lastItem != null && backButtonAtTop) {
//                lastItem.updateSelectorBackground(false, true);
//            }
//            if (linearLayout.getChildCount() <= 0) {
//                hasFolders = false;
//            } else {
//                ActionBarPopupWindow.GapView gap = new ActionBarPopupWindow.GapView(getParentActivity(), getResourceProvider(), Theme.key_actionBarDefaultSubmenuSeparator);
//                gap.setTag(R.id.fit_width_tag, 1);
//                ActionBarMenuSubItem backItem = new ActionBarMenuSubItem(getParentActivity(), backButtonAtTop, !backButtonAtTop);
//                backItem.setTextAndIcon(LocaleController.getString("Back", R.string.Back), R.drawable.ic_ab_back);
//                backItem.setMinimumWidth(160);
//                backItem.setOnClickListener(e -> {
//                    if (previewMenu[0] != null) {
//                        previewMenu[0].getSwipeBack().closeForeground();
//                    }
//                });
//                if (backButtonAtTop) {
//                    foldersMenuView.addView(backItem);
//                    foldersMenuView.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));
//                    foldersMenuView.addView(scrollView);
//                } else {
//                    foldersMenuView.addView(scrollView);
//                    foldersMenuView.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));
//                    foldersMenuView.addView(backItem);
//                }
//            }
//        }

        int flags = ActionBarPopupWindow.ActionBarPopupWindowLayout.FLAG_SHOWN_FROM_BOTTOM;
//        if (hasFolders) {
//            flags |= ActionBarPopupWindow.ActionBarPopupWindowLayout.FLAG_USE_SWIPEBACK;
//        }

        final ChatActivity[] chatActivity = new ChatActivity[1];
        previewMenu[0] = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getParentActivity(), R.drawable.popup_fixed_alert2, getResourceProvider(), flags);

//        if (hasFolders) {
//            foldersMenu[0] = previewMenu[0].addViewToSwipeBack(foldersMenuView);
//            ActionBarMenuSubItem addToFolderItem = new ActionBarMenuSubItem(getParentActivity(), true, false);
//            addToFolderItem.setTextAndIcon(LocaleController.getString("FilterAddTo", R.string.FilterAddTo), R.drawable.msg_addfolder);
//            addToFolderItem.setMinimumWidth(160);
//            addToFolderItem.setOnClickListener(e ->
//                    previewMenu[0].getSwipeBack().openForeground(foldersMenu[0])
//            );
//            previewMenu[0].addView(addToFolderItem);
//            previewMenu[0].getSwipeBack().setOnHeightUpdateListener(height -> {
//                if (chatActivity[0] == null || chatActivity[0].getFragmentView() == null) {
//                    return;
//                }
//                ViewGroup.LayoutParams lp = chatActivity[0].getFragmentView().getLayoutParams();
//                if (lp instanceof ViewGroup.MarginLayoutParams) {
//                    ((ViewGroup.MarginLayoutParams) lp).bottomMargin = AndroidUtilities.dp(24 + 16 + 8) + height;
//                    chatActivity[0].getFragmentView().setLayoutParams(lp);
//                }
//            });
//        }

        ActionBarMenuSubItem markAsUnreadItem = new ActionBarMenuSubItem(getParentActivity(), true, false);
        if (cell.getHasUnread()) {
            markAsUnreadItem.setTextAndIcon(LocaleController.getString("MarkAsRead", R.string.MarkAsRead), R.drawable.msg_markread);
        } else {
            markAsUnreadItem.setTextAndIcon(LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread), R.drawable.msg_markunread);
        }
        markAsUnreadItem.setMinimumWidth(160);
        markAsUnreadItem.setOnClickListener(e -> {
            if (cell.getHasUnread()) {
                markAsRead(dialogId);
            } else {
                markAsUnread(dialogId);
            }
            finishPreviewFragment();
        });
        previewMenu[0].addView(markAsUnreadItem);

        final boolean[] hasPinAction = new boolean[1];
        hasPinAction[0] = true;
        TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogId);
        boolean containsFilter;
        final MessagesController.DialogFilter filter = (
            (containsFilter = (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) && (!actionBar.isActionModeShowed() || actionBar.isActionModeShowed(null))) ?
                getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0] : null
        );
        if (!isDialogPinned(dialog)) {
            int pinnedCount = 0;
            int pinnedSecretCount = 0;
            int newPinnedCount = 0;
            int newPinnedSecretCount = 0;
            ArrayList<TLRPC.Dialog> dialogs = getMessagesController().getDialogs(folderId);
            for (int a = 0, N = dialogs.size(); a < N; a++) {
                TLRPC.Dialog dialog1 = dialogs.get(a);
                if (dialog1 instanceof TLRPC.TL_dialogFolder) {
                    continue;
                }
                if (isDialogPinned(dialog1)) {
                    if (DialogObject.isEncryptedDialog(dialog1.id)) {
                        pinnedSecretCount++;
                    } else {
                        pinnedCount++;
                    }
                } else if (!getMessagesController().isPromoDialog(dialog1.id, false)) {
                    break;
                }
            }
            int alreadyAdded = 0;
            if (dialog != null && !isDialogPinned(dialog)) {
                if (DialogObject.isEncryptedDialog(dialogId)) {
                    newPinnedSecretCount++;
                } else {
                    newPinnedCount++;
                }
                if (filter != null && filter.alwaysShow.contains(dialogId)) {
                    alreadyAdded++;
                }
            }
            int maxPinnedCount;
            if (containsFilter && filter != null) {
                maxPinnedCount = 100 - filter.alwaysShow.size();
            } else if (folderId != 0 || filter != null) {
                maxPinnedCount = getMessagesController().maxFolderPinnedDialogsCount;
            } else {
                maxPinnedCount = getMessagesController().maxPinnedDialogsCount;
            }
            hasPinAction[0] = !(newPinnedSecretCount + pinnedSecretCount > maxPinnedCount || newPinnedCount + pinnedCount - alreadyAdded > maxPinnedCount);
        }

        if (hasPinAction[0]) {
            ActionBarMenuSubItem unpinItem = new ActionBarMenuSubItem(getParentActivity(), false, false);
            if (isDialogPinned(dialog)) {
                unpinItem.setTextAndIcon(LocaleController.getString("UnpinMessage", R.string.UnpinMessage), R.drawable.msg_unpin);
            } else {
                unpinItem.setTextAndIcon(LocaleController.getString("PinMessage", R.string.PinMessage), R.drawable.msg_pin);
            }
            unpinItem.setMinimumWidth(160);
            unpinItem.setOnClickListener(e -> {
                finishPreviewFragment();
                AndroidUtilities.runOnUIThread(() -> {
                    int minPinnedNum = Integer.MAX_VALUE;
                    if (filter != null && isDialogPinned(dialog)) {
                        for (int c = 0, N = filter.pinnedDialogs.size(); c < N; c++) {
                            minPinnedNum = Math.min(minPinnedNum, filter.pinnedDialogs.valueAt(c));
                        }
                        minPinnedNum -= canPinCount;
                    }
                    TLRPC.EncryptedChat encryptedChat = null;
                    if (DialogObject.isEncryptedDialog(dialogId)) {
                        encryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
                    }
                    if (!isDialogPinned(dialog)) {
                        pinDialog(dialogId, true, filter, minPinnedNum, true);
                        getUndoView().showWithAction(0, UndoView.ACTION_PIN_DIALOGS, 1, 1600, null, null);
                        if (filter != null) {
                            if (encryptedChat != null) {
                                if (!filter.alwaysShow.contains(encryptedChat.user_id)) {
                                    filter.alwaysShow.add(encryptedChat.user_id);
                                }
                            } else {
                                if (!filter.alwaysShow.contains(dialogId)) {
                                    filter.alwaysShow.add(dialogId);
                                }
                            }
                        }
                    } else {
                        pinDialog(dialogId, false, filter, minPinnedNum, true);
                        getUndoView().showWithAction(0, UndoView.ACTION_UNPIN_DIALOGS, 1, 1600, null, null);
                    }
                    if (filter != null) {
                        FilterCreateActivity.saveFilterToServer(filter, filter.flags, filter.emoticon, filter.name, filter.alwaysShow, filter.neverShow, filter.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, null);
                    }
                    getMessagesController().reorderPinnedDialogs(folderId, null, 0);
                    updateCounters(true);
                    if (viewPages != null) {
                        for (int a = 0; a < viewPages.length; a++) {
                            viewPages[a].dialogsAdapter.onReorderStateChanged(false);
                        }
                    }
                    updateVisibleRows(MessagesController.UPDATE_MASK_REORDER | MessagesController.UPDATE_MASK_CHECK);

                }, 100);
            });
            previewMenu[0].addView(unpinItem);
        }

        if (!DialogObject.isUserDialog(dialogId) || !UserObject.isUserSelf(getMessagesController().getUser(dialogId))) {
            ActionBarMenuSubItem muteItem = new ActionBarMenuSubItem(getParentActivity(), false, false);
            if (!getMessagesController().isDialogMuted(dialogId, 0)) {
                muteItem.setTextAndIcon(LocaleController.getString("Mute", R.string.Mute), R.drawable.msg_mute);
            } else {
                muteItem.setTextAndIcon(LocaleController.getString("Unmute", R.string.Unmute), R.drawable.msg_unmute);
            }
            muteItem.setMinimumWidth(160);
            muteItem.setOnClickListener(e -> {
                boolean isMuted = getMessagesController().isDialogMuted(dialogId, 0);
                if (!isMuted) {
                    getNotificationsController().setDialogNotificationsSettings(dialogId, 0, NotificationsController.SETTING_MUTE_FOREVER);
                } else {
                    getNotificationsController().setDialogNotificationsSettings(dialogId, 0, NotificationsController.SETTING_MUTE_UNMUTE);
                }
                BulletinFactory.createMuteBulletin(this, !isMuted, null).show();
                finishPreviewFragment();
            });
            previewMenu[0].addView(muteItem);
        }

        ActionBarMenuSubItem deleteItem = new ActionBarMenuSubItem(getParentActivity(), false, true);
        deleteItem.setIconColor(getThemedColor(Theme.key_dialogRedIcon));
        deleteItem.setTextColor(getThemedColor(Theme.key_dialogTextRed));
        deleteItem.setTextAndIcon(LocaleController.getString("Delete", R.string.Delete), R.drawable.msg_delete);
        deleteItem.setMinimumWidth(160);
        deleteItem.setOnClickListener(e -> {
            performSelectedDialogsAction(dialogIdArray, delete, false, false);
            finishPreviewFragment();
        });
        previewMenu[0].addView(deleteItem);

        if (getMessagesController().checkCanOpenChat(args, DialogsActivity.this)) {
            if (searchString != null) {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats);
            }
            prepareBlurBitmap();
            parentLayout.setHighlightActionButtons(true);
            if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
                presentFragmentAsPreview(chatActivity[0] = new ChatActivity(args));
            } else {
                presentFragmentAsPreviewWithMenu(chatActivity[0] = new ChatActivity(args), previewMenu[0]);
                if (chatActivity[0] != null) {
                    chatActivity[0].allowExpandPreviewByClick = true;
                    try {
                        chatActivity[0].getAvatarContainer().getAvatarImageView().performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                    } catch (Exception ignore) {}
                }
            }
            return true;
        }
        return false;
    }

    private void updateFloatingButtonOffset() {
        floatingButtonContainer.setTranslationY(floatingButtonTranslation - floatingButtonPanOffset - Math.max(additionalFloatingTranslation, additionalFloatingTranslation2) * (1f - floatingButtonHideProgress));
    }

    private boolean hasHiddenArchive() {
        return !onlySelect && initialDialogsType == 0 && folderId == 0 && getMessagesController().hasHiddenArchive();
    }

    private boolean waitingForDialogsAnimationEnd(ViewPage viewPage) {
        return viewPage.dialogsItemAnimator.isRunning();
    }

    private void checkAnimationFinished() {
        AndroidUtilities.runOnUIThread(() -> {
            if (viewPages != null && folderId != 0 && (frozenDialogsList == null || frozenDialogsList.isEmpty())) {
                for (int a = 0; a < viewPages.length; a++) {
                    viewPages[a].listView.setEmptyView(null);
                    viewPages[a].progressView.setVisibility(View.INVISIBLE);
                }
                finishFragment();
            }
            setDialogsListFrozen(false);
            updateDialogIndices();
        }, 300);
    }

    private void setScrollY(float value) {
        if (fragmentView == null) {
            return;
        }
        if (scrimView != null) {
            scrimView.getLocationInWindow(scrimViewLocation);
        }
        actionBar.setTranslationY(value);
        if (topBulletin != null) {
            topBulletin.updatePosition();
        }
        if (filterTabsView != null) {
            filterTabsView.setTranslationY(value);
        }
        if (animatedStatusView != null) {
            animatedStatusView.translateY2((int) value);
            animatedStatusView.setAlpha(1f - -value / ActionBar.getCurrentActionBarHeight());
        }
        updateContextViewPosition();
        if (viewPages != null) {
            for (int a = 0; a < viewPages.length; a++) {
                viewPages[a].listView.setTopGlowOffset(viewPages[a].listView.getPaddingTop() + (int) value);
            }
        }
        fragmentView.invalidate();
    }

    private void prepareBlurBitmap() {
        if (blurredView == null) {
            return;
        }
        int w = (int) (fragmentView.getMeasuredWidth() / 6.0f);
        int h = (int) (fragmentView.getMeasuredHeight() / 6.0f);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1.0f / 6.0f, 1.0f / 6.0f);
        fragmentView.draw(canvas);
        Utilities.stackBlurBitmap(bitmap, Math.max(7, Math.max(w, h) / 180));
        blurredView.setBackground(new BitmapDrawable(bitmap));
        blurredView.setAlpha(0.0f);
        blurredView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTransitionAnimationProgress(boolean isOpen, float progress) {
        if (blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            if (isOpen) {
                blurredView.setAlpha(1.0f - progress);
            } else {
                blurredView.setAlpha(progress);
            }
        }
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && blurredView != null && blurredView.getVisibility() == View.VISIBLE) {
            blurredView.setVisibility(View.GONE);
            blurredView.setBackground(null);
        }
        if (isOpen && afterSignup) {
            if (!NekoConfig.disableVibration.Bool()) {
                try {
                    fragmentView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                } catch (Exception ignored) {}
            }
            if (getParentActivity() instanceof LaunchActivity) {
                ((LaunchActivity) getParentActivity()).getFireworksOverlay().start();
            }
        }
    }

    private void resetScroll() {
        if (actionBar.getTranslationY() == 0) {
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(this, SCROLL_Y, 0));
        animatorSet.setInterpolator(CubicBezierInterpolator.DEFAULT);
        animatorSet.setDuration(250);
        animatorSet.start();
    }

    private void hideActionMode(boolean animateCheck) {
        actionBar.hideActionMode();
        if (menuDrawable != null) {
            actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrOpenMenu", R.string.AccDescrOpenMenu));
        }
        selectedDialogs.clear();
        if (menuDrawable != null) {
            menuDrawable.setRotation(0, true);
        } else if (backDrawable != null) {
            backDrawable.setRotation(0, true);
        }
        if (filterTabsView != null) {
            filterTabsView.animateColorsTo(Theme.key_actionBarTabLine, Theme.key_actionBarTabActiveText, Theme.key_actionBarTabUnactiveText, Theme.key_actionBarTabSelector, Theme.key_actionBarDefault);
        }
        if (actionBarColorAnimator != null) {
            actionBarColorAnimator.cancel();
        }
        actionBarColorAnimator = ValueAnimator.ofFloat(progressToActionMode, 0);
        actionBarColorAnimator.addUpdateListener(valueAnimator -> {
            progressToActionMode = (float) valueAnimator.getAnimatedValue();
            for (int i = 0; i < actionBar.getChildCount(); i++) {
                if (actionBar.getChildAt(i).getVisibility() == View.VISIBLE && actionBar.getChildAt(i) != actionBar.getActionMode() && actionBar.getChildAt(i) != actionBar.getBackButton()) {
                    actionBar.getChildAt(i).setAlpha(1f - progressToActionMode);
                }
            }
            if (fragmentView != null) {
                fragmentView.invalidate();
            }
        });
        actionBarColorAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        actionBarColorAnimator.setDuration(200);
        actionBarColorAnimator.start();
        allowMoving = false;
        if (!movingDialogFilters.isEmpty()) {
            for (int a = 0, N = movingDialogFilters.size(); a < N; a++) {
                MessagesController.DialogFilter filter = movingDialogFilters.get(a);
                FilterCreateActivity.saveFilterToServer(filter, filter.flags, filter.emoticon, filter.name, filter.alwaysShow, filter.neverShow, filter.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, null);
            }
            movingDialogFilters.clear();
        }
        if (movingWas) {
            getMessagesController().reorderPinnedDialogs(folderId, null, 0);
            movingWas = false;
        }
        updateCounters(true);
        if (viewPages != null) {
            for (int a = 0; a < viewPages.length; a++) {
                viewPages[a].dialogsAdapter.onReorderStateChanged(false);
            }
        }
        updateVisibleRows(MessagesController.UPDATE_MASK_REORDER | MessagesController.UPDATE_MASK_CHECK | (animateCheck ? MessagesController.UPDATE_MASK_CHAT : 0));
    }

    private int getPinnedCount() {
        int pinnedCount = 0;
        ArrayList<TLRPC.Dialog> dialogs;
        boolean containsFilter = (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) && (!actionBar.isActionModeShowed() || actionBar.isActionModeShowed(null));
        if (containsFilter) {
            dialogs = getDialogsArray(currentAccount, viewPages[0].dialogsType, folderId, dialogsListFrozen);
        } else {
            dialogs = getMessagesController().getDialogs(folderId);
        }
        for (int a = 0, N = dialogs.size(); a < N; a++) {
            TLRPC.Dialog dialog = dialogs.get(a);
            if (dialog instanceof TLRPC.TL_dialogFolder) {
                continue;
            }
            if (isDialogPinned(dialog)) {
                pinnedCount++;
            } else if (!getMessagesController().isPromoDialog(dialog.id, false)) {
                break;
            }
        }
        return pinnedCount;
    }

    private boolean isDialogPinned(TLRPC.Dialog dialog) {
        if (dialog == null) {
            return false;
        }
        MessagesController.DialogFilter filter;
        boolean containsFilter = (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) && (!actionBar.isActionModeShowed() || actionBar.isActionModeShowed(null));
        if (containsFilter) {
            filter = getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0];
        } else {
            filter = null;
        }
        if (filter != null) {
            return filter.pinnedDialogs.indexOfKey(dialog.id) >= 0;
        }
        return dialog.pinned;
    }

    private void performSelectedDialogsAction(ArrayList<Long> selectedDialogs, int action, boolean alert, boolean longPress) {
        if (getParentActivity() == null) {
            return;
        }
        MessagesController.DialogFilter filter;
        boolean containsFilter = (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) && (!actionBar.isActionModeShowed() || actionBar.isActionModeShowed(null));
        if (containsFilter) {
            filter = getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0];
        } else {
            filter = null;
        }
        int count = selectedDialogs.size();
        int pinnedActionCount = 0;
        if (action == archive || action == archive2) {
            ArrayList<Long> copy = new ArrayList<>(selectedDialogs);
            getMessagesController().addDialogToFolder(copy, canUnarchiveCount == 0 ? 1 : 0, -1, null, 0);
            if (canUnarchiveCount == 0) {
                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                boolean hintShowed = preferences.getBoolean("archivehint_l", false) || SharedConfig.archiveHidden;
                if (!hintShowed) {
                    preferences.edit().putBoolean("archivehint_l", true).apply();
                }
                int undoAction;
                if (hintShowed) {
                    undoAction = copy.size() > 1 ? UndoView.ACTION_ARCHIVE_FEW : UndoView.ACTION_ARCHIVE;
                } else {
                    undoAction = copy.size() > 1 ? UndoView.ACTION_ARCHIVE_FEW_HINT : UndoView.ACTION_ARCHIVE_HINT;
                }
                getUndoView().showWithAction(0, undoAction, null, () -> getMessagesController().addDialogToFolder(copy, folderId == 0 ? 0 : 1, -1, null, 0));
            } else {
                ArrayList<TLRPC.Dialog> dialogs = getMessagesController().getDialogs(folderId);
                if (viewPages != null && dialogs.isEmpty()) {
                    viewPages[0].listView.setEmptyView(null);
                    viewPages[0].progressView.setVisibility(View.INVISIBLE);
                    finishFragment();
                }
            }
            hideActionMode(false);
            return;
        } else if ((action == pin || action == pin2) && canPinCount != 0) {
            int pinnedCount = 0;
            int pinnedSecretCount = 0;
            int newPinnedCount = 0;
            int newPinnedSecretCount = 0;
            ArrayList<TLRPC.Dialog> dialogs = getMessagesController().getDialogs(folderId);
            for (int a = 0, N = dialogs.size(); a < N; a++) {
                TLRPC.Dialog dialog = dialogs.get(a);
                if (dialog instanceof TLRPC.TL_dialogFolder) {
                    continue;
                }
                if (isDialogPinned(dialog)) {
                    if (DialogObject.isEncryptedDialog(dialog.id)) {
                        pinnedSecretCount++;
                    } else {
                        pinnedCount++;
                    }
                } else if (!getMessagesController().isPromoDialog(dialog.id, false)) {
                    break;
                }
            }
            int alreadyAdded = 0;
            for (int a = 0; a < count; a++) {
                long selectedDialog = selectedDialogs.get(a);
                TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(selectedDialog);
                if (dialog == null || isDialogPinned(dialog)) {
                    continue;
                }
                if (DialogObject.isEncryptedDialog(selectedDialog)) {
                    newPinnedSecretCount++;
                } else {
                    newPinnedCount++;
                }
                if (filter != null && filter.alwaysShow.contains(selectedDialog)) {
                    alreadyAdded++;
                }
            }
            int maxPinnedCount;
            if (containsFilter) {
                maxPinnedCount = 100 - filter.alwaysShow.size();
            } else if (NekoConfig.unlimitedPinnedDialogs.Bool() || folderId != 0 || filter != null) {
                maxPinnedCount = getMessagesController().maxFolderPinnedDialogsCount;
            } else {
                maxPinnedCount = getUserConfig().isPremium() ? getMessagesController().dialogFiltersPinnedLimitPremium : getMessagesController().dialogFiltersPinnedLimitDefault;
            }
            if (newPinnedSecretCount + pinnedSecretCount > maxPinnedCount || newPinnedCount + pinnedCount - alreadyAdded > maxPinnedCount) {
                if (folderId != 0 || filter != null) {
                    AlertsCreator.showSimpleAlert(DialogsActivity.this, LocaleController.formatString("PinFolderLimitReached", R.string.PinFolderLimitReached, LocaleController.formatPluralString("Chats", maxPinnedCount)));
                } else {
                    LimitReachedBottomSheet limitReachedBottomSheet = new LimitReachedBottomSheet(this, getParentActivity(), LimitReachedBottomSheet.TYPE_PIN_DIALOGS, currentAccount);
                    showDialog(limitReachedBottomSheet);
                }
                return;
            }
        } else if ((action == delete || action == clear) && count > 1 && alert) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            if (action == delete) {
                builder.setTitle(LocaleController.formatString("DeleteFewChatsTitle", R.string.DeleteFewChatsTitle, LocaleController.formatPluralString("ChatsSelected", count)));
                builder.setMessage(LocaleController.getString("AreYouSureDeleteFewChats", R.string.AreYouSureDeleteFewChats));
            } else {
                if (canClearCacheCount != 0) {
                    builder.setTitle(LocaleController.formatString("ClearCacheFewChatsTitle", R.string.ClearCacheFewChatsTitle, LocaleController.formatPluralString("ChatsSelectedClearCache", count)));
                    builder.setMessage(LocaleController.getString("AreYouSureClearHistoryCacheFewChats", R.string.AreYouSureClearHistoryCacheFewChats));
                } else {
                    builder.setTitle(LocaleController.formatString("ClearFewChatsTitle", R.string.ClearFewChatsTitle, LocaleController.formatPluralString("ChatsSelectedClear", count)));
                    builder.setMessage(LocaleController.getString("AreYouSureClearHistoryFewChats", R.string.AreYouSureClearHistoryFewChats));
                }
            }
            builder.setPositiveButton(action == delete ? LocaleController.getString("Delete", R.string.Delete)
                    : canClearCacheCount != 0 ? LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache)
                    : LocaleController.getString("ClearHistory", R.string.ClearHistory), (dialog1, which) -> {
                if (selectedDialogs.isEmpty()) {
                    return;
                }
                ArrayList<Long> didsCopy = new ArrayList<>(selectedDialogs);
                getUndoView().showWithAction(didsCopy, action == delete ? UndoView.ACTION_DELETE_FEW : UndoView.ACTION_CLEAR_FEW, null, null, () -> {
                    if (action == delete) {
                        getMessagesController().setDialogsInTransaction(true);
                        performSelectedDialogsAction(didsCopy, action, false, false);
                        getMessagesController().setDialogsInTransaction(false);
                        getMessagesController().checkIfFolderEmpty(folderId);
                        if (folderId != 0 && getDialogsArray(currentAccount, viewPages[0].dialogsType, folderId, false).size() == 0) {
                            viewPages[0].listView.setEmptyView(null);
                            viewPages[0].progressView.setVisibility(View.INVISIBLE);
                            finishFragment();
                        }
                    } else {
                        performSelectedDialogsAction(didsCopy, action, false, false);
                    }
                }, null);
                hideActionMode(action == clear);
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            AlertDialog alertDialog = builder.create();
            showDialog(alertDialog);
            TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (button != null) {
                button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
            }
            return;
        } else if (action == block && alert) {
            TLRPC.User user;
            if (count == 1) {
                long did = selectedDialogs.get(0);
                user = getMessagesController().getUser(did);
            } else {
                user = null;
            }
            AlertsCreator.createBlockDialogAlert(DialogsActivity.this, count, canReportSpamCount != 0, user, (report, delete) -> {
                for (int a = 0, N = selectedDialogs.size(); a < N; a++) {
                    long did = selectedDialogs.get(a);
                    if (report) {
                        TLRPC.User u = getMessagesController().getUser(did);
                        getMessagesController().reportSpam(did, u, null, null, false);
                    }
                    if (delete) {
                        getMessagesController().deleteDialog(did, 0, true);
                    }
                    getMessagesController().blockPeer(did);
                }
                hideActionMode(false);
            });
            return;
        }
        int minPinnedNum = Integer.MAX_VALUE;
        if (filter != null && (action == pin || action == pin2) && canPinCount != 0) {
            for (int c = 0, N = filter.pinnedDialogs.size(); c < N; c++) {
                minPinnedNum = Math.min(minPinnedNum, filter.pinnedDialogs.valueAt(c));
            }
            minPinnedNum -= canPinCount;
        }
        boolean scrollToTop = false;
        for (int a = 0; a < count; a++) {
            long selectedDialog = selectedDialogs.get(a);
            TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(selectedDialog);
            if (dialog == null) {
                continue;
            }
            TLRPC.Chat chat;
            TLRPC.User user = null;

            TLRPC.EncryptedChat encryptedChat = null;
            if (DialogObject.isEncryptedDialog(selectedDialog)) {
                encryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(selectedDialog));
                chat = null;
                if (encryptedChat != null) {
                    user = getMessagesController().getUser(encryptedChat.user_id);
                } else {
                    user = new TLRPC.TL_userEmpty();
                }
            } else if (DialogObject.isUserDialog(selectedDialog)) {
                user = getMessagesController().getUser(selectedDialog);
                chat = null;
            } else {
                chat = getMessagesController().getChat(-selectedDialog);
            }
            if (chat == null && user == null) {
                continue;
            }
            boolean isBot = user != null && user.bot && !MessagesController.isSupportUser(user);
            if (action == pin || action == pin2) {
                if (canPinCount != 0) {
                    if (isDialogPinned(dialog)) {
                        continue;
                    }
                    pinnedActionCount++;
                    pinDialog(selectedDialog, true, filter, minPinnedNum, count == 1);
                    if (filter != null) {
                        minPinnedNum++;
                        if (encryptedChat != null) {
                            if (!filter.alwaysShow.contains(encryptedChat.user_id)) {
                                filter.alwaysShow.add(encryptedChat.user_id);
                            }
                        } else {
                            if (!filter.alwaysShow.contains(dialog.id)) {
                                filter.alwaysShow.add(dialog.id);
                            }
                        }
                    }
                } else {
                    if (!isDialogPinned(dialog)) {
                        continue;
                    }
                    pinnedActionCount++;
                    pinDialog(selectedDialog, false, filter, minPinnedNum, count == 1);
                }
            } else if (action == read) {
                if (canReadCount != 0) {
                    markAsRead(selectedDialog);
                } else {
                    markAsUnread(selectedDialog);
                }
            } else if (action == delete || action == clear) {
                if (count == 1) {
                    if (action == delete && canDeletePsaSelected) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("PsaHideChatAlertTitle", R.string.PsaHideChatAlertTitle));
                        builder.setMessage(LocaleController.getString("PsaHideChatAlertText", R.string.PsaHideChatAlertText));
                        builder.setPositiveButton(LocaleController.getString("PsaHide", R.string.PsaHide), (dialog1, which) -> {
                            getMessagesController().hidePromoDialog();
                            hideActionMode(false);
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else {
                        AlertsCreator.createClearOrDeleteDialogAlert(DialogsActivity.this, action == clear, chat, user, DialogObject.isEncryptedDialog(dialog.id), action == delete, (param) -> {
                            hideActionMode(false);
                            if (action == clear && ChatObject.isChannel(chat) && (!chat.megagroup || ChatObject.isPublic(chat))) {
                                getMessagesController().deleteDialog(selectedDialog, 2, param);
                            } else {
                                if (action == delete && folderId != 0 && getDialogsArray(currentAccount, viewPages[0].dialogsType, folderId, false).size() == 1) {
                                    viewPages[0].progressView.setVisibility(View.INVISIBLE);
                                }

                                debugLastUpdateAction = 3;
                                int selectedDialogIndex = -1;
                                if (action == delete) {
                                    setDialogsListFrozen(true);
                                    if (frozenDialogsList != null) {
                                        for (int i = 0; i < frozenDialogsList.size(); i++) {
                                            if (frozenDialogsList.get(i).id == selectedDialog) {
                                                selectedDialogIndex = i;
                                                break;
                                            }
                                        }
                                    }
                                    checkAnimationFinished();
                                }

                                getUndoView().showWithAction(selectedDialog, action == clear ? UndoView.ACTION_CLEAR : UndoView.ACTION_DELETE, () -> performDeleteOrClearDialogAction(action, selectedDialog, chat, isBot, param));

                                ArrayList<TLRPC.Dialog> currentDialogs = new ArrayList<>(getDialogsArray(currentAccount, viewPages[0].dialogsType, folderId, false));
                                int currentDialogIndex = -1;
                                for (int i = 0; i < currentDialogs.size(); i++) {
                                    if (currentDialogs.get(i).id == selectedDialog) {
                                        currentDialogIndex = i;
                                        break;
                                    }
                                }

                                if (action == delete) {
                                    if (selectedDialogIndex >= 0 && currentDialogIndex < 0 && frozenDialogsList != null) {
                                        frozenDialogsList.remove(selectedDialogIndex);
                                        viewPages[0].dialogsItemAnimator.prepareForRemove();
                                        viewPages[0].updateList(true);
                                    } else {
                                        setDialogsListFrozen(false);
                                    }
                                }
                            }
                        });
                    }
                    return;
                } else {
                    if (getMessagesController().isPromoDialog(selectedDialog, true)) {
                        getMessagesController().hidePromoDialog();
                    } else {
                        if (action == clear && canClearCacheCount != 0) {
                            getMessagesController().deleteDialog(selectedDialog, 2, false);
                        } else {
                            performDeleteOrClearDialogAction(action, selectedDialog, chat, isBot, false);
                        }
                    }
                }
            } else if (action == mute) {
                if (count == 1 && canMuteCount == 1) {
                    showDialog(AlertsCreator.createMuteAlert(this, selectedDialog, 0,null), dialog12 -> hideActionMode(true));
                    return;
                } else {
                    if (canUnmuteCount != 0) {
                        if (!getMessagesController().isDialogMuted(selectedDialog, 0)) {
                            continue;
                        }
                        getNotificationsController().setDialogNotificationsSettings(selectedDialog, 0, NotificationsController.SETTING_MUTE_UNMUTE);
                    } else if (longPress) {
                        showDialog(AlertsCreator.createMuteAlert(this, selectedDialogs, 0,null), dialog12 -> hideActionMode(true));
                        return;
                    } else {
                        if (getMessagesController().isDialogMuted(selectedDialog, 0)) {
                            continue;
                        }
                        getNotificationsController().setDialogNotificationsSettings(selectedDialog, 0, NotificationsController.SETTING_MUTE_FOREVER);
                    }
                }
            }
        }
        if (action == mute && !(count == 1 && canMuteCount == 1)) {
            BulletinFactory.createMuteBulletin(this, canUnmuteCount == 0, null).show();
        }
        if (action == pin || action == pin2) {
            if (filter != null) {
                FilterCreateActivity.saveFilterToServer(filter, filter.flags, filter.emoticon, filter.name, filter.alwaysShow, filter.neverShow, filter.pinnedDialogs, false, false, true, true, false, DialogsActivity.this, null);
            } else {
                getMessagesController().reorderPinnedDialogs(folderId, null, 0);
            }
            if (searchIsShowed) {
                getUndoView().showWithAction(0, canPinCount != 0 ? UndoView.ACTION_PIN_DIALOGS : UndoView.ACTION_UNPIN_DIALOGS, pinnedActionCount);
            }
        }
        if (scrollToTop) {
            if (initialDialogsType != 10) {
                hideFloatingButton(false);
            }
            scrollToTop();
        }
        hideActionMode(action != pin2 && action != pin && action != delete);
    }

    private void markAsRead(long did) {
        TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(did);
        MessagesController.DialogFilter filter;
        boolean containsFilter = (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) && (!actionBar.isActionModeShowed() || actionBar.isActionModeShowed(null));
        if (containsFilter) {
            filter = getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0];
        } else {
            filter = null;
        }
        debugLastUpdateAction = 2;
        int selectedDialogIndex = -1;
        if (filter != null && (filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ) != 0 && !filter.alwaysShow(currentAccount, dialog)) {
            setDialogsListFrozen(true);
            checkAnimationFinished();
            if (frozenDialogsList != null) {
                for (int i = 0; i < frozenDialogsList.size(); i++) {
                    if (frozenDialogsList.get(i).id == did) {
                        selectedDialogIndex = i;
                        break;
                    }
                }
                if (selectedDialogIndex < 0) {
                    setDialogsListFrozen(false, false);
                }
            }
        }
        if (getMessagesController().isForum(did)) {
            getMessagesController().markAllTopicsAsRead(did);
        }

        getMessagesController().markMentionsAsRead(did, 0);
        getMessagesController().markDialogAsRead(did, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, 0, true, 0);

        if (selectedDialogIndex >= 0) {
            frozenDialogsList.remove(selectedDialogIndex);
            viewPages[0].dialogsItemAnimator.prepareForRemove();
            viewPages[0].updateList(true);
        }
    }

    private void markAsUnread(long did) {
        getMessagesController().markDialogAsUnread(did, null, 0);
    }

    private void performDeleteOrClearDialogAction(int action, long selectedDialog, TLRPC.Chat chat, boolean isBot, boolean revoke) {
        if (action == clear) {
            getMessagesController().deleteDialog(selectedDialog, 1, revoke);
        } else {
            if (chat != null) {
                if (ChatObject.isNotInChat(chat)) {
                    getMessagesController().deleteDialog(selectedDialog, 0, revoke);
                } else {
                    TLRPC.User currentUser = getMessagesController().getUser(getUserConfig().getClientUserId());
                    getMessagesController().deleteParticipantFromChat((int) -selectedDialog, currentUser, null, revoke, false);
                }
            } else {
                getMessagesController().deleteDialog(selectedDialog, 0, revoke);
                if (isBot) {
                    getMessagesController().blockPeer((int) selectedDialog);
                }
            }
            if (AndroidUtilities.isTablet()) {
                getNotificationCenter().postNotificationName(NotificationCenter.closeChats, selectedDialog);
            }
            getMessagesController().checkIfFolderEmpty(folderId);
        }
    }

    private void pinDialog(long selectedDialog, boolean pin, MessagesController.DialogFilter filter, int minPinnedNum, boolean animated) {

        int selectedDialogIndex = -1;
        int currentDialogIndex = -1;

        int scrollToPosition = viewPages[0].dialogsType == 0 && hasHiddenArchive() && viewPages[0].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN ? 1 : 0;
        int currentPosition = viewPages[0].layoutManager.findFirstVisibleItemPosition();

        if (filter != null) {
            int index = filter.pinnedDialogs.get(selectedDialog, Integer.MIN_VALUE);
            if (!pin && index == Integer.MIN_VALUE) {
                return;
            }

        }

        debugLastUpdateAction = pin ? 4 : 5;
        boolean needScroll = false;
        if (currentPosition > scrollToPosition || !animated) {
            needScroll = true;
        } else {
            setDialogsListFrozen(true);
            checkAnimationFinished();
            if (frozenDialogsList != null) {
                for (int i = 0; i < frozenDialogsList.size(); i++) {
                    if (frozenDialogsList.get(i).id == selectedDialog) {
                        selectedDialogIndex = i;
                        break;
                    }
                }
            }
        }

        boolean updated;
        if (filter != null) {
            if (pin) {
                filter.pinnedDialogs.put(selectedDialog, minPinnedNum);
            } else {
                filter.pinnedDialogs.delete(selectedDialog);
            }

            if (animated) {
                getMessagesController().onFilterUpdate(filter);
            }
            updated = true;
        } else {
            updated = getMessagesController().pinDialog(selectedDialog, pin, null, -1);
        }


        if (updated) {
            if (needScroll) {
                if (initialDialogsType != 10) {
                    hideFloatingButton(false);
                }
                scrollToTop();
            } else {
                ArrayList<TLRPC.Dialog> currentDialogs = getDialogsArray(currentAccount, viewPages[0].dialogsType, folderId, false);
                for (int i = 0; i < currentDialogs.size(); i++) {
                    if (currentDialogs.get(i).id == selectedDialog) {
                        currentDialogIndex = i;
                        break;
                    }
                }
            }
        }

        if (!needScroll) {
            boolean animate = false;
            if (selectedDialogIndex >= 0) {
                if (frozenDialogsList != null && currentDialogIndex >= 0 && selectedDialogIndex != currentDialogIndex) {
                    frozenDialogsList.add(currentDialogIndex, frozenDialogsList.remove(selectedDialogIndex));
                    viewPages[0].dialogsItemAnimator.prepareForRemove();
                    viewPages[0].updateList(true);

                    viewPages[0].layoutManager.scrollToPositionWithOffset(viewPages[0].dialogsType == 0 && hasHiddenArchive() && viewPages[0].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN ? 1 : 0, (int) actionBar.getTranslationY());

                    animate = true;
                } else if (currentDialogIndex >= 0 && selectedDialogIndex == currentDialogIndex) {
                    animate = true;
                    AndroidUtilities.runOnUIThread(() -> setDialogsListFrozen(false), 200);
                }
            }
            if (!animate) {
                setDialogsListFrozen(false);
            }
        }
    }

    private void scrollToTop() {
        int scrollDistance = viewPages[0].layoutManager.findFirstVisibleItemPosition() * AndroidUtilities.dp(SharedConfig.useThreeLinesLayout ? 78 : 72);
        int position = viewPages[0].dialogsType == 0 && hasHiddenArchive() && viewPages[0].archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN ? 1 : 0;
        RecyclerView.ItemAnimator animator = viewPages[0].listView.getItemAnimator();
//        if (animator != null) {
//            animator.endAnimations();
//        }
        if (scrollDistance >= viewPages[0].listView.getMeasuredHeight() * 1.2f) {
            viewPages[0].scrollHelper.setScrollDirection(RecyclerAnimationScrollHelper.SCROLL_DIRECTION_UP);
            viewPages[0].scrollHelper.scrollToPosition(position, 0, false, true);
            resetScroll();
        } else {
            viewPages[0].listView.smoothScrollToPosition(position);
        }
    }

    private void updateCounters(boolean hide) {
        int canClearHistoryCount = 0;
        int canDeleteCount = 0;
        int canUnpinCount = 0;
        int canArchiveCount = 0;
        canDeletePsaSelected = false;
        canUnarchiveCount = 0;
        canUnmuteCount = 0;
        canMuteCount = 0;
        canPinCount = 0;
        canReadCount = 0;
        forumCount = 0;
        canClearCacheCount = 0;
        int cantBlockCount = 0;
        canReportSpamCount = 0;
        if (hide) {
            return;
        }
        int count = selectedDialogs.size();
        long selfUserId = getUserConfig().getClientUserId();
        SharedPreferences preferences = getNotificationsSettings();
        for (int a = 0; a < count; a++) {
            TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(selectedDialogs.get(a));
            if (dialog == null) {
                continue;
            }

            long selectedDialog = dialog.id;
            boolean pinned = isDialogPinned(dialog);
            boolean hasUnread = dialog.unread_count != 0 || dialog.unread_mark;

            if (getMessagesController().isForum(selectedDialog)) {
                forumCount++;
            }
            if (getMessagesController().isDialogMuted(selectedDialog, 0)) {
                canUnmuteCount++;
            } else {
                canMuteCount++;
            }

            if (hasUnread) {
                canReadCount++;
            }

            if (folderId == 1 || dialog.folder_id == 1) {
                canUnarchiveCount++;
            } else if (selectedDialog != selfUserId && selectedDialog != 777000 && !getMessagesController().isPromoDialog(selectedDialog, false)) {
                canArchiveCount++;
            }

            if (!DialogObject.isUserDialog(selectedDialog) || selectedDialog == selfUserId) {
                cantBlockCount++;
            } else {
                TLRPC.User user = getMessagesController().getUser(selectedDialog);
                if (MessagesController.isSupportUser(user)) {
                    cantBlockCount++;
                } else {
                    if (preferences.getBoolean("dialog_bar_report" + selectedDialog, true)) {
                        canReportSpamCount++;
                    }
                }
            }

            if (DialogObject.isChannel(dialog)) {
                final TLRPC.Chat chat = getMessagesController().getChat(-selectedDialog);
                CharSequence[] items;
                if (getMessagesController().isPromoDialog(dialog.id, true)) {
                    canClearCacheCount++;
                    if (getMessagesController().promoDialogType == MessagesController.PROMO_TYPE_PSA) {
                        canDeleteCount++;
                        canDeletePsaSelected = true;
                    }
                } else {
                    if (pinned) {
                        canUnpinCount++;
                    } else {
                        canPinCount++;
                    }
                    if (chat != null && chat.megagroup) {
                        if (!ChatObject.isPublic(chat)) {
                            canClearHistoryCount++;
                        } else {
                            canClearCacheCount++;
                        }
                    } else {
                        canClearCacheCount++;
                    }
                    canDeleteCount++;
                }
            } else {
                final boolean isChat = DialogObject.isChatDialog(dialog.id);
                TLRPC.User user;
                TLRPC.Chat chat = isChat ? getMessagesController().getChat(-dialog.id) : null;
                if (DialogObject.isEncryptedDialog(dialog.id)) {
                    TLRPC.EncryptedChat encryptedChat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialog.id));
                    if (encryptedChat != null) {
                        user = getMessagesController().getUser(encryptedChat.user_id);
                    } else {
                        user = new TLRPC.TL_userEmpty();
                    }
                } else {
                    user = !isChat && DialogObject.isUserDialog(dialog.id) ? getMessagesController().getUser(dialog.id) : null;
                }
                final boolean isBot = user != null && user.bot && !MessagesController.isSupportUser(user);

                if (pinned) {
                    canUnpinCount++;
                } else {
                    canPinCount++;
                }
                canClearHistoryCount++;
                canDeleteCount++;
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            TransitionSet transition = new TransitionSet();
//            transition.addTransition(new Visibility() {
//                @Override
//                public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
//                    AnimatorSet set = new AnimatorSet();
//                    set.playTogether(
//                            ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1f),
//                            ObjectAnimator.ofFloat(view, View.SCALE_X, 0.5f, 1f),
//                            ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.5f, 1f)
//                    );
//                    set.setInterpolator(CubicBezierInterpolator.DEFAULT);
//                    return set;
//                }
//
//                @Override
//                public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
//                    AnimatorSet set = new AnimatorSet();
//                    set.playTogether(
//                            ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), 0f),
//                            ObjectAnimator.ofFloat(view, View.SCALE_X, view.getScaleX(), 0.5f),
//                            ObjectAnimator.ofFloat(view, View.SCALE_Y, view.getScaleX(), 0.5f)
//                    );
//                    set.setInterpolator(CubicBezierInterpolator.DEFAULT);
//                    return set;
//                }
//            }).addTransition(new ChangeBounds());
//            transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
//            transition.setInterpolator(CubicBezierInterpolator.EASE_OUT);
//            transition.setDuration(150);
//            TransitionManager.beginDelayedTransition(actionBar.getActionMode(), transition);
//        }
        if (canDeleteCount != count) {
            deleteItem.setVisibility(View.GONE);
        } else {
            deleteItem.setVisibility(View.VISIBLE);
        }
        if (canClearCacheCount != 0 && canClearCacheCount != count || canClearHistoryCount != 0 && canClearHistoryCount != count) {
            clearItem.setVisibility(View.GONE);
        } else {
            clearItem.setVisibility(View.VISIBLE);
            if (canClearCacheCount != 0) {
                clearItem.setText(LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache));
            } else {
                clearItem.setText(LocaleController.getString("ClearHistory", R.string.ClearHistory));
            }
        }
        if (canUnarchiveCount != 0) {
            final String contentDescription = LocaleController.getString("Unarchive", R.string.Unarchive);
            archiveItem.setTextAndIcon(contentDescription, R.drawable.msg_unarchive);
            archive2Item.setIcon(R.drawable.msg_unarchive);
            archive2Item.setContentDescription(contentDescription);
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                archive2Item.setVisibility(View.VISIBLE);
                archiveItem.setVisibility(View.GONE);
            } else {
                archiveItem.setVisibility(View.VISIBLE);
                archive2Item.setVisibility(View.GONE);
            }
        } else if (canArchiveCount != 0) {
            final String contentDescription = LocaleController.getString("Archive", R.string.Archive);
            archiveItem.setTextAndIcon(contentDescription, R.drawable.msg_archive);
            archive2Item.setIcon(R.drawable.msg_archive);
            archive2Item.setContentDescription(contentDescription);
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                archive2Item.setVisibility(View.VISIBLE);
                archiveItem.setVisibility(View.GONE);
            } else {
                archiveItem.setVisibility(View.VISIBLE);
                archive2Item.setVisibility(View.GONE);
            }
        } else {
            archiveItem.setVisibility(View.GONE);
            archive2Item.setVisibility(View.GONE);
        }
        if (canPinCount + canUnpinCount != count) {
            pinItem.setVisibility(View.GONE);
            pin2Item.setVisibility(View.GONE);
        } else {
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                pin2Item.setVisibility(View.VISIBLE);
                pinItem.setVisibility(View.GONE);
            } else {
                pinItem.setVisibility(View.VISIBLE);
                pin2Item.setVisibility(View.GONE);
            }
        }
        if (cantBlockCount != 0) {
            blockItem.setVisibility(View.GONE);
        } else {
            blockItem.setVisibility(View.VISIBLE);
        }
        if (filterTabsView == null || filterTabsView.getVisibility() != View.VISIBLE || filterTabsView.currentTabIsDefault()) {
            removeFromFolderItem.setVisibility(View.GONE);
        } else {
            removeFromFolderItem.setVisibility(View.VISIBLE);
        }
        if (filterTabsView != null && ((searchTabsView != null && searchTabsView.getVisibility() == View.VISIBLE) || (filterTabsView.getVisibility() == View.VISIBLE && filterTabsView.currentTabIsDefault())) && !FiltersListBottomSheet.getCanAddDialogFilters(this, selectedDialogs).isEmpty()) {
            addToFolderItem.setVisibility(View.VISIBLE);
        } else {
            addToFolderItem.setVisibility(View.GONE);
        }
        if (canUnmuteCount != 0) {
            muteItem.setIcon(R.drawable.msg_unmute);
            muteItem.setContentDescription(LocaleController.getString("ChatsUnmute", R.string.ChatsUnmute));
        } else {
            muteItem.setIcon(R.drawable.msg_mute);
            muteItem.setContentDescription(LocaleController.getString("ChatsMute", R.string.ChatsMute));
        }
        if (canReadCount != 0) {
            readItem.setTextAndIcon(LocaleController.getString("MarkAsRead", R.string.MarkAsRead), R.drawable.msg_markread);
            readItem.setVisibility(View.VISIBLE);
        } else {
            if (forumCount == 0) {
                readItem.setTextAndIcon(LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread), R.drawable.msg_markunread);
                readItem.setVisibility(View.VISIBLE);
            } else {
                readItem.setVisibility(View.GONE);
            }
        }
        if (canPinCount != 0) {
            pinItem.setIcon(R.drawable.msg_pin);
            pinItem.setContentDescription(LocaleController.getString("PinToTop", R.string.PinToTop));
            pin2Item.setText(LocaleController.getString("DialogPin", R.string.DialogPin));
        } else {
            pinItem.setIcon(R.drawable.msg_unpin);
            pinItem.setContentDescription(LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop));
            pin2Item.setText(LocaleController.getString("DialogUnpin", R.string.DialogUnpin));
        }
    }

    private boolean validateSlowModeDialog(long dialogId) {
        if (messagesCount <= 1 && (commentView == null || commentView.getVisibility() != View.VISIBLE || TextUtils.isEmpty(commentView.getFieldText()))) {
            return true;
        }
        if (!DialogObject.isChatDialog(dialogId)) {
            return true;
        }
        TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
        if (chat != null && !ChatObject.hasAdminRights(chat) && chat.slowmode_enabled) {
            AlertsCreator.showSimpleAlert(DialogsActivity.this, LocaleController.getString("Slowmode", R.string.Slowmode), LocaleController.getString("SlowmodeSendError", R.string.SlowmodeSendError));
            return false;
        }
        return true;
    }

    private void showOrUpdateActionMode(long dialogId, View cell) {
        addOrRemoveSelectedDialog(dialogId, cell);
        boolean updateAnimated = false;
        if (actionBar.isActionModeShowed()) {
            if (selectedDialogs.isEmpty()) {
                hideActionMode(true);
                return;
            }
            updateAnimated = true;
        } else {
            if (searchIsShowed) {
                createActionMode(ACTION_MODE_SEARCH_DIALOGS_TAG);
                if (actionBar.getBackButton().getDrawable() instanceof MenuDrawable) {
                    actionBar.setBackButtonDrawable(new BackDrawable(false));
                }
            } else {
                createActionMode(null);
            }
            AndroidUtilities.hideKeyboard(fragmentView.findFocus());
            actionBar.setActionModeOverrideColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            actionBar.showActionMode();
            resetScroll();
            if (menuDrawable != null) {
                actionBar.setBackButtonContentDescription(LocaleController.getString("AccDescrGoBack", R.string.AccDescrGoBack));
            }
            if (getPinnedCount() > 1) {
                if (viewPages != null) {
                    for (int a = 0; a < viewPages.length; a++) {
                        viewPages[a].dialogsAdapter.onReorderStateChanged(true);
                    }
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_REORDER);
            }

            if (!searchIsShowed) {
                AnimatorSet animatorSet = new AnimatorSet();
                ArrayList<Animator> animators = new ArrayList<>();
                for (int a = 0; a < actionModeViews.size(); a++) {
                    View view = actionModeViews.get(a);
                    view.setPivotY(ActionBar.getCurrentActionBarHeight() / 2);
                    AndroidUtilities.clearDrawableAnimation(view);
                    animators.add(ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.1f, 1.0f));
                }
                animatorSet.playTogether(animators);
                animatorSet.setDuration(200);
                animatorSet.start();
            }

            if (actionBarColorAnimator != null) {
                actionBarColorAnimator.cancel();
            }
            actionBarColorAnimator = ValueAnimator.ofFloat(progressToActionMode, 1f);
            actionBarColorAnimator.addUpdateListener(valueAnimator -> {
                progressToActionMode = (float) valueAnimator.getAnimatedValue();
                for (int i = 0; i < actionBar.getChildCount(); i++) {
                    if (actionBar.getChildAt(i).getVisibility() == View.VISIBLE && actionBar.getChildAt(i) != actionBar.getActionMode() && actionBar.getChildAt(i) != actionBar.getBackButton()) {
                        actionBar.getChildAt(i).setAlpha(1f - progressToActionMode);
                    }
                }
                if (fragmentView != null) {
                    fragmentView.invalidate();
                }
            });
            actionBarColorAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
            actionBarColorAnimator.setDuration(200);
            actionBarColorAnimator.start();

            if (filterTabsView != null) {
                filterTabsView.animateColorsTo(Theme.key_profile_tabSelectedLine, Theme.key_profile_tabSelectedText, Theme.key_profile_tabText, Theme.key_profile_tabSelector, Theme.key_actionBarActionModeDefault);
            }
            if (menuDrawable != null) {
                menuDrawable.setRotateToBack(false);
                menuDrawable.setRotation(1, true);
            } else if (backDrawable != null) {
                backDrawable.setRotation(1, true);
            }
        }
        updateCounters(false);
        selectedDialogsCountTextView.setNumber(selectedDialogs.size(), updateAnimated);
    }

    private void closeSearch() {
        if (AndroidUtilities.isTablet()) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
            if (searchObject != null) {
                searchViewPager.dialogsSearchAdapter.putRecentSearch(searchDialogId, searchObject);
                searchObject = null;
            }
        } else {
            closeSearchFieldOnHide = true;
        }
    }

    protected RecyclerListView getListView() {
        return viewPages[0].listView;
    }

    protected RecyclerListView getSearchListView() {
        return searchViewPager.searchListView;
    }

    public UndoView getUndoView() {
        if (undoView[0].getVisibility() == View.VISIBLE) {
            UndoView old = undoView[0];
            undoView[0] = undoView[1];
            undoView[1] = old;
            old.hide(true, 2);
            ContentView contentView = (ContentView) fragmentView;
            contentView.removeView(undoView[0]);
            contentView.addView(undoView[0]);
        }
        return undoView[0];
    }

    private void updateProxyButton(boolean animated, boolean force) {
        if (proxyDrawable == null || doneItem != null && doneItem.getVisibility() == View.VISIBLE) {
            return;
        }
        boolean showDownloads = false;
        if (NaConfig.INSTANCE.getAlwaysShowDownloadIcon().Bool()) {
            showDownloads = true;
        } else {
            for (int i = 0; i < getDownloadController().downloadingFiles.size(); i++){
                if (getFileLoader().isLoadingFile(getDownloadController().downloadingFiles.get(i).getFileName())) {
                    showDownloads = true;
                    break;
                }
            }
        }
        if (!searching && (getDownloadController().hasUnviewedDownloads() || showDownloads || (downloadsItem.getVisibility() == View.VISIBLE && downloadsItem.getAlpha() == 1 && !force))) {
            downloadsItemVisible = true;
            downloadsItem.setVisibility(View.VISIBLE);
        } else {
            downloadsItem.setVisibility(View.GONE);
            downloadsItemVisible = false;
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String proxyAddress = preferences.getString("proxy_ip", "");
        boolean proxyEnabled;
        proxyEnabled = preferences.getBoolean("proxy_enabled", false);
        if (!downloadsItemVisible && !NekoConfig.useProxyItem.Bool() && (!NekoConfig.hideProxyByDefault.Bool() || (proxyEnabled && !TextUtils.isEmpty(proxyAddress)) || getMessagesController().blockedCountry && !SharedConfig.proxyList.isEmpty())) {
            if (!actionBar.isSearchFieldVisible() && (doneItem == null || doneItem.getVisibility() != View.VISIBLE)) {
                proxyItem.setVisibility(View.VISIBLE);
            }
            proxyItemVisible = true;
            proxyDrawable.setConnected(true, currentConnectionState == ConnectionsManager.ConnectionStateConnected || currentConnectionState == ConnectionsManager.ConnectionStateUpdating, animated);
        } else {
            proxyItemVisible = false;
            proxyItem.setVisibility(View.GONE);
        }
    }

    private AnimatorSet doneItemAnimator;

    private void showDoneItem(boolean show) {
        if (doneItem == null) {
            return;
        }
        if (doneItemAnimator != null) {
            doneItemAnimator.cancel();
            doneItemAnimator = null;
        }
        doneItemAnimator = new AnimatorSet();
        doneItemAnimator.setDuration(180);
        if (show) {
            doneItem.setVisibility(View.VISIBLE);
        } else {
            doneItem.setSelected(false);
            Drawable background = doneItem.getBackground();
            if (background != null) {
                background.setState(StateSet.NOTHING);
                background.jumpToCurrentState();
            }
            if (searchItem != null) {
                searchItem.setVisibility(View.VISIBLE);
            }
            if (proxyItem != null && proxyItemVisible) {
                proxyItem.setVisibility(View.VISIBLE);
            }
            if (passcodeItem != null && passcodeItemVisible) {
                passcodeItem.setVisibility(View.VISIBLE);
            }
            if (downloadsItem != null && downloadsItemVisible) {
                downloadsItem.setVisibility(View.VISIBLE);
            }
        }
        ArrayList<Animator> arrayList = new ArrayList<>();
        arrayList.add(ObjectAnimator.ofFloat(doneItem, View.ALPHA, show ? 1.0f : 0.0f));
        if (proxyItemVisible) {
            arrayList.add(ObjectAnimator.ofFloat(proxyItem, View.ALPHA, show ? 0.0f : 1.0f));
        }
        if (passcodeItemVisible) {
            arrayList.add(ObjectAnimator.ofFloat(passcodeItem, View.ALPHA, show ? 0.0f : 1.0f));
        }
        arrayList.add(ObjectAnimator.ofFloat(searchItem, View.ALPHA, show ? 0.0f : 1.0f));
        doneItemAnimator.playTogether(arrayList);
        doneItemAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                doneItemAnimator = null;
                if (show) {
                    if (searchItem != null) {
                        searchItem.setVisibility(View.INVISIBLE);
                    }
                    if (proxyItem != null && proxyItemVisible) {
                        proxyItem.setVisibility(View.INVISIBLE);
                    }
                    if (passcodeItem != null && passcodeItemVisible) {
                        passcodeItem.setVisibility(View.INVISIBLE);
                    }
                    if (downloadsItem != null && downloadsItemVisible) {
                        downloadsItem.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (doneItem != null) {
                        doneItem.setVisibility(View.GONE);
                    }
                }
            }
        });
        doneItemAnimator.start();
    }

    private boolean isNextButton = false;
    private AnimatorSet commentViewAnimator;

    private void updateSelectedCount() {
        if (commentView != null) {
            if (selectedDialogs.isEmpty()) {
                if (initialDialogsType == 3 && selectAlertString == null) {
                    actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
                } else {
                    actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
                }
                if (commentView.getTag() != null) {
                    commentView.hidePopup(false);
                    commentView.closeKeyboard();
                    if (commentViewAnimator != null) {
                        commentViewAnimator.cancel();
                    }
                    commentViewAnimator = new AnimatorSet();
                    commentView.setTranslationY(0);
                    commentViewAnimator.playTogether(
                            ObjectAnimator.ofFloat(commentView, View.TRANSLATION_Y, commentView.getMeasuredHeight()),
                            ObjectAnimator.ofFloat(writeButtonContainer, View.SCALE_X, .2f),
                            ObjectAnimator.ofFloat(writeButtonContainer, View.SCALE_Y, .2f),
                            ObjectAnimator.ofFloat(writeButtonContainer, View.ALPHA, 0),
                            ObjectAnimator.ofFloat(selectedCountView, View.SCALE_X, 0.2f),
                            ObjectAnimator.ofFloat(selectedCountView, View.SCALE_Y, 0.2f),
                            ObjectAnimator.ofFloat(selectedCountView, View.ALPHA, 0.0f)
                    );
                    commentViewAnimator.setDuration(180);
                    commentViewAnimator.setInterpolator(new DecelerateInterpolator());
                    commentViewAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            commentView.setVisibility(View.GONE);
                            writeButtonContainer.setVisibility(View.GONE);
                        }
                    });
                    commentViewAnimator.start();
                    commentView.setTag(null);
                    fragmentView.requestLayout();
                }
            } else {
                selectedCountView.invalidate();
                if (commentView.getTag() == null) {
                    commentView.setFieldText("");
                    if (commentViewAnimator != null) {
                        commentViewAnimator.cancel();
                    }
                    commentView.setVisibility(View.VISIBLE);
                    writeButtonContainer.setVisibility(View.VISIBLE);
                    commentViewAnimator = new AnimatorSet();
                    commentViewAnimator.playTogether(
                            ObjectAnimator.ofFloat(commentView, View.TRANSLATION_Y, commentView.getMeasuredHeight(),0),
                            ObjectAnimator.ofFloat(writeButtonContainer, View.SCALE_X, 1f),
                            ObjectAnimator.ofFloat(writeButtonContainer, View.SCALE_Y, 1f),
                            ObjectAnimator.ofFloat(writeButtonContainer, View.ALPHA, 1f),
                            ObjectAnimator.ofFloat(selectedCountView, View.SCALE_X, 1f),
                            ObjectAnimator.ofFloat(selectedCountView, View.SCALE_Y, 1f),
                            ObjectAnimator.ofFloat(selectedCountView, View.ALPHA, 1f)
                    );
                    commentViewAnimator.setDuration(180);
                    commentViewAnimator.setInterpolator(new DecelerateInterpolator());
                    commentViewAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            commentView.setTag(2);
                            commentView.requestLayout();
                        }
                    });
                    commentViewAnimator.start();
                    commentView.setTag(1);
                }
                actionBar.setTitle(LocaleController.formatPluralString("Recipient", selectedDialogs.size()));
            }
        } else if (initialDialogsType == 10) {
            hideFloatingButton(selectedDialogs.isEmpty());
        }

        isNextButton = shouldShowNextButton(this, selectedDialogs, commentView != null ? commentView.getFieldText() : "", false);
        AndroidUtilities.updateViewVisibilityAnimated(writeButton[0], !isNextButton, 0.5f, true);
        AndroidUtilities.updateViewVisibilityAnimated(writeButton[1], isNextButton, 0.5f, true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons(boolean alert) {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (getUserConfig().syncContacts && askAboutContacts && activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (alert) {
                AlertDialog.Builder builder = AlertsCreator.createContactsPermissionDialog(activity, param -> {
                    askAboutContacts = param != 0;
                    MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts).apply();
                    askForPermissons(false);
                });
                showDialog(permissionDialog = builder.create());
                return;
            }
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if ((Build.VERSION.SDK_INT <= 28 || BuildVars.NO_SCOPED_STORAGE) && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissons.isEmpty()) {
            if (askingForPermissions) {
                askingForPermissions = false;
                showFiltersHint();
            }
            return;
        }
        String[] items = permissons.toArray(new String[0]);
        try {
            activity.requestPermissions(items, 1);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (scrimPopupWindow != null) {
            scrimPopupWindow.dismiss();
        }
        if (!onlySelect && floatingButtonContainer != null) {
            floatingButtonContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floatingButtonTranslation = floatingHidden ? AndroidUtilities.dp(100) : 0;
                    updateFloatingButtonOffset();
                    floatingButtonContainer.setClickable(!floatingHidden);
                    if (floatingButtonContainer != null) {
                        floatingButtonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        if (grantResults[a] == PackageManager.PERMISSION_GRANTED) {
                            AndroidUtilities.runOnUIThread(() -> getNotificationCenter().postNotificationName(NotificationCenter.forceImportContactsStart));
                            getContactsController().forceImportContacts();
                        } else {
                            MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts = false).apply();
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[a] == PackageManager.PERMISSION_GRANTED) {
                            ImageLoader.getInstance().checkMediaPaths();
                        }
                        break;
                }
            }
            if (askingForPermissions) {
                askingForPermissions = false;
                showFiltersHint();
            }
        } else if (requestCode == 4) {
            boolean allGranted = true;
            for (int a = 0; a < grantResults.length; a++) {
                if (grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
//            if (allGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && FilesMigrationService.filesMigrationBottomSheet != null) {
//                FilesMigrationService.filesMigrationBottomSheet.migrateOldFolder();
//            }

        }
    }

    private void reloadViewPageDialogs(ViewPage viewPage, boolean newMessage) {
        if (viewPage.getVisibility() != View.VISIBLE) {
            return;
        }
        int oldItemCount = viewPage.dialogsAdapter.getCurrentCount();

        if (viewPage.dialogsType == 0 && hasHiddenArchive() && viewPage.listView.getChildCount() == 0 && viewPage.archivePullViewState == ARCHIVE_ITEM_STATE_HIDDEN) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) viewPage.listView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(1, (int) actionBar.getTranslationY());
        }

        // NekoX TODO: Remove satouriko's fix wrong tab dialogs after forward
        if (viewPage.dialogsAdapter.isDataSetChanged() || newMessage) {
            viewPage.dialogsAdapter.updateHasHints();
            int newItemCount = viewPage.dialogsAdapter.getItemCount();
            if (newItemCount == 1 && oldItemCount == 1 && viewPage.dialogsAdapter.getItemViewType(0) == 5) {
                viewPage.updateList(true);
            } else {
                viewPage.updateList(false);
                if (newItemCount > oldItemCount && initialDialogsType != 11 && initialDialogsType != 12 && initialDialogsType != 13) {
                    viewPage.recyclerItemsEnterAnimator.showItemsAnimated(oldItemCount);
                }
            }
        } else {
            updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
            int newItemCount = viewPage.dialogsAdapter.getItemCount();
            if (newItemCount > oldItemCount && initialDialogsType != 11 && initialDialogsType != 12 && initialDialogsType != 13) {
                viewPage.recyclerItemsEnterAnimator.showItemsAnimated(oldItemCount);
            }
        }
        try {
            viewPage.listView.setEmptyView(folderId == 0 ? viewPage.progressView : null);
        } catch (Exception e) {
            FileLog.e(e);
        }
        checkListLoad(viewPage);
    }

    public void setPanTranslationOffset(float y) {
        floatingButtonPanOffset = y;
        updateFloatingButtonOffset();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (viewPages == null || dialogsListFrozen) {
                return;
            }
            for (int a = 0; a < viewPages.length; a++) {
                final ViewPage viewPage = viewPages[a];
                MessagesController.DialogFilter filter = null;
                if (viewPages[0].dialogsType == 7 || viewPages[0].dialogsType == 8) {
                    filter = getMessagesController().selectedDialogFilter[viewPages[0].dialogsType == 8 ? 1 : 0];
                }
                boolean isUnread = filter != null && (filter.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ) != 0;
                if (slowedReloadAfterDialogClick && isUnread) {
                    // in unread tab dialogs reload too instantly removes dialog from folder after clicking on it
                    AndroidUtilities.runOnUIThread(() -> {
                        reloadViewPageDialogs(viewPage, args.length > 0);
                        if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                            filterTabsView.checkTabsCounter();
                        }
                    }, 160);
                } else {
                    reloadViewPageDialogs(viewPage, args.length > 0);
                }
            }
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                filterTabsView.checkTabsCounter();
            }
            slowedReloadAfterDialogClick = false;
        } else if (id == NotificationCenter.dialogsUnreadCounterChanged) {
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE) {
                filterTabsView.notifyTabCounterChanged(filterTabsView.getDefaultTabId());
            }
        } else if (id == NotificationCenter.dialogsUnreadReactionsCounterChanged) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.emojiLoaded) {
            updateVisibleRows(0);
            if (filterTabsView != null) {
                filterTabsView.getTabsContainer().invalidateViews();
            }
        } else if (id == NotificationCenter.closeSearchByActiveAction) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
        } else if (id == NotificationCenter.proxySettingsChanged) {
            updateProxyButton(false, false);
        } else if (id == NotificationCenter.updateInterfaces) {
            Integer mask = (Integer) args[0];
            updateVisibleRows(mask);
            if (filterTabsView != null && filterTabsView.getVisibility() == View.VISIBLE && (mask & MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE) != 0) {
                filterTabsView.checkTabsCounter();
            }
            if (viewPages != null) {
                for (int a = 0; a < viewPages.length; a++) {
                    if ((mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                        viewPages[a].dialogsAdapter.sortOnlineContacts(true);
                    }
                }
            }
            updateStatus(UserConfig.getInstance(account).getCurrentUser(), true);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded.put(currentAccount, false);
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoad) {
            if (viewPages == null || dialogsListFrozen) {
                return;
            }
            boolean wasVisible = floatingProgressVisible;
            setFloatingProgressVisible(false, true);
            for (ViewPage page : viewPages) {
                page.dialogsAdapter.setForceUpdatingContacts(false);
            }
            if (wasVisible) {
                setContactsAlpha(0f);
                animateContactsAlpha(1f);
            }

            boolean updateVisibleRows = false;
            for (int a = 0; a < viewPages.length; a++) {
                if (viewPages[a].isDefaultDialogType() && getMessagesController().getAllFoldersDialogsCount() <= 10) {
                    viewPages[a].dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows = true;
                }
            }
            if (updateVisibleRows) {
                updateVisibleRows(0);
            }
        } else if (id == NotificationCenter.openedChatChanged) {
            if (viewPages == null) {
                return;
            }
            for (int a = 0; a < viewPages.length; a++) {
                if (viewPages[a].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                    boolean close = (Boolean) args[2];
                    long dialog_id = (Long) args[0];
                    int topicId = (int) args[1];
                    if (close) {
                        if (dialog_id == openedDialogId.dialogId && topicId == openedDialogId.topicId) {
                            openedDialogId.dialogId = 0;
                            openedDialogId.topicId = 0;
                        }
                    } else {
                        openedDialogId.dialogId = dialog_id;
                        openedDialogId.topicId = topicId;
                    }
                    viewPages[a].dialogsAdapter.setOpenedDialogId(openedDialogId.dialogId);
                }
            }
            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (searchViewPager != null && searchViewPager.dialogsSearchAdapter != null) {
                searchViewPager.dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.replyMessagesDidLoad) {
            updateVisibleRows(MessagesController.UPDATE_MASK_MESSAGE_TEXT);
        } else if (id == NotificationCenter.reloadHints) {
            if (searchViewPager != null && searchViewPager.dialogsSearchAdapter != null) {
                searchViewPager.dialogsSearchAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.didUpdateConnectionState) {
            int state = AccountInstance.getInstance(account).getConnectionsManager().getConnectionState();
            if (currentConnectionState != state) {
                currentConnectionState = state;
                updateProxyButton(true, false);
            }
        } else if (id == NotificationCenter.onDownloadingFilesChanged) {
            updateProxyButton(true, false);
            if (searchViewPager != null) {
                updateSpeedItem(searchViewPager.getCurrentPosition() == 2);
            }
        } else if (id == NotificationCenter.needDeleteDialog) {
            if (fragmentView == null || isPaused) {
                return;
            }
            long dialogId = (Long) args[0];
            TLRPC.User user = (TLRPC.User) args[1];
            TLRPC.Chat chat = (TLRPC.Chat) args[2];
            boolean revoke = (Boolean) args[3];
            Runnable deleteRunnable = () -> {
                if (chat != null) {
                    if (ChatObject.isNotInChat(chat)) {
                        getMessagesController().deleteDialog(dialogId, 0, revoke);
                    } else {
                        getMessagesController().deleteParticipantFromChat(-dialogId, getMessagesController().getUser(getUserConfig().getClientUserId()), null, revoke, revoke);
                    }
                } else {
                    getMessagesController().deleteDialog(dialogId, 0, revoke);
                    if (user != null && user.bot) {
                        getMessagesController().blockPeer(user.id);
                    }
                }
                getMessagesController().checkIfFolderEmpty(folderId);
            };
            if (undoView[0] != null) {
                if (!ChatObject.isForum(chat)) {
                    getUndoView().showWithAction(dialogId, UndoView.ACTION_DELETE, deleteRunnable);
                } else {
                    deleteRunnable.run();
                }
            } else {
                deleteRunnable.run();
            }
        } else if (id == NotificationCenter.folderBecomeEmpty) {
            int fid = (Integer) args[0];
            if (folderId == fid && folderId != 0) {
                finishFragment();
            }
        } else if (id == NotificationCenter.dialogFiltersUpdated) {
            updateFilterTabs(true, true);
        } else if (id == NotificationCenter.filterSettingsUpdated) {
            showFiltersHint();
        } else if (id == NotificationCenter.newSuggestionsAvailable) {
            showNextSupportedSuggestion();
        } else if (id == NotificationCenter.forceImportContactsStart) {
            setFloatingProgressVisible(true, true);
            for (ViewPage page : viewPages) {
                page.dialogsAdapter.setForceShowEmptyCell(false);
                page.dialogsAdapter.setForceUpdatingContacts(true);
                page.dialogsAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.messagesDeleted) {
            if (searchIsShowed && searchViewPager != null) {
                ArrayList<Integer> markAsDeletedMessages = (ArrayList<Integer>) args[0];
                long channelId = (Long) args[1];
                searchViewPager.messagesDeleted(channelId, markAsDeletedMessages);
            }
        } else if (id == NotificationCenter.didClearDatabase) {
            if (viewPages != null) {
                for (int a = 0; a < viewPages.length; a++) {
                    viewPages[a].dialogsAdapter.didDatabaseCleared();
                }
            }
            SuggestClearDatabaseBottomSheet.dismissDialog();
        } else if (id == NotificationCenter.appUpdateAvailable) {
            updateMenuButton(true);
        } else if (id == NotificationCenter.fileLoaded || id == NotificationCenter.fileLoadFailed || id == NotificationCenter.fileLoadProgressChanged) {
            String name = (String) args[0];
            if (SharedConfig.isAppUpdateAvailable()) {
                String fileName = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                if (fileName.equals(name)) {
                    updateMenuButton(true);
                }
            }
        } else if (id == NotificationCenter.onDatabaseMigration) {
            boolean startMigration = (boolean) args[0];
            if (fragmentView != null) {
                if (startMigration) {
                    if (databaseMigrationHint == null) {
                        databaseMigrationHint = new DatabaseMigrationHint(fragmentView.getContext(), currentAccount);
                        databaseMigrationHint.setAlpha(0f);
                        ((ContentView) fragmentView).addView(databaseMigrationHint);
                        databaseMigrationHint.animate().alpha(1).setDuration(300).setStartDelay(1000).start();
                    }
                    databaseMigrationHint.setTag(1);
                } else {
                    if (databaseMigrationHint != null && databaseMigrationHint.getTag() != null) {
                        View localView = databaseMigrationHint;
                        localView.animate().setListener(null).cancel();
                        localView.animate().setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (localView.getParent() != null) {
                                    ((ViewGroup) localView.getParent()).removeView(localView);
                                }
                                databaseMigrationHint = null;
                            }
                        }).alpha(0f).setStartDelay(0).setDuration(150).start();
                        databaseMigrationHint.setTag(null);
                    }
                }
            }
        } else if (id == NotificationCenter.onDatabaseOpened) {
            checkSuggestClearDatabase();
        } else if (id == NotificationCenter.userEmojiStatusUpdated) {
            updateStatus((TLRPC.User) args[0], true);
        } else if (id == NotificationCenter.currentUserPremiumStatusChanged) {
            updateStatus(UserConfig.getInstance(account).getCurrentUser(), true);
        }
    }

    private void checkSuggestClearDatabase() {
        if (getMessagesStorage().showClearDatabaseAlert) {
            getMessagesStorage().showClearDatabaseAlert = false;
            SuggestClearDatabaseBottomSheet.show(this);
        }
    }

    View databaseMigrationHint;

    private void updateMenuButton(boolean animated) {
        if (menuDrawable == null) {
            return;
        }
        int type;
        float downloadProgress;
        if (SharedConfig.isAppUpdateAvailable()) {
            String fileName = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
            if (getFileLoader().isLoadingFile(fileName)) {
                type = MenuDrawable.TYPE_UDPATE_DOWNLOADING;
                Float p = ImageLoader.getInstance().getFileProgress(fileName);
                downloadProgress = p != null ? p : 0.0f;
            } else {
                type = MenuDrawable.TYPE_UDPATE_AVAILABLE;
                downloadProgress = 0.0f;
            }
        } else {
            type = MenuDrawable.TYPE_DEFAULT;
            downloadProgress = 0.0f;
        }
        updateAppUpdateViews(animated);
        menuDrawable.setType(type, animated);
        menuDrawable.setUpdateDownloadProgress(downloadProgress, animated);
    }

    private String showingSuggestion;

    private void showNextSupportedSuggestion() {
        if (showingSuggestion != null) {
            return;
        }
        for (String suggestion : getMessagesController().pendingSuggestions) {
            if (showSuggestion(suggestion)) {
                showingSuggestion = suggestion;
                return;
            }
        }
    }

    private void onSuggestionDismiss() {
        if (showingSuggestion == null) {
            return;
        }
        getMessagesController().removeSuggestion(0, showingSuggestion);
        showingSuggestion = null;
        showNextSupportedSuggestion();
    }

    private boolean showSuggestion(String suggestion) {
        if ("AUTOARCHIVE_POPULAR".equals(suggestion)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("HideNewChatsAlertTitle", R.string.HideNewChatsAlertTitle));
            builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString("HideNewChatsAlertText", R.string.HideNewChatsAlertText)));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.setPositiveButton(LocaleController.getString("GoToSettings", R.string.GoToSettings), (dialog, which) -> {
                presentFragment(new PrivacySettingsActivity());
                AndroidUtilities.scrollToFragmentRow(parentLayout, "newChatsRow");
            });
            showDialog(builder.create(), dialog -> onSuggestionDismiss());
            return true;
        }
        return false;
    }

    private void showFiltersHint() {
        if (askingForPermissions || !getMessagesController().dialogFiltersLoaded || !getMessagesController().showFiltersTooltip || filterTabsView == null || !getMessagesController().dialogFilters.isEmpty() || isPaused || !getUserConfig().filtersLoaded || inPreviewMode) {
            return;
        }
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        if (preferences.getBoolean("filterhint", false)) {
            return;
        }
        preferences.edit().putBoolean("filterhint", true).apply();
        AndroidUtilities.runOnUIThread(() -> getUndoView().showWithAction(0, UndoView.ACTION_FILTERS_AVAILABLE, null, () -> presentFragment(new FiltersSetupActivity())), 1000);
    }

    private void setDialogsListFrozen(boolean frozen, boolean notify) {
        if (viewPages == null || dialogsListFrozen == frozen) {
            return;
        }
        if (frozen) {
            frozenDialogsList = new ArrayList<>(getDialogsArray(currentAccount, viewPages[0].dialogsType, folderId, false));
        } else {
            frozenDialogsList = null;
        }
        dialogsListFrozen = frozen;
        viewPages[0].dialogsAdapter.setDialogsListFrozen(frozen);
        if (!frozen && notify) {
            viewPages[0].dialogsAdapter.notifyDataSetChanged();
        }
    }

    private void setDialogsListFrozen(boolean frozen) {
        setDialogsListFrozen(frozen, true);
    }

    public class DialogsHeader extends TLRPC.Dialog {
        public static final int HEADER_TYPE_MY_CHANNELS = 0;
        public static final int HEADER_TYPE_MY_GROUPS = 1;
        public static final int HEADER_TYPE_GROUPS = 2;
        public int headerType;
        public DialogsHeader(int type) {
            this.headerType = type;
        }
    }

    @NonNull
    public ArrayList<TLRPC.Dialog> getDialogsArray(int currentAccount, int dialogsType, int folderId, boolean frozen) {
        if (frozen && frozenDialogsList != null) {
            return frozenDialogsList;
        }
        MessagesController messagesController = AccountInstance.getInstance(currentAccount).getMessagesController();
        if (dialogsType == 0) {
            return messagesController.getDialogs(folderId);
        } else if (dialogsType == 1 || dialogsType == 10 || dialogsType == 13) {
            return messagesController.dialogsServerOnly;
        } else if (dialogsType == 2) {
            ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>(messagesController.dialogsCanAddUsers.size() + messagesController.dialogsMyChannels.size() + messagesController.dialogsMyGroups.size() + 2);
            if (messagesController.dialogsMyChannels.size() > 0 && allowChannels) {
                dialogs.add(new DialogsHeader(DialogsHeader.HEADER_TYPE_MY_CHANNELS));
                dialogs.addAll(messagesController.dialogsMyChannels);
            }
            if (messagesController.dialogsMyGroups.size() > 0 && allowGroups) {
                dialogs.add(new DialogsHeader(DialogsHeader.HEADER_TYPE_MY_GROUPS));
                dialogs.addAll(messagesController.dialogsMyGroups);
            }
            if (messagesController.dialogsCanAddUsers.size() > 0) {
                final int count = messagesController.dialogsCanAddUsers.size();
                boolean first = true;
                for (int i = 0; i < count; ++i) {
                    TLRPC.Dialog dialog = messagesController.dialogsCanAddUsers.get(i);
                    if (allowChannels && ChatObject.isChannelAndNotMegaGroup(-dialog.id, currentAccount) ||
                        allowGroups && (ChatObject.isMegagroup(currentAccount, -dialog.id) || !ChatObject.isChannel(-dialog.id, currentAccount))) {
                        if (first) {
                            dialogs.add(new DialogsHeader(DialogsHeader.HEADER_TYPE_GROUPS));
                            first = false;
                        }
                        dialogs.add(dialog);
                    }
                }
            }
            return dialogs;
        } else if (dialogsType == 3) {
            return messagesController.dialogsForward;
        } else if (dialogsType == 4 || dialogsType == 12) {
            return messagesController.dialogsUsersOnly;
        } else if (dialogsType == 5) {
            return messagesController.dialogsChannelsOnly;
        } else if (dialogsType == 6 || dialogsType == 11) {
            return messagesController.dialogsGroupsOnly;
        } else if (dialogsType == 7 || dialogsType == 8) {
            MessagesController.DialogFilter dialogFilter = messagesController.selectedDialogFilter[dialogsType == 7 ? 0 : 1];
            if (dialogFilter == null) {
                return messagesController.getDialogs(folderId);
            } else {
                return dialogFilter.dialogs;
            }
        } else if (dialogsType == 9) {
            return messagesController.dialogsForBlock;
        } else if (dialogsType == DIALOGS_TYPE_START_ATTACH_BOT) {
            ArrayList<TLRPC.Dialog> dialogs = new ArrayList<>();
            if (allowUsers || allowBots) {
                for (TLRPC.Dialog d : messagesController.dialogsUsersOnly) {
                    if (messagesController.getUser(d.id).bot ? allowBots : allowUsers) {
                        dialogs.add(d);
                    }
                }
            }
            if (allowGroups) {
                dialogs.addAll(messagesController.dialogsGroupsOnly);
            }
            if (allowChannels) {
                dialogs.addAll(messagesController.dialogsChannelsOnly);
            }
            return dialogs;
        }
        return new ArrayList<>();
    }

    public void setSideMenu(RecyclerView recyclerView) {
        sideMenu = recyclerView;
        sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (SharedConfig.passcodeHash.length() != 0 && !searching) {
            if (doneItem == null || doneItem.getVisibility() != View.VISIBLE) {
                passcodeItem.setVisibility(View.VISIBLE);
            }
            passcodeItem.setIcon(passcodeDrawable);
            passcodeItemVisible = true;
        } else {
            passcodeItem.setVisibility(View.GONE);
            passcodeItemVisible = false;
        }
    }

    private void setFloatingProgressVisible(boolean visible, boolean animate) {
        if (floatingButton == null || floatingProgressView == null) {
            return;
        }
        if (animate) {
            if (visible == floatingProgressVisible) {
                return;
            }
            if (floatingProgressAnimator != null) {
                floatingProgressAnimator.cancel();
            }

            floatingProgressVisible = visible;

            floatingProgressAnimator = new AnimatorSet();
            floatingProgressAnimator.playTogether(
                    ObjectAnimator.ofFloat(floatingButton, View.ALPHA, visible ? 0f : 1f),
                    ObjectAnimator.ofFloat(floatingButton, View.SCALE_X, visible ? 0.1f : 1f),
                    ObjectAnimator.ofFloat(floatingButton, View.SCALE_Y, visible ? 0.1f : 1f),
                    ObjectAnimator.ofFloat(floatingProgressView, View.ALPHA, visible ? 1f : 0f),
                    ObjectAnimator.ofFloat(floatingProgressView, View.SCALE_X, visible ? 1f : 0.1f),
                    ObjectAnimator.ofFloat(floatingProgressView, View.SCALE_Y, visible ? 1f : 0.1f)
            );
            floatingProgressAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    floatingProgressView.setVisibility(View.VISIBLE);
                    floatingButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animation == floatingProgressAnimator) {
                        if (visible) {
                            if (floatingButton != null) {
                                floatingButton.setVisibility(View.GONE);
                            }
                        } else {
                            if (floatingButton != null) {
                                floatingProgressView.setVisibility(View.GONE);
                            }
                        }
                        floatingProgressAnimator = null;
                    }
                }
            });
            floatingProgressAnimator.setDuration(150);
            floatingProgressAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
            floatingProgressAnimator.start();
        } else {
            if (floatingProgressAnimator != null) {
                floatingProgressAnimator.cancel();
            }

            floatingProgressVisible = visible;
            if (visible) {
                floatingButton.setAlpha(0f);
                floatingButton.setScaleX(0.1f);
                floatingButton.setScaleY(0.1f);
                floatingButton.setVisibility(View.GONE);

                floatingProgressView.setAlpha(1f);
                floatingProgressView.setScaleX(1f);
                floatingProgressView.setScaleY(1f);
                floatingProgressView.setVisibility(View.VISIBLE);
            } else {
                floatingButton.setAlpha(1f);
                floatingButton.setScaleX(1f);
                floatingButton.setScaleY(1f);
                floatingButton.setVisibility(View.VISIBLE);

                floatingProgressView.setAlpha(0f);
                floatingProgressView.setScaleX(0.1f);
                floatingProgressView.setScaleY(0.1f);
                floatingProgressView.setVisibility(View.GONE);
            }
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (rightSlidingDialogContainer.hasFragment()) {
            hide = true;
        }
        if (floatingHidden == hide || hide && floatingForceVisible) {
            return;
        }
        floatingHidden = hide;
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(floatingButtonHideProgress, floatingHidden ? 1f : 0f);
        valueAnimator.addUpdateListener(animation -> {
            floatingButtonHideProgress = (float) animation.getAnimatedValue();
            floatingButtonTranslation = AndroidUtilities.dp(100) * floatingButtonHideProgress;
            updateFloatingButtonOffset();
        });
        animatorSet.playTogether(valueAnimator);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(floatingInterpolator);
        floatingButtonContainer.setClickable(!hide);
        animatorSet.start();
    }

    public float getContactsAlpha() {
        return contactsAlpha;
    }

    public void animateContactsAlpha(float alpha) {
        if (contactsAlphaAnimator != null) {
            contactsAlphaAnimator.cancel();
        }

        contactsAlphaAnimator = ValueAnimator.ofFloat(contactsAlpha, alpha).setDuration(250);
        contactsAlphaAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
        contactsAlphaAnimator.addUpdateListener(animation -> setContactsAlpha((float) animation.getAnimatedValue()));
        contactsAlphaAnimator.start();
    }

    public void setContactsAlpha(float alpha) {
        contactsAlpha = alpha;
        for (ViewPage p : viewPages) {
            RecyclerListView listView = p.listView;
            for (int i = 0; i < listView.getChildCount(); i++) {
                View v = listView.getChildAt(i);
                if (v != null && listView.getChildAdapterPosition(v) >= p.dialogsAdapter.getDialogsCount() + 1) {
                    v.setAlpha(alpha);
                }
            }
        }
    }

    public void setScrollDisabled(boolean disable) {
        for (ViewPage p : viewPages) {
            LinearLayoutManager llm = (LinearLayoutManager) p.listView.getLayoutManager();
            llm.setScrollDisabled(disable);
        }
    }

    private void updateDialogIndices() {
        if (viewPages == null) {
            return;
        }
        for (int b = 0; b < viewPages.length; b++) {
            if (viewPages[b].getVisibility() != View.VISIBLE || viewPages[b].dialogsAdapter.getDialogsListIsFrozen()) {
                continue;
            }
//            ArrayList<TLRPC.Dialog> dialogs = getDialogsArray(currentAccount, viewPages[b].dialogsType, folderId, false);
//            int count = viewPages[b].listView.getChildCount();
//            for (int a = 0; a < count; a++) {
//                View child = viewPages[b].listView.getChildAt(a);
//                if (child instanceof DialogCell) {
//                    DialogCell dialogCell = (DialogCell) child;
//                    TLRPC.Dialog dialog = getMessagesController().dialogs_dict.get(dialogCell.getDialogId());
//                    if (dialog == null) {
//                        continue;
//                    }
//                    int index = dialogs.indexOf(dialog);
//                    if (index < 0) {
//                        continue;
//                    }
//                 //   dialogCell.setDialogIndex(index);
//                }
//            }
            viewPages[b].updateList(false);
        }
    }

    private void updateVisibleRows(int mask) {
        updateVisibleRows(mask, true);
    }

    private void updateVisibleRows(int mask, boolean animated) {
        if ((dialogsListFrozen && (mask & MessagesController.UPDATE_MASK_REORDER) == 0) || isPaused) {
            return;
        }
        for (int c = 0; c < 3; c++) {
            RecyclerListView list;
            ViewPage viewPage = null;
            if (c == 2) {
                list = searchViewPager != null ? searchViewPager.searchListView : null;
            } else if (viewPages != null) {
                list = c < viewPages.length ? viewPages[c].listView : null;
                if (list != null && viewPages[c].getVisibility() != View.VISIBLE) {
                    continue;
                }
                if (list != null) {
                    viewPage = viewPages[c];
                }
            } else {
                continue;
            }
            if (list == null || list.getAdapter() == null) {
                continue;
            }
            if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0 || mask == 0) {
                if (viewPage != null) {
                    viewPage.updateList(false);
                    continue;
                }
            }
            int count = list.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = list.getChildAt(a);
                if (child instanceof DialogCell) {
                    if (list.getAdapter() != searchViewPager.dialogsSearchAdapter) {
                        DialogCell cell = (DialogCell) child;
                        if ((mask & MessagesController.UPDATE_MASK_REORDER) != 0) {
                            cell.onReorderStateChanged(actionBar.isActionModeShowed(), true);
                            if (dialogsListFrozen) {
                                continue;
                            }
                        }
                        if ((mask & MessagesController.UPDATE_MASK_CHECK) != 0) {
                            cell.setChecked(false, (mask & MessagesController.UPDATE_MASK_CHAT) != 0);
                        } else {
                            if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                                if (list.getAdapter() != null) {
                                    viewPage.updateList(false);
                                    break;
                                }
                                if (viewPages[c].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                                    cell.setDialogSelected(cell.getDialogId() == openedDialogId.dialogId);
                                }
                            } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                                if (viewPages[c].isDefaultDialogType() && AndroidUtilities.isTablet()) {
                                    cell.setDialogSelected(cell.getDialogId() == openedDialogId.dialogId);
                                }
                            } else {
                                if (cell.update(mask, animated)) {
                                    viewPage.updateList(false);
                                    break;
                                }
                            }
                            if (selectedDialogs != null) {
                                cell.setChecked(selectedDialogs.contains(cell.getDialogId()), false);
                            }
                        }
                    }
                }


                if (child instanceof UserCell) {
                    ((UserCell) child).update(mask);
                } else if (child instanceof ProfileSearchCell) {
                    ProfileSearchCell cell = (ProfileSearchCell) child;
                    cell.update(mask);
                    if (selectedDialogs != null) {
                        cell.setChecked(selectedDialogs.contains(cell.getDialogId()), false);
                    }
                }
                if (dialogsListFrozen) {
                    continue;
                }
                if (child instanceof RecyclerListView) {
                    RecyclerListView innerListView = (RecyclerListView) child;
                    int count2 = innerListView.getChildCount();
                    for (int b = 0; b < count2; b++) {
                        View child2 = innerListView.getChildAt(b);
                        if (child2 instanceof HintDialogCell) {
                            ((HintDialogCell) child2).update(mask);
                        }
                    }
                }
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public boolean shouldShowNextButton(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param) {
        return false;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public void setInitialSearchString(String initialSearchString) {
        this.initialSearchString = initialSearchString;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    public boolean isArchive() {
        return folderId == 1;
    }

    public void setInitialSearchType(int type) {
        this.initialSearchType = type;
    }

    private boolean checkCanWrite(final long dialogId) {
        if (addToGroupAlertString == null && checkCanWrite) {
            if (DialogObject.isChatDialog(dialogId)) {
                TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
                if (ChatObject.isChannel(chat) && !chat.megagroup && ((cantSendToChannels || !ChatObject.isCanWriteToChannel(-dialogId, currentAccount)) || hasPoll == 2)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle));
                    if (hasPoll == 2) {
                        builder.setMessage(LocaleController.getString("PublicPollCantForward", R.string.PublicPollCantForward));
                    } else {
                        builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                    }
                    builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                    showDialog(builder.create());
                    return false;
                }
            } else if (DialogObject.isEncryptedDialog(dialogId) && (hasPoll != 0 || hasInvoice)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle));
                if (hasPoll != 0) {
                    builder.setMessage(LocaleController.getString("PollCantForwardSecretChat", R.string.PollCantForwardSecretChat));
                } else {
                    builder.setMessage(LocaleController.getString("InvoiceCantForwardSecretChat", R.string.InvoiceCantForwardSecretChat));
                }
                builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                showDialog(builder.create());
                return false;
            }
        }
        return true;
    }

    public void didSelectResult(final long dialogId, int topicId, boolean useAlert, final boolean param) {
        if (!checkCanWrite(dialogId)) {
            return;
        }
        if (initialDialogsType == 11 || initialDialogsType == 12 || initialDialogsType == 13) {
            if (checkingImportDialog) {
                return;
            }
            TLRPC.User user;
            TLRPC.Chat chat;
            if (DialogObject.isUserDialog(dialogId)) {
                user = getMessagesController().getUser(dialogId);
                chat = null;
                if (!user.mutual_contact) {
                    getUndoView().showWithAction(dialogId, UndoView.ACTION_IMPORT_NOT_MUTUAL, null);
                    return;
                }
            } else {
                user = null;
                chat = getMessagesController().getChat(-dialogId);
                if (!ChatObject.hasAdminRights(chat) || !ChatObject.canChangeChatInfo(chat)) {
                    getUndoView().showWithAction(dialogId, UndoView.ACTION_IMPORT_GROUP_NOT_ADMIN, null);
                    return;
                }
            }
            final AlertDialog progressDialog = new AlertDialog(getParentActivity(), AlertDialog.ALERT_TYPE_SPINNER);
            TLRPC.TL_messages_checkHistoryImportPeer req = new TLRPC.TL_messages_checkHistoryImportPeer();
            req.peer = getMessagesController().getInputPeer(dialogId);
            getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                checkingImportDialog = false;
                if (response != null) {
                    TLRPC.TL_messages_checkedHistoryImportPeer res = (TLRPC.TL_messages_checkedHistoryImportPeer) response;
                    AlertsCreator.createImportDialogAlert(this, arguments.getString("importTitle"), res.confirm_text, user, chat, () -> {
                        setDialogsListFrozen(true);
                        ArrayList<MessagesStorage.TopicKey> dids = new ArrayList<>();
                        dids.add(MessagesStorage.TopicKey.of(dialogId, 0));
                        delegate.didSelectDialogs(DialogsActivity.this, dids, null, param);
                    });
                } else {
                    AlertsCreator.processError(currentAccount, error, this, req);
                    getNotificationCenter().postNotificationName(NotificationCenter.historyImportProgressChanged, dialogId, req, error);
                }
            }));
            try {
                progressDialog.showDelayed(300);
            } catch (Exception ignore) {

            }
        } else if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            String title;
            String message;
            String buttonText;
            if (DialogObject.isEncryptedDialog(dialogId)) {
                TLRPC.EncryptedChat chat = getMessagesController().getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
                TLRPC.User user = getMessagesController().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                title = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                message = LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user));
                buttonText = LocaleController.getString("Send", R.string.Send);
            } else if (DialogObject.isUserDialog(dialogId)) {
                if (dialogId == getUserConfig().getClientUserId()) {
                    title = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                    message = LocaleController.formatStringSimple(selectAlertStringGroup, LocaleController.getString("SavedMessages", R.string.SavedMessages));
                    buttonText = LocaleController.getString("Send", R.string.Send);
                } else {
                    TLRPC.User user = getMessagesController().getUser(dialogId);
                    if (user == null || selectAlertString == null) {
                        return;
                    }
                    title = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                    message = LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user));
                    buttonText = LocaleController.getString("Send", R.string.Send);
                }
            } else {
                TLRPC.Chat chat = getMessagesController().getChat(-dialogId);
                if (chat == null) {
                    return;
                }
                CharSequence chatTitle = chat.title;
                if (topicId != 0) {
                    TLRPC.TL_forumTopic topic = getMessagesController().getTopicsController().findTopic(chat.id, topicId);
                    if (topic != null) {
                        chatTitle += " " + topic.title;
                    }
                }

                if (addToGroupAlertString != null) {
                    title = LocaleController.getString("AddToTheGroupAlertTitle", R.string.AddToTheGroupAlertTitle);
                    message = LocaleController.formatStringSimple(addToGroupAlertString, chatTitle);
                    buttonText = LocaleController.getString("Add", R.string.Add);
                } else {
                    title = LocaleController.getString("SendMessageTitle", R.string.SendMessageTitle);
                    message = LocaleController.formatStringSimple(selectAlertStringGroup, chatTitle);
                    buttonText = LocaleController.getString("Send", R.string.Send);
                }
            }
            builder.setTitle(title);
            builder.setMessage(AndroidUtilities.replaceTags(message));
            builder.setPositiveButton(buttonText, (dialogInterface, i) -> didSelectResult(dialogId, topicId,false, false));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                ArrayList<MessagesStorage.TopicKey> dids = new ArrayList<>();
                dids.add(MessagesStorage.TopicKey.of(dialogId, topicId));
                delegate.didSelectDialogs(DialogsActivity.this, dids, null, param);
                if (resetDelegate) {
                    delegate = null;
                }
            } else {
                finishFragment();
            }
        }
    }

    public RLottieImageView getFloatingButton() {
        return floatingButton;
    }


    private ActionBarPopupWindow sendPopupWindow;

    private boolean onSendLongClick(View view) {
        final Activity parentActivity = getParentActivity();
        final Theme.ResourcesProvider resourcesProvider = getResourceProvider();
        if (parentActivity == null) {
            return false;
        }
        LinearLayout layout = new LinearLayout(parentActivity);
        layout.setOrientation(LinearLayout.VERTICAL);

        ActionBarPopupWindow.ActionBarPopupWindowLayout sendPopupLayout2 = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourcesProvider);
        sendPopupLayout2.setAnimationEnabled(false);
        sendPopupLayout2.setOnTouchListener(new View.OnTouchListener() {
            private android.graphics.Rect popupRect = new android.graphics.Rect();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                        v.getHitRect(popupRect);
                        if (!popupRect.contains((int) event.getX(), (int) event.getY())) {
                            sendPopupWindow.dismiss();
                        }
                    }
                }
                return false;
            }
        });
        sendPopupLayout2.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
        });
        sendPopupLayout2.setShownFromBottom(false);
        sendPopupLayout2.setupRadialSelectors(getThemedColor(Theme.key_dialogButtonSelector));

        ActionBarMenuSubItem sendWithoutSound = new ActionBarMenuSubItem(parentActivity, true, true, resourcesProvider);
        sendWithoutSound.setTextAndIcon(LocaleController.getString("SendWithoutSound", R.string.SendWithoutSound), R.drawable.input_notify_off);
        sendWithoutSound.setMinimumWidth(AndroidUtilities.dp(196));
        sendWithoutSound.setOnClickListener(v -> {
            if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
            this.notify = false;
            if (delegate == null || selectedDialogs.isEmpty()) {
                return;
            }
            ArrayList<MessagesStorage.TopicKey> topicKeys = new ArrayList<>();
            for (int i = 0; i < selectedDialogs.size(); i++) {
                topicKeys.add(MessagesStorage.TopicKey.of(selectedDialogs.get(i), 0));
            }
            delegate.didSelectDialogs(DialogsActivity.this, topicKeys, commentView.getFieldText(), false);
        });

        ActionBarMenuSubItem showSendersNameView = new ActionBarMenuSubItem(parentActivity, true, true, false, resourcesProvider);
        showSendersNameView.setTextAndIcon(LocaleController.getString("ShowSendersName", R.string.ShowSendersName), 0);
        showSendersNameView.setChecked(!ChatActivity.noForwardQuote);

        ActionBarMenuSubItem hideSendersNameView = new ActionBarMenuSubItem(parentActivity, true, false, true, resourcesProvider);
        hideSendersNameView.setTextAndIcon(LocaleController.getString("HideSendersName", R.string.HideSendersName), 0);
        hideSendersNameView.setChecked(ChatActivity.noForwardQuote);
        showSendersNameView.setOnClickListener(e -> {
            if (ChatActivity.noForwardQuote) {
                ChatActivity.noForwardQuote = false;
                showSendersNameView.setChecked(true);
                hideSendersNameView.setChecked(false);
            }
        });
        hideSendersNameView.setOnClickListener(e -> {
            if (!ChatActivity.noForwardQuote) {
                ChatActivity.noForwardQuote = true;
                showSendersNameView.setChecked(false);
                hideSendersNameView.setChecked(true);
            }
        });

        sendPopupLayout2.addView(showSendersNameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        sendPopupLayout2.addView(hideSendersNameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        sendPopupLayout2.addView(sendWithoutSound, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        layout.addView(sendPopupLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        sendPopupWindow = new ActionBarPopupWindow(layout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        sendPopupWindow.setAnimationEnabled(false);
        sendPopupWindow.setAnimationStyle(R.style.PopupContextAnimation2);
        sendPopupWindow.setOutsideTouchable(true);
        sendPopupWindow.setClippingEnabled(true);
        sendPopupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
        sendPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        sendPopupWindow.getContentView().setFocusableInTouchMode(true);
        SharedConfig.removeScheduledOrNoSoundHint();

        layout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
        sendPopupWindow.setFocusable(true);
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int y = location[1] - layout.getMeasuredHeight() - AndroidUtilities.dp(2);
        sendPopupWindow.showAtLocation(view, Gravity.LEFT | Gravity.TOP, location[0] + view.getMeasuredWidth() - layout.getMeasuredWidth() + AndroidUtilities.dp(8), y);
        sendPopupWindow.dimBehind();
        if (!NekoConfig.disableVibration.Bool()) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }

        return false;
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            for (int b = 0; b < 3; b++) {
                RecyclerListView list;
                if (b == 2) {
                    if (searchViewPager == null) {
                        continue;
                    }
                    list = searchViewPager.searchListView;
                } else if (viewPages != null) {
                    list = b < viewPages.length ? viewPages[b].listView : null;
                } else {
                    continue;
                }
                if (list == null) {
                    continue;
                }
                int count = list.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = list.getChildAt(a);
                    if (child instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) child).update(0);
                    } else if (child instanceof DialogCell) {
                        ((DialogCell) child).update(0);
                    } else if (child instanceof UserCell) {
                        ((UserCell) child).update(0);
                    }
                }
            }
            if (searchViewPager != null && searchViewPager.dialogsSearchAdapter != null) {
                RecyclerListView recyclerListView = searchViewPager.dialogsSearchAdapter.getInnerListView();
                if (recyclerListView != null) {
                    int count = recyclerListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = recyclerListView.getChildAt(a);
                        if (child instanceof HintDialogCell) {
                            ((HintDialogCell) child).update();
                        }
                    }
                }
            }
            if (sideMenu != null) {
                View child = sideMenu.getChildAt(0);
                if (child instanceof DrawerProfileCell) {
                    DrawerProfileCell profileCell = (DrawerProfileCell) child;
                    profileCell.applyBackground(true);
                    profileCell.updateColors();
                }
            }
            if (viewPages != null) {
                for (int a = 0; a < viewPages.length; a++) {
                    if (viewPages[a].pullForegroundDrawable == null) {
                        continue;
                    }
                    viewPages[a].pullForegroundDrawable.updateColors();
                }
            }
            if (actionBar != null) {
                actionBar.setPopupBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground), true);
                actionBar.setPopupItemsColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem), false, true);
                actionBar.setPopupItemsColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon), true, true);
                actionBar.setPopupItemsSelectorColor(Theme.getColor(Theme.key_dialogButtonSelector), true);
            }
            if (statusDrawable != null) {
                updateStatus(UserConfig.getInstance(currentAccount).getCurrentUser(), false);
            }

            if (scrimPopupWindowItems != null) {
                for (int a = 0; a < scrimPopupWindowItems.length; a++) {
                    if (scrimPopupWindowItems[a] != null) {
                        scrimPopupWindowItems[a].setColors(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem), Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon));
                        scrimPopupWindowItems[a].setSelectorColor(Theme.getColor(Theme.key_dialogButtonSelector));
                    }
                }
            }
            if (scrimPopupWindow != null) {
                final View contentView = scrimPopupWindow.getContentView();
                contentView.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));
                contentView.invalidate();
            }
            if (doneItem != null) {
                doneItem.setIconColor(Theme.getColor(Theme.key_actionBarDefaultIcon));
            }
            if (commentView != null) {
                commentView.updateColors();
            }

            if (filtersView != null) {
                filtersView.updateColors();
            }
            if (searchViewPager != null) {
                searchViewPager.updateColors();
            }
            if (searchTabsView != null) {
                searchTabsView.updateColors();
            }
            if (blurredView != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    blurredView.setForeground(new ColorDrawable(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_windowBackgroundWhite), 100)));
                }
            }
            if (searchItem != null) {
                EditTextBoldCursor editText = searchItem.getSearchField();
                if (whiteActionBar) {
                    editText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    editText.setHintTextColor(Theme.getColor(Theme.key_player_time));
                    editText.setCursorColor(Theme.getColor(Theme.key_chat_messagePanelCursor));
                } else {
                    editText.setCursorColor(Theme.getColor(Theme.key_actionBarDefaultSearch));
                    editText.setHintTextColor(Theme.getColor(Theme.key_actionBarDefaultSearchPlaceholder));
                    editText.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSearch));
                }
                searchItem.updateColor();
            }
            updateFloatingButtonColor();
            setSearchAnimationProgress(searchAnimationProgress, false);
        };

        ArrayList<ThemeDescription> arrayList = new ArrayList<>();

        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        if (movingView != null) {
            arrayList.add(new ThemeDescription(movingView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
        }

        if (doneItem != null) {
            arrayList.add(new ThemeDescription(doneItem, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_actionBarDefaultSelector));
        }

        if (folderId == 0) {
            if (onlySelect) {
                arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
            }
            arrayList.add(new ThemeDescription(fragmentView, 0, null, actionBarDefaultPaint, null, null, Theme.key_actionBarDefault));
            if (searchViewPager != null) {
                arrayList.add(new ThemeDescription(searchViewPager.searchListView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
            }
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, cellDelegate, Theme.key_actionBarDefaultIcon));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, new Drawable[]{Theme.dialogs_holidayDrawable}, null, Theme.key_actionBarDefaultTitle));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));
        } else {
            arrayList.add(new ThemeDescription(fragmentView, 0, null, actionBarDefaultPaint, null, null, Theme.key_actionBarDefaultArchived));
            if (searchViewPager != null) {
                arrayList.add(new ThemeDescription(searchViewPager.searchListView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefaultArchived));
            }
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultArchivedIcon));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, new Drawable[]{Theme.dialogs_holidayDrawable}, null, Theme.key_actionBarDefaultArchivedTitle));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultArchivedSelector));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultArchivedSearch));
            arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultArchivedSearchPlaceholder));
        }

        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarActionModeDefaultIcon));
        //arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_BACKGROUND, null, null, null, null, Theme.key_actionBarActionModeDefault));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_TOPBACKGROUND, null, null, null, null, Theme.key_actionBarActionModeDefaultTop));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_AM_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarActionModeDefaultSelector));
        arrayList.add(new ThemeDescription(selectedDialogsCountTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_actionBarActionModeDefaultIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_actionBarDefaultSubmenuBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_actionBarDefaultSubmenuItem));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_actionBarDefaultSubmenuItemIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_dialogButtonSelector));

        if (filterTabsView != null) {
            if (actionBar.isActionModeShowed()) {
                arrayList.add(new ThemeDescription(filterTabsView, 0, new Class[]{FilterTabsView.class}, new String[]{"selectorDrawable"}, null, null, null, Theme.key_profile_tabSelectedLine));
                arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_profile_tabSelectedText));
                arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_profile_tabText));
                arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_profile_tabSelector));
            } else {
                arrayList.add(new ThemeDescription(filterTabsView, 0, new Class[]{FilterTabsView.class}, new String[]{"selectorDrawable"}, null, null, null, Theme.key_actionBarTabLine));
                arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_actionBarTabActiveText));
                arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_actionBarTabUnactiveText));
                arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_actionBarTabSelector));
            }
            arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), 0, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_chats_tabUnreadActiveBackground));
            arrayList.add(new ThemeDescription(filterTabsView.getTabsContainer(), 0, new Class[]{FilterTabsView.TabView.class}, null, null, null, Theme.key_chats_tabUnreadUnactiveBackground));
        }
        arrayList.add(new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon));
        arrayList.add(new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground));
        arrayList.add(new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground));

        arrayList.addAll(SimpleThemeDescription.createThemeDescriptions(()->{
            if (searchViewPager != null) {
                ActionBarMenu actionMode = searchViewPager.getActionMode();
                if (actionMode != null) {
                    actionMode.setBackgroundColor(getThemedColor(Theme.key_actionBarActionModeDefault));
                }
                ActionBarMenuItem speedItem = searchViewPager.getSpeedItem();
                if (speedItem != null) {
                    speedItem.getIconView().setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), PorterDuff.Mode.SRC_IN));
                }
            }
        }, Theme.key_actionBarActionModeDefault, Theme.key_actionBarActionModeDefaultIcon));

        for (int a = 0; a < 3; a++) {
            RecyclerListView list;
            if (a == 2) {
                if (searchViewPager == null) {
                    continue;
                }
                list = searchViewPager.searchListView;
            } else if (viewPages != null) {
                list = a < viewPages.length ? viewPages[a].listView : null;
            } else {
                continue;
            }
            if (list == null) {
                continue;
            }

            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, Theme.avatarDrawables, null, Theme.key_avatar_text));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_scamDrawable, Theme.dialogs_fakeDrawable}, null, Theme.key_chats_draft));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable, Theme.dialogs_reorderDrawable}, null, Theme.key_chats_pinnedIcon));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Paint[]{Theme.dialogs_namePaint[0], Theme.dialogs_namePaint[1], Theme.dialogs_searchNamePaint}, null, null, Theme.key_chats_name));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Paint[]{Theme.dialogs_nameEncryptedPaint[0], Theme.dialogs_nameEncryptedPaint[1], Theme.dialogs_searchNameEncryptedPaint}, null, null, Theme.key_chats_secretName));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[1], null, null, Theme.key_chats_message_threeLines));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint[0], null, null, Theme.key_chats_message));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_messageNamePaint, null, null, Theme.key_chats_nameMessage_threeLines));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_draft));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable}, null, Theme.key_chats_sentCheck));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkReadDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentReadCheck));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_mentionDrawable}, null, Theme.key_chats_mentionIcon));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_archivePinBackground));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_archiveBackground));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_chats_onlineCircle));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{DialogCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_CHECKBOX, new Class[]{DialogCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_windowBackgroundWhite));
            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{DialogCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle));

            arrayList.add(new ThemeDescription(list, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3));

            GraySectionCell.createThemeDescriptions(arrayList, list);

            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray));

            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGray));
            arrayList.add(new ThemeDescription(list, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

            arrayList.add(new ThemeDescription(list, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText2));
        }

        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundSaved));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Orange));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Violet));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Green));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Cyan));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Blue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Pink));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_background2Saved));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundArchived));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundArchivedHidden));

        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameMessage));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_draft));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_attachMessage));

        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameArchived));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameMessageArchived));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameMessageArchived_threeLines));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_messageArchived));

        if (viewPages != null) {
            for (int a = 0; a < viewPages.length; a++) {
                if (folderId == 0) {
                    arrayList.add(new ThemeDescription(viewPages[a].listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
                } else {
                    arrayList.add(new ThemeDescription(viewPages[a].listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefaultArchived));
                }

                arrayList.add(new ThemeDescription(viewPages[a].listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView1"}, null, null, null, Theme.key_chats_nameMessage_threeLines));
                arrayList.add(new ThemeDescription(viewPages[a].listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView2"}, null, null, null, Theme.key_chats_message));

                if (SharedConfig.archiveHidden) {
                    arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveAvatarDrawable}, "Arrow1", Theme.key_avatar_backgroundArchivedHidden));
                    arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveAvatarDrawable}, "Arrow2", Theme.key_avatar_backgroundArchivedHidden));
                } else {
                    arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveAvatarDrawable}, "Arrow1", Theme.key_avatar_backgroundArchived));
                    arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveAvatarDrawable}, "Arrow2", Theme.key_avatar_backgroundArchived));
                }
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveAvatarDrawable}, "Box2", Theme.key_avatar_text));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveAvatarDrawable}, "Box1", Theme.key_avatar_text));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_pinArchiveDrawable}, "Arrow", Theme.key_chats_archiveIcon));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_pinArchiveDrawable}, "Line", Theme.key_chats_archiveIcon));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_unpinArchiveDrawable}, "Arrow", Theme.key_chats_archiveIcon));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_unpinArchiveDrawable}, "Line", Theme.key_chats_archiveIcon));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveDrawable}, "Arrow", Theme.key_chats_archiveBackground));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveDrawable}, "Box2", Theme.key_chats_archiveIcon));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_archiveDrawable}, "Box1", Theme.key_chats_archiveIcon));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_hidePsaDrawable}, "Line 1", Theme.key_chats_archiveBackground));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_hidePsaDrawable}, "Line 2", Theme.key_chats_archiveBackground));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_hidePsaDrawable}, "Line 3", Theme.key_chats_archiveBackground));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_hidePsaDrawable}, "Cup Red", Theme.key_chats_archiveIcon));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_hidePsaDrawable}, "Box", Theme.key_chats_archiveIcon));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_unarchiveDrawable}, "Arrow1", Theme.key_chats_archiveIcon));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_unarchiveDrawable}, "Arrow2", Theme.key_chats_archivePinBackground));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_unarchiveDrawable}, "Box2", Theme.key_chats_archiveIcon));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{DialogCell.class}, new RLottieDrawable[]{Theme.dialogs_unarchiveDrawable}, "Box1", Theme.key_chats_archiveIcon));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteGrayText));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlueText));

                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText4));
                arrayList.add(new ThemeDescription(viewPages[a].listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText4));

                arrayList.add(new ThemeDescription(viewPages[a].progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle));

                ViewPager pager = viewPages[a].dialogsAdapter.getArchiveHintCellPager();
                arrayList.add(new ThemeDescription(pager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_chats_nameMessage_threeLines));
                arrayList.add(new ThemeDescription(pager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"imageView2"}, null, null, null, Theme.key_chats_unreadCounter));
                arrayList.add(new ThemeDescription(pager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"headerTextView"}, null, null, null, Theme.key_chats_nameMessage_threeLines));
                arrayList.add(new ThemeDescription(pager, 0, new Class[]{ArchiveHintInnerCell.class}, new String[]{"messageTextView"}, null, null, null, Theme.key_chats_message));
                arrayList.add(new ThemeDescription(pager, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefaultArchived));
            }
        }

        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_archivePullDownBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_archivePullDownBackgroundActive));

        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadowCats));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{DrawerProfileCell.class}, null, null, cellDelegate, Theme.key_chats_menuTopBackgroundCats));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{DrawerProfileCell.class}, null, null, cellDelegate, Theme.key_chats_menuTopBackground));

        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText));

        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_unreadCounterText));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_unreadCounter));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_menuBackground));
        arrayList.add(new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon));
        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText));

        arrayList.add(new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        if (searchViewPager != null) {
            arrayList.add(new ThemeDescription(searchViewPager.dialogsSearchAdapter != null ? searchViewPager.dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter));
            arrayList.add(new ThemeDescription(searchViewPager.dialogsSearchAdapter != null ? searchViewPager.dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted));
            arrayList.add(new ThemeDescription(searchViewPager.dialogsSearchAdapter != null ? searchViewPager.dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText));
            arrayList.add(new ThemeDescription(searchViewPager.dialogsSearchAdapter != null ? searchViewPager.dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_archiveTextPaint, null, null, Theme.key_chats_archiveText));
            arrayList.add(new ThemeDescription(searchViewPager.dialogsSearchAdapter != null ? searchViewPager.dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
            arrayList.add(new ThemeDescription(searchViewPager.dialogsSearchAdapter != null ? searchViewPager.dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, null, null, null, Theme.key_chats_onlineCircle));
        }

        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground));
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause));
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle));
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_FASTSCROLL, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerPerformer));
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose));

        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground));
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText));

        for (int a = 0; a < undoView.length; a++) {
            arrayList.add(new ThemeDescription(undoView[a], ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_undo_background));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"undoImageView"}, null, null, null, Theme.key_undo_cancelColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"undoTextView"}, null, null, null, Theme.key_undo_cancelColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"infoTextView"}, null, null, null, Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"subinfoTextView"}, null, null, null, Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"textPaint"}, null, null, null, Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"progressPaint"}, null, null, null, Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "info1", Theme.key_undo_background));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "info2", Theme.key_undo_background));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc12", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc11", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc10", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc9", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc8", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc7", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc6", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc5", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc4", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc3", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc2", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "luc1", Theme.key_undo_infoColor));
            arrayList.add(new ThemeDescription(undoView[a], 0, new Class[]{UndoView.class}, new String[]{"leftImageView"}, "Oval", Theme.key_undo_infoColor));
        }

        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackgroundGray));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRedIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialog_inlineProgressBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialog_inlineProgress));

        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogSearchBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogSearchHint));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogSearchIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogSearchText));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogFloatingButton));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogFloatingIcon));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogShadowLine));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_sheet_scrollUp));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_sheet_other));

        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBar));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSelector));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTitle));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTop));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSubtitle));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarItems));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_background));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_time));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressCachedBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progress));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_button));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_buttonActive));

        if (commentView != null) {
            arrayList.add(new ThemeDescription(commentView, 0, null, Theme.chat_composeBackgroundPaint, null, null, Theme.key_chat_messagePanelBackground));
            arrayList.add(new ThemeDescription(commentView, 0, null, null, new Drawable[]{Theme.chat_composeShadowDrawable}, null, Theme.key_chat_messagePanelShadow));
            arrayList.add(new ThemeDescription(commentView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{ChatActivityEnterView.class}, new String[]{"messageEditText"}, null, null, null, Theme.key_chat_messagePanelText));
            arrayList.add(new ThemeDescription(commentView, ThemeDescription.FLAG_CURSORCOLOR, new Class[]{ChatActivityEnterView.class}, new String[]{"messageEditText"}, null, null, null, Theme.key_chat_messagePanelCursor));
            arrayList.add(new ThemeDescription(commentView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{ChatActivityEnterView.class}, new String[]{"messageEditText"}, null, null, null, Theme.key_chat_messagePanelHint));
//            arrayList.add(new ThemeDescription(commentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{ChatActivityEnterView.class}, new String[]{"sendButton"}, null, null, null, Theme.key_chat_messagePanelSend));
        }

        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_actionBarTipBackground));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_player_time));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chat_messagePanelCursor));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_actionBarIconBlue));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_groupcreate_spanBackground));

        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayGreen1));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayGreen2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayBlue1));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayBlue2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_topPanelGreen1));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_topPanelGreen2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_topPanelBlue1));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_topPanelBlue2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_topPanelGray));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayAlertGradientMuted));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayAlertGradientMuted2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayAlertGradientUnmuted));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayAlertGradientUnmuted2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_mutedByAdminGradient));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_mutedByAdminGradient2));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_mutedByAdminGradient3));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayAlertMutedByAdmin));
        arrayList.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_voipgroup_overlayAlertMutedByAdmin2));

        if (filtersView != null) {
            arrayList.addAll(filtersView.getThemeDescriptions());
            filtersView.updateColors();
        }

        if (searchViewPager != null) {
            searchViewPager.getThemeDescriptions(arrayList);
        }

        if (speedItem != null) {
            arrayList.addAll(SimpleThemeDescription.createThemeDescriptions(() -> {
                speedItem.getIconView().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), PorterDuff.Mode.SRC_IN));
                speedItem.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector)));
            }, Theme.key_actionBarActionModeDefaultIcon, Theme.key_actionBarActionModeDefaultSelector));
        }

        return arrayList;
    }

    private void updateFloatingButtonColor() {
        if (getParentActivity() == null || floatingButtonContainer == null) {
            return;
        }
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = ContextCompat.getDrawable(getParentActivity(), R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButtonContainer.setBackground(drawable);
    }

    float slideFragmentProgress = 1f;
    boolean isSlideBackTransition;
    boolean isDrawerTransition;
    ValueAnimator slideBackTransitionAnimator;

    @Override
    protected Animator getCustomSlideTransition(boolean topFragment, boolean backAnimation, float distanceToMove) {
        if (backAnimation) {
            slideBackTransitionAnimator = ValueAnimator.ofFloat(slideFragmentProgress, 1f);
            return slideBackTransitionAnimator;
        }
        int duration = (int) (Math.max((int) (200.0f / getLayoutContainer().getMeasuredWidth() * distanceToMove), 80) * 1.2f);
        slideBackTransitionAnimator = ValueAnimator.ofFloat(slideFragmentProgress, 1f);
        slideBackTransitionAnimator.addUpdateListener(valueAnimator -> setSlideTransitionProgress((float) valueAnimator.getAnimatedValue()));
        slideBackTransitionAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        slideBackTransitionAnimator.setDuration(duration);
        slideBackTransitionAnimator.start();
        return slideBackTransitionAnimator;
    }

    @Override
    public void prepareFragmentToSlide(boolean topFragment, boolean beginSlide) {
        if (!topFragment && beginSlide) {
            isSlideBackTransition = true;
            setFragmentIsSliding(true);
        } else {
            slideBackTransitionAnimator = null;
            isSlideBackTransition = false;
            setFragmentIsSliding(false);
            setSlideTransitionProgress(1f);
        }
    }

    private void setFragmentIsSliding(boolean sliding) {
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
            return;
        }
        if (sliding) {
            if (viewPages != null && viewPages[0] != null) {
             //   viewPages[0].setLayerType(View.LAYER_TYPE_HARDWARE, null);
                viewPages[0].setClipChildren(false);
                viewPages[0].setClipToPadding(false);
                viewPages[0].listView.setClipChildren(false);
            }

            if (actionBar != null) {
              //  actionBar.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            if (filterTabsView != null) {
               // filterTabsView.getListView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            if (fragmentView != null) {
                ((ViewGroup) fragmentView).setClipChildren(false);
                fragmentView.requestLayout();
            }
        } else {

            for (int i = 0; i < viewPages.length; i++) {
                ViewPage page = viewPages[i];
                if (page != null) {
                    page.setLayerType(View.LAYER_TYPE_NONE, null);
                    page.setClipChildren(true);
                    page.setClipToPadding(true);
                    page.listView.setClipChildren(true);
                }
            }

            if (actionBar != null) {
                actionBar.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            if (filterTabsView != null) {
                filterTabsView.getListView().setLayerType(View.LAYER_TYPE_NONE, null);
            }
            if (fragmentView != null) {
                ((ViewGroup) fragmentView).setClipChildren(true);
                fragmentView.requestLayout();
            }
        }
    }

    @Override
    public void onSlideProgress(boolean isOpen, float progress) {
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
            return;
        }
        if (isSlideBackTransition && slideBackTransitionAnimator == null) {
            setSlideTransitionProgress(progress);
        }
    }

    private void setSlideTransitionProgress(float progress) {
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
            return;
        }
        slideFragmentProgress = progress;
        if (fragmentView != null) {
            fragmentView.invalidate();
        }
        float s = 1f - 0.05f * (1f - slideFragmentProgress);

        if (filterTabsView != null) {
            filterTabsView.getListView().setScaleX(s);
            filterTabsView.getListView().setScaleY(s);
            filterTabsView.getListView().setTranslationX((isDrawerTransition ? AndroidUtilities.dp(4) : -AndroidUtilities.dp(4)) * (1f - slideFragmentProgress));
            filterTabsView.getListView().setPivotX(isDrawerTransition ? filterTabsView.getMeasuredWidth() : 0);
            filterTabsView.getListView().setPivotY(0);
            filterTabsView.invalidate();
        }
        if (rightSlidingDialogContainer != null && rightSlidingDialogContainer.getFragmentView() != null) {
            if (!rightFragmentTransitionInProgress) {
                rightSlidingDialogContainer.getFragmentView().setScaleX(s);
                rightSlidingDialogContainer.getFragmentView().setScaleY(s);
                rightSlidingDialogContainer.getFragmentView().setTranslationX((isDrawerTransition ? AndroidUtilities.dp(4) : -AndroidUtilities.dp(4)) * (1f - slideFragmentProgress));
            }
            rightSlidingDialogContainer.getFragmentView().setPivotX(isDrawerTransition ? rightSlidingDialogContainer.getMeasuredWidth() : 0);
            rightSlidingDialogContainer.getFragmentView().setPivotY(0);
        }
    }

    @Override
    public INavigationLayout.BackButtonState getBackButtonState() {
        return isArchive() || rightSlidingDialogContainer.isOpenned ? INavigationLayout.BackButtonState.BACK : INavigationLayout.BackButtonState.MENU;
    }

    @Override
    public void setProgressToDrawerOpened(float progress) {
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW || isSlideBackTransition) {
            return;
        }
        boolean drawerTransition = progress > 0;
        if (searchIsShowed) {
            drawerTransition = false;
            progress = 0;
        }
        if (drawerTransition != isDrawerTransition) {
            isDrawerTransition = drawerTransition;
            if (isDrawerTransition) {
                setFragmentIsSliding(true);
            } else {
                setFragmentIsSliding(false);
            }
            if (fragmentView != null) {
                fragmentView.requestLayout();
            }
        }
        setSlideTransitionProgress(1f - progress);
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return !((initialDialogsType == 3 && NekoConfig.showTabsOnForward.Bool()) && viewPages[0].selectedType != filterTabsView.getFirstTabId());
    }

    public void setShowSearch(String query, int i) {
        if (!searching) {
            initialSearchType = i;
            actionBar.openSearchField(query, false);
        } else {
            if (!searchItem.getSearchField().getText().toString().equals(query)) {
                searchItem.getSearchField().setText(query);
            }
            int p = searchViewPager.getPositionForType(i);
            if (p >= 0) {
                if (searchViewPager.getTabsView().getCurrentTabId() != p) {
                    searchViewPager.getTabsView().scrollToTab(p, p);
                }
            }
        }
    }

    public ActionBarMenuItem getSearchItem() {
        return searchItem;
    }

    @Override
    public boolean isLightStatusBar() {
        if (!searching && rightSlidingDialogContainer != null && rightSlidingDialogContainer.getFragment() != null) {
            return rightSlidingDialogContainer.getFragment().isLightStatusBar();
        }
        int color = (searching && whiteActionBar) ? Theme.getColor(Theme.key_windowBackgroundWhite) : Theme.getColor(folderId == 0 ? Theme.key_actionBarDefault : Theme.key_actionBarDefaultArchived);
        if (actionBar.isActionModeShowed()) {
            color = Theme.getColor(Theme.key_actionBarActionModeDefault);
        }
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    @Override
    public List<FloatingDebugController.DebugItem> onGetDebugItems() {
        return Arrays.asList(
                new FloatingDebugController.DebugItem(LocaleController.getString(R.string.DebugDialogsActivity)),
                new FloatingDebugController.DebugItem(LocaleController.getString(R.string.ClearLocalDatabase), () -> {
                    getMessagesStorage().clearLocalDatabase();
                    Toast.makeText(getContext(), LocaleController.getString(R.string.DebugClearLocalDatabaseSuccess), Toast.LENGTH_SHORT).show();
                }),
                new FloatingDebugController.DebugItem(LocaleController.getString(R.string.DebugClearSendMessageAsPeers), () -> getMessagesController().clearSendAsPeers())
        );
    }

    @Override
    public boolean closeLastFragment() {
        if (rightSlidingDialogContainer.hasFragment()) {
            rightSlidingDialogContainer.finishPreview();
            searchViewPager.updateTabs();
            return true;
        }
        return super.closeLastFragment();
    }

    public boolean getAllowGlobalSearch() {
        return allowGlobalSearch;
    }

    @Override
    public boolean canBeginSlide() {
        return !rightSlidingDialogContainer.hasFragment();
    }
}
