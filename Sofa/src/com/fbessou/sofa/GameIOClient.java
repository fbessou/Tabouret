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

import com.fbessou.sofa.message.GameAcceptMessage;
import com.fbessou.sofa.message.GameJoinMessage;
import com.fbessou.sofa.message.GameLeaveMessage;
import com.fbessou.sofa.message.GameOutputEventMessage;
import com.fbessou.sofa.message.GameRenameMessage;
import com.fbessou.sofa.message.Message;
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
	
	private GamePadMessageListener mGamePadListener = null;
	
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
		Log.i("GameIOClient", "Creating fragment, connecting");
		// connect to proxy
		ProxyConnector connector = new ProxyConnector(getActivity().getApplicationContext(), GameIOProxy.DefaultGamePort, this);
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
		Log.i("GameIOClient", "Destroying fragment");
		
		if(mSocket != null) {
			try {
				Log.i("GameIOClient", "clearBufferedMessage");
				mSender.clearBufferedMessage();
				sendMessage(new GameLeaveMessage());
				// The sender must send its message before closing socket
				Thread.sleep(500);// FIXME find a better way to be sure that the leave message has been sent
				Log.i("GameIOClient", "close socket:"+mSocket+" after sleeping 500ms");
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

		Log.i("GameIOClient", "updateGameInfo");
		mSender.send(new GameRenameMessage(info.getName()).toString());
	}
	
	public GameInformation getGameInfo() {
		return gameInfo;
	}
	
	/**
	 * Sends the given message if we are connected.
	 * @param m message to send
	 */
	private void sendMessage(Message m) {
		Log.v("GameIOClient", "send message:"+m.toString());
		if(isConnected())
			mSender.send(m.toString());
		else {
			Log.w("GameIOClient", "Warning : cannot send message, disconnected from proxy");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
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
					if(!mGamePadListener.onGamePadJoined(gamePadId))
						// if we do not send the GameAcceptMessage, the proxy will messages coming from this game-pad
						break;// TODO send refused message
				}
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
			case OUTPUTEVENT: // Should not occur
			case ACCEPT: // Should not occur
				break;
			
			}
		} catch (Exception e) {
			Log.e("GameIOClient", "onStringReceived error", e);
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
			Log.i("GameIOClient", "Connection established, start sender and receiver");
			// Start Sender and Receiver
			mSocket = socket;
			mSender = new StringSender(socket);
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			mSender.start();
			mReceiver.start();
			
			// Send Join message
			sendMessage(new GameJoinMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		// TODO FIXME What could we do? try to reconnect ? But first, check if this service is shuting down ;)
		Log.i("GameIOClient","disconnected from socket:"+socket);
	}

	/**
	 * Indicates if this gameIOClient is connected to the proxy.
	 * @return connected or not
	 */
	public boolean isConnected() {
		return mSocket != null && mSocket.isConnected();
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
		void onGamePadInputEventReceived(InputEvent event, int gamepad);
		void onGamePadRenamed(String newNickname, int gamepad);
		void onGamePadLeft(int gamepad);
		/** @return true if the game pad is accepted **/
		boolean onGamePadJoined(int gamepad);
		void onGamePadUnexpectedlyDisconnected(int gamepad);
	}
}