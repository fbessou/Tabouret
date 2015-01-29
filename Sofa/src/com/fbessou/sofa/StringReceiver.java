
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
 */
public class StringReceiver implements Runnable {
	public Socket socket; // Socket to receive from
	public interface Listener{
		void onStringReceived(String s);
	}
	Listener mListener=null;
	/**
	 * 
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
					mListener.onStringReceived(s);
				}
				else
					Log.d("StringReceiver",s);
			}

			
		} catch (IOException e) {
			Log.d("StringReceiver","Connection closed");
		}
		if(mListener!=null){
			mListener.onStringReceived(null);
		}
	}
	
	public void setListener(Listener listener){
		mListener=listener;
	}
	
	Listener getListener(){
		return mListener;
	}
}
