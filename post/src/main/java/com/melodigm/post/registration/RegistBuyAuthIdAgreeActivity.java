package com.melodigm.post.registration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.agreement.AgreementServiceActivity;
import com.melodigm.post.common.Constants;

public class RegistBuyAuthIdAgreeActivity extends BaseActivity implements View.OnClickListener {
    private RelativeLayout rlRegistBuyAuthIdAgreeConfirmBtn;
    private ImageView ivRegistBuyAuthIdAgreeServiceBtn, ivRegistBuyAuthIdAgreePrivateInfoBtn, ivRegistBuyAuthIdAgreeAdultBtn;
    private boolean isAgreeService, isAgreePrivateInfo, isAgreeAdult;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_buy_auth_id_agree);

        mContext = this;

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_CLOSE, this);
        setPostHeaderTitle(getString(R.string.regist_buy_auth_id), false);

        rlRegistBuyAuthIdAgreeConfirmBtn = (RelativeLayout)findViewById(R.id.rlRegistBuyAuthIdAgreeConfirmBtn);
        ivRegistBuyAuthIdAgreeServiceBtn = (ImageView)findViewById(R.id.ivRegistBuyAuthIdAgreeServiceBtn);
        ivRegistBuyAuthIdAgreePrivateInfoBtn = (ImageView)findViewById(R.id.ivRegistBuyAuthIdAgreePrivateInfoBtn);
        ivRegistBuyAuthIdAgreeAdultBtn = (ImageView)findViewById(R.id.ivRegistBuyAuthIdAgreeAdultBtn);

        rlRegistBuyAuthIdAgreeConfirmBtn.setOnClickListener(this);
	}

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            // Header 닫기 onClick
            case R.id.btnHeaderClose:
                finish();
                break;
            // POST 서비스 이용약관 동의 문구 onClick
            case R.id.tvRegistBuyAuthIdAgreeServiceBtn:
                intent = new Intent(mContext, AgreementServiceActivity.class);
                intent.putExtra("AGREEMENT_TYPE", "SERVICE");
                startActivity(intent);
                break;
            // POST 서비스 이용약관 동의 버튼 onClick
            case R.id.rlRegistBuyAuthIdAgreeServiceBtn:
                isAgreeService = !isAgreeService;
                if (isAgreeService) {
                    ivRegistBuyAuthIdAgreeServiceBtn.setAlpha(1.0f);
                } else {
                    ivRegistBuyAuthIdAgreeServiceBtn.setAlpha(0.2f);
                }
                updateConfirmUI();
                break;
            // POST 개인정보 수집 및 이용 동의 문구 onClick
            case R.id.tvRegistBuyAuthIdAgreePrivateBtn:
                intent = new Intent(mContext, AgreementServiceActivity.class);
                intent.putExtra("AGREEMENT_TYPE", "PRIVATE");
                startActivity(intent);
                break;
            // POST 개인정보 수집 및 이용 동의 버튼 onClick
            case R.id.rlRegistBuyAuthIdAgreePrivateInfoBtn:
                isAgreePrivateInfo = !isAgreePrivateInfo;
                if (isAgreePrivateInfo) {
                    ivRegistBuyAuthIdAgreePrivateInfoBtn.setAlpha(1.0f);
                } else {
                    ivRegistBuyAuthIdAgreePrivateInfoBtn.setAlpha(0.2f);
                }
                updateConfirmUI();
                break;
            // 만 14세 이상의 개인입니다. onClick
            case R.id.rlRegistBuyAuthIdAgreeAdultBtn:
                isAgreeAdult = !isAgreeAdult;
                if (isAgreeAdult) {
                    ivRegistBuyAuthIdAgreeAdultBtn.setAlpha(1.0f);
                } else {
                    ivRegistBuyAuthIdAgreeAdultBtn.setAlpha(0.2f);
                }
                updateConfirmUI();
                break;
            // 확인 onClick
            case R.id.rlRegistBuyAuthIdAgreeConfirmBtn:
                intent = new Intent(mContext, RegistBuyAuthIdAuthActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void updateConfirmUI() {
        if (isAgreeService && isAgreePrivateInfo && isAgreeAdult) {
            rlRegistBuyAuthIdAgreeConfirmBtn.setAlpha(1.0f);
            rlRegistBuyAuthIdAgreeConfirmBtn.setEnabled(true);
        } else {
            rlRegistBuyAuthIdAgreeConfirmBtn.setAlpha(0.3f);
            rlRegistBuyAuthIdAgreeConfirmBtn.setEnabled(false);
        }
    }
}
