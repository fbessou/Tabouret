/**
 * 
 */
package com.fbessou.sofa;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.SparseArray;

import com.fbessou.sofa.ClientAccepter.OnClientAcceptedListener;
import com.fbessou.sofa.ConnectionWatcher.OnDelayPassedListener;
import com.fbessou.sofa.message.GameAcceptMessage;
import com.fbessou.sofa.message.GameCustomMessage;
import com.fbessou.sofa.message.GameJoinMessage;
import com.fbessou.sofa.message.GameLeaveMessage;
import com.fbessou.sofa.message.GameOutputEventMessage;
import com.fbessou.sofa.message.GamePadCustomMessage;
import com.fbessou.sofa.message.GamePadInputEventMessage;
import com.fbessou.sofa.message.GamePadJoinMessage;
import com.fbessou.sofa.message.GamePadLeaveMessage;
import com.fbessou.sofa.message.GamePadPingMessage;
import com.fbessou.sofa.message.GamePadRenameMessage;
import com.fbessou.sofa.message.GamePingMessage;
import com.fbessou.sofa.message.GameRejectMessage;
import com.fbessou.sofa.message.GameRenameMessage;
import com.fbessou.sofa.message.Message;
import com.fbessou.sofa.message.ProxyGameAcceptMessage;
import com.fbessou.sofa.message.ProxyGameCustomMessage;
import com.fbessou.sofa.message.ProxyGameJoinMessage;
import com.fbessou.sofa.message.ProxyGameLeaveMessage;
import com.fbessou.sofa.message.ProxyGameOutputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadCustomMessage;
import com.fbessou.sofa.message.ProxyGamePadInputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadJoinMessage;
import com.fbessou.sofa.message.ProxyGamePadLeaveMessage;
import com.fbessou.sofa.message.ProxyGamePadLostMessage;
import com.fbessou.sofa.message.ProxyGamePadPingMessage;
import com.fbessou.sofa.message.ProxyGamePadPongMessage;
import com.fbessou.sofa.message.ProxyGamePadRenameMessage;
import com.fbessou.sofa.message.ProxyGamePingMessage;
import com.fbessou.sofa.message.ProxyGamePongMessage;
import com.fbessou.sofa.message.ProxyGameRejectMessage;
import com.fbessou.sofa.message.ProxyGameRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;

/**
 * This service provide an implementation for a basic bidirectional JsonOverTCP
 * proxy. This service provides two entry points : for the game server and for the game-pads.
 * It associates a client UUID to a player id and it permits client to disconnect and reconnect
 * without changing its player ID.
 * FIXME How to stop this service ?
 * TODO -> Define auto-stop trigger
 * 				example:
 * 					no message received during the last X seconds
 * 					Wifi turned off (I'm speaking about wifi not wifiP2P)
 * 					GameActivity destroyed (if proxy is running on the game owner device)
 * 					... Any other idea?
 * 
 * @author Frank Bessou
 *
 */
public class GameIOProxy extends Service implements OnClientAcceptedListener {
	
	private static final long MaxMuteDuration = 4500, AlertMuteDuration = 3500;
	
	/**
	 * List containing all the blocked GamePads. A game-pad is blocked if the
	 * following cases: - There is no game connected to this proxy - The game
	 * doesn't allow more connections. TODO - The game-pad has not said "hello".
	 * 
	 * When a game-pad is blocked, all messages sent through this proxy will not
	 * be transmitted to the game.
	 */
	private ArrayList<Socket> mBlockedGamePads;

	/**
	 * Array which associates player SId to UUID. The index in the array represents the
	 * Id. Here, the number of player is limited to 16.
	 * This array can be use to know if a game-pad ID is taken or not. 
	 */
	private UUID[] mPadIdToUUID = new UUID[MAX_REGISTERED_GAMEPAD_COUNT];
	private static final int MAX_REGISTERED_GAMEPAD_COUNT = 16;
	/**
	 * The last ID given to the last game-pad. When we need to find a new ID,
	 * we start the search from this ID. Thus, we favour the recovering ability.
	 */
	private int mLastGivenID = 0;

	/**
	 * Map that associates a client UUID to its corresponding ID in the game.
	 * The UUID is only used for recovery, when a client disconnect and want to
	 * reconnect with the same "player" ID.
	 * So, this hashmap does not contain only the registered game-pad, but it also
	 * contains the old game-pads until their ID is taken by an other game-pad.
	 */
	private HashMap<UUID, Integer> mUUIDToPadId;

	/**
	 * Map that associates a Socket to a client padId
	 */
	private SparseArray<GamePadConnection> mGamePads;

	/**
	 * Port the proxy is listening on for game connection
	 */
	public static final int DefaultGamePort = 6969;
	private ClientAccepter mGameAccepter;

	/**
	 * 
	 */
	private GameConnection mGameConnection = null;
	/**
	 * Port the proxy is listening on for clients connection
	 */
	public static final int DefaultGamePadsPort = 9696;
	private ClientAccepter mGamepadAccepter;

	/**
	 * Constructor, create an empty list of clients
	 */
	public GameIOProxy() {
		mUUIDToPadId = new HashMap<UUID, Integer>();
		mGamePads = new SparseArray<GamePadConnection>();
		mBlockedGamePads = new ArrayList<Socket>();

		Log.i("GameIOProxy", "initialisation");
	}

	/**
	 * Called whenever a client connect to this proxy
	 * 
	 * @param socket
	 *            The client's socket.
	 * @param port
	 *            The port the client connected to.
	 */
	public boolean onClientAccepted(Socket socket, int port) {
		if (port == DefaultGamePort) {
			Log.i("GameIOProxy", "game accepted from port "+DefaultGamePort);
			registerGame(socket);
		} else if (port == DefaultGamePadsPort) {
			Log.i("GameIOProxy", "game pad accepted from port "+DefaultGamePadsPort);
			registerGamePad(socket);
		} else {
			Log.w("GameIOProxy", "Client accepted from unknown port "+port);
		}
		return true;
	}

	/**
	 * Adds a socket to the list of connected GamePads. This method is called
	 * when a GamePad connects to this proxy on the port designated by
	 * mGamePadsPort.
	 * 
	 * @param clientSocket
	 */
	private void registerGamePad(Socket gamepadSocket) {
		mBlockedGamePads.add(gamepadSocket);
		new GamePadConnection(gamepadSocket);
	}

	/**
	 * Called when a game connects to this proxy on the port designated by
	 * mGamePort.
	 * 
	 * @param gameSocket
	 *            The socket corresponding to the newly connected game.
	 */
	private void registerGame(Socket gameSocket) {
		// If the game socket is already defined don't
		// close newly created socket.
		// FIXME What happens if few games connect? 
		/*
		 * if (mGameSocket != null) { if (gameSocket != null){ try {
		 * gameSocket.close(); Log.i("GameIOProxy","Abort connection"); } catch
		 * (IOException e) { Log.w("GameIOProxy",
		 * "Error on invalid game socket's closing."); } } } else{
		 */ // We are accepting a game
		if (mGameConnection == null || mGameConnection.mSocket == null || !mGameConnection.mSocket.isConnected()) {
			if(mGameConnection != null)
				Log.i("GameIOProxy", "Previous game disconnected, accept a new game");
			mGameConnection = new GameConnection(gameSocket);

			Log.i("GameIOProxy", "A game has registered");
		}
		else
			Log.w("GameIOProxy", "An other game is already registered");
	}

	/**
	 * Search the next free id from the {@code mLastGivenID} to favor the
	 * recovering ability.
	 * @return a free id, -1 if no id available.
	 */
	private int getNewId() {
		int nextId = mLastGivenID;
		// find the next free id
		do {
			nextId++;
			if(nextId >= MAX_REGISTERED_GAMEPAD_COUNT)
				nextId = 0;
		} while(mPadIdToUUID[nextId] != null && nextId != mLastGivenID);
		
		// return -1 if not found
		return mPadIdToUUID[nextId] != null ? -1 : nextId;
	}
	/**
	 * Should be called each time the id given by {@code getNewId()} is
	 * associated to a pad
	 */
	private void setLastGivenId(int id) {
		mLastGivenID = id;
	}
	
	/**
	 * Unregisters all the game-pads. Should be called before close the game-pad
	 * connection. 
	 */
	private void unregisterAllTheGamePads() {
		Log.i("GameIOProxy", "unregistering all the game pads");
		for(int i = mGamePads.size()-1; i >= 0; i--) {
			GamePadConnection gpConnection = mGamePads.valueAt(i);
			if(gpConnection.isRegistered())
				gpConnection.unregister();
		}
	}
	/**
	 * Reject (cancel accept) all the game-pads.
	 */
	private void rejectAllTheGamePads() {
		Log.i("GameIOProxy", "rejecting all the game pads");
		for(int i = mGamePads.size()-1; i >= 0; i--) {
			GamePadConnection gpConnection = mGamePads.valueAt(i);
			if(gpConnection.isAccepted()) {
				sendToGamePad(new ProxyGameRejectMessage(), gpConnection.mPadId);
				gpConnection.reject();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("GameIOProxy", "Creating Sevice, start sender and receiver");
		
		// Start to listen for connections
		mGameAccepter = new ClientAccepter(DefaultGamePort, this);
		mGamepadAccepter = new ClientAccepter(DefaultGamePadsPort, this);
		
		mGameAccepter.start();
		mGamepadAccepter.start();
		
		// Auto stop this service when the wifi is turned off
		setAutoStopMode(StopTrigger.WIFI_TURNING_OFF);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.i("GameIOProxy", "destroying service");
		
		// Interrupt the client accepter threads
		mGameAccepter.interrupt();
		mGamepadAccepter.interrupt();
		
		// Close game connection
		if(mGameConnection != null) {
			Log.i("GameIOProxy", "Close game socket");
			mGameConnection.disconnect();
		}
		
		// Close unregistered game-pad socket
		Log.i("GameIOProxy", "Close unregistered game-pad socket");
		for(Socket socket : mBlockedGamePads) {
			try {
				socket.close();
			} catch (IOException e) {
				Log.w("GameIOProxy", "error while try to close unregistered game pad socket:"+socket, e);
			}
		}
		
		unregisterAllTheGamePads();
		
		//Close registered game-pad connection
		for(int i = mGamePads.size() - 1; i >= 0; i--) {
			GamePadConnection gpConnection = mGamePads.valueAt(i);
			gpConnection.disconnect();
		}
		
		// Disable Auto stop when the wifi is turned off
		disableAutoStopMode(StopTrigger.WIFI_TURNING_OFF);
		
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Send the given message to the game
	 * @param msg Message to send
	 */
	void sendToGame(ProxyMessage msg) {
		Log.v("GameIOProxy", "sendToGame: "+msg.toString());
		if (mGameConnection != null) {
			mGameConnection.send(msg.toString());
		} else {
			Log.w("GameIOProxy", "Warning : cannot send message to a disconnected game");
		}
	}
	/**
	 * Send the given message to the game pad
	 * @param msg Message to send
	 * @param padId The ID of the game-pad recipient or -1 for broadcast
	 */
	void sendToGamePad(ProxyMessage msg, int padId) {
		Log.v("GameIOProxy", "sendToGamePad id:"+padId+" msg:"+msg.toString());
		if(padId != -1) {
			// Unique recipient
			GamePadConnection gpConnection = mGamePads.get(padId);
			
			if(gpConnection != null)
				gpConnection.send(msg.toString());
			else
				Log.w("GameIOProxy", "Game-pad with id "+padId+" not found. Message dropped.");
		}
		else {
			// Broadcast
			for(int i = mGamePads.size() - 1; i >= 0; i--) {
				GamePadConnection gpConnection = mGamePads.valueAt(i);
				gpConnection.send(msg.toString());
			}
		}
	}

	/**
	 * 
	 * @author Frank Bessou
	 *
	 */
	private class GameConnection extends StringSender implements StringReceiver.Listener, StringSender.Listener, OnDelayPassedListener {
		/**
		 * The socket used to communicate with the game server
		 */
		private Socket mSocket;
		
		/**
		 * 
		 */
		private StringReceiver mReceiver;
		
		private ConnectionWatcher mConnectionWatcher;

		/**
		 * @param socket
		 */
		public GameConnection(Socket socket) {
			super(socket);
			mSocket = socket;
			
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			this.setListener(this);
			mReceiver.start();
			this.start();
			
			mConnectionWatcher = new ConnectionWatcher(AlertMuteDuration, MaxMuteDuration, this);
			mConnectionWatcher.enable();
			
			Log.w("GameIOProxy", "Initialisation. GameConnection: start sender and receiver");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
		 */
		@Override
		public void onStringReceived(String string, Socket socket) {
			Log.v("GameIOProxy", "GameConnection.onStringReceived: "+string+ " from socket:"+socket);
			mConnectionWatcher.notifyTimer();
			
			// Interpret and redirect the message
			try {
				// Read the message
				Message message = Message.gameFromJSON(new JSONObject(string));
				// Prepare to transmit a new message
				ProxyMessage proxyMessage = null;
				// Default recipient is broadcast.
				int recipientID = -1;
				
				// Make a new message according to the received message and define a unique recipient if needed
				switch(message.getType()) {
				case JOIN:
					proxyMessage = new ProxyGameJoinMessage((GameJoinMessage) message);
					break;
				case LEAVE:
					proxyMessage = new ProxyGameLeaveMessage((GameLeaveMessage) message);
					disconnect();
					break;
				case ACCEPT:
					proxyMessage = new ProxyGameAcceptMessage((GameAcceptMessage) message);
					recipientID = ((GameAcceptMessage) message).getGamePadId(); // Unique recipient
					// Mark the game pad as accepted
					mGamePads.get(recipientID).accept();
					break;
				case RENAME:
					proxyMessage = new ProxyGameRenameMessage((GameRenameMessage) message);
					break;
				case OUTPUTEVENT:
					proxyMessage = new ProxyGameOutputEventMessage((GameOutputEventMessage) message);
					recipientID = ((GameOutputEventMessage) message).getGamePadId(); // Unique recipient
					break;
				case INPUTEVENT:
					// The game cannot send Input event message, we ignore this message
					Log.w("GameIOProxy", "GameConnection: "+message.getType()+" received from the game. Message dropped.");
				case PING:
					sendToGame(new ProxyGamePongMessage((GamePingMessage) message));
					break;
				case PONG:
					break;
				case CUSTOM:
					proxyMessage = new ProxyGameCustomMessage((GameCustomMessage) message);
					recipientID = ((GameCustomMessage) message).getGamePadId(); // Unique recipient
					break;
				case REJECT:
					proxyMessage = new ProxyGameRejectMessage((GameRejectMessage) message);
					recipientID = ((GameRejectMessage) message).getGamePadId();
					break;
				case LOST: // Should not occur
					break;
				}
				
				// If a message has been defined, send it
				if(proxyMessage != null) {
					sendToGamePad(proxyMessage, recipientID);
				}
			} 
			catch (Exception e) {
				Log.e("GameIOProxy", "GameConnection.exception: "+string+ " from socket:"+socket, e);
			}
		}

		/* (non-Javadoc)
		 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
		 */
		@Override
		public void onClosed(Socket socket) {
			Log.i("GameIOProxy", "GameConnection.onClosed (from stringReceiver)");
			disconnect();
		}

		/**
		 * Closes this connection. Shutdowns the associated StringSender and StringReceiver.
		 */
		public void disconnect() {
			Log.i("GameIOProxy", "GameConnection: disconnect");
			
			mConnectionWatcher.disable();
			
			if(mSocket != null) {
				try {
					mSocket.close();
				} catch (IOException e) {
					Log.i("GameIOProxy", "GameConnection: error closing socket", e);
					e.printStackTrace();
				}
				mSocket = null;
			}
			
			this.interrupt();
			mReceiver.interrupt();
			
			rejectAllTheGamePads();
		}

		@Override
		public void onAlertDelayPassed() {
			Log.i("GameIOProxy", "GameConnection: Warning! Alert delay passed");
			
			// Send ping to game pad
			sendToGame(new ProxyGamePingMessage());
		}

		@Override
		public void onMaxDelayPassed() {
			Log.i("GameIOProxy", "GameConnection: Max delay passed");
			disconnect();
		}
	}

	private class GamePadConnection extends StringSender implements StringReceiver.Listener, StringSender.Listener, OnDelayPassedListener {
		/** The id associated to this game-pad. Equals
		 * to -1 if the game-pad is not registered */
		private int mPadId = -1;
		
		private Socket mSocket;
		
		private ConnectionWatcher mConnectionWatcher;
		
		// FIXME why is the receiver an attribute? Meanwhile the sender is extended... 
		private StringReceiver mReceiver;
		
		/**
		 * A game pad is accepted by the game if the game send GameAcceptMessage
		 * to the game-pad. If a game-pad is not accepted, it can only send the
		 * GamePadJoinMessage to the game, the other messages are blocked by the
		 * proxy.
		 */
		private boolean mIsAcceptedByGame = false;

		/**
		 * 
		 */
		public GamePadConnection(Socket socket) {
			// Initialize the Sender
			super(socket);
			mSocket = socket;
			
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			this.setListener(this);
			mReceiver.start();
			this.start();
			
			mConnectionWatcher = new ConnectionWatcher(AlertMuteDuration, MaxMuteDuration, this);
			mConnectionWatcher.enable();
			
			Log.i("GameIOProxy", "Initialisation. GamePadConnection: start sender and receiver");
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang
		 * .String)
		 */
		@Override
		public void onStringReceived(String string, Socket socket) {
			Log.v("GameIOProxy", "GamePadConnection.onStringReceived: "+string+" from socket:"+socket);
			
			mConnectionWatcher.notifyTimer();
			
			try {
				// Read the message
				Message message = Message.gamePadFromJSON(new JSONObject(string));
				// Prepare to transmit a new message
				ProxyMessage proxyMessage = null;

				switch(message.getType()) {
				case JOIN:
					// Register if still not done
					if(!isRegistered())
						registerOrRecover(((GamePadJoinMessage) message).getUUID());
					// transmit message if the game-pad has been successfully registered
					if(isRegistered())
						proxyMessage = new ProxyGamePadJoinMessage((GamePadJoinMessage) message, mPadId);
					break;
				case LEAVE:
					if(isRegistered() && isAccepted()) {
						proxyMessage = new ProxyGamePadLeaveMessage((GamePadLeaveMessage) message, mPadId);
						unregister();
					}
					else
						Log.w("GameIOProxy", "GamePadConnection: Leave received from a non-registered/non-accepted game-pad");
					break;
				case RENAME:
					if(isRegistered() && isAccepted())
						proxyMessage = new ProxyGamePadRenameMessage((GamePadRenameMessage) message, mPadId);
					else
						Log.w("GameIOProxy", "GamePadConnection: Rename received from a non-registered game-pad");
					break;
				case INPUTEVENT:
					if(isRegistered() && isAccepted()) {
						proxyMessage = new ProxyGamePadInputEventMessage((GamePadInputEventMessage) message, mPadId);
					}
					else
						Log.w("GameIOProxy", "GamePadConnection: Input event received from a non-registered game-pad");
					break;
				case PING:
					sendToGamePad(new ProxyGamePadPongMessage((GamePadPingMessage) message), mPadId);
					break;
				case PONG:
					break;
				case CUSTOM:
					if(isRegistered() && isAccepted())
						proxyMessage = new ProxyGamePadCustomMessage((GamePadCustomMessage) message, mPadId);
					else
						Log.w("GameIOProxy", "GamePadConnection: Custom message received from a non-registered game-pad");
					break;
				case REJECT: // Should not occur
				case OUTPUTEVENT: // Should not occur
				case ACCEPT: // Should not occur
				case LOST: // Should not occur
					break;
				}

				// If the message has been defined, send it
				if(proxyMessage != null) {
					sendToGame(proxyMessage);
				}
			} catch (Exception e) {
				Log.e("GameIOProxy", "GamePadConnection.onStringReceived error:", e);
			}

		}

		/** Associates the playerId, the UUID and this game-pad.<br>
		 * must be respected : 0 <= playerId < GameIOProxy.mIds.length **/
		void associateIdUUID(int padId, UUID uuid) {
			Log.i("GameIOProxy", "GamePadConnection: Associate id:" + padId + " to UUID:" + uuid);
			
			// Save value in GameIOProxy.this
			mPadIdToUUID[padId] = uuid;
			mGamePads.put(padId, this);
			mUUIDToPadId.put(uuid, padId);
			// remove from the list of unregistered game-pads
			mBlockedGamePads.remove(mSocket);
			
			mPadId = padId;
			setLastGivenId(padId);
		}

		/** Register this game-pad and get a new id. If the given UUID has 
		 * been already registered earlier and if the previous ID is still
		 * free, the game-pad recover its previous ID
		 * @params Game-pad's UUID
		 * @return true if successfully register or recover
		 * */
		boolean registerOrRecover(UUID inUUID) { // priority to the newly connected client
			Log.i("GameIOProxy", "GamePadConnection.registerOrRecover UUID:"+inUUID);
			// Try to recover if possible
			if(mUUIDToPadId.containsKey(inUUID)) {
				int previousID = mUUIDToPadId.get(inUUID);
				// Check if this previous id is free
				if(mPadIdToUUID[previousID] == null) {
					// The previous id is free, we can recover
					Log.i("GameIOProxy", "GamePadConnection: recovering id:"+previousID);
					associateIdUUID(previousID, inUUID);
					return true;
				} else {
					Log.i("GameIOProxy", "GamePadConnection: cannot recover id:"+previousID);
				}
			}
			
			// Find a free id
			int freeId = getNewId();
			if(freeId != -1) {
				Log.i("GameIOProxy", "GamePadConnection: get new free id:"+freeId);
				// free id available, we can register
				associateIdUUID(freeId, inUUID);
				return true;
			}
			
			// cannot recover nor register
			Log.w("GameIOProxy", "GamePadConnection: Cannot find free id for UUID:" + inUUID);
			return false;
		}
		
		/**
		 * Removes the game-pad from the registered game-pad lists.<br>
		 * We do not remove the pad id from the hash map {@code mUUIDToPadId}.
		 * It may be used to recover later.
		 */
		void unregister() {
			Log.i("GameIOProxy", "GamePadConnection: unregister game pad id:" + mPadId);
			// Remove from the list of registered game-pads
			mGamePads.remove(mPadId);
			mPadIdToUUID[mPadId] = null;
			// Go back to the list of unregistered game-pads
			mBlockedGamePads.add(mSocket);
			
			mPadId = -1;
			mIsAcceptedByGame = false;
		}
		
		/**
		 * Indicates if this game-pad has been registered
		 */
		public boolean isRegistered() {
			return mPadId != -1;
		}
		
		/* (non-Javadoc)
		 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
		 */
		@Override
		public void onClosed(Socket socket) {
			Log.i("GameIOProxy", "GamePadConnection.onClosed (from stringReceiver)");
			// if the game-pad is still registered, it means that the
			// game-pad has disconnected unexpectedly
			if(isRegistered()) {
				sendToGame(new ProxyGamePadLostMessage(mPadId));
				unregister();
			}
			disconnect();
		}

		/**
		 * Mark this game-pad as accepted by the game
		 */
		public void accept() {
			Log.i("GameIOProxy", "GamePadConnection: id:"+mPadId+" accepted by the game");
			mIsAcceptedByGame = true;
		}
		/**
		 * Mark this game-pad as not accepted by the game
		 */
		public void reject() {
			Log.i("GameIOProxy", "GamePadConnection: id:"+mPadId+" rejected by the game");
			mIsAcceptedByGame = false;
		}
		
		public boolean isAccepted() {
			return mIsAcceptedByGame;
		}
		
		/**
		 * Closes this connection by closing the associated socket. Should called
		 * {@code unregister()} before this, otherwise it will be considered as
		 * an unexpected disconnection.
		 */
		public void disconnect() {
			Log.i("GameIOProxy", "GamePadConnection: closing socket");
			mConnectionWatcher.disable();
			
			if(mSocket != null) {
				try {
					mSocket.close();
				} catch (IOException e) {
					Log.e("GameIOProxy", "GamePadConnection: error closing socket", e);
				}
			}
			
			mSocket = null;
			
			this.interrupt();
			mReceiver.interrupt();
		}


		@Override
		public void onAlertDelayPassed() {
			Log.i("GameIOProxy", "GamePadConnection: Warning! Alert delay passed");
			
			// Send ping to game pad
			sendToGamePad(new ProxyGamePadPingMessage(), mPadId);
		}

		@Override
		public void onMaxDelayPassed() {
			Log.i("GameIOProxy", "GamePadConnection: Max delay passed");
			if(isRegistered()) {
				sendToGame(new ProxyGamePadLostMessage(mPadId));
				unregister();
			}
			disconnect();
		}
	}// Class GamePadConnection

	StopperBroadcastReceiver mStopperBroadcastReceiver;
	/** Make this service stop when the Wifi is turned off **/
	void setAutoStopMode(StopTrigger trigger) {

		Log.i("GameIOProxy", "Auto stop mode sets to "+trigger);
		switch (trigger) {
		case WIFI_TURNING_OFF:
		case WIFI_TURNED_OFF:
			mStopperBroadcastReceiver = new StopperBroadcastReceiver(trigger);
			break;
		case NEVER:
		default:
			// never stop this service :(
			break;
		}
		
	}
	void disableAutoStopMode(StopTrigger trigger) {

		Log.i("GameIOProxy", "Disabling auto stop mode set to "+trigger);
		switch (trigger) {
		case WIFI_TURNING_OFF:
		case WIFI_TURNED_OFF:
			mStopperBroadcastReceiver.unregister();
			break;
		case NEVER:
		default:
			// never stop this service :(
			break;
		}
		
	}
	enum StopTrigger {WIFI_TURNED_OFF, WIFI_TURNING_OFF, NEVER};
	class StopperBroadcastReceiver extends BroadcastReceiver {
		StopTrigger trigger;
		public StopperBroadcastReceiver(StopTrigger trigger) {
			this.trigger = trigger;
			IntentFilter filter;
			switch(trigger) {
			default:
			case WIFI_TURNING_OFF:
			case WIFI_TURNED_OFF:
				filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
				break;
			}
			Log.i("GameIOProxy", "registerReceiver for trigger:"+trigger+"on broadcastReceiver");
			registerReceiver(this, filter);
		}
		public void unregister() {
			unregisterReceiver(this);
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				// Wifi turned off
				if(trigger == StopTrigger.WIFI_TURNED_OFF && state == WifiManager.WIFI_STATE_DISABLED
						|| trigger == StopTrigger.WIFI_TURNING_OFF && state == WifiManager.WIFI_STATE_DISABLING) {
					Log.i("GameIOProxy", "Wifi state changed to disabled, stopping the service");
					// Stop the service
					GameIOProxy.this.stopSelf();
				}
			}
		}
		
	}
}
