package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.PlayerConstants;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.PostDialog;

import java.util.ArrayList;

public class PlayerAdapter extends ArrayAdapter<OstDataItem> implements View.OnClickListener {
    private static final int PLAYER_DISPLAY_MODE_STORY = 1;
    private static final int PLAYER_DISPLAY_MODE_LIST = 2;
    private static final int PLAYER_DISPLAY_MODE_EDIT = 3;

    private int mDiaplayType = 2;
	private Context mContext;
    private LayoutInflater mInflater;
    private RequestManager mGlideRequestManager;
	private int mLayoutId;
	private ArrayList<OstDataItem> mItems;
    private PostDatabases mdbHelper;
    private PostDialog mPostDialog;
    private WeakRefHandler mHandler;
    private String mPOI = "", mSSI = "";
    private int deleteLayoutAnimationPosition = -1;
    private boolean deleteLayoutShow = false;

    public PlayerAdapter(Context context, int textViewResourceId, WeakRefHandler handler, ArrayList<OstDataItem> objects, RequestManager requestManager) {
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

    public ArrayList<OstDataItem> getAllItems() {
        return mItems;
    }

    public void addAllItems(ArrayList<OstDataItem> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    public void setDeleteLayoutAnimationPosition(int position, boolean show) {
        this.deleteLayoutAnimationPosition = position;
        this.deleteLayoutShow = show;
    }

    public void setDisplayType(int displayType) {
        this.mDiaplayType = displayType;
    }

    public class ViewHolder {
        LinearLayout searchOstItemLayout;
        CircularImageView btnOstImage;
        ImageView ivPostType;
        TextView tvOstSongName;
        TextView tvOstArtiName;
        TextView tvPlayerNumber;
        RelativeLayout rlPlayerState, rlPlayerItemDescBtn, rlPlayerItemDeleteBtn, drag_handle;
        ImageView ivPlayerStateImage;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.searchOstItemLayout = (LinearLayout)convertView.findViewById(R.id.searchOstItemLayout);
            viewHolder.btnOstImage = (CircularImageView)convertView.findViewById(R.id.btnOstImage);
            viewHolder.ivPostType = (ImageView)convertView.findViewById(R.id.ivPostType);
            viewHolder.tvOstSongName = (TextView)convertView.findViewById(R.id.tvOstSongName);
            viewHolder.tvOstArtiName = (TextView)convertView.findViewById(R.id.tvOstArtiName);
            viewHolder.tvPlayerNumber = (TextView)convertView.findViewById(R.id.tvPlayerNumber);
            viewHolder.rlPlayerState = (RelativeLayout)convertView.findViewById(R.id.rlPlayerState);
            viewHolder.ivPlayerStateImage = (ImageView)convertView.findViewById(R.id.ivPlayerStateImage);
            viewHolder.rlPlayerItemDescBtn = (RelativeLayout)convertView.findViewById(R.id.rlPlayerItemDescBtn);
            viewHolder.rlPlayerItemDeleteBtn = (RelativeLayout)convertView.findViewById(R.id.rlPlayerItemDeleteBtn);
            viewHolder.drag_handle = (RelativeLayout)convertView.findViewById(R.id.drag_handle);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        } else {
            viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#00000000"));
        }

        // 삭제 버튼 애니메이션 효과
        Animation visibleAnimation = new AlphaAnimation(0.0f, 1.0f);
        visibleAnimation.setDuration(1000);

        if (deleteLayoutAnimationPosition == position) {
            if (deleteLayoutShow) {
                viewHolder.rlPlayerItemDescBtn.setVisibility(View.GONE);
                viewHolder.drag_handle.setVisibility(View.GONE);
                viewHolder.rlPlayerItemDeleteBtn.setVisibility(View.VISIBLE);
                viewHolder.rlPlayerItemDeleteBtn.setAnimation(visibleAnimation);
            } else {
                viewHolder.rlPlayerItemDeleteBtn.setVisibility(View.GONE);

                if (mDiaplayType == PLAYER_DISPLAY_MODE_LIST) {
                    viewHolder.rlPlayerItemDescBtn.setVisibility(View.VISIBLE);
                    viewHolder.drag_handle.setVisibility(View.GONE);
                } else {
                    viewHolder.rlPlayerItemDescBtn.setVisibility(View.GONE);
                    viewHolder.drag_handle.setVisibility(View.VISIBLE);
                }
            }
        } else {
            viewHolder.rlPlayerItemDeleteBtn.setVisibility(View.GONE);

            if (mDiaplayType == PLAYER_DISPLAY_MODE_LIST) {
                viewHolder.rlPlayerItemDescBtn.setVisibility(View.VISIBLE);
                viewHolder.drag_handle.setVisibility(View.GONE);
            } else {
                viewHolder.rlPlayerItemDescBtn.setVisibility(View.GONE);
                viewHolder.drag_handle.setVisibility(View.VISIBLE);
            }
        }

        // 순번
        viewHolder.tvPlayerNumber.setText(String.valueOf(position + 1));

        // 노래제목 / 가수명
        viewHolder.tvOstSongName.setText(mItems.get(position).getSONG_NM());
        viewHolder.tvOstArtiName.setText(mItems.get(position).getARTI_NM());

        // 재생곡이 아닐 경우
        if (PlayerConstants.SONG_NUMBER != position) {
            if (mDiaplayType == PLAYER_DISPLAY_MODE_LIST)
                viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#00000000"));
            viewHolder.tvOstSongName.setTextColor(Color.parseColor("#FFFFFFFF"));
            viewHolder.tvPlayerNumber.setVisibility(View.VISIBLE);
            viewHolder.rlPlayerState.setVisibility(View.GONE);
        }
        // 재생중일 경우
        else if (PlayerConstants.SONG_NUMBER == position && !PlayerConstants.SONG_PAUSED) {
            if (mDiaplayType == PLAYER_DISPLAY_MODE_LIST)
                viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
            viewHolder.tvOstSongName.setTextColor(Color.parseColor("#FF00AFD5"));
            viewHolder.tvPlayerNumber.setVisibility(View.GONE);
            viewHolder.rlPlayerState.setVisibility(View.VISIBLE);
            viewHolder.ivPlayerStateImage.setImageResource(R.drawable.icon_plist_pause);
        }
        // 멈춤중일 경우
        else if (PlayerConstants.SONG_NUMBER == position && PlayerConstants.SONG_PAUSED) {
            if (mDiaplayType == PLAYER_DISPLAY_MODE_LIST)
                viewHolder.searchOstItemLayout.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
            viewHolder.tvOstSongName.setTextColor(Color.parseColor("#FF00AFD5"));
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
