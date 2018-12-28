package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class GetMusicPathRes {
    private String URL = "";

	public GetMusicPathRes(JSONObject jsonObj) throws JSONException {
        try{URL = jsonObj.getString("URL");} catch(Exception e){}
	}

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
