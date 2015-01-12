package com.fbessou.tabouret.view;

import org.xmlpull.v1.XmlPullParser;

import com.fbessou.tabouret.GamePadInformation;

import android.content.Context;
import android.widget.RelativeLayout;

public class GamePadLayout extends RelativeLayout{
	
	protected GamePadInformation mInformation;
	
	
 	public GamePadLayout(Context context) {
		super(context);
		
	}
 	
	static public GamePadLayout parseXML(Context context,XmlPullParser xmlParser) {
		return new GamePadLayout(context);
	}
	
}
