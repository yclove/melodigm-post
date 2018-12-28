package com.melodigm.post.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.data.CabinetMusicDataItem;
import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.protocol.data.MusPopKeyWordItem;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.data.PostHashTagKeyWordItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PostDatabases {
    private final static String DB_NAME = "POST";
    private final static int DB_VERSION = 15;
    private Context mContext;
    private DBOpenHelper mDbHelper;
    private SQLiteDatabase mDb;

    public PostDatabases(Context context) {
        mContext = context;
    }

    public class DBOpenHelper extends SQLiteOpenHelper {
        public DBOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL(CREATE_SORT_TABLE);
                db.execSQL(CREATE_POST_SEARCH_RECENT_TABLE);
                db.execSQL(CREATE_SEARCH_RECENT_TABLE);
                db.execSQL(CREATE_OST_PLAY_LIST_TABLE);
                db.execSQL(CREATE_OST_PLAY_HISTORY_TABLE);
                db.execSQL(CREATE_POST_HASHTAG_KEYWORD_TABLE);
                db.execSQL(CREATE_POST_POP_KEYWORD_TABLE);
                db.execSQL(CREATE_MUS_POP_KEYWORD_TABLE);
                db.execSQL(CREATE_POST_COLOR_TABLE);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            LogUtil.e("DataBase Update !!!");
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_SORT);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_POST_SEARCH_RECENT);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_SEARCH_RECENT);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_OST_PLAY_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_OST_PLAY_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_POST_POP_KEYWORD);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_MUS_POP_KEYWORD);
            db.execSQL("DROP TABLE IF EXISTS " + PostDBInfo.DB_TABLE_POST_COLOR);
            db.beginTransaction();
            try {
                db.execSQL(CREATE_SORT_TABLE);
                db.execSQL(CREATE_POST_SEARCH_RECENT_TABLE);
                db.execSQL(CREATE_SEARCH_RECENT_TABLE);
                db.execSQL(CREATE_OST_PLAY_LIST_TABLE);
                db.execSQL(CREATE_OST_PLAY_HISTORY_TABLE);
                db.execSQL(CREATE_POST_HASHTAG_KEYWORD_TABLE);
                db.execSQL(CREATE_POST_POP_KEYWORD_TABLE);
                db.execSQL(CREATE_MUS_POP_KEYWORD_TABLE);
                db.execSQL(CREATE_POST_COLOR_TABLE);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
    }

    public DBOpenHelper open() {
        mDbHelper = new DBOpenHelper(mContext);
        try {
            mDb = mDbHelper.getWritableDatabase();
        } catch (Exception ex) {
            mDb = mDbHelper.getReadableDatabase();
        }
        return mDbHelper;
    }

    public void close() {
        mDbHelper.close();
    }

    /* SORTING 저장 테이블 생성문 */
    private final static String CREATE_SORT_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_SORT +
            "("+ PostDBInfo.COLUMN_SORT_TYPE + " varchar(255), "
            + PostDBInfo.COLUMN_SORT_GENDER + " varchar(255), "
            + PostDBInfo.COLUMN_SORT_GENERATION + " varchar(255), "
            + PostDBInfo.COLUMN_SORT_TIME + " varchar(255), "
            + PostDBInfo.COLUMN_SORT_DISTANCE + " varchar(255), "
            + PostDBInfo.COLUMN_SORT_DATE + " varchar(255)" +" )";

    /* 최근 이야기 검색어 저장 테이블 생성문 */
    private final static String CREATE_POST_SEARCH_RECENT_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_POST_SEARCH_RECENT +
            "("+ PostDBInfo.COLUMN_POST_WORD + " varchar(255), "
            + PostDBInfo.COLUMN_POST_SEARCH_DATE + " varchar(255)" +" )";

    /* 최근 검색어 저장 테이블 생성문 */
    private final static String CREATE_SEARCH_RECENT_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_SEARCH_RECENT +
            "("+ PostDBInfo.COLUMN_WORD + " varchar(255), "
            + PostDBInfo.COLUMN_SEARCH_DATE + " varchar(255)" +" )";

    /* OST 재생 목록 테이블 생성문 */
    private final static String CREATE_OST_PLAY_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_OST_PLAY_LIST +
            "("+ PostDBInfo.COLUMN_OST_POST_TYPE + " varchar(255), "
            + PostDBInfo.COLUMN_OST_OTI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_POI_SSI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_POI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_SSI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_TITLE_STATE + " varchar(255), "
            + PostDBInfo.COLUMN_OST_COLOR_HEX + " varchar(255), "
            + PostDBInfo.COLUMN_OST_ALBUM_PATH + " varchar(255), "
            + PostDBInfo.COLUMN_OST_RADIO_PATH + " varchar(255), "
            + PostDBInfo.COLUMN_OST_SONG_NM + " varchar(255), "
            + PostDBInfo.COLUMN_OST_ARTI_NM + " varchar(255), "
            + PostDBInfo.COLUMN_OST_ADD_DATE + " varchar(255)" +" )";

    /* OST 재생 히스토리 테이블 생성문 */
    private final static String CREATE_OST_PLAY_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_OST_PLAY_HISTORY +
            "("+ PostDBInfo.COLUMN_OST_POST_TYPE + " varchar(255), "
            + PostDBInfo.COLUMN_OST_OTI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_POI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_SSI + " varchar(255), "
            + PostDBInfo.COLUMN_OST_TITLE_STATE + " varchar(255), "
            + PostDBInfo.COLUMN_OST_COLOR_HEX + " varchar(255), "
            + PostDBInfo.COLUMN_OST_ALBUM_PATH + " varchar(255), "
            + PostDBInfo.COLUMN_OST_RADIO_PATH + " varchar(255), "
            + PostDBInfo.COLUMN_OST_SONG_NM + " varchar(255), "
            + PostDBInfo.COLUMN_OST_ARTI_NM + " varchar(255), "
            + PostDBInfo.COLUMN_OST_ADD_DATE + " varchar(255)" +" )";

    /* 인기 해시태그 검색어 테이블 생성문 */
    private final static String CREATE_POST_HASHTAG_KEYWORD_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD +
            "("+ PostDBInfo.COLUMN_POST_HASHTAG_RANKING + " int, "
            + PostDBInfo.COLUMN_POST_HASHTAG_KEYWORD + " varchar(255), "
            + PostDBInfo.COLUMN_POST_HASHTAG_COUNT + " int)";

    /* 인기 이야기 검색어 테이블 생성문 */
    private final static String CREATE_POST_POP_KEYWORD_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_POST_POP_KEYWORD +
            "("+ PostDBInfo.COLUMN_POST_POP_RANKING + " int, "
            + PostDBInfo.COLUMN_POST_POP_KEYWORD + " varchar(255))";

    /* 인기 노래 검색어 테이블 생성문 */
    private final static String CREATE_MUS_POP_KEYWORD_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_MUS_POP_KEYWORD +
            "("+ PostDBInfo.COLUMN_MUS_POP_RANKING + " int, "
            + PostDBInfo.COLUMN_MUS_POP_KEYWORD + " varchar(255))";

    /* 컬러 테이블 생성문 */
    private final static String CREATE_POST_COLOR_TABLE = "CREATE TABLE IF NOT EXISTS " + PostDBInfo.DB_TABLE_POST_COLOR +
            "("+ PostDBInfo.COLUMN_POST_COLOR_ICI + " varchar(255), "
            + PostDBInfo.COLUMN_POST_COLOR_CODE + " varchar(255), "
            + PostDBInfo.COLUMN_POST_COLOR_SORT_ORDER + " int, "
            + PostDBInfo.COLUMN_POST_COLOR_ICON_NM + " varchar(255))";

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      Sorting 관련 API                                       //
    ////////////////////////////////////////////////////////////////////////////////////

    public HashMap<String, String> getSortData(String sortType) {
        HashMap<String, String> result =  new HashMap<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_SORT, null, PostDBInfo.COLUMN_SORT_TYPE + " = ?", new String[]{sortType}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    String GENDER = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_SORT_GENDER));
                    String GENERATION = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_SORT_GENERATION));
                    String TIME = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_SORT_TIME));
                    String DISTANCE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_SORT_DISTANCE));

                    if (GENDER == null) GENDER = "";
                    if (GENERATION == null) GENERATION = "";
                    if (TIME == null) TIME = "";
                    if (DISTANCE == null) DISTANCE = "";

                    result.put("GENDER", GENDER);
                    result.put("GENERATION", GENERATION);
                    result.put("TIME", TIME);
                    result.put("DISTANCE", DISTANCE);
                }
            } else {
                result.put("GENDER", "");
                result.put("GENERATION", "");
                result.put("TIME", "");
                result.put("DISTANCE", "");
            }
            cursor.close();
        }

        return result;
    }

    public void setSortData(String sortType, HashMap<String, String> sortData) {
        boolean isExsit = false;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_SORT, null, PostDBInfo.COLUMN_SORT_TYPE + " = ?", new String[]{sortType}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0)
                isExsit = true;
            cursor.close();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        String sortDate = simpleDateFormat.format(new Date(now));

        if (isExsit) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PostDBInfo.COLUMN_SORT_GENDER, sortData.get("GENDER"));
            contentValues.put(PostDBInfo.COLUMN_SORT_GENERATION, sortData.get("GENERATION"));
            contentValues.put(PostDBInfo.COLUMN_SORT_TIME, sortData.get("TIME"));
            contentValues.put(PostDBInfo.COLUMN_SORT_DISTANCE, sortData.get("DISTANCE"));
            contentValues.put(PostDBInfo.COLUMN_SORT_DATE, sortDate);
            mDb.update(PostDBInfo.DB_TABLE_SORT, contentValues, PostDBInfo.COLUMN_SORT_TYPE + " = ?", new String[]{sortType});
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PostDBInfo.COLUMN_SORT_TYPE, sortType);
            contentValues.put(PostDBInfo.COLUMN_SORT_GENDER, sortData.get("GENDER"));
            contentValues.put(PostDBInfo.COLUMN_SORT_GENERATION, sortData.get("GENERATION"));
            contentValues.put(PostDBInfo.COLUMN_SORT_TIME, sortData.get("TIME"));
            contentValues.put(PostDBInfo.COLUMN_SORT_DISTANCE, sortData.get("DISTANCE"));
            contentValues.put(PostDBInfo.COLUMN_SORT_DATE, sortDate);
            mDb.insert(PostDBInfo.DB_TABLE_SORT, null, contentValues);
        }

        if (Constants.DEBUG_DATABASE) {
            cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_SORT + " ORDER BY " + PostDBInfo.COLUMN_SORT_DATE + " DESC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_SORT);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      최근 이야기 검색어 관련 API                          //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 최근 검색어 모두 삭제
     */
    public void deleteAllPostRecentSearchWords() {
        mDb.delete(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT, null, null);
    }

    /**
     * 최근 검색어 갯수 가져오기
     * @return 최근검색어 갯수
     */
    public int getPostRecentSearchWordsCount(){
        int result =  0;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT,null,null, null, null, null, null);
        if(cursor != null){
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    /**
     * 최근 검색어 마지막 검색날짜 정렬순으로 가져오기
     * @return 최근검색어 목록
     */
    public ArrayList<String> getAllPostRecentSearchWords(){
        ArrayList<String> result =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT,null,null, null, null, null, PostDBInfo.COLUMN_POST_SEARCH_DATE + " DESC");
        if(cursor != null){
            while(cursor.moveToNext()){
                String word = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_WORD));
                result.add(word);
            }
            cursor.close();
        }

        return result;
    }

    /**
     * 최근 검색어 삭제하기
     */
    public void deletePostRecentSearchWord(String search){
        //해당 검색어 삭제하기!!
        mDb.delete(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT, PostDBInfo.COLUMN_POST_WORD + " = '" +search + "'", null);
    }

    /**
     * 최근 검색어로 저장하기
     * @return 최근검색어 목록
     */
    public void updatePostSearchWord(String search){
        boolean isExsit = false;
        //아이템이 있는지 검사!!
        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT,null,PostDBInfo.COLUMN_POST_WORD+"=?",new String[]{search}, null, null, null);
        if(cursor != null){
            if(cursor.getCount() > 0)isExsit = true;
            cursor.close();
        }

        SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        String strNow = DATA_FORMAT.format(new Date(now));

        if(isExsit){
            //아이템이 있다면, 날짜만 갱신!!
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_POST_WORD, search);
            val.put(PostDBInfo.COLUMN_POST_SEARCH_DATE, strNow);
            mDb.update(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT, val, PostDBInfo.COLUMN_POST_WORD+"=?", new String[]{search});
        } else {
            //아이템이 없다면, 추가!!
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_POST_WORD, search);
            val.put(PostDBInfo.COLUMN_POST_SEARCH_DATE, strNow);
            mDb.insert(PostDBInfo.DB_TABLE_POST_SEARCH_RECENT, null, val);
        }

        //10개 이상 데이터는 삭제!!
        //Delete From SEARCH_RECENT_WORD Where word
        //in (Select word From SEARCH_RECENT_WORD
        //order by search_date DESC LIMIT 100  OFFSET 10)
        String strSQL = "DELETE FROM " + PostDBInfo.DB_TABLE_POST_SEARCH_RECENT + " WHERE " + PostDBInfo.COLUMN_POST_WORD +
                " IN (SELECT " + PostDBInfo.COLUMN_POST_WORD + " FROM " +  PostDBInfo.DB_TABLE_POST_SEARCH_RECENT +
                " ORDER BY " + PostDBInfo.COLUMN_POST_SEARCH_DATE + " DESC LIMIT 100 OFFSET 20)";
        mDb.execSQL(strSQL);

        if (Constants.DEBUG_DATABASE) {
            cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_POST_SEARCH_RECENT + " ORDER BY " + PostDBInfo.COLUMN_POST_SEARCH_DATE + " DESC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_POST_SEARCH_RECENT);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      최근 노래 검색어 관련 API                          //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 최근 검색어 모두 삭제
     */
    public void deleteAllRecentSearchWords() {
        mDb.delete(PostDBInfo.DB_TABLE_SEARCH_RECENT, null, null);
    }

    /**
     * 최근 검색어 갯수 가져오기
     * @return 최근검색어 갯수
     */
    public int getRecentSearchWordsCount(){
        int result =  0;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_SEARCH_RECENT,null,null, null, null, null, null);
        if(cursor != null){
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    /**
     * 최근 검색어 마지막 검색날짜 정렬순으로 가져오기
     * @return 최근검색어 목록
     */
    public ArrayList<String> getAllRecentSearchWords(){
        ArrayList<String> result =  new ArrayList<String>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_SEARCH_RECENT,null,null, null, null, null, PostDBInfo.COLUMN_SEARCH_DATE + " DESC");
        if(cursor != null){
            while(cursor.moveToNext()){
                String word = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_WORD));
                result.add(word);
            }
            cursor.close();
        }

        return result;
    }

    /**
     * 최근 검색어 삭제하기
     */
    public void deleteRecentSearchWord(String search){
        //해당 검색어 삭제하기!!
        mDb.delete(PostDBInfo.DB_TABLE_SEARCH_RECENT, PostDBInfo.COLUMN_WORD + " = '" +search + "'", null);
    }

    /**
     * 최근 검색어로 저장하기
     * @return 최근검색어 목록
     */
    public void updateSearchWord(String search){
        boolean isExsit = false;
        //아이템이 있는지 검사!!
        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_SEARCH_RECENT,null,PostDBInfo.COLUMN_WORD+"=?",new String[]{search}, null, null, null);
        if(cursor != null){
            if(cursor.getCount() > 0)isExsit = true;
            cursor.close();
        }

        SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        String strNow = DATA_FORMAT.format(new Date(now));

        if(isExsit){
            //아이템이 있다면, 날짜만 갱신!!
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_WORD, search);
            val.put(PostDBInfo.COLUMN_SEARCH_DATE, strNow);
            mDb.update(PostDBInfo.DB_TABLE_SEARCH_RECENT, val, PostDBInfo.COLUMN_WORD+"=?", new String[]{search});
        } else {
            //아이템이 없다면, 추가!!
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_WORD, search);
            val.put(PostDBInfo.COLUMN_SEARCH_DATE, strNow);
            mDb.insert(PostDBInfo.DB_TABLE_SEARCH_RECENT, null, val);
        }

        //10개 이상 데이터는 삭제!!
        //Delete From SEARCH_RECENT_WORD Where word
        //in (Select word From SEARCH_RECENT_WORD
        //order by search_date DESC LIMIT 100  OFFSET 10)
        String strSQL = "DELETE FROM " + PostDBInfo.DB_TABLE_SEARCH_RECENT + " WHERE " + PostDBInfo.COLUMN_WORD +
                " IN (SELECT " + PostDBInfo.COLUMN_WORD + " FROM " +  PostDBInfo.DB_TABLE_SEARCH_RECENT +
                " ORDER BY " + PostDBInfo.COLUMN_SEARCH_DATE + " DESC LIMIT 100 OFFSET 20)";
        mDb.execSQL(strSQL);

        if (Constants.DEBUG_DATABASE) {
            cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_SEARCH_RECENT + " ORDER BY " + PostDBInfo.COLUMN_SEARCH_DATE + " DESC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_SEARCH_RECENT);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      재생 목록 관련 API                                    //
    ////////////////////////////////////////////////////////////////////////////////////

    public ArrayList<OstDataItem> getOstPlayList(){
        ArrayList<OstDataItem> arrOstDataItem =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_OST_PLAY_LIST, null, null, null, null, null, PostDBInfo.COLUMN_OST_ADD_DATE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OstDataItem ostDataItem = new OstDataItem();
                String POST_TYPE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_POST_TYPE));
                String OTI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_OTI));
                String POI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_POI));
                String SSI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_SSI));
                String TITLE_STATE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_TITLE_STATE));
                String COLOR_HEX = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_COLOR_HEX));
                String ALBUM_PATH = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_ALBUM_PATH));
                String RADIO_PATH = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_RADIO_PATH));
                String SONG_NM = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_SONG_NM));
                String ARTI_NM = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_ARTI_NM));
                String ADD_DATE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_ADD_DATE));

                ostDataItem.setPOST_TYPE(POST_TYPE);
                ostDataItem.setOTI(OTI);
                ostDataItem.setPOI(POI);
                ostDataItem.setSSI(SSI);
                ostDataItem.setTITL_TOGGLE_YN(TITLE_STATE);
                ostDataItem.setCOLOR_HEX(COLOR_HEX);
                ostDataItem.setALBUM_PATH(ALBUM_PATH);
                ostDataItem.setRADIO_PATH(RADIO_PATH);
                ostDataItem.setSONG_NM(SONG_NM);
                ostDataItem.setARTI_NM(ARTI_NM);
                ostDataItem.setADD_DATE(ADD_DATE);
                arrOstDataItem.add(ostDataItem);
            }
            cursor.close();
        }

        return arrOstDataItem;
    }

    public int updateOstPlayList(Object item, String... args) {
        int updateCount = 0;
        String POST_TYPE, POI_SSI, POI, OTI, SSI, TITLE_STATE, COLOR_HEX, RADIO_PATH, ALBUM_PATH, ARTI_NM, SONG_NM;
        if (item instanceof PostDataItem) {
            POST_TYPE = ((PostDataItem)item).getPOST_TYPE();
            POI = ((PostDataItem)item).getPOI();
            OTI = ((PostDataItem)item).getOTI();
            SSI = ((PostDataItem)item).getSSI();
            TITLE_STATE = "Y";
            COLOR_HEX = ((PostDataItem)item).getCOLOR_HEX();
            RADIO_PATH = ((PostDataItem)item).getRADIO_PATH();
            ALBUM_PATH = ((PostDataItem)item).getTITLE_ALBUM_PATH();
            ARTI_NM = ((PostDataItem)item).getTITLE_ARTI_NM();
            SONG_NM = ((PostDataItem)item).getTITLE_SONG_NM();
            POI_SSI = POI + SSI;
        } else if (item instanceof OstDataItem) {
            POST_TYPE = ((OstDataItem) item).getPOST_TYPE();
            POI = ((OstDataItem) item).getPOI();
            OTI = ((OstDataItem) item).getOTI();
            SSI = ((OstDataItem) item).getSSI();
            TITLE_STATE = ((OstDataItem) item).getTITL_TOGGLE_YN();
            COLOR_HEX = ((OstDataItem) item).getCOLOR_HEX();
            RADIO_PATH = ((OstDataItem) item).getRADIO_PATH();
            ALBUM_PATH = ((OstDataItem) item).getALBUM_PATH();
            ARTI_NM = ((OstDataItem) item).getARTI_NM();
            SONG_NM = ((OstDataItem) item).getSONG_NM();
            POI_SSI = POI + SSI;
        } else if (item instanceof CabinetMusicDataItem) {
            POST_TYPE = ((CabinetMusicDataItem) item).getPOST_TYPE();
            POI = ((CabinetMusicDataItem) item).getPOI();
            OTI = ((CabinetMusicDataItem) item).getOTI();
            SSI = ((CabinetMusicDataItem) item).getSSI();
            TITLE_STATE = ((CabinetMusicDataItem) item).getTITL_TOGGLE_YN();
            COLOR_HEX = "";
            RADIO_PATH = "";
            ALBUM_PATH = ((CabinetMusicDataItem) item).getALBUM_PATH();
            ARTI_NM = ((CabinetMusicDataItem) item).getARTI_NM();
            SONG_NM = ((CabinetMusicDataItem) item).getSONG_NM();
            POI_SSI = POI + SSI;
        } else {
            return updateCount;
        }

        boolean isExsit = false;
        //아이템이 있는지 검사!!
        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_OST_PLAY_LIST,null,PostDBInfo.COLUMN_OST_POI + "=? AND " + PostDBInfo.COLUMN_OST_SSI + "=?",new String[]{POI, SSI}, null, null, null);
        if(cursor != null){
            if(cursor.getCount() > 0)isExsit = true;
            cursor.close();
        }

        SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
        String playerListAddPosition;
        if (args != null && args.length == 1 && args[0] instanceof String) {
            playerListAddPosition = args[0];
        } else {
            playerListAddPosition = SPUtil.getSharedPreference(mContext, Constants.SP_PLAYER_LIST_ADD_POSITION);
        }
        Calendar calendar = Calendar.getInstance();
        String ADD_DATE = "", TO_ADD_DATE = "";
        long now = 0;

        try {
            Cursor toCursor;
            if ("".equalsIgnoreCase(playerListAddPosition) || "BOTTOM".equalsIgnoreCase(playerListAddPosition)) {
                toCursor =  mDb.query(PostDBInfo.DB_TABLE_OST_PLAY_LIST, null, null, null, null, null, PostDBInfo.COLUMN_OST_ADD_DATE + " DESC", "1");
                if (toCursor != null) {
                    while (toCursor.moveToNext()) {
                        TO_ADD_DATE = toCursor.getString(toCursor.getColumnIndex(PostDBInfo.COLUMN_OST_ADD_DATE));
                    }
                    toCursor.close();
                }

                if (CommonUtil.isNotNull(TO_ADD_DATE)) {
                    calendar.setTime(DATA_FORMAT.parse(TO_ADD_DATE));
                    calendar.add(Calendar.SECOND, +1);
                }
            }
            else if ("NEXT".equalsIgnoreCase(playerListAddPosition)) {
                ArrayList<OstDataItem> arrOstDataItems = getOstPlayList();
                if (arrOstDataItems.size() > 0 && arrOstDataItems.size() > PlayerConstants.SONG_NUMBER) {
                    toCursor = mDb.query(PostDBInfo.DB_TABLE_OST_PLAY_LIST,null,PostDBInfo.COLUMN_OST_POI + "=? AND " + PostDBInfo.COLUMN_OST_SSI + "=?",new String[]{arrOstDataItems.get(PlayerConstants.SONG_NUMBER).getPOI(), arrOstDataItems.get(PlayerConstants.SONG_NUMBER).getSSI()}, null, null, null, "1");
                    if (toCursor != null) {
                        while (toCursor.moveToNext()) {
                            TO_ADD_DATE = toCursor.getString(toCursor.getColumnIndex(PostDBInfo.COLUMN_OST_ADD_DATE));
                        }
                        toCursor.close();
                    }

                    if (CommonUtil.isNotNull(TO_ADD_DATE)) {
                        calendar.setTime(DATA_FORMAT.parse(TO_ADD_DATE));
                        calendar.add(Calendar.SECOND, +1);

                        String strSQL = "UPDATE " + PostDBInfo.DB_TABLE_OST_PLAY_LIST +
                                " SET " + PostDBInfo.COLUMN_OST_ADD_DATE + " = " + PostDBInfo.COLUMN_OST_ADD_DATE + " + 1 " +
                                " WHERE " + PostDBInfo.COLUMN_OST_ADD_DATE + " > " + TO_ADD_DATE;
                        LogUtil.e(strSQL);
                        mDb.execSQL(strSQL);
                    }
                }
            }
            else if ("TOP".equalsIgnoreCase(playerListAddPosition)) {
                toCursor =  mDb.query(PostDBInfo.DB_TABLE_OST_PLAY_LIST, null, null, null, null, null, PostDBInfo.COLUMN_OST_ADD_DATE + " ASC", "1");
                if (toCursor != null) {
                    while (toCursor.moveToNext()) {
                        TO_ADD_DATE = toCursor.getString(toCursor.getColumnIndex(PostDBInfo.COLUMN_OST_ADD_DATE));
                    }
                    toCursor.close();
                }

                if (CommonUtil.isNotNull(TO_ADD_DATE)) {
                    calendar.setTime(DATA_FORMAT.parse(TO_ADD_DATE));
                    calendar.add(Calendar.SECOND, -1);
                }
            }
        } catch (Exception e) {}
        now = calendar.getTimeInMillis();
        ADD_DATE = DATA_FORMAT.format(new Date(now));

        LogUtil.e("DATABASE ▶ " + playerListAddPosition + " : " + TO_ADD_DATE + " : " + ADD_DATE);

        ContentValues val = new ContentValues();
        val.put(PostDBInfo.COLUMN_OST_POST_TYPE, POST_TYPE);
        val.put(PostDBInfo.COLUMN_OST_OTI, OTI);
        val.put(PostDBInfo.COLUMN_OST_POI, POI);
        val.put(PostDBInfo.COLUMN_OST_SSI, SSI);
        val.put(PostDBInfo.COLUMN_OST_POI_SSI, POI_SSI);
        val.put(PostDBInfo.COLUMN_OST_TITLE_STATE, TITLE_STATE);
        val.put(PostDBInfo.COLUMN_OST_COLOR_HEX, COLOR_HEX);
        val.put(PostDBInfo.COLUMN_OST_RADIO_PATH, RADIO_PATH);
        val.put(PostDBInfo.COLUMN_OST_ALBUM_PATH, ALBUM_PATH);
        val.put(PostDBInfo.COLUMN_OST_SONG_NM, SONG_NM);
        val.put(PostDBInfo.COLUMN_OST_ARTI_NM, ARTI_NM);
        val.put(PostDBInfo.COLUMN_OST_ADD_DATE, ADD_DATE);

        if (isExsit) {
            //아이템이 있다면, 날짜만 갱신!!
            mDb.update(PostDBInfo.DB_TABLE_OST_PLAY_LIST, val, PostDBInfo.COLUMN_OST_POI + "=? AND " + PostDBInfo.COLUMN_OST_SSI + "=?", new String[]{POI, SSI});
        } else {
            //아이템이 없다면, 추가!!
            mDb.insert(PostDBInfo.DB_TABLE_OST_PLAY_LIST, null, val);
            updateCount++;
        }

        // 500곡 초과 시 삭제 옵션이 활성화 되어 있을 경우
        // 500ROW를 건너뛰고 1000개를 출력하라는 의미
        if (SPUtil.getBooleanSharedPreference(mContext, Constants.SP_PLAYER_DELETE_500_LIST)) {
            String sortType = "DESC";
            if ("TOP".equalsIgnoreCase(playerListAddPosition)) sortType = "ASC";

            String strSQL = "DELETE FROM " + PostDBInfo.DB_TABLE_OST_PLAY_LIST + " WHERE " + PostDBInfo.COLUMN_OST_POI_SSI +
                    " IN (SELECT " + PostDBInfo.COLUMN_OST_POI_SSI + " FROM " +  PostDBInfo.DB_TABLE_OST_PLAY_LIST +
                    " ORDER BY " + PostDBInfo.COLUMN_OST_ADD_DATE + " " + sortType + " LIMIT 1000 OFFSET 500)";
            LogUtil.e(strSQL);
            mDb.execSQL(strSQL);
        }

        if (Constants.DEBUG_DATABASE) {
            cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_OST_PLAY_LIST + " ORDER BY " + PostDBInfo.COLUMN_OST_ADD_DATE + " ASC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_OST_PLAY_LIST);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }

        return updateCount;
    }

    public int deleteOstPlayList(String[] POI_SSI){
        int rtCount = 0;
        rtCount = mDb.delete(PostDBInfo.DB_TABLE_OST_PLAY_LIST, PostDBInfo.COLUMN_OST_POI + "=? AND " + PostDBInfo.COLUMN_OST_SSI + "=?", POI_SSI);
        return rtCount;
    }

    public void deleteAllOstPlayList() {
        mDb.delete(PostDBInfo.DB_TABLE_OST_PLAY_LIST, null, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      재생 히스토리 관련 API                              //
    ////////////////////////////////////////////////////////////////////////////////////

    public ArrayList<OstDataItem> getOstPlayHistory(){
        ArrayList<OstDataItem> arrOstDataItem =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_OST_PLAY_HISTORY, null, null, null, null, null, PostDBInfo.COLUMN_OST_ADD_DATE + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OstDataItem ostDataItem = new OstDataItem();
                String POST_TYPE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_POST_TYPE));
                String OTI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_OTI));
                String POI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_POI));
                String SSI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_SSI));
                String TITLE_STATE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_TITLE_STATE));
                String COLOR_HEX = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_COLOR_HEX));
                String ALBUM_PATH = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_ALBUM_PATH));
                String RADIO_PATH = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_RADIO_PATH));
                String SONG_NM = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_SONG_NM));
                String ARTI_NM = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_ARTI_NM));
                String ADD_DATE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_OST_ADD_DATE));

                ostDataItem.setPOST_TYPE(POST_TYPE);
                ostDataItem.setOTI(OTI);
                ostDataItem.setPOI(POI);
                ostDataItem.setSSI(SSI);
                ostDataItem.setTITL_TOGGLE_YN(TITLE_STATE);
                ostDataItem.setCOLOR_HEX(COLOR_HEX);
                ostDataItem.setALBUM_PATH(ALBUM_PATH);
                ostDataItem.setRADIO_PATH(RADIO_PATH);
                ostDataItem.setSONG_NM(SONG_NM);
                ostDataItem.setARTI_NM(ARTI_NM);
                ostDataItem.setADD_DATE(ADD_DATE);
                arrOstDataItem.add(ostDataItem);
            }
            cursor.close();
        }

        return arrOstDataItem;
    }

    public int updateOstPlayHistory(Object item) {
        int updateCount = 0;
        String POST_TYPE, POI, OTI, SSI, TITLE_STATE, COLOR_HEX, RADIO_PATH, ALBUM_PATH, ARTI_NM, SONG_NM;
        if (item instanceof PostDataItem) {
            POST_TYPE = ((PostDataItem)item).getPOST_TYPE();
            POI = ((PostDataItem)item).getPOI();
            OTI = ((PostDataItem)item).getOTI();
            SSI = ((PostDataItem)item).getSSI();
            TITLE_STATE = "Y";
            COLOR_HEX = ((PostDataItem)item).getCOLOR_HEX();
            RADIO_PATH = ((PostDataItem)item).getRADIO_PATH();
            ALBUM_PATH = ((PostDataItem)item).getTITLE_ALBUM_PATH();
            ARTI_NM = ((PostDataItem)item).getTITLE_ARTI_NM();
            SONG_NM = ((PostDataItem)item).getTITLE_SONG_NM();
        } else if (item instanceof OstDataItem) {
            POST_TYPE = ((OstDataItem) item).getPOST_TYPE();
            POI = ((OstDataItem) item).getPOI();
            OTI = ((OstDataItem) item).getOTI();
            SSI = ((OstDataItem) item).getSSI();
            TITLE_STATE = ((OstDataItem) item).getTITL_TOGGLE_YN();
            COLOR_HEX = ((OstDataItem) item).getCOLOR_HEX();
            RADIO_PATH = ((OstDataItem) item).getRADIO_PATH();
            ALBUM_PATH = ((OstDataItem) item).getALBUM_PATH();
            ARTI_NM = ((OstDataItem) item).getARTI_NM();
            SONG_NM = ((OstDataItem) item).getSONG_NM();
        } else if (item instanceof CabinetMusicDataItem) {
            POST_TYPE = ((CabinetMusicDataItem) item).getPOST_TYPE();
            POI = ((CabinetMusicDataItem) item).getPOI();
            OTI = ((CabinetMusicDataItem) item).getOTI();
            SSI = ((CabinetMusicDataItem) item).getSSI();
            TITLE_STATE = ((CabinetMusicDataItem) item).getTITL_TOGGLE_YN();
            COLOR_HEX = "";
            RADIO_PATH = "";
            ALBUM_PATH = ((CabinetMusicDataItem) item).getALBUM_PATH();
            ARTI_NM = ((CabinetMusicDataItem) item).getARTI_NM();
            SONG_NM = ((CabinetMusicDataItem) item).getSONG_NM();
        } else {
            return updateCount;
        }

        SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        String strNow = DATA_FORMAT.format(new Date(now));

        ContentValues val = new ContentValues();
        val.put(PostDBInfo.COLUMN_OST_POST_TYPE, POST_TYPE);
        val.put(PostDBInfo.COLUMN_OST_OTI, OTI);
        val.put(PostDBInfo.COLUMN_OST_POI, POI);
        val.put(PostDBInfo.COLUMN_OST_SSI, SSI);
        val.put(PostDBInfo.COLUMN_OST_TITLE_STATE, TITLE_STATE);
        val.put(PostDBInfo.COLUMN_OST_COLOR_HEX, COLOR_HEX);
        val.put(PostDBInfo.COLUMN_OST_RADIO_PATH, RADIO_PATH);
        val.put(PostDBInfo.COLUMN_OST_ALBUM_PATH, ALBUM_PATH);
        val.put(PostDBInfo.COLUMN_OST_SONG_NM, SONG_NM);
        val.put(PostDBInfo.COLUMN_OST_ARTI_NM, ARTI_NM);
        val.put(PostDBInfo.COLUMN_OST_ADD_DATE, strNow);

        mDb.insert(PostDBInfo.DB_TABLE_OST_PLAY_HISTORY, null, val);
        updateCount++;

        //10개 이상 데이터는 삭제!!
        String strSQL = "DELETE FROM " + PostDBInfo.DB_TABLE_SEARCH_RECENT + " WHERE " + PostDBInfo.COLUMN_WORD +
                " IN (SELECT " + PostDBInfo.COLUMN_WORD + " FROM " +  PostDBInfo.DB_TABLE_SEARCH_RECENT +
                " ORDER BY " + PostDBInfo.COLUMN_SEARCH_DATE + " DESC LIMIT 100 OFFSET 20)";
        mDb.execSQL(strSQL);

        if (Constants.DEBUG_DATABASE) {
            Cursor cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_OST_PLAY_HISTORY + " ORDER BY " + PostDBInfo.COLUMN_OST_ADD_DATE + " DESC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_OST_PLAY_HISTORY);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }

        return updateCount;
    }

    public int deleteOstPlayHistory(String[] POI_SSI){
        int rtCount = 0;
        rtCount = mDb.delete(PostDBInfo.DB_TABLE_OST_PLAY_HISTORY, PostDBInfo.COLUMN_OST_POI + "=? AND " + PostDBInfo.COLUMN_OST_SSI + "=?", POI_SSI);
        return rtCount;
    }

    public void deleteAllOstPlayHistory() {
        mDb.delete(PostDBInfo.DB_TABLE_OST_PLAY_HISTORY, null, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      인기 해시태그 검색어 관련 API                    //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 인기 검색어 갯수 가져오기
     * @return 인기 검색어 갯수
     */
    public int getPostHashTagKeyWordCount(){
        int result =  0;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD,null,null, null, null, null, null);
        if(cursor != null){
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    /**
     * 인기 검색어 가져오기
     * @return 인기 검색어 목록
     */
    public ArrayList<PostHashTagKeyWordItem> getAllPostHashTagKeyWords(){
        ArrayList<PostHashTagKeyWordItem> result =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD,null,null, null, null, null, PostDBInfo.COLUMN_POST_HASHTAG_RANKING + " ASC");
        if(cursor != null){
            while(cursor.moveToNext()){
                int POST_HASHTAG_RANKING = cursor.getInt(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_HASHTAG_RANKING));
                String POST_HASHTAG_KEYWORD = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_HASHTAG_KEYWORD));
                int POST_HASHTAG_COUNT = cursor.getInt(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_HASHTAG_COUNT));

                PostHashTagKeyWordItem item = new PostHashTagKeyWordItem();
                item.setRANKING(POST_HASHTAG_RANKING);
                item.setKEYWORD(POST_HASHTAG_KEYWORD);
                item.setCOUNT(POST_HASHTAG_COUNT);
                result.add(item);
            }
            cursor.close();
        }

        return result;
    }

    /**
     * 인기 검색어 모두 삭제
     */
    public void deleteAllPostHashTagKeyWords() {
        mDb.delete(PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD, null, null);
    }

    /**
     * 인기 검색어로 저장하기
     */
    public void updatePostHashTagKeyWord(ArrayList<PostHashTagKeyWordItem> arrPostHashTagKeyWordItems){
        for (PostHashTagKeyWordItem item : arrPostHashTagKeyWordItems) {
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_POST_HASHTAG_RANKING, item.getRANKING());
            val.put(PostDBInfo.COLUMN_POST_HASHTAG_KEYWORD, item.getKEYWORD());
            val.put(PostDBInfo.COLUMN_POST_HASHTAG_COUNT, item.getCOUNT());
            mDb.insert(PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD, null, val);
        }

        if (Constants.DEBUG_DATABASE) {
            Cursor cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD + " ORDER BY " + PostDBInfo.COLUMN_POST_HASHTAG_RANKING + " ASC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_POST_HASHTAG_KEYWORD);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      인기 이야기 검색어 관련 API                       //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 인기 검색어 갯수 가져오기
     * @return 인기 검색어 갯수
     */
    public int getPostPopKeyWordCount(){
        int result =  0;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_POP_KEYWORD,null,null, null, null, null, null);
        if(cursor != null){
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    /**
     * 인기 검색어 가져오기
     * @return 인기 검색어 목록
     */
    public ArrayList<MusPopKeyWordItem> getAllPostPopKeyWords(){
        ArrayList<MusPopKeyWordItem> result =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_POP_KEYWORD,null,null, null, null, null, PostDBInfo.COLUMN_POST_POP_RANKING + " ASC");
        if(cursor != null){
            while(cursor.moveToNext()){
                int POST_POP_RANKING = cursor.getInt(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_POP_RANKING));
                String POST_POP_KEYWORD = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_POP_KEYWORD));

                MusPopKeyWordItem item = new MusPopKeyWordItem();
                item.setRANKING(POST_POP_RANKING);
                item.setKEYWORD(POST_POP_KEYWORD);
                result.add(item);
            }
            cursor.close();
        }

        return result;
    }

    /**
     * 인기 검색어 모두 삭제
     */
    public void deleteAllPostPopKeyWords() {
        mDb.delete(PostDBInfo.DB_TABLE_POST_POP_KEYWORD, null, null);
    }

    /**
     * 인기 검색어로 저장하기
     */
    public void updatePostPopKeyWord(ArrayList<MusPopKeyWordItem> arrPostPopKeyWordItems){
        for (MusPopKeyWordItem item : arrPostPopKeyWordItems) {
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_POST_POP_RANKING, item.getRANKING());
            val.put(PostDBInfo.COLUMN_POST_POP_KEYWORD, item.getKEYWORD());
            mDb.insert(PostDBInfo.DB_TABLE_POST_POP_KEYWORD, null, val);
        }

        if (Constants.DEBUG_DATABASE) {
            Cursor cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_POST_POP_KEYWORD + " ORDER BY " + PostDBInfo.COLUMN_POST_POP_RANKING + " ASC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_POST_POP_KEYWORD);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      인기 노래 검색어 관련 API                                 //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 인기 검색어 갯수 가져오기
     * @return 인기 검색어 갯수
     */
    public int getMusPopKeyWordCount(){
        int result =  0;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_MUS_POP_KEYWORD,null,null, null, null, null, null);
        if(cursor != null){
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    /**
     * 인기 검색어 가져오기
     * @return 인기 검색어 목록
     */
    public ArrayList<MusPopKeyWordItem> getAllMusPopKeyWords(){
        ArrayList<MusPopKeyWordItem> result =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_MUS_POP_KEYWORD,null,null, null, null, null, PostDBInfo.COLUMN_MUS_POP_RANKING + " ASC");
        if(cursor != null){
            while(cursor.moveToNext()){
                int MUS_POP_RANKING = cursor.getInt(cursor.getColumnIndex(PostDBInfo.COLUMN_MUS_POP_RANKING));
                String MUS_POP_KEYWORD = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_MUS_POP_KEYWORD));

                MusPopKeyWordItem item = new MusPopKeyWordItem();
                item.setRANKING(MUS_POP_RANKING);
                item.setKEYWORD(MUS_POP_KEYWORD);
                result.add(item);
            }
            cursor.close();
        }

        return result;
    }

    /**
     * 인기 검색어 모두 삭제
     */
    public void deleteAllMusPopKeyWords() {
        mDb.delete(PostDBInfo.DB_TABLE_MUS_POP_KEYWORD, null, null);
    }

    /**
     * 인기 검색어로 저장하기
     */
    public void updateMusPopKeyWord(ArrayList<MusPopKeyWordItem> arrMusPopKeyWordItems){
        for (MusPopKeyWordItem item : arrMusPopKeyWordItems) {
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_MUS_POP_RANKING, item.getRANKING());
            val.put(PostDBInfo.COLUMN_MUS_POP_KEYWORD, item.getKEYWORD());
            mDb.insert(PostDBInfo.DB_TABLE_MUS_POP_KEYWORD, null, val);
        }

        if (Constants.DEBUG_DATABASE) {
            Cursor cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_MUS_POP_KEYWORD + " ORDER BY " + PostDBInfo.COLUMN_MUS_POP_RANKING + " ASC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_MUS_POP_KEYWORD);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                                      컬러 관련 API                                           //
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * 컬러 갯수 가져오기
     * @return 인기 검색어 갯수
     */
    public int getPostColorCount(){
        int result =  0;

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_COLOR,null,null, null, null, null, null);
        if(cursor != null){
            result = cursor.getCount();
            cursor.close();
        }

        return result;
    }

    /**
     * 컬러 가져오기
     * @return 컬러 목록
     */
    public ArrayList<ColorItem> getAllPostColors(){
        ArrayList<ColorItem> result =  new ArrayList<>();

        Cursor cursor =  mDb.query(PostDBInfo.DB_TABLE_POST_COLOR,null,null, null, null, null, PostDBInfo.COLUMN_POST_COLOR_SORT_ORDER + " ASC");
        if(cursor != null){
            while(cursor.moveToNext()){
                String POST_COLOR_ICI = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_COLOR_ICI));
                String POST_COLOR_CODE = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_COLOR_CODE));
                int POST_COLOR_SORT_ORDER = cursor.getInt(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_COLOR_SORT_ORDER));
                String POST_COLOR_ICON_NM = cursor.getString(cursor.getColumnIndex(PostDBInfo.COLUMN_POST_COLOR_ICON_NM));

                ColorItem colorItem = new ColorItem();
                colorItem.setICI(POST_COLOR_ICI);
                colorItem.setCOLOR_CODE(POST_COLOR_CODE);
                colorItem.setSORT_ORDER(POST_COLOR_SORT_ORDER);
                colorItem.setICON_NM(POST_COLOR_ICON_NM);
                result.add(colorItem);
            }
            cursor.close();
        }

        return result;
    }

    /**
     * 컬러 모두 삭제
     */
    public void deleteAllPostColors() {
        mDb.delete(PostDBInfo.DB_TABLE_POST_COLOR, null, null);
    }

    /**
     * 컬러 저장하기
     */
    public void updatePostColor(ArrayList<ColorItem> arrColorItems) {
        for (ColorItem item : arrColorItems) {
            ContentValues val = new ContentValues();
            val.put(PostDBInfo.COLUMN_POST_COLOR_ICI, item.getICI());
            val.put(PostDBInfo.COLUMN_POST_COLOR_CODE, item.getCOLOR_CODE());
            val.put(PostDBInfo.COLUMN_POST_COLOR_SORT_ORDER, item.getSORT_ORDER());
            val.put(PostDBInfo.COLUMN_POST_COLOR_ICON_NM, item.getICON_NM());
            mDb.insert(PostDBInfo.DB_TABLE_POST_COLOR, null, val);
        }

        if (Constants.DEBUG_DATABASE) {
            Cursor cursor = mDb.rawQuery("SELECT * FROM " + PostDBInfo.DB_TABLE_POST_COLOR + " ORDER BY " + PostDBInfo.COLUMN_POST_COLOR_SORT_ORDER + " ASC", null);
            String tableString = String.format("TABLE : %s", PostDBInfo.DB_TABLE_POST_COLOR);
            LogUtil.e(tableString);

            if (cursor .moveToFirst()) {
                String[] columnNames = cursor.getColumnNames();
                do {
                    String columString = "";
                    for (String name: columnNames) {
                        columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                    }

                    LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                } while (cursor.moveToNext());
            }
        }
    }
}
