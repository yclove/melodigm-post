package com.melodigm.post.protocol.network;

import com.melodigm.post.common.Constants;
import com.melodigm.post.util.LogUtil;

public class RequestException extends Exception {
	private static final long serialVersionUID = -6893372536185374080L;
	
	private int code;
	
	public RequestException(String msg, int code) {
		super(msg);
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
	
	public static RequestException createException(String msg, int code) {
		RequestException exception;
		
		switch(code) {
		case Request.ERROR_NETWORK_ERROR :
			exception = new RequestException("네트워크 접속에 실패하였습니다.", code);
			break;
		case Request.ERROR_INVALID_METHOD:
			exception = new RequestException("'" + msg + "' 지원하지 않는 요청 방식입니다.", code);
			break;
		case Request.ERROR_INVALID_STORAGE :
			exception = new RequestException("'" + msg + "' 지원하지 않는 저장 방식입니다. ", code);
			break;
		case Request.ERROR_WRITE_FILE :
			exception = new RequestException("'" + msg + "' 파일 저장에 오류가 발생했습니다.", code);
			break;
		case Request.ERROR_NO_SPACE :
			exception = new RequestException("저장 공간이 부족합니다.", code);
			break;
		default:
			exception = new RequestException(msg, code);
			break;
		}
		
		return exception;
	}
}
