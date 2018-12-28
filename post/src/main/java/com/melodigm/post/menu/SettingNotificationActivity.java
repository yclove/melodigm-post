package com.melodigm.post.menu;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ToggleButton;

import com.melodigm.post.BaseActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;
import com.melodigm.post.protocol.HPRequest;
import com.melodigm.post.protocol.POSTException;
import com.melodigm.post.protocol.data.GetUpdateNotificationReq;
import com.melodigm.post.protocol.data.NotiSettingItem;
import com.melodigm.post.protocol.network.RequestException;
import com.melodigm.post.util.RunnableThread;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.StopRunnable;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.util.HashMap;
import java.util.Map;

public class SettingNotificationActivity extends BaseActivity implements IOnHandlerMessage, View.OnClickListener {
    private NotiSettingItem mNotiSettingItem;
    private ToggleButton tbSettingNotificationPostBtn, tbSettingNotificationOstBtn, tbSettingNotificationServiceBtn, tbSettingNotificationTodayBtn;
    private boolean isNotificationPost, isNotificationOst, isNotificationService, isNotificationToday;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_setting_notification);

        mContext = this;
        mHandler = new WeakRefHandler(this);
        mThreads = new HashMap<>();

        setDisplay();
	}
	
	private void setDisplay() {
        setPostHeader(Constants.HEADER_TYPE_BACK_HOME, this);
        setPostHeaderTitle(getString(R.string.notification), false);

        tbSettingNotificationPostBtn = (ToggleButton)findViewById(R.id.tbSettingNotificationPostBtn);
        tbSettingNotificationPostBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getData(Constants.QUERY_UPDATE_NOTIFICATION);
            }
        });

        tbSettingNotificationOstBtn = (ToggleButton)findViewById(R.id.tbSettingNotificationOstBtn);
        tbSettingNotificationOstBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getData(Constants.QUERY_UPDATE_NOTIFICATION);
            }
        });

        tbSettingNotificationServiceBtn = (ToggleButton)findViewById(R.id.tbSettingNotificationServiceBtn);
        tbSettingNotificationServiceBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getData(Constants.QUERY_UPDATE_NOTIFICATION);
            }
        });

        tbSettingNotificationTodayBtn = (ToggleButton)findViewById(R.id.tbSettingNotificationTodayBtn);
        tbSettingNotificationTodayBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getData(Constants.QUERY_UPDATE_NOTIFICATION);
            }
        });

        getData(Constants.QUERY_SELECT_NOTIFICATION);
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Header 뒤로가기 onClick
            case R.id.btnHeaderBack:
                finish();
                break;
        }
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
        if (queryType == Constants.QUERY_SELECT_NOTIFICATION) {
            thread = getSelectNotificationThread();
        } else if (queryType == Constants.QUERY_UPDATE_NOTIFICATION) {
            thread = getUpdateNotificationThread();
        }

        if(thread != null){
            mThreads.put(queryType, thread);
        }
    }

    public RunnableThread getSelectNotificationThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    mNotiSettingItem = request.getSelectNotification();
                    mHandler.sendEmptyMessage(Constants.QUERY_SELECT_NOTIFICATION);
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

    public RunnableThread getUpdateNotificationThread() {
        RunnableThread thread = new RunnableThread(new StopRunnable() {
            @Override
            public void run() {
                HPRequest request = new HPRequest(mContext);
                try {
                    GetUpdateNotificationReq getUpdateNotificationReq = new GetUpdateNotificationReq();
                    getUpdateNotificationReq.setPOST((tbSettingNotificationPostBtn.isChecked()) ? "Y" : "N");
                    getUpdateNotificationReq.setOST((tbSettingNotificationOstBtn.isChecked()) ? "Y" : "N");
                    getUpdateNotificationReq.setMANNER((tbSettingNotificationServiceBtn.isChecked()) ? "Y" : "N");
                    getUpdateNotificationReq.setTODAY((tbSettingNotificationTodayBtn.isChecked()) ? "Y" : "N");
                    request.getUpdateNotification(getUpdateNotificationReq);
                    mHandler.sendEmptyMessage(Constants.QUERY_UPDATE_NOTIFICATION);
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

        Bundle data = msg.getData();

        switch(msg.what) {
            // 사용자 알림 설정 정보 조회 성공 후 Handler
            case Constants.QUERY_SELECT_NOTIFICATION:
                if (mNotiSettingItem != null) {
                    isNotificationPost = "Y".equalsIgnoreCase(mNotiSettingItem.getPOST());
                    isNotificationOst = "Y".equalsIgnoreCase(mNotiSettingItem.getOST());
                    isNotificationService = "Y".equalsIgnoreCase(mNotiSettingItem.getMANNER());
                    isNotificationToday = "Y".equalsIgnoreCase(mNotiSettingItem.getTODAY());

                    if (isNotificationPost != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_POST))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_POST, isNotificationPost);
                    if (isNotificationOst != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_OST))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_OST, isNotificationOst);
                    if (isNotificationService != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE, isNotificationService);
                    if (isNotificationToday != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_TODAY))
                        SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_TODAY, isNotificationToday);
                }

                resetNotification();
                break;
            // 사용자 알림 설정 정보 등록 성공 후 Handler
           case Constants.QUERY_UPDATE_NOTIFICATION:
               isNotificationPost = tbSettingNotificationPostBtn.isChecked();
               isNotificationOst = tbSettingNotificationOstBtn.isChecked();
               isNotificationService = tbSettingNotificationServiceBtn.isChecked();
               isNotificationToday = tbSettingNotificationTodayBtn.isChecked();

               if (isNotificationPost != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_POST))
                   SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_POST, isNotificationPost);
               if (isNotificationOst != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_OST))
                   SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_OST, isNotificationOst);
               if (isNotificationService != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE))
                   SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_SERVICE, isNotificationService);
               if (isNotificationToday != SPUtil.getBooleanSharedPreference(mContext, Constants.SP_NOTIFICATION_TODAY))
                   SPUtil.setSharedPreference(mContext, Constants.SP_NOTIFICATION_TODAY, isNotificationToday);
               break;
           case Constants.DIALOG_EXCEPTION_REQUEST:
               resetNotification();
               if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_REQUEST, false).show();}
               setResult(Constants.RESULT_FAIL);
               break;
           case Constants.DIALOG_EXCEPTION_POST:
                resetNotification();
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_POST, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
           case Constants.DIALOG_EXCEPTION_UPDATE_NEED:
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_NEED, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
           case Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT:
                if(!isFinishing()) {createGlobalDialog(Constants.DIALOG_EXCEPTION_UPDATE_SUPPORT, false).show();}
                setResult(Constants.RESULT_FAIL);
                break;
        }
    }

    private void resetNotification() {
        tbSettingNotificationPostBtn.setChecked(isNotificationPost);
        tbSettingNotificationOstBtn.setChecked(isNotificationOst);
        tbSettingNotificationServiceBtn.setChecked(isNotificationService);
        tbSettingNotificationTodayBtn.setChecked(isNotificationToday);
    }
}
