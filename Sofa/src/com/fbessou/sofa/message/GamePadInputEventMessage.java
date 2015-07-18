package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbessou.sofa.InputEvent;

/**
 * @author Proïd
 *
 */
public class GamePadInputEventMessage extends Message {
	
	protected final InputEvent mInputEvent;
	
	/**
	 * @params inputEvent Input event.
	 */
	public GamePadInputEventMessage(InputEvent inputEvent) {
		super(Type.INPUTEVENT);
		mInputEvent = inputEvent;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GamePadInputEventMessage(JSONObject json) throws Exception {
		super(json);
		mInputEvent = new InputEvent(json.getJSONObject("event"));
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("event", mInputEvent.toJSON());
		
		return json;
	}
}
