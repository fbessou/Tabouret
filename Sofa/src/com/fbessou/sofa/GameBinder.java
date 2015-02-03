/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.fbessou.sofa.ProxyConnector.OnConnectedListener;

/**
 * @author Frank Bessou
 *
 */
public class GameBinder extends Fragment implements Sensor.Listener, StringReceiver.Listener, OnConnectedListener {
	/**
	 * 
	 */
	GameInformation gameInfo;
	/**
	 * Stored unique identifier to help recovering
	 */
	UUID mUUID;

	/**
	 * 
	 */
	ArrayList<Sensor> mAvailableSensors = new ArrayList<Sensor>();

	// Communication with proxy

	/**
	 * Socket connecting to a proxy
	 */
	private Socket mSocket = null;
	private StringReceiver mReceiver = null;
	private StringSender mSender = null;


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		ProxyConnector connector = new ProxyConnector(this.getActivity().getApplicationContext(), 9696, this);
		connector.connect();
		/**
		 * // Before connecting to this service, we have to wait for the service
		 * // until we are sure it is running LocalBroadcastManager lbm =
		 * LocalBroadcastManager
		 * .getInstance(getActivity().getApplicationContext());
		 * lbm.registerReceiver(new BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) {
		 * 
		 *           } }, new IntentFilter("IO_PROXY_RUNNING"));
		 **/
	}

	// ArrayList<Output> mOutputMapping;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fbessou.sofa.Sensor.Listener#onInputEvent(com.fbessou.sofa.InputEvent
	 * )
	 */
	@Override
	public void onInputEvent(final InputEvent evt) {
		Log.i("GameBinder", evt.toString());
		new Thread(new Runnable() {

			@Override
			public void run() {

				// check we are connected to a server
				if (mSocket != null) {
					try {
						JSONObject obj = new JSONObject();
						obj.put("type","inputevent");
						obj.put("event",getJson(evt) );
						mSender.send(obj.toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void addSensor(Sensor sensor) {
		mAvailableSensors.add(sensor);
		sensor.setListener(this);
	}

	JSONObject getJson(InputEvent evt) {
		JSONObject eventJ = new JSONObject();
		try {
			eventJ.put("input", evt.inputId);
			eventJ.put("pad", evt.padId);
			switch (evt.eventType) {
			case MOTION_3D:
				eventJ.put("type", "motion3d");
				eventJ.put("x", evt.x);
				eventJ.put("y", evt.y);
				eventJ.put("z", evt.z);
				break;
			case MOTION_2D:
				eventJ.put("type", "motion2d");
				eventJ.put("x", evt.x);
				eventJ.put("y", evt.y);
				break;
			case MOTION_1D:
				eventJ.put("type", "motion1d");
				eventJ.put("x", evt.x);
				break;
			case KEY_DOWN:
				eventJ.put("type", "key_down");
				break;
			case KEY_UP:
				eventJ.put("type", "key_up");
				break;
			case TEXT_SENT:
				eventJ.put("type", "text");
				eventJ.put("text", evt.text);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return eventJ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {

		try {
			if (mSocket != null) {
				Log.i("CLOSING", "CLOSING");
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
	 */
	@Override
	public void onStringReceived(String string, Socket socket) {
		try{
			JSONObject message = new JSONObject(string);
			if(message.has("type")){
			switch (message.getString("type")) {
			case "hello":
				mUUID = UUID.fromString(message.getString("uuid"));
				break;
			case "outputevent":
				JSONObject event = message.getJSONObject("event");
				switch (event.getString("type")) {
				case "haptic" :
					Vibrator v = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(150);
					break;

				default:
					break;
				}
			default:
				break;
			}
				
			}
		}catch(Exception e){
		}
	}

	/* (non-Javadoc)
	 * @see com.fbessou.sofa.ProxyConnector.OnConnectedListener#onConnected(java.net.Socket)
	 */
	@Override
	public void onConnected(Socket socket) {
		Log.i("Connected?",""+socket);
		if(socket!=null){
			mSocket = socket;
			mReceiver = new StringReceiver(mSocket);
			// TODO setListener
			mSender = new StringSender(mSocket);
			mReceiver.start();
			mSender.start();
			if(mUUID!=null)
				mSender.send("{\"type\":\"hello\",\"uuid\":\""+mUUID+"\"}");
			else
				mSender.send("{\"type\":\"hello\"}");
		}
		else
			Log.w("GameBinder","Couldn't connect to the proxy");
	}

	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		Log.i("GameBinder","Shit, we are disconnected.");
	}

}
