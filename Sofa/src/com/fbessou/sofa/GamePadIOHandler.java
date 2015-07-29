package com.fbessou.sofa;

import android.app.Activity;

import com.fbessou.sofa.GameBinder.GameMessageListener;
import com.fbessou.sofa.sensor.Sensor;

public class GamePadIOHandler implements GameMessageListener {
	GameBinder mGameBinder;
	// TODO ArrayList<Indicator>
	
	public GamePadIOHandler() {
		// TODO something
	}
	
	public void start(Activity activity, GamePadInformation info) {
		mGameBinder = GameBinder.getGameBinder(activity, info);
		mGameBinder.setGameMessageListener(this);
	}
	
	public void attachSensor(Sensor sensor) {
		if(mGameBinder != null)
			mGameBinder.addSensor(sensor);
		else
			throw new RuntimeException("To attach sensor, the method start(activity, info) must be invoked first");
	}
	
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
