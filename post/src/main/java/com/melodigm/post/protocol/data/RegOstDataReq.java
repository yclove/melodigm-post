package com.melodigm.post.protocol.data;

import java.util.ArrayList;

public class RegOstDataReq {
	String POI = "";
	String SSI = "";
    String ICI = "";
    String CONT = "";

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

    public String getICI() {
        return ICI;
    }

    public void setICI(String ICI) {
        this.ICI = ICI;
    }

    public String getCONT() {
        return CONT;
    }

    public void setCONT(String CONT) {
        this.CONT = CONT;
    }
}
