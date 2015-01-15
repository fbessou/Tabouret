package com.fbessou.tabouret.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class ButtonView extends View {
	
	
	ButtonView(Context context) {
		super(context);
	}
	
	private boolean mWasPressed = false;
	@SuppressLint("ClickableViewAccessibility") // Doesn't matter! :P
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = super.onTouchEvent(event);
		
		boolean pressed = isPressed();
		if(mWasPressed != pressed) {
			mStateChangedListener.onStateChanged(pressed);
			mWasPressed = pressed;
		}
		
		return handled;
	}
	
	private OnStateChangeListener mStateChangedListener;
	
	public void setOnStateChangedListener(OnStateChangeListener listener) {
		mStateChangedListener = listener;
	}
	
	public interface OnStateChangeListener {
		public void onStateChanged(boolean isPressed);
	}
}
