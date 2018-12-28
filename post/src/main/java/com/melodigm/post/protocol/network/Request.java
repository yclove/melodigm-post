package com.melodigm.post.protocol.network;

import org.apache.http.HttpEntity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;


public abstract class Request {
	public static final int ERROR_NONE				= 0;
	public static final int ERROR_NETWORK_ERROR		= 1;
	public static final int ERROR_INVALID_METHOD	= 2;
	public static final int ERROR_INVALID_STORAGE	= 3;
	public static final int ERROR_WRITE_FILE		= 4;
	public static final int ERROR_NO_SPACE			= 5;
	
	public static final int STATE_READY						= 0;
	public static final int STATE_BEFORE_DOWNLOAD			= 1;
	public static final int STATE_CONINUTE_DOWNLOAD			= 2;
	public static final int STATE_COMPLETE_DOWNLOAD			= 3;
	public static final int STATE_REMOVE_ALL				= 4;
	
	public static final int REQ_PROTOCOL_HTTP				= 0; // default;
	public static final int REQ_PROTOCOL_HTTPS				= 1;
	
	public static final String REQ_HTTP_METHOD_GET			= "GET"; // default;
	public static final String REQ_HTTP_METHOD_POST			= "POST";
	public static final String REQ_HTTP_METHOD_PUT			= "PUT";
	
	public static final int STORAGE_TYPE_MEM				= 0; // default;
	
	public static final int REQ_CONTENTS_IMAGE				= 0; // default;
	public static final int REQ_CONTENTS_XML				= 1;
	public static final int REQ_CONTENTS_FILE				= 2; 
	
	public static final int REQ_TYPE_SYNC					= 0;
	public static final int REQ_TYPE_ASYNC					= 1;
	public static final int REQ_TYPE_INDEPENDENT_ASYNC		= 2;
	
	int						requestType;
	boolean					showProgressDialog;
	boolean					useAleadyExist;

	int						index;
	int						subIndex;
	
	int						protocol;
	
	String					method;
	String					params;
	HttpEntity				paramEntity;
	String					requestURL;
	int						requestContentsType;
	int						state;
	int						storageType;
	int						errorCode;
	String					errorMsg;
	Object					userData;
	Activity				activity;
	Dialog					progressDialog;
	String					progressTitle = "Loading...";
	OnDownloadStateListener	downloadStateListener;
	
	URLRequestHandler handler;
	
	Request() {
		requestType = REQ_TYPE_SYNC;
		showProgressDialog = true;
		useAleadyExist = true;
		method = REQ_HTTP_METHOD_GET;
		protocol = REQ_PROTOCOL_HTTP;
		requestContentsType = REQ_CONTENTS_IMAGE;
		state = STATE_READY;
		storageType = STORAGE_TYPE_MEM;
		errorCode = ERROR_NONE;
	}
	
	public int getRequestType() {
		return requestType;
	}
	
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
	
	public boolean useAleadyExist() {
		return useAleadyExist;
	}
	
	public boolean useAleadyExist(boolean use) {
		this.useAleadyExist = use;
		return useAleadyExist;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getSubIndex() {
		return subIndex;
	}
	public void setSubIndex(int subIndex) {
		this.subIndex = subIndex;
	}
	public int getProtocol() {
		return protocol;
	}
	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	
	public int getStorageType() {
		return storageType;
	}
	
	public void setStorageType(int storageType) {
		this.storageType = storageType;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getParams() {
		return params;
	}
	
	public void setParams(String params) {
		this.params = params;
	}
	
	public String getURL() {
		return requestURL;
	}
	
	public void setURL(String requestURL) {
		this.requestURL = requestURL;
	}
	
	public int getRequestContentsType() {
		return requestContentsType;
	}
	
	public void setRequestContentsType(int contentsType) {
		this.requestContentsType = contentsType;
	}
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorMessage() {
		return errorMsg;
	}
	public void getErrorMessage(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public Object getUserData() {
		return userData;
	}
	public void setUserData(Object userData) {
		this.userData = userData;
	}
	public Activity getActivity() {
		return activity;
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	public Dialog getDialog() {
		return progressDialog;
	}
	
	public void showDialog() {
		if(showProgressDialog && activity != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					if(progressDialog == null) 
						progressDialog = ProgressDialog.show(activity, "",progressTitle, true, true);
					else
						progressDialog.show();					
				}
			});
		}
	}
	
	public void dismissDialog() {
		if(activity != null && showProgressDialog && progressDialog != null) {
			activity.runOnUiThread(new Runnable() {
				public void run() {
					if(!activity.isFinishing() && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}	
				}
			});
		}
	}
	
	public void setDialog(Dialog dialog) {
		this.progressDialog = dialog;
	}
	
	public OnDownloadStateListener getOnDownloadStateListener() {
		return downloadStateListener;
	}
	
	public void setListener(OnDownloadStateListener listener) {
		this.downloadStateListener = listener;
	}
	
	public boolean showProgressDialog() {
		return this.showProgressDialog;
	}
	
	public boolean showProgressDialog(boolean isShow) {
		this.showProgressDialog = isShow;
		return this.showProgressDialog;
	}
	
	public HttpEntity getParamList() {
		return paramEntity;
	}
	
	public void setParamList(HttpEntity paramEntity) {
		this.paramEntity = paramEntity;
	}
	
	public void setProgressTitle(String title) {
		this.progressTitle = title;
	}
	
	void onComplete() {
		dismissDialog();
	}
}
