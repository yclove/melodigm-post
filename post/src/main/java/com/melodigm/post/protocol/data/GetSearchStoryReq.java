package com.melodigm.post.protocol.data;

public class GetSearchStoryReq {
    String KEYWORD = "";
    int CURRENT_PAGE;
    String ICI = "";
    String POST_TYPE = "";
    String ORDER_TYPE = "";

    public String getKEYWORD() {
        return KEYWORD;
    }

    public void setKEYWORD(String KEYWORD) {
        this.KEYWORD = KEYWORD;
    }

    public int getCURRENT_PAGE() {
        return CURRENT_PAGE;
    }

    public void setCURRENT_PAGE(int CURRENT_PAGE) {
        this.CURRENT_PAGE = CURRENT_PAGE;
    }

    public String getICI() {
        return ICI;
    }

    public void setICI(String ICI) {
        this.ICI = ICI;
    }

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getORDER_TYPE() {
        return ORDER_TYPE;
    }

    public void setORDER_TYPE(String ORDER_TYPE) {
        this.ORDER_TYPE = ORDER_TYPE;
    }
}
