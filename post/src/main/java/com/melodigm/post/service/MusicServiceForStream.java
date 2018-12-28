package com.melodigm.post.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.data.GetMusicPaymentReq;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.CastUtil;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.StreamingMediaPlayer;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * YCNOTE - Service 주기
 * onCreate() -> onStartCommand() 순으로 이뤄 지는데요.
 * Service가 실행되고 있는 도중에 다시한번  startService()  메서드가 호출되면 onStartCommand() 주기 부터 실행하게 됩니다.
 * 마치 Activity의 onResume() 처럼 말이죠.
 * 그렇기 때문에 중요한 작업에 대해서는 onCreate() 보다는 onStartCommand() 메서드에 구현을 해줘야 합니다.
 */
public class MusicServiceForStream extends Service implements IOnHandlerMessage, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private Context mContext;
    private WeakRefHandler mHandler;
    private HashMap<Integer, RunnableThread> mThreads = null;
    private PostDatabases mdbHelper;
    private StreamingMediaPlayer audioStreamer;
    private String mDataSource;
    private String mFormat = "mp3";
    private boolean isInterrupted = false;
    private static Timer mTimer;
    private MainTask mMainTask;
    private boolean isPlayerTimer = false;
    private int mPlayerTimerTotalSecond = 0;

    private final Handler localHandler = new Handler();

    public static final String NOTIFY_PREVIOUS = "com.melodigm.post.previous";
    public static final String NOTIFY_DELETE = "com.melodigm.post.delete";
    public static final String NOTIFY_PAUSE = "com.melodigm.post.pause";
    public static final String NOTIFY_PLAY = "com.melodigm.post.play";
    public static final String NOTIFY_NEXT = "com.melodigm.post.next";

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

        PlayerConstants.SONG_NUMBER = SPUtil.getIntSharedPreference(mContext, Constants.SP_MPS_POSITION);
        PlayerConstants.MPS_REPEAT = SPUtil.getIntSharedPreference(mContext, Constants.SP_MPS_REPEAT);
        PlayerConstants.MPS_RANDOM = SPUtil.getIntSharedPreference(mContext, Constants.SP_MPS_RANDOM);
        LogUtil.e("♬ MusicService 생성 ▶ SONG_NUMBER(" + PlayerConstants.SONG_NUMBER + "), MPS_REPEAT(" + PlayerConstants.MPS_REPEAT + "), MPS_RANDOM(" + PlayerConstants.MPS_RANDOM + ")");

        startTimer();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogUtil.e("onAudioFocusChange : " + focusChange);

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                LogUtil.e("AudioManager.AUDIOFOCUS_GAIN");

                // resume playback
                if (audioStreamer != null && audioStreamer.getMediaPlayer() != null && !audioStreamer.getMediaPlayer().isPlaying()) {
                    PlayerConstants.SONG_PAUSED = false;
                    audioStreamer.getMediaPlayer().start();
                }
                audioStreamer.getMediaPlayer().setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS");

                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (audioStreamer != null && audioStreamer.getMediaPlayer() != null && audioStreamer.getMediaPlayer().isPlaying()) {
                    Controls.pauseControl(mContext);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT");

                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (audioStreamer != null && audioStreamer.getMediaPlayer() != null && audioStreamer.getMediaPlayer().isPlaying()) {
                    Controls.pauseControl(mContext);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                LogUtil.e("AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");

                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (audioStreamer != null && audioStreamer.getMediaPlayer() != null) {
                    audioStreamer.getMediaPlayer().setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        LogUtil.e("♬ MusicService ▶ onPrepared");

        // stop을 호출하게 되면 노래의 재생은  Stopped 상태가 되며 완전히 종료 되게 된다. Stopped 상태에서는 다시 start()를 실행 할수 없다. 이때는 다시 prepare 를 시킨후 start를 실행 하여야 될것이다.
        player.start();

        if (!PlayerConstants.SONG_RADIO) {
            if (mdbHelper == null) mdbHelper = new PostDatabases(this);
            mdbHelper.open();
            mdbHelper.updateOstPlayHistory(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER));
            mdbHelper.close();
        }

        String ssi = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI();
        String playCycleType = Constants.REQUEST_PLAY_CYCLE_TYPE_START;
        Integer playElapTime = 0;
        Integer playSeekingTime = 0;
        String oti = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getOTI();
        getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        LogUtil.e("♬ MusicService ▶ onError : what(" + what + "), extra(" + extra + ")");
        Controls.pauseControl(mContext);

        // MediaPlayer를 error 상태로 돌입하였고, reset이 필요하다.
        mp.reset();
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
        getData(Constants.QUERY_MUSIC_PAYMENT, ssi, playCycleType, playElapTime, playSeekingTime, oti);

        if (PlayerConstants.SONG_RADIO) {
            LogUtil.e("♬ MusicService ▶ 라디오 타이틀 곡을 재생합니다.");
            DeviceUtil.showToast(mContext, "♬ MusicService ▶ 라디오 타이틀 곡을 재생합니다.");
            Controls.nextControl(getApplicationContext());
        } else {
            if (isExistsMusicData(PlayerConstants.SONG_NUMBER + 1)) {
                LogUtil.e("♬ MusicService ▶ 다음곡을 재생합니다.");
                DeviceUtil.showToast(mContext, "♬ MusicService ▶ 다음곡을 재생합니다.");
                Controls.nextControl(getApplicationContext());
            } else {
                if (PlayerConstants.MPS_REPEAT == Constants.MPS_REPEAT_ALL) {
                    LogUtil.e("♬ MusicService ▶ 전곡 반복 재생으로 다시 재생합니다.");
                    DeviceUtil.showToast(mContext, "♬ MusicService ▶ 전곡 반복 재생으로 다시 재생합니다.");
                    Controls.nextControl(getApplicationContext());
                } else {
                    LogUtil.e("♬ MusicService ▶ 재생이 모두 끝났습니다.");
                    DeviceUtil.showToast(mContext, "♬ MusicService ▶ 재생이 모두 끝났습니다.");
                    Controls.pauseControl(getApplicationContext());
                }
            }
        }
    }

    private boolean requestFocus() {
        boolean isFocus;
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        isFocus = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        LogUtil.e("requestFocus : " + isFocus);
        return isFocus;
    }

    private void startStreamingAudio() {
        try {
            if (audioStreamer != null) {
                audioStreamer.interrupt();
            }
            audioStreamer = new StreamingMediaPlayer(mContext, null);

            if (CommonUtil.isNull(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH())) {
                PlayerConstants.SONG_RADIO = false;
            } else {
                PlayerConstants.SONG_RADIO = true;
            }

            String musicFilePath = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath();
            String musicFileName;

            if (PlayerConstants.SONG_RADIO) {
                // 라디오 파일 명
                musicFileName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH();
            } else {
                // 음악 파일 명
                musicFileName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI() + mFormat + ".dat";
            }

            //audioStreamer.createMediaPlayer("http://music.melodigm.com.s3.amazonaws.com/mp3/013396E31A63BB59C6DE.mp3").start();
            //audioStreamer.createMediaPlayer("http://music.melodigm.com/mp3/01C47DCA1A1CE29C29B4.mp3").start();

            if (isExistsFile(PlayerConstants.SONG_NUMBER)) {
                if (PlayerConstants.SONG_RADIO)
                    // 라디오 데이터 소스
                    mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH() + ".encrypt";
                else
                    // 음악 데이터 소스
                    mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI() + mFormat + ".dat.encrypt";

                LogUtil.e("♬ MusicService ▶ 파일이 존재하여 재활용 진행합니다.\n" + mDataSource);

                String decryptPath = "";
                try {
                    decryptPath = decrypt(mDataSource);
                } catch (Exception e) {
                    LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                } finally {
                    if (CommonUtil.isNotNull(decryptPath)) {
                        LogUtil.e("♬ MusicService ▶ 음원 복호화 파일 : " + decryptPath);
                        try {
                            final File dataSourceFile = new File(decryptPath);
                            audioStreamer.createMediaPlayer(dataSourceFile).start();

                            Runnable notification = new Runnable() {
                                public void run() {
                                    LogUtil.e("♬ MusicService ▶ 복호화 파일이 존재하여 삭제합니다. ( " + dataSourceFile.getAbsolutePath() + "( " + CastUtil.getFileSize(dataSourceFile.length()) + " )" + " )");
                                    dataSourceFile.delete();
                                }
                            };
                            localHandler.postDelayed(notification, 1000);
                        } catch (IOException e) {
                            LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                        }
                    }
                }
            } else {
                String mediaUrl;
                if (PlayerConstants.SONG_RADIO) {
                    mediaUrl = Constants.API_POST + "mus0401v1.post?REQ_DATA={'POI':'" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI() + "'}";
                    LogUtil.e("♬ MusicService ▶ 라디오 데이터 요청 : " + mediaUrl);
                } else {
                    mediaUrl = Constants.API_POST + "mus0401v1.post?REQ_DATA={'SSI':'" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI() + "','TYPE':'" + mFormat + "'}";
                    LogUtil.e("♬ MusicService ▶ 음악 데이터 요청 : " + mediaUrl);
                }
                audioStreamer.startStreaming(mediaUrl, 5208, 216, musicFilePath + "/" + musicFileName);
            }
        } catch (Exception e) {
            LogUtil.e("♬ MusicService ▶ Error starting to stream audio.", e);
        }
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
            if (PlayerConstants.SONGS_LIST.size() != 0) {
                if (PlayerConstants.SONGS_LIST.size() <= PlayerConstants.SONG_NUMBER)  PlayerConstants.SONG_NUMBER = 0;
                ostDataItem = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
            }
            int duration, position;
            if (audioStreamer != null && audioStreamer.getMediaPlayer() != null) {
                duration = audioStreamer.getMediaPlayer().getDuration();
                position = audioStreamer.getMediaPlayer().getCurrentPosition();
            } else {
                duration = 0;
                position = 0;
            }

            Bundle data = new Bundle();
            data.putParcelable("OstDataItem", ostDataItem);
            data.putInt("MUSIC_COUNT", PlayerConstants.SONGS_LIST.size());
            data.putInt("DURATON", duration);
            data.putInt("POSITION", position);

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
                    try {
                        if (requestFocus()) {
                            startStreamingAudio();
                        } else {
                            Controls.pauseControl(mContext);
                        }
                    } catch(Exception e) {
                        LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                    }
                    return false;
                }
            });

            PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String)msg.obj;
                    if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
                        if (isExistsMusicData(PlayerConstants.SONG_NUMBER)) {
                            PlayerConstants.SONG_PAUSED = false;

                            if (audioStreamer == null) {
                                if (requestFocus()) {
                                    startStreamingAudio();
                                } else {
                                    Controls.pauseControl(mContext);
                                }
                            } else if (audioStreamer != null && audioStreamer.getMediaPlayer() != null && !audioStreamer.getMediaPlayer().isPlaying()) {
                                if (requestFocus())
                                    audioStreamer.getMediaPlayer().start();
                                else
                                    Controls.pauseControl(mContext);
                            }
                        } else
                            DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                    }else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
                        PlayerConstants.SONG_PAUSED = true;

                        if (audioStreamer != null && audioStreamer.getMediaPlayer() != null && audioStreamer.getMediaPlayer().isPlaying()) {
                            audioStreamer.getMediaPlayer().pause();
                        }
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
                        LogUtil.m(mContext);
                        LogUtil.file(getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE));
                        handler.sendEmptyMessage(0);
                        break;
                    case Constants.MPS_COMMAND_SET:
                        if (audioStreamer != null && audioStreamer.getMediaPlayer() != null) {
                            audioStreamer.getMediaPlayer().seekTo(SEEKBAR_POSITION);
                        }
                        break;
                    case Constants.MPS_COMMAND_PUT:
                        if (CommonUtil.isNotNull(POI) && CommonUtil.isNotNull(SSI))
                            setPlayList(POI, SSI);
                        else
                            setPlayList(0);
                        break;
                    case Constants.MPS_COMMAND_ADD:
                        if (CommonUtil.isNotNull(POI) && CommonUtil.isNotNull(SSI))
                            setPlayList(POI, SSI);
                        else
                            setPlayList(0);

                        if (isExistsMusicData(PlayerConstants.SONG_NUMBER)) {
                            if (requestFocus()) {
                                PlayerConstants.SONG_PAUSED = false;
                                startStreamingAudio();
                            } else {
                                Controls.pauseControl(mContext);
                            }
                        } else
                            DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                        break;
                    case Constants.MPS_COMMAND_PLAY:
                        if (audioStreamer == null) {
                            if (requestFocus()) {
                                startStreamingAudio();
                            } else {
                                Controls.pauseControl(mContext);
                            }
                        } else if (audioStreamer != null && audioStreamer.getMediaPlayer() != null && !audioStreamer.getMediaPlayer().isPlaying()) {
                            if (requestFocus()) {
                                audioStreamer.getMediaPlayer().start();
                            } else {
                                Controls.pauseControl(mContext);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            LogUtil.e("♬ MusicService ▶ " + e.getMessage());
        }

        return START_NOT_STICKY;
    }

    /**
     * 서비스가 소멸되는 도중에 이 메소드가 호출되며 주로 Thread, Listener, BroadcastReceiver와 같은 자원들을 정리하는데 사용하면 됩니다.
     * TaskKiller에 의해 서비스가 강제종료될 경우에는 이 메소드가 호출되지 않습니다
     */
    @Override
    public void onDestroy() {
        LogUtil.e("♬ MusicService ▶ onDestroy");
        super.onDestroy();

        if (audioStreamer != null) {
            audioStreamer.interrupt();
        }

        mTimer.cancel();
        mTimer.purge();
        mTimer = null;

        PlayerConstants.SONG_PAUSED = true;
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
        if (PlayerConstants.SONG_RADIO)
            LogUtil.e("♬ MusicService ▶ " + PlayerConstants.SONGS_LIST.get(position).getRADIO_PATH() + ".encrypt 라디오 파일을 찾습니다.");
        else
            LogUtil.e("♬ MusicService ▶ " + PlayerConstants.SONGS_LIST.get(position).getSSI() + mFormat + ".dat.encrypt 음악 파일을 찾습니다.");

        boolean isExists = false;
        String[] musicFileList = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).list();
        for (int i = 0; i < musicFileList.length; i++) {
            if (PlayerConstants.SONG_RADIO) {
                if ((PlayerConstants.SONGS_LIST.get(position).getRADIO_PATH() + ".encrypt").equalsIgnoreCase(musicFileList[i])) {
                    isExists = true;
                    break;
                }
            } else {
                if ((PlayerConstants.SONGS_LIST.get(position).getSSI() + mFormat + ".dat.encrypt").equalsIgnoreCase(musicFileList[i])) {
                    isExists = true;
                    break;
                }
            }
        }
        return isExists;
    }

    private static String decrypt(String encFilePath) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(encFilePath);

        FileOutputStream fos = new FileOutputStream(encFilePath + ".decrypt");
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
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
            if (args != null && args.length > 4 && args[0] instanceof String && args[1] instanceof String && args[2] instanceof Integer && args[3] instanceof Integer && args[4] instanceof String) {
                thread = getMusicPaymentThread((String)args[0], (String)args[1], (Integer)args[2], (Integer)args[3], (String)args[4]);
            }
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getMusicPaymentThread(final String ssi, final String playCycleType, final Integer playElapTime, final Integer playSeekingTime, final String oti) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    GetMusicPaymentReq getMusicPaymentReq = new GetMusicPaymentReq();
                    getMusicPaymentReq.setSSI(ssi);
                    getMusicPaymentReq.setPLAY_CYCLE_TYPE(playCycleType);
                    getMusicPaymentReq.setPLAY_ELAP_TIME(playElapTime);
                    getMusicPaymentReq.setPLAY_SEEKING_TIME(playSeekingTime);
                    getMusicPaymentReq.setOTI(oti);
                    request.getMusicPayment(getMusicPaymentReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_MUSIC_PAYMENT);
                } catch(Exception e) {}
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case Constants.QUERY_MUSIC_PAYMENT:
                break;
        }
    }
}
