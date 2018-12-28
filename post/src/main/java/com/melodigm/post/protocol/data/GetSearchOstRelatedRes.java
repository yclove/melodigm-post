package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetSearchOstRelatedRes {
    private ArrayList<OstRelatedItem> arrOstRelatedItem = new ArrayList<>();

    public ArrayList<OstRelatedItem> getOstRelatedItem() {
        return arrOstRelatedItem;
    }

    public GetSearchOstRelatedRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrOstRelatedItem.add(new OstRelatedItem((JSONObject)DATA.get(i)));
            }
        }
    }
}
