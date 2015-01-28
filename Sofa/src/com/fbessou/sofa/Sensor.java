/**
 * 
 */
package com.fbessou.sofa;


/**
 * @author Frank Bessou
 *
 */
public class Sensor {
	
	public interface Listener{
		void onInputEvent(InputEvent evt);
	}
	
	public enum SensorType {
		ANALOG_1D, ANALOG_2D, ANALOG_3D, KEY, TEXT;

		public Character getFootprint() {
			switch (this) {
			case ANALOG_1D:
				return 'l';
			case ANALOG_2D:
				return '+';
			case KEY:
				return 'l';
			case TEXT:
				return 't';
			default:
				return 'u';
			}
		}
	}
	
	private boolean enabled=true;
	/**
	 * unique identifier 
	 */
	private int mId;
	private SensorType mType;
	private Listener mListener;
	
	
	public static int sourceId = 0;
	/**
	 * Constructor
	 */
	public Sensor(SensorType type) {
		mType = type;
		mId = sourceId++;
	}
	
	public int getId(){
		return mId;
	}
	
	/**
	 * Connect this sensor to a listener.
	 * On state change, the onInputEvent method will be called on this listener.
	 * @param listener
	 */
	public void setListener(Listener listener){
		mListener = listener;
	}
	
	protected void triggerEvent(InputEvent event){
		if(mListener != null)
			mListener.onInputEvent(event);
	}
	// ?
	Character getFootprint(){
		return mType.getFootprint();
	}

}
