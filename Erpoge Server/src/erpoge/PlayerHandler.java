package erpoge;

import java.io.IOException;
import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.tootallnate.websocket.WebSocket;
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
import erpoge.terrain.Location;
import erpoge.terrain.Portal;
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
	private WebSocket connection;
	public PlayerHandler(String name, Race race, String cls,
			int x, int y) {
		super(name, race, cls, x, y);
	}
	/* Net setters */
	public void setConnection(WebSocket connection) {
		this.connection = connection;
	}
	public void deauthorize() {
		// Inform all characters who are on global map right now that
		// this character has left
		isAuthorized = false;
		world.onlinePlayers.remove(this.characterId);
		world.addEvent(new EventDeauthorization(characterId));
		world.flushEvents(Location.TO_WORLD, this);
	}
	public void authorize() {
		// Inform all characters who are on global map right now that
		// this character has entered the game
		isAuthorized = true;
		world.onlinePlayers.put(this.characterId, this);
		if (location != null) {
			world.addEvent(new EventWorldEntering(characterId, name, cls, race.race2int(),
					worldX, worldY));
			world.flushEvents(Location.TO_WORLD, this);
		}
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
		PlayerCharacter newPlayer = world.createPlayer(
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
		attack(location.characters.get(messageAttack.aimId));
		location.flushEvents(Location.TO_LOCATION, this);
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
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aPutOn(String message) throws IOException {
		// put on an item
		// v - item id
		int itemId = gson.fromJson(message,	ClientMessagePutOn.class).itemId;
		putOn(inventory.getUnique(itemId), false);
		if (isOnGlobalMap()) {
			world.sendOutEvent(this, new EventPutOn(characterId, itemId));
		} else {
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}
	public void aTakeOff(String message) throws IOException {
		// take off an item
		// v - item id
		int itemId = gson.fromJson(message,
				ClientMessageTakeOff.class).itemId;
		takeOff(equipment.getUnique(itemId));
		if (isOnGlobalMap()) {
			world.sendOutEvent(this, new EventTakeOff(characterId, itemId));
		} else {
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}
	public void aPickUpPile(String message) throws IOException {
		ClientMessagePickUpPile data = gson.fromJson(message,
				ClientMessagePickUpPile.class);
		connection.character.pickUp(location.cells[x][y].items.getPile(data.typeId).separatePile(data.amount));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aPickUpUnique(String message) throws IOException {
		ClientMessagePickUpUnique data = gson.fromJson(message,
				ClientMessagePickUpUnique.class);
		pickUp(location.cells[x][y].items.getUnique(data.itemId));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aDropPile(String message) throws IOException {
		// drop an item
		ClientMessageDropPile data = gson.fromJson(message,
				ClientMessageDropPile.class);
		drop(inventory.getPile(data.typeId).separatePile(data.amount));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aDropUnique(String message) throws IOException {
		// drop an item
		ClientMessageDropUnique data = gson.fromJson(message,
				ClientMessageDropUnique.class);
		drop(inventory.getUnique(data.itemId));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aWorldTravel(String message) throws IOException {
		ClientMessageWorldTravel clientDataWT = gson.fromJson(message, ClientMessageWorldTravel.class);
		connection.character.worldTravel(clientDataWT.x, clientDataWT.y);
	}
	public void aDeauth(String message) throws IOException {
		if (connection.character != null) {
			connection.character.deauthorize();
		}
	}
	public void aChatMessage(String message) throws IOException {
		ClientMessageChatMessage clientDataCHM = gson.fromJson(message, ClientMessageChatMessage.class);
		connection.character.say(clientDataCHM.text);
	}
	public void aOpenContainer(String message) throws IOException {
		ClientMessageCoordinate containerCoord = gson.fromJson(message, ClientMessageCoordinate.class);
		connection.send("[{\"e\":\"openContainer\",\"items\":["+
				connection.character.location.jsonGetContainerContents(containerCoord.x, containerCoord.y)+"]}]");
	}
	public void aTakeFromContainer(String message) throws IOException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = location.getContainer(data.x, data.y);
		if (ItemsTypology.item(data.typeId).isUnique()) {
			takeFromContainer(container.getUnique(data.param), container);
		} else {
			takeFromContainer(container.getPile(data.typeId).separatePile(data.param), container);
		}			
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aPutToContainer(String message) throws IOException {
		ClientMessageTakeFromContainer data = gson.fromJson(message, ClientMessageTakeFromContainer.class);
		Container container = location.getContainer(data.x, data.y);
		if (ItemsTypology.item(data.typeId).isUnique()) {
			super.putToContainer(inventory.getUnique(data.param), container);
		} else {
			super.putToContainer(inventory.getPile(data.typeId).separatePile(data.param), container);
		}
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aCastSpell(String message) throws IOException {
		ClientMessageCastSpell data = gson.fromJson(message, ClientMessageCastSpell.class);
		connection.character.castSpell(data.spellId, data.x, data.y);
		connection.character.location.flushEvents(Location.TO_LOCATION, connection.character);
	}
	public void aShootMissile(String message) throws IOException {
		ClientMessageShootMissile data = gson.fromJson(message, ClientMessageShootMissile.class);
		shootMissile(data.x, data.y, inventory.getPile(data.missile).separatePile(1));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aUseObject(String message) throws IOException {
		ClientMessageUseObject data = gson.fromJson(message, ClientMessageUseObject.class);
		connection.character.useObject(data.x, data.y);
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aCheckOut(String message) throws IOException {
		connection.character.location().checkOut(connection.character);
	}
	public void aEnterLocation(String message) throws IOException {
		ClientMessageEnterLocation messageEnter = gson.fromJson(message, ClientMessageEnterLocation.class);
		Location location;
		if (world.locations.containsKey(new Coordinate(messageEnter.x, messageEnter.y))) {
			location = world.locations.get(new Coordinate(messageEnter.x, messageEnter.y));
		} else {
			location = world.createLocation(messageEnter.x, messageEnter.y, Main.DEFAULT_LOCATION_WIDTH,Main.DEFAULT_LOCATION_HEIGHT, Main.TEST_LOCATION_TYPE, "����� �������");
		}
		location.addCharacter(connection.character);
		String enteringData = jsonGetEnteringData(connection.character.isOnGlobalMap());
		connection.send(enteringData);
	}
	public void aLeaveLocation(String message) throws IOException {
		Portal portal = connection.character.getNearbyPortal();
		if (portal != null) {
			connection.character.goToAnotherLevel(portal);
		} else {
			connection.character.leaveLocation();
		}
		connection.send(connection.character.jsonGetEnteringData(connection.character.isOnGlobalMap()));
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
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public boolean isAuthorized() {
		return isAuthorized;
	}
	public void aPush(String message) {
		ClientMessagePush data = gson.fromJson(message, ClientMessagePush.class);
		push(location.cells[data.x][data.y].character(), Side.int2side(data.direction));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aChangePlaces(String message) {
		ClientMessageChangePlaces data = gson.fromJson(message, ClientMessageChangePlaces.class);
		Main.console(data.x+" "+data.y);
		changePlaces(location.cells[data.x][data.y].character());
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aMakeSound(String message) {
		ClientMessageMakeSound data = gson.fromJson(message, ClientMessageMakeSound.class);
		makeSound(SoundType.int2type(data.type));
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aJump(String message) {
		ClientMessageJump data = gson.fromJson(message, ClientMessageJump.class);
		jump(data.x, data.y);
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void aShieldBash(String message) {
		ClientMessageShieldBash data = gson.fromJson(message, ClientMessageShieldBash.class);
		Character aim = location.cells[data.x][data.y].character();
		if (aim == null) {
			shieldBash(data.x, data.y);
		} else {
			shieldBash(aim);
		}
		
		location.flushEvents(Location.TO_LOCATION, this);		
	}
}
