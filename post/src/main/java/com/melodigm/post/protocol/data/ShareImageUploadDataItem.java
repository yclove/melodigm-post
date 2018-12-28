package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareImageUploadDataItem {
    public ShareImageUploadDataItem() {}

    private String FULL_PATH = "";

	public ShareImageUploadDataItem(JSONObject jsonObj) throws JSONException {
        try{FULL_PATH = jsonObj.getString("FULL_PATH");} catch(Exception e){}
	}

    public String getFULL_PATH() {
        return FULL_PATH;
    }

    public void setFULL_PATH(String FULL_PATH) {
        this.FULL_PATH = FULL_PATH;
    }
}
