package erpoge.core;

import java.lang.reflect.Modifier;
import java.util.HashSet;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.Handshakedata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erpoge.core.net.clientmessages.ClientMessageAction;

public class ConnectionServer {
	private static HashSet<Connection> connections = new HashSet<Connection>();
	public static final int 
	SERVER_INFO             = 0,
	ATTACK                  = 1,
	MOVE                    = 2,
	PUT_ON                  = 3,
	TAKE_OFF                = 4,
	PICK_UP_PILE            = 5,
	LOGIN                   = 6,
	LOAD_CONTENTS           = 7,
	AUTH                    = 8,
	APPEAR                  = 9,
	____________            = 10,
	DEAUTH                  = 11,
	CHAT_MESSAGE            = 12,
	DROP_PILE               = 13,
	OPEN_CONTAINER          = 14,
	PUT_TO_CONTAINER        = 15,
	TAKE_FROM_CONTAINER     = 16,
	CAST_SPELL              = 17,
	SHOOT_MISSILE           = 18,
	USE_OBJECT              = 19,
	CHECK_OUT               = 20,
	ENTER_LOCATION          = 21,
	LEAVE_LOCATION          = 22,
	ANSWER                  = 23,
	START_CONVERSATION      = 24,
	DROP_UNIQUE             = 25,
	PICK_UP_UNIQUE          = 26,
	LOAD_PASSIVE_CONTENTS   = 27,
	ACCOUNT_REGISTER        = 28,
	PLAYER_CREATE           = 29,
	IDLE                    = 30,
	ENTER_STATE             = 31,
	
	/* Player special actions */
	PUSH                    = 201,
	CHANGE_PLACES           = 202,
	MAKE_SOUND              = 203,
	JUMP                    = 204,
	SHIELD_BASH             = 205;
	public static final int MAX_NUM_OF_PLAYERS = 16;
	public static final Gson gson = new Gson();
	public static final Gson gsonIncludesStatic = new GsonBuilder()
    	.excludeFieldsWithModifiers(Modifier.TRANSIENT)
    	.create();
	
	static HorizontalPlane defaultPlane;

	public ConnectionServer() {
		Main.outln("Start listening on port "+WebSocket.DEFAULT_PORT);
	}
	
	/**
	 * Log ongoing connections into standard output
	 */
	public static void showConnections() {
		for (Connection connection : connections) {
		// Itearate over all the opened Connection instances.
			Main.out(connection);
		}
		Main.outln();
	}
	public static void setDefaultPlane(HorizontalPlane plane) {
		defaultPlane = plane;
	}
	public static void onOpen(Connection ws) {
		connections.add(ws);
	}
	public static void onMessage(Connection connection, String message) {
		Main.outln(message);
		int action = gson.fromJson(message, ClientMessageAction.class).a;
		PlayerHandler player = connection.getPlayerHandler();
		try {
			switch (action) {
			case LOGIN:
				AsyncEventProvider.aLogin(message, connection);
				break;
			case ACCOUNT_REGISTER:
				AsyncEventProvider.aAccountRegister(message, connection);
				break;
			case PLAYER_CREATE:
				throw new Error("Not implemented!");
			case LOAD_CONTENTS:
				player.aLoadContents(message, connection);
				break;
			case SERVER_INFO:
				AsyncEventProvider.aServerInfo(message, connection);
				break;
			case ATTACK:
				player.aAttack(message);
				break;
			case MOVE:
				player.aStep(message);
				break;
			case PUT_ON:
				player.aPutOn(message);
				break;
			case TAKE_OFF:
				player.aTakeOff(message);
				break;
			case PICK_UP_PILE:
				player.aPickUpPile(message);
				break;
			case PICK_UP_UNIQUE:
				player.aPickUpUnique(message);
				break;
			case DROP_PILE:
				player.aDropPile(message);
				break;
			case DROP_UNIQUE:
				player.aDropUnique(message);
				break;
			case DEAUTH:
				Main.outln(message);
				player.aDeauth(message);
				break;
			case CHAT_MESSAGE:
				player.aChatMessage(message);
				break;
			case TAKE_FROM_CONTAINER:
				player.aTakeFromContainer(message);
				break;
			case PUT_TO_CONTAINER:
				player.aPutToContainer(message);
				break;
			case CAST_SPELL:
				player.aCastSpell(message);
				break;
			case SHOOT_MISSILE:
				player.aShootMissile(message);
				break;
			case USE_OBJECT:
				player.aUseObject(message);
				break;
			case CHECK_OUT:
				player.aCheckOut(message);
				break;
			case ANSWER:
				player.aAnswer(message);
				break;
			case START_CONVERSATION:
				player.aStartConversation(message);
				break;
			case LOAD_PASSIVE_CONTENTS:
				AsyncEventProvider.aLoadPassiveContents(message, connection);
				break;
			case IDLE:
				player.aIdle(message);
				break;
			case PUSH:
				player.aPush(message);
				break;
			case CHANGE_PLACES:
				player.aChangePlaces(message);
				break;
			case MAKE_SOUND:
				player.aMakeSound(message);
				break;
			case JUMP:
				player.aJump(message);
				break;
			case SHIELD_BASH:
				player.aShieldBash(message);
				break;
			case ENTER_STATE:
				player.aEnterState(message);
				break;
			default:
				throw new Error("Unhandlable action code "+action+" came from a client");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void onClose(Connection connection) {
		throw new Error("Client closed, don't know what to do : (");
	}
	public static void onOpen(Connection connection, Handshakedata handshake) {
		connections.add(connection);
	}
	public static void onError(Connection connection, Exception ex) {
		ex.printStackTrace();
	}
}
