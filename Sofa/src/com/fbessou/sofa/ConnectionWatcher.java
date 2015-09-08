package com.fbessou.sofa;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Call the methods from the given listener when the delay are passed after the
 * last call of notifyTimer(). 
 */
public class ConnectionWatcher {
	private long mMaxDuration, mAlertDuration;
	private ScheduledThreadPoolExecutor mTimer;
	private Runnable mRunnableAlert, mRunnableMax;
	private boolean mIsEnabled = false;
	
	private ScheduledFuture<?> mScheduledPost;
	private OnDelayPassedListener mListener;
	
	/**
	 * Call the methods from the given listener when the delay are passed after the
	 * last call of notifyTimer().
	 * 
	 * @param alertDelay Delay before call the first method: onAlertDelayPassed()
	 * @param maxDelay Delay before call the last method: onMaxDelayPassed()
	 * @param listener Interface of methods to call when delay are passed
	 */
	public ConnectionWatcher(long alertDelay, long maxDelay, OnDelayPassedListener listener) {
		mMaxDuration = maxDelay;
		mAlertDuration = alertDelay;
		mListener = listener;
		mTimer = new ScheduledThreadPoolExecutor(1);
		
		mRunnableAlert = new Runnable() {
			@Override
			public void run() {
				// Post the next runnable
				mScheduledPost = mTimer.schedule(mRunnableMax, mMaxDuration - mAlertDuration, TimeUnit.MILLISECONDS);
				
				if(mListener != null)
					mListener.onAlertDelayPassed();
			}
		};
		
		mRunnableMax = new Runnable() {
			@Override
			public void run() {
				if(mListener != null)
					mListener.onMaxDelayPassed();
			}
		};
	}
	
	/** Turn this timer on. Will be able to notify when the timer reaches the maximum duration. **/
	public void enable() {
		mIsEnabled = true;
		notifyTimer();
	}
	
	/** Turn this timer off. Will not notify anything whatever happens **/
	public void disable() {
		mIsEnabled = false;
		if(mScheduledPost != null && !mScheduledPost.isDone())
			mScheduledPost.cancel(true);
	}
	
	/** Reset the timer. Should be called each time a message has been successfully sent or received **/
	public void notifyTimer() {
		if(mIsEnabled) {
			if(mScheduledPost != null && !mScheduledPost.isDone())
				mScheduledPost.cancel(true);
			
			mScheduledPost = mTimer.schedule(mRunnableAlert, mAlertDuration, TimeUnit.MILLISECONDS);
		}
	}
	
	public interface OnDelayPassedListener {
		public void onAlertDelayPassed();
		public void onMaxDelayPassed();
	}
}
