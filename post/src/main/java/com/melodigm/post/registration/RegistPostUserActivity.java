package com.melodigm.post.registration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.facebook.*;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.analytics.HitBuilders;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.BaseApplication;
import com.melodigm.post.R;
import com.melodigm.post.agreement.AgreementServiceActivity;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.RegPostUserReq;
import com.melodigm.post.protocol.data.RegPostUserRes;
import com.melodigm.post.protocol.data.SetSnsAccountRestoreReq;
import com.melodigm.post.protocol.data.SetSnsAccountRestoreRes;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.*;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.hashtaglink.AgreementTag;
import jp.line.android.sdk.LineSdkContext;
import jp.line.android.sdk.LineSdkContextManager;
import jp.line.android.sdk.login.LineAuthManager;
import jp.line.android.sdk.login.LineLoginFuture;
import jp.line.android.sdk.login.LineLoginFutureListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegistPostUserActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private RegPostUserRes regPostUserRes;
    private ImageView ivRotate;
    private TextView tvUserBirthYear, tvUserBirthYearBtn, tvAgreeAllAgreement;
    private ToggleButton tgMaleBtn, tgFemaleBtn;
    private LinearLayout llBirthYearGenderLayout, llWelcomeLayout, llRegAlreadyBtn, llRegNextBtn, llRegActionBtn;
    private PostDialog mChoiceYearDialog;
    private String mUserBirthYear;
    private String mUserGender;
    private LetterSpacingTextView lstvWelcomeToPost;

    // 데이터 복원
    private SetSnsAccountRestoreRes setSnsAccountRestoreRes;
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
    private LinearLayout llRestoreLayout, llRestoreConfirmBtn;

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

        // 라인
        mLineSdkContext = LineSdkContextManager.getSdkContext();
        mLineAuthManager = mLineSdkContext.getAuthManager();
        mLineAuthManager.logout();

        // 인스타그램
        mInstagramApp = new InstagramApp(this, Constants.INSTAGRAM_CLIENT_ID, Constants.INSTAGRAM_CLIENT_SECRET, Constants.INSTAGRAM_CALLBACK_URL);
        mInstagramApp.setListener(mInstagramListener);
        if (mInstagramApp.hasAccessToken()) mInstagramApp.resetAccessToken();

        setContentView(R.layout.activity_regist_post_user);

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
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        btnHeaderBack.setVisibility(View.GONE);

        // 로고
        ivRotate = (ImageView) findViewById(R.id.ivRotate);
        RotateAnimation rotate = new RotateAnimation(180, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setRepeatCount(Animation.INFINITE);
        //ivRotate.startAnimation(rotate);

        // 생년 및 성별 화면
        llBirthYearGenderLayout = (LinearLayout) findViewById(R.id.llBirthYearGenderLayout);

        // 생년 및 성별 화면 > 태어난 해
        tvUserBirthYear = (TextView) findViewById(R.id.tvUserBirthYear);
        tvUserBirthYearBtn = (TextView) findViewById(R.id.tvUserBirthYearBtn);
        tvUserBirthYearBtn.setOnClickListener(onClickListener);

        // 생년 및 성별 화면 > 성별 > 남
        tgMaleBtn = (ToggleButton) findViewById(R.id.tgMaleBtn);
        tgMaleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tgFemaleBtn.setChecked(!tgMaleBtn.isChecked());
                updateNextUI();
            }
        });

        // 생년 및 성별 화면 > 성별 > 여
        tgFemaleBtn = (ToggleButton) findViewById(R.id.tgFemaleBtn);
        tgFemaleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tgMaleBtn.setChecked(!tgFemaleBtn.isChecked());
                updateNextUI();
            }
        });

        // 생년 및 성별 화면 > 이전에 이용하던 누군가 입니다
        llRegAlreadyBtn = (LinearLayout) findViewById(R.id.llRegAlreadyBtn);
        llRegAlreadyBtn.setOnClickListener(onClickListener);

        // 생년 및 성별 화면 > 다음
        llRegNextBtn = (LinearLayout) findViewById(R.id.llRegNextBtn);
        llRegNextBtn.setOnClickListener(onClickListener);

        updateNextUI();

        // Welcome 화면
        llWelcomeLayout = (LinearLayout) findViewById(R.id.llWelcomeLayout);

        lstvWelcomeToPost = (LetterSpacingTextView) findViewById(R.id.lstvWelcomeToPost);
        lstvWelcomeToPost.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvWelcomeToPost.setText(getString(R.string.welcome_to_post));

        // Welcome 화면 > 시작하기
        llRegActionBtn = (LinearLayout) findViewById(R.id.llRegActionBtn);
        llRegActionBtn.setOnClickListener(onClickListener);

        tvAgreeAllAgreement = (TextView) findViewById(R.id.tvAgreeAllAgreement);
        final Pattern usePattern = Pattern.compile("이용약관,");
        final Pattern privatePattern = Pattern.compile("개인정보보호정책,");
        final Pattern locationPattern = Pattern.compile("위치정보이용약관");
        final Matcher useMatcher = usePattern.matcher(tvAgreeAllAgreement.getText().toString());
        final Matcher privateMatcher = privatePattern.matcher(tvAgreeAllAgreement.getText().toString());
        final Matcher locationMatcher = locationPattern.matcher(tvAgreeAllAgreement.getText().toString());

        SpannableString spannableString = new SpannableString(tvAgreeAllAgreement.getText());
        while (useMatcher.find())
            spannableString.setSpan(new AgreementTag(mContext, mHandler, "SERVICE"), useMatcher.start(), useMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        while (privateMatcher.find())
            spannableString.setSpan(new AgreementTag(mContext, mHandler, "SCHEME"), privateMatcher.start(), privateMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        while (locationMatcher.find())
            spannableString.setSpan(new AgreementTag(mContext, mHandler, "LOCATION"), locationMatcher.start(), locationMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvAgreeAllAgreement.setText(spannableString);
        tvAgreeAllAgreement.setMovementMethod(LinkMovementMethod.getInstance());

        // 데이터 복원 화면
        llRestoreLayout = (LinearLayout) findViewById(R.id.llRestoreLayout);
        llRestoreConfirmBtn = (LinearLayout) findViewById(R.id.llRestoreConfirmBtn);
        ivSyncFacebookCheckImage = (ImageView) findViewById(R.id.ivSyncFacebookCheckImage);
        ivSyncTwitterCheckImage = (ImageView) findViewById(R.id.ivSyncTwitterCheckImage);
        ivSyncInstagramCheckImage = (ImageView) findViewById(R.id.ivSyncInstagramCheckImage);
        ivSyncLineCheckImage = (ImageView) findViewById(R.id.ivSyncLineCheckImage);

        fbLoginButton = (LoginButton) findViewById(R.id.fbLoginButton);
        fbLoginButton.setReadPermissions("public_profile", "email", "user_friends");
        fbLoginButton.registerCallback(mFacebookCallbackManager, mFacebookCallback);

        updateConfirmUI();

        llBirthYearGenderLayout.setVisibility(View.VISIBLE);
        llWelcomeLayout.setVisibility(View.GONE);
        llRestoreLayout.setVisibility(View.GONE);
        btnHeaderBack.setVisibility(View.GONE);
    }

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            ACCOUNT_ID = loginResult.getAccessToken().getUserId();
            ACCOUNT_AUTH_TYPE = Constants.REQUEST_ACCOUNT_AUTH_TYPE_FACEBOOK;
            LogUtil.e("페이스북 ▶ ACCOUNT_ID(" + ACCOUNT_ID + "), ACCOUNT_AUTH_TYPE(" + ACCOUNT_AUTH_TYPE + ")");

            LoginManager.getInstance().logOut();

            if (CommonUtil.isNotNull(ACCOUNT_ID) && CommonUtil.isNotNull(ACCOUNT_AUTH_TYPE)) {
                getData(Constants.QUERY_SNS_ACCOUNT_RESTORE);
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
                    LogUtil.e("라인 ▶ ACCOUNT_ID(" + ACCOUNT_ID + "), ACCOUNT_AUTH_TYPE(" + ACCOUNT_AUTH_TYPE + ")");

                    mLineAuthManager.logout();

                    if (CommonUtil.isNotNull(ACCOUNT_ID) && CommonUtil.isNotNull(ACCOUNT_AUTH_TYPE)) {
                        getData(Constants.QUERY_SNS_ACCOUNT_RESTORE);
                    }
                    break;
                case CANCELED:
                    LogUtil.e("라인 ▶ CANCELED");
                    break;
                case FAILED:
                    LogUtil.e("라인 ▶ FAILED");
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
                getData(Constants.QUERY_SNS_ACCOUNT_RESTORE);
            }
        }

        @Override
        public void onFail(String error) {
            LogUtil.e("인스타그램 ▶ onFail : " + error);
        }
    };

    private void getData(int queryType) {
        if (!isFinishing()) {
            if (mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        if (queryType == Constants.QUERY_SNS_ACCOUNT_RESTORE) {
            new Thread(setSnsAccountRestoreThread).start();
        }
    }

    private Runnable setSnsAccountRestoreThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(mContext);
            try {
                SetSnsAccountRestoreReq setSnsAccountRestoreReq = new SetSnsAccountRestoreReq();
                setSnsAccountRestoreReq.setACCOUNT_ID(ACCOUNT_ID);
                setSnsAccountRestoreReq.setACCOUNT_AUTH_TYPE(ACCOUNT_AUTH_TYPE);
                setSnsAccountRestoreReq.setPUSH_ID(SPUtil.getSharedPreference(mContext, Constants.SP_PUSH_ID));
                setSnsAccountRestoreRes = request.setSnsAccountRestore(setSnsAccountRestoreReq);

                if (CommonUtil.isNull(setSnsAccountRestoreRes.getUAI())) {
                    mPOSTException = new POSTException("0000", getResources().getString(R.string.exception_request_uai));
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                } else {
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
                            .setAction(Constants.GA_ACTION_SNS_RESTORE)
                            .setLabel(snsName)
                            .build());
                    SPUtil.setSharedPreference(mContext, Constants.SP_USER_ID, setSnsAccountRestoreRes.getUAI());
                    SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_ID, ACCOUNT_ID);
                    SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE, ACCOUNT_AUTH_TYPE);
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NON);
                }
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

    private void updateConfirmUI() {
        if (CommonUtil.isNull(snsType)) {
            llRestoreConfirmBtn.setAlpha(0.2f);
            llRestoreConfirmBtn.setEnabled(false);
        } else {
            llRestoreConfirmBtn.setAlpha(1.0f);
            llRestoreConfirmBtn.setEnabled(true);
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
    public void onClick(View v) {
        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // Footer 확인 onClick
            case R.id.llRestoreConfirmBtn:
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
                        LineLoginFuture loginFuture = mLineAuthManager.login((Activity) mContext);
                        loginFuture.addFutureListener(mLineLoginFutureListener);
                        break;
                }
                break;
            // 페이스북 / 트위터 / 인스타그램 / 라인 onClick
            case R.id.llSyncFacebookBtn:
            case R.id.llSyncTwitterBtn:
            case R.id.llSyncInstagramBtn:
            case R.id.llSyncLineBtn:
                snsType = (String) v.getTag();
                updateUI();
                break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    llBirthYearGenderLayout.setVisibility(View.VISIBLE);
                    llWelcomeLayout.setVisibility(View.GONE);
                    llRestoreLayout.setVisibility(View.GONE);
                    btnHeaderBack.setVisibility(View.GONE);
                    break;
                // 선택 onClick
                case R.id.tvUserBirthYearBtn:
                    mChoiceYearDialog = null;
                    mChoiceYearDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CHOICE_YEAR, onClickListener, mUserBirthYear);
                    mChoiceYearDialog.show();
                    break;
                // 태어난 해 Dialog > 확인 onClick
                case R.id.llSelectYearConfirmBtn:
                    if (mChoiceYearDialog != null) mChoiceYearDialog.dismiss();
                    mUserBirthYear = String.valueOf((int) v.getTag());
                    tvUserBirthYear.setText(mUserBirthYear);
                    tvUserBirthYearBtn.setAlpha(1.0f);
                    updateNextUI();
                    break;
                case R.id.llRegActionBtn:
                    if (chkeckData()) getData();
                    break;
                // 이전에 이용하던 누군가 입니다 onClick
                case R.id.llRegAlreadyBtn:
                    llBirthYearGenderLayout.setVisibility(View.GONE);
                    llRestoreLayout.setVisibility(View.VISIBLE);
                    btnHeaderBack.setVisibility(View.VISIBLE);
                    break;
                // 다음 onClick
                case R.id.llRegNextBtn:
                    llBirthYearGenderLayout.setVisibility(View.GONE);
                    llWelcomeLayout.setVisibility(View.VISIBLE);
                    btnHeaderBack.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private void updateNextUI() {
        if (CommonUtil.isNull(mUserBirthYear) || (!tgMaleBtn.isChecked() && !tgFemaleBtn.isChecked())) {
            llRegNextBtn.setAlpha(0.2f);
            llRegNextBtn.setEnabled(false);
        } else {
            llRegNextBtn.setAlpha(1.0f);
            llRegNextBtn.setEnabled(true);
        }
    }

    private boolean chkeckData() {
        boolean isResponse = true;
        if (CommonUtil.isNull(mUserBirthYear)) {
            isResponse = false;
            DeviceUtil.showToast(mContext, getResources().getString(R.string.required_birth_year));
        }
        return isResponse;
    }

    private void getData() {
        if (!isFinishing()) {
            if (mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        new Thread(registThread).start();
    }

    private Runnable registThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(RegistPostUserActivity.this);
            try {
                RegPostUserReq regPostUserReq = new RegPostUserReq();
                regPostUserReq.setPUSH_ID(SPUtil.getSharedPreference(mContext, Constants.SP_PUSH_ID));
                regPostUserReq.setBIRTHDATE(mUserBirthYear);

                mUserGender = (tgMaleBtn.isChecked()) ? "M" : "F";

                regPostUserReq.setGENDER(mUserGender);
                regPostUserRes = request.regPostUser(regPostUserReq);

                SPUtil.setSharedPreference(mContext, Constants.SP_USER_ID, regPostUserRes.getUAI());
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
    public void handleMessage(Message msg) {
        if (mProgressDialog != null) {
            mProgressDialog.dissDialog();
        }

        Intent intent;

        switch (msg.what) {
            case Constants.DIALOG_EXCEPTION_NON:
                setResult(Constants.RESULT_SUCCESS);
                finish();
                break;
            // 동의 타입 별 onClick
            case Constants.QUERY_AGREEMENT_DATA:
                String agreementType = msg.getData().getString("AGREEMENT_TYPE");
                intent = new Intent(mContext, AgreementServiceActivity.class);
                intent.putExtra("AGREEMENT_TYPE", agreementType);
                startActivity(intent);
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_APP_CLOSE:
                isFinish = false;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 백 키를 터치한 경우
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isFinish) {
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
