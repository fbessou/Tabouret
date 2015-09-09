package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GamePadPongMessage extends Message {
	
	/**
	 * 
	 */
	public GamePadPongMessage() {
		super(Type.PONG);
	}

	/**
	 * Instantiate a message from a JSON object
	 * @throws Exception If message cannot be instantiated from the json object for any reasons.
	 * */
	public GamePadPongMessage(JSONObject json) throws Exception {
		super(json);
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		return json;
	}
}
