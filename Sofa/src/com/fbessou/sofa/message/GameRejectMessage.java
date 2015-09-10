package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GameRejectMessage extends Message {
	
	/** Id of the game-pad recipient **/
	private final int mGamePadId;
	
	/**
	 * 
	 * @param gamePadId Id of the game-pad recipient
	 */
	public GameRejectMessage(int gamePadId) {
		super(Type.REJECT);
		mGamePadId = gamePadId;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GameRejectMessage(JSONObject json) throws Exception {
		super(json);
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("gamepad", mGamePadId);
		
		return json;
	}
	
	/** Returns the ID of the game-pad recipient **/
	public int getGamePadId() {
		return mGamePadId;
	}
}
