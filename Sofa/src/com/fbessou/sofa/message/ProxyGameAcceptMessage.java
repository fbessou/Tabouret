package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGameAcceptMessage extends ProxyMessage {

	/** Name given by the game. Empty string by default. **/
	protected final String mName;
	
	/**
	 * @params message Message to transmit
	 */
	public ProxyGameAcceptMessage(GameAcceptMessage message) {
		super(message);
		mName = message.mName;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGameAcceptMessage(JSONObject json) throws Exception {
		super(json);
		mName = json.getString("name");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mName);
		
		return json;
	}
}
