package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class GetInitInfoRes {
    private int ACT_REG_CNT = 0;

	public GetInitInfoRes(JSONObject jsonObj) throws JSONException {
        try{ACT_REG_CNT = jsonObj.getInt("ACT_REG_CNT");} catch(Exception e){}
	}

    public int getACT_REG_CNT() {
        return this.ACT_REG_CNT;
    }

    public void setACT_REG_CNT(int ACT_REG_CNT) {
        this.ACT_REG_CNT = ACT_REG_CNT;
    }
}
