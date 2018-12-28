package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.registration.InitPostActivity;
import com.melodigm.post.sns.SnsAccountRestoreActivity;
import com.melodigm.post.sns.SnsAccountSyncActivity;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.SPUtil;

public class SettingAccountActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvUserGender, tvUserBirthYear;
    private LinearLayout llSnsAccountEmptyLayout, llSnsAccountLayout;
    private ImageView ivSnsAccountTypeIcon;
    private TextView tvSnsAccountType;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_setting_account);
        mContext = this;
        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.account), false);

        tvUserGender = (TextView) findViewById(R.id.tvUserGender);
        if ("F".equals(SPUtil.getSharedPreference(mContext, Constants.SP_USER_GENDER)))
            tvUserGender.setText("여");
        else if ("M".equals(SPUtil.getSharedPreference(mContext, Constants.SP_USER_GENDER)))
            tvUserGender.setText("남");
        tvUserBirthYear = (TextView) findViewById(R.id.tvUserBirthYear);
        tvUserBirthYear.setText(SPUtil.getSharedPreference(mContext, Constants.SP_USER_BIRTH_YEAR));
        llSnsAccountEmptyLayout = (LinearLayout) findViewById(R.id.llSnsAccountEmptyLayout);
        llSnsAccountLayout = (LinearLayout) findViewById(R.id.llSnsAccountLayout);
        ivSnsAccountTypeIcon = (ImageView) findViewById(R.id.ivSnsAccountTypeIcon);
        tvSnsAccountType = (TextView) findViewById(R.id.tvSnsAccountType);
        updateSnsUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSnsUI();
    }

    public void updateSnsUI() {
        if ( !"".equals(SPUtil.getSharedPreference(mContext, Constants.SP_ACCOUNT_ID)) && !"".equals(SPUtil.getSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE)) ) {
            llSnsAccountLayout.setVisibility(View.VISIBLE);
            llSnsAccountEmptyLayout.setVisibility(View.GONE);

            switch (SPUtil.getSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE)) {
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_FACEBOOK:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_fb);
                    tvSnsAccountType.setText(getString(R.string.facebook));
                    break;
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_TWITTER:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_twt);
                    tvSnsAccountType.setText(getString(R.string.twitter));
                    break;
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_INSTAGRAM:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_insta);
                    tvSnsAccountType.setText(getString(R.string.instagram));
                    break;
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_NAVER_LINE:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_line);
                    tvSnsAccountType.setText(getString(R.string.line));
                    break;
            }
        } else {
            llSnsAccountLayout.setVisibility(View.GONE);
            llSnsAccountEmptyLayout.setVisibility(View.VISIBLE);
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
            // 연동설정 onClick
            case R.id.rlSnsAccountSyncBtn:
                startActivityForResult(new Intent(mContext, SnsAccountSyncActivity.class), Constants.QUERY_SNS_ACCOUNT_SYNC);
                break;
            // 데이터 복원 onClick
            case R.id.llRestoreDataBtn:
                intent = new Intent(mContext, SnsAccountRestoreActivity.class);
                startActivity(intent);
                break;
            // 서비스 초기화 onClick
            case R.id.llInitServiceBtn:
                intent = new Intent(mContext, InitPostActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // SNS 계정 연동 후 ActivityResult
            case Constants.QUERY_SNS_ACCOUNT_SYNC:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    updateSnsUI();
                }
                break;
        }
    }
}
