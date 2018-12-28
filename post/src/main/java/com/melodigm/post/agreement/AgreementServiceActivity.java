package com.melodigm.post.agreement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.CommonUtil;

public class AgreementServiceActivity extends BaseActivity implements View.OnClickListener {
    private String mAgreementType;
    private WebView wvAgreementService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement_service);
        mContext = this;

        Intent intent = getIntent();
        mAgreementType = intent.getStringExtra("AGREEMENT_TYPE");
        if (CommonUtil.isNull(mAgreementType)) mAgreementType = "SERVICE";

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);

        wvAgreementService = (WebView)findViewById(R.id.wvAgreementService);
        if ("SERVICE".equals(mAgreementType)) {
            setPostHeaderTitle(getString(R.string.agreement_service), false);
            wvAgreementService.loadUrl(Constants.AGREEMENT_SERVICE);
        } else if ("PRIVATE".equals(mAgreementType)) {
            setPostHeaderTitle(getString(R.string.agreement_private), false);
            wvAgreementService.loadUrl(Constants.AGREEMENT_PRIVATE);
        } else if ("LOCATION".equals(mAgreementType)) {
            setPostHeaderTitle(getString(R.string.agreement_location), false);
            wvAgreementService.loadUrl(Constants.AGREEMENT_LOCATION);
        } else if ("SCHEME".equals(mAgreementType)) {
            setPostHeaderTitle(getString(R.string.agreement_scheme), false);
            wvAgreementService.loadUrl(Constants.AGREEMENT_SCHEME);
        }
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
        }
    }
}
