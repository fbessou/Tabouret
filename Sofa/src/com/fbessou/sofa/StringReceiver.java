
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
 * 
 * This thread should be automatically closed if the socket is closed or disconnected.
 * It is not the job of the StringReceiver to close the socket.
 */
public class StringReceiver extends Thread {
	private Socket mSocket; // Socket to receive from
	
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String s = null;

			// Read lines while EOF is not reached or the thread is not interrupted
			while ((s = readNextLine(reader)) != null) {
				if(mListener != null)
					mListener.onStringReceived(s, mSocket);
				else
					Log.d("StringReceiver", s);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Exception thrown in readNextLine after shutdown() is called
		}
		
		if(mListener != null) //Obvious FIXME really? it will permit to the gameIOProxy to handle unexpected disconnection!
			mListener.onClosed(mSocket);
	}
	
	/**
	 * Returns the next line of the BufferedReader or null if the socket is closed.
	 * 
	 * @param reader
	 * @return string message or null
	 * @throws InterruptedException 
	 * @throws IOException @see {@link BufferedReader#readLine()}
	 */
	private String readNextLine(BufferedReader reader) throws InterruptedException, IOException {
		// Wait for data
		while(!reader.ready()) {
			// Sleep 500µs
			Thread.sleep(0, 500*1000);
			
			// return null is the socket is no longer connected or if it is shutdown
			if(!mSocket.isConnected() || mSocket.isInputShutdown())
				return null;
		}
		
		return reader.readLine();
	}
	
	/**
	 * Interrupts properly this receiver. This method do not have to be called to close this receiver since
	 * this receiver should be automatically closed when its attached socket is closed.
	 * @see Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		super.interrupt();
	}
	
	public void setListener(Listener listener){
		mListener = listener;
	}
	
	Listener getListener(){
		return mListener;
	}
}
