package com.melodigm.post.registration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.PostActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetInitInfoRes;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InitPostActivity extends BaseActivity implements IOnHandlerMessage {
    private GetInitInfoRes mGetInitInfoRes;
    private TextView tvActStateCnt;
    private RelativeLayout rlAgreeInitDataBtn, rlInitPostConfirmBtn;
    private ImageView ivAgreeInitDataBtn;
    private boolean isAgreeInitData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_post);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getString(R.string.init_service), false);

        rlAgreeInitDataBtn = (RelativeLayout)findViewById(R.id.rlAgreeInitDataBtn);
        rlAgreeInitDataBtn.setOnClickListener(onClickListener);

        rlInitPostConfirmBtn = (RelativeLayout)findViewById(R.id.rlInitPostConfirmBtn);
        rlInitPostConfirmBtn.setOnClickListener(onClickListener);

        tvActStateCnt = (TextView)findViewById(R.id.tvActStateCnt);
        ivAgreeInitDataBtn = (ImageView)findViewById(R.id.ivAgreeInitDataBtn);

        updateConfirmUI();

        getData(Constants.QUERY_INIT_INFO);
    }

    private void getData(int queryType, Object... args) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_INIT_INFO) {
            thread = initInfoThread();
        } else if (queryType == Constants.QUERY_INIT) {
            thread = initThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread initInfoThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    mGetInitInfoRes = request.getInitInfo();
                    mHandler.sendEmptyMessage(Constants.QUERY_INIT_INFO);
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
        });
        thread.start();
        return thread;
    }

    public RunnableThread initThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);

                try {
                    request.initPost();
                    mHandler.sendEmptyMessage(Constants.QUERY_INIT);
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
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            case Constants.QUERY_INIT_INFO:
                tvActStateCnt.setText(NumberFormat.getNumberInstance(Locale.KOREA).format(mGetInitInfoRes.getACT_REG_CNT()));
                break;
            case Constants.QUERY_INIT:
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_ID, "");
                SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_ID, "");
                SPUtil.setSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE, "");

                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                mdbHelper.deleteAllRecentSearchWords();
                mdbHelper.deleteAllOstPlayList();
                mdbHelper.close();

                File file = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE);
                DeviceUtil.removeFiles(file);

                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onClickListener, mContext.getResources().getString(R.string.dialog_init_post));
                mPostDialog.show();
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 뒤로가기
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // 모든 내용을 확인하였으며 동의합니다 버튼 onClick
                case R.id.rlAgreeInitDataBtn:
                    isAgreeInitData = !isAgreeInitData;
                    if (isAgreeInitData) {
                        ivAgreeInitDataBtn.setAlpha(1.0f);
                    } else {
                        ivAgreeInitDataBtn.setAlpha(0.2f);
                    }
                    updateConfirmUI();
                    break;
                // 초기화 확인
                case R.id.rlInitPostConfirmBtn:
                    getData(Constants.QUERY_INIT);
                    break;
                // 안내 타입의 확인
                case R.id.btnInfoConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("HOME_NEWINTENT_MOVE", Constants.QUERY_REGIST_USER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    private void updateConfirmUI() {
        if (isAgreeInitData) {
            rlInitPostConfirmBtn.setAlpha(1.0f);
            rlInitPostConfirmBtn.setEnabled(true);
        } else {
            rlInitPostConfirmBtn.setAlpha(0.3f);
            rlInitPostConfirmBtn.setEnabled(false);
        }
    }
}
