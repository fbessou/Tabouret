package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

public class GameObjectMessage extends Message {
	
	protected String mObjectToString;
	/** Id of the game-pad recipient **/
	private final int mGamePadId;
	
	public GameObjectMessage(Object object, int gamePadId) {
		super(Type.OBJECT);
		mObjectToString = object.toString();
		mGamePadId = gamePadId;
	}
	
	GameObjectMessage(JSONObject json) throws Exception {
		super(json);
		mObjectToString = json.getString("object");
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put("object", mObjectToString);
		json.put("gamepad", mGamePadId);
		
		return json;
	}
	
	/** Returns the ID of the game-pad recipient **/
	public int getGamePadId() {
		return mGamePadId;
	}
}
