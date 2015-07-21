/**
 * 
 */
package com.fbessou.sofa;


/**
 * @author Frank Bessou
 *
 */
public class GameInformation {
	private String mName = "Nameless Game";
	private int mMaxPlayers = 16;
	//Map<String, String> inputs; 
	
	/** Returns a default game information. **/
	public static GameInformation getDefault() {
		return new GameInformation();
	}

	public GameInformation(String gameName) {
		this.mName = gameName;
	}
	private GameInformation() {
		
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getMaxPlayers() {
		return mMaxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.mMaxPlayers = maxPlayers;
	}
}
