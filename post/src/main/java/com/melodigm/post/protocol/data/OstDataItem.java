package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * YCNOTE - Parcelable vs Serializable
 * Serializable 은 Java 만 아는 사람이라면 쉽게 알 수 있는 serialization 방법.
 * 그냥 Serializable 을 implementation 만 해주면, serialize 가 필요한 순간에 알아서 serialze 해주는 편리한 marker interface.
 *
 * 그러나, mobile 시대가 강림하면서 등장한 유망한 어린이(?) 가 있으니 그는 바로 Parcelable.
 * 이 녀석은 IPC ( Inter Process Communication ) 에 최적화된 녀석으로 Serialize 보다 속도가 빠르다.
 * 물론, 해야 하는 일은 Serialize 보다 훨씬 많다.
 * 직접 serialize 되어야 할 녀석들을 선별해서 그것을 쓰고 읽는 작업을 해주어야 한다.
 *
 * 그럼 왜 serialization 이 parcelable 보다 속도가 느릴까?
 * 그 이유는, serialization 은 reflection 방법을 사용하여 serialization 을 하는데, parcelable 은 프로그래머가 직접 바로 setting 을 해주기 때문에 빠른 것이다.
 * ( reflection 이 성능이슈를 야기할 수 있다는 것은 이미 알고 있을꺼라 생각한다.. )
 *
 * Serialization 도 그렇게 느리지는 않지만, Parcelable 이 훨씬 빠르다.
 * 정말 많은 뭉태기의 property 들이 한 클래스에 있는 경우는 드물겠지만, 그런 경우라면 Parcelable 의 성능이 훨씬 빠르다.
 */
public class OstDataItem implements Parcelable {
    private String POST_TYPE = "";
    private String POST_UAI = "";
    private String UAI = "";
    private String OTI = "";
    private String POI = "";
    private String SSI = "";
    private String REG_DATE = "";
    private String TITL_TOGGLE_YN = "";
    private String CONT = "";
    private String SONG_NM = "";
    private String ARTI_NM = "";
    private String COLOR = "";
    private String COLOR_HEX = "";
    private String EMOTICON = "";
    private String LIKE_TOGGLE_YN = "";
    private String LIKE_CNT = "";
    private String DCRE_CNT = "";
    private String DCRE_TOGGLE_YN = "";
    private String OST_REPLY_CNT = "";
    private String OST_REPLY_YN = "";
    private String ALBUM_PATH = "";
    private String RADIO_PATH = "";
    private String ADD_DATE = "";
    private String OST_YN = "";
    private boolean isChecked = false;
    private int intPlayState = 0;

    public OstDataItem(Parcel in) {
        POST_TYPE = in.readString();
        POST_UAI = in.readString();
        UAI = in.readString();
        OTI = in.readString();
        POI = in.readString();
        SSI = in.readString();
        REG_DATE = in.readString();
        TITL_TOGGLE_YN = in.readString();
        CONT = in.readString();
        SONG_NM = in.readString();
        ARTI_NM = in.readString();
        COLOR = in.readString();
        COLOR_HEX = in.readString();
        EMOTICON = in.readString();
        LIKE_TOGGLE_YN = in.readString();
        LIKE_CNT = in.readString();
        DCRE_CNT = in.readString();
        DCRE_TOGGLE_YN = in.readString();
        OST_REPLY_CNT = in.readString();
        OST_REPLY_YN = in.readString();
        ALBUM_PATH = in.readString();
        RADIO_PATH = in.readString();
        ADD_DATE = in.readString();
        OST_YN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(POST_TYPE);
        dest.writeString(POST_UAI);
        dest.writeString(UAI);
        dest.writeString(OTI);
        dest.writeString(POI);
        dest.writeString(SSI);
        dest.writeString(REG_DATE);
        dest.writeString(TITL_TOGGLE_YN);
        dest.writeString(CONT);
        dest.writeString(SONG_NM);
        dest.writeString(ARTI_NM);
        dest.writeString(COLOR);
        dest.writeString(COLOR_HEX);
        dest.writeString(EMOTICON);
        dest.writeString(LIKE_TOGGLE_YN);
        dest.writeString(LIKE_CNT);
        dest.writeString(DCRE_CNT);
        dest.writeString(DCRE_TOGGLE_YN);
        dest.writeString(OST_REPLY_CNT);
        dest.writeString(OST_REPLY_YN);
        dest.writeString(ALBUM_PATH);
        dest.writeString(RADIO_PATH);
        dest.writeString(ADD_DATE);
        dest.writeString(OST_YN);
    }

    public static final Parcelable.Creator<OstDataItem> CREATOR = new Parcelable.Creator<OstDataItem>() {
        public OstDataItem createFromParcel(Parcel in) {
            return new OstDataItem(in);
        }

        public OstDataItem[] newArray(int size) {
            return new OstDataItem[size];
        }
    };

    public OstDataItem() {
    }

    public OstDataItem(JSONObject jsonObj) throws JSONException {
        try {POST_TYPE = jsonObj.getString("POST_TYPE");} catch(Exception e){}
        try {POST_UAI = jsonObj.getString("POST_UAI");} catch(Exception e){}
        try {UAI = jsonObj.getString("UAI");} catch(Exception e){}
        try {OTI = jsonObj.getString("OTI");} catch(Exception e){}
        try {POI = jsonObj.getString("POI");} catch(Exception e){}
        try {SSI = jsonObj.getString("SSI");} catch(Exception e){}
        try {REG_DATE = jsonObj.getString("REG_DATE");} catch(Exception e){}
        try {TITL_TOGGLE_YN = jsonObj.getString("TITL_TOGGLE_YN");} catch(Exception e){}
        try {CONT = jsonObj.getString("CONT");} catch(Exception e){}
        try {SONG_NM = jsonObj.getString("SONG_NM");} catch(Exception e){}
        try {ARTI_NM = jsonObj.getString("ARTI_NM");} catch(Exception e){}
        try {DCRE_CNT = jsonObj.getString("DCRE_CNT");} catch(Exception e){}
        try {DCRE_TOGGLE_YN = jsonObj.getString("DCRE_TOGGLE_YN");} catch(Exception e){}
        try {ALBUM_PATH = jsonObj.getString("ALBUM_PATH");} catch(Exception e){}
        try {RADIO_PATH = jsonObj.getString("RADIO_PATH");} catch(Exception e){}
        try {ADD_DATE = jsonObj.getString("ADD_DATE");} catch(Exception e){}
        try {OST_YN = jsonObj.getString("OST_YN");} catch(Exception e){}
        try {COLOR_HEX = jsonObj.getString("COLOR_HEX");} catch(Exception e){}

        try {
            JSONObject REPLY_DATA = jsonObj.getJSONObject("REPLY_DATA");
            try {OST_REPLY_CNT = REPLY_DATA.getString("OST_REPLY_CNT");} catch(Exception e){}
            try {OST_REPLY_YN = REPLY_DATA.getString("OST_REPLY_YN");} catch(Exception e){}
        } catch (Exception e) {}

        try {
            JSONObject ICON_DATA = jsonObj.getJSONObject("ICON_DATA");
            try {COLOR = ICON_DATA.getString("COLOR");} catch(Exception e){}
            try {COLOR_HEX = ICON_DATA.getString("COLOR_HEX");} catch(Exception e){}
            try {EMOTICON = ICON_DATA.getString("EMOTICON");} catch(Exception e){}
            try {LIKE_CNT = ICON_DATA.getString("LIKE_CNT");} catch(Exception e){}
            try {LIKE_TOGGLE_YN = ICON_DATA.getString("LIKE_TOGGLE_YN");} catch(Exception e){}
        } catch (Exception e) {}
	}

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getPOST_UAI() {
        return POST_UAI;
    }

    public void setPOST_UAI(String POST_UAI) {
        this.POST_UAI = POST_UAI;
    }

    public String getUAI() {
        return UAI;
    }

    public void setUAI(String UAI) {
        this.UAI = UAI;
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

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }

    public String getTITL_TOGGLE_YN() {
        return TITL_TOGGLE_YN;
    }

    public void setTITL_TOGGLE_YN(String TITL_TOGGLE_YN) {
        this.TITL_TOGGLE_YN = TITL_TOGGLE_YN;
    }

    public String getCONT() {
        return CONT;
    }

    public void setCONT(String CONT) {
        this.CONT = CONT;
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

    public String getEMOTICON() {
        return EMOTICON;
    }

    public void setEMOTICON(String EMOTICON) {
        this.EMOTICON = EMOTICON;
    }

    public String getLIKE_TOGGLE_YN() {
        return LIKE_TOGGLE_YN;
    }

    public void setLIKE_TOGGLE_YN(String LIKE_TOGGLE_YN) {
        this.LIKE_TOGGLE_YN = LIKE_TOGGLE_YN;
    }

    public String getLIKE_CNT() {
        return LIKE_CNT;
    }

    public void setLIKE_CNT(String LIKE_CNT) {
        this.LIKE_CNT = LIKE_CNT;
    }

    public String getDCRE_CNT() {
        return DCRE_CNT;
    }

    public void setDCRE_CNT(String DCRE_CNT) {
        this.DCRE_CNT = DCRE_CNT;
    }

    public String getDCRE_TOGGLE_YN() {
        return DCRE_TOGGLE_YN;
    }

    public void setDCRE_TOGGLE_YN(String DCRE_TOGGLE_YN) {
        this.DCRE_TOGGLE_YN = DCRE_TOGGLE_YN;
    }

    public String getOST_REPLY_CNT() {
        return OST_REPLY_CNT;
    }

    public void setOST_REPLY_CNT(String OST_REPLY_CNT) {
        this.OST_REPLY_CNT = OST_REPLY_CNT;
    }

    public String getOST_REPLY_YN() {
        return OST_REPLY_YN;
    }

    public void setOST_REPLY_YN(String OST_REPLY_YN) {
        this.OST_REPLY_YN = OST_REPLY_YN;
    }

    public String getALBUM_PATH() {
        return ALBUM_PATH;
    }

    public void setALBUM_PATH(String ALBUM_PATH) {
        this.ALBUM_PATH = ALBUM_PATH;
    }

    public String getRADIO_PATH() {
        return RADIO_PATH;
    }

    public void setRADIO_PATH(String RADIO_PATH) {
        this.RADIO_PATH = RADIO_PATH;
    }

    public String getADD_DATE() {
        return ADD_DATE;
    }

    public void setADD_DATE(String ADD_DATE) {
        this.ADD_DATE = ADD_DATE;
    }

    public String getOST_YN() {
        return OST_YN;
    }

    public void setOST_YN(String OST_YN) {
        this.OST_YN = OST_YN;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getIntPlayState() {
        return intPlayState;
    }

    public void setIntPlayState(int intPlayState) {
        this.intPlayState = intPlayState;
    }
}
