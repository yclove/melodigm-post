package com.melodigm.post.protocol.data;

public class SetSnsAccountRestoreReq {
    String ACCOUNT_ID = "";
    String ACCOUNT_AUTH_TYPE = "";
    String PUSH_ID = "";

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

    public String getPUSH_ID() {
        return PUSH_ID;
    }

    public void setPUSH_ID(String PUSH_ID) {
        this.PUSH_ID = PUSH_ID;
    }
}
