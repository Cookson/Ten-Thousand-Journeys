package erpoge.core.net;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erpoge.core.Character;
import erpoge.core.Main;
import erpoge.core.characters.CharacterManager;
import erpoge.core.characters.PlayerCharacter;
import erpoge.core.inventory.Item;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.itemtypes.ItemType;
import erpoge.core.itemtypes.ItemsTypology;
import erpoge.core.net.clientmessages.*;
import erpoge.core.net.serverevents.EventChunkContents;
import erpoge.core.net.serverevents.EventPutOn;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.Portal;
import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.Handshakedata;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;
import net.tootallnate.websocket.drafts.Draft_10;
import net.tootallnate.websocket.drafts.Draft_17;
import net.tootallnate.websocket.drafts.Draft_75;
import net.tootallnate.websocket.drafts.Draft_76;


public class MainHandler extends WebSocketServer {
	public static MainHandler instance;
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
	private HorizontalPlane defaultPlane;

	public MainHandler() throws UnknownHostException {
		super(new InetSocketAddress(InetAddress.getByName( "localhost" ), WebSocket.DEFAULT_PORT), new Draft_17());
		Main.outln("Start listening on port "+WebSocket.DEFAULT_PORT);
	}
	public static void startServer() {
		try {
			instance = new MainHandler();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		instance.start();
	}
	public static void showConnections() {
		for (WebSocket conn : instance.connections) {
			if (conn.character != null) {
				Main.out(conn.character.name+" ");
			}
		}
		Main.outln();
	}
	public static void setDefaultPlane(HorizontalPlane plane) {
		instance.defaultPlane = plane;
	}
	/* Handlers */
	private void aServerInfo(String message, WebSocket conn) throws InterruptedException {
		// ping
		conn.send("[{\"e\":\"serverInfo\",\"serverName\":\"Erpoge Server\",\"online\":31337,\"chunkWidth\":"+Chunk.WIDTH+"}]");
	}
	private void aLoadPassiveContents(String message, WebSocket conn) throws InterruptedException {
	/**
	 * Sends only contents of world without any login information.
	 * Used, for example, in world preview in client.
	 */
		EventQueue queue = new EventQueue(conn);
		queue.addEvent(new EventChunkContents(defaultPlane.getChunkWithCell(-20,-20)));
		queue.addEvent(new EventChunkContents(defaultPlane.getChunkWithCell(-20,  0)));
		queue.addEvent(new EventChunkContents(defaultPlane.getChunkWithCell(  0,-20)));
		queue.addEvent(new EventChunkContents(defaultPlane.getChunkWithCell(  0,  0)));
		queue.flush();
	}
	private void aLogin(String message, WebSocket conn) throws InterruptedException {
		/* 	in: {
				l: String login,
				p: String password,
			}
			out: [[characterId, name, race, class, level, maxHp, maxMp, str, dex, wis, itl, items, equipment, spells]xN]
		*/
		ClientMessageLogin data = gson.fromJson(message, ClientMessageLogin.class);
		if (data.l.equals("")) {
			conn.send("[{\"e\":\"login\",\"error\":1}]");
		} else if (data.p.equals("")) {
			conn.send("[{\"e\":\"login\",\"error\":2}]");
		} else if (Accounts.hasAccount(data.l)) {
			Account account = Accounts.account(data.l);
			if (data.p.equals(account.password)) {
				conn.send("[{\"e\":\"login\","+account.jsonPartGetCharactersAuthInfo()+"}]");
			} else {
				conn.send("[{\"e\":\"login\",\"error\":3}]");
			}
		} else {
			conn.send("[{\"e\":\"login\",\"error\":3}]");
		}
	}
	private void aLoadContents(String message, WebSocket conn) throws InterruptedException {
		// Almost the same as LOAD_LOCATON_CONTENTS
		ClientMessageAuth clientData = gson.fromJson(message, ClientMessageAuth.class);
		if (clientData.login.equals("")) {
		// Login is empty
			conn.send("[{\"e\":\"loadContents\",\"error\":0}]");
		} else if (clientData.password.equals("")) {
		// Password is empty
			conn.send("[{\"e\":\"loadContents\",\"error\":1}]");
		}
		Account account = Accounts.account(clientData.login);
		if (account == null) {
		// No such account
			conn.send("[{\"e\":\"loadContents\",\"error\":2}]");
		} else if (!account.password.equals(clientData.password)) {
		// Client password doesn't match account password
			conn.send("[{\"e\":\"loadContents\",\"error\":3}]");
		} else if (!account.hasCharacterWithId(clientData.characterId)) {
			conn.send("[{\"e\":\"loadContents\",\"error\":4}]");
		} else {
		// Player login data is correct
			PlayerHandler player = CharacterManager.getPlayerById(clientData.characterId);
			if (player.isAuthorized()) {
				if (player.connection.isClosed()) {
					conn.character = player;
				} else {
				// Error: player has already authorized
					conn.send("[{\"e\":\"loadContents\",\"error\":5}]");
					return;
				}
			} else {
			// If that player is not authorized
				conn.character = player;
			}
			conn.character.authorize();
			conn.character.setConnection(conn);
			conn.character.getEnteringEventQueue();
		}
	}
	/* Listeners */
	public void onClientOpen(WebSocket conn) {
//		Main.log("Client open");
	}
	public void onClientClose(WebSocket conn) {
//		Main.log("Client close");
		if (conn.character != null && conn.character.isAuthorized()) {
			conn.character.deauthorize();
		}
	}
	public void onClientMessage(WebSocket conn, String message) {
		Main.log(message);
		int action = gson.fromJson(message, ClientMessageAction.class).a;
		try {
			switch (action) {
			case LOGIN: 
				aLogin(message, conn);
				break;
			case ACCOUNT_REGISTER:
				conn.character.aAccountRegister(message);
				break;
			case PLAYER_CREATE:
				conn.character.aPlayerCreate(message);
				break;
			case LOAD_CONTENTS:
				aLoadContents(message, conn);
				break;
			case SERVER_INFO:
				aServerInfo(message, conn);
				break;
			case ATTACK:
				conn.character.aAttack(message);
				break;
			case MOVE:
				conn.character.aStep(message);
				break;
			case PUT_ON:
				conn.character.aPutOn(message);
				break;
			case TAKE_OFF:
				conn.character.aTakeOff(message);
				break;
			case PICK_UP_PILE:
				conn.character.aPickUpPile(message);
				break;
			case PICK_UP_UNIQUE:
				conn.character.aPickUpUnique(message);
				break;
			case DROP_PILE:
				conn.character.aDropPile(message);
				break;
			case DROP_UNIQUE:
				conn.character.aDropUnique(message);
				break;
			case DEAUTH:
				Main.outln(message);
				conn.character.aDeauth(message);
				break;
			case CHAT_MESSAGE:
				conn.character.aChatMessage(message);
				break;
			case OPEN_CONTAINER:
				conn.character.aOpenContainer(message);
				break;
			case TAKE_FROM_CONTAINER:
				conn.character.aTakeFromContainer(message);
				break;
			case PUT_TO_CONTAINER:
				conn.character.aPutToContainer(message);
				break;
			case CAST_SPELL:
				conn.character.aCastSpell(message);
				break;
			case SHOOT_MISSILE:
				conn.character.aShootMissile(message);
				break;
			case USE_OBJECT:
				conn.character.aUseObject(message);
				break;
			case CHECK_OUT:
				conn.character.aCheckOut(message);
				break;
			case ANSWER:
				conn.character.aAnswer(message);
				break;
			case START_CONVERSATION:
				conn.character.aStartConversation(message);
				break;
			case LOAD_PASSIVE_CONTENTS:
				aLoadPassiveContents(message, conn);
				break;
			case IDLE:
				conn.character.aIdle(message);
				break;
			case PUSH:
				conn.character.aPush(message);
				break;
			case CHANGE_PLACES:
				conn.character.aChangePlaces(message);
				break;
			case MAKE_SOUND:
				conn.character.aMakeSound(message);
				break;
			case JUMP:
				conn.character.aJump(message);
				break;
			case SHIELD_BASH:
				conn.character.aShieldBash(message);
				break;
			case ENTER_STATE:
				conn.character.aEnterState(message);
				break;
			default:
				throw new Error("Unhandlable action code "+action+" came from a client");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void onIOError(IOException ex) {
		ex.printStackTrace();
	}
	@Override
	public void onClientClose(WebSocket conn, int code, String reason,
			boolean remote) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClientOpen(WebSocket conn, Handshakedata handshake) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
	}
	
}
