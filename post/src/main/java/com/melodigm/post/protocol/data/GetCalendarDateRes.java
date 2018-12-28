package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetCalendarDateRes {
    private boolean[] arrCalendarDateItem = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

    public boolean[] getCalendarDateItems() {
        return arrCalendarDateItem;
    }

    public GetCalendarDateRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        for (int i = 0; i < DATA.length(); i++) {
            try {
                arrCalendarDateItem[i + 1] = !((JSONObject)DATA.get(i)).getBoolean("BEING_FLAG");
            } catch(Exception e) {}
        }
    }
}
