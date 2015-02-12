/**
 * 
 */
package com.fbessou.sofa;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils.StringSplitter;
import android.util.Log;
import android.util.SparseArray;

import com.fbessou.sofa.ClientAccepter.OnClientAcceptedListener;

/**
 * This service provide an implementation for a basic bidirectional JsonOverTCP
 * proxy. It is This service provides two entry points : -
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
	ArrayList<Socket> mBlockedGamePads;

	/**
	 * Array which associates Id to UUID. The index in the array represents the
	 * Id.
	 */
	UUID[] mIds = new UUID[16];

	/**
	 * Map that associates a client UUID to its corresponding ID in the game.
	 * The UUID is only used for recovery, when a client disconnect and want to
	 * reconnect with the same "player" ID.
	 */
	private Map<UUID, Integer> mUUIDToId;

	/**
	 * Map that associates a Socket to a client UUID
	 */
	private SparseArray<GamePadConnection> mGamePads;

	/**
	 * Port the proxy is listening on for game connection
	 */
	private int mGamePort = 6969;

	/**
	 * 
	 */
	private GameConnection mGameConnection = null;
	/**
	 * Port the proxy is listening on for clients connection
	 */
	private int mGamePadsPort = 9696;

	/**
	 * Constructor, create an empty list of clients
	 */
	public GameIOProxy() {
		mUUIDToId = new HashMap<UUID, Integer>();
		mGamePads = new SparseArray<GamePadConnection>();
		mBlockedGamePads = new ArrayList<Socket>();
		Log.i("GameIOProxy", "Running");
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
				v2.vibrate(100);
			}

		}
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
		if (port == mGamePort) {
			registerGame(socket);
		} else if (port == mGamePadsPort) {
			registerGamePad(socket);
		}
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// Start to listen for connections
		new ClientAccepter(mGamePort, this).start();
		new ClientAccepter(mGamePadsPort, this).start();
		super.onCreate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// close sockets
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
	 * 
	 * @param string
	 */
	void sendToGame(String string) {
		if (mGameConnection != null) {
			mGameConnection.send(string);
		}
	}

	/**
	 * 
	 * @author Frank Bessou
	 *
	 */
	class GameConnection extends StringSender implements StringReceiver.Listener {
		/**
		 * 
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
		 * com.fbessou.sofa.StringReceiver.Listener#onStringReceived(java.lang
		 * .String)
		 */
		@Override
		public void onStringReceived(String string, Socket socket) {
			Log.i("IO",string);
			
			// Interpret and redirect the message
			try {
				JSONObject obj = new JSONObject(string);
				switch (obj.getString("type")) {
				case "outputevent":
					JSONObject evt = obj.getJSONObject("event");
					int pad = evt.getInt("pad");
					if(pad==-1){
						
					} else if (mGamePads.get(pad)!=null){
						mGamePads.get(pad).send(string);
					}
					break;
				default:
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.i("GameIOProxy", "Game message : " + string);
		}

		/* (non-Javadoc)
		 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
		 */
		@Override
		public void onClosed(Socket socket) {
			mGameConnection = null;
			
		}

	}

	class GamePadConnection extends StringSender implements StringReceiver.Listener {

		private int mId;
		
		private Socket mSocket;
		
		private StringReceiver mReceiver;

		private String mNickname; //For debug, there is no reason to store the

		/**
		 * 
		 */
		public GamePadConnection(Socket socket) {
			// Initialise the Sender
			super(socket);
			mSocket = socket;
			mReceiver = new StringReceiver(socket);
			mReceiver.setListener(this);
			new Thread(this).start();
			new Thread(mReceiver).start();
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
			try {// For JSON decoding
				JSONObject object = new JSONObject(string);
				String comType = object.getString("type");
				// If the message is a presentation
				switch (comType) {
				case "hello":
					UUID lastUUID = null;
					if (object.has("uuid"))
						lastUUID = UUID.fromString(object.getString(object.getString("uuid")));
					registerOrRecover(lastUUID,null);
					// Vibrate to indicate the gamepad has connected
					Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					v1.vibrate(100);
					break;
				case "inputevent":
					JSONObject event = object.getJSONObject("event");
					event.put("pad", mId);
					object.put("event", event);
					sendToGame(object.toString());
					break;
				default:
					Log.i("On envoie", string);
					sendToGame(string);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		void associateIdUUID(int id, UUID uuid) {
			Log.i("GameIOProxy", "Unnamed (" + id + ") Connected");
			mIds[id] = uuid;
			mGamePads.put(id, this);
			mId = id;
			String response = "{\"type\":\"hello\",\"uuid\":\"" + uuid + "\"}";
			send(response);
			// Send the notification to the game
			try {
				JSONObject msg = new JSONObject();
				msg.put("type", "padevent");
				msg.put("event", PadEvent.createJoinEvent(mId));
				sendToGame(msg.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		void registerOrRecover(UUID inUUID, String name) { // priority to the newly connected
												// client
			int firstAvailableId = -1;
			mNickname = name;
			for (int i = 0; i < mIds.length; i++) {

				if (mGamePads.get(i) == null && firstAvailableId == -1) {
					firstAvailableId = i;
					if (inUUID == null) { // Successfully recovered
						associateIdUUID(i, UUID.randomUUID());
						return;
					}
				}

				if (inUUID != null && inUUID.equals(mIds[i])) {// the uuid match
																// an id
					if (mGamePads.get(i) == null) {// there is no gamepads
													// connected with this id
						associateIdUUID(firstAvailableId, inUUID);
						return;
					} else { // id is already attributed
						associateIdUUID(firstAvailableId, UUID.randomUUID());
						return;
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see com.fbessou.sofa.StringReceiver.Listener#onClosed(java.net.Socket)
		 */
		@Override
		public void onClosed(Socket socket) {
			Log.i("GameIOProxy", "Unnamed (" + mId + ") Disconnected");
			mGamePads.append(mId, null);
			// Send notification to the client
			try {
				JSONObject msg = new JSONObject();
				msg.put("type", "padevent");
				msg.put("event", PadEvent.createLeaveEvent(mId));
				sendToGame(msg.toString());
			} catch (Exception e) {
				e.printStackTrace();
				;
			}
		}

	}// Class GamePadConnection

}
