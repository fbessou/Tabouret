/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

/**
 * @author Frank Bessou
 *
 */
public class StringSender extends Thread {
	public Socket socket; // Socket to send data to
	private LinkedBlockingQueue<String> mStrings = new LinkedBlockingQueue<String>();

	/**
	 * 
	 */
	public StringSender(Socket socket) {
		this.socket=socket;
	}
	
	@Override
	public void run() {
		try {
			OutputStream stream = socket.getOutputStream();
			String s = null;
			
			while (!(s=mStrings.take()).equals("eos")) {
				Log.i("StringSender","Sending "+s);
				stream.write(s.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void send(String string){
		mStrings.add(string+"\n");
	}
	
}
