package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.fbessou.tabouret.NodeParser;

public class Container extends RelativeLayout {

	public Container(Context context) {
		super(context);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	public static class Parser extends NodeParser {

		/**
		 * 
		 */
		public Parser(NodeParser parentParser) {
			super("container", parentParser);
		}

		/**
		 * 
		 */
		public Parser(String tagName, NodeParser parentParser) {
			super(tagName, parentParser);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fbessou.tabouret.NodeParser#parse()
		 */
		@Override
		public View parse() {
			Container container = new Container(getContext());
			setView(container);
			try {
				parseAttributes();
				parseChildren();
				applyLayout();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}

			return container;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fbessou.tabouret.NodeParser#parseChild(java.lang.String)
		 */
		@Override
		protected void parseChild(String tagName) {
			switch (tagName.toLowerCase()) {
			case "container":
				Container container = (Container) new Container.Parser(this).parse();
				((Container) getView()).addView(container);
				break;
			case "joystick":
				JoystickView joystick = (JoystickView) new JoystickView.Parser(this).parse();
				((Container) getView()).addView(joystick);
				break;
			case "button":
				ButtonView button = (ButtonView) new ButtonView.Parser(this).parse();
				((Container) getView()).addView(button);
				break;
			default:
				super.parseChild(tagName);
			}
		}

	}

}
