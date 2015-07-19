package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadLeaveMessage extends ProxyMessage {
	
	/** Id of the game that sends the message **/
	private final int mGamePadId;
	
	/**
	 * 
	 * @param message Message to transmit
	 * @param gamePadId Id of the game pad.
	 */
	public ProxyGamePadLeaveMessage(GamePadLeaveMessage message, int gamePadId) {
		super(message);
		mGamePadId = gamePadId;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGamePadLeaveMessage(JSONObject json) throws Exception {
		super(json);
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("gamepad", mGamePadId);
		
		return json;
	}

	
	public int getGamePadId() {
		return mGamePadId;
	}
}
