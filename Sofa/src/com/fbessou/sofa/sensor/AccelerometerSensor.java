package com.fbessou.sofa.sensor;

import com.fbessou.sofa.InputEvent;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerSensor extends com.fbessou.sofa.sensor.Sensor implements SensorEventListener {
	private float mValueX, mValueY, mValueZ;
	private SensorManager sensorMgr;
	
	/** Minimum variation for x, y or z values needed to trigger a new event **/
	private static final float deltaMinTrigger = 0.05f;
	
	public AccelerometerSensor() {
		super();
	}
	public AccelerometerSensor(int id) {
		super(id);
	}
	/** Starts handling accelerometer sensor and triggering events **/
	public void start(Context context) {
		sensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	}
	/** Stops accelerometer sensor **/
	public void stop() {
		sensorMgr.unregisterListener(this);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		float dx = Math.abs(event.values[0] - mValueX);
		float dy = Math.abs(event.values[1] - mValueY);
		float dz = Math.abs(event.values[2] - mValueZ);
		
		if(dx > deltaMinTrigger || dy > deltaMinTrigger || dz > deltaMinTrigger) {
			mValueX = event.values[0];
			mValueY = event.values[1];
			mValueZ = event.values[2];
			
			InputEvent evt = InputEvent.createMotion3DEvent(0, mValueX,mValueY,mValueZ, mId);
			triggerEvent(evt);
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
