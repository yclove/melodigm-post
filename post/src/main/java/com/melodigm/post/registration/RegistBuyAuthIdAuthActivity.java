package com.melodigm.post.registration;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.PostDialog;

public class RegistBuyAuthIdAuthActivity extends BaseActivity {
    private EditText etAuthAdultName, etAuthAdultBirth, etAuthAdultForeignerNo, etAuthAdultPhone;
    private LinearLayout llAuthAdultMaleBtn, llAuthAdultFemaleBtn, llAuthAdultForeignBtn, llAuthAdultPhonePrefixBtn;
    private ImageView ivAuthAdultMaleCircle, ivAuthAdultFemaleCircle;
    private TextView tvAuthAdultForeignText, tvNewsAgency, tvCallAuthNoBtn;
    private RelativeLayout rlAuthAdultConfirmBtn;
    private boolean isForeigner = false;
    private String newsAgency;
    private String gender;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_buy_auth_id_auth);
        mContext = this;
        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getString(R.string.regist_buy_auth_id), false);

        etAuthAdultName = (EditText)findViewById(R.id.etAuthAdultName);
        etAuthAdultBirth = (EditText)findViewById(R.id.etAuthAdultBirth);
        etAuthAdultForeignerNo = (EditText)findViewById(R.id.etAuthAdultForeignerNo);
        ivAuthAdultMaleCircle = (ImageView)findViewById(R.id.ivAuthAdultMaleCircle);
        ivAuthAdultFemaleCircle = (ImageView)findViewById(R.id.ivAuthAdultFemaleCircle);
        tvAuthAdultForeignText = (TextView)findViewById(R.id.tvAuthAdultForeignText);
        tvNewsAgency = (TextView)findViewById(R.id.tvNewsAgency);
        etAuthAdultPhone = (EditText)findViewById(R.id.etAuthAdultPhone);

        llAuthAdultMaleBtn = (LinearLayout)findViewById(R.id.llAuthAdultMaleBtn);
        llAuthAdultMaleBtn.setOnClickListener(onClickListener);
        llAuthAdultFemaleBtn = (LinearLayout)findViewById(R.id.llAuthAdultFemaleBtn);
        llAuthAdultFemaleBtn.setOnClickListener(onClickListener);
        llAuthAdultForeignBtn = (LinearLayout)findViewById(R.id.llAuthAdultForeignBtn);
        llAuthAdultForeignBtn.setOnClickListener(onClickListener);
        llAuthAdultPhonePrefixBtn = (LinearLayout)findViewById(R.id.llAuthAdultPhonePrefixBtn);
        llAuthAdultPhonePrefixBtn.setOnClickListener(onClickListener);
        tvCallAuthNoBtn = (TextView)findViewById(R.id.tvCallAuthNoBtn);
        tvCallAuthNoBtn.setOnClickListener(onClickListener);
        rlAuthAdultConfirmBtn = (RelativeLayout) findViewById(R.id.rlAuthAdultConfirmBtn);
        rlAuthAdultConfirmBtn.setOnClickListener(onClickListener);

        ivAuthAdultMaleCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF000000")));
        ivAuthAdultFemaleCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF000000")));

        etAuthAdultName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                updateCallAuthNoUI();
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

        etAuthAdultBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                updateCallAuthNoUI();
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

        etAuthAdultForeignerNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                updateCallAuthNoUI();
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

        etAuthAdultPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                updateCallAuthNoUI();
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

        updateCallAuthNoUI();
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // 성별 > 남 onClick
                case R.id.llAuthAdultMaleBtn:
                    gender = "M";
                    llAuthAdultMaleBtn.setAlpha(1.0f);
                    llAuthAdultFemaleBtn.setAlpha(0.3f);
                    break;
                // 성별 > 여 onClick
                case R.id.llAuthAdultFemaleBtn:
                    gender = "F";
                    llAuthAdultMaleBtn.setAlpha(0.3f);
                    llAuthAdultFemaleBtn.setAlpha(1.0f);
                    break;
                // 내국인 / 외국인 onClick
                case R.id.llAuthAdultForeignBtn:
                    isForeigner = !isForeigner;
                    updateForeignerUI();
                    break;
                // 휴대폰 인증 > 선택 onClick
                case R.id.llAuthAdultPhonePrefixBtn:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NEWS_AGENCY, onClickListener);
                    mPostDialog.show();
                    break;
                // 휴대폰 인증 > 선택 > 통신사 onClick
                case R.id.llNewsAgencySkt:
                case R.id.llNewsAgencyKt:
                case R.id.llNewsAgencyLg:
                case R.id.llNewsAgencySaving:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    newsAgency = (String)v.getTag();
                    if (CommonUtil.isNotNull(newsAgency)) {
                        switch (newsAgency) {
                            case "SKT":
                                tvNewsAgency.setText(getString(R.string.news_agency_skt));
                                break;
                            case "KT":
                                tvNewsAgency.setText(getString(R.string.news_agency_kt));
                                break;
                            case "LG":
                                tvNewsAgency.setText(getString(R.string.news_agency_lg));
                                break;
                            case "Saving":
                                tvNewsAgency.setText(getString(R.string.news_agency_saving));
                                break;
                        }
                    }
                    updateCallAuthNoUI();
                    break;
                // 인증번호요청 onClick
                case R.id.tvCallAuthNoBtn:
                    DeviceUtil.showToast(mContext, "인증번호요청 onClick");
                    break;
                // 확인 onClick
                case R.id.rlAuthAdultConfirmBtn:
                    DeviceUtil.showToast(mContext, "확인 onClick");
                    break;
            }
        }
    };

    private void updateForeignerUI() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if (isForeigner) {
            etAuthAdultBirth.setVisibility(View.GONE);
            etAuthAdultForeignerNo.setVisibility(View.VISIBLE);
            tvAuthAdultForeignText.setText(getString(R.string.foreigner));
        } else {
            etAuthAdultBirth.setVisibility(View.VISIBLE);
            etAuthAdultForeignerNo.setVisibility(View.GONE);
            tvAuthAdultForeignText.setText(getString(R.string.local));
        }
    }

    private void updateCallAuthNoUI() {
        if (CommonUtil.isNotNull(etAuthAdultName.getText().toString()) && ((!isForeigner && etAuthAdultBirth.getText().toString().length() == 6) || (isForeigner && etAuthAdultForeignerNo.getText().toString().length() == 13)) && CommonUtil.isNotNull(gender) && CommonUtil.isNotNull(newsAgency) && etAuthAdultPhone.getText().toString().length() >= 10) {
            tvCallAuthNoBtn.setAlpha(1.0f);
            tvCallAuthNoBtn.setEnabled(true);
        } else {
            tvCallAuthNoBtn.setAlpha(0.3f);
            tvCallAuthNoBtn.setEnabled(false);
        }
    }
}
