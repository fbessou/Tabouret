package com.fbessou.sofa;


import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.os.Handler;
import android.util.SparseArray;

import com.fbessou.sofa.GameIOClient.GamePadMessageListener;
import com.fbessou.sofa.GameIOHandler.GamePadStateChangedEvent.Type;

public class GameIOHandler implements GamePadMessageListener {
	GameIOClient mGameIO;
	
	/** Array of connected game pad  **/
	private SparseArray<GamePadInGameInformation> mGamePads;
	/** Number max of game pad allowed in the game **/
	private int maxGamePadCount = 16;
	
	private enum Mode {QUEUE, LISTENER};
	private final Mode mode;

	/** Queues for mode QUEUE **/
	/** Queue of input event received from game pad **/
	private LinkedBlockingQueue<GamePadInputEvent> mInputEventQueue;
	/** Queue of game pad state changed event (Join/Leave) **/
	private LinkedBlockingQueue<GamePadStateChangedEvent> mStateEventQueue;
	
	/** Listeners for mode LISTENER **/
	private StateChangedEventListener mStateChangedEventListener;
	private InputEventListener mInputEventListener;
	private static Handler handler;
	
	/** QUEUE MODE: use pollEvent methods **/
	public GameIOHandler() {
		mode = Mode.QUEUE;
		mInputEventQueue = new LinkedBlockingQueue<>();
		mStateEventQueue = new LinkedBlockingQueue<>();
	}
	public GamePadInputEvent pollInputEvent() {
		return mInputEventQueue.poll();
	}
	
	/** LISTENER MODE: use listener interfaces. methods of listener run in the same thread this constructor is called **/
	public GameIOHandler(InputEventListener iel, StateChangedEventListener scel) {
		mode = Mode.LISTENER;
		mStateChangedEventListener = scel;
		mInputEventListener = iel;
		handler = new Handler();
	}
	
	public void start(Activity activity, GameInformation info) {
		mGameIO = GameIOClient.getGameIOClient(activity, info);
	}

	public void sendOutputEventBroadcast(OutputEvent event) {
		sendOutputEvent(event, -1);
	}
	public void sendOutputEvent(OutputEvent event, int gamepadId) {
		if(gamepadId != -1 && mGamePads.get(gamepadId) == null) {
			//Log.w("GameIOHandler", "Cannot send output event: game pad id "+gamepadId+" unknown");
			return;
		}
		mGameIO.sendOutputEvent(event, gamepadId);
	}
	public void updateGameInformation(GameInformation info) {
		mGameIO.updateGameInfo(info);
	}
	
	public int getGamePadCount() {
		return mGamePads.size();
	}
	public GamePadInGameInformation getGamePadInformation(int index) {
		return mGamePads.valueAt(index);
	}
	public GamePadInGameInformation getGamePadInformationId(int id) {
		return mGamePads.get(id);
	}
	
	/** Interface GamePadMessageListener **/
	@Override
	public void onGamePadInputEventReceived(InputEvent event, int gamepad) {
		final GamePadInputEvent gpEvent = new GamePadInputEvent();
		gpEvent.event = event;
		gpEvent.gamePadId = gamepad;
		
		if(mode == Mode.LISTENER) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mInputEventListener.onInputEvent(gpEvent);
				}
			});
		} else {
			mInputEventQueue.offer(gpEvent);
		}
	}
	/** Interface GamePadMessageListener **/
	@Override
	public void onGamePadRenamed(String newNickname, int gamepad) {
		final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
		gpEvent.eventType = Type.INFORMATION;
		gpEvent.gamePadId = gamepad;
		gpEvent.newInformation = mGamePads.get(gamepad).staticInformations;
		gpEvent.newInformation.setNickname(newNickname);
		if(mode == Mode.LISTENER) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mStateChangedEventListener.onPadEvent(gpEvent);
				}
			});
		} else {
			mStateEventQueue.offer(gpEvent);
		}
	}
	/** Interface GamePadMessageListener **/
	@Override
	public void onGamePadLeft(int gamepad) {
		final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
		gpEvent.eventType = Type.LEFT;
		gpEvent.gamePadId = gamepad;
		if(mode == Mode.LISTENER) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mStateChangedEventListener.onPadEvent(gpEvent);
				}
			});
		} else {
			mStateEventQueue.offer(gpEvent);
		}
	}
	/** Interface GamePadMessageListener **/
	@Override
	public boolean onGamePadJoined(int gamepad) {
		if(getGamePadCount() < maxGamePadCount) {
			final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
			gpEvent.eventType = Type.JOINED;
			gpEvent.gamePadId = gamepad;
			if(mode == Mode.LISTENER) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						mStateChangedEventListener.onPadEvent(gpEvent);
					}
				});
			} else {
				mStateEventQueue.offer(gpEvent);
			}
			return true;
		}
		// Max count reached
		else {
			return false;
		}
	}
	/** Interface GamePadMessageListener **/
	@Override
	public void onGamePadUnexpectedlyDisconnected(int gamepad) {
		final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
		gpEvent.eventType = Type.UNEXPECTEDLY_DISCONNECTED;
		gpEvent.gamePadId = gamepad;
		if(mode == Mode.LISTENER) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mStateChangedEventListener.onPadEvent(gpEvent);
				}
			});
		} else {
			mStateEventQueue.offer(gpEvent);
		}
	}

	public interface InputEventListener {
		public void onInputEvent(GamePadInputEvent event);
	}
	public interface StateChangedEventListener {
		public void onPadEvent(GamePadStateChangedEvent event);
	}

	public static class GamePadInputEvent {
		public InputEvent event;
		public int gamePadId;
	}
	
	public static class GamePadStateChangedEvent {
		public int gamePadId;
		public enum Type{JOINED, LEFT, UNEXPECTEDLY_DISCONNECTED, INFORMATION};
		public Type eventType;
		/** New information if event type is INFORAMATION **/
		public GamePadInformation newInformation;
	}
	
	public static class GamePadInGameInformation {
		enum State {JOINED, LEFT, UNEXPECTEDLY_DISCONNECTED};
		public GamePadInformation staticInformations;
		public int gamePadId;
		public State state;
	}
}
