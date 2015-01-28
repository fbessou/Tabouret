/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;

/**
 * @author Frank Bessou
 *
 */
public class ClientAccepter implements Runnable {
	
	public interface OnClientAcceptedListener{
		public boolean onClientAccepted(Socket socket, int port);
	}
	/**
	 * Port the Accepter is listening on.
	 */
	private int mPort;

	/**
	 * Server socket used to accept connections.
	 */
	private ServerSocket mServerSocket;

	/**
	 * Listener object whose onClientAccepted() method's will be called,
	 * every time a client connects.
	 */
	private OnClientAcceptedListener mListener;

	/**
	 * Create a runnable listening on a given port.
	 * 
	 * @param port
	 *            The port to listen on.
	 */
	public ClientAccepter(int port, OnClientAcceptedListener listener) {
		mPort = port;
		if(listener==null){
			Log.w("ClientAccepter","ClientAccepter has been created without listener.");
		} else {
			mListener = listener;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			boolean continueAccepting = true;
			ServerSocket mServerSocket = new ServerSocket();
			// create a ServerSocket and start accepting.
			mServerSocket.setReuseAddress(true);
			mServerSocket.bind(new InetSocketAddress(mPort));
			try {
				while (continueAccepting) {
					Socket socket = mServerSocket.accept();
					socket.setTcpNoDelay(true);
					continueAccepting = mListener.onClientAccepted(socket, mPort);
				}
				//If we exited normally, don't forget to close the sockets.
				stop();
			} catch (SocketException e) {
				mServerSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Call this method to interrupt the current action
	 * on the ServerSocket.
	 * When this method is called the Runnable should end.
	 */
	void stop() {
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}