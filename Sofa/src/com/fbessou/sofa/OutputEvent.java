/**
 * 
 */
package com.fbessou.sofa;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class OutputEvent {

	/**
	 * The target pad of this event -1 for broadcast
	 */
	public int padId = -1;

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
	
	/**
	 * Type of haptic feedback effect to be used
	 */
	public int feedback;

	/**
	 * If FEEDBACKEffect is CUSTOM, vibrations indicates how the Vibrator should
	 * vibrate. The values in this array indicates the active and inactive time
	 * of the vibrator in milliseconds. Ex : [15,10,30,10,20,10,100] means a
	 * vibration will be made during 15ms before a pause of 10ms, followed by
	 * another pulse of 30ms...
	 */
	public ArrayList<Integer> vibrations = null;

	/**
	 * Enum containing all the types a OutputEvent can be.
	 *
	 */
	public enum OutputEventType {
		FEEDBACK,
		TEXT, // Needs an output id
		SOUND,
		STATE; // Needs an output id
	};

	/**
	 * Type of this particular instance of event.
	 */
	public OutputEventType eventType = null;

	/**
	 * Construct an OutputEvent from scratch.
	 */
	public OutputEvent(OutputEventType type) {
		eventType = type;
	}

	/**
	 * Construct an InputEvent from a JSONObject
	 */
	public OutputEvent(JSONObject obj) throws JSONException {

		padId = obj.getInt("pad");
		switch (obj.getString("type")) {
		case "feedback":
			eventType = OutputEventType.FEEDBACK;
			feedback = obj.getInt("feedback");
			if(feedback==VIBRATE_CUSTOM){
				JSONArray vibs =obj.getJSONArray("vibrations");
				int length = vibs.length();
				vibrations= new ArrayList<Integer>();
				for(int i = 0; i<length;i++){
					vibrations.add(vibs.optInt(i));
				}
			}
			break;
		case "text":
			eventType = OutputEventType.TEXT;
			outputId = obj.getInt("output");
			text = obj.getString("text");
			break;
		case "sound":
			eventType = OutputEventType.SOUND;
			sound = obj.getInt("sound");
			break;
		case "state":
			eventType = OutputEventType.STATE;
			outputId = obj.getInt("output");
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
		obj.put("pad",padId);
		switch (eventType) {
		case FEEDBACK:
			obj.put("type", "feedback");
			switch (feedback) {
			case VIBRATE_CUSTOM:
				obj.put("feedback", feedback);
				obj.put("vibrations", new JSONArray(vibrations));
				break;
			default:
				obj.put("feedback", feedback);
				break;
			}
			break;
		case STATE:
			obj.put("type", "state");
			obj.put("state", state);
			obj.put("output", outputId);
			break;
		case SOUND:
			obj.put("type", "sound");
			obj.put("sound", sound);
		case TEXT:
			obj.put("type", "text");
			obj.put("text", text);
			obj.put("output", outputId);
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

	/**
	 * 
	 * @return
	 */
	public static OutputEvent createFeedback(Integer feedback, Integer padId) {
		OutputEvent evt = new OutputEvent(OutputEventType.FEEDBACK);
		evt.padId = padId;
		evt.feedback = feedback;
		return evt;
	}

	/**
	 * 
	 * @param feedback
	 * @param padId
	 * @return
	 */
	public static OutputEvent createFeedback(ArrayList<Integer> list, Integer padId) {
		OutputEvent evt = new OutputEvent(OutputEventType.FEEDBACK);
		evt.padId = padId;
		evt.vibrations = list;
		return evt;
	}

	/**
	 * 
	 * @param text
	 * @param padId
	 * @param outputId
	 * @return
	 */
	public static OutputEvent createTextEvent(String text, Integer padId, Integer outputId) {
		OutputEvent evt = new OutputEvent(OutputEventType.TEXT);
		evt.padId = padId;
		evt.text = text;
		evt.outputId = outputId;
		return evt;
	}

	/**
	 * 
	 * @param text
	 * @param padId
	 * @param outputId
	 * @return
	 */
	public static OutputEvent createStateEvent(Integer state, Integer padId, Integer outputId) {
		OutputEvent evt = new OutputEvent(OutputEventType.STATE);
		evt.padId = padId;
		evt.state = state;
		evt.outputId = outputId;
		return evt;
	}
}
