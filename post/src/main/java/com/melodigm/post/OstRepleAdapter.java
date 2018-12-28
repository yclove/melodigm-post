package com.melodigm.post;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.OstRepleItem;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.swipelayout.SwipeRevealLayout;
import com.melodigm.post.widget.swipelayout.ViewBinderHelper;

import java.util.ArrayList;
import java.util.List;

public class OstRepleAdapter extends ArrayAdapter<OstRepleItem> implements View.OnClickListener {
	private Context mContext;
    WeakRefHandler mHandler;
    private LayoutInflater mInflater;
	private int mLayoutId;
	private List<OstRepleItem> mItems;
    String mORI;
    PostDialog mPostDialog;
    private final ViewBinderHelper binderHelper;

    public OstRepleAdapter(Context context, int textViewResourceId, ArrayList<OstRepleItem> objects, WeakRefHandler handler) {
		super(context, textViewResourceId, objects);
		mContext = context;
		mLayoutId = textViewResourceId;
		mItems = objects;
        this.mHandler = handler;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binderHelper = new ViewBinderHelper();
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}
	
	@Override
	public OstRepleItem getItem(int position) {
		return mItems.get(position);
	}

    class ViewHolder {
        ImageView ivOstRepleBullet, ivOstRepleWriterBullet;
        TextView tvOstRepleCont;
        RelativeLayout btnOstRepleDel;
        View deleteView;
        SwipeRevealLayout swipeLayout;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivOstRepleBullet = (ImageView)convertView.findViewById(R.id.ivOstRepleBullet);
            viewHolder.ivOstRepleWriterBullet = (ImageView)convertView.findViewById(R.id.ivOstRepleWriterBullet);
            viewHolder.tvOstRepleCont = (TextView)convertView.findViewById(R.id.tvOstRepleCont);
            viewHolder.btnOstRepleDel = (RelativeLayout)convertView.findViewById(R.id.btnOstRepleDel);
            viewHolder.deleteView = convertView.findViewById(R.id.delete_layout);
            viewHolder.swipeLayout = (SwipeRevealLayout) convertView.findViewById(R.id.swipe_layout);

            convertView.setTag(viewHolder);
		}
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // OST Reple Bullet 아이콘 / 삭제 버튼
        // 본인 등록자
        if ("BC02".equals(mItems.get(position).getOST_REPLY_TYPE())) {
            viewHolder.ivOstRepleWriterBullet.setVisibility(View.GONE);
            viewHolder.ivOstRepleBullet.setVisibility(View.VISIBLE);
            viewHolder.ivOstRepleBullet.setBackground(new ColorCircleDrawable(Color.parseColor("#FF00AFD5")));

            viewHolder.swipeLayout.setLockDrag(false);
            viewHolder.btnOstRepleDel.setVisibility(View.VISIBLE);
            viewHolder.btnOstRepleDel.setTag(R.id.tag_ori, mItems.get(position).getORI());
            viewHolder.btnOstRepleDel.setOnClickListener(this);
        }
        // 사연 등록자
        else if ("BC03".equals(mItems.get(position).getOST_REPLY_TYPE())) {
            viewHolder.ivOstRepleBullet.setVisibility(View.GONE);
            viewHolder.ivOstRepleWriterBullet.setVisibility(View.VISIBLE);

            viewHolder.swipeLayout.setLockDrag(true);
            viewHolder.btnOstRepleDel.setVisibility(View.GONE);
            viewHolder.btnOstRepleDel.setOnClickListener(null);
        }
        // OST 등록자
        else if ("BC04".equals(mItems.get(position).getOST_REPLY_TYPE())) {
            viewHolder.ivOstRepleWriterBullet.setVisibility(View.GONE);
            viewHolder.ivOstRepleBullet.setVisibility(View.VISIBLE);
            viewHolder.ivOstRepleBullet.setBackground(new ColorCircleDrawable(Color.parseColor("#FFD73D66")));

            viewHolder.swipeLayout.setLockDrag(true);
            viewHolder.btnOstRepleDel.setVisibility(View.GONE);
            viewHolder.btnOstRepleDel.setOnClickListener(null);
        }
        // 타인 등록자
        else {
            viewHolder.ivOstRepleWriterBullet.setVisibility(View.GONE);
            viewHolder.ivOstRepleBullet.setVisibility(View.GONE);

            viewHolder.swipeLayout.setLockDrag(true);
            viewHolder.btnOstRepleDel.setVisibility(View.GONE);
            viewHolder.btnOstRepleDel.setOnClickListener(null);
        }

        // OST Reple 내용
        viewHolder.tvOstRepleCont.setText(mItems.get(position).getCONT());

		return convertView;
	}

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            // OST Reple 삭제 onClick
            case R.id.btnOstRepleDel:
                mORI = (String)v.getTag(R.id.tag_ori);
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete));
                mPostDialog.show();
                break;
            // OST Reple 삭제 확인 onClick
            case R.id.btnConfirmConfirm:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                data.putString("ORI", mORI);
                msg.setData(data);
                msg.what = Constants.QUERY_OST_REPLE_DELETE;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
