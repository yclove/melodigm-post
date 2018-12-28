package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetColorImageRes {
    private String FID = "";
    private String BG_PIC_PATH = "";

	public GetColorImageRes(JSONObject jsonObj) throws JSONException {
        try {FID = jsonObj.getString("FID");} catch(Exception e){}
        try {BG_PIC_PATH = jsonObj.getString("BG_PIC_PATH");} catch(Exception e){}
	}

    public String getFID() {
        return FID;
    }

    public void setFID(String FID) {
        this.FID = FID;
    }

    public String getBG_PIC_PATH() {
        return BG_PIC_PATH;
    }

    public void setBG_PIC_PATH(String BG_PIC_PATH) {
        this.BG_PIC_PATH = BG_PIC_PATH;
    }
}
