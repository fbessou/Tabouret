/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.json.JSONObject;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.fbessou.sofa.message.GamePadInputEventMessage;
import com.fbessou.sofa.message.GamePadJoinMessage;
import com.fbessou.sofa.message.GamePadRenameMessage;
import com.fbessou.sofa.message.Message;
import com.fbessou.sofa.message.ProxyGameOutputEventMessage;
import com.fbessou.sofa.message.ProxyGameRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;

/**
 * @author Frank Bessou
 *
 * GameBinder used by game pads.
 * TODO check mSender != null or something like that before sending a message
 */
public class GameBinder extends Fragment implements Sensor.InputEventListener, StringReceiver.Listener, ProxyConnector.OnConnectedListener {
	
	/**
	 * 
	 */
	GamePadInformation mGamePadInfo;
	
	/**
	 * Stored unique identifier to help recovering
	 */
	UUID mUUID;

	/**
	 * FIXME Move to gamePadInfo?
	 */
	ArrayList<Sensor> mAvailableSensors = new ArrayList<Sensor>();

	GameMessageListener mGameListener;
	
	// Communication with proxy

	/**
	 * Socket connecting to a proxy
	 */
	private Socket mSocket = null;
	private StringReceiver mReceiver = null;
	private StringSender mSender = null;

	/**
	 * 
	 */
	public GameBinder(GamePadInformation info) {
		mGamePadInfo = info;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		ProxyConnector connector = new ProxyConnector(this.getActivity().getApplicationContext(), GameIOProxy.DefaultGamePadsPort, this);
		connector.connect();
		/**
		 * // Before connecting to this service, we have to wait for the service
		 * // until we are sure it is running LocalBroadcastManager lbm =
		 * LocalBroadcastManager
		 * .getInstance(getActivity().getApplicationContext());
		 * lbm.registerReceiver(new BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) {
		 * 
		 *           } }, new IntentFilter("IO_PROXY_RUNNING"));
		 **/
	}

	// ArrayList<Output> mOutputMapping;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fbessou.sofa.Sensor.Listener#onInputEvent(com.fbessou.sofa.InputEvent
	 * )
	 */
	@Override
	public void onInputEventTriggered(InputEvent evt) {
		mSender.send(new GamePadInputEventMessage(evt).toString());
	}

	public void addSensor(Sensor sensor) {
		mAvailableSensors.add(sensor);
		sensor.setListener(this);
	}

	public void removeSensor(Sensor sensor) {
		mAvailableSensors.remove(sensor);
		sensor.setListener(null);
	}

	public void removeAllSensor() {
		for(Sensor s : mAvailableSensors)
			s.setListener(null);
		
		mAvailableSensors.clear();
	}
	
	public void addAllSensor(Collection<Sensor> sensors) {
		for(Sensor s : sensors)
			s.setListener(this);
		
		mAvailableSensors.addAll(sensors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {

		try {
			if (mSocket != null) {
				Log.i("CLOSING", "CLOSING");
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
		try{
			Message message = ProxyMessage.gameFromJSON(new JSONObject(string));
			switch(message.getType()) {
			case ACCEPT:
				// We are now officially connected to the game! Congratulation!
				break;
			case JOIN:
				// Game is ready, join the game
				mSender.send(new GamePadJoinMessage(mGamePadInfo.getNickname(), mGamePadInfo.getUUID()).toString());
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
					mGameListener.onGameOutputReceived(event);
				}
				break;
			case RENAME:
				if(mGameListener != null) {
					String name = ((ProxyGameRenameMessage)message).getNewName();
					mGameListener.onGameRenamed(name);
				}
				break;
			case INPUTEVENT: // Should not occur
				break;
			}
		}catch(Exception e){
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		// TODO FIXME What could we do? try to reconnect ? But first, check if this service is shuting down ;)
		Log.i("GameBinder","Shit, we are disconnected.");
	}

	/* (non-Javadoc)
	 * @see com.fbessou.sofa.ProxyConnector.OnConnectedListener#onConnected(java.net.Socket)
	 */
	@Override
	public void onConnected(Socket socket) {
		if(socket == null) {
			// TODO retry 
			Log.e("GamePadIOClient", "Connection failed");
		}
		else {
			mSocket = socket;
			mSender = new StringSender(mSocket);
			mReceiver = new StringReceiver(mSocket);
			mReceiver.setListener(this);
			mSender.start();
			mReceiver.start();

			// Send Join message
			mSender.send(new GamePadJoinMessage(mGamePadInfo.getNickname(), mGamePadInfo.getUUID()).toString());
		}
	}
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.ProxyConnector.OnConnectedListener#onDisconnected()
	 */
	@Override
	public void onDisconnected() {
		// TODO FIXME reconnect ?
	}
	
	/**
	 * Indicates if this gamePadIOClient is connected to the proxy.
	 * @return connected or not
	 */
	public boolean isConnected() {
		return mSocket != null && mSocket.isConnected();
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

		mSender.send(new GamePadRenameMessage(info.getNickname()).toString());
	}

	/**
	 * 
	 * @author Proïd
	 *
	 */
	public interface GameMessageListener {
		void onGameOutputReceived(OutputEvent event);
		void onGameRenamed(String newName);
		void onGameLeft();
		//void onGameUnexpectedlyDisconnected();
	}
}
