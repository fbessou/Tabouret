/**
 * 
 */
package com.fbessou.sofa;

import java.lang.reflect.Array;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class InputEvent {

	public int inputId = 0;
	public int padId = 0;
	
	// Content description
	public InputEventType eventType;
	public float x = 0;
	public float y = 0;
	public float z = 0;
	public String text;

	// ? public Sensor.SensorType sensorType;
	public enum InputEventType {
		KEY_DOWN, KEY_UP, MOTION_1D, MOTION_2D, MOTION_3D, TEXT_SENT
	}

	/**
	 * Create an empty {@link InputEvent} of type determined by type
	 * @param type The type of the created event
	 */
	public InputEvent(InputEventType type) {
		eventType = type;
	}

	/**
	 * Create an {@link InputEvent} instance from a {@link JSONObject}
	 * @param jo
	 * @throws JSONException
	 */
	public InputEvent(JSONObject jo) throws JSONException {
		padId = jo.getInt("pad");
		inputId = jo.getInt("input");
		switch (jo.getString("type")) {
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

	/** 
	 * returns the JSON string corresponding to this event
	 */
	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return "{\"type\":\"unknown\"}";
		}
	}

	/**
	 * Create a JSONObject from an instance of {@link InputEvent}
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("input", inputId);
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
			object.put("text", text);
		default:
			break;
		}
		return object;
	}
	
	/**
	 * Create a key down event
	 */
	static public InputEvent createKeyDownEvent(int inputId, int padId){
		InputEvent evt = new InputEvent(InputEventType.KEY_DOWN);
		evt.inputId = inputId;
		evt.padId = padId;
		return evt;
	}
	
	/**
	 * Create a key up event
	 */
	static public InputEvent createKeyUpEvent(int inputId, int padId){
		InputEvent evt = new InputEvent(InputEventType.KEY_UP);
		evt.inputId = inputId;
		evt.padId = padId;
		return evt;
	}
	
	/**
	 * Create a motion1d event
	 */
	static public InputEvent createMotion1DEvent(int inputId, float x, int padId){
		InputEvent evt = new InputEvent(InputEventType.MOTION_1D);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.x = x;
		return evt;
	}
	
	/**
	 * Create a motion2d event
	 */
	static public InputEvent createMotion2DEvent(int inputId, float x, float y, int padId){
		InputEvent evt = new InputEvent(InputEventType.MOTION_2D);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.x = x;
		evt.y = y;
		return evt;
	}
	
	/**
	 * Create a motion 2d event from an array
	 */
	public InputEvent createMotion2DEvent(int inputId, Object arr, int padId) throws IllegalArgumentException{
		if(!arr.getClass().isArray())
			throw new IllegalArgumentException("Second argument is not an array :"+arr.getClass());
		InputEvent evt = new InputEvent(InputEventType.MOTION_2D);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.x = Array.getFloat(arr, 0);
		evt.y = Array.getFloat(arr, 1);
		return evt;
	}
	
	/**
	 * Create a motion3d event
	 */
	static public InputEvent createMotion3DEvent(int inputId, Float x, Float y, Float z, int padId){
		InputEvent evt = new InputEvent(InputEventType.MOTION_3D);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.x = x;
		evt.y = y;
		evt.z = z;
		return evt;
	}
	
	/**
	 * Create a motion3d event from an array
	 */
	static public InputEvent createMotion3DEvent(int inputId, Object arr, int padId) throws IllegalArgumentException{
		if(!arr.getClass().isArray())
			throw new IllegalArgumentException("Second argument is not an array :"+arr.getClass());
		InputEvent evt = new InputEvent(InputEventType.MOTION_3D);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.x = Array.getFloat(arr, 0);
		evt.y = Array.getFloat(arr, 1);
		evt.z = Array.getFloat(arr, 2);

		return evt;
	}
}
