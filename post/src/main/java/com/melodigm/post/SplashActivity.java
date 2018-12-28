package com.melodigm.post;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.AppVersionRes;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.TextureVideoView;

import java.io.File;
import java.util.ArrayList;

public class SplashActivity extends BaseActivity implements IOnHandlerMessage {
    private TextureVideoView mTextureVideoView;
    private ImageView ivRotate;
    private TextView tvAppVersion;
    private AppVersionRes mAppVersionRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        setDisplay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTextureVideoView != null) {
            mTextureVideoView.stop();
            mTextureVideoView = null;
        }
    }

    private void setDisplay() {
        tvAppVersion = (TextView)findViewById(R.id.tvAppVersion);
        if (CommonUtil.isNotNull(Constants.APP_VERSION_NAME))
            tvAppVersion.setText(getString(R.string.app_version, Constants.APP_VERSION_NAME));

        mTextureVideoView = (TextureVideoView)findViewById(R.id.textureVideoView);
        //mTextureVideoView.setAlpha(0.3f);
        mTextureVideoView.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);

        // TODO : 인트로 동영상 확인 처리.
        /*String introBg = SPUtil.getSharedPreference(mContext, Constants.SP_INTRO_BG);
        if (CommonUtil.isNotNull(introBg)) {
            String[] tmpDirs = introBg.split("/");

            if (CommonUtil.isNotNull(tmpDirs[tmpDirs.length - 1])) {
                String introFilePath = getDir(Constants.SERVICE_INTRO_FILE_PATH, MODE_PRIVATE).getAbsolutePath() + "/" + tmpDirs[tmpDirs.length - 1];
                File file = new File(introFilePath);
                if (file.exists()) {
                    mTextureVideoView.setDataSource(introFilePath);
                } else {
                    mTextureVideoView.setDataSource(mContext, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.swings));
                }
            } else {
                mTextureVideoView.setDataSource(mContext, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.swings));
            }
        } else {
            mTextureVideoView.setDataSource(mContext, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.swings));
        }*/
        mTextureVideoView.setDataSource(mContext, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.swings));

        mTextureVideoView.setLooping(true);
        mTextureVideoView.play();

        ivRotate = (ImageView)findViewById(R.id.ivRotate);
        RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setRepeatCount(Animation.INFINITE);
        ivRotate.startAnimation(rotate);

        // Network에 연결되어 있지 않을 경우
        if (!DeviceUtil.isOnline(mContext))
            mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NETWORK);
        else
            getData();
    }

    private void getData() {
        new Thread(appVersionThread).start();
    }

    private Runnable appVersionThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(SplashActivity.this);
            try {
                mAppVersionRes = request.getAppVersion();
                mHandler.sendEmptyMessage(Constants.QUERY_APP_VERSION);
            } catch(RequestException e) {
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
            } catch(POSTException e) {
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
    };

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case Constants.QUERY_APP_VERSION:
                if (SPUtil.getIntSharedPreference(mContext, Constants.SP_LATEST_APP_VERSION) != mAppVersionRes.getAPP_VER())
                    SPUtil.setSharedPreference(mContext, Constants.SP_LATEST_APP_VERSION, mAppVersionRes.getAPP_VER());

                if (!SPUtil.getSharedPreference(mContext, Constants.SP_ICI_LIKE).equals(mAppVersionRes.getLIKE_ICI()))
                    SPUtil.setSharedPreference(mContext, Constants.SP_ICI_LIKE, mAppVersionRes.getLIKE_ICI());

                if (!SPUtil.getSharedPreference(mContext, Constants.SP_COUNTRY_CODE).equals(mAppVersionRes.getCOUNTRY_CODE()))
                    SPUtil.setSharedPreference(mContext, Constants.SP_COUNTRY_CODE, mAppVersionRes.getCOUNTRY_CODE());

                if (!SPUtil.getSharedPreference(mContext, Constants.SP_INTRO_BG).equals(mAppVersionRes.getPOST_INTRO_BG()))
                    SPUtil.setSharedPreference(mContext, Constants.SP_INTRO_BG, mAppVersionRes.getPOST_INTRO_BG());

                if (mAppVersionRes.getArrNotiSettingItem().size() > 0) {
                    boolean isNotificationPost = "Y".equalsIgnoreCase(mAppVersionRes.getArrNotiSettingItem().get(0).getPOST());
                    boolean isNotificationOst = "Y".equalsIgnoreCase(mAppVersionRes.getArrNotiSettingItem().get(0).getOST());
                    boolean isNotificationService = "Y".equalsIgnoreCase(mAppVersionRes.getArrNotiSettingItem().get(0).getMANNER());
                    boolean isNotificationToday = "Y".equalsIgnoreCase(mAppVersionRes.getArrNotiSettingItem().get(0).getTODAY());

                    if (isNotificationPost != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_POST))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_POST, isNotificationPost);
                    if (isNotificationOst != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_OST))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_OST, isNotificationOst);
                    if (isNotificationService != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE, isNotificationService);
                    if (isNotificationToday != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_TODAY))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_TODAY, isNotificationToday);
                }

                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                mdbHelper.deleteAllPostColors();
                mdbHelper.deleteAllPostHashTagKeyWords();
                mdbHelper.deleteAllPostPopKeyWords();
                mdbHelper.deleteAllMusPopKeyWords();

                if (mAppVersionRes.getColorItemList().size() > 0)
                    mdbHelper.updatePostColor(mAppVersionRes.getColorItemList());

                if (mAppVersionRes.getArrPostHashTagKeyWordItem().size() > 0)
                    mdbHelper.updatePostHashTagKeyWord(mAppVersionRes.getArrPostHashTagKeyWordItem());

                if (mAppVersionRes.getArrPostPopKeyWordItem().size() > 0)
                    mdbHelper.updatePostPopKeyWord(mAppVersionRes.getArrPostPopKeyWordItem());

                if (mAppVersionRes.getArrMusPopKeyWordItem().size() > 0)
                    mdbHelper.updateMusPopKeyWord(mAppVersionRes.getArrMusPopKeyWordItem());

                mdbHelper.close();

                TedPermission.with(this)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(
                                android.Manifest.permission.RECORD_AUDIO // 마이크
                                , android.Manifest.permission.READ_CONTACTS // 연락처
                                , android.Manifest.permission.ACCESS_FINE_LOCATION // 위치
                                , android.Manifest.permission.ACCESS_COARSE_LOCATION // 위치
                                , android.Manifest.permission.WRITE_EXTERNAL_STORAGE // 저장공간
                                , android.Manifest.permission.READ_PHONE_STATE // 전화
                                , android.Manifest.permission.CALL_PHONE // 전화
                        )
                        .check();
                break;
			case Constants.DIALOG_EXCEPTION_NON:
                setResult(Constants.RESULT_SUCCESS);
                finish();
				break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_NETWORK :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_NETWORK, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_APP_CLOSE :
                isFinish = false;
                break;
        }
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            sendBroadCast();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            sendBroadCast();
        }
    };

    private boolean isSendBroadCast = false;
    private void sendBroadCast() {
        if (!isSendBroadCast) {
            isSendBroadCast = true;
            LogUtil.e("▶▶▶▶▶▶▶▶▶▶ SEND BROADCAST ▶ QUERY_APP_VERSION");
            Intent intent = new Intent("QUERY_APP_VERSION");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            mHandler.sendEmptyMessageDelayed(Constants.DIALOG_EXCEPTION_NON, 3000);
            //mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NON);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 백 키를 터치한 경우
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!isFinish) {
                DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_app_close));
                isFinish = true;
                mHandler.sendEmptyMessageDelayed(Constants.DIALOG_APP_CLOSE, Constants.TIMEOUT_BACK_KEY);
                return false;
            } else {
                setResult(Constants.RESULT_FAIL);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
