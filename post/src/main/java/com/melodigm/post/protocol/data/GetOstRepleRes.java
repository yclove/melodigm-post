package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetOstRepleRes {
    private OstDataItem ostDataItem;
    private ArrayList<OstRepleItem> arrOstRepleItem = new ArrayList<>();

    public OstDataItem getOstDataItem() {
        return ostDataItem;
    }

    public void setOstDataItem(OstDataItem ostDataItem) {
        this.ostDataItem = ostDataItem;
    }

    public ArrayList<OstRepleItem> getOstRepleItem() {
        return arrOstRepleItem;
    }

	public GetOstRepleRes(JSONObject jsonObj) throws JSONException {
        ostDataItem = new OstDataItem(jsonObj);

        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrOstRepleItem.add(new OstRepleItem((JSONObject)DATA.get(i)));
            }
        }
	}
}
