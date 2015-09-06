package com.fbessou.sofa;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionKeeper {
	private long mMaxMuteDuration;
	private ScheduledThreadPoolExecutor mTimer;
	private Runnable mRunnable = null;
	private boolean mIsEnabled = false;
	
	private ScheduledFuture<?> mScheduledPost;
	private OnMaxMuteDurationReachedListener mListener;
	
	public ConnectionKeeper(long maxMuteDuration, OnMaxMuteDurationReachedListener listener) {
		mMaxMuteDuration = maxMuteDuration;
		mListener = listener;
		mTimer = new ScheduledThreadPoolExecutor(1);
		
		mRunnable = new Runnable() {
			@Override
			public void run() {
				if(mListener != null)
					mListener.onMaxMuteDurationReached();
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
			
			mScheduledPost = mTimer.schedule(mRunnable, mMaxMuteDuration, TimeUnit.MILLISECONDS);
		}
	}
	
	public interface OnMaxMuteDurationReachedListener {
		public void onMaxMuteDurationReached();
	}
}
