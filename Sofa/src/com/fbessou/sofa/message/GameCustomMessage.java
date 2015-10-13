package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

public class GameCustomMessage extends Message {
	
	protected String mCustomMessage;
	/** Id of the game-pad recipient **/
	private final int mGamePadId;
	
	public GameCustomMessage(String customMessage, int gamePadId) {
		super(Type.CUSTOM);
		mCustomMessage = customMessage;
		mGamePadId = gamePadId;
	}
	
	GameCustomMessage(JSONObject json) throws Exception {
		super(json);
		mCustomMessage = json.getString("custom");
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put("custom", mCustomMessage);
		json.put("gamepad", mGamePadId);
		
		return json;
	}
	
	/** Returns the ID of the game-pad recipient **/
	public int getGamePadId() {
		return mGamePadId;
	}
}
