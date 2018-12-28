package com.melodigm.post.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.protocol.data.NotificationCenterItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationCenterAdapter extends ArrayAdapter<NotificationCenterItem> {
	private Context mContext;
    private LayoutInflater mInflater;
	private int mLayoutId;
	private List<NotificationCenterItem> mItems;

	public NotificationCenterAdapter(Context context, int textViewResourceId, ArrayList<NotificationCenterItem> objects) {
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
	public NotificationCenterItem getItem(int position) {
		return mItems.get(position);
	}

    class ViewHolder {
        TextView tvNotificationCenterMessage;
        ImageView ivNotificationCenterGroupImage, ivNotificationCenterType;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

		if(convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvNotificationCenterMessage = (TextView)convertView.findViewById(R.id.tvNotificationCenterMessage);
            viewHolder.ivNotificationCenterGroupImage = (ImageView)convertView.findViewById(R.id.ivNotificationCenterGroupImage);
            viewHolder.ivNotificationCenterType = (ImageView)convertView.findViewById(R.id.ivNotificationCenterType);

            convertView.setTag(viewHolder);
		} else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 알림센터 그룹 및 타입별 아이콘
        viewHolder.ivNotificationCenterGroupImage.setImageResource(android.R.color.transparent);
        viewHolder.ivNotificationCenterType.setImageResource(android.R.color.transparent);
        switch (mItems.get(position).getNOTI_TYPE()) {
            // 내사연LIKE
            case "AF01":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_post);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_like_rel);
                break;
            // 내사연신고처리
            case "AF02":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_post);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.bt_del);
                break;
            // 내사연OST등록
            case "AF03":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_post);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_ost_rel);
                break;
            // 사연내OSTLIKE
            case "AF04":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostp);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_like_rel);
                break;
            // 사연내OST신고처리
            case "AF05":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostp);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.bt_del);
                break;
            // 사연내OST타이틀곡선정
            case "AF06":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tltitle_post);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_ost_rel);
                break;
            // 사연내OST댓글
            case "AF07":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostp);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_ost_rerel);
                break;
            // 내라디오LIKE
            case "AF08":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_rdo);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_like_rel);
                break;
            // 내라디오신고처리
            case "AF09":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_rdo);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.bt_del);
                break;
            // 내라디오OST등록
            case "AF10":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_rdo);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_rdo_rel);
                break;
            // 라디오내OSTLIKE
            case "AF11":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostr);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_like_rel);
                break;
            // 라디오내OST신고처리
            case "AF12":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostr);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.bt_del);
                break;
            // 라디오내OST댓글
            case "AF13":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostr);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_rdo_rerel);
                break;
            // 투데이내OSTLIKE
            case "AF14":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostt);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_like_rel);
                break;
            // 투데이내OST신고처리
            case "AF15":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostt);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.bt_del);
                break;
            // 투데이내OST댓글
            case "AF16":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostt);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_tdo_rerel);
                break;
            // 5곡재생가능알림
            case "AF17":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_post_pass);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_card_r);
                break;
            // 만료일주일전알림
            case "AF18":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_post_pass);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_card_r);
                break;
            // 기간만료알림
            case "AF19":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_post_pass);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_card_r);
                break;
            // 소진만료알림
            case "AF20":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_post_pass);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_card_r);
                break;
            // 사연내OST사연자감상
            case "AF21":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostp);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_plist_play);
                break;
            // 라디오내OST사연자
            case "AF22":
                viewHolder.ivNotificationCenterGroupImage.setImageResource(R.drawable.icon_tl_ostr);
                viewHolder.ivNotificationCenterType.setImageResource(R.drawable.icon_rlist_play);
                break;
        }

        // 타임라인 내용
        viewHolder.tvNotificationCenterMessage.setText(mItems.get(position).getDESCR());

		return convertView;
	}
}
