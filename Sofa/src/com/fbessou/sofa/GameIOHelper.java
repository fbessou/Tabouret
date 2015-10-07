package com.fbessou.sofa;


import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.os.Handler;
import android.util.SparseArray;

import com.fbessou.sofa.GameIOClient.GamePadMessageListener;
import com.fbessou.sofa.GameIOClient.ConnectionStateChangedListener;
import com.fbessou.sofa.GameIOHelper.GamePadStateChangedEvent.Type;
import com.fbessou.sofa.message.GameRejectMessage;

public class GameIOHelper {
	GameIOClient mGameIO;
	Activity mActivity;
	
	/** Array of connected game pad  **/
	private SparseArray<GamePadInGameInformation> mGamePads = new SparseArray<>();
	/** Array of disconnected game pad (previously connected) **/
	private SparseArray<GamePadInGameInformation> mDisconnectedGamePads = new SparseArray<>();
	/** Number max of game pad allowed in the game **/
	private int maxGamePadCount = 16;

	/** Game information **/
	GameInformation mGameInfo;
	
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
	public GameIOHelper(Activity activity, GameInformation info) {
		mActivity = activity;
		mGameInfo = info;
		
		mode = Mode.QUEUE;
		mInputEventQueue = new LinkedBlockingQueue<>();
		mStateEventQueue = new LinkedBlockingQueue<>();
	}
	public GamePadInputEvent pollInputEvent() {
		return mInputEventQueue.poll();
	}
	public GamePadStateChangedEvent pollStateChangedEvent() {
		return mStateEventQueue.poll();
	}

	/** Handler to post runnable in the main GUI thread **/
	Handler mGUIHandler;
	ConnectionStateChangedListener mConnectionListener;
	
	/** LISTENER MODE: use listener interfaces. methods of listener run in the same thread this constructor is called **/
	public GameIOHelper(Activity activity, GameInformation info, InputEventListener iel, StateChangedEventListener scel) {
		mActivity = activity;
		mGameInfo = info;
		
		mode = Mode.LISTENER;
		mStateChangedEventListener = scel;
		mInputEventListener = iel;
		handler = new Handler();
	}
	
	public void start(ConnectionStateChangedListener connectionListener) {
		mGameIO = GameIOClient.getGameIOClient(mActivity, mGameInfo);
		mGameIO.setGamePadMessageListener(new GamePadMessage());
		mGameIO.setOnConnectionStateChangedListener(new Connection());
		mGUIHandler = new Handler(mActivity.getMainLooper());
		mConnectionListener = connectionListener;
	}
	public boolean isConnected() {
		return mGameIO != null && mGameIO.isConnected();
	}

	/** Send the same output event to every connected game pad **/
	public void sendOutputEventBroadcast(OutputEvent event) {
		sendOutputEvent(event, -1);
	}
	/** Send an output event to the game pad **/
	public void sendOutputEvent(OutputEvent event, int gamepadId) {
		if(mGameIO == null)
			return;
		
		if(gamepadId != -1 && isGamePadConnected(gamepadId)) {
			//Log.w("GameIOHandler", "Cannot send output event: game pad id "+gamepadId+" unknown");
			return;
		}
		mGameIO.sendOutputEvent(event, gamepadId);
	}
	/** Update the game informations and share them **/
	public void updateGameInformation(GameInformation info) {
		mGameInfo = info;
		if(mGameIO != null) {
			mGameIO.updateGameInfo(mGameInfo);
		}
	}
	/** Reject the game pad. It will not be accessible anymore. **/
	public void rejectGamePad(int gamepadId) {
		if(mGameIO == null)
			return;
		
		if(gamepadId != -1 && mGamePads.get(gamepadId) == null) {
			//Log.w("GameIOHandler", "Cannot reject: game pad id "+gamepadId+" unknown");
			return;
		}
		mGameIO.sendMessage(new GameRejectMessage(gamepadId));
	}
	
	/** Returns the number of connected game-pads*/
	public int getGamePadCount() {
		return mGamePads.size();
	}
	/** Returns a list of the ID of the connected game-pads*/
	public ArrayList<Integer> getGamePadIds(int index) {
		ArrayList<Integer> keys = new ArrayList<Integer>();
		
		for(int i = 0; i < mGamePads.size(); i++)
			keys.add(mGamePads.keyAt(i));
		
		return keys;
	}
	/** Returns the information of the game-pad. Returns null if the id is unknown. **/
	public GamePadInGameInformation getGamePadInformationId(int id) {
		if(mGamePads.get(id) != null)
			return mGamePads.get(id);
		else if(mDisconnectedGamePads.get(id) != null)
			return mDisconnectedGamePads.get(id);
		else
			return null;
	}
	/** Returns true if the given id refers to a connected game-pad **/
	public boolean isGamePadConnected(int gamepadId) {
		return mGamePads.get(gamepadId) != null;
	}
	
	/** Interface GamePadMessageListener **/
	private class GamePadMessage implements GamePadMessageListener {
		@Override
		public void onGamePadInputEventReceived(InputEvent event, int gamepad) {
			if(isGamePadConnected(gamepad))
				return;
			
			final GamePadInputEvent gpEvent = new GamePadInputEvent();
			gpEvent.event = event;
			gpEvent.gamePadId = gamepad;
			
			if(mode == Mode.LISTENER) {
				if(mInputEventListener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mInputEventListener.onInputEvent(gpEvent);
						}
					});
				}
			} else {
				mInputEventQueue.offer(gpEvent);
			}
		}
		/** Interface GamePadMessageListener **/
		@Override
		public void onGamePadRenamed(String newNickname, int gamepad) {
			if(isGamePadConnected(gamepad))
				return;
			
			final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
			gpEvent.eventType = Type.INFORMATION;
			gpEvent.gamePadId = gamepad;
			gpEvent.newInformation = mGamePads.get(gamepad).staticInformations;
			gpEvent.newInformation.setNickname(newNickname);
			if(mode == Mode.LISTENER) {
				if(mStateChangedEventListener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mStateChangedEventListener.onPadEvent(gpEvent);
						}
					});
				}
			} else {
				mStateEventQueue.offer(gpEvent);
			}
		}
		/** Interface GamePadMessageListener **/
		@Override
		public void onGamePadLeft(int gamepad) {
			if(isGamePadConnected(gamepad))
				return;
			
			final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
			gpEvent.eventType = Type.LEFT;
			gpEvent.gamePadId = gamepad;
			
			// Move the game-pad to the list of disconnected game-pads
			mDisconnectedGamePads.put(gamepad, mGamePads.get(gamepad));
			mGamePads.delete(gamepad);
			
			if(mode == Mode.LISTENER) {
				if(mStateChangedEventListener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mStateChangedEventListener.onPadEvent(gpEvent);
						}
					});
				}
			} else {
				mStateEventQueue.offer(gpEvent);
			}
		}
		/** Interface GamePadMessageListener **/
		@Override
		public boolean onGamePadJoined(String nickName, int gamepad) {
			if(getGamePadCount() < maxGamePadCount) {
				final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
				gpEvent.eventType = Type.JOINED;
				gpEvent.gamePadId = gamepad;
				// Add the game pad to the list
				mGamePads.put(gamepad, new GamePadInGameInformation());
				mGamePads.get(gamepad).staticInformations = new GamePadInformation(nickName, null);
				
				if(mDisconnectedGamePads.get(gamepad) != null)
					mDisconnectedGamePads.delete(gamepad);
				
				if(mode == Mode.LISTENER) {
					if(mStateChangedEventListener != null) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								mStateChangedEventListener.onPadEvent(gpEvent);
							}
						});
					}
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
			if(isGamePadConnected(gamepad))
				return;
			
			final GamePadStateChangedEvent gpEvent = new GamePadStateChangedEvent();
			gpEvent.eventType = Type.UNEXPECTEDLY_DISCONNECTED;
			gpEvent.gamePadId = gamepad;
			
			// Move the game-pad to the list of disconnected game-pads
			mDisconnectedGamePads.put(gamepad, mGamePads.get(gamepad));
			mGamePads.delete(gamepad);
			
			if(mode == Mode.LISTENER) {
				if(mStateChangedEventListener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mStateChangedEventListener.onPadEvent(gpEvent);
						}
				});
				}
			} else {
				mStateEventQueue.offer(gpEvent);
			}
		}
	}
	
	/** Interface OnConnectionStateChangedListener **/
	private class Connection implements ConnectionStateChangedListener {
		@Override
		public void onConnected() {
			mGUIHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mConnectionListener != null)
						mConnectionListener.onConnected();
				}
			});
		}
		@Override
		public void onDisconnected() {
			mGUIHandler.post(new Runnable() {
				@Override
				public void run() {
					if(mConnectionListener != null)
						mConnectionListener.onDisconnected();
				}
			});
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
