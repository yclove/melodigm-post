package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetPostDataRes {
    public GetPostDataRes() {}

    private int ROW_CNT = 0;
    private int TOTAL_CNT = 0;

    private ArrayList<PostDataItem> arrPostDataItem = new ArrayList<>();

    public ArrayList<PostDataItem> getPostDataItemList() {
        return arrPostDataItem;
    }

    public void addPostDataItem(PostDataItem item) {
        this.arrPostDataItem.add(item);
    }

	public GetPostDataRes(JSONObject jsonObj) throws JSONException {
        try{ROW_CNT = jsonObj.getInt("ROW_CNT");} catch(Exception e){}
        try{TOTAL_CNT = jsonObj.getInt("TOTAL_CNT");} catch(Exception e){}

        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrPostDataItem.add(new PostDataItem((JSONObject)DATA.get(i)));
            }
        }
	}

    public int getROW_CNT() {
        return ROW_CNT;
    }

    public void setROW_CNT(int ROW_CNT) {
        this.ROW_CNT = ROW_CNT;
    }

    public int getTOTAL_CNT() {
        return TOTAL_CNT;
    }

    public void setTOTAL_CNT(int TOTAL_CNT) {
        this.TOTAL_CNT = TOTAL_CNT;
    }
}
