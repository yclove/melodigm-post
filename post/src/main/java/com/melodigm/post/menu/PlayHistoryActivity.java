package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.AddCabinetMusicItem;
import com.melodigm.post.protocol.data.AddCabinetMusicReq;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.search.SearchMusicInterface;
import com.melodigm.post.search.SearchSongAdapter;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayHistoryActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private SearchSongAdapter mSearchSongAdapter;
    private ArrayList<OstDataItem> arrSongDataItem;
    private View viewOstHeader, viewOstFooter;
    private ListView lvDataList;
    private LinearLayout llMusicSelectFooter, llEmptyLayout;
    private TextView tvRightCnt, tvOstSelectCnt;

    private String mBXI;
    private ArrayList<AddCabinetMusicItem> arrAddCabinetMusicItem = new ArrayList<>();
    private RelativeLayout rlBuyUseCouponBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_play_history);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        mdbHelper = new PostDatabases(this);
        mdbHelper.open();
        arrSongDataItem = mdbHelper.getOstPlayHistory();
        mdbHelper.close();

        setDisplay();
	}

	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.play_history), false);

        viewOstHeader = getLayoutInflater().inflate(R.layout.view_ost_header, null, false);
        viewOstFooter = getLayoutInflater().inflate(R.layout.view_ost_footer, null, false);
        lvDataList = (ListView)findViewById(R.id.lvDataList);
        llEmptyLayout = (LinearLayout)findViewById(R.id.llEmptyLayout);
        llMusicSelectFooter = (LinearLayout)findViewById(R.id.llMusicSelectFooter);
        tvRightCnt = (TextView)findViewById(R.id.tvRightCnt);
        tvOstSelectCnt = (TextView)findViewById(R.id.tvOstSelectCnt);
        rlBuyUseCouponBtn = (RelativeLayout)findViewById(R.id.rlBuyUseCouponBtn);
        rlBuyUseCouponBtn.setOnClickListener(onGlobalClickListener);

        mSearchSongAdapter = new SearchSongAdapter(mContext, R.layout.adapter_search_song, arrSongDataItem, mGlideRequestManager);

        lvDataList.addHeaderView(viewOstHeader, null, false);
        lvDataList.setAdapter(mSearchSongAdapter);
        mSearchSongAdapter.setOnListItemClickListener(new SearchMusicInterface() {
            @Override
            public void onItemClick(int page, int position) {
                int checkedOstDataItemCount = 0;

                arrSongDataItem.get(position).setIsChecked(!arrSongDataItem.get(position).isChecked());
                for (OstDataItem item : arrSongDataItem) {
                    if (item.isChecked()) checkedOstDataItemCount++;
                }

                // 선택한 OST 수보다 보유한 이용권 잔여 재생 가능수가 많을 경우 (오버되었을 경우)
                if (checkedOstDataItemCount > Constants.RIGHT_COUNT) {
                    tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.post));
                    rlBuyUseCouponBtn.setVisibility(View.VISIBLE);
                } else {
                    tvRightCnt.setTextColor(ContextCompat.getColor(mContext, R.color.radio));
                    rlBuyUseCouponBtn.setVisibility(View.GONE);
                }

                tvRightCnt.setText(String.valueOf(Constants.RIGHT_COUNT));
                tvOstSelectCnt.setText(String.valueOf(checkedOstDataItemCount));
                mSearchSongAdapter.notifyDataSetChanged();

                if (checkedOstDataItemCount > 0) {
                    lvDataList.addFooterView(viewOstFooter, null, false);
                    llMusicSelectFooter.setVisibility(View.VISIBLE);
                } else {
                    lvDataList.removeFooterView(viewOstFooter);
                    llMusicSelectFooter.setVisibility(View.GONE);
                }
            }
        });

        if (arrSongDataItem.size() > 0) {
            lvDataList.setVisibility(View.VISIBLE);
            llEmptyLayout.setVisibility(View.GONE);
        } else {
            lvDataList.setVisibility(View.GONE);
            llEmptyLayout.setVisibility(View.VISIBLE);
        }
	}

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle data = new Bundle();
        Message msg = new Message();
        String POI = "", SSI = "";
        boolean isChecked = false;
        int checkedCount = 0;

        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // Footer > 듣기 onClick
            case R.id.llMusicListeningBtn:
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                for(OstDataItem item : arrSongDataItem) {
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
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, getString(R.string.msg_ost_add_play_list, checkedCount));
                    mPostDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        }
                    }, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    intent.putExtra("POI", POI);
                    intent.putExtra("SSI", SSI);
                    startService(intent);
                } else {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_required_music_selected));
                }
                break;
            // Footer > 추가 onClick
            case R.id.llMusicAddBtn:
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                for(OstDataItem item : arrSongDataItem) {
                    if (item.isChecked()) {
                        isChecked = true;
                        checkedCount++;
                        mdbHelper.updateOstPlayList(item);
                    }
                }
                mdbHelper.close();

                if (isChecked) {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_NOTICE, this, mContext.getString(R.string.msg_ost_add_play_list, checkedCount));
                    mPostDialog.show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        }
                    }, 3000);

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_PUT);
                    mContext.startService(intent);
                } else {
                    DeviceUtil.showToast(mContext, getString(R.string.msg_required_music_selected));
                }
                break;
            // Footer > 담기 onClick
            case R.id.llMusicPutBtn:
                if (arrSongDataItem.size() > 0) {
                    intent = new Intent(mContext, CabinetActivity.class);
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, Constants.REQUEST_CABINET_TYPE_PUT);
                    startActivityForResult(intent, Constants.QUERY_CABINET_DATA);
                }
                break;
        }
    }

    private void getData(int queryType, Object... args) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

    if (queryType == Constants.QUERY_ADD_CABINET) {
            addCabinetMusicThread();
        }
    }

    public Thread addCabinetMusicThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    arrAddCabinetMusicItem.clear();
                    for (OstDataItem item : arrSongDataItem) {
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

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        Bundle data = msg.getData();

        switch(msg.what) {
            // 보관함에 담기 후 Handler
            case Constants.QUERY_ADD_CABINET:
                DeviceUtil.showToast(mContext, getString(R.string.msg_add_cabinet_result));
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Footer > 담기 > 보관함 이동 후 ActivityResult
            case Constants.QUERY_CABINET_DATA:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    String mCabinetType = data.getStringExtra(Constants.REQUEST_CABINET_TYPE);
                    if (CommonUtil.isNotNull(mCabinetType)) {
                        if (Constants.REQUEST_CABINET_TYPE_PUT.equals(mCabinetType)) {
                            mBXI = data.getStringExtra("BXI");
                            if (CommonUtil.isNotNull(mBXI)) {
                                getData(Constants.QUERY_ADD_CABINET);
                            }
                        }
                    }
                }
                break;
        }
    }
}
