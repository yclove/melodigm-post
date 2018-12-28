package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class CabinetMusicDataItem implements Parcelable {
    private String SORT_ORDER = "";
    private String BXI = "";
    private String SSI = "";
    private String POI = "";
    private String OTI = "";
    private String SONG_NM = "";
    private String ARTI_NM = "";
    private String OTI_DEL_YN = "";
    private String POI_DISP_YN = "";
    private String POST_TYPE = "";
    private String REG_DATE = "";
    private String ALBUM_PATH = "";
    private String TITL_TOGGLE_YN = "";
    private boolean isChecked = false;

    public CabinetMusicDataItem(Parcel in) {
        SORT_ORDER = in.readString();
        BXI = in.readString();
        SSI = in.readString();
        POI = in.readString();
        OTI = in.readString();
        SONG_NM = in.readString();
        ARTI_NM = in.readString();
        OTI_DEL_YN = in.readString();
        POI_DISP_YN = in.readString();
        POST_TYPE = in.readString();
        REG_DATE = in.readString();
        ALBUM_PATH = in.readString();
        TITL_TOGGLE_YN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SORT_ORDER);
        dest.writeString(BXI);
        dest.writeString(SSI);
        dest.writeString(POI);
        dest.writeString(OTI);
        dest.writeString(SONG_NM);
        dest.writeString(ARTI_NM);
        dest.writeString(OTI_DEL_YN);
        dest.writeString(POI_DISP_YN);
        dest.writeString(POST_TYPE);
        dest.writeString(REG_DATE);
        dest.writeString(ALBUM_PATH);
        dest.writeString(TITL_TOGGLE_YN);
    }

    public static final Creator<CabinetMusicDataItem> CREATOR = new Creator<CabinetMusicDataItem>() {
        public CabinetMusicDataItem createFromParcel(Parcel in) {
            return new CabinetMusicDataItem(in);
        }

        public CabinetMusicDataItem[] newArray(int size) {
            return new CabinetMusicDataItem[size];
        }
    };

    public CabinetMusicDataItem() {
    }

    public CabinetMusicDataItem(JSONObject jsonObj) throws JSONException {
        try {SORT_ORDER = jsonObj.getString("SORT_ORDER");} catch(Exception e){}
        try {BXI = jsonObj.getString("BXI");} catch(Exception e){}
        try {SSI = jsonObj.getString("SSI");} catch(Exception e){}
        try {POI = jsonObj.getString("POI");} catch(Exception e){}
        try {OTI = jsonObj.getString("OTI");} catch(Exception e){}
        try {SONG_NM = jsonObj.getString("SONG_NM");} catch(Exception e){}
        try {ARTI_NM = jsonObj.getString("ARTI_NM");} catch(Exception e){}
        try {OTI_DEL_YN = jsonObj.getString("OTI_DEL_YN");} catch(Exception e){}
        try {POI_DISP_YN = jsonObj.getString("POI_DISP_YN");} catch(Exception e){}
        try {POST_TYPE = jsonObj.getString("POST_TYPE");} catch(Exception e){}
        try {REG_DATE = jsonObj.getString("REG_DATE");} catch(Exception e){}
        try {ALBUM_PATH = jsonObj.getString("ALBUM_PATH");} catch(Exception e){}
        try {TITL_TOGGLE_YN = jsonObj.getString("TITL_TOGGLE_YN");} catch(Exception e){}
	}

    public String getSORT_ORDER() {
        return SORT_ORDER;
    }

    public void setSORT_ORDER(String SORT_ORDER) {
        this.SORT_ORDER = SORT_ORDER;
    }

    public String getBXI() {
        return BXI;
    }

    public void setBXI(String BXI) {
        this.BXI = BXI;
    }

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
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

    public String getOTI_DEL_YN() {
        return OTI_DEL_YN;
    }

    public void setOTI_DEL_YN(String OTI_DEL_YN) {
        this.OTI_DEL_YN = OTI_DEL_YN;
    }

    public String getPOI_DISP_YN() {
        return POI_DISP_YN;
    }

    public void setPOI_DISP_YN(String POI_DISP_YN) {
        this.POI_DISP_YN = POI_DISP_YN;
    }

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }

    public String getALBUM_PATH() {
        return ALBUM_PATH;
    }

    public void setALBUM_PATH(String ALBUM_PATH) {
        this.ALBUM_PATH = ALBUM_PATH;
    }

    public String getTITL_TOGGLE_YN() {
        return TITL_TOGGLE_YN;
    }

    public void setTITL_TOGGLE_YN(String TITL_TOGGLE_YN) {
        this.TITL_TOGGLE_YN = TITL_TOGGLE_YN;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
