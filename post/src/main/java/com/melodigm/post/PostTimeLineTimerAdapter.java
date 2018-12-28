package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melodigm.post.protocol.data.PostSortData;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.wheel.adapters.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.List;

public class PostTimeLineTimerAdapter extends AbstractWheelTextAdapter {
    Context mContext;
    List<PostSortData> mItems = new ArrayList<>();
    WeakRefHandler mHandler;

    protected PostTimeLineTimerAdapter(Context context, WeakRefHandler handler) {
        super(context, R.layout.adapter_post_timeline_timer, NO_RESOURCE);
        this.mContext = context;
        this.mHandler = handler;

        mItems.add(new PostSortData("", "", "1"));
        mItems.add(new PostSortData("", "", "2"));
        mItems.add(new PostSortData("", "", "3"));
        mItems.add(new PostSortData("", "", "4"));
        mItems.add(new PostSortData("", "", "5"));
        mItems.add(new PostSortData("BD01", "굿모닝", "6"));
        mItems.add(new PostSortData("", "", "7"));
        mItems.add(new PostSortData("BD02", "출근", "8"));
        mItems.add(new PostSortData("BD03", "오전", "9"));
        mItems.add(new PostSortData("", "", "10"));
        mItems.add(new PostSortData("", "", "11"));
        mItems.add(new PostSortData("BD04", "점심", "12"));
        mItems.add(new PostSortData("BD05", "오후", "1"));
        mItems.add(new PostSortData("", "", "2"));
        mItems.add(new PostSortData("", "", "3"));
        mItems.add(new PostSortData("", "", "4"));
        mItems.add(new PostSortData("", "", "5"));
        mItems.add(new PostSortData("BD06", "퇴근", "6"));
        mItems.add(new PostSortData("BD07", "저녁", "7"));
        mItems.add(new PostSortData("", "", "8"));
        mItems.add(new PostSortData("BD08", "밤", "9"));
        mItems.add(new PostSortData("", "", "10"));
        mItems.add(new PostSortData("", "", "11"));
        mItems.add(new PostSortData("BD09", "굿나잇", "12"));

        setItemTextResource(R.id.tvSortVal);
    }

    @Override
    public View getItem(int position, View cachedView, ViewGroup parent) {
        View view = super.getItem(position, cachedView, parent);

        PostSortData item = mItems.get(position);

        TextView tvSortPoint = (TextView) view.findViewById(R.id.tvSortPoint);
        if (CommonUtil.isNull(item.getmTitle()))
            tvSortPoint.setVisibility(View.GONE);
        else
            tvSortPoint.setVisibility(View.VISIBLE);

        TextView tvSortVal = (TextView) view.findViewById(R.id.tvSortVal);
        tvSortVal.setText(item.getmTitle());

        if (item.isSelected())
            tvSortVal.setTextColor(Color.parseColor("#FFFFD83B"));
        else
            tvSortVal.setTextColor(Color.parseColor("#FFFFFFFF"));

        TextView tvSortMultiVal = (TextView) view.findViewById(R.id.tvSortMultiVal);
        tvSortMultiVal.setText(item.getmContent());

        if (item.isSelected())
            tvSortMultiVal.setTextColor(Color.parseColor("#FFFFD83B"));
        else
            tvSortMultiVal.setTextColor(Color.parseColor("#4DFFFFFF"));

        return view;
    }

    @Override
    public int getItemsCount() {
        return mItems.size();
    }

    public void setItemSelected(int position) {
        for (PostSortData item : mItems) {
            item.setSelected(false);
        }

        if (mItems.size() > position) {
            mItems.get(position).setSelected(true);
            notifyDataChangedEvent();
        }
    }

    @Override
    protected CharSequence getItemText(int index) {
        return mItems.get(index).getmTitle();
    }
}