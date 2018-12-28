package com.melodigm.post.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.melodigm.post.BaseApplication;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetMusicPathReq;
import com.melodigm.post.protocol.data.GetMusicPathRes;
import com.melodigm.post.protocol.data.GetMusicPaymentReq;
import com.melodigm.post.protocol.data.GetMusicSecurityReq;
import com.melodigm.post.protocol.data.GetMusicSecurityRes;
import com.melodigm.post.protocol.data.GetPostPositionDataReq;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.receiver.LockScreenReceiver;
import com.melodigm.post.receiver.MediaButtonReceiver;
import com.melodigm.post.util.CastUtil;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * YCNOTE - Service 주기
 * onCreate() -> onStartCommand() 순으로 이뤄 지는데요.
 * Service가 실행되고 있는 도중에 다시한번  startService()  메서드가 호출되면 onStartCommand() 주기 부터 실행하게 됩니다.
 * 마치 Activity의 onResume() 처럼 말이죠.
 * 그렇기 때문에 중요한 작업에 대해서는 onCreate() 보다는 onStartCommand() 메서드에 구현을 해줘야 합니다.
 */
public class MusicService extends Service implements IOnHandlerMessage, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private Context mContext;
    private GetMusicPathRes mGetMusicPathRes;
    private GetMusicSecurityRes mGetMusicSecurityRes;
    private PostDatabases mdbHelper;
    private MediaPlayer mMediaPlayer;
    private String[] dataSourceInfo = {"", ""};
    private int downloadingPercent = 0;
    private boolean isInitialized = false;
    private Timer mTimer;
    private MainTask mMainTask;
    private boolean isPlayerTimer = false;
    private int mPlayerTimerTotalSecond = 0;

    private OstDataItem mOstDataItem;
    private final Handler localHandler = new Handler();
    private WeakRefHandler mHandler;
    private HashMap<Integer, RunnableThread> mThreads = null;

    private Tracker mTracker;
    private boolean isShowMobileNetworkDialog = false;
    private boolean isShowLocaleDialog = false;
    private String mFormat;

    private LockScreenReceiver mLockScreenReceiver = null;
    private MediaButtonReceiver mMediaButtonReceiver = null;

    private boolean isPayment = false;

    /**
     * 다른 컴포넌트가 bindService()를 호출해서 서비스와 연결을 시도하면 이 메소드가 호출됩니다.
     * 이 메소드에서 IBinder를 반환해서 서비스와 컴포넌트가 통신하는데 사용하는 인터페이스를 제공해야 합니다.
     * 만약 시작 타입의 서비스를 구현한다면 null을 반환하면 됩니다.
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * 서비스가 처음으로 생성되면 호출됩니다.
     * 이 메소드 안에서 초기의 설정 작업을 하면되고 서비스가 이미 실행중이면 이 메소드는 호출되지 않습니다.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        mLockScreenReceiver = new LockScreenReceiver();
        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mLockScreenReceiver, screenOffFilter);

        mMediaButtonReceiver = new MediaButtonReceiver();
        IntentFilter mediaButtonFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        // 기본적으로 이이폰 버튼은 기본 미디어플레이어가 우선권을 갖고있다고한다. 그래서 기본 미디어 플레이어보다 높은 우선순위를 지정해 준다.
        mediaButtonFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY + 1);
        registerReceiver(mMediaButtonReceiver, mediaButtonFilter);

        ComponentName componentName = new ComponentName(this, MediaButtonReceiver.class);
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.registerMediaButtonEventReceiver(componentName);

        PlayerConstants.SONG_NUMBER = SPUtil.getIntSharedPreference(mContext, Constants.SP_MPS_POSITION);
        PlayerConstants.MPS_REPEAT = SPUtil.getIntSharedPreference(mContext, Constants.SP_MPS_REPEAT);
        PlayerConstants.MPS_RANDOM = SPUtil.getIntSharedPreference(mContext, Constants.SP_MPS_RANDOM);

        LogUtil.e("♬ MusicService 생성 ▶ SONG_NUMBER(" + PlayerConstants.SONG_NUMBER + "), MPS_REPEAT(" + PlayerConstants.MPS_REPEAT + "), MPS_RANDOM(" + PlayerConstants.MPS_RANDOM + ")");

        startTimer();
    }

    @Override
    public void onPrepared(final MediaPlayer player) {
        LogUtil.e("♬ MusicService ▶ onPrepared");

        // stop을 호출하게 되면 노래의 재생은  Stopped 상태가 되며 완전히 종료 되게 된다. Stopped 상태에서는 다시 start()를 실행 할수 없다. 이때는 다시 prepare 를 시킨후 start를 실행 하여야 될것이다.
        player.start();
        isInitialized = true;
        isPayment = false;

        String path = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath();
        DeviceUtil.removeFiles(path + "/", ".decrypt");

        String ssi, playCycleType, oti, poi;
        int playElapTime, playSeekingTime;
        if (PlayerConstants.SONG_ON_AIR) {
            if (mOstDataItem != null) {
                ssi = mOstDataItem.getSSI();
                playCycleType = Constants.REQUEST_PLAY_CYCLE_TYPE_START;
                playElapTime = 0;
                playSeekingTime = 0;
                oti = mOstDataItem.getOTI();
                poi = mOstDataItem.getPOI();
                getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti, poi);
            }
        } else {
            if (PlayerConstants.SONGS_LIST.size() > PlayerConstants.SONG_NUMBER) {
                ssi = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI();
                playCycleType = Constants.REQUEST_PLAY_CYCLE_TYPE_START;
                playElapTime = 0;
                playSeekingTime = 0;
                oti = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getOTI();
                poi = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI();
                getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti, poi);
            }
        }

        if (!PlayerConstants.SONG_RADIO) {
            if (mdbHelper == null) mdbHelper = new PostDatabases(mContext);
            mdbHelper.open();

            if (PlayerConstants.SONG_ON_AIR) {
                mdbHelper.updateOstPlayHistory(mOstDataItem);
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.GA_CATEGORY_MUSIC)
                        .setAction(Constants.GA_ACTION_MUSIC_LISTENING)
                        .setLabel(mOstDataItem.getSONG_NM() + " - " + mOstDataItem.getARTI_NM())
                        .build());
            } else {
                mdbHelper.updateOstPlayHistory(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER));
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.GA_CATEGORY_MUSIC)
                        .setAction(Constants.GA_ACTION_MUSIC_LISTENING)
                        .setLabel(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSONG_NM() + " - " + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getARTI_NM())
                        .build());
            }

            mdbHelper.close();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtil.e(String.format(Locale.US, "♬ MusicService ▶ onError : what(%d), extra(%d)", what, extra));
        clearRadioOnAir();
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtil.e("♬ MusicService ▶ onCompletion");

        String ssi = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI();
        String playCycleType = Constants.REQUEST_PLAY_CYCLE_TYPE_END;
        Integer playElapTime = mediaPlayer.getDuration() / 1000;
        Integer playSeekingTime = 0;
        String oti = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getOTI();
        String poi = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI();
        getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti, poi);

        if (!PlayerConstants.SONG_RADIO) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.GA_CATEGORY_MUSIC)
                    .setAction(Constants.GA_ACTION_MUSIC_COMPLETION)
                    .setLabel(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSONG_NM() + " - " + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getARTI_NM())
                    .build());
        }

        if (PlayerConstants.SONG_RADIO) {
            LogUtil.e("♬ MusicService ▶ 라디오 타이틀 곡을 재생합니다.");
            Controls.nextControl(getApplicationContext());
        } else {
            if (PlayerConstants.MPS_RANDOM == Constants.MPS_RANDOM_OK) {
                LogUtil.e("♬ MusicService ▶ 랜덤곡을 재생합니다.");
                Controls.nextControl(getApplicationContext());
            } else {
                if (isExistsMusicData(PlayerConstants.SONG_NUMBER + 1)) {
                    LogUtil.e("♬ MusicService ▶ 다음곡을 재생합니다.");
                    Controls.nextControl(getApplicationContext());
                } else {
                    if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ALL) {
                        LogUtil.e("♬ MusicService ▶ 전곡 반복 재생으로 다시 재생합니다.");
                        Controls.nextControl(getApplicationContext());
                    } else {
                        LogUtil.e("♬ MusicService ▶ 재생이 모두 끝났습니다.");
                        clearRadioOnAir();
                    }
                }
            }
        }
    }

    private MediaPlayer.OnCompletionListener mOnAirCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            LogUtil.e("♬ MusicService ▶ mOnAirCompletionListener > onCompletion");

            if (mOstDataItem != null) {
                String ssi = mOstDataItem.getSSI();
                String playCycleType = Constants.REQUEST_PLAY_CYCLE_TYPE_END;
                Integer playElapTime = 0;
                Integer playSeekingTime = mediaPlayer.getDuration() / 1000;
                String oti = mOstDataItem.getOTI();
                String poi = mOstDataItem.getPOI();
                getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti, poi);
            }

            if (!PlayerConstants.SONG_RADIO) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.GA_CATEGORY_MUSIC)
                        .setAction(Constants.GA_ACTION_MUSIC_COMPLETION)
                        .setLabel(mOstDataItem.getSONG_NM() + " - " + mOstDataItem.getARTI_NM())
                        .build());
            }

            if (PlayerConstants.SONG_RADIO) {
                LogUtil.e("♬ MusicService ▶ ON AIR 라디오 타이틀 곡을 재생합니다.");
                if (requestFocus()) {
                    playMusic();
                } else {
                    Controls.pauseControl(mContext);
                }
            } else {
                getData(Constants.QUERY_POST_DATA, "AF");
            }
        }
    };

    private CacheListener mCacheListener = new CacheListener() {
        @Override
        public void onCacheAvailable(final File file, String url, int percentsAvailable) {
            LogUtil.e(String.format(Locale.US, "onCacheAvailable : (%03d%%) %s(%s), url(%s)", percentsAvailable, file.getName(), CastUtil.getFileSize(file.length()), url));
            downloadingPercent = percentsAvailable;

            if (percentsAvailable > 100 || percentsAvailable < 0) {
                LogUtil.e("♬ MusicService ▶ 비정상적인 다운로드로 인하여 Listener 해지 및 파일 삭제 : " + url);
                BaseApplication.getProxy(mContext).unregisterCacheListener(mCacheListener, url);
                boolean isDelete = file.delete();
                if (!isDelete) LogUtil.e("♬ MusicService ▶ 비정상적인 다운로드로 인하여 파일 삭제 중 실패하였습니다.");
            } else if (percentsAvailable == 100) {
                Runnable delayEncrypt = new Runnable() {
                    public void run() {
                        boolean isPlayFileSave = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_FILE_SAVE, true);
                        if (isPlayFileSave) {
                            getData(Constants.QUERY_MUSIC_SECURITY, file.getAbsolutePath().replaceAll(".download", ""), file.getAbsolutePath().replaceAll(file.getName(), dataSourceInfo[1]));
                        } else {
                            LogUtil.e("♬ MusicService ▶ 임시저장 사용 설정이 비활성되어 원본파일을 삭제합니다.");
                            File oriFile = new File(file.getAbsolutePath().replaceAll(".download", ""));
                            boolean isDelete = oriFile.delete();
                            if (!isDelete) LogUtil.e("♬ MusicService ▶ 임시저장 사용 설정이 비활성되어 원본파일 삭제 중 실패하였습니다.");
                        }
                    }
                };
                localHandler.postDelayed(delayEncrypt, 1000);
            }
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogUtil.e(String.format(Locale.US, "♬ MusicService ▶ onAudioFocusChange : %d", focusChange));

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: // 1
                LogUtil.e("AudioManager.AUDIOFOCUS_GAIN");

                if (mMediaPlayer != null && isInitialized) {
                    mMediaPlayer.setVolume(1.0f, 1.0f);

                    if (!PlayerConstants.SONG_PAUSED) {
                        mMediaPlayer.start();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS: // -1
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS");

                if (mMediaPlayer != null && isInitialized && !PlayerConstants.SONG_PAUSED) {
                    Controls.pauseControl(mContext);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // -2
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");

                if (mMediaPlayer != null && isInitialized && mMediaPlayer.isPlaying()) {
                    Controls.pauseControl(mContext);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // -3
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                if (mMediaPlayer != null && isInitialized) {
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private boolean requestFocus() {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void startTimer() {
        LogUtil.e("♬ MusicService ▶ 타이머 생성");

        if (mTimer == null) mTimer = new Timer();
        if (mMainTask == null) mMainTask = new MainTask();
        mTimer.scheduleAtFixedRate(mMainTask, 0, 1000);
    }

    private void stopTimer() {
        LogUtil.e("♬ MusicService ▶ 타이머 제거");

        if(mMainTask != null) {
            mMainTask.cancel(); //타이머 task를 timer 큐에서 지워버린다.
            mMainTask = null;
        }

        if(mTimer != null) {
            // 스케쥴 Task와 타이머를 취소한다.
            mTimer.cancel();
            // Task큐의 모든 Task를 제거한다.
            mTimer.purge();
            mTimer = null;
        }
    }

    private class MainTask extends TimerTask{
        public void run(){
            handler.sendEmptyMessage(0);
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            OstDataItem ostDataItem = null;

            if (PlayerConstants.SONG_ON_AIR) {
                ostDataItem = mOstDataItem;
            } else {
                if (PlayerConstants.SONGS_LIST.size() != 0) {
                    if (PlayerConstants.SONGS_LIST.size() <= PlayerConstants.SONG_NUMBER)  PlayerConstants.SONG_NUMBER = 0;
                    ostDataItem = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
                }
            }

            int duration, position;
            if (!isInitialized) {
                duration = 0;
                position = 0;
            } else {
                if (Constants.RIGHT_COUNT > 0 || isPayment) {
                    duration = mMediaPlayer.getDuration();
                } else {
                    duration = 30000;
                }
                position = mMediaPlayer.getCurrentPosition();

                // 30초 과금통신
                //if (!isPayment && position >= 30000 && position < 31000) {
                if (!isPayment && position >= 30000) {
                    isPayment = true;
                    String ssi = ostDataItem.getSSI();
                    String playCycleType = Constants.REQUEST_PLAY_CYCLE_TYPE_PAYMENT;
                    Integer playElapTime = 0;
                    Integer playSeekingTime = 30;
                    String oti = ostDataItem.getOTI();
                    String poi = ostDataItem.getPOI();
                    getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti, poi);
                }
            }

            Bundle data = new Bundle();
            data.putParcelable("OstDataItem", ostDataItem);
            data.putInt("MUSIC_COUNT", PlayerConstants.SONGS_LIST.size());
            data.putInt("DURATON", duration);
            data.putInt("POSITION", position);

            if (isShowMobileNetworkDialog) {
                data.putBoolean("isShowMobileNetworkDialog", isShowMobileNetworkDialog);
                isShowMobileNetworkDialog = false;
            }

            if (isShowLocaleDialog) {
                data.putBoolean("isShowLocaleDialog", isShowLocaleDialog);
                isShowLocaleDialog = false;
            }

            if (isPlayerTimer != PlayerConstants.PLAYER_TIMER) {
                if (PlayerConstants.PLAYER_TIMER) {
                    mPlayerTimerTotalSecond = SPUtil.getIntSharedPreference(mContext, Constants.SP_PLAYER_TIMER);
                }
                isPlayerTimer = PlayerConstants.PLAYER_TIMER;
            }

            if (PlayerConstants.PLAYER_TIMER) {
                long currentTimeSec = System.currentTimeMillis() / 1000;
                long playingTimeSecond = currentTimeSec - PlayerConstants.PLAYER_TIMER_START_TIME;
                long lostPlayingTimeSecond = mPlayerTimerTotalSecond - playingTimeSecond;
                if (lostPlayingTimeSecond <= 0) {
                    PlayerConstants.PLAYER_TIMER = false;
                    Controls.pauseControl(mContext);
                }
            }

            try{
                PlayerConstants.PROGRESSBAR_HANDLER.sendMessage(PlayerConstants.PROGRESSBAR_HANDLER.obtainMessage(0, data));
            }catch(Exception e){}
        }
    };

    /**
     * YCNOTE - Service onStartCommand
     * 다른 컴포넌트가 startService()를 호출해서 서비스가 시작되면 이 메소드가 호출됩니다. 만약 연결된 타입의 서비스를 구현한다면 이 메소드는 재정의 할 필요가 없습니다.
     * onStartCommand() 메서드는 3가지 리턴 타입을 갖게 되는데요.
     * START_STICKY
     * Service가 강제 종료되었을 경우 시스템이 다시 Service를 재시작 시켜 주지만 intent 값을 null로 초기화 시켜서 재시작 합니다.
     * Service 실행시 startService(Intent service) 메서드를 호출 하는데 onStartCommand(Intent intent, int flags, int startId) 메서드에 intent로 value를 넘겨 줄 수 있습니다.
     * 기존에 intent에 value값이 설정이 되있다고 하더라도 Service 재시작시 intent 값이 null로 초기화 되서 재시작 됩니다.
     *
     * START_NOT_STICKY
     * 이 Flag를 리턴해 주시면, 강제로 종료 된 Service가 재시작 하지 않습니다. 시스템에 의해 강제 종료되어도 괸찮은 작업을 진행 할 때 사용해 주시면 됩니다.
     *
     * START_REDELIVER_INTENT
     * START_STICKY와 마찬가지로 Service가 종료 되었을 경우 시스템이 다시 Service를 재시작 시켜 주지만 intent 값을 그대로 유지 시켜 줍니다.
     * startService() 메서드 호출시 Intent value값을 사용한 경우라면 해당 Flag를 사용해서 리턴값을 설정해 주면 됩니다.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.e("♬ MusicService ▶ onStartCommand");
        try {
            if (PlayerConstants.SONGS_LIST.size() <= 0)
                setPlayList(-1);

            PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String)msg.obj;
                    if (PlayerConstants.SONG_ON_AIR) {
                        if("next".equalsIgnoreCase(message)){
                            if (PlayerConstants.SONG_RADIO) {
                                if (requestFocus()) {
                                    playMusic();
                                } else {
                                    Controls.pauseControl(mContext);
                                }
                            } else {
                                getData(Constants.QUERY_POST_DATA, "AF");
                            }
                        } else {
                            getData(Constants.QUERY_POST_DATA, "BF");
                        }
                    } else {
                        try {
                            if (requestFocus()) {
                                playMusic();
                            } else {
                                Controls.pauseControl(mContext);
                            }
                        } catch(Exception e) {
                            LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                            clearRadioOnAir();
                        }
                    }

                    return false;
                }
            });

            PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String)msg.obj;
                    if (message.equalsIgnoreCase(getResources().getString(R.string.play))) {
                        // 플레이어가 아직 준비가 안되어 있을 경우
                        if (!isInitialized) {
                            if (isExistsMusicData(PlayerConstants.SONG_NUMBER)) {
                                PlayerConstants.SONG_PAUSED = false;
                                if (requestFocus()) {
                                    playMusic();
                                } else {
                                    Controls.pauseControl(mContext);
                                }
                            } else
                                DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                        } else {
                            PlayerConstants.SONG_PAUSED = false;
                            mMediaPlayer.start();
                        }
                    }else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
                        PlayerConstants.SONG_PAUSED = true;
                        if (isInitialized) mMediaPlayer.pause();
                    }
                    return false;
                }
            });

            if (intent != null) {
                Bundle data = intent.getExtras();
                String MPS_COMMAND = data.getString(Constants.MPS_COMMAND);
                // 플레이어 재생 위치값을 얻기위한 전달 값
                String POI = data.getString("POI");
                String SSI = data.getString("SSI");
                int SEEKBAR_POSITION = data.getInt("SEEKBAR_POSITION");

                LogUtil.e("♬ MusicService ▶ onStartCommand : MPS_COMMAND(" + MPS_COMMAND + ")");

                switch(MPS_COMMAND) {
                    case Constants.MPS_COMMAND_GET:
                        /*LogUtil.m(mContext);
                        LogUtil.file(getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE));*/
                        handler.sendEmptyMessage(0);
                        break;
                    case Constants.MPS_COMMAND_SET:
                        if (isInitialized) {
                            double _total = Double.valueOf(String.valueOf(mMediaPlayer.getDuration()));
                            double _size = Double.valueOf(String.valueOf(SEEKBAR_POSITION));
                            int _percent = (int)(_size / _total * 100.0);
                            LogUtil.e("♬ MusicService ▶ 다운로드(" + downloadingPercent + "%), 이동시간(" + _percent + "%)");
                            if (downloadingPercent == 100 || downloadingPercent > _percent + 5) {
                                if (Constants.RIGHT_COUNT > 0 || SEEKBAR_POSITION < 30000) {
                                    mMediaPlayer.seekTo(SEEKBAR_POSITION);
                                    LogUtil.e("♬ MusicService ▶ 이동시간 : " + DateUtil.getConvertMsToFormat(SEEKBAR_POSITION / 1000));
                                }
                            }
                        }
                        break;
                    case Constants.MPS_COMMAND_PUT:
                        if (mdbHelper == null)
                            mdbHelper = new PostDatabases(this);

                        mdbHelper.open();
                        PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
                        mdbHelper.close();
                        break;
                    case Constants.MPS_COMMAND_ADD:
                        if (CommonUtil.isNotNull(POI) && CommonUtil.isNotNull(SSI))
                            setPlayList(POI, SSI);
                        else
                            setPlayList(0);

                        if (isExistsMusicData(PlayerConstants.SONG_NUMBER)) {
                            PlayerConstants.SONG_PAUSED = false;
                            if (requestFocus()) {
                                playMusic();
                            } else {
                                Controls.pauseControl(mContext);
                            }
                        } else
                            DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                        break;
                    case Constants.MPS_COMMAND_PLAY:
                        if (mMediaPlayer != null) {
                            if (!mMediaPlayer.isPlaying()) {
                                if (!isInitialized) {
                                    Controls.playControl(mContext);
                                } else
                                    mMediaPlayer.start();
                            }
                        } else {
                            Controls.playControl(mContext);
                        }
                        break;
                    case Constants.MPS_COMMAND_ON_AIR:
                        PlayerConstants.SONG_PAUSED = false;
                        getData(Constants.QUERY_POST_DATA, "MF");
                        break;
                    case Constants.MPS_COMMAND_ON_AIR_CLEAR:
                        clearRadioOnAir();
                        break;
                }
            }
        } catch (Exception e) {
            LogUtil.e("♬ MusicService ▶ " + e.getMessage());
            clearRadioOnAir();
        }

        return START_NOT_STICKY;
    }

    private void clearRadioOnAir() {
        PlayerConstants.SONG_PAUSED = true;
        PlayerConstants.SONG_ON_AIR = false;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            isInitialized = false;
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 서비스가 소멸되는 도중에 이 메소드가 호출되며 주로 Thread, Listener, BroadcastReceiver와 같은 자원들을 정리하는데 사용하면 됩니다.
     * TaskKiller에 의해 서비스가 강제종료될 경우에는 이 메소드가 호출되지 않습니다
     */
    @Override
    public void onDestroy() {
        LogUtil.e("♬ MusicService ▶ onDestroy");
        super.onDestroy();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        stopTimer();
        PlayerConstants.SONG_PAUSED = true;
        BaseApplication.getProxy(mContext).unregisterCacheListener(mCacheListener);

        unregisterReceiver(mLockScreenReceiver);
        unregisterReceiver(mMediaButtonReceiver);
    }

    private void playMusic() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            isInitialized = false;
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // 최초 생성된 MediaPlayer의 상태는 Idle 상태이다.
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);

        // 해외 접속 차단 확인
        if (checkLocale()) {
            mFormat = SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_STREAMING_QUALITY, "AAC");

            if (PlayerConstants.SONG_ON_AIR)
                mMediaPlayer.setOnCompletionListener(mOnAirCompletionListener);
            else
                mMediaPlayer.setOnCompletionListener(this);

            if (PlayerConstants.SONG_ON_AIR) {
                if ("".equals(mOstDataItem.getRADIO_PATH())) {
                    PlayerConstants.SONG_RADIO = false;
                } else {
                    PlayerConstants.SONG_RADIO = !PlayerConstants.SONG_RADIO;
                }

                // ON AIR 재생 파일이 존재할 경우
                if (isOnAirExistsFile()) {
                    String mDataSource;
                    if (PlayerConstants.SONG_RADIO)
                        // 라디오 데이터 소스
                        mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + mOstDataItem.getPOI() + ".encrypt";
                    else
                        // 음악 데이터 소스
                        mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + mOstDataItem.getSSI() + mFormat + ".encrypt";

                    LogUtil.e("♬ MusicService ▶ 파일이 존재하여 재활용 진행합니다.");
                    getData(Constants.QUERY_MUSIC_SECURITY, mDataSource);
                }
                // ON AIR 재생 파일이 존재하지 않을 경우
                else {
                    if (CommonUtil.isNotNull(dataSourceInfo[0])) {
                        LogUtil.e("♬ MusicService ▶ 재생곡 변경으로 기존 Listener 해지 및 파일 삭제");
                        BaseApplication.getProxy(mContext).unregisterCacheListener(mCacheListener);
                        String path = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath();
                        DeviceUtil.removeFiles(path + "/", ".download");
                    }

                    checkMobileNetwork();
                }
            } else {
                if ("".equals(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH())) {
                    PlayerConstants.SONG_RADIO = false;
                } else {
                    PlayerConstants.SONG_RADIO = !PlayerConstants.SONG_RADIO;
                }

                // 재생목록 파일이 존재할 경우
                if (isExistsFile(PlayerConstants.SONG_NUMBER)) {
                    String mDataSource;
                    if (PlayerConstants.SONG_RADIO)
                        // 라디오 데이터 소스
                        mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI() + ".encrypt";
                    else
                        // 음악 데이터 소스
                        mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI() + mFormat + ".encrypt";

                    LogUtil.e("♬ MusicService ▶ 파일이 존재하여 재활용 진행합니다.");
                    getData(Constants.QUERY_MUSIC_SECURITY, mDataSource);
                }
                // 재생목록 파일이 존재하지 않을 경우
                else {
                    if (CommonUtil.isNotNull(dataSourceInfo[0])) {
                        LogUtil.e("♬ MusicService ▶ 재생곡 변경으로 기존 Listener 해지 및 파일 삭제");
                        BaseApplication.getProxy(mContext).unregisterCacheListener(mCacheListener);
                        String path = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath();
                        DeviceUtil.removeFiles(path + "/", ".download");
                    }

                    checkMobileNetwork();
                }
            }
        }
    }

    private void checkMobileNetwork() {
        // 데이터 네트워크 사용을 거부하고 현재 모바일 내트워크 접속 중일 경우
        if (!SPUtil.getBooleanSharedPreference(mContext, Constants.SP_USE_DATA_NETWORK) && PlayerConstants.MOBILE_NETWORK) {
            boolean isNotificationService = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE);
            String notificationSound = (isNotificationService) ? "00.caf" : "01.caf";

            // 앱이 실행중일 경우
            if (DeviceUtil.isForegroundActivity(mContext, Constants.SERVICE_PACKAGE)) {
                isShowMobileNetworkDialog = true;
            }
            // 앱이 실행중이지 않을 경우
            else {
                Bundle data = new Bundle();
                data.putString("action", "YC01");
                data.putString("uai", SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID));
                data.putString("ticker", "데이터 네트워크 차단 상태입니다");
                data.putString("title", "POST");
                data.putString("message", "데이터 네트워크 차단 상태입니다");
                data.putString("sound", notificationSound);
                DeviceUtil.sendNotification(mContext, data);
            }
            clearRadioOnAir();
        } else {
            getData(Constants.QUERY_MUSIC_PATH);
        }
    }

    private boolean checkLocale() {
        if (!"KR".equalsIgnoreCase(SPUtil.getSharedPreference(mContext, Constants.SP_COUNTRY_CODE))) {
            boolean isNotificationService = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE);
            String notificationSound = (isNotificationService) ? "00.caf" : "01.caf";

            // 앱이 실행중일 경우
            if (DeviceUtil.isForegroundActivity(mContext, Constants.SERVICE_PACKAGE)) {
                isShowLocaleDialog = true;
            }
            // 앱이 실행중이지 않을 경우
            else {
                Bundle data = new Bundle();
                data.putString("action", "YC02");
                data.putString("uai", SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID));
                data.putString("ticker", "해외 서비스 이용 제한");
                data.putString("title", "POST");
                data.putString("message", "해외 서비스 이용 제한");
                data.putString("sound", notificationSound);
                DeviceUtil.sendNotification(mContext, data);
            }
            clearRadioOnAir();

            return false;
        }
        return true;
    }

    private void setPlayList(Object... args) {
        if (mdbHelper == null)
            mdbHelper = new PostDatabases(this);

        mdbHelper.open();
        PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
        mdbHelper.close();

        if (args != null && args.length > 0 && args[0] instanceof Integer) {
            // 최초 목록 생성 시점이 아닐 경우 제일 마지막으로 포지션을 위치시킨다.
            if ((int)args[0] != -1) {
                PlayerConstants.SONG_NUMBER = PlayerConstants.SONGS_LIST.size() - 1;
                SPUtil.setSharedPreference(mContext, Constants.SP_MPS_POSITION, PlayerConstants.SONG_NUMBER);
            }
        } else if (args != null && args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
            for (int rowNum = 0; rowNum < PlayerConstants.SONGS_LIST.size(); rowNum++) {
                if ( PlayerConstants.SONGS_LIST.get(rowNum).getPOI().equals(args[0]) && PlayerConstants.SONGS_LIST.get(rowNum).getSSI().equals(args[1]) ) {
                    PlayerConstants.SONG_NUMBER = rowNum;
                    SPUtil.setSharedPreference(mContext, Constants.SP_MPS_POSITION, PlayerConstants.SONG_NUMBER);
                    LogUtil.e("♬ MusicService ▶ 위치 : " + PlayerConstants.SONG_NUMBER);
                    break;
                }
            }
        }
    }

    private boolean isExistsMusicData(int position) {
        if (PlayerConstants.SONGS_LIST == null || position < 0) return false;
        if (PlayerConstants.SONGS_LIST.size() <= position) return false;
        return true;
    }

    private boolean isExistsFile(int position) {
        boolean isExists = false;
        boolean isPlayFileSave = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_FILE_SAVE, true);

        if (PlayerConstants.SONG_RADIO)
            LogUtil.e("♬ MusicService ▶ " + PlayerConstants.SONGS_LIST.get(position).getPOI() + ".encrypt 라디오 파일을 찾습니다.");
        else
            LogUtil.e("♬ MusicService ▶ " + PlayerConstants.SONGS_LIST.get(position).getSSI() + mFormat + ".encrypt 음악 파일을 찾습니다.");

        if (!isPlayFileSave) {
            LogUtil.e("♬ MusicService ▶ 임시저장 사용 설정이 비활성되어 스트리밍 서비스로 전환됩니다.");
            return isExists;
        }

        String[] musicFileList = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).list();
        for (int i = 0; i < musicFileList.length; i++) {
            if (PlayerConstants.SONG_RADIO) {
                if ((PlayerConstants.SONGS_LIST.get(position).getPOI() + ".encrypt").equalsIgnoreCase(musicFileList[i])) {
                    isExists = true;
                    break;
                }
            } else {
                if ((PlayerConstants.SONGS_LIST.get(position).getSSI() + mFormat + ".encrypt").equalsIgnoreCase(musicFileList[i])) {
                    isExists = true;
                    break;
                }
            }
        }
        return isExists;
    }

    private boolean isOnAirExistsFile() {
        boolean isExists = false;
        boolean isPlayFileSave = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_FILE_SAVE, true);

        if (PlayerConstants.SONG_RADIO)
            LogUtil.e("♬ MusicService ▶ " + mOstDataItem.getPOI() + ".encrypt ON AIR 라디오 파일을 찾습니다.");
        else
            LogUtil.e("♬ MusicService ▶ " + mOstDataItem.getSSI() + mFormat + ".encrypt ON AIR 음악 파일을 찾습니다.");

        if (!isPlayFileSave) {
            LogUtil.e("♬ MusicService ▶ 임시저장 사용 설정이 비활성되어 스트리밍 서비스로 전환됩니다.");
            return isExists;
        }

        String[] musicFileList = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).list();
        for (int i = 0; i < musicFileList.length; i++) {
            if (PlayerConstants.SONG_RADIO) {
                if ((mOstDataItem.getPOI() + ".encrypt").equalsIgnoreCase(musicFileList[i])) {
                    isExists = true;
                    break;
                }
            } else {
                if ((mOstDataItem.getSSI() + mFormat + ".encrypt").equalsIgnoreCase(musicFileList[i])) {
                    isExists = true;
                    break;
                }
            }
        }
        return isExists;
    }

    private static void encrypt(String oriFilePath, String encFilePath, String securityKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(oriFilePath);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(encFilePath + ".encrypt");

        // Length is 16 byte
        // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357
        SecretKeySpec sks = new SecretKeySpec(securityKey.getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[16384]; // 16kb
        while((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();
    }

    private static String decrypt(String encFilePath, String securityKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(encFilePath);

        FileOutputStream fos = new FileOutputStream(encFilePath + ".decrypt");
        SecretKeySpec sks = new SecretKeySpec(securityKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[16384]; // 16kb
        while((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();

        return encFilePath + ".decrypt";
    }

    private void getData(int queryType, Object... args) {
        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_MUSIC_PAYMENT) {
            if (args != null && args.length > 5 && args[0] instanceof String && args[1] instanceof String && args[2] instanceof Integer && args[3] instanceof Integer && args[4] instanceof String && args[5] instanceof String) {
                thread = getMusicPaymentThread((String)args[0], (String)args[1], (Integer)args[2], (Integer)args[3], (String)args[4], (String)args[5]);
            }
        } else if (queryType == Constants.QUERY_MUSIC_PATH) {
            thread = getMusicPathThread();
        }
        // POST 데이터 조회 Trigger
        else if (queryType == Constants.QUERY_POST_DATA) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                thread = getPostDataThread((String)args[0]);
            }
        }
        // 음원 파일 대칭키 Trigger
        else if (queryType == Constants.QUERY_MUSIC_SECURITY) {
            thread = getMusicSecurityThread(args);
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getMusicPathThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicPathReq getMusicPathReq = new GetMusicPathReq();

                    if (PlayerConstants.SONG_ON_AIR) {
                        if (PlayerConstants.SONG_RADIO) {
                            getMusicPathReq.setPOI(mOstDataItem.getPOI());
                        } else {
                            getMusicPathReq.setSSI(mOstDataItem.getSSI());
                            getMusicPathReq.setTYPE(mFormat);
                        }

                        mGetMusicPathRes = request.getMusicPath(getMusicPathReq);
                        mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PATH);
                    } else {
                        if (PlayerConstants.SONG_RADIO) {
                            getMusicPathReq.setPOI(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI());
                        } else {
                            getMusicPathReq.setSSI(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI());
                            getMusicPathReq.setTYPE(mFormat);
                        }

                        mGetMusicPathRes = request.getMusicPath(getMusicPathReq);
                        mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PATH);
                    }
                } catch(Exception e) {
                    Controls.pauseControl(mContext);
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getMusicPaymentThread(final String ssi, final String playCycleType, final Integer playElapTime, final Integer playSeekingTime, final String oti, final String poi) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicPaymentReq getMusicPaymentReq = new GetMusicPaymentReq();
                    if (PlayerConstants.SONG_RADIO)
                        getMusicPaymentReq.setPOI(poi);
                    else
                        getMusicPaymentReq.setSSI(ssi);
                    getMusicPaymentReq.setPLAY_CYCLE_TYPE(playCycleType);
                    getMusicPaymentReq.setPLAY_ELAP_TIME(playElapTime);
                    getMusicPaymentReq.setPLAY_SEEKING_TIME(playSeekingTime);
                    getMusicPaymentReq.setOTI(oti);
                    request.getMusicPayment(getMusicPaymentReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PAYMENT);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PAYMENT_FAILED);
                } catch (POSTException e) {
                    if ("0600".equals(e.getCode())) {
                        if (PlayerConstants.SONG_RADIO) {
                            LogUtil.e("♬ MusicService ▶ 라디오 타이틀 곡을 재생합니다.");
                            Controls.nextControl(mContext);
                        } else {
                            if (PlayerConstants.MPS_RANDOM == Constants.MPS_RANDOM_OK) {
                                LogUtil.e("♬ MusicService ▶ 랜덤곡을 재생합니다.");
                                Controls.nextControl(mContext);
                            } else {
                                if (isExistsMusicData(PlayerConstants.SONG_NUMBER + 1)) {
                                    LogUtil.e("♬ MusicService ▶ 다음곡을 재생합니다.");
                                    Controls.nextControl(mContext);
                                } else {
                                    if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ALL) {
                                        LogUtil.e("♬ MusicService ▶ 전곡 반복 재생으로 다시 재생합니다.");
                                        Controls.nextControl(mContext);
                                    } else {
                                        LogUtil.e("♬ MusicService ▶ 재생이 모두 끝났습니다.");
                                        clearRadioOnAir();
                                    }
                                }
                            }
                        }
                    } else {
                        mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PAYMENT_FAILED);
                    }
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
                    GetPostPositionDataReq getPostPositionDataReq = new GetPostPositionDataReq();
                    getPostPositionDataReq.setPOI(PlayerConstants.SONG_ON_AIR_POI);
                    getPostPositionDataReq.setPOI_MOVE_FLAG(args[0]);
                    getPostPositionDataReq.setPOST_TYPE(Constants.REQUEST_TYPE_RADIO);
                    PostDataItem postDataItem = request.getPostPositionData(getPostPositionDataReq);

                    if (CommonUtil.isNull(postDataItem.getPOI())) {
                        LogUtil.e("♬ MusicService ▶ " + args[0]);
                        if ("AF".equals(args[0]))
                            DeviceUtil.showToast(mContext, "♬ MusicService ▶ 다음 라디오 사연이 없어 ON AIR를 종료합니다.");
                        else if ("BF".equals(args[0]))
                            DeviceUtil.showToast(mContext, "♬ MusicService ▶ 이전 라디오 사연이 없어 ON AIR를 종료합니다.");
                        clearRadioOnAir();
                    } else {
                        mOstDataItem = new OstDataItem();
                        mOstDataItem.setPOI(postDataItem.getPOI());
                        PlayerConstants.SONG_ON_AIR_POI = postDataItem.getPOI();
                        mOstDataItem.setSSI(postDataItem.getSSI());
                        mOstDataItem.setRADIO_PATH(postDataItem.getRADIO_PATH());
                        mOstDataItem.setPOST_TYPE(postDataItem.getPOST_TYPE());
                        mOstDataItem.setSONG_NM(postDataItem.getTITLE_SONG_NM());
                        mOstDataItem.setARTI_NM(postDataItem.getTITLE_ARTI_NM());
                        mOstDataItem.setALBUM_PATH(postDataItem.getTITLE_ALBUM_PATH());
                        playMusic();
                    }
                } catch(Exception e) {
                    LogUtil.e("♬ MusicService ▶ ON AIR 데이터 조회 중 실패 - " + e.getMessage());
                    PlayerConstants.SONG_PAUSED = true;
                    PlayerConstants.SONG_ON_AIR = false;
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                        isInitialized = false;
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getMusicSecurityThread(final Object... args) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicSecurityReq getMusicSecurityReq = new GetMusicSecurityReq();

                    if (PlayerConstants.SONG_ON_AIR) {
                        if (PlayerConstants.SONG_RADIO) {
                            getMusicSecurityReq.setPOI(mOstDataItem.getPOI());
                        } else {
                            getMusicSecurityReq.setSSI(mOstDataItem.getSSI());
                            getMusicSecurityReq.setTYPE(mFormat);
                        }
                    } else {
                        if (PlayerConstants.SONG_RADIO) {
                            getMusicSecurityReq.setPOI(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI());
                        } else {
                            getMusicSecurityReq.setSSI(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI());
                            getMusicSecurityReq.setTYPE(mFormat);
                        }
                    }
                    mGetMusicSecurityRes = request.getMusicSecurity(getMusicSecurityReq);

                    Bundle data = new Bundle();
                    Message msg = new Message();
                    if (args != null && args.length == 1 && args[0] instanceof String) {
                        data.putString("encFilePath", (String)args[0]);
                    } else if (args != null && args.length == 2 && args[0] instanceof String && args[1] instanceof String) {
                        data.putString("oriFilePath", (String)args[0]);
                        data.putString("encFilePath", (String)args[1]);
                    }
                    msg.setData(data);
                    msg.what = Constants.QUERY_MUSIC_SECURITY;
                    mHandler.sendMessage(msg);
                } catch(Exception e) {
                    Controls.pauseControl(mContext);
                }
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            // 미디어 파일 다운로드 조회 성공 후 Handler
            case Constants.QUERY_MUSIC_PATH:
                dataSourceInfo[0] = mGetMusicPathRes.getURL();
                if (PlayerConstants.SONG_ON_AIR) {
                    if (PlayerConstants.SONG_RADIO) {
                        dataSourceInfo[1] = mOstDataItem.getPOI();
                    } else {
                        dataSourceInfo[1] = mOstDataItem.getSSI() + mFormat;
                    }
                } else {
                    if (PlayerConstants.SONG_RADIO) {
                        dataSourceInfo[1] = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI();
                    } else {
                        dataSourceInfo[1] = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI() + mFormat;
                    }
                }

                HttpProxyCacheServer proxy = BaseApplication.getProxy(mContext);
                proxy.registerCacheListener(mCacheListener, dataSourceInfo[0]);
                try {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(proxy.getProxyUrl(dataSourceInfo[0]));
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.prepareAsync();
                    } else {
                        clearRadioOnAir();
                    }
                } catch (IOException e) {
                    LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                    clearRadioOnAir();
                }
                break;
            // 미디어 파일 서버 대칭키 조회 성공 후 Handler
            case Constants.QUERY_MUSIC_SECURITY:
                String oriFilePath = msg.getData().getString("oriFilePath", "");
                String encFilePath = msg.getData().getString("encFilePath", "");

                if (CommonUtil.isNotNull(oriFilePath) && CommonUtil.isNotNull(encFilePath)) {
                    try {
                        encrypt(oriFilePath, encFilePath, mGetMusicSecurityRes.getSYMMETRIC_KEY().substring(0, 16));
                    } catch (Exception e) {
                        LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                        clearRadioOnAir();
                    } finally {
                        File oriFile = new File(oriFilePath);
                        boolean isDelete = oriFile.delete();
                        if (!isDelete) LogUtil.e("♬ MusicService ▶ 원본파일 삭제 중 실패하였습니다.");
                    }
                } else if (CommonUtil.isNull(oriFilePath) && CommonUtil.isNotNull(encFilePath)) {
                    try {
                        String decryptPath = decrypt(encFilePath, mGetMusicSecurityRes.getSYMMETRIC_KEY().substring(0, 16));

                        if (CommonUtil.isNotNull(decryptPath) && mMediaPlayer != null) {
                            File dataSourceFile = new File(decryptPath);
                            FileInputStream fis = new FileInputStream(dataSourceFile);

                            mMediaPlayer.reset();
                            /**
                             * setDataSource 메서들 호출하고 나면 MediaPlayer 상태는 Initialized 상태가 된다. 즉 파일과 MediaPlayer의 동기화가 된 상태이다.
                             */
                            mMediaPlayer.setDataSource(fis.getFD());
                            /**
                             * AudioManager.STREAM_RING - 벨소리
                             * AudioManager.STREAM_MUSIC - 미디어
                             * AudioManager.STREAM_VOICE_CALL - 전화(수신 스피커)
                             */
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            /**
                             * prepareAsync의 경우 Stopped와 Initialized에서 호출되는 메소드로 이를 하고나면 Preparing 상태가된다.
                             * 이는 비디오 파일 같은 큰 자원은 prepare 하는데 시간이 걸리므로 prepare보다  prepareAsync를 사용 하는게 효율적이다.
                             * 스트리밍이 끝나고 prepare이 되면 OnpreparedListener.Onprepared가 호출되어 prepare 상태가되고 MediaPlayer.start를 이용 하여 재생을 하면된다.
                             */
                            mMediaPlayer.prepareAsync();
                            downloadingPercent = 100;
                        } else {
                            clearRadioOnAir();
                        }
                    } catch (Exception e) {
                        LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                        clearRadioOnAir();
                    }
                }
                break;
            // 과금요청 성공 후 Handler
            case Constants.QUERY_MUSIC_PAYMENT:
                break;
            // 과금요청 실패 후 Handler
            case Constants.QUERY_MUSIC_PAYMENT_FAILED:
                clearRadioOnAir();
                break;
        }
    }
}
