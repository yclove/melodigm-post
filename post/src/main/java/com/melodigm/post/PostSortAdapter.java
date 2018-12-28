package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melodigm.post.protocol.data.PostSortData;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

public class PostSortAdapter extends PagerAdapter {
    Context mContext;
    WeakRefHandler mHandler;
    LayoutInflater inflater;
    List<PostSortData> mItems = new ArrayList<>();
    String mDesignType;

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public PostSortAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler, String designType) {
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
        this.mDesignType = designType;
    }

    @Override
    public int getCount() {
        if ("motion".equals(mDesignType)) {
            return 100;
        } else {
            return mItems.size();
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;

        if ("multi".equals(mDesignType))
            view = inflater.inflate(R.layout.viewpager_sort_multi, null);
        else if ("single".equals(mDesignType))
            view = inflater.inflate(R.layout.viewpager_sort, null);
        else if ("timeline".equals(mDesignType))
            view = inflater.inflate(R.layout.viewpager_sort_timeline, null);
        else
            view = inflater.inflate(R.layout.viewpager_sort_motion, null);

        if (!"motion".equals(mDesignType)) {
            PostSortData item = mItems.get(position);

            TextView tvSortPoint = (TextView) view.findViewById(R.id.tvSortPoint);
            if (CommonUtil.isNull(item.getmTitle()))
                tvSortPoint.setVisibility(View.GONE);
            else
                tvSortPoint.setVisibility(View.VISIBLE);

            TextView tvSortVal = (TextView) view.findViewById(R.id.tvSortVal);
            tvSortVal.setText(item.getmTitle());

            if ("timeline".equals(mDesignType)) {
                if (item.isSelected())
                    tvSortVal.setTextColor(Color.parseColor("#FFFFD83B"));
                else
                    tvSortVal.setTextColor(Color.parseColor("#FFFFFFFF"));
            } else {
                if (item.isSelected())
                    tvSortVal.setTextColor(Color.parseColor("#FF00AFD5"));
                else
                    tvSortVal.setTextColor(Color.parseColor("#FF000000"));
            }

            if ("multi".equals(mDesignType)) {
                TextView tvSortMultiVal = (TextView) view.findViewById(R.id.tvSortMultiVal);
                tvSortMultiVal.setText(item.getmContent());

                if (item.isSelected())
                    tvSortMultiVal.setTextColor(Color.parseColor("#FF00AFD5"));
                else
                    tvSortMultiVal.setTextColor(Color.parseColor("#4D000000"));
            } else if ("timeline".equals(mDesignType)) {
                TextView tvSortMultiVal = (TextView) view.findViewById(R.id.tvSortMultiVal);
                tvSortMultiVal.setText(item.getmContent());

                if (item.isSelected())
                    tvSortMultiVal.setTextColor(Color.parseColor("#FFFFD83B"));
                else
                    tvSortMultiVal.setTextColor(Color.parseColor("#4DFFFFFF"));
            }
        }

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

    public void addAllItems(ArrayList<PostSortData> data) {
        if (data != null) {
            mItems = data;
        } else {
            mItems.clear();
        }
        notifyDataSetChanged();
    }

    public void setItemSelected(int position) {
        for (PostSortData item : mItems) {
            item.setSelected(false);
        }
        mItems.get(position).setSelected(true);
        notifyDataSetChanged();
    }
}
