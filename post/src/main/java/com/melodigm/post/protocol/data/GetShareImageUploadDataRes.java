package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetShareImageUploadDataRes {
    public GetShareImageUploadDataRes() {}

    private ArrayList<ShareImageUploadDataItem> arrShareImageUploadDataItem = new ArrayList<>();

    public ArrayList<ShareImageUploadDataItem> getShareImageUploadDataItemList() {
        return arrShareImageUploadDataItem;
    }

	public GetShareImageUploadDataRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrShareImageUploadDataItem.add(new ShareImageUploadDataItem((JSONObject)DATA.get(i)));
            }
        }
	}
}
