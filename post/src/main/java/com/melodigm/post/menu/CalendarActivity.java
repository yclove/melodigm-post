package com.melodigm.post.menu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetCalendarDateReq;
import com.melodigm.post.protocol.data.GetCalendarDateRes;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.*;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.calendar.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CalendarActivity extends BaseActivity implements IOnHandlerMessage, OnDateSelectedListener, OnMonthChangedListener {
    private String mType = "", mRegDate = "", mBackgroundImage = "", mSelectedDate = "";
    private LetterSpacingTextView tvCalendarMonth, tvCalendarMonthEng, tvCalendarFooterDay, tvCalendarFooterMonthYear;
    private ImageView ivCalendarRoot;
    private static MaterialCalendarView mMaterialCalendarView;
    private RelativeLayout btnCalendarReset;

    private Calendar mCalendar;
    private GetCalendarDateRes getCalendarDateRes;

    private static PrimeDayDisableDecorator mPrimeDayDisableDecorator;
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_calendar);
        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        Intent intent = getIntent();
        mType = intent.getStringExtra("TYPE");
        mRegDate = intent.getStringExtra("REG_DATE");
        mBackgroundImage = intent.getStringExtra("BACKGROUND_IMAGE");

        if (DateUtil.isDate(mRegDate))
            setDisplay();
	}

	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "yyyy"), false);
        setHeaderCheckImage();

        ivCalendarRoot = (ImageView) findViewById(R.id.ivCalendarRoot);
        if (CommonUtil.isNotNull(mBackgroundImage)) {
            mGlideRequestManager
                .load(mBackgroundImage)
                .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                .into(ivCalendarRoot);
        }
        tvCalendarMonth = (LetterSpacingTextView) findViewById(R.id.tvCalendarMonth);
        tvCalendarMonth.setText(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "MM"));
        tvCalendarMonthEng = (LetterSpacingTextView) findViewById(R.id.tvCalendarMonthEng);
        tvCalendarMonthEng.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvCalendarMonthEng.setText(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "MMMM").toUpperCase());

        tvCalendarFooterDay = (LetterSpacingTextView) findViewById(R.id.tvCalendarFooterDay);
        tvCalendarFooterDay.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvCalendarFooterDay.setText(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "dd"));
        tvCalendarFooterMonthYear = (LetterSpacingTextView) findViewById(R.id.tvCalendarFooterMonthYear);
        tvCalendarFooterMonthYear.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvCalendarFooterMonthYear.setText(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "MMM yyyy"));

        btnCalendarReset = (RelativeLayout) findViewById(R.id.btnCalendarReset);
        btnCalendarReset.setOnClickListener(onClickListener);

        mMaterialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);
        mMaterialCalendarView.setOnDateChangedListener(this);
        mMaterialCalendarView.setOnMonthChangedListener(this);
        mMaterialCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        MontserratDecorator mMontserratDecorator = new MontserratDecorator(mContext);
        mMaterialCalendarView.addDecorators(
                new SundayDecorator(mMontserratDecorator.getMontserratRegular()),
                new SaturdayDecorator(mMontserratDecorator.getMontserratRegular()),
                mMontserratDecorator
        );

        mPrimeDayDisableDecorator = new PrimeDayDisableDecorator();
        //mMaterialCalendarView.addDecorator(new EnableOneToTenDecorator());

        mMaterialCalendarView.setTitleYear(btnHeaderTitle);
        mMaterialCalendarView.setTitleMonth(tvCalendarMonth);
        mMaterialCalendarView.setTitleMonthEng(tvCalendarMonthEng);
        mMaterialCalendarView.setSelectedDate(Date.valueOf(mRegDate));
        mMaterialCalendarView.setCurrentDate(Date.valueOf(mRegDate));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.JANUARY, 1);
        mMaterialCalendarView.setMinimumDate(calendar.getTime());

        mMaterialCalendarView.setMaximumDate(Date.valueOf(DateUtil.getCurrentDate("yyyy-MM-dd")));
	}

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @Nullable CalendarDay date, boolean selected) {
        mSelectedDate = getSelectedDatesString();
        tvCalendarFooterDay.setText(DateUtil.getDateDisplayUnit(date.getDate(), "dd"));
        tvCalendarFooterMonthYear.setText(DateUtil.getDateDisplayUnit(date.getDate(), "MMM yyyy"));
        setHeaderCheckImage();
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        mCalendar = Calendar.getInstance();
        mCalendar.setTime(date.getDate());
        getData(Constants.QUERY_CALENDAR);
    }

    private String getSelectedDatesString() {
        CalendarDay date = mMaterialCalendarView.getSelectedDate();
        if (date == null) {
            return "";
        }

        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        return transFormat.format(date.getDate());
    }

    private static class PrimeDayDisableDecorator implements DayViewDecorator {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return PRIME_TABLE[day.getDay()];
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }

        public void setPrimeTable(boolean arrItems[]) {
            PRIME_TABLE = arrItems;
            mMaterialCalendarView.addDecorator(mPrimeDayDisableDecorator);
        }

        private static boolean PRIME_TABLE[];
    }

    private static class EnableOneToTenDecorator implements DayViewDecorator {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.getDay() <= 10;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
        }
    }

    private void setHeaderCheckImage() {
        if (CommonUtil.isNull(mSelectedDate) || mRegDate.equals(mSelectedDate)) {
            btnHeaderCheck.setAlpha(0.2f);
            btnHeaderCheck.setOnClickListener(null);
        } else {
            btnHeaderCheck.setAlpha(1.0f);
            btnHeaderCheck.setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.btnHeaderCheck:
                    Intent intent = getIntent();
                    intent.putExtra("REG_DATE", mSelectedDate);
                    setResult(Constants.RESULT_SUCCESS, intent);
                    finish();
                    break;
                // Calendar Reset onClick
                case R.id.btnCalendarReset:
                    mMaterialCalendarView.setSelectedDate(Date.valueOf(mRegDate));
                    mMaterialCalendarView.setCurrentDate(Date.valueOf(mRegDate));
                    tvCalendarFooterDay.setText(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "dd"));
                    tvCalendarFooterMonthYear.setText(DateUtil.getDateDisplayUnit(Date.valueOf(mRegDate), "MMM yyyy"));

                    break;
            }
        }
    };

    private void getData(int queryType, Object... args) {
        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_CALENDAR) {
            thread = getCalendarDateThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getCalendarDateThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetCalendarDateReq getCalendarDateReq = new GetCalendarDateReq();
                    getCalendarDateReq.setYEAR(String.valueOf(mCalendar.get(Calendar.YEAR)));
                    getCalendarDateReq.setMONTH(String.valueOf(mCalendar.get(Calendar.MONTH) + 1));
                    if ("NOTIFICATIONCENTER".equals(mType)) {
                        getCalendarDateRes = request.getCalendarDateForNotificationCenter(getCalendarDateReq);
                    } else if ("TIMELINE".equals(mType)) {
                        getCalendarDateRes = request.getCalendarDate(getCalendarDateReq);
                    } else {
                        getCalendarDateReq.setPOST_TYPE(mType);
                        getCalendarDateRes = request.getCalendarDateForPost(getCalendarDateReq);
                    }
                    mHandler.sendEmptyMessage(Constants.QUERY_CALENDAR);
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
        switch(msg.what) {
            case Constants.QUERY_CALENDAR:
                mPrimeDayDisableDecorator.setPrimeTable(getCalendarDateRes.getCalendarDateItems());
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

    class MontserratDecorator implements DayViewDecorator {
        private final Typeface montserratRegular;

        public MontserratDecorator(Context context) {
            // FontFactory is a Util class that I created to search for fonts inside assets/fonts
            // TODO : YCLOVE
//            montserratRegular = Typekit.createFromAsset(context, "fonts/Montserrat-Regular.ttf");
            montserratRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
        }

        @Override
        public boolean shouldDecorate(CalendarDay calendarDay) {
            return true;
        }

        @Override
        public void decorate(DayViewFacade dayViewFacade) {
            // CustomTypefaceSpan is a Custom TypefaceSpan (CustomTypefaceSpan extends TypefaceSpan) that changes the TypeFace of a Span
            dayViewFacade.addSpan(new CustomTypefaceSpan("", montserratRegular, 0, 0));
        }

        public Typeface getMontserratRegular() {
            return montserratRegular;
        }
    }
}
