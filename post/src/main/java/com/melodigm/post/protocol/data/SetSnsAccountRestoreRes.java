package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class SetSnsAccountRestoreRes {
    private String UAI = "";

	public SetSnsAccountRestoreRes(JSONObject jsonObj) throws JSONException {
        try{UAI = jsonObj.getString("UAI");} catch(Exception e){}
	}

    public String getUAI() {
        return UAI;
    }

    public void setUAI(String UAI) {
        this.UAI = UAI;
    }
}
