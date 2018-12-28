package com.melodigm.post.protocol.data;

public class GetSearchLocationReq {
	String PLACE = "";
	String LOCA_LAT = "";
    String LOCA_LNG = "";

    public String getPLACE() {
        return PLACE;
    }

    public void setPLACE(String PLACE) {
        this.PLACE = PLACE;
    }

    public String getLOCA_LAT() {
        return LOCA_LAT;
    }

    public void setLOCA_LAT(String LOCA_LAT) {
        this.LOCA_LAT = LOCA_LAT;
    }

    public String getLOCA_LNG() {
        return LOCA_LNG;
    }

    public void setLOCA_LNG(String LOCA_LNG) {
        this.LOCA_LNG = LOCA_LNG;
    }
}
