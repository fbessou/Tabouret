package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbessou.sofa.OutputEvent;

/**
 * @author Proïd
 *
 */
public class ProxyGameOutputEventMessage extends ProxyMessage {

	protected final OutputEvent mOutputEvent;
	
	/**
	 * @params message Message to transmit
	 */
	public ProxyGameOutputEventMessage(GameOutputEventMessage message) {
		super(message);
		mOutputEvent = message.mOutputEvent;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGameOutputEventMessage(JSONObject json) throws Exception {
		super(json);
		mOutputEvent = new OutputEvent(json.getJSONObject("event"));
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("event", mOutputEvent.toJSON());
		
		return json;
	}

	public OutputEvent getOutputEvent() {
		return mOutputEvent;
	}
}
