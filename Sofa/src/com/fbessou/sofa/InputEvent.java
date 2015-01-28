/**
 * 
 */
package com.fbessou.sofa;

import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class InputEvent{
	
	public int sourceId;
	//Content description
	public EventType eventType;
	public float x=0;
	public float y=0;
	public float z=0;
	public String text;
	// ? public Sensor.SensorType sensorType;
	public enum EventType{
		KEY_DOWN,
		KEY_UP,
		MOTION_1D,
		MOTION_2D,
		MOTION_3D,
		TEXT_SENT
	}
	/**
	 * 
	 */
	public InputEvent(EventType type) {
		eventType=type;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String ret = Integer.toString(sourceId)+" : ";
		switch (eventType) {
		case KEY_DOWN:
			ret+="pressed";
			break;
		case KEY_UP:
			ret+="released";
			break;
		case MOTION_1D:
			ret+="aze";
			break;
		case MOTION_2D:
			ret+="motion x:"+x+" y:"+y;
			break;
		default:
			break;
		}
		return ret;
	}
	
}
