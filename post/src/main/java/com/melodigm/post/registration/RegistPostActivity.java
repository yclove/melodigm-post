package com.melodigm.post.registration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import com.melodigm.post.protocol.data.RegPostDataReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.search.SearchLocationActivity;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.ImageUtil;
import com.melodigm.post.util.LocationUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.ColorCircleDrawable;
import com.melodigm.post.widget.ColorIndicator;
import com.melodigm.post.widget.ColorIndicatorView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;
import com.melodigm.post.widget.hashtaglink.Hashtag;
import com.melodigm.post.widget.parallaxscroll.ParallaxImageView;

import java.io.File;
import java.util.ArrayList;

public class RegistPostActivity extends BaseActivity implements IOnHandlerMessage, OnMapReadyCallback {
    private RegPostDataReq regPostDataReq = null;
    private GetColorImageRes getColorImageRes;
    private ColorItem mColorItem;
    private LinearLayout mChoiceColorLayout, mChoiceColorLayoutFooter;
    private ColorIndicatorView mColorIndicatorView;
    private boolean isKeyBoardVisible;
    private int keyboardHeight;
    private boolean isHashTagSetting = false;
    private String mICI = "", mFID = "";

    private PostDialog mChoicePictureDialog;
    private RelativeLayout rootLayout, mapLayout, btnPostColorLayout, btnPopupCloseLayout, rlPostAlignBtn, btnPostTagAdd, btnPostImageAdd;
    private EditText etPostSubject, etPostContent;
    private LinearLayout btnPostLocationAdd, llPostSubjectUnderLine, llPostColorLayout;
    private LetterSpacingTextView tvPostLocationName;
    private ParallaxImageView ivBackground;
    private ImageView ivPostColorCirCle, ivPostLocationAddImage, ivPostAlignImage, ivPostTagAddImage, ivPostImageAddImage;
    private String[] mContentBitmapUrl = new String[2];
    private String[] mContentKey = new String[]{Constants.REQUEST_FILE_USE_TYPE_USER_BACK, Constants.REQUEST_FILE_USE_TYPE_MAP_BACK, ""};
    private Bitmap[] mContentBitmap = new Bitmap[2];

    private ArrayList<ColorItem> arrColorItem;

    private String mPostAlign = Constants.REQUEST_POST_PST_TYPE_LEFT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_post);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        arrColorItem = mdbHelper.getAllPostColors();
        mdbHelper.close();

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

    private void setHeaderCheckImage() {
        if (etPostContent.getText().toString().length() < 5) {
            btnHeaderCheck.setAlpha(0.2f);
            btnHeaderCheck.setOnClickListener(null);
        } else {
            btnHeaderCheck.setAlpha(1.0f);
            btnHeaderCheck.setOnClickListener(onClickListener);
        }
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(R.drawable.icon_top_post);

        regPostDataReq = new RegPostDataReq();

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
        ivBackground = (ParallaxImageView)findViewById(R.id.ivBackground);
        mapLayout = (RelativeLayout)findViewById(R.id.mapLayout);
        mChoiceColorLayout = (LinearLayout)findViewById(R.id.choiceColorLayout);
        mChoiceColorLayoutFooter = (LinearLayout)findViewById(R.id.llChoiceColorFooter);
        mColorIndicatorView = (ColorIndicatorView)findViewById(R.id.choiceColorIndicator);
        etPostSubject = (EditText)findViewById(R.id.etPostSubject);
        llPostSubjectUnderLine = (LinearLayout)findViewById(R.id.llPostSubjectUnderLine);
        llPostColorLayout = (LinearLayout)findViewById(R.id.llPostColorLayout);

        btnPostColorLayout = (RelativeLayout)findViewById(R.id.btnPostColorLayout);
        ivPostColorCirCle = (ImageView)findViewById(R.id.ivPostColorCirCle);
        ivPostColorCirCle.setBackground(new ColorCircleDrawable(Color.parseColor("#FFFFFFFF")));

        etPostContent = (EditText)findViewById(R.id.etPostContent);
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

        btnPopupCloseLayout.setOnClickListener(onClickListener);
        btnPostColorLayout.setOnClickListener(onClickListener);
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
                setHeaderCheckImage();
                ArrayList<int[]> hashtagSpans = CommonUtil.getSpans(s.toString(), '#');
                if (hashtagSpans.size() > 0)
                    ivPostTagAddImage.setImageResource(R.drawable.bt_hash_rel);
                else
                    ivPostTagAddImage.setImageResource(R.drawable.bt_hash_nor);

                if (s.length() > 0) {
                    char c;
                    c = s.charAt(s.length() - 1);

                    if (c == '\n' || c == ' ') {
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

        etPostContent.requestFocus();
        /*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etPostContent, InputMethodManager.SHOW_FORCED);*/

        checkKeyboardHeight(rootLayout);
        setHeaderCheckImage();
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
                regPostDataReq.setPOST_TYPE(Constants.REQUEST_TYPE_POST);
                regPostDataReq.setDISP_TYPE(Constants.REQUEST_DISP_TYPE_PUBLIC);
                regPostDataReq.setICI(mColorItem.getICI());
                regPostDataReq.setFID(mFID);

                request.regPost(regPostDataReq, mContentBitmapUrl, mContentKey, mContentBitmap);
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NON);
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
            case Constants.DIALOG_EXCEPTION_NON:
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
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.btnHeaderCheck :
                    getData(Constants.QUERY_WRITE_POST_DATA);
                    break;
                // 컬러 팝업 닫기 onClick
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
        }
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;
    private void checkKeyboardHeight(final View parentLayout) {
        /**
         * YCNOTE - ViewTreeObserver : 모든 뷰에는 ViewTreeObserver가 있으며 getViewTreeObserver()를 통해서 얻어 올 수 있다. 여기에 뷰와 관련된 interface를 제공하여 뷰의 상태를 전달 받을 수 있다. 주요 Listener는 다음과 같다.
         * OnGlobalFocusChangeListener – 뷰의 포커스 변경시 호출
         * OnGlobalLayoutListener – 뷰의 변경이 생기면 호출
         * OnPreDrawListener – 뷰가 그려지기전 호출
         *
         * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
         *      view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
         * } else {
         *      view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
         * }
         */
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
