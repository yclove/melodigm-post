package com.melodigm.post.protocol.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AppVersionRes {
    private int APP_VER = 0;
    private String CPS_UPD_YN = "";
    private String APP_VERNM = "";
    private String LIKE_ICI = "";
    private String COUNTRY_CODE = "";
    private String POST_INTRO_BG = "";
    private ArrayList<ColorItem> arrColorItem = new ArrayList<>();
    private ArrayList<PostHashTagKeyWordItem> arrPostHashTagKeyWordItem = new ArrayList<>();
    private ArrayList<MusPopKeyWordItem> arrPostPopKeyWordItem = new ArrayList<>();
    private ArrayList<MusPopKeyWordItem> arrMusPopKeyWordItem = new ArrayList<>();
    private ArrayList<NotiSettingItem> arrNotiSettingItem = new ArrayList<>();

    public ArrayList<ColorItem> getColorItemList() {
        return arrColorItem;
    }

    public ArrayList<PostHashTagKeyWordItem> getArrPostHashTagKeyWordItem() {
        return arrPostHashTagKeyWordItem;
    }

    public ArrayList<MusPopKeyWordItem> getArrPostPopKeyWordItem() {
        return arrPostPopKeyWordItem;
    }

    public ArrayList<MusPopKeyWordItem> getArrMusPopKeyWordItem() {
        return arrMusPopKeyWordItem;
    }

    public ArrayList<NotiSettingItem> getArrNotiSettingItem() {
        return arrNotiSettingItem;
    }

    public AppVersionRes(JSONObject jsonObj) throws JSONException {
        try{APP_VER = jsonObj.getInt("APP_VER");} catch(Exception e){}
        try{CPS_UPD_YN = jsonObj.getString("CPS_UPD_YN");} catch(Exception e){}
        try{APP_VERNM = jsonObj.getString("APP_VERNM");} catch(Exception e){}
        try{LIKE_ICI = jsonObj.getString("LIKE_ICI");} catch(Exception e){}
        try{COUNTRY_CODE = jsonObj.getString("COUNTRY_CODE");} catch(Exception e){}
        try{POST_INTRO_BG = jsonObj.getString("POST_INTRO_BG");} catch(Exception e){}

        try {
            JSONArray DATA = jsonObj.getJSONArray("COLOR");
            if (DATA != null) {
                for (int i = 0; i < DATA.length(); i++) {
                    arrColorItem.add(new ColorItem((JSONObject)DATA.get(i)));
                }
            }
        } catch (Exception e) {}

        try {
            JSONArray HASH_POP_KEYWORD = jsonObj.getJSONArray("HASH_POP_KEYWORD");
            if (HASH_POP_KEYWORD != null) {
                for (int i = 0; i < HASH_POP_KEYWORD.length(); i++) {
                    arrPostHashTagKeyWordItem.add(new PostHashTagKeyWordItem((JSONObject)HASH_POP_KEYWORD.get(i)));
                }
            }
        } catch (Exception e) {}

        try {
            JSONArray POST_POP_KEYWORD = jsonObj.getJSONArray("POST_POP_KEYWORD");
            if (POST_POP_KEYWORD != null) {
                for (int i = 0; i < POST_POP_KEYWORD.length(); i++) {
                    arrPostPopKeyWordItem.add(new MusPopKeyWordItem((JSONObject)POST_POP_KEYWORD.get(i)));
                }
            }
        } catch (Exception e) {}

        try {
            JSONArray MUS_POP_KEYWORD = jsonObj.getJSONArray("MUS_POP_KEYWORD");
            if (MUS_POP_KEYWORD != null) {
                for (int i = 0; i < MUS_POP_KEYWORD.length(); i++) {
                    arrMusPopKeyWordItem.add(new MusPopKeyWordItem((JSONObject)MUS_POP_KEYWORD.get(i)));
                }
            }
        } catch (Exception e) {}

        try {
            JSONArray NOTI_SETTING = jsonObj.getJSONArray("NOTI_SETTING");
            if (NOTI_SETTING != null) {
                for (int i = 0; i < NOTI_SETTING.length(); i++) {
                    arrNotiSettingItem.add(new NotiSettingItem((JSONObject)NOTI_SETTING.get(i)));
                }
            }
        } catch (Exception e) {}
	}

    public int getAPP_VER() {
        return APP_VER;
    }

    public void setAPP_VER(int APP_VER) {
        this.APP_VER = APP_VER;
    }

    public String getCPS_UPD_YN() {
        return CPS_UPD_YN;
    }

    public void setCPS_UPD_YN(String CPS_UPD_YN) {
        this.CPS_UPD_YN = CPS_UPD_YN;
    }

    public String getAPP_VERNM() {
        return APP_VERNM;
    }

    public void setAPP_VERNM(String APP_VERNM) {
        this.APP_VERNM = APP_VERNM;
    }

    public String getLIKE_ICI() {
        return LIKE_ICI;
    }

    public void setLIKE_ICI(String LIKE_ICI) {
        this.LIKE_ICI = LIKE_ICI;
    }

    public String getCOUNTRY_CODE() {
        return COUNTRY_CODE;
    }

    public void setCOUNTRY_CODE(String COUNTRY_CODE) {
        this.COUNTRY_CODE = COUNTRY_CODE;
    }

    public String getPOST_INTRO_BG() {
        return POST_INTRO_BG;
    }

    public void setPOST_INTRO_BG(String POST_INTRO_BG) {
        this.POST_INTRO_BG = POST_INTRO_BG;
    }
}
