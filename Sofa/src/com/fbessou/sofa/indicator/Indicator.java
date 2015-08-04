package com.fbessou.sofa.indicator;

import android.os.Handler;

import com.fbessou.sofa.OutputEvent;

abstract public class Indicator {
	protected Handler mUIHandler;
	/** unique identifier **/
	protected int mId;
	
	/** Static id counter used to auto generate an id for each new indicator **/
	private static int sourceId = 0;
	/** Reset the source id for auto generated id**/
	public static void init() {
		sourceId = 0;
	}

	/** Make a new indicator with auto generated id. This constructor must be call in the UI Thread. **/
	public Indicator() {
		mId = sourceId++;
		mUIHandler = new Handler();
	}
	/** Make a new indicator with auto generated id. This constructor must be call in the UI Thread. **/
	public Indicator(int id) {
		mId = id;
		mUIHandler = new Handler();
	}
	
	/** Returns the pad's id**/
	public int getPadId() {
		return mId;
	}
	/** Sets the pad's id **/
	public void setPadId(int id) {
		mId = id;
	}
	
	/** Called when a new ouput event is received **/
	public abstract void onOututEventReceived(OutputEvent event);
}
