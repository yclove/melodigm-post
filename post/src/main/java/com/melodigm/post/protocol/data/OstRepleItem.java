package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class OstRepleItem implements Parcelable {
    private String ORI = "";
    private String OST_REPLY_TYPE = "";
    private String CONT = "";

    public OstRepleItem(Parcel in) {
        ORI = in.readString();
        OST_REPLY_TYPE = in.readString();
        CONT = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ORI);
        dest.writeString(OST_REPLY_TYPE);
        dest.writeString(CONT);
    }

    public static final Creator<OstRepleItem> CREATOR = new Creator<OstRepleItem>() {
        public OstRepleItem createFromParcel(Parcel in) {
            return new OstRepleItem(in);
        }

        public OstRepleItem[] newArray(int size) {
            return new OstRepleItem[size];
        }
    };

    public OstRepleItem() {
    }

    public OstRepleItem(JSONObject jsonObj) throws JSONException {
        try {ORI = jsonObj.getString("ORI");} catch(Exception e){}
        try {OST_REPLY_TYPE = jsonObj.getString("OST_REPLY_TYPE");} catch(Exception e){}
        try {CONT = jsonObj.getString("CONT");} catch(Exception e){}
	}

    public String getORI() {
        return ORI;
    }

    public void setORI(String ORI) {
        this.ORI = ORI;
    }

    public String getOST_REPLY_TYPE() {
        return OST_REPLY_TYPE;
    }

    public void setOST_REPLY_TYPE(String OST_REPLY_TYPE) {
        this.OST_REPLY_TYPE = OST_REPLY_TYPE;
    }

    public String getCONT() {
        return CONT;
    }

    public void setCONT(String CONT) {
        this.CONT = CONT;
    }
}
