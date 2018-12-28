package com.melodigm.post.menu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetStampDataReq;
import com.melodigm.post.protocol.data.GetStampDataRes;
import com.melodigm.post.protocol.data.StampGroupItem;
import com.melodigm.post.protocol.data.StampItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StampActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener, AbsListView.OnScrollListener {
	private GetStampDataRes getStampDataRes;
    private StampAdapter mStampAdapter;
    private LinearLayout llTransUseCouponBtn, llStampListEmpty;
    private TextView tvStampCount, tvStampHistory, tvStampFilter;
    private ExpandableListView exlvStampList;

    private int currentPage = 1;
    private boolean isLockListView = true;
    private boolean isTotalDataLoad = false;
    private String SEARCH_DATE = "";
    private String TRANS_TYPE = Constants.REQUEST_TRANS_TYPE_ALL;
    private ArrayList<String> mListDataHeader;
    private HashMap<String, List<StampItem>> mListDataChild;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_stamp);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getResources().getString(R.string.stamp), false);

        // 이용권 전환하기 버튼
        llTransUseCouponBtn = (LinearLayout) findViewById(R.id.llTransUseCouponBtn);

        // 우표 필터 버튼
        tvStampFilter = (TextView) findViewById(R.id.tvStampFilter);

        // 우표 보유 수
        tvStampCount = (TextView) findViewById(R.id.tvStampCount);

        // 우표 총 적립 / 총 소비 내역
        tvStampHistory = (TextView) findViewById(R.id.tvStampHistory);

        // 우표 목록 / Empty 레이아웃
        llStampListEmpty = (LinearLayout) findViewById(R.id.llStampListEmpty);

        mListDataHeader = new ArrayList<>();
        mListDataChild = new HashMap<>();
        exlvStampList = (ExpandableListView) findViewById(R.id.exlvStampList);
        mStampAdapter = new StampAdapter(mContext, mListDataHeader, mListDataChild);
        exlvStampList.setAdapter(mStampAdapter);
        exlvStampList.setOnScrollListener(this);
        exlvStampList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        SEARCH_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");
        getData(Constants.QUERY_STAMP_DATA);
	}

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // 공유 onClick
            case R.id.rlShareBtn:
                DeviceUtil.showToast(mContext, "공유 onClick");
                break;
            // 이용권 전환하기 onClick
            case R.id.llTransUseCouponBtn:
                DeviceUtil.showToast(mContext, "이용권 전환하기 onClick");
                break;
            // 이용권 필터 onClick
            case R.id.llStampFilterBtn:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_STAMP_FILTER, this, TRANS_TYPE);
                mPostDialog.show();
                break;
            // 이용권 필터 > 전체 onClick
            case R.id.llStampFilterAll:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                SEARCH_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");
                TRANS_TYPE = Constants.REQUEST_TRANS_TYPE_ALL;
                tvStampFilter.setText(getString(R.string.all));
                isTotalDataLoad = false;
                mListDataHeader.clear();
                mListDataChild.clear();
                mStampAdapter.notifyDataSetChanged();
                getData(Constants.QUERY_STAMP_DATA);
                break;
            // 이용권 필터 > 적립 onClick
            case R.id.llStampFilterKeep:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                SEARCH_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");
                TRANS_TYPE = Constants.REQUEST_TRANS_TYPE_KEEP;
                tvStampFilter.setText(getString(R.string.sort_keep));
                currentPage = 1;
                isTotalDataLoad = false;
                mListDataHeader.clear();
                mListDataChild.clear();
                mStampAdapter.notifyDataSetChanged();
                getData(Constants.QUERY_STAMP_DATA);
                break;
            // 이용권 필터 > 소비 onClick
            case R.id.llStampFilterUse:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                SEARCH_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");
                TRANS_TYPE = Constants.REQUEST_TRANS_TYPE_USE;
                tvStampFilter.setText(getString(R.string.sort_use));
                currentPage = 1;
                isTotalDataLoad = false;
                mListDataHeader.clear();
                mListDataChild.clear();
                mStampAdapter.notifyDataSetChanged();
                getData(Constants.QUERY_STAMP_DATA);
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // 현재 가장 처음에 보이는 셀번호와 보여지는 셀번호를 더한값이
        // 전체의 숫자와 동일해지면 가장 아래로 스크롤 되었다고 가정합니다.
        int count = totalItemCount - visibleItemCount;

        if (firstVisibleItem >= count && totalItemCount != 0 && !isLockListView && !isTotalDataLoad) {
            isLockListView = true;
            getData(Constants.QUERY_STAMP_DATA);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    private void getData(int queryType) {
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
        if (queryType == Constants.QUERY_STAMP_DATA) {
            thread = getStampDataThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getStampDataThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetStampDataReq getStampDataReq = new GetStampDataReq();
                    getStampDataReq.setPOINT_TYPE(Constants.REQUEST_POINT_TYPE_STAMP);
                    getStampDataReq.setSEARCH_DATE(SEARCH_DATE);
                    getStampDataReq.setTRANS_TYPE(TRANS_TYPE);
                    getStampDataRes = request.getStampData(getStampDataReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_STAMP_DATA);
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
            // 우표 조회 후 Handler
            case Constants.QUERY_STAMP_DATA:
                tvStampCount.setText(getString(R.string.msg_stamp_count, getStampDataRes.getNOW_POINT()));
                tvStampHistory.setText(getString(R.string.msg_stamp_history, getStampDataRes.getACMLT_POINT(), getStampDataRes.getCSPT_POINT()));

                // 최초 호출 시 데이터가 없을 경우
                if (getStampDataRes.getStampGroupItems().size() <= 0 && currentPage == 1) {
                    llStampListEmpty.setVisibility(View.VISIBLE);
                    exlvStampList.setVisibility(View.GONE);
                } else {
                    llStampListEmpty.setVisibility(View.GONE);
                    exlvStampList.setVisibility(View.VISIBLE);

                    if (getStampDataRes.getStampGroupItems().size() > 0) {
                        currentPage++;

                        if (getStampDataRes.getNOW_POINT() >= 50) {
                            llTransUseCouponBtn.setVisibility(View.VISIBLE);
                        }

                        for (StampGroupItem item : getStampDataRes.getStampGroupItems()) {
                            mListDataHeader.add(item.getREG_DATE());
                            mListDataChild.put(item.getREG_DATE(), item.getStampItems());
                        }

                        isLockListView = false;
                        String lastSearchDate = getStampDataRes.getStampGroupItems().get(getStampDataRes.getStampGroupItems().size() - 1).getREG_DATE();
                        java.util.Date newSearchDate = DateUtil.addDays(Date.valueOf(lastSearchDate), -1);
                        SEARCH_DATE = DateUtil.getDateDisplayUnit(newSearchDate, "yyyy-MM-dd");
                        mStampAdapter.notifyDataSetChanged();

                        for (int i = 0; i < mStampAdapter.getGroupCount(); i++) {
                            exlvStampList.expandGroup(i);
                        }
                    } else {
                        isTotalDataLoad = true;
                    }
                }
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                break;
        }
    }
}
