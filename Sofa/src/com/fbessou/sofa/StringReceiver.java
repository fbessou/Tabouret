
package com.fbessou.sofa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import android.util.Log;

/**
 * @author Frank Bessou
 *
 * This class uses a socket to listen on. Strings are read line by line.
 * The distant application must provide Strings separated by newline character.
 * 
 * If the StringReceiver is linked to a listener, the listener's onStringReceived() method
 * will be called on reception.
 */
public class StringReceiver extends Thread {
	public Socket mSocket; // Socket to receive from FIXME why public ?
	
	/**
	 * 
	 * @author Frank Bessou
	 *
	 */
	public interface Listener{
		void onStringReceived(String s, Socket socket);
		void onClosed(Socket socket);
	}
	
	/**
	 * 
	 */
	private Listener mListener = null;
	
	/**
	 * Construct a StringReceiver instance which will listen on the socket
	 * 
	 * @param socket
	 * 				The socket to listen on.
	 */
	public StringReceiver(Socket socket) {
		this.mSocket = socket;
	}
	
	@Override
	public void run() {
		try {
			// Use the sockect as an input stream
			InputStream stream = mSocket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));//FIXME Buffered = delayed ?
			String s = null;

			// Read lines while EOF is not reached or the thread is not interrupted
			while ((s = reader.readLine()) != null && !this.isInterrupted()) {
				if(mListener != null)
					mListener.onStringReceived(s, mSocket);
				else
					Log.d("StringReceiver", s);
			}
			
		} catch (IOException e) {
			Log.d("StringReceiver","Connection closed");
		}
		
		if(mListener != null) //Obvious FIXME really ? even with the method shutdown() ?
			mListener.onClosed(mSocket);
	}
	
	/** Shutdowns the input stream of the sockect and consequently ends this thread.
	 * The listener's method #onClosed() should be called soon. **/
	public void shutdown() {
		if(this.isAlive()) {
			// Shutdowns the input stream (-> EOF)
			try {
				mSocket.shutdownInput();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Interrupts this thread
			this.interrupt();
		}
	}
	
	public void setListener(Listener listener){
		mListener = listener;
	}
	
	Listener getListener(){
		return mListener;
	}
}
