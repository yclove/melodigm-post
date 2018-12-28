package com.melodigm.post.common;

public class Constants {
	// COMMON
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_REQUEST = false;
    public static final boolean DEBUG_DATABASE = false;

    public static int APP_VERSION_CODE = 0;
    public static String APP_VERSION_NAME = "";
    public static int STAMP_COUNT = 0;
    public static int RIGHT_COUNT = 0;

	public static final String SERVICE_CHAR_SET = "UTF-8";
	public static final String SERVICE_TAG = "POST";
	public static final String SERVICE_PACKAGE = "com.melodigm.post";
	public static final String SERVICE_EMAIL = "service@melodigm.com";
    public static final String SERVICE_IMAGE_FILE_PATH = "/DCIM/Camera";
    public static final int SERVICE_MAX_IMAGE_SIZE = 800;
    public static final String SERVICE_POST_FILE_NAME = "Post.png";
    public static final String SERVICE_INTRO_FILE_PATH = "PostIntro";
    public static final String SERVICE_MUSIC_FILE_PATH = "PostMusic";
    public static final String SERVICE_SHARE_FILE_PATH = "PostShare";
    public static final String SERVICE_SHARE_FILE_NAME = "PostShare.png";
    public static final String SERVICE_VOICE_FILE_PATH = "PostVoice";
    public static final String SERVICE_VOICE_FILE_NAME = "PostVoice.mp4";
    public static final String SERVICE_CABINET_IMAGE_PATH = "PostCabinet";
    public static final String SERVICE_CABINET_IMAGE_NAME = "PostCabinet.png";
    public static final float STROKE_WIDTH = 10.0f;
    public static final int TEXT_VIEW_SPACING = 3;
    public static final float GLIDE_THUMBNAIL = 0.1f;

    // CUSTOM SCHEME
	public static final String SERVICE_SCHEME = "melodigm";
    public static final String SERVICE_SCHEME_HOST = "post";
    public static final String KAKAOLINK_SCHEME_HOST = "kakaolink";

	// TIMEOUT
    public static final int TIMEOUT_HTTP_CONNECTION = 30000;
    public static final int TIMEOUT_HTTP_SOCKET = 30000;
	public static final int TIMEOUT_BACK_KEY = 2000;
	public static final int TIMEOUT_GESTURE_DISTANCE = 200;
	public static final int TIMEOUT_GESTURE_VELOCITY = 300;

    // DIALOG TYPE
    public static final int DIALOG_TYPE_CHOICE_PICTURE = 1;
    public static final int DIALOG_TYPE_CHOICE_YEAR = 2;
    public static final int DIALOG_TYPE_CONFIRM = 3;
    public static final int DIALOG_TYPE_INFO = 4;
    public static final int DIALOG_TYPE_PLAY_ALL = 5;
    public static final int DIALOG_TYPE_NOTIFY = 6;
    public static final int DIALOG_TYPE_PLAYER_ADD = 7;
    public static final int DIALOG_TYPE_SHARE = 8;
    public static final int DIALOG_TYPE_NOTICE = 9;
    public static final int DIALOG_TYPE_NOTICE_CLOSE = 10;
    public static final int DIALOG_TYPE_PLAYER_SORT = 11;
    public static final int DIALOG_TYPE_STAMP_FILTER = 12;
    public static final int DIALOG_TYPE_STAMP_REQUIRED = 13;
    public static final int DIALOG_TYPE_POPULAR = 14;
    public static final int DIALOG_TYPE_PLAYER_TIMER = 15;
    public static final int DIALOG_TYPE_LOCATION_SERVICE = 16;
    public static final int DIALOG_TYPE_STORY_SORT = 17;
    public static final int DIALOG_TYPE_STORY_TYPE = 18;
    public static final int DIALOG_TYPE_UPDATE_NEED = 19;
    public static final int DIALOG_TYPE_UPDATE_SUPPORT = 20;
    public static final int DIALOG_TYPE_RADIO_TITLE = 21;
    public static final int DIALOG_TYPE_RADIO_ON_AIR = 22;
    public static final int DIALOG_TYPE_RADIO_ON_AIR_CLOSE = 23;
    public static final int DIALOG_TYPE_NOT_FOUND_USER = 24;
    public static final int DIALOG_TYPE_NEWS_AGENCY = 25;

    // HEADER TYPE
    public static final int HEADER_TYPE_MENU = 1;
    public static final int HEADER_TYPE_BACK_HOME_CHECK = 2;
    public static final int HEADER_TYPE_BACK_HOME = 3;
    public static final int HEADER_TYPE_CLOSE = 4;
    public static final int HEADER_TYPE_MENU_POPULAR = 5;

	// DIALOG MSG
    public static final int DIALOG_APP_CLOSE = 9999;
    public static final int DIALOG_EXCEPTION_NON = -1;
    public static final int DIALOG_EXCEPTION_REQUEST = -2;
    public static final int DIALOG_EXCEPTION_POST = -3;
    public static final int DIALOG_EXCEPTION_UPDATE_NEED = -4;
    public static final int DIALOG_EXCEPTION_UPDATE_SUPPORT = -5;
    public static final int DIALOG_EXCEPTION_NETWORK = -6;
    public static final int DIALOG_EXCEPTION_IMAGE_UPLOAD = -7;

    // RESULT
    public static final int RESULT_SUCCESS = 1000;
    public static final int RESULT_FAIL = 1001;

    // 트위터 Key - dev@melodigm.com
    public static final String TWITTER_KEY = "omwuXGYJKEvJZi3YbvblSCTWA";
    public static final String TWITTER_SECRET = "pdaZjBrI5vzcFgbE5dPFbvVTFreFJULIkr0R4On35yfCLEdwjf";

    // 인스타그램 KEY - melodigm_dev
    public static final String INSTAGRAM_CLIENT_ID = "df263e531daa44feb7acde4cec3c9079";
    public static final String INSTAGRAM_CLIENT_SECRET = "0d4454d2e98d422faa6124f80dc50bb4";
    public static final String INSTAGRAM_CALLBACK_URL = "postinstagram://connect";

    // POST Security Key
    public static final String POST_SECRET_KEY = "feb6e0bfcbc93744df2048f9e30b92e74102bdc231b7";
    public static final String POST_ACCESS_KEY = "90585bea";

    // HEADER
    public static final String HEADER_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String HEADER_CONTENT_TYPE_MULTIPART = "multipart/form-data";
    public static final String HEADER_DEVICE_TYPE = "AH01";

    // Request Code
    public static final String REQUEST_CABINET_TYPE = "CABINET_TYPE";
    public static final String REQUEST_CABINET_TYPE_PUT = "PUT";
    public static final String REQUEST_CABINET_TYPE_PLAY = "PLAY";
    public static final String REQUEST_TYPE_POST = "AA01";        // 사연
    public static final String REQUEST_TYPE_TODAY = "AA02";     // TODAY
    public static final String REQUEST_TYPE_RADIO = "AA03";     // 라디오
    public static final String REQUEST_DISP_TYPE_PUBLIC = "AB01";       // 공개
    public static final String REQUEST_DISP_TYPE_PRIVATE = "AB02";     // 나만
    public static final String REQUEST_DISP_TYPE_FRIEND = "AB03";       // 친구
    public static final String REQUEST_DISP_TYPE_DELETE = "AB04";       // 삭제
    public static final String REQUEST_DISP_TYPE_NOTIFY = "AB05";      // 신고
    public static final String REQUEST_DISP_TYPE_RESET = "AB06";         // 초기화
    public static final String REQUEST_FILE_USE_TYPE_POST_BACK = "AC01";    // 포스트배경사진
    public static final String REQUEST_FILE_USE_TYPE_VOICE = "AC02";             // 음성사연
    public static final String REQUEST_FILE_USE_TYPE_USER_BACK = "AC03";   // 유저배경사진
    public static final String REQUEST_FILE_USE_TYPE_MAP_BACK = "AC04";   // 지도배경사진
    public static final String REQUEST_REG_PLAC_TYPE_POST = "AE01";         // 포스트
    public static final String REQUEST_REG_PLAC_TYPE_STORAGE = "AE02";  // 보관함
    public static final String REQUEST_REG_PLAC_TYPE_OST = "AE03";           // OST
    public static final String REQUEST_REG_PLAC_TYPE_ALBUM = "AE04";      // 앨범
    public static final String REQUEST_REG_PLAC_TYPE_MUSIC = "AE05";      // 음원
    public static final String REQUEST_DCRE_TARGET_TYPE_POST = "AJ01";      // POST
    public static final String REQUEST_DCRE_TARGET_TYPE_OST = "AJ02";      // OST
    public static final String REQUEST_ACCOUNT_AUTH_TYPE_EMAIL = "AD01";            // 이메일
    public static final String REQUEST_ACCOUNT_AUTH_TYPE_APP_ID = "AD02";           // 앱ID
    public static final String REQUEST_ACCOUNT_AUTH_TYPE_FACEBOOK = "AD03";      // Facebook
    public static final String REQUEST_ACCOUNT_AUTH_TYPE_TWITTER = "AD04";          // Twitter
    public static final String REQUEST_ACCOUNT_AUTH_TYPE_INSTAGRAM = "AD05";      // Instagram
    public static final String REQUEST_ACCOUNT_AUTH_TYPE_NAVER_LINE = "AD06";      // 네이버 라인
    public static final String REQUEST_DCRE_RESN_CODE_AK01 = "AK01";    // 부적절한 홍보/광고 게시물
    public static final String REQUEST_DCRE_RESN_CODE_AK02 = "AK02";    // 음란 또는 청소년에게 부적합한 내용
    public static final String REQUEST_DCRE_RESN_CODE_AK03 = "AK03";    // 명예훼손/사생활 침해 및 저작권 침해 등
    public static final String REQUEST_POST_PST_TYPE_LEFT = "BF01";         // 왼쪽 정렬
    public static final String REQUEST_POST_PST_TYPE_CENTER = "BF02";    // 중앙 정렬
    public static final String REQUEST_POST_PST_TYPE_RIGHT = "BF03";      // 오른쪽 정렬
    public static final String REQUEST_POINT_TYPE_STAMP = "AX01";           // 우표
    public static final String REQUEST_TRANS_TYPE_ALL = "ALL";           // 전체
    public static final String REQUEST_TRANS_TYPE_KEEP = "BG";           // 적립
    public static final String REQUEST_TRANS_TYPE_USE = "BH";           // 소비
    public static final String REQUEST_PLAY_CYCLE_TYPE_START = "AL01";           // 시작
    public static final String REQUEST_PLAY_CYCLE_TYPE_PAYMENT = "AL02";     // 과금
    public static final String REQUEST_PLAY_CYCLE_TYPE_END = "AL03";              // 완료
    public static final String REQUEST_ACTION_DIVN_TYPE_POST = "AW01"; // 포스트활동
    public static final String REQUEST_ACTION_DIVN_TYPE_OST = "AW02"; // 음원활동
    public static final String REQUEST_ACTION_DIVN_TYPE_PAYMENT = "AW03"; // 상품구매활동
    public static final String REQUEST_ACTION_DIVN_TYPE_SHARE = "AW04"; // 소셜공유활동
    public static final String REQUEST_ACTION_TYPE_IMAGE = "BL01"; // 이미지저장
    public static final String REQUEST_ACTION_TYPE_FACEBOOK = "BL02"; // 페이스북
    public static final String REQUEST_ACTION_TYPE_TWITTER = "BL03"; // 트위터
    public static final String REQUEST_ACTION_TYPE_INSTAGRAM = "BL04"; // 인스타그램
    public static final String REQUEST_ACTION_TYPE_KAKAOTALK = "BL05"; // 카카오톡
    public static final String REQUEST_ACTION_TYPE_LINE = "BL06"; // 라인
    public static final String REQUEST_ACTION_PLAC_TYPE_PAYMENT = "BK01"; // 상품구매내역
    public static final String REQUEST_ACTION_PLAC_TYPE_ICON = "BK02"; // 아이콘등록내역
    public static final String REQUEST_ACTION_PLAC_TYPE_POST = "BK03"; // 포스트
    public static final String REQUEST_ACTION_PLAC_TYPE_OST = "BK04"; // OST
    public static final String REQUEST_ACTION_PLAC_TYPE_PLAYLIST = "BK05"; // 음원재생내역

    // Query Code
    public static final int QUERY_APP_VERSION = 9001;
    public static final int QUERY_REGIST_USER = 9002;
    public static final int QUERY_USER_INFO = 9003;
    public static final int QUERY_UPDATE_USER_INFO = 9004;
    public static final int QUERY_POST_DATA = 9005;
    public static final int QUERY_POST_DATA_VIEW = 9006;
    public static final int QUERY_WRITE_POST_DATA = 9007;
    public static final int QUERY_CHOICE_PICTURE = 9008;
    public static final int QUERY_CHOICE_CAMERA = 9009;
    public static final int QUERY_LOCATION_CHANGE = 9010;
    public static final int QUERY_LOCATION_SEARCH = 9011;
    public static final int QUERY_OST_DATA = 9012;
    public static final int QUERY_WRITE_OST_DATA = 9013;
    public static final int QUERY_POST_LIKE = 9014;
    public static final int QUERY_POST_DATA_UPDATE = 9015;
    public static final int QUERY_OST_DATA_VIEW = 9016;
    public static final int QUERY_POST_NOTIFY = 9017;
    public static final int QUERY_POST_DELETE = 9018;
    public static final int QUERY_SNS_ACCOUNT_SYNC = 9019;
    public static final int QUERY_SNS_ACCOUNT_RESTORE = 9020;
    public static final int QUERY_OST_TITLE = 9021;
    public static final int QUERY_OST_DATA_UPDATE = 9022;
    public static final int QUERY_OST_SEARCH = 9023;
    public static final int QUERY_OST_RELATED = 9024;
    public static final int QUERY_OST_RELATED_VIEW = 9025;
    public static final int QUERY_OST_ADD_PLAY_LIST = 9026;
    public static final int QUERY_OST_DOWNLOAD = 9027;
    public static final int QUERY_CABINET_DATA = 9028;
    public static final int QUERY_WRITE_CABINET = 9029;
    public static final int QUERY_CABINET_DESC = 9030;
    public static final int QUERY_ADD_CABINET = 9031;
    public static final int QUERY_CROP_IMAGE = 9032;
    public static final int QUERY_CABINET_SORT = 9033;
    public static final int QUERY_SLIDE_MENU = 9034;
    public static final int QUERY_NOTIFY_RESULT = 9035;
    public static final int QUERY_LIKE_RESULT = 9036;
    public static final int QUERY_INIT_INFO = 9037;
    public static final int QUERY_INIT = 9038;
    public static final int QUERY_OST_NOTIFY = 9039;
    public static final int QUERY_OST_DELETE = 9040;
    public static final int QUERY_OST_LIKE = 9041;
    public static final int QUERY_POST_SORT = 9042;
    public static final int QUERY_TIME_LINE = 9043;
    public static final int QUERY_OST_REPLE = 9044;
    public static final int QUERY_OST_REPLE_WRITE = 9045;
    public static final int QUERY_OST_REPLE_DELETE = 9046;
    public static final int QUERY_OST_REPLE_DELETE_RESULT = 9047;
    public static final int QUERY_COLOR_RANDOM_IMAGE = 9048;
    public static final int QUERY_CALENDAR = 9049;
    public static final int QUERY_POST_DATA_MORE = 9050;
    public static final int QUERY_CABINET_PLAY = 9051;
    public static final int QUERY_PLAY_LIST_DELETE = 9052;
    public static final int QUERY_MUSIC_SEARCH = 9053;
    public static final int QUERY_STAMP_DATA = 9054;
    public static final int QUERY_CABINET_DELETE = 9055;
    public static final int QUERY_POST_DATA_ADD = 9056;
    public static final int QUERY_NOTIFICATION_CENTER = 9057;
    public static final int QUERY_PLAYER_TIMER = 9058;
    public static final int QUERY_LOCATION_SERVICE_SETTING = 9059;
    public static final int QUERY_AGREEMENT_DATA = 9060;
    public static final int QUERY_STORY_SEARCH = 9061;
    public static final int QUERY_STORY_RELATED = 9062;
    public static final int QUERY_SNS_SHARE = 9063;
    public static final int QUERY_MUSIC_PAYMENT = 9064;
    public static final int QUERY_MUSIC_PAYMENT_FAILED = 9065;
    public static final int QUERY_MUSIC_PATH = 9066;
    public static final int QUERY_MUSIC_SECURITY = 9067;
    public static final int QUERY_SEND_MAIL = 9068;
    public static final int QUERY_SELECT_NOTIFICATION = 9069;
    public static final int QUERY_UPDATE_NOTIFICATION = 9070;
    public static final int QUERY_UPDATE_QUALITY = 9071;
    public static final int QUERY_ACTION_LOG = 9072;

    // COLOR
	public static final int COLOR_WHITE = 0xFFFFFFFF;
	public static final int COLOR_YELLOW = 0xFFFFFF00;
	
	// JARVIS Server API
    public static final String API_POST = "http://dev.melodigm.com:8888/";
    public static long API_LASTEST_TIMESTAMP = 0;
    public static String API_UV_LOGGING = "N";
	public static final int API_RETRY_COUNT = 10;
	public static final double API_INIT_LNG = 126.98955d;

    // 이용약관 / 개인정보 수집 및 이용 동의 / 위치정보이용약관 / 개인정보보호정책 API
    public static final String AGREEMENT_SERVICE = API_POST + "app0102v1.post";
    public static final String AGREEMENT_PRIVATE = API_POST + "app0103v1.post";
    public static final String AGREEMENT_LOCATION = API_POST + "app0104v1.post";
    public static final String AGREEMENT_SCHEME = API_POST + "app0105v1.post";

    // Music Player Service
    public static final String MPS_COMMAND = "COMMAND";
    public static final String MPS_COMMAND_GET = "COMMAND_GET";
    public static final String MPS_COMMAND_SET = "COMMAND_SET";
    public static final String MPS_COMMAND_PUT = "COMMAND_PUT";
    public static final String MPS_COMMAND_ADD = "COMMAND_ADD";
    public static final String MPS_COMMAND_PLAY = "COMMAND_PLAY";
    public static final String MPS_COMMAND_PAUSE = "COMMAND_PAUSE";
    public static final String MPS_COMMAND_PREV = "COMMAND_PREV";
    public static final String MPS_COMMAND_NEXT = "COMMAND_NEXT";
    public static final String MPS_COMMAND_ON_AIR = "COMMAND_ON_AIR";
    public static final String MPS_COMMAND_ON_AIR_CLEAR = "COMMAND_ON_AIR_CLEAR";
    public static final int MPS_REPEAT_NO = 0;
    public static final int MPS_REPEAT_ALL = 1;
    public static final int MPS_REPEAT_ONE = 2;
    public static final int MPS_RANDOM_NO = 0;
    public static final int MPS_RANDOM_OK = 1;
    public static final String NOTIFY_PREVIOUS = "com.melodigm.post.previous";
    public static final String NOTIFY_DELETE = "com.melodigm.post.delete";
    public static final String NOTIFY_PAUSE = "com.melodigm.post.pause";
    public static final String NOTIFY_PLAY = "com.melodigm.post.play";
    public static final String NOTIFY_NEXT = "com.melodigm.post.next";


    // SharedPreferences
    public static final String SP_INTRO_BG = "INTRO_BG";
    public static final String SP_USER_ID = "USER_ID";
    public static final String SP_USER_GENDER = "USER_GENDER";
    public static final String SP_USER_BIRTH_YEAR = "USER_BIRTH_YEAR";
    public static final String SP_PUSH_ID = "PUSH_ID";
    public static final String SP_PUSH_STATE = "PUSH_STATE";
    public static final String SP_LATEST_APP_VERSION = "LATEST_APP_VERSION";
    public static final String SP_USE_DATA_NETWORK = "USE_DATA_NETWORK";
    public static final String SP_USER_LAT = "USER_LAT";
    public static final String SP_USER_LNG = "USER_LNG";
    public static final String SP_ACCOUNT_ID = "ACCOUNT_ID";
    public static final String SP_ACCOUNT_AUTH_TYPE = "ACCOUNT_AUTH_TYPE";
    public static final String SP_MPS_POSITION = "MPS_POSITION";
    public static final String SP_MPS_REPEAT = "MPS_REPEAT";
    public static final String SP_MPS_RANDOM = "MPS_RANDOM";
    public static final String SP_ICI_LIKE = "ICI_LIKE";
    public static final String SP_COUNTRY_CODE = "COUNTRY_CODE";
    public static final String SP_PLAYER_TIMER = "PLAYER_TIMER";
    public static final String SP_PLAYER_STREAMING_QUALITY = "PLAYER_STREAMING_QUALITY";
    public static final String SP_PLAYER_LIST_ADD_POSITION = "PLAYER_LIST_ADD_POSITION";
    public static final String SP_PLAYER_DELETE_500_LIST = "PLAYER_DELETE_500_LIST";
    public static final String SP_PLAYER_FILE_SAVE = "PLAYER_FILE_SAVE";
    public static final String SP_PLAYER_DISPLAY_LOCK_SCREEN_ALBUM = "PLAYER_DISPLAY_LOCK_SCREEN_ALBUM";
    public static final String SP_NOTIFICATION_POST = "NOTIFICATION_POST";
    public static final String SP_NOTIFICATION_OST = "NOTIFICATION_OST";
    public static final String SP_NOTIFICATION_SERVICE = "NOTIFICATION_SERVICE";
    public static final String SP_NOTIFICATION_TODAY = "NOTIFICATION_TODAY";
    public static final String SP_BADGE_COUNT = "BADGE_COUNT";

    // Google Analytics
    public static final String GA_CATEGORY_MUSIC = "음원";
    public static final String GA_CATEGORY_SEARCH = "검색";
    public static final String GA_CATEGORY_SNS = "SNS";
    public static final String GA_CATEGORY_SHARE = "공유";

    public static final String GA_ACTION_MUSIC_LISTENING = "듣기";
    public static final String GA_ACTION_MUSIC_COMPLETION = "감상";
    public static final String GA_ACTION_SEARCH_MUSIC = "노래";
    public static final String GA_ACTION_SEARCH_OST = "OST";
    public static final String GA_ACTION_SEARCH_STORY = "사연";
    public static final String GA_ACTION_SNS_SYNC = "계정연동";
    public static final String GA_ACTION_SNS_RESTORE = "계정복원";
    public static final String GA_ACTION_SHARE_IMAGE = "이미지로 저장";
    public static final String GA_ACTION_SHARE_FACEBOOK = "페이스북";
    public static final String GA_ACTION_SHARE_TWITTER = "트위터";
    public static final String GA_ACTION_SHARE_INSTAGRAM = "인스타그램";
    public static final String GA_ACTION_SHARE_LINE = "라인";
    public static final String GA_ACTION_SHARE_KAKAOTALK = "카카오톡";
}
