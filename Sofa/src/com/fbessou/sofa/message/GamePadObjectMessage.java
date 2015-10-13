package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

public class GamePadObjectMessage extends Message {
	
	protected String mObjectToString;
	
	public GamePadObjectMessage(Object object) {
		super(Type.OBJECT);
		mObjectToString = object.toString();
	}
	
	GamePadObjectMessage(JSONObject json) throws Exception {
		super(json);
		mObjectToString = json.getString("object");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		json.put("object", mObjectToString);
		
		return json;
	}
}
