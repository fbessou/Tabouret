package com.fbessou.sofa;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;
@Deprecated
public class GameMessageReceiver implements StringReceiver.Listener {
	private PadEventListener mPadEventListener;
	private InputEventListener mInputEventListener;
	
	private LinkedBlockingQueue<InputEvent> mInputEventQueue = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<PadEvent> mPadEventQueue = new LinkedBlockingQueue<>();
	
	public void setGamePadEventListener(PadEventListener listener) {
		mPadEventListener = listener;
	}
	public void setInputEventListener(InputEventListener listener) {
		mInputEventListener = listener;
	}
	
	public PadEvent pollPadEvent() {
		//FIXME Is this really needed
		if(mPadEventQueue.isEmpty())
			return null;
		return mPadEventQueue.poll();
		
	}
	public InputEvent pollInputEvent() {
		// FIXME same as in pollPadEvent
		if(mInputEventQueue.isEmpty())
			return null;
		
		return mInputEventQueue.poll();
	}
	
	@Override
	public void onStringReceived(String s, Socket socket) {

		try {
			JSONObject jo = new JSONObject(s);
			switch(jo.getString("type")) {
			case "padevent":
				PadEvent padEvt = new PadEvent(jo.getJSONObject("event"));
				if(mPadEventListener != null)
					mPadEventListener.onPadEvent(padEvt);//new InputEvent(jo.getJSONObject("event")));
				else
					mPadEventQueue.add(padEvt);
				break;
			case "inputevent":
				InputEvent inputEvt = new InputEvent(jo.getJSONObject("event"));
				if(mInputEventListener != null)
					mInputEventListener.onInputEvent(inputEvt);
				else
					mInputEventQueue.add(inputEvt);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//Log.d("###", "JSONException on "+s);
		}
	}
	
	public interface InputEventListener {
		public void onInputEvent(InputEvent event);
	}
	
	public interface PadEventListener {
		public void onPadEvent(PadEvent event);
	}
	/* (non-Javadoc)
	 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
	 */
	@Override
	public void onClosed(Socket socket) {
		
	}
}
