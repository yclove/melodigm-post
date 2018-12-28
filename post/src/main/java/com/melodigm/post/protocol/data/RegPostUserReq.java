package com.melodigm.post.protocol.data;

public class RegPostUserReq {
	String PUSH_ID = "";
	String BIRTHDATE = "";
	String GENDER = "";

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
}
