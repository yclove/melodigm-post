package com.melodigm.post;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.google.android.gms.analytics.Tracker;
import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.menu.BuyUseCouponActivity;
import com.melodigm.post.menu.GeneralActivity;
import com.melodigm.post.menu.MyPostActivity;
import com.melodigm.post.menu.NotificationCenterActivity;
import com.melodigm.post.menu.SettingActivity;
import com.melodigm.post.menu.TimeLineActivity;
import com.melodigm.post.menu.UseCouponActivity;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.search.SearchMusicActivity;
import com.melodigm.post.search.SearchStoryActivity;
import com.melodigm.post.service.MusicService;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LocationUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.CircularSeekBar;
import com.melodigm.post.widget.CircularSeekBar.OnCircularSeekBarChangeListener;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.ProgressDialog;
import com.melodigm.post.widget.slidingmenu.SlidingMenu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public abstract class BaseActivity extends Activity {
    public Context mContext;
    public Tracker mTracker;
    public RequestManager mGlideRequestManager;
    public HashMap<Integer, RunnableThread> mThreads = null;
    public PostDatabases mdbHelper;
    public String mActivityName;
    public WeakRefHandler mHandler;
    public ProgressDialog mProgressDialog = new ProgressDialog();
    public PostDialog mPostDialog;
    public LocationUtil mLocationUtil;
    public String mUserId = "";
    public POSTException mPOSTException;
    public boolean isFinish = false;

    public SlidingMenu mSlidingMenu;
    public LinearLayout btnHeaderTitleLayout, llPlayerMusicListBtn, llMovePlayer, llSlidingMenuPlayerLayout, postFooterLayout, llPopularBtn, llMenuBuyUseCoupon;
    public RelativeLayout postHeaderLayout, btnHeaderMenu, btnHeaderBack, btnHeaderCheck, btnHeaderTimeLine, btnHeaderClose, btnMenuTimeLine, btnSearchMusic, btnBuyUseCoupon, btnFooterSort, btnFooterShare, btnPostTimeLineFooterSort;
    public FrameLayout headerMenuLineLayout, ostHeaderMenuLineLayout;
    public View headerMenuLineSt, headerMenuLineNd, headerMenuLineRd, ostHeaderMenuLineSt, ostHeaderMenuLineNd, ostHeaderMenuLineRd;
    public LetterSpacingTextView btnHeaderTitle, tvPlayerMusicType, tvMenuPost, tvMenuToday, tvMenuRadio, tvMenuPopular, tvMenuMyPost, lstvPopularText;
    public ImageView btnHeaderTitleClose, btnHeaderImage, ivHeaderTimeLine, btnFooterSortImage, btnPostTimeLineFooterSortImage, ivPlayerMusicType;

    public CircularSeekBar circularSeekBar;
    public String playerSSI = "";
    public boolean isPlayerTimer = false;
    public boolean isPlayerOnAir = false;
    public RelativeLayout btnPlayerPlay, btnPlayerPause;
    public CircularImageView ivCircleImage;
    public ImageView btnPlayerPrevious, btnPlayerNext, ivPlayerPlay, ivPlayerPause;
    public TextView tvPlayerMusicCount, tvPlayerSongName, tvPlayerArtiName, tvMenuSearchStory, tvMenuSetting, tvMenuGeneral;

    public String mGetPOI, mGetOTI, mGetORI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivityName = this.getClass().getSimpleName();

        LogUtil.e(mActivityName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        mGlideRequestManager = Glide.with(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.static_out);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        int newIntentMove = intent.getIntExtra("HOME_NEWINTENT_MOVE", 0);
        LogUtil.e("HOME_NEWINTENT_MOVE : " + newIntentMove);

        // GCM 등록 후 다음 화면으로 이동
        if (newIntentMove == Constants.QUERY_REGIST_USER)
            mHandler.sendEmptyMessage(Constants.QUERY_REGIST_USER);

        String action = "", poi = "", ori = "", oti = "";
        // 카카오톡 / GCM 에서 클릭하여 들어온 경우
        if (intent.getData() != null && (Constants.KAKAOLINK_SCHEME_HOST.equals(intent.getData().getHost()) || Constants.SERVICE_SCHEME_HOST.equals(intent.getData().getHost()))) {
            action = intent.getData().getQueryParameter("action");
            poi = intent.getData().getQueryParameter("poi");
            ori = intent.getData().getQueryParameter("ori");
            oti = intent.getData().getQueryParameter("oti");
        }

        if (CommonUtil.isNotNull(action)) {
            if (Constants.SERVICE_SCHEME_HOST.equals(intent.getData().getHost())) {
                SPUtil.setSharedPreference(mContext, Constants.SP_BADGE_COUNT, 0);
                DeviceUtil.updateBadgeCount(mContext, 0);
            }

            switch (action) {
                case "YC01": // 데이터 네트워크 사용을 거부하고 현재 모바일 내트워크 접속 중일 경우(앱이 실행중이지 않을 경우)
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, onGlobalClickListener, getString(R.string.dialog_confirm_data_network), getString(R.string.dialog_confirm_data_network_title));
                    mPostDialog.show();
                    break;
                case "YC02": // 해외 접속 차단 확인일 경우(앱이 실행중이지 않을 경우)
                    isFinish = false;
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onGlobalClickListener, getString(R.string.dialog_info_locale), getString(R.string.dialog_info_locale_title));
                    mPostDialog.show();
                    break;
                case "AF01": // 내사연LIKE
                case "AF03": // 내사연OST등록
                case "AF04": // 사연내OSTLIKE
                case "AF06": // 사연내OST타이틀곡선정
                    intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra("POST_TYPE", Constants.REQUEST_TYPE_POST);
                    intent.putExtra("POI", poi);
                    startActivity(intent);
                    break;
                case "AF07": // 사연내OST댓글
                    intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra("POST_TYPE", Constants.REQUEST_TYPE_POST);
                    intent.putExtra("POI", poi);
                    intent.putExtra("OTI", oti);
                    intent.putExtra("ORI", ori);
                    startActivity(intent);
                    break;
                case "AF08": // 내라디오LIKE
                case "AF10": // 내라디오OST등록
                    intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra("POST_TYPE", Constants.REQUEST_TYPE_RADIO);
                    intent.putExtra("POI", poi);
                    startActivity(intent);
                    break;
                case "AF13": // 라디오내OST댓글
                    intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra("POST_TYPE", Constants.REQUEST_TYPE_RADIO);
                    intent.putExtra("POI", poi);
                    intent.putExtra("OTI", oti);
                    intent.putExtra("ORI", ori);
                    startActivity(intent);
                    break;
                case "AF05": // 사연내OST신고처리
                case "AF12": // 라디오내OST신고처리
                case "AF15": // 투데이내OST신고처리
                case "AF02": // 내사연신고처리
                case "AF09": // 내라디오신고처리
                    intent = new Intent(mContext, NotificationCenterActivity.class);
                    startActivity(intent);
                    break;
                case "AF17": // 5곡재생가능알림
                case "AF19": // 기간만료알림
                case "AF20": // 소진만료알림
                    intent = new Intent(mContext, UseCouponActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    public void setSlidingMenu(View.OnClickListener onClickListener) {
        try {
            if (mSlidingMenu == null) {
                //setBehindContentView(R.layout.menu_frame);
                mSlidingMenu = new SlidingMenu(this);
                //mSlidingMenu = (SlidingMenu)findViewById(R.id.slidingmenulayout);
                //mSlidingMenu = getSlidingMenu();
                mSlidingMenu.setMode(SlidingMenu.LEFT);
                // 좌측 끝을 터치 시 메뉴를 활성시킴
                mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
                mSlidingMenu.setShadowDrawable(R.drawable.shadow);
                //mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
                mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_behind_width);
                // Contents 영역이 수치만큼 어두워 짐
                mSlidingMenu.setFadeDegree(0.35f);
                //mSlidingMenu.setBehindScrollScale(0.0f);
                mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
                mSlidingMenu.setMenu(R.layout.view_slidemenu);
                mSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
                    @Override
                    public void onOpen() {
                        Intent intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_GET);
                        startService(intent);
                    }
                });
                mSlidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
                    @Override
                    public void onClose() {
                    }
                });

                circularSeekBar = (CircularSeekBar)findViewById(R.id.circularSeekBar);
                circularSeekBar.setMax(100);
                circularSeekBar.setProgress(0);
                circularSeekBar.setOnSeekBarChangeListener(new CircleSeekBarListener());

                tvPlayerMusicType = (LetterSpacingTextView)findViewById(R.id.tvPlayerMusicType);
                tvPlayerMusicType.setSpacing(Constants.TEXT_VIEW_SPACING);

                ivPlayerMusicType = (ImageView)findViewById(R.id.ivPlayerMusicType);

                llPlayerMusicListBtn = (LinearLayout)findViewById(R.id.llPlayerMusicListBtn);
                llPlayerMusicListBtn.setOnClickListener(onClickListener);

                tvPlayerMusicCount = (TextView)findViewById(R.id.tvPlayerMusicCount);
                llSlidingMenuPlayerLayout = (LinearLayout)findViewById(R.id.llSlidingMenuPlayerLayout);
                ivCircleImage = (CircularImageView)findViewById(R.id.ivCircleImage);
                tvPlayerSongName = (TextView)findViewById(R.id.tvPlayerSongName);
                tvPlayerArtiName = (TextView)findViewById(R.id.tvPlayerArtiName);

                btnPlayerPrevious = (ImageView)findViewById(R.id.btnPlayerPrevious);
                btnPlayerPrevious.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controls.previousControl(mContext);
                    }
                });

                btnPlayerNext = (ImageView)findViewById(R.id.btnPlayerNext);
                btnPlayerNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controls.nextControl(mContext);
                    }
                });

                btnPlayerPlay = (RelativeLayout)findViewById(R.id.btnPlayerPlay);
                btnPlayerPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPlayerPlay.setVisibility(View.GONE);
                        btnPlayerPause.setVisibility(View.VISIBLE);
                        boolean isServiceRunning = DeviceUtil.isServiceRunning(MusicService.class.getName(), mContext);
                        if (!isServiceRunning) {
                            Intent intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                            intent.setPackage(Constants.SERVICE_PACKAGE);
                            intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_PLAY);
                            startService(intent);
                        } else
                            Controls.playControl(mContext);
                    }
                });
                ivPlayerPlay = (ImageView) findViewById(R.id.ivPlayerPlay);

                btnPlayerPause = (RelativeLayout)findViewById(R.id.btnPlayerPause);
                btnPlayerPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PlayerConstants.SONG_ON_AIR) {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                            mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_RADIO_ON_AIR_CLOSE, onGlobalClickListener);
                            mPostDialog.show();
                        } else {
                            btnPlayerPlay.setVisibility(View.VISIBLE);
                            btnPlayerPause.setVisibility(View.GONE);
                            Controls.pauseControl(mContext);
                        }
                    }
                });
                ivPlayerPause = (ImageView)findViewById(R.id.ivPlayerPause);

                llMovePlayer = (LinearLayout)findViewById(R.id.llMovePlayer);
                llMovePlayer.setOnClickListener(onClickListener);

                llMenuBuyUseCoupon = (LinearLayout)findViewById(R.id.llMenuBuyUseCoupon);
                llMenuBuyUseCoupon.setOnClickListener(onGlobalClickListener);

                tvMenuPost = (LetterSpacingTextView)findViewById(R.id.tvMenuPost);
                tvMenuPost.setSpacing(Constants.TEXT_VIEW_SPACING);
                tvMenuPost.setText(getString(R.string.menu_post));
                tvMenuPost.setTag(R.id.tag_popular_state, "N");
                tvMenuPost.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_POST);
                tvMenuPost.setOnClickListener(onClickListener);

                tvMenuToday = (LetterSpacingTextView)findViewById(R.id.tvMenuToday);
                tvMenuToday.setSpacing(Constants.TEXT_VIEW_SPACING);
                tvMenuToday.setText(getString(R.string.menu_today));
                tvMenuToday.setTag(R.id.tag_popular_state, "N");
                tvMenuToday.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_TODAY);
                tvMenuToday.setOnClickListener(onClickListener);

                tvMenuRadio = (LetterSpacingTextView)findViewById(R.id.tvMenuRadio);
                tvMenuRadio.setSpacing(Constants.TEXT_VIEW_SPACING);
                tvMenuRadio.setText(getString(R.string.menu_radio));
                tvMenuRadio.setTag(R.id.tag_popular_state, "N");
                tvMenuRadio.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_RADIO);
                tvMenuRadio.setOnClickListener(onClickListener);

                tvMenuPopular = (LetterSpacingTextView)findViewById(R.id.tvMenuPopular);
                tvMenuPopular.setSpacing(Constants.TEXT_VIEW_SPACING);
                tvMenuPopular.setText(getString(R.string.menu_popular));
                tvMenuPopular.setTag(R.id.tag_popular_state, "Y");
                tvMenuPopular.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_POST);
                tvMenuPopular.setOnClickListener(onClickListener);

                tvMenuSearchStory = (TextView)findViewById(R.id.tvMenuSearchStory);
                tvMenuSearchStory.setOnClickListener(menuOnClickListener);

                tvMenuMyPost = (LetterSpacingTextView)findViewById(R.id.tvMenuMyPost);
                tvMenuMyPost.setSpacing(Constants.TEXT_VIEW_SPACING);
                tvMenuMyPost.setText(getString(R.string.menu_mypost));
                tvMenuMyPost.setOnClickListener(menuOnClickListener);

                tvMenuSetting = (TextView)findViewById(R.id.tvMenuSetting);
                tvMenuSetting.setOnClickListener(menuOnClickListener);

                tvMenuGeneral = (TextView)findViewById(R.id.tvMenuGeneral);
                tvMenuGeneral.setOnClickListener(menuOnClickListener);

                btnMenuTimeLine = (RelativeLayout)findViewById(R.id.btnMenuTimeLine);
                btnMenuTimeLine.setOnClickListener(menuOnClickListener);

                btnSearchMusic = (RelativeLayout)findViewById(R.id.btnSearchMusic);
                btnSearchMusic.setOnClickListener(menuOnClickListener);

                btnBuyUseCoupon = (RelativeLayout)findViewById(R.id.btnBuyUseCoupon);
                btnBuyUseCoupon.setOnClickListener(menuOnClickListener);
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }
    }

    private View.OnClickListener menuOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;

            switch (v.getId()) {
                // SlidingMenu 이야기 검색 onClick
                case R.id.tvMenuSearchStory:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    startActivity(new Intent(mContext, SearchStoryActivity.class));
                    break;
                // SlidingMenu MY POST onClick
                case R.id.tvMenuMyPost:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    startActivity(new Intent(mContext, MyPostActivity.class));
                    break;
                // SlidingMenu 설정 onClick
                case R.id.tvMenuSetting:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    startActivity(new Intent(mContext, SettingActivity.class));
                    break;
                // SlidingMenu 일반 onClick
                case R.id.tvMenuGeneral:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    startActivity(new Intent(mContext, GeneralActivity.class));
                    break;
                // SlidingMenu 타임라인 onClick
                case R.id.btnMenuTimeLine:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    intent = new Intent(mContext, TimeLineActivity.class);
                    startActivity(intent);
                    break;
                // SlidingMenu 노래검색 onClick
                case R.id.btnSearchMusic:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    intent = new Intent(mContext, SearchMusicActivity.class);
                    startActivityForResult(intent, Constants.QUERY_MUSIC_SEARCH);
                    break;
                // SlidingMenu 이용권구매 onClick
                case R.id.btnBuyUseCoupon:
                    /*if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) mSlidingMenu.toggle();*/
                    intent = new Intent(mContext, BuyUseCouponActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        if (!("LockScreenActivity".equals(mActivityName) || "SplashActivity".equals(mActivityName) || "RegistPostUserActivity".equals(mActivityName) || ("PostActivity".equals(mActivityName) && CommonUtil.isNull(mGetPOI))))
            overridePendingTransition(R.anim.static_in, R.anim.slide_out_right);
    }

    public class CircleSeekBarListener implements OnCircularSeekBarChangeListener {
        int originalProgress;

        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
            if (fromUser) circularSeekBar.setProgress(originalProgress);
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            originalProgress = seekBar.getProgress();
        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
        }
    }

    public void setPostHeader(int headerType, View.OnClickListener onClickListener, String... args) {
        postHeaderLayout = (RelativeLayout)findViewById(R.id.postHeaderLayout);
        postFooterLayout = (LinearLayout) findViewById(R.id.postFooterLayout);
        btnHeaderMenu = (RelativeLayout)findViewById(R.id.btnHeaderMenu);
        btnHeaderBack = (RelativeLayout)findViewById(R.id.btnHeaderBack);
        btnHeaderTitleLayout = (LinearLayout)findViewById(R.id.btnHeaderTitleLayout);
        btnHeaderTitle = (LetterSpacingTextView)findViewById(R.id.btnHeaderTitle);
        btnHeaderTitleClose = (ImageView)findViewById(R.id.btnHeaderTitleClose);
        btnHeaderImage = (ImageView)findViewById(R.id.btnHeaderImage);
        btnHeaderCheck = (RelativeLayout)findViewById(R.id.btnHeaderCheck);
        btnHeaderClose = (RelativeLayout)findViewById(R.id.btnHeaderClose);
        btnHeaderTimeLine = (RelativeLayout)findViewById(R.id.btnHeaderTimeLine);
        ivHeaderTimeLine = (ImageView)findViewById(R.id.ivHeaderTimeLine);
        llPopularBtn = (LinearLayout) findViewById(R.id.llPopularBtn);
        lstvPopularText = (LetterSpacingTextView) findViewById(R.id.lstvPopularText);

        btnHeaderMenu.setOnClickListener(onClickListener);
        btnHeaderBack.setOnClickListener(onClickListener);
        btnHeaderTitleLayout.setOnClickListener(onClickListener);
        btnHeaderCheck.setOnClickListener(onClickListener);
        btnHeaderClose.setOnClickListener(onClickListener);
        btnHeaderTimeLine.setOnClickListener(onClickListener);
        llPopularBtn.setOnClickListener(onClickListener);

        btnHeaderMenu.setVisibility(View.GONE);
        btnHeaderBack.setVisibility(View.GONE);
        btnHeaderCheck.setVisibility(View.GONE);
        btnHeaderTimeLine.setVisibility(View.GONE);
        btnHeaderClose.setVisibility(View.GONE);
        llPopularBtn.setVisibility(View.GONE);

        if (headerType == Constants.HEADER_TYPE_MENU) {
            btnHeaderMenu.setVisibility(View.VISIBLE);
            btnHeaderTimeLine.setVisibility(View.VISIBLE);
        } else if (headerType == Constants.HEADER_TYPE_BACK_HOME_CHECK) {
            btnHeaderBack.setVisibility(View.VISIBLE);
            btnHeaderCheck.setVisibility(View.VISIBLE);
        } else if (headerType == Constants.HEADER_TYPE_BACK_HOME) {
            btnHeaderBack.setVisibility(View.VISIBLE);
        } else if (headerType == Constants.HEADER_TYPE_CLOSE) {
            btnHeaderClose.setVisibility(View.VISIBLE);
        } else if (headerType == Constants.HEADER_TYPE_MENU_POPULAR) {
            btnHeaderMenu.setVisibility(View.VISIBLE);
            llPopularBtn.setVisibility(View.VISIBLE);
            lstvPopularText.setSpacing(Constants.TEXT_VIEW_SPACING);

            if (Constants.REQUEST_TYPE_POST.equals(args[0])) {
                lstvPopularText.setText(getString(R.string.post));
                lstvPopularText.setTextColor(Color.parseColor("#FF00AFD5"));
            } else if (Constants.REQUEST_TYPE_RADIO.equals(args[0])) {
                lstvPopularText.setText(getString(R.string.radio));
                lstvPopularText.setTextColor(Color.parseColor("#FFF65857"));
            } else if (Constants.REQUEST_TYPE_TODAY.equals(args[0])) {
                lstvPopularText.setText(getString(R.string.today));
                lstvPopularText.setTextColor(Color.parseColor("#FFFFD83B"));
            }
        }

        headerMenuLineLayout = (FrameLayout)findViewById(R.id.headerMenuLineLayout);
        headerMenuLineSt = findViewById(R.id.headerMenuLineSt);
        headerMenuLineNd = findViewById(R.id.headerMenuLineNd);
        headerMenuLineRd = findViewById(R.id.headerMenuLineRd);

        ostHeaderMenuLineLayout = (FrameLayout)findViewById(R.id.ostHeaderMenuLineLayout);
        ostHeaderMenuLineSt = findViewById(R.id.ostHeaderMenuLineSt);
        ostHeaderMenuLineNd = findViewById(R.id.ostHeaderMenuLineNd);
        ostHeaderMenuLineRd = findViewById(R.id.ostHeaderMenuLineRd);

        btnFooterSort = (RelativeLayout)findViewById(R.id.btnFooterSort);
        btnFooterSortImage = (ImageView)findViewById(R.id.btnFooterSortImage);
        btnFooterShare = (RelativeLayout)findViewById(R.id.btnFooterShare);

        btnPostTimeLineFooterSort = (RelativeLayout)findViewById(R.id.btnPostTimeLineFooterSort);
        btnPostTimeLineFooterSortImage = (ImageView)findViewById(R.id.btnPostTimeLineFooterSortImage);
    }

    public void setPostHeaderTitle(int headerImage) {
        btnHeaderTitleLayout.setVisibility(View.VISIBLE);
        btnHeaderTitle.setVisibility(View.GONE);
        btnHeaderTitleClose.setVisibility(View.GONE);
        btnHeaderImage.setVisibility(View.VISIBLE);
        btnHeaderImage.setImageResource(headerImage);
    }

    public void setPostHeaderTitle(String headerTitle, boolean isOnClickEvent, int... colorHex) {
        if (isOnClickEvent) {
            if (CommonUtil.isNotNull(headerTitle)) {
                btnHeaderTitleLayout.setEnabled(true);
                postHeaderLayout.setBackgroundColor(Color.parseColor("#CCD73D66"));
                btnHeaderTitleLayout.setVisibility(View.VISIBLE);
                btnHeaderTitle.setText("#" + headerTitle);
                btnHeaderTitleClose.setVisibility(View.VISIBLE);
            } else {
                btnHeaderTitleLayout.setEnabled(false);
                postHeaderLayout.setBackgroundColor(Color.parseColor("#00000000"));
                btnHeaderTitleLayout.setVisibility(View.INVISIBLE);
                btnHeaderTitleClose.setVisibility(View.GONE);
                btnHeaderTitle.setText("");
            }
        } else {
            btnHeaderTitleLayout.setEnabled(false);
            btnHeaderTitleLayout.setVisibility(View.VISIBLE);
            btnHeaderTitleClose.setVisibility(View.GONE);
            btnHeaderTitle.setText(headerTitle);

            if (CommonUtil.isRegexOnlyEng(headerTitle.replaceAll(" ", "")))
                btnHeaderTitle.setSpacing(Constants.TEXT_VIEW_SPACING);
        }

        if (colorHex != null && colorHex.length > 0) {
            btnHeaderTitle.setTextColor(colorHex[0]);
        } else {
            btnHeaderTitle.setTextColor(Color.parseColor("#FFFFFFFF"));
        }
    }

    public Dialog createGlobalDialog(int requestCode, final Boolean isFinish) {
        this.isFinish = isFinish;

        switch (requestCode) {
            case Constants.DIALOG_EXCEPTION_IMAGE_UPLOAD:
                return new AlertDialog.Builder(this)
                        .setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                        .setMessage(getApplicationContext().getResources().getString(R.string.exception_image_upload))
                        .setPositiveButton(getApplicationContext().getResources().getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (isFinish) {
                                            finish();
                                        }
                                    }
                                }).setCancelable(false).create();
            case Constants.DIALOG_EXCEPTION_REQUEST:
                return new AlertDialog.Builder(this)
                        .setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                        .setMessage(getApplicationContext().getResources().getString(R.string.exception_request))
                        .setPositiveButton(getApplicationContext().getResources().getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (isFinish) {
                                            finish();
                                        }
                                    }
                                }).setCancelable(false).create();
            case Constants.DIALOG_EXCEPTION_POST:
                if (mPOSTException.getCode().equals(mPOSTException.NOT_FOUND_USER)) {
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOT_FOUND_USER, onGlobalClickListener, mPOSTException.getMessage());
                } else {
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onGlobalClickListener, mPOSTException.getMessage());
                }
                return mPostDialog;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED:
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_UPDATE_NEED, onGlobalClickListener);
                return mPostDialog;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT:
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_UPDATE_SUPPORT, onGlobalClickListener);
                return mPostDialog;
            case Constants.DIALOG_EXCEPTION_NETWORK:
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onGlobalClickListener, getString(R.string.exception_network));
                return mPostDialog;
        }
        return null;
    }

    public void getGlobalData(int queryType) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        if (queryType == Constants.QUERY_LOCATION_CHANGE) {
            if (mLocationUtil == null) {
                mLocationUtil = new LocationUtil(mContext, mHandler);
            }

            mLocationUtil.run();
        }
    }

    public View.OnClickListener onGlobalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;

            switch (v.getId()) {
                // 미등록 사용자 타입 확인 onClick
                case R.id.btnNotFoundUserConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;

                    SPUtil.setSharedPreference(mContext, Constants.SP_USER_ID, "");
                    SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_ID, "");
                    SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE, "");

                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mdbHelper.deleteAllRecentSearchWords();
                    mdbHelper.deleteAllOstPlayList();
                    mdbHelper.close();

                    File file = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE);
                    DeviceUtil.removeFiles(file);

                    intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("HOME_NEWINTENT_MOVE", Constants.QUERY_REGIST_USER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                // 확인 타입의 확인 onClick
                case R.id.btnConfirmConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    SPUtil.setSharedPreference(mContext, Constants.SP_USE_DATA_NETWORK, true);
                    break;
                // 안내 타입의 확인 onClick
                case R.id.btnInfoConfirm:
                    if (mPostDialog != null) mPostDialog.dismiss();
                    if (isFinish) finish();
                    break;
                // 필수 업데이트 타입의 확인
                // 권장 업데이트 타입의 업데이트 onClick
                case R.id.btnUpdateNeedConfirm:
                case R.id.ivUpdateSupportUpdateBtn:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.SERVICE_PACKAGE));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                // 권장 업데이트 타입의 보기 onClick
                case R.id.btnUpdateSupportSee:
                    DeviceUtil.showToast(mContext, "공지사항으로 이동");
                    break;
                // RADIO ON AIR 해제 > 일시멈춤 onClick
                case R.id.btnOnAirPause:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    Controls.pauseControl(mContext);
                    break;
                // RADIO ON AIR 해제 > 해제 onClick
                case R.id.btnOnAirCancel:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ON_AIR_CLEAR);
                    startService(intent);
                    break;
                // 이용권구매 onClick
                case R.id.llMenuBuyUseCoupon:
                case R.id.llBuyUseCoupon:
                case R.id.rlBuyUseCouponBtn:
                    intent = new Intent(mContext, BuyUseCouponActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    public ViewPropertyAnimation.Animator animationObject = new ViewPropertyAnimation.Animator() {
        @Override
        public void animate(View view) {
            view.setAlpha(0f);
            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            fadeAnim.setDuration(2000);
            fadeAnim.start();
        }
    };

    public Animation getFadeInAnimation(long duration) {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        return animation;
    }

    public Animation getFadeOutAnimation(long duration) {
        Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(duration);
        return animation;
    }

    // https://github.com/ToxicBakery/ViewPagerTransforms
    public static final class TransformerItem {
        final String title;
        final Class<? extends ViewPager.PageTransformer> clazz;

        public TransformerItem(Class<? extends ViewPager.PageTransformer> clazz) {
            this.clazz = clazz;
            title = clazz.getSimpleName();
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public void btnGlobalListener(View v) {
        switch (v.getId()) {
            // Footer Sort onClick
            case R.id.btnFooterSort:
            case R.id.btnPostTimeLineFooterSort:
                startActivityForResult(new Intent(this, PostSortActivity.class), Constants.QUERY_POST_SORT);
                break;
        }
    }

    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), Constants.SERVICE_POST_FILE_NAME);  // 외장메모리 경로
            try {
                f.createNewFile();      // 외장메모리에 temp.jpg 파일 생성
            } catch (IOException e) {
            }

            return f;
        } else
            return null;
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    public boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    // 갤러리 호출
    // Intent간 data가 100kb 넘어갈 경우 에러가 발생하므로 임시 파일로 저장후 읽어서 가져온다.
    // 버튼클릭 ->임시파일 생성 -> 갤러리호출 -> 사진 선택 -> 임시파일에 사진 저장 -> 액티비티 복귀 -> 임시파일로부터 사진 읽기
    public void getImgFromPhone() {
        Intent intent = new Intent(Intent.ACTION_PICK); // 또는 ACTION_GET_CONTENT
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra("crop", "true"); // Crop기능 활성화
        intent.putExtra("outputX", 1080);
        intent.putExtra("outputY", 1920);
        intent.putExtra("aspectX", 9);
        intent.putExtra("aspectY", 16);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());                         // 임시파일 생성
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString()); // 포맷방식

        startActivityForResult(intent, Constants.QUERY_CHOICE_PICTURE);
    }

    // 카메라 호출
    public void getImgFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);         // 또는 ACTION_PICK
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());                         // 임시파일 생성

        startActivityForResult(intent, Constants.QUERY_CHOICE_CAMERA);
    }

    public void cropImage(Uri contentUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri of image
        cropIntent.setDataAndType(contentUri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        /*cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);*/
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, Constants.QUERY_CROP_IMAGE);
    }
}
