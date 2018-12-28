package com.melodigm.post.protocol.data;

import java.util.ArrayList;

public class RegPostDataReq {
	String POST_SUBJ = "";
	String POST_CONT = "";
    String POST_PST_TYPE = "";
    String POST_TYPE = "";
    String DISP_TYPE = "";
    String LOCA_LNG = "";
    String LOCA_LAT = "";
    String PLACE = "";
    String KWD = "";
    ArrayList<String> HASH_TAG = new ArrayList<String>();
    String ICI = "";
    String FID = "";
    int RADIO_RUNTIME = 0;
    String SSI = "";

    public String getPOST_SUBJ() {
        return POST_SUBJ;
    }

    public void setPOST_SUBJ(String POST_SUBJ) {
        this.POST_SUBJ = POST_SUBJ;
    }

    public String getPOST_CONT() {
        return POST_CONT;
    }

    public void setPOST_CONT(String POST_CONT) {
        this.POST_CONT = POST_CONT;
    }

    public String getPOST_PST_TYPE() {
        return POST_PST_TYPE;
    }

    public void setPOST_PST_TYPE(String POST_PST_TYPE) {
        this.POST_PST_TYPE = POST_PST_TYPE;
    }

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getDISP_TYPE() {
        return DISP_TYPE;
    }

    public void setDISP_TYPE(String DISP_TYPE) {
        this.DISP_TYPE = DISP_TYPE;
    }

    public String getLOCA_LNG() {
        return LOCA_LNG;
    }

    public void setLOCA_LNG(String LOCA_LNG) {
        this.LOCA_LNG = LOCA_LNG;
    }

    public String getLOCA_LAT() {
        return LOCA_LAT;
    }

    public void setLOCA_LAT(String LOCA_LAT) {
        this.LOCA_LAT = LOCA_LAT;
    }

    public String getPLACE() {
        return PLACE;
    }

    public void setPLACE(String PLACE) {
        this.PLACE = PLACE;
    }

    public String getKWD() {
        return KWD;
    }

    public void setKWD(String KWD) {
        this.KWD = KWD;
    }

    public ArrayList<String> getHASH_TAG() {
        return HASH_TAG;
    }

    public void setHASH_TAG(String HASH_TAG) {
        this.HASH_TAG.add(HASH_TAG);
    }

    public String getICI() {
        return ICI;
    }

    public void setICI(String ICI) {
        this.ICI = ICI;
    }

    public String getFID() {
        return FID;
    }

    public void setFID(String FID) {
        this.FID = FID;
    }

    public int getRADIO_RUNTIME() {
        return RADIO_RUNTIME;
    }

    public void setRADIO_RUNTIME(int RADIO_RUNTIME) {
        this.RADIO_RUNTIME = RADIO_RUNTIME;
    }

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }
}
