package com.melodigm.post;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.melodigm.post.widget.wheel.adapters.AbstractWheelTextAdapter;

public class TodayTimeLineGridAdapter extends AbstractWheelTextAdapter {
    Context mContext;

    protected TodayTimeLineGridAdapter(Context context) {
        super(context, R.layout.adapter_today_timeline_grid, NO_RESOURCE);
        this.mContext = context;
    }

    @Override
    public View getItem(int position, View cachedView, ViewGroup parent) {
        View view = super.getItem(position, cachedView, parent);
        return view;
    }

    @Override
    public int getItemsCount() {
        return 1;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return "";
    }
}