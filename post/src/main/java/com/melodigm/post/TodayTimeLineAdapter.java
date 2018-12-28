package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.wheel.adapters.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.List;

public class TodayTimeLineAdapter extends AbstractWheelTextAdapter implements View.OnClickListener {
    Context mContext;
    List<PostDataItem> mItems = new ArrayList<>();
    WeakRefHandler mHandler;

    protected TodayTimeLineAdapter(Context context, WeakRefHandler handler) {
        super(context, R.layout.adapter_today_timeline, NO_RESOURCE);
        this.mContext = context;
        this.mHandler = handler;

        setItemTextResource(R.id.lstvTodayRegDate);
    }

    @Override
    public View getItem(int position, View cachedView, ViewGroup parent) {
        View view = super.getItem(position, cachedView, parent);

        PostDataItem item = mItems.get(position);

        LinearLayout llTodayTimeLineItem = (LinearLayout)view.findViewById(R.id.llTodayTimeLineItem);
        llTodayTimeLineItem.setOnClickListener(this);

        LetterSpacingTextView lstvTodayRegDate = (LetterSpacingTextView)view.findViewById(R.id.lstvTodayRegDate);
        lstvTodayRegDate.setSpacing(Constants.TEXT_VIEW_SPACING);

        try {
            String REG_DATE = item.getREG_DATE().substring(0, 10);
            String TODAY_REG_DATE = DateUtil.getCurrentDate("yyyy-MM-dd");

            if (REG_DATE.equals(TODAY_REG_DATE)) {
                lstvTodayRegDate.setTextColor(Color.parseColor("#FFFFCC4F"));
                lstvTodayRegDate.setText(mContext.getString(R.string.today));
            } else {
                lstvTodayRegDate.setTextColor(Color.parseColor("#80000000"));
                lstvTodayRegDate.setText(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(mItems.get(position).getREG_DATE().substring(0, 10)), "dd") + " " + DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(mItems.get(position).getREG_DATE().substring(0, 10)), "EEE").toUpperCase());
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }

        ImageView ivTodayCircle = (ImageView)view.findViewById(R.id.ivTodayCircle);
        if ("FFFFFF".equals(item.getCOLOR_HEX().toUpperCase())) {
            ivTodayCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF000000")));
        } else {
            ivTodayCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF" + item.getCOLOR_HEX())));
        }

        TextView tvTodayKWD = (TextView)view.findViewById(R.id.tvTodayKWD);
        tvTodayKWD.setText(item.getKWD());
        if ("FFFFFF".equals(item.getCOLOR_HEX().toUpperCase())) {
            tvTodayKWD.setTextColor(Color.parseColor("#FF000000"));
        } else {
            tvTodayKWD.setTextColor(Color.parseColor("#FF" + item.getCOLOR_HEX()));
        }
        tvTodayKWD.setOnClickListener(this);

        if (mItems.size() == position + 1) {
            mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA_ADD);
        }

        return view;
    }

    @Override
    public int getItemsCount() {
        return mItems.size();
    }

    public void addAllItems(ArrayList<PostDataItem> data) {
        if (data != null) {
            mItems = data;
        } else {
            mItems.clear();
        }
        notifyDataInvalidatedEvent();
    }

    @Override
    protected CharSequence getItemText(int index) {
        return mItems.get(index).getREG_DATE();
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();
        int position;

        switch (v.getId()) {
            case R.id.llTodayTimeLineItem:
                DeviceUtil.showToast(mContext, "llTodayTimeLineItem");
                break;
        }
    }
}