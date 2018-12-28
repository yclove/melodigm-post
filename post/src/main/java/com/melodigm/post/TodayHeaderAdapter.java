package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.util.ArrayList;
import java.util.List;

public class TodayHeaderAdapter extends PagerAdapter implements View.OnClickListener {
    Context mContext;
    WeakRefHandler mHandler;
    LayoutInflater inflater;
    List<PostDataItem> mItems = new ArrayList<>();
    String mColor;

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public TodayHeaderAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler) {
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.viewpager_today, null);

        PostDataItem item = mItems.get(position);

        RelativeLayout vg_today = (RelativeLayout) view.findViewById(R.id.vg_today);
        LetterSpacingTextView lstvDayWeekDay = (LetterSpacingTextView) view.findViewById(R.id.lstvDayWeekDay);
        ImageView ivTodayCircle = (ImageView) view.findViewById(R.id.ivTodayCircle);
        TextView tvKWD = (TextView) view.findViewById(R.id.tvKWD);

        int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DeviceUtil.getScreenWidthInDPs(mContext) / 2, mContext.getResources().getDisplayMetrics());
        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 92, mContext.getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        vg_today.setLayoutParams(lp);

        vg_today.setTag(R.id.tag_color, item.getCOLOR());
        vg_today.setOnClickListener(this);

        lstvDayWeekDay.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvDayWeekDay.setText(DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(mItems.get(position).getREG_DATE().substring(0, 10)), "dd") + " " + DateUtil.getDateDisplayUnit(java.sql.Date.valueOf(mItems.get(position).getREG_DATE().substring(0, 10)), "EEE").toUpperCase());
        if (CommonUtil.isNotNull(mColor)) lstvDayWeekDay.setTextColor(Color.parseColor("#FF" + mItems.get(position).getCOLOR_HEX()));

        ivTodayCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF" + mItems.get(position).getCOLOR_HEX())));
        tvKWD.setText(mItems.get(position).getKWD());
        tvKWD.setTextColor(Color.parseColor("#FF" + mItems.get(position).getCOLOR_HEX()));

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View v, Object obj) {
        return v == obj;
    }

    public void addAllItems(ArrayList<PostDataItem> data) {
        if (data != null) {
            mItems = data;
        } else {
            mItems.clear();
        }
        notifyDataSetChanged();
    }

    public void setAdapterColor(String color) {
        this.mColor = color;
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            case R.id.vg_today:
                if (CommonUtil.isNull(mColor))
                    data.putString("ICI", (String) v.getTag(R.id.tag_color));
                else
                    data.putString("ICI", "");
                msg.setData(data);
                msg.what = Constants.QUERY_POST_DATA;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
