package com.melodigm.post.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.CastUtil;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;

import java.io.File;

public class SettingPlayerActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout llSettingStreamingQualityAACBtn, llSettingStreamingQuality128Btn, llSettingStreamingQuality192Btn, llSettingStreamingQuality320Btn;
    private LetterSpacingTextView lstvSettingStreamingQualityAACBtn, lstvSettingStreamingQuality128Btn, lstvSettingStreamingQuality192Btn, lstvSettingStreamingQuality320Btn;
    private ImageView ivSettingStreamingQualityAACBtn, ivSettingStreamingQuality128Btn, ivSettingStreamingQuality192Btn, ivSettingStreamingQuality320Btn;

    private LinearLayout llSettingPlayListAddPositionTopBtn, llSettingPlayListAddPositionNextBtn, llSettingPlayListAddPositionBottomBtn;
    private ImageView ivSettingPlayListAddPositionTopBtn, ivSettingPlayListAddPositionNextBtn, ivSettingPlayListAddPositionBottomBtn;
    private String mPlayListAddPosition;

    private ToggleButton tbSettingDelete500ListBtn, tbSettingUsePlayFileSaveBtn, tbSettingDisplayLockScreenAlbumBtn;
    private TextView tvConfirmDeviceStorage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_setting_player);
        mContext = this;
        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.player_setting), false);

        llSettingStreamingQualityAACBtn = (LinearLayout)findViewById(R.id.llSettingStreamingQualityAACBtn);
        llSettingStreamingQuality128Btn = (LinearLayout)findViewById(R.id.llSettingStreamingQuality128Btn);
        llSettingStreamingQuality192Btn = (LinearLayout)findViewById(R.id.llSettingStreamingQuality192Btn);
        llSettingStreamingQuality320Btn = (LinearLayout)findViewById(R.id.llSettingStreamingQuality320Btn);
        lstvSettingStreamingQualityAACBtn = (LetterSpacingTextView)findViewById(R.id.lstvSettingStreamingQualityAACBtn);
        lstvSettingStreamingQuality128Btn = (LetterSpacingTextView)findViewById(R.id.lstvSettingStreamingQuality128Btn);
        lstvSettingStreamingQuality192Btn = (LetterSpacingTextView)findViewById(R.id.lstvSettingStreamingQuality192Btn);
        lstvSettingStreamingQuality320Btn = (LetterSpacingTextView)findViewById(R.id.lstvSettingStreamingQuality320Btn);
        lstvSettingStreamingQualityAACBtn.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvSettingStreamingQuality128Btn.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvSettingStreamingQuality192Btn.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvSettingStreamingQuality320Btn.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvSettingStreamingQualityAACBtn.setText(getString(R.string.aac));
        lstvSettingStreamingQuality128Btn.setText(getString(R.string.mp3_128k));
        lstvSettingStreamingQuality192Btn.setText(getString(R.string.mp3_192k));
        lstvSettingStreamingQuality320Btn.setText(getString(R.string.mp3_320k));
        ivSettingStreamingQualityAACBtn = (ImageView)findViewById(R.id.ivSettingStreamingQualityAACBtn);
        ivSettingStreamingQuality128Btn = (ImageView)findViewById(R.id.ivSettingStreamingQuality128Btn);
        ivSettingStreamingQuality192Btn = (ImageView)findViewById(R.id.ivSettingStreamingQuality192Btn);
        ivSettingStreamingQuality320Btn = (ImageView)findViewById(R.id.ivSettingStreamingQuality320Btn);

        updateStreamingQualityUI();

        // 재생목록 추가 위치
        mPlayListAddPosition = SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_LIST_ADD_POSITION);
        if (CommonUtil.isNull(mPlayListAddPosition)) {
            mPlayListAddPosition = "BOTTOM";
            SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_LIST_ADD_POSITION, mPlayListAddPosition);
        }

        llSettingPlayListAddPositionTopBtn = (LinearLayout)findViewById(R.id.llSettingPlayListAddPositionTopBtn);
        llSettingPlayListAddPositionNextBtn = (LinearLayout)findViewById(R.id.llSettingPlayListAddPositionNextBtn);
        llSettingPlayListAddPositionBottomBtn = (LinearLayout)findViewById(R.id.llSettingPlayListAddPositionBottomBtn);
        ivSettingPlayListAddPositionTopBtn = (ImageView)findViewById(R.id.ivSettingPlayListAddPositionTopBtn);
        ivSettingPlayListAddPositionNextBtn = (ImageView)findViewById(R.id.ivSettingPlayListAddPositionNextBtn);
        ivSettingPlayListAddPositionBottomBtn = (ImageView)findViewById(R.id.ivSettingPlayListAddPositionBottomBtn);

        updatePlayListAddPositionUI();

        // 재생목록 설정
        boolean isDelete500List = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_DELETE_500_LIST);

        tbSettingDelete500ListBtn = (ToggleButton)findViewById(R.id.tbSettingDelete500ListBtn);
        if (isDelete500List) tbSettingDelete500ListBtn.setChecked(true);
        tbSettingDelete500ListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_DELETE_500_LIST, tbSettingDelete500ListBtn.isChecked());
            }
        });

        // 파일 임시저장 설정
        boolean isPlayFileSave = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_FILE_SAVE, true);
        tbSettingUsePlayFileSaveBtn = (ToggleButton)findViewById(R.id.tbSettingUsePlayFileSaveBtn);
        tbSettingUsePlayFileSaveBtn.setChecked(isPlayFileSave);

        tbSettingUsePlayFileSaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tbSettingUsePlayFileSaveBtn.isChecked()) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onClickListener, mContext.getResources().getString(R.string.dialog_info_setting_play_file_save));
                    mPostDialog.show();
                } else {
                    /*File file = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE);
                    DeviceUtil.removeFiles(file);
                    updateDeviceStorageUI();*/
                }
                SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_FILE_SAVE, tbSettingUsePlayFileSaveBtn.isChecked());
            }
        });

        tvConfirmDeviceStorage = (TextView)findViewById(R.id.tvConfirmDeviceStorage);

        updateDeviceStorageUI();

        // 잠금화면 앨범커버 표시
        boolean isDiaplayLockScreenAlbum = SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_DISPLAY_LOCK_SCREEN_ALBUM, true);

        tbSettingDisplayLockScreenAlbumBtn = (ToggleButton)findViewById(R.id.tbSettingDisplayLockScreenAlbumBtn);
        if (isDiaplayLockScreenAlbum) tbSettingDisplayLockScreenAlbumBtn.setChecked(true);
        tbSettingDisplayLockScreenAlbumBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_DISPLAY_LOCK_SCREEN_ALBUM, tbSettingDisplayLockScreenAlbumBtn.isChecked());
            }
        });
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 안내 확인 onClick
                case R.id.btnInfoConfirm:
                    if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // 안내 확인 onClick
            case R.id.btnInfoConfirm:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                break;
            // 스트리밍 음질 선택 onClick
            case R.id.llSettingStreamingQuality320Btn:
                if (!SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_STREAMING_QUALITY, "AAC").equals(v.getTag())) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, this, mContext.getResources().getString(R.string.dialog_info_setting_streaming_quality_high));
                    mPostDialog.show();
                }
            case R.id.llSettingStreamingQualityAACBtn:
            case R.id.llSettingStreamingQuality128Btn:
            case R.id.llSettingStreamingQuality192Btn:
                if (!SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_STREAMING_QUALITY, "AAC").equals(v.getTag())) {
                    SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_STREAMING_QUALITY, (String)v.getTag());
                    updateStreamingQualityUI();
                }
                break;
            // 재생목록 추가 위치 onClick
            case R.id.llSettingPlayListAddPositionTopBtn:
            case R.id.llSettingPlayListAddPositionNextBtn:
            case R.id.llSettingPlayListAddPositionBottomBtn:
                mPlayListAddPosition = (String)v.getTag();
                SPUtil.setSharedPreference(mContext, Constants.SP_PLAYER_LIST_ADD_POSITION, mPlayListAddPosition);
                updatePlayListAddPositionUI();
                break;
            // 모든 임시저장 파일 삭제 onClick
            case R.id.llDeletePlayFileBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete_play_file));
                mPostDialog.show();
                break;
            // Confirm 확인 onClick
            case R.id.btnConfirmConfirm:
                File file = getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE);
                DeviceUtil.removeFiles(file);
                updateDeviceStorageUI();
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, this, mContext.getResources().getString(R.string.dialog_info_delete));
                mPostDialog.show();
                break;
        }
    }

    private void updateStreamingQualityUI() {
        switch (SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_STREAMING_QUALITY, "AAC")) {
            case "AAC":
                llSettingStreamingQualityAACBtn.setAlpha(1.0f);
                llSettingStreamingQuality128Btn.setAlpha(0.5f);
                llSettingStreamingQuality192Btn.setAlpha(0.5f);
                llSettingStreamingQuality320Btn.setAlpha(0.5f);
                ivSettingStreamingQualityAACBtn.setVisibility(View.VISIBLE);
                ivSettingStreamingQuality128Btn.setVisibility(View.GONE);
                ivSettingStreamingQuality192Btn.setVisibility(View.GONE);
                ivSettingStreamingQuality320Btn.setVisibility(View.GONE);
                break;
            case "128":
                llSettingStreamingQualityAACBtn.setAlpha(0.5f);
                llSettingStreamingQuality128Btn.setAlpha(1.0f);
                llSettingStreamingQuality192Btn.setAlpha(0.5f);
                llSettingStreamingQuality320Btn.setAlpha(0.5f);
                ivSettingStreamingQualityAACBtn.setVisibility(View.GONE);
                ivSettingStreamingQuality128Btn.setVisibility(View.VISIBLE);
                ivSettingStreamingQuality192Btn.setVisibility(View.GONE);
                ivSettingStreamingQuality320Btn.setVisibility(View.GONE);
                break;
            case "192":
                llSettingStreamingQualityAACBtn.setAlpha(0.5f);
                llSettingStreamingQuality128Btn.setAlpha(0.5f);
                llSettingStreamingQuality192Btn.setAlpha(1.0f);
                llSettingStreamingQuality320Btn.setAlpha(0.5f);
                ivSettingStreamingQualityAACBtn.setVisibility(View.GONE);
                ivSettingStreamingQuality128Btn.setVisibility(View.GONE);
                ivSettingStreamingQuality192Btn.setVisibility(View.VISIBLE);
                ivSettingStreamingQuality320Btn.setVisibility(View.GONE);
                break;
            case "320":
                llSettingStreamingQualityAACBtn.setAlpha(0.5f);
                llSettingStreamingQuality128Btn.setAlpha(0.5f);
                llSettingStreamingQuality192Btn.setAlpha(0.5f);
                llSettingStreamingQuality320Btn.setAlpha(1.0f);
                ivSettingStreamingQualityAACBtn.setVisibility(View.GONE);
                ivSettingStreamingQuality128Btn.setVisibility(View.GONE);
                ivSettingStreamingQuality192Btn.setVisibility(View.GONE);
                ivSettingStreamingQuality320Btn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updatePlayListAddPositionUI() {
        switch (mPlayListAddPosition) {
            case "TOP":
                llSettingPlayListAddPositionTopBtn.setAlpha(1.0f);
                llSettingPlayListAddPositionNextBtn.setAlpha(0.5f);
                llSettingPlayListAddPositionBottomBtn.setAlpha(0.5f);
                ivSettingPlayListAddPositionTopBtn.setVisibility(View.VISIBLE);
                ivSettingPlayListAddPositionNextBtn.setVisibility(View.GONE);
                ivSettingPlayListAddPositionBottomBtn.setVisibility(View.GONE);
                break;
            case "NEXT":
                llSettingPlayListAddPositionTopBtn.setAlpha(0.5f);
                llSettingPlayListAddPositionNextBtn.setAlpha(1.0f);
                llSettingPlayListAddPositionBottomBtn.setAlpha(0.5f);
                ivSettingPlayListAddPositionTopBtn.setVisibility(View.GONE);
                ivSettingPlayListAddPositionNextBtn.setVisibility(View.VISIBLE);
                ivSettingPlayListAddPositionBottomBtn.setVisibility(View.GONE);
                break;
            case "BOTTOM":
                llSettingPlayListAddPositionTopBtn.setAlpha(0.5f);
                llSettingPlayListAddPositionNextBtn.setAlpha(0.5f);
                llSettingPlayListAddPositionBottomBtn.setAlpha(1.0f);
                ivSettingPlayListAddPositionTopBtn.setVisibility(View.GONE);
                ivSettingPlayListAddPositionNextBtn.setVisibility(View.GONE);
                ivSettingPlayListAddPositionBottomBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateDeviceStorageUI() {
        tvConfirmDeviceStorage.setText(getString(R.string.confirm_device_storage_val, CastUtil.getFileSize(DeviceUtil.getTotalInternalMemorySize()), CastUtil.getFileSize(DeviceUtil.getAvailableInternalMemorySize())));
    }
}
