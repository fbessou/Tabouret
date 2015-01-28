package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;

import com.fbessou.tabouret.GamePadActivity;
import com.fbessou.tabouret.GamePadInformation;
import com.fbessou.tabouret.NodeParser;

public class GamePadLayout extends Container{
	
	protected GamePadInformation mInformation;
	protected int mOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
 	public GamePadLayout(Context context) {
		super(context);
		
	}
 	
	static public GamePadLayout parseXML(Context context,XmlPullParser xmlParser) {
		return new GamePadLayout(context);
	}
	
	protected void setOrientation(int orientation) {
		mOrientation=orientation;
	}
	
	public int getOrientation() {
		return mOrientation;
	}
	
	public static class Parser extends Container.Parser{
		
		/**
		 * @param parser {@link XmlPullParser} used to parse a stream
		 * @param context context used to create views
		 * @param resDir directory where are located resources (images, sounds, icons)
		 */
		public Parser(XmlPullParser parser, GamePadActivity gamepad,String resDir) {
			super("layout",new NodeParser(null,parser,gamepad,resDir));
		}
		
		/* (non-Javadoc)
		 * @see com.fbessou.tabouret.view.Container.Parser#parse()
		 */
		@Override
		public View parse() {
			GamePadLayout layout = new GamePadLayout(getContext());
			setView(layout);
			try {
				parseAttributes();
				parseChildren();
				//applyLayout();
			} catch (XmlPullParserException e ) {
				e.printStackTrace();
			}

			return layout;
			
		}
		/* (non-Javadoc)
		 * @see com.fbessou.tabouret.NodeParser#parseAttribute(java.lang.String, java.lang.String)
		 */
		@Override
		protected void parseAttribute(String key, String val) {
			if(mView != null){
				switch (key.toLowerCase()) {
				case "orientation":
					switch (val.toLowerCase()) {

					case "portrait":
						((GamePadLayout)getView()).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

						break;
					case "landscape":
					default:
						((GamePadLayout)getView()).setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
						break;
					}
					break;
				default:
					super.parseAttribute(key, val);
					break;
				}
			}
		}
	}
}
