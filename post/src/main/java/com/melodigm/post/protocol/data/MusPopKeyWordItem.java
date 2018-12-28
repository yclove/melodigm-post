package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class MusPopKeyWordItem {
    private int RANKING = 0;
    private String KEYWORD = "";

    public MusPopKeyWordItem() {}

	public MusPopKeyWordItem(JSONObject jsonObj) throws JSONException {
        try {RANKING = jsonObj.getInt("RANKING");} catch(Exception e){}
        try {KEYWORD = jsonObj.getString("KEYWORD");} catch(Exception e){}
	}

    public int getRANKING() {
        return RANKING;
    }

    public void setRANKING(int RANKING) {
        this.RANKING = RANKING;
    }

    public String getKEYWORD() {
        return KEYWORD;
    }

    public void setKEYWORD(String KEYWORD) {
        this.KEYWORD = KEYWORD;
    }
}
