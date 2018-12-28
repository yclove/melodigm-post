package com.melodigm.post.protocol.network;

import java.util.HashMap;

import android.graphics.Bitmap;

public class MultiPartBitmapRequest extends SimpleRequest {
	
	Bitmap[] imgParam = null;
	String[] key = null;
	HashMap<String, String> mParamMap = null;
	String[] imageName = null;
	
	public MultiPartBitmapRequest() {
		super();
		method = REQ_HTTP_METHOD_POST;
	}
	
	public void setImgParameter(String[] key, String[] name,Bitmap[] bm, HashMap<String, String> paramMap) {
		this.imgParam = bm;
		this.key = key; 
		imageName = name;
		mParamMap = paramMap;
	}
}
