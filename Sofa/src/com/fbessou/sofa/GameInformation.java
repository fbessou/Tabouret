/**
 * 
 */
package com.fbessou.sofa;


/**
 * @author Frank Bessou
 *
 */
public class GameInformation {
	String name = "Nameless Game";
	int maxPlayers = 16;
	//Map<String, String> inputs; 
	
	/** Returns a default game information. **/
	public static GameInformation getDefault() {
		return new GameInformation();
	}

	public GameInformation(String gameName) {
		this.name = gameName;
	}
	private GameInformation() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
}
