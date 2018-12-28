package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StampGroupItem implements Parcelable {
    private String REG_DATE = "";

    private ArrayList<StampItem> arrStampItem = new ArrayList<>();

    public ArrayList<StampItem> getStampItems() {
        return arrStampItem;
    }

    public StampGroupItem(Parcel in) {
        REG_DATE = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(REG_DATE);
    }

    public static final Creator<StampGroupItem> CREATOR = new Creator<StampGroupItem>() {
        public StampGroupItem createFromParcel(Parcel in) {
            return new StampGroupItem(in);
        }

        public StampGroupItem[] newArray(int size) {
            return new StampGroupItem[size];
        }
    };

    public StampGroupItem() {
    }

    public StampGroupItem(JSONObject jsonObj) throws JSONException {
        try {REG_DATE = jsonObj.getString("REG_DATE");} catch(Exception e){}

        try {
            JSONArray DAY_DATA = jsonObj.getJSONArray("DAY_DATA");
            for (int i = 0; i < DAY_DATA.length(); i++) {
                arrStampItem.add(new StampItem((JSONObject)DAY_DATA.get(i)));
            }
        } catch(Exception e){}
    }

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }
}
