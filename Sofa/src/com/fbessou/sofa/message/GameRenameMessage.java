package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GameRenameMessage extends Message {
	
	/** New name given by the game. Empty string by default. **/
	protected final String mNewName;
	
	/**
	 * 
	 * @param newName New name of the game. Equivalent to en empty string if null
	 */
	public GameRenameMessage(String newName) {
		super(Type.RENAME);
		mNewName = newName == null ? "" : newName;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GameRenameMessage(JSONObject json) throws Exception {
		super(json);
		mNewName = json.getString("name");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mNewName);
		
		return json;
	}
}
