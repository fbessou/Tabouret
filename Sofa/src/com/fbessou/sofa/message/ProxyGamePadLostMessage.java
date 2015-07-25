package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadLostMessage extends ProxyMessage {
	
	/** Id of the game that sends the message **/
	private final int mGamePadId;
	
	/**
	 * 
	 * @param message Message to transmit
	 * @param gamePadId Id of the game pad.
	 */
	public ProxyGamePadLostMessage(int gamePadId) {
		super(Type.LOST);
		mGamePadId = gamePadId;
	}

	/**
	 * Instantiate a message from a JSON object
	 * @throws Exception If message cannot be instantiate from the json object for any reasons.
	 * */
	public ProxyGamePadLostMessage(JSONObject json) throws Exception {
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
