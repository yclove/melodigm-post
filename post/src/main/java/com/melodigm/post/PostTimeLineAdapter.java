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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.EllipsizingTextView;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.util.ArrayList;
import java.util.List;

public class PostTimeLineAdapter extends PagerAdapter implements View.OnClickListener {
    Context mContext;
    WeakRefHandler mHandler;
    LayoutInflater inflater;
    RequestManager mGlideRequestManager;
    List<PostDataItem> mItems = new ArrayList<>();
    int mPosition = 0;
    String mColor;

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public PostTimeLineAdapter(Context context, LayoutInflater inflater, WeakRefHandler handler, RequestManager requestManager) {
        this.mContext = context;
        this.inflater = inflater;
        this.mHandler = handler;
        this.mGlideRequestManager = requestManager;
    }

    @Override
    public int getCount() {
        if (mItems.size() > 0)
            return mItems.size();
        else
            return 3;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.viewpager_post_timeline, null);

        LinearLayout llPostTimeLineEmptyFirstLayout = (LinearLayout)view.findViewById(R.id.llPostTimeLineEmptyFirstLayout);
        LinearLayout llPostTimeLineEmptySecondLayout = (LinearLayout)view.findViewById(R.id.llPostTimeLineEmptySecondLayout);
        LetterSpacingTextView lstvPostTimeLineEmptySubject = (LetterSpacingTextView)view.findViewById(R.id.lstvPostTimeLineEmptySubject);

        RelativeLayout rlPostTimeLineItem = (RelativeLayout)view.findViewById(R.id.rlPostTimeLineItem);
        LetterSpacingTextView lstvPostTimeLineSubject = (LetterSpacingTextView)view.findViewById(R.id.lstvPostTimeLineSubject);
        LetterSpacingTextView lstvPostTimeLineRadioDuration = (LetterSpacingTextView)view.findViewById(R.id.lstvPostTimeLineRadioDuration);
        EllipsizingTextView tvPostTimeLineContent = (EllipsizingTextView)view.findViewById(R.id.tvPostTimeLineContent);
        LinearLayout llPostTimeLineOstLayout = (LinearLayout)view.findViewById(R.id.llPostTimeLineOstLayout);
        CircularImageView lvPostTimeLineOst = (CircularImageView)view.findViewById(R.id.lvPostTimeLineOst);
        TextView tvPostTimeLineOstSongName = (TextView)view.findViewById(R.id.tvPostTimeLineOstSongName);
        TextView tvPostTimeLineOstArtiName = (TextView)view.findViewById(R.id.tvPostTimeLineOstArtiName);

        if (mItems.size() == 0) {
            if (position == 0) {
                llPostTimeLineEmptyFirstLayout.setVisibility(View.VISIBLE);
                llPostTimeLineEmptySecondLayout.setVisibility(View.GONE);
            } else {
                llPostTimeLineEmptyFirstLayout.setVisibility(View.GONE);
                llPostTimeLineEmptySecondLayout.setVisibility(View.VISIBLE);

                lstvPostTimeLineSubject.setSpacing(Constants.TEXT_VIEW_SPACING);
                lstvPostTimeLineSubject.setText(mContext.getString(R.string.post_story));
            }
        } else {
            llPostTimeLineEmptyFirstLayout.setVisibility(View.GONE);
            llPostTimeLineEmptySecondLayout.setVisibility(View.GONE);

            PostDataItem item = mItems.get(position);

            // ITEM 클릭 이벤트
            rlPostTimeLineItem.setTag(R.id.tag_position, position);
            rlPostTimeLineItem.setOnClickListener(this);

            // POST 제목
            if (CommonUtil.isNotNull(item.getPOST_SUBJ())) {
                lstvPostTimeLineSubject.setSpacing(0.0f);
                lstvPostTimeLineSubject.setText(item.getPOST_SUBJ());
                if ("FFFFFF".equals(item.getCOLOR_HEX().toUpperCase())) {
                    lstvPostTimeLineSubject.setTextColor(Color.parseColor("#FF000000"));
                } else {
                    lstvPostTimeLineSubject.setTextColor(Color.parseColor("#FF" + item.getCOLOR_HEX()));
                }
                lstvPostTimeLineSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            } else {
                lstvPostTimeLineSubject.setSpacing(Constants.TEXT_VIEW_SPACING);
                if (Constants.REQUEST_TYPE_POST.equals(item.getPOST_TYPE())) {
                    lstvPostTimeLineSubject.setText(mContext.getString(R.string.post_story));
                } else if (Constants.REQUEST_TYPE_RADIO.equals(item.getPOST_TYPE())) {
                    lstvPostTimeLineSubject.setText(mContext.getString(R.string.post_radio));
                }
                lstvPostTimeLineSubject.setTextColor(Color.parseColor("#80000000"));
                lstvPostTimeLineSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            }

            // RADIO 재생시간
            if (Constants.REQUEST_TYPE_RADIO.equals(item.getPOST_TYPE())) {
                lstvPostTimeLineRadioDuration.setVisibility(View.VISIBLE);
                lstvPostTimeLineRadioDuration.setSpacing(Constants.TEXT_VIEW_SPACING);
                lstvPostTimeLineRadioDuration.setText(DateUtil.getConvertMsToFormat(item.getRADIO_RUNTIME() / 1000));
            } else {
                lstvPostTimeLineRadioDuration.setVisibility(View.GONE);
            }

            // OST 타이틀 영역
            if (CommonUtil.isNull(item.getOTI())) {
                llPostTimeLineOstLayout.setVisibility(View.GONE);
                tvPostTimeLineContent.setMaxLines(12);
            } else {
                llPostTimeLineOstLayout.setVisibility(View.VISIBLE);
                tvPostTimeLineContent.setMaxLines(8);

                if (!"".equals(item.getTITLE_ALBUM_PATH())) {
                    mGlideRequestManager
                            .load(item.getTITLE_ALBUM_PATH())
                            .error(R.drawable.icon_album_dummy)
                            .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()))
                            .into(lvPostTimeLineOst);
                }
                tvPostTimeLineOstSongName.setText(item.getTITLE_SONG_NM());
                tvPostTimeLineOstArtiName.setText(item.getTITLE_ARTI_NM());
            }

            // POST 내용
            tvPostTimeLineContent.setMoreLinkEnabled(false);
            tvPostTimeLineContent.setText(item.getPOST_CONT());

            if (mItems.size() == position + 1) {
                mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA_ADD);
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

    public void addAllItems(ArrayList<PostDataItem> data) {
        if (data != null) {
            mItems = data;
        } else {
            mItems.clear();
        }
        notifyDataSetChanged();
    }

    public void setPosition(int position) {
        this.mPosition = position;
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
            case R.id.rlPostTimeLineItem:
                data.putInt("POSITION", (int)v.getTag(R.id.tag_position));
                msg.setData(data);
                msg.what = Constants.QUERY_TIME_LINE;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
