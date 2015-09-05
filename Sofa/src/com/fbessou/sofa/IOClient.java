package com.fbessou.sofa;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.os.Bundle;

import com.fbessou.sofa.message.GamePadPingMessage;
import com.fbessou.sofa.message.Message;

public abstract class IOClient extends Fragment implements StringReceiver.Listener, ProxyConnector.OnConnectedListener, StringSender.Listener {

	/**
	 * Socket connecting to a proxy
	 */
	protected Socket mSocket = null;
	protected StringReceiver mReceiver = null;
	protected StringSender mSender = null;

	/** Connection keeper **/
	/** Maximum mute duration. If the client does not send any message during this duration,
	 * the proxy can consider this client as disconnected.
	 * We need to send message to stay connected to the proxy. **/
	private static final long MaxMuteDuration = 4000;
	/** Timer used to send automatically a message if nothing has been sent for a long time. **/ 
	private ScheduledThreadPoolExecutor mConnectionKeeperTimer;
	private Runnable mConnectionKeeperRunnable = null;
	private boolean mIsConnectionKeeperEnabled = false;
	
	/** Connector **/
	private ProxyConnector mConnector;
	private Timer mRetryConnectingTimer;
	private int mPort;
	
	private boolean mIsFragmentDestroying = false;


	public IOClient(int port) {
		mPort = port;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mIsFragmentDestroying = false;
		
		Log.i("IOClient", "Creating fragment and connecting");
		
		mConnector = new ProxyConnector(this.getActivity().getApplicationContext(), mPort, this);
		mConnector.connect();
		mRetryConnectingTimer = new Timer();
		
		initConnectionKeeper();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("IOClient", "Destroying fragment");

		mIsFragmentDestroying = true;
		
		// Connector
		mConnector.unregisterReceiver();
		mRetryConnectingTimer.cancel();
		mRetryConnectingTimer.purge();
		
		// Disabling connection keeper
		disableConnectionKeeper();
		
		if(mSocket != null) {
			try {
				beforeCommunicationDisabled();
				
				// The sender must send its message before closing socket
				Thread.sleep(2000);// FIXME find a better way to be sure that the leave message has been sent
				
				Log.i("IOClient", "Close socket: "+mSocket+" after sleeping 2000ms");
				mSocket.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onConnected(Socket socket) {
		if(socket == null) {
			// Connection failed, retry in 5 seconds
			mRetryConnectingTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mConnector.connect();
				}
			}, 5000);
			Log.e("IOClient", "Connection failed, retry in 5 seconds...");
		}
		else {
			Log.i("IOClient", "Connection established, start sender and receiver");
			mSocket = socket;
			mSender = new StringSender(mSocket);
			mReceiver = new StringReceiver(mSocket);
			mSender.setListener(this);
			mReceiver.setListener(this);
			mSender.start();
			mReceiver.start();
			
			onCommunicationEnabled();
		}
	}

	/** Called when this client is connected to the proxy and messages can be sent and received.
	 * This method should be override to add features like sending join message **/
	protected void onCommunicationEnabled() {
		// Turn on the connection keeper
		enableConnectionKeeper();
	}

	@Override
	public void onMessageSent(String msg, Socket socket) {
		/** Make sure we will send an other message next. (to stay active) **/
		postDelayedConnectionKeeper();
	}
	
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringSender.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		Log.i("IOClient", "disconnected from socket:"+socket);
		disableConnectionKeeper();
		
		// Try to reconnect. But first, check if this service is shutting down ;)
		if(!mIsFragmentDestroying) {
			reconnect();
		}
	}
	
	/** Called before closing the communication.
	 * This method should be override to add features like send leave message **/
	protected void beforeCommunicationDisabled() {
		Log.i("IOClient", "clearBufferedMessage");
		mSender.clearBufferedMessage();
		
	}

	/**
	 * Sends the given message if we are connected.
	 * @param m message to send
	 */
	abstract public void sendMessage(Message m);
	
	/**
	 * Indicates if this gamePadIOClient is connected to the proxy.
	 * @return connected or not
	 */
	public boolean isConnected() {
		return mSocket != null && mSocket.isConnected();
	}

	/** Re-established the connection if it is broken **/
	public void reconnect() {
		if(!isConnected()) {
			Log.i("IOClient", "Reconnect");
			mConnector.connect();
		} else {
			Log.i("IOClient", "Already connected, cannot to reconnect.");
		}
	}
	
	/** Called when the maximum duration of silence has been reached. This method should
	 * send a message to the proxy to keep the connection. **/
	protected void onConnectionKeeperNotified() {
		Log.i("IOClient", "Warning: Max silence duration reached");
	}
	
	/** Initialize the connection keeper. It has to check that this client
	 * does not keep quiet more than {@code IOClient.MaxMuteDuration} **/
	private void initConnectionKeeper() {
		mConnectionKeeperRunnable = new Runnable() {
			@Override
			public void run() {
				sendMessage(new GamePadPingMessage());
			}
		};
		mConnectionKeeperTimer = new ScheduledThreadPoolExecutor(1);
		mIsConnectionKeeperEnabled = false;
	}
	private void enableConnectionKeeper() {
		Log.i("IOClient", "Enabling connection keeper");
		mIsConnectionKeeperEnabled = true;
		postDelayedConnectionKeeper();
	}
	private void disableConnectionKeeper() {
		Log.i("IOClient", "Disabling connection keeper");
		mIsConnectionKeeperEnabled = false;
		mConnectionKeeperTimer.remove(mConnectionKeeperRunnable);
	}
	/** Reset the timer of the connection keeper **/
	private void postDelayedConnectionKeeper() {
		if(mIsConnectionKeeperEnabled) {
			mConnectionKeeperTimer.remove(mConnectionKeeperRunnable);
			mConnectionKeeperTimer.schedule(mConnectionKeeperRunnable, MaxMuteDuration, TimeUnit.MILLISECONDS);
		}
	}
	
}
