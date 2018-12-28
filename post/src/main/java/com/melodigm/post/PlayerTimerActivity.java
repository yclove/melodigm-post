package com.melodigm.post;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.TextureVideoView;
import com.melodigm.post.widget.wheel.OnWheelScrollListener;
import com.melodigm.post.widget.wheel.WheelView;

import java.util.HashMap;

public class PlayerTimerActivity extends BaseActivity implements IOnHandlerMessage {
    private TextureVideoView mTextureVideoView;
    private WheelView mPlayerTimerGridWheelView, mPlayerTimerHourWheelView, mPlayerTimerMinuteWheelView;
    private PlayerTimerGridAdapter mPlayerTimerGridAdapter;
    private PlayerTimerAdapter mPlayerTimerHourAdapter, mPlayerTimerMinuteAdapter;
    private RelativeLayout rlPlayerTimerToggleBtn;
    private TextView tvPlayerTimerToggleText;
    private int mPlayerTimerTotalSecond = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_timer);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        mPlayerTimerTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER);

        setDisplay();
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_CLOSE, onClickListener);
        setPostHeaderTitle(getString(R.string.player_timer), false);

        mTextureVideoView = (TextureVideoView)findViewById(R.id.mTextureVideoView);
        mTextureVideoView.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);
        mTextureVideoView.setDataSource(mContext, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.swings));
        mTextureVideoView.setLooping(true);
        mTextureVideoView.play();

        mPlayerTimerGridAdapter = new PlayerTimerGridAdapter(mContext);
        mPlayerTimerGridWheelView = (WheelView)findViewById(R.id.mPlayerTimerGridWheelView);
        mPlayerTimerGridWheelView.setViewAdapter(mPlayerTimerGridAdapter);
        mPlayerTimerGridWheelView.setDrawShadows(false);
        mPlayerTimerGridWheelView.setCyclic(true);

        mPlayerTimerHourAdapter = new PlayerTimerAdapter(mContext, "HOUR");
        mPlayerTimerHourWheelView = (WheelView)findViewById(R.id.mPlayerTimerHourWheelView);
        mPlayerTimerHourWheelView.setViewAdapter(mPlayerTimerHourAdapter);
        mPlayerTimerHourWheelView.setDrawShadows(false);
        mPlayerTimerHourWheelView.setCyclic(true);

        mPlayerTimerHourWheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mPlayerTimerGridWheelView.dispatchTouchEvent(event);
                return false;
            }
        });

        mPlayerTimerHourWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {}
            @Override
            public void onScrollingFinished(WheelView wheel) {
                mPlayerTimerHourAdapter.setCurrentItem("HOUR", wheel.getCurrentItem());
                updateTriggerUI();
            }
        });

        mPlayerTimerMinuteAdapter = new PlayerTimerAdapter(mContext, "MINUTE");
        mPlayerTimerMinuteWheelView = (WheelView)findViewById(R.id.mPlayerTimerMinuteWheelView);
        mPlayerTimerMinuteWheelView.setViewAdapter(mPlayerTimerMinuteAdapter);
        mPlayerTimerMinuteWheelView.setDrawShadows(false);
        mPlayerTimerMinuteWheelView.setCyclic(true);

        mPlayerTimerMinuteWheelView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mPlayerTimerGridWheelView.dispatchTouchEvent(event);
                return false;
            }
        });

        mPlayerTimerMinuteWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {}
            @Override
            public void onScrollingFinished(WheelView wheel) {
                mPlayerTimerMinuteAdapter.setCurrentItem("MINUTE", wheel.getCurrentItem());
                updateTriggerUI();
            }
        });

        // Footer 시작 / 정지 버튼
        rlPlayerTimerToggleBtn = (RelativeLayout)findViewById(R.id.rlPlayerTimerToggleBtn);
        rlPlayerTimerToggleBtn.setOnClickListener(onClickListener);

        // Footer 시작 / 정지 문구
        tvPlayerTimerToggleText = (TextView)findViewById(R.id.tvPlayerTimerToggleText);

        // 타이머가 실행중일 경우
        if (PlayerConstants.PLAYER_TIMER) {
            rlPlayerTimerToggleBtn.setBackgroundColor(Color.parseColor("#E6D73D66"));
            tvPlayerTimerToggleText.setText(getString(R.string.stop_kr));

            long playingTimeSecond = System.currentTimeMillis() / 1000 - PlayerConstants.PLAYER_TIMER_START_TIME;
            long lostPlayingTimeSecond = mPlayerTimerTotalSecond - playingTimeSecond + 60;

            int playerTimerHour = (int)lostPlayingTimeSecond / 3600;
            int playerTimerMinute = (int)(lostPlayingTimeSecond % 3600) / 60;
            mPlayerTimerHourWheelView.setCurrentItem(playerTimerHour);
            mPlayerTimerHourAdapter.setCurrentItem("HOUR", mPlayerTimerHourWheelView.getCurrentItem());
            mPlayerTimerMinuteWheelView.setCurrentItem(playerTimerMinute);
            mPlayerTimerMinuteAdapter.setCurrentItem("MINUTE", mPlayerTimerMinuteWheelView.getCurrentItem());

            mPlayerTimerGridWheelView.setEnabled(false);
            mPlayerTimerHourWheelView.setEnabled(false);
            mPlayerTimerMinuteWheelView.setEnabled(false);
        }
        // 타이머가 실행중이 아닐 경우
        else {
            rlPlayerTimerToggleBtn.setBackgroundColor(Color.parseColor("#E600AFD5"));
            tvPlayerTimerToggleText.setText(getString(R.string.start_kr));

            int playerTimerHour = mPlayerTimerTotalSecond / 3600;
            int playerTimerMinute = (mPlayerTimerTotalSecond % 3600) / 60;
            mPlayerTimerHourWheelView.setCurrentItem(playerTimerHour);
            mPlayerTimerHourAdapter.setCurrentItem("HOUR", mPlayerTimerHourWheelView.getCurrentItem());
            mPlayerTimerMinuteWheelView.setCurrentItem(playerTimerMinute);
            mPlayerTimerMinuteAdapter.setCurrentItem("MINUTE", mPlayerTimerMinuteWheelView.getCurrentItem());
        }

        updateTriggerUI();
    }

    private void updateTriggerUI() {
        // 타이머가 실행중이 아닐 경우
        if (!PlayerConstants.PLAYER_TIMER) {
            if (mPlayerTimerHourWheelView.getCurrentItem() == 0 && mPlayerTimerMinuteWheelView.getCurrentItem() == 0) {
                rlPlayerTimerToggleBtn.setAlpha(0.2f);
                rlPlayerTimerToggleBtn.setOnClickListener(null);
            } else {
                rlPlayerTimerToggleBtn.setAlpha(1.0f);
                rlPlayerTimerToggleBtn.setOnClickListener(onClickListener);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 닫기 onClick
                case R.id.btnHeaderClose:
                    finish();
                    break;
                // Footer 시작 / 정지 버튼 onClick
                case R.id.rlPlayerTimerToggleBtn:
                    // 타이머가 실행중일 경우
                    if (PlayerConstants.PLAYER_TIMER) {
                        PlayerConstants.PLAYER_TIMER = false;
                    }
                    // 타이머가 실행중이 아닐 경우
                    else {
                        PlayerConstants.PLAYER_TIMER = true;
                        PlayerConstants.PLAYER_TIMER_START_TIME = System.currentTimeMillis() / 1000;
                        int hourPosition = mPlayerTimerHourWheelView.getCurrentItem();
                        int minutePosition = mPlayerTimerMinuteWheelView.getCurrentItem();
                        int totalSecond = (hourPosition * 3600) + (minutePosition * 60);
                        SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_TIMER, totalSecond);
                    }
                    setResult(Constants.RESULT_SUCCESS);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch (msg.what) {
            case Constants.QUERY_APP_VERSION:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTextureVideoView != null) {
            mTextureVideoView.stop();
            mTextureVideoView = null;
        }
    }
}
