package com.melodigm.post.protocol.network;

import java.io.InputStream;
import java.util.Map;

public class SimpleRequest extends Request {
	InputStream in;
	public Map<String, String> addHeader;
	
	public SimpleRequest() {
		super();
	}
	
	public InputStream getInputStream() {
		return in;
	}
}
