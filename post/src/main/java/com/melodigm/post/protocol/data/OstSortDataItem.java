package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class OstSortDataItem implements Parcelable {
    private String SSI = "";
    private String OTI = "";
    private String DEL_YN = "";
    private int SORT_ORDER = 0;

    public OstSortDataItem(Parcel in) {
        SSI = in.readString();
        OTI = in.readString();
        DEL_YN = in.readString();
        SORT_ORDER = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSI);
        dest.writeString(OTI);
        dest.writeString(DEL_YN);
        dest.writeInt(SORT_ORDER);
    }

    public static final Creator<OstSortDataItem> CREATOR = new Creator<OstSortDataItem>() {
        public OstSortDataItem createFromParcel(Parcel in) {
            return new OstSortDataItem(in);
        }

        public OstSortDataItem[] newArray(int size) {
            return new OstSortDataItem[size];
        }
    };

    public OstSortDataItem() {
    }

    public OstSortDataItem(JSONObject jsonObj) throws JSONException {
        try {SSI = jsonObj.getString("SSI");} catch(Exception e){}
        try {OTI = jsonObj.getString("OTI");} catch(Exception e){}
        try {DEL_YN = jsonObj.getString("DEL_YN");} catch(Exception e){}
        try {SORT_ORDER = jsonObj.getInt("SORT_ORDER");} catch(Exception e){}
	}

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getOTI() {
        return OTI;
    }

    public void setOTI(String OTI) {
        this.OTI = OTI;
    }

    public String getDEL_YN() {
        return DEL_YN;
    }

    public void setDEL_YN(String DEL_YN) {
        this.DEL_YN = DEL_YN;
    }

    public int getSORT_ORDER() {
        return SORT_ORDER;
    }

    public void setSORT_ORDER(int SORT_ORDER) {
        this.SORT_ORDER = SORT_ORDER;
    }
}
