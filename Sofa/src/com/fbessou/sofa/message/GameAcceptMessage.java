package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GameAcceptMessage extends Message {
	
	/** Name given by the game. Empty string by default. **/
	protected final String mName;
	/** Id of the game-pad recipient **/
	private final int mGamePadId;
	
	/**
	 * 
	 * @param name Name of the game. Equivalent to en empty string if null
	 * @param gamePadId Id of the game-pad recipient
	 */
	public GameAcceptMessage(String name, int gamePadId) {
		super(Type.ACCEPT);
		mName = name == null ? "" : name;
		mGamePadId = gamePadId;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GameAcceptMessage(JSONObject json) throws Exception {
		super(json);
		mName = json.getString("name");
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mName);
		json.put("gamepad", mGamePadId);
		
		return json;
	}
	
	/** Returns the ID of the game-pad recipient **/
	public int getGamePadId() {
		return mGamePadId;
	}
}
