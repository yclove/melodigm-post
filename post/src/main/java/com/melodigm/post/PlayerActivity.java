package com.melodigm.post;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.menu.CabinetActivity;
import com.melodigm.post.menu.SettingPlayerActivity;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.AddCabinetMusicItem;
import com.melodigm.post.protocol.data.AddCabinetMusicReq;
import com.melodigm.post.protocol.data.GetMusicPathReq;
import com.melodigm.post.protocol.data.GetMusicPathRes;
import com.melodigm.post.protocol.data.GetPostPositionDataReq;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.data.SetPostLikeReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.search.SearchMusicActivity;
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
import com.melodigm.post.widget.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener, MediaPlayer.OnCompletionListener, OnGestureListener {
    private static final int PLAYER_DISPLAY_MODE_STORY = 1;
    private static final int PLAYER_DISPLAY_MODE_LIST = 2;
    private static final int PLAYER_DISPLAY_MODE_EDIT = 3;

    private int mDiaplayType = PLAYER_DISPLAY_MODE_LIST;
    private String mPostType;
    private PostDataItem mPostDataItem;
    private GetMusicPathRes mGetMusicPathRes;
    private LinearLayout llContentLayout, llEmptyStoryLayout, llEmptyStoryOstLayout, llStoryLayout, llStoryContent, llStoryContentEmpty, llStoryOstLayout, llMoveStory, llPlayThumb, postContentLayout, radioPlayerLayout, llBuyUseCoupon;
    private LinearLayout llPlayerHeader, llPlayerHeaderEdit, llPlayerFooter, llPlayerEditFooter, llPlayerListEmptyLayout, llPlayerHeaderPutBtn, llPlayerHeaderEditBtn, llPostRadioUnderLine, llTodayUnderLine, llPlayerTimerLayout, llMorePostSubjectUnderLine, llPostLikeBtn;
    private LetterSpacingTextView tvPostSubject, lstvStoryType, tvPlayingDuration, tvPlayDuration, tvPlayLeftDuration, tvRadioPlay, tvRadioPlayDuration, lstvPlayerTimerLostSecondText, tvMorePostSubject;
    private RelativeLayout rlAlbumImage, rlRadioPlayBtn, rlPlayerToggleBtn, rlPrevBtn, rlPlayBtn, rlNextBtn, rlShareBtn, rlMoreLayout, rlMoreLayoutCloseBtn;
    private CircularImageView ivAlbumImage, ivOstImage;
    private ImageView ivPlayerBackground, ivPlayerAlbumBackground, ivRepeatBtn, ivRandomBtn, ivPlayBtn, ivPostOstTitle, ivRadioPlay, ivPlayerToggleImage, ivPostLikeBtn, ivPlayerTimerIcon, ivOnAirIcon;
    private TextView tvPlayerHeaderQuality, tvSongName, tvArtiName, tvOstSongName, tvOstArtiName, tvTodaySubject, tvPlayerHeaderSelectAll, tvSelectCnt, tvPostLikeCount;
    private SeekBar mSeekBar, mVolumeSeekBar;
    private EllipsizingTextView tvPostContent, tvMorePostContent;

    private DragSortListView lvPlayer;
    private View vListViewTransHeader, vListViewTransFooter;
    private PlayerAdapter mPlayerAdapter;
    private View vContentFooterBG;

    private boolean isShowPlayerList = false;
    private boolean isVolumeCtr = false;
    private boolean isDrag = false;
    private AudioManager audioManager;
    private int maxVolume;
    private int curVolume;
    private String mRadioPath;
    private String mBXI;
    private boolean isSelectedAll = true;
    private String putMode = "ALL";

    private int mPlayerTimerTotalSecond = 0;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private GestureDetector gestureDetector, gestureScanner;
    private View.OnTouchListener touchListener;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        int rtPosition = PlayerConstants.SONG_NUMBER;
                        if (from == PlayerConstants.SONG_NUMBER) {
                            rtPosition = to;
                        } else if (from < PlayerConstants.SONG_NUMBER && to >= PlayerConstants.SONG_NUMBER) {
                            rtPosition = PlayerConstants.SONG_NUMBER - 1;
                        } else if (from > PlayerConstants.SONG_NUMBER && to <= PlayerConstants.SONG_NUMBER) {
                            rtPosition = PlayerConstants.SONG_NUMBER + 1;
                        }

                        OstDataItem item = mPlayerAdapter.getItem(from);
                        mPlayerAdapter.remove(item);
                        mPlayerAdapter.insert(item, to);

                        PlayerConstants.SONG_NUMBER = rtPosition;
                        PlayerConstants.SONGS_LIST = mPlayerAdapter.getAllItems();

                        mdbHelper = new PostDatabases(mContext);
                        mdbHelper.open();
                        mdbHelper.deleteAllOstPlayList();
                        for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                            mdbHelper.updateOstPlayList(mPlayerAdapter.getItem(i), "BOTTOM");
                        }
                        mdbHelper.close();
                    }
                }

            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        /**
         * YCNOTE - ContentObserver
         * ContentObserver 는 해당 URI 를 Observe 하다가, 변경이 생기면 알려주는 놈입니다.
         */
        this.getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mVolumeObserver );
        gestureScanner = new GestureDetector(this);

        Intent intent = getIntent();
        isShowPlayerList = intent.getBooleanExtra("SHOW_PLAYER_LIST", false);

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

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gestureScanner.onTouchEvent(me);
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                Controls.nextControl(mContext);
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                Controls.previousControl(mContext);
            // down to up swipe
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
                return true;
            // up to down swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
                return true;
        } catch (Exception e) {}
        return true;
    }

    public void onLongPress(MotionEvent e) {}

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public void onShowPress(MotionEvent e) {}

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (mVolumeSeekBar != null && audioManager != null) {
                int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mVolumeSeekBar.setProgress(volume);
            }
        }
    };

    /*public boolean onKeyDown(int keycode, KeyEvent event){
        if(keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_DOWN){
            return true;
        }
        return false;
    }*/

    private void setDisplay() {
        Intent intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
        intent.setPackage(Constants.SERVICE_PACKAGE);
        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_GET);
        startService(intent);

        llContentLayout = (LinearLayout)findViewById(R.id.llContentLayout);
        vContentFooterBG = findViewById(R.id.vContentFooterBG);
        llPlayerListEmptyLayout = (LinearLayout)findViewById(R.id.llPlayerListEmptyLayout);
        llPlayerHeader = (LinearLayout)findViewById(R.id.llPlayerHeader);
        llPlayerHeaderPutBtn = (LinearLayout)findViewById(R.id.llPlayerHeaderPutBtn);
        llPlayerHeaderEditBtn = (LinearLayout)findViewById(R.id.llPlayerHeaderEditBtn);
        tvPlayerHeaderSelectAll = (TextView)findViewById(R.id.tvPlayerHeaderSelectAll);
        tvSelectCnt = (TextView)findViewById(R.id.tvSelectCnt);
        llPlayerHeaderEdit = (LinearLayout)findViewById(R.id.llPlayerHeaderEdit);
        llPlayerFooter = (LinearLayout)findViewById(R.id.llPlayerFooter);
        llPlayerEditFooter = (LinearLayout)findViewById(R.id.llPlayerEditFooter);
        lvPlayer = (DragSortListView)findViewById(R.id.lvPlayer);
        ivPlayerToggleImage = (ImageView)findViewById(R.id.ivPlayerToggleImage);
        llEmptyStoryLayout = (LinearLayout)findViewById(R.id.llEmptyStoryLayout);
        llEmptyStoryOstLayout = (LinearLayout)findViewById(R.id.llEmptyStoryOstLayout);
        llBuyUseCoupon = (LinearLayout)findViewById(R.id.llBuyUseCoupon);
        llBuyUseCoupon.setOnClickListener(onGlobalClickListener);
        llStoryLayout = (LinearLayout)findViewById(R.id.llStoryLayout);
        llStoryContent = (LinearLayout)findViewById(R.id.llStoryContent);
        llStoryContentEmpty = (LinearLayout)findViewById(R.id.llStoryContentEmpty);
        llStoryOstLayout = (LinearLayout)findViewById(R.id.llStoryOstLayout);
        llStoryOstLayout.setOnClickListener(this);
        ivPlayerBackground = (ImageView)findViewById(R.id.ivPlayerBackground);
        ivPlayerAlbumBackground = (ImageView)findViewById(R.id.ivPlayerAlbumBackground);
        llMoveStory = (LinearLayout)findViewById(R.id.llMoveStory);
        ivOnAirIcon = (ImageView)findViewById(R.id.ivOnAirIcon);
        lstvStoryType = (LetterSpacingTextView)findViewById(R.id.lstvStoryType);
        lstvStoryType.setSpacing(Constants.TEXT_VIEW_SPACING);
        rlAlbumImage = (RelativeLayout)findViewById(R.id.rlAlbumImage);
        ivAlbumImage = (CircularImageView)findViewById(R.id.ivAlbumImage);
        tvSongName = (TextView)findViewById(R.id.tvSongName);
        mSeekBar = (SeekBar)findViewById(R.id.playProgressBar);
        llPostLikeBtn = (LinearLayout)findViewById(R.id.llPostLikeBtn);

        // Player Timer 레이아웃
        llPlayerTimerLayout = (LinearLayout)findViewById(R.id.llPlayerTimerLayout);
        lstvPlayerTimerLostSecondText = (LetterSpacingTextView)findViewById(R.id.lstvPlayerTimerLostSecondText);
        lstvPlayerTimerLostSecondText.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvPlayerTimerLostSecondText.setText(getString(R.string.common_define_play_time));
        ivPlayerTimerIcon = (ImageView)findViewById(R.id.ivPlayerTimerIcon);

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
        tvArtiName = (TextView)findViewById(R.id.tvArtiName);
        mVolumeSeekBar = (SeekBar)findViewById(R.id.playVolumeBar);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeekBar.setMax(maxVolume);
        mVolumeSeekBar.setProgress(curVolume);
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStopTrackingTouch(SeekBar arg0) {}
            @Override public void onStartTrackingTouch(SeekBar arg0) {}
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });
        ivRepeatBtn = (ImageView)findViewById(R.id.ivRepeatBtn);
        ivRandomBtn = (ImageView)findViewById(R.id.ivRandomBtn);
        ivPlayBtn = (ImageView)findViewById(R.id.ivPlayBtn);

        tvPostSubject = (LetterSpacingTextView)findViewById(R.id.tvPostSubject);
        tvPostContent = (EllipsizingTextView) findViewById(R.id.tvPostContent);
        postContentLayout = (LinearLayout)findViewById(R.id.postContentLayout);
        radioPlayerLayout = (LinearLayout)findViewById(R.id.radioPlayerLayout);
        rlRadioPlayBtn = (RelativeLayout)findViewById(R.id.rlRadioPlayBtn);
        mPlayProgressBar = (SeekBar)findViewById(R.id.radioPlayProgressBar);
        ivRadioPlay = (ImageView)findViewById(R.id.ivRadioPlay);
        tvRadioPlay = (LetterSpacingTextView)findViewById(R.id.tvRadioPlay);
        tvRadioPlay.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvRadioPlay.setText(getString(R.string.play));
        tvRadioPlayDuration = (LetterSpacingTextView)findViewById(R.id.tvRadioPlayDuration);
        tvRadioPlayDuration.setSpacing(0);
        tvRadioPlayDuration.setText(getString(R.string.common_define_play_time));
        ivOstImage = (CircularImageView)findViewById(R.id.ivOstImage);
        tvOstSongName = (TextView)findViewById(R.id.tvOstSongName);
        tvOstArtiName = (TextView)findViewById(R.id.tvOstArtiName);
        ivPostLikeBtn = (ImageView)findViewById(R.id.ivPostLikeBtn);
        tvPostLikeCount = (TextView)findViewById(R.id.tvPostLikeCount);
        ivPostOstTitle = (ImageView)findViewById(R.id.ivPostOstTitle);

        llPostRadioUnderLine = (LinearLayout)findViewById(R.id.llPostRadioUnderLine);
        llTodayUnderLine = (LinearLayout)findViewById(R.id.llTodayUnderLine);
        tvTodaySubject = (TextView)findViewById(R.id.tvTodaySubject);

        tvPlayerHeaderQuality = (TextView)findViewById(R.id.tvPlayerHeaderQuality);
        updateQualityUI();

        if (mdbHelper == null)
            mdbHelper = new PostDatabases(this);

        mdbHelper.open();
        PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
        mdbHelper.close();

        mPlayerAdapter = new PlayerAdapter(mContext, R.layout.adapter_player, mHandler, PlayerConstants.SONGS_LIST, mGlideRequestManager);
        vListViewTransHeader = getLayoutInflater().inflate(R.layout.view_listview_header_trans, null, false);
        vListViewTransFooter = getLayoutInflater().inflate(R.layout.view_listview_footer_trans, null, false);
        //lvPlayer.addHeaderView(vListViewTransHeader, null, false);
        lvPlayer.addFooterView(vListViewTransFooter, null, false);
        lvPlayer.setAdapter(mPlayerAdapter);
        lvPlayer.setDropListener(onDrop);
        mPlayerAdapter.notifyDataSetChanged();
        lvPlayer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                if (mDiaplayType == PLAYER_DISPLAY_MODE_LIST) {
                    mPlayerAdapter.setDeleteLayoutAnimationPosition(-1, false);
                    mPlayerAdapter.notifyDataSetChanged();

                    if (PlayerConstants.SONG_NUMBER == position) {
                        if (PlayerConstants.SONG_PAUSED) {
                            Controls.playControl(mContext);
                        } else {
                            Controls.pauseControl(mContext);
                        }
                    } else {
                        PlayerConstants.SONG_NUMBER = position - 1;
                        Controls.nextControl(mContext);
                    }
                } else if (mDiaplayType == PLAYER_DISPLAY_MODE_EDIT) {
                    int checkedCount = 0;

                    mPlayerAdapter.getItem(position).setIsChecked(!mPlayerAdapter.getItem(position).isChecked());
                    for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                        if (mPlayerAdapter.getItem(i).isChecked()) checkedCount++;
                    }
                    tvSelectCnt.setText(mContext.getString(R.string.n_music_select_count, checkedCount));

                    if (checkedCount > 0) {
                        llPlayerFooter.setVisibility(View.GONE);
                        llPlayerEditFooter.setVisibility(View.VISIBLE);
                        tvPlayerHeaderSelectAll.setText(getString(R.string.common_unselect_all));
                        isSelectedAll = false;
                    } else {
                        llPlayerFooter.setVisibility(View.VISIBLE);
                        llPlayerEditFooter.setVisibility(View.GONE);
                        tvPlayerHeaderSelectAll.setText(getString(R.string.common_select_all));
                        isSelectedAll = true;
                    }

                    mPlayerAdapter.notifyDataSetChanged();
                }
            }
        });

        rlPlayerToggleBtn = (RelativeLayout)findViewById(R.id.rlPlayerToggleBtn);
        rlPrevBtn = (RelativeLayout)findViewById(R.id.rlPrevBtn);
        rlPlayBtn = (RelativeLayout)findViewById(R.id.rlPlayBtn);
        rlNextBtn = (RelativeLayout)findViewById(R.id.rlNextBtn);
        rlShareBtn = (RelativeLayout)findViewById(R.id.rlShareBtn);

        gestureDetector = new GestureDetector(new mGestureDetector());
        touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        lvPlayer.setLisViewOnTouchListener(touchListener);

        // 더보기 팝업
        rlMoreLayout = (RelativeLayout)findViewById(R.id.rlMoreLayout);
        rlMoreLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlMoreLayoutCloseBtn);
        rlMoreLayoutCloseBtn.setOnClickListener(this);
        tvMorePostSubject = (LetterSpacingTextView)findViewById(R.id.tvMorePostSubject);
        llMorePostSubjectUnderLine = (LinearLayout)findViewById(R.id.llMorePostSubjectUnderLine);
        tvMorePostContent = (EllipsizingTextView)findViewById(R.id.tvMorePostContent);

        updateInitUI();
    }

    /**
     * YCNOTE - 제스처
     * interface GestureDetector.OnDoubleTapListener : 두 번 터치했을 경우의 관련 리스너
     * interface GestureDetector.OnGestureListener : 일반적인 제스처들, 한 번 터치나 스크롤 관련 리스너
     * 인터페이스의 모든 메서드를 반드시 구현해야 한다.
     * class GestureDetector.SimpleOnGestureListener : 클래스를 확장하여 모든 제스처에 사용할 수 있다.
     * 모든 멧드가 기본적인 방식으로(그냥 false를 돌려줌) 구현되어 있다. 따라서 응용 프로그램에 필요한 사건에 해당 하느 메서드만 재정의하면 된다.
     *
     * onDown() : 터치하려고 손을 대기만 해도 호출되는 메서드이며, 모든 제스처의 시작이다.
     * onSingleTapUp() : 한번 터치 제스처 중에 UP 이벤트가 발생했을 경우, 호출되는 메서드이다.
     * onSingleTapConfirmed() : 한번 터치 했을 경우, 호출되는 메서드이다.
     * onDoubleTap() : 두 번 터치를 했을 경우, 호출되는 메서드이다.
     * onDoubleTapEvent() : 두 번 터치 제스처 중에 DOWN/MOVE/UP 이벤트가 발생했을 경우, 호출되는 메서드이다.
     * onLongPress() : 길게 눌렀을 경우, 호출되는 메서드이다.
     * onShowPress() : 터치 화면을 처음 눌렀을 경우, 그러나 손가락을 떼거나 다른곳으로 이동하기 전에 호출되는 메서드이다. 터치가 감지되었음을 시청각적으로 표시하고자 할 때 유용하다.
     * onScroll() : 손가락으로 화면을 누른 채 손가락을 일정한 속도와 방향으로 움직인 후 떼었을 경우, 호출되는 메서드이다.
     * onFling() : 손가락으로 화면을 누른 채 가속도를 붙여서 손가락을 움직인 후 떼었을 경우, 호출되는 메서드이다. 이를 던지기(Fling) 또는 튀기기(Flick)라고 부른다. 이 제스처에 대한 일반적인 반응 방식은, 손가락을 뗀 후에도 대상이 일정 기간 동안 계속 움직이게 하는 것이다.
     */
    class mGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // y값이 너무 많이 움직였으면 아무 것도 안함
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    YourSlideRightToLeft(lvPlayer.pointToPosition((int)e1.getX(),(int)e1.getY()));
                    // 제스처 이벤트일 경우 onTouch 이벤트를 무시하기 위하여 false 값을 리턴한다.
                    return false;
                }
                // left to right swipe
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    YourSlideLeftToRight(lvPlayer.pointToPosition((int)e1.getX(),(int)e1.getY()));
                    // 제스처 이벤트일 경우 onTouch 이벤트를 무시하기 위하여 false 값을 리턴한다.
                    return false;
                }
            } catch (Exception e) {
                // nothing
                return true;
            }

            return true;
        }
    }

    public void YourSlideRightToLeft(int position) {
        mPlayerAdapter.setDeleteLayoutAnimationPosition(position, true);
        mPlayerAdapter.notifyDataSetChanged();
    }

    public void YourSlideLeftToRight(int position) {
        mPlayerAdapter.setDeleteLayoutAnimationPosition(position, false);
        mPlayerAdapter.notifyDataSetChanged();
    }

    private void updateInitUI() {
        // 재생목록이 있을 경우
        if (PlayerConstants.SONG_ON_AIR || PlayerConstants.SONGS_LIST.size() > 0) {
            // 재생목록 화면 일 경우
            if (isShowPlayerList) {
                llContentLayout.setVisibility(View.GONE);
                vContentFooterBG.setVisibility(View.GONE);
                lvPlayer.setVisibility(View.VISIBLE);
                lvPlayer.setSelection(PlayerConstants.SONG_NUMBER);
                llPlayerListEmptyLayout.setVisibility(View.GONE);
                llPlayerHeader.setVisibility(View.VISIBLE);
                llPlayerFooter.setBackgroundColor(Color.parseColor("#CC000000"));
                ivPlayerToggleImage.setImageResource(R.drawable.bt_plylist_ok);
            }
            // 재생목록 화면이 아닐 경우
            else {
                llContentLayout.setVisibility(View.VISIBLE);
                vContentFooterBG.setVisibility(View.VISIBLE);
                lvPlayer.setVisibility(View.GONE);
                llPlayerListEmptyLayout.setVisibility(View.GONE);
                llPlayerHeader.setVisibility(View.GONE);
                llPlayerFooter.setBackgroundColor(Color.parseColor("#00000000"));
                ivPlayerToggleImage.setImageResource(R.drawable.bt_plylist_no);
            }

            llPlayerHeaderPutBtn.setAlpha(1.0f);
            llPlayerHeaderEditBtn.setAlpha(1.0f);
            llPlayerFooter.setAlpha(1.0f);
            rlShareBtn.setEnabled(true);
            rlNextBtn.setEnabled(true);
            rlPlayBtn.setEnabled(true);
            rlPrevBtn.setEnabled(true);
            rlPlayerToggleBtn.setEnabled(true);
        }
        // 재생목록이 없을 경우
        else {
            ivPlayerBackground.setImageResource(R.drawable.img_int_temp);
            llContentLayout.setVisibility(View.GONE);
            vContentFooterBG.setVisibility(View.GONE);
            lvPlayer.setVisibility(View.GONE);
            llPlayerListEmptyLayout.setVisibility(View.VISIBLE);
            llPlayerHeader.setVisibility(View.VISIBLE);
            llPlayerFooter.setBackgroundColor(Color.parseColor("#CC000000"));

            ivPlayerToggleImage.setImageResource(R.drawable.bt_plylist_ok);
            llPlayerHeaderPutBtn.setAlpha(0.5f);
            llPlayerHeaderEditBtn.setAlpha(0.5f);
            llPlayerFooter.setAlpha(0.5f);
            rlShareBtn.setEnabled(false);
            rlNextBtn.setEnabled(false);
            rlPlayBtn.setEnabled(false);
            rlPrevBtn.setEnabled(false);
            rlPlayerToggleBtn.setEnabled(false);

            isShowPlayerList = true;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle data = new Bundle();
        Message msg = new Message();
        OstDataItemCompare mOstDataItemCompare;
        OstDataItem mOstDataItem;

        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.rlPlayerHeaderBack:
                finish();
                break;
            // Header 음질 onClick
            case R.id.llPlayerHeaderQuality:
                intent = new Intent(mContext, SettingPlayerActivity.class);
                startActivityForResult(intent, Constants.QUERY_UPDATE_QUALITY);
                break;
            // Header 검색 아이콘 onClick
            case R.id.llPlayerHeaderSearch:
                intent = new Intent(mContext, SearchMusicActivity.class);
                startActivityForResult(intent, Constants.QUERY_MUSIC_SEARCH);
                break;
            // 스토리 이동 onClick
            case R.id.llMoveStory:
                if (CommonUtil.isNotNull(mPostDataItem.getPOI())) {
                    intent = new Intent(mContext, PostDetailActivity.class);
                    intent.putExtra("POST_TYPE", mPostType);
                    intent.putExtra("POI", mPostDataItem.getPOI());
                    startActivityForResult(intent, Constants.QUERY_POST_DATA);
                }
                break;
            // RADIO PLAY onClick
            case R.id.rlRadioPlayBtn:
                mBtnStartPlayOnClick();
                break;
            // Volume onClick
            case R.id.rlVolumeBtn:
                if (isVolumeCtr) {
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);
                    mVolumeSeekBar.setVisibility(View.GONE);
                    mVolumeSeekBar.setAnimation(animation);
                } else {
                    Animation animation = new AlphaAnimation(0.0f, 1.0f);
                    animation.setDuration(500);
                    mVolumeSeekBar.setVisibility(View.VISIBLE);
                    mVolumeSeekBar.setAnimation(animation);
                }
                isVolumeCtr = !isVolumeCtr;
                break;
            // 반복 버튼 onClick
            case R.id.rlRepeatBtn:
                if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ALL) {
                    PlayerConstants.MPS_REPEAT = Constants.MPS_REPEAT_ONE;
                } else if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ONE) {
                    PlayerConstants.MPS_REPEAT = Constants.MPS_REPEAT_NO;
                } else {
                    PlayerConstants.MPS_REPEAT = Constants.MPS_REPEAT_ALL;
                }
                SPUtil.setSharedPreference(mContext, Constants.SP_MPS_REPEAT, PlayerConstants.MPS_REPEAT);
                updatePlayerUI();
                break;
            // 랜덤 버튼 onClick
            case R.id.rlRandomBtn:
                if (PlayerConstants.MPS_RANDOM == Constants.MPS_RANDOM_OK) {
                    PlayerConstants.MPS_RANDOM = Constants.MPS_RANDOM_NO;
                } else {
                    PlayerConstants.MPS_RANDOM = Constants.MPS_RANDOM_OK;
                }
                SPUtil.setSharedPreference(mContext, Constants.SP_MPS_RANDOM, PlayerConstants.MPS_RANDOM);
                updatePlayerUI();
                break;
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
            // 화면 토글 버튼 onClick
            case R.id.rlPlayerToggleBtn:
                Animation visibleAnimation = new AlphaAnimation(0.0f, 1.0f);
                visibleAnimation.setDuration(500);

                Animation goneAnimation = new AlphaAnimation(0.0f, 1.0f);
                goneAnimation.setDuration(500);

                if (isShowPlayerList) {
                    mDiaplayType = PLAYER_DISPLAY_MODE_STORY;
                    llContentLayout.setVisibility(View.VISIBLE);
                    llContentLayout.setAnimation(visibleAnimation);
                    vContentFooterBG.setVisibility(View.VISIBLE);
                    lvPlayer.setVisibility(View.GONE);
                    llPlayerHeader.setVisibility(View.GONE);
                    ivPlayerToggleImage.setImageResource(R.drawable.bt_plylist_no);
                    llPlayerFooter.setBackgroundColor(Color.parseColor("#00000000"));
                } else {
                    if (PlayerConstants.SONG_ON_AIR) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_RADIO_ON_AIR, this, getString(R.string.dialog_radio_on_air_not_support));
                        mPostDialog.show();
                        break;
                    }
                    mDiaplayType = PLAYER_DISPLAY_MODE_LIST;
                    llContentLayout.setVisibility(View.GONE);
                    vContentFooterBG.setVisibility(View.GONE);
                    lvPlayer.setVisibility(View.VISIBLE);
                    lvPlayer.setAnimation(visibleAnimation);
                    if (mPlayerAdapter.getCount() <= PlayerConstants.SONG_NUMBER)  PlayerConstants.SONG_NUMBER = 0;
                    lvPlayer.setSelection(PlayerConstants.SONG_NUMBER);
                    llPlayerHeader.setVisibility(View.VISIBLE);
                    ivPlayerToggleImage.setImageResource(R.drawable.bt_plylist_ok);
                    llPlayerFooter.setBackgroundColor(Color.parseColor("#CC000000"));
                }
                isShowPlayerList = !isShowPlayerList;
                break;
            // 공유 버튼 onClick
            case R.id.rlShareBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAYER_TIMER, this);
                mPostDialog.show();
                break;
            // 재생타이머 Footer > 재생타이머 onClick
            case R.id.llPlayerTimerLayout:
            case R.id.llPlayerTimerBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                intent = new Intent(mContext, PlayerTimerActivity.class);
                startActivityForResult(intent, Constants.QUERY_PLAYER_TIMER);
                break;
            // 재생타이머 Footer > 곡 정보 onClick
            case R.id.llPlayerTimerMusicInfoBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                DeviceUtil.showToast(mContext, "Player Timer Footer > 곡 정보 onClick");
                break;
            // 재생타이머 Footer > 담기 onClick
            case R.id.llPlayerTimerPutBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                if (PlayerConstants.SONGS_LIST.size() > 0) {
                    putMode = "CURRENT";
                    intent = new Intent(mContext, CabinetActivity.class);
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                    startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                }
                break;
            // 재생타이머 Footer > 공유 onClick
            case R.id.llPlayerTimerShareBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                DeviceUtil.showToast(mContext, "Player Timer Footer > 공유 onClick");
                break;
            // PlayList Header > 추가 onClick
            case R.id.llPlayerHeaderAddBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAYER_ADD, this);
                mPostDialog.show();
                break;
            // PlayList Header > 추가 > 보관함 onClick
            case R.id.llPlayerAddCabinetBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                intent = new Intent(mContext, CabinetActivity.class);
                intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PLAY);
                startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                break;
            // PlayList Header > 추가 > 노래 검색 onClick
            case R.id.llPlayerAddSearchMusicBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                intent = new Intent(mContext, SearchMusicActivity.class);
                startActivityForResult(intent, Constants.QUERY_MUSIC_SEARCH);
                break;
            // PlayList Header > 전체 담기 onClick
            case R.id.llPlayerHeaderPutBtn:
                if (mPlayerAdapter.getCount() > 0) {
                    putMode = "ALL";
                    intent = new Intent(mContext, CabinetActivity.class);
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                    startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                }
                break;
            // PlayList Header > 선택 담기 onClick
            case R.id.llPutBtn:
                if (mPlayerAdapter.getCount() > 0) {
                    putMode = "SELECT";
                    intent = new Intent(mContext, CabinetActivity.class);
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                    startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                }
                break;
            // PlayList Header > 편집 onClick
            case R.id.llPlayerHeaderEditBtn:
                if (mPlayerAdapter.getCount() > 0) {
                    mDiaplayType = PLAYER_DISPLAY_MODE_EDIT;
                    lvPlayer.setLisViewOnTouchListener();
                    mPlayerAdapter.setDisplayType(mDiaplayType);

                    // 재생 중 노래 선택 효과 / 삭제 버튼 지우기
                    mPlayerAdapter.setDeleteLayoutAnimationPosition(-1, false);
                    for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                        mPlayerAdapter.getItem(i).setIsChecked(false);
                    }

                    mPlayerAdapter.notifyDataSetChanged();

                    llPlayerHeader.setVisibility(View.GONE);
                    llPlayerHeaderEdit.setVisibility(View.VISIBLE);
                }
                break;
            // PlayList Header > 편집 > 닫기 onClick
            case R.id.llPlayerHeaderEditCloseBtn:
                mDiaplayType = PLAYER_DISPLAY_MODE_LIST;
                lvPlayer.setLisViewOnTouchListener(touchListener);
                mPlayerAdapter.setDisplayType(mDiaplayType);

                // 재생 중 노래 선택 효과 보여주기
                if (mPlayerAdapter.getCount() > 0) {
                    for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                        mPlayerAdapter.getItem(i).setIsChecked(false);
                    }
                    if (mPlayerAdapter.getCount() <= PlayerConstants.SONG_NUMBER)  PlayerConstants.SONG_NUMBER = 0;
                    mPlayerAdapter.getItem(PlayerConstants.SONG_NUMBER).setIsChecked(true);
                }

                mPlayerAdapter.notifyDataSetChanged();

                llPlayerHeader.setVisibility(View.VISIBLE);
                llPlayerHeaderEdit.setVisibility(View.GONE);
                llPlayerFooter.setVisibility(View.VISIBLE);
                llPlayerEditFooter.setVisibility(View.GONE);
                break;
            // 전체선택 / 전체해제 onClick
            case R.id.llPlayerHeaderSelectAllBtn:
                // 전체 선택
                if (isSelectedAll) {
                    for (int i = 0; i < mPlayerAdapter.getCount(); i++)
                        mPlayerAdapter.getItem(i).setIsChecked(true);
                    tvPlayerHeaderSelectAll.setText(getString(R.string.common_unselect_all));
                    mPlayerAdapter.notifyDataSetChanged();

                    tvSelectCnt.setText(mContext.getString(R.string.n_music_select_count, mPlayerAdapter.getCount()));
                    llPlayerFooter.setVisibility(View.GONE);
                    llPlayerEditFooter.setVisibility(View.VISIBLE);
                }
                // 전체 해제
                else {
                    for (int i = 0; i < mPlayerAdapter.getCount(); i++)
                        mPlayerAdapter.getItem(i).setIsChecked(false);
                    tvPlayerHeaderSelectAll.setText(getString(R.string.common_select_all));
                    mPlayerAdapter.notifyDataSetChanged();

                    llPlayerFooter.setVisibility(View.VISIBLE);
                    llPlayerEditFooter.setVisibility(View.GONE);
                }
                isSelectedAll = !isSelectedAll;
                break;
            // 순서정렬 onClick
            case R.id.llPlayerHeaderSortBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAYER_SORT, this);
                mPostDialog.show();
                break;
            // 순서정렬 > 아이템 onClick
            case R.id.llPlayerSortArtiAscBtn:
            case R.id.llPlayerSortArtiDescBtn:
            case R.id.llPlayerSortSongAscBtn:
            case R.id.llPlayerSortSongDescBtn:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                mOstDataItem = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);

                String sortField = (String)v.getTag(R.id.tag_sort_field);
                boolean isAsc = (boolean)v.getTag(R.id.tag_sort_asc);

                mOstDataItemCompare = new OstDataItemCompare(sortField, isAsc);
                Collections.sort(PlayerConstants.SONGS_LIST, mOstDataItemCompare);

                PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.indexOf(mOstDataItem);
                mPlayerAdapter.notifyDataSetChanged();

                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                mdbHelper.deleteAllOstPlayList();
                for (OstDataItem item : PlayerConstants.SONGS_LIST) {
                    mdbHelper.updateOstPlayList(item);
                }
                mdbHelper.close();
                break;
            // Footer 삭제 onClick
            case R.id.llDeleteBtn:
                boolean isChecked = false;
                for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                    if (isChecked) break;
                    if (mPlayerAdapter.getItem(i).isChecked()) isChecked = true;
                }

                if (isChecked) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete));
                    mPostDialog.show();
                } else {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, this, mContext.getResources().getString(R.string.dialog_info_player_select_empty_music));
                    mPostDialog.show();
                }
                break;
            // 안내 확인 onClick
            case R.id.btnInfoConfirm:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                break;
            // Footer 삭제 확인 onClick
            case R.id.btnConfirmConfirm:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();

                String nowPOI = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI();
                String nowSSI = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI();

                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();

                String POI_SSI[] = new String[2];
                int updateCount = 0;
                for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                    if (mPlayerAdapter.getItem(i).isChecked()) {
                        POI_SSI[0] = mPlayerAdapter.getItem(i).getPOI();
                        POI_SSI[1] = mPlayerAdapter.getItem(i).getSSI();
                        updateCount = updateCount + mdbHelper.deleteOstPlayList(POI_SSI);
                    }
                }
                PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
                mdbHelper.close();

                boolean isExistMusic = false;
                for (int i = 0; i < PlayerConstants.SONGS_LIST.size(); i++) {
                    if (nowPOI.equals(PlayerConstants.SONGS_LIST.get(i).getPOI()) && nowSSI.equals(PlayerConstants.SONGS_LIST.get(i).getSSI())) {
                        isExistMusic = true;
                        PlayerConstants.SONG_NUMBER = i;
                        break;
                    }
                }

                if (!isExistMusic) {
                    PlayerConstants.SONG_NUMBER = 0;
                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ON_AIR_CLEAR);
                    startService(intent);
                }

                tvSelectCnt.setText(mContext.getString(R.string.n_music_select_count, 0));

                data.putInt("UPDATE_COUNT", updateCount);
                msg.setData(data);
                msg.what = Constants.QUERY_PLAY_LIST_DELETE;
                mHandler.sendMessage(msg);
                break;
            // 더보기 팝업 > 닫기 onClick
            case R.id.rlMoreLayoutCloseBtn:
                rlMoreLayout.setVisibility(View.GONE);
                rlMoreLayout.setAnimation(getFadeOutAnimation(500));
                break;
            // POST 좋아요 onClick
            case R.id.llPostLikeBtn:
                if (mPostDataItem != null && CommonUtil.isNotNull(mPostDataItem.getPOI())) {
                    getData(Constants.QUERY_POST_LIKE, mPostDataItem.getPOI(), Constants.REQUEST_REG_PLAC_TYPE_POST, mPostDataItem.getLIKE_TOGGLE_YN());
                }
                break;
            // OST 타이틀 onClick
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

    class OstDataItemCompare implements Comparator<OstDataItem> {
        final String mSortField;
        final boolean isAsc;

        OstDataItemCompare(String sortField, boolean asc) {
            this.mSortField = sortField;
            this.isAsc = asc;
        }

        @Override
        public int compare(OstDataItem s1, OstDataItem s2) {
            if ("ARTI".equals(mSortField))
                if (isAsc)
                    return s1.getARTI_NM().compareTo(s2.getARTI_NM());
                else
                    return s2.getARTI_NM().compareTo(s1.getARTI_NM());
            else
                if (isAsc)
                    return s1.getSONG_NM().compareTo(s2.getSONG_NM());
                else
                    return s2.getSONG_NM().compareTo(s1.getSONG_NM());
        }
    }

    private void getData(int queryType, Object... args) {
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
        if (queryType == Constants.QUERY_POST_DATA) {
            if (args != null && args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
                thread = getPostDataThread((String)args[0], (String)args[1]);
            }
        } else if (queryType == Constants.QUERY_ADD_CABINET) {
            thread = addCabinetMusicThread();
        } else if (queryType == Constants.QUERY_MUSIC_PATH) {
            thread = getMusicPathThread();
        }
        // POST / OST 좋아요 Trigger
        else if (queryType == Constants.QUERY_POST_LIKE) {
            Bundle data = new Bundle();
            Message msg = new Message();
            data.putString("REG_PLAC_TYPE", (String) args[1]);
            data.putString("TOGGLE_YN", (String) args[2]);
            msg.setData(data);
            msg.what = Constants.QUERY_LIKE_RESULT;
            mHandler.sendMessage(msg);

            thread = setPostLikeThread((String) args[0], (String) args[1], (String) args[2]);
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

    public RunnableThread addCabinetMusicThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    ArrayList<AddCabinetMusicItem> arrAddCabinetMusicItem = new ArrayList<>();

                    if ("ALL".equals(putMode)) {
                        for (OstDataItem item : PlayerConstants.SONGS_LIST) {
                            AddCabinetMusicItem addCabinetMusicItem = new AddCabinetMusicItem();
                            addCabinetMusicItem.setSSI(item.getSSI());
                            addCabinetMusicItem.setOTI(item.getOTI());
                            arrAddCabinetMusicItem.add(addCabinetMusicItem);
                        }
                    } else if ("SELECT".equals(putMode)) {
                        for (int i = 0; i < mPlayerAdapter.getCount(); i++) {
                            if (mPlayerAdapter.getItem(i).isChecked()) {
                                AddCabinetMusicItem addCabinetMusicItem = new AddCabinetMusicItem();
                                addCabinetMusicItem.setSSI(mPlayerAdapter.getItem(i).getSSI());
                                addCabinetMusicItem.setOTI(mPlayerAdapter.getItem(i).getOTI());
                                arrAddCabinetMusicItem.add(addCabinetMusicItem);
                            }
                        }
                    } else if ("CURRENT".equals(putMode)) {
                        if (PlayerConstants.SONGS_LIST.size() > 0) {
                            OstDataItem ostDataItem = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
                            AddCabinetMusicItem addCabinetMusicItem = new AddCabinetMusicItem();
                            addCabinetMusicItem.setSSI(ostDataItem.getSSI());
                            addCabinetMusicItem.setOTI(ostDataItem.getOTI());
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

    public RunnableThread getMusicPathThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicPathReq getMusicPathReq = new GetMusicPathReq();
                    getMusicPathReq.setPOI(mPostDataItem.getPOI());
                    mGetMusicPathRes = request.getMusicPath(getMusicPathReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PATH);
                } catch(Exception e) {}
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

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        Bundle data = msg.getData();

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

                if (mPostType.equals(Constants.REQUEST_TYPE_POST)) {
                    lstvStoryType.setText(getString(R.string.post));
                    lstvStoryType.setTextColor(Color.parseColor("#FF00AFD5"));
                } else if (mPostType.equals(Constants.REQUEST_TYPE_RADIO)) {
                    lstvStoryType.setText(getString(R.string.radio));
                    lstvStoryType.setTextColor(Color.parseColor("#FFF65857"));
                } else if (mPostType.equals(Constants.REQUEST_TYPE_TODAY)) {
                    lstvStoryType.setText(getString(R.string.today));
                    lstvStoryType.setTextColor(Color.parseColor("#FFFFCC4F"));
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

                // 좋아요 아이콘 / 횟수
                if ("Y".equals(mPostDataItem.getLIKE_TOGGLE_YN()))
                    ivPostLikeBtn.setImageResource(R.drawable.icon_like_rel);
                else
                    ivPostLikeBtn.setImageResource(R.drawable.icon_like_nor);

                tvPostLikeCount.setText(String.valueOf(mPostDataItem.getLIKE_CNT()));

                if (!SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID).equals(mPostDataItem.getUAI())) {
                    llPostLikeBtn.setOnClickListener(this);
                } else {
                    llPostLikeBtn.setOnClickListener(null);
                }

                // POST RADIO
                mRadioPath = mPostDataItem.getRADIO_PATH();
                int height;
                if (CommonUtil.isNull(mRadioPath)) {
                    radioPlayerLayout.setVisibility(View.GONE);

                    if (mPostType.equals(Constants.REQUEST_TYPE_TODAY))
                        height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 78, mContext.getResources().getDisplayMetrics());
                    else
                        height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 124, mContext.getResources().getDisplayMetrics());
                } else {
                    radioPlayerLayout.setVisibility(View.VISIBLE);
                    height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, mContext.getResources().getDisplayMetrics());
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                postContentLayout.setLayoutParams(lp);
                break;
            // PlayList Header > 담기 후 Handler
            case Constants.QUERY_ADD_CABINET:
                DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_add_cabinet_result));
                break;
            // PlayList Item Delete Handler
            case Constants.QUERY_PLAY_LIST_DELETE:
                mPlayerAdapter.addAllItems(PlayerConstants.SONGS_LIST);
                mPlayerAdapter.notifyDataSetChanged();
                updateInitUI();

                int UPDATE_COUNT = data.getInt("UPDATE_COUNT", 0);
                if (UPDATE_COUNT > 0) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, getString(R.string.msg_ost_delete_play_list, String.valueOf(UPDATE_COUNT)));
                    mPostDialog.show();
                    mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);
                }
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
            // 미디어 파일 다운로드 조회 성공 후 Handler
            case Constants.QUERY_MUSIC_PATH:
                mPlayerState = PLAYING;
                initMediaPlayer();
                startPlay();
                updateUI();
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
            // POST 좋아요 성공 Handler
            case Constants.QUERY_LIKE_RESULT:
                String REG_PLAC_TYPE = data.getString("REG_PLAC_TYPE", "");
                String TOGGLE_YN = "Y".equalsIgnoreCase(data.getString("TOGGLE_YN", "N")) ? "N" : "Y";
                int likeCount = ("Y".equals(TOGGLE_YN)) ? 1 : -1;

                // POST 좋아요 성공 Handler
                if (Constants.REQUEST_REG_PLAC_TYPE_POST.equals(REG_PLAC_TYPE)) {
                    mPostDataItem.setLIKE_TOGGLE_YN(TOGGLE_YN);
                    mPostDataItem.setLIKE_CNT(mPostDataItem.getLIKE_CNT() + likeCount);

                    if ("Y".equals(mPostDataItem.getLIKE_TOGGLE_YN()))
                        ivPostLikeBtn.setImageResource(R.drawable.icon_like_rel);
                    else
                        ivPostLikeBtn.setImageResource(R.drawable.icon_like_nor);

                    ivPostLikeBtn.invalidate();

                    tvPostLikeCount.setText(String.valueOf(mPostDataItem.getLIKE_CNT()));
                    tvPostLikeCount.invalidate();
                }
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

    private void updatePlayerUI() {
        if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ALL)
            ivRepeatBtn.setImageResource(R.drawable.icon_rpt_all);
        else if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ONE)
            ivRepeatBtn.setImageResource(R.drawable.icon_rpt_one);
        else
            ivRepeatBtn.setImageResource(R.drawable.icon_rpt_no);

        if (PlayerConstants.MPS_RANDOM == Constants.MPS_RANDOM_OK)
            ivRandomBtn.setImageResource(R.drawable.icon_rdm_ok);
        else
            ivRandomBtn.setImageResource(R.drawable.icon_rdm_no);

        if (PlayerConstants.SONG_PAUSED)
            ivPlayBtn.setImageResource(R.drawable.bt_player_play);
        else
            ivPlayBtn.setImageResource(R.drawable.bt_player_pause);
    }

    private void updateQualityUI() {
        String streamingQuality = SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_STREAMING_QUALITY, "AAC");
        switch (streamingQuality) {
            case "AAC":
                tvPlayerHeaderQuality.setText(getString(R.string.aac));
                break;
            case "128":
                tvPlayerHeaderQuality.setText(getString(R.string._128k));
                break;
            case "192":
                tvPlayerHeaderQuality.setText(getString(R.string._192k));
                break;
            case "320":
                tvPlayerHeaderQuality.setText(getString(R.string._320k));
                break;
        }
    }

    private boolean isPaused = true;

    @Override
    public void onResume() {
        super.onResume();
        PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
            @Override
            public void handleMessage(Message msg){
                Bundle data = (Bundle)msg.obj;
                OstDataItem ostDataItem = data.getParcelable("OstDataItem");
                int MUSIC_COUNT = data.getInt("MUSIC_COUNT", 0);
                int DURATON = data.getInt("DURATON", 0);
                int POSITION = data.getInt("POSITION", 0);

                if (Constants.RIGHT_COUNT == 0) {
                    llBuyUseCoupon.setVisibility(View.VISIBLE);
                } else {
                    llBuyUseCoupon.setVisibility(View.GONE);
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

                mSeekBar.setMax(DURATON);
                if (!isDrag) mSeekBar.setProgress(POSITION);

                tvPlayingDuration.setText(DateUtil.getConvertMsToFormat(POSITION / 1000));
                tvPlayLeftDuration.setText(DateUtil.getConvertMsToFormat((DURATON - POSITION) / 1000));

                if (isPaused != PlayerConstants.SONG_PAUSED) {
                    isPaused = PlayerConstants.SONG_PAUSED;
                    mPlayerAdapter.notifyDataSetChanged();
                }

                if (ostDataItem != null) {
                    if (!playerSSI.equals(ostDataItem.getPOI() + ostDataItem.getSSI())) {
                        playerSSI = ostDataItem.getPOI() + ostDataItem.getSSI();

                        // Layout Init
                        lstvStoryType.setText("");
                        radioPlayerLayout.setVisibility(View.GONE);
                        ivPlayerAlbumBackground.setVisibility(View.GONE);

                        // 스토리가 없는 음악
                        if (CommonUtil.isNull(ostDataItem.getPOI())) {
                            llEmptyStoryLayout.setVisibility(View.VISIBLE);
                            llEmptyStoryOstLayout.setVisibility(View.VISIBLE);
                            llStoryLayout.setVisibility(View.GONE);
                            llStoryOstLayout.setVisibility(View.GONE);
                            llMoveStory.setVisibility(View.GONE);

                            if (PlayerConstants.SONG_ON_AIR) {
                                Animation animation = new AlphaAnimation(1.0f, 0.2f);
                                animation.setDuration(1000);
                                animation.setRepeatMode(Animation.REVERSE);
                                animation.setRepeatCount(Animation.INFINITE);
                                ivOnAirIcon.startAnimation(animation);
                                ivOnAirIcon.setVisibility(View.VISIBLE);
                            } else {
                                ivOnAirIcon.clearAnimation();
                                ivOnAirIcon.setVisibility(View.GONE);
                            }

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
                            llEmptyStoryLayout.setVisibility(View.GONE);
                            llEmptyStoryOstLayout.setVisibility(View.GONE);
                            llStoryLayout.setVisibility(View.VISIBLE);
                            llStoryOstLayout.setVisibility(View.VISIBLE);

                            if (PlayerConstants.SONG_ON_AIR) {
                                llMoveStory.setVisibility(View.GONE);
                                Animation animation = new AlphaAnimation(1.0f, 0.2f);
                                animation.setDuration(1000);
                                animation.setRepeatMode(Animation.REVERSE);
                                animation.setRepeatCount(Animation.INFINITE);
                                ivOnAirIcon.startAnimation(animation);
                                ivOnAirIcon.setVisibility(View.VISIBLE);
                            } else {
                                llMoveStory.setVisibility(View.VISIBLE);
                                ivOnAirIcon.clearAnimation();
                                ivOnAirIcon.setVisibility(View.GONE);
                            }

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

                        mPlayerAdapter.notifyDataSetChanged();
                    }
                } else {
                    tvSongName.setText(getString(R.string.msg_ost_play_list_empty));
                    updateInitUI();
                }

                updatePlayerUI();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // PlayList Header > 추가 / 담기 > 보관함 이동 후 ActivityResult
            case Constants.QUERY_CABINET_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    String mCabinetType = data.getStringExtra(Constants.REQUEST_CABINET_TYPE);
                    if (CommonUtil.isNotNull(mCabinetType)) {
                        // 담기 후
                        if (Constants.REQUEST_CABINET_TYPE_PUT.equals(mCabinetType)) {
                            mBXI = data.getStringExtra("BXI");
                            if (CommonUtil.isNotNull(mBXI)) {
                                getData(Constants.QUERY_ADD_CABINET);
                            }
                        }
                        // 추가 후
                        else if (Constants.REQUEST_CABINET_TYPE_PLAY.equals(mCabinetType)) {
                            mPlayerAdapter.addAllItems(PlayerConstants.SONGS_LIST);
                            mPlayerAdapter.notifyDataSetChanged();
                            updateInitUI();

                            int UPDATE_COUNT = data.getIntExtra("UPDATE_COUNT", 0);
                            if (UPDATE_COUNT > 0) {
                                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, getString(R.string.msg_ost_add_play_list, UPDATE_COUNT));
                                mPostDialog.show();
                                mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);
                            }
                        }
                    }
                }
                break;
            // 사연 상세 이동 후 ActivityResult
            case Constants.QUERY_POST_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    playerSSI = "";
                }
                break;
            // 재생타이머 이동 후 ActivityResult
            case Constants.QUERY_PLAYER_TIMER:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    if (PlayerConstants.PLAYER_TIMER)
                        mPlayerTimerTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER);
                }
                break;
            // 음질 변경 후 ActivityResult
            case Constants.QUERY_UPDATE_QUALITY:
                updateQualityUI();
                break;
        }
    }

    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;

    private MediaPlayer mPlayer = null;
    private int mPlayerState = PLAY_STOP;
    private SeekBar mPlayProgressBar;
    private void mBtnStartPlayOnClick() {
        if (mPlayerState == PLAY_STOP) {
            getData(Constants.QUERY_MUSIC_PATH);
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_PAUSE;
            pausePlay();
            updateUI();
        } else if (mPlayerState == PLAY_PAUSE) {
            mPlayerState = PLAYING;
            startPlay();
            updateUI();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);

        updateUI();
    }

    private void initMediaPlayer() {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);

        try {
            mPlayer.setDataSource(mGetMusicPathRes.getURL());
            mPlayer.prepare();
            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            tvRadioPlayDuration.setText(maxMinPointStr + maxSecPointStr);

            mPlayProgressBar.setProgress(0);
        }
        catch(Exception e) {
            LogUtil.e("미디어 플레이어 Prepare Error ==========> " + e.getMessage());
        }
    }

    // 재생 시작
    private void startPlay() {
        LogUtil.e("startPlay().....");

        try {
            Controls.pauseControl(mContext);
            mPlayer.start();

            // SeekBar의 상태를 0.1초마다 체크
            mProgressHandler2.sendEmptyMessageDelayed(0, 100);
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }
    }

    private void pausePlay() {
        LogUtil.e("pausePlay().....");

        // 재생을 일시 정지하고
        mPlayer.pause();

        // 재생이 일시정지되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void stopPlay() {
        LogUtil.e("stopPlay().....");

        // 재생을 중지하고
        mPlayer.stop();

        // 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void updateUI() {
        if (mPlayerState == PLAY_STOP) {
            tvRadioPlay.setText(getString(R.string.play));
            ivRadioPlay.setImageResource(R.drawable.bt_rad_play);
            mPlayProgressBar.setProgress(0);
        } else if (mPlayerState == PLAYING) {
            tvRadioPlay.setText(getString(R.string.stop));
            ivRadioPlay.setImageResource(R.drawable.bt_rad_pause);
        } else if (mPlayerState == PLAY_PAUSE) {
            tvRadioPlay.setText(getString(R.string.play));
            ivRadioPlay.setImageResource(R.drawable.bt_rad_play);
        }
    }

    // 재생시 SeekBar 처리
    Handler mProgressHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            if (mPlayer == null) return;

            try {
                if (mPlayer.isPlaying()) {
                    mPlayProgressBar.setProgress(mPlayer.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                }
            } catch (IllegalStateException e) {
            } catch (Exception e) {}
        }
    };
}
