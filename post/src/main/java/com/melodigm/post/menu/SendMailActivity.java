package com.melodigm.post.menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetSendMailReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.util.HashMap;
import java.util.Map;

public class SendMailActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private EditText etSendMail;
    private TextView tvSendMailAddress;
    private RelativeLayout rlConfirmBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_send_mail);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.send_mail), false);

        etSendMail = (EditText)findViewById(R.id.etSendMail);
        tvSendMailAddress = (TextView)findViewById(R.id.tvSendMailAddress);
        tvSendMailAddress.setText(Constants.SERVICE_EMAIL);

        rlConfirmBtn = (RelativeLayout)findViewById(R.id.rlConfirmBtn);

        etSendMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                updateConfirmUI();
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        updateConfirmUI();
	}

    private void updateConfirmUI() {
        if (etSendMail.getText().toString().length() > 0) {
            rlConfirmBtn.setAlpha(1.0f);
            rlConfirmBtn.setEnabled(true);
        } else {
            rlConfirmBtn.setAlpha(0.2f);
            rlConfirmBtn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // 이메일 onClick
            case R.id.tvSendMailAddress:
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Constants.SERVICE_EMAIL));
                startActivity(intent);
                break;
            // 확인 onClick
            case R.id.rlConfirmBtn:
                getData(Constants.QUERY_SEND_MAIL);
                break;
            // 안내 확인 onClick
            case R.id.btnInfoConfirm:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                finish();
                break;
        }
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
        if (queryType == Constants.QUERY_SEND_MAIL) {
            thread = getSendMailThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getSendMailThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetSendMailReq getSendMailReq = new GetSendMailReq();
                    getSendMailReq.setCONT(etSendMail.getText().toString());
                    request.getSendMail(getSendMailReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_SEND_MAIL);
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
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            // 보관함 생성 / 수정 후 Handler
            case Constants.QUERY_SEND_MAIL:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, this, mContext.getResources().getString(R.string.dialog_send_mail));
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
}
