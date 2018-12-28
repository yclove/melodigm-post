package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.LocationUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.LetterSpacingTextView;

public class SettingActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private ToggleButton tbSettingUseDataNetworkBtn;
    private LetterSpacingTextView lstvSettingLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_setting);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.setting), false);

        boolean isUseDataNetwork = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_USE_DATA_NETWORK);

        tbSettingUseDataNetworkBtn = (ToggleButton)findViewById(R.id.tbSettingUseDataNetworkBtn);
        if (isUseDataNetwork) tbSettingUseDataNetworkBtn.setChecked(true);
        tbSettingUseDataNetworkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SPUtil.setSharedPreference(mContext, Constants.SP_USE_DATA_NETWORK, tbSettingUseDataNetworkBtn.isChecked());
            }
        });

        // 위치 서비스 설정
        lstvSettingLocation = (LetterSpacingTextView)findViewById(R.id.lstvSettingLocation);
        lstvSettingLocation.setSpacing(Constants.TEXT_VIEW_SPACING);
        mLocationUtil = new LocationUtil(mContext, mHandler);
        if (mLocationUtil.isLocationEnabled()) {
            lstvSettingLocation.setText(getString(R.string.on));
        } else {
            lstvSettingLocation.setText(getString(R.string.off));
            SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, "");
            SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, "");
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
            // 재생 관련 설정 onClick
            case R.id.llSettingPlayerBtn:
                intent = new Intent(mContext, SettingPlayerActivity.class);
                startActivity(intent);
                break;
            // 알림 onClick
            case R.id.llSettingNotificationBtn:
                intent = new Intent(mContext, SettingNotificationActivity.class);
                startActivity(intent);
                break;
            // 계정 onClick
            case R.id.llSettingAccountBtn:
                intent = new Intent(mContext, SettingAccountActivity.class);
                startActivity(intent);
                break;
            // onClick
            case R.id.llSettingLocationBtn:
                intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, Constants.QUERY_LOCATION_SERVICE_SETTING);
                break;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            // 위치 서비스 조회 성공 후 Handler
            case Constants.QUERY_LOCATION_CHANGE:
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, msg.getData().getString("USER_LAT", ""));
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, msg.getData().getString("USER_LNG", ""));
                mLocationUtil.stopLocationUpdate();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 위치 서비스 설정 후 ActivityResult
            case Constants.QUERY_LOCATION_SERVICE_SETTING:
                if (mLocationUtil.isLocationEnabled()) {
                    lstvSettingLocation.setText(getString(R.string.on));
                    if ("".equals(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LAT)) || "".equals(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LNG))) {
                        mLocationUtil.run();
                    }
                } else {
                    lstvSettingLocation.setText(getString(R.string.off));
                    SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, "");
                    SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, "");
                }
                break;
        }
    }
}
