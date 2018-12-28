package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class TimeLineItem implements Parcelable {
    private String POST_TYPE = "";
    private String COLOR = "";
    private String COLOR_HEX = "";
    private String POI = "";
    private String OTI = "";
    private String POST_DCRE_YN = "";
    private String OST_DCRE_YN = "";
    private String POST_SUBJ = "";
    private String POST_CONT = "";
    private String OST_CONT = "";
    private String LIKE_YN = "";
    private String REPLY_YN = "";
    private String TITL_YN = "";
    private String SSI = "";
    private String ALBUM_PATH = "";
    private String SONG_NM = "";
    private String ARTI_NM = "";
    private int RUN_TIME = 0;
    private String REG_DATE = "";

    public TimeLineItem(Parcel in) {
        POST_TYPE = in.readString();
        COLOR = in.readString();
        COLOR_HEX = in.readString();
        POI = in.readString();
        OTI = in.readString();
        POST_DCRE_YN = in.readString();
        OST_DCRE_YN = in.readString();
        POST_SUBJ = in.readString();
        POST_CONT = in.readString();
        OST_CONT = in.readString();
        LIKE_YN = in.readString();
        REPLY_YN = in.readString();
        TITL_YN = in.readString();
        SSI = in.readString();
        ALBUM_PATH = in.readString();
        SONG_NM = in.readString();
        ARTI_NM = in.readString();
        RUN_TIME = in.readInt();
        REG_DATE = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(POST_TYPE);
        dest.writeString(COLOR);
        dest.writeString(COLOR_HEX);
        dest.writeString(POI);
        dest.writeString(OTI);
        dest.writeString(POST_DCRE_YN);
        dest.writeString(OST_DCRE_YN);
        dest.writeString(POST_SUBJ);
        dest.writeString(POST_CONT);
        dest.writeString(OST_CONT);
        dest.writeString(LIKE_YN);
        dest.writeString(REPLY_YN);
        dest.writeString(TITL_YN);
        dest.writeString(SSI);
        dest.writeString(ALBUM_PATH);
        dest.writeString(SONG_NM);
        dest.writeString(ARTI_NM);
        dest.writeInt(RUN_TIME);
        dest.writeString(REG_DATE);
    }

    public static final Creator<TimeLineItem> CREATOR = new Creator<TimeLineItem>() {
        public TimeLineItem createFromParcel(Parcel in) {
            return new TimeLineItem(in);
        }

        public TimeLineItem[] newArray(int size) {
            return new TimeLineItem[size];
        }
    };

    public TimeLineItem() {
    }

    public TimeLineItem(JSONObject jsonObj) throws JSONException {
        try {POST_TYPE = jsonObj.getString("POST_TYPE");} catch(Exception e){}
        try {COLOR = jsonObj.getString("COLOR");} catch(Exception e){}
        try {COLOR_HEX = jsonObj.getString("COLOR_HEX");} catch(Exception e){}
        try {POI = jsonObj.getString("POI");} catch(Exception e){}
        try {OTI = jsonObj.getString("OTI");} catch(Exception e){}
        try {POST_DCRE_YN = jsonObj.getString("POST_DCRE_YN");} catch(Exception e){}
        try {OST_DCRE_YN = jsonObj.getString("OST_DCRE_YN");} catch(Exception e){}
        try {POST_SUBJ = jsonObj.getString("POST_SUBJ");} catch(Exception e){}
        try {POST_CONT = jsonObj.getString("POST_CONT");} catch(Exception e){}
        try {OST_CONT = jsonObj.getString("OST_CONT");} catch(Exception e){}
        try {LIKE_YN = jsonObj.getString("LIKE_YN");} catch(Exception e){}
        try {REPLY_YN = jsonObj.getString("REPLY_YN");} catch(Exception e){}
        try {TITL_YN = jsonObj.getString("TITL_YN");} catch(Exception e){}
        try {SSI = jsonObj.getString("SSI");} catch(Exception e){}
        try {ALBUM_PATH = jsonObj.getString("ALBUM_PATH");} catch(Exception e){}
        try {SONG_NM = jsonObj.getString("SONG_NM");} catch(Exception e){}
        try {ARTI_NM = jsonObj.getString("ARTI_NM");} catch(Exception e){}
        try {RUN_TIME = jsonObj.getInt("RUNTIME");} catch(Exception e){}
        try {REG_DATE = jsonObj.getString("REG_DATE");} catch(Exception e){}
	}

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getCOLOR() {
        return COLOR;
    }

    public void setCOLOR(String COLOR) {
        this.COLOR = COLOR;
    }

    public String getCOLOR_HEX() {
        return COLOR_HEX;
    }

    public void setCOLOR_HEX(String COLOR_HEX) {
        this.COLOR_HEX = COLOR_HEX;
    }

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

    public String getPOST_DCRE_YN() {
        return POST_DCRE_YN;
    }

    public void setPOST_DCRE_YN(String POST_DCRE_YN) {
        this.POST_DCRE_YN = POST_DCRE_YN;
    }

    public String getOST_DCRE_YN() {
        return OST_DCRE_YN;
    }

    public void setOST_DCRE_YN(String OST_DCRE_YN) {
        this.OST_DCRE_YN = OST_DCRE_YN;
    }

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

    public String getOST_CONT() {
        return OST_CONT;
    }

    public void setOST_CONT(String OST_CONT) {
        this.OST_CONT = OST_CONT;
    }

    public String getLIKE_YN() {
        return LIKE_YN;
    }

    public void setLIKE_YN(String LIKE_YN) {
        this.LIKE_YN = LIKE_YN;
    }

    public String getREPLY_YN() {
        return REPLY_YN;
    }

    public void setREPLY_YN(String REPLY_YN) {
        this.REPLY_YN = REPLY_YN;
    }

    public String getTITL_YN() {
        return TITL_YN;
    }

    public void setTITL_YN(String TITL_YN) {
        this.TITL_YN = TITL_YN;
    }

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getALBUM_PATH() {
        return ALBUM_PATH;
    }

    public void setALBUM_PATH(String ALBUM_PATH) {
        this.ALBUM_PATH = ALBUM_PATH;
    }

    public String getSONG_NM() {
        return SONG_NM;
    }

    public void setSONG_NM(String SONG_NM) {
        this.SONG_NM = SONG_NM;
    }

    public String getARTI_NM() {
        return ARTI_NM;
    }

    public void setARTI_NM(String ARTI_NM) {
        this.ARTI_NM = ARTI_NM;
    }

    public int getRUN_TIME() {
        return RUN_TIME;
    }

    public void setRUN_TIME(int RUN_TIME) {
        this.RUN_TIME = RUN_TIME;
    }

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }
}
