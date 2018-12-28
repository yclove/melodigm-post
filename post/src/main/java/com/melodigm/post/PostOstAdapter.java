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

import com.bumptech.glide.RequestManager;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.PostDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostOstAdapter extends ArrayAdapter<OstDataItem> implements View.OnClickListener {
	private Context mContext;
    WeakRefHandler mHandler;
    RequestManager mGlideRequestManager;
    private LayoutInflater mInflater;
    String mPOST_TYPE;
	private int mLayoutId;
	private List<OstDataItem> mItems;
    String mPOI = "", mOTI = "";
    PostDialog mPostDialog;

	public PostOstAdapter(Context context, int textViewResourceId, ArrayList<OstDataItem> objects, WeakRefHandler handler, RequestManager requestManager) {
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

    public String getPostType() {
        return mPOST_TYPE;
    }

    public void setPostType(String postType) {
        this.mPOST_TYPE = postType;
    }

    /** YCNOTE - ViewHolder
     * 뷰들을 홀더에 꼽아놓듯이 보관하는 객체를 말합니다. 각각의 Row를 그려낼 때 그 안의 위젯들의 속성을 변경하기 위해 findViewById를 호출하는데 이것의 비용이 큰것을 줄이기 위해 사용합니다.
     */
    class ViewHolder {
        public LinearLayout ostItemLayout, ostContLayout;
        public CircularImageView btnOstImage;
        public ImageView btnOstNotify, btnOstDelete, ivOstTitle, btnOstLike, ivOstRepleImage;
        public TextView tvOstRegDate, tvSongName, tvArtiName, tvOstCont, tvOstLikeCount, tvOstRepleCount;
        public RelativeLayout btnOstCngTitle, btnOstReple;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // 캐시된 뷰가 없을 경우 새로 생성하고 뷰홀더를 생성한다.
		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ostItemLayout = (LinearLayout)convertView.findViewById(R.id.ostItemLayout);
            viewHolder.ostContLayout = (LinearLayout)convertView.findViewById(R.id.ostContLayout);
            viewHolder.btnOstNotify = (ImageView)convertView.findViewById(R.id.btnOstNotify);
            viewHolder.btnOstDelete = (ImageView)convertView.findViewById(R.id.btnOstDelete);
            viewHolder.btnOstImage = (CircularImageView)convertView.findViewById(R.id.btnOstImage);
            viewHolder.ivOstTitle = (ImageView)convertView.findViewById(R.id.ivOstTitle);
            viewHolder.btnOstLike = (ImageView)convertView.findViewById(R.id.btnOstLike);
            viewHolder.tvOstRegDate = (TextView)convertView.findViewById(R.id.tvOstRegDate);
            viewHolder.tvSongName = (TextView)convertView.findViewById(R.id.tvSongName);
            viewHolder.tvArtiName = (TextView)convertView.findViewById(R.id.tvArtiName);
            viewHolder.tvOstCont = (TextView)convertView.findViewById(R.id.tvOstCont);
            viewHolder.tvOstLikeCount = (TextView)convertView.findViewById(R.id.tvOstLikeCount);
            viewHolder.btnOstCngTitle = (RelativeLayout)convertView.findViewById(R.id.btnOstCngTitle);
            viewHolder.btnOstReple = (RelativeLayout)convertView.findViewById(R.id.btnOstReple);
            viewHolder.tvOstRepleCount = (TextView)convertView.findViewById(R.id.tvOstRepleCount);
            viewHolder.ivOstRepleImage = (ImageView)convertView.findViewById(R.id.ivOstRepleImage);

            convertView.setTag(viewHolder);
		}
        // 캐시된 뷰가 있을 경우 저장된 뷰홀더를 사용한다
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 아이템 선택 효과
        if (mItems.get(position).isChecked()) {
            viewHolder.ostItemLayout.setBackgroundColor(Color.parseColor("#FFFBF7F2"));
        } else {
            viewHolder.ostItemLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

        // OST 등록일시
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(mItems.get(position).getREG_DATE());
            viewHolder.tvOstRegDate.setText(DateUtil.getDateDisplay(date));
        } catch (ParseException e) {
            viewHolder.tvOstRegDate.setText(mItems.get(position).getREG_DATE());
        }

        // OST 신고 / 삭제 버튼
        String UAI = SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID);
        if (UAI.equals(mItems.get(position).getUAI())) {
            viewHolder.btnOstNotify.setVisibility(View.GONE);
            viewHolder.btnOstDelete.setVisibility(View.VISIBLE);
        } else {
            viewHolder.btnOstNotify.setVisibility(View.VISIBLE);
            viewHolder.btnOstDelete.setVisibility(View.GONE);
        }

        viewHolder.btnOstNotify.setTag(R.id.tag_oti, mItems.get(position).getOTI());
        viewHolder.btnOstNotify.setOnClickListener(this);

        viewHolder.btnOstDelete.setTag(R.id.tag_poi, mItems.get(position).getPOI());
        viewHolder.btnOstDelete.setTag(R.id.tag_oti, mItems.get(position).getOTI());
        viewHolder.btnOstDelete.setOnClickListener(this);

        // OST 타이틀 선정 버튼
        if ("Y".equals(mItems.get(position).getTITL_TOGGLE_YN())) {
            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST))
                viewHolder.ivOstTitle.setImageResource(R.drawable.icon_title_post);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO))
                viewHolder.ivOstTitle.setImageResource(R.drawable.icon_title_radio);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY))
                viewHolder.ivOstTitle.setImageResource(R.drawable.icon_title_today);

            viewHolder.tvSongName.setTextColor(Color.parseColor("#FF00AFD5"));
        } else {
            viewHolder.ivOstTitle.setImageResource(R.drawable.bt_title_chk);
            viewHolder.tvSongName.setTextColor(Color.parseColor("#FF000000"));
        }

        if (UAI.equals(mItems.get(position).getPOST_UAI()) && !UAI.equals(mItems.get(position).getUAI()) && mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
            viewHolder.btnOstCngTitle.setVisibility(View.VISIBLE);
            viewHolder.btnOstCngTitle.setTag(R.id.tag_position, position);
            viewHolder.btnOstCngTitle.setTag(R.id.tag_poi, mItems.get(position).getPOI());
            viewHolder.btnOstCngTitle.setTag(R.id.tag_oti, mItems.get(position).getOTI());
            viewHolder.btnOstCngTitle.setTag(R.id.tag_ssi, mItems.get(position).getSSI());
            viewHolder.btnOstCngTitle.setTag(R.id.tag_titl_toggle_yn, mItems.get(position).getTITL_TOGGLE_YN());
            viewHolder.btnOstCngTitle.setOnClickListener(this);
        } else {
            viewHolder.btnOstCngTitle.setOnClickListener(null);

            if ("Y".equals(mItems.get(position).getTITL_TOGGLE_YN()))
                viewHolder.btnOstCngTitle.setVisibility(View.VISIBLE);
            else
                viewHolder.btnOstCngTitle.setVisibility(View.GONE);
        }

        // OST 이미지
        if (!"".equals(mItems.get(position).getALBUM_PATH())) {
            mGlideRequestManager
                .load(mItems.get(position).getALBUM_PATH())
                .error(R.drawable.icon_album_dummy)
                .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                .into(viewHolder.btnOstImage);

            if (!"FFFFFF".equals(mItems.get(position).getCOLOR_HEX().toUpperCase())) {
                viewHolder.btnOstImage.setBorderColor(Color.parseColor("#FF" + mItems.get(position).getCOLOR_HEX()));
                viewHolder.btnOstImage.setBorderWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics()));
            } else {
                viewHolder.btnOstImage.setBorderColor(android.R.color.transparent);
                viewHolder.btnOstImage.setBorderWidth(0.0f);
            }
        }

        // OST 노래재목 / 가수 / 내용
        viewHolder.tvSongName.setText(mItems.get(position).getSONG_NM());
        viewHolder.tvArtiName.setText(mItems.get(position).getARTI_NM());

        if (CommonUtil.isNull(mItems.get(position).getCONT())) {
            viewHolder.ostContLayout.setVisibility(View.GONE);
            viewHolder.tvOstCont.setText("");
        } else {
            viewHolder.ostContLayout.setVisibility(View.VISIBLE);
            viewHolder.tvOstCont.setText(mItems.get(position).getCONT());
        }

        // OST 좋아요
        if ("Y".equals(mItems.get(position).getLIKE_TOGGLE_YN()))
            viewHolder.btnOstLike.setImageResource(R.drawable.icon_like_rel);
        else
            viewHolder.btnOstLike.setImageResource(R.drawable.icon_ost_likenor);

        if (!UAI.equals(mItems.get(position).getUAI())) {
            viewHolder.btnOstLike.setTag(R.id.tag_position, position);
            viewHolder.btnOstLike.setOnClickListener(this);
        } else {
            viewHolder.btnOstLike.setOnClickListener(null);
        }

        viewHolder.tvOstLikeCount.setText(mItems.get(position).getLIKE_CNT());

        // OST 대댓글
        if ("Y".equals(mItems.get(position).getOST_REPLY_YN())) {
            if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST))
                viewHolder.ivOstRepleImage.setImageResource(R.drawable.icon_ost_rerel);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO))
                viewHolder.ivOstRepleImage.setImageResource(R.drawable.icon_rdo_rerel);
            else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY))
                viewHolder.ivOstRepleImage.setImageResource(R.drawable.icon_tdo_rerel);
        } else
            viewHolder.ivOstRepleImage.setImageResource(R.drawable.icon_ost_renor);

        viewHolder.btnOstReple.setTag(R.id.tag_oti, mItems.get(position).getOTI());
        viewHolder.btnOstReple.setOnClickListener(this);
        viewHolder.tvOstRepleCount.setText(mItems.get(position).getOST_REPLY_CNT());

		return convertView;
	}

    @Override
    public void onClick(View v) {
        Bundle data = new Bundle();
        Message msg = new Message();

        switch (v.getId()) {
            // OST 신고 onClick
            case R.id.btnOstNotify:
                data.putString("OTI", (String) v.getTag(R.id.tag_oti));
                msg.setData(data);
                msg.what = Constants.QUERY_OST_NOTIFY;
                mHandler.sendMessage(msg);
                break;
            // OST 삭제 onClick
            case R.id.btnOstDelete:
                mPOI = (String)v.getTag(R.id.tag_poi);
                mOTI = (String)v.getTag(R.id.tag_oti);
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete));
                mPostDialog.show();
                break;
            // OST 삭제 확인 onClick
            case R.id.btnConfirmConfirm:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                data.putString("POI", mPOI);
                data.putString("OTI", mOTI);
                msg.setData(data);
                msg.what = Constants.QUERY_OST_DELETE;
                mHandler.sendMessage(msg);
                break;
            // OST 타이틀 선정 onClick
            case R.id.btnOstCngTitle:
                data.putInt("POSITION", (int) v.getTag(R.id.tag_position));
                data.putString("POI", (String) v.getTag(R.id.tag_poi));
                data.putString("OTI", (String) v.getTag(R.id.tag_oti));
                data.putString("SSI", (String) v.getTag(R.id.tag_ssi));
                data.putString("TITL_TOGGLE_YN", (String) v.getTag(R.id.tag_titl_toggle_yn));
                msg.setData(data);
                msg.what = Constants.QUERY_OST_TITLE;
                mHandler.sendMessage(msg);
                break;
            // OST 좋아요 onClick
            case R.id.btnOstLike:
                data.putInt("POSITION", (int) v.getTag(R.id.tag_position));
                msg.setData(data);
                msg.what = Constants.QUERY_OST_LIKE;
                mHandler.sendMessage(msg);
                break;
            // OST 대댓글 이동 onClick
            case R.id.btnOstReple:
                data.putString("OTI", (String)v.getTag(R.id.tag_oti));
                msg.setData(data);
                msg.what = Constants.QUERY_OST_REPLE;
                mHandler.sendMessage(msg);
                break;
        }
    }
}
