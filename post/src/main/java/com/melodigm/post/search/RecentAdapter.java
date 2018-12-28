package com.melodigm.post.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melodigm.post.R;

import java.util.ArrayList;

public class RecentAdapter extends BaseAdapter {

    protected Context mContext = null;
    protected ArrayList<String> mItems = null; //최근 검색어 목록!!
    protected OnListItemClickListener mItemClickListener = null;

    private boolean isDelBtnVisible = true;

    public RecentAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    public RecentAdapter(Context context, ArrayList<String> items) {
        mContext = context;
        mItems = items;
    }

    public void addAllItems(ArrayList<String> data){
        if(data != null) mItems = data;
        else mItems.clear();
    }

    public void deleteAllItems() {
        mItems.clear();
    }

    public void deleteItem(int position) {
        mItems.remove(position);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public String getItem(int position) {
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
            convertView = inflater.inflate(R.layout.adapter_search_recent_list_item, parent, false);
        }
        String item = getItem(position);

        TextView txtWord = (TextView) convertView.findViewById(R.id.search_recent_word);
        txtWord.setText(item);

        RelativeLayout btnDelete = (RelativeLayout) convertView.findViewById(R.id.search_recent_btn_delete);
        btnDelete.setTag(position);
        btnDelete.setOnClickListener(itemClickListener);
        if(isDelBtnVisible)
            btnDelete.setVisibility(View.VISIBLE);
        else
            btnDelete.setVisibility(View.INVISIBLE);

        convertView.setTag(position);
        convertView.setOnClickListener(itemClickListener);

        return convertView;
    }

    public interface OnListItemClickListener{
        void onItemClick(String word, int position);
        void onItemDeleteClick(String word, int position); //최신 검색어 삭제!!!! 리스트 갱신!!
    }

    public void setOnListItemClickListener(OnListItemClickListener listener){
        mItemClickListener = listener;
    }

    public void setDeleteBtnVisibility(boolean bVisible){
        isDelBtnVisible = bVisible;
    }

    //아이템 클릭 시,
    View.OnClickListener itemClickListener =  new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position;
            String item;

            switch (v.getId()) {
                case R.id.search_recent_btn_delete:
                    position = (Integer)v.getTag();
                    item = getItem(position);

                    if(mItemClickListener != null)
                        mItemClickListener.onItemDeleteClick(item, position);
                    break;
                default:
                    position = (Integer)v.getTag();
                    item = getItem(position);

                    if(mItemClickListener != null)
                        mItemClickListener.onItemClick(item, position);
                    break;
            }

        }
    };
}
