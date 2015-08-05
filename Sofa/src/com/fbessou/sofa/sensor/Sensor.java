/**
 * 
 */
package com.fbessou.sofa.sensor;

import com.fbessou.sofa.InputEvent;


/**
 * @author Frank Bessou
 *
 */
abstract public class Sensor {
	public static final int KEY_CATEGORY_VALUE = 1000;
	public static final int ANALOG_CATEGORY_VALUE = 2000;
	public static final int WORLD_CATEGORY_VALUE = 3000;
	
	/** Listener on input event **/
	public interface InputEventTriggeredListener{
		void onInputEventTriggered(InputEvent evt);
	}
	
	/** Static id counter used to auto generate an id for each new sensor **/
	private static int sourceId = 0;
	/** Reset the source id for auto generated id**/
	public static void init() {
		sourceId = 0;
	}
	
	/** Indicates if the sensor is enabled **/
	private boolean mEnabled = true;
	/** unique identifier **/
	protected int mId;
	/** Listener attached to this sensor **/
	private InputEventTriggeredListener mListener;
	
	
	/** Make a new sensor with auto generated id **/
	public Sensor() {
		mId = sourceId++;
	}
	/** Make a new sensor with auto generated id **/
	public Sensor(int id) {
		mId = id;
	}
	
	/** Returns the pad's id**/
	public int getPadId() {
		return mId;
	}
	/** Sets the pad's id **/
	public void setPadId(int id) {
		mId = id;
	}
	/**
	 * Attach this sensor to a listener.
	 * On state change, the onInputEvent method will be called on this listener.
	 * @param listener
	 */
	public void setListener(InputEventTriggeredListener listener){
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
	
}
