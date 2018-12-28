package com.melodigm.post.menu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.StampItem;
import com.melodigm.post.widget.LetterSpacingTextView;

import java.util.HashMap;
import java.util.List;

public class StampAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> mListDataHeader;
    private HashMap<String, List<StampItem>> mListDataChild;

    public StampAdapter(Context context, List<String> listDataHeader, HashMap<String, List<StampItem>> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final StampItem item = (StampItem)getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.adapter_menu_stamp_child, null);
        }

        ImageView ivStampType = (ImageView)convertView.findViewById(R.id.ivStampType);
        ImageView ivStampAction = (ImageView)convertView.findViewById(R.id.ivStampAction);
        TextView tvStampAction = (TextView)convertView.findViewById(R.id.tvStampAction);
        TextView tvStampPoint = (TextView)convertView.findViewById(R.id.tvStampPoint);
        ImageView ivStampPoint = (ImageView)convertView.findViewById(R.id.ivStampPoint);
        View vStampChildUnderLine = convertView.findViewById(R.id.vStampChildUnderLine);

        tvStampAction.setText(item.getACT_MSG());
        if (childPosition + 1 == getChildrenCount(groupPosition)) {
            vStampChildUnderLine.setVisibility(View.GONE);
        } else {
            vStampChildUnderLine.setVisibility(View.VISIBLE);
        }

        switch (item.getACT_TYPE()) {
            // 사연 좋아요
            case "BG01":
                ivStampType.setImageResource(R.drawable.icon_tl_post);
                ivStampAction.setImageResource(R.drawable.icon_like_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 사연 OST 좋아요
            case "BG02":
                ivStampType.setImageResource(R.drawable.icon_tl_ostp);
                ivStampAction.setImageResource(R.drawable.icon_like_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 사연 타이틀 선정
            case "BG03":
                ivStampType.setImageResource(R.drawable.icon_tltitle_post);
                ivStampAction.setImageResource(R.drawable.icon_ost_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 라디오 좋아요
            case "BG04":
                ivStampType.setImageResource(R.drawable.icon_tl_rdo);
                ivStampAction.setImageResource(R.drawable.icon_like_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 라디오 OST 좋아요
            case "BG05":
                ivStampType.setImageResource(R.drawable.icon_tl_ostr);
                ivStampAction.setImageResource(R.drawable.icon_like_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 투데이 OST 좋아요
            case "BG06":
                ivStampType.setImageResource(R.drawable.icon_tl_ostt);
                ivStampAction.setImageResource(R.drawable.icon_like_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 투데이 타이틀 선정
            case "BG07":
                ivStampType.setImageResource(R.drawable.icon_title_today);
                ivStampAction.setImageResource(R.drawable.icon_tod_rel);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // GIFT / 나의 첫 이야기
            case "BG08":
            case "BG09":
                ivStampType.setImageResource(R.drawable.icon_post_gift);
                ivStampAction.setImageResource(R.drawable.icon_card_r);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_plus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
            // 라디오사연포스팅
            case "BH01":
                ivStampType.setImageResource(R.drawable.icon_tl_rdo);
                ivStampAction.setImageResource(R.drawable.icon_rabullet);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            // 사연 3회 신고
            case "BH02":
                ivStampType.setImageResource(R.drawable.icon_tl_post);
                ivStampAction.setImageResource(R.drawable.bt_del);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            // 사연 OST 3회 신고
            case "BH03":
                ivStampType.setImageResource(R.drawable.icon_tl_ostp);
                ivStampAction.setImageResource(R.drawable.bt_del);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            // 라디오 3회 신고
            case "BH04":
                ivStampType.setImageResource(R.drawable.icon_tl_rdo);
                ivStampAction.setImageResource(R.drawable.bt_del);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            // 라디오 OST 3회 신고
            case "BH05":
                ivStampType.setImageResource(R.drawable.icon_tl_ostr);
                ivStampAction.setImageResource(R.drawable.bt_del);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            // 투데이 OST 3회 신고
            case "BH06":
                ivStampType.setImageResource(R.drawable.icon_tl_ostt);
                ivStampAction.setImageResource(R.drawable.bt_del);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            // 이용권
            case "BH07":
                ivStampType.setImageResource(R.drawable.icon_post_pass);
                ivStampAction.setImageResource(R.drawable.icon_card_r);
                ivStampPoint.setImageResource(R.drawable.icon_stamp_minus);
                tvStampPoint.setText(String.valueOf(item.getPOINT_VAR_NUM()));
                tvStampPoint.setTextColor(Color.parseColor("#FF00AFD5"));
                break;
            default:
                ivStampType.setImageResource(R.color.transparent);
                ivStampAction.setImageResource(R.color.transparent);
                ivStampPoint.setImageResource(R.color.transparent);
                tvStampPoint.setText("0");
                tvStampPoint.setTextColor(Color.parseColor("#FFD73D66"));
                break;
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
        String headerTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.adapter_menu_stamp_group, null);
        }

        LetterSpacingTextView lstvGroupHeader = (LetterSpacingTextView)convertView.findViewById(R.id.lstvGroupHeader);
        lstvGroupHeader.setSpacing(Constants.TEXT_VIEW_SPACING);
        lstvGroupHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
