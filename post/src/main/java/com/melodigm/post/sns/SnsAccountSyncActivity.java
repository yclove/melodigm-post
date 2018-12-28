package com.melodigm.post.sns;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.BaseApplication;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.SetSnsAccountSyncReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.InstagramApp;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;

import jp.line.android.sdk.LineSdkContext;
import jp.line.android.sdk.LineSdkContextManager;
import jp.line.android.sdk.login.LineAuthManager;
import jp.line.android.sdk.login.LineLoginFuture;
import jp.line.android.sdk.login.LineLoginFutureListener;

public class SnsAccountSyncActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private CallbackManager mFacebookCallbackManager = null;
    private AccessTokenTracker mFacebookAccessTokenTracker = null;
    private int mFacebookRequestCode, mTwitterRequestCode;
    private LoginButton fbLoginButton = null;
    private LineSdkContext mLineSdkContext;
    private LineAuthManager mLineAuthManager;
    private InstagramApp mInstagramApp;

    private String ACCOUNT_ID = "";
    private String ACCOUNT_AUTH_TYPE = "";
    private String snsType;

    private ImageView ivSyncFacebookCheckImage, ivSyncTwitterCheckImage, ivSyncInstagramCheckImage, ivSyncLineCheckImage;
    private RelativeLayout rlConfirmBtn;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        // 페이스북
        FacebookSdk.sdkInitialize(getApplicationContext());
        mFacebookRequestCode = CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode();

        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logOut();

        mFacebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                LogUtil.e("페이스북 ▶ Current Token : " + currentAccessToken);
                LoginManager.getInstance().logOut();
            }
        };
        mFacebookAccessTokenTracker.startTracking();

        // 네이버라인
        mLineSdkContext = LineSdkContextManager.getSdkContext();
        mLineAuthManager = mLineSdkContext.getAuthManager();
        mLineAuthManager.logout();

        // 인스타그램
        mInstagramApp = new InstagramApp(this, Constants.INSTAGRAM_CLIENT_ID, Constants.INSTAGRAM_CLIENT_SECRET, Constants.INSTAGRAM_CALLBACK_URL);
        mInstagramApp.setListener(mInstagramListener);
        if (mInstagramApp.hasAccessToken()) mInstagramApp.resetAccessToken();

        setContentView(R.layout.activity_sns_account_sync);

        setDisplay();
	}

    private void connectOrDisconnectUser() {
        if (mInstagramApp.hasAccessToken()) {
            mInstagramApp.resetAccessToken();
        } else {
            mInstagramApp.authorize();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mFacebookAccessTokenTracker.stopTracking();
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.title_sns_account_sync), false);

        rlConfirmBtn = (RelativeLayout) findViewById(R.id.rlConfirmBtn);
        ivSyncFacebookCheckImage = (ImageView)findViewById(R.id.ivSyncFacebookCheckImage);
        ivSyncTwitterCheckImage = (ImageView)findViewById(R.id.ivSyncTwitterCheckImage);
        ivSyncInstagramCheckImage = (ImageView)findViewById(R.id.ivSyncInstagramCheckImage);
        ivSyncLineCheckImage = (ImageView)findViewById(R.id.ivSyncLineCheckImage);

        fbLoginButton = (LoginButton)findViewById(R.id.fbLoginButton);
        fbLoginButton.setReadPermissions("public_profile", "email", "user_friends");
        fbLoginButton.registerCallback(mFacebookCallbackManager, mFacebookCallback);

        updateConfirmUI();
    }

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            ACCOUNT_ID = loginResult.getAccessToken().getUserId();
            ACCOUNT_AUTH_TYPE = Constants.REQUEST_ACCOUNT_AUTH_TYPE_FACEBOOK;
            LogUtil.e("페이스북 ▶ ACCOUNT_ID(" + ACCOUNT_ID + "), ACCOUNT_AUTH_TYPE(" + ACCOUNT_AUTH_TYPE + ")");

            LoginManager.getInstance().logOut();

            if (CommonUtil.isNotNull(ACCOUNT_ID) && CommonUtil.isNotNull(ACCOUNT_AUTH_TYPE)) {
                getData(Constants.QUERY_SNS_ACCOUNT_SYNC);
            }
        }

        @Override
        public void onCancel() {
            LogUtil.e("페이스북 ▶ onCancel");
        }

        @Override
        public void onError(FacebookException e) {
            LogUtil.e("페이스북 ▶ onError : " + e.getMessage());
        }
    };

    private LineLoginFutureListener mLineLoginFutureListener = new LineLoginFutureListener() {
        @Override
        public void loginComplete(LineLoginFuture future) {
            switch (future.getProgress()) {
                case SUCCESS:
                    jp.line.android.sdk.model.AccessToken token = future.getAccessToken();
                    ACCOUNT_ID = token.mid;
                    ACCOUNT_AUTH_TYPE = Constants.REQUEST_ACCOUNT_AUTH_TYPE_NAVER_LINE;
                    LogUtil.e("네이버라인 ▶ ACCOUNT_ID(" + ACCOUNT_ID + "), ACCOUNT_AUTH_TYPE(" + ACCOUNT_AUTH_TYPE + ")");

                    mLineAuthManager.logout();

                    if (CommonUtil.isNotNull(ACCOUNT_ID) && CommonUtil.isNotNull(ACCOUNT_AUTH_TYPE)) {
                        getData(Constants.QUERY_SNS_ACCOUNT_SYNC);
                    }
                    break;
                case CANCELED:
                    LogUtil.e("네이버라인 ▶ CANCELED");
                    break;
                case FAILED:
                    LogUtil.e("네이버라인 ▶ FAILED");
                    break;
            }
        }
    };

    private InstagramApp.OAuthAuthenticationListener mInstagramListener = new InstagramApp.OAuthAuthenticationListener() {
        @Override
        public void onSuccess() {
            ACCOUNT_ID = mInstagramApp.getId();
            ACCOUNT_AUTH_TYPE = Constants.REQUEST_ACCOUNT_AUTH_TYPE_INSTAGRAM;
            LogUtil.e("인스타그램 ▶ ACCOUNT_ID(" + ACCOUNT_ID + "), ACCOUNT_AUTH_TYPE(" + ACCOUNT_AUTH_TYPE + ")");

            mInstagramApp.resetAccessToken();

            if (CommonUtil.isNotNull(ACCOUNT_ID) && CommonUtil.isNotNull(ACCOUNT_AUTH_TYPE)) {
                getData(Constants.QUERY_SNS_ACCOUNT_SYNC);
            }
        }
        @Override
        public void onFail(String error) {
            LogUtil.e("인스타그램 ▶ onFail : " + error);
        }
    };

    private void getData(int queryType) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        if (queryType == Constants.QUERY_SNS_ACCOUNT_SYNC) {
            new Thread(setSnsAccountSyncThread).start();
        }
    }

    private Runnable setSnsAccountSyncThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(mContext);
            try {
                SetSnsAccountSyncReq setSnsAccountSyncReq = new SetSnsAccountSyncReq();
                setSnsAccountSyncReq.setACCOUNT_ID(ACCOUNT_ID);
                setSnsAccountSyncReq.setACCOUNT_AUTH_TYPE(ACCOUNT_AUTH_TYPE);
                request.setSnsAccountSync(setSnsAccountSyncReq);
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NON);
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
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // Footer 확인 onClick
            case R.id.rlConfirmBtn:
                switch (snsType) {
                    case "facebook":
                        fbLoginButton.performClick();
                        break;
                    case "twitter":
                        break;
                    case "instagram":
                        connectOrDisconnectUser();
                        break;
                    case "line":
                        LineLoginFuture loginFuture = mLineAuthManager.login((Activity)mContext);
                        loginFuture.addFutureListener(mLineLoginFutureListener);
                        break;
                }
                break;
            // 페이스북 / 트위터 / 인스타그램 / 네이버라인 onClick
            case R.id.llSyncFacebookBtn:
            case R.id.llSyncTwitterBtn:
            case R.id.llSyncInstagramBtn:
            case R.id.llSyncLineBtn:
                snsType = (String)v.getTag();
                updateUI();
                break;
        }
    }

    private void updateConfirmUI() {
        if (CommonUtil.isNull(snsType)) {
            rlConfirmBtn.setAlpha(0.2f);
            rlConfirmBtn.setEnabled(false);
        } else {
            rlConfirmBtn.setAlpha(1.0f);
            rlConfirmBtn.setEnabled(true);
        }
    }

    private void updateUI() {
        ivSyncFacebookCheckImage.setVisibility(View.GONE);
        ivSyncTwitterCheckImage.setVisibility(View.GONE);
        ivSyncInstagramCheckImage.setVisibility(View.GONE);
        ivSyncLineCheckImage.setVisibility(View.GONE);

        switch (snsType) {
            case "facebook":
                ivSyncFacebookCheckImage.setVisibility(View.VISIBLE);
                break;
            case "twitter":
                ivSyncTwitterCheckImage.setVisibility(View.VISIBLE);
                break;
            case "instagram":
                ivSyncInstagramCheckImage.setVisibility(View.VISIBLE);
                break;
            case "line":
                ivSyncLineCheckImage.setVisibility(View.VISIBLE);
                break;
        }

        updateConfirmUI();
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            case Constants.DIALOG_EXCEPTION_NON:
                String snsName = "";
                switch (ACCOUNT_AUTH_TYPE) {
                    case Constants.REQUEST_ACCOUNT_AUTH_TYPE_FACEBOOK:
                        snsName = getString(R.string.facebook);
                        break;
                    case Constants.REQUEST_ACCOUNT_AUTH_TYPE_TWITTER:
                        snsName = getString(R.string.twitter);
                        break;
                    case Constants.REQUEST_ACCOUNT_AUTH_TYPE_INSTAGRAM:
                        snsName = getString(R.string.instagram);
                        break;
                    case Constants.REQUEST_ACCOUNT_AUTH_TYPE_NAVER_LINE:
                        snsName = getString(R.string.line);
                        break;
                }
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Constants.GA_CATEGORY_SNS)
                        .setAction(Constants.GA_ACTION_SNS_SYNC)
                        .setLabel(snsName)
                        .build());
                SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_ID, ACCOUNT_ID);
                SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE, ACCOUNT_AUTH_TYPE);
                setResult(Constants.RESULT_SUCCESS);
                finish();
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
            case Constants.DIALOG_EXCEPTION_NETWORK :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_NETWORK, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        // 페이스북 인증 후 onActivityResult
        if (requestCode == mFacebookRequestCode) {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
