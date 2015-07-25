/**
 * 
 */
package com.fbessou.sofa.sensor;

import com.fbessou.sofa.InputEvent;


/**
 * @author Frank Bessou
 *
 */
public class Analog2DSensor extends Sensor {
	private float mValueX = 0;
	private float mValueY = 0;

	/**
	 * @param type
	 */
	public Analog2DSensor() {
		super(SensorType.ANALOG_2D);
	}
	
	public void putValue(float x,float y){
		// Clamp values
		x = Math.max(-1, Math.min(x, 1));
		y = Math.max(-1, Math.min(y, 1));
		
		boolean changed = (Math.abs(x-mValueX) > 0.001 || Math.abs(y-mValueY) > 0.001);
		if(changed){
			mValueX = x;
			mValueY = y;
			
			InputEvent evt = InputEvent.createMotion2DEvent(0/*TODO*/, mValueX, mValueY, mId);
			
			triggerEvent(evt);
		}
	}
}
