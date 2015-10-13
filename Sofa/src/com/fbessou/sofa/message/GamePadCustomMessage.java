package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

public class GamePadCustomMessage extends Message {
	
	protected String mCustomMessage;
	
	public GamePadCustomMessage(String customMessage) {
		super(Type.CUSTOM);
		mCustomMessage = customMessage;
	}
	
	GamePadCustomMessage(JSONObject json) throws Exception {
		super(json);
		mCustomMessage = json.getString("custom");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put("custom", mCustomMessage);
		
		return json;
	}
}
