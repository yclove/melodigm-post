package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.registration.RegistBuyAuthIdAgreeActivity;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.util.HashMap;

public class BuyUseCouponActivity extends BaseActivity implements IOnHandlerMessage {
    private TextView tvBuyUseCouponBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_buy_usecoupon);
        
        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
	}

	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getString(R.string.buy_usecoupon), false);

        tvBuyUseCouponBtn = (TextView)findViewById(R.id.tvBuyUseCouponBtn);
        tvBuyUseCouponBtn.setOnClickListener(onClickListener);
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // 구매 인증 아이디 신규 등록 onClick
                case R.id.tvBuyUseCouponBtn:
                    Intent intent = new Intent(mContext, RegistBuyAuthIdAgreeActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        Bundle data = msg.getData();

        switch(msg.what) {
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
