package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;

public class MyPostActivity extends BaseActivity {
	private RelativeLayout btnTimeLine, btnAlrimCenter, btnMyUseCoupon, btnCabinet, btnStamp, btnPlayHistory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_mypost);
        mContext = this;
        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.title_mypost), false);

        btnTimeLine = (RelativeLayout) findViewById(R.id.btnTimeLine);
        btnAlrimCenter = (RelativeLayout) findViewById(R.id.btnAlrimCenter);
        btnMyUseCoupon = (RelativeLayout) findViewById(R.id.btnMyUseCoupon);
        btnCabinet = (RelativeLayout) findViewById(R.id.btnCabinet);
        btnStamp = (RelativeLayout) findViewById(R.id.btnStamp);
        btnPlayHistory = (RelativeLayout) findViewById(R.id.btnPlayHistory);

        btnTimeLine.setOnClickListener(onClickListener);
        btnAlrimCenter.setOnClickListener(onClickListener);
        btnMyUseCoupon.setOnClickListener(onClickListener);
        btnCabinet.setOnClickListener(onClickListener);
        btnStamp.setOnClickListener(onClickListener);
        btnPlayHistory.setOnClickListener(onClickListener);
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;

            switch (v.getId()) {
                case R.id.btnHeaderBack :
                    finish();
                    break;
                case R.id.btnHeaderCheck:
                    break;
                // 타임라인 onClick
                case R.id.btnTimeLine:
                    intent = new Intent(mContext, TimeLineActivity.class);
                    startActivity(intent);
                    break;
                // 알림센터 onClick
                case R.id.btnAlrimCenter:
                    intent = new Intent(mContext, NotificationCenterActivity.class);
                    startActivity(intent);
                    break;
                // 내 이용권 onClick
                case R.id.btnMyUseCoupon:
                    intent = new Intent(mContext, UseCouponActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btnCabinet:
                    startActivity(new Intent(mContext, CabinetActivity.class));
                    break;
                // 우표 onClick
                case R.id.btnStamp:
                    intent = new Intent(mContext, StampActivity.class);
                    startActivity(intent);
                    break;
                // 재생 히스토리 onClick
                case R.id.btnPlayHistory:
                    intent = new Intent(mContext, PlayHistoryActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
}
