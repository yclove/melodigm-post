package com.melodigm.post.protocol.data;

public class GetStampDataReq {
    String POINT_TYPE = "";
    String TRANS_TYPE = "";
	String SEARCH_DATE = "";

    public String getPOINT_TYPE() {
        return POINT_TYPE;
    }

    public void setPOINT_TYPE(String POINT_TYPE) {
        this.POINT_TYPE = POINT_TYPE;
    }

    public String getTRANS_TYPE() {
        return TRANS_TYPE;
    }

    public void setTRANS_TYPE(String TRANS_TYPE) {
        this.TRANS_TYPE = TRANS_TYPE;
    }

    public String getSEARCH_DATE() {
        return SEARCH_DATE;
    }

    public void setSEARCH_DATE(String SEARCH_DATE) {
        this.SEARCH_DATE = SEARCH_DATE;
    }
}
