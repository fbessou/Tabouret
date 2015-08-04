package com.fbessou.sofa.indicator;

import android.widget.CompoundButton;

import com.fbessou.sofa.OutputEvent;

public class BooleanIndicator extends Indicator {
	
	/** Last string received **/
	private boolean mIsOn;
	private CompoundButton mAttachedView;
	
	public BooleanIndicator() {
		super();
	}
	public BooleanIndicator(int id) {
		super(id);
	}
	
	/** Attaches this indicator to a compound button (ie: CheckBox, Switch,
	 * ToggleButton, and others two state view). Each output event received
	 * will be display in this view. **/
	public void attachTo(CompoundButton view) {
		mAttachedView = view;
	}
	
	@Override
	public void onOutputEventReceived(OutputEvent event) {
		mIsOn = (event.state != 0);
		
		/** Update the view on the UI thread **/
		mUIHandler.post(new Runnable() {
			@Override
			public void run() {
				mAttachedView.setChecked(mIsOn);	
			}
		});
	}
}
