package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class CalendarDateItem {
    private String REG_DATE = "";

    public CalendarDateItem() {}

	public CalendarDateItem(JSONObject jsonObj) throws JSONException {
        try {REG_DATE = jsonObj.getString("REG_DATE");} catch(Exception e){}
	}

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }
}
