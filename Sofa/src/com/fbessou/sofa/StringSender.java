/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Frank Bessou
 * 
 * This thread should be automatically closed if the socket is closed or disconnected.
 * It is not the job of the StringSender to close the socket.
 * 
 */
public class StringSender extends Thread {
	
	private Socket mSocket; // Socket to send data to
	/** The queue of message to send **/
	private LinkedBlockingQueue<String> mStrings = new LinkedBlockingQueue<String>();

	/**
	 * 
	 */
	public StringSender(Socket socket) {
		this.mSocket = socket;
	}
	
	@Override
	public void run() {
		try {
			OutputStream stream = mSocket.getOutputStream();
			String s = null;
			
			while ((s = takeMessageFromQueue()) != null) {
				//Log.i("StringSender", "Sending "+s);
				
				/** do not forget the '\n' character, it is the delimiter
				 * for <code>com.fbessou.sofa.StringReceiver</code> **/
				stream.write((s+"\n").getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}  catch (InterruptedException e) {
			// Exception thrown by LinkedBlockingQueue.pool(delay) after shutdown() is called
		}
	}
	
	/**
	 * Returns the next message of the queue or null is the socket is closed.
	 * @return String to send or null if socket is closed
	 * @throws InterruptedException
	 */
	private String takeMessageFromQueue() throws InterruptedException {
		while(true) {
			// Wait for a string. Throws InterruptedException.
			String s = mStrings.poll(500, TimeUnit.MICROSECONDS);
			if(s != null)
				return s;
			
			// return null is the socket is no longer connected or if it is shutdown
			if(!mSocket.isConnected() || mSocket.isOutputShutdown())
				return null;
		}
	}

	/**
	 * Interrupts properly this sender. This method do not have to be called to close this sender since
	 * this sender should be automatically closed when its attached socket is closed.
	 * @see Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		super.interrupt();
	}
	
	/**
	 * Sends a message through this Sender. You can send {@code StringSender.EOS} to end this sender.
	 * Drops the message if the sender is not running.
	 * @param string String to send. 
	 */
	public void send(String string){
		if(isAlive() && !isInterrupted())
			mStrings.add(string);
		else {
			// Log.d("StringSender", "Sender not running, message dropped");
		}
	}
	
	/**
	 * Removes all the messages from the buffer
	 */
	public void clearBufferedMessage() {
		mStrings.clear();
	}
}
