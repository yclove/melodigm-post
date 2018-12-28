package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class PushDataItem {
	String PUSH_ID = "";
	String UAI = "";
    String DEVI_TYPE = "";

    public PushDataItem(JSONObject jsonObj) throws JSONException {
        try{PUSH_ID = jsonObj.getString("PUSH_ID");} catch(Exception e){}
        try{UAI = jsonObj.getString("UAI");} catch(Exception e){}
        try{DEVI_TYPE = jsonObj.getString("DEVI_TYPE");} catch(Exception e){}
    }

    public String getPUSH_ID() {
        return PUSH_ID;
    }

    public void setPUSH_ID(String PUSH_ID) {
        this.PUSH_ID = PUSH_ID;
    }

    public String getUAI() {
        return UAI;
    }

    public void setUAI(String UAI) {
        this.UAI = UAI;
    }

    public String getDEVI_TYPE() {
        return DEVI_TYPE;
    }

    public void setDEVI_TYPE(String DEVI_TYPE) {
        this.DEVI_TYPE = DEVI_TYPE;
    }
}
