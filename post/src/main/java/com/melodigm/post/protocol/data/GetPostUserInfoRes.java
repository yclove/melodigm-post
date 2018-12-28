package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetPostUserInfoRes {
	String PUSH_ID = "";
	String BIRTHDATE = "";
    String GENDER = "";
    String ACCOUNT_ID = "";
    String ACCOUNT_AUTH_TYPE = "";

    private ArrayList<PushDataItem> arrPushDataItem = new ArrayList<>();

    public ArrayList<PushDataItem> getPushDataItemList() {
        return arrPushDataItem;
    }

    public GetPostUserInfoRes(JSONObject jsonObj) throws JSONException {
        try{PUSH_ID = jsonObj.getString("PUSH_ID");} catch(Exception e){}
        try{BIRTHDATE = jsonObj.getString("BIRTHDATE");} catch(Exception e){}
        try{GENDER = jsonObj.getString("GENDER");} catch(Exception e){}
        try{ACCOUNT_ID = jsonObj.getString("ACCOUNT_ID");} catch(Exception e){}
        try{ACCOUNT_AUTH_TYPE = jsonObj.getString("ACCOUNT_AUTH_TYPE");} catch(Exception e){}

        try {
            JSONArray PUSH_IDS = jsonObj.getJSONArray("PUSH_IDS");
            for (int i = 0; i < PUSH_IDS.length(); i++) {
                arrPushDataItem.add(new PushDataItem((JSONObject)PUSH_IDS.get(i)));
            }
        } catch (Exception e) {}
    }

    public String getPUSH_ID() {
        return PUSH_ID;
    }

    public void setPUSH_ID(String PUSH_ID) {
        this.PUSH_ID = PUSH_ID;
    }

    public String getBIRTHDATE() {
        return BIRTHDATE;
    }

    public void setBIRTHDATE(String BIRTHDATE) {
        this.BIRTHDATE = BIRTHDATE;
    }

    public String getGENDER() {
        return GENDER;
    }

    public void setGENDER(String GENDER) {
        this.GENDER = GENDER;
    }

    public String getACCOUNT_ID() {
        return ACCOUNT_ID;
    }

    public void setACCOUNT_ID(String ACCOUNT_ID) {
        this.ACCOUNT_ID = ACCOUNT_ID;
    }

    public String getACCOUNT_AUTH_TYPE() {
        return ACCOUNT_AUTH_TYPE;
    }

    public void setACCOUNT_AUTH_TYPE(String ACCOUNT_AUTH_TYPE) {
        this.ACCOUNT_AUTH_TYPE = ACCOUNT_AUTH_TYPE;
    }
}
