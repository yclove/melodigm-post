package com.melodigm.post.util;

public class StopRunnable implements Runnable {
	public boolean mStop = false;
	
	public void stopRun(){
		mStop = true;
	}

	@Override
	public void run() {}
}
