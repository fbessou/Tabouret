/**
 * 
 */
package com.fbessou.sofa;

import com.fbessou.sofa.InputEvent.EventType;

import android.text.InputType;

/**
 * @author Frank Bessou
 *
 */
public class KeySensor extends Sensor {

	private boolean mOn;
	/**
	 * @param type
	 */
	public KeySensor(boolean on) {
		super(Sensor.SensorType.KEY);
		mOn = on;
	}
	
	public void setValue(boolean on){
		boolean changed = (mOn != on);
		if(changed){
			InputEvent evt = new InputEvent(on?EventType.KEY_DOWN:EventType.KEY_UP);
			evt.inputId = getId();
			triggerEvent(evt);
		}
		mOn=on;
	}
}
