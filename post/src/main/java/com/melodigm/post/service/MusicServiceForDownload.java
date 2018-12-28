package com.melodigm.post.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.controls.Controls;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.data.GetMusicPaymentReq;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.KeyUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.TimeUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
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
public class MusicServiceForDownload extends Service implements IOnHandlerMessage, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private Context mContext;
    private PostDatabases mdbHelper;
    private MediaPlayer mMediaPlayer;
    private String mDataSource;
    private String mFormat = "mp3";
    // 중단 여부
    private boolean isInterrupted = false;

    /*private LockScreenReceiver mLockScreenReceiver = null;*/
    int NOTIFICATION_ID = 1111;
    public static final String NOTIFY_PREVIOUS = "com.melodigm.post.previous";
    public static final String NOTIFY_DELETE = "com.melodigm.post.delete";
    public static final String NOTIFY_PAUSE = "com.melodigm.post.pause";
    public static final String NOTIFY_PLAY = "com.melodigm.post.play";
    public static final String NOTIFY_NEXT = "com.melodigm.post.next";
    private static boolean currentVersionSupportBigNotification = false;
    private static boolean currentVersionSupportLockScreenControls = false;
    private static Timer mTimer;
    private MainTask mMainTask;
    private boolean isPlayerTimer = false;
    private int mPlayerTimerTotalSecond = 0;

    public WeakRefHandler mHandler;
    public HashMap<Integer, RunnableThread> mThreads = null;

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

        /*mLockScreenReceiver = new LockScreenReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mLockScreenReceiver, filter);*/

        currentVersionSupportBigNotification = DeviceUtil.currentVersionSupportBigNotification();
        currentVersionSupportBigNotification = false;
        currentVersionSupportLockScreenControls = DeviceUtil.currentVersionSupportLockScreenControls();

        // 최초 생성된 MediaPlayer의 상태는 Idle 상태이다.
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        startTimer();
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
            if (mMediaPlayer != null) {
                OstDataItem ostDataItem = null;
                if (PlayerConstants.SONGS_LIST.size() != 0) {
                    if (PlayerConstants.SONGS_LIST.size() <= PlayerConstants.SONG_NUMBER)  PlayerConstants.SONG_NUMBER = 0;
                    ostDataItem = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
                }
                int duration, position;
                if (CommonUtil.isNull(mDataSource)) {
                    duration = 0;
                    position = 0;
                } else {
                    duration = mMediaPlayer.getDuration();
                    position = mMediaPlayer.getCurrentPosition();
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
                    newNotification();
                    try {
                        playMusic();
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
                    if(mMediaPlayer == null)
                        return false;
                    if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
                    /*if(currentVersionSupportLockScreenControls){
                        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                    }*/
                        // 플레이어가 아직 준비가 안되어 있을 경우
                        if (mMediaPlayer.getCurrentPosition() == 0) {
                            if (isExistsMusicData(PlayerConstants.SONG_NUMBER)) {
                                PlayerConstants.SONG_PAUSED = false;
                                playMusic();
                                newNotification();
                            } else
                                DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                        } else {
                            PlayerConstants.SONG_PAUSED = false;
                            mMediaPlayer.start();
                        }
                    }else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
                        PlayerConstants.SONG_PAUSED = true;
                    /*if(currentVersionSupportLockScreenControls){
                        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                    }*/
                        mMediaPlayer.pause();
                    }
                    newNotification();
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
                        if (mMediaPlayer != null) {
                            mMediaPlayer.seekTo(SEEKBAR_POSITION);
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
                            PlayerConstants.SONG_PAUSED = false;
                            playMusic();
                            newNotification();
                        } else
                            DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                        break;
                    case Constants.MPS_COMMAND_PLAY:
                        // 재생중이 아닐경우
                        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                            // 플레이어가 아직 준비가 안되어 있을 경우
                            if (mMediaPlayer.getCurrentPosition() == 0) {
                                if (isExistsMusicData(PlayerConstants.SONG_NUMBER)) {
                                    Controls.playControl(mContext);
                                } else
                                    DeviceUtil.showToast(mContext, "음원정보가 없습니다.");
                            } else
                                mMediaPlayer.start();
                        }
                        break;
                }

                /*if (intent.getAction() == null) {
                    if (mScreenReceiver == null) {
                        mLockScreenReceiver = new LockScreenReceiver();
                        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                        registerReceiver(mLockScreenReceiver, filter);
                    }
                }*/
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
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            interrupt();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        /*if (mScreenReceiver != null) unregisterReceiver(mScreenReceiver);*/
        PlayerConstants.SONG_PAUSED = true;
    }

    private void playMusic() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            Controls.pauseControl(mContext);
        }

        interrupt();
        if ("".equals(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH())) {
            PlayerConstants.SONG_RADIO = false;
        } else {
            PlayerConstants.SONG_RADIO = !PlayerConstants.SONG_RADIO;
        }

        if (isExistsFile(PlayerConstants.SONG_NUMBER)) {
            if (PlayerConstants.SONG_RADIO)
                // 라디오 데이터 소스
                mDataSource = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH();
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
                    File dataSourceFile = null;
                    try {
                        dataSourceFile = new File(decryptPath);
                        FileInputStream fis = new FileInputStream(dataSourceFile);

                        mMediaPlayer.reset();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(fis.getFD());
                        mMediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                    } finally {
                        dataSourceFile.delete();
                    }
                }
            }
        } else {
            if (PlayerConstants.SONG_RADIO)
                // 라디오 데이터 요청
                getMusicDownloadThread(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getRADIO_PATH(), PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getPOI());
            else
                // 음악 데이터 요청
                getMusicDownloadThread(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getSSI(), mFormat);
        }
    }

    @SuppressLint("NewApi")
    private void newNotification() {
        if (true) return;
        LogUtil.e("♬ MusicService ▶ newNotification");
        OstDataItem ostDataItem = (PlayerConstants.SONGS_LIST.size() == 0) ? null : PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);

        if (ostDataItem != null) {
            String songName = ostDataItem.getSONG_NM();
            String albumName = ostDataItem.getARTI_NM();
            RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_music);
            RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_music_expand);

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.icon_ost_nor)
                    .setContentTitle(songName).build();

            setListeners(simpleContentView);
            setListeners(expandedView);

            notification.contentView = simpleContentView;
            if (currentVersionSupportBigNotification) {
                notification.bigContentView = expandedView;
            }

            /*try {
                if (CommonUtil.isNull(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getALBUM_PATH())) {
                    notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.icon_album_dummy);
                    if (currentVersionSupportBigNotification)
                        notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.icon_album_dummy);
                } else {
                    notification.contentView.setImageViewUri(R.id.imageViewAlbumArt, Uri.parse(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getALBUM_PATH()));
                    if (currentVersionSupportBigNotification)
                        notification.bigContentView.setImageViewUri(R.id.imageViewAlbumArt, Uri.parse(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getALBUM_PATH()));
                }
            } catch(Exception e) {
                LogUtil.e("♬ MusicService ▶ Error : " + e.getMessage());
            }*/

            if (PlayerConstants.SONG_PAUSED) {
                notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
                notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

                if (currentVersionSupportBigNotification) {
                    notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
                    notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
                }
            } else {
                notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

                if(currentVersionSupportBigNotification){
                    notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                    notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
                }
            }

            notification.contentView.setTextViewText(R.id.textSongName, songName);
            notification.contentView.setTextViewText(R.id.textAlbumName, albumName);

            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setTextViewText(R.id.textSongName, songName);
                notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
            }

            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    public void setListeners(RemoteViews view) {
        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);

        PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);
    }

    private void setPlayList(Object... args) {
        if (mdbHelper == null)
            mdbHelper = new PostDatabases(this);

        mdbHelper.open();
        PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
        /*for (OstDataItem item : PlayerConstants.SONGS_LIST) {
            LogUtil.e(item.getSONG_NM() + "(" + item.getARTI_NM() + ") - " + item.getSSI() + " / " + item.getPOI() + " / " + item.getPOST_TYPE());
        }*/
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
            LogUtil.e("♬ MusicService ▶ " + PlayerConstants.SONGS_LIST.get(position).getRADIO_PATH() + " 라디오 파일을 찾습니다.");
        else
            LogUtil.e("♬ MusicService ▶ " + PlayerConstants.SONGS_LIST.get(position).getSSI() + mFormat + ".dat.encrypt 음악 파일을 찾습니다.");

        boolean isExists = false;
        String[] musicFileList = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).list();
        for (int i = 0; i < musicFileList.length; i++) {
            if (PlayerConstants.SONG_RADIO) {
                if ((PlayerConstants.SONGS_LIST.get(position).getRADIO_PATH()).equalsIgnoreCase(musicFileList[i])) {
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

    public RunnableThread getMusicDownloadThread(final String SSI, final String format) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                boolean isSuccess = true;
                String musicFilePath = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE).getAbsolutePath();
                String musicFileName;

                if (PlayerConstants.SONG_RADIO) {
                    // 라디오 파일 명
                    musicFileName = SSI;
                    LogUtil.e("♬ MusicService ▶ 라디오파일이 없어 다운로드를 진행합니다.");
                } else {
                    // 음악 파일 명
                    musicFileName = SSI + format + ".dat";
                    LogUtil.e("♬ MusicService ▶ 음악파일이 없어 다운로드를 진행합니다.");
                }

                File file = new File(musicFilePath + "/" + musicFileName);
                long fileSize = 0;
                long downloadFileSize = 0;

                try {
                    URL url;
                    if (PlayerConstants.SONG_RADIO)
                        url = new URL(Constants.API_POST + "mus0401v1.post?REQ_DATA={'POI':'" + format + "'}");
                    else
                        url = new URL(Constants.API_POST + "mus0401v1.post?REQ_DATA={'SSI':'" + SSI + "','TYPE':'" + format + "'}");

                    // HttpURLConnection 객체 생성.
                    HttpURLConnection urlConn = null;

                    // URL 연결 (웹페이지 URL 연결.)
                    urlConn = (HttpURLConnection)url.openConnection();

                    // TimeOut 시간 (서버 접속시 연결 시간)
                    urlConn.setConnectTimeout(Constants.TIMEOUT_HTTP_CONNECTION);

                    // TimeOut 시간 (Read시 연결 시간)
                    urlConn.setReadTimeout(Constants.TIMEOUT_HTTP_CONNECTION);

                    // 요청 방식 선택 (GET, POST)
                    urlConn.setRequestMethod("POST");

                    // Request Header값 셋팅 setRequestProperty(String key, String value)
                    String timeStamp = TimeUtil.getTimeStamp();
                    String tokenKey = KeyUtil.getTokenKey(Constants.POST_SECRET_KEY, Constants.POST_ACCESS_KEY, timeStamp);

                    urlConn.setRequestProperty("device-os-version", android.os.Build.VERSION.RELEASE);
                    urlConn.setRequestProperty("device-model", android.os.Build.MODEL);
                    urlConn.setRequestProperty("device-type", Constants.HEADER_DEVICE_TYPE);
                    urlConn.setRequestProperty("post-app-version", String.valueOf(Constants.APP_VERSION_CODE));
                    urlConn.setRequestProperty("post-app-user-id", SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID));
                    urlConn.setRequestProperty("post-access-token", tokenKey);
                    urlConn.setRequestProperty("post-access-key", Constants.POST_ACCESS_KEY);
                    urlConn.setRequestProperty("post-timestamp", timeStamp);

                    // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
                    urlConn.setDoOutput(true);

                    // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
                    urlConn.setDoInput(true);

                    InputStream is = urlConn.getInputStream();

                    String contentLength = urlConn.getHeaderField("Content-Length");
                    if (CommonUtil.isNotNull(contentLength))
                        fileSize = Integer.valueOf(contentLength);

                    OutputStream os = new FileOutputStream(file);
                    int c = 0;
                    byte[] buf = new byte[16384]; // 16kb
                    LogUtil.e("♬ MusicService ▶ " + NumberFormat.getNumberInstance(Locale.KOREA).format(fileSize) + " Bytes");
                    isInterrupted = false;
                    int rowCount = 0;
                    do {
                        int numread = is.read(buf);
                        if (numread <= 0)
                            break;
                        os.write(buf, 0, numread);

                        if (rowCount % 40 == 0) {
                            LogUtil.e("♬ MusicService ▶ " + NumberFormat.getNumberInstance(Locale.KOREA).format(file.length()) + " Bytes 다운로드");
                        }
                        rowCount++;
                    } while (validateNotInterrupted());
                    os.flush();
                    is.close();
                    os.close();
                } catch (Exception e) {
                    isSuccess = false;
                    LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                } finally {
                    if (isSuccess) {
                        try {
                            downloadFileSize = file.length();
                            LogUtil.e("♬ MusicService ▶ " + NumberFormat.getNumberInstance(Locale.KOREA).format(downloadFileSize) + " Bytes 다운로드 완료");

                            if (fileSize == downloadFileSize) {
                                mDataSource = musicFilePath + "/" + musicFileName;
                                mMediaPlayer.reset();
                                /**
                                 * 벨소리
                                 * mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                                 * 미디어
                                 * mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                 * 전화(수신 스피커)
                                 * mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                                 */
                                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                /**
                                 * setDataSource 메서들 호출하고 나면 MediaPlayer 상태는 Initialized 상태가 된다. 즉 파일과 MediaPlayer의 동기화가 된 상태이다.
                                 */
                                File dataSourceFile = new File(musicFilePath + "/" + musicFileName);
                                FileInputStream fis = new FileInputStream(dataSourceFile);
                                mMediaPlayer.setDataSource(fis.getFD());
                                /**
                                 * prepareAsync의 경우 Stopped와 Initialized에서 호출되는 메소드로 이를 하고나면 Preparing 상태가된다.
                                 * 이는 비디오 파일 같은 큰 자원은 prepare 하는데 시간이 걸리므로 prepare보다  prepareAsync를 사용 하는게 효율적이다.
                                 * 스트리밍이 끝나고 prepare이 되면 OnpreparedListener.Onprepared가 호출되어 prepare 상태가되고 MediaPlayer.start를 이용 하여 재생을 하면된다.
                                 */
                                mMediaPlayer.prepareAsync();

                                try {
                                    encrypt(mDataSource);
                                } catch (Exception e) {
                                    LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                                } finally {
                                    dataSourceFile.delete();
                                }
                            } else {
                                LogUtil.e("♬ MusicService ▶ 다운로드가 완료되지 않아 파일을 삭제처리 합니다. (" + musicFilePath + "/" + musicFileName + ")");
                                file.delete();
                            }
                        } catch (IOException e) {
                            LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                        }
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    private boolean validateNotInterrupted() {
        if (isInterrupted) {
            //Controls.pauseControl(mContext);
            return false;
        } else {
            return true;
        }
    }

    public void interrupt() {
        LogUtil.e("♬ MusicService ▶ 다운로드 중단");
        isInterrupted = true;
        validateNotInterrupted();
    }

    private static void encrypt(String oriFilePath) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(oriFilePath);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(oriFilePath + ".encrypt");

        // Length is 16 byte
        // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
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
