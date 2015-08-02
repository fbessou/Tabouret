/**
 * 
 */
package com.fbessou.sofa.sensor;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.fbessou.sofa.InputEvent;


/**
 * @author Frank Bessou
 * Clamp value in range 0..1
 */
public class Analog1DSensor extends Sensor {
	private float mValue = 0;
	

	public Analog1DSensor() {
		super();
	}
	public Analog1DSensor(int id) {
		super(id);
	}
	
	/** Attaches a seek bar to this sensor. Each action will trigger an event. **/
	public void attachTo(SeekBar seekBarView) {
		seekBarView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				InputEvent evt = InputEvent.createUp1DEvent(0, mValue, mId);
				triggerEvent(evt);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				InputEvent evt = InputEvent.createDown1DEvent(0, mValue, mId);
				triggerEvent(evt);
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				putValue((float)progress / seekBar.getMax());
			}
		});
	}
	
	protected void putValue(float val){
		mValue = Math.max(0.0f, Math.min(val, 1.0f));
		
		InputEvent evt = InputEvent.createMotion1DEvent(0/*TODO*/, mValue, mId);
		
		triggerEvent(evt);
	}
}
