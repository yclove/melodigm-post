package com.melodigm.post.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.melodigm.post.common.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class StreamingMediaPlayer {
    //private static final int INTIAL_KB_BUFFER =  96*10/8;//assume 96kbps*10secs/8bits per byte
    private static final int INTIAL_KB_BUFFER =  512;

    private TextView textStreamed;
    private ImageButton playButton;
    private ProgressBar	progressBar;

    //  Track for display by progressBar
    private long mediaLengthInKb, mediaLengthInSeconds;
    private int totalKbRead = 0;

    // Create Handler to call View updates on the main UI thread.
    private final Handler handler = new Handler();

    public MediaPlayer mediaPlayer;
    private File downloadingMediaFile;

    // 중단 여부
    private boolean isInterrupted;
    private Context context;
    private int counter = 0;

    private String mOutputFilaPath;

    private MediaPlayer.OnCompletionListener mCompletionListener;

    public StreamingMediaPlayer(Context  context, MediaPlayer.OnCompletionListener mCompletionListener) {
        this.context = context;
        this.mCompletionListener = mCompletionListener;
    }

    /**
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */
    public void startStreaming(final String mediaUrl, long mediaLengthInKb, long mediaLengthInSeconds, String outputFilaPath) throws IOException {
        this.mediaLengthInKb = mediaLengthInKb;
        this.mediaLengthInSeconds = mediaLengthInSeconds;
        this.mOutputFilaPath = outputFilaPath;

        // 쓰레드 시작
        Runnable r = new Runnable() {
            public void run() {
                try {
                    downloadAudioIncrement(mediaUrl);
                } catch (IOException e) {
                    LogUtil.e("Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
                    return;
                }
            }
        };
        new Thread(r).start();
    }

    /**
     * Download the url stream to a temporary location and then call the setDataSource
     * for that local file
     */
    public void downloadAudioIncrement(String mediaUrl) throws IOException {

        // 파일 URL 주소로 부터 연결
        URLConnection cn = new URL(mediaUrl).openConnection();
        String timeStamp = TimeUtil.getTimeStamp();
        String tokenKey = KeyUtil.getTokenKey(Constants.POST_SECRET_KEY, Constants.POST_ACCESS_KEY, timeStamp);

        cn.setRequestProperty("device-os-version", android.os.Build.VERSION.RELEASE);
        cn.setRequestProperty("device-model", android.os.Build.MODEL);
        cn.setRequestProperty("device-type", Constants.HEADER_DEVICE_TYPE);
        cn.setRequestProperty("post-app-version", String.valueOf(Constants.APP_VERSION_CODE));
        cn.setRequestProperty("post-app-user-id", SPUtil.getSharedPreference(context, Constants.SP_USER_ID));
        cn.setRequestProperty("post-access-token", tokenKey);
        cn.setRequestProperty("post-access-key", Constants.POST_ACCESS_KEY);
        cn.setRequestProperty("post-timestamp", timeStamp);
        cn.connect();

        InputStream stream = cn.getInputStream();
        if (stream == null) {
            LogUtil.e("Unable to create InputStream for mediaUrl:" + mediaUrl);
        }

        // 캐시 폴더 만들고 .dat파일 생성
        downloadingMediaFile = new File(context.getCacheDir(),"downloadingMedia.dat");

        // 같은 경로에 파일이 존재 하면 그 파일 삭제함.. 캐시메모리 때문
        if (downloadingMediaFile.exists()) {
            boolean isDelete = downloadingMediaFile.delete();
            if (isDelete) {
                LogUtil.e("다운로드가 사작되었습니다. downloadingMediaFile 파일이 존재하여 삭제되었습니다. ( " + downloadingMediaFile.getAbsolutePath() + " )");
            } else {
                LogUtil.e("다운로드가 사작되었습니다. downloadingMediaFile 파일이 존재하여 삭제 중 실패 되었습니다.");
            }
        }

        // 다시 파일 생성
        FileOutputStream out = new FileOutputStream(downloadingMediaFile);
        byte buf[] = new byte[16384]; // 16kb
        int totalBytesRead = 0, incrementalBytesRead = 0;
        int rowCount = 0;
        // 캐시 영역에 파일 저장
        do {
            int numread = stream.read(buf);
            if (numread <= 0)
                break;
            out.write(buf, 0, numread);
            totalBytesRead += numread;
            incrementalBytesRead += numread;
            totalKbRead = totalBytesRead/1000;

            testMediaBuffer();
            //fireDataLoadUpdate();
            if (rowCount % 40 == 0) {
                LogUtil.e("♬ MusicService ▶ " + NumberFormat.getNumberInstance(Locale.KOREA).format(downloadingMediaFile.length()) + " Bytes 다운로드");
            }
            rowCount++;
        } while (validateNotInterrupted());
        // 파일 전송이 끝나면 종료
        stream.close();
        if (validateNotInterrupted()) {
            fireDataFullyLoaded();
        }
    }

    private boolean validateNotInterrupted() {
        if (isInterrupted) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                //mediaPlayer.release();
            }
            return false;
        } else {
            return true;
        }
    }


    /**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */
    private void  testMediaBuffer() {
        Runnable updater = new Runnable() {
            public void run() {
                if (mediaPlayer == null) {
                    //  Only create the MediaPlayer once we have the minimum buffered data
                    if ( totalKbRead >= INTIAL_KB_BUFFER) {
                        try {
                            // 받은 파일의 크기가 INTIAL_KB_BUFFER(120) 이상이면 음악파일 재생
                            startMediaPlayer();
                        } catch (Exception e) {
                            LogUtil.e("♬ MusicService ▶ Error copying buffered conent.", e);
                        }
                    }
                } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ) {
                    //  NOTE:  The media player has stopped at the end so transfer any existing buffered data
                    //  We test for < 1second of data because the media player can stop when there is still
                    //  a few milliseconds of data left to play
                    // 음악 파일을 받다가 끊어지면 음악 재생을 멈춘다
                    LogUtil.e("♬ MusicService ▶ Duration(" + DateUtil.getConvertMsToFormat(mediaPlayer.getDuration() / 1000) + "), Position(" + DateUtil.getConvertMsToFormat(mediaPlayer.getCurrentPosition() / 1000) + ")");
                    transferBufferToMediaPlayer();
                }
            }
        };
        handler.post(updater);
    }

    private void startMediaPlayer() {
        try {
            File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");

            // We double buffer the data to avoid potential read/write errors that could happen if the
            // download thread attempted to write at the same time the MediaPlayer was trying to read.
            // For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while
            // the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack,
            // we move the currently loaded data to a temporary buffer file that we start playing while the remaining
            // data downloads.
            moveFile(downloadingMediaFile, bufferedFile);

            mediaPlayer = createMediaPlayer(bufferedFile);
            // 음악 파일 생성 후 재생
            // We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.
            mediaPlayer.start();
            //startPlayProgressUpdater();
            //playButton.setEnabled(true);
        } catch (IOException e) {
            LogUtil.e("♬ MusicService ▶ Error initializing the MediaPlayer.", e);
            return;
        }
    }

    public MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
        MediaPlayer mPlayer = new MediaPlayer();
        mPlayer.setOnErrorListener(
                new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        LogUtil.e("♬ MusicService ▶ Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
                        return false;
                    }
                });
        mPlayer.setOnCompletionListener(mCompletionListener);
        //  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
        //  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
        //  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
        FileInputStream fis = new FileInputStream(mediaFile);
        LogUtil.e("♬ MusicService ▶ setDataSource : " + mediaFile.getAbsolutePath() + "( " + CastUtil.getFileSize(mediaFile.length()) + " )");
        mPlayer.setDataSource(fis.getFD());
        mPlayer.prepare();
        mediaPlayer = mPlayer;
        LogUtil.e("♬ MusicService ▶ 재생시간 : " + DateUtil.getConvertMsToFormat(mediaPlayer.getDuration() / 1000));
        return mediaPlayer;
    }

    public MediaPlayer createMediaPlayer(String mediaFileUrl) throws IOException {
        MediaPlayer mPlayer = new MediaPlayer();
        mPlayer.setOnErrorListener(
                new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        LogUtil.e("♬ MusicService ▶ Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
                        return false;
                    }
                });
        mPlayer.setOnCompletionListener(mCompletionListener);
        //  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
        //  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
        //  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
        LogUtil.e("♬ MusicService ▶ setDataSource URL : " + mediaFileUrl);
        mPlayer.setDataSource(mediaFileUrl);
        mPlayer.prepare();
        mediaPlayer = mPlayer;
        LogUtil.e("♬ MusicService ▶ 재생시간 : " + DateUtil.getConvertMsToFormat(mediaPlayer.getDuration() / 1000));
        return mediaPlayer;
    }

    /**
     * Transfer buffered data to the MediaPlayer.
     * NOTE: Interacting with a MediaPlayer on a non-main UI thread can cause thread-lock and crashes so
     * this method should always be called using a Handler.
     */
    private void transferBufferToMediaPlayer() {
        try {
            // First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
            boolean wasPlaying = mediaPlayer.isPlaying();
            int curPosition = mediaPlayer.getCurrentPosition();

            // Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later.
            File oldBufferedFile = new File(context.getCacheDir(),"playingMedia" + counter + ".dat");
            File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");

            //  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to
            // keep and track fully downloaded files for later use, write caching code and please send me a copy.
            // YCNOTE - 종료시 삭제하도록 설정
            bufferedFile.deleteOnExit();
            moveFile(downloadingMediaFile,bufferedFile);

            // Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),
            // this always happens so quickly that the user never realized we've stopped the player and started a new one
            LogUtil.e("♬ MusicService ▶ 미디어 플레이어 Pause");
            mediaPlayer.pause();

            // Create a new MediaPlayer rather than try to re-prepare the prior one.
            mediaPlayer = createMediaPlayer(bufferedFile);
            mediaPlayer.seekTo(curPosition);

            //  Restart if at end of prior buffered content or mediaPlayer was previously playing.
            //	NOTE:  We test for < 1second of data because the media player can stop when there is still
            //  a few milliseconds of data left to play
            boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
            if (wasPlaying || atEndOfFile){
                LogUtil.e("♬ MusicService ▶ 미디어 플레이어 Start");
                mediaPlayer.start();
            }

            // Lastly delete the previously playing buffered File as it's no longer needed.
            LogUtil.e("♬ MusicService ▶ 이미 재생 된 파일이 존재하여 삭제합니다. ( " + oldBufferedFile.getAbsolutePath() + "( " + CastUtil.getFileSize(oldBufferedFile.length()) + " )" + " )");
            oldBufferedFile.delete();
        } catch (Exception e) {
            LogUtil.e("♬ MusicService ▶ Error updating to newly loaded content.", e);
        }
    }

    private void fireDataLoadUpdate() {
        Runnable updater = new Runnable() {
            public void run() {
                textStreamed.setText((totalKbRead + " Kb read"));
                float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);
                progressBar.setSecondaryProgress((int)(loadProgress*100));
            }
        };
        handler.post(updater);
    }

    private void fireDataFullyLoaded() {
        Runnable updater = new Runnable() {
            public void run() {
                transferBufferToMediaPlayer();

                // Delete the downloaded File as it's now been transferred to the currently playing buffer file.
                LogUtil.e("♬ MusicService ▶ 다운로드가 끝났습니다. downloadingMediaFile 파일이 존재하여 삭제합니다. ( " + downloadingMediaFile.getAbsolutePath() + "( " + CastUtil.getFileSize(downloadingMediaFile.length()) + " )" + " )");
                try {
                    encrypt(downloadingMediaFile, mOutputFilaPath);
                } catch (Exception e) {
                    LogUtil.e("♬ MusicService ▶ " + e.getMessage());
                }
                //textStreamed.setText(("Audio full loaded: " + totalKbRead + " Kb read"));
            }
        };
        handler.post(updater);
    }

    private static void encrypt(File oriFile, String outputFilePath) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(oriFile);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(outputFilePath + ".encrypt");

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

        oriFile.delete();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void startPlayProgressUpdater() {
        float progress = (((float)mediaPlayer.getCurrentPosition()/1000)/mediaLengthInSeconds);
        progressBar.setProgress((int)(progress*100));

        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification,1000);
        }
    }

    public void interrupt() {
        LogUtil.e("♬ MusicService ▶ 다운로드 중단");

        //playButton.setEnabled(false);
        isInterrupted = true;
        validateNotInterrupted();
    }

    /**
     *  Move the file in oldLocation to newLocation.
     */
    public void moveFile(File	oldLocation, File	newLocation) throws IOException {
        if ( oldLocation.exists( )) {
            BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
            BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
            try {
                byte[]  buff = new byte[8192];
                int numChars;
                while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
                    writer.write( buff, 0, numChars );
                }

                LogUtil.e("다운로드 파일 : " + oldLocation.getAbsolutePath() + "( " + CastUtil.getFileSize(oldLocation.length()) + " )");
                LogUtil.e("플레이 파일 : " + newLocation.getAbsolutePath() + "( " + CastUtil.getFileSize(newLocation.length()) + " )");
            } catch( IOException ex ) {
                throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if ( reader != null ){
                        writer.close();
                        reader.close();
                    }
                } catch( IOException ex ){
                    LogUtil.e("Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
                }
            }
        } else {
            throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
        }
    }
}