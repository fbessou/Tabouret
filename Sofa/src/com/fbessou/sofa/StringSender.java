/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Frank Bessou
 *
 */
public class StringSender extends Thread {
	/** String to send to close the stream **/
	public static final String EOS = "eos";
	
	public Socket socket; // Socket to send data to FIXME why public ?
	/** The queue of message to send **/
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
			
			while (!(s = mStrings.take()).equals(EOS)) {
				//Log.i("StringSender", "Sending "+s);
				
				/** do not forget the '\n' characters, it is the delimiter
				 * for <code>com.fbessou.sofa.StringReceiver</code> **/
				stream.write((s+"\n").getBytes());
			}
			
			socket.shutdownOutput();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** Shutdowns this sender by clearing the message queue and sending the string <code>EOS</code>.<br>
	 * You can manually shutdown without clearing the queue by calling <code>send(EOS)</code>. */
	public void shutdown() {
		// Clear the message queue
		mStrings.clear();
		// Send EOS
		mStrings.add(EOS);
	}
	
	/**
	 * Send a message through this Sender
	 * @param string
	 */
	public void send(String string){
		mStrings.add(string);
	}
	
}
