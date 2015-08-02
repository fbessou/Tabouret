/**
 * 
 */
package com.fbessou.sofa.sensor;

import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.view.JoystickView;
import com.fbessou.sofa.view.JoystickView.OnPositionChangedListener;


/**
 * @author Frank Bessou
 *	Clamp values in range -1..1
 */
public class Analog2DSensor extends Sensor {
	private float mValueX = 0;
	private float mValueY = 0;

	/** Minimum variation for x or y values needed to trigger a new event **/
	private static final float deltaMinTrigger = 0.005f;
	
	public Analog2DSensor() {
		super();
	}
	public Analog2DSensor(int id) {
		super(id);
	}
	
	/** Attaches a joystick view to this sensor. Each action will trigger an event.**/
	public void attachTo(JoystickView joystickView) {
		joystickView.setOnPositionChangedListener(new OnPositionChangedListener() {
			@Override
			public void positionChanged(JoystickView joystick, float px, float py) {
				putValue(px, py);
			}
		});
	}
	
	protected void putValue(float x,float y){
		// Clamp values
		x = Math.max(-1, Math.min(x, 1));
		y = Math.max(-1, Math.min(y, 1));
		
		boolean changed = (Math.abs(x-mValueX) > deltaMinTrigger || Math.abs(y-mValueY) > deltaMinTrigger);
		if(changed){
			mValueX = x;
			mValueY = y;
			
			InputEvent evt = InputEvent.createMotion2DEvent(0/*TODO*/, mValueX, mValueY, mId);
			
			triggerEvent(evt);
		}
	}
}
