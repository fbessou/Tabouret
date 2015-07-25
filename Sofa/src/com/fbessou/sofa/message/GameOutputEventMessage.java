package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbessou.sofa.OutputEvent;

/**
 * @author Proïd
 *
 */
public class GameOutputEventMessage extends Message {
	
	protected final OutputEvent mOutputEvent;
	/** Id of the game-pad recipient **/
	private final int mGamePadId;
	
	/**
	 * 
	 * @param name Name of the game. Equivalent to en empty string if null
	 */
	public GameOutputEventMessage(OutputEvent outputEvent, int gamePadId) {
		super(Type.OUTPUTEVENT);
		mOutputEvent = outputEvent;
		mGamePadId = gamePadId;
	}
	
	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GameOutputEventMessage(JSONObject json) throws Exception {
		super(json);
		mOutputEvent = new OutputEvent(json.getJSONObject("event"));
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("event", mOutputEvent.toJSON());
		json.put("gamepad", mGamePadId);
		
		return json;
	}

	/** Returns the ID of the game-pad recipient **/
	public int getGamePadId() {
		return mGamePadId;
	}
}
