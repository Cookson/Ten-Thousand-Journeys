package erpoge.core.net;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.tootallnate.websocket.WebSocket;
import erpoge.core.Character;
import erpoge.core.Main;
import erpoge.core.TimeStream;
import erpoge.core.characters.CharacterManager;
import erpoge.core.characters.CharacterState;
import erpoge.core.characters.PlayerCharacter;
import erpoge.core.characters.Race;
import erpoge.core.itemtypes.ItemsTypology;
import erpoge.core.meta.Side;
import erpoge.core.net.clientmessages.*;
import erpoge.core.net.serverevents.EventAuthentificationSuccessful;
import erpoge.core.net.serverevents.EventChunkContents;
import erpoge.core.net.serverevents.EventDeauthorization;
import erpoge.core.net.serverevents.EventPlayerData;
import erpoge.core.net.serverevents.EventPutOn;
import erpoge.core.net.serverevents.EventTakeOff;
import erpoge.core.net.serverevents.EventWorldEntering;
import erpoge.core.objects.SoundType;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.Container;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.Portal;

/**
 * Extension of PlayerCharacter that simply contains methods
 * for incoming data handling and sending outcoming data to clients.
 * Methods transform raw string/integer data to game objects, if necessary,
 * and then pass these objects to Character methods (which do not
 * apply raw data in many cases)
 */
public class PlayerHandler extends PlayerCharacter {
	private static final Gson gson = new Gson();
	private static final Gson gsonIncludesStatic = new GsonBuilder()
    	.excludeFieldsWithModifiers(Modifier.TRANSIENT)
    	.create();
	public WebSocket connection;
	public PlayerHandler(HorizontalPlane plane, int x, int y, String name, Race race, String cls) {
		super(plane, x, y, name, race, cls);
	}
	/* Net setters */
	public void setConnection(WebSocket connection) {
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
	
	public void aAccountRegister(String message) throws InterruptedException {
		ClientMessageAccountRegister accountRegsterData = gson.fromJson(message, ClientMessageAccountRegister.class);
		if (Accounts.hasAccount(accountRegsterData.l)) {
			connection.send("{\"a\":"+MainHandler.ACCOUNT_REGISTER+",\"error\":1}");
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
			connection.send("[{\"e\":\"login\","+account.jsonPartGetCharactersAuthInfo()+"}]");
		}
	}
	public void aPlayerCreate(String message) throws InterruptedException {
		ClientMessagePlayerCreate playerCreateData = gson.fromJson(message, ClientMessagePlayerCreate.class);
		Account accountPlayerCreate = Accounts.account(playerCreateData.account);
		PlayerCharacter newPlayer = CharacterManager.createPlayer(
				playerCreateData.name, 
				Race.int2race(playerCreateData.race), 
				playerCreateData.cls, 
				13, 26);
		accountPlayerCreate.addCharacter(newPlayer);
		accountPlayerCreate.accountStatistic();
		connection.send("[{\"e\":\"login\","+accountPlayerCreate.jsonPartGetCharactersAuthInfo()+"}]");
	}
	public void aAttack(String message) throws InterruptedException {
		ClientMessageAttack data = gson.fromJson(message, ClientMessageAttack.class);
		for (Character ch : timeStream.characters) {
			if (ch.characterId == data.aimId) {
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
		takeOff(equipment.getUnique(itemId));
		timeStream.flushEvents();
	}
	public void aPickUpPile(String message) throws InterruptedException {
		ClientMessagePickUpPile data = gson.fromJson(message,
				ClientMessagePickUpPile.class);
		connection.character.pickUp(plane.getCell(x,y).items.getPile(data.typeId).separatePile(data.amount));
		timeStream.flushEvents();
	}
	public void aPickUpUnique(String message) throws InterruptedException {
		ClientMessagePickUpUnique data = gson.fromJson(message,
				ClientMessagePickUpUnique.class);
		pickUp(plane.getCell(x, y).items.getUnique(data.itemId));
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
		if (connection.character != null) {
			connection.character.deauthorize();
		}
	}
	public void aChatMessage(String message) throws InterruptedException {
		ClientMessageChatMessage data = gson.fromJson(message, ClientMessageChatMessage.class);
		connection.character.say(data.text);
	}
	public void aOpenContainer(String message) throws InterruptedException {
		ClientMessageCoordinate data = gson.fromJson(message, ClientMessageCoordinate.class);
		connection.send("[{\"e\":\"openContainer\",\"items\":["+
				plane.getChunkWithCell(data.x, data.y).jsonGetContainerContents(data.x, data.y)+"]}]");
	}
	public void aTakeFromContainer(String message) throws InterruptedException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = plane.getChunkWithCell(data.x, data.y).getContainer(data.x, data.y);
		if (ItemsTypology.item(data.typeId).isUnique()) {
			takeFromContainer(container.getUnique(data.param), container);
		} else {
			takeFromContainer(container.getPile(data.typeId).separatePile(data.param), container);
		}			
		timeStream.flushEvents();
	}
	public void aPutToContainer(String message) throws InterruptedException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = plane.getChunkWithCell(data.x, data.y).getContainer(data.x, data.y);
		if (ItemsTypology.item(data.typeId).isUnique()) {
			super.putToContainer(inventory.getUnique(data.param), container);
		} else {
			super.putToContainer(inventory.getPile(data.typeId).separatePile(data.param), container);
		}
		timeStream.flushEvents();
	}
	public void aCastSpell(String message) throws InterruptedException {
		ClientMessageCastSpell data = gson.fromJson(message, ClientMessageCastSpell.class);
		connection.character.castSpell(data.spellId, data.x, data.y);
		timeStream.flushEvents();
	}
	public void aShootMissile(String message) throws InterruptedException {
		ClientMessageShootMissile data = gson.fromJson(message, ClientMessageShootMissile.class);
		Main.console(data.x+" "+data.y+" "+data.missile+" "+data.unique);
		if (data.unique) {
			shootMissile(data.x, data.y, inventory.getUnique(data.missile));
		} else {
			shootMissile(data.x, data.y, inventory.getPile(data.missile).separatePile(1));
		}
		timeStream.flushEvents();
	}
	public void aUseObject(String message) throws InterruptedException {
		ClientMessageUseObject data = gson.fromJson(message, ClientMessageUseObject.class);
		connection.character.useObject(data.x, data.y);
		timeStream.flushEvents();
	}
	public void aCheckOut(String message) throws InterruptedException {
		timeStream.checkOut(this);
	}
	public void aAnswer(String message) throws InterruptedException {
		ClientMessageAnswer messageAnswer = gson.fromJson(message, ClientMessageAnswer.class);
		connection.character.dialogueAnswer(messageAnswer.answerId);
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
		makeSound(SoundType.int2type(data.type));
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
	public void getEnteringEventQueue() {
		/*{ 	p : [(1)x, (2)y, (0)characterId, (4)name, (5)race, (6)class, 
			 *  		(7)maxHp, (8)maxMp, (9)maxEp, (10)hp, (11)mp, (12)ep, 
			 *  		(13)str, (14)dex, (15)wis, (16)itl, (17)items[], (18)equipment[], (19)spells[], (20)skills[],
			 *  		(21)ac, (22)ev, (23)resistances[]],
			 * 		online : [[characterId,x,y,name,maxHp,hp,effects,equipment(,cls,race)|(,type)]xM], 
			 * } 
			 */
		EventQueue queue = new EventQueue(connection);
		queue.addEvent(new EventAuthentificationSuccessful());
		
		for (Chunk chunk : timeStream.chunks) {
			queue.addEvent(new EventChunkContents(chunk));
		}
		Collection collection = new ArrayList();
		collection.add(x);
		collection.add(y);
		collection.add(characterId);
		collection.add(name);
		collection.add(race.race2int());
		collection.add(cls);
		collection.add(maxHp);
		collection.add(maxMp);
		collection.add(maxEp);
		collection.add(hp);
		collection.add(mp);
		collection.add(ep);
		collection.add(str);
		collection.add(dex);
		collection.add(wis);
		collection.add(itl);
		collection.add(inventory.getDataForSending());
		collection.add(equipment.getDataForSending());
		collection.add(spells);
		collection.add(skills);
		collection.add(armor);
		collection.add(evasion);
		collection.add(new int[] {fireRes, coldRes, poisonRes, acidRes});
		
		queue.addEvent(new EventPlayerData(collection));
		queue.flush();
	}
	public void aEnterState(String message) {
		ClientMessageEnterState data = gson.fromJson(message, ClientMessageEnterState.class);
		enterState(CharacterState.int2state(data.stateId));
		timeStream.flushEvents();
	}
}
