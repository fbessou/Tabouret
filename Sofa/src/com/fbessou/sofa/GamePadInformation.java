/**
 * 
 */
package com.fbessou.sofa;

import java.util.UUID;


/**
 * @author Frank Bessou
 *
 */
public class GamePadInformation {
	private String mNickname = "Nameless pad";
	private UUID mUUID;
	// Gamepad xml name/version
	
	/**
	 *  Returns a default gameoad information.
	 *  @deprecated this method create a new UUID. **/
	@Deprecated
	public static GamePadInformation getDefault() {
		return new GamePadInformation();
	}

	public GamePadInformation(String nickname, UUID uuid) {
		this.mNickname = nickname;
		mUUID = uuid;
	}
	/**
	 * @deprecated This constructor create a new random UUID. Should be defined before and saved for re-use.
	 */
	@Deprecated
	private GamePadInformation() {
		mUUID = UUID.randomUUID();// 
	}

	public String getNickname() {
		return mNickname;
	}

	public void setNickname(String name) {
		mNickname = name;
	}

	public UUID getUUID() {
		return mUUID;
	}
}
