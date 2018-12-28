package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class PostHashTagKeyWordItem {
    private int RANKING = 0;
    private String KEYWORD = "";
    private int COUNT = 0;

    public PostHashTagKeyWordItem() {}

	public PostHashTagKeyWordItem(JSONObject jsonObj) throws JSONException {
        try {RANKING = jsonObj.getInt("RANKING");} catch(Exception e){}
        try {KEYWORD = "#" + jsonObj.getString("KEYWORD");} catch(Exception e){}
        try {COUNT = jsonObj.getInt("KEYWORD_CNT");} catch(Exception e){}
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

    public int getCOUNT() {
        return COUNT;
    }

    public void setCOUNT(int COUNT) {
        this.COUNT = COUNT;
    }
}
