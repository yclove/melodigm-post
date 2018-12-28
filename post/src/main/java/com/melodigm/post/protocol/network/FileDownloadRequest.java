package com.melodigm.post.protocol.network;

public class FileDownloadRequest extends Request {
	public static final int STORAGE_TYPE_CACHE				= 1;
	public static final int STORAGE_TYPE_EXTERNAL			= 2;
	
	boolean					useCallbackDownloadedSize;
	String					downloadedFileName;
	long					curDownloadSize;
	long					totalDownloadSize;
	
	public FileDownloadRequest() {
		super();
		storageType = STORAGE_TYPE_CACHE;
		useCallbackDownloadedSize = false;
	}

	public String getDownloadedFileName() {
		return downloadedFileName;
	}

    public void setDownloadedFileName(String downloadedFileName) {
        this.downloadedFileName = downloadedFileName;
    }

    public long getCurrentDownloadedSize() {
		return curDownloadSize;
	}
	
	public long getTotalDownloadedFileSize() {
		return totalDownloadSize;
	}
	
	public boolean useCallbackDownloadedSize() {
		return useCallbackDownloadedSize;
	}
	
	public boolean useCallbackDownloadedSize(boolean use) {
		return useCallbackDownloadedSize = use;
	}
}
