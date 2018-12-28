package com.melodigm.post;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetPostPositionDataReq;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.EllipsizingTextView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LockScreenActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private String mPostType;
    private PostDataItem mPostDataItem;
    private LinearLayout llContentLayout, llEmptyStoryLayout, llStoryLayout, llStoryContent, llStoryContentEmpty, llStoryOstLayout, postContentLayout;
    private LinearLayout llPostRadioUnderLine, llTodayUnderLine, llPlayerTimerLayout;
    private LetterSpacingTextView tvPostSubject, lstvPlayerTimerLostSecondText;
    private RelativeLayout rlPrevBtn, rlPlayBtn, rlNextBtn;
    private CircularImageView ivAlbumImage, ivOstImage;
    private ImageView ivPlayerBackground, ivPlayerAlbumBackground, ivPlayBtn, ivPostOstTitle, ivPlayerTimerIcon, ivRotate;
    private TextView tvSongName, tvArtiName, tvOstSongName, tvOstArtiName, tvTodaySubject;
    private EllipsizingTextView tvPostContent;

    private String mRadioPath;

    private int mPlayerTimerTotalSecond = 0;
    private Timer mTimer;
    private TimerTask mTimerTask;

    // 더보기 팝업
    private RelativeLayout rlMoreLayout, rlMoreLayoutCloseBtn;
    private LetterSpacingTextView tvMorePostSubject;
    private LinearLayout llMorePostSubjectUnderLine;
    private EllipsizingTextView tvMorePostContent;

    private int currentPause = -1;

    private boolean isDrag = false;
    private SeekBar mSeekBar;
    private LetterSpacingTextView tvPlayingDuration, tvPlayDuration, tvPlayLeftDuration;
    private LinearLayout llPlayThumb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        /**
         * YCNOTE - Window addFlags
         * FLAG_SHOW_WHEN_LOCKED    : 안드로이드 기본 잠금화면 보다 위에 이 Activity를 띄워라
         * FLAG_DISMISS_KEYGUARD        : 안드로이드 기본 잠금화면을 없애라
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        mPlayerTimerTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER);
        startTimer();

        setDisplay();
    }

    private void startTimer() {
        LogUtil.e("타이머 생성");
        if (mTimer == null) mTimer = new Timer();
        if (mTimerTask == null) mTimerTask = new MainTask();
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    private void stopTimer() {
        LogUtil.e("타이머 제거");
        if (mTimerTask != null) {
            mTimerTask.cancel(); //타이머 task를 timer 큐에서 지워버린다.
            mTimerTask = null;
        }

        if (mTimer != null) {
            // 스케쥴 Task와 타이머를 취소한다.
            mTimer.cancel();
            // Task큐의 모든 Task를 제거한다.
            mTimer.purge();
            mTimer = null;
        }
    }

    private class MainTask extends TimerTask {
        public void run(){
            mHandler.sendEmptyMessage(Constants.QUERY_PLAYER_TIMER);
        }
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    private void setDisplay() {
        Intent intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
        intent.setPackage(Constants.SERVICE_PACKAGE);
        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_GET);
        startService(intent);

        llContentLayout = (LinearLayout)findViewById(R.id.llContentLayout);
        llEmptyStoryLayout = (LinearLayout)findViewById(R.id.llEmptyStoryLayout);
        llStoryLayout = (LinearLayout)findViewById(R.id.llStoryLayout);
        llStoryContent = (LinearLayout)findViewById(R.id.llStoryContent);
        llStoryContentEmpty = (LinearLayout)findViewById(R.id.llStoryContentEmpty);
        llStoryOstLayout = (LinearLayout)findViewById(R.id.llStoryOstLayout);
        llStoryOstLayout.setOnClickListener(this);
        ivPlayerBackground = (ImageView)findViewById(R.id.ivPlayerBackground);
        ivPlayerAlbumBackground = (ImageView)findViewById(R.id.ivPlayerAlbumBackground);
        ivAlbumImage = (CircularImageView)findViewById(R.id.ivAlbumImage);
        tvSongName = (TextView)findViewById(R.id.tvSongName);

        // Player Timer 레이아웃
        llPlayerTimerLayout = (LinearLayout)findViewById(R.id.llPlayerTimerLayout);
        lstvPlayerTimerLostSecondText = (LetterSpacingTextView)findViewById(R.id.lstvPlayerTimerLostSecondText);
        lstvPlayerTimerLostSecondText.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvPlayerTimerLostSecondText.setText(getString(R.string.common_define_play_time));
        ivPlayerTimerIcon = (ImageView)findViewById(R.id.ivPlayerTimerIcon);

        tvArtiName = (TextView)findViewById(R.id.tvArtiName);
        ivPlayBtn = (ImageView)findViewById(R.id.ivPlayBtn);

        tvPostSubject = (LetterSpacingTextView)findViewById(R.id.tvPostSubject);
        tvPostContent = (EllipsizingTextView) findViewById(R.id.tvPostContent);
        postContentLayout = (LinearLayout)findViewById(R.id.postContentLayout);
        ivOstImage = (CircularImageView)findViewById(R.id.ivOstImage);
        tvOstSongName = (TextView)findViewById(R.id.tvOstSongName);
        tvOstArtiName = (TextView)findViewById(R.id.tvOstArtiName);
        ivPostOstTitle = (ImageView)findViewById(R.id.ivPostOstTitle);

        llPostRadioUnderLine = (LinearLayout)findViewById(R.id.llPostRadioUnderLine);
        llTodayUnderLine = (LinearLayout)findViewById(R.id.llTodayUnderLine);
        tvTodaySubject = (TextView)findViewById(R.id.tvTodaySubject);

        if (mdbHelper == null)
            mdbHelper = new PostDatabases(this);

        mdbHelper.open();
        PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
        mdbHelper.close();

        rlPrevBtn = (RelativeLayout)findViewById(R.id.rlPrevBtn);
        rlPlayBtn = (RelativeLayout)findViewById(R.id.rlPlayBtn);
        rlNextBtn = (RelativeLayout)findViewById(R.id.rlNextBtn);

        // 더보기 팝업
        rlMoreLayout = (RelativeLayout)findViewById(R.id.rlMoreLayout);
        rlMoreLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlMoreLayoutCloseBtn);
        rlMoreLayoutCloseBtn.setOnClickListener(this);
        tvMorePostSubject = (LetterSpacingTextView)findViewById(R.id.tvMorePostSubject);
        llMorePostSubjectUnderLine = (LinearLayout)findViewById(R.id.llMorePostSubjectUnderLine);
        tvMorePostContent = (EllipsizingTextView)findViewById(R.id.tvMorePostContent);

        ivRotate = (ImageView)findViewById(R.id.ivRotate);

        tvPlayingDuration = (LetterSpacingTextView)findViewById(R.id.tvPlayingDuration);
        tvPlayingDuration.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvPlayingDuration.setText(getString(R.string.common_define_play_time));
        tvPlayDuration = (LetterSpacingTextView)findViewById(R.id.tvPlayDuration);
        tvPlayDuration.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvPlayDuration.setText(getString(R.string.common_define_play_time));
        tvPlayLeftDuration = (LetterSpacingTextView)findViewById(R.id.tvPlayLeftDuration);
        tvPlayLeftDuration.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvPlayLeftDuration.setText(getString(R.string.common_define_play_time));
        llPlayThumb = (LinearLayout)findViewById(R.id.llPlayThumb);
        mSeekBar = (SeekBar)findViewById(R.id.playProgressBar);

        updateInitUI();
    }

    private void updateInitUI() {
        // 재생목록이 있을 경우
        if (PlayerConstants.SONG_ON_AIR || PlayerConstants.SONGS_LIST.size() > 0) {
            rlNextBtn.setEnabled(true);
            rlPlayBtn.setEnabled(true);
            rlPrevBtn.setEnabled(true);
        }
        // 재생목록이 없을 경우
        else {
            ivPlayerBackground.setImageResource(R.drawable.img_int_temp);
            rlNextBtn.setEnabled(false);
            rlPlayBtn.setEnabled(false);
            rlPrevBtn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            // 이전 곡 버튼 onClick
            case R.id.rlPrevBtn:
                Controls.previousControl(mContext);
                break;
            // 재생 버튼 onClick
            case R.id.rlPlayBtn:
                if (PlayerConstants.SONG_PAUSED) {
                    Controls.playControl(mContext);
                } else {
                    if (PlayerConstants.SONG_ON_AIR) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_RADIO_ON_AIR_CLOSE, onGlobalClickListener);
                        mPostDialog.show();
                    } else {
                        Controls.pauseControl(mContext);
                    }
                }
                break;
            // 다음 곡 버튼 onClick
            case R.id.rlNextBtn:
                Controls.nextControl(mContext);
                break;
            // 안내 확인 onClick
            case R.id.btnInfoConfirm:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                break;
            // 더보기 팝업 > 닫기 onClick
            case R.id.rlMoreLayoutCloseBtn:
                Animation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(500);
                rlMoreLayout.setVisibility(View.GONE);
                rlMoreLayout.setAnimation(animation);
                break;
            // 컨텐츠 레이아웃 / OST 타이틀 onClick
            case R.id.llContentLayout:
            case R.id.llStoryOstLayout:
                if (ivPlayerAlbumBackground.getVisibility() == View.GONE) {
                    ivPlayerAlbumBackground.setVisibility(View.VISIBLE);
                    ivPlayerAlbumBackground.startAnimation(getFadeInAnimation(1000));

                    llEmptyStoryLayout.setVisibility(View.VISIBLE);
                    llEmptyStoryLayout.startAnimation(getFadeInAnimation(1000));

                    llStoryLayout.setVisibility(View.GONE);
                    llStoryLayout.startAnimation(getFadeOutAnimation(1000));
                } else {
                    ivPlayerAlbumBackground.setVisibility(View.GONE);
                    ivPlayerAlbumBackground.startAnimation(getFadeOutAnimation(1000));

                    llEmptyStoryLayout.setVisibility(View.GONE);
                    llEmptyStoryLayout.startAnimation(getFadeOutAnimation(1000));

                    llStoryLayout.setVisibility(View.VISIBLE);
                    llStoryLayout.startAnimation(getFadeInAnimation(1000));
                }
                break;
        }
    }

    private void getData(int queryType, Object... args) {
        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_POST_DATA) {
            if (args != null && args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
                thread = getPostDataThread((String)args[0], (String)args[1]);
            }
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getPostDataThread(final String POST_TYPE, final String POI) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetPostPositionDataReq getPostPositionDataReq = new GetPostPositionDataReq();
                    getPostPositionDataReq.setPOI(POI);
                    getPostPositionDataReq.setPOI_MOVE_FLAG("MF");
                    mPostDataItem = request.getPostPositionData(getPostPositionDataReq);
                    mPostType = POST_TYPE;
                    mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA);
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
        switch(msg.what) {
            // POST 조회 후 Handler
            case Constants.QUERY_POST_DATA:
                if (CommonUtil.isNull(mPostDataItem.getPOI())) {
                    llStoryContentEmpty.setVisibility(View.VISIBLE);
                    llStoryContent.setVisibility(View.GONE);
                } else {
                    llStoryContentEmpty.setVisibility(View.GONE);
                    llStoryContent.setVisibility(View.VISIBLE);
                }

                // POST 배경
                if (!"".equals(mPostDataItem.getBG_USER_PATH())) {
                    mGlideRequestManager
                            .load(mPostDataItem.getBG_USER_PATH())
                            .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                            .animate(animationObject)
                            .into(ivPlayerBackground);
                } else {
                    mGlideRequestManager
                            .load(mPostDataItem.getBG_PIC_PATH())
                            .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                            .animate(animationObject)
                            .into(ivPlayerBackground);
                }

                // POST 제목
                String postTitle = "";
                if (mPostType.equals(Constants.REQUEST_TYPE_TODAY)) {
                    llPostRadioUnderLine.setVisibility(View.GONE);
                    llTodayUnderLine.setVisibility(View.VISIBLE);
                    tvTodaySubject.setVisibility(View.VISIBLE);

                    postTitle = mPostDataItem.getKWD();
                } else if (mPostType.equals(Constants.REQUEST_TYPE_POST) || mPostType.equals(Constants.REQUEST_TYPE_RADIO)) {
                    llPostRadioUnderLine.setVisibility(View.VISIBLE);
                    llTodayUnderLine.setVisibility(View.GONE);
                    tvTodaySubject.setVisibility(View.GONE);

                    postTitle = mPostDataItem.getPOST_SUBJ();
                }

                if (CommonUtil.isNull(postTitle)) {
                    tvPostSubject.setTextColor(Color.parseColor("#FFFFFFFF"));
                    tvPostSubject.setAlpha(0.5f);
                    tvPostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                    tvPostSubject.setSpacing(Constants.TEXT_VIEW_SPACING);

                    if (mPostType.equals(Constants.REQUEST_TYPE_POST)) {
                        tvPostSubject.setText(mContext.getString(R.string.post_story));
                    } else if (mPostType.equals(Constants.REQUEST_TYPE_RADIO)) {
                        tvPostSubject.setText(mContext.getString(R.string.post_radio));
                    }
                } else {
                    if (!"".equals(mPostDataItem.getCOLOR_HEX())) {
                        tvPostSubject.setTextColor(Color.parseColor("#" + mPostDataItem.getCOLOR_HEX()));
                    } else {
                        tvPostSubject.setTextColor(Color.parseColor("#FFFFFFFF"));
                    }
                    tvPostSubject.setAlpha(1.0f);
                    tvPostSubject.setSpacing(0.0f);
                    tvPostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
                    tvPostSubject.setText(postTitle);
                }

                // TODAY 제목
                if (mPostType.equals(Constants.REQUEST_TYPE_TODAY)) {
                    tvTodaySubject.setText(mPostDataItem.getPOST_SUBJ());
                    if (!"".equals(mPostDataItem.getCOLOR_HEX())) tvTodaySubject.setTextColor(Color.parseColor("#" + mPostDataItem.getCOLOR_HEX()));
                }

                // POST 내용
                if (mPostType.equals(Constants.REQUEST_TYPE_POST))
                    tvPostContent.setMaxLines(8);
                else if (mPostType.equals(Constants.REQUEST_TYPE_TODAY))
                    tvPostContent.setMaxLines(4);
                else if (mPostType.equals(Constants.REQUEST_TYPE_RADIO))
                    tvPostContent.setMaxLines(6);

                tvPostContent.setHandler(mHandler);
                tvPostContent.setScrollEnabled(false);
                tvPostContent.setText(mPostDataItem.getPOST_CONT());

                // 제목 / 내용 정렬
                if (Constants.REQUEST_POST_PST_TYPE_LEFT.equals(mPostDataItem.getPOST_PST_TYPE()) || "".equals(mPostDataItem.getPOST_PST_TYPE())) {
                    tvPostSubject.setGravity(Gravity.START|Gravity.CENTER);
                    llPostRadioUnderLine.setGravity(Gravity.START);
                    llTodayUnderLine.setGravity(Gravity.START);
                    tvPostContent.setGravity(Gravity.TOP|Gravity.START);
                } else if (Constants.REQUEST_POST_PST_TYPE_CENTER.equals(mPostDataItem.getPOST_PST_TYPE())) {
                    tvPostSubject.setGravity(Gravity.CENTER|Gravity.CENTER);
                    llPostRadioUnderLine.setGravity(Gravity.CENTER);
                    llTodayUnderLine.setGravity(Gravity.CENTER);
                    tvPostContent.setGravity(Gravity.TOP|Gravity.CENTER);
                } else if (Constants.REQUEST_POST_PST_TYPE_RIGHT.equals(mPostDataItem.getPOST_PST_TYPE())) {
                    tvPostSubject.setGravity(Gravity.END|Gravity.CENTER);
                    llPostRadioUnderLine.setGravity(Gravity.END);
                    llTodayUnderLine.setGravity(Gravity.END);
                    tvPostContent.setGravity(Gravity.TOP|Gravity.END);
                }

                // POST RADIO
                mRadioPath = mPostDataItem.getRADIO_PATH();
                int height;
                if (CommonUtil.isNull(mRadioPath)) {
                    if (mPostType.equals(Constants.REQUEST_TYPE_TODAY))
                        height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 78, mContext.getResources().getDisplayMetrics());
                    else
                        height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 124, mContext.getResources().getDisplayMetrics());
                } else {
                    height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, mContext.getResources().getDisplayMetrics());
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                postContentLayout.setLayoutParams(lp);
                break;
            // Notice Dialog Dismiss Handler
            case Constants.DIALOG_TYPE_NOTICE_CLOSE:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                break;
            // 재생타이머 UI 처리 Handler
            case Constants.QUERY_PLAYER_TIMER:
                if (isPlayerTimer != PlayerConstants.PLAYER_TIMER) {
                    if (PlayerConstants.PLAYER_TIMER) {
                        llPlayerTimerLayout.setVisibility(View.VISIBLE);
                        Animation animation = new AlphaAnimation(1.0f, 0.2f);
                        animation.setDuration(1000);
                        animation.setRepeatMode(Animation.REVERSE);
                        animation.setRepeatCount(Animation.INFINITE);
                        ivPlayerTimerIcon.startAnimation(animation);
                    } else {
                        llPlayerTimerLayout.setVisibility(View.GONE);
                        ivPlayerTimerIcon.clearAnimation();
                    }
                    isPlayerTimer = PlayerConstants.PLAYER_TIMER;
                }

                if (PlayerConstants.PLAYER_TIMER) {
                    long currentTimeSec = System.currentTimeMillis() / 1000;
                    long playingTimeSecond = currentTimeSec - PlayerConstants.PLAYER_TIMER_START_TIME;
                    long lostPlayingTimeSecond = mPlayerTimerTotalSecond - playingTimeSecond;
                    lstvPlayerTimerLostSecondText.setText(DateUtil.getConvertMsToFormat(lostPlayingTimeSecond));
                }
                break;
            // 더보기 Handler
            case Constants.QUERY_POST_DATA_MORE:
                if (CommonUtil.isNull(mPostDataItem.getPOST_SUBJ())) {
                    tvMorePostSubject.setAlpha(0.5f);
                    tvMorePostSubject.setSpacing(Constants.TEXT_VIEW_SPACING);
                    tvMorePostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                    tvMorePostSubject.setTextColor(Color.parseColor("#FFFFFFFF"));

                    if (Constants.REQUEST_TYPE_POST.equals(mPostType)) {
                        tvMorePostSubject.setText(mContext.getString(R.string.post_story));
                    } else if (Constants.REQUEST_TYPE_RADIO.equals(mPostType)) {
                        tvMorePostSubject.setText(mContext.getString(R.string.post_radio));
                    }
                } else {
                    tvMorePostSubject.setAlpha(1.0f);
                    tvMorePostSubject.setSpacing(0.0f);
                    tvMorePostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
                    tvMorePostSubject.setText(mPostDataItem.getPOST_SUBJ());
                    tvMorePostSubject.setTextColor(Color.parseColor("#FF" + mPostDataItem.getCOLOR_HEX()));
                }

                tvMorePostContent.setHandler(mHandler);
                tvMorePostContent.setText(mPostDataItem.getPOST_CONT());

                // 제목 / 내용 정렬
                if (Constants.REQUEST_POST_PST_TYPE_LEFT.equals(mPostDataItem.getPOST_PST_TYPE()) || CommonUtil.isNull(mPostDataItem.getPOST_PST_TYPE())) {
                    tvMorePostSubject.setGravity(Gravity.START|Gravity.CENTER);
                    llMorePostSubjectUnderLine.setGravity(Gravity.START);
                    tvMorePostContent.setGravity(Gravity.TOP|Gravity.START);
                } else if (Constants.REQUEST_POST_PST_TYPE_CENTER.equals(mPostDataItem.getPOST_PST_TYPE())) {
                    tvMorePostSubject.setGravity(Gravity.CENTER|Gravity.CENTER);
                    llMorePostSubjectUnderLine.setGravity(Gravity.CENTER);
                    tvMorePostContent.setGravity(Gravity.TOP|Gravity.CENTER);
                } else if (Constants.REQUEST_POST_PST_TYPE_RIGHT.equals(mPostDataItem.getPOST_PST_TYPE())) {
                    tvMorePostSubject.setGravity(Gravity.END|Gravity.CENTER);
                    llMorePostSubjectUnderLine.setGravity(Gravity.END);
                    tvMorePostContent.setGravity(Gravity.TOP|Gravity.END);
                }

                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(500);
                rlMoreLayout.setVisibility(View.VISIBLE);
                rlMoreLayout.setAnimation(animation);
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
            @Override
            public void handleMessage(Message msg){
                Bundle data = (Bundle)msg.obj;
                OstDataItem ostDataItem = data.getParcelable("OstDataItem");
                int DURATON = data.getInt("DURATON", 0);
                int POSITION = data.getInt("POSITION", 0);

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

                mSeekBar.setMax(DURATON);
                if (!isDrag) mSeekBar.setProgress(POSITION);

                tvPlayingDuration.setText(DateUtil.getConvertMsToFormat(POSITION / 1000));
                tvPlayLeftDuration.setText(DateUtil.getConvertMsToFormat((DURATON - POSITION) / 1000));

                if (ostDataItem != null) {
                    if (!playerSSI.equals(ostDataItem.getPOI() + ostDataItem.getSSI())) {
                        playerSSI = ostDataItem.getPOI() + ostDataItem.getSSI();

                        ivPlayerAlbumBackground.setVisibility(View.GONE);

                        // 스토리가 없는 음악
                        if (CommonUtil.isNull(ostDataItem.getPOI())) {
                            llContentLayout.setOnClickListener(null);
                            llEmptyStoryLayout.setVisibility(View.VISIBLE);
                            llStoryLayout.setVisibility(View.GONE);
                            llStoryOstLayout.setVisibility(View.GONE);

                            mGlideRequestManager
                                    .load(ostDataItem.getALBUM_PATH())
                                    .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                    .animate(animationObject)
                                    .into(ivPlayerBackground);

                            mGlideRequestManager
                                    .load(ostDataItem.getALBUM_PATH())
                                    .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, mContext.getResources().getDisplayMetrics()))
                                    .into(ivAlbumImage);

                            if (CommonUtil.isNull(ostDataItem.getCOLOR_HEX())) {
                                ivAlbumImage.setBorderColor(Color.parseColor("#FFFFFFFF"));
                            } else {
                                ivAlbumImage.setBorderColor(Color.parseColor("#" + ostDataItem.getCOLOR_HEX()));
                            }

                            tvSongName.setText((CommonUtil.isNull(ostDataItem.getSONG_NM())) ? "" : ostDataItem.getSONG_NM());
                            tvArtiName.setText((CommonUtil.isNull(ostDataItem.getARTI_NM())) ? "" : ostDataItem.getARTI_NM());
                        }
                        // 스토리가 있는 음악
                        else {
                            llContentLayout.setOnClickListener(LockScreenActivity.this);
                            llEmptyStoryLayout.setVisibility(View.GONE);
                            llStoryLayout.setVisibility(View.VISIBLE);
                            llStoryOstLayout.setVisibility(View.VISIBLE);

                            // OST 타이틀 영역
                            llStoryOstLayout.setVisibility(View.VISIBLE);
                            if (!"".equals(ostDataItem.getALBUM_PATH())) {
                                mGlideRequestManager
                                        .load(ostDataItem.getALBUM_PATH())
                                        .error(R.drawable.icon_album_dummy)
                                        .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                                        .into(ivOstImage);
                            }
                            tvOstSongName.setText(ostDataItem.getSONG_NM());
                            tvOstArtiName.setText(ostDataItem.getARTI_NM());

                            if (ostDataItem.getPOST_TYPE().equals(Constants.REQUEST_TYPE_POST)) {
                                if ("Y".equals(ostDataItem.getTITL_TOGGLE_YN()))
                                    ivPostOstTitle.setImageResource(R.drawable.icon_title_post);
                                else
                                    ivPostOstTitle.setImageResource(R.drawable.icon_ost_post);
                            } else if (ostDataItem.getPOST_TYPE().equals(Constants.REQUEST_TYPE_RADIO)) {
                                if ("Y".equals(ostDataItem.getTITL_TOGGLE_YN()))
                                    ivPostOstTitle.setImageResource(R.drawable.icon_title_radio);
                                else
                                    ivPostOstTitle.setImageResource(R.drawable.icon_ost_radio);
                            } else if (ostDataItem.getPOST_TYPE().equals(Constants.REQUEST_TYPE_TODAY)) {
                                if ("Y".equals(ostDataItem.getTITL_TOGGLE_YN()))
                                    ivPostOstTitle.setImageResource(R.drawable.icon_title_today);
                                else
                                    ivPostOstTitle.setImageResource(R.drawable.icon_ost_today);
                            } else {
                                ivPostOstTitle.setImageResource(android.R.color.transparent);
                            }

                            mGlideRequestManager
                                    .load(ostDataItem.getALBUM_PATH())
                                    .error(R.drawable.img_int_temp)
                                    .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                                    .animate(animationObject)
                                    .into(ivPlayerAlbumBackground);

                            mGlideRequestManager
                                    .load(ostDataItem.getALBUM_PATH())
                                    .error(R.drawable.icon_album_dummy)
                                    .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, mContext.getResources().getDisplayMetrics()))
                                    .into(ivAlbumImage);

                            if (CommonUtil.isNull(ostDataItem.getCOLOR_HEX())) {
                                ivAlbumImage.setBorderColor(Color.parseColor("#FFFFFFFF"));
                            } else {
                                ivAlbumImage.setBorderColor(Color.parseColor("#" + ostDataItem.getCOLOR_HEX()));
                            }

                            tvSongName.setText((CommonUtil.isNull(ostDataItem.getSONG_NM())) ? "" : ostDataItem.getSONG_NM());
                            tvArtiName.setText((CommonUtil.isNull(ostDataItem.getARTI_NM())) ? "" : ostDataItem.getARTI_NM());

                            getData(Constants.QUERY_POST_DATA, ostDataItem.getPOST_TYPE(), ostDataItem.getPOI());
                        }

                        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (llPlayThumb.getVisibility() == View.VISIBLE) {
                                    tvPlayDuration.setText(DateUtil.getConvertMsToFormat(progress / 1000));
                                    int padding = mSeekBar.getPaddingLeft() + mSeekBar.getPaddingRight();
                                    int startPos = mSeekBar.getLeft() + mSeekBar.getPaddingLeft();
                                    int moveX = (mSeekBar.getWidth()-padding) * mSeekBar.getProgress() / mSeekBar.getMax() + startPos - (llPlayThumb.getWidth()/2);
                                    llPlayThumb.setX(moveX);
                                }
                            }
                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                                isDrag = true;
                                llPlayThumb.setVisibility(View.VISIBLE);
                                llPlayThumb.setAnimation(getFadeInAnimation(500));
                            }
                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                isDrag = false;
                                Intent intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                                intent.setPackage(Constants.SERVICE_PACKAGE);
                                intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_SET);
                                intent.putExtra("SEEKBAR_POSITION", seekBar.getProgress());
                                startService(intent);

                                llPlayThumb.setVisibility(View.GONE);
                                llPlayThumb.setAnimation(getFadeOutAnimation(500));
                            }
                        });
                    }
                } else {
                    finish();
                }

                int currentPauseCode;
                if (PlayerConstants.SONG_PAUSED)
                    currentPauseCode = 1;
                else
                    currentPauseCode = 0;

                if (currentPause != currentPauseCode) {
                    currentPause = currentPauseCode;

                    if (currentPause == 1) {
                        ivPlayBtn.setImageResource(R.drawable.bt_player_play);
                        ivRotate.clearAnimation();
                    } else {
                        ivPlayBtn.setImageResource(R.drawable.bt_player_pause);
                        ivRotate = (ImageView)findViewById(R.id.ivRotate);
                        RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
                        rotate.setDuration(500);
                        rotate.setRepeatCount(Animation.INFINITE);
                        ivRotate.startAnimation(rotate);
                    }
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 재생타이머 이동 후 ActivityResult
            case Constants.QUERY_PLAYER_TIMER:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    if (PlayerConstants.PLAYER_TIMER)
                        mPlayerTimerTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER);
                }
                break;
        }
    }
}
