package com.melodigm.post.util;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class DMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mScanner;
    private String mFilepath = null;

    public DMediaScanner(Context context) {
        mScanner = new MediaScannerConnection(context, this);
    }

    public void startScan(String filepath) {
        mFilepath = filepath;
        mScanner.connect(); // 이 함수 호출 후 onMediaScannerConnected가 호출 됨.
    }

    @Override
    public void onMediaScannerConnected() {
        if(mFilepath != null) {
            String filepath = new String(mFilepath);
            mScanner.scanFile(filepath, null); // MediaStore의 정보를 업데이트
        }

        mFilepath = null;
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mScanner.disconnect(); // onMediaScannerConnected 수행이 끝난 후 연결 해제
    }
}