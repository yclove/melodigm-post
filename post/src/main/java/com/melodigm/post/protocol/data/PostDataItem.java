package com.melodigm.post.protocol.data;

import android.net.ParseException;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.google.gson.JsonParseException;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.parallaxscroll.ParallaxImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PostDataItem implements Parcelable {
    private String POST_TYPE = "";
    private String KWD = "";
    private String EMOTICON = "";
    private String POST_PST_TYPE = "";
    private String AGE_ZONE = "";
    private String RADIO_PATH = "";
    private String BG_PIC_PATH = "";
    private String BG_USER_PATH = "";
    private String BG_MAP_PATH = "";
    private String POI = "";
    private String POST_SUBJ = "";
    private String POST_CONT = "";
    private String COLOR = "";
    private String COLOR_HEX = "";
    private String PLACE = "";
    private String OST_REG_YN = "";
    private String REG_DATE = "";
    private int LIKE_CNT = 0;
    private int OST_CNT = 0;
    private int DCRE_CNT = 0;
    private String LIKE_TOGGLE_YN = "";
    private String UAI = "";
    private String DCRE_TOGGLE_YN = "";
    private String OTI = "";
    private String SSI = "";
    private String TITLE_ALBUM_PATH = "";
    private String TITLE_ARTI_NM = "";
    private String TITLE_SONG_NM = "";
    private int RADIO_RUNTIME = 0;
    private SeekBar mSeekbar = null;
    private LetterSpacingTextView tvRadioPlay = null;
    private ImageView ivRadioPlay = null;
    private LetterSpacingTextView tvRadioPlayDuration = null;
    private ParallaxImageView ivRoot = null;
    private LinearLayout llPostLikeBtn = null;

    public PostDataItem(Parcel in) {
        POST_TYPE = in.readString();
        KWD = in.readString();
        EMOTICON = in.readString();
        POST_PST_TYPE = in.readString();
        AGE_ZONE = in.readString();
        RADIO_PATH = in.readString();
        BG_PIC_PATH = in.readString();
        BG_USER_PATH = in.readString();
        BG_MAP_PATH = in.readString();
        POI = in.readString();
        POST_SUBJ = in.readString();
        POST_CONT = in.readString();
        COLOR = in.readString();
        COLOR_HEX = in.readString();
        PLACE = in.readString();
        OST_REG_YN = in.readString();
        REG_DATE = in.readString();
        LIKE_CNT = in.readInt();
        OST_CNT = in.readInt();
        DCRE_CNT = in.readInt();
        LIKE_TOGGLE_YN = in.readString();
        UAI = in.readString();
        DCRE_TOGGLE_YN = in.readString();
        OTI = in.readString();
        SSI = in.readString();
        TITLE_ALBUM_PATH = in.readString();
        TITLE_ARTI_NM = in.readString();
        TITLE_SONG_NM = in.readString();
        RADIO_RUNTIME = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(POST_TYPE);
        dest.writeString(KWD);
        dest.writeString(EMOTICON);
        dest.writeString(POST_PST_TYPE);
        dest.writeString(AGE_ZONE);
        dest.writeString(RADIO_PATH);
        dest.writeString(BG_PIC_PATH);
        dest.writeString(BG_USER_PATH);
        dest.writeString(BG_MAP_PATH);
        dest.writeString(POI);
        dest.writeString(POST_SUBJ);
        dest.writeString(POST_CONT);
        dest.writeString(COLOR);
        dest.writeString(COLOR_HEX);
        dest.writeString(PLACE);
        dest.writeString(OST_REG_YN);
        dest.writeString(REG_DATE);
        dest.writeInt(LIKE_CNT);
        dest.writeInt(OST_CNT);
        dest.writeInt(DCRE_CNT);
        dest.writeString(LIKE_TOGGLE_YN);
        dest.writeString(UAI);
        dest.writeString(DCRE_TOGGLE_YN);
        dest.writeString(OTI);
        dest.writeString(SSI);
        dest.writeString(TITLE_ALBUM_PATH);
        dest.writeString(TITLE_ARTI_NM);
        dest.writeString(TITLE_SONG_NM);
        dest.writeInt(RADIO_RUNTIME);
    }

    public static final Parcelable.Creator<PostDataItem> CREATOR = new Parcelable.Creator<PostDataItem>() {
        public PostDataItem createFromParcel(Parcel in) {
            return new PostDataItem(in);
        }

        public PostDataItem[] newArray(int size) {
            return new PostDataItem[size];
        }
    };

    public PostDataItem(JSONObject jsonObj) throws JSONException {
        try {POST_TYPE = jsonObj.getString("POST_TYPE");} catch(Exception e){}
        try {KWD = jsonObj.getString("KWD");} catch(Exception e){}
        try {POST_PST_TYPE = jsonObj.getString("POST_PST_TYPE");} catch(Exception e){}
        try {AGE_ZONE = jsonObj.getString("AGE_ZONE");} catch(Exception e){}
        try {POI = jsonObj.getString("POI");} catch(Exception e){}
        try {POST_SUBJ = jsonObj.getString("POST_SUBJ");} catch(Exception e){}
        try {POST_CONT = jsonObj.getString("POST_CONT");} catch(Exception e){}
        try {PLACE = jsonObj.getString("PLACE");} catch(Exception e){}
        try {REG_DATE = jsonObj.getString("REG_DATE");} catch(Exception e){}
        try {DCRE_CNT = jsonObj.getInt("DCRE_CNT");} catch(Exception e){}
        try {UAI = jsonObj.getString("UAI");} catch(Exception e){}
        try {DCRE_TOGGLE_YN = jsonObj.getString("DCRE_TOGGLE_YN");} catch(Exception e){}

        try {
            JSONObject OST_DATA = jsonObj.getJSONObject("OST_DATA");
            try {OTI = OST_DATA.getString("OTI");} catch(Exception e){}
            try {OST_REG_YN = OST_DATA.getString("OST_REG_YN");} catch(Exception e){}
            try {SSI = OST_DATA.getString("TITL_SSI");} catch(Exception e){}
            try {TITLE_SONG_NM = OST_DATA.getString("TITL_SONG_NM");} catch(Exception e){}
            try {TITLE_ARTI_NM = OST_DATA.getString("TITL_ARTI_NM");} catch(Exception e){}
            try {TITLE_ALBUM_PATH = OST_DATA.getString("TITL_ALBUM_PATH");} catch(Exception e){}
            try {OST_CNT = OST_DATA.getInt("OST_CNT");} catch(Exception e){}
        } catch (Exception e) {}

        try {
            JSONObject RES_DATA = jsonObj.getJSONObject("RES_DATA");
            try {RADIO_PATH = RES_DATA.getString("RADIO_PATH");} catch(Exception e){}
            try {BG_PIC_PATH = RES_DATA.getString("BG_PIC_PATH");} catch(Exception e){}
            try {BG_USER_PATH = RES_DATA.getString("BG_USER_PATH");} catch(Exception e){}
            try {BG_MAP_PATH = RES_DATA.getString("BG_MAP_PATH");} catch(Exception e){}
            try {RADIO_RUNTIME = RES_DATA.getInt("RADIO_RUNTIME");} catch(Exception e){}
        } catch (Exception e) {}

        try {
            JSONObject ICON_DATA = jsonObj.getJSONObject("ICON_DATA");
            try {COLOR = ICON_DATA.getString("COLOR");} catch(Exception e){}
            try {COLOR_HEX = ICON_DATA.getString("COLOR_HEX");} catch(Exception e){}
            try {EMOTICON = ICON_DATA.getString("EMOTICON");} catch(Exception e){}
            try {LIKE_CNT = ICON_DATA.getInt("LIKE_CNT");} catch(Exception e){}
            try {LIKE_TOGGLE_YN = ICON_DATA.getString("LIKE_TOGGLE_YN");} catch(Exception e){}
        } catch (Exception e) {}
    }

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getKWD() {
        return KWD;
    }

    public void setKWD(String KWD) {
        this.KWD = KWD;
    }

    public String getEMOTICON() {
        return EMOTICON;
    }

    public void setEMOTICON(String EMOTICON) {
        this.EMOTICON = EMOTICON;
    }

    public String getPOST_PST_TYPE() {
        return POST_PST_TYPE;
    }

    public void setPOST_PST_TYPE(String POST_PST_TYPE) {
        this.POST_PST_TYPE = POST_PST_TYPE;
    }

    public String getAGE_ZONE() {
        return AGE_ZONE;
    }

    public void setAGE_ZONE(String AGE_ZONE) {
        this.AGE_ZONE = AGE_ZONE;
    }

    public String getRADIO_PATH() {
        return RADIO_PATH;
    }

    public void setRADIO_PATH(String RADIO_PATH) {
        this.RADIO_PATH = RADIO_PATH;
    }

    public String getBG_PIC_PATH() {
        return BG_PIC_PATH;
    }

    public void setBG_PIC_PATH(String BG_PIC_PATH) {
        this.BG_PIC_PATH = BG_PIC_PATH;
    }

    public String getBG_USER_PATH() {
        return BG_USER_PATH;
    }

    public void setBG_USER_PATH(String BG_USER_PATH) {
        this.BG_USER_PATH = BG_USER_PATH;
    }

    public String getBG_MAP_PATH() {
        return BG_MAP_PATH;
    }

    public void setBG_MAP_PATH(String BG_MAP_PATH) {
        this.BG_MAP_PATH = BG_MAP_PATH;
    }

    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }

    public String getPOST_SUBJ() {
        return POST_SUBJ;
    }

    public void setPOST_SUBJ(String POST_SUBJ) {
        this.POST_SUBJ = POST_SUBJ;
    }

    public String getPOST_CONT() {
        return POST_CONT;
    }

    public void setPOST_CONT(String POST_CONT) {
        this.POST_CONT = POST_CONT;
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

    public String getPLACE() {
        return PLACE;
    }

    public void setPLACE(String PLACE) {
        this.PLACE = PLACE;
    }

    public String getOST_REG_YN() {
        return OST_REG_YN;
    }

    public void setOST_REG_YN(String OST_REG_YN) {
        this.OST_REG_YN = OST_REG_YN;
    }

    public String getREG_DATE() {
        return REG_DATE;
    }

    public void setREG_DATE(String REG_DATE) {
        this.REG_DATE = REG_DATE;
    }

    public int getLIKE_CNT() {
        return LIKE_CNT;
    }

    public void setLIKE_CNT(int LIKE_CNT) {
        this.LIKE_CNT = LIKE_CNT;
    }

    public int getOST_CNT() {
        return OST_CNT;
    }

    public void setOST_CNT(int OST_CNT) {
        this.OST_CNT = OST_CNT;
    }

    public int getDCRE_CNT() {
        return DCRE_CNT;
    }

    public void setDCRE_CNT(int DCRE_CNT) {
        this.DCRE_CNT = DCRE_CNT;
    }

    public String getLIKE_TOGGLE_YN() {
        return LIKE_TOGGLE_YN;
    }

    public void setLIKE_TOGGLE_YN(String LIKE_TOGGLE_YN) {
        this.LIKE_TOGGLE_YN = LIKE_TOGGLE_YN;
    }

    public String getUAI() {
        return UAI;
    }

    public void setUAI(String UAI) {
        this.UAI = UAI;
    }

    public String getDCRE_TOGGLE_YN() {
        return DCRE_TOGGLE_YN;
    }

    public void setDCRE_TOGGLE_YN(String DCRE_TOGGLE_YN) {
        this.DCRE_TOGGLE_YN = DCRE_TOGGLE_YN;
    }

    public String getOTI() {
        return OTI;
    }

    public void setOTI(String OTI) {
        this.OTI = OTI;
    }

    public String getSSI() {
        return SSI;
    }

    public void setSSI(String SSI) {
        this.SSI = SSI;
    }

    public String getTITLE_ALBUM_PATH() {
        return TITLE_ALBUM_PATH;
    }

    public void setTITLE_ALBUM_PATH(String TITLE_ALBUM_PATH) {
        this.TITLE_ALBUM_PATH = TITLE_ALBUM_PATH;
    }

    public String getTITLE_ARTI_NM() {
        return TITLE_ARTI_NM;
    }

    public void setTITLE_ARTI_NM(String TITLE_ARTI_NM) {
        this.TITLE_ARTI_NM = TITLE_ARTI_NM;
    }

    public String getTITLE_SONG_NM() {
        return TITLE_SONG_NM;
    }

    public void setTITLE_SONG_NM(String TITLE_SONG_NM) {
        this.TITLE_SONG_NM = TITLE_SONG_NM;
    }

    public int getRADIO_RUNTIME() {
        return RADIO_RUNTIME;
    }

    public void setRADIO_RUNTIME(int RADIO_RUNTIME) {
        this.RADIO_RUNTIME = RADIO_RUNTIME;
    }

    public SeekBar getSeekbar() {
        return mSeekbar;
    }

    public void setSeekbar(SeekBar seekbar) {
        this.mSeekbar = seekbar;
    }

    public LetterSpacingTextView getTvRadioPlay() {
        return tvRadioPlay;
    }

    public void setTvRadioPlay(LetterSpacingTextView tvRadioPlay) {
        this.tvRadioPlay = tvRadioPlay;
    }

    public ImageView getIvRadioPlay() {
        return ivRadioPlay;
    }

    public void setIvRadioPlay(ImageView ivRadioPlay) {
        this.ivRadioPlay = ivRadioPlay;
    }

    public LetterSpacingTextView getTvRadioPlayDuration() {
        return tvRadioPlayDuration;
    }

    public void setTvRadioPlayDuration(LetterSpacingTextView tvRadioPlayDuration) {
        this.tvRadioPlayDuration = tvRadioPlayDuration;
    }

    public ParallaxImageView getIvRoot() {
        return ivRoot;
    }

    public void setIvRoot(ParallaxImageView ivRoot) {
        this.ivRoot = ivRoot;
    }

    public LinearLayout getLlPostLikeBtn() {
        return llPostLikeBtn;
    }

    public void setLlPostLikeBtn(LinearLayout llPostLikeBtn) {
        this.llPostLikeBtn = llPostLikeBtn;
    }
}
