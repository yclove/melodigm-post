package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetNotificationCenterRes {
    private ArrayList<NotificationCenterItem> arrNotificationCenterItem = new ArrayList<>();

    public ArrayList<NotificationCenterItem> getNotificationCenterItem() {
        return arrNotificationCenterItem;
    }

    public GetNotificationCenterRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrNotificationCenterItem.add(new NotificationCenterItem((JSONObject)DATA.get(i)));
            }
        }
    }
}
