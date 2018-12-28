package com.melodigm.post.menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.agreement.AgreementServiceActivity;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.widget.LetterSpacingTextView;

public class GeneralActivity extends BaseActivity implements View.OnClickListener {
    private ImageView ivSnsAccountTypeIcon, ivAppUpdateBtn;
    private LetterSpacingTextView tvAppVersion;
    private int latestAppVersionCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_general);
        mContext = this;

        latestAppVersionCode = SPUtil.getIntSharedPreference(mContext, Constants.SP_LATEST_APP_VERSION);

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.general), false);

        ivSnsAccountTypeIcon = (ImageView)findViewById(R.id.ivSnsAccountTypeIcon);
        if ( !"".equals(SPUtil.getSharedPreference(mContext, Constants.SP_ACCOUNT_ID)) && !"".equals(SPUtil.getSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE)) ) {
            switch (SPUtil.getSharedPreference(mContext, Constants.SP_ACCOUNT_AUTH_TYPE)) {
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_FACEBOOK:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_fb);
                    break;
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_TWITTER:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_twt);
                    break;
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_INSTAGRAM:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_insta);
                    break;
                case Constants.REQUEST_ACCOUNT_AUTH_TYPE_NAVER_LINE:
                    ivSnsAccountTypeIcon.setImageResource(R.drawable.icon_line);
                    break;
            }
        } else {
            ivSnsAccountTypeIcon.setVisibility(View.GONE);
        }

        ivAppUpdateBtn = (ImageView)findViewById(R.id.ivAppUpdateBtn);
        tvAppVersion = (LetterSpacingTextView)findViewById(R.id.tvAppVersion);

        if (Constants.APP_VERSION_CODE < latestAppVersionCode) {
            ivAppUpdateBtn.setVisibility(View.VISIBLE);
            tvAppVersion.setVisibility(View.GONE);
        } else {
            ivAppUpdateBtn.setVisibility(View.GONE);
            tvAppVersion.setVisibility(View.VISIBLE);

            tvAppVersion.setSpacing(Constants.TEXT_VIEW_SPACING);
            tvAppVersion.setText(getString(R.string.app_version, Constants.APP_VERSION_NAME));
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
            // 도움말 및 의견 보내기 onClick
            case R.id.llHelpDeskBtn:
                intent = new Intent(mContext, HelpDeskActivity.class);
                startActivity(intent);
                break;
            // 이용약관 / 개인정보보호정책 / 위치정보이용약관 onClick
            case R.id.llAgreementServiceBtn:
            case R.id.llAgreementSchemeBtn:
            case R.id.llAgreementLocationBtn:
                String agreementType = (String)v.getTag();
                intent = new Intent(mContext, AgreementServiceActivity.class);
                intent.putExtra("AGREEMENT_TYPE", agreementType);
                startActivity(intent);
                break;
            // UPDATE 버튼 onClick
            case R.id.ivAppUpdateBtn:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.SERVICE_PACKAGE));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }
}
