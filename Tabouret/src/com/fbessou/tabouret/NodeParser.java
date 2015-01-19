/**
 * 
 */
package com.fbessou.tabouret;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author Frank Bessou
 *
 */
public class NodeParser {

	private Context mContext; // Context used to create views
	private final String mTagName; // tag currently parsed
	protected View mView; // view currently created
	protected XmlPullParser mParser; // parser used to read stream
	protected RelativeLayout.LayoutParams mLayoutParams;
	protected final String mResourceDir; // Directory where are located image,
											// sounds and other resources.

	/**
	 * @param context
	 */
	public NodeParser(String tagName, XmlPullParser parser, Context context, String resourceDir) {
		mTagName = tagName;
		mContext = context;
		mParser = parser;
		mResourceDir = resourceDir;
		mLayoutParams = new RelativeLayout.LayoutParams(1, 1);
	}

	public NodeParser(String tagName, NodeParser parentParser) {
		mTagName = tagName;
		mContext = parentParser.getContext();
		mParser = parentParser.getParser();
		mResourceDir = parentParser.getResourceDir();
		mLayoutParams = new RelativeLayout.LayoutParams(1, 1);
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
				Log.i("NodeParser", "Parsing attribute "+key+" : "+val);
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
						new BitmapDrawable(mContext.getResources(), BitmapFactory
								.decodeFile(mResourceDir + "/" + val)));
				break;
			case "width":
				getLayouParams().width = Integer.parseInt(val);
				break;
			case "height":
				getLayouParams().height = Integer.parseInt(val);
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
				getLayouParams().topMargin = Integer.parseInt(val);
				break;
			case "margin-right" :
				getLayouParams().rightMargin = Integer.parseInt(val);
				break;
			case "margin-bottom" :
				getLayouParams().bottomMargin = Integer.parseInt(val);
				break;
			case "margin-left" :
				getLayouParams().leftMargin = Integer.parseInt(val);
				break;
			default:
				Log.w("NodeParser(" + mTagName + ")", "Unrecognized attribute " + key);
				break;
			}
		}
	}

	protected Context getContext() {
		return mContext;
	}

	/**
	 * @return the Parser
	 */
	public XmlPullParser getParser() {
		return mParser;
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

}
