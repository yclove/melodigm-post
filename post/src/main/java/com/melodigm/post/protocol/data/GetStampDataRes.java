package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetStampDataRes {
    private int NOW_POINT = 0;
    private int ACMLT_POINT = 0;
    private int CSPT_POINT = 0;

    private ArrayList<StampGroupItem> arrStampGroupItem = new ArrayList<>();

    public ArrayList<StampGroupItem> getStampGroupItems() {
        return arrStampGroupItem;
    }

    public GetStampDataRes(JSONObject jsonObj) throws JSONException {
        try {NOW_POINT = jsonObj.getInt("NOW_POINT");} catch(Exception e){}
        try {ACMLT_POINT = jsonObj.getInt("ACMLT_POINT");} catch(Exception e){}
        try {CSPT_POINT = jsonObj.getInt("CSPT_POINT");} catch(Exception e){}

        try {
            JSONArray DATA = jsonObj.getJSONArray("DATA");
            for (int i = 0; i < DATA.length(); i++) {
                arrStampGroupItem.add(new StampGroupItem((JSONObject)DATA.get(i)));
            }
        } catch(Exception e){}
    }

    public int getNOW_POINT() {
        return NOW_POINT;
    }

    public void setNOW_POINT(int NOW_POINT) {
        this.NOW_POINT = NOW_POINT;
    }

    public int getACMLT_POINT() {
        return ACMLT_POINT;
    }

    public void setACMLT_POINT(int ACMLT_POINT) {
        this.ACMLT_POINT = ACMLT_POINT;
    }

    public int getCSPT_POINT() {
        return CSPT_POINT;
    }

    public void setCSPT_POINT(int CSPT_POINT) {
        this.CSPT_POINT = CSPT_POINT;
    }
}
