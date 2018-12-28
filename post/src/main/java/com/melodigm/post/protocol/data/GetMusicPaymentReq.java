package com.melodigm.post.protocol.data;

public class GetMusicPaymentReq {
    String SSI = "";
    String PLAY_CYCLE_TYPE = "";
    int PLAY_ELAP_TIME = 0;
    int PLAY_SEEKING_TIME = 0;
    String OTI = "";
    String POI = "";

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getPLAY_CYCLE_TYPE() {
        return PLAY_CYCLE_TYPE;
    }

    public void setPLAY_CYCLE_TYPE(String PLAY_CYCLE_TYPE) {
        this.PLAY_CYCLE_TYPE = PLAY_CYCLE_TYPE;
    }

    public int getPLAY_ELAP_TIME() {
        return PLAY_ELAP_TIME;
    }

    public void setPLAY_ELAP_TIME(int PLAY_ELAP_TIME) {
        this.PLAY_ELAP_TIME = PLAY_ELAP_TIME;
    }

    public int getPLAY_SEEKING_TIME() {
        return PLAY_SEEKING_TIME;
    }

    public void setPLAY_SEEKING_TIME(int PLAY_SEEKING_TIME) {
        this.PLAY_SEEKING_TIME = PLAY_SEEKING_TIME;
    }

    public String getOTI() {
        return OTI;
    }

    public void setOTI(String OTI) {
        this.OTI = OTI;
    }

    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }
}
