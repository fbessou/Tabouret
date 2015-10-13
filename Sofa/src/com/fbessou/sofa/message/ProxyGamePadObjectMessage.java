package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadObjectMessage extends ProxyMessage {

	private String mObjectToString;
	
	/**
	 * @params message Message to transmit
	 */
	public ProxyGamePadObjectMessage(GamePadObjectMessage message) {
		super(message);
		mObjectToString = message.mObjectToString;
	}

	/**
	 * Instantiate a message from a JSON object
	 * @throws Exception If message cannot be instantiate from the json object for any reasons.
	 * */
	public ProxyGamePadObjectMessage(JSONObject json) throws Exception {
		super(json);
		mObjectToString = json.getString("object");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("object", mObjectToString);
		
		return json;
	}
	
	/** Returns the string representation of the object */
	public String getStringObject() {
		return mObjectToString;
	}
}
