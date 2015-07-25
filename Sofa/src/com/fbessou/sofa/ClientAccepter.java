/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Frank Bessou
 *
 */
public class ClientAccepter extends Thread {
	
	public interface OnClientAcceptedListener{
		// Should return true if more clients can be accepted
		public boolean onClientAccepted(Socket socket, int port);
	}
	
	/**
	 * Port the Accepter is listening on.
	 */
	private int mPort;

	/**
	 * Server socket used to accept connections.
	 */
	//private ServerSocket mServerSocket;

	/**
	 * Listener object whose onClientAccepted() method's will be called,
	 * every time a client connects.
	 */
	private OnClientAcceptedListener mListener;

	/**
	 * Create a runnable listening on a given port.
	 * 
	 * @param port The port to listen on.
	 * @param listener (must not be null)
	 */
	public ClientAccepter(int port, OnClientAcceptedListener listener) {
		mPort = port;
		
		// Verify immediately that the listener is not null
		if(listener == null)
			throw new NullPointerException("The listener (interface ClientAccepter.OnClientAcceptedListener) must not be null");
		
		mListener = listener;
		Log.i("ClientAccepter", "initialisation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		Log.i("ClientAccepter", "running");
		
		try {
			// create a ServerSocket and start accepting.
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(mPort));
			Log.i("ClientAccepter", "Server socket created on port:"+mPort);
			
			// Define a non-infinite accept timeout, it will permit us to stop
			// this thread whenever we want with Thread#interrupt()
			serverSocket.setSoTimeout(1000);
			
			boolean continueAccepting = true;

			Log.i("ClientAccepter", "Listening on port:"+mPort);
			while (continueAccepting) {
				try {
					Socket socket = serverSocket.accept();
					socket.setTcpNoDelay(true);
					Log.i("ClientAccepter", "client accepted on port:"+mPort);
					continueAccepting = mListener.onClientAccepted(socket, mPort);
				} catch(InterruptedIOException e) {
					// Accept timeout
					// Stop accepting if this thread has a pending interrupt request (sent with Thread#interrupt())
					if(isInterrupted())
						continueAccepting = false;
				}
			}
		}
		catch (SocketException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("ClientAccepter", "Shuting down");
		// Don't forget to close the sockets.
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.e("ClientAccepter", "Error closing socket", e);
			}
		}
	}
}