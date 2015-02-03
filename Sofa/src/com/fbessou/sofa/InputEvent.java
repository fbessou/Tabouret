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

	public int inputId=0;
	public int padId=0;
	//Content description
	public InputEventType eventType;
	public float x=0;
	public float y=0;
	public float z=0;
	public String text;
	// ? public Sensor.SensorType sensorType;
	public enum InputEventType{
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
	public InputEvent(InputEventType type) {
		eventType=type;
	}
	public InputEvent(JSONObject jo) throws JSONException {
		padId = jo.getInt("pad");
		inputId = jo.getInt("input");
		switch(jo.getString("type")) {
		case "key_up":
			eventType = InputEventType.KEY_UP;
			break;
		case "key_down":
			eventType = InputEventType.KEY_DOWN;
			break;
		case "motion1d":
			eventType = InputEventType.MOTION_1D;
			x = (float) jo.getDouble("x");
			break;
		case "motion2d":
			eventType = InputEventType.MOTION_2D;
			x = (float) jo.getDouble("x");
			y = (float) jo.getDouble("y");
			break;
		case "motion3d":
			eventType = InputEventType.MOTION_3D;
			x = (float) jo.getDouble("x");
			y = (float) jo.getDouble("y");
			z = (float) jo.getDouble("z");
			break;
		case "text":
			eventType = InputEventType.TEXT_SENT;
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
	
	/**
	 * 
	 */
	private JSONObject toJSON() {
		JSONObject object = new JSONObject();
		try {
			switch (eventType) {
			case KEY_DOWN:
				object.put("type", "keydown");
				break;
			case KEY_UP:
				object.put("type", "keyup");
				break;
			case MOTION_1D:
				object.put("type", "motion1d");
				object.put("x", x);
				break;
			case MOTION_2D:
				object.put("type", "motion2d");
				object.put("x", x);
				object.put("y", y);
				break;
			case MOTION_3D:
				object.put("type", "motion3d");
				object.put("x", x);
				object.put("y", y);
				object.put("z", z);
				break;
			case TEXT_SENT:
			default:
				break;
			}
		} catch (Exception e) {
			object=null;
		}

		return object;
		
	}
	
}
