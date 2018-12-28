package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class OstRelatedItem {
    private String KEYWORD = "";
    private String ROW_NUM = "";
    private String TYPE = "";
    private String TYPE_NM = "";

	public OstRelatedItem(JSONObject jsonObj) throws JSONException {
        try {KEYWORD = jsonObj.getString("KEYWORD");} catch(Exception e){}
        try {ROW_NUM = jsonObj.getString("ROW_NUM");} catch(Exception e){}
        try {TYPE = jsonObj.getString("TYPE");} catch(Exception e){}
        try {TYPE_NM = jsonObj.getString("TYPE_NM");} catch(Exception e){}
	}

    public String getKEYWORD() {
        return KEYWORD;
    }

    public void setKEYWORD(String KEYWORD) {
        this.KEYWORD = KEYWORD;
    }

    public String getROW_NUM() {
        return ROW_NUM;
    }

    public void setROW_NUM(String ROW_NUM) {
        this.ROW_NUM = ROW_NUM;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public String getTYPE_NM() {
        return TYPE_NM;
    }

    public void setTYPE_NM(String TYPE_NM) {
        this.TYPE_NM = TYPE_NM;
    }
}
