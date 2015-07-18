/**
 * 
 */
package com.fbessou.sofa;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * PadEvent created by a game pad
 * @author Frank Bessou
 *
 */
public class PadEvent {
	public UUID uuid;
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
	 * ["name":<String>], ["uuid":<String>]}
	 */
	public PadEvent(JSONObject object) throws JSONException {
		switch (object.getString("type")) {
		case "join":
			eventType = PadEventType.JOIN;
			padName = object.getString("name");
			uuid = UUID.fromString(object.getString("uuid"));
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
		switch (eventType) {
		case JOIN:
			event.put("type", "join");
			event.put("name", padName);
			event.put("UUID", uuid.toString());
			break;
		case LEAVE:
			event.put("type","leave");
			break;
		case RENAME:
			event.put("type", "rename");
			event.put("name", padName);
			break;
		default:
			break;
		}
		return event;
	}

	public static PadEvent createLeaveEvent(){
		PadEvent event = new PadEvent(PadEventType.LEAVE);
		return event;
	}
	
	public static PadEvent createJoinEvent(UUID uuid, String name){
		PadEvent event = new PadEvent(PadEventType.JOIN);
		event.padName = name;
		event.uuid = uuid;
		return event;
	}
	
	public static PadEvent createRenameEvent(String name){
		PadEvent event = new PadEvent(PadEventType.RENAME);
		event.padName = name;
		return event;
	}
}
