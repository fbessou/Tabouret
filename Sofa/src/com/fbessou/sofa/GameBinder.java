/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.fbessou.sofa.ProxyConnector.OnConnectedListener;

import android.R.string;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils.StringSplitter;
import android.util.Log;

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
						mSocket.getOutputStream().write(getJson(evt).getBytes());
					} catch (IOException e) {
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

	String getJson(InputEvent evt) {
		JSONObject eventJ = new JSONObject();
		try {
			eventJ.put("inputId", evt.sourceId);
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
				eventJ.put("type", "motion2d");
				eventJ.put("x", evt.x);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return eventJ.toString() + "\n";
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
	public void onStringReceived(String s) {
		try{
			JSONObject message = new JSONObject(s);
			if(message.has("type")){
			switch (message.getString("type")) {
			case "hello":
				mUUID = UUID.fromString(message.getString("uuid"));
				break;
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
			new Thread(mReceiver).start();
			new Thread(mSender).start();
			if(mUUID!=null)
				mSender.send("{\"type\":\"hello\",\"uuid\":\""+mUUID+"\"}\n");
			else
				mSender.send("{\"type\":\"hello\"}\n");
		}
		else
			Log.w("GameBinder","Couldn't connect to the proxy");
	}

}
