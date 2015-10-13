/**
 * 
 */
package com.fbessou.sofa;

import java.net.Socket;

import org.json.JSONObject;

import android.app.Activity;
import android.app.FragmentManager;

import com.fbessou.sofa.message.GamePadJoinMessage;
import com.fbessou.sofa.message.GamePadLeaveMessage;
import com.fbessou.sofa.message.GamePadPingMessage;
import com.fbessou.sofa.message.GamePadPongMessage;
import com.fbessou.sofa.message.GamePadRenameMessage;
import com.fbessou.sofa.message.Message;
import com.fbessou.sofa.message.Message.Type;
import com.fbessou.sofa.message.ProxyGameCustomMessage;
import com.fbessou.sofa.message.ProxyGameOutputEventMessage;
import com.fbessou.sofa.message.ProxyGameRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;

/**
 * @author Frank Bessou
 *
 * GamePadIOClient used by game pads.
 */
public class GamePadIOClient extends IOClient {
	
	/**
	 * 
	 */
	private GamePadInformation mGamePadInfo;

	private GameMessageListener mGameListener;
	private boolean mIsAcceptedByGame = false;

	private ConnectionStateChangedListener mConnectionListener;

	/**
	 * 
	 */
	public GamePadIOClient(GamePadInformation info) {
		super(GameIOProxy.DefaultGamePadsPort);
		mGamePadInfo = info;
	}

	
	/** Called when this client is connected to the proxy and messages can be sent and received. **/
	@Override
	protected void onCommunicationEnabled() {
		super.onCommunicationEnabled();
		
		// Send Join message
		sendMessage(new GamePadJoinMessage(mGamePadInfo.getNickname(), mGamePadInfo.getUUID()));
		
		if(mConnectionListener != null)
			mConnectionListener.onConnectedToProxy();
	}
	@Override
	protected void onCommunicationDisabled() {
		super.onCommunicationDisabled();
		
		if(mConnectionListener != null)
			mConnectionListener.onDisconnectedFromProxy();
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
		super.onStringReceived(string, socket);
		Log.v("GamePadIOClient", "onStringReceived: "+string+" from socket:"+socket);
		try{
			Message message = ProxyMessage.gameFromJSON(new JSONObject(string));
			switch(message.getType()) {
			case ACCEPT:
				if(!mIsAcceptedByGame) {
					// We are now officially connected to the game! Congratulation!
					mIsAcceptedByGame = true;
					if(mConnectionListener != null)
						mConnectionListener.onConnectedToGame();
					Log.i("GamePadIOClient", "Accepted by the game");
				}
				break;
			case JOIN:
				// Game is ready, join the game
				sendMessage(new GamePadJoinMessage(mGamePadInfo.getNickname(), mGamePadInfo.getUUID()));
				break;
			case LEAVE:
				// Game leaves
				if(mGameListener != null) {
					mGameListener.onGameLeft();
				}
				break;
			case OUTPUTEVENT:
				if(mGameListener != null) {
					OutputEvent event = ((ProxyGameOutputEventMessage)message).getOutputEvent();
					mGameListener.onOutputReceived(event);
				}
				break;
			case RENAME:
				if(mGameListener != null) {
					String name = ((ProxyGameRenameMessage)message).getNewName();
					mGameListener.onGameRenamed(name);
				}
				break;
			case PING:
				// Send the response : "pong"
				sendMessage(new GamePadPongMessage());
				break;
			case PONG:
				// We have received the answer to our "ping" message
				// TODO Create method pingProxy() and compute delay between ping and pong
				break;
			case REJECT:
				if(mIsAcceptedByGame) {
					mIsAcceptedByGame = false;
					if(mConnectionListener != null)
						mConnectionListener.onDisconnectedFromGame();
				}
				break;
			case CUSTOM:
				if(mGameListener != null) {
					String custom = ((ProxyGameCustomMessage)message).getCustomMessage();
					mGameListener.onCustomMessageReceived(custom);
				}
				break;
			case LOST: // Should not occur
			case INPUTEVENT: // Should not occur
				break;
			}
		}catch(Exception e){
			Log.e("GamePadIOClient", "onStringReceived error ", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringSender.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		super.onClosed(socket);
		
		mIsAcceptedByGame = false;
	}

	/** Called before closing the communication. **/
	@Override
	protected void beforeCommunicationDisabled() {
		super.beforeCommunicationDisabled();
		
		// Send "leave" message
		sendMessage(new GamePadLeaveMessage());
	}
	
	/**
	 * Sends the given message if we are connected.
	 * @param m message to send
	 */
	@Override
	public void sendMessage(Message m) {
		Log.v("GamePadIOClient", "sendMessage: "+m.toString());
		if(isConnected()) {
			if(m.getType() != Type.JOIN && m.getType() != Type.PING  && m.getType() != Type.PONG && !mIsAcceptedByGame)
				Log.w("GamePadIOClient", "Try to send "+m.getType()+" but game pad not accepted by game. Cancel.");
			else
				mSender.send(m.toString());
		}
		else
			Log.w("GamePadIOClient", "sendMessage error : cannot send message, disconnected from proxy");
	}
	
	/** Called when the alert duration of silence has been reached. This method should
	 * send a message to the proxy to keep the connection. **/
	@Override
	public void onAlertDelayPassed() {
		super.onAlertDelayPassed();
		
		// Send "ping" message
		sendMessage(new GamePadPingMessage());
	}
	
	public GamePadInformation getGamePadInfo() {
		return mGamePadInfo;
	}
	/**
	 * Updates and (TODO)send game info
	 * @param info
	 */
	public void updateGamePadInfo(GamePadInformation info) {
		mGamePadInfo = info;
		Log.i("GamePadIOClient", "updateGamePadInfo");

		sendMessage(new GamePadRenameMessage(info.getNickname()));
	}
	
	/** Indicates if this game pad client has been accepted by the game and joined it **/
	public boolean isConnectedToGame() {
		return mIsAcceptedByGame;
	}

	/**
	 * Returns a gameBinder retrieved from fragment manager, otherwise a newly created gameBinder.
	 * @param context
	 * @param info Use to define game pad info if the gameBinder need to be
	 * 				created. Can be null, default values will be used instead.
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static GamePadIOClient getGamePadIOClient(Activity activity, GamePadInformation info) {
		FragmentManager fm = activity.getFragmentManager();
		GamePadIOClient gameBinder = (GamePadIOClient) fm.findFragmentByTag("GamePadIOClient");
		if(gameBinder == null) {
			if(info == null)
				info = GamePadInformation.getDefault();
			
			gameBinder = new GamePadIOClient(info);
			fm.beginTransaction().add(gameBinder, "GamePadIOClient").commit();
		}
		return gameBinder;
	}
	/** Sets a listener to handle the messages coming form the game */
	public void setGameMessageListener(GameMessageListener listener) {
		mGameListener = listener;
	}
	
	public interface GameMessageListener {
		void onCustomMessageReceived(String customMessage);
		void onOutputReceived(OutputEvent event);
		void onGameRenamed(String newName);
		void onGameLeft();
		//void onGameUnexpectedlyDisconnected();
	}

	/** Sets a listener to handle the connection changes (connection_to/disconnection_from  proxy/game)**/
	public void setOnConnectionStateChangedListener(ConnectionStateChangedListener listener) {
		this.mConnectionListener = listener;
	}
	
	public interface ConnectionStateChangedListener {
		public void onConnectedToProxy();
		public void onConnectedToGame();
		public void onDisconnectedFromGame();
		public void onDisconnectedFromProxy();
	}
}
