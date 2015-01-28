
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
	LinkedBlockingQueue<String> mStrings = new LinkedBlockingQueue<String>();
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
				mStrings.add(s);
				if(mListener!=null){
					mListener.onStringReceived(s);
				}
				Log.d("StringReceiver",s);
			}
			
			mStrings.add("EOF");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setListener(Listener listener){
		mListener=listener;
	}
	
	Listener getListener(){
		return mListener;
	}
}
