/**
 * 
 */
package com.fbessou.sofa;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author Frank Bessou
 *
 */
public class PadEvent {
	public Integer padId = null;
	String padName = null;
	public PadEventType eventType = null;

	public enum PadEventType {
		JOIN, LEAVE, RENAME
	}

	public PadEvent(PadEventType type){
		eventType=type;
	}
	/**
	 * Reads a JSONObject in the form : {"type":<"join"|"leave"|"rename">,
	 * "pad":<int>, ["name":<String>]}
	 */
	public PadEvent(JSONObject object) throws JSONException {
		padId = object.getInt("pad");
		switch (object.getString("type")) {
		case "join":
			eventType = PadEventType.JOIN;
			break;
		case "leave":
			eventType = PadEventType.LEAVE;
			break;
		case "rename":
			eventType = PadEventType.RENAME;
			padName = object.getString("name");
		default:
			throw new JSONException("Invalid game pad event type");
		}
	}
	
	JSONObject toJSON() throws JSONException{
		JSONObject event = new JSONObject();
		event.put("pad",padId);
		switch (eventType) {
		case JOIN:
			event.put("type", "join");
			break;
		case LEAVE:
			event.put("type","leave");
			break;
		case RENAME:
			event.put("type", "rename");
			event.put("name",padName);
			break;
		default:
			break;
		}
		return event;
	}

	public static PadEvent createLeaveEvent(int pad){
		PadEvent event = new PadEvent(PadEventType.LEAVE);
		event.padId=pad;
		return event;
	}
	
	public static PadEvent createJoinEvent(int pad){
		PadEvent event = new PadEvent(PadEventType.JOIN);
		event.padId=pad;
		return event;
	}
	
	public static PadEvent createRenameEvent(int pad,String name){
		PadEvent event = new PadEvent(PadEventType.RENAME);
		event.padId=pad;
		event.padName=name;
		return event;
	}
}
