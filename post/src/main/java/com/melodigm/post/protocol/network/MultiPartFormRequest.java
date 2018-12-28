package com.melodigm.post.protocol.network;

import java.util.Map;

public class MultiPartFormRequest extends FileDownloadRequest {
	
	Map<String, String> params;
	Map<String, String> fileParams;
	
	public MultiPartFormRequest() {
		super();
		method = REQ_HTTP_METHOD_POST;
	}
	
	public void setParameters(Map<String, String> params) {
		this.params = params;
	}
	
	public void setFileParameters(Map<String, String> params) {
		this.fileParams = params;
	}
}
