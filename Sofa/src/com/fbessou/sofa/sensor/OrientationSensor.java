package com.fbessou.sofa.sensor;

import com.fbessou.sofa.InputEvent;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensor extends com.fbessou.sofa.sensor.Sensor implements SensorEventListener {
	private float[] mLastAccelValues = null, mLastMagnetValues = null;
	private float mAzimuth, mPitch, mRoll;
	private SensorManager sensorMgr;
	
	/** Minimum variation for azimuth, pitch or roll values needed to trigger a new event **/
	private static final float deltaMinTrigger = 0.2f;
	
	public OrientationSensor() {
		super();
	}
	public OrientationSensor(int id) {
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
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mLastAccelValues = event.values.clone();
		} else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mLastMagnetValues = event.values.clone();
		
			if(computeOrientation()) {
				InputEvent evt = InputEvent.createMotion3DEvent(0, mAzimuth, mPitch, mRoll, mId);
				triggerEvent(evt);
			}
		}
	}
	
	/** Computes the orientation with the current accelerometer and magnetic values.
	 * @return true if the values has significantly changed **/
	private boolean computeOrientation() {
		float[] R = new float[9]/*, I = new float[9]*/;
        boolean success = SensorManager.getRotationMatrix(R, null/*I*/, mLastAccelValues, mLastMagnetValues);
        if(!success)
        	return false;
        
        float outR[] = new float[9];
        // after calling getRotationMatrix pass the rotationMatix below:
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        
        float orientation[] = new float[3]; /* azimuth, pitch, roll */
        SensorManager.getOrientation(outR, orientation);
        
        float dx = Math.abs(orientation[0] - mAzimuth);
		float dy = Math.abs(orientation[1] - mPitch);
		float dz = Math.abs(orientation[2] - mRoll);
		
		if(dx > deltaMinTrigger || dy > deltaMinTrigger || dz > deltaMinTrigger) {
			mAzimuth = orientation[0];
			mPitch = orientation[1];
			mRoll = orientation[2];
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
