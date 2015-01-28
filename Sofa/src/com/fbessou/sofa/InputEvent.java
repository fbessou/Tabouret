/**
 * 
 */
package com.fbessou.sofa;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class InputEvent{

	public int inputId;
	public int padId;
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
	public InputEvent(JSONObject jo) throws JSONException {
		padId = jo.getInt("pad");
		inputId = jo.getInt("input");
		switch(jo.getString("type")) {
		case "key_up":
			eventType = EventType.KEY_UP;
			break;
		case "key_down":
			eventType = EventType.KEY_DOWN;
			break;
		case "motion1d":
			eventType = EventType.MOTION_1D;
			x = (float) jo.getDouble("x");
			break;
		case "motion2d":
			eventType = EventType.MOTION_2D;
			x = (float) jo.getDouble("x");
			y = (float) jo.getDouble("y");
			break;
		case "motion3d":
			eventType = EventType.MOTION_3D;
			x = (float) jo.getDouble("x");
			y = (float) jo.getDouble("y");
			z = (float) jo.getDouble("z");
			break;
		case "text":
			eventType = EventType.TEXT_SENT;
			text = jo.getString("text");
			break;
		default:
			throw new JSONException("Invalid input event type");
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String ret = ""+padId+" "+inputId+" : ";
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
		case MOTION_3D:
			ret+="motion x:"+x+" y:"+y+" z:"+z;
			break;
		case TEXT_SENT:
			ret+="text \""+text+"\"";
			break;
		default:
			break;
		}
		return ret;
	}
	
}
