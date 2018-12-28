package com.melodigm.post.search;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.widget.CircularImageView;

import java.util.ArrayList;

public class SearchAlbumAdapter extends ArrayAdapter<OstDataItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
    private RequestManager mGlideRequestManager;
	private ArrayList<OstDataItem> mItems;

	public SearchAlbumAdapter(Context context, int textViewResourceId, ArrayList<OstDataItem> objects, RequestManager requestManager) {
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
        RelativeLayout rlAlbumItemLayout;
        CircularImageView ivImage;
        TextView tvSongName, tvArtiName, tvAlbumRegDate;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.rlAlbumItemLayout = (RelativeLayout)convertView.findViewById(R.id.rlAlbumItemLayout);
            viewHolder.ivImage = (CircularImageView)convertView.findViewById(R.id.ivImage);
            viewHolder.tvSongName = (TextView)convertView.findViewById(R.id.tvSongName);
            viewHolder.tvArtiName = (TextView)convertView.findViewById(R.id.tvArtiName);
            viewHolder.tvAlbumRegDate = (TextView)convertView.findViewById(R.id.tvAlbumRegDate);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.rlAlbumItemLayout.setBackgroundColor(Color.parseColor("#FFFBF7F2"));
        } else {
            viewHolder.rlAlbumItemLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

        // 앨범 이미지
        mGlideRequestManager
            .load(mItems.get(position).getALBUM_PATH())
            .error(R.drawable.icon_album_dummy)
            .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
            .into(viewHolder.ivImage);

        if (!"".equals(mItems.get(position).getCOLOR_HEX())) {
            viewHolder.ivImage.setBorderColor(Color.parseColor("#FF" + mItems.get(position).getCOLOR_HEX()));
            viewHolder.ivImage.setBorderWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics()));
        } else {
            viewHolder.ivImage.setBorderColor(android.R.color.transparent);
            viewHolder.ivImage.setBorderWidth(0.0f);
        }


        // 노래제목 / 가수 명 / 앨범 등록일
        viewHolder.tvSongName.setText(mItems.get(position).getSONG_NM());
        viewHolder.tvArtiName.setText(mItems.get(position).getARTI_NM());
        viewHolder.tvAlbumRegDate.setText(DateUtil.getCurrentDate("yyyy.MM.dd"));

		return convertView;
    }
}
