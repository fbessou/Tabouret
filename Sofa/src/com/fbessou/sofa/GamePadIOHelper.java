package com.fbessou.sofa;

import java.util.ArrayList;

import android.app.Activity;
import android.util.SparseArray;

import com.fbessou.sofa.GamePadIOClient.GameMessageListener;
import com.fbessou.sofa.indicator.Indicator;
import com.fbessou.sofa.message.GamePadInputEventMessage;
import com.fbessou.sofa.sensor.Sensor;
import com.fbessou.sofa.sensor.Sensor.InputEventTriggeredListener;

public class GamePadIOHelper implements InputEventTriggeredListener {
	GamePadIOClient mGamePadIO;
	
	/** Indicators attached to this IO helper **/
	SparseArray<Indicator> mIndicators = new SparseArray<>();
	/** Sensors attached to this IO helper **/
	ArrayList<Sensor> mSensors = new ArrayList<>();
	
	public GamePadIOHelper() {
		// TODO something
	}
	
	public void start(Activity activity, GamePadInformation info) {
		mGamePadIO = GamePadIOClient.getGamePadIOClient(activity, info);
		mGamePadIO.setGameMessageListener(new GameMessage());
	}
	
	public void attachSensor(Sensor sensor) {
		sensor.setListener(this);
		mSensors.add(sensor);
	}
	
	public void attachIndicator(Indicator indicator) {
		mIndicators.put(indicator.getPadId(), indicator);
	}
	
	public void updateInformation(GamePadInformation info) {
		mGamePadIO.updateGamePadInfo(info);
	}
	
	private class GameMessage implements GameMessageListener {
		@Override
		public void onGameOutputReceived(OutputEvent event) {
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
			// TODO Auto-generated method stub
		}
	
		@Override
		public void onGameLeft() {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onInputEventTriggered(InputEvent evt) {
		GamePadInputEventMessage msg = new GamePadInputEventMessage(evt);
		mGamePadIO.sendMessage(msg);
	}
}
