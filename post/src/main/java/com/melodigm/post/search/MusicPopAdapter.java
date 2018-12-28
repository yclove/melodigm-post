package com.melodigm.post.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.protocol.data.MusPopKeyWordItem;

import java.util.ArrayList;

public class MusicPopAdapter extends BaseAdapter {
    protected Context mContext = null;
    protected ArrayList<MusPopKeyWordItem> mItems = null;
    protected OnListItemClickListener mItemClickListener = null;

    public MusicPopAdapter(Context context, ArrayList<MusPopKeyWordItem> items) {
        mContext = context;
        mItems = items;
    }

    public void addAllItems(ArrayList<MusPopKeyWordItem> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public MusPopKeyWordItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_search_music_pop_list_item, parent, false);
        }
        MusPopKeyWordItem item = getItem(position);

        TextView tvMusicPopRank = (TextView)convertView.findViewById(R.id.tvMusicPopRank);
        TextView tvMusicPopKeyWord = (TextView)convertView.findViewById(R.id.tvMusicPopKeyWord);
        tvMusicPopRank.setText(String.valueOf(item.getRANKING()));
        tvMusicPopKeyWord.setText(item.getKEYWORD());

        convertView.setTag(item);
        convertView.setOnClickListener(itemClickListener);

        return convertView;
    }

    public interface OnListItemClickListener{
        void onItemClick(MusPopKeyWordItem item);
    }

    public void setOnListItemClickListener(OnListItemClickListener listener){
        mItemClickListener = listener;
    }

    //아이템 클릭 시,
    View.OnClickListener itemClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MusPopKeyWordItem item;

            switch (v.getId()) {
                default:
                    item = (MusPopKeyWordItem)v.getTag();
                    if(mItemClickListener != null)
                        mItemClickListener.onItemClick(item);
                    break;
            }

        }
    };
}
