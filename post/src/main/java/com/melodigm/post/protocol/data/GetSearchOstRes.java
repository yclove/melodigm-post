package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetSearchOstRes {
    private int TOTAL_CNT = 0;
    private String TYPE = "";
    private int ALBUM_CNT = 0;
    private int SONG_CNT = 0;
    private int ARTI_CNT = 0;

    private ArrayList<OstDataItem> arrOstDataItem = new ArrayList<>();
    private ArrayList<OstDataItem> arrSongDataItem = new ArrayList<>();
    private ArrayList<OstDataItem> arrArtiDataItem = new ArrayList<>();
    private ArrayList<OstDataItem> arrAlbumDataItem = new ArrayList<>();

    public ArrayList<OstDataItem> getOstDataItem() {
        return arrOstDataItem;
    }

    public ArrayList<OstDataItem> getSongDataItem() {
        return arrSongDataItem;
    }

    public ArrayList<OstDataItem> getArtiDataItem() {
        return arrArtiDataItem;
    }

    public ArrayList<OstDataItem> getAlbumDataItem() {
        return arrAlbumDataItem;
    }

	public GetSearchOstRes(JSONObject jsonObj) throws JSONException {
        try{TOTAL_CNT = jsonObj.getInt("TOTAL_CNT");} catch(Exception e){}
        try{TYPE = jsonObj.getString("TYPE");} catch(Exception e){}
        try{ALBUM_CNT = jsonObj.getInt("ALBUM_CNT");} catch(Exception e){}
        try{SONG_CNT = jsonObj.getInt("SONG_CNT");} catch(Exception e){}
        try{ARTI_CNT = jsonObj.getInt("ARTI_CNT");} catch(Exception e){}

        try {
            JSONArray DATA = jsonObj.optJSONArray("DATA");
            for (int i = 0; i < DATA.length(); i++)
                arrOstDataItem.add(new OstDataItem((JSONObject)DATA.get(i)));
        } catch (Exception e) {}

        try {
            JSONArray SONG = jsonObj.optJSONArray("SONG");
            for (int i = 0; i < SONG.length(); i++)
                arrSongDataItem.add(new OstDataItem((JSONObject)SONG.get(i)));
        } catch (Exception e) {}

        try {
            JSONArray ARTI = jsonObj.optJSONArray("ARTI");
            for (int i = 0; i < ARTI.length(); i++)
                arrArtiDataItem.add(new OstDataItem((JSONObject)ARTI.get(i)));
        } catch (Exception e) {}

        try {
            JSONArray ALBUM = jsonObj.optJSONArray("ALBUM");
            for (int i = 0; i < ALBUM.length(); i++)
                arrAlbumDataItem.add(new OstDataItem((JSONObject)ALBUM.get(i)));
        } catch (Exception e) {}
	}

    public int getTOTAL_CNT() {
        return TOTAL_CNT;
    }

    public void setTOTAL_CNT(int TOTAL_CNT) {
        this.TOTAL_CNT = TOTAL_CNT;
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
    }

    public int getALBUM_CNT() {
        return ALBUM_CNT;
    }

    public void setALBUM_CNT(int ALBUM_CNT) {
        this.ALBUM_CNT = ALBUM_CNT;
    }

    public int getSONG_CNT() {
        return SONG_CNT;
    }

    public void setSONG_CNT(int SONG_CNT) {
        this.SONG_CNT = SONG_CNT;
    }

    public int getARTI_CNT() {
        return ARTI_CNT;
    }

    public void setARTI_CNT(int ARTI_CNT) {
        this.ARTI_CNT = ARTI_CNT;
    }
}
