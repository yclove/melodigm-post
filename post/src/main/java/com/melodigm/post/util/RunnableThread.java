package com.melodigm.post.util;

public class RunnableThread extends Thread {

	private StopRunnable mRun = null;

	public RunnableThread(StopRunnable runnable, String threadName) {
		super(runnable, threadName);
		setRunnable(runnable);
	}

	public RunnableThread(StopRunnable runnable) {
		super(runnable);
		setRunnable(runnable);
	}

	public RunnableThread(ThreadGroup group, StopRunnable runnable, String threadName, long stackSize) {
		super(group, runnable, threadName, stackSize);
		setRunnable(runnable);
	}

	public RunnableThread(ThreadGroup group, StopRunnable runnable, String threadName) {
		super(group, runnable, threadName);
		setRunnable(runnable);
	}

	public RunnableThread(ThreadGroup group, StopRunnable runnable) {
		super(group, runnable);
		setRunnable(runnable);
	}
	
	private void setRunnable(StopRunnable runnable){
		mRun = runnable;
		mRun.mStop = false;
	}
	
	public StopRunnable getRunnable(){
		return mRun;
	}
}
