package com.melodigm.post.protocol.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CabinetDataItem implements Parcelable {
    private String BXI = "";
    private String UBX_IMG = "";
    private String UBX_NM = "";
    private String DESCR = "";
    private int MUS_CNT = 0;
    private boolean isChecked = false;

    private ArrayList<OstDataItem> arrOstDataItem = new ArrayList<>();

    public CabinetDataItem(Parcel in) {
        BXI = in.readString();
        UBX_IMG = in.readString();
        UBX_NM = in.readString();
        DESCR = in.readString();
        MUS_CNT = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(BXI);
        dest.writeString(UBX_IMG);
        dest.writeString(UBX_NM);
        dest.writeString(DESCR);
        dest.writeInt(MUS_CNT);
    }

    public static final Creator<CabinetDataItem> CREATOR = new Creator<CabinetDataItem>() {
        public CabinetDataItem createFromParcel(Parcel in) {
            return new CabinetDataItem(in);
        }

        public CabinetDataItem[] newArray(int size) {
            return new CabinetDataItem[size];
        }
    };

    public CabinetDataItem() {
    }

    public CabinetDataItem(JSONObject jsonObj) throws JSONException {
        try {BXI = jsonObj.getString("BXI");} catch(Exception e){}
        try {UBX_IMG = jsonObj.getString("UBX_IMG");} catch(Exception e){}
        try {UBX_NM = jsonObj.getString("UBX_NM");} catch(Exception e){}
        try {DESCR = jsonObj.getString("DESCR");} catch(Exception e){}
        try {MUS_CNT = jsonObj.getInt("MUS_CNT");} catch(Exception e){}

        try {
            JSONArray MUS_LIST = jsonObj.getJSONArray("MUS_LIST");
            for (int i = 0; i < MUS_LIST.length(); i++) {
                arrOstDataItem.add(new OstDataItem((JSONObject)MUS_LIST.get(i)));
            }
        } catch (Exception e) {}
	}

    public String getBXI() {
        return BXI;
    }

    public void setBXI(String BXI) {
        this.BXI = BXI;
    }

    public String getUBX_IMG() {
        return UBX_IMG;
    }

    public void setUBX_IMG(String UBX_IMG) {
        this.UBX_IMG = UBX_IMG;
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

    public int getMUS_CNT() {
        return MUS_CNT;
    }

    public void setMUS_CNT(int MUS_CNT) {
        this.MUS_CNT = MUS_CNT;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public ArrayList<OstDataItem> getArrOstDataItem() {
        return arrOstDataItem;
    }

    public void setArrOstDataItem(ArrayList<OstDataItem> arrOstDataItem) {
        this.arrOstDataItem = arrOstDataItem;
    }
}
