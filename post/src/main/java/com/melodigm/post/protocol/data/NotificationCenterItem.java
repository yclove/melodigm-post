package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationCenterItem implements Parcelable {
    private String NOTI_TYPE = "";
    private String DESCR = "";
    private String NOTI_SRC_ID = "";
    private String USER_DEL_YN = "";
    private String POI = "";
    private String POST_TYPE = "";
    private String POST_DISP_TYPE  = "";
    private String POST_DCRE_CNT = "";
    private String OTI = "";
    private String OST_DEL_YN = "";
    private String OST_DCRE_CNT = "";
    private String ORI = "";
    private String REOST_DEL_YN = "";

    public NotificationCenterItem(Parcel in) {
        NOTI_TYPE = in.readString();
        DESCR = in.readString();
        NOTI_SRC_ID = in.readString();
        USER_DEL_YN = in.readString();
        POI = in.readString();
        POST_TYPE = in.readString();
        POST_DISP_TYPE = in.readString();
        POST_DCRE_CNT = in.readString();
        OTI = in.readString();
        OST_DEL_YN = in.readString();
        OST_DCRE_CNT = in.readString();
        ORI = in.readString();
        REOST_DEL_YN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(NOTI_TYPE);
        dest.writeString(DESCR);
        dest.writeString(NOTI_SRC_ID);
        dest.writeString(USER_DEL_YN);
        dest.writeString(POI);
        dest.writeString(POST_TYPE);
        dest.writeString(POST_DISP_TYPE);
        dest.writeString(POST_DCRE_CNT);
        dest.writeString(OTI);
        dest.writeString(OST_DEL_YN);
        dest.writeString(OST_DCRE_CNT);
        dest.writeString(ORI);
        dest.writeString(REOST_DEL_YN);
    }

    public static final Creator<NotificationCenterItem> CREATOR = new Creator<NotificationCenterItem>() {
        public NotificationCenterItem createFromParcel(Parcel in) {
            return new NotificationCenterItem(in);
        }

        public NotificationCenterItem[] newArray(int size) {
            return new NotificationCenterItem[size];
        }
    };

    public NotificationCenterItem() {
    }

    public NotificationCenterItem(JSONObject jsonObj) throws JSONException {
        try {NOTI_TYPE = jsonObj.getString("NOTI_TYPE");} catch(Exception e){}
        try {DESCR = jsonObj.getString("DESCR");} catch(Exception e){}
        try {NOTI_SRC_ID = jsonObj.getString("NOTI_SRC_ID");} catch(Exception e){}

        JSONObject DETAIL_INFO = jsonObj.getJSONObject("DETAIL_INFO");
        if (DETAIL_INFO != null) {
            try {USER_DEL_YN = DETAIL_INFO.getString("USER_DEL_YN");} catch(Exception e){}
            try {POI = DETAIL_INFO.getString("POI");} catch(Exception e){}
            try {POST_TYPE = DETAIL_INFO.getString("POST_TYPE");} catch(Exception e){}
            try {POST_DISP_TYPE = DETAIL_INFO.getString("POST_DISP_TYPE");} catch(Exception e){}
            try {POST_DCRE_CNT = DETAIL_INFO.getString("POST_DCRE_CNT");} catch(Exception e){}
            try {OTI = DETAIL_INFO.getString("OTI");} catch(Exception e){}
            try {OST_DEL_YN = DETAIL_INFO.getString("OST_DEL_YN");} catch(Exception e){}
            try {OST_DCRE_CNT = DETAIL_INFO.getString("OST_DCRE_CNT");} catch(Exception e){}
            try {ORI = DETAIL_INFO.getString("ORI");} catch(Exception e){}
            try {REOST_DEL_YN = DETAIL_INFO.getString("REOST_DEL_YN");} catch(Exception e){}
        }
	}

    public String getNOTI_TYPE() {
        return NOTI_TYPE;
    }

    public void setNOTI_TYPE(String NOTI_TYPE) {
        this.NOTI_TYPE = NOTI_TYPE;
    }

    public String getDESCR() {
        return DESCR;
    }

    public void setDESCR(String DESCR) {
        this.DESCR = DESCR;
    }

    public String getNOTI_SRC_ID() {
        return NOTI_SRC_ID;
    }

    public void setNOTI_SRC_ID(String NOTI_SRC_ID) {
        this.NOTI_SRC_ID = NOTI_SRC_ID;
    }

    public String getUSER_DEL_YN() {
        return USER_DEL_YN;
    }

    public void setUSER_DEL_YN(String USER_DEL_YN) {
        this.USER_DEL_YN = USER_DEL_YN;
    }

    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getPOST_DISP_TYPE() {
        return POST_DISP_TYPE;
    }

    public void setPOST_DISP_TYPE(String POST_DISP_TYPE) {
        this.POST_DISP_TYPE = POST_DISP_TYPE;
    }

    public String getPOST_DCRE_CNT() {
        return POST_DCRE_CNT;
    }

    public void setPOST_DCRE_CNT(String POST_DCRE_CNT) {
        this.POST_DCRE_CNT = POST_DCRE_CNT;
    }

    public String getOTI() {
        return OTI;
    }

    public void setOTI(String OTI) {
        this.OTI = OTI;
    }

    public String getOST_DEL_YN() {
        return OST_DEL_YN;
    }

    public void setOST_DEL_YN(String OST_DEL_YN) {
        this.OST_DEL_YN = OST_DEL_YN;
    }

    public String getOST_DCRE_CNT() {
        return OST_DCRE_CNT;
    }

    public void setOST_DCRE_CNT(String OST_DCRE_CNT) {
        this.OST_DCRE_CNT = OST_DCRE_CNT;
    }

    public String getORI() {
        return ORI;
    }

    public void setORI(String ORI) {
        this.ORI = ORI;
    }

    public String getREOST_DEL_YN() {
        return REOST_DEL_YN;
    }

    public void setREOST_DEL_YN(String REOST_DEL_YN) {
        this.REOST_DEL_YN = REOST_DEL_YN;
    }
}
