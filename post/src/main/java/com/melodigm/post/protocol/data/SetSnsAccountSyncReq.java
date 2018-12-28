package com.melodigm.post.protocol.data;

public class SetSnsAccountSyncReq {
    String ACCOUNT_ID = "";
    String ACCOUNT_AUTH_TYPE = "";

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
}
