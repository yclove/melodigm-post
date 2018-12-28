package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetPostPositionDataRes {
    private String ROW_CNT = "";
    private String TOTAL_CNT = "";

    private ArrayList<PostDataItem> arrPostDataItem = new ArrayList<>();

    public ArrayList<PostDataItem> getPostDataItemList() {
        return arrPostDataItem;
    }

	public GetPostPositionDataRes(JSONObject jsonObj) throws JSONException {
        try{ROW_CNT = jsonObj.getString("ROW_CNT");} catch(Exception e){}
        try{TOTAL_CNT = jsonObj.getString("TOTAL_CNT");} catch(Exception e){}

        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrPostDataItem.add(new PostDataItem((JSONObject)DATA.get(i)));
            }
        }
	}

    public String getROW_CNT() {
        return ROW_CNT;
    }

    public void setROW_CNT(String ROW_CNT) {
        this.ROW_CNT = ROW_CNT;
    }

    public String getTOTAL_CNT() {
        return TOTAL_CNT;
    }

    public void setTOTAL_CNT(String TOTAL_CNT) {
        this.TOTAL_CNT = TOTAL_CNT;
    }
}
