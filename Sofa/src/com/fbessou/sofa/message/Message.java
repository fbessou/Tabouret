package com.fbessou.sofa.message;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Define a message and its content that can be send
 * @author Proïd
 *
 */
public class Message {
	/**
	 * Type of message. Use toString() to get the name in lower case.
	 **/
	public enum Type {
		JOIN,// TODO ?? (GamePadJoinMessage.class, GameJoinMessage.class, ProxyGamePadJoinMessage.class, ProxyGameJoinMessage.class),
		RENAME,
		LEAVE,
		INPUTEVENT,
		OUTPUTEVENT,
		ACCEPT;
		
		public String toString() {
			return super.toString().toLowerCase(Locale.ENGLISH);
		};
		public static Type get(String name) {
			return Type.valueOf(name.toUpperCase(Locale.ENGLISH));
		}
	};
	
	private final Type mType;
	
	/**
	 * Create a new message
	 * @param type
	 */
	protected Message(Type type) {
		mType = type;
	}
	
	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	Message(JSONObject json) throws Exception {
		String type = json.getString("type");
		mType = Type.get(type);
	}
	
	public Type getType() {
		return mType;
	}
	
	/** Returns the JSON string corresponding to this message **/
	protected JSONObject toJSON()  throws JSONException{
		JSONObject json = new JSONObject();
		
		json.put("type", mType.toString());
		
		return json;
	}
	
	/** Returns the message as a JSON object **/
	final public String toString() {
		try {
			return toJSON().toString();
		} catch (JSONException e) {
			return "";
		}
	}

	/**
	 * Returns the game message corresponding to the given JSON object. You can cast the message
	 * according to the {@link Message#getType()} value.
	 * 
	 * @param json JSON object used to create message.
	 * @return Message corresponding to this JSON object. 
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 */
	public static Message gameFromJSON(JSONObject json) throws Exception {
		Message msg = new Message(json);
		switch(msg.getType()) {
		case JOIN:
			return new GameJoinMessage(json);
		case LEAVE:
			return new GameLeaveMessage(json);
		case ACCEPT:
			return new GameAcceptMessage(json);
		case RENAME:
			return new GameRenameMessage(json);
		case OUTPUTEVENT:
			return new GameOutputEventMessage(json);
		case INPUTEVENT: // Should not occur
		default:
			return msg;
		}
	}
	
	/**
	 * Returns the game-pad message corresponding to the given JSON object. You can cast the message
	 * according to the {@link Message#getType()} value.
	 * 
	 * @param json JSON object used to create message.
	 * @return Message corresponding to this JSON object. 
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 */
	public static Message gamePadFromJSON(JSONObject json) throws Exception {
		Message msg = new Message(json);
		switch(msg.getType()) {
		case JOIN:
			return new GamePadJoinMessage(json);
		case LEAVE:
			return new GamePadLeaveMessage(json);
		case RENAME:
			return new GamePadRenameMessage(json);
		case INPUTEVENT:
			return new GamePadInputEventMessage(json);
		case OUTPUTEVENT: // Should not occur
		case ACCEPT: // Should not occur
		default:
			return msg;
		}
	}
}
