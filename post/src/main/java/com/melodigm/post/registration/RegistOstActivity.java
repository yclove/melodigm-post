package com.melodigm.post.registration;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.ColorItem;
import com.melodigm.post.protocol.data.OstDataItem;
import com.melodigm.post.protocol.data.RegOstDataReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.search.SearchOstActivity;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.PostDatabases;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;
import com.melodigm.post.widget.ColorIndicator;
import com.melodigm.post.widget.ColorIndicatorView;

import java.util.ArrayList;

public class RegistOstActivity extends BaseActivity implements IOnHandlerMessage {
    private RegOstDataReq regOstDataReq = null;
    private String mPOI = "", mBG_USER_PATH = "", mBG_PIC_PATH = "";
    private RelativeLayout btnOstSearch, btnOstSearchRetry;
    private ColorItem mColorItem;
    private ColorIndicatorView mColorIndicatorView;
    private EditText etOstSubject, etOstContent;
    private OstDataItem mOstDataItem;
    private LinearLayout contentLayout, ostBeforeLayout, ostAfterLayout, llControlLayout;
    private CircularImageView ivOstImage;
    private ImageView ivBackground;
    private TextView tvSongName, tvArtiName;
    private View vBlockLayout;
    private ArrayList<ColorItem> arrColorItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_ost);

        mContext = this;
        mHandler = new WeakRefHandler(this);

        mdbHelper = new PostDatabases(mContext);
        mdbHelper.open();
        arrColorItem = mdbHelper.getAllPostColors();
        mdbHelper.close();

        Intent intent = getIntent();
        mPOI = intent.getStringExtra("POI");
        mBG_USER_PATH = intent.getStringExtra("BG_USER_PATH");
        mBG_PIC_PATH = intent.getStringExtra("BG_PIC_PATH");

        setDisplay();
    }

    private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        setPostHeaderTitle(R.drawable.img_top_ost);

        contentLayout = (LinearLayout)findViewById(R.id.contentLayout);
        ivBackground = (ImageView)findViewById(R.id.ivBackground);
        llControlLayout = (LinearLayout)findViewById(R.id.llControlLayout);
        vBlockLayout = findViewById(R.id.vBlockLayout);

        String ostBackground = "";
        if (!"".equals(mBG_USER_PATH))
            ostBackground = mBG_USER_PATH;
        else
            ostBackground = mBG_PIC_PATH;

        mGlideRequestManager
            .load(ostBackground)
            .override(DeviceUtil.getScreenWidthInPXs(mContext), DeviceUtil.getScreenHeightInPXs(mContext))
            .animate(animationObject)
            .into(ivBackground);

        // OST 검색 전 레이아웃
        ostBeforeLayout = (LinearLayout)findViewById(R.id.ostBeforeLayout);
        btnOstSearch = (RelativeLayout)findViewById(R.id.btnOstSearch);
        btnOstSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchOstActivity.class);
                intent.putExtra("POI", mPOI);
                startActivityForResult(intent, Constants.QUERY_OST_SEARCH);
            }
        });

        // OST 검색 후 레이아웃
        ostAfterLayout = (LinearLayout)findViewById(R.id.ostAfterLayout);
        ivOstImage = (CircularImageView)findViewById(R.id.ivOstImage);
        tvSongName = (TextView)findViewById(R.id.tvSongName);
        tvArtiName = (TextView)findViewById(R.id.tvArtiName);
        btnOstSearchRetry = (RelativeLayout)findViewById(R.id.btnOstSearchRetry);
        btnOstSearchRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchOstActivity.class);
                intent.putExtra("POI", mPOI);
                startActivityForResult(intent, Constants.QUERY_OST_SEARCH);
            }
        });

        etOstContent = (EditText)findViewById(R.id.etOstContent);
        mColorIndicatorView = (ColorIndicatorView)findViewById(R.id.choiceColorIndicator);

        mColorIndicatorView.setOnChangeTabListener(new ColorIndicator() {
            @Override
            public void changeTabIndicator(ColorItem item) {
                mColorItem = item;
                if ("FFFFFF".equals(mColorItem.getCOLOR_CODE().toUpperCase())) {
                    ivOstImage.setBorderColor(android.R.color.transparent);
                    ivOstImage.setBorderWidth(0.0f);
                } else {
                    ivOstImage.setBorderColor(Color.parseColor("#FF" + mColorItem.getCOLOR_CODE()));
                    ivOstImage.setBorderWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics()));
                }
            }
        });

        if (mColorIndicatorView != null && arrColorItem.size() > 0) {
            mColorIndicatorView.addAllItems(arrColorItem);
            mColorItem = arrColorItem.get(0);
        }

        setDisplayHeaderCheck();
    }

    private void setDisplayHeaderCheck() {
        if (CommonUtil.isNull(tvSongName.getText().toString())) {
            btnHeaderCheck.setAlpha(0.3f);
            btnHeaderCheck.setOnClickListener(null);
        } else {
            btnHeaderCheck.setAlpha(1.0f);
            btnHeaderCheck.setOnClickListener(onClickListener);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // Header 뒤로가기 onClick
                case R.id.btnHeaderBack:
                    finish();
                    break;
                // Header 확인 onClick
                case R.id.btnHeaderCheck:
                    if (mOstDataItem != null) {
                        getData();
                    }
                    break;
            }
        }
    };

    private void getData() {
        if (!isFinishing()) {
            if (mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        new Thread(registThread).start();
    }

    private Runnable registThread = new Runnable() {
        public void run() {
            HPRequest request = new HPRequest(mContext);

            try {
                regOstDataReq = new RegOstDataReq();
                regOstDataReq.setPOI(mPOI);
                regOstDataReq.setSSI(mOstDataItem.getSSI());
                regOstDataReq.setICI(mColorItem.getICI());
                regOstDataReq.setCONT(etOstContent.getText().toString());
                request.regOst(regOstDataReq);
                mHandler.sendEmptyMessage(Constants.QUERY_WRITE_OST_DATA);
            } catch (RequestException e) {
                mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_REQUEST);
            } catch (POSTException e) {
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
        if (mProgressDialog != null) {
            mProgressDialog.dissDialog();
        }

        switch (msg.what) {
            case Constants.QUERY_WRITE_OST_DATA:
                setResult(Constants.RESULT_SUCCESS);
                finish();
                break;
            case Constants.DIALOG_EXCEPTION_REQUEST:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_POST:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_NEED:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
            case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT:
                if (!isFinishing()) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();
                }
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.QUERY_OST_SEARCH) {
            if (resultCode == Constants.RESULT_SUCCESS) {
                mOstDataItem = data.getParcelableExtra("OstDataItem");

                if (mOstDataItem != null) {
                    contentLayout.setAlpha(1.0f);
                    ostBeforeLayout.setVisibility(View.GONE);
                    ostAfterLayout.setVisibility(View.VISIBLE);

                    mGlideRequestManager
                        .load(mOstDataItem.getALBUM_PATH())
                        .error(R.drawable.icon_album_dummy)
                        .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                        .into(ivOstImage);

                    tvSongName.setText(mOstDataItem.getSONG_NM());
                    tvArtiName.setText(mOstDataItem.getARTI_NM());

                    llControlLayout.setAlpha(1.0f);
                    vBlockLayout.setVisibility(View.GONE);
                    setDisplayHeaderCheck();
                }
            }
        }
    }
}
