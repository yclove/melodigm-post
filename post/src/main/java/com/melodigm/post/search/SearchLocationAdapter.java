package com.melodigm.post.search;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.LocationItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DistanceUtil;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.util.ArrayList;
import java.util.List;

public class SearchLocationAdapter extends ArrayAdapter<LocationItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
	private List<LocationItem> mItems;

	public SearchLocationAdapter(Context context, int textViewResourceId, ArrayList<LocationItem> objects) {
		super(context, textViewResourceId, objects);
		mContext = context;
		mLayoutId = textViewResourceId;
		mItems = objects;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}
	
	@Override
	public LocationItem getItem(int position) {
		return mItems.get(position);
	}

    /** YCNOTE - ViewHolder
     * 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말합니다. 각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데 이것의 비용이 큰것을 줄이기 위해 사용합니다.
     */
    class ViewHolder {
        public LetterSpacingTextView tvLocationName;
        public TextView tvLocationAddress;
        public RelativeLayout ivLocationCheck;
        public View vUnderLine;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // 캐시된 뷰가 없을 경우 새로 생성하고 뷰홀더를 생성한다.
		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvLocationName = (LetterSpacingTextView)convertView.findViewById(R.id.tvLocationName);
            viewHolder.tvLocationAddress = (TextView)convertView.findViewById(R.id.tvLocationAddress);
            viewHolder.ivLocationCheck = (RelativeLayout)convertView.findViewById(R.id.ivLocationCheck);
            viewHolder.vUnderLine = convertView.findViewById(R.id.vUnderLine);

            convertView.setTag(viewHolder);
		}
        // 캐시된 뷰가 있을 경우 저장된 뷰홀더를 사용한다
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (mItems.get(position).isChecked()) {
            viewHolder.ivLocationCheck.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivLocationCheck.setVisibility(View.GONE);
        }

        if (CommonUtil.isNull(mItems.get(position).getLat()) && CommonUtil.isNull(mItems.get(position).getLng())) {
            viewHolder.tvLocationName.setText(mContext.getString(R.string.common_somewhere));
            viewHolder.tvLocationName.setSpacing(Constants.TEXT_VIEW_SPACING);
            viewHolder.tvLocationAddress.setVisibility(View.GONE);
        } else if (CommonUtil.isNull(mItems.get(position).getVicinity())) {
            viewHolder.tvLocationName.setText(mContext.getString(R.string.common_show_km));
            viewHolder.tvLocationName.setSpacing(0);
            viewHolder.tvLocationAddress.setVisibility(View.GONE);
        } else {
            float distanceVal = DistanceUtil.getDistance(Double.parseDouble(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LAT)), Double.parseDouble(SPUtil.getSharedPreference(mContext, Constants.SP_USER_LNG)), Double.parseDouble(mItems.get(position).getLat()), Double.parseDouble(mItems.get(position).getLng()));
            String strDistanceVal = String.format("%.1f", distanceVal / 1000);

            viewHolder.tvLocationName.setText(mItems.get(position).getName());
            viewHolder.tvLocationName.setSpacing(0);
            viewHolder.tvLocationAddress.setVisibility(View.VISIBLE);
            viewHolder.tvLocationAddress.setText(strDistanceVal + "km " + mItems.get(position).getVicinity());
        }

        if (position > 1)
            viewHolder.vUnderLine.setBackgroundColor(Color.parseColor("#FFF2F2F2"));
        else
            viewHolder.vUnderLine.setBackgroundColor(Color.parseColor("#4F000000"));

		return convertView;
	}

    public void setLocationCheck(int position) {
        for(LocationItem items : mItems) {
            items.setIsChecked(false);
        }

        mItems.get(position).setIsChecked(true);
        notifyDataSetChanged();
    }
}
