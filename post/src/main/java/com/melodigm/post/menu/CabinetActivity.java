package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.CabinetDataItem;
import com.melodigm.post.protocol.data.GetCabinetDataRes;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CabinetActivity extends BaseActivity implements IOnHandlerMessage {
	private LinearLayout llCreateCabinetBtn, llCabinetListEmpty;
    private GetCabinetDataRes getCabinetDataRes;
    private ArrayList<CabinetDataItem> arrCabinetDataItem;
    private CabinetAdapter mCabinetAdapter;
    private ListView lvCabinetList;
    private String mCabinetType = Constants.REQUEST_CABINET_TYPE_PUT;
    private String mBXI = "";
    private int mPosition = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cabinet);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        Intent intent = getIntent();
        mCabinetType = intent.getStringExtra(Constants.REQUEST_CABINET_TYPE);

        setDisplay();
	}

    private void setHeaderCheckImage() {
        if (Constants.REQUEST_CABINET_TYPE_PUT.equals(mCabinetType)) {
            if (CommonUtil.isNull(mBXI)) {
                btnHeaderCheck.setAlpha(0.2f);
                btnHeaderCheck.setOnClickListener(null);
            } else {
                btnHeaderCheck.setAlpha(1.0f);
                btnHeaderCheck.setOnClickListener(onClickListener);
            }
        } else {
            btnHeaderCheck.setAlpha(0.0f);
            btnHeaderCheck.setOnClickListener(null);
        }
    }

	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.menu_cabinet), false);
        setHeaderCheckImage();

        llCreateCabinetBtn = (LinearLayout) findViewById(R.id.llCreateCabinetBtn);
        llCreateCabinetBtn.setOnClickListener(onClickListener);

        llCabinetListEmpty = (LinearLayout) findViewById(R.id.llCabinetListEmpty);
        lvCabinetList = (ListView) findViewById(R.id.lvCabinetList);
        arrCabinetDataItem = new ArrayList<>();
        mCabinetAdapter = new CabinetAdapter(mContext, R.layout.adapter_menu_cabinet, arrCabinetDataItem, mCabinetType, mHandler, mGlideRequestManager);
        lvCabinetList.setAdapter(mCabinetAdapter);
        lvCabinetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                if (Constants.REQUEST_CABINET_TYPE_PUT.equals(mCabinetType)) {
                    for (CabinetDataItem item : arrCabinetDataItem) {
                        item.setIsChecked(false);
                    }
                    arrCabinetDataItem.get(position).setIsChecked(true);
                    mCabinetAdapter.notifyDataSetChanged();

                    mBXI = arrCabinetDataItem.get(position).getBXI();
                    setHeaderCheckImage();
                } else {
                    Intent intent = new Intent(mContext, CabinetDescActivity.class);
                    intent.putExtra("CabinetDataItem", mCabinetAdapter.getItem(position));
                    startActivityForResult(intent, Constants.QUERY_CABINET_DESC);
                }
            }
        });

        getData(Constants.QUERY_CABINET_DATA);
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            int updateCount;
            String POI, SSI;

            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.btnHeaderCheck:
                    if (CommonUtil.isNotNull(mBXI)) {
                        intent = getIntent();
                        intent.putExtra("BXI", mBXI);
                        intent.putExtra(Constants.REQUEST_CABINET_TYPE, mCabinetType);
                        setResult(Constants.RESULT_SUCCESS, intent);
                        finish();
                    }
                    break;
                // 보관함 만들기 onClick
                case R.id.llCreateCabinetBtn:
                    startActivityForResult(new Intent(mContext, RegistCabinetActivity.class), Constants.QUERY_WRITE_CABINET);
                    break;
                // 보관함 재생 > 재생 목록에 추가하고 듣기 onClick
                case R.id.btnPlayAllAddList:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    POI = "";
                    SSI = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    updateCount = 0;
                    for(OstDataItem item : arrCabinetDataItem.get(mPosition).getArrOstDataItem()) {
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        updateCount = updateCount + mdbHelper.updateOstPlayList(item);
                    }
                    mdbHelper.close();

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    intent.putExtra("POI", POI);
                    intent.putExtra("SSI", SSI);
                    startService(intent);

                    intent = getIntent();
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, mCabinetType);
                    intent.putExtra("UPDATE_COUNT", updateCount);
                    setResult(Constants.RESULT_SUCCESS, intent);
                    finish();
                    break;
                // 보관함 재생 > 재생 목록을 교체하고 듣기 onClick
                case R.id.btnPlayAllChangeList:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    POI = "";
                    SSI = "";
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mdbHelper.deleteAllOstPlayList();
                    updateCount = 0;
                    for(OstDataItem item : arrCabinetDataItem.get(mPosition).getArrOstDataItem()) {
                        if (CommonUtil.isNull(POI) && CommonUtil.isNull(SSI)) {
                            POI = item.getPOI();
                            SSI = item.getSSI();
                        }
                        updateCount = updateCount + mdbHelper.updateOstPlayList(item);
                    }
                    mdbHelper.close();

                    intent = new Intent("com.melodigm.post.service.MusicService.LAUNCHER");
                    intent.setPackage(Constants.SERVICE_PACKAGE);
                    intent.putExtra(Constants.MPS_COMMAND, Constants.MPS_COMMAND_ADD);
                    intent.putExtra("POI", POI);
                    intent.putExtra("SSI", SSI);
                    startService(intent);

                    intent = getIntent();
                    intent.putExtra(Constants.REQUEST_CABINET_TYPE, mCabinetType);
                    intent.putExtra("UPDATE_COUNT", updateCount);
                    setResult(Constants.RESULT_SUCCESS, intent);
                    finish();
                    break;
                // 안내 타입의 확인
                case R.id.btnInfoConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
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
        if (queryType == Constants.QUERY_CABINET_DATA) {
            thread = getCabinetDataThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getCabinetDataThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    getCabinetDataRes = request.getCabinetData();
                    mHandler.sendEmptyMessage(Constants.QUERY_CABINET_DATA);
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
            case Constants.QUERY_CABINET_DATA :
                arrCabinetDataItem.clear();

                if (getCabinetDataRes.getCabinetDataItem().size() > 0) {
                    llCabinetListEmpty.setVisibility(View.GONE);
                    lvCabinetList.setVisibility(View.VISIBLE);

                    for(CabinetDataItem item : getCabinetDataRes.getCabinetDataItem()) {
                        arrCabinetDataItem.add(item);
                    }
                } else {
                    llCabinetListEmpty.setVisibility(View.VISIBLE);
                    lvCabinetList.setVisibility(View.GONE);
                }
                mCabinetAdapter.notifyDataSetChanged();
                break;
            // 보관함 재생 버튼 Handler
            case Constants.QUERY_CABINET_PLAY:
                mPosition = data.getInt("POSITION", -1);
                int COUNT = data.getInt("COUNT");
                if (COUNT > 0) {
                    if (mPosition >= 0) {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_PLAY_ALL, onClickListener);
                        mPostDialog.show();
                    }
                } else {
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onClickListener, mContext.getResources().getString(R.string.dialog_info_cabinet_empty_music));
                    mPostDialog.show();
                }
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
            case Constants.QUERY_WRITE_CABINET:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    getData(Constants.QUERY_CABINET_DATA);
                }
                break;
            case Constants.QUERY_CABINET_DESC:
                getData(Constants.QUERY_CABINET_DATA);
                break;
        }
    }
}
