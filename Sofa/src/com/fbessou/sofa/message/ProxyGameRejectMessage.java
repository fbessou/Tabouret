package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGameRejectMessage extends ProxyMessage {

	/**
	 * @params message Message to transmit
	 */
	public ProxyGameRejectMessage(GameRejectMessage message) {
		super(message);
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGameRejectMessage(JSONObject json) throws Exception {
		super(json);
	}
	
	public ProxyGameRejectMessage() {
		super(Type.REJECT);
	}

	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		return json;
	}
}
