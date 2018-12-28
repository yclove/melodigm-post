package com.melodigm.post.menu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.PostActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetTimeLineReq;
import com.melodigm.post.protocol.data.GetTimeLineRes;
import com.melodigm.post.protocol.data.TimeLineItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimeLineActivity extends BaseActivity implements IOnHandlerMessage {
	private GetTimeLineRes getTimeLineRes;
    private ArrayList<TimeLineItem> arrTimeLineItem;
    private TimeLineAdapter mTimeLineAdapter;
    private ListView lvTimeLineList;
    private LetterSpacingTextView tvTimeLineDay, tvTimeLineWeekDay, tvTimeLineDate;
    private RelativeLayout btnTimeLineFilter, btnTimeLineFilterPost, btnTimeLineFilterOst, btnTimeLineFilterLike, btnTimeLineFilterAll;
    private ImageView ivTimeLineEmptyCircle, btnTimeLineFilterImage, btnTimeLineFilterPostImage, btnTimeLineFilterOstImage, btnTimeLineFilterLikeImage, btnTimeLineFilterAllImage;
    private LinearLayout btnCalendarMove, btnTimeLineFilterLayout, lvTimeLineListEmpty;
    private boolean isFilterShow = false;
    private String REG_DATE = "";
    private String FILTER_TYPE = "ALL";
    private View vListViewTimeLineFooter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_timeline);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
	}

	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.menu_timeline), false);

        tvTimeLineDay = (LetterSpacingTextView) findViewById(R.id.tvTimeLineDay);
        tvTimeLineWeekDay = (LetterSpacingTextView) findViewById(R.id.tvTimeLineWeekDay);
        tvTimeLineDate = (LetterSpacingTextView) findViewById(R.id.tvTimeLineDate);

        btnTimeLineFilterImage = (ImageView) findViewById(R.id.btnTimeLineFilterImage);
        btnTimeLineFilterPostImage = (ImageView) findViewById(R.id.btnTimeLineFilterPostImage);
        btnTimeLineFilterOstImage = (ImageView) findViewById(R.id.btnTimeLineFilterOstImage);
        btnTimeLineFilterLikeImage = (ImageView) findViewById(R.id.btnTimeLineFilterLikeImage);
        btnTimeLineFilterAllImage = (ImageView) findViewById(R.id.btnTimeLineFilterAllImage);
        btnTimeLineFilterLayout = (LinearLayout) findViewById(R.id.btnTimeLineFilterLayout);
        btnCalendarMove = (LinearLayout) findViewById(R.id.btnCalendarMove);
        btnCalendarMove.setOnClickListener(onClickListener);
        btnTimeLineFilter = (RelativeLayout) findViewById(R.id.btnTimeLineFilter);
        btnTimeLineFilter.setOnClickListener(onClickListener);
        btnTimeLineFilterPost = (RelativeLayout) findViewById(R.id.btnTimeLineFilterPost);
        btnTimeLineFilterPost.setOnClickListener(onClickListener);
        btnTimeLineFilterOst = (RelativeLayout) findViewById(R.id.btnTimeLineFilterOst);
        btnTimeLineFilterOst.setOnClickListener(onClickListener);
        btnTimeLineFilterLike = (RelativeLayout) findViewById(R.id.btnTimeLineFilterLike);
        btnTimeLineFilterLike.setOnClickListener(onClickListener);
        btnTimeLineFilterAll = (RelativeLayout) findViewById(R.id.btnTimeLineFilterAll);
        btnTimeLineFilterAll.setOnClickListener(onClickListener);

        lvTimeLineListEmpty = (LinearLayout) findViewById(R.id.lvTimeLineListEmpty);
        ivTimeLineEmptyCircle = (ImageView) findViewById(R.id.ivTimeLineEmptyCircle);
        ivTimeLineEmptyCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF959595")));

        lvTimeLineList = (ListView) findViewById(R.id.lvTimeLineList);
        arrTimeLineItem = new ArrayList<>();
        mTimeLineAdapter = new TimeLineAdapter(mContext, R.layout.adapter_menu_timeline, arrTimeLineItem, mGlideRequestManager);
        vListViewTimeLineFooter = getLayoutInflater().inflate(R.layout.view_listview_footer_timeline, null, false);
        lvTimeLineList.addFooterView(vListViewTimeLineFooter, null, false);
        lvTimeLineList.setAdapter(mTimeLineAdapter);
        lvTimeLineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                // TODO : 각 타겟 별 이동 정의 후 개발
                /*if (CommonUtil.isNotNull(arrTimeLineItem.get(position).getPOI())) {
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("POST_TYPE", arrTimeLineItem.get(position).getPOST_TYPE());
                    intent.putExtra("POI", arrTimeLineItem.get(position).getPOI());
                    startActivityForResult(intent, Constants.QUERY_POST_DATA);
                }*/
            }
        });

        REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");
        updateFilterImage();
        getData(Constants.QUERY_TIME_LINE);
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Filter onClick
                case R.id.btnTimeLineFilter:
                    updateFilterLayout();
                    break;
                // Filter > Post onClick
                case R.id.btnTimeLineFilterPost:
                    FILTER_TYPE = "POST";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_TIME_LINE);
                    break;
                // Filter > Ost onClick
                case R.id.btnTimeLineFilterOst:
                    FILTER_TYPE = "OST";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_TIME_LINE);
                    break;
                // Filter > Like onClick
                case R.id.btnTimeLineFilterLike:
                    FILTER_TYPE = "LIKE";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_TIME_LINE);
                    break;
                // Filter > All onClick
                case R.id.btnTimeLineFilterAll:
                    FILTER_TYPE = "ALL";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_TIME_LINE);
                    break;
                // 달력 이동 onClick
                case R.id.btnCalendarMove:
                    Intent intent = new Intent(mContext, CalendarActivity.class);
                    intent.putExtra("TYPE", "TIMELINE");
                    intent.putExtra("REG_DATE", REG_DATE);
                    startActivityForResult(intent, Constants.QUERY_CALENDAR);
                    break;
            }
        }
    };

    private void updateFilterDate() {
        if (REG_DATE == null || !DateUtil.isDate(REG_DATE)) return;
        tvTimeLineDay.setText(REG_DATE.substring(8, 10));
        tvTimeLineWeekDay.setText(DateUtil.getDateDisplayUnit(Date.valueOf(REG_DATE), "EEEE").toUpperCase());
        tvTimeLineDate.setText(DateUtil.getDateDisplayUnit(Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4));
        tvTimeLineWeekDay.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvTimeLineDate.setSpacing(Constants.TEXT_VIEW_SPACING);
    }

    private void updateFilterLayout() {
        if (isFilterShow) {
            btnTimeLineFilterImage.setImageResource(R.drawable.arwdown_white50);
            btnTimeLineFilterLayout.setVisibility(View.GONE);
        } else {
            btnTimeLineFilterImage.setImageResource(R.drawable.arwup_white50);
            btnTimeLineFilterLayout.setVisibility(View.VISIBLE);
        }
        isFilterShow = !isFilterShow;
    }

    private void updateFilterImage() {
        btnTimeLineFilterPostImage.setAlpha(0.3f);
        btnTimeLineFilterOstImage.setAlpha(0.3f);
        btnTimeLineFilterLikeImage.setAlpha(0.3f);
        btnTimeLineFilterAllImage.setAlpha(0.3f);

        switch (FILTER_TYPE) {
            case "POST":
                btnTimeLineFilterPostImage.setAlpha(1.0f);
                break;
            case "OST":
                btnTimeLineFilterOstImage.setAlpha(1.0f);
                break;
            case "LIKE":
                btnTimeLineFilterLikeImage.setAlpha(1.0f);
                break;
            case "ALL":
                btnTimeLineFilterAllImage.setAlpha(1.0f);
                break;
        }
    }

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
        if (queryType == Constants.QUERY_TIME_LINE) {
            thread = getTimeLineThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getTimeLineThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetTimeLineReq getTimeLineReq = new GetTimeLineReq();
                    getTimeLineReq.setREG_DATE(REG_DATE);
                    getTimeLineReq.setFILTER_TYPE(FILTER_TYPE);
                    getTimeLineRes = request.getTimeLine(getTimeLineReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_TIME_LINE);
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
            // 타임라인 데이터 조회 후 Handler
            case Constants.QUERY_TIME_LINE:
                arrTimeLineItem.clear();
                if (getTimeLineRes.getTimeLineItem().size() > 0) {
                    for(TimeLineItem item : getTimeLineRes.getTimeLineItem()) {
                        arrTimeLineItem.add(item);
                    }
                    lvTimeLineListEmpty.setVisibility(View.GONE);
                    lvTimeLineList.setVisibility(View.VISIBLE);
                } else {
                    lvTimeLineListEmpty.setVisibility(View.VISIBLE);
                    lvTimeLineList.setVisibility(View.GONE);
                }
                mTimeLineAdapter.notifyDataSetChanged();
                updateFilterDate();
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST:
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
                break;
            case Constants.DIALOG_EXCEPTION_POST:
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED:
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT:
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Calendar 날짜 선택 후 ActivityResult
            case Constants.QUERY_CALENDAR:
                if (resultCode == Constants.RESULT_SUCCESS)
                    if (!REG_DATE.equals(data.getStringExtra("REG_DATE"))) {
                        REG_DATE = data.getStringExtra("REG_DATE");
                        getData(Constants.QUERY_TIME_LINE);
                    }
                break;
        }
    }
}
