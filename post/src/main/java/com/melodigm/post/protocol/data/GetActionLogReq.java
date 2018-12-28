package com.melodigm.post.protocol.data;

public class GetActionLogReq {
	String ACT_DIVN_TYPE = "";
	String ACT_TYPE = "";
    String ACT_PLAC_TYPE = "";
    String ACT_PLAC_ID = "";

    public String getACT_DIVN_TYPE() {
        return ACT_DIVN_TYPE;
    }

    public void setACT_DIVN_TYPE(String ACT_DIVN_TYPE) {
        this.ACT_DIVN_TYPE = ACT_DIVN_TYPE;
    }

    public String getACT_TYPE() {
        return ACT_TYPE;
    }

    public void setACT_TYPE(String ACT_TYPE) {
        this.ACT_TYPE = ACT_TYPE;
    }

    public String getACT_PLAC_TYPE() {
        return ACT_PLAC_TYPE;
    }

    public void setACT_PLAC_TYPE(String ACT_PLAC_TYPE) {
        this.ACT_PLAC_TYPE = ACT_PLAC_TYPE;
    }

    public String getACT_PLAC_ID() {
        return ACT_PLAC_ID;
    }

    public void setACT_PLAC_ID(String ACT_PLAC_ID) {
        this.ACT_PLAC_ID = ACT_PLAC_ID;
    }
}
