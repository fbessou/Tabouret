/**
 * 
 */
package com.fbessou.sofa;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * @author Frank Bessou
 *
 */
public class GameInformation {
	private static String defaultName = "Nameless game";
	private static int defaultMaxPlayers = 16;
	private String mName;
	private int mMaxPlayers;
	
	private Context mContext;
	

	public GameInformation(Context context) {
		mContext = context;
		mName = getNameFromPreferences();
		mMaxPlayers = getMaxPlayerFromPreferences();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		if(this.mName != name) {
			this.mName = name;
			saveNameInPreferences();
		}
	}

	public int getMaxPlayers() {
		return mMaxPlayers;
	}
	
	/**
	 * Set the maximum number of player allowed by the game
	 */
	public void setMaxPlayers(int maxPlayers) {
		if(this.mMaxPlayers != maxPlayers) {
			this.mMaxPlayers = maxPlayers;
			saveMaxPlayerInPreferences();
		}
	}


	/**
	 * Retrieve the name from the shared preferences or create a default one if not existing.
	 * If created, the name is automatically saved in shared preferences
	 **/
	private String getNameFromPreferences() {
		if(mContext == null)
			return defaultName;
		SharedPreferences prefs = mContext.getSharedPreferences("game-info", Context.MODE_PRIVATE);
		String s = prefs.getString("name", null);
		if(s != null)
			return s;
		else {
			// user name not found in prefs, create a default name
			prefs.edit().putString("name", defaultName).commit();
			return defaultName;
		}
	}
	
	/**
	 * Save the name in the shared preferences
	 */
	private void saveNameInPreferences() {
		if(mContext == null)
			return;
		SharedPreferences prefs = mContext.getSharedPreferences("game-info", Context.MODE_PRIVATE);
		prefs.edit().putString("name", mName).commit();
	}
	
	/**
	 * Save the nickname in the shared preferences
	 */
	private void saveMaxPlayerInPreferences() {
		if(mContext == null)
			return;
		SharedPreferences prefs = mContext.getSharedPreferences("game-info", Context.MODE_PRIVATE);
		prefs.edit().putInt("max-player", mMaxPlayers).commit();
	}
	
	/**
	 * Retrieve the maximum number of players from the shared preferences or return a new default value if not existing.
	 * The new default value is automatically saved.
	 **/
	private int getMaxPlayerFromPreferences() {
		if(mContext == null)
			return defaultMaxPlayers;
		SharedPreferences prefs = mContext.getSharedPreferences("game-info", Context.MODE_PRIVATE);
		int n = prefs.getInt("max-player", 0);
		if(n > 0)
			return n;
		else {
			// user name not found in prefs, create a default name
			prefs.edit().putInt("max-player", defaultMaxPlayers).commit();
			return defaultMaxPlayers;
		}
	}
}
