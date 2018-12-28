package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class ColorItem {
    private String ICI = "";
    private String COLOR_CODE = "";
    private int SORT_ORDER = 0;
    private String ICON_NM = "";

    public ColorItem() {}

	public ColorItem(JSONObject jsonObj) throws JSONException {
        try {ICI = jsonObj.getString("ICI");} catch(Exception e){}
        try {COLOR_CODE = jsonObj.getString("COLOR_CODE");} catch(Exception e){}
        try {SORT_ORDER = jsonObj.getInt("SORT_ORDER");} catch(Exception e){}
        try {ICON_NM = jsonObj.getString("ICON_NM");} catch(Exception e){}
	}

    public String getICI() {
        return ICI;
    }

    public void setICI(String ICI) {
        this.ICI = ICI;
    }

    public String getCOLOR_CODE() {
        return COLOR_CODE;
    }

    public void setCOLOR_CODE(String COLOR_CODE) {
        this.COLOR_CODE = COLOR_CODE;
    }

    public int getSORT_ORDER() {
        return SORT_ORDER;
    }

    public void setSORT_ORDER(int SORT_ORDER) {
        this.SORT_ORDER = SORT_ORDER;
    }

    public String getICON_NM() {
        return ICON_NM;
    }

    public void setICON_NM(String ICON_NM) {
        this.ICON_NM = ICON_NM;
    }
}
