/**
 * 
 */
package com.fbessou.sofa;


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
			InputEvent evt = on ? InputEvent.createKeyDownEvent(0, mId) : InputEvent.createKeyUpEvent(0, mId);
			
			triggerEvent(evt);
		}
		mOn = on;
	}
}
