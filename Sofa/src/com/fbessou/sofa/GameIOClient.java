/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.fbessou.sofa.message.GameAcceptMessage;
import com.fbessou.sofa.message.GameJoinMessage;
import com.fbessou.sofa.message.GameLeaveMessage;
import com.fbessou.sofa.message.GameOutputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadInputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadJoinMessage;
import com.fbessou.sofa.message.ProxyGamePadLeaveMessage;
import com.fbessou.sofa.message.ProxyGamePadRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;

/**
 * Game
 * @author Frank Bessou
 *
 */
public class GameIOClient extends Fragment implements StringReceiver.Listener, ProxyConnector.OnConnectedListener {
	
	/**
	 * Informations of the running game
	 */
	GameInformation gameInfo;

	/**
	 * Socket connecting to a proxy
	 */
	private Socket mSocket = null;
	private StringReceiver mReceiver = null;
	private StringSender mSender = null;
	
	private GamePadMessageListener gamePadListener = null;
	
	// TODO list of accepted gamepad (whitelist)? Thus, we could filter game
	// pad messages if the game want to refuse players even if the max game pad
	// count (GameInformation) is not reached.
	// Otherwise, we could let the proxy build and use a whitelist according to
	// the GameAcceptMessage that it receives -> DONE!
	
	/**
	 * 
	 */
	private GameIOClient(GameInformation gameInfo) {
		this.gameInfo = gameInfo;
		setRetainInstance(true);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// connect to proxy
		ProxyConnector connector = new ProxyConnector(getActivity().getApplicationContext(), GameIOProxy.DefaultGamePadsPort, this);
		connector.connect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(mSocket != null) {
			try {
				mSender.clearBufferedMessage();
				mSender.send(new GameLeaveMessage().toString());
				// The sender must send its message before closing socket
				Thread.sleep(500);// FIXME find a better way to be sure that the leave message has been sent
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Updates and (TODO)send game info
	 * @param info
	 */
	public void updateGameInfo(GameInformation info) {
		gameInfo = info;
		// TODO resend gameInfo
	}
	
	public GameInformation getGameInfo() {
		return gameInfo;
	}
	
	public void setGamePadMessageListener(GamePadMessageListener listener) {
		gamePadListener = listener;
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
		try {
			ProxyMessage message = ProxyMessage.gamePadFromJSON(new JSONObject(string));

			switch(message.getType()) {
			case INPUTEVENT:
				if(gamePadListener != null) {
					int gamePadId = ((ProxyGamePadInputEventMessage)message).getGamePadId();
					gamePadListener.onGamePadInputEventReceived(((ProxyGamePadInputEventMessage)message).getInputEvent(), gamePadId);
				}
				break;
				
			case JOIN: {
				int gamePadId = ((ProxyGamePadJoinMessage)message).getGamePadId();
				if(gamePadListener != null) {
					if(!gamePadListener.onGamePadJoined(gamePadId))
						// if we do not send the GameAcceptMessage, the proxy will messages coming from this game-pad
						break;// TODO send refused message
				}
				mSender.send(new GameAcceptMessage(gameInfo.name, gamePadId).toString());
				break;
			}
			case LEAVE:
				if(gamePadListener != null) {
					int gamePadId = ((ProxyGamePadLeaveMessage)message).getGamePadId();
					gamePadListener.onGamePadLeft(gamePadId);
				}
				break;
				
			case RENAME:
				if(gamePadListener != null) {
					int gamePadId = ((ProxyGamePadRenameMessage)message).getGamePadId();
					gamePadListener.onGamePadRenamed(((ProxyGamePadRenameMessage)message).getNewNickname(), gamePadId);
				}
				break;
				
			case OUTPUTEVENT: // Should not occur
			case ACCEPT: // Should not occur
				break;
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends an output event to the gamepad(s).
	 * @param event Output event to send
	 * @param gamepad Recipent of this event. -1 for broadcast
	 */
	public void sendOutputEvent(OutputEvent event, int gamepad) {
		mSender.send(new GameOutputEventMessage(event, gamepad).toString());
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.ProxyConnector.OnConnectedListener#onConnected(java.net.Socket)
	 */
	@Override
	public void onConnected(Socket socket) {
		if(socket == null) {
			// TODO retry 
			Log.e("GameIOClient", "Connection failed");
		}
		else {
			// Start Sender and Receiver
			mSocket = socket;
			mSender = new StringSender(socket);
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			mSender.start();
			mReceiver.start();
			
			// Send Join message
			mSender.send(new GameJoinMessage().toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		// TODO FIXME What could we do? try to reconnect ? But first, check if this service is shuting down before ;)
		Log.i("GameBinder","Shit, we are disconnected.");
	}
	
	/** 
	 * Disconnected from wifi p2p 
	 * 
	 * @see com.fbessou.sofa.ProxyConnector.OnConnectedListener#onDisconnected()
	 */
	@Override
	public void onDisconnected() {
		// TODO what could we do here? Maybe just display a message.
	}

	/**
	 * Indicates if this gameIOClient is connected to the proxy.
	 * @return connected or not
	 */
	public boolean isConnected() {
		return mSender != null && mSocket.isConnected();
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
	
	/**
	 * 
	 * @author Proïd
	 *
	 */
	public interface GamePadMessageListener {
		void onGamePadInputEventReceived(InputEvent event, int gamepad);
		void onGamePadRenamed(String newNickname, int gamepad);
		void onGamePadLeft(int gamepad);
		/** @return true if the gamepad is accepted **/
		boolean onGamePadJoined(int gamepad);
		//void onGamePadUnexpectedlyDisconnected(int gamepad);
	}
}
