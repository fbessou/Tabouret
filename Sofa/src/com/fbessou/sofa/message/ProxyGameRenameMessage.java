package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class ProxyGameRenameMessage extends ProxyMessage {

	/** New name given by the game. Empty string by default. **/
	protected final String mNewName;
	
	/**
	 * @params message Message to transmit
	 */
	public ProxyGameRenameMessage(GameRenameMessage message) {
		super(message);
		mNewName = message.mNewName;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGameRenameMessage(JSONObject json) throws Exception {
		super(json);
		mNewName = json.getString("name");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mNewName);
		
		return json;
	}

	
	public String getNewName() {
		return mNewName;
	}
}
	