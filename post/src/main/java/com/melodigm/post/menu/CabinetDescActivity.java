package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.AddCabinetMusicItem;
import com.melodigm.post.protocol.data.AddCabinetMusicReq;
import com.melodigm.post.protocol.data.CabinetDataItem;
import com.melodigm.post.protocol.data.CabinetMusicDataItem;
import com.melodigm.post.protocol.data.GetCabinetDeleteReq;
import com.melodigm.post.protocol.data.GetCabinetDescReq;
import com.melodigm.post.protocol.data.GetCabinetDescRes;
import com.melodigm.post.protocol.data.OstSortDataItem;
import com.melodigm.post.protocol.data.SetCabinetSortReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CabinetDescActivity extends BaseActivity implements IOnHandlerMessage {
    private static final int DISPLAY_MODE_LIST = 1;
    private static final int DISPLAY_MODE_EDIT = 2;

    private int mDiaplayType = DISPLAY_MODE_LIST;
	private CabinetDataItem mCabinetDataItem;   // 보관함 상세 진입 시 전달 Object
    private String mBXI = "";
    private String deleteType;
    private ArrayList<AddCabinetMusicItem> arrAddCabinetMusicItem;
    private GetCabinetDescRes getCabinetDescRes;
    private ArrayList<CabinetMusicDataItem> arrCabinetMusicDataItem;
    private CabinetDescAdapter mCabinetDescAdapter;
    private CircularImageView btnCabinetImage;
    private ImageView ivCabinetRoot, ivCabinetImagePopup;
    private TextView tvCabinetName, tvCabinetDesc, tvCabinetSelectAll, tvCabinetTotalCnt, tvRightCnt, tvSelectCnt, tvEditSelectCnt;
    private DragSortListView lvCabinetMusicList;
    private SetCabinetSortReq setCabinetSortReq;
    private LinearLayout llCabinetListEmpty, llCabinetDelBtn, llCabinetSelectAll, llCabinetPlayAll, llCabinetHeaderEditBtn, llCabinetHeaderEditCloseBtn, llCabinetHeader, llCabinetSelectFooter, llCabinetSelectEditFooter, llMusicListeningBtn, llMusicAddBtn, llMusicPutBtn, llDeleteBtn, llCabinetEditBtn;
    private RelativeLayout rlCabinetImagePopup, rlBuyUseCouponBtn;
    private boolean isSelectedAll = true;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        CabinetMusicDataItem item = mCabinetDescAdapter.getItem(from);
                        mCabinetDescAdapter.remove(item);
                        mCabinetDescAdapter.insert(item, to);
                        getData(Constants.QUERY_CABINET_SORT);
                    }
                }
            };

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cabinet_desc);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        Intent intent = getIntent();
        mCabinetDataItem = intent.getParcelableExtra("CabinetDataItem");

        if (mCabinetDataItem != null) {
            setDisplay();
        } else {
            DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_required_data));
            finish();
        }
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.menu_cabinet), false);

        llCabinetListEmpty = (LinearLayout)findViewById(R.id.llCabinetListEmpty);

        // 보관함 이미지 팝업
        rlCabinetImagePopup = (RelativeLayout)findViewById(R.id.rlCabinetImagePopup);
        rlCabinetImagePopup.setOnClickListener(onClickListener);
        ivCabinetImagePopup = (ImageView) findViewById(R.id.ivCabinetImagePopup);

        // 보관함 수정 버튼
        llCabinetEditBtn = (LinearLayout)findViewById(R.id.llCabinetEditBtn);
        llCabinetEditBtn.setOnClickListener(onClickListener);

        // 보관함 Header
        llCabinetHeader = (LinearLayout)findViewById(R.id.llCabinetHeader);

        // 보관함 Header > 삭제 버튼
        llCabinetDelBtn = (LinearLayout)findViewById(R.id.llCabinetDelBtn);
        llCabinetDelBtn.setOnClickListener(onClickListener);

        // 보관함 Header > 전체선택
        llCabinetSelectAll = (LinearLayout)findViewById(R.id.llCabinetSelectAll);
        llCabinetSelectAll.setOnClickListener(onClickListener);

        // 보관함 Header > 전체듣기
        llCabinetPlayAll = (LinearLayout)findViewById(R.id.llCabinetPlayAll);
        llCabinetPlayAll.setOnClickListener(onClickListener);

        // 보관함 Header > 편집
        llCabinetHeaderEditBtn = (LinearLayout)findViewById(R.id.llCabinetHeaderEditBtn);
        llCabinetHeaderEditBtn.setOnClickListener(onClickListener);

        // 보관함 Header > 편집 닫기
        llCabinetHeaderEditCloseBtn = (LinearLayout)findViewById(R.id.llCabinetHeaderEditCloseBtn);
        llCabinetHeaderEditCloseBtn.setOnClickListener(onClickListener);

        // 보관함 이미지
        ivCabinetRoot = (ImageView)findViewById(R.id.ivCabinetRoot);
        btnCabinetImage = (CircularImageView)findViewById(R.id.btnCabinetImage);
        if (CommonUtil.isNotNull(mCabinetDataItem.getUBX_IMG())) {
            btnCabinetImage.setOnClickListener(onClickListener);
            mGlideRequestManager
                .load(mCabinetDataItem.getUBX_IMG())
                .override(DeviceUtil.getScreenWidthInPXs(mContext), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 269, mContext.getResources().getDisplayMetrics()))
                .into(ivCabinetRoot);

            mGlideRequestManager
                .load(mCabinetDataItem.getUBX_IMG())
                .error(R.drawable.icon_album_dummy)
                .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                .into(btnCabinetImage);

            mGlideRequestManager
                .load(mCabinetDataItem.getUBX_IMG())
                .into(ivCabinetImagePopup);
        }

        tvCabinetName = (TextView)findViewById(R.id.tvCabinetName);
        tvCabinetName.setText(mCabinetDataItem.getUBX_NM());
        tvCabinetDesc = (TextView)findViewById(R.id.tvCabinetDesc);
        tvCabinetDesc.setText(mCabinetDataItem.getDESCR());

        // 전체선택 / 전체해제
        tvCabinetSelectAll = (TextView)findViewById(R.id.tvCabinetSelectAll);

        // 보관함 곡 수
        tvCabinetTotalCnt = (TextView)findViewById(R.id.tvCabinetTotalCnt);

        // 선택 Footer
        llCabinetSelectFooter = (LinearLayout)findViewById(R.id.llCabinetSelectFooter);
        llCabinetSelectEditFooter = (LinearLayout)findViewById(R.id.llCabinetSelectEditFooter);
        rlBuyUseCouponBtn = (RelativeLayout) findViewById(R.id.rlBuyUseCouponBtn);
        rlBuyUseCouponBtn.setOnClickListener(onGlobalClickListener);

        // Footer 듣기 / 추가 / 담기 / 삭제
        llMusicListeningBtn = (LinearLayout)findViewById(R.id.llMusicListeningBtn);
        llMusicListeningBtn.setOnClickListener(onClickListener);
        llMusicAddBtn = (LinearLayout)findViewById(R.id.llMusicAddBtn);
        llMusicAddBtn.setOnClickListener(onClickListener);
        llMusicPutBtn = (LinearLayout)findViewById(R.id.llMusicPutBtn);
        llMusicPutBtn.setOnClickListener(onClickListener);
        llDeleteBtn = (LinearLayout)findViewById(R.id.llDeleteBtn);
        llDeleteBtn.setOnClickListener(onClickListener);

        // 전체 곡수 / 선택 곡수
        tvRightCnt = (TextView)findViewById(R.id.tvRightCnt);
        tvSelectCnt = (TextView)findViewById(R.id.tvSelectCnt);
        tvEditSelectCnt = (TextView)findViewById(R.id.tvEditSelectCnt);

        setCabinetSortReq = new SetCabinetSortReq();
        arrCabinetMusicDataItem = new ArrayList<>();
        arrAddCabinetMusicItem = new ArrayList<>();
        lvCabinetMusicList = (DragSortListView)findViewById(R.id.lvCabinetMusicList);
        mCabinetDescAdapter = new CabinetDescAdapter(mContext, R.layout.adapter_menu_cabinet_desc, arrCabinetMusicDataItem, mGlideRequestManager);
        lvCabinetMusicList.setAdapter(mCabinetDescAdapter);
        lvCabinetMusicList.setDropListener(onDrop);
        lvCabinetMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                if (mDiaplayType == DISPLAY_MODE_LIST) {
                    int checkedCount = 0;

                    mCabinetDescAdapter.getItem(position).setChecked(!mCabinetDescAdapter.getItem(position).isChecked());
                    for (int i = 0; i < mCabinetDescAdapter.getCount(); i++) {
                        if (mCabinetDescAdapter.getItem(i).isChecked()) checkedCount++;
                    }

                    // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                    if (checkedCount > Constants.RIGHT_COUNT) {
                        tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.post));
                        rlBuyUseCouponBtn.setVisibility(View.VISIBLE);
                    } else {
                        tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                        rlBuyUseCouponBtn.setVisibility(View.GONE);
                    }

                    tvSelectCnt.setText(String.valueOf(checkedCount));
                    tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));

                    if (checkedCount > 0) {
                        llCabinetSelectFooter.setVisibility(View.VISIBLE);
                        tvCabinetSelectAll.setText(getString(R.string.common_unselect_all));
                        isSelectedAll = false;
                    } else {
                        llCabinetSelectFooter.setVisibility(View.GONE);
                        tvCabinetSelectAll.setText(getString(R.string.common_select_all));
                        isSelectedAll = true;
                    }

                    mCabinetDescAdapter.notifyDataSetChanged();
                } else if (mDiaplayType == DISPLAY_MODE_EDIT) {
                    int checkedCount = 0;

                    mCabinetDescAdapter.getItem(position).setChecked(!mCabinetDescAdapter.getItem(position).isChecked());
                    for (int i = 0; i < mCabinetDescAdapter.getCount(); i++) {
                        if (mCabinetDescAdapter.getItem(i).isChecked()) checkedCount++;
                    }
                    tvEditSelectCnt.setText(getString(R.string.n_music_select_count, checkedCount));

                    if (checkedCount > 0) {
                        llCabinetSelectEditFooter.setVisibility(View.VISIBLE);
                        tvCabinetSelectAll.setText(getString(R.string.common_unselect_all));
                        isSelectedAll = false;
                    } else {
                        llCabinetSelectEditFooter.setVisibility(View.GONE);
                        tvCabinetSelectAll.setText(getString(R.string.common_select_all));
                        isSelectedAll = true;
                    }

                    mCabinetDescAdapter.notifyDataSetChanged();
                }
            }
        });

        getData(Constants.QUERY_CABINET_DESC);
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            boolean isChecked = false;
            int checkedCount = 0;
            String POI = "", SSI = "";

            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // 보관함 사진 onClick
                case R.id.btnCabinetImage:
                    rlCabinetImagePopup.setVisibility(View.VISIBLE);
                    break;
                // 보관함 사진 팝업 onClick
                case R.id.rlCabinetImagePopup:
                    rlCabinetImagePopup.setVisibility(View.GONE);
                    break;
                // 보관함 수정 버튼 onClick
                case R.id.llCabinetEditBtn:
                    intent = new Intent(mContext, RegistCabinetActivity.class);
                    intent.putExtra("CabinetDataItem", mCabinetDataItem);
                    startActivityForResult(intent, Constants.QUERY_WRITE_CABINET);
                    break;
                // 보관함 삭제 버튼 onClick
                case R.id.llCabinetDelBtn:
                    deleteType = "CABINET";
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete_cabinet));
                    mPostDialog.show();
                    break;
                // Footer 삭제 onClick
                case R.id.llDeleteBtn:
                    for (int i = 0; i < mCabinetDescAdapter.getCount(); i++) {
                        if (isChecked) break;
                        if (mCabinetDescAdapter.getItem(i).isChecked()) isChecked = true;
                    }

                    if (isChecked) {
                        deleteType = "MUSIC";
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete));
                        mPostDialog.show();
                    } else {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, this, mContext.getResources().getString(R.string.dialog_info_player_select_empty_music));
                        mPostDialog.show();
                    }
                    break;
                // 보관함 삭제 / Footer 노래 삭제 확인 onClick
                case R.id.btnConfirmConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    if ("CABINET".equals(deleteType)) {
                        getData(Constants.QUERY_CABINET_DELETE);
                    } else {
                        if (mCabinetDescAdapter.getCount() > 0) {
                            getData(Constants.QUERY_CABINET_SORT, "DELETE");
                        }
                    }
                    break;
                // 전체선택 / 전체해제 onClick
                case R.id.llCabinetSelectAll:
                    if (mCabinetDescAdapter.getCount() > 0) {
                        int checkedOstDataItemCount = 0;

                        // 전체 선택
                        if (isSelectedAll) {
                            checkedOstDataItemCount = mCabinetDescAdapter.getCount();

                            for (int i = 0; i < mCabinetDescAdapter.getCount(); i++)
                                mCabinetDescAdapter.getItem(i).setChecked(true);
                            tvCabinetSelectAll.setText(getString(R.string.common_unselect_all));
                            mCabinetDescAdapter.notifyDataSetChanged();

                            tvSelectCnt.setText(String.valueOf(mCabinetDescAdapter.getCount()));
                            tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));
                            llCabinetSelectFooter.setVisibility(View.VISIBLE);
                        }
                        // 전체 해제
                        else {
                            for (int i = 0; i < mCabinetDescAdapter.getCount(); i++)
                                mCabinetDescAdapter.getItem(i).setChecked(false);
                            tvCabinetSelectAll.setText(getString(R.string.common_select_all));
                            mCabinetDescAdapter.notifyDataSetChanged();

                            tvSelectCnt.setText("0");
                            tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));
                            llCabinetSelectFooter.setVisibility(View.GONE);
                        }

                        // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                        if (checkedOstDataItemCount > Constants.RIGHT_COUNT) {
                            tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.post));
                            rlBuyUseCouponBtn.setVisibility(View.VISIBLE);
                        } else {
                            tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                            rlBuyUseCouponBtn.setVisibility(View.GONE);
                        }

                        isSelectedAll = !isSelectedAll;
                    }
                    break;
                // 전체듣기 onClick
                case R.id.llCabinetPlayAll:
                    if (mCabinetDescAdapter.getCount() > 0) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAY_ALL, onClickListener);
                        mPostDialog.show();
                    }
                    break;
                // 전체듣기 > 재생 목록에 추가하고 듣기 onClick
                case R.id.btnPlayAllAddList:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    for(CabinetMusicDataItem item : arrCabinetMusicDataItem) {
                        isChecked = true;
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getResources().getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        intent.putExtra("POI", POI);
                        intent.putExtra("SSI", SSI);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_required_music_selected));
                    }
                    break;
                // 전체듣기 > 재생 목록을 교체하고 듣기 onClick
                case R.id.btnPlayAllChangeList:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    POI = "";
                    SSI = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mdbHelper.deleteAllOstPlayList();
                    for(CabinetMusicDataItem item : arrCabinetMusicDataItem) {
                        isChecked = true;
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getResources().getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        intent.putExtra("POI", POI);
                        intent.putExtra("SSI", SSI);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_required_music_selected));
                    }
                    break;
                // 편집 onClick
                case R.id.llCabinetHeaderEditBtn:
                    if (mCabinetDescAdapter.getCount() > 0) {
                        mDiaplayType = DISPLAY_MODE_EDIT;
                        mCabinetDescAdapter.setDisplayType(mDiaplayType);
                        mCabinetDescAdapter.notifyDataSetChanged();

                        llCabinetPlayAll.setVisibility(View.GONE);
                        llCabinetHeaderEditBtn.setVisibility(View.GONE);
                        llCabinetHeaderEditCloseBtn.setVisibility(View.VISIBLE);

                        for (int i = 0; i < mCabinetDescAdapter.getCount(); i++)
                            mCabinetDescAdapter.getItem(i).setChecked(false);
                        tvCabinetSelectAll.setText(getString(R.string.common_select_all));
                        mCabinetDescAdapter.notifyDataSetChanged();

                        tvSelectCnt.setText("0");
                        tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));
                        llCabinetSelectFooter.setVisibility(View.GONE);

                        isSelectedAll = true;
                    }
                    break;
                // 편집 종료 onClick
                case R.id.llCabinetHeaderEditCloseBtn:
                    if (mCabinetDescAdapter.getCount() > 0) {
                        mDiaplayType = DISPLAY_MODE_LIST;
                        mCabinetDescAdapter.setDisplayType(mDiaplayType);
                        mCabinetDescAdapter.notifyDataSetChanged();

                        llCabinetPlayAll.setVisibility(View.VISIBLE);
                        llCabinetHeaderEditBtn.setVisibility(View.VISIBLE);
                        llCabinetHeaderEditCloseBtn.setVisibility(View.GONE);

                        for (int i = 0; i < mCabinetDescAdapter.getCount(); i++)
                            mCabinetDescAdapter.getItem(i).setChecked(false);
                        tvCabinetSelectAll.setText(getString(R.string.common_select_all));
                        mCabinetDescAdapter.notifyDataSetChanged();

                        tvEditSelectCnt.setText(getString(R.string.n_music_select_count, 0));
                        llCabinetSelectEditFooter.setVisibility(View.GONE);

                        isSelectedAll = true;
                    }
                    break;
                // Footer > 듣기 onClick
                case R.id.llMusicListeningBtn:
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    for(CabinetMusicDataItem item : arrCabinetMusicDataItem) {
                        if (item.isChecked()) {
                            isChecked = true;
                            if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                                POI = item.getPOI();
                                SSI = item.getSSI();
                            }
                            checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                        }
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getResources().getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                        intent.putExtra("POI", POI);
                        intent.putExtra("SSI", SSI);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_required_music_selected));
                    }
                    break;
                // Footer > 추가 onClick
                case R.id.llMusicAddBtn:
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    for(CabinetMusicDataItem item : arrCabinetMusicDataItem) {
                        if (item.isChecked()) {
                            isChecked = true;
                            checkedCount = checkedCount + mdbHelper.updateOstPlayList(item);
                        }
                    }
                    mdbHelper.close();

                    if (isChecked) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, onClickListener, getResources().getString(R.string.msg_ost_add_play_list, checkedCount));
                        mPostDialog.show();
                        mHandler.sendEmptyMessageDelayed(Constants.DIALOG_TYPE_NOTICE_CLOSE, 3000);

                        intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                        intent.setPackage(Constants.SERVICE_PACKAGE);
                        intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_PUT);
                        startService(intent);
                    } else {
                        DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_required_music_selected));
                    }
                    break;
                // Footer > 담기 onClick
                case R.id.llMusicPutBtn:
                    for(CabinetMusicDataItem item : arrCabinetMusicDataItem) {
                        if (item.isChecked()) {
                            isChecked = true;
                            break;
                        }
                    }

                    if (isChecked) {
                        intent = new Intent(mContext, CabinetActivity.class);
                        intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                        startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                    } else {
                        DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_required_music_selected));
                    }
                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_CABINET_DESC) {
            thread = getCabinetDescThread();
        } else if (queryType == Constants.QUERY_CABINET_DELETE) {
            thread = getCabinetDeleteThread();
        } else if (queryType == Constants.QUERY_ADD_CABINET) {
            thread = addCabinetMusicThread();
        } else if (queryType == Constants.QUERY_CABINET_SORT) {
            if (args != null && args.length == 1 && args[0] instanceof String)
                thread = setCabinetSortThread((String)args[0]);
            else
                thread = setCabinetSortThread("SORT");
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getCabinetDescThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetCabinetDescReq getCabinetDescReq = new GetCabinetDescReq();
                    getCabinetDescReq.setBXI(mCabinetDataItem.getBXI());
                    getCabinetDescRes = request.getCabinetDesc(getCabinetDescReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_CABINET_DESC);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getCabinetDeleteThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetCabinetDeleteReq getCabinetDeleteReq = new GetCabinetDeleteReq();
                    getCabinetDeleteReq.setBXI(mCabinetDataItem.getBXI());
                    request.getCabinetDelete(getCabinetDeleteReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_CABINET_DELETE);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread addCabinetMusicThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    arrAddCabinetMusicItem.clear();
                    for (CabinetMusicDataItem item : arrCabinetMusicDataItem) {
                        if (item.isChecked()) {
                            AddCabinetMusicItem addCabinetMusicItem = new AddCabinetMusicItem();
                            addCabinetMusicItem.setSSI(item.getSSI());
                            addCabinetMusicItem.setOTI(item.getOTI());
                            arrAddCabinetMusicItem.add(addCabinetMusicItem);
                        }
                    }

                    AddCabinetMusicReq addCabinetMusicReq = new AddCabinetMusicReq();
                    addCabinetMusicReq.setBXI(mBXI);
                    addCabinetMusicReq.setAddCabinetMusicItem(arrAddCabinetMusicItem);
                    request.addCabinetMusic(addCabinetMusicReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_ADD_CABINET);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread setCabinetSortThread(final String sortType) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    setCabinetSortReq.clearList();
                    OstSortDataItem ostSortDataItem;
                    if ("SORT".equals(sortType)) {
                        for (int i = 0; i < mCabinetDescAdapter.getCount(); i++) {
                            ostSortDataItem = new OstSortDataItem();
                            ostSortDataItem.setSSI(mCabinetDescAdapter.getItem(i).getSSI());
                            ostSortDataItem.setOTI(mCabinetDescAdapter.getItem(i).getOTI());
                            ostSortDataItem.setDEL_YN("N");
                            ostSortDataItem.setSORT_ORDER(i + 1);
                            setCabinetSortReq.setSOS_LIST(ostSortDataItem);
                        }
                    } else {
                        for (int i = 0; i < mCabinetDescAdapter.getCount(); i++) {
                            if (mCabinetDescAdapter.getItem(i).isChecked()) {
                                ostSortDataItem = new OstSortDataItem();
                                ostSortDataItem.setSSI(mCabinetDescAdapter.getItem(i).getSSI());
                                ostSortDataItem.setOTI(mCabinetDescAdapter.getItem(i).getOTI());
                                ostSortDataItem.setDEL_YN("Y");
                                ostSortDataItem.setSORT_ORDER(i + 1);
                                setCabinetSortReq.setSOS_LIST(ostSortDataItem);
                            }
                        }
                    }

                    setCabinetSortReq.setBXI(mCabinetDataItem.getBXI());
                    request.setCabinetSort(setCabinetSortReq);
                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putString("sortType", sortType);
                    msg.setData(data);
                    msg.what = Constants.QUERY_CABINET_SORT;
                    mHandler.sendMessage(msg);
                } catch (RequestException e) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
                } catch (POSTException e) {
                    mPOSTException = e;

                    if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                    } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                    } else {
                        mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            // 보관함 조회 후 Handler
            case Constants.QUERY_CABINET_DESC:
                if (getCabinetDescRes.getCabinetMusicDataItem().size() > 0) {
                    llCabinetHeader.setAlpha(1.0f);
                    llCabinetListEmpty.setVisibility(View.GONE);
                    lvCabinetMusicList.setVisibility(View.VISIBLE);

                    arrCabinetMusicDataItem.clear();
                    for(CabinetMusicDataItem item : getCabinetDescRes.getCabinetMusicDataItem()) {
                        arrCabinetMusicDataItem.add(item);
                    }
                    mCabinetDescAdapter.notifyDataSetChanged();

                    tvCabinetTotalCnt.setText(getString(R.string.n_cabinet_music_count, getCabinetDescRes.getCabinetMusicDataItem().size()));
                } else {
                    llCabinetHeader.setAlpha(0.3f);
                    llCabinetListEmpty.setVisibility(View.VISIBLE);
                    lvCabinetMusicList.setVisibility(View.GONE);

                    tvCabinetTotalCnt.setText(getString(R.string.n_cabinet_music_count, getCabinetDescRes.getCabinetMusicDataItem().size()));
                }
                break;
            // 보관함 삭제 후 Handler
            case Constants.QUERY_CABINET_DELETE:
                setResult(Constants.RESULT_SUCCESS);
                finish();
                break;
            // 정렬 / 선택 삭제 후 Handler
            case Constants.QUERY_CABINET_SORT:
                String sortType = msg.getData().getString("sortType", "");
                if ("DELETE".equals(sortType)) {
                    tvCabinetSelectAll.setText(getString(R.string.common_select_all));
                    tvEditSelectCnt.setText(getString(R.string.n_music_select_count, 0));
                    llCabinetSelectEditFooter.setVisibility(View.GONE);
                    isSelectedAll = true;
                    getData(Constants.QUERY_CABINET_DESC);
                }
                break;
            // 보관함에 담기 Handler
            case Constants.QUERY_ADD_CABINET:
                DeviceUtil.showToast(mContext, getResources().getString(R.string.msg_add_cabinet_result));
                break;
            // Notice Dialog Dismiss Handler
            case Constants.DIALOG_TYPE_NOTICE_CLOSE:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, true).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.QUERY_WRITE_CABINET:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    getData(Constants.QUERY_CABINET_DESC);
                }
                break;
            // Footer > 담기 > 보관함 이동 후 ActivityResult
            case Constants.QUERY_CABINET_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    mBXI = data.getStringExtra("BXI");
                    if (CommonUtil.isNotNull(mBXI)) {
                        getData(Constants.QUERY_ADD_CABINET);
                    }
                }
                break;
        }
    }
}
