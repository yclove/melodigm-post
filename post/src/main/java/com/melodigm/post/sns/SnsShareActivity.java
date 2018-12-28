package com.melodigm.post.sns;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;
import com.melodigm.post.BaseActivity;
import com.melodigm.post.BaseApplication;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetActionLogReq;
import com.melodigm.post.protocol.data.GetPostPositionDataReq;
import com.melodigm.post.protocol.data.GetShareImageUploadDataRes;
import com.melodigm.post.protocol.data.PostDataItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.CommonUtil;
import com.melodigm.post.util.DMediaScanner;
import com.melodigm.post.util.DateUtil;
import com.melodigm.post.util.DeviceUtil;
import com.melodigm.post.util.ImageUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import com.melodigm.post.widget.EllipsizingTextView;
import com.melodigm.post.widget.LetterSpacingTextView;
import com.melodigm.post.widget.PostDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnsShareActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private String mPOST_TYPE, mSHARE_TYPE, mPOI;
    private PostDataItem mPostDataItem;
    private RelativeLayout contentLayout;
    private ImageView ivRoot;
    private LetterSpacingTextView tvPostSubject;
    private TextView tvTodaySubject;
    private EllipsizingTextView tvPostContent;
    private int defaultTextSize = 13;
    private int defaultImageSize = 0;
    private String shareLinkUrl;

    private int mFacebookRequestCode, mTwitterRequestCode;
    private CallbackManager mFacebookCallbackManager;
    private ShareDialog mFacebookShareDialog;

    private GetShareImageUploadDataRes mGetShareImageUploadDataRes;
    private KakaoLink mKakaoLink;
    private KakaoTalkLinkMessageBuilder mKakaoTalkLinkMessageBuilder;
    private String shareImagePath = "";
    private String[] mContentBitmapUrl = new String[1];
    private String[] mContentKey = new String[]{Constants.REQUEST_FILE_USE_TYPE_USER_BACK};
    private Bitmap[] mContentBitmap = new Bitmap[1];

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_share);
        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        // Obtain the shared Tracker instance.
        BaseApplication application = (BaseApplication)getApplication();
        mTracker = application.getDefaultTracker();

        Intent intent = getIntent();
        mPOST_TYPE = intent.getStringExtra("POST_TYPE");
        mSHARE_TYPE = intent.getStringExtra("SHARE_TYPE");
        mPOI = intent.getStringExtra("POI");

        // 페이스북
        FacebookSdk.sdkInitialize(getApplicationContext());
        mFacebookRequestCode = CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode();
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookShareDialog = new ShareDialog(this);
        mFacebookShareDialog.registerCallback(mFacebookCallbackManager, mFacebookCallback);

        // 카카오링크
        try {
            mKakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            mKakaoTalkLinkMessageBuilder = mKakaoLink.createKakaoTalkLinkMessageBuilder();
        } catch (KakaoParameterException e) {
            DeviceUtil.showToast(mContext, e.getMessage());
        }

        setDisplay();
	}

    private FacebookCallback<Sharer.Result> mFacebookCallback = new FacebookCallback<Sharer.Result>() {
        Bundle data = new Bundle();
        Message msg = new Message();

        @Override
        public void onSuccess(Sharer.Result sharerResult) {
            LogUtil.e("페이스북 ▶ onSuccess : " + sharerResult.getPostId());

            data.putString("message", getString(R.string.msg_share_success));
            data.putString("type", Constants.REQUEST_ACTION_TYPE_FACEBOOK);
            msg.setData(data);
            msg.what = Constants.QUERY_SNS_SHARE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onCancel() {
            data.putString("message", getString(R.string.msg_share_cancel));
            msg.setData(data);
            msg.what = Constants.QUERY_SNS_SHARE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onError(FacebookException e) {
            data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
            msg.setData(data);
            msg.what = Constants.QUERY_SNS_SHARE;
            mHandler.sendMessage(msg);
        }
    };

	private void setDisplay() {
        contentLayout = (RelativeLayout)findViewById(R.id.contentLayout);
        ivRoot = (ImageView)findViewById(R.id.ivRoot);
        tvPostSubject = (LetterSpacingTextView)findViewById(R.id.tvPostSubject);
        tvTodaySubject = (TextView)findViewById(R.id.tvTodaySubject);
        tvPostContent = (EllipsizingTextView)findViewById(R.id.tvPostContent);

        ViewGroup.LayoutParams lp = contentLayout.getLayoutParams();
        lp.height = DeviceUtil.getScreenWidthInPXs(mContext);
        defaultImageSize = lp.height;

        getData(Constants.QUERY_POST_DATA);
	}

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // 안내 타입의 확인 onClick
                case R.id.btnInfoConfirm:
                    if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
            // Header 확인 onClick
            case R.id.btnHeaderCheck:
                if (isSDCARDMOUNTED()) {
                    getData(Constants.QUERY_SNS_SHARE);
                }
                break;
            // Footer 마이너스 onClick
            case R.id.btnFooterMinus:
                defaultTextSize--;
                tvPostContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, defaultTextSize);
                break;
            // Footer 플러스 onClick
            case R.id.btnFooterPlus:
                defaultTextSize++;
                tvPostContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, defaultTextSize);
                break;
        }
    }

    private void getData(int queryType, Object... args) {
        if (queryType != Constants.QUERY_ACTION_LOG) {
            if (!isFinishing()) {
                if(mProgressDialog != null) {
                    mProgressDialog.showDialog(mContext);
                }
            }
        }

        // 이전 서버 통신이 있으면 모두 정지
        for(Map.Entry<Integer, RunnableThread> entry : mThreads.entrySet()){
            entry.getValue().getRunnable().stopRun();
        }
        mThreads.clear();

        RunnableThread thread = null;
        if (queryType == Constants.QUERY_POST_DATA) {
            thread = getPostDataThread();
        } else if (queryType == Constants.QUERY_SNS_SHARE) {
            switch (mSHARE_TYPE) {
                case "IMAGE":
                    LogUtil.e("이미지로 저장 공유 시작");
                    thread = getSnsShareImageThread();
                    break;
                case "FACEBOOK":
                    LogUtil.e("페이스북 공유 시작");
                    thread = getSnsShareFacebookThread();
                    break;
                case "TWITTER":
                    LogUtil.e("트위터 공유 시작");
                    thread = getSnsShareTwitterThread();
                    break;
                case "INSTAGRAM":
                    LogUtil.e("인스타그램 공유 시작");
                    thread = getSnsShareInstagramThread();
                    break;
                case "LINE":
                    LogUtil.e("라인 공유 시작");
                    thread = getSnsShareLineThread();
                    break;
                case "KAKAOTALK":
                    LogUtil.e("카카오톡 공유 시작");
                    if (args != null && args.length == 1 && args[0] instanceof String)
                        thread = getSnsShareKakaoLinkThread();
                    else
                        thread = getSnsShareImageUploadThread();
                    break;
            }
        } else if (queryType == Constants.QUERY_ACTION_LOG) {
            if (args != null && args.length > 0 && args[0] instanceof String) {
                thread = getActionLogThread((String)args[0]);
            }
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getPostDataThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetPostPositionDataReq getPostPositionDataReq = new GetPostPositionDataReq();
                    getPostPositionDataReq.setPOI(mPOI);
                    getPostPositionDataReq.setPOI_MOVE_FLAG("MF");
                    mPostDataItem = request.getPostPositionData(getPostPositionDataReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_POST_DATA);
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

    public RunnableThread getSnsShareImageThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory() + "/POST";
                String fileName = DateUtil.getCurrentDate("yyyyMMddHHmmss") + ".PNG";

                File dir = new File(filePath);
                if(!dir.exists()) {
                    dir.mkdirs();
                }

                OutputStream outStream;
                File file = new File(filePath, fileName);

                Bundle data = new Bundle();
                Message msg = new Message();
                try {
                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    DMediaScanner scanner = new DMediaScanner(mContext);
                    scanner.startScan(filePath + "/" + fileName);
                    data.putString("message", getString(R.string.msg_save_image_success));
                    data.putString("type", Constants.REQUEST_ACTION_TYPE_IMAGE);
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    data.putString("message", getString(R.string.msg_save_image_failed) + "\n" + e.getMessage());
                }
                msg.setData(data);
                msg.what = Constants.QUERY_SNS_SHARE;
                mHandler.sendMessage(msg);
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getSnsShareFacebookThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                /**
                 * contentURL: 공유될 링크
                 * contentTitle: 링크의 콘텐츠 제목
                 * imageURL: 게시물에 표시될 썸네일 이미지의 URL
                 * contentDescription: 일반적으로 2~4개의 문장으로 구성된 콘텐츠 설명
                 */
                String filePath = mContext.getDir(Constants.SERVICE_SHARE_FILE_PATH, mContext.MODE_PRIVATE).getAbsolutePath();
                String fileName = Constants.SERVICE_SHARE_FILE_NAME;

                OutputStream outStream;
                File file = new File(filePath, fileName);

                try {
                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    if (ShareDialog.canShow(SharePhotoContent.class)) {
                        Bitmap image = BitmapFactory.decodeFile(filePath + "/" + fileName);
                        SharePhoto photo = new SharePhoto.Builder()
                                .setBitmap(image)
                                .build();
                        SharePhotoContent content = new SharePhotoContent.Builder()
                                .addPhoto(photo)
                                .build();
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setContentTitle("Shared from nearbyme application")
                                .setContentDescription("This is a wonderful place")
                                .setContentUrl(Uri.parse("http://www.villathena.com/images/nearby/thumbs/le-bus-bleu-private-tours.jpg"))
                                .setImageUrl(Uri.fromFile(file))
                                .build();
                        mFacebookShareDialog.show(content);
                    } else {
                        Bundle data = new Bundle();
                        Message msg = new Message();
                        data.putString("message", getString(R.string.msg_share_failed));
                        msg.setData(data);
                        msg.what = Constants.QUERY_SNS_SHARE;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
                    msg.setData(data);
                    msg.what = Constants.QUERY_SNS_SHARE;
                    mHandler.sendMessage(msg);
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getSnsShareTwitterThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory() + "/POST";
                String fileName = Constants.SERVICE_SHARE_FILE_NAME;

                File dir = new File(filePath);
                if(!dir.exists()) {
                    dir.mkdirs();
                }

                OutputStream outStream;
                File file = new File(filePath, fileName);

                try {
                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    /*Intent intent = new TweetComposer.Builder(mContext)
                            .image(Uri.fromFile(file))
                            .createIntent();
                    startActivityForResult(intent, mTwitterRequestCode);*/
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
                    msg.setData(data);
                    msg.what = Constants.QUERY_SNS_SHARE;
                    mHandler.sendMessage(msg);
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getSnsShareInstagramThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory() + "/POST";
                String fileName = Constants.SERVICE_SHARE_FILE_NAME;

                File dir = new File(filePath);
                if(!dir.exists()) {
                    dir.mkdirs();
                }

                OutputStream outStream;
                File file = new File(filePath, fileName);

                Bundle data = new Bundle();
                Message msg = new Message();
                try {
                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    // Create the new Intent using the 'Send' action.
                    Intent share = new Intent(Intent.ACTION_SEND);

                    // Set the MIME type
                    share.setType("image/*");

                    // Create the URI from the media
                    File media = new File(filePath, fileName);
                    Uri uri = Uri.fromFile(media);

                    // Add the URI to the Intent.
                    share.putExtra(Intent.EXTRA_STREAM, uri);

                    share.setPackage("com.instagram.android");

                    PackageManager packManager = getPackageManager();
                    List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(share,  PackageManager.MATCH_DEFAULT_ONLY);

                    boolean resolved = false;
                    for (ResolveInfo resolveInfo: resolvedInfoList) {
                        if (resolveInfo.activityInfo.packageName.startsWith("com.instagram.android")) {
                            share.setClassName(
                                    resolveInfo.activityInfo.packageName,
                                    resolveInfo.activityInfo.name );
                            resolved = true;
                            break;
                        }
                    }
                    if (resolved) {
                        // Broadcast the Intent.
                        startActivity(share);
                        data.putString("message", "");
                        data.putString("type", Constants.REQUEST_ACTION_TYPE_INSTAGRAM);
                    } else {
                        data.putString("message", getString(R.string.msg_share_failed_not_install));
                    }
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
                }
                msg.setData(data);
                msg.what = Constants.QUERY_SNS_SHARE;
                mHandler.sendMessage(msg);
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getSnsShareLineThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory() + "/POST";
                String fileName = Constants.SERVICE_SHARE_FILE_NAME;

                OutputStream outStream;
                File file = new File(filePath, fileName);

                Bundle data = new Bundle();
                Message msg = new Message();
                try {
                    if (!file.exists()) file.createNewFile();

                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    // Create the new Intent using the 'Send' action.
                    Intent share = new Intent(Intent.ACTION_SEND);

                    // Set the MIME type
                    share.setType("image/*");

                    // Create the URI from the media
                    File media = new File(filePath, fileName);
                    Uri uri = Uri.fromFile(media);

                    // Add the URI to the Intent.
                    share.putExtra(Intent.EXTRA_STREAM, uri);

                    share.setPackage("jp.naver.line.android");

                    PackageManager packManager = getPackageManager();
                    List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(share,  PackageManager.MATCH_DEFAULT_ONLY);

                    boolean resolved = false;
                    for (ResolveInfo resolveInfo: resolvedInfoList) {
                        if(resolveInfo.activityInfo.packageName.startsWith("jp.naver.line.android")){
                            share.setClassName(
                                    resolveInfo.activityInfo.packageName,
                                    resolveInfo.activityInfo.name );
                            resolved = true;
                            break;
                        }
                    }
                    if (resolved) {
                        // Broadcast the Intent.
                        startActivity(share);
                        data.putString("message", "");
                        data.putString("type", Constants.REQUEST_ACTION_TYPE_LINE);
                    } else {
                        data.putString("message", getString(R.string.msg_share_failed_not_install));
                    }
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
                }
                msg.setData(data);
                msg.what = Constants.QUERY_SNS_SHARE;
                mHandler.sendMessage(msg);
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getSnsShareImageUploadThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                String filePath = mContext.getDir(Constants.SERVICE_SHARE_FILE_PATH, mContext.MODE_PRIVATE).getAbsolutePath();
                mContentBitmapUrl[0] = Constants.SERVICE_SHARE_FILE_NAME;

                OutputStream outStream;
                File file = new File(filePath, mContentBitmapUrl[0]);

                try {
                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    mContentBitmap[0] = BitmapFactory.decodeFile(filePath + "/" + mContentBitmapUrl[0]);

                    mGetShareImageUploadDataRes = request.uploadShareImage(mContentBitmapUrl, mContentKey, mContentBitmap);
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
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    Bundle data = new Bundle();
                    Message msg = new Message();
                    data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
                    msg.setData(data);
                    msg.what = Constants.QUERY_SNS_SHARE;
                    mHandler.sendMessage(msg);
                }
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getSnsShareKakaoLinkThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory() + "/POST";
                String fileName = Constants.SERVICE_SHARE_FILE_NAME;

                File dir = new File(filePath);
                if(!dir.exists()) {
                    dir.mkdirs();
                }

                OutputStream outStream;
                File file = new File(filePath, fileName);

                Bundle data = new Bundle();
                Message msg = new Message();
                try {
                    outStream = new FileOutputStream(file);
                    ImageUtil.getDrawingCache(contentLayout).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();

                    // 텍스트 글자수는 최대 1000자로 제한 됩니다.
                    //mKakaoTalkLinkMessageBuilder.addText("나만의 감성 앱 POST");

                    // 보여질 크기의 경우 최소 가로(width) 80px, 세로(height) 80px이므로 이보다 크게 설정하여야 합니다. 또한 이미지 용량은 500kb이하로 제한됩니다.
                    mKakaoTalkLinkMessageBuilder.addImage(shareImagePath, 150, 150);

                    mKakaoTalkLinkMessageBuilder.addAppButton("POST",
                            new AppActionBuilder()
                                    .addActionInfo(AppActionInfoBuilder
                                            .createAndroidActionInfoBuilder()
                                            .setExecuteParam("action=AF01&poi=" + mPOI)
                                            .setMarketParam("referrer=kakaotalklink")
                                            .build())
                                    .addActionInfo(AppActionInfoBuilder
                                            .createiOSActionInfoBuilder()
                                            .setExecuteParam("action=AF01&poi=" + mPOI)
                                            .build())
                                    .setUrl(shareLinkUrl) // PC 카카오톡 에서 사용하게 될 웹사이트 주소
                                    .build());
                    mKakaoLink.sendMessage(mKakaoTalkLinkMessageBuilder, mContext);
                    data.putString("message", "");
                    data.putString("type", Constants.REQUEST_ACTION_TYPE_KAKAOTALK);
                } catch (Exception e) {
                    LogUtil.e(e.getMessage());
                    data.putString("message", getString(R.string.msg_share_failed) + "\n" + e.getMessage());
                }
                msg.setData(data);
                msg.what = Constants.QUERY_SNS_SHARE;
                mHandler.sendMessage(msg);
            }
        });
        thread.start();
        return thread;
    }

    public RunnableThread getActionLogThread(final String type) {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetActionLogReq getActionLogReq = new GetActionLogReq();
                    getActionLogReq.setACT_DIVN_TYPE(Constants.REQUEST_ACTION_DIVN_TYPE_POST);
                    getActionLogReq.setACT_TYPE(type);
                    getActionLogReq.setACT_PLAC_TYPE(Constants.REQUEST_ACTION_PLAC_TYPE_POST);
                    getActionLogReq.setACT_PLAC_ID(mPOI);
                    request.getActionLog(getActionLogReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_ACTION_LOG);
                } catch (RequestException e) {
                } catch (POSTException e) {
                }
            }
        });
        thread.start();
        return thread;
    }

    @Override
    public void handleMessage(Message msg) {
        if(mProgressDialog != null) { mProgressDialog.dissDialog(); }

        Bundle data = msg.getData();

        switch(msg.what) {
            // POST 데이터 조회 성공 Handler
            case Constants.QUERY_POST_DATA:
                if (!"".equals(mPostDataItem.getBG_USER_PATH())) {
                    mGlideRequestManager
                            .load(mPostDataItem.getBG_USER_PATH())
                            .thumbnail(Constants.GLIDE_THUMBNAIL)
                            .override(defaultImageSize, defaultImageSize)
                            .into(ivRoot);
                } else {
                    mGlideRequestManager
                            .load(mPostDataItem.getBG_PIC_PATH())
                            .thumbnail(Constants.GLIDE_THUMBNAIL)
                            .override(defaultImageSize, defaultImageSize)
                            .into(ivRoot);
                }

                if (Constants.REQUEST_TYPE_TODAY.equals(mPOST_TYPE)) {
                    tvTodaySubject.setVisibility(View.VISIBLE);
                    tvPostSubject.setGravity(Gravity.CENTER);

                    tvPostSubject.setSpacing(0.0f);
                    tvPostSubject.setText(mPostDataItem.getKWD());
                    if (!"".equals(mPostDataItem.getCOLOR_HEX())) {
                        tvPostSubject.setTextColor(Color.parseColor("#" + mPostDataItem.getCOLOR_HEX()));
                    }

                    tvTodaySubject.setText(mPostDataItem.getPOST_SUBJ());
                    if (!"".equals(mPostDataItem.getCOLOR_HEX())) {
                        tvTodaySubject.setTextColor(Color.parseColor("#" + mPostDataItem.getCOLOR_HEX()));
                    }
                } else {
                    tvTodaySubject.setVisibility(View.GONE);
                    tvPostSubject.setGravity(Gravity.CENTER_VERTICAL);

                    if (CommonUtil.isNull(mPostDataItem.getPOST_SUBJ())) {
                        tvPostSubject.setTextColor(Color.parseColor("#FFFFFFFF"));
                        tvPostSubject.setAlpha(0.5f);
                        tvPostSubject.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                        tvPostSubject.setSpacing(Constants.TEXT_VIEW_SPACING);

                        if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
                            tvPostSubject.setText(mContext.getString(R.string.post_story));
                        } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
                            tvPostSubject.setText(mContext.getString(R.string.post_radio));
                        }
                    } else {
                        tvPostSubject.setSpacing(0.0f);
                        tvPostSubject.setText(mPostDataItem.getPOST_SUBJ());
                        if (!"".equals(mPostDataItem.getCOLOR_HEX())) {
                            tvPostSubject.setTextColor(Color.parseColor("#" + mPostDataItem.getCOLOR_HEX()));
                        }
                    }
                }

                if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_POST)) {
                    tvPostContent.setMaxLines(12);
                } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_TODAY)) {
                    tvPostContent.setMaxLines(12);
                } else if (mPOST_TYPE.equals(Constants.REQUEST_TYPE_RADIO)) {
                    tvPostContent.setMaxLines(12);
                }
                tvPostContent.setMoreLinkEnabled(false);
                tvPostContent.setText(mPostDataItem.getPOST_CONT());

                shareLinkUrl = "http://dev.melodigm.com/app0102v1.post";
                break;
            // 이미지 공유 Handler
            case Constants.QUERY_SNS_SHARE:
                if (mPostDialog != null && mPostDialog.isShowing()) mPostDialog.dismiss(); mPostDialog = null;

                String dialogMessage = data.getString("message", "");
                String type = data.getString("type", "");
                if (CommonUtil.isNotNull(type)) {
                    String trackerAction = "";
                    switch (type) {
                        case Constants.REQUEST_ACTION_TYPE_IMAGE:
                            trackerAction = Constants.GA_ACTION_SHARE_IMAGE;
                            break;
                        case Constants.REQUEST_ACTION_TYPE_FACEBOOK:
                            trackerAction = Constants.GA_ACTION_SHARE_FACEBOOK;
                            break;
                        case Constants.REQUEST_ACTION_TYPE_TWITTER:
                            trackerAction = Constants.GA_ACTION_SHARE_TWITTER;
                            break;
                        case Constants.REQUEST_ACTION_TYPE_INSTAGRAM:
                            trackerAction = Constants.GA_ACTION_SHARE_INSTAGRAM;
                            break;
                        case Constants.REQUEST_ACTION_TYPE_LINE:
                            trackerAction = Constants.GA_ACTION_SHARE_LINE;
                            break;
                        case Constants.REQUEST_ACTION_TYPE_KAKAOTALK:
                            trackerAction = Constants.GA_ACTION_SHARE_KAKAOTALK;
                            break;
                    }

                    if (CommonUtil.isNotNull(trackerAction)) {
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory(Constants.GA_CATEGORY_SHARE)
                                .setAction(trackerAction)
                                .setLabel(SPUtil.getSharedPreference(mContext, Constants.SP_USER_ID))
                                .build());
                    }

                    getData(Constants.QUERY_ACTION_LOG, type);
                }

                if (CommonUtil.isNull(dialogMessage)) {
                    finish();
                } else {
                    mPostDialog = new PostDialog(mContext, Constants.DIALOG_TYPE_INFO, onClickListener, data.getString("message"));
                    mPostDialog.show();
                }
                break;
            // 공유할 이미지 업로드 후 Handler
            case Constants.DIALOG_EXCEPTION_NON:
                shareImagePath = mGetShareImageUploadDataRes.getShareImageUploadDataItemList().get(0).getFULL_PATH();
                if (CommonUtil.isNotNull(shareImagePath)) {
                    getData(Constants.QUERY_SNS_SHARE, shareImagePath);
                }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        LogUtil.e("Request Code(" + requestCode + "), Result Code(" + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, intent);
        // 페이스북 공유 후 onActivityResult
        if (requestCode == mFacebookRequestCode) {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, intent);
        }
        // 트위터 인증 후 onActivityResult
        else if (requestCode == mTwitterRequestCode) {
            Bundle data = new Bundle();
            Message msg = new Message();

            if (resultCode == Activity.RESULT_OK) {
                // success
                data.putString("message", getString(R.string.msg_share_success));
                data.putString("type", Constants.REQUEST_ACTION_TYPE_TWITTER);
            } else {
                // failure
                data.putString("message", getString(R.string.msg_share_failed));
            }

            msg.setData(data);
            msg.what = Constants.QUERY_SNS_SHARE;
            mHandler.sendMessage(msg);
        }
    }
}
