package erpoge.core;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import erpoge.core.net.clientmessages.ClientMessageAccountRegister;
import erpoge.core.net.clientmessages.ClientMessageLogin;

/**
 * This singleton manages sending asynchronous events to clients. Asynchronous
 * events are all the events that occur independently from any TimeStream. These
 * are such events as client asking for server info, client authenticating,
 * client asking for server statistics, silent saving his configuration on
 * server etc.
 */
public class AsyncEventProvider {
	private static final Gson gsonAsync = new GsonBuilder()
			.registerTypeAdapter(Account.class, new JsonSerializer<Account>() {

				@Override
				public JsonElement serialize(Account account, Type typeOfSrc,
						JsonSerializationContext context) {
					JsonArray jArray = new JsonArray();
					for (PlayerCharacter player : account.characters) {
						JsonArray jArrayPlayer = new JsonArray();
						jArrayPlayer.add(new JsonPrimitive(player.getId()));
						jArrayPlayer.add(new JsonPrimitive(player.getName()));
						jArrayPlayer.add(new JsonPrimitive(player.getCls()));
						jArrayPlayer.add(new JsonPrimitive(player.getType().getName()));
						jArray.add(jArrayPlayer);
					}
					return jArray;
				}
			})
			.create();

	/**
	 * Sends only contents of world without any login information.
	 * Used, for example, in world preview in client.
	 */
	static void aLoadPassiveContents(String message, Connection connection) throws InterruptedException {
		EventQueue queue = new EventQueue()
			.add(ServerEvents.create("chunkContents", ConnectionServer.defaultPlane.getChunkWithCell(-20,-20)))
			.add(ServerEvents.create("chunkContents", ConnectionServer.defaultPlane.getChunkWithCell(-20,  0)))
			.add(ServerEvents.create("chunkContents", ConnectionServer.defaultPlane.getChunkWithCell(  0,-20)))
			.add(ServerEvents.create("chunkContents", ConnectionServer.defaultPlane.getChunkWithCell(  0,  0)));
		connection.send(queue.serialize());
	}
	/**
	 * Sends information about server. Necessary for a client to be able to
	 * connect to the server
	 * 
	 * @param message
	 * @param conn
	 * @throws InterruptedException
	 */
	static void aServerInfo(String message, Connection conn) throws InterruptedException {
		conn.send("[{\"e\":\"serverInfo\",\"serverName\":\"Erpoge Server\",\"online\":31337,\"chunkWidth\":"+Chunk.WIDTH+"}]");
	}
	
	static void aLogin(String message, Connection connection) throws InterruptedException {
		/* 	in: {
				l: String login,
				p: String password,
			}
			out: [[characterId, name, zrace, class, level, maxHp, maxMp, str, dex, wis, itl, items, equipment, spells]xN]
		*/
		ClientMessageLogin data = gsonAsync.fromJson(message, ClientMessageLogin.class);
		if (data.l.equals("")) {
			connection.send("[{\"e\":\"login\",\"error\":1}]");
		} else if (data.p.equals("")) {
			connection.send("[{\"e\":\"login\",\"error\":2}]");
		} else if (Accounts.hasAccount(data.l)) {
			Account account = Accounts.account(data.l);
			if (data.p.equals(account.password)) {
				EventQueue queue = new EventQueue()
					.add(ServerEvents.create("login", account, gsonAsync));
				connection.send(queue.serialize());
			} else {
				connection.send("[{\"e\":\"login\",\"error\":3}]");
			}
		} else {
			connection.send("[{\"e\":\"login\",\"error\":3}]");
		}
	}
	static void aAccountRegister(String message, Connection connection) throws InterruptedException {
		ClientMessageAccountRegister accountRegsterData = gsonAsync.fromJson(message, ClientMessageAccountRegister.class);
		if (Accounts.hasAccount(accountRegsterData.l)) {
			connection.send("{\"e\":\"accountRegister\",\"error\":1}");
		} else {
			Accounts.addAccount(new Account(accountRegsterData.l, accountRegsterData.p));
			Account account = Accounts.account(accountRegsterData.l);
//			PlayerCharacter shanok = world.createCharacter("player", "Bliot", 3, "Эльфоцап", 13, 26);
//			shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_BOW, 0));
//			shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_BOW, 0));
//			shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_BOW, 0));
//			shanok.getItem(UniqueItem.createItemByClass(Item.CLASS_SWORD, 0));
//			shanok.getItem(ItemPile.createPileFromClass(Item.CLASS_AMMO, 0, 1000));
//			Accounts.account(accountRegsterData.l).addCharacter(shanok);
			EventQueue queue = new EventQueue()
				.add(ServerEvents.create("login", account, gsonAsync));
			connection.send(queue.serialize());
		}
	}
	
}
