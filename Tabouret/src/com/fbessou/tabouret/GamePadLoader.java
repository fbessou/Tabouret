package com.fbessou.tabouret;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

import com.fbessou.tabouret.view.GamePadLayout;

/**
 * 
 * @author frank
 *
 */
public class GamePadLoader {
	private XmlPullParser mXmlParser;
	private Context mContext;
	private Schema mSchema;

	public GamePadLoader(Context context, String xsdFile) {
		mContext = context;
		// TODO see if valdation can be done
		/*
		 * SchemaFactory schemaFactory =
		 * SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); try {
		 * mSchema = schemaFactory.newSchema(new File(xsdFile)); } catch
		 * (SAXException e) { e.printStackTrace(); }
		 */
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			mXmlParser = factory.newPullParser();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param fileName
	 *            the file to open
	 * @return true if the XML represent a valid gamepad, false otherwise
	 */
	public boolean isFileValid(String fileName) {
		try {
			return isXMLValid(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param xmlReader
	 */
	public boolean isXMLValid(Reader xmlReader) {
		Validator validator = mSchema.newValidator();
		// TODO set ErrorHandler !!
		try {
			validator.validate(new StreamSource(xmlReader));
			return true;

		} catch (SAXException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Load a GamePad from a given file
	 * 
	 * @param fileName
	 *            is the file containing the gamepad information
	 */
	public void parseFile(String fileName) {
		try {
			File f = new File(fileName);
			parseXML(new FileReader(f));
		} catch (FileNotFoundException e) {
			Log.e("LayoutLoader",
					mContext.getResources().getString(R.string.err_filenotfound, fileName));
		}
	}

	/**
	 * 
	 * @param reader
	 * @param listener
	 *            Listener called when the d
	 */
	public GamePadLayout parseXML(Reader reader) {
		GamePadLayout layout = null;

		try {
			mXmlParser.setInput(reader);

			mXmlParser.getEventType();
			while (mXmlParser.getEventType() != XmlPullParser.END_DOCUMENT) {

				switch (mXmlParser.getEventType()) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (mXmlParser.getName().equalsIgnoreCase("gamepad")) {
						layout = parseGamePadElement();
					}
					break;
				default:
					break;
				}
				mXmlParser.next();
			}
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
		}

		return layout;
	}

	public GamePadInformation parseInfoFromFile(String fileName){
		try{
			return parseInfoFromReader(new FileReader(new File(fileName)));
		} catch(FileNotFoundException e){
			e.printStackTrace();
			return null;
		}
		
		
	}
	/**
	 * 
	 * @param reader
	 *            contains the e
	 * @return a GamePadInformation instance containing info about the given
	 *         GamePad document
	 */
	public GamePadInformation parseInfoFromReader(Reader reader) {
		try {
			mXmlParser.setInput(reader);
			while (true) {
				mXmlParser.next();
				switch (mXmlParser.getEventType()) {
				case XmlPullParser.START_TAG:
					if (mXmlParser.getName().equalsIgnoreCase("information")){
						return GamePadInformation.parseXML(mXmlParser);
						}
						break;
				case XmlPullParser.END_DOCUMENT:
					return null;
				default:
					break;
				}
			}
		} catch (XmlPullParserException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	GamePadLayout parseGamePadElement() throws XmlPullParserException, IOException {
		GamePadInformation info = null;
		GamePadLayout layout = null;
		mXmlParser.nextTag();
		while (true) {

			switch (mXmlParser.getEventType()) {
			case XmlPullParser.START_TAG:
				if (mXmlParser.getName().equalsIgnoreCase("information")) {
					info = GamePadInformation.parseXML(mXmlParser);
					Log.i("TEST", "" + info);
				} else if (mXmlParser.getName().equalsIgnoreCase("layout")) {

				}
				break;
			case XmlPullParser.END_TAG:
				if (mXmlParser.getName().equalsIgnoreCase("gamepad"))
					return layout;
				if (mXmlParser.getName().equalsIgnoreCase("layout")) {
				}

			default:
				break;
			}
			mXmlParser.nextTag();
		}

	}

}
