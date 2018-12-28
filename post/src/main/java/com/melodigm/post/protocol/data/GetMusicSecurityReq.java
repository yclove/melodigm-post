package com.melodigm.post.protocol.data;

public class GetMusicSecurityReq {
    String POI = "";
    String SSI = "";
    String TYPE = "";

    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }
}
