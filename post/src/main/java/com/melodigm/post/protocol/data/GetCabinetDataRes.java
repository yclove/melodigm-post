package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetCabinetDataRes {
    private ArrayList<CabinetDataItem> arrCabinetDataItem = new ArrayList<>();

    public ArrayList<CabinetDataItem> getCabinetDataItem() {
        return arrCabinetDataItem;
    }

	public GetCabinetDataRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrCabinetDataItem.add(new CabinetDataItem((JSONObject)DATA.get(i)));
            }
        }
	}
}
