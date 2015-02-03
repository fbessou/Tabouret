
package com.fbessou.sofa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

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
	public Socket socket; // Socket to receive from
	
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
	private Listener mListener=null;
	
	/**
	 * Construct a StringReceiver instance which will listen on the socket
	 * 
	 * @param socket
	 * 				The socket to listen on.
	 */
	public StringReceiver(Socket socket) {
		this.socket=socket;
	}
	
	@Override
	public void run() {
		try {
			InputStream stream = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String s = null;

			while ((s = reader.readLine()) != null) {
				if(mListener!=null){
					mListener.onStringReceived(s,socket);
				}
				else
					Log.d("StringReceiver",s);
			}

			
		} catch (IOException e) {
			Log.d("StringReceiver","Connection closed");
		}
		
		if(mListener!=null){ //Obvious
			mListener.onClosed(socket);
		}
	}
	
	public void setListener(Listener listener){
		mListener=listener;
	}
	
	Listener getListener(){
		return mListener;
	}
}
