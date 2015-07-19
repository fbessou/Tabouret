package com.fbessou.sofa.message;

import org.json.JSONObject;

/**
 * A message that is sent by the proxy. It will be made
 * using an other message that the proxy has to transmit. 
 **/
public class ProxyMessage extends Message {

	/**
	 * Instatiate the proxy message from an other message.
	 * @param message Message to transmit
	 */
	protected ProxyMessage(Message message) {
		super(message.getType());
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	ProxyMessage(JSONObject json) throws Exception {
		super(json);
	}

	/**
	 * Returns the game proxy message corresponding to the given JSON object. You can cast the message
	 * according to the {@link Message#getType()} value.
	 * 
	 * @param json JSON object used to create message.
	 * @return Message corresponding to this JSON object. 
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 */
	public static ProxyMessage gameFromJSON(JSONObject json) throws Exception {
		ProxyMessage msg = new ProxyMessage(json);
		switch(msg.getType()) {
		case JOIN:
			return new ProxyGameJoinMessage(json);
		case LEAVE:
			return new ProxyGameLeaveMessage(json);
		case ACCEPT:
			return new ProxyGameAcceptMessage(json);
		case RENAME:
			return new ProxyGameRenameMessage(json);
		case OUTPUTEVENT:
			return new ProxyGameOutputEventMessage(json);
		case INPUTEVENT: // Should not occur
		default:
			return msg;
		}
	}
	
	/**
	 * Returns the game-pad proxy message corresponding to the given JSON object. You can cast the message
	 * according to the {@link Message#getType()} value.
	 * 
	 * @param json JSON object used to create message.
	 * @return Message corresponding to this JSON object. 
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 */
	public static ProxyMessage gamePadFromJSON(JSONObject json) throws Exception {
		ProxyMessage msg = new ProxyMessage(json);
		switch(msg.getType()) {
		case JOIN:
			return new ProxyGamePadJoinMessage(json);
		case LEAVE:
			return new ProxyGamePadLeaveMessage(json);
		case RENAME:
			return new ProxyGamePadRenameMessage(json);
		case INPUTEVENT:
			return new ProxyGamePadInputEventMessage(json);
		case OUTPUTEVENT: // Should not occur
		case ACCEPT: // Should not occur
		default:
			return msg;
		}
	}
}
