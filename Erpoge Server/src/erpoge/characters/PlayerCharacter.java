package erpoge.characters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.tootallnate.websocket.WebSocket;

import erpoge.Chat;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.MainHandler;
import erpoge.clientmessages.ServerMessageCharacterAuthInfo;
import erpoge.inventory.Item;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.serverevents.*;
import erpoge.terrain.Container;
import erpoge.terrain.Location;
import erpoge.terrain.World;

public class PlayerCharacter extends Character {
	protected final String cls;
	protected int level = 1;
	public final int race;
	
	private int str;
	private int dex;
	private int wis;
	private int itl;
	private int armor;
	private int evasion;
	private final ArrayList<Integer> protections = new ArrayList<Integer>(
			CharacterTypes.NUMBER_OF_PROTECTIONS);
	
	public int worldX;
	public int worldY;
	private HashMap<String, Integer> skills = new HashMap<String, Integer>();
	private int party = 0;
	private PlayerCharacter inviter;
	private NonPlayerCharacter dialoguePartner;
	public World world;
	public boolean checkedOut = false;
	private boolean isAuthorized = false;
	public static final String[] skillNames = {"mace", "axe", "shield",
			"sword", "polearm", "stealth", "reaction", "bow", "dagger",
			"unarmed", "staff", "kinesis", "fire", "cold", "necros",
			"demonology", "mental", "magicItems", "craft", "traps",
			"effraction", "mechanics", "alchemy"};
	public WebSocket connection;
	public PlayerCharacter(String type, String name, int race, String cls, Location location,
			int x, int y) {
		super(type, name, x, y);
		this.cls = cls;
		this.race = race;
		maxHp = 100000;
		maxMp = 100000;
		hp = 100000;
		mp = 100000;
		for (String skillName : skillNames) {
			skills.put(skillName, 0);
		}
		fraction = 1;
	}

	public int level() {
		return level;
	}

	public String cls() {
		return cls;
	}
	public int getArmor() {
		return armor;
	}
	public int getEvasion() {
		return evasion;
	}
	public void say(String message) {
		if (this.isOnGlobalMap()) {
			// World message
			Chat.worldMessage(this, message);
			Main.console(message);
			world.flushEvent(new EventChatMessage(characterId, message));
		} else {
			// location message
			Chat.locationMessage(this, message);
			location.addEvent(new EventChatMessage(characterId, message));
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void putOn(int itemId) {
		
		super.putOn(inventory.getUnique(itemId), false);
		if (this.isOnGlobalMap()) {
			world.sendOutEvent(this, new EventPutOn(characterId, itemId));
		} else {
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void castSpell(int spellId, int x, int y) {
		if (this.isOnGlobalMap()) {
			throw new Error("Spell cast from character " + name
					+ " on global map!");
		} else {
			super.castSpell(spellId, x, y);
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}
	public void die() {
		super.die();
		try {
			connection.send(jsonGetEnteringData(isOnGlobalMap()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Character ch : location.characters.values()) {
			if (ch instanceof PlayerCharacter) {
				return;
			}
		}
		location.noMorePlayersInLocation = true;
	}
	public void takeOff(int itemId) {
		super.takeOff(ammunition.getUnique(itemId));
		if (this.isOnGlobalMap()) {
			world.sendOutEvent(this, new EventTakeOff(characterId, itemId));
		} else {
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void drop(int typeId, int amount) {
		if (this.isOnGlobalMap()) {
			throw new Error("Drop on global map");
		} else {
			super.drop(inventory.getPile(typeId).separatePile(amount));
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}
	public void drop(int itemId) {
		if (this.isOnGlobalMap()) {
			throw new Error("Drop on global map");
		} else {
			super.drop(inventory.getUnique(itemId));
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void pickUp(int typeId, int amount) {
		if (this.isOnGlobalMap()) {
			throw new Error("Pick up on global map");
		} else {
			super.pickUp(location.cells[x][y].items.getPile(typeId).separatePile(amount));
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}
	
	public void pickUp(int itemId) {
		if (this.isOnGlobalMap()) {
			throw new Error("Pick up on global map");
		} else {
			super.pickUp(location.cells[x][y].items.getUnique(itemId));
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void attack(Character aim) {
		if (this.isOnGlobalMap()) {
			throw new Error("Attack on global map");
		} else {
			super.attack(aim);
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void shootMissile(int x, int y, int missile) {
		if (this.isOnGlobalMap()) {
			throw new Error("Shoot missile on global map");
		} else {
			super.shootMissile(x, y, inventory.getPile(missile).separatePile(1));
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void useObject(int x, int y) {
		if (this.isOnGlobalMap()) {
			throw new Error("Use object on global map");
		} else {
			super.useObject(x, y);
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void takeFromContainer(int typeId, int amount, int x, int y) {
		if (this.isOnGlobalMap()) {
			throw new Error("Take from contaier on global map");
		} else {
			super.takeFromContainer(inventory.getPile(typeId).separatePile(amount), x, y);
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public void putToContainer(int typeId, int amount, int x, int y) {
		if (this.isOnGlobalMap()) {
			throw new Error("Put to contaier on global map");
		} else {
			super.putToContainer(inventory.getPile(typeId).separatePile(amount), x, y);
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public String jsonGetEnteringData(boolean isWorld) {
		// ������, ����������� ������� ��� ������ � ���� �� �����������
		// ���������
		// The same data is used for entering both global and local map
		// in: isWorld - is character in world. If it is false, then location
		// data is formed.
		// Location data differs from world data only by cell contents
		// structure.
		/*
		 * on world map: { onGlobalMap: true, w :
		 * {w,h,c:[[ground,forest,road,river,race,[objects]]xN]}, p : [
		 * characterId, name, race, class, level, maxHp, maxMp, str, dex, wis,
		 * itl, items, ammunition, spells, skills, worldX, worldY ], islead :
		 * boolean, online :
		 * [[characterId,name,class,race,party,worldX,worldY]xM], chat :
		 * [name,message, name,message ...] || 0, invite :
		 * [inviterId,inviterName] || 0 }, in location: { onGlobalMap: false, l
		 * : {w,h,locationId,c:[[ground,forest,road,river,race,[objects]]xN]}, p
		 * : [ characterId, name, race, class, level, maxHp, maxMp, str, dex,
		 * wis, itl, items, ammunition, spells, skills, x, y ], islead :
		 * boolean, online :
		 * [[characterId,x,y,name,maxHp,hp,maxMp,mp,effects,ammunition
		 * (,cls,race)|(,type)]xM], }
		 */
		String answer = "{\"a\":"
				+ MainHandler.LOAD_CONTENTS
				+ ",\"onGlobalMap\":"
				+ isWorld
				+ ",\""
				+ (isWorld ? "w" : "l")
				+ "\":{"
				+ (isWorld ? world.jsonPartGetWorldContents() : location
						.jsonPartGetLocationContents()) + "},";
		answer += "\"p\":[" + characterId + ",\"" + name + "\"," + race + ",\""
				+ cls + "\"," + level + "," + maxHp + "," + maxMp + ","
				+ (isWorld ? "" : hp + "," + mp + ",") + str + "," + dex + ","
				+ wis + "," + itl + ",["+inventory.jsonGetContents()+"],"+ammunition.jsonGetAmmunition()+",[";

		// spells
		int i = 0;
		int iterations = spells.size() - 1;
		if (iterations > -1) {
			for (; i < iterations; i++) {
				answer += spells.get(i) + ",";
			}
			answer += spells.get(i);
		}
		answer += "],[";

		// skills
		i = 0;
		for (iterations = skillNames.length - 1; i < iterations; i++) {
			answer += skills.get(skillNames[i]) + ",";
		}
		answer += skills.get(skillNames[i]) + "],";

		// worldX, worldY, islead
		boolean isLead = true;
		answer += (isWorld ? worldX : x) + "," + (isWorld ? worldY : y)
				+ "],\"islead\":" + isLead + ",\"online\":[";

		// online characters
		ArrayList<Character> onlinePlayers = new ArrayList<Character>((isWorld)
				? world.onlinePlayers.values()
				: location.getCharacters());
		i = 0;
		iterations = onlinePlayers.size() - 1;

		if (iterations > -1) {
			if (isWorld) {
				for (; i < iterations; i++) {
					PlayerCharacter player = (PlayerCharacter) onlinePlayers
							.get(i);
					answer += "[" + player.characterId + ",\"" + player.name
							+ "\",\"" + player.cls + "\"," + player.race + ","
							+ player.party + "," + player.worldX + ","
							+ player.worldY + "],";
				}
				PlayerCharacter player = (PlayerCharacter) onlinePlayers.get(i);
				answer += "[" + player.characterId + ",\"" + player.name
						+ "\",\"" + player.cls + "\"," + player.race + ","
						+ player.party + "," + player.worldX + ","
						+ player.worldY + "]";
			} else {
				for (; i < iterations; i++) {
					Character character = onlinePlayers.get(i);
					answer += "[" + character.characterId + "," + character.x
							+ "," + character.y + ",\"" + character.name
							+ "\"," + character.fraction + ","
							+ character.maxHp + "," + character.hp + ","
							+ character.maxMp + "," + character.mp + ","
							+ character.jsonGetEffects() + ","
							+ character.jsonGetAmmunition();
					if (character instanceof PlayerCharacter) {
						PlayerCharacter player = (PlayerCharacter) onlinePlayers
								.get(i);
						answer += ",\"" + player.cls + "\"," + player.race
								+ "],";
					} else {
						answer += ",\"" + character.type + "\"],";
					}
				}
				Character character = onlinePlayers.get(i);
				answer += "[" + character.characterId + "," + character.x + ","
						+ character.y + ",\"" + character.name + "\","
						+ character.fraction + "," + character.maxHp + ","
						+ character.hp + "," + character.maxMp + ","
						+ character.mp + "," + character.jsonGetEffects() + ","
						+ character.jsonGetAmmunition();
				if (character instanceof PlayerCharacter) {
					PlayerCharacter player = (PlayerCharacter) onlinePlayers
							.get(i);
					answer += ",\"" + player.cls + "\"," + player.race + "]";
				} else {
					answer += ",\"" + character.type + "\"]";
				}
			}
		}

		answer += "],\"chat\":[";
		// chat messages and invite
		Chat.Message[] messages = Chat.getMessagesAfter(System
				.currentTimeMillis() - 1000 * 60 * 10);

		if (messages.length > 0) {
			for (i = 0; i < messages.length - 1; i++) {
				answer += "\"" + messages[i].player.name + "\",\""
						+ messages[i].message + "\",";
			}
			answer += "\"" + messages[i].player.name + "\",\""
					+ messages[i].message + "\"";
		}
		answer += "],";
		if (inviter != null) {
			answer += "\"invite\":[" + inviter.characterId + "," + inviter.name
					+ "],";
		}

		// entering
		answer += "\"en\":false}";
		return answer;
	}

	public String jsonGetAuthData() {
		/*
		 * { onGlobalMap : boolean, worldX : int, worldY : int, party : int,
		 * characterId : int, name : string }
		 */
		return "{\"onGlobalMap\":" + (location == null) + ",\"worldX\":"
				+ worldX + ",\"worldY\":" + worldY + ",\"party\":" + party
				+ ",\"characterId\":" + characterId + ",\"name\":\"" + name
				+ "\"}";
	}

	public void leaveLocation() {
		worldX = location.worldX;
		worldY = location.worldY;
		energy = 0;
		hp = maxHp;
		mp = maxMp;
		location.removeCharacter(this);
	}

	public void worldTravel(int x, int y) {
		worldX = x;
		worldY = y;
		world.flushEvent(new EventWorldTravel(x, y, characterId));
//		world.addEvent();
//		world.flushEvents(Location.TO_WORLD, this);
	}

	public String jsonGetWorldTravel(int x, int y) {
		if (canTravelTo(x, y)) {
			return "{\"x\":" + x + ",\"y\":" + y + "}";
		} else {
			return "{\"x\":" + worldX + ",\"y\":" + worldY + "}";
		}
	}

	private boolean canTravelTo(int x, int y) {
		return true;
	}

	private void flushEvents() {
		if (isOnGlobalMap()) {
			world.flushEvents(Location.TO_WORLD, this);
		} else {
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}

	public boolean isAuthorized() {
		return isAuthorized;
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
			world.addEvent(new EventWorldEntering(characterId, name, cls, race,
					worldX, worldY));
			world.flushEvents(Location.TO_WORLD, this);
		}
	}

	public void setConnection(WebSocket connection) {
		this.connection = connection;
	}

	public Location getPortalLocation() {
		Set<Coordinate> keys = location.locationPortals.keySet();
		for (Coordinate k : keys) {
			if (k.isNear(x, y) || k.x == x && k.y == y) {
				return location.locationPortals.get(k);
			}
		}
		return null;
	}
	public void dialogueAnswer(int answerIndex) {
		say(dialoguePartner.dialogues.get(this).getAnswerText(answerIndex));
		dialoguePartner.proceedToNextDialoguePoint(this, answerIndex);
		moveTime(500);
		location.flushEvents(Location.TO_LOCATION, this);
	}
	public void startConversation(int characterId) {
		dialoguePartner = (NonPlayerCharacter) location.characters
				.get(characterId);
		if (dialoguePartner.dialogue != null) {
			dialoguePartner.applyConversationStarting(this);
			location.flushEvents(Location.TO_LOCATION, this);
		}
	}
}
