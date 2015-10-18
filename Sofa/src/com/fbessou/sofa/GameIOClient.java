/**
 * 
 */
package com.fbessou.sofa;

import java.net.Socket;

import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;

import com.fbessou.sofa.message.GameAcceptMessage;
import com.fbessou.sofa.message.GameJoinMessage;
import com.fbessou.sofa.message.GameLeaveMessage;
import com.fbessou.sofa.message.GameOutputEventMessage;
import com.fbessou.sofa.message.GamePingMessage;
import com.fbessou.sofa.message.GamePongMessage;
import com.fbessou.sofa.message.GameRejectMessage;
import com.fbessou.sofa.message.GameRenameMessage;
import com.fbessou.sofa.message.Message;
import com.fbessou.sofa.message.ProxyGamePadCustomMessage;
import com.fbessou.sofa.message.ProxyGamePadInputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadJoinMessage;
import com.fbessou.sofa.message.ProxyGamePadLeaveMessage;
import com.fbessou.sofa.message.ProxyGamePadLostMessage;
import com.fbessou.sofa.message.ProxyGamePadRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;

/**
 * Game
 * @author Frank Bessou
 */
public class GameIOClient extends IOClient {
	
	/**
	 * Informations of the running game
	 */
	GameInformation gameInfo;
	
	private GamePadMessageListener mGamePadListener = null;

	private ConnectionStateChangedListener mConnectionListener;

	
	// TODO list of accepted game pad (white-list)? Thus, we could filter game
	// pad messages if the game want to refuse players even if the max game pad
	// count (GameInformation) is not reached.
	// Otherwise, we could let the proxy build and use a white-list according to
	// the GameAcceptMessage that it receives -> DONE!
	
	/**
	 * 
	 */
	private GameIOClient(GameInformation gameInfo) {
		super(GameIOProxy.DefaultGamePort);
		this.gameInfo = gameInfo;
	}
	
	/**
	 * Updates and (TODO)send game info
	 * @param info
	 */
	public void updateGameInfo(GameInformation info) {
		gameInfo = info;

		Log.i("GameIOClient", "updateGameInfo");
		mSender.send(new GameRenameMessage(info.getName()).toString());
	}
	
	public GameInformation getGameInfo() {
		return gameInfo;
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
		super.onStringReceived(string, socket);
		
		Log.v("GameIOClient", "onStringReceived: "+string+" from socket:"+socket);
		try {
			ProxyMessage message = ProxyMessage.gamePadFromJSON(new JSONObject(string));

			switch(message.getType()) {
			case INPUTEVENT:
				if(mGamePadListener != null) {
					int gamePadId = ((ProxyGamePadInputEventMessage)message).getGamePadId();
					mGamePadListener.onGamePadInputEventReceived(((ProxyGamePadInputEventMessage)message).getInputEvent(), gamePadId);
				}
				break;
				
			case JOIN: {
				int gamePadId = ((ProxyGamePadJoinMessage)message).getGamePadId();
				if(mGamePadListener != null) {
					if(!mGamePadListener.onGamePadJoined(((ProxyGamePadJoinMessage)message).getNickname(), gamePadId)) {
						// if we do not send the GameAcceptMessage, the proxy will block messages coming from this game-pad
						Log.i("GameIOClient", "Game pad "+gamePadId+" refused");
						sendMessage(new GameRejectMessage(gamePadId));
						break;
					}
				}
				Log.i("GameIOClient", "Game pad "+gamePadId+" accepted");
				sendMessage(new GameAcceptMessage(gameInfo.getName(), gamePadId));
				break;
			}
			case LEAVE:
				if(mGamePadListener != null) {
					int gamePadId = ((ProxyGamePadLeaveMessage)message).getGamePadId();
					mGamePadListener.onGamePadLeft(gamePadId);
				}
				break;
				
			case RENAME:
				if(mGamePadListener != null) {
					int gamePadId = ((ProxyGamePadRenameMessage)message).getGamePadId();
					mGamePadListener.onGamePadRenamed(((ProxyGamePadRenameMessage)message).getNewNickname(), gamePadId);
				}
				break;
			case LOST:
				if(mGamePadListener != null) {
					int gamePadId = ((ProxyGamePadLostMessage)message).getGamePadId();
					mGamePadListener.onGamePadUnexpectedlyDisconnected(gamePadId);
				}
				break;
			case CUSTOM:
				if(mGamePadListener != null) {
					int gamePadId = ((ProxyGamePadCustomMessage)message).getGamePadId();
					String custom = ((ProxyGamePadCustomMessage)message).getCustomMessage();
					mGamePadListener.onGamePadCustomMessageReceived(custom, gamePadId);
				}
				break;
			case PONG:
				// Response of our "ping" message
				break;
			case PING:
				// Send the response: "pong"
				sendMessage(new GamePongMessage());
				break;
			case REJECT: // Should not occur
			case OUTPUTEVENT: // Should not occur
			case ACCEPT: // Should not occur
				break;
			
			}
		} catch (Exception e) {
			Log.e("GameIOClient", "onStringReceived error", e);
		}
	}

	/** Called when the alert duration of silence has been reached. This method should
	 * send a message to the proxy to keep the connection. **/
	@Override
	public void onAlertDelayPassed() {
		super.onAlertDelayPassed();

		// Send "ping" message
		sendMessage(new GamePingMessage());
	};
	
	/** Called when this client is connected to the proxy and messages can be sent and received. **/
	@Override
	protected void onCommunicationEnabled() {
		super.onCommunicationEnabled();
		// Send Join message
		sendMessage(new GameJoinMessage());

		if(mConnectionListener != null)
			mConnectionListener.onConnected();
	}
	@Override
	protected void onCommunicationDisabled() {
		super.onCommunicationDisabled();
		
		if(mConnectionListener != null)
			mConnectionListener.onDisconnected();
	}
	
	/** Called before closing the communication. **/
	protected void beforeCommunicationDisabled() {
		super.beforeCommunicationDisabled();

		// Send "leave" message
		sendMessage(new GameLeaveMessage());
	}

	/**
	 * Sends the given message if we are connected.
	 * @param m message to send
	 */
	public void sendMessage(Message m) {
		Log.v("GameIOClient", "send message:"+m.toString());
		if(isConnected())
			mSender.send(m.toString());
		else {
			Log.w("GameIOClient", "Warning : cannot send message, disconnected from proxy");
		}
	}

	/**
	 * Sends an output event to the gamepad(s).
	 * @param event Output event to send
	 * @param gamepad Recipent of this event. -1 for broadcast
	 */
	public void sendOutputEvent(OutputEvent event, int gamepad) {
		Log.i("GameIOClient", "sendOutputEvent to game pad id:"+gamepad+" event:"+event.toString());
		sendMessage(new GameOutputEventMessage(event, gamepad));
	}
	
	/**
	 * Returns a gameIOClient retrieved from fragment manager, otherwise a newly created gameIOClient.
	 * @param context
	 * @param info Use to define game info if the gameIOClient need to be
	 * 				created. Can be null, default values will be used instead.
	 * @return
	 */
	public static GameIOClient getGameIOClient(Activity activity, GameInformation info) {
		FragmentManager fm = activity.getFragmentManager();
		GameIOClient gameIO = (GameIOClient) fm.findFragmentByTag("gameIOClient");
		if(gameIO == null) {
			if(info == null)
				info = GameInformation.getDefault();
			
			gameIO = new GameIOClient(info);
			fm.beginTransaction().add(gameIO, "gameIOClient").commit();
		}
		return gameIO;
	}

	public void setGamePadMessageListener(GamePadMessageListener listener) {
		mGamePadListener = listener;
	}
	
	/**
	 * 
	 * @author Proïd
	 *
	 */
	public interface GamePadMessageListener {
		void onGamePadCustomMessageReceived(String customMessage, int gamepad);
		void onGamePadInputEventReceived(InputEvent event, int gamepad);
		void onGamePadRenamed(String newNickname, int gamepad);
		void onGamePadLeft(int gamepad);
		/** @return true if the game pad must be accepted, false to refuse it **/
		boolean onGamePadJoined(String nickname, int gamepad);
		void onGamePadUnexpectedlyDisconnected(int gamepad);
	}
	/** Sets a listener **/
	public void setOnConnectionStateChangedListener(ConnectionStateChangedListener listener) {
		this.mConnectionListener = listener;
	}
	
	public interface ConnectionStateChangedListener {
		public void onConnected();
		public void onDisconnected();
	}
}
