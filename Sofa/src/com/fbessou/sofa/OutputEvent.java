/**
 * 
 */
package com.fbessou.sofa;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Frank Bessou
 *
 */
public class OutputEvent {
	/**
	 * The target pad of this event
	 * -1 for broadcast
	 */
	public Integer padId = null;
	
	/**
	 * The target output of the event.
	 * (ex : for a text : the textView id)
	 */
	public Integer outputId=null;
	
	/**
	 * For a state event, set state of an output.
	 */
	public Integer state=null;
	
	/**
	 * sound id to play
	 */
	public Integer sound=null;
	
	/**
	 * text to provide to output identified by outputId
	 */
	public String text;
	
	
	/**
	 * Type of FEEDBACK effect to be used
	 */
	//TODO replace with an enum
	public Integer feedback=null;
	
	/**
	 * If FEEDBACKEffect is CUSTOM, vibrations indicates how the Vibrator should vibrate.
	 * The values in this array indicates the active and inactive time of the vibrator in milliseconds.
	 * Ex : [15,10,30,10,20,10,100] means a vibration will be made during 15ms before a pause of 10ms, followed by another pulse of 30ms...
	 */
	public ArrayList<Integer> vibrations;
	
	/**
	 * Enum containing all the types a OutputEvent can be.
	 *
	 */
	public enum OutputEventType{
		FEEDBACK,
		TEXT, // Needs an output id
		SOUND,
		STATE; // Needs an output id
	};
	
	/**
	 * Type of this particular instance of event.
	 */
	public OutputEventType eventType=null;

	/**
	 * Construct an OutputEvent from scratch.
	 */
	public OutputEvent(OutputEventType type) {
		eventType=type;
	}
	
	/**
	 * Construct an InputEvent from a JSONObject
	 */
	public OutputEvent(JSONObject obj) throws JSONException {

		padId=obj.getInt("pad");
		switch (obj.getString("type")) {
		case "FEEDBACK":
			eventType=OutputEventType.FEEDBACK;
			feedback=obj.getInt("feedback");
			break;
		case "text":
			eventType=OutputEventType.TEXT;
			outputId=obj.getInt("output");
			text=obj.getString("text");
			break;
		case "sound":
			eventType=OutputEventType.SOUND;
			sound=obj.getInt("sound");
			break;
		case "state":
			eventType=OutputEventType.STATE;
			outputId=obj.getInt("output");
			state=obj.getInt("state");
			break;
		default:
			throw new JSONException("Invalid JSONObject");
		}
	}
	
	/**
	 * 
	 * @return a JSONObject that can be sent to a GamePad (eventually through a GameIOProxy)
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put("pad",padId);
		switch (eventType) {
		case FEEDBACK:
			obj.put("type", "FEEDBACK");
			switch (feedback) {
			case -1:
				obj.put("feedback", -1);
				//TODO load array of vibrations
				break;
			default:
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
	 * 
	 * @return
	 */
	public static OutputEvent createFeedback(Integer feedback, Integer padId){
		OutputEvent evt = new OutputEvent(OutputEventType.FEEDBACK);
		evt.padId=padId;
		evt.feedback = feedback;
		return evt;
	}
	/**
	 * 
	 * @param feedback
	 * @param padId
	 * @return
	 */
	public static OutputEvent createFeedback(ArrayList<Integer> list, Integer padId){
		OutputEvent evt = new OutputEvent(OutputEventType.FEEDBACK);
		evt.padId=padId;
		evt.vibrations = list;
		return evt;
	}
	
	public static OutputEvent createTextEvent(String text, Integer padId, Integer outputId){
		OutputEvent evt = new OutputEvent(OutputEventType.TEXT);
		evt.padId=padId;
		evt.text=text;
		evt.outputId=outputId;
		return evt;
	}
	
	/**
	 * 
	 * @param text
	 * @param padId
	 * @param outputId
	 * @return
	 */
	public static OutputEvent createStateEvent(Integer state, Integer padId, Integer outputId){
		OutputEvent evt = new OutputEvent(OutputEventType.STATE);
		evt.padId=padId;
		evt.state=state;
		evt.outputId=outputId;
		return evt;
	}
}
