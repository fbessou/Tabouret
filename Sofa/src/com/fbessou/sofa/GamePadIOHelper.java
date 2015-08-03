package com.fbessou.sofa;

import android.app.Activity;

import com.fbessou.sofa.GamePadIOClient.GameMessageListener;
import com.fbessou.sofa.sensor.Sensor;

public class GamePadIOHelper {
	GamePadIOClient mGamePadIO;
	// TODO ArrayList<Indicator>
	
	public GamePadIOHelper() {
		// TODO something
	}
	
	public void start(Activity activity, GamePadInformation info) {
		mGamePadIO = GamePadIOClient.getGamePadIOClient(activity, info);
		mGamePadIO.setGameMessageListener(new GameMessage());
	}
	
	public void attachSensor(Sensor sensor) {
		if(mGamePadIO != null)
			mGamePadIO.addSensor(sensor);
		else
			throw new RuntimeException("To attach sensor, the method start(activity, info) must be invoked first");
	}
	
	public void updateInformation(GamePadInformation info) {
		mGamePadIO.updateGamePadInfo(info);
	}
	
	private class GameMessage implements GameMessageListener {
		@Override
		public void onGameOutputReceived(OutputEvent event) {
			// TODO Auto-generated method stub
			
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
}
