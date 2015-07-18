/**
 * 
 */
package com.fbessou.sofa;


/**
 * @author Frank Bessou
 *
 */
public class Sensor {
	
	/** Listener on input event **/
	public interface InputEventListener{
		void onInputEvent(InputEvent evt);
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
	private boolean mEnabled=true;
	/** unique identifier **/
	private int mId;
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
	
	/** Trigger the given event **/
	protected void triggerEvent(InputEvent event){
		if(mListener != null)
			mListener.onInputEvent(event);
	}
	/** FIXME ? **/
	Character getFootprint(){
		return mType.getFootprint();
	}

}
