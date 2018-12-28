package com.melodigm.post.menu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;

public class HelpDeskActivity extends BaseActivity implements View.OnClickListener {
    private ToggleButton tbSettingUseDataNetworkBtn, tbSettingUseLocationInfoBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_helpdesk);
        mContext = this;
        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.support_send_mail), false);
	}

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // 앱스토어 평가하러 가기 onClick
            case R.id.llGoAppStoreBtn:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.SERVICE_PACKAGE));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            // 의견 보내기 onClick
            case R.id.llSendMailBtn:
                intent = new Intent(mContext, SendMailActivity.class);
                startActivity(intent);
                break;
        }
    }
}
