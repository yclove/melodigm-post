package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class NotiSettingItem {
    private String MANNER = "";
    private String POST = "";
    private String TODAY = "";
    private String OST = "";

    public NotiSettingItem() {}

    public NotiSettingItem(JSONObject jsonObj) throws JSONException {
        try {MANNER = jsonObj.getString("MANNER");} catch(Exception e){}
        try {POST = jsonObj.getString("POST");} catch(Exception e){}
        try {TODAY = jsonObj.getString("TODAY");} catch(Exception e){}
        try {OST = jsonObj.getString("OST");} catch(Exception e){}
    }

    public String getMANNER() {
        return MANNER;
    }

    public void setMANNER(String MANNER) {
        this.MANNER = MANNER;
    }

    public String getPOST() {
        return POST;
    }

    public void setPOST(String POST) {
        this.POST = POST;
    }

    public String getTODAY() {
        return TODAY;
    }

    public void setTODAY(String TODAY) {
        this.TODAY = TODAY;
    }

    public String getOST() {
        return OST;
    }

    public void setOST(String OST) {
        this.OST = OST;
    }
}
