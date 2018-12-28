package com.melodigm.post.auth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.widget.ColorCircleDrawable;

public class AuthAdultActivity extends BaseActivity implements View.OnClickListener {
    private EditText etAuthAdultBirth, etAuthAdultForeignerNo;
    private LinearLayout llAuthAdultMaleBtn, llAuthAdultFemaleBtn;
    private ImageView ivAuthAdultMaleCircle, ivAuthAdultFemaleCircle;
    private TextView tvAuthAdultForeignText;
    private boolean isForeigner = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_adult);
        mContext = this;
        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.auth_adult), false);

        etAuthAdultBirth = (EditText)findViewById(R.id.etAuthAdultBirth);
        etAuthAdultForeignerNo = (EditText)findViewById(R.id.etAuthAdultForeignerNo);
        llAuthAdultMaleBtn = (LinearLayout)findViewById(R.id.llAuthAdultMaleBtn);
        llAuthAdultFemaleBtn = (LinearLayout)findViewById(R.id.llAuthAdultFemaleBtn);
        ivAuthAdultMaleCircle = (ImageView)findViewById(R.id.ivAuthAdultMaleCircle);
        ivAuthAdultFemaleCircle = (ImageView)findViewById(R.id.ivAuthAdultFemaleCircle);
        tvAuthAdultForeignText = (TextView) findViewById(R.id.tvAuthAdultForeignText);

        ivAuthAdultMaleCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF000000")));
        ivAuthAdultFemaleCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF000000")));
	}

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // 성별 > 남 onClick
            case R.id.llAuthAdultMaleBtn:
                llAuthAdultMaleBtn.setAlpha(1.0f);
                llAuthAdultFemaleBtn.setAlpha(0.3f);
                break;
            // 성별 > 여 onClick
            case R.id.llAuthAdultFemaleBtn:
                llAuthAdultMaleBtn.setAlpha(0.3f);
                llAuthAdultFemaleBtn.setAlpha(1.0f);
                break;
            // 내국인 / 외국인 onClick
            case R.id.llAuthAdultForeignBtn:
                isForeigner = !isForeigner;
                updateForeignerUI();
                break;
            // 확인 onClick
            case R.id.rlAuthAdultConfirmBtn:

                break;
        }
    }

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
}
