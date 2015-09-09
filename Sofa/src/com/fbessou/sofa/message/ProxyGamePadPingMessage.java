package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadPingMessage extends ProxyMessage {
	
	/**
	 */
	public ProxyGamePadPingMessage() {
		super(Type.PING);
	}

	/**
	 * Instantiate a message from a JSON object
	 * @throws Exception If message cannot be instantiate from the json object for any reasons.
	 * */
	public ProxyGamePadPingMessage(JSONObject json) throws Exception {
		super(json);
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		return json;
	}
}
