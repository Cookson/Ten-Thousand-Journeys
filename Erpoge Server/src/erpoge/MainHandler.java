package erpoge;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erpoge.characters.Character;
import erpoge.characters.PlayerCharacter;
import erpoge.clientmessages.*;
import erpoge.inventory.Item;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.ItemType;
import erpoge.itemtypes.ItemsTypology;
import erpoge.serverevents.EventPutOn;
import erpoge.terrain.Location;
import erpoge.terrain.Portal;
import erpoge.terrain.World;
import net.tootallnate.websocket.Draft;
import net.tootallnate.websocket.WebSocket;
import net.tootallnate.websocket.WebSocketServer;
import net.tootallnate.websocket.drafts.Draft_76;


public class MainHandler extends WebSocketServer {
	public static final MainHandler instance = new MainHandler(8787);
	public static final int 
	SERVER_INFO				= 0,
	ATTACK					= 1,
	MOVE					= 2,
	PUT_ON					= 3,
	TAKE_OFF				= 4,
	PICK_UP_PILE			= 5,
	LOGIN 					= 6,
	LOAD_CONTENTS			= 7,
	AUTH					= 8,
	APPEAR 					= 9,
	WORLD_TRAVEL			= 10,
	DEAUTH					= 11,
	CHAT_MESSAGE			= 12,
	DROP_PILE				= 13,
	OPEN_CONTAINER			= 14,
	PUT_TO_CONTAINER		= 15,
	TAKE_FROM_CONTAINER		= 16,
	CAST_SPELL				= 17,
	SHOOT_MISSILE			= 18,
	USE_OBJECT				= 19,
	CHECK_OUT				= 20,
	ENTER_LOCATION			= 21,
	LEAVE_LOCATION			= 22,
	ANSWER					= 23,
	START_CONVERSATION		= 24,
	DROP_UNIQUE				= 25,
	PICK_UP_UNIQUE			= 26,
	LOAD_PASSIVE_CONTENTS	= 27,
	ACCOUNT_REGISTER		= 28,
	PLAYER_CREATE			= 29,
	IDLE                    = 30;
	public static final int MAX_NUM_OF_PLAYERS = 16;
	public static final Gson gson = new Gson();
	public static final Gson gsonIncludesStatic = new GsonBuilder()
    	.excludeFieldsWithModifiers(Modifier.TRANSIENT)
    	.create();
	public static World world;

	public MainHandler(int port) {
		super(port, new Draft_76());
		Main.outln("Start listening on port " + port);
	}
	public static void assignWorld(World world) {
		instance.world = world;
	}
	public static void startServer() {
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
	/* Handlers */
	private void aServerInfo(String message, WebSocket conn) throws IOException {
		// ping
		conn.send("{\"a\":"+MainHandler.SERVER_INFO+",\"serverName\":\"Erpoge Server\",\"online\":106}");
	}
	private void aLoadPassiveContents(String message, WebSocket conn) throws IOException {
	/**
	 * Sends only contents of world witout any login information.
	 * Used, for example, in world preview in client.
	 */
		conn.send("{\"a\":"+MainHandler.SERVER_INFO+","+world.jsonPartGetWorldContents()+"}");
	}
	private void aLogin(String message, WebSocket conn) throws IOException {
		/* 	in: {
				l: String login,
				p: String password,
			}
			out: [[characterId, name, race, class, level, maxHp, maxMp, str, dex, wis, itl, items, ammunition, spells]xN]
		*/
		ClientMessageLogin data = gson.fromJson(message, ClientMessageLogin.class);
		if (data.l.equals("")) {
			conn.send("{\"a\":"+MainHandler.LOGIN+",\"error\":1}");
		} else if (data.p.equals("")) {
			conn.send("{\"a\":"+MainHandler.LOGIN+",\"error\":2}");
		} else if (Accounts.hasAccount(data.l)) {
			Account account = Accounts.account(data.l);
			if (data.p.equals(account.password)) {
				conn.send("{\"a\":"+MainHandler.LOGIN+","+account.jsonPartGetCharactersAuthInfo()+"}");
			} else {
				conn.send("{\"a\":"+MainHandler.LOGIN+",\"error\":3}");
			}
		} else {
			conn.send("{\"a\":"+MainHandler.LOGIN+",\"error\":3}");
		}
	}
	private void aLoadContents(String message, WebSocket conn) throws IOException {
		// Almost the same as LOAD_LOCATON_CONTENTS
		ClientMessageAuth clientData = gson.fromJson(message, ClientMessageAuth.class);
		if (clientData.login.equals("")) {
		// Login is empty
			conn.send("{\"a\":"+MainHandler.LOAD_CONTENTS+",\"error\":0}");
		} else if (clientData.password.equals("")) {
		// Password is empty
			conn.send("{\"a\":"+MainHandler.LOAD_CONTENTS+",\"error\":1}");
		}
		Account account = Accounts.account(clientData.login);
		
		if (account == null) {
		// No such account
			conn.send("{\"a\":"+MainHandler.LOAD_CONTENTS+",\"error\":2}");
		} else if (!account.password.equals(clientData.password)) {
		// Client password doesn't match account password
			conn.send("{\"a\":"+MainHandler.LOAD_CONTENTS+",\"error\":3}");
		} else if (!account.hasCharacterWithId(clientData.characterId)) {
			conn.send("{\"a\":"+MainHandler.LOAD_CONTENTS+",\"error\":4}");
		} else {
		// Everything is okay
			conn.character = world.getPlayerById(clientData.characterId);
			if (!conn.character.isAuthorized()) {
				conn.character.authorize();
				conn.character.setConnection(conn);
			}
			conn.send(conn.character.jsonGetEnteringData(conn.character.isOnGlobalMap()));
		}
	}
	/* Listeners */
	public void onClientOpen(WebSocket conn) {
		Main.console("Client open");
	}
	public void onClientClose(WebSocket conn) {
		Main.console("Client close");
		if (conn.character != null && conn.character.isAuthorized()) {
			conn.character.deauthorize();
		}
	}
	public void onClientMessage(WebSocket conn, String message) {
		Main.console(message);
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
				conn.character.aMove(message);
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
			case WORLD_TRAVEL:
				conn.character.aWorldTravel(message);
				break;
			case DEAUTH:
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
			case ENTER_LOCATION:
				conn.character.aEnterLocation(message);
				break;
			case LEAVE_LOCATION:
				conn.character.aLeaveLocation(message);
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
			default:
				throw new Error("Unhandlable action code "+action+" came from a client");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void onIOError(IOException ex) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onError(Throwable ex) {
		// TODO Auto-generated method stub
		
	}
}
