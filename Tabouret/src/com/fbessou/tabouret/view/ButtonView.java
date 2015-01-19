package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParserException;

import com.fbessou.tabouret.NodeParser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

public class ButtonView extends View {

	ButtonView(Context context) {
		super(context);
	}

	private boolean mWasPressed = false;

	@SuppressLint("ClickableViewAccessibility")
	// Doesn't matter! :P
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = super.onTouchEvent(event);

		boolean pressed = isPressed();
		if (mWasPressed != pressed) {
			mStateChangedListener.onStateChanged(pressed);
			mWasPressed = pressed;
		}

		return handled;
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
