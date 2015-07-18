package com.fbessou.tabouret.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RelativeLayout;

public class SliderView extends View {
	private Bitmap mIndicatorBmp;
	private int mLength, mWidth;
	private Orientation mOrientation;
	
	public SliderView(Context context, Orientation orient, int length, Bitmap indicator) {
		super(context);
		mIndicatorBmp = indicator;
		mWidth = (indicator == null) ? 60 : indicator.getWidth();
			
		mLength = length;
		mOrientation = orient;
		
		//RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w + mStickRadius*2, h + mStickRadius*2);
	}
	
	
	public interface OnPositionChangedListener {
		/** Gets the value in range [0; 1]**/
		public void onPositionChanged(float value);
	}
	
	public enum Orientation {VERTICAL, HORIZONTAL };
}
