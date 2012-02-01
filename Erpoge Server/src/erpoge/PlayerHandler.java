package erpoge;

import java.io.IOException;
import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.tootallnate.websocket.WebSocket;
import erpoge.characters.CharacterManager;
import erpoge.characters.PlayerCharacter;
import erpoge.characters.Race;
import erpoge.characters.Character;
import erpoge.clientmessages.*;
import erpoge.itemtypes.ItemsTypology;
import erpoge.serverevents.EventDeauthorization;
import erpoge.serverevents.EventPutOn;
import erpoge.serverevents.EventTakeOff;
import erpoge.serverevents.EventWorldEntering;
import erpoge.terrain.Container;
import erpoge.terrain.HorizontalPlane;
import erpoge.terrain.Location;
import erpoge.terrain.Portal;
import erpoge.terrain.TimeStream;
import erpoge.objects.SoundType;

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
		getTimeStream().addEvent(new EventDeauthorization(characterId));
		getTimeStream().flushEvents();
	}
	public void authorize() {
		// Inform all characters who are on global map right now that
		// this character has entered the game
		isAuthorized = true;
	}
	
	/* Action handlers */
	
	public void aAccountRegister(String message) throws IOException {
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
	public void aPlayerCreate(String message) throws IOException {
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
	public void aAttack(String message) throws IOException {
		ClientMessageAttack messageAttack = gson.fromJson(message, ClientMessageAttack.class);
		for (Character ch : getTimeStream().characters) {
			if (ch.characterId == messageAttack.aimId) {
				attack(ch);
				getTimeStream().flushEvents();
				return;
			}
		}
		throw new Error("No character with id "+messageAttack.aimId);
	}
	public void aStep(String message) throws IOException {
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
		getTimeStream().flushEvents();
	}
	public void aPutOn(String message) throws IOException {
		// put on an item
		// v - item id
		int itemId = gson.fromJson(message,	ClientMessagePutOn.class).itemId;
		putOn(inventory.getUnique(itemId), false);
		getTimeStream().flushEvents();
	}
	public void aTakeOff(String message) throws IOException {
		// take off an item
		// v - item id
		int itemId = gson.fromJson(message,
				ClientMessageTakeOff.class).itemId;
		takeOff(equipment.getUnique(itemId));
		getTimeStream().flushEvents();
	}
	public void aPickUpPile(String message) throws IOException {
		ClientMessagePickUpPile data = gson.fromJson(message,
				ClientMessagePickUpPile.class);
		connection.character.pickUp(plane.getCell(x,y).items.getPile(data.typeId).separatePile(data.amount));
		getTimeStream().flushEvents();
	}
	public void aPickUpUnique(String message) throws IOException {
		ClientMessagePickUpUnique data = gson.fromJson(message,
				ClientMessagePickUpUnique.class);
		pickUp(plane.getCell(x, y).items.getUnique(data.itemId));
		getTimeStream().flushEvents();
	}
	public void aDropPile(String message) throws IOException {
		// drop an item
		ClientMessageDropPile data = gson.fromJson(message,
				ClientMessageDropPile.class);
		drop(inventory.getPile(data.typeId).separatePile(data.amount));
		getTimeStream().flushEvents();
	}
	public void aDropUnique(String message) throws IOException {
		// drop an item
		ClientMessageDropUnique data = gson.fromJson(message,
				ClientMessageDropUnique.class);
		drop(inventory.getUnique(data.itemId));
		getTimeStream().flushEvents();
	}
	public void aDeauth(String message) throws IOException {
		if (connection.character != null) {
			connection.character.deauthorize();
		}
	}
	public void aChatMessage(String message) throws IOException {
		ClientMessageChatMessage data = gson.fromJson(message, ClientMessageChatMessage.class);
		connection.character.say(data.text);
	}
	public void aOpenContainer(String message) throws IOException {
		ClientMessageCoordinate data = gson.fromJson(message, ClientMessageCoordinate.class);
		connection.send("[{\"e\":\"openContainer\",\"items\":["+
				plane.getChunk(data.x, data.y).jsonGetContainerContents(data.x, data.y)+"]}]");
	}
	public void aTakeFromContainer(String message) throws IOException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = plane.getChunk(data.x, data.y).getContainer(data.x, data.y);
		if (ItemsTypology.item(data.typeId).isUnique()) {
			takeFromContainer(container.getUnique(data.param), container);
		} else {
			takeFromContainer(container.getPile(data.typeId).separatePile(data.param), container);
		}			
		getTimeStream().flushEvents();
	}
	public void aPutToContainer(String message) throws IOException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = plane.getChunk(data.x, data.y).getContainer(data.x, data.y);
		if (ItemsTypology.item(data.typeId).isUnique()) {
			super.putToContainer(inventory.getUnique(data.param), container);
		} else {
			super.putToContainer(inventory.getPile(data.typeId).separatePile(data.param), container);
		}
		getTimeStream().flushEvents();
	}
	public void aCastSpell(String message) throws IOException {
		ClientMessageCastSpell data = gson.fromJson(message, ClientMessageCastSpell.class);
		connection.character.castSpell(data.spellId, data.x, data.y);
		getTimeStream().flushEvents();
	}
	public void aShootMissile(String message) throws IOException {
		ClientMessageShootMissile data = gson.fromJson(message, ClientMessageShootMissile.class);
		shootMissile(data.x, data.y, inventory.getPile(data.missile).separatePile(1));
		getTimeStream().flushEvents();
	}
	public void aUseObject(String message) throws IOException {
		ClientMessageUseObject data = gson.fromJson(message, ClientMessageUseObject.class);
		connection.character.useObject(data.x, data.y);
		getTimeStream().flushEvents();
	}
	public void aCheckOut(String message) throws IOException {
		connection.character.getTimeStream().checkOut(connection.character);
	}
	public void aAnswer(String message) throws IOException {
		ClientMessageAnswer messageAnswer = gson.fromJson(message, ClientMessageAnswer.class);
		connection.character.dialogueAnswer(messageAnswer.answerId);
	}
	public void aStartConversation(String message) throws IOException {
		ClientMessageStartConversation data = gson.fromJson(message, ClientMessageStartConversation.class);
		startConversation(data.characterId);
	}
	public void aIdle(String message) {
		idle();
		getTimeStream().flushEvents();
	}
	public boolean isAuthorized() {
		return isAuthorized;
	}
	public void aPush(String message) {
		ClientMessagePush data = gson.fromJson(message, ClientMessagePush.class);
		push(plane.getChunk(x, y).getCell(data.x, data.y).character(), Side.int2side(data.direction));
		getTimeStream().flushEvents();
	}
	public void aChangePlaces(String message) {
		ClientMessageChangePlaces data = gson.fromJson(message, ClientMessageChangePlaces.class);
		changePlaces(plane.getChunk(x, y).getCell(data.x, data.y).character());
		getTimeStream().flushEvents();
	}
	public void aMakeSound(String message) {
		ClientMessageMakeSound data = gson.fromJson(message, ClientMessageMakeSound.class);
		makeSound(SoundType.int2type(data.type));
		getTimeStream().flushEvents();
	}
	public void aJump(String message) {
		ClientMessageJump data = gson.fromJson(message, ClientMessageJump.class);
		jump(data.x, data.y);
		getTimeStream().flushEvents();
	}
	public void aShieldBash(String message) {
		ClientMessageShieldBash data = gson.fromJson(message, ClientMessageShieldBash.class);
		Character aim = plane.getChunk(x, y).getCell(data.x, data.y).character();
		if (aim == null) {
			shieldBash(data.x, data.y);
		} else {
			shieldBash(aim);
		}
		
		getTimeStream().flushEvents();		
	}
	public boolean inTimeStream(TimeStream timeStream) {
		return this.getTimeStream() == timeStream; 
	}
}
