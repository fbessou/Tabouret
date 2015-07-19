/**
 * 
 */
package com.fbessou.sofa;


/**
 * @author Frank Bessou
 *
 */
public class Analog1DSensor extends Sensor {
	private float mValue = 0;
	/**
	 * 
	 */
	public Analog1DSensor() {
		super(SensorType.ANALOG_1D);
	}
	
	public void putValue(float val){
		mValue = Math.max(0.0f, Math.min(val, 1.0f));
		
		InputEvent evt = InputEvent.createMotion1DEvent(0/*TODO*/, mValue, mId);
		
		triggerEvent(evt);
	}
}
