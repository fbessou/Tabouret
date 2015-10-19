/**
 * 
 */
package com.fbessou.sofa;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class OutputEvent {
	/**
	 * The target output of the event. (ex : for a text : the textView id) -1
	 * for unspecified
	 */
	public int outputId = -1;

	/**
	 * For a state event, set state of an output.
	 */
	public int state = 0;

	/**
	 * sound id to play
	 */
	public int sound;

	/**
	 * text to provide to output identified by outputId
	 */
	public String text;


	public static final int VIBRATE_SHORT = 0;
	public static final int VIBRATE_LONG = 1;
	public static final int VIBRATE_CUSTOM = 255;
	
	public static final int STATE_TRUE = 1, STATE_FALSE = 0;
	
	/**
	 * Type of haptic feedback effect to be used
	 */
	public int feedback;

	/**
	 * If Feedback effect is CUSTOM, vibrations indicates how the Vibrator should
	 * vibrate. The values in this array indicates the active and inactive time
	 * of the vibrator in milliseconds. Ex : [15,10,30,10,20,10,100] means a
	 * pause of 15ms before a vibration during 10ms, followed by
	 * another pause of 30ms...
	 */
	public long[] vibrations = null;

	/**
	 * Enum containing all the types a OutputEvent can be.
	 *
	 */
	public enum Type {
		FEEDBACK, // Need a feedback type
		TEXT, // Needs an output id
		SOUND, // Needs a sound id
		STATE; // Needs an output id
		
		public String toString() {
			return super.toString().toLowerCase(Locale.ENGLISH);
		};
		public static Type get(String name) {
			return Type.valueOf(name.toUpperCase(Locale.ENGLISH));
		}
	};

	/**
	 * Type of this particular instance of event.
	 */
	public Type eventType = null;

	/**
	 * Construct an OutputEvent from scratch.
	 */
	public OutputEvent(Type type) {
		eventType = type;
	}

	/**
	 * Construct an InputEvent from a JSONObject
	 */
	public OutputEvent(JSONObject obj) throws Exception {
		eventType = Type.get(obj.getString("type"));
		outputId = obj.getInt("outputId");
		
		switch (eventType) {
		case FEEDBACK:
			feedback = obj.getInt("feedback");
			if(feedback == VIBRATE_CUSTOM){
				JSONArray vibs = obj.getJSONArray("vibrations");
				int length = vibs.length();
				vibrations = new long[length];
				for(int i = 0; i < length;i++)
					vibrations[i] = vibs.optLong(i);
			}
			break;
		case TEXT:
			text = obj.getString("text");
			break;
		case SOUND:
			sound = obj.getInt("sound");
			break;
		case STATE:
			state = obj.getInt("state");
			break;
		default:
			throw new JSONException("Invalid JSONObject");
		}
	}

	/**
	 * 
	 * @return a JSONObject that can be sent to a GamePad (eventually through a
	 *         GameIOProxy)
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put("type", eventType.toString());
		obj.put("outputId", outputId);
		switch (eventType) {
		case FEEDBACK:
			obj.put("feedback", feedback);
			if (feedback == VIBRATE_CUSTOM) {
				JSONArray array = new JSONArray();
				for(long v : vibrations)
					array.put(v);
				obj.put("vibrations", array);
			}
			break;
		case STATE:
			obj.put("state", state);
			break;
		case SOUND:
			obj.put("sound", sound);
		case TEXT:
			obj.put("text", text);
			break;
		default:
			break;
		}
		return obj;
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

	/** Create a feedback output event (vibrate)
	 * @param feedback either VIBRATE_SHORT or VIBRATE_LONG */
	public static OutputEvent createFeedback(int feedback, int outputId) {
		OutputEvent evt = new OutputEvent(Type.FEEDBACK);
		evt.feedback = feedback;
		evt.outputId = outputId;
		return evt;
	}

	/** Create a custom feedback output event (vibrate)
	 * @param list The values in this array indicates the active and inactive time
	 * of the vibrator in milliseconds. Ex : [15,10,30,...] means a
	 * pause of 15ms before a 10ms long vibration, followed by
	 * another pause of 30ms... */
	public static OutputEvent createFeedback(ArrayList<Integer> list, int outputId) {
		OutputEvent evt = new OutputEvent(Type.FEEDBACK);
		evt.vibrations = new long[list.size()];
		for(int i = 0; i < list.size(); i++)
			evt.vibrations[i] = list.get(i);
		evt.outputId = outputId;
		return evt;
	}

	/**
	 * Create a text output event
	 * @param text the text to send
	 * @param outputId the id of the target game-pad's indicator
	 */
	public static OutputEvent createTextEvent(String text, int outputId) {
		OutputEvent evt = new OutputEvent(Type.TEXT);
		evt.text = text;
		evt.outputId = outputId;
		return evt;
	}

	/**
	 * Create a state output event
	 * @param state state to send. Can be either STATE_TRUE or STATE_FALSE (0 or 1) for a boolean state, any other values otherwise
	 * @param outputId the id of the target game-pad's indicator
	 */
	public static OutputEvent createStateEvent(int state, int outputId) {
		OutputEvent evt = new OutputEvent(Type.STATE);
		evt.state = state;
		evt.outputId = outputId;
		return evt;
	}
}
