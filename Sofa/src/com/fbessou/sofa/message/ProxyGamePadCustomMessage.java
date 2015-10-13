package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadCustomMessage extends ProxyMessage {

	private String mCustomMessage;
	/** Id of the game that sends the message **/
	private final int mGamePadId;
	
	
	/**
	 * @params message Message to transmit
	 */
	public ProxyGamePadCustomMessage(GamePadCustomMessage message, int gamepad) {
		super(message);
		mCustomMessage = message.mCustomMessage;
		mGamePadId = gamepad;
	}

	/**
	 * Instantiate a message from a JSON object
	 * @throws Exception If message cannot be instantiate from the json object for any reasons.
	 * */
	public ProxyGamePadCustomMessage(JSONObject json) throws Exception {
		super(json);
		mCustomMessage = json.getString("custom");
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("custom", mCustomMessage);
		json.put("gamepad", mGamePadId);
		
		return json;
	}
	

	public int getGamePadId() {
		return mGamePadId;
	}
	
	/** Returns the custom message  */
	public String getCustomMessage() {
		return mCustomMessage;
	}
}
