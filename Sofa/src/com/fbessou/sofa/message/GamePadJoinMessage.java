package com.fbessou.sofa.message;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Proïd
 *
 */
public class GamePadJoinMessage extends Message {
	
	/** Nickname given by the game-pad. Empty string by default. Should not be null **/
	protected final String mNickname;
	protected final UUID mUUID;
	
	/**
	 * 
	 * @param nickname Name of the game pad. Equivalent to en empty string if null
	 * @param uuid Unique identifer of the game pad.
	 */
	public GamePadJoinMessage(String nickname, UUID uuid) {
		super(Type.JOIN);
		mNickname = nickname == null ? "" : nickname;
		mUUID = uuid;
	}

	/**
	 * Instatiate a message from a JSON object
	 * @throws Exception If message cannot be instatiate from the json object for any reasons.
	 * */
	public GamePadJoinMessage(JSONObject json) throws Exception {
		super(json);
		mNickname = json.getString("name");
		mUUID = UUID.fromString(json.getString("uuid"));
	}
	
	@Override
	protected JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("name", mNickname);
		json.put("uuid", mUUID.toString());
		
		return json;
	}

	public UUID getUUID() {
		return mUUID;
	}
}
