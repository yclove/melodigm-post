package com.melodigm.post.menu;

import android.content.Intent;
import android.net.Uri;
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
import com.melodigm.post.protocol.data.GetNotificationCenterReq;
import com.melodigm.post.protocol.data.GetNotificationCenterRes;
import com.melodigm.post.protocol.data.NotificationCenterItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationCenterActivity extends BaseActivity implements IOnHandlerMessage {
	private GetNotificationCenterRes getNotificationCenterRes;
    private ArrayList<NotificationCenterItem> arrNotificationCenterItem;
    private NotificationCenterAdapter mNotificationCenterAdapter;
    private ListView lvNotificationCenterList;
    private LetterSpacingTextView tvNotificationCenterDay, tvNotificationCenterWeekDay, tvNotificationCenterDate;
    private RelativeLayout btnNotificationCenterFilter, btnNotificationCenterFilterUseCoupon, btnNotificationCenterFilterReple, btnNotificationCenterFilterOst, btnNotificationCenterFilterLike, btnNotificationCenterFilterAll;
    private ImageView btnNotificationCenterFilterImage, btnNotificationCenterFilterUseCouponImage, btnNotificationCenterFilterRepleImage, btnNotificationCenterFilterOstImage, btnNotificationCenterFilterLikeImage, btnNotificationCenterFilterAllImage;
    private LinearLayout btnCalendarMove, btnNotificationCenterFilterLayout, llNotificationCenterListEmpty;
    private boolean isFilterShow = false;
    private String REG_DATE = "";
    private String FILTER_TYPE = "ALL";

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_notificationcenter);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, onClickListener);
        setPostHeaderTitle(getResources().getString(R.string.menu_alrimcenter), false);

        tvNotificationCenterDay = (LetterSpacingTextView) findViewById(R.id.tvNotificationCenterDay);
        tvNotificationCenterWeekDay = (LetterSpacingTextView) findViewById(R.id.tvNotificationCenterWeekDay);
        tvNotificationCenterDate = (LetterSpacingTextView) findViewById(R.id.tvNotificationCenterDate);

        btnNotificationCenterFilterImage = (ImageView) findViewById(R.id.btnNotificationCenterFilterImage);
        btnNotificationCenterFilterUseCouponImage = (ImageView) findViewById(R.id.btnNotificationCenterFilterUseCouponImage);
        btnNotificationCenterFilterRepleImage = (ImageView) findViewById(R.id.btnNotificationCenterFilterRepleImage);
        btnNotificationCenterFilterOstImage = (ImageView) findViewById(R.id.btnNotificationCenterFilterOstImage);
        btnNotificationCenterFilterLikeImage = (ImageView) findViewById(R.id.btnNotificationCenterFilterLikeImage);
        btnNotificationCenterFilterAllImage = (ImageView) findViewById(R.id.btnNotificationCenterFilterAllImage);
        btnNotificationCenterFilterLayout = (LinearLayout) findViewById(R.id.btnNotificationCenterFilterLayout);
        btnCalendarMove = (LinearLayout) findViewById(R.id.btnCalendarMove);
        btnCalendarMove.setOnClickListener(onClickListener);
        btnNotificationCenterFilter = (RelativeLayout) findViewById(R.id.btnNotificationCenterFilter);
        btnNotificationCenterFilter.setOnClickListener(onClickListener);
        btnNotificationCenterFilterUseCoupon = (RelativeLayout) findViewById(R.id.btnNotificationCenterFilterUseCoupon);
        btnNotificationCenterFilterUseCoupon.setOnClickListener(onClickListener);
        btnNotificationCenterFilterReple = (RelativeLayout) findViewById(R.id.btnNotificationCenterFilterReple);
        btnNotificationCenterFilterReple.setOnClickListener(onClickListener);
        btnNotificationCenterFilterOst = (RelativeLayout) findViewById(R.id.btnNotificationCenterFilterOst);
        btnNotificationCenterFilterOst.setOnClickListener(onClickListener);
        btnNotificationCenterFilterLike = (RelativeLayout) findViewById(R.id.btnNotificationCenterFilterLike);
        btnNotificationCenterFilterLike.setOnClickListener(onClickListener);
        btnNotificationCenterFilterAll = (RelativeLayout) findViewById(R.id.btnNotificationCenterFilterAll);
        btnNotificationCenterFilterAll.setOnClickListener(onClickListener);

        llNotificationCenterListEmpty = (LinearLayout) findViewById(R.id.llNotificationCenterListEmpty);

        lvNotificationCenterList = (ListView) findViewById(R.id.lvNotificationCenterList);
        arrNotificationCenterItem = new ArrayList<>();
        mNotificationCenterAdapter = new NotificationCenterAdapter(mContext, R.layout.adapter_menu_notificationcenter, arrNotificationCenterItem);
        lvNotificationCenterList.setAdapter(mNotificationCenterAdapter);
        lvNotificationCenterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l_position) {
                NotificationCenterItem item = arrNotificationCenterItem.get(position);
                if (("AF17".equalsIgnoreCase(item.getNOTI_TYPE()) || "AF19".equalsIgnoreCase(item.getNOTI_TYPE()) || "AF20".equalsIgnoreCase(item.getNOTI_TYPE())) || (CommonUtil.isNotNull(item.getPOI()) && !(Constants.REQUEST_DISP_TYPE_DELETE.equals(item.getPOST_DISP_TYPE()) || Constants.REQUEST_DISP_TYPE_NOTIFY.equals(item.getPOST_DISP_TYPE())))) {
                    Intent actionIntent = new Intent(getApplicationContext(), PostActivity.class);
                    actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    actionIntent.setData(Uri.parse(Constants.SERVICE_SCHEME + "://" + Constants.SERVICE_SCHEME_HOST + "?action=" + item.getNOTI_TYPE() + "&poi=" + item.getPOI() + "&ori=" + item.getORI() + "&oti=" + item.getOTI()));
                    startActivity(actionIntent);
                }
            }
        });

        REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");
        updateFilterImage();
        getData(Constants.QUERY_NOTIFICATION_CENTER);
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
                case R.id.btnNotificationCenterFilter:
                    updateFilterLayout();
                    break;
                // Filter > 이용권 onClick
                case R.id.btnNotificationCenterFilterUseCoupon:
                    FILTER_TYPE = "PROD";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_NOTIFICATION_CENTER);
                    break;
                // Filter > 대댓글 onClick
                case R.id.btnNotificationCenterFilterReple:
                    FILTER_TYPE = "REPLE";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_NOTIFICATION_CENTER);
                    break;
                // Filter > OST onClick
                case R.id.btnNotificationCenterFilterOst:
                    FILTER_TYPE = "OST";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_NOTIFICATION_CENTER);
                    break;
                // Filter > 좋아요 onClick
                case R.id.btnNotificationCenterFilterLike:
                    FILTER_TYPE = "LIKE";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_NOTIFICATION_CENTER);
                    break;
                // Filter > All onClick
                case R.id.btnNotificationCenterFilterAll:
                    FILTER_TYPE = "ALL";
                    updateFilterLayout();
                    updateFilterImage();
                    getData(Constants.QUERY_NOTIFICATION_CENTER);
                    break;
                // 달력 이동 onClick
                case R.id.btnCalendarMove:
                    Intent intent = new Intent(mContext, CalendarActivity.class);
                    intent.putExtra("TYPE", "NOTIFICATIONCENTER");
                    intent.putExtra("REG_DATE", REG_DATE);
                    startActivityForResult(intent, Constants.QUERY_CALENDAR);
                    break;
            }
        }
    };

    private void updateFilterDate() {
        if (REG_DATE == null || !DateUtil.isDate(REG_DATE)) return;
        tvNotificationCenterDay.setText(REG_DATE.substring(8, 10));
        tvNotificationCenterWeekDay.setText(DateUtil.getDateDisplayUnit(Date.valueOf(REG_DATE), "EEEE").toUpperCase());
        tvNotificationCenterDate.setText(DateUtil.getDateDisplayUnit(Date.valueOf(REG_DATE), "MMMM").toUpperCase() + " " + REG_DATE.substring(0, 4));
        tvNotificationCenterWeekDay.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvNotificationCenterDate.setSpacing(Constants.TEXT_VIEW_SPACING);
    }

    private void updateFilterLayout() {
        if (isFilterShow) {
            btnNotificationCenterFilterImage.setImageResource(R.drawable.arwdown_white50);
            btnNotificationCenterFilterLayout.setVisibility(View.GONE);
        } else {
            btnNotificationCenterFilterImage.setImageResource(R.drawable.arwup_white50);
            btnNotificationCenterFilterLayout.setVisibility(View.VISIBLE);
        }
        isFilterShow = !isFilterShow;
    }

    private void updateFilterImage() {
        btnNotificationCenterFilterUseCouponImage.setAlpha(0.3f);
        btnNotificationCenterFilterRepleImage.setAlpha(0.3f);
        btnNotificationCenterFilterOstImage.setAlpha(0.3f);
        btnNotificationCenterFilterLikeImage.setAlpha(0.3f);
        btnNotificationCenterFilterAllImage.setAlpha(0.3f);

        switch (FILTER_TYPE) {
            case "PROD":
                btnNotificationCenterFilterUseCouponImage.setAlpha(1.0f);
                break;
            case "REPLE":
                btnNotificationCenterFilterRepleImage.setAlpha(1.0f);
                break;
            case "OST":
                btnNotificationCenterFilterOstImage.setAlpha(1.0f);
                break;
            case "LIKE":
                btnNotificationCenterFilterLikeImage.setAlpha(1.0f);
                break;
            case "ALL":
                btnNotificationCenterFilterAllImage.setAlpha(1.0f);
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
        if (queryType == Constants.QUERY_NOTIFICATION_CENTER) {
            thread = getNotificationCenterThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getNotificationCenterThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetNotificationCenterReq getNotificationCenterReq = new GetNotificationCenterReq();
                    getNotificationCenterReq.setREG_DATE(REG_DATE);
                    getNotificationCenterReq.setFILTER_TYPE(FILTER_TYPE);
                    getNotificationCenterRes = request.getNotificationCenter(getNotificationCenterReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_NOTIFICATION_CENTER);
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
            case Constants.QUERY_NOTIFICATION_CENTER:
                arrNotificationCenterItem.clear();
                if (getNotificationCenterRes.getNotificationCenterItem().size() > 0) {
                    for(NotificationCenterItem item : getNotificationCenterRes.getNotificationCenterItem()) {
                        arrNotificationCenterItem.add(item);
                    }
                    llNotificationCenterListEmpty.setVisibility(View.GONE);
                    lvNotificationCenterList.setVisibility(View.VISIBLE);
                } else {
                    llNotificationCenterListEmpty.setVisibility(View.VISIBLE);
                    lvNotificationCenterList.setVisibility(View.GONE);
                }
                mNotificationCenterAdapter.notifyDataSetChanged();
                updateFilterDate();
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
                        getData(Constants.QUERY_NOTIFICATION_CENTER);
                    }
                break;
        }
    }
}
