/**
 * YCNOTE - Audio Record
     1. Mediarecorder
     API문서: http://developer.android.com/reference/android/media/MediaRecorder.html
     사용법: http://developer.android.com/guide/topics/media/index.html

     Mediarecorder는 그 이름을 보면 알 수 있듯이, media를 record한다. audio는 마이크를 통해 녹음하여 sdcard에 그 파일을 저장한다. 녹음된 audio의 포맷은 MPEG4, RAW AMR, 3GP가 있다.

     <장점>
     1) 사용이 쉽다
     2) 오디오를 압축된 포맷으로 녹음한다.
     3) 전화 소리를 녹음할 수 있다 (수신, 송신 측 모두)
     4) 음성 인식을 녹음을 할 수 있다.

     <단점>
     1) 오디오 버퍼에 접근할 수 없다.
     2) 녹음된 오디오를 처리하기가 어렵다 – 왜냐하면 이미 압축된 포맷으로 녹음되었기 때문에.
     3) sampling rate를 바꿀 수 없다.
     4) recording을 어떻게 발생시킬 지를 거의 컨트롤할 수 없다. (very little or no control)

     2. Audiorecord
     API 문서: http://developer.android.com/reference/android/media/AudioRecord.html
     사용법: http://hashspeaks.wordpress.com/2009/06/18/audiorecord-part-4/

     Audiorecord API는 Mediarecorder의 제한을 극복하기 위한 구글의 공식적인 API다. 녹음되는 Audio는 이후의 processing을 위해서 버퍼에 저장된다. Audiorecord의 녹음 방법과 Audio처리 방법은 자바의 방법이다.

     <장점>
     1) 오디오 녹음을 MONO와 STEREO 중 선택 가능하다.
     2) sample size, sample rate, buffersize 등 다양한 오디오 레코딩 속성을 설정할 수 있다.
     3) 녹음된 audio는 버퍼를 통해 제공된다.

     <단점>
     1) Buffer 다루기가 어렵다. 만약 이게 무슨 일을 하는 지 알지 못한다면, 만든 파일을 잃어버릴지도..

     3. Audiorecord: native interface
     A LITTLE BIT HELP : http://hashspeaks.wordpress.com/2009/04/18/audiorecord-in-android-part-2/
     사용법:- ( Coming soon... how to build using NDK, sample code and how to use this API )

     native interface는 C/C++ 라이브러리를 적용한 API를 제공한다. 이 라이브러리들은 JNI를 통해 자바 액티비티로 호출 가능하다. 이 interface를 사용한 프로그램은 NDK를 통해 컴파일되고 JNI를 통해 안드로이드 애플리케이션으로 사용된다.
 */
package com.melodigm.post.registration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.protocol.data.GetColorImageReq;
import com.melodigm.post.protocol.data.GetColorImageRes;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.RegPostDataReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.search.SearchLocationActivity;
import com.melodigm.post.search.SearchOstActivity;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.ImageUtil;
import com.melodigm.post.util.LocationUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.ColorIndicator;
import com.melodigm.post.widget.ColorIndicatorView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.hashtaglink.Hashtag;
import com.melodigm.post.widget.parallaxscroll.ParallaxImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RegistRadioActivity extends BaseActivity implements IOnHandlerMessage, OnMapReadyCallback, MediaPlayer.OnCompletionListener {
    private static final int REC_STOP = 0;
    private static final int RECORDING = 1;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;
    private static final int REC_MAX_TIME = 180000;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private int mRecState = REC_STOP;
    private int mPlayerState = PLAY_STOP;
    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;
    private String mFilePath, mFileName = null;
    private SeekBar mRecProgressBar, mPlayProgressBar;

    private RegPostDataReq regPostDataReq = null;
    private GetColorImageRes getColorImageRes;
    private ColorItem mColorItem;
    private LinearLayout mChoiceColorLayout, mChoiceColorLayoutFooter;
    private ColorIndicatorView mColorIndicatorView;
    private boolean isKeyBoardVisible;
    private int keyboardHeight;
    private boolean isHashTagSetting = false;
    private String mICI = "", mFID = "";
    private int radioRuntime = 0;

    private PostDialog mChoicePictureDialog;
    private RelativeLayout rootLayout, mapLayout, btnPostColorLayout, btnPopupCloseLayout, btnPostTagAdd, btnPostImageAdd, rlPostAlignBtn;
    private EditText etPostSubject, etPostContent;
    private LinearLayout btnPostLocationAdd, llPostColorLayout, llPostSubjectUnderLine;
    private LetterSpacingTextView tvPostLocationName, tvRadioRec, tvRadioPlay, tvRadioRecDuration, tvRadioPlayDuration;
    private ParallaxImageView ivBackground;
    private ImageView ivRadioHeaderCircle, ivPostColorCirCle, ivPostLocationAddImage, ivPostAlignImage, ivPostTagAddImage, ivPostImageAddImage, ivRadioRecCircle;
    private String[] mContentBitmapUrl = new String[2];
    private String[] mContentKey = new String[]{Constants.REQUEST_FILE_USE_TYPE_USER_BACK, Constants.REQUEST_FILE_USE_TYPE_MAP_BACK, ""};
    private Bitmap[] mContentBitmap = new Bitmap[2];
    private boolean isRecording = false;

    private LinearLayout contentLayout, ostBeforeLayout, ostAfterLayout, llControlLayout, radioRecorderLayout, radioPlayerLayout, radioReRecordLayout, recordingLayout;
    private RelativeLayout btnOstSearch, btnOstSearchRetry, btnRadioRecord, btnRadioPlay, btnRadioPlayBig, btnRecordRetry, btnRecordComplete, btnRadioReRecord;
    private CircularImageView ivOstImage;
    private ImageView ivRadioPlay, btnRadioRecordImage, btnRadioPlayBigImage, btnRadioRecordEffect;
    private TextView tvSongName, tvArtiName;
    private View vBlockLayout;
    private ArrayList<ColorItem> arrColorItem;
    private OstDataItem mOstDataItem;

    private String mPostAlign = Constants.REQUEST_POST_PST_TYPE_LEFT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_radio);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        arrColorItem = mdbHelper.getAllPostColors();
        mdbHelper.close();

        mFilePath = getDir(Constants.SERVICE_VOICE_FILE_PATH, MODE_PRIVATE).getAbsolutePath();
        mFileName = Constants.SERVICE_VOICE_FILE_NAME;

        setDisplay();
    }

    @Override
    public void onPause() {
        // 배경이미지 센서 제거
        if (ivBackground != null) ivBackground.unregisterSensorManager();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // 배경이미지 센서 등록
        if (ivBackground != null) ivBackground.registerSensorManager();
    }

    @Override
    protected void onDestroy() {
        File file = new File(Environment.getExternalStorageDirectory(), Constants.SERVICE_POST_FILE_NAME);
        boolean isDelete = file.delete();
        LogUtil.e("기존 POST 이미지 삭제 : " + isDelete);
        super.onDestroy();
    }

    private void setDisplay() {
        regPostDataReq = new RegPostDataReq();

        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(R.drawable.icon_top_post);
        setHeaderCheckImage();

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        contentLayout = (LinearLayout)findViewById(R.id.contentLayout);
        ivBackground = (ParallaxImageView)findViewById(R.id.ivBackground);
        mapLayout = (RelativeLayout)findViewById(R.id.mapLayout);
        llControlLayout = (LinearLayout)findViewById(R.id.llControlLayout);
        vBlockLayout = findViewById(R.id.vBlockLayout);

        mChoiceColorLayout = (LinearLayout)findViewById(R.id.choiceColorLayout);
        mChoiceColorLayoutFooter = (LinearLayout)findViewById(R.id.llChoiceColorFooter);
        mColorIndicatorView = (ColorIndicatorView)findViewById(R.id.choiceColorIndicator);
        ivRadioHeaderCircle = (ImageView)findViewById(R.id.ivRadioHeaderCircle);
        ivRadioHeaderCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFF65857")));

        etPostSubject = (EditText)findViewById(R.id.etPostSubject);
        etPostSubject.setEnabled(false);
        llPostSubjectUnderLine = (LinearLayout)findViewById(R.id.llPostSubjectUnderLine);
        llPostColorLayout = (LinearLayout)findViewById(R.id.llPostColorLayout);
        btnPostColorLayout = (RelativeLayout)findViewById(R.id.btnPostColorLayout);
        ivPostColorCirCle = (ImageView)findViewById(R.id.ivPostColorCirCle);
        ivPostColorCirCle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFFFFFFF")));
        etPostContent = (EditText)findViewById(R.id.etPostContent);
        etPostContent.setEnabled(false);
        btnPopupCloseLayout = (RelativeLayout)findViewById(R.id.btnPopupCloseLayout);
        btnPostLocationAdd = (LinearLayout)findViewById(R.id.btnPostLocationAdd);
        ivPostLocationAddImage = (ImageView)findViewById(R.id.ivPostLocationAddImage);
        tvPostLocationName = (LetterSpacingTextView)findViewById(R.id.tvPostLocationName);
        tvPostLocationName.setText(getString(R.string.common_somewhere));
        tvPostLocationName.setSpacing(Constants.TEXT_VIEW_SPACING);
        rlPostAlignBtn = (RelativeLayout)findViewById(R.id.rlPostAlignBtn);
        ivPostAlignImage = (ImageView)findViewById(R.id.ivPostAlignImage);
        btnPostTagAdd = (RelativeLayout)findViewById(R.id.btnPostTagAdd);
        ivPostTagAddImage = (ImageView)findViewById(R.id.ivPostTagAddImage);
        btnPostImageAdd = (RelativeLayout)findViewById(R.id.btnPostImageAdd);
        ivPostImageAddImage = (ImageView)findViewById(R.id.ivPostImageAddImage);

        btnPostColorLayout.setOnClickListener(onClickListener);
        btnPopupCloseLayout.setOnClickListener(onClickListener);
        btnPostLocationAdd.setOnClickListener(onClickListener);

        rlPostAlignBtn.setOnClickListener(onClickListener);
        btnPostTagAdd.setOnClickListener(null);
        btnPostTagAdd.setAlpha(0.2f);
        btnPostImageAdd.setOnClickListener(onClickListener);

        mColorIndicatorView.setOnChangeTabListener(new ColorIndicator() {
            @Override
            public void changeTabIndicator(ColorItem item) {
                mColorItem = item;
                ivPostColorCirCle.setBackground(new ColorCircleDrawable(Integer.parseInt(mColorItem.getCOLOR_CODE(), 16) + 0xFF000000));
                etPostSubject.setTextColor(Integer.parseInt(mColorItem.getCOLOR_CODE(), 16) + 0xFF000000);
                mICI = item.getICI();

                if ("FFFFFF".equals(mColorItem.getCOLOR_CODE().toUpperCase())) {
                    ivOstImage.setBorderColor(android.R.color.transparent);
                    ivOstImage.setBorderWidth(0.0f);
                } else {
                    ivOstImage.setBorderColor(Color.parseColor("#FF" + mColorItem.getCOLOR_CODE()));
                    ivOstImage.setBorderWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics()));
                }

                getData(Constants.QUERY_COLOR_RANDOM_IMAGE);
            }
        });

        if (mColorIndicatorView != null && arrColorItem.size() > 0) {
            mColorIndicatorView.addAllItems(arrColorItem);
            mColorItem = arrColorItem.get(0);
        }

        etPostContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(s.toString(), '#');
                if (hashtagSpans.size() > 0)
                    ivPostTagAddImage.setImageResource(R.drawable.bt_hash_rel);
                else
                    ivPostTagAddImage.setImageResource(R.drawable.bt_hash_nor);

                if (s.length() > 0) {
                    char c;
                    c = s.charAt(s.length() - 1);

                    if (c == '\n' || c == ' ') {
                        LogUtil.e(String.valueOf(c));

                        if (isHashTagSetting) {
                            isHashTagSetting = false;
                        } else {
                            isHashTagSetting = true;

                            SpannableString spannableString = new SpannableString(s.toString());

                            for (int i = 0; i < hashtagSpans.size(); i++) {
                                int[] span = hashtagSpans.get(i);
                                int hashTagStart = span[0];
                                int hashTagEnd = span[1];

                                if (hashTagStart >= 0 && hashTagEnd >= 0)
                                    spannableString.setSpan(new Hashtag(mContext, mHandler), hashTagStart, hashTagEnd, 0);
                            }
                            etPostContent.setText(spannableString);
                            etPostContent.setSelection(etPostContent.length());
                            etPostContent.requestFocus();
                        }
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        etPostSubject.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean focus) {
                if (focus) {
                    btnPostColorLayout.setVisibility(View.VISIBLE);
                } else {
                    if (mChoiceColorLayout.getVisibility() == View.VISIBLE) mChoiceColorLayout.setVisibility(View.GONE);
                    btnPostColorLayout.setVisibility(View.GONE);
                }
            }
        });

        etPostContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean focus) {
                if (focus) {
                    btnPostTagAdd.setOnClickListener(onClickListener);
                    btnPostTagAdd.setAlpha(1.0f);
                } else {
                    btnPostTagAdd.setOnClickListener(null);
                    btnPostTagAdd.setAlpha(0.2f);
                }
            }
        });

        // OST 검색 전 레이아웃
        ostBeforeLayout = (LinearLayout) findViewById(R.id.ostBeforeLayout);
        btnOstSearch = (RelativeLayout) findViewById(R.id.btnOstSearch);

        // OST 검색 후 레이아웃
        ostAfterLayout = (LinearLayout) findViewById(R.id.ostAfterLayout);
        ivOstImage = (CircularImageView) findViewById(R.id.ivOstImage);
        tvSongName = (TextView) findViewById(R.id.tvSongName);
        tvArtiName = (TextView) findViewById(R.id.tvArtiName);
        btnOstSearchRetry = (RelativeLayout) findViewById(R.id.btnOstSearchRetry);
        btnOstSearchRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchOstActivity.class);
                startActivityForResult(intent, Constants.QUERY_OST_SEARCH);
            }
        });

        // RADIO 녹음 레이아웃
        radioRecorderLayout = (LinearLayout) findViewById(R.id.radioRecorderLayout);
        radioPlayerLayout = (LinearLayout) findViewById(R.id.radioPlayerLayout);
        recordingLayout = (LinearLayout) findViewById(R.id.recordingLayout);
        radioReRecordLayout = (LinearLayout) findViewById(R.id.radioReRecordLayout);
        mRecProgressBar = (SeekBar) findViewById(R.id.recProgressBar);
        // 녹음시간 최대 3분 (ms)
        mRecProgressBar.setMax(REC_MAX_TIME);
        mPlayProgressBar = (SeekBar) findViewById(R.id.playProgressBar);
        btnRadioRecordImage = (ImageView) findViewById(R.id.btnRadioRecordImage);
        btnRadioRecord = (RelativeLayout) findViewById(R.id.btnRadioRecord);
        btnRadioRecord.setOnClickListener(onClickListener);
        btnRadioPlay = (RelativeLayout) findViewById(R.id.btnRadioPlay);
        btnRadioPlay.setOnClickListener(onClickListener);
        btnRadioPlayBig = (RelativeLayout) findViewById(R.id.btnRadioPlayBig);
        btnRadioPlayBig.setOnClickListener(onClickListener);
        btnRadioPlayBigImage = (ImageView) findViewById(R.id.btnRadioPlayBigImage);

        btnRadioRecordEffect = (ImageView) findViewById(R.id.btnRadioRecordEffect);
        btnRadioRecordEffect.setBackground(new ColorCircleDrawable(Color.parseColor("#80F65857")));

        ivRadioRecCircle = (ImageView) findViewById(R.id.ivRadioRecCircle);
        ivRadioRecCircle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFF65857")));
        ivRadioRecCircle.setAlpha(0.5f);

        ivRadioPlay = (ImageView) findViewById(R.id.ivRadioPlay);

        tvRadioRec = (LetterSpacingTextView) findViewById(R.id.tvRadioRec);
        tvRadioPlay = (LetterSpacingTextView) findViewById(R.id.tvRadioPlay);
        tvRadioRecDuration = (LetterSpacingTextView) findViewById(R.id.tvRadioRecDuration);
        tvRadioPlayDuration = (LetterSpacingTextView) findViewById(R.id.tvRadioPlayDuration);
        tvRadioRec.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvRadioPlay.setSpacing(Constants.TEXT_VIEW_SPACING);
        tvRadioRecDuration.setSpacing(0);
        tvRadioPlayDuration.setSpacing(0);
        tvRadioRec.setText(getString(R.string.rec));
        tvRadioPlay.setText(getString(R.string.play));
        tvRadioRecDuration.setText(getString(R.string.common_define_rec_time));
        tvRadioPlayDuration.setText(getString(R.string.common_define_play_time));

        btnRecordRetry = (RelativeLayout) findViewById(R.id.btnRecordRetry);
        btnRecordComplete = (RelativeLayout) findViewById(R.id.btnRecordComplete);
        btnRadioReRecord = (RelativeLayout) findViewById(R.id.btnRadioReRecord);
        btnRadioReRecord.setOnClickListener(onClickListener);
        updateRadioButton();

        checkKeyboardHeight(rootLayout);
        getData(Constants.QUERY_COLOR_RANDOM_IMAGE);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                map.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    public void onSnapshotReady(Bitmap bitmap) {
                        LogUtil.e("SnapShot Call Back");
                        mContentBitmap[1] = bitmap;
                        mContentBitmapUrl[1] = "Snapshot.jpg";
                        new Thread(registThread).start();
                    }
                });
            }
        });

        /**
         * Zoom Level
         *
         * 1: World
         * 5: Landmass/continent
         * 10: City
         * 15: Streets
         * 20: Buildings
         */
        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(regPostDataReq.getLOCA_LAT()), Double.valueOf(regPostDataReq.getLOCA_LNG()))).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_pin)));
        marker.showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(regPostDataReq.getLOCA_LAT()), Double.valueOf(regPostDataReq.getLOCA_LNG())), 15));
        //map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    private void getData(int queryType) {
        if (queryType == Constants.QUERY_COLOR_RANDOM_IMAGE) {
            new Thread(getColorImageThread).start();
        } else if (queryType == Constants.QUERY_WRITE_POST_DATA) {
            if (!isFinishing()) {
                if(mProgressDialog != null) {
                    mProgressDialog.showDialog(mContext);
                }
            }

            if (CommonUtil.isNotNull(regPostDataReq.getPLACE())) {
                mapLayout.setVisibility(View.VISIBLE);
                ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            } else {
                new Thread(registThread).start();
            }
        }
    }

    private Runnable getColorImageThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(mContext);

            try {
                GetColorImageReq getColorImageReq = new GetColorImageReq();
                getColorImageReq.setICI(mICI);
                getColorImageRes = request.getColorImage(getColorImageReq);
                mHandler.sendEmptyMessage(Constants.QUERY_COLOR_RANDOM_IMAGE);
            } catch(RequestException e) {
            } catch(POSTException e) {
            }
        }
    };

    private Runnable registThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(mContext);

            try {
                String postContentText = etPostContent.getText().toString();
                ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(postContentText, '#');
                for(int i = 0; i < hashtagSpans.size(); i++) {
                    int[] span = hashtagSpans.get(i);
                    int hashTagStart = span[0];
                    int hashTagEnd = span[1];

                    regPostDataReq.setHASH_TAG(postContentText.substring(hashTagStart, hashTagEnd).replaceAll("#", ""));
                }

                regPostDataReq.setPOST_SUBJ(etPostSubject.getText().toString());
                regPostDataReq.setPOST_CONT(etPostContent.getText().toString());
                regPostDataReq.setPOST_PST_TYPE(mPostAlign);
                regPostDataReq.setPOST_TYPE(Constants.REQUEST_TYPE_RADIO);
                regPostDataReq.setDISP_TYPE(Constants.REQUEST_DISP_TYPE_PUBLIC);
                regPostDataReq.setICI(mColorItem.getICI());
                regPostDataReq.setRADIO_RUNTIME(radioRuntime);
                regPostDataReq.setFID(mFID);

                request.regPost(regPostDataReq, mContentBitmapUrl, mContentKey, mContentBitmap);
                mHandler.sendEmptyMessage(Constants.QUERY_WRITE_POST_DATA);
            } catch(RequestException e) {
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
            } catch(POSTException e) {
                mPOSTException = e;

                if (POSTException.SW_UPDATE_NEEDED.equals(e.getCode())) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_NEED);
                } else if (POSTException.SW_UPDATE_SUPPORT.equals(e.getCode())) {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT);
                } else {
                    mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_POST);
                }
            }
        }
    };

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            // POST/RADIO 칼러 랜덤 배경 이미지 요청 성공 Handler
            case Constants.QUERY_COLOR_RANDOM_IMAGE:
                mFID = getColorImageRes.getFID();

                if (mContentBitmap[0] == null && CommonUtil.isNull(mContentBitmapUrl[0])) {
                    mGlideRequestManager
                        .load(getColorImageRes.getBG_PIC_PATH())
                        .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
                        .animate(animationObject)
                        .into(ivBackground);
                }
                break;
            // 위치 서비스 조회 성공 후 Handler
            case Constants.QUERY_LOCATION_CHANGE:
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, msg.getData().getString("USER_LAT", ""));
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, msg.getData().getString("USER_LNG", ""));
                mLocationUtil.stopLocationUpdate();

                startActivityForResult(new Intent(mContext, SearchLocationActivity.class), Constants.QUERY_LOCATION_SEARCH);
                break;
            case Constants.QUERY_WRITE_POST_DATA:
                setResult(Constants.RESULT_SUCCESS);
                finish();
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT :
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.contentGalleryLayout:
                    if(mChoicePictureDialog != null) mChoicePictureDialog.dismiss();
                    getImgFromPhone();
                    break;
                case R.id.contentCameraLayout:
                    if(mChoicePictureDialog != null) mChoicePictureDialog.dismiss();
                    getImgFromCamera();
                    break;
                case R.id.btnPostColorLayout:
                    if (mChoiceColorLayout.getVisibility() == View.GONE) {
                        Animation animation = new AlphaAnimation(0.0f, 1.0f);
                        animation.setDuration(500);
                        mChoiceColorLayout.setVisibility(View.VISIBLE);
                        mChoiceColorLayout.setAnimation(animation);
                    } else {
                        Animation animation = new AlphaAnimation(1.0f, 0.0f);
                        animation.setDuration(500);
                        mChoiceColorLayout.setVisibility(View.GONE);
                        mChoiceColorLayout.setAnimation(animation);
                    }
                    break;
                // 위치 등록 onClick
                case R.id.btnPostLocationAdd:
                    // 위치 서비스 설정
                    mLocationUtil = new LocationUtil(mContext, mHandler);
                    if (mLocationUtil.isLocationEnabled()) {
                        getGlobalData(Constants.QUERY_LOCATION_CHANGE);
                    } else {
                        if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                        mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_LOCATION_SERVICE, onClickListener);
                        mPostDialog.show();
                    }
                    break;
                // 위치 서비스 설정 onClick
                case R.id.btnLocationServiceSetting:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, Constants.QUERY_LOCATION_SERVICE_SETTING);
                    break;
                // 정렬 아이콘 onClick
                case R.id.rlPostAlignBtn:
                    if (Constants.REQUEST_POST_PST_TYPE_LEFT.equals(mPostAlign)) {
                        mPostAlign = Constants.REQUEST_POST_PST_TYPE_CENTER;
                        ivPostAlignImage.setImageResource(R.drawable.bt_align_c);
                        etPostSubject.setGravity(Gravity.TOP|Gravity.CENTER);
                        llPostColorLayout.setGravity(Gravity.END);
                        llPostSubjectUnderLine.setGravity(Gravity.CENTER);
                        etPostContent.setGravity(Gravity.TOP|Gravity.CENTER);
                    } else if (Constants.REQUEST_POST_PST_TYPE_CENTER.equals(mPostAlign)) {
                        /*mPostAlign = Constants.REQUEST_POST_PST_TYPE_RIGHT;
                        ivPostAlignImage.setImageResource(R.drawable.bt_align_r);
                        etPostSubject.setGravity(Gravity.TOP|Gravity.END);
                        llPostColorLayout.setGravity(Gravity.START);
                        llPostSubjectUnderLine.setGravity(Gravity.END);
                        etPostContent.setGravity(Gravity.TOP|Gravity.END);*/
                        mPostAlign = Constants.REQUEST_POST_PST_TYPE_LEFT;
                        ivPostAlignImage.setImageResource(R.drawable.bt_align_l);
                        etPostSubject.setGravity(Gravity.TOP|Gravity.START);
                        llPostColorLayout.setGravity(Gravity.END);
                        llPostSubjectUnderLine.setGravity(Gravity.START);
                        etPostContent.setGravity(Gravity.TOP|Gravity.START);
                    } else if (Constants.REQUEST_POST_PST_TYPE_RIGHT.equals(mPostAlign)) {
                        mPostAlign = Constants.REQUEST_POST_PST_TYPE_LEFT;
                        ivPostAlignImage.setImageResource(R.drawable.bt_align_l);
                        etPostSubject.setGravity(Gravity.TOP|Gravity.START);
                        llPostColorLayout.setGravity(Gravity.END);
                        llPostSubjectUnderLine.setGravity(Gravity.START);
                        etPostContent.setGravity(Gravity.TOP|Gravity.START);
                    }
                    break;
                // HashTag 등록 onClick
                case R.id.btnPostTagAdd:
                    int start =etPostContent.getSelectionStart();
                    etPostContent.getText().insert(start, "#");

                    ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(etPostContent.getText().toString(), '#');
                    SpannableString spannableString = new SpannableString(etPostContent.getText().toString());

                    for (int i = 0; i < hashtagSpans.size(); i++) {
                        int[] span = hashtagSpans.get(i);
                        int hashTagStart = span[0];
                        int hashTagEnd = span[1];

                        if (hashTagStart >= 0 && hashTagEnd >= 0)
                            spannableString.setSpan(new Hashtag(mContext, mHandler), hashTagStart, hashTagEnd, 0);
                    }

                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CCD73D66")), etPostContent.getText().toString().length() - 1, etPostContent.getText().toString().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    etPostContent.setText(spannableString);
                    etPostContent.setSelection(start + 1);
                    break;
                // 이미지 등록 onClick
                case R.id.btnPostImageAdd :
                    getImgFromPhone();
                    /*mChoicePictureDialog = null;
                    mChoicePictureDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CHOICE_PICTURE, onClickListener);
                    mChoicePictureDialog.show();*/
                    break;
                // RADIO REC onClick
                case R.id.btnRadioRecord:
                    mBtnStartRecOnClick();
                    break;
                // RADIO PLAY onClick
                case R.id.btnRadioPlay:
                case R.id.btnRadioPlayBig:
                    mBtnStartPlayOnClick();
                    break;
                // RADIO 다시 녹음 / 다시 onClick
                case R.id.btnRadioReRecord:
                case R.id.btnRecordRetry:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_CONFIRM, this, mContext.getResources().getString(R.string.dialog_confirm_delete_voice));
                    mPostDialog.show();
                    break;
                // RADIO 다시 확인 onClick
                case R.id.btnConfirmConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    isRecording = false;
                    mContentKey[2] = "";
                    updateRadioButton();
                    break;
                // RADIO 완료 onClick
                case R.id.btnRecordComplete:
                    mContentKey[2] = Constants.REQUEST_FILE_USE_TYPE_VOICE;

                    btnOstSearch.setAlpha(1.0f);
                    btnOstSearch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, SearchOstActivity.class);
                            startActivityForResult(intent, Constants.QUERY_OST_SEARCH);
                        }
                    });

                    updateRadioButton();
                    break;
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.btnHeaderCheck :
                    getData(Constants.QUERY_WRITE_POST_DATA);
                    break;
                // 안내 타입의 확인 onClick
                case R.id.btnInfoConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    break;
                case R.id.btnPopupCloseLayout :
                    Animation animation = new AlphaAnimation(1.0f, 0.0f);
                    animation.setDuration(500);
                    mChoiceColorLayout.setVisibility(View.GONE);
                    mChoiceColorLayout.setAnimation(animation);
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 갤러리 이미지 선택 후 ActivityResult
            case Constants.QUERY_CHOICE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {   // 갤러리에서 이미지 가져와서 비트맵으로 셋팅
                    try {
                        mContentBitmap[0] = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + Constants.SERVICE_POST_FILE_NAME);
                        mContentBitmapUrl[0] = Constants.SERVICE_POST_FILE_NAME;

                        ivBackground.setImageBitmap(mContentBitmap[0]);
                        ivPostImageAddImage.setImageResource(R.drawable.bt_img_rel);
                    } catch (Exception e) {
                        LogUtil.e(e.getMessage());
                        mContentBitmap[0] = null;
                        mContentBitmapUrl[0] = "";
                    }
                }
                break;
            // 카메라 이미지 선택 후 ActivityResult
            case Constants.QUERY_CHOICE_CAMERA:
                if(resultCode == RESULT_OK) {
                    cropImage(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), Constants.SERVICE_POST_FILE_NAME)));
                }
                break;
            // 이미지 Crop 후 ActivityResult
            case Constants.QUERY_CROP_IMAGE:
                try {
                    // 대부분의 기기가 사진 촬영시에 기기의 회전율을 고려하지 않고 바로 저장해 버린다는 문제를 재회전을 통해 복구하는 로직을 추가
                    ExifInterface exif = new ExifInterface(Environment.getExternalStorageDirectory() + "/" + Constants.SERVICE_POST_FILE_NAME);
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = ImageUtil.exifOrientationToDegrees(exifOrientation);
                    mContentBitmap[0] = ImageUtil.rotate(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + Constants.SERVICE_POST_FILE_NAME), exifDegree);
                    mContentBitmapUrl[0] = Constants.SERVICE_POST_FILE_NAME;

                    ivBackground.setImageBitmap(mContentBitmap[0]);
                    ivPostImageAddImage.setImageResource(R.drawable.bt_img_rel);
                } catch (Exception e) {
                    if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_IMAGE_UPLOAD, false).show();}
                    mContentBitmap[0] = null;
                    mContentBitmapUrl[0] = "";
                }
                break;
            // 위치 서비스 설정 후 ActivityResult
            case Constants.QUERY_LOCATION_SERVICE_SETTING:
                if (mLocationUtil.isLocationEnabled()) {
                    getGlobalData(Constants.QUERY_LOCATION_CHANGE);
                }
                break;
            // 위치 정보 조회 후 ActivityResult
            case Constants.QUERY_LOCATION_SEARCH:
                if (resultCode == RESULT_OK) {
                    String LOCA_LAT = data.getStringExtra("LOCA_LAT");
                    String LOCA_LNG = data.getStringExtra("LOCA_LNG");
                    String PLACE = data.getStringExtra("PLACE");

                    regPostDataReq.setLOCA_LAT(LOCA_LAT);
                    regPostDataReq.setLOCA_LNG(LOCA_LNG);
                    regPostDataReq.setPLACE(PLACE);

                    ivPostLocationAddImage.setImageResource(R.drawable.icon_pin_rel);

                    if (CommonUtil.isNotNull(LOCA_LAT) && CommonUtil.isNotNull(LOCA_LNG) && CommonUtil.isNull(PLACE)) {
                        tvPostLocationName.setText(getString(R.string.common_show_km));
                    } else {
                        tvPostLocationName.setText(regPostDataReq.getPLACE());
                    }

                    tvPostLocationName.setTextColor(Color.parseColor("#FFFFCC4F"));

                    if (CommonUtil.isNull(LOCA_LAT) && CommonUtil.isNull(LOCA_LNG))
                        tvPostLocationName.setSpacing(Constants.TEXT_VIEW_SPACING);
                    else
                        tvPostLocationName.setSpacing(0);
                }
                break;
            // OST 조회 후 ActivityResult
            case Constants.QUERY_OST_SEARCH:
                if (resultCode == Constants.RESULT_SUCCESS) {
                    mOstDataItem = data.getParcelableExtra("OstDataItem");

                    if (mOstDataItem != null) {
                        ostBeforeLayout.setVisibility(View.GONE);
                        ostAfterLayout.setVisibility(View.VISIBLE);

                        mGlideRequestManager
                            .load(mOstDataItem.getALBUM_PATH())
                            .error(R.drawable.icon_album_dummy)
                            .override((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                            .into(ivOstImage);

                        tvSongName.setText(mOstDataItem.getSONG_NM());
                        tvArtiName.setText(mOstDataItem.getARTI_NM());

                        regPostDataReq.setSSI(mOstDataItem.getSSI());

                        etPostSubject.setAlpha(1.0f);
                        etPostSubject.setEnabled(true);
                        llPostSubjectUnderLine.setAlpha(1.0f);
                        etPostContent.setAlpha(1.0f);
                        etPostContent.setEnabled(true);
                        llControlLayout.setAlpha(1.0f);
                        vBlockLayout.setVisibility(View.GONE);
                        setHeaderCheckImage();
                    }
                }
                break;
        }
    }

    private void mBtnStartRecOnClick() {
        if (mRecState == REC_STOP) {
            mRecState = RECORDING;
            startRec();
            updateUI();
        } else if (mRecState == RECORDING) {
            mRecState = REC_STOP;
            stopRec();
            updateUI();
        }
    }

    private void mBtnStartPlayOnClick() {
        if (mPlayerState == PLAY_STOP) {
            mPlayerState = PLAYING;
            initMediaPlayer();
            startPlay();
            updateUI();
        } else if (mPlayerState == PLAYING) {
            mPlayerState = PLAY_PAUSE;
            pausePlay();
            updateUI();
        } else if (mPlayerState == PLAY_PAUSE) {
            mPlayerState = PLAYING;
            startPlay();
            updateUI();
        }
    }

    // 녹음시 SeekBar처리
    Handler mProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            mCurRecTimeMs = mCurRecTimeMs + 100;
            mCurProgressTimeDisplay = mCurProgressTimeDisplay + 100;

            // 녹음시간이 음수이면 정지버튼을 눌러 정지시켰음을 의미하므로 SeekBar는 그대로 정지시키고 레코더를 정지시킨다.
            if (mCurRecTimeMs < 0) {}
            // 녹음시간이 아직 최대녹음제한시간보다 작으면 녹음중이라는 의미이므로 SeekBar의 위치를 옮겨주고 1초 후에 다시 체크하도록 한다.
            else if (mCurRecTimeMs < REC_MAX_TIME) {
                mRecProgressBar.setProgress(mCurProgressTimeDisplay);
                mProgressHandler.sendEmptyMessageDelayed(0, 100);

                int amplitude = mRecorder.getMaxAmplitude();
                double amplitudeDb = 20 * Math.log10((double)Math.abs(amplitude) / 32768);
                float volume = Float.valueOf(String.valueOf(amplitudeDb));

                final int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, volume + 50, getResources().getDisplayMetrics());
                final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, volume + 50, getResources().getDisplayMetrics());

                final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { btnRadioRecordEffect.setLayoutParams(lp); }
                });

                if (mCurRecTimeMs % 1000 == 0) {
                    tvRadioRecDuration.setText("-" + DateUtil.getConvertMsToFormat((REC_MAX_TIME - mCurRecTimeMs) / 1000));

                    // 녹음이미지 점멸 이벤트
                    if (mCurRecTimeMs % 2000 == 0)
                        ivRadioRecCircle.setAlpha(0.5f);
                    else
                        ivRadioRecCircle.setAlpha(1.0f);
                }
            }
            // 녹음시간이 최대 녹음제한 시간보다 크면 녹음을 정지 시킨다.
            else {
                mBtnStartRecOnClick();
            }
        }
    };

    // 재생시 SeekBar 처리
    Handler mProgressHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            if (mPlayer == null) return;

            try {
                if (mPlayer.isPlaying()) {
                    mPlayProgressBar.setProgress(mPlayer.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                }
            } catch (IllegalStateException e) {
            } catch (Exception e) {}
        }
    };

    // 녹음시작
    private void startRec() {
        mCurRecTimeMs = 0;
        mCurProgressTimeDisplay = 0;

        // SeekBar의 상태를 1초후 체크 시작
        mProgressHandler.sendEmptyMessageDelayed(0, 100);

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.reset();
        } else {
            mRecorder.reset();
        }

        try {
            //오디오 파일 생성
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(mFilePath + "/" + mFileName);
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            LogUtil.e(e.getMessage());
        } catch (IOException e) {
            LogUtil.e(e.getMessage());
        }
    }

    // 녹음정지
    private void stopRec() {
        try {
            mRecorder.stop();
            File voiceFile = getDir(Constants.SERVICE_VOICE_FILE_PATH, MODE_PRIVATE);
            LogUtil.file(voiceFile);

            if (mCurRecTimeMs < 5000) {
                ivRadioRecCircle.setAlpha(0.5f);
                mRecProgressBar.setProgress(0);
                tvRadioRecDuration.setText(getString(R.string.common_define_rec_time));

                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onClickListener, mContext.getResources().getString(R.string.msg_required_ost_rec_time));
                mPostDialog.show();
            } else {
                isRecording = true;
                radioRuntime = mCurRecTimeMs;
                updateRadioButton();
            }
        } catch(Exception e) {
        } finally {
            mRecorder.release();
            mRecorder = null;
        }

        mCurRecTimeMs = -999;
        // SeekBar의 상태를 즉시 체크
        mProgressHandler.sendEmptyMessageDelayed(0, 0);
    }

    private void initMediaPlayer() {
        // 미디어 플레이어 생성
        if (mPlayer == null)
            mPlayer = new MediaPlayer();
        else
            mPlayer.reset();

        mPlayer.setOnCompletionListener(this);
        String fullFilePath = mFilePath + "/" + mFileName;

        try {
            mPlayer.setDataSource(fullFilePath);
            mPlayer.prepare();
            int point = mPlayer.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            tvRadioPlayDuration.setText(maxMinPointStr + maxSecPointStr);

            mPlayProgressBar.setProgress(0);
        } catch(Exception e) {
            LogUtil.e("미디어 플레이어 Prepare Error(" + fullFilePath + ") ==========> " + e);
        }
    }

    // 재생 시작
    private void startPlay() {
        LogUtil.e("startPlay().....");

        try {
            mPlayer.start();

            // SeekBar의 상태를 1초마다 체크
            mProgressHandler2.sendEmptyMessageDelayed(0, 100);
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }
    }

    private void pausePlay() {
        LogUtil.e("pausePlay().....");

        // 재생을 일시 정지하고
        mPlayer.pause();

        // 재생이 일시정지되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void stopPlay() {
        LogUtil.e("stopPlay().....");

        // 재생을 중지하고
        mPlayer.stop();

        // 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
    }

    private void releaseMediaPlayer() {
        LogUtil.e("releaseMediaPlayer().....");
        mPlayer.release();
        mPlayer = null;
        mPlayProgressBar.setProgress(0);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);

        updateUI();
    }

    private void updateUI() {
        if (mRecState == REC_STOP) {
            tvRadioRec.setTextColor(Color.parseColor("#80FFFFFF"));
            btnRadioRecordImage.setImageResource(R.drawable.bt_rad_rec);
            btnRadioRecordEffect.setVisibility(View.GONE);
            //mRecProgressBar.setProgress(0);
        } else if (mRecState == RECORDING) {
            tvRadioRec.setTextColor(Color.parseColor("#FFF65857"));
            btnRadioRecordImage.setImageResource(R.drawable.bt_rad_recstop);
            btnRadioRecordEffect.setVisibility(View.VISIBLE);
        }

        if (mPlayerState == PLAY_STOP) {
            tvRadioPlay.setText(getString(R.string.play));
            ivRadioPlay.setImageResource(R.drawable.bt_rad_play);
            btnRadioPlayBigImage.setImageResource(R.drawable.bt_rad_replay);
            mPlayProgressBar.setProgress(0);
        } else if (mPlayerState == PLAYING) {
            tvRadioPlay.setText(getString(R.string.stop));
            ivRadioPlay.setImageResource(R.drawable.bt_rad_pause);
            btnRadioPlayBigImage.setImageResource(R.drawable.bt_rad_recstop);
        } else if (mPlayerState == PLAY_PAUSE) {
            tvRadioPlay.setText(getString(R.string.play));
            ivRadioPlay.setImageResource(R.drawable.bt_rad_play);
            btnRadioPlayBigImage.setImageResource(R.drawable.bt_rad_replay);
        }
    }

    private void updateRadioButton() {
        // YCNOTE - ChildView getId to String
        LogUtil.e("contentLayout.getChildCount() : " + contentLayout.getChildCount());
        for ( int i = 0; i < contentLayout.getChildCount();  i++ ){
            View view = contentLayout.getChildAt(i);
            if (view.getId() == View.NO_ID)
                LogUtil.e("view id : NO_ID");
            else
                LogUtil.e("view id : " + view.getResources().getResourceEntryName(view.getId()));
            //view.setEnabled(false);
        }

        if (mContentKey[2].equals(Constants.REQUEST_FILE_USE_TYPE_VOICE)) {
            recordingLayout.setVisibility(View.INVISIBLE);
        } else {
            recordingLayout.setVisibility(View.VISIBLE);
        }

        if (isRecording) {
            mRecProgressBar.setProgress(0);
            tvRadioRecDuration.setText(getString(R.string.common_define_rec_time));
            radioRecorderLayout.setVisibility(View.GONE);
            radioPlayerLayout.setVisibility(View.VISIBLE);
            radioReRecordLayout.setVisibility(View.VISIBLE);

            btnRadioRecord.setVisibility(View.GONE);
            btnRadioPlayBig.setVisibility(View.VISIBLE);
            btnRecordRetry.setAlpha(1.0f);
            btnRecordComplete.setAlpha(1.0f);
            btnRecordRetry.setOnClickListener(onClickListener);
            btnRecordComplete.setOnClickListener(onClickListener);
        } else {
            mPlayProgressBar.setProgress(0);
            tvRadioPlayDuration.setText(getString(R.string.common_define_play_time));
            radioPlayerLayout.setVisibility(View.GONE);
            radioRecorderLayout.setVisibility(View.VISIBLE);
            radioReRecordLayout.setVisibility(View.INVISIBLE);

            btnRadioRecord.setVisibility(View.VISIBLE);
            btnRadioPlayBig.setVisibility(View.GONE);
            btnRecordRetry.setAlpha(0.3f);
            btnRecordComplete.setAlpha(0.3f);
            btnRecordRetry.setOnClickListener(null);
            btnRecordComplete.setOnClickListener(null);
        }

        setHeaderCheckImage();
    }

    private void setHeaderCheckImage() {
        if (CommonUtil.isNull(regPostDataReq.getSSI()) || CommonUtil.isNull(mContentKey[2])) {
            btnHeaderCheck.setAlpha(0.2f);
            btnHeaderCheck.setOnClickListener(null);
        } else {
            btnHeaderCheck.setAlpha(1.0f);
            btnHeaderCheck.setOnClickListener(onClickListener);
        }
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;
    private void checkKeyboardHeight(final View parentLayout) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                parentLayout.getWindowVisibleDisplayFrame(r);

                int screenHeight = parentLayout.getRootView().getHeight();
                int heightDifference = screenHeight - (r.bottom);

                previousHeightDiffrence = heightDifference;
                if (heightDifference > 100) {
                    isKeyBoardVisible = true;
                    changeKeyboardHeight(heightDifference);
                } else {
                    isKeyBoardVisible = false;
                }

                if (isKeyBoardVisible) {
                    mChoiceColorLayoutFooter.setVisibility(LinearLayout.VISIBLE);
                } else {
                    mChoiceColorLayoutFooter.setVisibility(LinearLayout.GONE);
                }
            }
        });
    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height
     *            minimum height by which we can make sure actual keyboard is
     *            open or not
     */
    private void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            mChoiceColorLayoutFooter.setLayoutParams(params);
        }
    }
}
