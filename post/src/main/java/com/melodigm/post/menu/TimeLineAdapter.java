package com.melodigm.post.menu;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.protocol.data.TimeLineItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.EllipsizingTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeLineAdapter extends ArrayAdapter<TimeLineItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
    private RequestManager mGlideRequestManager;
	private List<TimeLineItem> mItems;

	public TimeLineAdapter(Context context, int textViewResourceId, ArrayList<TimeLineItem> objects, RequestManager requestManager) {
		super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mLayoutId = textViewResourceId;
        this.mItems = objects;
        this.mGlideRequestManager = requestManager;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}
	
	@Override
	public TimeLineItem getItem(int position) {
		return mItems.get(position);
	}

    class ViewHolder {
        TextView tvTimeLineRegDate, tvTimeLineTitle, tvTimeLineOstSongName, tvTimeLineOstArtiName, tvTimeLineOstContent;
        ImageView ivTimeLineTypeCircle, ivTimeLineType, ivTimeLineIcon;
        CircularImageView ivOstImage;
        LinearLayout timeLineBodyPostLayout, timeLineBodyOstLayout;
        EllipsizingTextView tvTimeLinePostContent;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvTimeLineRegDate = (TextView)convertView.findViewById(R.id.tvTimeLineRegDate);
            viewHolder.ivTimeLineTypeCircle = (ImageView)convertView.findViewById(R.id.ivTimeLineTypeCircle);
            viewHolder.tvTimeLineTitle = (TextView)convertView.findViewById(R.id.tvTimeLineTitle);
            viewHolder.ivTimeLineType = (ImageView)convertView.findViewById(R.id.ivTimeLineType);
            viewHolder.ivTimeLineIcon = (ImageView)convertView.findViewById(R.id.ivTimeLineIcon);
            viewHolder.timeLineBodyPostLayout = (LinearLayout)convertView.findViewById(R.id.timeLineBodyPostLayout);
            viewHolder.tvTimeLinePostContent = (EllipsizingTextView)convertView.findViewById(R.id.tvTimeLinePostContent);
            viewHolder.timeLineBodyOstLayout = (LinearLayout)convertView.findViewById(R.id.timeLineBodyOstLayout);
            viewHolder.ivOstImage = (CircularImageView)convertView.findViewById(R.id.ivOstImage);
            viewHolder.tvTimeLineOstSongName = (TextView)convertView.findViewById(R.id.tvTimeLineOstSongName);
            viewHolder.tvTimeLineOstArtiName = (TextView)convertView.findViewById(R.id.tvTimeLineOstArtiName);
            viewHolder.tvTimeLineOstContent = (TextView)convertView.findViewById(R.id.tvTimeLineOstContent);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 타임라인 등록일시
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(mItems.get(position).getREG_DATE());
            viewHolder.tvTimeLineRegDate.setText(DateUtil.getDateDisplayBy12_24(date));
        } catch (ParseException e) {
            viewHolder.tvTimeLineRegDate.setText(mItems.get(position).getREG_DATE());
        }

        // 타임라인 타입별 아이콘
        viewHolder.ivTimeLineType.setImageResource(android.R.color.transparent);
        viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#00000000")));
        switch (mItems.get(position).getPOST_TYPE()) {
            case "BI01":
                if ("Y".equals(mItems.get(position).getTITL_YN()))
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tltitle_post);
                else
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tl_post);
                viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF00AFD5")));
                break;
            case "BI04":
                if ("Y".equals(mItems.get(position).getTITL_YN()))
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tltitle_post);
                else
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tl_ostp);
                viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FF00AFD5")));
                break;
            case "BI02":
                viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tl_tod);
                viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFFFD83B")));
                break;
            case "BI05":
                viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tl_ostt);
                viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFFFD83B")));
                break;
            case "BI03":
                if ("Y".equals(mItems.get(position).getTITL_YN()))
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_title_today);
                else
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tl_rdo);
                viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFF65857")));
                break;
            case "BI06":
                if ("Y".equals(mItems.get(position).getTITL_YN()))
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_title_today);
                else
                    viewHolder.ivTimeLineType.setImageResource(R.drawable.icon_tl_ostr);
                viewHolder.ivTimeLineTypeCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFF65857")));
                break;
        }

        // 좋아요 타입
        viewHolder.ivTimeLineIcon.setImageResource(android.R.color.transparent);
        if ("Y".equals(mItems.get(position).getLIKE_YN())) {
            viewHolder.ivTimeLineIcon.setImageResource(R.drawable.icon_like_rel);
        } else {
            // 대댓글 타입
            if ("Y".equals(mItems.get(position).getREPLY_YN())) {
                if ("BF04".equals(mItems.get(position).getPOST_TYPE()))
                    viewHolder.ivTimeLineIcon.setImageResource(R.drawable.icon_ost_rerel);
                else if ("BF05".equals(mItems.get(position).getPOST_TYPE()))
                    viewHolder.ivTimeLineIcon.setImageResource(R.drawable.icon_tdo_rerel);
                else if ("BF06".equals(mItems.get(position).getPOST_TYPE()))
                    viewHolder.ivTimeLineIcon.setImageResource(R.drawable.icon_rdo_rerel);
            }
        }

        // 타임라인 내용
        viewHolder.timeLineBodyOstLayout.setVisibility(View.GONE);
        viewHolder.timeLineBodyPostLayout.setVisibility(View.GONE);

        // POST 타입
        viewHolder.tvTimeLineTitle.setAlpha(1.0f);
        viewHolder.tvTimeLineTitle.setText("");
        if ( "BI01".equals(mItems.get(position).getPOST_TYPE()) || "BI02".equals(mItems.get(position).getPOST_TYPE()) || "BI03".equals(mItems.get(position).getPOST_TYPE()) ) {
            // 타임라인 타이틀
            // 라디오 + 제목 / 내용 없을 경우 제목에 재생시간을 표시한다
            if ("BI03".equals(mItems.get(position).getPOST_TYPE()) && CommonUtil.isNull(mItems.get(position).getPOST_SUBJ()) && CommonUtil.isNull(mItems.get(position).getPOST_CONT())) {
                viewHolder.tvTimeLineTitle.setText(DateUtil.getConvertMsToFormat(mItems.get(position).getRUN_TIME() / 1000));
                viewHolder.tvTimeLineTitle.setAlpha(0.8f);
            } else {
                if (!"".equals(mItems.get(position).getCOLOR_HEX())) {
                    if ("ffffff".equals(mItems.get(position).getCOLOR_HEX()))
                        viewHolder.tvTimeLineTitle.setTextColor(Color.parseColor("#FF000000"));
                    else
                        viewHolder.tvTimeLineTitle.setTextColor(Color.parseColor("#" + mItems.get(position).getCOLOR_HEX()));
                }
                viewHolder.tvTimeLineTitle.setText(mItems.get(position).getPOST_SUBJ());
            }


            viewHolder.timeLineBodyOstLayout.setVisibility(View.GONE);

            // 내용이 없을 경우 내용영역을 노출하지 않는다
            if (CommonUtil.isNull(mItems.get(position).getPOST_CONT())) {
                viewHolder.timeLineBodyPostLayout.setVisibility(View.GONE);
            } else {
                viewHolder.timeLineBodyPostLayout.setVisibility(View.VISIBLE);
                viewHolder.tvTimeLinePostContent.setMaxLines(2);
                viewHolder.tvTimeLinePostContent.setMoreLinkEnabled(false);
                viewHolder.tvTimeLinePostContent.setText(mItems.get(position).getPOST_CONT());
            }
        }
        // OST 타입
        else if ( "BI04".equals(mItems.get(position).getPOST_TYPE()) || "BI05".equals(mItems.get(position).getPOST_TYPE()) || "BI06".equals(mItems.get(position).getPOST_TYPE()) ) {
            viewHolder.tvTimeLineTitle.setText("");
            viewHolder.timeLineBodyPostLayout.setVisibility(View.GONE);
            viewHolder.timeLineBodyOstLayout.setVisibility(View.VISIBLE);

            if (!"".equals(mItems.get(position).getALBUM_PATH())) {
                mGlideRequestManager
                    .load(mItems.get(position).getALBUM_PATH())
                    .error(R.drawable.icon_album_dummy)
                    .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                    .into(viewHolder.ivOstImage);
            }

            viewHolder.tvTimeLineOstSongName.setText(mItems.get(position).getSONG_NM());
            viewHolder.tvTimeLineOstArtiName.setText(mItems.get(position).getARTI_NM());

            if (CommonUtil.isNull(mItems.get(position).getOST_CONT())) {
                viewHolder.tvTimeLineOstContent.setVisibility(View.GONE);
            } else {
                viewHolder.tvTimeLineOstContent.setVisibility(View.VISIBLE);
                viewHolder.tvTimeLineOstContent.setText(mItems.get(position).getOST_CONT());
            }
        }

		return convertView;
	}
}
