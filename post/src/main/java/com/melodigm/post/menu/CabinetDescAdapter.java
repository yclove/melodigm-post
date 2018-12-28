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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.CabinetMusicDataItem;
import com.melodigm.post.widget.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class CabinetDescAdapter extends ArrayAdapter<CabinetMusicDataItem> {
    private static final int DISPLAY_MODE_LIST = 1;
    private static final int DISPLAY_MODE_EDIT = 2;

    private int mDiaplayType = DISPLAY_MODE_LIST;
	private Context mContext;
    private LayoutInflater mInflater;
    private RequestManager mGlideRequestManager;
	private int mLayoutId;
	private List<CabinetMusicDataItem> mItems;

	public CabinetDescAdapter(Context context, int textViewResourceId, ArrayList<CabinetMusicDataItem> objects, RequestManager requestManager) {
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
	public CabinetMusicDataItem getItem(int position) {
		return mItems.get(position);
	}

    public void setDisplayType(int displayType) {
        this.mDiaplayType = displayType;
    }

    /** YCNOTE - ViewHolder
     * 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말합니다. 각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데 이것의 비용이 큰것을 줄이기 위해 사용합니다.
     */
    class ViewHolder {
        LinearLayout cabinetItemLayout;
        CircularImageView btnOstImage;
        ImageView ivPostType;
        TextView tvOstSongName;
        TextView tvOstArtiName;
        TextView tvCabinetMusicNumber;
        RelativeLayout rlCabinetItemDescBtn, drag_handle;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // 캐시된 뷰가 없을 경우 새로 생성하고 뷰홀더를 생성한다.
		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.cabinetItemLayout = (LinearLayout)convertView.findViewById(R.id.cabinetItemLayout);
            viewHolder.btnOstImage = (CircularImageView)convertView.findViewById(R.id.btnOstImage);
            viewHolder.ivPostType = (ImageView)convertView.findViewById(R.id.ivPostType);
            viewHolder.tvOstSongName = (TextView)convertView.findViewById(R.id.tvOstSongName);
            viewHolder.tvOstArtiName = (TextView)convertView.findViewById(R.id.tvOstArtiName);
            viewHolder.tvCabinetMusicNumber = (TextView)convertView.findViewById(R.id.tvCabinetMusicNumber);
            viewHolder.rlCabinetItemDescBtn = (RelativeLayout)convertView.findViewById(R.id.rlCabinetItemDescBtn);
            viewHolder.drag_handle = (RelativeLayout)convertView.findViewById(R.id.drag_handle);

            convertView.setTag(viewHolder);
		}
        // 캐시된 뷰가 있을 경우 저장된 뷰홀더를 사용한다
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.cabinetItemLayout.setBackgroundColor(Color.parseColor("#FFFBF7F2"));
        } else {
            viewHolder.cabinetItemLayout.setBackgroundColor(Color.parseColor("#00000000"));
        }

        // 순번
        viewHolder.tvCabinetMusicNumber.setText(String.valueOf(position + 1));

        // 노래제목 / 가수명
        viewHolder.tvOstSongName.setText(mItems.get(position).getSONG_NM());
        viewHolder.tvOstArtiName.setText(mItems.get(position).getARTI_NM());

        if (!"".equals(mItems.get(position).getALBUM_PATH())) {
            mGlideRequestManager
                .load(mItems.get(position).getALBUM_PATH())
                .error(R.drawable.icon_album_dummy)
                .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                .into(viewHolder.btnOstImage);
        }

        viewHolder.ivPostType.setImageResource(android.R.color.transparent);
        if (mItems.get(position).getPOST_TYPE().equals(Constants.REQUEST_TYPE_POST)) {
            if ("Y".equals(mItems.get(position).getTITL_TOGGLE_YN()))
                viewHolder.ivPostType.setImageResource(R.drawable.icon_tl_post);
            else
                viewHolder.ivPostType.setImageResource(R.drawable.icon_tl_ostp);
        } else if (mItems.get(position).getPOST_TYPE().equals(Constants.REQUEST_TYPE_RADIO)) {
            if ("Y".equals(mItems.get(position).getTITL_TOGGLE_YN()))
                viewHolder.ivPostType.setImageResource(R.drawable.icon_tl_rdo);
            else
                viewHolder.ivPostType.setImageResource(R.drawable.icon_tl_ostr);
        } else if (mItems.get(position).getPOST_TYPE().equals(Constants.REQUEST_TYPE_TODAY)) {
            if ("Y".equals(mItems.get(position).getTITL_TOGGLE_YN()))
                viewHolder.ivPostType.setImageResource(R.drawable.icon_tl_tod);
            else
                viewHolder.ivPostType.setImageResource(R.drawable.icon_tl_ostt);
        }

        if (mDiaplayType == DISPLAY_MODE_LIST) {
            viewHolder.rlCabinetItemDescBtn.setVisibility(View.VISIBLE);
            viewHolder.drag_handle.setVisibility(View.GONE);
        } else {
            viewHolder.rlCabinetItemDescBtn.setVisibility(View.GONE);
            viewHolder.drag_handle.setVisibility(View.VISIBLE);
        }

		return convertView;
	}
}
