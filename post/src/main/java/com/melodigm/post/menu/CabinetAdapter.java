package com.melodigm.post.menu;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.CabinetDataItem;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class CabinetAdapter extends ArrayAdapter<CabinetDataItem> implements View.OnClickListener {
	private Context mContext;
    private LayoutInflater mInflater;
    private RequestManager mGlideRequestManager;
	private int mLayoutId;
	private List<CabinetDataItem> mItems;
    private String mCabinetType;
    private WeakRefHandler mHandler;

	public CabinetAdapter(Context context, int textViewResourceId, ArrayList<CabinetDataItem> objects, String cabinetType, WeakRefHandler handler, RequestManager requestManager) {
		super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mLayoutId = textViewResourceId;
        this.mItems = objects;
        this.mCabinetType = cabinetType;
        this.mHandler = handler;
        this.mGlideRequestManager = requestManager;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}
	
	@Override
	public CabinetDataItem getItem(int position) {
		return mItems.get(position);
	}

    /** YCNOTE - ViewHolder
     * 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말합니다. 각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데 이것의 비용이 큰것을 줄이기 위해 사용합니다.
     */
    class ViewHolder {
        RelativeLayout rlCabinetRootLayout, rlPlayMusicBtn;
        TextView tvCabinetName, tvCabinetMusicCount, tvCabinetMusicCountAdd;
        CircularImageView ivCabinetImage;
        LinearLayout llCabinetAddLayout;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // 캐시된 뷰가 없을 경우 새로 생성하고 뷰홀더를 생성한다.
		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.rlCabinetRootLayout = (RelativeLayout)convertView.findViewById(R.id.rlCabinetRootLayout);
            viewHolder.ivCabinetImage = (CircularImageView)convertView.findViewById(R.id.ivCabinetImage);
            viewHolder.tvCabinetName = (TextView)convertView.findViewById(R.id.tvCabinetName);
            viewHolder.tvCabinetMusicCount = (TextView)convertView.findViewById(R.id.tvCabinetMusicCount);
            viewHolder.tvCabinetMusicCountAdd = (TextView)convertView.findViewById(R.id.tvCabinetMusicCountAdd);
            viewHolder.rlPlayMusicBtn = (RelativeLayout)convertView.findViewById(R.id.rlPlayMusicBtn);
            viewHolder.llCabinetAddLayout = (LinearLayout)convertView.findViewById(R.id.llCabinetAddLayout);

            convertView.setTag(viewHolder);
		}
        // 캐시된 뷰가 있을 경우 저장된 뷰홀더를 사용한다
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.rlCabinetRootLayout.setBackgroundColor(Color.parseColor("#FFFBF7F2"));
        } else {
            viewHolder.rlCabinetRootLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

        if (!"".equals(mItems.get(position).getUBX_IMG())) {
            mGlideRequestManager
                .load(mItems.get(position).getUBX_IMG())
                .error(R.drawable.icon_album_dummy)
                .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                .into(viewHolder.ivCabinetImage);
        }

        viewHolder.tvCabinetName.setText(mItems.get(position).getUBX_NM());
        viewHolder.tvCabinetMusicCount.setText(mContext.getString(R.string.n_music_count, mItems.get(position).getMUS_CNT()));
        viewHolder.tvCabinetMusicCountAdd.setText(mContext.getString(R.string.n_music_count, mItems.get(position).getMUS_CNT()));

        // 보관함 타입에 따른 화면 구성
        if (Constants.REQUEST_CABINET_TYPE_PLAY.equals(mCabinetType)) {
            viewHolder.tvCabinetMusicCount.setVisibility(View.GONE);
            viewHolder.llCabinetAddLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvCabinetMusicCount.setVisibility(View.VISIBLE);
            viewHolder.llCabinetAddLayout.setVisibility(View.GONE);
        }

        viewHolder.llCabinetAddLayout.setTag(R.id.tag_position, position);
        viewHolder.llCabinetAddLayout.setTag(R.id.tag_count, mItems.get(position).getMUS_CNT());
        viewHolder.llCabinetAddLayout.setOnClickListener(this);

		return convertView;
	}

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            case R.id.llCabinetAddLayout:
                data.putInt("POSITION", (int)v.getTag(R.id.tag_position));
                data.putInt("COUNT", (int)v.getTag(R.id.tag_count));
                msg.setData(data);
                msg.what = Constants.QUERY_CABINET_PLAY;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
