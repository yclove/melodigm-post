package com.melodigm.post.protocol.data;

public class GetSearchOstReq {
    String KEYWORD = "";
    String ICI = "";
    String TYPE = "";
    String CHO_SRC_ID = "";

    public String getKEYWORD() {
        return KEYWORD;
    }

    public void setKEYWORD(String KEYWORD) {
        this.KEYWORD = KEYWORD;
    }

    public String getICI() {
        return ICI;
    }

    public void setICI(String ICI) {
        this.ICI = ICI;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public String getCHO_SRC_ID() {
        return CHO_SRC_ID;
    }

    public void setCHO_SRC_ID(String CHO_SRC_ID) {
        this.CHO_SRC_ID = CHO_SRC_ID;
    }
}
