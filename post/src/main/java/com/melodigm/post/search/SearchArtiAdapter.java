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
import com.melodigm.post.protocol.data.OstDataItem;

import java.util.ArrayList;

public class SearchArtiAdapter extends ArrayAdapter<OstDataItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
	private ArrayList<OstDataItem> mItems;

	public SearchArtiAdapter(Context context, int textViewResourceId, ArrayList<OstDataItem> objects) {
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
	public OstDataItem getItem(int position) {
		return mItems.get(position);
	}

    public void addAllItems(ArrayList<OstDataItem> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    public class ViewHolder {
        RelativeLayout rlArtiItemLayout;
        TextView tvArtiName, tvArtiInfo;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.rlArtiItemLayout = (RelativeLayout)convertView.findViewById(R.id.rlArtiItemLayout);
            viewHolder.tvArtiName = (TextView)convertView.findViewById(R.id.tvArtiName);
            viewHolder.tvArtiInfo = (TextView)convertView.findViewById(R.id.tvArtiInfo);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.rlArtiItemLayout.setBackgroundColor(Color.parseColor("#FFFBF7F2"));
        } else {
            viewHolder.rlArtiItemLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

        // 가수 명 / 가수 정보
        viewHolder.tvArtiName.setText(mItems.get(position).getARTI_NM());
        viewHolder.tvArtiInfo.setText(mItems.get(position).getSONG_NM());

		return convertView;
    }
}
