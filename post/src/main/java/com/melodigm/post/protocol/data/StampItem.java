package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class StampItem implements Parcelable {
    private String ACT_TYPE = "";
    private int POINT_VAR_NUM = 0;
    private String ACT_DIVN_TYPE = "";
    private String ACT_PLAC_ID = "";
    private String ACT_MSG = "";

    public StampItem(Parcel in) {
        ACT_TYPE = in.readString();
        POINT_VAR_NUM = in.readInt();
        ACT_DIVN_TYPE = in.readString();
        ACT_PLAC_ID = in.readString();
        ACT_MSG = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ACT_TYPE);
        dest.writeInt(POINT_VAR_NUM);
        dest.writeString(ACT_DIVN_TYPE);
        dest.writeString(ACT_PLAC_ID);
        dest.writeString(ACT_MSG);
    }

    public static final Creator<StampItem> CREATOR = new Creator<StampItem>() {
        public StampItem createFromParcel(Parcel in) {
            return new StampItem(in);
        }

        public StampItem[] newArray(int size) {
            return new StampItem[size];
        }
    };

    public StampItem() {
    }

    public StampItem(JSONObject jsonObj) throws JSONException {
        try {ACT_TYPE = jsonObj.getString("ACT_TYPE");} catch(Exception e){}
        try {POINT_VAR_NUM = jsonObj.getInt("POINT_VAR_NUM");} catch(Exception e){}
        try {ACT_DIVN_TYPE = jsonObj.getString("ACT_DIVN_TYPE");} catch(Exception e){}
        try {ACT_PLAC_ID = jsonObj.getString("ACT_PLAC_ID");} catch(Exception e){}
        try {ACT_MSG = jsonObj.getString("ACT_MSG");} catch(Exception e){}
	}

    public String getACT_TYPE() {
        return ACT_TYPE;
    }

    public void setACT_TYPE(String ACT_TYPE) {
        this.ACT_TYPE = ACT_TYPE;
    }

    public int getPOINT_VAR_NUM() {
        return POINT_VAR_NUM;
    }

    public void setPOINT_VAR_NUM(int POINT_VAR_NUM) {
        this.POINT_VAR_NUM = POINT_VAR_NUM;
    }

    public String getACT_DIVN_TYPE() {
        return ACT_DIVN_TYPE;
    }

    public void setACT_DIVN_TYPE(String ACT_DIVN_TYPE) {
        this.ACT_DIVN_TYPE = ACT_DIVN_TYPE;
    }

    public String getACT_PLAC_ID() {
        return ACT_PLAC_ID;
    }

    public void setACT_PLAC_ID(String ACT_PLAC_ID) {
        this.ACT_PLAC_ID = ACT_PLAC_ID;
    }

    public String getACT_MSG() {
        return ACT_MSG;
    }

    public void setACT_MSG(String ACT_MSG) {
        this.ACT_MSG = ACT_MSG;
    }
}
