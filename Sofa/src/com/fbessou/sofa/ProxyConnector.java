/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Handler;

/**
 * Use this class to easily connect to a proxy when you don't know if
 * it is running on your device or on the groupOwner
 * TODO retry connect()
 */
public class ProxyConnector extends BroadcastReceiver implements ConnectionInfoListener {
	private boolean mIsWifiP2pConnected = false;
	private boolean mIsReceiverRegistered = false;
	/** Set to true if we are currently trying to establish a connection **/
	private boolean mIsEstablishingConnection = false;
	
	private int mPort;
	private InetAddress mHostAddress;
	
	private Context mContext;
	private WifiP2pManager mWifiManager;
	private Channel mChannel;
	
	private Handler mHandler;

	public interface OnConnectedListener {
		/**
		 * When a TCP connection is established or failed, this method is called.
		 * 
		 * @param socket The distant socket obtained from connection or null if connection can't be established.
		 */
		void onConnected(Socket socket);
	}
	/**
	 * Interface whose the onConnected method is called when the connection is established.
	 */
	private OnConnectedListener mListener = null;

	/**
	 * (Must be call in a thread with a Looper - activity or service - or should
	 * add a looper as a parameter of this constructor)
	 */
	public ProxyConnector(Context context, int port, OnConnectedListener listener) {
		mContext = context.getApplicationContext();
		
		mPort = port;
		mWifiManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mWifiManager.initialize(mContext, mContext.getMainLooper(), null);
		
		mListener = listener;
		mHandler = new Handler(); // Must be call in a thread with a Looper: activity or service
		
		Log.i("ProxyConnector", "initialisation");
	}
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mContext.registerReceiver(this, filter);
		mIsReceiverRegistered = true;
		
		Log.i("ProxyConnector", "Register receiver with filter WIFI_P2P_CONNECTION_CHANGED_ACTION");
	}
	/**
	 * Should be called when the activity / service is destroyed
	 */
	public void unregisterReceiver() {
		mContext.unregisterReceiver(this);
		mIsReceiverRegistered = false;
		
		Log.i("ProxyConnector", "Unregister receiver with filter WIFI_P2P_CONNECTION_CHANGED_ACTION");
	}

	/** Start the process of connection. The methods of the listener will
	 * be called when the result is known. You cannot start a new connection
	 * if a previous connection is not ended. **/
	public void connect() {
		if(mIsEstablishingConnection) {
			Log.w("ProxyConnector", "Cannot start the process of connection. An other process is still running.");
			return;
		}
		
		mIsEstablishingConnection = true;
		
		// Register wifi p2p receiver and wait for group owner informations
		if(!mIsReceiverRegistered)
			registerReceiver();
		// If we already know the host address, connect right now
		else if(mIsWifiP2pConnected)
			connectToProxy(mHostAddress, mPort);
		
		Log.i("ProxyConnector", "start process of connection, broadcastReceiver.registerReceiver wifi p2p");
	}

	/**
	 * Called when this broadcast receiver receives an intent
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 **/
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {
			NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (info.isConnected()) {
				Log.i("ProxyConnector", "wifi p2p connection etablished, querying connection info");
				mWifiManager.requestConnectionInfo(mChannel, this);
			}
			else {
				mIsWifiP2pConnected = false;
			}
		}
	}

	/**
	 * This callback is called when the device is connected with wifi p2p to an other device.
	 * The wifi p2p info let us know if this device is the group owner (in this case
	 * the proxy address is localhost) or not (in this case, we get the proxy's address from info).
	 * Finally, we connect to proxy.
	 * 
	 * 
	 * @see android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener#
	 * onConnectionInfoAvailable(android.net.wifi.p2p.WifiP2pInfo)
	 **/
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		mIsWifiP2pConnected = true;
		
		// Update the address of the group owner
		if (!info.isGroupOwner) {
			mHostAddress = info.groupOwnerAddress;
			Log.i("ProxyConnector","The group owner is "+mHostAddress.getHostAddress());
		} else {
			mHostAddress = null /* local host */;
			Log.i("ProxyConnector", "I am the group owner");
		}
		
		if(mIsEstablishingConnection)
			connectToProxy(mHostAddress, mPort);
	}

	/**
	 * Connect to the proxy with an address and a port
	 * @param hostaddr proxy's address. May be null for localhost
	 * @param port proxy's port
	 **/
	void connectToProxy(final InetAddress hostaddr, final int port) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Socket socket = null;
				InetAddress address = hostaddr;
				try {
					// Get the localhost address if hostaddr is not defined
					if(address == null)
						address = InetAddress.getLocalHost();
					
					// create a new socket and connect
					socket = new Socket(address, port);
					socket.setTcpNoDelay(true);
					
					Log.i("ProxyConnector", "Connected to " + address);
				} catch (IOException e) {
					socket = null;
					
					Log.w("ProxyConnector", "Connexion attempt to "+address+" failed", e);
				}

				// Call the callback method in the main looper
				final Socket finalSocket = socket;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mListener.onConnected(finalSocket);
					}
				});
				
				// Connection establishment finished
				mIsEstablishingConnection = false;
			}
		}).start();
	}
}
