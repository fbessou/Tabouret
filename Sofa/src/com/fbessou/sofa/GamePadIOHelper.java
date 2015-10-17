package com.fbessou.sofa;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Handler;
import android.util.SparseArray;

import com.fbessou.sofa.GamePadIOClient.ConnectionStateChangedListener;
import com.fbessou.sofa.GamePadIOClient.GameMessageListener;
import com.fbessou.sofa.indicator.Indicator;
import com.fbessou.sofa.message.GamePadCustomMessage;
import com.fbessou.sofa.message.GamePadInputEventMessage;
import com.fbessou.sofa.sensor.Sensor;
import com.fbessou.sofa.sensor.Sensor.InputEventTriggeredListener;

public class GamePadIOHelper implements InputEventTriggeredListener {
	GamePadIOClient mGamePadIO;
	Activity mActivity;
	
	/** Indicators attached to this IO helper **/
	SparseArray<Indicator> mIndicators = new SparseArray<>();
	/** Sensors attached to this IO helper **/
	ArrayList<Sensor> mSensors = new ArrayList<>();
	
	/** Handler to post runnable in the main GUI thread **/
	Handler mGUIHandler;
	ConnectionStateChangedListener mConnectionListener;
	OnCustomMessageReceivedListener mCustomMessageListener;
	
	/** Game-pad information **/
	GamePadInformation mGamePadInfo;
	
	public GamePadIOHelper(Activity activity, GamePadInformation info) {
		mGamePadInfo = info;
		mActivity = activity;
		mGUIHandler = new Handler(mActivity.getMainLooper());
	}
	
	/**
	 * 
	 * @param connectionListener can be null (note: the methods of this listener will be called in the main GUI thread)
	 */
	public void start(ConnectionStateChangedListener connectionListener) {
		mGamePadIO = GamePadIOClient.getGamePadIOClient(mActivity, mGamePadInfo);
		mGamePadIO.setGameMessageListener(new GameMessage());
		mGamePadIO.setOnConnectionStateChangedListener(new Connection());
		mConnectionListener = connectionListener;
	}
	
	public boolean isConnected() {
		return mGamePadIO != null && mGamePadIO.isConnected();
	}
	
	public void attachSensor(Sensor sensor) {
		sensor.setListener(this);
		mSensors.add(sensor);
	}
	
	public void attachIndicator(Indicator indicator) {
		mIndicators.put(indicator.getPadId(), indicator);
	}
	
	public void setOnCustomMessageReceivedListener(OnCustomMessageReceivedListener listener) {
		mCustomMessageListener = listener;
	}
	
	public void sendCustomMessage(String customMessage) {
		if(mGamePadIO != null) {
			GamePadCustomMessage msg = new GamePadCustomMessage(customMessage);
			mGamePadIO.sendMessage(msg);
		}
	}
	
	public void updateInformation(GamePadInformation info) {
		mGamePadInfo = info;
		if(mGamePadIO != null)
			mGamePadIO.updateGamePadInfo(mGamePadInfo);
	}
	
	private class GameMessage implements GameMessageListener {
		@Override
		public void onOutputReceived(OutputEvent event) {
			// Get the associated indicator if existing
			Indicator target = mIndicators.get(event.outputId);
			if(target != null) {
				// Transmit the event
				target.onOutputEventReceived(event);
			} else {
				Log.w("GamePadIOHelper", "Output event received but attached indicator not found");
			}
		}
	
		@Override
		public void onGameRenamed(String newName) {
			// TODO attach that to an indicator
		}
	
		@Override
		public void onGameLeft() {
			// TODO Remove this useless methods? the game-pad should be rejected before receiving this message
		}

		
		@Override
		public void onCustomMessageReceived(String customMessage) {
			if(mCustomMessageListener != null)
				mCustomMessageListener.onCustomMessageReceived(customMessage);
		}
	}

	@Override
	public void onInputEventTriggered(InputEvent evt) {
		GamePadInputEventMessage msg = new GamePadInputEventMessage(evt);
		mGamePadIO.sendMessage(msg);
	}

	private class Connection implements ConnectionStateChangedListener {
		@Override
		public void onConnectedToProxy() {
			mGUIHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mConnectionListener != null)
						mConnectionListener.onConnectedToProxy();
				}
			});
		}
	
		@Override
		public void onConnectedToGame() {
			mGUIHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mConnectionListener != null)
						mConnectionListener.onConnectedToGame();
				}
			});
		}
	
		@Override
		public void onDisconnectedFromGame() {
			mGUIHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mConnectionListener != null)
						mConnectionListener.onDisconnectedFromGame();
				}
			});
		}
	
		@Override
		public void onDisconnectedFromProxy() {
			mGUIHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mConnectionListener != null)
						mConnectionListener.onDisconnectedFromProxy();
				}
			});
		}
	}

	public interface OnCustomMessageReceivedListener {
		public void onCustomMessageReceived(String customMessage);
	}
}
