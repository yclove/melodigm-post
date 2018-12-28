package com.melodigm.post;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.PostSortData;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.MultiViewPager;

import java.util.ArrayList;
import java.util.HashMap;

public class PostSortActivity extends BaseActivity implements IOnHandlerMessage {
    private HashMap<String, String> mCurrentSortData, mSortData;
    private MultiViewPager mMotionViewPager, mGenderViewPager, mGenerationViewPager, mTimeViewPager, mLocationViewPager;
    private PostSortAdapter mMotionAdapter, mGenderAdapter, mGenerationAdapter, mTimeAdapter, mLocationAdapter;
    private ArrayList<PostSortData> mGenderData, mGenerationData, mTimeData, mLocationData;
    private int mGenderPosition, mGenerationPosition, mTimePosition, mLocationPosition;
    private int mGenderSpot, mGenerationSpot, mTimeSpot, mLocationSpot;
    private RelativeLayout btnInitSort;
    private ImageView btnInitSortImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_sort);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        mCurrentSortData = mdbHelper.getSortData("POST");
        mSortData = mdbHelper.getSortData("POST");
        mdbHelper.close();

        setDisplay();
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(R.drawable.bt_bt_sort_nor);

        btnInitSort = (RelativeLayout) findViewById(R.id.btnInitSort);
        btnInitSortImage = (ImageView) findViewById(R.id.btnInitSortImage);

        // Footer Sort Image 활성 / 비활성 처리
        setInitSortImage();
        setHeaderCheckImage();

        // MultiViewPager
        mMotionViewPager = (MultiViewPager) findViewById(R.id.motionViewPager);
        mGenderViewPager = (MultiViewPager) findViewById(R.id.genderViewPager);
        mGenerationViewPager = (MultiViewPager) findViewById(R.id.generationViewPager);
        mTimeViewPager = (MultiViewPager) findViewById(R.id.timeViewPager);
        mLocationViewPager = (MultiViewPager) findViewById(R.id.locationViewPager);

        // Adapter
        mMotionAdapter = new PostSortAdapter(mContext, getLayoutInflater(), mHandler, "motion");
        mGenderAdapter = new PostSortAdapter(mContext, getLayoutInflater(), mHandler, "single");
        mGenerationAdapter = new PostSortAdapter(mContext, getLayoutInflater(), mHandler, "single");
        mTimeAdapter = new PostSortAdapter(mContext, getLayoutInflater(), mHandler, "multi");
        mLocationAdapter = new PostSortAdapter(mContext, getLayoutInflater(), mHandler, "single");

        // Data
        if ("M".equals(mSortData.get("GENDER"))) mGenderPosition = 0;
        else if ("".equals(mSortData.get("GENDER"))) mGenderPosition = 1;
        else if ("F".equals(mSortData.get("GENDER"))) mGenderPosition = 2;
        mGenderData = new ArrayList<>();
        mGenderData.add(new PostSortData("M", "남자", "M".equals(mSortData.get("GENDER"))));
        mGenderData.add(new PostSortData("", "누구든", "".equals(mSortData.get("GENDER"))));
        mGenderData.add(new PostSortData("F", "여자", "F".equals(mSortData.get("GENDER"))));

        if ("10".equals(mSortData.get("GENERATION"))) mGenerationPosition = 0;
        else if ("20".equals(mSortData.get("GENERATION"))) mGenerationPosition = 1;
        else if ("".equals(mSortData.get("GENERATION"))) mGenerationPosition = 2;
        else if ("30".equals(mSortData.get("GENERATION"))) mGenerationPosition = 3;
        else if ("40".equals(mSortData.get("GENERATION"))) mGenerationPosition = 4;
        else if ("50".equals(mSortData.get("GENERATION"))) mGenerationPosition = 5;
        else if ("60".equals(mSortData.get("GENERATION"))) mGenerationPosition = 6;
        mGenerationData = new ArrayList<>();
        mGenerationData.add(new PostSortData("10", "10대", "10".equals(mSortData.get("GENERATION"))));
        mGenerationData.add(new PostSortData("20", "20대", "20".equals(mSortData.get("GENERATION"))));
        mGenerationData.add(new PostSortData("", "상관없이", "".equals(mSortData.get("GENERATION"))));
        mGenerationData.add(new PostSortData("30", "30대", "30".equals(mSortData.get("GENERATION"))));
        mGenerationData.add(new PostSortData("40", "40대", "40".equals(mSortData.get("GENERATION"))));
        mGenerationData.add(new PostSortData("50", "50대", "50".equals(mSortData.get("GENERATION"))));
        mGenerationData.add(new PostSortData("60", "60대+", "60".equals(mSortData.get("GENERATION"))));

        if ("BD01".equals(mSortData.get("TIME"))) mTimePosition = 0;
        else if ("BD02".equals(mSortData.get("TIME"))) mTimePosition = 2;
        else if ("BD03".equals(mSortData.get("TIME"))) mTimePosition = 3;
        else if ("BD04".equals(mSortData.get("TIME"))) mTimePosition = 6;
        else if ("".equals(mSortData.get("TIME"))) mTimePosition = 7;
        else if ("BD05".equals(mSortData.get("TIME"))) mTimePosition = 8;
        else if ("BD06".equals(mSortData.get("TIME"))) mTimePosition = 13;
        else if ("BD07".equals(mSortData.get("TIME"))) mTimePosition = 14;
        else if ("BD08".equals(mSortData.get("TIME"))) mTimePosition = 16;
        else if ("BD09".equals(mSortData.get("TIME"))) mTimePosition = 19;
        mTimeData = new ArrayList<>();
        mTimeData.add(new PostSortData("BD01", "굿모닝", "6", "BD01".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "", "7"));
        mTimeData.add(new PostSortData("BD02", "출근", "8", "BD02".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("BD03", "오전", "9", "BD03".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "", "10"));
        mTimeData.add(new PostSortData("", "", "11"));
        mTimeData.add(new PostSortData("BD04", "점심", "12", "BD04".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "언제나", "", "".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("BD05", "오후", "1", "BD05".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "", "2"));
        mTimeData.add(new PostSortData("", "", "3"));
        mTimeData.add(new PostSortData("", "", "4"));
        mTimeData.add(new PostSortData("", "", "5"));
        mTimeData.add(new PostSortData("BD06", "퇴근", "6", "BD06".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("BD07", "저녁", "7", "BD07".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "", "8"));
        mTimeData.add(new PostSortData("BD08", "밤", "9", "BD08".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "", "10"));
        mTimeData.add(new PostSortData("", "", "11"));
        mTimeData.add(new PostSortData("BD09", "굿나잇", "12", "BD09".equals(mSortData.get("TIME"))));
        mTimeData.add(new PostSortData("", "", "1"));
        mTimeData.add(new PostSortData("", "", "2"));
        mTimeData.add(new PostSortData("", "", "3"));
        mTimeData.add(new PostSortData("", "", "4"));
        mTimeData.add(new PostSortData("", "", "5"));

        if ("20".equals(mSortData.get("DISTANCE"))) mLocationPosition = 0;
        else if ("50".equals(mSortData.get("DISTANCE"))) mLocationPosition = 1;
        else if ("".equals(mSortData.get("DISTANCE"))) mLocationPosition = 2;
        else if ("100".equals(mSortData.get("DISTANCE"))) mLocationPosition = 3;
        else if ("200".equals(mSortData.get("DISTANCE"))) mLocationPosition = 4;
        mLocationData = new ArrayList<>();
        mLocationData.add(new PostSortData("20", "20km", "20".equals(mSortData.get("DISTANCE"))));
        mLocationData.add(new PostSortData("50", "50km", "50".equals(mSortData.get("DISTANCE"))));
        mLocationData.add(new PostSortData("", "어디든지", "".equals(mSortData.get("DISTANCE"))));
        mLocationData.add(new PostSortData("100", "100km", "100".equals(mSortData.get("DISTANCE"))));
        mLocationData.add(new PostSortData("200", "200km", "200".equals(mSortData.get("DISTANCE"))));

        // MultiViewPager Set Adapter
        mMotionViewPager.setAdapter(mMotionAdapter);
        mGenderViewPager.setAdapter(mGenderAdapter);
        mGenerationViewPager.setAdapter(mGenerationAdapter);
        mTimeViewPager.setAdapter(mTimeAdapter);
        mLocationViewPager.setAdapter(mLocationAdapter);

        // Adapter Add Items
        mGenderAdapter.addAllItems(mGenderData);
        mGenerationAdapter.addAllItems(mGenerationData);
        mTimeAdapter.addAllItems(mTimeData);
        mLocationAdapter.addAllItems(mLocationData);

        // Adapter Notify Data Set Changed
        mGenderAdapter.notifyDataSetChanged();
        mGenerationAdapter.notifyDataSetChanged();
        mTimeAdapter.notifyDataSetChanged();
        mLocationAdapter.notifyDataSetChanged();

        // MutiViewPager Init Item
        mMotionViewPager.setCurrentItem(50);
        mGenderViewPager.setCurrentItem(mGenderPosition);
        mGenerationViewPager.setCurrentItem(mGenerationPosition);
        mTimeViewPager.setCurrentItem(mTimePosition);
        mLocationViewPager.setCurrentItem(mLocationPosition);

        mGenderSpot = mGenderPosition;
        mGenerationSpot = mGenerationPosition;
        mTimeSpot = mTimePosition;
        mLocationSpot = mLocationPosition;

        // MultiViewPager Listener
        mGenderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageSelected(int state) {}
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mGenderViewPager.getCurrentItem();

                    mSortData.put("GENDER", mGenderAdapter.mItems.get(position).getmCode());
                    mGenderAdapter.setItemSelected(position);
                    //mMotionViewPager.setCurrentItem(mMotionViewPager.getCurrentItem() + (mGenderSpot - position));
                    mGenderSpot = position;
                    setInitSortImage();
                    setHeaderCheckImage();
                }
            }
        });

        mGenderViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), -1 * event.getX(), event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                mMotionViewPager.onTouchEvent(motionEvent);
                return false;
            }
        });

        mGenerationViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageSelected(int state) {}
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mGenerationViewPager.getCurrentItem();

                    mSortData.put("GENERATION", mGenerationAdapter.mItems.get(position).getmCode());
                    mGenerationAdapter.setItemSelected(position);
                    //mMotionViewPager.setCurrentItem(mMotionViewPager.getCurrentItem() + (mGenerationSpot - position));
                    mGenerationSpot = position;
                    setInitSortImage();
                    setHeaderCheckImage();
                }
            }
        });

        mGenerationViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), -1 * event.getX(), event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                mMotionViewPager.onTouchEvent(motionEvent);
                return false;
            }
        });

        mTimeViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageSelected(int state) {}
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mTimeViewPager.getCurrentItem();

                    if (position == 1) { // 7시 > 6시
                        mTimeViewPager.setCurrentItem(0);
                        return;
                    } else if (position == 4) { // 10시 > 9시
                        mTimeViewPager.setCurrentItem(3);
                        return;
                    } else if (position == 5) { // 11시 > 12시
                        mTimeViewPager.setCurrentItem(6);
                        return;
                    } else if (position == 9 || position == 10) { // 14시 or 15시 > 13시
                        mTimeViewPager.setCurrentItem(8);
                        return;
                    } else if (position == 11 || position == 12) { // 16시 or 17시 > 18시
                        mTimeViewPager.setCurrentItem(13);
                        return;
                    } else if (position == 15) { // 20시 > 19시
                        mTimeViewPager.setCurrentItem(14);
                        return;
                    } else if (position == 17) { // 22시 > 21시
                        mTimeViewPager.setCurrentItem(16);
                        return;
                    } else if (position == 18 || position == 20 || position == 21 || position == 22 || position == 23 || position == 24) { // 23시 or 01시 or 02시 or 03시 or 04시 or 05시 > 24시
                        mTimeViewPager.setCurrentItem(19);
                        return;
                    }

                    mSortData.put("TIME", mTimeAdapter.mItems.get(position).getmCode());
                    mTimeAdapter.setItemSelected(position);
                    //mMotionViewPager.setCurrentItem(mMotionViewPager.getCurrentItem() + (mTimeSpot - position));
                    mTimeSpot = position;
                    setInitSortImage();
                    setHeaderCheckImage();
                }
            }
        });

        mTimeViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), -1 * event.getX(), event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                mMotionViewPager.onTouchEvent(motionEvent);
                return false;
            }
        });

        mLocationViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageSelected(int state) {}
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int position = mLocationViewPager.getCurrentItem();

                    mSortData.put("DISTANCE", mLocationAdapter.mItems.get(position).getmCode());
                    mLocationAdapter.setItemSelected(position);
                    //mMotionViewPager.setCurrentItem(mMotionViewPager.getCurrentItem() + (mLocationSpot - position));
                    mLocationSpot = position;
                    setInitSortImage();
                    setHeaderCheckImage();
                }
            }
        });

        mLocationViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MotionEvent motionEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), -1 * event.getX(), event.getY(), event.getPressure(), event.getSize(), event.getMetaState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags());
                mMotionViewPager.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    private void setInitSortImage() {
        if ( CommonUtil.isNull(mSortData.get("GENDER")) && CommonUtil.isNull(mSortData.get("GENERATION")) && CommonUtil.isNull(mSortData.get("TIME")) && CommonUtil.isNull(mSortData.get("DISTANCE")) ) {
            btnInitSortImage.setBackgroundResource(R.drawable.bt_sort_disable);
            btnInitSort.setOnClickListener(null);
        } else {
            btnInitSortImage.setBackgroundResource(R.drawable.bt_sort_enable);
            btnInitSort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGenderViewPager.setCurrentItem(1);
                    mGenerationViewPager.setCurrentItem(2);
                    mTimeViewPager.setCurrentItem(7);
                    mLocationViewPager.setCurrentItem(2);
                }
            });
        }
    }

    private void setHeaderCheckImage() {
       if (mCurrentSortData.equals(mSortData)) {
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
                    mdbHelper = new PostDatabases(mContext);
                    mdbHelper.open();
                    mdbHelper.setSortData("POST", mSortData);
                    mdbHelper.close();
                    setResult(Constants.RESULT_SUCCESS);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch (msg.what) {
            case Constants.QUERY_APP_VERSION:
                break;
        }
    }
}
