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
import android.util.Log;

/**
 * Use this class to connect to easily connect to a proxy when you don't know if it
 * is running on your device or on the groupOwner
 *
 */
public class ProxyConnector extends BroadcastReceiver implements ConnectionInfoListener {
	private int mPort;
	private Context mContext;
	private WifiP2pManager mManager;
	private Channel mChannel;
	public interface OnConnectedListener{
		void onConnected(Socket socket);
	}
	
	private OnConnectedListener mListener=null;
	/**
	 * 
	 */
	public ProxyConnector(Context context, int port, OnConnectedListener listener) {
		mContext=context.getApplicationContext();
		mPort=port;
		mListener=listener;
		mManager = (WifiP2pManager)mContext.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(mContext, mContext.getMainLooper(),null);

	}
	
	public void connect(){
		IntentFilter filter = new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mContext.registerReceiver(this, filter);
		Log.i("Advancing","aeazeazea");
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
			NetworkInfo info = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (info.isConnected()) {
				mManager.requestConnectionInfo(mChannel, this);
			}
			else{
				connectToProxy(null, mPort);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener#onConnectionInfoAvailable(android.net.wifi.p2p.WifiP2pInfo)
	 */
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		if (!info.isGroupOwner) {
			final InetAddress ip = info.groupOwnerAddress;
			Log.d(ip.getHostAddress(), "aze");

			connectToProxy(ip, mPort);

		}
		else
			connectToProxy(null, mPort);

	}

	void connectToProxy(final InetAddress hostaddr, final int port) {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				Socket socket=null;
				try {
					String address = (hostaddr == null? InetAddress.getLocalHost().getHostAddress(): hostaddr.getHostAddress());
					socket = new Socket(address, port);
					Log.i("Connected", "Connected to "+address);
				} catch (IOException e) {
					socket = null;
					Log.v("GameBinder", "Connexion attempt failed");
					e.printStackTrace();
				}
				if(hostaddr!=null && socket==null )
					connectToProxy(null, mPort);
				else
					mListener.onConnected(socket);
			}
		}).start();
	}
}
