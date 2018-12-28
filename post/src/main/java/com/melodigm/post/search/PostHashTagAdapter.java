package com.melodigm.post.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.melodigm.post.R;
import com.melodigm.post.protocol.data.PostHashTagKeyWordItem;

import java.util.ArrayList;

public class PostHashTagAdapter extends BaseAdapter {
    protected Context mContext = null;
    protected ArrayList<PostHashTagKeyWordItem> mItems = null;
    protected OnListItemClickListener mItemClickListener = null;

    public PostHashTagAdapter(Context context, ArrayList<PostHashTagKeyWordItem> items) {
        mContext = context;
        mItems = items;
    }

    public void addAllItems(ArrayList<PostHashTagKeyWordItem> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public PostHashTagKeyWordItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.adapter_search_story_hashtag_list_item, parent, false);
        }
        PostHashTagKeyWordItem item = getItem(position);

        TextView tvPostHashTagRank = (TextView)convertView.findViewById(R.id.tvPostHashTagRank);
        TextView tvPostHashTagKeyWord = (TextView)convertView.findViewById(R.id.tvPostHashTagKeyWord);
        TextView tvPostHashTagCount = (TextView)convertView.findViewById(R.id.tvPostHashTagCount);

        tvPostHashTagRank.setText(String.valueOf(item.getRANKING()));
        tvPostHashTagKeyWord.setText(item.getKEYWORD());
        tvPostHashTagCount.setText(String.valueOf(item.getCOUNT()));

        convertView.setTag(item);
        convertView.setOnClickListener(itemClickListener);

        return convertView;
    }

    public interface OnListItemClickListener{
        void onItemClick(PostHashTagKeyWordItem item);
    }

    public void setOnListItemClickListener(OnListItemClickListener listener){
        mItemClickListener = listener;
    }

    //아이템 클릭 시,
    View.OnClickListener itemClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PostHashTagKeyWordItem item;

            switch (v.getId()) {
                default:
                    item = (PostHashTagKeyWordItem)v.getTag();
                    if(mItemClickListener != null)
                        mItemClickListener.onItemClick(item);
                    break;
            }

        }
    };
}
