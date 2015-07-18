package com.fbessou.sofa.message;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadJoinMessage extends ProxyMessage {
	
	/** Nickname given by the game-pad. Empty string by default. Should not be null **/
	private final String mNickname;
	private final UUID mUUID;
	/** Id of the game that sends the message **/
	private final int mGamePadId;
	
	/**
	 * 
	 * @param message Message to transmit
	 * @param gamePadId Id of the game pad.
	 */
	public ProxyGamePadJoinMessage(GamePadJoinMessage message, int gamePadId) {
		super(message);
		mNickname = message.mNickname;
		mUUID = message.mUUID;
		mGamePadId = gamePadId;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGamePadJoinMessage(JSONObject json) throws Exception {
		super(json);
		mNickname = json.getString("name");
		mUUID = UUID.fromString(json.getString("uuid"));
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mNickname);
		json.put("uuid", mUUID.toString());
		json.put("gamepad", mGamePadId);
		
		return json;
	}
}
