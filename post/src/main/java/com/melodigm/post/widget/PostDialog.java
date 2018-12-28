package com.melodigm.post.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.LogUtil;

import java.lang.reflect.Field;

public class PostDialog extends Dialog implements View.OnClickListener, NumberPicker.OnValueChangeListener {
    private Context mContext;
    private LinearLayout llSelectYearConfirmBtn;
    private ImageView ivNotifyAk01Circle, ivNotifyAk02Circle, ivNotifyAk03Circle, ivNotifyConfirm;
    private TextView tvNotifyAk01, tvNotifyAk02, tvNotifyAk03, tvNotifyConfirm;
    private LinearLayout btnNotifyConfirm;

    public void setCancelabled(boolean cancleable) {
        setCancelable(cancleable);
    }

    public PostDialog(Context context, int dialogType, View.OnClickListener onClickListener, Object... dialogMessage) {
        super(context, R.style.DialogTheme);
        this.mContext = context;
        setCancelable(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

        View view;
        WindowManager.LayoutParams lp;

        switch (dialogType) {
            // 미등록 사용자 타입
            case Constants.DIALOG_TYPE_NOT_FOUND_USER:
                setCancelable(false);
                view = inflater.inflate(R.layout.dialog_not_found_user, null);
                setContentView(view);

                TextView tvNotFoundUserMessage = (TextView)findViewById(R.id.tvNotFoundUserMessage);
                Button btnNotFoundUserConfirm = (Button)findViewById(R.id.btnNotFoundUserConfirm);

                tvNotFoundUserMessage.setText((String)dialogMessage[0]);
                btnNotFoundUserConfirm.setOnClickListener(onClickListener);

                break;
            // 필수 업데이트 타입
            case Constants.DIALOG_TYPE_UPDATE_NEED:
                setCancelable(false);
                view = inflater.inflate(R.layout.dialog_update_need, null);
                setContentView(view);

                Button btnUpdateNeedConfirm = (Button)findViewById(R.id.btnUpdateNeedConfirm);

                btnUpdateNeedConfirm.setOnClickListener(onClickListener);
                break;
            // 권장 업데이트 타입
            case Constants.DIALOG_TYPE_UPDATE_SUPPORT:
                view = inflater.inflate(R.layout.dialog_update_support, null);
                setContentView(view);

                ImageView ivUpdateSupportUpdateBtn = (ImageView)findViewById(R.id.ivUpdateSupportUpdateBtn);
                Button btnUpdateSupportCancel = (Button)findViewById(R.id.btnUpdateSupportCancel);
                Button btnUpdateSupportSee = (Button)findViewById(R.id.btnUpdateSupportSee);

                ivUpdateSupportUpdateBtn.setOnClickListener(onClickListener);
                btnUpdateSupportCancel.setOnClickListener(this);
                btnUpdateSupportSee.setOnClickListener(onClickListener);
                break;
            // 카메라 / 갤러리 선택 타입
            case Constants.DIALOG_TYPE_CHOICE_PICTURE:
                view = inflater.inflate(R.layout.dialog_choice_picture, null);
                setContentView(view);

                LinearLayout mBtnCamera = (LinearLayout)findViewById(R.id.contentCameraLayout);
                LinearLayout mBtnGallery = (LinearLayout)findViewById(R.id.contentGalleryLayout);

                mBtnCamera.setOnClickListener(onClickListener);
                mBtnGallery.setOnClickListener(onClickListener);
                break;
            // 태어난 해 선택 타입
            case Constants.DIALOG_TYPE_CHOICE_YEAR:
                view = inflater.inflate(R.layout.dialog_choice_year, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.PostDialogSlideAnimation;

                int currentYear = Integer.valueOf(DateUtil.getCurrentDate("yyyy"));
                int defaultYear = (CommonUtil.isNull((String)dialogMessage[0])) ? 2002 : Integer.valueOf((String)dialogMessage[0]);

                final NumberPicker numberPicker = (NumberPicker)findViewById(R.id.numberPicker);
                numberPickerTextColor( numberPicker, Color.BLACK );
                numberPicker.setMaxValue(currentYear);
                numberPicker.setMinValue(currentYear - 100);
                numberPicker.setValue(defaultYear);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setOnValueChangedListener(this);

                setNumberPickerDividerColor(numberPicker);

                RelativeLayout rlSelectYearLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlSelectYearLayoutCloseBtn);
                rlSelectYearLayoutCloseBtn.setOnClickListener(this);

                llSelectYearConfirmBtn = (LinearLayout)findViewById(R.id.llSelectYearConfirmBtn);
                llSelectYearConfirmBtn.setTag(defaultYear);
                llSelectYearConfirmBtn.setOnClickListener(onClickListener);
                break;
            // 확인 타입
            case Constants.DIALOG_TYPE_CONFIRM:
                view = inflater.inflate(R.layout.dialog_confirm, null);
                setContentView(view);

                LinearLayout llConfrimTitle = (LinearLayout)findViewById(R.id.llConfrimTitle);
                TextView tvConfrimTitle = (TextView)findViewById(R.id.tvConfrimTitle);
                TextView tvConfrimMessage = (TextView)findViewById(R.id.tvConfrimMessage);
                Button btnConfirmCancel = (Button)findViewById(R.id.btnConfirmCancel);
                Button btnConfirmConfirm = (Button)findViewById(R.id.btnConfirmConfirm);

                tvConfrimMessage.setText((String)dialogMessage[0]);
                btnConfirmCancel.setOnClickListener(this);

                if (dialogMessage.length > 1 && dialogMessage[1] instanceof Bundle)
                    btnConfirmConfirm.setTag(R.id.tag_data, dialogMessage[1]);

                if (dialogMessage.length > 1 && dialogMessage[1] instanceof String) {
                    llConfrimTitle.setVisibility(View.VISIBLE);
                    tvConfrimTitle.setText((String)dialogMessage[1]);
                }

                btnConfirmConfirm.setOnClickListener(onClickListener);
                break;
            // 위치 확인 타입
            case Constants.DIALOG_TYPE_LOCATION_SERVICE:
                view = inflater.inflate(R.layout.dialog_location_service, null);
                setContentView(view);

                Button btnLocationServiceCancel = (Button)findViewById(R.id.btnLocationServiceCancel);
                Button btnLocationServiceSetting = (Button)findViewById(R.id.btnLocationServiceSetting);

                btnLocationServiceCancel.setOnClickListener(this);
                btnLocationServiceSetting.setOnClickListener(onClickListener);
                break;
            // 안내 타입
            case Constants.DIALOG_TYPE_INFO:
                view = inflater.inflate(R.layout.dialog_info, null);
                setContentView(view);

                LinearLayout llInfoTitle = (LinearLayout)findViewById(R.id.llInfoTitle);
                TextView tvInfoTitle = (TextView)findViewById(R.id.tvInfoTitle);
                TextView tvInfoMessage = (TextView)findViewById(R.id.tvInfoMessage);
                Button btnInfoConfirm = (Button)findViewById(R.id.btnInfoConfirm);

                tvInfoMessage.setText((String)dialogMessage[0]);

                if (dialogMessage.length > 1 && dialogMessage[1] instanceof String) {
                    llInfoTitle.setVisibility(View.VISIBLE);
                    tvInfoTitle.setText((String)dialogMessage[1]);
                }

                btnInfoConfirm.setOnClickListener(onClickListener);
                break;
            // RADIO 타이틀 선택
            case Constants.DIALOG_TYPE_RADIO_TITLE:
                view = inflater.inflate(R.layout.dialog_radio_title, null);
                setContentView(view);

                LinearLayout btnRadioTitleOnce = (LinearLayout)findViewById(R.id.btnRadioTitleOnce);
                LinearLayout btnRadioTitleCycle = (LinearLayout)findViewById(R.id.btnRadioTitleCycle);
                Button btnRadioTitleCancel = (Button)findViewById(R.id.btnRadioTitleCancel);

                btnRadioTitleOnce.setOnClickListener(onClickListener);
                btnRadioTitleCycle.setOnClickListener(onClickListener);
                btnRadioTitleCancel.setOnClickListener(this);
                break;
            // 통신사 선택
            case Constants.DIALOG_TYPE_NEWS_AGENCY:
                view = inflater.inflate(R.layout.dialog_news_agency, null);
                setContentView(view);

                LinearLayout llNewsAgencySkt = (LinearLayout)findViewById(R.id.llNewsAgencySkt);
                LinearLayout llNewsAgencyKt = (LinearLayout)findViewById(R.id.llNewsAgencyKt);
                LinearLayout llNewsAgencyLg = (LinearLayout)findViewById(R.id.llNewsAgencyLg);
                LinearLayout llNewsAgencySaving = (LinearLayout)findViewById(R.id.llNewsAgencySaving);

                llNewsAgencySkt.setOnClickListener(onClickListener);
                llNewsAgencyKt.setOnClickListener(onClickListener);
                llNewsAgencyLg.setOnClickListener(onClickListener);
                llNewsAgencySaving.setOnClickListener(onClickListener);
                break;
            // RADIO 타이틀 선택 > 연속 재생 선택
            case Constants.DIALOG_TYPE_RADIO_ON_AIR:
                view = inflater.inflate(R.layout.dialog_radio_on_air, null);
                setContentView(view);

                TextView tvRadioOnAirInfoMessage = (TextView)findViewById(R.id.tvRadioOnAirInfoMessage);
                Button btnRadioOnAirCancel = (Button)findViewById(R.id.btnRadioOnAirCancel);
                tvRadioOnAirInfoMessage.setText((String)dialogMessage[0]);
                btnRadioOnAirCancel.setOnClickListener(this);
                break;
            // RADIO ON AIR 해제
            case Constants.DIALOG_TYPE_RADIO_ON_AIR_CLOSE:
                view = inflater.inflate(R.layout.dialog_radio_on_air_close, null);
                setContentView(view);

                Button btnOnAirPause = (Button)findViewById(R.id.btnOnAirPause);
                Button btnOnAirCancel = (Button)findViewById(R.id.btnOnAirCancel);

                btnOnAirPause.setOnClickListener(onClickListener);
                btnOnAirCancel.setOnClickListener(onClickListener);
                break;
            // OST 팝업 > 전체듣기
            case Constants.DIALOG_TYPE_PLAY_ALL:
                view = inflater.inflate(R.layout.dialog_play_all, null);
                setContentView(view);

                LinearLayout btnPlayAllAddList = (LinearLayout)findViewById(R.id.btnPlayAllAddList);
                LinearLayout btnPlayAllChangeList = (LinearLayout)findViewById(R.id.btnPlayAllChangeList);
                Button btnPlayAllCancel = (Button)findViewById(R.id.btnPlayAllCancel);

                btnPlayAllAddList.setOnClickListener(onClickListener);
                btnPlayAllChangeList.setOnClickListener(onClickListener);
                btnPlayAllCancel.setOnClickListener(this);
                break;
            // Player 추가 타입
            case Constants.DIALOG_TYPE_PLAYER_ADD:
                view = inflater.inflate(R.layout.dialog_player_add, null);
                setContentView(view);

                LinearLayout llPlayerAddCabinetBtn = (LinearLayout)findViewById(R.id.llPlayerAddCabinetBtn);
                LinearLayout llPlayerAddSearchMusicBtn = (LinearLayout)findViewById(R.id.llPlayerAddSearchMusicBtn);
                Button btnPlayerAddCancel = (Button)findViewById(R.id.btnPlayerAddCancel);

                llPlayerAddCabinetBtn.setOnClickListener(onClickListener);
                llPlayerAddSearchMusicBtn.setOnClickListener(onClickListener);
                btnPlayerAddCancel.setOnClickListener(this);
                break;
            // Player 정렬 타입
            case Constants.DIALOG_TYPE_PLAYER_SORT:
                view = inflater.inflate(R.layout.dialog_player_sort, null);
                setContentView(view);

                LinearLayout llPlayerSortArtiAscBtn = (LinearLayout)findViewById(R.id.llPlayerSortArtiAscBtn);
                LinearLayout llPlayerSortArtiDescBtn = (LinearLayout)findViewById(R.id.llPlayerSortArtiDescBtn);
                LinearLayout llPlayerSortSongAscBtn = (LinearLayout)findViewById(R.id.llPlayerSortSongAscBtn);
                LinearLayout llPlayerSortSongDescBtn = (LinearLayout)findViewById(R.id.llPlayerSortSongDescBtn);
                Button btnPlayerSortCancel = (Button)findViewById(R.id.btnPlayerSortCancel);

                llPlayerSortArtiAscBtn.setTag(R.id.tag_sort_field, "ARTI");
                llPlayerSortArtiAscBtn.setTag(R.id.tag_sort_asc, true);
                llPlayerSortArtiAscBtn.setOnClickListener(onClickListener);

                llPlayerSortArtiDescBtn.setTag(R.id.tag_sort_field, "ARTI");
                llPlayerSortArtiDescBtn.setTag(R.id.tag_sort_asc, false);
                llPlayerSortArtiDescBtn.setOnClickListener(onClickListener);

                llPlayerSortSongAscBtn.setTag(R.id.tag_sort_field, "SONG");
                llPlayerSortSongAscBtn.setTag(R.id.tag_sort_asc, true);
                llPlayerSortSongAscBtn.setOnClickListener(onClickListener);

                llPlayerSortSongDescBtn.setTag(R.id.tag_sort_field, "SONG");
                llPlayerSortSongDescBtn.setTag(R.id.tag_sort_asc, false);
                llPlayerSortSongDescBtn.setOnClickListener(onClickListener);

                btnPlayerSortCancel.setOnClickListener(this);
                break;
            // 이야기 검색 리스트 > Header > 이야기 정렬 타입
            case Constants.DIALOG_TYPE_STORY_SORT:
                view = inflater.inflate(R.layout.dialog_story_sort, null);
                setContentView(view);

                LinearLayout llStorySortLatest = (LinearLayout)findViewById(R.id.llStorySortLatest);
                LinearLayout llStorySortPopular = (LinearLayout)findViewById(R.id.llStorySortPopular);
                LinearLayout llStorySortOst = (LinearLayout)findViewById(R.id.llStorySortOst);
                ImageView ivStorySortLatest = (ImageView)findViewById(R.id.ivStorySortLatest);
                ImageView ivStorySortPopular = (ImageView)findViewById(R.id.ivStorySortPopular);
                ImageView ivStorySortOst = (ImageView)findViewById(R.id.ivStorySortOst);
                Button btnStorySortCancel = (Button)findViewById(R.id.btnStorySortCancel);

                if ("REG".equals(dialogMessage[0])) {
                    ivStorySortLatest.setVisibility(View.VISIBLE);
                } else if ("LIKE".equals(dialogMessage[0])) {
                    ivStorySortPopular.setVisibility(View.VISIBLE);
                } else if ("OST".equals(dialogMessage[0])) {
                    ivStorySortOst.setVisibility(View.VISIBLE);
                }

                llStorySortLatest.setTag(R.id.tag_sort_field, "REG");
                llStorySortLatest.setOnClickListener(onClickListener);

                llStorySortPopular.setTag(R.id.tag_sort_field, "LIKE");
                llStorySortPopular.setOnClickListener(onClickListener);

                llStorySortOst.setTag(R.id.tag_sort_field, "OST");
                llStorySortOst.setOnClickListener(onClickListener);

                btnStorySortCancel.setOnClickListener(this);
                break;
            // 우표 필요 타입
            case Constants.DIALOG_TYPE_STAMP_REQUIRED:
                view = inflater.inflate(R.layout.dialog_stamp_required, null);
                setContentView(view);

                // 자세히 보기
                Button btnDetailView = (Button)findViewById(R.id.btnDetailView);
                btnDetailView.setOnClickListener(onClickListener);

                // 확인
                Button btnStampRequiredConfirm = (Button)findViewById(R.id.btnStampRequiredConfirm);
                btnStampRequiredConfirm.setOnClickListener(this);
                break;
            // 이야기 검색 리스트 > Header > 이야기 타입 타입
            case Constants.DIALOG_TYPE_STORY_TYPE:
                view = inflater.inflate(R.layout.dialog_story_type, null);
                setContentView(view);

                LinearLayout llStoryTypeAllBtn = (LinearLayout)findViewById(R.id.llStoryTypeAllBtn);
                LinearLayout llStoryTypePostBtn = (LinearLayout)findViewById(R.id.llStoryTypePostBtn);
                LinearLayout llStoryTypeTodayBtn = (LinearLayout)findViewById(R.id.llStoryTypeTodayBtn);
                LinearLayout llStoryTypeRadioBtn = (LinearLayout)findViewById(R.id.llStoryTypeRadioBtn);
                LetterSpacingTextView lstvStoryTypePostText = (LetterSpacingTextView)findViewById(R.id.lstvStoryTypePostText);
                LetterSpacingTextView lstvStoryTypeTodayText = (LetterSpacingTextView)findViewById(R.id.lstvStoryTypeTodayText);
                LetterSpacingTextView lstvStoryTypeRadioText = (LetterSpacingTextView)findViewById(R.id.lstvStoryTypeRadioText);
                ImageView ivStoryTypeAllCheckImage = (ImageView)findViewById(R.id.ivStoryTypeAllCheckImage);
                ImageView ivStoryTypePostCheckImage = (ImageView)findViewById(R.id.ivStoryTypePostCheckImage);
                ImageView ivStoryTypeTodayCheckImage = (ImageView)findViewById(R.id.ivStoryTypeTodayCheckImage);
                ImageView ivStoryTypeRadioCheckImage = (ImageView)findViewById(R.id.ivStoryTypeRadioCheckImage);
                Button btnStoryTypeCancel = (Button)findViewById(R.id.btnStoryTypeCancel);

                lstvStoryTypePostText.setSpacing(Constants.TEXT_VIEW_SPACING);
                lstvStoryTypeTodayText.setSpacing(Constants.TEXT_VIEW_SPACING);
                lstvStoryTypeRadioText.setSpacing(Constants.TEXT_VIEW_SPACING);

                lstvStoryTypePostText.setText(mContext.getString(R.string.post));
                lstvStoryTypeTodayText.setText(mContext.getString(R.string.today));
                lstvStoryTypeRadioText.setText(mContext.getString(R.string.radio));

                if ("".equals(dialogMessage[0])) {
                    ivStoryTypeAllCheckImage.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TYPE_POST.equals(dialogMessage[0])) {
                    ivStoryTypePostCheckImage.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TYPE_TODAY.equals(dialogMessage[0])) {
                    ivStoryTypeTodayCheckImage.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TYPE_RADIO.equals(dialogMessage[0])) {
                    ivStoryTypeRadioCheckImage.setVisibility(View.VISIBLE);
                }

                llStoryTypeAllBtn.setTag(R.id.tag_post_type, "");
                llStoryTypeAllBtn.setOnClickListener(onClickListener);

                llStoryTypePostBtn.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_POST);
                llStoryTypePostBtn.setOnClickListener(onClickListener);

                llStoryTypeTodayBtn.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_TODAY);
                llStoryTypeTodayBtn.setOnClickListener(onClickListener);

                llStoryTypeRadioBtn.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_RADIO);
                llStoryTypeRadioBtn.setOnClickListener(onClickListener);

                btnStoryTypeCancel.setOnClickListener(this);
                break;
            // 우표 정렬 타입
            case Constants.DIALOG_TYPE_STAMP_FILTER:
                view = inflater.inflate(R.layout.dialog_stamp_filter, null);
                setContentView(view);

                LinearLayout llStampFilterAll = (LinearLayout)findViewById(R.id.llStampFilterAll);
                LinearLayout llStampFilterKeep = (LinearLayout)findViewById(R.id.llStampFilterKeep);
                LinearLayout llStampFilterUse = (LinearLayout)findViewById(R.id.llStampFilterUse);
                ImageView ivStampFilterAll = (ImageView)findViewById(R.id.ivStampFilterAll);
                ImageView ivStampFilterKeep = (ImageView)findViewById(R.id.ivStampFilterKeep);
                ImageView ivStampFilterUse = (ImageView)findViewById(R.id.ivStampFilterUse);
                Button btnStampFilterCancel = (Button)findViewById(R.id.btnStampFilterCancel);

                if (Constants.REQUEST_TRANS_TYPE_ALL.equals(dialogMessage[0])) {
                    ivStampFilterAll.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TRANS_TYPE_KEEP.equals(dialogMessage[0])) {
                    ivStampFilterKeep.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TRANS_TYPE_USE.equals(dialogMessage[0])) {
                    ivStampFilterUse.setVisibility(View.VISIBLE);
                }

                llStampFilterAll.setOnClickListener(onClickListener);
                llStampFilterKeep.setOnClickListener(onClickListener);
                llStampFilterUse.setOnClickListener(onClickListener);
                btnStampFilterCancel.setOnClickListener(this);
                break;
            // POPULAR 선택 타입
            case Constants.DIALOG_TYPE_POPULAR:
                view = inflater.inflate(R.layout.dialog_popular, null);
                setContentView(view);

                LinearLayout llPopularPostBtn = (LinearLayout)findViewById(R.id.llPopularPostBtn);
                LinearLayout llPopularTodayBtn = (LinearLayout)findViewById(R.id.llPopularTodayBtn);
                LinearLayout llPopularRadioBtn = (LinearLayout)findViewById(R.id.llPopularRadioBtn);
                LetterSpacingTextView lstvPopularPostText = (LetterSpacingTextView)findViewById(R.id.lstvPopularPostText);
                LetterSpacingTextView lstvPopularTodayText = (LetterSpacingTextView)findViewById(R.id.lstvPopularTodayText);
                LetterSpacingTextView lstvPopularRadioText = (LetterSpacingTextView)findViewById(R.id.lstvPopularRadioText);
                ImageView ivPopularPostCheckImage = (ImageView)findViewById(R.id.ivPopularPostCheckImage);
                ImageView ivPopularTodayCheckImage = (ImageView)findViewById(R.id.ivPopularTodayCheckImage);
                ImageView ivPopularRadioCheckImage = (ImageView)findViewById(R.id.ivPopularRadioCheckImage);

                lstvPopularPostText.setSpacing(Constants.TEXT_VIEW_SPACING);
                lstvPopularTodayText.setSpacing(Constants.TEXT_VIEW_SPACING);
                lstvPopularRadioText.setSpacing(Constants.TEXT_VIEW_SPACING);

                lstvPopularPostText.setText(mContext.getString(R.string.post));
                lstvPopularTodayText.setText(mContext.getString(R.string.today));
                lstvPopularRadioText.setText(mContext.getString(R.string.radio));

                if (Constants.REQUEST_TYPE_POST.equals(dialogMessage[0])) {
                    ivPopularPostCheckImage.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TYPE_TODAY.equals(dialogMessage[0])) {
                    ivPopularTodayCheckImage.setVisibility(View.VISIBLE);
                } else if (Constants.REQUEST_TYPE_RADIO.equals(dialogMessage[0])) {
                    ivPopularRadioCheckImage.setVisibility(View.VISIBLE);
                }

                llPopularPostBtn.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_POST);
                llPopularPostBtn.setOnClickListener(onClickListener);

                llPopularTodayBtn.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_TODAY);
                llPopularTodayBtn.setOnClickListener(onClickListener);

                llPopularRadioBtn.setTag(R.id.tag_post_type, Constants.REQUEST_TYPE_RADIO);
                llPopularRadioBtn.setOnClickListener(onClickListener);
                break;
            // 신고하기 타입
            case Constants.DIALOG_TYPE_NOTIFY:
                view = inflater.inflate(R.layout.dialog_notify, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.PostDialogSlideAnimation;

                RelativeLayout btnNotifyCloseLayout = (RelativeLayout)findViewById(R.id.btnNotifyCloseLayout);
                LinearLayout btnNotifyAk01 = (LinearLayout)findViewById(R.id.btnNotifyAk01);
                LinearLayout btnNotifyAk02 = (LinearLayout)findViewById(R.id.btnNotifyAk02);
                LinearLayout btnNotifyAk03 = (LinearLayout)findViewById(R.id.btnNotifyAk03);

                btnNotifyConfirm = (LinearLayout)findViewById(R.id.btnNotifyConfirm);
                ivNotifyAk01Circle = (ImageView)findViewById(R.id.ivNotifyAk01Circle);
                ivNotifyAk02Circle = (ImageView)findViewById(R.id.ivNotifyAk02Circle);
                ivNotifyAk03Circle = (ImageView)findViewById(R.id.ivNotifyAk03Circle);
                ivNotifyConfirm = (ImageView)findViewById(R.id.ivNotifyConfirm);
                tvNotifyAk01 = (TextView)findViewById(R.id.tvNotifyAk01);
                tvNotifyAk02 = (TextView)findViewById(R.id.tvNotifyAk02);
                tvNotifyAk03 = (TextView)findViewById(R.id.tvNotifyAk03);
                tvNotifyConfirm = (TextView)findViewById(R.id.tvNotifyConfirm);

                btnNotifyConfirm.setTag(R.id.tag_dcre_plac_id, dialogMessage[0]);
                btnNotifyConfirm.setTag(R.id.tag_dcre_target_type, dialogMessage[1]);

                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));

                btnNotifyAk01.setOnClickListener(this);
                btnNotifyAk02.setOnClickListener(this);
                btnNotifyAk03.setOnClickListener(this);

                btnNotifyCloseLayout.setOnClickListener(this);
                btnNotifyConfirm.setOnClickListener(onClickListener);
                break;
            // 공유 타입
            case Constants.DIALOG_TYPE_SHARE:
                view = inflater.inflate(R.layout.dialog_share, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.PostDialogSlideAnimation;

                RelativeLayout rlShareLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlShareLayoutCloseBtn);
                rlShareLayoutCloseBtn.setOnClickListener(this);

                LinearLayout llShareImage = (LinearLayout)findViewById(R.id.llShareImage);
                LinearLayout llShareFacebook = (LinearLayout)findViewById(R.id.llShareFacebook);
                LinearLayout llShareTwitter = (LinearLayout)findViewById(R.id.llShareTwitter);
                LinearLayout llShareInstagram = (LinearLayout)findViewById(R.id.llShareInstagram);
                LinearLayout llShareLine = (LinearLayout)findViewById(R.id.llShareLine);
                LinearLayout llShareKakaoTalk = (LinearLayout)findViewById(R.id.llShareKakaoTalk);

                llShareImage.setOnClickListener(onClickListener);
                llShareFacebook.setOnClickListener(onClickListener);
                llShareTwitter.setOnClickListener(onClickListener);
                llShareInstagram.setOnClickListener(onClickListener);
                llShareLine.setOnClickListener(onClickListener);
                llShareKakaoTalk.setOnClickListener(onClickListener);
                break;
            // 공지 타입
            case Constants.DIALOG_TYPE_NOTICE:
                view = inflater.inflate(R.layout.dialog_notice, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.PostDialogSlideUpAnimation;

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // Dialog 호출시 배경화면이 검정색으로 바뀌는 것 막기
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                TextView tvNoticeMsg = (TextView)findViewById(R.id.tvNoticeMsg);
                tvNoticeMsg.setText((String)dialogMessage[0]);

                RelativeLayout rlNoticeLayoutCloseBtn = (RelativeLayout)findViewById(R.id.rlNoticeLayoutCloseBtn);
                rlNoticeLayoutCloseBtn.setOnClickListener(this);
                break;
            // Player 타이머 타입
            case Constants.DIALOG_TYPE_PLAYER_TIMER:
                view = inflater.inflate(R.layout.dialog_player_timer, null);
                setContentView(view);

                lp = new WindowManager.LayoutParams();
                lp.copyFrom(getWindow().getAttributes());
                //This makes the dialog take up the full width
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                getWindow().setAttributes(lp);
                //배경 투명하게 하기
                getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                //애니메이션 효과 주기
                getWindow().getAttributes().windowAnimations = R.style.PostDialogSlideAnimation;

                // Dialog 호출시 배경화면이 검정색으로 바뀌는 것 막기
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                View vPlayerTimerCancelBtn = findViewById(R.id.vPlayerTimerCancelBtn);
                vPlayerTimerCancelBtn.setOnClickListener(this);

                LinearLayout llPlayerTimerBtn = (LinearLayout)findViewById(R.id.llPlayerTimerBtn);
                llPlayerTimerBtn.setOnClickListener(onClickListener);

                LinearLayout llPlayerTimerMusicInfoBtn = (LinearLayout)findViewById(R.id.llPlayerTimerMusicInfoBtn);
                llPlayerTimerMusicInfoBtn.setOnClickListener(onClickListener);

                LinearLayout llPlayerTimerPutBtn = (LinearLayout)findViewById(R.id.llPlayerTimerPutBtn);
                llPlayerTimerPutBtn.setOnClickListener(onClickListener);

                LinearLayout llPlayerTimerShareBtn = (LinearLayout)findViewById(R.id.llPlayerTimerShareBtn);
                llPlayerTimerShareBtn.setOnClickListener(onClickListener);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 확인 타입의 취소 / OST 팝업 > 전체듣기 취소 / 신고하기 타입의 닫기
            case R.id.btnUpdateSupportCancel:
            case R.id.btnConfirmCancel:
            case R.id.btnPlayAllCancel:
            case R.id.btnPlayerAddCancel:
            case R.id.btnPlayerSortCancel:
            case R.id.btnNotifyCloseLayout:
            case R.id.rlSelectYearLayoutCloseBtn:
            case R.id.rlShareLayoutCloseBtn:
            case R.id.rlNoticeLayoutCloseBtn:
            case R.id.btnStampFilterCancel:
            case R.id.btnStampRequiredConfirm:
            case R.id.vPlayerTimerCancelBtn:
            case R.id.btnLocationServiceCancel:
            case R.id.btnStorySortCancel:
            case R.id.btnStoryTypeCancel:
            case R.id.btnRadioTitleCancel:
            case R.id.btnRadioOnAirCancel:
                dismiss();
                break;
            // 신고하기 타입 01 onClick
            case R.id.btnNotifyAk01:
                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                tvNotifyAk01.setTextColor(Color.parseColor("#FFD73D66"));
                tvNotifyAk02.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk03.setTextColor(Color.parseColor("#99000000"));
                btnNotifyConfirm.setTag(R.id.tag_dcre_resn_code, Constants.REQUEST_DCRE_RESN_CODE_AK01);
                updateNofityConfirmUI();
                break;
            // 신고하기 타입 02 onClick
            case R.id.btnNotifyAk02:
                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                tvNotifyAk01.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk02.setTextColor(Color.parseColor("#FFD73D66"));
                tvNotifyAk03.setTextColor(Color.parseColor("#99000000"));
                btnNotifyConfirm.setTag(R.id.tag_dcre_resn_code, Constants.REQUEST_DCRE_RESN_CODE_AK02);
                updateNofityConfirmUI();
                break;
            // 신고하기 타입 03 onClick
            case R.id.btnNotifyAk03:
                ivNotifyAk01Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk02Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#99000000")));
                ivNotifyAk03Circle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));
                tvNotifyAk01.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk02.setTextColor(Color.parseColor("#99000000"));
                tvNotifyAk03.setTextColor(Color.parseColor("#FFD73D66"));
                btnNotifyConfirm.setTag(R.id.tag_dcre_resn_code, Constants.REQUEST_DCRE_RESN_CODE_AK03);
                updateNofityConfirmUI();
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        LogUtil.e("" + newVal);
        llSelectYearConfirmBtn.setTag(newVal);
    }

    private void setNumberPickerDividerColor(NumberPicker numberPicker){
        final int count = numberPicker.getChildCount();

        for (int i = 0; i < count; i++) {
            try{
                Field dividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FFF2F2F2"));
                dividerField.set(numberPicker, colorDrawable);
                numberPicker.invalidate();
            } catch(NoSuchFieldException e){
                LogUtil.e(e.getMessage());
            } catch(IllegalAccessException e){
                LogUtil.e(e.getMessage());
            } catch(IllegalArgumentException e){
                LogUtil.e(e.getMessage());
            }
        }
    }

    void numberPickerTextColor( NumberPicker $v, int $c ){
        for(int i = 0, j = $v.getChildCount() ; i < j; i++){
            View t0 = $v.getChildAt(i);
            if( t0 instanceof EditText ){
                try{
                    Field t1 = $v.getClass() .getDeclaredField( "mSelectorWheelPaint" );
                    t1.setAccessible(true);
                    ((Paint)t1.get($v)) .setColor($c);
                    ((Paint)t1.get($v)) .setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, mContext.getResources().getDisplayMetrics()));
                    ((EditText)t0) .setTextColor($c);
                    ((EditText)t0) .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                    $v.invalidate();
                }catch(Exception e){}
            }
        }
    }

    private void updateNofityConfirmUI() {
        String notifyType = (String)btnNotifyConfirm.getTag(R.id.tag_dcre_resn_code);
        if (CommonUtil.isNull(notifyType)) {
            tvNotifyConfirm.setTextColor(Color.parseColor("#99000000"));
            ivNotifyConfirm.setImageResource(R.drawable.icon_chk_gray);
        } else {
            tvNotifyConfirm.setTextColor(Color.parseColor("#FF00AFD5"));
            ivNotifyConfirm.setImageResource(R.drawable.icon_chk_blue);
        }
    }

    // 신고하기 Layout 처리
    /*private void showChoiceNotifyLayout() {
        if(mChoiceNotifyLayout == null || isAnimationCheck == true) return;

        if (mChoiceNotifyLayout.getVisibility() == View.INVISIBLE) {
            mChoiceNotifyLayout.setVisibility(View.VISIBLE);
        }

        ObjectAnimator objectAnimator;
        if (!isChoiceNotifyVisible) {
            objectAnimator = ObjectAnimator.ofFloat(mChoiceNotifyLayout, "translationY", mChoiceNotifyLayout.getHeight(), 0.0f);
            mMainViewPager.setSwiping(false);
        }else{
            objectAnimator = ObjectAnimator.ofFloat(mChoiceNotifyLayout, "translationY", 0.0f, mChoiceNotifyLayout.getHeight());
            mMainViewPager.setSwiping(true);
        }

        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {isAnimationCheck = true;}

            @Override
            public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {isAnimationCheck = false;}

            @Override
            public void onAnimationCancel(Animator animation) {}
        });

        objectAnimator.setDuration(400);
        objectAnimator.start();
        objectAnimator = null;

        isChoiceNotifyVisible = !isChoiceNotifyVisible;
    }*/
}
