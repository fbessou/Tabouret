package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGameCustomMessage extends ProxyMessage {

	private String mCustomMessage;
	
	/**
	 * @params message Message to transmit
	 */
	public ProxyGameCustomMessage(GameCustomMessage message) {
		super(message);
		mCustomMessage = message.mCustomMessage;
	}

	/**
	 * Instantiate a message from a JSON object
	 * @throws Exception If message cannot be instantiate from the json object for any reasons.
	 * */
	public ProxyGameCustomMessage(JSONObject json) throws Exception {
		super(json);
		mCustomMessage = json.getString("custom");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("custom", mCustomMessage);
		
		return json;
	}
	
	/** Returns the custom message  */
	public String getCustomMessage() {
		return mCustomMessage;
	}
}
