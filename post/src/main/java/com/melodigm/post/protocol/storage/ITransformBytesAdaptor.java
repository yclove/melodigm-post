package com.melodigm.post.protocol.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ITransformBytesAdaptor {
	public void doCancel();
	public int transform(byte[] in, int inLen, OutputStream out)
			throws IOException;
	public int unTransform(InputStream in, OutputStream out)
			throws IOException;
}
