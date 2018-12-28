package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
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
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.swipelayout.SwipeRevealLayout;

import java.util.ArrayList;

public class PlayerAdapterSlide extends ArrayAdapter<OstDataItem> implements View.OnClickListener {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
    private RequestManager mGlideRequestManager;
	private ArrayList<OstDataItem> mItems;
    private PostDatabases mdbHelper;
    private PostDialog mPostDialog;
    private WeakRefHandler mHandler;
    private String mPOI = "", mSSI = "";

	public PlayerAdapterSlide(Context context, int textViewResourceId, WeakRefHandler handler, ArrayList<OstDataItem> objects, RequestManager requestManager) {
		super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mLayoutId = textViewResourceId;
        this.mItems = objects;
        this.mHandler = handler;
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
        LinearLayout searchOstItemLayout;
        ImageView btnOstImage, ivPostType;
        TextView tvOstSongName;
        TextView tvOstArtiName;
        TextView tvPlayerNumber;
        RelativeLayout rlPlayerState, rlPlayerItemDeleteBtn;
        ImageView ivPlayerStateImage;
        SwipeRevealLayout swipe_layout;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.searchOstItemLayout = (LinearLayout)convertView.findViewById(R.id.searchOstItemLayout);
            viewHolder.btnOstImage = (ImageView)convertView.findViewById(R.id.btnOstImage);
            viewHolder.ivPostType = (ImageView)convertView.findViewById(R.id.ivPostType);
            viewHolder.tvOstSongName = (TextView)convertView.findViewById(R.id.tvOstSongName);
            viewHolder.tvOstArtiName = (TextView)convertView.findViewById(R.id.tvOstArtiName);
            viewHolder.tvPlayerNumber = (TextView)convertView.findViewById(R.id.tvPlayerNumber);
            viewHolder.rlPlayerState = (RelativeLayout)convertView.findViewById(R.id.rlPlayerState);
            viewHolder.ivPlayerStateImage = (ImageView)convertView.findViewById(R.id.ivPlayerStateImage);
            viewHolder.rlPlayerItemDeleteBtn = (RelativeLayout)convertView.findViewById(R.id.rlPlayerItemDeleteBtn);
            viewHolder.swipe_layout = (SwipeRevealLayout)convertView.findViewById(R.id.swipe_layout);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.swipe_layout.close(false);

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        } else {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#00000000"));
        }

        // 순번
        viewHolder.tvPlayerNumber.setText(String.valueOf(position + 1));

        // 재생곡이 아닐 경우
        if (PlayerConstants.SONG_NUMBER != position) {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#00000000"));
            viewHolder.tvPlayerNumber.setVisibility(View.VISIBLE);
            viewHolder.rlPlayerState.setVisibility(View.GONE);
        }
        // 재생중일 경우
        else if (PlayerConstants.SONG_NUMBER == position && !PlayerConstants.SONG_PAUSED) {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
            viewHolder.tvPlayerNumber.setVisibility(View.GONE);
            viewHolder.rlPlayerState.setVisibility(View.VISIBLE);
            viewHolder.ivPlayerStateImage.setImageResource(R.drawable.icon_plist_pause);
        }
        // 멈춤중일 경우
        else if (PlayerConstants.SONG_NUMBER == position && PlayerConstants.SONG_PAUSED) {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
            viewHolder.tvPlayerNumber.setVisibility(View.GONE);
            viewHolder.rlPlayerState.setVisibility(View.VISIBLE);
            viewHolder.ivPlayerStateImage.setImageResource(R.drawable.icon_plist_play);
        }

        if (!"".equals(mItems.get(position).getALBUM_PATH())) {
            mGlideRequestManager
                .load(mItems.get(position).getALBUM_PATH())
                .error(R.drawable.icon_album_dummy)
                .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                .into(viewHolder.btnOstImage);
        }

        viewHolder.tvOstSongName.setText(mItems.get(position).getSONG_NM());
        viewHolder.tvOstArtiName.setText(mItems.get(position).getARTI_NM());

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

        // 삭제 버튼
        viewHolder.rlPlayerItemDeleteBtn.setTag(R.id.tag_poi, mItems.get(position).getPOI());
        viewHolder.rlPlayerItemDeleteBtn.setTag(R.id.tag_ssi, mItems.get(position).getSSI());
        viewHolder.rlPlayerItemDeleteBtn.setOnClickListener(this);

		return convertView;
    }

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            // 삭제 onClick
            case R.id.rlPlayerItemDeleteBtn:
                mPOI = (String)v.getTag(R.id.tag_poi);
                mSSI = (String)v.getTag(R.id.tag_ssi);
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete));
                mPostDialog.show();
                break;
            // 삭제 확인 onClick
            case R.id.btnConfirmConfirm:
                if(mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss();

                String POI_SSI[] = new String[2];
                POI_SSI[0] = mPOI;
                POI_SSI[1] = mSSI;
                mdbHelper = new PostDatabases(mContext);
                mdbHelper.open();
                int updateCount = mdbHelper.deleteOstPlayList(POI_SSI);
                PlayerConstants.SONGS_LIST = mdbHelper.getOstPlayList();
                mdbHelper.close();

                data.putInt("UPDATE_COUNT", updateCount);
                msg.setData(data);
                msg.what = Constants.QUERY_PLAY_LIST_DELETE;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
