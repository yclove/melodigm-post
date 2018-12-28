package com.melodigm.post.protocol.data;

public class RegCabinetDataReq {
    String BXI = "";
	String UBX_NM = "";
	String DESCR = "";

    public String getBXI() {
        return BXI;
    }

    public void setBXI(String BXI) {
        this.BXI = BXI;
    }

    public String getUBX_NM() {
        return UBX_NM;
    }

    public void setUBX_NM(String UBX_NM) {
        this.UBX_NM = UBX_NM;
    }

    public String getDESCR() {
        return DESCR;
    }

    public void setDESCR(String DESCR) {
        this.DESCR = DESCR;
    }
}
