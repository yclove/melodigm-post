package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetTimeLineRes {
    private ArrayList<TimeLineItem> arrTimeLineItem = new ArrayList<>();

    public ArrayList<TimeLineItem> getTimeLineItem() {
        return arrTimeLineItem;
    }

    public GetTimeLineRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrTimeLineItem.add(new TimeLineItem((JSONObject)DATA.get(i)));
            }
        }
    }
}
