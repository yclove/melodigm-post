package com.melodigm.post.search;

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

import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.widget.CircularImageView;

import java.util.ArrayList;

public class SearchOstAdapter extends ArrayAdapter<OstDataItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
    private RequestManager mGlideRequestManager;
	private ArrayList<OstDataItem> mItems;
    private OnListViewClickListener mOnListViewClickListener = null;
    private int preMusicPosition = -1;
	public SearchOstAdapter(Context context, int textViewResourceId, ArrayList<OstDataItem> objects, RequestManager requestManager) {
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
	public OstDataItem getItem(int position) {
		return mItems.get(position);
	}

    public void addAllItems(ArrayList<OstDataItem> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    public class ViewHolder {
        public LinearLayout searchOstItemLayout;
        public CircularImageView btnOstImage;
        public ImageView ivExistOstIcon, ivPreControlIcon;
        public TextView tvOstSongName;
        public TextView tvOstArtiName;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.searchOstItemLayout = (LinearLayout)convertView.findViewById(R.id.searchOstItemLayout);
            viewHolder.btnOstImage = (CircularImageView)convertView.findViewById(R.id.btnOstImage);
            viewHolder.ivExistOstIcon = (ImageView)convertView.findViewById(R.id.ivExistOstIcon);
            viewHolder.ivPreControlIcon = (ImageView)convertView.findViewById(R.id.ivPreControlIcon);
            viewHolder.tvOstSongName = (TextView)convertView.findViewById(R.id.tvOstSongName);
            viewHolder.tvOstArtiName = (TextView)convertView.findViewById(R.id.tvOstArtiName);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        OstDataItem item = mItems.get(position);
        if (item != null) {
            // 아이템 선택 효과
            if (item.isChecked()) {
                viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#FFFBF7F2"));
            } else {
                viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            }

            if (!"".equals(item.getALBUM_PATH())) {
                mGlideRequestManager
                        .load(item.getALBUM_PATH())
                        .error(R.drawable.icon_album_dummy)
                        .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                        .into(viewHolder.btnOstImage);
            }

            // OST 등록 여부에 따른 Display
            if ("Y".equals(item.getOST_YN())) {
                viewHolder.ivExistOstIcon.setVisibility(View.VISIBLE);
                viewHolder.ivPreControlIcon.setVisibility(View.GONE);
                viewHolder.btnOstImage.setAlpha(0.2f);
                viewHolder.tvOstSongName.setAlpha(0.2f);
                viewHolder.tvOstArtiName.setAlpha(0.2f);
            } else {
                viewHolder.ivExistOstIcon.setVisibility(View.GONE);
                viewHolder.ivPreControlIcon.setVisibility(View.VISIBLE);
                viewHolder.btnOstImage.setAlpha(1.0f);
                viewHolder.tvOstSongName.setAlpha(1.0f);
                viewHolder.tvOstArtiName.setAlpha(1.0f);
            }

            viewHolder.tvOstSongName.setText(item.getSONG_NM());
            viewHolder.tvOstArtiName.setText(item.getARTI_NM());

            // 미리듣기 중일 경우 아이콘 변경
            if (position == preMusicPosition) {
                viewHolder.ivPreControlIcon.setImageResource(R.drawable.bt_pre_stop);
            } else {
                viewHolder.ivPreControlIcon.setImageResource(R.drawable.bt_titleplay);
            }

            viewHolder.ivPreControlIcon.setTag(R.id.tag_position, position);
            viewHolder.ivPreControlIcon.setTag(R.id.tag_ssi, item.getSSI());
            viewHolder.ivPreControlIcon.setOnClickListener(mOnClickListener);
        }

		return convertView;
    }

    public void updatePreControlUI(int position) {
        preMusicPosition = position;
        notifyDataSetChanged();
    }

    public interface OnListViewClickListener{
        void onViewClick(int position, String SSI);
    }

    public void setOnListViewClickListener(OnListViewClickListener listener){
        mOnListViewClickListener = listener;
    }

    //아이템 클릭 시,
    View.OnClickListener mOnClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int)v.getTag(R.id.tag_position);
            String SSI = (String)v.getTag(R.id.tag_ssi);
            if(mOnListViewClickListener != null)
                mOnListViewClickListener.onViewClick(position, SSI);
        }
    };
}
