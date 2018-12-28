package com.melodigm.post.search;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.protocol.data.OstRelatedItem;
import com.melodigm.post.util.CommonUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RelatedAdapter extends BaseAdapter {
	protected Context mContext = null;
	protected ArrayList<OstRelatedItem> mItems = null; //연관 검색어 목록!!(자동완성)
	protected OnListItemClickListener m_ItemClickListener = null;
    private String mWord = "";

	public RelatedAdapter(Context ctx) {
        mContext = ctx;
		mItems = new ArrayList<>();
	}

	public RelatedAdapter(Context ctx, ArrayList<OstRelatedItem> items) {
        mContext = ctx;
		mItems = items;
	}

    public void setWord(String word) {
        mWord = word;
    }

	public void addAllItems(ArrayList<OstRelatedItem> data){
		mItems = data;
	}
	
	public void deleteAllItems() {
		mItems.clear();
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public OstRelatedItem getItem(int position) {
		try {
			return mItems.get(position);
		}catch (Exception ex){
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_search_related_list_item, parent, false);
		}
        OstRelatedItem item = getItem(position);
		if(item == null){
			return convertView;
		}

        TextView search_related_word = (TextView)convertView.findViewById(R.id.search_related_word);
        TextView search_related_type = (TextView)convertView.findViewById(R.id.search_related_type);

		// 검색어 색상변경 처리
		ArrayList<int[]> spans = new ArrayList<>();
        final Pattern p = Pattern.compile(mWord.toLowerCase());
        final Matcher matcher = p.matcher(item.getKEYWORD().toLowerCase());
        final SpannableStringBuilder spannable = new SpannableStringBuilder(item.getKEYWORD());

		while (matcher.find()) {
			int[] currentSpan = new int[2];
			currentSpan[0] = matcher.start();
			currentSpan[1] = matcher.end();
			spans.add(currentSpan);
		}

		/**
		 * YCNOTE - Spannable > setSpan flags
		 * SPAN_앞쪽속성유지여부_뒤쪽속성유지여부
		 * EXCLUSIVE 확장 X  / INCLUSIVE 확장 O
		 */
		for (int[] items : spans) {
			if (items[0] >= 0 && items[1] >= 0)
				spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF00AFD5")), items[0], items[1], Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}

        search_related_word.setText(spannable);

        // OST 음원 자동완성  검색
        if (CommonUtil.isNull(item.getTYPE())) {
            search_related_type.setVisibility(View.GONE);
        }
        // 노래 (앨범/음원/아티스트) 자동완성 검색
        else {
            search_related_type.setVisibility(View.VISIBLE);
            String typeKr = "";
            if ("SONG".equalsIgnoreCase(item.getTYPE()))
                typeKr = "곡";
            else if ("ALBUM".equalsIgnoreCase(item.getTYPE()))
                typeKr = "앨범";
            else if ("ARTI".equalsIgnoreCase(item.getTYPE()))
                typeKr = "아티스트";
            search_related_type.setText(typeKr);
        }

        convertView.setTag(position);
		convertView.setOnClickListener(itemClickListener);
		
		//ImageUtil.setBackgroundD(convertView, ImageUtil.getMakeSelector(mContext, "#ffffffff", "#ffe9e9ed", "#ffffffff", "#ffffffff", "#ffffffff", "#ffffffff"));

		return convertView;
	}

	public interface OnListItemClickListener{
		void onItemClick(OstRelatedItem item);
	}
	
	public void setOnListItemClickListener(OnListItemClickListener listener){
		m_ItemClickListener = listener;
	}
	
	//아이템 클릭 시
	OnClickListener itemClickListener =  new OnClickListener() {
		@Override
		public void onClick(View v) {			
			int position = (Integer)v.getTag();
            OstRelatedItem item = getItem(position);
						
			if(item != null && m_ItemClickListener != null){
				m_ItemClickListener.onItemClick(item);
			}
		}
	};
}
