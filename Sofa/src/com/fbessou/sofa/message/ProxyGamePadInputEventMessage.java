package com.fbessou.sofa.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbessou.sofa.InputEvent;

/**
 * @author Proïd
 *
 */
public class ProxyGamePadInputEventMessage extends ProxyMessage {
	
	private final InputEvent mInputEvent;
	/** Id of the game that sends the message **/
	private final int mGamePadId;

	/**
	 * 
	 * @param message Message to transmit
	 * @param gamePadId Id of the game pad.
	 */
	public ProxyGamePadInputEventMessage(GamePadInputEventMessage message, int gamePadId) {
		super(message);
		mInputEvent = message.mInputEvent;
		mGamePadId = gamePadId;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public ProxyGamePadInputEventMessage(JSONObject json) throws Exception {
		super(json);
		mInputEvent = new InputEvent(json.getJSONObject("event"));
		mGamePadId = json.getInt("gamepad");
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("event", mInputEvent.toJSON());
		json.put("gamepad", mGamePadId);
		
		return json;
	}

	
	public int getGamePadId() {
		return mGamePadId;
	}
	public InputEvent getInputEvent() {
		return mInputEvent;
	}
}
