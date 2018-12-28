package com.melodigm.post;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melodigm.post.widget.wheel.adapters.AbstractWheelTextAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlayerTimerAdapter extends AbstractWheelTextAdapter {
    Context mContext;
    String mTimerType;
    List<String> mItems = new ArrayList<>();
    int hourPosition = 0;
    int minutePosition = 0;

    protected PlayerTimerAdapter(Context context, String timerType) {
        super(context, R.layout.adapter_player_timer, NO_RESOURCE);
        this.mContext = context;
        this.mTimerType = timerType;

        if ("HOUR".equals(mTimerType)) {
            for (int i = 0; i < 7; i++) {
                mItems.add(String.format("%02d", i));
            }
        } else {
            for (int i = 0; i < 60; i++) {
                mItems.add(String.format("%02d", i));
            }
        }
    }

    @Override
    public View getItem(int position, View cachedView, ViewGroup parent) {
        View view = super.getItem(position, cachedView, parent);

        String item = mItems.get(position);

        TextView tvPlayerTimerText = (TextView)view.findViewById(R.id.tvPlayerTimerText);
        tvPlayerTimerText.setText(item);

        if ("HOUR".equals(mTimerType)) {
            if (hourPosition == position)
                tvPlayerTimerText.setAlpha(1.0f);
            else
                tvPlayerTimerText.setAlpha(0.2f);
        } else {
            if (minutePosition == position)
                tvPlayerTimerText.setAlpha(1.0f);
            else
                tvPlayerTimerText.setAlpha(0.2f);
        }

        return view;
    }

    @Override
    public int getItemsCount() {
        if ("HOUR".equals(mTimerType))
            return 7;
        else
            return 60;
    }

    @Override
    protected CharSequence getItemText(int index) {
        return "";
    }

    public void setCurrentItem(String timerType, int position) {
        if ("HOUR".equals(timerType))
            this.hourPosition = position;
        else
            this.minutePosition = position;

        notifyDataChangedEvent();
    }
}