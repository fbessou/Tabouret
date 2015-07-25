/**
 * 
 */
package com.fbessou.tabouret;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author Frank Bessou
 *
 */
public class NodeParser {
	static final Pattern dimensionPattern = Pattern.compile("([0-9]+)(dp|px)?");

	private final String mTagName; // tag currently parsed
	protected View mView; // view currently created
	protected XmlPullParser mParser; // parser used to read stream
	protected RelativeLayout.LayoutParams mLayoutParams;
	protected final String mResourceDir; // Directory where are located image, sounds and other resources.
	protected final GamePadActivity mGamePad;

	/**
	 * @param context
	 */
	public NodeParser(String tagName, XmlPullParser parser, GamePadActivity gamepad, String resourceDir) {
		mTagName = tagName;
		mParser = parser;
		mResourceDir = resourceDir;
		mLayoutParams = new RelativeLayout.LayoutParams(1, 1);
		mGamePad = gamepad;
	}

	public NodeParser(String tagName, NodeParser parentParser) {
		mTagName = tagName;
		mParser = parentParser.getParser();
		mResourceDir = parentParser.getResourceDir();
		mLayoutParams = new RelativeLayout.LayoutParams(1, 1);
		mGamePad = parentParser.getGamePad();
	}

	public View parse() {
		try {
			parseAttributes();
			parseChildren();
			applyLayout();

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 
	 */
	protected void setView(View v) {
		mView = v;
	}

	/**
	 * 
	 */
	protected View getView() {
		return mView;
	}

	protected String getResourceDir() {
		return mResourceDir;
	}

	/**
	 * 
	 */
	protected RelativeLayout.LayoutParams getLayouParams() {
		return mLayoutParams;
	}

	protected void applyLayout() {
		mView.setLayoutParams(mLayoutParams);
	}

	/**
	 * Loop over all inner tags and call parseChild on
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	protected void parseChildren() throws XmlPullParserException {
		if (isParserStateValid()) {

			try {
				getParser().next();

				while (true) {
					switch (getParser().getEventType()) {
					case XmlPullParser.START_TAG:
						parseChild(getParser().getName());
						getParser().next();
						break;
					case XmlPullParser.END_TAG:
						if (getParser().getName().equalsIgnoreCase(mTagName))
							;
						return;
					case XmlPullParser.END_DOCUMENT:
						Log.e("NodeParser", "Tag " + mTagName + "opened but never closed.");
						return;
					default:

						getParser().next();

						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Override this method if the parsed node contains children
	 */
	protected void parseChild(String tagName) {
	}

	/**
	 * Parse the attributes of the tag. This function must be called before any
	 * call to parseChild(), parseChildren() or a next[Tag]() on the used
	 * {@link XmlPullParser}.
	 */
	protected void parseAttributes() {
		if (isParserStateValid()) {
			for (int i = 0; i < getParser().getAttributeCount(); i++) {
				String key = getParser().getAttributeName(i);
				String val = getParser().getAttributeValue(i);
				parseAttribute(key,val);
				//Log.i("NodeParser", "Parsing attribute "+key+" : "+val);
			}
		} else {
			Log.e("NodeParser(" + mTagName + ")", "Calling parseAttributes on an invalid tag");
		}
	}

	protected void parseAttribute(String key, String val) {
		if (getView() != null) {

			switch (key.toLowerCase()) {
			// backgrounds
			case "background-color":
				getView().setBackgroundColor(Color.parseColor(val));
				break;
			case "background-image":
				getView().setBackground(
						new BitmapDrawable(getContext().getResources(), BitmapFactory
								.decodeFile(mResourceDir + "/" + val)));
				break;
			case "width":
				if(val.equalsIgnoreCase("fill"))
					getLayouParams().width=LayoutParams.MATCH_PARENT;
				else
					getLayouParams().width = parseDimension(val);
				break;
			case "height":
				if(val.equalsIgnoreCase("fill"))
					getLayouParams().height=LayoutParams.MATCH_PARENT;
				else
					getLayouParams().height = parseDimension(val);
				break;
			case "v-align":
				switch (val.toLowerCase()) {
				case "center":
					getLayouParams().addRule(RelativeLayout.CENTER_VERTICAL);
					break;
				case "top":
					getLayouParams().addRule(RelativeLayout.ALIGN_PARENT_TOP);
					break;
				case "bottom":
					getLayouParams().addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					break;

				default:
					break;
				}
				break;
			case "h-align":
				switch (val.toLowerCase()) {
				case "center":
					getLayouParams().addRule(RelativeLayout.CENTER_HORIZONTAL);
					break;
				case "left":
					getLayouParams().addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					break;
				case "right":
					getLayouParams().addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					break;
				default:
					break;
				}
				break;
			case "margin-top" :
				getLayouParams().topMargin = parseDimension(val);
				break;
			case "margin-right" :
				getLayouParams().rightMargin = parseDimension(val);
				break;
			case "margin-bottom" :
				getLayouParams().bottomMargin = parseDimension(val);
				break;
			case "margin-left" :
				getLayouParams().leftMargin = parseDimension(val);
				break;
			default:
				Log.w("NodeParser(" + mTagName + ")", "Unrecognized attribute " + key);
				break;
			}
		}
	}
	
	/**
	 * Get this activity's context
	 * @return The context used by this activity
	 */
	protected Context getContext() {
		return mGamePad;
	}

	/**
	 * @return the Parser
	 */
	public XmlPullParser getParser() {
		return mParser;
	}
	
	/**
	 * @return the currently built GamePad
	 */
	public GamePadActivity getGamePad(){
		return mGamePad;
	}
	/**
	 * Check that the {@link XmlPullParser} current event type is TAG_START
	 * element and that the tag's name correspond's to the type managed by this
	 * parser node parsed
	 * 
	 * @return
	 */
	private boolean isParserStateValid() {
		try {
			return (getParser().getEventType() == XmlPullParser.START_TAG && getParser().getName()
					.equalsIgnoreCase(mTagName));
		} catch (XmlPullParserException e) {
			return false;
		}
	}
	
	/**
	 * 
	 * @param dimension a String in the form "1234unit" ex : "10dp"
	 * @return dimension in pixel
	 */
	protected int parseDimension(String dimension){
		int dim = 100;
		//Log.i("NodeParser",dimension);

		Matcher matcher = dimensionPattern.matcher(dimension);
		if(matcher.matches()){
			String dimS = matcher.group(1);
			try {
				dim = Integer.parseInt(dimS);
				if(matcher.groupCount()>2){
					String unit = matcher.group(2);
					switch (unit) {
					case "dp":
						dim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dim, getContext().getResources().getDisplayMetrics());
						break;
					case "px":
					default:
						break;
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return dim;
	
	}

}
