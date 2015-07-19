/**
 * 
 */
package com.fbessou.sofa;

import java.lang.reflect.Array;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class InputEvent {
	
	// FIXME use public Sensor.SensorType instead ? no ! sensor should generate inputeventtype and offer other service like clamp values
	public enum Type {
		KEYDOWN, KEYUP, FLOATDOWN, FLOATMOVE, FLOATUP, TEXT;

		public String toString() {
			return super.toString().toLowerCase(Locale.ENGLISH);
		};
		public static InputEvent.Type get(String name) {
			return InputEvent.Type.valueOf(name.toUpperCase(Locale.ENGLISH));
		}
	}

	/** Id of the input **/
	public int inputId = 0;
	/** Id of the pad that triggers the event **/
	public int padId = 0;
	
	/** Content description **/
	public InputEvent.Type eventType;
	public float values[];
	public String text;


	/**
	 * Create an empty {@link InputEvent} of type determined by type
	 * @param type The type of the created event
	 */
	public InputEvent(InputEvent.Type type) {
		eventType = type;
	}

	/**
	 * Create an {@link InputEvent} instance from a {@link JSONObject}
	 * @param jo
	 * @throws Exception if the input event cannot be instantiate for any reasons.
	 */
	public InputEvent(JSONObject jo) throws Exception {
		padId = jo.getInt("pad");
		inputId = jo.getInt("input");
		eventType = InputEvent.Type.get(jo.getString("type")); // throws exception if type unknown
		switch (eventType) {
		case FLOATDOWN:
		case FLOATMOVE:
		case FLOATUP:
			JSONArray array = jo.getJSONArray("values");
			values = new float[array.length()];
			for(int i = 0; i < values.length; i++)
				values[i] = (float) array.getDouble(i);
			break;
			
		case TEXT:
			text = jo.getString("text");
			break;
			
		case KEYDOWN:
		case KEYUP:
			// nothing else to read
			break;
		}
	}

	/**
	 * Returns the first value of the array {@code values}. The array size
	 * must be at least 1.
	 * */
	public float getX() {
		return values[0];
	}
	/**
	 * Returns the second value of the array {@code values}. The array size
	 * must be at least 2.
	 * */
	public float getY() {
		return values[1];
	}
	/**
	 * Returns the third value of the array {@code values}. The array size
	 * must be at least 3.
	 * */
	public float getZ() {
		return values[2];
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
		object.put("pad", padId);
		object.put("tyoe", eventType.toString());
		switch (eventType) {
		case FLOATDOWN:
		case FLOATMOVE:
		case FLOATUP:
			JSONArray array = new JSONArray();
			for(float v : values)
				array.put(v);
			object.put("values", array);
			break;
			
		case TEXT:
			object.put("text", text);
			break;
			
		case KEYDOWN:
		case KEYUP:
			// nothing else to put
			break;
		}
		return object;
	}
	
	/**
	 * Create a key down event
	 */
	static public InputEvent createKeyDownEvent(int inputId, int padId){
		InputEvent evt = new InputEvent(InputEvent.Type.KEYDOWN);
		evt.inputId = inputId;
		evt.padId = padId;
		return evt;
	}
	
	/**
	 * Create a key up event
	 */
	static public InputEvent createKeyUpEvent(int inputId, int padId){
		InputEvent evt = new InputEvent(InputEvent.Type.KEYUP);
		evt.inputId = inputId;
		evt.padId = padId;
		return evt;
	}
	
	/**
	 * Create a motion1d event
	 */
	static public InputEvent createMotion1DEvent(int inputId, float x, int padId){
		InputEvent evt = new InputEvent(InputEvent.Type.FLOATMOVE);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.values= new float[] {x};
		return evt;
	}
	
	/**
	 * Create a motion2d event
	 */
	static public InputEvent createMotion2DEvent(int inputId, float x, float y, int padId){
		InputEvent evt = new InputEvent(InputEvent.Type.FLOATMOVE);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.values= new float[] {x, y};
		return evt;
	}
	
	/**
	 * Create a motion 2d event from an array
	 */
	public InputEvent createMotion2DEvent(int inputId, Object arr, int padId) throws IllegalArgumentException{
		if(!arr.getClass().isArray())
			throw new IllegalArgumentException("Second argument is not an array :"+arr.getClass());
		InputEvent evt = new InputEvent(InputEvent.Type.FLOATMOVE);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.values= new float[] {
				Array.getFloat(arr, 0),
				Array.getFloat(arr, 1),
			};
		return evt;
	}
	
	/**
	 * Create a motion3d event
	 */
	static public InputEvent createMotion3DEvent(int inputId, float x, float y, float z, int padId){
		InputEvent evt = new InputEvent(InputEvent.Type.FLOATMOVE);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.values= new float[] {x, y, z};
		return evt;
	}
	
	/**
	 * Create a motion3d event from an array
	 */
	static public InputEvent createMotion3DEvent(int inputId, Object arr, int padId) throws IllegalArgumentException{
		if(!arr.getClass().isArray())
			throw new IllegalArgumentException("Second argument is not an array :"+arr.getClass());
		InputEvent evt = new InputEvent(InputEvent.Type.FLOATMOVE);
		evt.inputId = inputId;
		evt.padId = padId;
		evt.values= new float[] {
				Array.getFloat(arr, 0),
				Array.getFloat(arr, 1),
				Array.getFloat(arr, 2),
			};

		return evt;
	}
}
