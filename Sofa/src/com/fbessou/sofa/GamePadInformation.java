/**
 * 
 */
package com.fbessou.sofa;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * @author Frank Bessou
 *
 */
public class GamePadInformation {
	private static String defaultNickname = "Nameless pad";
	
	private String mNickname;
	private UUID mUUID;
	
	private Context mContext;
	

	/**
	 * This constructor must be used in the game pad activity.<br>
	 * Load the game pad information from the shared preferences.
	 * If UUID does not exist, a new random one is created.
	 * If the nickname does not exist, the default one is chosen
	 **/
	public GamePadInformation(Context context) {
		mContext = context;
		mUUID = getUUIDFromPreferences();
		mNickname = getNameFromPreferences();
	}
	protected GamePadInformation(String nickname) {
		mNickname = nickname;
	}

	/**
	 * Return the nickname
	 */
	public String getNickname() {
		return mNickname;
	}

	/**
	 * Set a new nickname. It will be automatically saved in the shared preferences
	 */
	public void setNickname(String name) {
		if(mNickname != name) {
			mNickname = name;
			saveNameInPreferences();
		}
	}

	/**
	 * Return the UUID
	 **/
	protected UUID getUUID() {
		return mUUID;
	}
	
	/**
	 * Generate a new random UUID and save it in the shared preferences
	 */
	public void resetUUID() {
		mUUID = UUID.randomUUID();
		if(mContext == null)
			return;
		SharedPreferences prefs = mContext.getSharedPreferences("game-pad-info", Context.MODE_PRIVATE);
		prefs.edit().putString("UUID", mUUID.toString()).commit();
	}
	
	/**
	 * Retrieve the UUID from the shared preferences or create a new one if not existing.
	 * If created, UUID is automatically saved in shared preferences
	 **/
	private UUID getUUIDFromPreferences() {
		if(mContext == null)
			return null;
		SharedPreferences prefs = mContext.getSharedPreferences("game-pad-info", Context.MODE_PRIVATE);
		String s = prefs.getString("UUID", null);
		try {
			return UUID.fromString(s);
		} catch(Exception e) {
			// UUID not found in prefs
			UUID uuid = UUID.randomUUID();
			prefs.edit().putString("UUID", uuid.toString()).commit();
			return uuid;
		}
	}
	
	/**
	 * Retrieve the name from the shared preferences or create a default one if not existing.
	 * If created, the name is automatically saved in shared preferences
	 **/
	private String getNameFromPreferences() {
		if(mContext == null)
			return defaultNickname;
		SharedPreferences prefs = mContext.getSharedPreferences("game-pad-info", Context.MODE_PRIVATE);
		String s = prefs.getString("name", null);
		if(s != null)
			return s;
		else {
			// user name not found in prefs, create a default name
			prefs.edit().putString("name", defaultNickname).commit();
			return defaultNickname;
		}
	}
	
	/**
	 * Save the nickname in the shared preferences
	 */
	private void saveNameInPreferences() {
		if(mContext == null)
			return;
		SharedPreferences prefs = mContext.getSharedPreferences("game-pad-info", Context.MODE_PRIVATE);
		prefs.edit().putString("name", mNickname).commit();
	}
	
}
