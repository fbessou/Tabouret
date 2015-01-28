package com.fbessou.sofa;

import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.fbessou.sofa.InputEvent;
import com.fbessou.sofa.StringReceiver;

public class GameMessageReceiver implements StringReceiver.Listener {
	private PadEventListener mPadEventListener;
	private InputEventListener mInputEventListener;
	
	private LinkedBlockingQueue<InputEvent> mInputEventQueue = new LinkedBlockingQueue<>();
	//private LinkedBlockingQueue<PadEvent> mPadEventQueue = new LinkedBlockingQueue<>();
	
	public void setGamePadEventListener(PadEventListener listener) {
		mPadEventListener = listener;
	}
	public void setInputEventListener(InputEventListener listener) {
		mInputEventListener = listener;
	}
	/* TODO PadEvent */void pollPadEvent() {
		
	}
	public InputEvent pollInputEvent() {
		if(mInputEventQueue.isEmpty())
			return null;
		
		return mInputEventQueue.poll();
	}
	
	@Override
	public void onStringReceived(String s) {
		try {
			JSONObject jo = new JSONObject(s);
			switch(jo.getString("type")) {
			case "padevent":
				if(mPadEventListener != null)
					mPadEventListener.onPadEvent(/* TODO */);//new InputEvent(jo.getJSONObject("event")));
				// TODO else
				//	mPadEventQueue.add(e);
				break;
			case "inputevent":
				InputEvent e = new InputEvent(jo.getJSONObject("event"));
				if(mInputEventListener != null)
					mInputEventListener.onInputEvent(e);
				else
					mInputEventQueue.add(e);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("###", "JSONException on "+s);
		}
	}
	
	public interface InputEventListener {
		public void onInputEvent(InputEvent event);
	}
	public interface PadEventListener {
		public void onPadEvent(/*TODO class GamePadEvent*/);
	}
}
