package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetCabinetDescRes {
    private ArrayList<CabinetMusicDataItem> arrCabinetMusicDataItem = new ArrayList<>();

    public ArrayList<CabinetMusicDataItem> getCabinetMusicDataItem() {
        return arrCabinetMusicDataItem;
    }

	public GetCabinetDescRes(JSONObject jsonObj) throws JSONException {
        JSONArray DATA = jsonObj.getJSONArray("DATA");
        if (DATA != null) {
            for (int i = 0; i < DATA.length(); i++) {
                arrCabinetMusicDataItem.add(new CabinetMusicDataItem((JSONObject)DATA.get(i)));
            }
        }
	}
}
