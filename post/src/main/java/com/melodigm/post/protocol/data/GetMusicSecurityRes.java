package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class GetMusicSecurityRes {
    private String SYMMETRIC_KEY = "";

	public GetMusicSecurityRes(JSONObject jsonObj) throws JSONException {
        try{SYMMETRIC_KEY = jsonObj.getString("SYMMETRIC_KEY");} catch(Exception e){}
	}

    public String getSYMMETRIC_KEY() {
        return SYMMETRIC_KEY;
    }

    public void setSYMMETRIC_KEY(String SYMMETRIC_KEY) {
        this.SYMMETRIC_KEY = SYMMETRIC_KEY;
    }
}
