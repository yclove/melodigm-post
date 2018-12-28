package com.melodigm.post.protocol.data;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationItem {
    private String lat = "";
    private String lng = "";
    private String name = "";
    private String vicinity = "";
    private boolean isChecked = false;

    public LocationItem(String lat, String lng, String name, String vicinity) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.vicinity = vicinity;
    }

	public LocationItem(JSONObject jsonObj) throws JSONException {
        try {lat = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lat");} catch(Exception e){}
        try {lng = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lng");} catch(Exception e){}
        try {name = jsonObj.getString("name");} catch(Exception e){}
        try {vicinity = jsonObj.getString("vicinity");} catch(Exception e){}
	}

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
