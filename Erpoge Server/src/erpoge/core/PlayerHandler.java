package erpoge.core;

import java.util.Arrays;

import com.google.gson.Gson;

import erpoge.core.characters.CharacterState;
import erpoge.core.meta.Side;
import erpoge.core.net.clientmessages.ClientMessageAnswer;
import erpoge.core.net.clientmessages.ClientMessageAttack;
import erpoge.core.net.clientmessages.ClientMessageAuth;
import erpoge.core.net.clientmessages.ClientMessageCastSpell;
import erpoge.core.net.clientmessages.ClientMessageChangePlaces;
import erpoge.core.net.clientmessages.ClientMessageChatMessage;
import erpoge.core.net.clientmessages.ClientMessageDropPile;
import erpoge.core.net.clientmessages.ClientMessageDropUnique;
import erpoge.core.net.clientmessages.ClientMessageEnterState;
import erpoge.core.net.clientmessages.ClientMessageJump;
import erpoge.core.net.clientmessages.ClientMessageMakeSound;
import erpoge.core.net.clientmessages.ClientMessagePickUpPile;
import erpoge.core.net.clientmessages.ClientMessagePickUpUnique;
import erpoge.core.net.clientmessages.ClientMessagePush;
import erpoge.core.net.clientmessages.ClientMessagePutOn;
import erpoge.core.net.clientmessages.ClientMessageShieldBash;
import erpoge.core.net.clientmessages.ClientMessageShootMissile;
import erpoge.core.net.clientmessages.ClientMessageStartConversation;
import erpoge.core.net.clientmessages.ClientMessageStep;
import erpoge.core.net.clientmessages.ClientMessageTakeFromContainer;
import erpoge.core.net.clientmessages.ClientMessageTakeOff;
import erpoge.core.net.clientmessages.ClientMessageUseObject;
import erpoge.core.terrain.Container;

/**
 * Extension of PlayerCharacter that contains methods
 * for incoming data handling and sending outcoming data to clients.
 * Methods transform raw string/integer data to game objects, if necessary,
 * and then pass these objects to Character methods (which do not
 * apply raw data in many cases)
 */
public class PlayerHandler extends PlayerCharacter {
	public static final long serialVersionUID = 9299166661L;
	public boolean checkedOut = false;
	protected boolean isAuthorized = false;
	private static final Gson gson = new Gson();
	public Connection connection;
	public PlayerHandler(HorizontalPlane plane, int x, int y, String name, CharacterType race, String cls) {
		super(plane, x, y, name, race, cls);
	}
	/* Net setters */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public void deauthorize() {
		// Inform all characters who are on global map right now that
		// this character has left
		isAuthorized = false;
//		timeStream.addEvent(new EventDeauthorization(characterId));
//		timeStream.flushEvents();
	}
	public void authorize() {
		// Inform all characters who are on global map right now that
		// this character has entered the game
		isAuthorized = true;
	}
	
	/* Action handlers */
	
	
	public void aAttack(String message) throws InterruptedException {
		ClientMessageAttack data = gson.fromJson(message, ClientMessageAttack.class);
		for (Character ch : timeStream.characters) {
			if (ch.id == data.aimId) {
				attack(ch);
				getTimeStream().flushEvents();
				return;
			}
		}
		throw new Error("No character with id "+data.aimId);
	}
	public void aStep(String message) throws InterruptedException {
		int dx, dy;
		switch (gson.fromJson(message, ClientMessageStep.class).dir) {
			case 0: dx =  0; dy = -1; break;
			case 1:	dx =  1; dy = -1; break;
			case 2:	dx =  1; dy =  0; break;
			case 3:	dx =  1; dy =  1; break;
			case 4:	dx =  0; dy =  1; break;
			case 5:	dx = -1; dy =  1; break;
			case 6: dx = -1; dy =  0; break;
			default: dx = -1; dy = -1;
		}
		step(x + dx, y + dy);
		timeStream.flushEvents();
	}
	public void aPutOn(String message) throws InterruptedException {
		// put on an item
		// v - item id
		int itemId = gson.fromJson(message,	ClientMessagePutOn.class).itemId;
		putOn(inventory.getUnique(itemId), false);
		timeStream.flushEvents();
	}
	public void aTakeOff(String message) throws InterruptedException {
		// take off an item
		// v - item id
		int itemId = gson.fromJson(message,
				ClientMessageTakeOff.class).itemId;
		takeOff(body.getItem(itemId));
		timeStream.flushEvents();
	}
	public void aPickUpPile(String message) throws InterruptedException {
		ClientMessagePickUpPile data = gson.fromJson(message,
				ClientMessagePickUpPile.class);
		pickUp(plane.getItems(x, y).getPile(data.typeId).separatePile(data.amount));
		timeStream.flushEvents();
	}
	public void aPickUpUnique(String message) throws InterruptedException {
		ClientMessagePickUpUnique data = gson.fromJson(message,
				ClientMessagePickUpUnique.class);
		pickUp(plane.getItems(x, y).getUnique(data.itemId));
		timeStream.flushEvents();
	}
	public void aDropPile(String message) throws InterruptedException {
		// drop an item
		ClientMessageDropPile data = gson.fromJson(message,
				ClientMessageDropPile.class);
		drop(inventory.getPile(data.typeId).separatePile(data.amount));
		getTimeStream().flushEvents();
	}
	public void aDropUnique(String message) throws InterruptedException {
		// drop an item
		ClientMessageDropUnique data = gson.fromJson(message,
				ClientMessageDropUnique.class);
		drop(inventory.getUnique(data.itemId));
		getTimeStream().flushEvents();
	}
	public void aDeauth(String message) throws InterruptedException {
			deauthorize();
	}
	public void aChatMessage(String message) throws InterruptedException {
		ClientMessageChatMessage data = gson.fromJson(message, ClientMessageChatMessage.class);
		say(data.text);
	}
	public void aTakeFromContainer(String message) throws InterruptedException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = plane.getChunkWithCell(data.x, data.y).getContainer(data.x, data.y);
		if (StaticData.getItemType(data.typeId).isStackable()) {
			takeFromContainer(container.getPile(data.typeId).separatePile(data.param), container);
		} else {
			takeFromContainer(container.getUnique(data.param), container);
		}			
		timeStream.flushEvents();
	}
	public void aPutToContainer(String message) throws InterruptedException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = plane.getChunkWithCell(data.x, data.y).getContainer(data.x, data.y);
		if (StaticData.getItemType(data.typeId).isStackable()) {
			super.putToContainer(inventory.getPile(data.typeId).separatePile(data.param), container);
		} else {
			super.putToContainer(inventory.getUnique(data.param), container);
		}
		timeStream.flushEvents();
	}
	public void aCastSpell(String message) throws InterruptedException {
		ClientMessageCastSpell data = gson.fromJson(message, ClientMessageCastSpell.class);
		castSpell(data.spellId, data.x, data.y);
		timeStream.flushEvents();
	}
	public void aShootMissile(String message) throws InterruptedException {
		ClientMessageShootMissile data = gson.fromJson(message, ClientMessageShootMissile.class);
		if (data.unique) {
			shootMissile(data.x, data.y, inventory.getUnique(data.missile));
		} else {
			shootMissile(data.x, data.y, inventory.getPile(data.missile).separatePile(1));
		}
		timeStream.flushEvents();
	}
	public void aUseObject(String message) throws InterruptedException {
		ClientMessageUseObject data = gson.fromJson(message, ClientMessageUseObject.class);
		useObject(data.x, data.y);
		timeStream.flushEvents();
	}
	public void aCheckOut(String message) throws InterruptedException {
		timeStream.checkOut(this);
	}
	public void aAnswer(String message) throws InterruptedException {
		ClientMessageAnswer messageAnswer = gson.fromJson(message, ClientMessageAnswer.class);
		dialogueAnswer(messageAnswer.answerId);
	}
	public void aStartConversation(String message) throws InterruptedException {
		ClientMessageStartConversation data = gson.fromJson(message, ClientMessageStartConversation.class);
		startConversation(data.characterId);
	}
	public void aIdle(String message) {
		idle();
		timeStream.flushEvents();
	}
	public boolean isAuthorized() {
		return isAuthorized;
	}
	public void aPush(String message) {
		ClientMessagePush data = gson.fromJson(message, ClientMessagePush.class);
		push(plane.getChunkWithCell(x, y).getCell(data.x, data.y).character(), Side.int2side(data.direction));
		timeStream.flushEvents();
	}
	public void aChangePlaces(String message) {
		ClientMessageChangePlaces data = gson.fromJson(message, ClientMessageChangePlaces.class);
		changePlaces(plane.getChunkWithCell(x, y).getCell(data.x, data.y).character());
		timeStream.flushEvents();
	}
	public void aMakeSound(String message) {
		ClientMessageMakeSound data = gson.fromJson(message, ClientMessageMakeSound.class);
		makeSound(StaticData.getSoundType(data.type));
		timeStream.flushEvents();
	}
	public void aJump(String message) {
		ClientMessageJump data = gson.fromJson(message, ClientMessageJump.class);
		jump(data.x, data.y);
		getTimeStream().flushEvents();
	}
	public void aShieldBash(String message) {
		ClientMessageShieldBash data = gson.fromJson(message, ClientMessageShieldBash.class);
		Character aim = plane.getChunkWithCell(x, y).getCell(data.x, data.y).character();
		if (aim == null) {
			shieldBash(data.x, data.y);
		} else {
			shieldBash(aim);
		}
		timeStream.flushEvents();		
	}
	public boolean inTimeStream(TimeStream timeStream) {
		return this.getTimeStream() == timeStream; 
	}
	
	public void aEnterState(String message) {
		ClientMessageEnterState data = gson.fromJson(message, ClientMessageEnterState.class);
		enterState(CharacterState.int2state(data.stateId));
		timeStream.flushEvents();
	}
	/**
	 * Sends data about PlayerHandler's parameters and surroundings to a client when 
	 * that client connects to the server with that player.
	 * @param message 
	 * @param ws
	 * @throws InterruptedException
	 */
	void aLoadContents(String message, Connection connection) throws InterruptedException {
		// Almost the same as LOAD_LOCATON_CONTENTS
		ClientMessageAuth clientData = gson.fromJson(message, ClientMessageAuth.class);
		if (clientData.login.equals("")) {
		// Login is empty
			connection.send("[{\"e\":\"loadContents\",\"error\":0}]");
		} else if (clientData.password.equals("")) {
		// Password is empty
			connection.send("[{\"e\":\"loadContents\",\"error\":1}]");
		}
		Account account = Accounts.account(clientData.login);
		if (account == null) {
		// No such account
			connection.send("[{\"e\":\"loadContents\",\"error\":2}]");
		} else if (!account.password.equals(clientData.password)) {
		// Client password doesn't match account password
			connection.send("[{\"e\":\"loadContents\",\"error\":3}]");
		} else if (!account.hasCharacterWithId(clientData.characterId)) {
			connection.send("[{\"e\":\"loadContents\",\"error\":4}]");
		} else {
		// Player login data is correct
			PlayerHandler player = connection.getPlayerHandler();
			if (player.isAuthorized()) {
				if (player.connection.isClosed()) {
				/* 
				 * If a PlayerHandler is authorized and associated with a 
				 * Connection that is already closed 
				 */
					connection.setPlayerHandler(player);
				} else {
				// Error: player is already in game
					connection.send("[{\"e\":\"loadContents\",\"error\":5}]");
					return;
				}
			} else {
			// If that player is not authorized
				connection.setPlayerHandler(player);
			}
			player.authorize();
			player.setConnection(connection);
			/*{ 	p : [(1)x, (2)y, (0)characterId, (4)name, (5)race, (6)class, 
			 *  		(7)maxHp, (8)maxMp, (9)maxEp, (10)hp, (11)mp, (12)ep, 
			 *  		(13)str, (14)dex, (15)wis, (16)itl, (17)items[], (18)equipment[], (19)spells[], (20)skills[],
			 *  		(21)ac, (22)ev, (23)resistances[]],
			 * 		online : [[characterId,x,y,name,maxHp,hp,effects,equipment(,cls,race)|(,type)]xM], 
			 * } 
			 */
			Chunk[] chunks = (Chunk[]) Arrays.asList(timeStream.chunks).toArray();
			EventQueue queue = new EventQueue()
				.add(ServerEvents.create("authenticationSuccessful", "1"))
				.add(ServerEvents.create("chunks", chunks))
				.add(ServerEvents.create("player", this));
			connection.send(queue.serialize());
		}
	}
}
