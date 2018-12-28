package com.melodigm.post;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.melodigm.post.common.Constants;
import com.melodigm.post.menu.CabinetActivity;
import com.melodigm.post.menu.CalendarActivity;
import com.melodigm.post.menu.StampActivity;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.AddCabinetMusicItem;
import com.melodigm.post.protocol.data.AddCabinetMusicReq;
import com.melodigm.post.protocol.data.GetOstDataReq;
import com.melodigm.post.protocol.data.GetOstDataRes;
import com.melodigm.post.protocol.data.GetPostDataReq;
import com.melodigm.post.protocol.data.GetPostDataRes;
import com.melodigm.post.protocol.data.GetPostPositionDataReq;
import com.melodigm.post.protocol.data.GetPostUserInfoRes;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.data.PushDataItem;
import com.melodigm.post.protocol.data.SetOstDeleteReq;
import com.melodigm.post.protocol.data.SetOstTitleReq;
import com.melodigm.post.protocol.data.SetPostDeleteReq;
import com.melodigm.post.protocol.data.SetPostLikeReq;
import com.melodigm.post.protocol.data.SetPostNotifyReq;
import com.melodigm.post.protocol.data.SetPostUserInfoReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.registration.RegistOstActivity;
import com.melodigm.post.registration.RegistPostActivity;
import com.melodigm.post.registration.RegistPostUserActivity;
import com.melodigm.post.registration.RegistRadioActivity;
import com.melodigm.post.service.PostRegistrationIntentService;
import com.melodigm.post.sns.SnsShareActivity;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LocationUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RandomUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.EllipsizingTextView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.MultiViewPager;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.SwipingViewPager;
import com.melodigm.post.widget.parallaxscroll.ParallaxImageView;
import com.melodigm.post.widget.parallaxscroll.ParallaxListView;
import com.melodigm.post.widget.transformer.ParallaxPagerTransformer;
import com.melodigm.post.widget.transformer.ScalePageTransformer;
import com.melodigm.post.widget.wheel.HorizontalWheelView;
import com.melodigm.post.widget.wheel.OnWheelClickedListener;
import com.melodigm.post.widget.wheel.OnWheelScrollListener;
import com.melodigm.post.widget.wheel.WheelView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PostActivity extends BaseActivity implements IOnHandlerMessage, FingerprintUiHelper.Callback {
    private GetPostUserInfoRes getPostUserInfoRes;
    private String mPopularState = "N";
    private String mGetPostDataAlignType = "BEFORE";
    private String mGetPostDataSearchDate = "";
    private boolean isQueryPostDataGetting = false;
    private String mPOST_TYPE;
    private GetPostDataRes getPostDataRes;
    private GetOstDataRes getOstDataRes;
    // 컬러 / 위치 필터
    private LinearLayout llFixColorLayout, llFixLocationLayout;
    private RelativeLayout rlFixColorBtn;
    private ImageView ivFixColorCirCle, ivOnAirIcon;
    private LetterSpacingTextView lstvFixLocationName;

    private String mHashTag, mColor, mColorHex, mPlace, mPOI;
    private HashMap<String, String> mSortData;
    private int mPagerPosition = 0, mChangePosition = 0;

    private SwipingViewPager mMainViewPager;
    private ArrayList<PostDataItem> arrPostDataItem;
    private PostAdapter mPostAdapter;
    private boolean isPostAfterDataFinish = false;
    private boolean isPostBeforeDataFinish = false;

    private LinearLayout btnOstSelectAll, btnOstPlayAll, btnOstSort, postEmptyLayout, ostSelectFooter, ostHeaderLayout, ostEmptyLayout, ostEmptyTitle, llOstListeningBtn, llOstAddBtn, llOstPutBtn, llMorePostSubjectUnderLine;
    private TextView tvOstSelectAll, postEmptyTitle, postEmptyContent, tvRightCnt, tvOstSelectCnt;
    private LetterSpacingTextView tvOstSort, tvMorePostSubject;
    private ParallaxImageView ivPostRoot;
    private ImageView btnPostWriteImage, btnPostTimeLineFooterPostWriteImage, btnOstWrite;
    private RelativeLayout ostRootLayout, btnOstHeaderMenu, btnPostWrite, btnPostTimeLineFooterPostWrite, ostBaseFooter, btnOstPopupClose, btnOstWriteMove, rlMoreLayout, rlMoreLayoutCloseBtn, rlBuyUseCouponBtn;
    private boolean isOstSelectedAll = true;
    private String mOstSortField = "REG_DATE";

    private ParallaxListView lvOstList;
    private View viewOstHeader, viewOstFooter;
    private ArrayList<OstDataItem> arrOstDataItem;
    private PostOstAdapter mPostOstAdapter;
    private ArrayList<AddCabinetMusicItem> arrAddCabinetMusicItem;

    private RelativeLayout indicatorLayout;
    private MultiViewPager mTodayHeaderMultiViewPager;
    private TodayHeaderAdapter mTodayHeaderAdapter;
    private EllipsizingTextView tvMorePostContent;

    private String mBXI;
    private PostDataItem mPostDataItem;

    // POST 타임라인
    private RelativeLayout rlPostTimeLineRootLayout, rlCalendarBtn;
    private LinearLayout llPostTimeLineHeader;
    private ParallaxImageView ivPostTimeLineRoot;
    private PostTimeLineAdapter mPostTimeLineAdapter;
    private MultiViewPager mPostTimeLineMultiViewPager;
    private PostTimeLineTimerAdapter mPostTimeLineTimerAdapter;
    private String mPostTimeLineRegDate = "";
    private boolean isGetPostDataFixed = false;
    private TextView tvPostTimeLineDay;
    private LetterSpacingTextView tvPostTimeLineWeekDayEng;
    private HorizontalWheelView mPostTimeLineTimerHWheelView;

    // TODAY 타임라인
    private RelativeLayout rlTodayTimeLineRootLayout;
    private WheelView mTodayTimeLineGridWheelView, mTodayTimeLineWheelView;
    private TodayTimeLineGridAdapter mTodayTimeLineGridAdapter;
    private TodayTimeLineAdapter mTodayTimeLineAdapter;
    private LinearLayout llTodayBtn;

    // 해외 접속 차단 레이아웃
    private RelativeLayout rlLimitServiceLayout, rlLimitServiceCloseBtn;
    private LinearLayout llLimitServiceConfirmBtn;

    // 앱 버전 코드 확인
    private int latestAppVersionCode;

    // 지문인식
    @Override
    public void onAuthenticated() {
        LogUtil.e("onAuthenticated");
        if (rlLimitServiceLayout != null) {
            rlLimitServiceLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError() {
        LogUtil.e("onError");
    }

    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }
    private ImageView mFingerPrintIcon;
    private TextView mFingerPrintStatus;
    private Stage mStage = Stage.FINGERPRINT;
    private FingerprintUiHelper mFingerprintUiHelper;
    private FingerprintManager.CryptoObject mCryptoObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        try {
            PackageInfo i = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            Constants.APP_VERSION_CODE = i.versionCode;
            Constants.APP_VERSION_NAME = i.versionName;
        } catch(PackageManager.NameNotFoundException e) {}

        latestAppVersionCode = SPUtil.getIntSharedPreference(mContext, Constants.SP_LATEST_APP_VERSION);

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        mSortData = mdbHelper.getSortData("POST");
        mdbHelper.close();

        Intent intent = getIntent();
        mPOST_TYPE = intent.getStringExtra("POST_TYPE");
        mGetPOI = intent.getStringExtra("POI");
        mGetOTI = intent.getStringExtra("OTI");
        mGetORI = intent.getStringExtra("ORI");

        if (CommonUtil.isNull(mPOST_TYPE) || !(Constants.REQUEST_TYPE_POST.equals(mPOST_TYPE) || Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE) || Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE))) {
            mPOST_TYPE = Constants.REQUEST_TYPE_POST;
        }

        // 카카오톡 / GCM 에서 클릭하여 들어온 경우
        if (intent.getData() != null && (Constants.KAKAOLINK_SCHEME_HOST.equals(intent.getData().getHost()) || Constants.SERVICE_SCHEME_HOST.equals(intent.getData().getHost()))) {
            String action = intent.getData().getQueryParameter("action");
            if (CommonUtil.isNotNull(action)) {
                onNewIntent(intent);
            }
        }

        setDisplay();
    }

    @Override
    public void onPause() {
        LogUtil.e("onPause");
        // 배경이미지 센서 제거
        //if (mPostAdapter != null) mPostAdapter.setParallaxPosition(mPagerPosition, false);
        if (ivPostRoot != null) ivPostRoot.unregisterSensorManager();
        if (ivPostTimeLineRoot != null) ivPostTimeLineRoot.unregisterSensorManager();
        if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle(false);
        super.onPause();

        // 지문인식
        if (mFingerprintUiHelper != null) {
            mFingerprintUiHelper.stopListening();
        }
    }

    @Override
    public void onResume() {
        LogUtil.e("onResume");
        super.onResume();

        // 지문인식
        if (mFingerprintUiHelper != null) {
            if (mStage == Stage.FINGERPRINT) {
                mFingerprintUiHelper.startListening(mCryptoObject);
            }
        }

        // 배경이미지 센서 등록
        //if (mPostAdapter != null) mPostAdapter.setParallaxPosition(mPagerPosition, true);

        if (CommonUtil.isNull(mGetPOI)) {
            if (mContext != null) LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, new IntentFilter("QUERY_APP_VERSION"));

            // Footer Sort Image 활성 / 비활성 처리
            if ( CommonUtil.isNull(mSortData.get("GENDER")) && CommonUtil.isNull(mSortData.get("GENERATION")) && CommonUtil.isNull(mSortData.get("TIME")) && CommonUtil.isNull(mSortData.get("DISTANCE")) ) {
                btnFooterSortImage.setImageResource(R.drawable.bt_bt_sort_nor);
                btnPostTimeLineFooterSortImage.setImageResource(R.drawable.bt_bt_sort_norb);
            } else {
                btnFooterSortImage.setImageResource(R.drawable.bt_bt_sort_rel);
                btnPostTimeLineFooterSortImage.setImageResource(R.drawable.bt_bt_sort_relb);
            }

            PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    try {
                        Bundle data = (Bundle)msg.obj;
                        OstDataItem ostDataItem = data.getParcelable("OstDataItem");
                        int MUSIC_COUNT = data.getInt("MUSIC_COUNT", 0);
                        int DURATON = data.getInt("DURATON", 0);
                        int POSITION = data.getInt("POSITION", 0);

                        if (Constants.RIGHT_COUNT == 0) {
                            llMenuBuyUseCoupon.setVisibility(View.VISIBLE);
                        } else {
                            llMenuBuyUseCoupon.setVisibility(View.GONE);
                        }

                        if (data.getBoolean("isShowLocaleDialog", false)) {
                            isFinish = false;
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                            mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onGlobalClickListener, getString(R.string.dialog_info_locale), getString(R.string.dialog_info_locale_title));
                            mPostDialog.show();
                        }

                        if (data.getBoolean("isShowMobileNetworkDialog", false)) {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                            mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, onGlobalClickListener, getString(R.string.dialog_confirm_data_network), getString(R.string.dialog_confirm_data_network_title));
                            mPostDialog.show();
                        }

                        // 메뉴 아이콘의 재생 색상을 설정한다.
                        if (isPlayerTimer != PlayerConstants.PLAYER_TIMER || isPlayerOnAir != PlayerConstants.SONG_ON_AIR) {
                            if (PlayerConstants.PLAYER_TIMER || PlayerConstants.SONG_ON_AIR) {
                                headerMenuLineSt.setBackgroundColor(Color.parseColor("#FFD73D66"));
                                headerMenuLineNd.setBackgroundColor(Color.parseColor("#FFD73D66"));
                                headerMenuLineRd.setBackgroundColor(Color.parseColor("#FFD73D66"));
                                ostHeaderMenuLineSt.setBackgroundColor(Color.parseColor("#FFD73D66"));
                                ostHeaderMenuLineNd.setBackgroundColor(Color.parseColor("#FFD73D66"));
                                ostHeaderMenuLineRd.setBackgroundColor(Color.parseColor("#FFD73D66"));

                                ivPlayerPlay.setImageResource(R.drawable.icon_mini_rplay);
                                ivPlayerPause.setImageResource(R.drawable.icon_mini_rpause);

                                if (circularSeekBar != null) circularSeekBar.setCircleProgressColor(Color.parseColor("#FFF65857"));
                            } else {
                                headerMenuLineSt.setBackgroundColor(Color.parseColor("#FF00AFD5"));
                                headerMenuLineNd.setBackgroundColor(Color.parseColor("#FF00AFD5"));
                                headerMenuLineRd.setBackgroundColor(Color.parseColor("#FF00AFD5"));
                                ostHeaderMenuLineSt.setBackgroundColor(Color.parseColor("#FF00AFD5"));
                                ostHeaderMenuLineNd.setBackgroundColor(Color.parseColor("#FF00AFD5"));
                                ostHeaderMenuLineRd.setBackgroundColor(Color.parseColor("#FF00AFD5"));

                                ivPlayerPlay.setImageResource(R.drawable.icon_mini_play);
                                ivPlayerPause.setImageResource(R.drawable.icon_mini_pause);

                                if (circularSeekBar != null) circularSeekBar.setCircleProgressColor(Color.parseColor("#FF00AFD5"));
                            }
                            isPlayerTimer = PlayerConstants.PLAYER_TIMER;
                            isPlayerOnAir = PlayerConstants.SONG_ON_AIR;
                        }

                        if (circularSeekBar != null) {
                            tvPlayerMusicCount.setText(String.valueOf(MUSIC_COUNT));
                            circularSeekBar.setMax(DURATON);
                            circularSeekBar.setProgress(POSITION);

                            if (ostDataItem != null) {
                                if (!playerSSI.equals(ostDataItem.getPOI() + ostDataItem.getSSI())) {
                                    LogUtil.e("♬ MusicService ▶ 재생 음악 변경");
                                    playerSSI = ostDataItem.getPOI() + ostDataItem.getSSI();

                                    llSlidingMenuPlayerLayout.setAlpha(1.0f);
                                    btnPlayerPrevious.setEnabled(true);
                                    btnPlayerNext.setEnabled(true);
                                    btnPlayerPlay.setEnabled(true);
                                    btnPlayerPause.setEnabled(true);
                                    tvPlayerSongName.setVisibility(View.VISIBLE);

                                    // ON AIR
                                    if (PlayerConstants.SONG_ON_AIR) {
                                        tvPlayerMusicType.setVisibility(View.GONE);
                                        ivPlayerMusicType.setVisibility(View.VISIBLE);
                                        Animation animation = new AlphaAnimation(1.0f, 0.2f);
                                        animation.setDuration(1000);
                                        animation.setRepeatMode(Animation.REVERSE);
                                        animation.setRepeatCount(Animation.INFINITE);
                                        ivPlayerMusicType.startAnimation(animation);

                                        if (Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)) {
                                            ivOnAirIcon.setVisibility(View.VISIBLE);
                                            ivOnAirIcon.startAnimation(animation);
                                        } else {
                                            ivOnAirIcon.setVisibility(View.GONE);
                                            ivOnAirIcon.clearAnimation();
                                        }
                                    } else {
                                        tvPlayerMusicType.setVisibility(View.VISIBLE);
                                        ivPlayerMusicType.setVisibility(View.GONE);
                                        ivPlayerMusicType.clearAnimation();
                                        ivOnAirIcon.setVisibility(View.GONE);
                                        ivOnAirIcon.clearAnimation();

                                        if (Constants.REQUEST_TYPE_POST.equals(ostDataItem.getPOST_TYPE())) {
                                            tvPlayerMusicType.setText(getString(R.string.post));
                                            tvPlayerMusicType.setTextColor(Color.parseColor("#FF00AFD5"));
                                        } else if (Constants.REQUEST_TYPE_RADIO.equals(ostDataItem.getPOST_TYPE())) {
                                            tvPlayerMusicType.setText(getString(R.string.radio));
                                            tvPlayerMusicType.setTextColor(Color.parseColor("#FFF65857"));
                                        } else if (Constants.REQUEST_TYPE_TODAY.equals(ostDataItem.getPOST_TYPE())) {
                                            tvPlayerMusicType.setText(getString(R.string.today));
                                            tvPlayerMusicType.setTextColor(Color.parseColor("#FFFFCC4F"));
                                        } else {
                                            tvPlayerMusicType.setText("");
                                        }
                                    }
                                    tvPlayerSongName.setText((CommonUtil.isNull(ostDataItem.getSONG_NM())) ? "" : ostDataItem.getSONG_NM());
                                    tvPlayerArtiName.setText((CommonUtil.isNull(ostDataItem.getARTI_NM())) ? "" : ostDataItem.getARTI_NM());
                                    mGlideRequestManager
                                            .load(ostDataItem.getALBUM_PATH())
                                            .error(R.drawable.icon_album_dummy)
                                            .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, mContext.getResources().getDisplayMetrics()))
                                            .into(ivCircleImage);
                                }
                            } else {
                                playerSSI = "";
                                llSlidingMenuPlayerLayout.setAlpha(0.2f);
                                btnPlayerPrevious.setEnabled(false);
                                btnPlayerNext.setEnabled(false);
                                btnPlayerPlay.setEnabled(false);
                                btnPlayerPause.setEnabled(false);
                                tvPlayerMusicType.setText("");
                                tvPlayerMusicType.setVisibility(View.VISIBLE);
                                ivPlayerMusicType.clearAnimation();
                                ivPlayerMusicType.setVisibility(View.GONE);
                                ivOnAirIcon.clearAnimation();
                                ivOnAirIcon.setVisibility(View.GONE);
                                ivCircleImage.setImageResource(R.drawable.icon_album_dummy);
                                tvPlayerSongName.setVisibility(View.GONE);
                                tvPlayerArtiName.setText(getString(R.string.msg_ost_play_list_empty));
                            }

                            if (PlayerConstants.SONG_PAUSED) {
                                btnPlayerPlay.setVisibility(View.VISIBLE);
                                btnPlayerPause.setVisibility(View.GONE);

                                if (headerMenuLineLayout.getVisibility() == View.VISIBLE) {
                                    headerMenuLineLayout.setVisibility(View.GONE);
                                    headerMenuLineSt.clearAnimation();
                                    headerMenuLineNd.clearAnimation();
                                    headerMenuLineRd.clearAnimation();
                                }

                                if (ostHeaderMenuLineLayout != null) {
                                    if (ostHeaderMenuLineLayout.getVisibility() == View.VISIBLE) {
                                        ostHeaderMenuLineLayout.setVisibility(View.GONE);
                                        ostHeaderMenuLineSt.clearAnimation();
                                        ostHeaderMenuLineNd.clearAnimation();
                                        ostHeaderMenuLineRd.clearAnimation();
                                    }
                                }
                            } else {
                                btnPlayerPlay.setVisibility(View.GONE);
                                btnPlayerPause.setVisibility(View.VISIBLE);

                                /**
                                 * YCNOTE - Animation(ScaleAnimation)
                                 */
                                ScaleAnimation scaleAnimationSt = new ScaleAnimation(0.0f, RandomUtil.getRandom(4, 10) / 10f, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
                                scaleAnimationSt.setDuration(RandomUtil.getRandom(500, 1000));
                                scaleAnimationSt.setRepeatMode(Animation.REVERSE);
                                scaleAnimationSt.setRepeatCount(Animation.INFINITE);
                                ScaleAnimation scaleAnimationNd = new ScaleAnimation(0.0f, RandomUtil.getRandom(4, 10) / 10f, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
                                scaleAnimationNd.setDuration(RandomUtil.getRandom(500, 1000));
                                scaleAnimationNd.setRepeatMode(Animation.REVERSE);
                                scaleAnimationNd.setRepeatCount(Animation.INFINITE);
                                ScaleAnimation scaleAnimationRd = new ScaleAnimation(0.0f, RandomUtil.getRandom(4, 10) / 10f, Animation.RELATIVE_TO_SELF, Animation.RELATIVE_TO_SELF);
                                scaleAnimationRd.setDuration(RandomUtil.getRandom(500, 1000));
                                scaleAnimationRd.setRepeatMode(Animation.REVERSE);
                                scaleAnimationRd.setRepeatCount(Animation.INFINITE);

                                if (headerMenuLineLayout.getVisibility() == View.GONE) {
                                    headerMenuLineLayout.setVisibility(View.VISIBLE);
                                    headerMenuLineSt.startAnimation(scaleAnimationSt);
                                    headerMenuLineNd.startAnimation(scaleAnimationNd);
                                    headerMenuLineRd.startAnimation(scaleAnimationRd);
                                }

                                if (ostHeaderMenuLineLayout != null) {
                                    if (ostHeaderMenuLineLayout.getVisibility() == View.GONE) {
                                        ostHeaderMenuLineLayout.setVisibility(View.VISIBLE);
                                        ostHeaderMenuLineSt.startAnimation(scaleAnimationSt);
                                        ostHeaderMenuLineNd.startAnimation(scaleAnimationNd);
                                        ostHeaderMenuLineRd.startAnimation(scaleAnimationRd);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.e("PROGRESSBAR_HANDLER ERROR : " + e.getMessage());
                    }
                }
            };
        }
    }

    private boolean isReceiveBroadCast = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isReceiveBroadCast) {
                isReceiveBroadCast = true;
                LogUtil.e("▶▶▶▶▶▶▶▶▶▶ RECEIVE BROADCAST ▶ QUERY_APP_VERSION");
                mHandler.sendEmptyMessage(Constants.QUERY_APP_VERSION);
                if (mContext != null) LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
            }
        }
    };

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (arrPostDataItem.size() > position) {
                mTracker.setScreenName(arrPostDataItem.get(position).getPOST_TYPE() + ":" + arrPostDataItem.get(position).getPOI() + ":" + arrPostDataItem.get(position).getPOST_SUBJ());
                mTracker.send(new HitBuilders.ScreenViewBuilder().build());

                // 배경이미지 센서 등록
                //mPostAdapter.setParallaxPosition(position, true);

                if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
                    mPostAdapter.resetPlayer(position);
                    mPostAdapter.setPosition(position);
                } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
                    mTodayHeaderMultiViewPager.setCurrentItem(position);

                    try {
                        String REG_DATE = arrPostDataItem.get(position).getREG_DATE().substring(0, 10);
                        String TODAY_REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");

                        if (REG_DATE.equals(TODAY_REG_DATE))
                            setPostHeaderTitle(getString(R.string.today), false, Color.parseColor("#FFFFD83B"));
                        else
                            setPostHeaderTitle(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4), false);
                    } catch (Exception e) {
                        LogUtil.e(e.getMessage());
                    }
                }

                if ((position + 1) == arrPostDataItem.size()) {
                    if (!isPostBeforeDataFinish && !isQueryPostDataGetting) {
                        isQueryPostDataGetting = true;
                        mGetPostDataAlignType = "BEFORE";
                        getData(Constants.QUERY_POST_DATA, "ADD");
                    }
                }
                mPagerPosition = position;
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (mPagerPosition == 0 && mMainViewPager.getCurrentItem() == 0) {
                    if (!isPostAfterDataFinish && !isQueryPostDataGetting) {
                        isQueryPostDataGetting = true;
                        mGetPostDataAlignType = "AFTER";
                        getData(Constants.QUERY_POST_DATA, "ADD");
                    }
                }
                mPagerPosition = mMainViewPager.getCurrentItem();
            }
        }
    };

    private void setDisplay() {
        // 특정 사연 보기가 아닐 경우
        if (CommonUtil.isNull(mGetPOI)) {
            setPostHeader(Constants.HEADER_TYPE_MENU, onClickListener);
            startActivityForResult(new Intent(mContext, SplashActivity.class), Constants.QUERY_APP_VERSION);
        }
        // 특정 사연 보기일 경우
        else {
            setPostHeader(Constants.HEADER_TYPE_CLOSE, onClickListener);
        }

        // FooterMenu 공유 버튼
        btnFooterShare.setOnClickListener(onClickListener);

        // FooterMenu POST 등록
        btnPostWriteImage = (ImageView)findViewById(R.id.btnPostWriteImage);
        btnPostTimeLineFooterPostWriteImage = (ImageView)findViewById(R.id.btnPostTimeLineFooterPostWriteImage);

        btnPostWrite = (RelativeLayout)findViewById(R.id.btnPostWrite);
        btnPostWrite.setOnClickListener(onClickListener);

        btnPostTimeLineFooterPostWrite = (RelativeLayout)findViewById(R.id.btnPostTimeLineFooterPostWrite);
        btnPostTimeLineFooterPostWrite.setOnClickListener(onClickListener);

        ivOnAirIcon = (ImageView)findViewById(R.id.ivOnAirIcon);
        ivOnAirIcon.setOnClickListener(onClickListener);

        // POST Empty Layout
        postEmptyLayout = (LinearLayout)findViewById(R.id.postEmptyLayout);
        postEmptyTitle = (TextView)findViewById(R.id.postEmptyTitle);
        postEmptyContent = (TextView)findViewById(R.id.postEmptyContent);

        ivPostRoot = (ParallaxImageView)findViewById(R.id.ivPostRoot);
        mMainViewPager = (SwipingViewPager)findViewById(R.id.mMainViewPager);
        indicatorLayout = (RelativeLayout)findViewById(R.id.indicatorLayout);
        mTodayHeaderMultiViewPager = (MultiViewPager)findViewById(R.id.mTodayHeaderMultiViewPager);
        ParallaxPagerTransformer parallaxPagerTransformer = new ParallaxPagerTransformer(R.id.ivRoot);
        //parallaxPagerTransformer.setBorder(20);
        parallaxPagerTransformer.setSpeed(0.5f);
        mMainViewPager.setPageTransformer(false, parallaxPagerTransformer);
        mPostAdapter = new PostAdapter(mContext, getLayoutInflater(), mHandler, mGlideRequestManager);
        mMainViewPager.setAdapter(mPostAdapter);

        ScalePageTransformer scalePageTransformer = new ScalePageTransformer();
        mTodayHeaderMultiViewPager.setPageTransformer(true, scalePageTransformer);
        mTodayHeaderAdapter = new TodayHeaderAdapter(mContext, getLayoutInflater(), mHandler);
        mTodayHeaderMultiViewPager.setAdapter(mTodayHeaderAdapter);

        mPostAdapter.setOnItemTouchListener(new PostAdapter.OnItemTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
                    MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX() / 2.0f, event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                    mTodayHeaderMultiViewPager.onTouchEvent(motionEvent);
                }
                return false;
            }
        });

        mMainViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
                    MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX() / 2.0f, event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                    mTodayHeaderMultiViewPager.onTouchEvent(motionEvent);
                }
                return false;
            }
        });

        mMainViewPager.addOnPageChangeListener(mPageChangeListener);

        // OST 팝업
        ostRootLayout = (RelativeLayout)findViewById(R.id.ostRootLayout);
        ostEmptyLayout = (LinearLayout)findViewById(R.id.ostEmptyLayout);
        ostBaseFooter = (RelativeLayout)findViewById(R.id.ostBaseFooter);
        rlBuyUseCouponBtn = (RelativeLayout)findViewById(R.id.rlBuyUseCouponBtn);
        rlBuyUseCouponBtn.setOnClickListener(onGlobalClickListener);
        ostSelectFooter = (LinearLayout)findViewById(R.id.ostSelectFooter);
        viewOstHeader = getLayoutInflater().inflate(R.layout.view_ost_header, null, false);
        viewOstFooter = getLayoutInflater().inflate(R.layout.view_ost_footer, null, false);
        tvOstSelectAll = (TextView)findViewById(R.id.tvOstSelectAll);
        tvOstSort = (LetterSpacingTextView)findViewById(R.id.tvOstSort);
        tvOstSort.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvOstSort.setText(getString(R.string.common_like));

        arrAddCabinetMusicItem = new ArrayList<>();
        lvOstList = (ParallaxListView)findViewById(R.id.lvOstList);
        lvOstList.addParallaxedHeaderView(viewOstHeader, null, false);
        lvOstList.addFooterView(viewOstFooter, null, false);
        lvOstList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                position--;
                int checkedOstDataItemCount = 0;

                arrOstDataItem.get(position).setIsChecked(!arrOstDataItem.get(position).isChecked());
                for (OstDataItem item : arrOstDataItem) {
                    if (item.isChecked()) checkedOstDataItemCount++;
                }

                // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                if (checkedOstDataItemCount > Constants.RIGHT_COUNT) {
                    tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.post));
                    rlBuyUseCouponBtn.setVisibility(View.VISIBLE);
                } else {
                    tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                    rlBuyUseCouponBtn.setVisibility(View.GONE);
                }

                tvOstSelectCnt.setText(String.valueOf(checkedOstDataItemCount));
                tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));
                mPostOstAdapter.notifyDataSetChanged();

                if (checkedOstDataItemCount > 0) {
                    ostBaseFooter.setVisibility(View.GONE);
                    ostSelectFooter.setVisibility(View.VISIBLE);
                    tvOstSelectAll.setText(getString(R.string.common_unselect_all));
                    isOstSelectedAll = false;
                } else {
                    ostBaseFooter.setVisibility(View.VISIBLE);
                    ostSelectFooter.setVisibility(View.GONE);
                    tvOstSelectAll.setText(getString(R.string.common_select_all));
                    isOstSelectedAll = true;
                }
            }
        });

        // OST 팝업 > Footer > OST 등록
        btnOstWrite = (ImageView)findViewById(R.id.btnOstWrite);
        btnOstWrite.setOnClickListener(onClickListener);

        // OST 팝업 > Header 색상 설정
        ostHeaderLayout = (LinearLayout)findViewById(R.id.ostHeaderLayout);
        ostEmptyTitle = (LinearLayout)findViewById(R.id.ostEmptyTitle);
        updateOstUI();

        // OST 팝업 > Menu
        btnOstHeaderMenu = (RelativeLayout)findViewById(R.id.btnOstHeaderMenu);
        btnOstHeaderMenu.setOnClickListener(onClickListener);

        // 특정 사연 보기일 경우
        if (CommonUtil.isNotNull(mGetPOI)) {
            btnOstHeaderMenu.setEnabled(false);
            btnOstHeaderMenu.setVisibility(View.INVISIBLE);
        }

        // OST 팝업 > 전체선택 / 전체해제
        btnOstSelectAll = (LinearLayout)findViewById(R.id.btnOstSelectAll);
        btnOstSelectAll.setOnClickListener(onClickListener);

        // OST 팝업 > 전체듣기
        btnOstPlayAll = (LinearLayout)findViewById(R.id.btnOstPlayAll);
        btnOstPlayAll.setOnClickListener(onClickListener);

        // OST 팝업 > Sort
        btnOstSort = (LinearLayout)findViewById(R.id.btnOstSort);
        btnOstSort.setOnClickListener(onClickListener);

        // OST 팝업 > 닫기
        btnOstPopupClose = (RelativeLayout)findViewById(R.id.btnOstPopupClose);
        btnOstPopupClose.setOnClickListener(onClickListener);

        // OST 팝업 > Empty > OST 등록
        btnOstWriteMove = (RelativeLayout)findViewById(R.id.btnOstWriteMove);
        btnOstWriteMove.setOnClickListener(onClickListener);

        // OST 팝업 > Footer > 카운트 / 듣기 / 추가 / 담기
        tvRightCnt = (TextView)findViewById(R.id.tvRightCnt);
        tvOstSelectCnt = (TextView)findViewById(R.id.tvOstSelectCnt);
        llOstListeningBtn = (LinearLayout)findViewById(R.id.llOstListeningBtn);
        llOstAddBtn = (LinearLayout)findViewById(R.id.llOstAddBtn);
        llOstPutBtn = (LinearLayout)findViewById(R.id.llOstPutBtn);
        llOstListeningBtn.setOnClickListener(onClickListener);
        llOstAddBtn.setOnClickListener(onClickListener);
        llOstPutBtn.setOnClickListener(onClickListener);

        // 더보기 팝업
        rlMoreLayout = (RelativeLayout)findViewById(R.id.rlMoreLayout);
        rlMoreLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlMoreLayoutCloseBtn);
        rlMoreLayoutCloseBtn.setOnClickListener(onClickListener);
        tvMorePostSubject = (LetterSpacingTextView)findViewById(R.id.tvMorePostSubject);
        llMorePostSubjectUnderLine = (LinearLayout)findViewById(R.id.llMorePostSubjectUnderLine);
        tvMorePostContent = (EllipsizingTextView)findViewById(R.id.tvMorePostContent);

        // POST 타임라인
        rlPostTimeLineRootLayout = (RelativeLayout) findViewById(R.id.rlPostTimeLineRootLayout);
        llPostTimeLineHeader = (LinearLayout) findViewById(R.id.llPostTimeLineHeader);
        ivPostTimeLineRoot = (ParallaxImageView) findViewById(R.id.ivPostTimeLineRoot);
        mPostTimeLineMultiViewPager = (MultiViewPager) findViewById(R.id.mPostTimeLineMultiViewPager);
        mPostTimeLineTimerHWheelView = (HorizontalWheelView) findViewById(R.id.mPostTimeLineTimerHWheelView);
        tvPostTimeLineDay = (TextView) findViewById(R.id.tvPostTimeLineDay);
        tvPostTimeLineWeekDayEng = (LetterSpacingTextView) findViewById(R.id.tvPostTimeLineWeekDayEng);
        tvPostTimeLineWeekDayEng.setSpacing(Constants.TEXT_VIEW_SPACING);
        mPostTimeLineRegDate = DateUtil.getCurrentDate("yyyy-MM-dd");
        rlCalendarBtn = (RelativeLayout) findViewById(R.id.rlCalendarBtn);
        rlCalendarBtn.setOnClickListener(onClickListener);

        mPostTimeLineTimerAdapter = new PostTimeLineTimerAdapter(mContext, mHandler);
        mPostTimeLineTimerHWheelView.setViewAdapter(mPostTimeLineTimerAdapter);
        mPostTimeLineTimerHWheelView.setDrawShadows(false);
        mPostTimeLineTimerHWheelView.setCyclic(true);
        mPostTimeLineTimerHWheelView.setEnabled(false);

        // 단말기의 가로 사이즈에서 아이템 사이즈(160dp)를 뺀 1/2만큼을 좌측으로 옮겨 선택 영역이 중앙이 아닌 좌측으로 변경시킨다.
        int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (DeviceUtil.getScreenWidthInDPs(mContext) - 160)  / 2, mContext.getResources().getDisplayMetrics());
        mPostTimeLineMultiViewPager.setClipToPadding(false);
        mPostTimeLineMultiViewPager.setPadding(-width, 0, width, 0);

        mPostTimeLineAdapter = new PostTimeLineAdapter(mContext, getLayoutInflater(), mHandler, mGlideRequestManager);
        mPostTimeLineMultiViewPager.setAdapter(mPostTimeLineAdapter);
        mPostTimeLineMultiViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (getPostDataRes != null && getPostDataRes.getPostDataItemList().size() > mPostTimeLineMultiViewPager.getCurrentItem()) {
                        if (rlPostTimeLineRootLayout.getVisibility() == View.VISIBLE) {
                            updatePostTimeLineHeader();

                            // POST 배경 / 지도
                            if (!"".equals(getPostDataRes.getPostDataItemList().get(mPostTimeLineMultiViewPager.getCurrentItem()).getBG_USER_PATH())) {
                                mGlideRequestManager
                                        .load(getPostDataRes.getPostDataItemList().get(mPostTimeLineMultiViewPager.getCurrentItem()).getBG_USER_PATH())
                                        .thumbnail(Constants.GLIDE_THUMBNAIL)
                                        .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                        .animate(animationObject)
                                        .into(ivPostTimeLineRoot);
                            } else {
                                mGlideRequestManager
                                        .load(getPostDataRes.getPostDataItemList().get(mPostTimeLineMultiViewPager.getCurrentItem()).getBG_PIC_PATH())
                                        .thumbnail(Constants.GLIDE_THUMBNAIL)
                                        .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                        .animate(animationObject)
                                        .into(ivPostTimeLineRoot);
                            }
                        }

                        if (mPagerPosition == 0 && mPostTimeLineMultiViewPager.getCurrentItem() == 0) {
                            if (!isPostAfterDataFinish && !isQueryPostDataGetting) {
                                isQueryPostDataGetting = true;
                                mGetPostDataAlignType = "AFTER";
                                getData(Constants.QUERY_POST_DATA, "ADD_TIMELINE");
                            }
                        }
                        mPagerPosition = mPostTimeLineMultiViewPager.getCurrentItem();
                    }
                }
            }
        });

        // TODAY 타임라인
        llTodayBtn = (LinearLayout)findViewById(R.id.llTodayBtn);
        llTodayBtn.setOnClickListener(onClickListener);

        rlTodayTimeLineRootLayout = (RelativeLayout)findViewById(R.id.rlTodayTimeLineRootLayout);

        mTodayTimeLineGridAdapter = new TodayTimeLineGridAdapter(mContext);
        mTodayTimeLineGridWheelView = (WheelView)findViewById(R.id.mTodayTimeLineGridWheelView);
        mTodayTimeLineGridWheelView.setViewAdapter(mTodayTimeLineGridAdapter);
        mTodayTimeLineGridWheelView.setDrawShadows(false);
        mTodayTimeLineGridWheelView.setCyclic(true);

        mTodayTimeLineAdapter = new TodayTimeLineAdapter(mContext, mHandler);
        mTodayTimeLineWheelView = (WheelView)findViewById(R.id.mTodayTimeLineWheelView);
        mTodayTimeLineWheelView.setViewAdapter(mTodayTimeLineAdapter);
        mTodayTimeLineWheelView.setDrawShadows(false);

        mTodayTimeLineWheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getX(), -1 * event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                mTodayTimeLineGridWheelView.onTouchEvent(motionEvent);
                return false;
            }
        });

        mTodayTimeLineWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {}
            @Override
            public void onScrollingFinished(WheelView wheel) {
                if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
                    try {
                        String REG_DATE = getPostDataRes.getPostDataItemList().get(mTodayTimeLineWheelView.getCurrentItem()).getREG_DATE().substring(0, 10);
                        String TODAY_REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");

                        if (REG_DATE.equals(TODAY_REG_DATE))
                            setPostHeaderTitle(getString(R.string.today), false, Color.parseColor("#99FFFFFF"));
                        else
                            setPostHeaderTitle(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4), false, Color.parseColor("#99FFFFFF"));
                    } catch (Exception e) {
                        LogUtil.e(e.getMessage());
                    }
                }
            }
        });

        mTodayTimeLineWheelView.addClickingListener(new OnWheelClickedListener() {
            @Override
            public void onItemClicked(WheelView wheel, int itemIndex) {
                LogUtil.e("" + itemIndex);
                mMainViewPager.setCurrentItem(itemIndex);
                mTodayHeaderMultiViewPager.setCurrentItem(itemIndex);
                ivHeaderTimeLine.setImageResource(R.drawable.bt_top_timerel);
                showLayout(rlTodayTimeLineRootLayout, false);
            }
        });

        // 컬러 / 위치 필터
        llFixColorLayout = (LinearLayout) findViewById(R.id.llFixColorLayout);
        rlFixColorBtn = (RelativeLayout) findViewById(R.id.rlFixColorBtn);
        rlFixColorBtn.setOnClickListener(onClickListener);
        ivFixColorCirCle = (ImageView) findViewById(R.id.ivFixColorCirCle);
        llFixLocationLayout = (LinearLayout) findViewById(R.id.llFixLocationLayout);
        llFixLocationLayout.setOnClickListener(onClickListener);
        lstvFixLocationName = (LetterSpacingTextView) findViewById(R.id.lstvFixLocationName);

        // 특정 사연 보기일 경우
        if (CommonUtil.isNotNull(mGetPOI)) {
            getData(Constants.QUERY_POST_DATA);
        }

        // 해외 접속 차단 레이아웃
        rlLimitServiceLayout = (RelativeLayout)findViewById(R.id.rlLimitServiceLayout);
        rlLimitServiceCloseBtn = (RelativeLayout)findViewById(R.id.rlLimitServiceCloseBtn);
        rlLimitServiceCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlLimitServiceLayout.setVisibility(View.GONE);
            }
        });
        llLimitServiceConfirmBtn = (LinearLayout)findViewById(R.id.llLimitServiceConfirmBtn);
        llLimitServiceConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlLimitServiceLayout.setVisibility(View.GONE);
            }
        });

        // 지문인식
        mFingerPrintIcon = (ImageView)findViewById(R.id.fingerprint_icon);
        mFingerPrintStatus = (TextView)findViewById(R.id.fingerprint_status);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintUiHelper = new FingerprintUiHelper(mContext.getSystemService(FingerprintManager.class), mFingerPrintIcon, mFingerPrintStatus, this);
        /*updateStage();*/
        } else {
            mFingerPrintIcon.setVisibility(View.GONE);
            mFingerPrintStatus.setVisibility(View.GONE);
        }
    }

    private void updateOstUI() {
        if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
            btnPostWrite.setVisibility(View.VISIBLE);
            btnFooterSort.setVisibility(View.VISIBLE);

            ostHeaderLayout.setBackgroundColor(Color.parseColor("#E600AFD5"));
            ostEmptyTitle.setBackgroundColor(Color.parseColor("#E600AFD5"));
            btnOstWrite.setImageResource(R.drawable.bt_rel_ost);
            btnPostWriteImage.setImageResource(R.drawable.bt_rel_post);
            btnPostTimeLineFooterPostWriteImage.setImageResource(R.drawable.bt_rel_post);
        } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
            btnPostWrite.setVisibility(View.VISIBLE);
            btnFooterSort.setVisibility(View.VISIBLE);

            ostHeaderLayout.setBackgroundColor(Color.parseColor("#E6F65857"));
            ostEmptyTitle.setBackgroundColor(Color.parseColor("#E6F65857"));
            btnOstWrite.setImageResource(R.drawable.bt_rel_radost);
            btnPostWriteImage.setImageResource(R.drawable.bt_rel_radio);
            btnPostTimeLineFooterPostWriteImage.setImageResource(R.drawable.bt_rel_radio);
        } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
            btnPostWrite.setVisibility(View.GONE);
            btnFooterSort.setVisibility(View.GONE);

            ostHeaderLayout.setBackgroundColor(Color.parseColor("#E6FFD83B"));
            ostEmptyTitle.setBackgroundColor(Color.parseColor("#E6FFD83B"));
            btnOstWrite.setImageResource(R.drawable.bt_rel_tdost);
        }

        // 특정 사연 보기이거나 POPULAR일 경우
        if (CommonUtil.isNotNull(mGetPOI) || "Y".equals(mPopularState)) {
            btnPostWrite.setVisibility(View.GONE);
            btnFooterSort.setVisibility(View.GONE);
        }

        // 특정 사연 보기가 아닐 경우
        if (CommonUtil.isNull(mGetPOI)) {
            // POPULAR일 경우
            if ("Y".equals(mPopularState))
                setPostHeader(Constants.HEADER_TYPE_MENU_POPULAR, onClickListener, mPOST_TYPE);
                // POPULAR가 아닐 경우
            else
                setPostHeader(Constants.HEADER_TYPE_MENU, onClickListener);
        }
    }

    private void showLayout(View view, boolean isShow) {
        mMainViewPager.setSwiping(!isShow);

        if (isShow) {
            if (view.getVisibility() == View.GONE) {
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(1000);
                view.setVisibility(View.VISIBLE);
                view.setAnimation(animation);
            }
        } else {
            if (view.getVisibility() == View.VISIBLE) {
                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(1000);
                view.setVisibility(View.GONE);
                view.setAnimation(animation);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            boolean isChecked = false;
            int checkedCount = 0;
            String POI = "", SSI = "";
            PostDataItem postDataItem;

            switch (v.getId()) {
                // 위치 서비스 설정 onClick
                case R.id.btnLocationServiceSetting:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, Constants.QUERY_LOCATION_SERVICE_SETTING);
                    break;
                // 컬러 필터 onClick
                case R.id.rlFixColorBtn:
                    mColor = "";
                    getData(Constants.QUERY_POST_DATA);
                    break;
                // 위치 필터 onClick
                case R.id.llFixLocationLayout:
                    mPlace = "";
                    getData(Constants.QUERY_POST_DATA);
                    break;
                // HeaderMenu / OST HeaderMenu onClick
                case R.id.btnHeaderMenu:
                case R.id.btnOstHeaderMenu:
                    if (mSlidingMenu == null) setSlidingMenu(onClickListener);
                    mSlidingMenu.showMenu();
                    break;
                // Header Title onClick
                case R.id.btnHeaderTitleLayout:
                    mHashTag = "";
                    getData(Constants.QUERY_POST_DATA);
                    break;
                // Header 타임라인 onClick
                case R.id.btnHeaderTimeLine:
                    String REG_DATE;
                    String TODAY_REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");

                    // POST or RADIO 일 경우
                    if (Constants.REQUEST_TYPE_POST.equals(mPOST_TYPE) || Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)) {
                        // 타임라인 활성화 시
                        if (rlPostTimeLineRootLayout.getVisibility() == View.GONE) {
                            ivPostTimeLineRoot.registerSensorManager();
                            setPostHeaderTitle("", true);

                            if (Constants.REQUEST_TYPE_POST.equals(mPOST_TYPE)) {
                                llPostTimeLineHeader.setBackgroundColor(Color.parseColor("#E600AFD5"));
                            } else if (Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)){
                                llPostTimeLineHeader.setBackgroundColor(Color.parseColor("#E6F65857"));
                            }
                            ivHeaderTimeLine.setImageResource(R.drawable.bt_top_timeback);
                            updatePostTimeLineHeader();
                            showLayout(postFooterLayout, false);
                            showLayout(rlPostTimeLineRootLayout, true);

                            if (getPostDataRes.getPostDataItemList().size() > 0) {
                                // POST 배경 / 지도
                                if (!"".equals(getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_USER_PATH())) {
                                    mGlideRequestManager
                                            .load(getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_USER_PATH())
                                            .thumbnail(Constants.GLIDE_THUMBNAIL)
                                            .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                            .animate(animationObject)
                                            .into(ivPostTimeLineRoot);
                                } else {
                                    mGlideRequestManager
                                            .load(getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_PIC_PATH())
                                            .thumbnail(Constants.GLIDE_THUMBNAIL)
                                            .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                            .animate(animationObject)
                                            .into(ivPostTimeLineRoot);
                                }

                                // 타임라인 선택사연을 현재사연으로 설정
                                mPostTimeLineMultiViewPager.setCurrentItem(mMainViewPager.getCurrentItem(), false);
                            }
                        }
                        // 타임라인 비활성화 시
                        else {
                            ivPostTimeLineRoot.unregisterSensorManager();
                            setPostHeaderTitle(mHashTag, true);

                            ivHeaderTimeLine.setImageResource(R.drawable.bt_top_timerel);
                            showLayout(postFooterLayout, true);
                            showLayout(rlPostTimeLineRootLayout, false);
                        }
                    }
                    // TODAY 일 경우
                    else if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
                        // 타임라인 활성화 시
                        if (rlTodayTimeLineRootLayout.getVisibility() == View.GONE) {
                            REG_DATE = getPostDataRes.getPostDataItemList().get(mTodayTimeLineWheelView.getCurrentItem()).getREG_DATE().substring(0, 10);
                            if (REG_DATE.equals(TODAY_REG_DATE))
                                setPostHeaderTitle(getString(R.string.today), false, Color.parseColor("#99FFFFFF"));
                            else
                                setPostHeaderTitle(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4), false, Color.parseColor("#99FFFFFF"));

                            ivHeaderTimeLine.setImageResource(R.drawable.bt_top_timeback);
                            showLayout(rlTodayTimeLineRootLayout, true);

                            if (getPostDataRes.getPostDataItemList().size() > 0) {
                                // 타임라인 선택사연을 현재사연으로 설정
                                mTodayTimeLineWheelView.setCurrentItem(mMainViewPager.getCurrentItem(), false);
                            }
                        }
                        // 타임라인 비활성화 시
                        else {
                            REG_DATE = getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getREG_DATE().substring(0, 10);
                            if (REG_DATE.equals(TODAY_REG_DATE))
                                setPostHeaderTitle(getString(R.string.today), false, Color.parseColor("#FFFFFFFF"));
                            else
                                setPostHeaderTitle(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4), false, Color.parseColor("#FFFFFFFF"));

                            ivHeaderTimeLine.setImageResource(R.drawable.bt_top_timerel);
                            showLayout(rlTodayTimeLineRootLayout, false);
                        }
                    }
                    break;
                // Header 닫기 onClick
                case R.id.btnHeaderClose:
                    finish();
                    break;
                // Header POPULAR onClick
                case R.id.llPopularBtn:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_POPULAR, this, mPOST_TYPE);
                    mPostDialog.show();
                    break;
                // Header POPULAR > POST / TODAY / RADIO onClick
                case R.id.llPopularPostBtn:
                case R.id.llPopularTodayBtn:
                case R.id.llPopularRadioBtn:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPOST_TYPE = (String)v.getTag(R.id.tag_post_type);
                    updateOstUI();
                    showLayout(ostRootLayout, false);
                    if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();
                    mHashTag = "";
                    mColor = "";
                    mPlace = "";
                    getData(Constants.QUERY_POST_DATA);
                    break;
                // OST 팝업 > 전체선택 / 전체해제 onClick
                case R.id.btnOstSelectAll:
                    int checkedOstDataItemCount = 0;

                    if (isOstSelectedAll) {
                        checkedOstDataItemCount = arrOstDataItem.size();

                        for (OstDataItem item : arrOstDataItem) {
                            item.setIsChecked(true);
                        }
                        ostBaseFooter.setVisibility(View.GONE);
                        ostSelectFooter.setVisibility(View.VISIBLE);
                        tvOstSelectAll.setText(getString(R.string.common_unselect_all));
                        tvOstSelectCnt.setText(String.valueOf(mPostOstAdapter.getCount()));
                        tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));
                        mPostOstAdapter.notifyDataSetChanged();
                    } else {
                        for (OstDataItem item : arrOstDataItem) {
                            item.setIsChecked(false);
                        }
                        ostBaseFooter.setVisibility(View.VISIBLE);
                        ostSelectFooter.setVisibility(View.GONE);
                        tvOstSelectAll.setText(getString(R.string.common_select_all));
                        tvOstSelectCnt.setText("0");
                        tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));

                        mPostOstAdapter.notifyDataSetChanged();
                    }

                    // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                    if (checkedOstDataItemCount > Constants.RIGHT_COUNT) {
                        tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.post));
                        rlBuyUseCouponBtn.setVisibility(View.VISIBLE);
                    } else {
                        tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                        rlBuyUseCouponBtn.setVisibility(View.GONE);
                    }

                    isOstSelectedAll = !isOstSelectedAll;
                    break;
                // OST 팝업 > 전체듣기 onClick
                case R.id.btnOstPlayAll:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAY_ALL, onClickListener);
                    mPostDialog.show();
                    break;
                // OST 팝업 > Sort onClick
                case R.id.btnOstSort:
                    if ("REG_DATE".equals(mOstSortField)) {
                        mOstSortField = "LIKE_CNT";
                        tvOstSort.setText(getString(R.string.common_time));
                    } else {
                        mOstSortField = "REG_DATE";
                        tvOstSort.setText(getString(R.string.common_like));
                    }

                    mHandler.sendEmptyMessage(Constants.QUERY_OST_DATA_VIEW);
                    break;
                // POST / OST 신고 확인 onClick onClick
                case R.id.btnNotifyConfirm:
                    if (CommonUtil.isNotNull((String)v.getTag(R.id.tag_dcre_plac_id)) && CommonUtil.isNotNull((String)v.getTag(R.id.tag_dcre_target_type)) && CommonUtil.isNotNull((String)v.getTag(R.id.tag_dcre_resn_code))) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        getData(Constants.QUERY_POST_NOTIFY, v.getTag(R.id.tag_dcre_plac_id), v.getTag(R.id.tag_dcre_target_type), v.getTag(R.id.tag_dcre_resn_code));
                    }
                    break;
                // OST 팝업 > 닫기 onClick
                case R.id.btnOstPopupClose :
                    showLayout(ostRootLayout, false);
                    break;
                // OST 등록 onClick
                case R.id.btnOstWriteMove:
                case R.id.btnOstWrite:
                    intent = new Intent(mContext, RegistOstActivity.class);
                    intent.putExtra("POI", (String)v.getTag(R.id.tag_poi));
                    intent.putExtra("BG_USER_PATH", (String)v.getTag(R.id.tag_bg_user_path));
                    intent.putExtra("BG_PIC_PATH", (String)v.getTag(R.id.tag_bg_pic_path));
                    startActivityForResult(intent, Constants.QUERY_WRITE_OST_DATA);
                    break;
                // OST 팝업 > 전체듣기 > 재생 목록에 추가하고 듣기 onClick
                case R.id.btnPlayAllAddList:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    POI = "";
                    SSI = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    for(OstDataItem item : arrOstDataItem) {
                        isChecked = true;
                        checkedCount++;
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        item.setPOST_TYPE(mPOST_TYPE);
                        mdbHelper.updateOstPlayList(item);
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        intent.putExtra("POI", POI);
                        intent.putExtra("SSI", SSI);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getString(R.string.msg_required_ost_selected));
                    }
                    break;
                // OST 팝업 > 전체듣기 > 재생 목록을 교체하고 듣기 onClick
                case R.id.btnPlayAllChangeList:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    POI = "";
                    SSI = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mdbHelper.deleteAllOstPlayList();
                    for(OstDataItem item : arrOstDataItem) {
                        isChecked = true;
                        checkedCount++;
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        item.setPOST_TYPE(mPOST_TYPE);
                        mdbHelper.updateOstPlayList(item);
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        intent.putExtra("POI", POI);
                        intent.putExtra("SSI", SSI);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getString(R.string.msg_required_ost_selected));
                    }
                    break;
                // OST 팝업 > Footer > 듣기 onClick
                case R.id.llOstListeningBtn:
                    POI = "";
                    SSI = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    for(OstDataItem item : arrOstDataItem) {
                        if (item.isChecked()) {
                            isChecked = true;
                            checkedCount++;
                            if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                                POI = item.getPOI();
                                SSI = item.getSSI();
                            }
                            item.setPOST_TYPE(mPOST_TYPE);
                            mdbHelper.updateOstPlayList(item);
                        }
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        intent.putExtra("POI", POI);
                        intent.putExtra("SSI", SSI);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getString(R.string.msg_required_ost_selected));
                    }
                    break;
                // OST 팝업 > Footer > 추가 onClick
                case R.id.llOstAddBtn:
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    for(OstDataItem item : arrOstDataItem) {
                        if (item.isChecked()) {
                            isChecked = true;
                            checkedCount++;
                            item.setPOST_TYPE(mPOST_TYPE);
                            mdbHelper.updateOstPlayList(item);
                        }
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_PUT);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getString(R.string.msg_required_ost_selected));
                    }
                    break;
                // OST 팝업 > Footer > 담기 onClick
                case R.id.llOstPutBtn:
                    for(OstDataItem item : arrOstDataItem) {
                        if (item.isChecked()) {
                            isChecked = true;
                            break;
                        }
                    }

                    if (isChecked) {
                        intent = new Intent(mContext, CabinetActivity.class);
                        intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                        startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                    } else {
                        DeviceUtil.showToast(mContext, getString(R.string.msg_required_ost_selected));
                    }
                    break;
                // 안내 타입의 확인 onClick
                case R.id.btnInfoConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    break;
                // OST 타이틀 Dialog > 한 곡만 재생 onClick
                case R.id.btnRadioTitleOnce:
                    postDataItem = getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem());
                    postDataItem.setPOST_TYPE(mPOST_TYPE);
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mdbHelper.updateOstPlayList(postDataItem);
                    mdbHelper.close();

                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getString(R.string.msg_ost_add_play_list, 1));
                    mPostDialog.show();
                    mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    startService(intent);
                    break;
                // OST 타이틀 Dialog > 연속 재생 onClick
                case R.id.btnRadioTitleCycle:
                    postDataItem = getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem());
                    PlayerConstants.SONG_ON_AIR = true;
                    PlayerConstants.SONG_ON_AIR_POI = postDataItem.getPOI();

                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_RADIO_ON_AIR, onClickListener, getString(R.string.dialog_radio_on_air));
                    mPostDialog.show();
                    mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 1000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ON_AIR);
                    startService(intent);
                    break;
                // FooterMenu POST 등록 onClick
                case R.id.btnPostWrite:
                case R.id.btnPostTimeLineFooterPostWrite:
                    if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
                        intent = new Intent(mContext, RegistPostActivity.class);
                        startActivityForResult(intent, Constants.QUERY_WRITE_POST_DATA);
                    } else {
                        if (Constants.STAMP_COUNT >= 5) {
                            intent = new Intent(mContext, RegistRadioActivity.class);
                            startActivityForResult(intent, Constants.QUERY_WRITE_POST_DATA);
                        } else {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                            mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_STAMP_REQUIRED, onClickListener);
                            mPostDialog.show();
                        }
                    }
                    break;
                // 우표 부족 Dialog 자세히 보기 onClick
                case R.id.btnDetailView:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    intent = new Intent(mContext, StampActivity.class);
                    startActivity(intent);
                    break;
                // FooterMenu Share onClick
                case R.id.btnFooterShare:
                    if (getPostDataRes != null && getPostDataRes.getPostDataItemList().size() > 0) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_SHARE, onClickListener);
                        mPostDialog.show();
                    }
                    break;
                // FooterMenu Share > SNS onClick
                case R.id.llShareImage:
                case R.id.llShareFacebook:
                case R.id.llShareTwitter:
                case R.id.llShareInstagram:
                case R.id.llShareLine:
                case R.id.llShareKakaoTalk:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    String shareType = (String)v.getTag();
                    if (CommonUtil.isNotNull(shareType)) {
                        intent = new Intent(mContext, SnsShareActivity.class);
                        intent.putExtra("POST_TYPE", mPOST_TYPE);
                        intent.putExtra("SHARE_TYPE", shareType);
                        intent.putExtra("POI", getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getPOI());
                        startActivityForResult(intent, Constants.QUERY_POST_DATA);
                    }
                    break;
                // SlidingMenu Player List 이동 onClick
                case R.id.llPlayerMusicListBtn:
                    if (!PlayerConstants.SONG_ON_AIR) {
                        intent = new Intent(mContext, PlayerActivity.class);
                        intent.putExtra("SHOW_PLAYER_LIST", true);
                        startActivity(intent);
                    }
                    break;
                // SlidingMenu Player 이동 onClick
                case R.id.llMovePlayer:
                    intent = new Intent(mContext, PlayerActivity.class);
                    intent.putExtra("SHOW_PLAYER_LIST", false);
                    startActivity(intent);
                    break;
                // SlidingMenu POST / TODAY / RADIO / POPULAR onClick
                case R.id.tvMenuPost:
                case R.id.tvMenuToday:
                case R.id.tvMenuRadio:
                case R.id.tvMenuPopular:
                    mPopularState = (String)v.getTag(R.id.tag_popular_state);
                    mPOST_TYPE = (String)v.getTag(R.id.tag_post_type);
                    updateOstUI();
                    showLayout(ostRootLayout, false);
                    if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();
                    mHashTag = "";
                    mColor = "";
                    mPlace = "";
                    getData(Constants.QUERY_POST_DATA);
                    break;
                // 더보기 팝업 > 닫기 onClick
                case R.id.rlMoreLayoutCloseBtn:
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);
                    rlMoreLayout.setVisibility(View.GONE);
                    rlMoreLayout.setAnimation(animation);
                    break;
                // POST 및 RADIO 타임라인 달력 이미지 onClick
                case R.id.rlCalendarBtn:
                    String backgroundImage = "";
                    // POST 배경 / 지도
                    if (getPostDataRes.getPostDataItemList().size() > 0) {
                        if (!"".equals(getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_USER_PATH())) {
                            backgroundImage = getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_USER_PATH();
                        } else {
                            backgroundImage = getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_PIC_PATH();
                        }
                    }

                    intent = new Intent(mContext, CalendarActivity.class);
                    intent.putExtra("TYPE", Constants.REQUEST_TYPE_POST);
                    intent.putExtra("REG_DATE", mPostTimeLineRegDate);
                    intent.putExtra("BACKGROUND_IMAGE", backgroundImage);
                    startActivityForResult(intent, Constants.QUERY_CALENDAR);
                    break;
                // TODAY 타임라인 오늘 onClick
                case R.id.llTodayBtn:
                    if (getPostDataRes.getPostDataItemList().size() > 0) {
                        mTodayTimeLineWheelView.setCurrentItem(0, true);
                    }
                    break;
                // OST 타이틀 선정 > 확인 onClick
                case R.id.btnConfirmConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    Bundle data = (Bundle)v.getTag(R.id.tag_data);
                    getData(Constants.QUERY_OST_TITLE, data);
                    break;
                // 라디오 화면의 ON AIR 아이콘 onClick
                case R.id.ivOnAirIcon:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_RADIO_ON_AIR_CLOSE, onGlobalClickListener);
                    mPostDialog.show();
                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        //if (!(queryType == Constants.QUERY_POST_DATA && args != null && args.length > 0 && args[0] instanceof String && "BEFORE".equals(mGetPostDataAlignType)) && queryType != Constants.QUERY_POST_LIKE) {
        if (queryType != Constants.QUERY_POST_LIKE) {
            if (!isFinishing()) {
                if(mProgressDialog != null) {
                    mProgressDialog.showDialog(mContext);
                }
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_USER_INFO) {
            thread = getUserInfoThread();
        } else if (queryType == Constants.QUERY_UPDATE_USER_INFO) {
            thread = setUserInfoThread();
        }
        // POST 데이터 조회 Trigger
        else if (queryType == Constants.QUERY_POST_DATA) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                thread = getPostDataThread((String)args[0]);
            } else {
                // 특정 날짜 조회가 아닐 경우
                if (!isGetPostDataFixed) {
                    setPostHeaderTitle(mHashTag, true);
                }
                thread = getPostDataThread();
            }
        }
        // POST / OST 좋아요 Trigger
        else if (queryType == Constants.QUERY_POST_LIKE) {
            if (args != null && args.length == 4 && args[0] instanceof String && args[1] instanceof String && args[2] instanceof String && args[3] instanceof Integer && (int) args[3] >= 0) {
                Bundle data = new Bundle();
                Message msg = new Message();
                data.putString("REG_PLAC_TYPE", (String) args[1]);
                data.putString("TOGGLE_YN", (String) args[2]);
                data.putInt("POSITION", (int) args[3]);
                msg.setData(data);
                msg.what = Constants.QUERY_LIKE_RESULT;
                mHandler.sendMessage(msg);

                thread = setPostLikeThread((String) args[0], (String) args[1], (String) args[2]);
            }
        }
        // OST 데이터 조회 Trigger
        else if (queryType == Constants.QUERY_OST_DATA) {
            if (args != null && args.length == 1 && args[0] instanceof String)
                thread = getOstDataThread((String)args[0]);
        }
        // POST / OST 신고 Trigger
        else if (queryType == Constants.QUERY_POST_NOTIFY) {
            if (args != null && args.length == 3 && args[0] instanceof String && args[1] instanceof String && args[2] instanceof String)
                thread = setPostNotifyThread((String)args[0], (String)args[1], (String)args[2]);
        }
        // POST 삭제 Trigger
        else if (queryType == Constants.QUERY_POST_DELETE) {
            thread = setPostDeleteThread();
        }
        // OST 삭제 Trigger
        else if (queryType == Constants.QUERY_OST_DELETE) {
            if (args != null && args.length == 2 && args[0] instanceof String && args[1] instanceof String)
                thread = setOstDeleteThread((String)args[0], (String)args[1]);
        } else if (queryType == Constants.QUERY_OST_TITLE) {
            if (args != null && args.length == 1 && args[0] instanceof Bundle) {
                thread = setOstTitleThread((Bundle)args[0]);
            }
        } else if (queryType == Constants.QUERY_ADD_CABINET) {
            thread = addCabinetMusicThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getUserInfoThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    getPostUserInfoRes = request.getPostUserInfo();
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NON);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread setUserInfoThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    SetPostUserInfoReq setPostUserInfoReq = new SetPostUserInfoReq();
                    setPostUserInfoReq.setPUSH_ID(SPUtil.getSharedPreference(mContext, Constants.SP_PUSH_ID));
                    request.setPostUserInfo(setPostUserInfoReq);
                } catch (RequestException e) {
                } catch (POSTException e) {
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getPostDataThread(final String... args) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    String flag = "";
                    // 특정 사연 보기가 아닐 경우
                    if (CommonUtil.isNull(mGetPOI)) {
                        GetPostDataReq getPostDataReq = new GetPostDataReq();
                        getPostDataReq.setPOPULAR(mPopularState);
                        getPostDataReq.setPOST_TYPE(mPOST_TYPE);
                        getPostDataReq.setDISP_TYPE(Constants.REQUEST_DISP_TYPE_PUBLIC);
                        getPostDataReq.setLOCA_LAT(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LAT));
                        getPostDataReq.setLOCA_LNG(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LNG));
                        getPostDataReq.setTAG(mHashTag);
                        getPostDataReq.setICI(mColor);
                        getPostDataReq.setPLACE(mPlace);

                        // TODAY 의 경우 필터 값들을 참조하지 않도록 한다.
                        if (!Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
                            getPostDataReq.setGENDER(mSortData.get("GENDER"));
                            getPostDataReq.setTIME_ZONE(mSortData.get("TIME"));
                            getPostDataReq.setAGE_ZONE(mSortData.get("GENERATION"));
                            getPostDataReq.setDISTANCE(mSortData.get("DISTANCE"));
                        }

                        // 추가 데이터 요청일 경우
                        if (args != null && args.length > 0 && args[0] instanceof String && getPostDataRes.getPostDataItemList().size() > 0) {
                            flag = args[0];
                            if ("BEFORE".equals(mGetPostDataAlignType)) {
                                mGetPostDataSearchDate = getPostDataRes.getPostDataItemList().get(getPostDataRes.getPostDataItemList().size() - 1).getREG_DATE();
                            } else if ("AFTER".equals(mGetPostDataAlignType)) {
                                mGetPostDataSearchDate = getPostDataRes.getPostDataItemList().get(0).getREG_DATE();
                            }

                            getPostDataReq.setSEARCH_DATE(mGetPostDataSearchDate);
                            getPostDataReq.setALIGN_TYPE(mGetPostDataAlignType);
                            GetPostDataRes temp = request.getPostData(getPostDataReq);
                            getPostDataRes.setROW_CNT(temp.getROW_CNT());
                            getPostDataRes.setTOTAL_CNT(temp.getTOTAL_CNT());

                            if ("BEFORE".equals(mGetPostDataAlignType)) {
                                isPostBeforeDataFinish = (temp.getPostDataItemList().size() == 0);
                                for (PostDataItem item : temp.getPostDataItemList()) {
                                    getPostDataRes.getPostDataItemList().add(item);
                                }
                            } else if ("AFTER".equals(mGetPostDataAlignType)) {
                                isPostAfterDataFinish = (temp.getPostDataItemList().size() == 0);
                                Collections.reverse(temp.getPostDataItemList());
                                for (int i = 0; i < temp.getPostDataItemList().size(); i++) {
                                    getPostDataRes.getPostDataItemList().add(i, temp.getPostDataItemList().get(i));
                                }
                                mChangePosition = temp.getPostDataItemList().size();
                            }
                        }
                        // 추가 데이터 요청이 아닐 경우
                        else {
                            // 특정 날짜 조회의 경우
                            if (isGetPostDataFixed) {
                                getPostDataReq.setSEARCH_DATE(mPostTimeLineRegDate + " 23:59:59:99.999999");
                            }
                            // 특정 날짜 조회가 아닐 경우
                            else {
                                /*mGetPostDataSearchDate = DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss.SSSSSS");
                                getPostDataReq.setSEARCH_DATE(mGetPostDataSearchDate);*/
                            }
                            getPostDataRes = request.getPostData(getPostDataReq);

                            if (getPostDataRes.getPostDataItemList().size() == 0) {
                                isPostBeforeDataFinish = true;
                                isPostAfterDataFinish = true;
                            } else {
                                isPostBeforeDataFinish = false;
                                isPostAfterDataFinish = false;
                            }
                        }
                    }
                    // 특정 사연 보기일 경우
                    else {
                        GetPostPositionDataReq getPostPositionDataReq = new GetPostPositionDataReq();
                        getPostPositionDataReq.setPOI(mGetPOI);
                        getPostPositionDataReq.setPOI_MOVE_FLAG("MF");
                        mPostDataItem = request.getPostPositionData(getPostPositionDataReq);
                        getPostDataRes = new GetPostDataRes();
                        getPostDataRes.addPostDataItem(mPostDataItem);
                        isPostBeforeDataFinish = true;
                        isPostAfterDataFinish = true;
                    }

                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putString("FLAG", flag);
                    msg.setData(data);
                    msg.what = Constants.QUERY_POST_DATA_VIEW;
                    mHandler.sendMessage(msg);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                } finally {
                    isQueryPostDataGetting = false;
                }
            }
        });
        thread.start();
        return thread;
    }

    // POST / OST 좋아요 Thread
    public RunnableThread setPostLikeThread(final String REG_PLAC_ID, final String REG_PLAC_TYPE, final String TOGGLE_YN) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    String updateToggle = "Y".equalsIgnoreCase(TOGGLE_YN) ? "N" : "Y";
                    SetPostLikeReq setPostLikeReq = new SetPostLikeReq();
                    setPostLikeReq.setREG_PLAC_ID(REG_PLAC_ID);
                    setPostLikeReq.setREG_PLAC_TYPE(REG_PLAC_TYPE);
                    setPostLikeReq.setICI(SPUtil.getSharedPreference(mContext, Constants.SP_ICI_LIKE));
                    setPostLikeReq.setTOGGLE_YN(updateToggle);
                    request.setPostLike(setPostLikeReq);
                } catch (RequestException e) {
                } catch (POSTException e) {
                }
            }
        });
        thread.start();
        return thread;
    }

    // OST 데이터 조회 Thread
    public RunnableThread getOstDataThread(final String POI) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetOstDataReq getOstDataReq = new GetOstDataReq();
                    getOstDataReq.setPOI(POI);
                    getOstDataRes = request.getOstData(getOstDataReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_OST_DATA_VIEW);
                } catch (RequestException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { showLayout(ostRootLayout, false); }
                    });

                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { showLayout(ostRootLayout, false); }
                    });

                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    // POST / OST 신고 Thread
    public RunnableThread setPostNotifyThread(final String DCRE_PLAC_ID, final String DCRE_TARGET_TYPE, final String DCRE_RESN_CODE) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    SetPostNotifyReq setPostNotifyReq = new SetPostNotifyReq();
                    setPostNotifyReq.setDCRE_PLAC_ID(DCRE_PLAC_ID);
                    setPostNotifyReq.setDCRE_TARGET_TYPE(DCRE_TARGET_TYPE);
                    setPostNotifyReq.setDCRE_RESN_CODE(DCRE_RESN_CODE);
                    request.setPostNotify(setPostNotifyReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_NOTIFY_RESULT);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    // POST 삭제 Thread
    public RunnableThread setPostDeleteThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    SetPostDeleteReq setPostDeleteReq = new SetPostDeleteReq();
                    setPostDeleteReq.setPOI(mPOI);
                    request.setPostDelete(setPostDeleteReq);
                    if (CommonUtil.isNull(mGetPOI)) {
                        mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setResult(Constants.RESULT_SUCCESS);
                                finish();
                            }
                        });
                    }
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }
    // OST 삭제 Thread
    public RunnableThread setOstDeleteThread(final String POI, final String OTI) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    SetOstDeleteReq setOstDeleteReq = new SetOstDeleteReq();
                    setOstDeleteReq.setPOI(POI);
                    setOstDeleteReq.setOTI(OTI);
                    request.setOstDelete(setOstDeleteReq);

                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putString("POI", POI);
                    msg.setData(data);
                    msg.what = Constants.QUERY_OST_DATA;

                    mHandler.sendMessage(msg);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread setOstTitleThread(final Bundle data) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    SetOstTitleReq setOstTitleReq = new SetOstTitleReq();
                    setOstTitleReq.setPOI(data.getString("POI"));
                    setOstTitleReq.setOTI(data.getString("OTI"));
                    setOstTitleReq.setSSI(data.getString("SSI"));

                    setOstTitleReq.setTITL_TOGGLE_YN("Y".equals(data.getString("TITL_TOGGLE_YN")) ? "N" : "Y");
                    request.setOstTitle(setOstTitleReq);

                    Message msg = new Message();
                    msg.setData(data);
                    msg.what = Constants.QUERY_OST_DATA_UPDATE;
                    mHandler.sendMessage(msg);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread addCabinetMusicThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    arrAddCabinetMusicItem.clear();
                    for (OstDataItem item : arrOstDataItem) {
                        if (item.isChecked()) {
                            AddCabinetMusicItem addCabinetMusicItem = new AddCabinetMusicItem();
                            addCabinetMusicItem.setSSI(item.getSSI());
                            addCabinetMusicItem.setOTI(item.getOTI());
                            arrAddCabinetMusicItem.add(addCabinetMusicItem);
                        }
                    }

                    AddCabinetMusicReq addCabinetMusicReq = new AddCabinetMusicReq();
                    addCabinetMusicReq.setBXI(mBXI);
                    addCabinetMusicReq.setAddCabinetMusicItem(arrAddCabinetMusicItem);
                    request.addCabinetMusic(addCabinetMusicReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_ADD_CABINET);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        Intent intent;
        Bundle data = msg.getData();
        int likePosition;

        switch (msg.what) {
            // App version 조회 후 GCM 등록 확인 Handler
            case Constants.QUERY_APP_VERSION:
                // TODO : 인트로 동영상 확인 처리.
                //checkIntroBgFile();
                String registrationId = SPUtil.getSharedPreference(mContext, Constants.SP_PUSH_ID);
                //if (CommonUtil.isNull(registrationId))
                getInstanceIdToken();

                mHandler.sendEmptyMessage(Constants.QUERY_REGIST_USER);
                break;
            // GCM 등록 확인 후 사용자 정보 등록 확인 Handler
            case Constants.QUERY_REGIST_USER:
                mUserId = SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID);

                // 사용자 정보 등록이 안되어 있을경우.
                if (CommonUtil.isNull(mUserId))
                    startActivityForResult(new Intent(this, RegistPostUserActivity.class), Constants.QUERY_REGIST_USER);
                    // 사용자 정보 등록이 되어 있을경우.
                else
                    mHandler.sendEmptyMessage(Constants.QUERY_USER_INFO);
                break;
            // 사용자 정보 등록 확인 후 사용자 정보 조회 Handler
            case Constants.QUERY_USER_INFO:
                getData(Constants.QUERY_USER_INFO);
                break;
            // 사용자 정보 조회 후 POST 데이터 조회 Handler
            case Constants.DIALOG_EXCEPTION_NON:
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_GENDER, getPostUserInfoRes.getGENDER());
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_BIRTH_YEAR, getPostUserInfoRes.getBIRTHDATE());
                SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_ID, getPostUserInfoRes.getACCOUNT_ID());
                SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE, getPostUserInfoRes.getACCOUNT_AUTH_TYPE());

                // 등록 된 PUSH ID 와 조회 된 PUSH ID 가 다를 경우 사용자 디바이스 정보 수정을 요청한다.
                boolean isPushExist = false;
                for (PushDataItem item : getPostUserInfoRes.getPushDataItemList()) {
                    if (SPUtil.getSharedPreference(mContext, Constants.SP_PUSH_ID).equals(item.getPUSH_ID())) {
                        isPushExist = true;
                        break;
                    }
                }

                if (!isPushExist) {
                    mHandler.sendEmptyMessage(Constants.QUERY_UPDATE_USER_INFO);
                }

                // 위치 서비스 설정
                if ("".equals(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LAT)) || "".equals(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LNG))) {
                    mLocationUtil = new LocationUtil(mContext, mHandler);
                    if (mLocationUtil.isLocationEnabled()) {
                        mLocationUtil.run();
                    } else {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_LOCATION_SERVICE, onClickListener);
                        mPostDialog.show();
                    }
                }

                // 해외 접속 차단 레이아웃
                if ("KR".equalsIgnoreCase(SPUtil.getSharedPreference(mContext, Constants.SP_COUNTRY_CODE)))
                    rlLimitServiceLayout.setVisibility(View.VISIBLE);

                // 앱 버전 코드 확인
                if (Constants.APP_VERSION_CODE < latestAppVersionCode)
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);

                mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA);
                break;
            case Constants.DIALOG_APP_CLOSE:
                isFinish = false;
                break;
            case Constants.QUERY_UPDATE_USER_INFO:
                getData(Constants.QUERY_UPDATE_USER_INFO);
                break;
            // 위치 서비스 조회 성공 후 Handler
            case Constants.QUERY_LOCATION_CHANGE:
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, msg.getData().getString("USER_LAT", ""));
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, msg.getData().getString("USER_LNG", ""));
                mLocationUtil.stopLocationUpdate();
                break;
            // POST 데이터 조회 Handler
            case Constants.QUERY_POST_DATA:
                if (CommonUtil.isNull(mGetPOI)) {
                    String HASHTAGS = data.getString("HASHTAGS");
                    String ICI = data.getString("ICI");
                    String COLOR_HEX = data.getString("COLOR_HEX");
                    String PLACE = data.getString("PLACE");

                    if (HASHTAGS != null) mHashTag = HASHTAGS;
                    if (ICI != null) mColor = ICI;
                    if (COLOR_HEX != null) mColorHex = COLOR_HEX;
                    if (PLACE != null) mPlace = PLACE;

                    if (CommonUtil.isNull(mPlace)) {
                        ivPostRoot.setImageResource(R.color.transparent);
                        ivPostRoot.unregisterSensorManager();
                    } else {
                        if (!"".equals(getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_MAP_PATH())) {
                            mGlideRequestManager
                                    .load(getPostDataRes.getPostDataItemList().get(mMainViewPager.getCurrentItem()).getBG_MAP_PATH())
                                    .thumbnail(Constants.GLIDE_THUMBNAIL)
                                    .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                    .animate(animationObject)
                                    .into(ivPostRoot);
                            ivPostRoot.registerSensorManager();
                        }
                    }

                    getData(Constants.QUERY_POST_DATA);
                }
                break;
            // POST 데이터 추가 조회 Handler
            case Constants.QUERY_POST_DATA_ADD:
                if (!isPostBeforeDataFinish && !isQueryPostDataGetting) {
                    isQueryPostDataGetting = true;
                    mGetPostDataAlignType = "BEFORE";
                    getData(Constants.QUERY_POST_DATA, "ADD_TIMELINE");
                }
                break;
            // POST 데이터 조회 성공 Handler
            case Constants.QUERY_POST_DATA_VIEW:
                String FLAG = data.getString("FLAG");
                setDataView(FLAG);
                break;
            // POST 및 RADIO 타임라인 ITEM Click Handler
            case Constants.QUERY_TIME_LINE:
                int position = data.getInt("POSITION", -1);
                if (position >= 0) {
                    mMainViewPager.setCurrentItem(position, false);
                    btnHeaderTimeLine.performClick();
                }
                break;
            // 더보기 Handler
            case Constants.QUERY_POST_DATA_MORE:
                if (CommonUtil.isNull(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_SUBJ())) {
                    tvMorePostSubject.setAlpha(0.5f);
                    tvMorePostSubject.setSpacing(Constants.TEXT_VIEW_SPACING);
                    tvMorePostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                    tvMorePostSubject.setTextColor(Color.parseColor("#FFFFFFFF"));

                    if (Constants.REQUEST_TYPE_POST.equals(mPOST_TYPE)) {
                        tvMorePostSubject.setText(mContext.getString(R.string.post_story));
                    } else if (Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)) {
                        tvMorePostSubject.setText(mContext.getString(R.string.post_radio));
                    }
                } else {
                    tvMorePostSubject.setAlpha(1.0f);
                    tvMorePostSubject.setSpacing(0.0f);
                    tvMorePostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
                    tvMorePostSubject.setText(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_SUBJ());
                    tvMorePostSubject.setTextColor(Color.parseColor("#FF" + mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getCOLOR_HEX()));
                }

                tvMorePostContent.setHandler(mHandler);
                tvMorePostContent.setText(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_CONT());

                // 제목 / 내용 정렬
                if (Constants.REQUEST_POST_PST_TYPE_LEFT.equals(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_PST_TYPE()) || CommonUtil.isNull(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_PST_TYPE())) {
                    tvMorePostSubject.setGravity(Gravity.START|Gravity.CENTER);
                    llMorePostSubjectUnderLine.setGravity(Gravity.START);
                    tvMorePostContent.setGravity(Gravity.TOP|Gravity.START);
                } else if (Constants.REQUEST_POST_PST_TYPE_CENTER.equals(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_PST_TYPE())) {
                    tvMorePostSubject.setGravity(Gravity.CENTER|Gravity.CENTER);
                    llMorePostSubjectUnderLine.setGravity(Gravity.CENTER);
                    tvMorePostContent.setGravity(Gravity.TOP|Gravity.CENTER);
                } else if (Constants.REQUEST_POST_PST_TYPE_RIGHT.equals(mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getPOST_PST_TYPE())) {
                    tvMorePostSubject.setGravity(Gravity.END|Gravity.CENTER);
                    llMorePostSubjectUnderLine.setGravity(Gravity.END);
                    tvMorePostContent.setGravity(Gravity.TOP|Gravity.END);
                }

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(500);
                rlMoreLayout.setVisibility(View.VISIBLE);
                rlMoreLayout.setAnimation(animation);
                break;
            // POST 좋아요 Handler
            case Constants.QUERY_POST_LIKE:
                likePosition = data.getInt("POSITION", -1);
                if (arrPostDataItem != null && likePosition >= 0 && arrPostDataItem.size() > likePosition && CommonUtil.isNotNull(arrPostDataItem.get(likePosition).getPOI())) {
                    getData(Constants.QUERY_POST_LIKE, arrPostDataItem.get(likePosition).getPOI(), Constants.REQUEST_REG_PLAC_TYPE_POST, arrPostDataItem.get(likePosition).getLIKE_TOGGLE_YN(), likePosition);
                }
                break;
            // OST 좋아요 Handler
            case Constants.QUERY_OST_LIKE:
                likePosition = data.getInt("POSITION", -1);
                if (arrOstDataItem != null && likePosition >= 0 && arrOstDataItem.size() > likePosition && CommonUtil.isNotNull(arrOstDataItem.get(likePosition).getPOI())) {
                    getData(Constants.QUERY_POST_LIKE, arrOstDataItem.get(likePosition).getOTI(), Constants.REQUEST_REG_PLAC_TYPE_OST, arrOstDataItem.get(likePosition).getLIKE_TOGGLE_YN(), likePosition);
                }
                break;
            // POST / OST 좋아요 성공 Handler
            case Constants.QUERY_LIKE_RESULT:
                int POSITION = data.getInt("POSITION", 0);
                String REG_PLAC_TYPE = data.getString("REG_PLAC_TYPE", "");
                String TOGGLE_YN = "Y".equalsIgnoreCase(data.getString("TOGGLE_YN", "N")) ? "N" : "Y";
                int likeCount = ("Y".equals(TOGGLE_YN)) ? 1 : -1;

                // POST 좋아요 성공 Handler
                if (Constants.REQUEST_REG_PLAC_TYPE_POST.equals(REG_PLAC_TYPE)) {
                    arrPostDataItem.get(POSITION).setLIKE_TOGGLE_YN(TOGGLE_YN);
                    arrPostDataItem.get(POSITION).setLIKE_CNT(arrPostDataItem.get(POSITION).getLIKE_CNT() + likeCount);

                    if (arrPostDataItem.get(POSITION).getLlPostLikeBtn() != null) {
                        for ( int i = 0; i < arrPostDataItem.get(POSITION).getLlPostLikeBtn().getChildCount();  i++ ) {
                            View view = arrPostDataItem.get(POSITION).getLlPostLikeBtn().getChildAt(i);
                            if (view.getId() != View.NO_ID) {
                                if ("btnPostLike".equalsIgnoreCase(view.getResources().getResourceEntryName(view.getId()))) {
                                    if ("Y".equals(arrPostDataItem.get(POSITION).getLIKE_TOGGLE_YN()))
                                        ((ImageView)view).setImageResource(R.drawable.icon_like_rel);
                                    else
                                        ((ImageView)view).setImageResource(R.drawable.icon_like_nor);

                                    view.invalidate();
                                } else if ("tvPostLikeCount".equalsIgnoreCase(view.getResources().getResourceEntryName(view.getId()))) {
                                    ((TextView)view).setText(String.valueOf(arrPostDataItem.get(POSITION).getLIKE_CNT()));

                                    view.invalidate();
                                }
                            }
                        }
                    }
                }
                // OST 좋아요 성공 Handler
                else if (Constants.REQUEST_REG_PLAC_TYPE_OST.equals(REG_PLAC_TYPE)) {
                    arrOstDataItem.get(POSITION).setLIKE_TOGGLE_YN(TOGGLE_YN);
                    arrOstDataItem.get(POSITION).setLIKE_CNT(String.valueOf(Integer.valueOf(arrOstDataItem.get(POSITION).getLIKE_CNT()) + likeCount));
                    mPostOstAdapter.notifyDataSetChanged();
                }
                break;
            // POST 신고 Handler
            case Constants.QUERY_POST_NOTIFY :
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTIFY, onClickListener, data.getString("POI", ""), Constants.REQUEST_DCRE_TARGET_TYPE_POST);
                mPostDialog.show();
                break;
            // OST 신고 Handler
            case Constants.QUERY_OST_NOTIFY :
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTIFY, onClickListener, data.getString("OTI", ""), Constants.REQUEST_DCRE_TARGET_TYPE_OST);
                mPostDialog.show();
                break;
            // POST / OST 신고 성공 Handler
            case Constants.QUERY_NOTIFY_RESULT:
                mPostAdapter.notifyDataSetChanged();
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onClickListener, getString(R.string.dialog_info_notify));
                mPostDialog.show();
                break;
            // POST 삭제 Handler
            case Constants.QUERY_POST_DELETE:
                mPOI = data.getString("POI");
                getData(Constants.QUERY_POST_DELETE);
                break;
            // OST 삭제 Handler
            case Constants.QUERY_OST_DELETE:
                getData(Constants.QUERY_OST_DELETE, data.getString("POI", ""), data.getString("OTI", ""));
                break;
            // OST 데이터 조회 Handler
            case Constants.QUERY_OST_DATA:
                btnOstWriteMove.setTag(R.id.tag_poi, data.getString("POI", ""));
                btnOstWriteMove.setTag(R.id.tag_bg_user_path, arrPostDataItem.get(mMainViewPager.getCurrentItem()).getBG_USER_PATH());
                btnOstWriteMove.setTag(R.id.tag_bg_pic_path, arrPostDataItem.get(mMainViewPager.getCurrentItem()).getBG_PIC_PATH());
                btnOstWrite.setTag(R.id.tag_poi, data.getString("POI", ""));
                btnOstWrite.setTag(R.id.tag_bg_user_path, arrPostDataItem.get(mMainViewPager.getCurrentItem()).getBG_USER_PATH());
                btnOstWrite.setTag(R.id.tag_bg_pic_path, arrPostDataItem.get(mMainViewPager.getCurrentItem()).getBG_PIC_PATH());
                getData(Constants.QUERY_OST_DATA, data.getString("POI", ""));
                break;
            // OST 데이터 조회 성공 Handler
            case Constants.QUERY_OST_DATA_VIEW :
                arrOstDataItem = new ArrayList<>();
                mPostOstAdapter = new PostOstAdapter(mContext, R.layout.adapter_ost, arrOstDataItem, mHandler, mGlideRequestManager);
                lvOstList.setAdapter(mPostOstAdapter);

                ostBaseFooter.setVisibility(View.VISIBLE);
                ostSelectFooter.setVisibility(View.GONE);
                tvOstSelectAll.setText(getString(R.string.common_select_all));
                isOstSelectedAll = true;

                if (getOstDataRes.getOstDataItemList().size() > 0) {
                    ostEmptyLayout.setVisibility(View.GONE);
                    lvOstList.setVisibility(View.VISIBLE);
                    btnOstSelectAll.setAlpha(1.0f);
                    btnOstSelectAll.setOnClickListener(onClickListener);
                    btnOstPlayAll.setAlpha(1.0f);
                    btnOstPlayAll.setOnClickListener(onClickListener);

                    boolean isChecked = false;
                    for (OstDataItem item : getOstDataRes.getOstDataItemList()) {
                        arrOstDataItem.add(item);
                        if (!isChecked) {
                            if (item.isChecked()) isChecked = true;
                        }
                    }

                    /**
                     * YCNOTE - ParallaxedHeaderView
                     */
                    /*ivOstHeader.setImageBitmap(ImageUtil.getDrawingCache(mMainViewPager));

                    int deviceHeight = DeviceUtil.getScreenHeightInDPs(mContext);
                    int itemHeight = 161;
                    float ivOstHeaderHeightVal;
                    if (arrOstDataItem.size() == 0) {
                        ivOstHeaderHeightVal = deviceHeight;
                    } else if (arrOstDataItem.size() == 1) {
                        ivOstHeaderHeightVal = deviceHeight - itemHeight;
                    } else {
                        ivOstHeaderHeightVal = deviceHeight - (itemHeight * 2);
                    }

                    final int ivOstHeaderHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ivOstHeaderHeightVal, getResources().getDisplayMetrics());
                    AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ivOstHeaderHeight);
                    ivOstHeader.setLayoutParams(layoutParams);*/

                    if ("REG_DATE".equals(mOstSortField)) {
                        RegDateCompare regDateCompare = new RegDateCompare();
                        Collections.sort(arrOstDataItem, regDateCompare);
                    } else {
                        LikeCountCompare likeCountCompare = new LikeCountCompare();
                        Collections.sort(arrOstDataItem, likeCountCompare);
                    }

                    mPostOstAdapter.setPostType(mPOST_TYPE);
                    mPostOstAdapter.notifyDataSetChanged();

                    if (isChecked) {
                        ostBaseFooter.setVisibility(View.GONE);
                        ostSelectFooter.setVisibility(View.VISIBLE);
                    } else {
                        ostBaseFooter.setVisibility(View.VISIBLE);
                        ostSelectFooter.setVisibility(View.GONE);
                    }

                    // 알림바에서 대댓글 이동 알림을 클릭하여 들어온 경우
                    if (CommonUtil.isNotNull(mGetOTI)) {
                        data = new Bundle();
                        msg = new Message();
                        data.putString("OTI", mGetOTI);
                        data.putString("ORI", mGetORI);
                        msg.setData(data);
                        msg.what = Constants.QUERY_OST_REPLE;
                        mHandler.sendMessage(msg);
                        mGetOTI = "";
                        mGetORI = "";
                    }
                } else {
                    ostEmptyLayout.setVisibility(View.VISIBLE);
                    lvOstList.setVisibility(View.GONE);
                    btnOstSelectAll.setAlpha(0.5f);
                    btnOstSelectAll.setOnClickListener(null);
                    btnOstPlayAll.setAlpha(0.5f);
                    btnOstPlayAll.setOnClickListener(null);
                }

                showLayout(ostRootLayout, true);
                break;
            // OST 타이틀 선정 Handler
            case Constants.QUERY_OST_TITLE:
                if ("N".equalsIgnoreCase(data.getString("TITL_TOGGLE_YN"))) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, onClickListener, getString(R.string.dialog_confirm_ost_title), data);
                    mPostDialog.show();
                }
                break;
            // OST 타이틀 선정 성공 Handler
            case Constants.QUERY_OST_DATA_UPDATE:
                setOstDataUpdate(data.getInt("POSITION"), "Y".equals(data.getString("TITL_TOGGLE_YN")) ? "N" : "Y");
                break;
            // OST 대댓글 이동 Handler
            case Constants.QUERY_OST_REPLE:
                String OTI = data.getString("OTI");
                String ORI = data.getString("ORI");
                intent = new Intent(mContext, OstRepleActivity.class);
                intent.putExtra("POST_TYPE", mPOST_TYPE);
                intent.putExtra("OTI", OTI);
                intent.putExtra("ORI", ORI);
                startActivityForResult(intent, Constants.QUERY_OST_REPLE);
                break;
            // OST 타이틀 Handler
            case Constants.QUERY_OST_ADD_PLAY_LIST:
                if (CommonUtil.isNotNull(mPOST_TYPE)) {
                    if (Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_RADIO_TITLE, onClickListener);
                        mPostDialog.show();
                    } else {
                        PostDataItem postDataItem = data.getParcelable("PostDataItem");
                        postDataItem.setPOST_TYPE(mPOST_TYPE);
                        mdbHelper = new PostDatabases(mContext);
                        mdbHelper.open();
                        mdbHelper.updateOstPlayList(postDataItem);
                        mdbHelper.close();

                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getResources().getString(R.string.msg_ost_add_play_list, 1));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        startService(intent);
                    }
                }
                break;
            // 보관함에 담기 Handler
            case Constants.QUERY_ADD_CABINET:
                DeviceUtil.showToast(mContext, getString(R.string.msg_add_cabinet_result));
                break;
            // Slide Menu 생성 Handler
            case Constants.QUERY_SLIDE_MENU:
                if (mSlidingMenu == null) setSlidingMenu(onClickListener);
                break;
            // Notice Dialog Dismiss Handler
            case Constants.DIALOG_TYPE_NOTICE_CLOSE:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, true).show();
                }
                break;
            case Constants.DIALOG_EXCEPTION_POST:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, !"0801".equals(mPOSTException.getCode())).show();
                }
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, true).show();
                }
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, true).show();
                }
                break;
        }
    }

    private void setDataView(String flag) {
        // 더보기 화면을 비활성 시킨다.
        if (rlMoreLayout.getVisibility() == View.VISIBLE) {
            Animation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(500);
            rlMoreLayout.setVisibility(View.GONE);
            rlMoreLayout.setAnimation(animation);
        }

        arrPostDataItem = getPostDataRes.getPostDataItemList();
        mPostAdapter.setAdapterColor(mColor);
        mPostAdapter.setShowMap((CommonUtil.isNotNull(mPlace)) ? true : false);
        mPostAdapter.setPostType(mPOST_TYPE);
        mPostAdapter.setGetPOI(mGetPOI);
        mPostAdapter.addAllItems(arrPostDataItem);
        mPostAdapter.notifyDataSetChanged();

        // POST / RADIO 데이터 요청일 경우
        if (Constants.REQUEST_TYPE_POST.equals(mPOST_TYPE) || Constants.REQUEST_TYPE_RADIO.equals(mPOST_TYPE)) {
            mPostTimeLineAdapter.addAllItems(arrPostDataItem);
            mPostTimeLineAdapter.notifyDataSetChanged();
            if (rlPostTimeLineRootLayout.getVisibility() == View.VISIBLE)
                updatePostTimeLineHeader();
            indicatorLayout.setVisibility(View.GONE);
        }
        // TODAY 데이터 요청일 경우
        else if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
            mTodayTimeLineAdapter.addAllItems(arrPostDataItem);
            mTodayHeaderAdapter.setAdapterColor(mColor);
            mTodayHeaderAdapter.addAllItems(arrPostDataItem);
            mTodayHeaderAdapter.notifyDataSetChanged();
            indicatorLayout.setVisibility(View.VISIBLE);
        }

        // 조회 된 데이터가 있을 경우
        if (getPostDataRes.getPostDataItemList().size() > 0) {
            postEmptyLayout.setVisibility(View.GONE);

            // 추가 데이터 요청이 아닐 경우
            if (CommonUtil.isNull(flag)) {
                mPageChangeListener.onPageSelected(0);
                mMainViewPager.setCurrentItem(0, false);
                //rlTodayTimeLineRootLayout.setVisibility(View.GONE);

                // 특정 날짜 조회의 경우
                if (isGetPostDataFixed) {
                    isGetPostDataFixed = false;
                } else {
                    if (CommonUtil.isNull(mHashTag)) postHeaderLayout.setBackgroundColor(Color.parseColor("#00000000"));
                    //ivHeaderTimeLine.setImageResource(R.drawable.bt_top_timerel);
                    //showLayout(postFooterLayout, true);
                    //showLayout(rlPostTimeLineRootLayout, false);
                    //showLayout(rlTodayTimeLineRootLayout, false);
                }

                if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE) && arrPostDataItem.size() > 0) {
                    mTodayHeaderMultiViewPager.setCurrentItem(0, false);
                    mTodayTimeLineWheelView.setCurrentItem(0, false);
                }
            }

            // AFTER 타입의 추가 데이터 요청일 경우 포지션 값을 추가 된 데이터 만큼 이동시킨다.
            if (mChangePosition > 0) {
                mMainViewPager.setCurrentItem(mMainViewPager.getCurrentItem() + mChangePosition, false);
                mPostTimeLineMultiViewPager.setCurrentItem(mPostTimeLineMultiViewPager.getCurrentItem() + mChangePosition, false);
                mChangePosition = 0;
            }

            // 컬러 필터
            if (CommonUtil.isNull(mColor) || mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
                llFixColorLayout.setVisibility(View.GONE);
            } else {
                llFixColorLayout.setVisibility(View.VISIBLE);
                ivFixColorCirCle.setBackground(new ColorCircleDrawable(Color.parseColor("#" + mColorHex)));
            }

            // 위치 필터
            if (CommonUtil.isNull(mPlace)) {
                ivPostRoot.setImageResource(android.R.color.transparent);
                llFixLocationLayout.setVisibility(View.GONE);
            } else {
                llFixLocationLayout.setVisibility(View.VISIBLE);
                lstvFixLocationName.setText(mPlace);
            }

            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
                try {
                    String REG_DATE = mPostAdapter.mItems.get(mMainViewPager.getCurrentItem()).getREG_DATE().substring(0, 10);
                    String TODAY_REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");

                    if (REG_DATE.equals(TODAY_REG_DATE))
                        setPostHeaderTitle(getString(R.string.today), false, Color.parseColor("#FFFFD83B"));
                    else
                        setPostHeaderTitle(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4), false);
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                }
            }

            // 알림바에서 대댓글 이동 알림을 클릭하여 들어온 경우
            if (CommonUtil.isNotNull(mGetOTI)) {
                Bundle data = new Bundle();
                Message msg = new Message();
                data.putString("POI", mGetPOI);
                msg.setData(data);
                msg.what = Constants.QUERY_OST_DATA;
                mHandler.sendMessage(msg);
            }
        }
        // 조회 된 데이터가 없을 경우
        else {
            postEmptyLayout.setVisibility(View.VISIBLE);
            llFixLocationLayout.setVisibility(View.GONE);
            ivPostRoot.setImageResource(R.drawable.img_int_temp);

            if ( CommonUtil.isNull(mSortData.get("GENDER")) && CommonUtil.isNull(mSortData.get("GENERATION")) && CommonUtil.isNull(mSortData.get("TIME")) && CommonUtil.isNull(mSortData.get("DISTANCE")) ) {
                postEmptyTitle.setText(getString(R.string.msg_post_empty_title));
                postEmptyContent.setText(getString(R.string.msg_post_empty_content));
            } else {
                postEmptyTitle.setText(getString(R.string.msg_post_empty_title_filter));
                postEmptyContent.setText(getString(R.string.msg_post_empty_content_filter));
            }
        }
    }

    private void updatePostTimeLineHeader() {
        if (getPostDataRes.getPostDataItemList().size() > 0) {
            int position = mPostTimeLineMultiViewPager.getCurrentItem();
            String positionRegDate = getPostDataRes.getPostDataItemList().get(position).getREG_DATE().substring(0, 10);
            String TODAY_REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");

            if (positionRegDate.equals(TODAY_REG_DATE))
                setPostHeaderTitle(getString(R.string.today), false, Color.parseColor("#99FFFFFF"));
            else
                setPostHeaderTitle(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(positionRegDate), "MMMM").toUpperCase() + " " + positionRegDate.substring(0, 4), false, Color.parseColor("#99FFFFFF"));

            tvPostTimeLineDay.setText(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(positionRegDate), "dd"));
            tvPostTimeLineWeekDayEng.setText(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(positionRegDate), "EEE"));

            int timePosition = Integer.valueOf(getPostDataRes.getPostDataItemList().get(position).getREG_DATE().substring(11, 13)) - 1;
            if (timePosition >= 0) {
                mPostTimeLineTimerHWheelView.setCurrentItem(timePosition, true);
                mPostTimeLineTimerAdapter.setItemSelected(timePosition);
            }
        }
    }

    private void setOstDataUpdate(final int position, final String toggleYn) {
        if ("Y".equals(toggleYn)) {
            DeviceUtil.showToast(mContext, getString(R.string.msg_ost_title_up));

            for (OstDataItem item : arrOstDataItem) {
                item.setTITL_TOGGLE_YN("N");
            }

            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setOTI(arrOstDataItem.get(position).getOTI());
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setSSI(arrOstDataItem.get(position).getSSI());
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setTITLE_ALBUM_PATH(arrOstDataItem.get(position).getALBUM_PATH());
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setTITLE_SONG_NM(arrOstDataItem.get(position).getSONG_NM());
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setTITLE_ARTI_NM(arrOstDataItem.get(position).getARTI_NM());
            mPostAdapter.notifyDataSetChanged();
        } else {
            DeviceUtil.showToast(mContext, getString(R.string.msg_ost_title_down));

            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setOTI("");
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setSSI("");
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setTITLE_ALBUM_PATH("");
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setTITLE_SONG_NM("");
            arrPostDataItem.get(mMainViewPager.getCurrentItem()).setTITLE_ARTI_NM("");
            mPostAdapter.notifyDataSetChanged();
        }

        arrOstDataItem.get(position).setTITL_TOGGLE_YN(toggleYn);

        if ("REG_DATE".equals(mOstSortField)) {
            RegDateCompare regDateCompare = new RegDateCompare();
            Collections.sort(arrOstDataItem, regDateCompare);
        } else {
            LikeCountCompare likeCountCompare = new LikeCountCompare();
            Collections.sort(arrOstDataItem, likeCountCompare);
        }

        mPostOstAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.QUERY_APP_VERSION:
                if (resultCode == Constants.RESULT_SUCCESS || resultCode == RESULT_CANCELED)
                    mHandler.sendEmptyMessageDelayed(Constants.QUERY_SLIDE_MENU, 1000);
                else
                    finish();
                break;
            case Constants.QUERY_REGIST_USER :
                if (resultCode == Constants.RESULT_SUCCESS) {
                    mHandler.sendEmptyMessage(Constants.QUERY_USER_INFO);
                } else {
                    finish();
                }
                break;
            case Constants.QUERY_WRITE_POST_DATA :
                if (resultCode == Constants.RESULT_SUCCESS) {
                    getData(Constants.QUERY_POST_DATA);
                }
                break;
            // 위치 서비스 설정 후 ActivityResult
            case Constants.QUERY_LOCATION_SERVICE_SETTING:
                if (mLocationUtil.isLocationEnabled()) {
                    mLocationUtil.run();
                }
                break;
            // OST 등록 성공 ActivityResult
            case Constants.QUERY_WRITE_OST_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    arrPostDataItem.get(mMainViewPager.getCurrentItem()).setOST_REG_YN("Y");
                    arrPostDataItem.get(mMainViewPager.getCurrentItem()).setOST_CNT(arrPostDataItem.get(mMainViewPager.getCurrentItem()).getOST_CNT() + 1);
                    mPostAdapter.notifyDataSetChanged();

                    getData(Constants.QUERY_OST_DATA, arrPostDataItem.get(mMainViewPager.getCurrentItem()).getPOI());
                }
                break;
            // POST SORT 성공 ActivityResult
            case Constants.QUERY_POST_SORT:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    mHashTag = "";
                    mColor = "";
                    mPlace = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mSortData = mdbHelper.getSortData("POST");
                    mdbHelper.close();
                    getData(Constants.QUERY_POST_DATA);
                }
                break;
            // OST 팝업 > Footer > 담기 > 보관함 이동 후 ActivityResult
            case Constants.QUERY_CABINET_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    mBXI = data.getStringExtra("BXI");
                    if (CommonUtil.isNotNull(mBXI)) {
                        getData(Constants.QUERY_ADD_CABINET);
                    }
                }
                break;
            // Calendar 날짜 선택 후 ActivityResult
            case Constants.QUERY_CALENDAR:
                if (resultCode == Constants.RESULT_SUCCESS)
                    if (!mPostTimeLineRegDate.equals(data.getStringExtra("REG_DATE"))) {
                        mPostTimeLineRegDate = data.getStringExtra("REG_DATE");
                        isGetPostDataFixed = true;
                        getData(Constants.QUERY_POST_DATA);
                    }
                break;
            // OST 대댓글 이동 후 ActivityResult
            case Constants.QUERY_OST_REPLE:
                if (btnOstWrite != null) {
                    String POI = (String)btnOstWrite.getTag(R.id.tag_poi);
                    if (CommonUtil.isNotNull(POI)) {
                        getData(Constants.QUERY_OST_DATA, POI);
                    }
                }
                break;
        }
    }

    class RegDateCompare implements Comparator<OstDataItem> {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public int compare(OstDataItem s1, OstDataItem s2) {
            String title1 = "Y".equals(s1.getTITL_TOGGLE_YN()) ? "N" : "Y";
            String title2 = "Y".equals(s2.getTITL_TOGGLE_YN()) ? "N" : "Y";
            int sComp = title1.compareTo(title2);

            if (sComp != 0) {
                return sComp;
            } else {
                Date d1 = null;
                Date d2 = null;

                try {
                    d1 = simpleDateFormat.parse(s1.getREG_DATE());
                    d2 = simpleDateFormat.parse(s2.getREG_DATE());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                /*return s1.getREG_DATE().compareTo(s2.getREG_DATE());*/
                return (d1.getTime() > d2.getTime() ? -1 : 1);     //descending
                //return (d1.getTime() > d2.getTime() ? 1 : -1);     //ascending
            }
        }
    }

    class LikeCountCompare implements Comparator<OstDataItem> {
        public int compare(OstDataItem s1, OstDataItem s2) {
            String title1 = "Y".equals(s1.getTITL_TOGGLE_YN()) ? "N" : "Y";
            String title2 = "Y".equals(s2.getTITL_TOGGLE_YN()) ? "N" : "Y";
            int sComp = title1.compareTo(title2);

            if (sComp != 0) {
                return sComp;
            } else {
                int i1 = Integer.parseInt(s1.getLIKE_CNT());
                int i2 = Integer.parseInt(s2.getLIKE_CNT());

                /*return s1.getLIKE_CNT().compareTo(s2.getLIKE_CNT());*/
                return (i1 > i2) ? -1: 1;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 백 키를 터치한 경우
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // OST 팝업이 활성화 되어 있을 경우
            if (ostRootLayout.getVisibility() == View.VISIBLE) {
                showLayout(ostRootLayout, false);
                return false;
            }
            // 슬라이드 메뉴가 활성화 되어 있을 경우
            else if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
                mSlidingMenu.toggle();
                return false;
            } else {
                if (!isFinish && CommonUtil.isNull(mGetPOI)) {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_app_close));
                    isFinish = true;
                    mHandler.sendEmptyMessageDelayed(Constants.DIALOG_APP_CLOSE, Constants.TIMEOUT_BACK_KEY);
                    return false;
                } else {
                    finish();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkIntroBgFile() {
        String introBg = SPUtil.getSharedPreference(mContext, Constants.SP_INTRO_BG);
        if (CommonUtil.isNotNull(introBg)) {
            String[] tmpDirs = introBg.split("/");

            if (CommonUtil.isNotNull(tmpDirs[tmpDirs.length - 1])) {
                String introFilePath = getDir(Constants.SERVICE_INTRO_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + tmpDirs[tmpDirs.length - 1];
                File file = new File(introFilePath);
                if (!file.exists()) {
                    File deleteFile = getDir(Constants.SERVICE_INTRO_FILE_PATH, MODE_PRIVATE);
                    DeviceUtil.removeFiles(deleteFile);
                    downloadIntroBg(introBg, file);
                }
            }
        }
    }

    private static void downloadIntroBg(final String introBg, final File outputFile) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean isRtState = true;
                long fileSize = 0;
                long downloadFileSize = 0;

                LogUtil.e("인트로 동영상 다운로드 시작 >>>");
                LogUtil.e(introBg);

                try {
                    URL url = new URL(introBg);

                    // HttpURLConnection 객체 생성.
                    HttpURLConnection urlConn = null;

                    // URL 연결 (웹페이지 URL 연결.)
                    urlConn = (HttpURLConnection)url.openConnection();

                    // TimeOut 시간 (서버 접속시 연결 시간)
                    urlConn.setConnectTimeout(Constants.TIMEOUT_HTTP_CONNECTION);

                    // TimeOut 시간 (Read시 연결 시간)
                    urlConn.setReadTimeout(Constants.TIMEOUT_HTTP_CONNECTION);

                    // 요청 방식 선택 (GET, POST)
                    urlConn.setRequestMethod("GET");

                    // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
                    urlConn.setDoOutput(false);

                    // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
                    urlConn.setDoInput(true);

                    String contentLength = urlConn.getHeaderField("Content-Length");
                    if (CommonUtil.isNotNull(contentLength))
                        fileSize = Integer.valueOf(contentLength);

                    InputStream is = urlConn.getInputStream();
                    OutputStream os = new FileOutputStream(outputFile);
                    int c = 0;
                    byte[] buf = new byte[16384]; // 16kb
                    LogUtil.e("인트로 동영상 ▶ " + NumberFormat.getNumberInstance(Locale.KOREA).format(fileSize) + " Bytes");
                    while ((c = is.read(buf)) != -1) {
                        os.write(buf, 0, c);
                    }
                    os.flush();
                    is.close();
                    os.close();
                } catch (Exception e) {
                    isRtState = false;
                    LogUtil.e("인트로 동영상 다운로드과정에서 예외가 발생했습니다.\n" + e.getMessage());
                } finally {
                    downloadFileSize = outputFile.length();
                    LogUtil.e("인트로 동영상 ▶ " + NumberFormat.getNumberInstance(Locale.KOREA).format(downloadFileSize) + " Bytes 다운로드 완료");

                    if (fileSize != downloadFileSize) {
                        LogUtil.e("인트로 동영상 ▶ 다운로드가 완료되지 않아 파일을 삭제처리 합니다.");
                        outputFile.delete();
                    }
                }

                return isRtState;
            }

            @Override
            protected void onPostExecute(Boolean isRtState) {}
        }.execute();
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService 를 실행한다.
     */
    public void getInstanceIdToken() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, PostRegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * Google Play Service 를 사용할 수 있는 환경이지를 체크한다.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                LogUtil.e("GCM ▶ This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
