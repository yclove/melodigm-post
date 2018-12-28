package com.melodigm.post.util;

public class PostDBInfo {
	// TABLE 명
    // SORT
    public final static String DB_TABLE_SORT = "SORT";
    // 최근 이야기 검색어 테이블
    public final static String DB_TABLE_POST_SEARCH_RECENT = "POST_SEARCH_RECENT_WORD";
    // 최근 검색어 테이블
    public final static String DB_TABLE_SEARCH_RECENT = "SEARCH_RECENT_WORD";
    // 재생 목록 테이블
    public final static String DB_TABLE_OST_PLAY_LIST = "OST_PLAY_LIST";
    // 재생 히스토리 테이블
    public final static String DB_TABLE_OST_PLAY_HISTORY = "OST_PLAY_HISTORY";
    // 인기 해시태그 검색어 테이블
    public final static String DB_TABLE_POST_HASHTAG_KEYWORD = "TABLE_POST_HASHTAG_KEYWORD";
    // 인기 이야기 검색어 테이블
    public final static String DB_TABLE_POST_POP_KEYWORD = "POST_POP_KEYWORD";
    // 인기 노래 검색어 테이블
    public final static String DB_TABLE_MUS_POP_KEYWORD = "MUS_POP_KEYWORD";
    // 컬러 테이블
    public final static String DB_TABLE_POST_COLOR = "POST_COLOR";

    // TABLE 필드명
    public static final String COLUMN_SORT_TYPE = "SORT_TYPE";                            // SORT TYPE (POST = POST)
    public static final String COLUMN_SORT_GENDER = "SORT_GENDER";                 // 성별
    public static final String COLUMN_SORT_GENERATION = "SORT_GENERATION"; // 세대
    public static final String COLUMN_SORT_TIME = "SORT_TIME";                           // 시간대
    public static final String COLUMN_SORT_DISTANCE = "SORT_DISTANCE";          // 거리
    public static final String COLUMN_SORT_DATE = "SORT_DATE";                          // SORTING 시간

    public static final String COLUMN_POST_WORD = "POST_WORD";                             // 검색어
    public static final String COLUMN_POST_SEARCH_DATE = "POST_SEARCH_DATE";   // 검색일

    public static final String COLUMN_WORD = "WORD";                             // 검색어
    public static final String COLUMN_SEARCH_DATE = "SEARCH_DATE";   // 검색일

    public static final String COLUMN_OST_POST_TYPE = "POST_TYPE";
    public static final String COLUMN_OST_POI_SSI = "POI_SSI";
    public static final String COLUMN_OST_OTI = "OTI";
    public static final String COLUMN_OST_POI = "POI";
    public static final String COLUMN_OST_SSI = "SSI";
    public static final String COLUMN_OST_TITLE_STATE = "TITLE_STATE";
    public static final String COLUMN_OST_COLOR_HEX = "COLOR_HEX";
    public static final String COLUMN_OST_ALBUM_PATH = "ALBUM_PATH";
    public static final String COLUMN_OST_RADIO_PATH = "RADIO_PATH";
    public static final String COLUMN_OST_SONG_NM = "SONG_NM";
    public static final String COLUMN_OST_ARTI_NM = "ARTI_NM";
    public static final String COLUMN_OST_ADD_DATE = "ADD_DATE";

    public static final String COLUMN_POST_HASHTAG_RANKING = "POST_HASHTAG_RANKING";
    public static final String COLUMN_POST_HASHTAG_KEYWORD = "POST_HASHTAG_KEYWORD";
    public static final String COLUMN_POST_HASHTAG_COUNT = "POST_HASHTAG_COUNT";

    public static final String COLUMN_POST_POP_RANKING = "POST_POP_RANKING";
    public static final String COLUMN_POST_POP_KEYWORD = "POST_POP_KEYWORD";

    public static final String COLUMN_MUS_POP_RANKING = "MUS_POP_RANKING";
    public static final String COLUMN_MUS_POP_KEYWORD = "MUS_POP_KEYWORD";

    public static final String COLUMN_POST_COLOR_ICI = "POST_COLOR_ICI";
    public static final String COLUMN_POST_COLOR_CODE = "POST_COLOR_CODE";
    public static final String COLUMN_POST_COLOR_SORT_ORDER = "POST_COLOR_SORT_ORDER";
    public static final String COLUMN_POST_COLOR_ICON_NM = "POST_COLOR_ICON_NM";
}
