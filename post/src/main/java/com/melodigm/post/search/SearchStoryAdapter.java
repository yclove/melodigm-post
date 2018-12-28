package com.melodigm.post.search;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.widget.EllipsizingTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchStoryAdapter extends ArrayAdapter<PostDataItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
    private RequestManager mGlideRequestManager;
	private ArrayList<PostDataItem> mItems;

	public SearchStoryAdapter(Context context, int textViewResourceId, ArrayList<PostDataItem> objects, RequestManager requestManager) {
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
	public PostDataItem getItem(int position) {
		return mItems.get(position);
	}

    public void addAllItems(ArrayList<PostDataItem> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    public class ViewHolder {
        public ImageView ivPostTypeIcon, ivStoryLikeIcon, ivStoryOstIcon;
        public TextView tvStorySubj, tvStoryRegDate, tvStoryLikeCount, tvStoryOstCount;
        public EllipsizingTextView tvStoryCont;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivPostTypeIcon = (ImageView)convertView.findViewById(R.id.ivPostTypeIcon);
            viewHolder.tvStorySubj = (TextView)convertView.findViewById(R.id.tvStorySubj);
            viewHolder.tvStoryRegDate = (TextView)convertView.findViewById(R.id.tvStoryRegDate);
            viewHolder.tvStoryCont = (EllipsizingTextView)convertView.findViewById(R.id.tvStoryCont);
            viewHolder.ivStoryLikeIcon = (ImageView)convertView.findViewById(R.id.ivStoryLikeIcon);
            viewHolder.tvStoryLikeCount = (TextView)convertView.findViewById(R.id.tvStoryLikeCount);
            viewHolder.tvStoryOstCount = (TextView)convertView.findViewById(R.id.tvStoryOstCount);
            viewHolder.ivStoryOstIcon = (ImageView)convertView.findViewById(R.id.ivStoryOstIcon);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        PostDataItem item = mItems.get(position);
        if (item != null) {
            // 이야기 타입 별 아이콘
            viewHolder.ivPostTypeIcon.setImageResource(android.R.color.transparent);
            if (item.getPOST_TYPE().equals(Constants.REQUEST_TYPE_POST)) {
                viewHolder.ivPostTypeIcon.setImageResource(R.drawable.icon_tl_post);
            } else if (item.getPOST_TYPE().equals(Constants.REQUEST_TYPE_RADIO)) {
                viewHolder.ivPostTypeIcon.setImageResource(R.drawable.icon_tl_rdo);
            } else if (item.getPOST_TYPE().equals(Constants.REQUEST_TYPE_TODAY)) {
                viewHolder.ivPostTypeIcon.setImageResource(R.drawable.icon_tl_tod);
            }

            // 이야기 제목
            viewHolder.tvStorySubj.setText("");
            if (!"".equals(item.getCOLOR_HEX())) {
                if ("ffffff".equals(item.getCOLOR_HEX()))
                    viewHolder.tvStorySubj.setTextColor(Color.parseColor("#FF000000"));
                else
                    viewHolder.tvStorySubj.setTextColor(Color.parseColor("#" + item.getCOLOR_HEX()));
            }
            viewHolder.tvStorySubj.setText(item.getPOST_SUBJ());

            // 이야기 등록 시간
            viewHolder.tvStoryRegDate.setText("");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date date = simpleDateFormat.parse(item.getREG_DATE());
                viewHolder.tvStoryRegDate.setText(DateUtil.getDateDisplay(date));
            } catch (ParseException e) {
                viewHolder.tvStoryRegDate.setText(item.getREG_DATE());
            }

            // 이야기 내용
            viewHolder.tvStoryCont.setText("");
            if (Constants.REQUEST_TYPE_RADIO.equals(item.getPOST_TYPE()) && CommonUtil.isNull(item.getPOST_CONT()))
                viewHolder.tvStoryCont.setText(mContext.getString(R.string.msg_radio_empty_content));
            else {
                viewHolder.tvStoryCont.setText(item.getPOST_CONT());
                viewHolder.tvStoryCont.setMaxLines(2);
                viewHolder.tvStoryCont.setMoreLinkEnabled(false);
            }

            // 이야기 좋아요 아이콘 / 카운트
            if ("Y".equals(item.getLIKE_TOGGLE_YN()))
                viewHolder.ivStoryLikeIcon.setImageResource(R.drawable.icon_like_rel);
            else
                viewHolder.ivStoryLikeIcon.setImageResource(R.drawable.icon_ost_likenor);
            viewHolder.tvStoryLikeCount.setText(String.valueOf(item.getLIKE_CNT()));

            // 이야기 OST 카운트 / 아이콘
            viewHolder.tvStoryOstCount.setText(String.valueOf(item.getOST_CNT()));
            if ("Y".equals(item.getOST_REG_YN())) {
                if (Constants.REQUEST_TYPE_POST.equals(item.getPOST_TYPE()))
                    viewHolder.ivStoryOstIcon.setImageResource(R.drawable.icon_ost_rel);
                else if (Constants.REQUEST_TYPE_RADIO.equals(item.getPOST_TYPE()))
                    viewHolder.ivStoryOstIcon.setImageResource(R.drawable.icon_rdo_rel);
                else if (Constants.REQUEST_TYPE_TODAY.equals(item.getPOST_TYPE()))
                    viewHolder.ivStoryOstIcon.setImageResource(R.drawable.icon_tod_rel);
            } else
                viewHolder.ivStoryOstIcon.setImageResource(R.drawable.icon_ost_grey);
        }

		return convertView;
    }
}
