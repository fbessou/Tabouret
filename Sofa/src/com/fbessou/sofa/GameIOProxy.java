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
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.fbessou.sofa.ClientAccepter.OnClientAcceptedListener;
import com.fbessou.sofa.message.GameAcceptMessage;
import com.fbessou.sofa.message.GameJoinMessage;
import com.fbessou.sofa.message.GameLeaveMessage;
import com.fbessou.sofa.message.GameOutputEventMessage;
import com.fbessou.sofa.message.GamePadInputEventMessage;
import com.fbessou.sofa.message.GamePadJoinMessage;
import com.fbessou.sofa.message.GamePadLeaveMessage;
import com.fbessou.sofa.message.GamePadRenameMessage;
import com.fbessou.sofa.message.GameRenameMessage;
import com.fbessou.sofa.message.Message;
import com.fbessou.sofa.message.ProxyGameAcceptMessage;
import com.fbessou.sofa.message.ProxyGameJoinMessage;
import com.fbessou.sofa.message.ProxyGameLeaveMessage;
import com.fbessou.sofa.message.ProxyGameOutputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadInputEventMessage;
import com.fbessou.sofa.message.ProxyGamePadJoinMessage;
import com.fbessou.sofa.message.ProxyGamePadLeaveMessage;
import com.fbessou.sofa.message.ProxyGamePadRenameMessage;
import com.fbessou.sofa.message.ProxyGameRenameMessage;
import com.fbessou.sofa.message.ProxyMessage;

/**
 * This service provide an implementation for a basic bidirectional JsonOverTCP
 * proxy. This service provides two entry points : for the game server and for the game-pads.
 * It associates a client UUID to a player id and it permits client to disconnect and reconnect
 * without changing its player ID.
 * TODO FIXME How to stop this service ?
 * 
 * @author Frank Bessou
 *
 */
public class GameIOProxy extends Service implements OnClientAcceptedListener {
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
	public static final int GamePort = 6969;
	private ClientAccepter mGameAccepter;

	/**
	 * 
	 */
	private GameConnection mGameConnection = null;
	/**
	 * Port the proxy is listening on for clients connection
	 */
	public static final int GamePadsPort = 9696;
	private ClientAccepter mGamepadAccepter;

	/**
	 * Constructor, create an empty list of clients
	 */
	public GameIOProxy() {
		mUUIDToPadId = new HashMap<UUID, Integer>();
		mGamePads = new SparseArray<GamePadConnection>();
		mBlockedGamePads = new ArrayList<Socket>();
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
		if (port == GamePort) {
			registerGame(socket);
		} else if (port == GamePadsPort) {
			registerGamePad(socket);
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
		 * "Error on invalid game socket's closing."); } } } else
		 */{ // We are accepting a game
			if (gameSocket != null) {
				mGameConnection = new GameConnection(gameSocket);

				Log.i("GameIOProxy", "A game has connected");
				Vibrator v2 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				if(v2 != null)
					v2.vibrate(100);
				
				Toast.makeText(this, "A game is connected", Toast.LENGTH_SHORT).show();
			}

		}
	}

	/**
	 * Search the next free id from the {@code mLastGivenID} to favour the
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
		return mPadIdToUUID[nextId] == null ? -1 : nextId;
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
		for(int i = mGamePads.size()-1; i >= 0; i--) {
			GamePadConnection gpConnection = mGamePads.valueAt(i);
			if(gpConnection.isRegistered())
				gpConnection.unregister();
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
		
		// Start to listen for connections
		mGameAccepter = new ClientAccepter(GamePort, this);
		mGamepadAccepter = new ClientAccepter(GamePadsPort, this);
		
		mGameAccepter.start();
		mGamepadAccepter.start();
		
		Log.i("GameIOProxy", "Running");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		
		// Interrupt the client accepter threads
		mGameAccepter.interrupt();
		mGamepadAccepter.interrupt();
		
		// Close game connection
		if(mGameConnection != null)
			mGameConnection.close();
		
		// Close unregistered game-pad socket
		for(Socket socket : mBlockedGamePads) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		unregisterAllTheGamePads();
		
		//Close registered game-pad connection
		for(int i = mGamePads.size() - 1; i >= 0; i--) {
			GamePadConnection gpConnection = mGamePads.valueAt(i);
			gpConnection.close();
		}
		
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
		if (mGameConnection != null) {
			mGameConnection.send(msg.toString());
		}
	}
	/**
	 * Send the given message to the gamepad
	 * @param msg Message to send
	 * @param padId The ID of the game-pad recipient or -1 for broadcast
	 */
	void sendToGamePad(ProxyMessage msg, int padId) {
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
	class GameConnection extends StringSender implements StringReceiver.Listener {
		/**
		 * The socket used to communicate with the game server
		 */
		private Socket mSocket;
		
		/**
		 * 
		 */
		private StringReceiver mReceiver;

		/**
		 * @param socket
		 */
		public GameConnection(Socket socket) {
			super(socket);
			mSocket = socket;
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			this.start();
			mReceiver.start();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang.String)
		 */
		@Override
		public void onStringReceived(String string, Socket socket) {
			// Interpret and redirect the message
			try {
				// Read the message
				Message message = Message.gameFromJSON(new JSONObject(string));
				// Prepare to transmit a new message
				ProxyMessage proxyMessage = null;
				// Default recipient is broadcast.
				int recipientID = -1;
				
				// Make a new message according to the received message
				switch(message.getType()) {
				case JOIN:
					proxyMessage = new ProxyGameJoinMessage((GameJoinMessage) message);
					break;
				case LEAVE:
					proxyMessage = new ProxyGameLeaveMessage((GameLeaveMessage) message);
					break;
				case ACCEPT:
					proxyMessage = new ProxyGameAcceptMessage((GameAcceptMessage) message);
					recipientID = ((GameAcceptMessage) message).getGamePadId(); // Unique recipient
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
					Log.w("GameIOProxy", "InputEvent received from the game. Message dropped.");
					break;
				}
				
				// If a message has been defined, send it
				if(proxyMessage != null) {
					sendToGamePad(proxyMessage, recipientID);
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* (non-Javadoc)
		 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
		 */
		@Override
		public void onClosed(Socket socket) {
			mGameConnection = null;
		}

		/**
		 * Closes this connection. Shutdowns the associated StringSender and StringReceiver.
		 */
		public void close() {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class GamePadConnection extends StringSender implements StringReceiver.Listener {
		/** The id associated to this game-pad. Equals
		 * to -1 if the game-pad is not registered */
		private int mPadId = -1;
		
		private Socket mSocket;
		
		// FIXME why is the receiver an attribute? Meanwhile the sender is extended... 
		private StringReceiver mReceiver;

		/**
		 * 
		 */
		public GamePadConnection(Socket socket) {
			// Initialise the Sender
			super(socket);
			mSocket = socket;
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			this.start();
			mReceiver.start();
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
			try {
				// Read the message
				Message message = Message.gamePadFromJSON(new JSONObject(string));
				// Prepare to transmit a new message
				ProxyMessage proxyMessage = null;
				
				switch(message.getType()) {
				case JOIN:
					// transmit message if the game-pad has been successfully registered
					if(registerOrRecover(((GamePadJoinMessage) message).getUUID()))
						proxyMessage = new ProxyGamePadJoinMessage((GamePadJoinMessage) message, mPadId);
					break;
				case LEAVE:
					if(mPadId != -1) {
						proxyMessage = new ProxyGamePadLeaveMessage((GamePadLeaveMessage) message, mPadId);
						unregister();
					}
					else
						Log.i("GameIOProxy", "Leave received from a non-registered game-pad");
					break;
				case RENAME:
					if(mPadId != -1)
						proxyMessage = new ProxyGamePadRenameMessage((GamePadRenameMessage) message, mPadId);
					else
						Log.i("GameIOProxy", "Rename received from a non-registered game-pad");
					break;
				case INPUTEVENT:
					if(mPadId != -1)
						proxyMessage = new ProxyGamePadInputEventMessage((GamePadInputEventMessage) message, mPadId);
					else
						Log.i("GameIOProxy", "Input event received from a non-registered game-pad");
					break;
				case OUTPUTEVENT:
				case ACCEPT:
				default:
					// The game-pad cannot send neithr Output event or accept message, we ignore this message
					Log.w("GameIOProxy", "InputEvent/Accept received from a game-pad. Message dropped.");
					break;
				}
				
				// If the message has been defined, send it
				if(proxyMessage != null) {
					sendToGame(proxyMessage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		/** Associates the playerId, the UUID and this game-pad.<br>
		 * must be respected : 0 <= playerId < GameIOProxy.mIds.length **/
		void associateIdUUID(int padId, UUID uuid) {
			Log.i("GameIOProxy", "Associate id:" + padId + " to UUID:" + uuid);
			
			// Save value in GameIOProxy.this
			mPadIdToUUID[padId] = uuid;
			mGamePads.put(padId, this);
			mUUIDToPadId.put(uuid, padId);
			// remove from the list of unregistered game-pads
			mBlockedGamePads.remove(mSocket);
			
			mPadId = padId;
			setLastGivenId(padId);

			// Vibrate to indicate the gamepad has connected
			Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			v1.vibrate(100);
			Toast.makeText(GameIOProxy.this, "Game pad (id:"+mPadId+") is registered", Toast.LENGTH_SHORT).show();
		}

		/** Register this game-pad and get a new id. If the given UUID has 
		 * been already registered earlier and if the previous ID is still
		 * free, the game-pad recover its previous ID
		 * @params Game-pad's UUID
		 * @return true if successfully register or recover
		 * */
		boolean registerOrRecover(UUID inUUID) { // priority to the newly connected client
			// Try to recover if possible
			if(mUUIDToPadId.containsKey(inUUID)) {
				int previousID = mUUIDToPadId.get(inUUID);
				// Check if this previous id is free
				if(mPadIdToUUID[previousID] == null) {
					// The previous id is free, we can recover
					associateIdUUID(previousID, inUUID);
					return true;
				}
			}
			
			// Find a free id
			int freeId = getNewId();
			if(freeId != -1) {
				// free id available, we can register
				associateIdUUID(freeId, inUUID);
				return true;
			}
			
			// cannot recover nor register
			Log.w("GameIOProxy", "Cannot finf free id for UUID:" + inUUID);
			return false;
		}
		
		/**
		 * Removes the game-pad from the registered game-pad lists.<br>
		 * We do not remove the pad id from the hash map {@code mUUIDToPadId}.
		 * It may be used to recover later.
		 * @param padId Game-pad id to unregister
		 */
		void unregister() {
			Log.i("GameIOProxy", "Discard id:" + mPadId);
			// Remove from the list of registered game-pads
			mGamePads.remove(mPadId);
			mPadIdToUUID[mPadId] = null;
			// Go back to the list of unregistered game-pads
			mBlockedGamePads.add(mSocket);
			
			mPadId = -1;
			
			// Vibrate to indicate the gamepad has disconnected
			Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			v1.vibrate(100);
			Toast.makeText(GameIOProxy.this, "Game pad (id:"+mPadId+") is disconnected", Toast.LENGTH_SHORT).show();
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
			// if the game-pad is still registered, it means that the
			// game-pad has disconnected unexpectedly
			if(isRegistered()) {
				unregister();
				// TODO Send notification to the game. 
				// -> TODO Create message ProxyGamePadLostMessage
			}
		}

		/**
		 * Closes this connection by closing the associated socket. Should called
		 * {@code unregister()} before this, otherwise it will be considered as
		 * an unexpected disconnection.
		 */
		public void close() {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}// Class GamePadConnection

}
