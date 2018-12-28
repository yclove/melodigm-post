package com.melodigm.post.protocol.storage;

public class StorageException extends Exception {
	private static final long serialVersionUID = -2756779495114254797L;
	
	public static final int ERROR_INVALID_DIRECTORY		= 1;
	public static final int ERROR_NOT_DIRECTORY		= 2;
	public static final int ERROR_CAN_NOT_CREATE		= 3;
	
	private int code;
	
	public StorageException(String msg, int code) {
		super(msg);
		this.code = code;
	}

	public static StorageException createException(String msg, int code) {
		StorageException exception;
		
		switch(code) {
		case ERROR_CAN_NOT_CREATE :
			exception = new StorageException("'" + msg + "' 폴더를 생성할 수 없습니다.", code);
			break;
		case ERROR_INVALID_DIRECTORY:
			exception = new StorageException("'" + msg + "' 폴더가 아닙니다.", code);
			break;
		case ERROR_NOT_DIRECTORY :
			exception = new StorageException("'" + msg + "' 폴더가 아닙니다.", code);
			break;
		default:
			exception = new StorageException(msg, code);
			break;
		}
		
		return exception;
	}
	

	public int getCode() {
		return this.code;
	}
}
