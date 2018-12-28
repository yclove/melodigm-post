package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetOstDataRes {
    private ArrayList<OstDataItem> arrOstDataItem = new ArrayList<OstDataItem>();

    public ArrayList<OstDataItem> getOstDataItemList() {
        return arrOstDataItem;
    }

	public GetOstDataRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrOstDataItem.add(new OstDataItem((JSONObject)DATA.get(i)));
            }
        }
	}
}
