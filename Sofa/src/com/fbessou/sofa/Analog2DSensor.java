/**
 * 
 */
package com.fbessou.sofa;


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
		boolean changed = (Math.abs(x-mValueX)>0.0001 || Math.abs(y-mValueY)>0.0001 );
		mValueX = Math.max(-1, Math.min(1,x));
		mValueY = Math.max(-1, Math.min(1,y));
		if(changed){
			InputEvent evt = InputEvent.createMotion2DEvent(0/*TODO*/, mValueX, mValueY, mId);
			
			triggerEvent(evt);
		}
	}
}
