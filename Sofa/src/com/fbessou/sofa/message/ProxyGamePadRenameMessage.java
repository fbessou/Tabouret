package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadRenameMessage extends ProxyMessage {
	
	/** Nickname given by the game-pad. Empty string by default. Should not be null **/
	private final String mNewNickname;
	/** Id of the game that sends the message **/
	private final int mGamePadId;

	/**
	 * 
	 * @param message Message to transmit
	 * @param gamePadId Id of the game pad.
	 */
	public ProxyGamePadRenameMessage(GamePadRenameMessage message, int gamePadId) {
		super(message);
		mNewNickname = message.mNewNickname;
		mGamePadId = gamePadId;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGamePadRenameMessage(JSONObject json) throws Exception {
		super(json);
		mNewNickname = json.getString("name");
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mNewNickname);
		json.put("gamepad", mGamePadId);
		
		return json;
	}
}
