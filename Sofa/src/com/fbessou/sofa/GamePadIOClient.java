/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.fbessou.sofa.message.GamePadInputEventMessage;
import com.fbessou.sofa.message.GamePadJoinMessage;
import com.fbessou.sofa.message.GamePadLeaveMessage;
import com.fbessou.sofa.message.GamePadRenameMessage;
import com.fbessou.sofa.message.Message;
import com.fbessou.sofa.message.ProxyGameOutputEventMessage;
import com.fbessou.sofa.message.ProxyGameRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;
import com.fbessou.sofa.message.Message.Type;
import com.fbessou.sofa.sensor.Sensor;

/**
 * @author Frank Bessou
 *
 * GameBinder used by game pads.
 */
public class GamePadIOClient extends Fragment implements Sensor.InputEventTriggeredListener, StringReceiver.Listener, ProxyConnector.OnConnectedListener {
	
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
	private boolean mIsAcceptedByGame = false;
	
	// Communication with proxy

	/**
	 * Socket connecting to a proxy
	 */
	private Socket mSocket = null;
	private StringReceiver mReceiver = null;
	private StringSender mSender = null;
	
	ProxyConnector mConnector;
	Timer mRetryConnectingTimer;

	/**
	 * 
	 */
	public GamePadIOClient(GamePadInformation info) {
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
		Log.i("GameBinder", "Creating fragment, connecting");
		mConnector = new ProxyConnector(this.getActivity().getApplicationContext(), GameIOProxy.DefaultGamePadsPort, this);
		mConnector.connect();
		mRetryConnectingTimer = new Timer();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.i("GameBinder", "destroying fragment");
		
		mConnector.unregisterReceiver();
		mRetryConnectingTimer.cancel();
		
		try {
			if (mSocket != null) {
				sendMessage(new GamePadLeaveMessage());
				Thread.sleep(2000);
				Log.i("GameBinder", "close socket:"+mSocket+" after sleeping 2000ms");
				mSocket.close();
			}
		} catch (IOException e) {
			Log.e("GameBinder", "Error closing socket", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		super.onDestroy();
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
		Log.v("GameBinder", "onInputEventTriggered");
		sendMessage(new GamePadInputEventMessage(evt));
	}

	/**
	 * Sends the given message if we are connected.
	 * @param m message to send
	 */
	private void sendMessage(Message m) {
		Log.v("GameBinder", "sendMessage: "+m.toString());
		if(isConnected()) {
			if(m.getType() != Type.JOIN && !mIsAcceptedByGame)
				Log.w("GameBinder", "Try to send "+m.getType()+" but game pad not accepted by game. Cancel.");
			else
				mSender.send(m.toString());
		}
		else
			Log.w("GameBinder", "sendMessage error : cannot send message, disconnected from proxy");
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
		Log.v("GameBinder", "onStringReceived: "+string+" from socket:"+socket);
		try{
			Message message = ProxyMessage.gameFromJSON(new JSONObject(string));
			switch(message.getType()) {
			case ACCEPT:
				// We are now officially connected to the game! Congratulation!
				mIsAcceptedByGame = true;
				Log.i("GameBinder", "Accepted by the game");
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
					mGameListener.onGameOutputReceived(event);
				}
				break;
			case RENAME:
				if(mGameListener != null) {
					String name = ((ProxyGameRenameMessage)message).getNewName();
					mGameListener.onGameRenamed(name);
				}
				break;
			case LOST: // Should not occur
			case INPUTEVENT: // Should not occur
				break;
			}
		}catch(Exception e){
			Log.e("GameBinder", "onStringReceived error ", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		// TODO FIXME What could we do? try to reconnect ? But first, check if this service is shuting down ;)
		Log.i("GameBinder", "disconnected from socket:"+socket);
		mIsAcceptedByGame = false;
	}

	/* (non-Javadoc)
	 * @see com.fbessou.sofa.ProxyConnector.OnConnectedListener#onConnected(java.net.Socket)
	 */
	@Override
	public void onConnected(Socket socket) {
		if(socket == null) {
			// Connection failed, retry in 5 seconds
			mRetryConnectingTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mConnector.connect();
				}
			}, 5000);
			Log.e("GameIOClient", "Connection failed, retry in 5 seconds...");
		}
		else {
			Log.i("GameBinder", "Connection established, start sender and receiver");
			mSocket = socket;
			mSender = new StringSender(mSocket);
			mReceiver = new StringReceiver(mSocket);
			mReceiver.setListener(this);
			mSender.start();
			mReceiver.start();

			// Send Join message
			sendMessage(new GamePadJoinMessage(mGamePadInfo.getNickname(), mGamePadInfo.getUUID()));
		}
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
		Log.i("GameBinder", "updateGamePadInfo");

		sendMessage(new GamePadRenameMessage(info.getNickname()));
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
		GamePadIOClient gameBinder = (GamePadIOClient) fm.findFragmentByTag("GameBinder");
		if(gameBinder == null) {
			if(info == null)
				info = GamePadInformation.getDefault();
			
			gameBinder = new GamePadIOClient(info);
			fm.beginTransaction().add(gameBinder, "GameBinder").commit();
		}
		return gameBinder;
	}
	
	public void setGameMessageListener(GameMessageListener listener) {
		mGameListener = listener;
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
