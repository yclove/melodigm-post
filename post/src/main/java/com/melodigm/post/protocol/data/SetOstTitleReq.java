package com.melodigm.post.protocol.data;

public class SetOstTitleReq {
    String POI = "";
    String OTI = "";
    String SSI = "";
    String TITL_TOGGLE_YN = "";

    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }

    public String getOTI() {
        return OTI;
    }

    public void setOTI(String OTI) {
        this.OTI = OTI;
    }

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getTITL_TOGGLE_YN() {
        return TITL_TOGGLE_YN;
    }

    public void setTITL_TOGGLE_YN(String TITL_TOGGLE_YN) {
        this.TITL_TOGGLE_YN = TITL_TOGGLE_YN;
    }
}
