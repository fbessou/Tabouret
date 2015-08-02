/**
 * 
 */
package com.fbessou.sofa.sensor;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.fbessou.sofa.InputEvent;


/**
 * @author Frank Bessou
 *
 */
public class KeySensor extends Sensor {

	private boolean mOn;
	/**
	 * (with auto generated id)
	 * @param on initial state of the key (in case of checkbox for example)
	 */
	public KeySensor(boolean on) {
		super();
		mOn = on;
	}
	/**
	 * 
	 * @param on initial state of the key (in case of check box for example)
	 */
	public KeySensor(boolean on, int id) {
		super(id);
		mOn = on;
	}
	
	/** Attaches a check box view to this sensor. Each state change (on/off) will trigger an event **/
	public void attachTo(CheckBox checkboxView) {
		// Add listener
		checkboxView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setValue(isChecked);
			}
		});
		
		// Copy the initial state
		setValue(checkboxView.isChecked());
	}
	
	/** Attaches a check box view to this sensor. Each state change (down/up) will trigger an event. **/
	@SuppressLint("ClickableViewAccessibility")
	public void attachTo(View view) {
		// Add listener
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				setValue(v.isPressed());
				return false;
			}
		});
	}
	
	/** Updates the value of this sensor and triggers an event if the state has changed. **/
	protected void setValue(boolean on) {
		if(mOn != on){
			// Create and trigger a new event
			InputEvent evt = on ? InputEvent.createKeyDownEvent(0, mId) : InputEvent.createKeyUpEvent(0, mId);
			triggerEvent(evt);
			
			mOn = on;
		}
	}
}
