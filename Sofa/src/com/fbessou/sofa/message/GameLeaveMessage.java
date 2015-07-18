package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GameLeaveMessage extends Message {
	
	/**
	 * 
	 */
	public GameLeaveMessage() {
		super(Type.LEAVE);
	}
	
	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GameLeaveMessage(JSONObject json) throws Exception {
		super(json);
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		
		return json;
	}
}
