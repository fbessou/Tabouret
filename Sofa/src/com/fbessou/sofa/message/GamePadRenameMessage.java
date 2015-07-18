package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GamePadRenameMessage extends Message {
	
	/** New nickname given by the game-pad. Empty string by default. Should not be null **/
	protected final String mNewNickname;
	
	/**
	 * 
	 * @param newNickname New name of the game pad. Equivalent to en empty string if null
	 */
	public GamePadRenameMessage(String newNickname) {
		super(Type.RENAME);
		mNewNickname = newNickname == null ? "" : newNickname;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GamePadRenameMessage(JSONObject json) throws Exception {
		super(json);
		mNewNickname = json.getString("name");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mNewNickname);
		
		return json;
	}
}
