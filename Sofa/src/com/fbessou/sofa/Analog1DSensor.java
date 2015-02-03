/**
 * 
 */
package com.fbessou.sofa;

import com.fbessou.sofa.InputEvent.InputEventType;

/**
 * @author Frank Bessou
 *
 */
public class Analog1DSensor extends Sensor {
	private float mValue = 0;
	/**
	 * @param type
	 */
	public Analog1DSensor() {
		super(SensorType.ANALOG_1D);
	}
	
	public void setValue(float val){
		mValue = Math.max(0, Math.min(1,val));
		InputEvent evt = new InputEvent(InputEventType.MOTION_1D);
		evt.inputId=getId();
		evt.x=mValue;
		triggerEvent(evt);
	}
}
