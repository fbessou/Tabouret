package com.fbessou.tabouret;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/** <a href="#GamePadLayout">GamePadLayout</a>
 * jeae
 * @author frank
 * 
 *
 */
public class GamePadInformation {
	public String name;
	public String authors;
	public String description;
	public String url;
	public String icon;
	
	
	public int versionMajor;
	public int versionMinor;
	
	@Override
	public String toString() {
		String ret = (name != null?"Name : "+name+" ":"")+
					 ("v"+versionMajor+"."+versionMinor+"\n")+
					 (authors != null?"By"+authors+"\n":"")+
					 (description != null?"Description : "+description:"");
		return ret;
	}
	public String getVersion(){
		return versionMajor+"."+versionMinor;
	}
	/**
	 * Read a XML tag content and retrieve the GamePadLayout described.
	 * @param xmlParser
	 * @return a <GamePad
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static  GamePadInformation parseXML(XmlPullParser xmlParser) throws XmlPullParserException, IOException{
		GamePadInformation info=new GamePadInformation();
		xmlParser.nextTag();
		while(true){
			switch (xmlParser.getEventType()) {
			case XmlPullParser.START_TAG:
				if(xmlParser.getName().equalsIgnoreCase("name"))
					info.name=xmlParser.nextText();
				if(xmlParser.getName().equalsIgnoreCase("description"))
					info.description=xmlParser.nextText();
				if(xmlParser.getName().equalsIgnoreCase("authors"))
					info.authors=xmlParser.nextText();
				if(xmlParser.getName().equalsIgnoreCase("icon"))
					info.icon=xmlParser.nextText();
				if(xmlParser.getName().equalsIgnoreCase("url"))
					info.url=xmlParser.nextText();
				if(xmlParser.getName().equalsIgnoreCase("version")){
					try {
						String v=xmlParser.nextText();
						int dot=v.indexOf(".");
						info.versionMajor=Integer.parseInt(v.substring(0, dot>0?dot:0));
						info.versionMinor=Integer.parseInt(v.substring(dot>0?dot+1:0,v.length()));
						
					} catch (NumberFormatException e) {
						Log.e("GamePadInformation",e.getMessage());
						Log.e("GamePadInformation","Error while reading version number");
						info.versionMajor=1;info.versionMinor=0;
					}


				}
				else
					xmlParser.next();
				break;
			case XmlPullParser.END_TAG:
				if(xmlParser.getName().equalsIgnoreCase("information"))
					return info;
				else
					xmlParser.nextTag();
				break;

			default:
				xmlParser.nextTag();
				break;
			}
		}		
	}
	
}
