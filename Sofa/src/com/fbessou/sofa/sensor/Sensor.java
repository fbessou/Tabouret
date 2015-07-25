/**
 * 
 */
package com.fbessou.sofa.sensor;

import com.fbessou.sofa.InputEvent;


/**
 * @author Frank Bessou
 *
 */
public class Sensor {
	
	/** Listener on input event **/
	public interface InputEventListener{
		void onInputEventTriggered(InputEvent evt);
	}
	
	/** Type of sensor **/
	public enum SensorType {
		ANALOG_1D('l'), ANALOG_2D('+'), ANALOG_3D('>'), KEY('l'), TEXT('t');
		
		/** A footprint is associated with each item FIXME why? */
		private char footprint;
		SensorType(char c) {
			footprint = c;
		}
		public Character getFootprint() {
			return footprint;
		}
	}
	
	/** Static id counter used to auto generate an id for each new sensor **/
	public static int sourceId = 0;
	
	/** Indicates if the sensor is enabled **/
	private boolean mEnabled = true;
	/** unique identifier **/
	protected int mId;
	/** Type of this sensor **/
	private SensorType mType;
	/** Listener attached to this sensor **/
	private InputEventListener mListener;
	
	
	/** Make a new sensor of the given type, with auto generated id **/
	public Sensor(SensorType type) {
		mType = type;
		mId = sourceId++;
	}
	
	public int getId() {
		return mId;
	}
	
	/**
	 * Attach this sensor to a listener.
	 * On state change, the onInputEvent method will be called on this listener.
	 * @param listener
	 */
	public void setListener(InputEventListener listener){
		mListener = listener;
	}
	
	/** Trigger the given event if this sensor is enable **/
	protected void triggerEvent(InputEvent event){
		if(mListener != null && mEnabled)
			mListener.onInputEventTriggered(event);
	}
	
	/**
	 * Sets the state of this sensor. A disabled sensor will not trigger events.
	 * @params enabled 
	 */
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}
	
	/**
	 * Returns the state of this sensor. A disabled sensor will not trigger events.
	 */
	public boolean isEnabled() {
		return mEnabled;
	}
	
	/** FIXME ? **/
	Character getFootprint(){
		return mType.getFootprint();
	}

}
