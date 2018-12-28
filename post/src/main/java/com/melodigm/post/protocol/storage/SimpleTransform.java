package com.melodigm.post.protocol.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleTransform implements ITransformBytesAdaptor {

	public static final int		BLOCK_SIZE		= 512; // bytes
	boolean cancel = false;
	
	public void doCancel() {
		this.cancel = true;
	}
	
	public int transform(byte[] in, int inLen, OutputStream out) 
			throws IOException {
    	for(int i = 0; i < inLen; i++) {
    		in[i] = (byte)(0xAFBFCFDF^in[i]);
		}
    	out.write(in, 0, inLen);
		
		return inLen;
	}

	public int unTransform(InputStream in, OutputStream out) 
			throws IOException {
		byte[] buffer = new byte[1024];
	    int readLen = 0;
	    int totalLen = 0;
	    while ( (readLen = in.read(buffer)) > 0 ) {
	    	if(cancel) break;
	    	for(int i = 0; i < readLen; i++) {
				buffer[i] = (byte)(0xAFBFCFDF^buffer[i]);
			}
	    	out.write(buffer, 0, readLen);
	    	totalLen += readLen;
	    }
		
		return totalLen;
	}
}
