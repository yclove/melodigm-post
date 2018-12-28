package com.melodigm.post.menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.CabinetDataItem;
import com.melodigm.post.protocol.data.RegCabinetDataReq;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistCabinetActivity extends BaseActivity implements IOnHandlerMessage {
    private CabinetDataItem mCabinetDataItem = null;
    private CircularImageView btnCabinetImage;
	private ImageView ivCabinetRoot;
    private EditText etCabinetName, etCabinetDesc;
    private String mContentBitmapUrl[] = new String[1];
    private String mContentKey[] = new String[]{"file"};
    private Bitmap mContentBitmap[] = new Bitmap[1];

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_regist_cabinet);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        Intent intent = getIntent();
        mCabinetDataItem = intent.getParcelableExtra("CabinetDataItem");

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME_CHECK, onClickListener);
        if (mCabinetDataItem == null) {
            setPostHeaderTitle(getResources().getString(R.string.title_regist_cabinet), false);
        } else {
            setPostHeaderTitle(getResources().getString(R.string.title_edit_cabinet), false);
        }

        // 보관함 배경 이미지
        ivCabinetRoot = (ImageView)findViewById(R.id.ivCabinetRoot);

        // 보관함 이미지
        btnCabinetImage = (CircularImageView)findViewById(R.id.btnCabinetImage);
        btnCabinetImage.setOnClickListener(onClickListener);

        // 보관함 이름 / 내용
        etCabinetName = (EditText)findViewById(R.id.etCabinetName);
        etCabinetDesc = (EditText)findViewById(R.id.etCabinetDesc);

        etCabinetName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                setHeaderCheckImage();
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
        
        if (mCabinetDataItem != null) {
            etCabinetName.setText(mCabinetDataItem.getUBX_NM());
            etCabinetDesc.setText(mCabinetDataItem.getDESCR());

            if (CommonUtil.isNotNull(mCabinetDataItem.getUBX_IMG())) {
                mGlideRequestManager
                    .load(mCabinetDataItem.getUBX_IMG())
                    .override(DeviceUtil.getScreenWidthInPXs(mContext), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 193, mContext.getResources().getDisplayMetrics()))
                    .into(ivCabinetRoot);

                mGlideRequestManager
                    .load(mCabinetDataItem.getUBX_IMG())
                    .error(R.drawable.icon_album_dummy)
                    .override((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()), (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, mContext.getResources().getDisplayMetrics()))
                    .into(btnCabinetImage);
            }
        }

        setHeaderCheckImage();
	}

    private void setHeaderCheckImage() {
        if (CommonUtil.isNotNull(etCabinetName.getText().toString())) {
            btnHeaderCheck.setAlpha(1.0f);
            btnHeaderCheck.setOnClickListener(onClickListener);
        } else {
            btnHeaderCheck.setAlpha(0.2f);
            btnHeaderCheck.setOnClickListener(null);
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
                    getData(Constants.QUERY_WRITE_CABINET);
                    break;
                // Header 닫기 onClick
                case R.id.btnHeaderClose:
                    finish();
                    break;
                // 보관함 이미지 onClick
                case R.id.btnCabinetImage:
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + Constants.SERVICE_CABINET_IMAGE_NAME);
                    boolean isDelete = file.delete();
                    LogUtil.e("기존 보관함 이미지 삭제 : " + isDelete);
                    getCabinetImgFromPhone();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + Constants.SERVICE_CABINET_IMAGE_NAME);
        boolean isDelete = file.delete();
        LogUtil.e("기존 보관함 이미지 삭제 : " + isDelete);
        super.onDestroy();
    }

    private void getData(int queryType, Object... args) {
        if (!isFinishing()) {
            if(mProgressDialog != null) {
                mProgressDialog.showDialog(mContext);
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_WRITE_CABINET) {
            thread = regCabinetDataThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread regCabinetDataThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    RegCabinetDataReq regCabinetDataReq = new RegCabinetDataReq();
                    if (mCabinetDataItem != null) {
                        regCabinetDataReq.setBXI(mCabinetDataItem.getBXI());
                        regCabinetDataReq.setUBX_NM(etCabinetName.getText().toString());
                        regCabinetDataReq.setDESCR(etCabinetDesc.getText().toString());
                        request.editCabinetData(regCabinetDataReq, mContentBitmapUrl, mContentKey, mContentBitmap);
                    } else {
                        regCabinetDataReq.setUBX_NM(etCabinetName.getText().toString());
                        regCabinetDataReq.setDESCR(etCabinetDesc.getText().toString());
                        request.regCabinetData(regCabinetDataReq, mContentBitmapUrl, mContentKey, mContentBitmap);
                    }
                    mHandler.sendEmptyMessage(Constants.QUERY_WRITE_CABINET);
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
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        switch(msg.what) {
            // 보관함 생성 / 수정 후 Handler
            case Constants.QUERY_WRITE_CABINET:
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);

        // 갤러리 이미지 리턴
        if (requestCode == Constants.QUERY_CHOICE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {   // 갤러리에서 이미지 가져와서 비트맵으로 셋팅
                try {
                    String filePath = Environment.getExternalStorageDirectory() + "/" + Constants.SERVICE_CABINET_IMAGE_NAME;
                    mContentBitmap[0] = BitmapFactory.decodeFile(filePath);
                    mContentBitmapUrl[0] = Constants.SERVICE_CABINET_IMAGE_NAME;

                    btnCabinetImage.setImageBitmap(mContentBitmap[0]);
                    if (mCabinetDataItem != null) {
                        ivCabinetRoot.setImageBitmap(mContentBitmap[0]);
                    }
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    mContentBitmap[0] = null;
                    mContentBitmapUrl[0] = "";
                }
            }
        }
    };

    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), Constants.SERVICE_CABINET_IMAGE_NAME);
            try {
                f.createNewFile();
            } catch (IOException e) {
                LogUtil.e(e.getMessage());
            }

            return f;
        } else
            return null;
    }

    public void getCabinetImgFromPhone() {
        Intent intent = new Intent(Intent.ACTION_PICK); // 또는 ACTION_GET_CONTENT
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra("crop", "true");
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());                         // 임시파일 생성
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString()); // 포맷방식

        startActivityForResult(intent, Constants.QUERY_CHOICE_PICTURE);
    }
}
