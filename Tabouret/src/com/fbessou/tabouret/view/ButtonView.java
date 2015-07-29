package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.fbessou.sofa.sensor.KeySensor;
import com.fbessou.tabouret.NodeParser;

public class ButtonView extends View {
	/** Buttons images, null if not defined **/
	StateListDrawable mDrawable;
	KeySensor sensor;
	
	ButtonView(Context context) {
		super(context);
		setClickable(true);
		sensor = new KeySensor(false);
	}
	
	/** To remember is the button was pressed at the last touch event **/
	private boolean mWasPressed = false;

	@SuppressLint("ClickableViewAccessibility")
	// Doesn't matter! :P
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		boolean handled = super.onTouchEvent(event);

		boolean pressed = isPressed();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			pressed=true;
			Vibrator v = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(10);
			sensor.setValue(true);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			pressed=false;
			sensor.setValue(false);

			break;
		default:
			break;
		}
		
		if (mWasPressed != pressed) {
			Log.i("Test",mDrawable.getState().toString());
			
			// Send the event to the listener
			if(mStateChangedListener != null)
				mStateChangedListener.onStateChanged(pressed);
			mWasPressed = pressed;
		}

		return true;
	}

	/** Sets the released button image; should be call only once
	 * @see to android.graphics.drawable.Drawable.createFromPath(pathName) **/
	void setReleasedImage(Drawable released) {
		if(mDrawable == null)
			mDrawable = new StateListDrawable();
		
		// Add the pressed state and its image
		mDrawable.addState(new int[] { -android.R.attr.state_pressed}, released);
		// Update the view's background
		setBackground(mDrawable);
	}
	
	/** Sets the pressed button image; should be call only once
	 * @see to android.graphics.drawable.Drawable.createFromPath(pathName) **/
	void setPressedImage(Drawable pressed) {
		if(mDrawable == null)
			mDrawable = new StateListDrawable();
		
		// Add the pressed state and its image
		mDrawable.addState(new int[] { android.R.attr.state_pressed }, pressed);
		
		// Update the view's background
		setBackground(mDrawable);
	}

	private OnStateChangeListener mStateChangedListener;

	public void setOnStateChangedListener(OnStateChangeListener listener) {
		mStateChangedListener = listener;
	}

	public interface OnStateChangeListener {
		public void onStateChanged(boolean isPressed);
	}

	/**
	 * XML parser
	 * @author Frank Bessou
	 *
	 */
	public static class Parser extends NodeParser {

		/**
		 * @param tagName
		 * @param parentParser
		 */
		public Parser(NodeParser parentParser) {
			super("button", parentParser);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fbessou.tabouret.NodeParser#parse()
		 */
		@Override
		public View parse() {
			ButtonView button = new ButtonView(getContext());
			setView(button);
			try {
				parseAttributes();
				parseChildren();
				applyLayout();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			mGamePad.getGamePadIOClient().addSensor(button.sensor);

			return button;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fbessou.tabouret.NodeParser#parseAttribute(java.lang.String,
		 * java.lang.String)
		 */
		@Override
		protected void parseAttribute(String key, String val) {
			if (getView() != null) {
				switch (key.toLowerCase()) {
				case "background-image":
					Log.i("Yeaakk","azeaze");
					((ButtonView)getView()).setReleasedImage(
							new BitmapDrawable(getContext().getResources(), BitmapFactory
									.decodeFile(mResourceDir + "/" + val)));
					String pressedName= val.replaceFirst("(.*)\\.([^.]+)$", "$1_.$2");
					((ButtonView)getView()).setPressedImage(
							new BitmapDrawable(getContext().getResources(), BitmapFactory
									.decodeFile(mResourceDir + "/" + pressedName)));
					break;
				/*
				 * case "mode": if(val.equalsIgnoreCase("togglable"))
				 * ((ButtonView)getView()).setTogglable(true);
				 */
				default:
					super.parseAttribute(key, val);
					break;
				}
			}
		}
	}
}
