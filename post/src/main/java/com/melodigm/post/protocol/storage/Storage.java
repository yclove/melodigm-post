package com.melodigm.post.protocol.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.melodigm.post.protocol.network.URLRequestHandler;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;

public class Storage {
	private static final String TEMP_FILE_NAME = "data.tmp";
	
	private static int TEMP_FILE_INDEX;
	
	String					currentDirectory;
	String					tempFileName;
	boolean					cancel;
	ITransformBytesAdaptor  tranformAdaptor;

	public Storage() {
		cancel = false;
	}
	
	public void setCurrentDirectory(String currentDirectory) {
		this.currentDirectory = currentDirectory;
		//PJYTEST
		//this.currentDirectory = "/sdcard";
	}
	
	public String getCurrentDirectory() {
		return currentDirectory;
	}
	
	public void makeDirectory()
			throws StorageException {
		String[] tmpDirs = currentDirectory.split("/");
		String makedDir = "";
		for(int i = 0; i < tmpDirs.length; i++) {
			if(tmpDirs[i].length() > 0) {
				makedDir += File.separator + tmpDirs[i];
				File tmpFile = new File(makedDir);
				if(!tmpFile.exists()) {
					if(!tmpFile.mkdir()) {
						throw StorageException.createException(makedDir, StorageException.ERROR_NOT_DIRECTORY);
					}
				} else if(!tmpFile.isDirectory()) {
					throw StorageException.createException(currentDirectory, StorageException.ERROR_NOT_DIRECTORY);
				}
			}
		}
		if(makedDir.length() == 0) {
			throw StorageException.createException(currentDirectory, StorageException.ERROR_INVALID_DIRECTORY);
		}
	}

	public void createTempFile(String path) {
		this.tempFileName = path + File.separator + (TEMP_FILE_INDEX++) + TEMP_FILE_NAME;
	}
	
	public String getTempFileName() {
		return tempFileName;
	}
	
	public void clearTempFile() {
		File file = new File(tempFileName);
    	if(file.exists()) {
    		file.delete();
    	}
	}
	
	public void setTranformBytesAdaptor(ITransformBytesAdaptor tranformAdaptor) {
		this.tranformAdaptor = tranformAdaptor;
	}
	
	public String getFullPath(String filename) {
		return currentDirectory + File.separator + filename;
	}
	
	public String existFile(String filename) {
		String pullPath = null;
		pullPath = currentDirectory + File.separator + filename;
    	File newFile = new File(pullPath);
    	if(newFile.exists()) {
    		if(newFile.length() > 0) {
        		return pullPath;
    		}
    	}
    	return null;
	}
	
	public InputStream getInputStream(String fullFileName) 
			throws IOException {
		File file = new File(fullFileName);
		FileInputStream fins = null;
		
		if(file.exists()) {
			fins = new FileInputStream(file);
			
			if(tranformAdaptor != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(fins.available());
				tranformAdaptor.unTransform(fins, baos);
				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				fins.close();
				baos.close();
				return bais;
			}
		}
		
		return fins;
	}
	
	public int write(InputStream in, OutputStream out) 
			throws IOException {
		byte[] buffer = new byte[10240];
		int readLen = 0;
		if((readLen = in.read(buffer)) > 0 ) {
			if(tranformAdaptor == null) {
		    	out.write(buffer, 0, readLen);
		    } else {
		    	tranformAdaptor.transform(buffer, readLen, out);
		    }
		}
		return readLen;
	}
	
	int writeFileByTransForm(InputStream in, OutputStream out)
		throws IOException {
		
		byte[] buffer = new byte[4096];
		int readLen = 0;
		int totalLen = 0;
	    
		while ( (readLen = in.read(buffer)) > 0 ) {
			if(cancel) break;
				out.write(buffer, 0, readLen);
		    tranformAdaptor.transform(buffer, readLen, out);
		    
			totalLen += readLen;
		}
		cancel = false;
		return totalLen;
	}
	
	int writeFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[4096];
		int readLen = 0;
		int totalLen = 0;
		
		while ( (readLen = in.read(buffer)) > 0 ) {
			if(cancel) break;
			out.write(buffer, 0, readLen);
			totalLen += readLen;
		}
	
		return totalLen;
	}
	
	public File writeFile(InputStream in, String filename) 
			throws IOException {
		String pullPath = currentDirectory + "/" + filename;
		File tempFile = new File(pullPath);
		if(tempFile.exists()) {
	    	tempFile.delete();
	    }
		
		FileOutputStream out = new FileOutputStream(tempFile);
		
		if(tranformAdaptor == null) {
			writeFile(in, out);
		} else {
			writeFileByTransForm(in, out);
		}

		out.flush();
		out.close();
		return tempFile;
	}
	
	public File writeFile(InputStream in, String filename, String charcaterSet) 
		throws IOException {
		String pullPath = currentDirectory + "/" + filename;
		File tempFile = new File(pullPath);
		if(tempFile.exists()) {
			tempFile.delete();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in, charcaterSet));
		FileWriter fw = new FileWriter(tempFile);
		
		char[] buffer = new char[4096];
		int readLen = 0;
		int totalLen = 0;
		
		while ( (readLen = br.read(buffer)) > 0 ) {
			if(cancel) break;
			fw.write(buffer, 0, readLen);
			totalLen += readLen;
			if(totalLen == 0) { // remove warnig --cerick--
				
			}
		}
		
		fw.flush();
		fw.close();
		return tempFile;
	}
	
	public String moveFile(File src, File desc, boolean isDelete) 
		throws IOException {
	    if(isDelete && desc.exists()) {
	    	desc.delete();
	    }

	    src.renameTo(desc);
	    return desc.getPath();
	}
	
	public String moveFile(File src, String filename, boolean isDelete) 
		throws IOException {
		return moveFile(src, new File(currentDirectory + File.separator+ filename), isDelete);
	}
	
	public String moveFile(String src, String desc, boolean isDelete) 
		throws IOException {
		return moveFile(new File(currentDirectory + File.separator+ src),
				new File(currentDirectory + File.separator+ desc), isDelete);
	}
	
	public static boolean hasExternalStorage() {
		if(Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState()))
			return true;
		return false;
	}
	
	public static long getAvailableInternalStorageSize() {   
        File path = Environment.getDataDirectory();   
        StatFs stat = new StatFs(path.getPath());   
        long blockSize = stat.getBlockSize();   
        long availableBlocks = stat.getAvailableBlocks();   
        return availableBlocks * blockSize;   
    }   
       
    public static long getTotalInternalStorageSize() {   
        File path = Environment.getDataDirectory();   
        StatFs stat = new StatFs(path.getPath());   
        long blockSize = stat.getBlockSize();   
        long totalBlocks = stat.getBlockCount();   
        return totalBlocks * blockSize;   
    }   
       
    public static long getAvailableExternalStorageSize() {  
        File path = Environment.getExternalStorageDirectory();   
        StatFs stat = new StatFs(path.getPath());   
        long blockSize = stat.getBlockSize();   
        long availableBlocks = stat.getAvailableBlocks();   
        return availableBlocks * blockSize;
    }   
       
    public static long getTotalExternalStorageSize() {
        File path = Environment.getExternalStorageDirectory();   
        StatFs stat = new StatFs(path.getPath());   
        long blockSize = stat.getBlockSize();   
        long totalBlocks = stat.getBlockCount();   
        return totalBlocks * blockSize;
    }
    
    public static String getExternalDirectory() {
    	return Environment.getExternalStorageDirectory().getPath();
    }
    
    public static double[] getExternalStorageSize() {		// MB byte
        double[] exSize = {0, 0};
    	if(hasExternalStorage()) {
	    	File file = Environment.getExternalStorageDirectory();
	        StatFs statFs = new StatFs(file.getAbsolutePath());
	        int blockCount = statFs.getBlockCount();
	        int blockSize = statFs.getBlockSize();
	        int freeBlockCount = statFs.getFreeBlocks();
	        
	        exSize[0] = ((double)blockCount * (double)blockSize)/1024/1024;
	        exSize[1] = ((double)freeBlockCount * (double)blockSize)/1024/1024;
    	}

        return exSize;
    }
    
    public static void deleteExtanalStorageFile(String path, String url) {
    	String fileName = URLRequestHandler.getImageFileNameFromURL(url);
    	fileName = path + fileName;
    	File file = new File(fileName);
    	if(file.exists()) {
    		try {
    			file.delete();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void deleteStorageFile(String path) {
    	File file = new File(path);
    	if(file.exists()) {
    		try {
    			file.delete();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void copyCacheToExternal(Storage storage, String src, String dest){
		try {
			File f1 = new File(src);
			File f2 = new File(dest);
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);

			storage.writeFile(in, out);
			out.flush();
			out.close();
		    in.close();
		}
		catch(FileNotFoundException ex){
		}
		catch(IOException e){    
		}
	}
    
	public static String getRealPathFromURI(Activity activity, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		
		/*
		 * YCLOVE : deprecation is deprecation
		 * Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
		 */
		Cursor cursor = activity.getContentResolver().query(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String filepath = cursor.getString(column_index);
		cursor.close();
		return filepath;
	}
}
