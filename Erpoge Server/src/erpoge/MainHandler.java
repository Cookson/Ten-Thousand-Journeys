package erpoge;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import erpoge.characters.Character;
import erpoge.characters.CharacterSet;
import erpoge.characters.PlayerCharacter;
import erpoge.clientmessages.*;
import erpoge.inventory.Item;
import erpoge.inventory.ItemPile;
import erpoge.inventory.ItemsTypology;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.ItemType;
import erpoge.terrain.Location;
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
	PLAYER_CREATE			= 29;
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
				/* 	in: {
						l: String login,
						p: String password,
					}
					out: [[characterId, name, race, class, level, maxHp, maxMp, str, dex, wis, itl, items, ammunition, spells]xN]
				*/
				ClientMessageLogin data = gson.fromJson(message, ClientMessageLogin.class);
				if (data.l.equals("")) {
					conn.send("{\"a\":"+LOGIN+",\"error\":1}");
				} else if (data.p.equals("")) {
					conn.send("{\"a\":"+LOGIN+",\"error\":2}");
				} else if (Accounts.hasAccount(data.l)) {
					Account account = Accounts.account(data.l);
					if (data.p.equals(account.password)) {
						conn.send("{\"a\":"+LOGIN+","+account.jsonPartGetCharactersAuthInfo()+"}");
					} else {
						conn.send("{\"a\":"+LOGIN+",\"error\":3}");
					}
				} else {
					conn.send("{\"a\":"+LOGIN+",\"error\":3}");
				}
				break;
			case ACCOUNT_REGISTER:
				ClientMessageAccountRegister accountRegsterData = gson.fromJson(message, ClientMessageAccountRegister.class);
				if (Accounts.hasAccount(accountRegsterData.l)) {
					conn.send("{\"a\":"+ACCOUNT_REGISTER+",\"error\":1}");
				} else {
					Accounts.addAccount(new Account(accountRegsterData.l, accountRegsterData.p));
					Account account = Accounts.account(accountRegsterData.l);
//					PlayerCharacter shanok = world.createCharacter("player", "Bliot", 3, "Эльфоцап", 13, 26);
//					shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_BOW, 0));
//					shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_BOW, 0));
//					shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_BOW, 0));
//					shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_SWORD, 0));
//					shanok.getItem(ItemPile.createPileFromClass(Item.CLASS_AMMO, 0, 1000));
//					Accounts.account(accountRegsterData.l).addCharacter(shanok);
					conn.send("{\"a\":"+LOGIN+","+account.jsonPartGetCharactersAuthInfo()+"}");
				}
				break;
			case PLAYER_CREATE:
				ClientMessagePlayerCreate playerCreateData = gson.fromJson(message, ClientMessagePlayerCreate.class);
				Account accountPlayerCreate = Accounts.account(playerCreateData.account);
				PlayerCharacter newPlayer = world.createCharacter(
						"player", 
						playerCreateData.name, 
						playerCreateData.race, 
						playerCreateData.cls, 
						13, 26);
				accountPlayerCreate.addCharacter(newPlayer);
				Main.console("New character "+newPlayer.name);
				accountPlayerCreate.accountStatistic();
				conn.send("{\"a\":"+LOGIN+","+accountPlayerCreate.jsonPartGetCharactersAuthInfo()+"}");
				break;
			case LOAD_CONTENTS:
			// Almost the same as LOAD_LOCATON_CONTENTS
				ClientMessageAuth clientData = gson.fromJson(message, ClientMessageAuth.class);
				if (clientData.login.equals("")) {
				// Login is empty
					conn.send("{\"a\":"+LOAD_CONTENTS+",\"error\":0}");
				} else if (clientData.password.equals("")) {
				// Password is empty
					conn.send("{\"a\":"+LOAD_CONTENTS+",\"error\":1}");
				}
				Account account = Accounts.account(clientData.login);
				
				if (account == null) {
				// No such account
					conn.send("{\"a\":"+LOAD_CONTENTS+",\"error\":2}");
				} else if (!account.password.equals(clientData.password)) {
				// Client password doesn't match account password
					conn.send("{\"a\":"+LOAD_CONTENTS+",\"error\":3}");
				} else if (!account.hasCharacterWithId(clientData.characterId)) {
					conn.send("{\"a\":"+LOAD_CONTENTS+",\"error\":4}");
				} else {
				// Everything is okay
					conn.character = world.getPlayerById(clientData.characterId);
					if (!conn.character.isAuthorized()) {
						conn.character.authorize();
						conn.character.setConnection(conn);
					}
					conn.send(conn.character.jsonGetEnteringData(conn.character.isOnGlobalMap()));
				}
				break;
			case SERVER_INFO:
				// ping
				conn.send("{\"a\":"+SERVER_INFO+",\"serverName\":\"Erpoge Server\",\"online\":106}");
				break;
			case ATTACK:
				// attack
				ClientMessageAttack messageAttack = gson.fromJson(message, ClientMessageAttack.class);
				conn.character.attack(
					conn.character.location.characters.get(messageAttack.aimId)
				);
				break;
			case MOVE:
				// move
				// v - movement direction
				conn.character.move(gson.fromJson(message,
						ClientMessageMove.class).dir);
				break;
			case PUT_ON:
				// put on an item
				// v - item id
				conn.character.putOn(gson.fromJson(message,
						ClientMessagePutOn.class).itemId);
				break;
			case TAKE_OFF:
				// take off an item
				// v - item id
				conn.character.takeOff(gson.fromJson(message,
						ClientMessageTakeOff.class).itemId);
				break;
			case PICK_UP_PILE:
				ClientMessagePickUpPile pickItemP = gson.fromJson(message,
						ClientMessagePickUpPile.class);
				conn.character.pickUp(pickItemP.typeId, pickItemP.amount);
				break;
			case PICK_UP_UNIQUE:
				ClientMessagePickUpUnique pickItemU = gson.fromJson(message,
						ClientMessagePickUpUnique.class);
				conn.character.pickUp(pickItemU.itemId);
				break;
			case DROP_PILE:
				// drop an item
				ClientMessageDropPile dsItemP = gson.fromJson(message,
						ClientMessageDropPile.class);
				conn.character.drop(dsItemP.typeId, dsItemP.amount);
				break;
			case DROP_UNIQUE:
				// drop an item
				ClientMessageDropUnique dsItemU = gson.fromJson(message,
						ClientMessageDropUnique.class);
				conn.character.drop(dsItemU.itemId);
				break;
			case WORLD_TRAVEL:
				ClientMessageWorldTravel clientDataWT = gson.fromJson(message, ClientMessageWorldTravel.class);
				conn.character.worldTravel(clientDataWT.x, clientDataWT.y);
				break;
			case DEAUTH:
				if (conn.character != null) {
					conn.character.deauthorize();
				}
				break;
			case CHAT_MESSAGE:
				ClientMessageChatMessage clientDataCHM = gson.fromJson(message, ClientMessageChatMessage.class);
				conn.character.say(clientDataCHM.text);
				break;
			case OPEN_CONTAINER:
				ClientMessageCoordinate containerCoord = gson.fromJson(message, ClientMessageCoordinate.class);
//				Main.console(conn.character.location.jsonGetContainerContents(containerCoord.x, containerCoord.y));
				conn.send("[{\"e\":\"openContainer\",\"items\":["+
						conn.character.location.jsonGetContainerContents(containerCoord.x, containerCoord.y)+"]}]");
				break;
			case TAKE_FROM_CONTAINER:
				ClientMessageTakeFromContainer take = gson.fromJson(message, ClientMessageTakeFromContainer.class);
				conn.character.takeFromContainer(take.typeId, take.param, take.x, take.y);
				break;
			case PUT_TO_CONTAINER:
				ClientMessageTakeFromContainer put = gson.fromJson(message, ClientMessageTakeFromContainer.class);
				conn.character.putToContainer(put.typeId, put.param, put.x, put.y);
				break;
			case CAST_SPELL:
				ClientMessageCastSpell messageSpell = gson.fromJson(message, ClientMessageCastSpell.class);
				conn.character.castSpell(messageSpell.spellId, messageSpell.x, messageSpell.y);
				break;
			case SHOOT_MISSILE:
				ClientMessageShootMissile messageShoot = gson.fromJson(message, ClientMessageShootMissile.class);
				conn.character.shootMissile(messageShoot.x, messageShoot.y, messageShoot.missile);
				break;
			case USE_OBJECT:
				ClientMessageUseObject messageUse = gson.fromJson(message, ClientMessageUseObject.class);
				conn.character.useObject(messageUse.x, messageUse.y);
				break;
			case CHECK_OUT:
				conn.character.location().checkOut(conn.character);
				break;
			case ENTER_LOCATION:
				ClientMessageEnterLocation messageEnter = gson.fromJson(message, ClientMessageEnterLocation.class);
				Location location;
				if (world.locations.containsKey(new Coordinate(messageEnter.x, messageEnter.y))) {
					location = world.locations.get(new Coordinate(messageEnter.x, messageEnter.y));
				} else {
					location = world.createLocation(messageEnter.x, messageEnter.y, Main.DEFAULT_LOCATION_WIDTH,Main.DEFAULT_LOCATION_HEIGHT, Main.TEST_LOCATION_TYPE, "����� �������");
				}
				location.addCharacter(conn.character);
				conn.send(conn.character.jsonGetEnteringData(conn.character.isOnGlobalMap()));
				break;
			case LEAVE_LOCATION:
				Location portalLocation = conn.character.getPortalLocation();
				conn.character.leaveLocation();
				if (portalLocation != null) {
				// If player character leeaves current location near a portal to another location,
				// then send him to that another location.
					portalLocation.addCharacter(conn.character);
				}
				conn.send(conn.character.jsonGetEnteringData(conn.character.isOnGlobalMap()));
				break;
			case ANSWER:
				ClientMessageAnswer messageAnswer = gson.fromJson(message, ClientMessageAnswer.class);
				conn.character.dialogueAnswer(messageAnswer.answerId);
				break;
			case START_CONVERSATION:
				ClientMessageStartConversation messageConversation = gson.fromJson(message, ClientMessageStartConversation.class);
				conn.character.startConversation(messageConversation.characterId);
				break;
			case LOAD_PASSIVE_CONTENTS:
				conn.send("{\"a\":"+SERVER_INFO+","+world.jsonPartGetWorldContents()+"}");
				break;
			default:
				throw new Error("Unhandlable action code came from a client");
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
