package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetSearchLocationRes {
    private String status = "";

    private ArrayList<LocationItem> arrLocationItem = new ArrayList<LocationItem>();

    public ArrayList<LocationItem> getLocationItemList() {
        return arrLocationItem;
    }

	public GetSearchLocationRes(JSONObject jsonObj) throws JSONException {
        try{status = jsonObj.getString("status");} catch(Exception e){}

        JSONArray DATA = jsonObj.getJSONArray("results");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrLocationItem.add(new LocationItem((JSONObject)DATA.get(i)));
            }
        }
	}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
