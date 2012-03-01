package erpoge.core.characters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.tootallnate.websocket.WebSocket;

import erpoge.core.Character;
import erpoge.core.Main;
import erpoge.core.inventory.Item;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.itemtypes.ItemsTypology;
import erpoge.core.meta.Coordinate;
import erpoge.core.net.Chat;
import erpoge.core.net.MainHandler;
import erpoge.core.net.clientmessages.ServerMessageCharacterAuthInfo;
import erpoge.core.net.serverevents.*;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.Container;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.Portal;
import erpoge.core.terrain.TerrainBasics;

public class PlayerCharacter extends Character {
	protected final String cls;
	public final Race race;
	
	protected int str;
	protected int dex;
	protected int wis;
	protected int itl;
	
	private final ArrayList<Integer> protections = new ArrayList<Integer>(
			CharacterTypes.NUMBER_OF_PROTECTIONS);
	
	protected HashMap<String, Integer> skills = new HashMap<String, Integer>();
	protected int party = 0;
	protected PlayerCharacter inviter;
	protected NonPlayerCharacter dialoguePartner;
	public boolean checkedOut = false;
	protected boolean isAuthorized = false;
	public static final String[] skillNames = {"mace", "axe", "shield",
			"sword", "polearm", "stealth", "reaction", "bow", "dagger",
			"unarmed", "staff", "kinesis", "fire", "cold", "necros",
			"demonology", "mental", "magicItems", "craft", "traps",
			"effraction", "mechanics", "alchemy"};
	public WebSocket connection;
	public PlayerCharacter(HorizontalPlane plane, int x, int y, String name, Race race, String cls) {
		super(plane, x, y, "player", name);
		this.cls = cls;
		this.race = race;
		this.plane = plane;
		this.plane.getCell(x, y).setPassability(TerrainBasics.PASSABILITY_SEE);
		maxMp = 30000;
		maxHp = 100000;
		maxEp = 100;
		hp = 100000;
		mp = 30000;
		ep = 100;
		for (String skillName : skillNames) {
			skills.put(skillName, 0);
		}
		fraction = 1;
	}

	/* Getters */
	public String toString() {
		return name+" the "+cls;
	}
	public String getCls() {
		return cls;
	}
	/* Setters */
	public void move(int x, int y) {
		int prevX = this.x;
		int prevY = this.y;
		super.move(x,y);
		if (
			plane.getChunkRoundedCoord(prevX) != plane.getChunkRoundedCoord(x)
			|| plane.getChunkRoundedCoord(prevY) != plane.getChunkRoundedCoord(y)
		) {
		// If player moves to another chunk, load chunks
			timeStream.loadApproachedChunks(plane, x, y);
			timeStream.unloadUnusedChunks(plane);
		}
	}
	/* Actions */
	public void say(String message) {
		// location message
		Chat.locationMessage(this, message);
		getTimeStream().addEvent(new EventChatMessage(characterId, message));
		getTimeStream().flushEvents();
	}
	public void die() {
		super.die();
		timeStream.removeCharacter(this);
	}
	public void startConversation(int characterId) {
		dialoguePartner = (NonPlayerCharacter) timeStream.getCharacterById(characterId);
		if (dialoguePartner.hasDialogue()) {
			dialoguePartner.applyConversationStarting(this);
			getTimeStream().flushEvents();
		}
	}
	/* Travelling */
	public void goToAnotherLevel(HorizontalPlane newPlane, int x, int y) {
	/**
	 * Transports character to another level of current location.
	 */
		this.plane = newPlane;
		this.x = x;
		this.y = y;
	}
	public void dialogueAnswer(int answerIndex) {
		say(dialoguePartner.dialogues.get(this).getAnswerText(answerIndex));
		dialoguePartner.proceedToNextDialoguePoint(this, answerIndex);
		moveTime(500);
		getTimeStream().flushEvents();
	}
	
	/* Data */
	public Set<Chunk> getClosestChunks() {
		Set<Chunk> answer = new HashSet<Chunk>();
		Chunk playerChunk = plane.getChunkWithCell(x, y);
		answer.add(playerChunk);
		answer.add(plane.getChunkByCoord(playerChunk.getX()-Chunk.WIDTH, playerChunk.getY()-Chunk.WIDTH));
		answer.add(plane.getChunkByCoord(playerChunk.getX(), playerChunk.getY()-Chunk.WIDTH));
		answer.add(plane.getChunkByCoord(playerChunk.getX()+Chunk.WIDTH, playerChunk.getY()-Chunk.WIDTH));
		
		answer.add(plane.getChunkByCoord(playerChunk.getX()-Chunk.WIDTH, playerChunk.getY()));
		answer.add(plane.getChunkByCoord(playerChunk.getX()+Chunk.WIDTH, playerChunk.getY()));
		
		answer.add(plane.getChunkByCoord(playerChunk.getX()-Chunk.WIDTH, playerChunk.getY()+Chunk.WIDTH));
		answer.add(plane.getChunkByCoord(playerChunk.getX(), playerChunk.getY()+Chunk.WIDTH));
		answer.add(plane.getChunkByCoord(playerChunk.getX()+Chunk.WIDTH, playerChunk.getY()+Chunk.WIDTH));
		return answer;
	}
	public String jsonPartGetEnteringData() {
		/**
		 * The same data is used for entering both global and local map
		 * 
		 * @param isWorld Is character in world. If it is false, then location
		 * 		data is formed. Location data differs from world data only by cell 
		 * 		contents structure.
		 */
		/* In world:
		 * {
		 * 		w :{w,h,c:[[ground,forest,road,river,race,[objects]]xN]}, 
		 *  	p : [(0)characterId, (1)worldX, (2)worldY, (3)isLead, (4)name, (5)race, (6)class, 
		 *  		(7)maxHp, (8)maxMp, (9)maxEp, (10)hp, (11)mp, (12)ep, 
		 *  		(13)str, (14)dex, (15)wis, (16)itl, (17)items[], (18)equipment[], (19)spells[], (20)skills[],
		 *  		(21)ac, (22)ev, (23)resistances[]],
		 * 		online : [[characterId,name,class,race,party,worldX,worldY]xM], 
		 * 		chat : [name,message, name,message ...] || 0, 
		 * 		invite : [inviterId,inviterName] || 0 
		 * }
		 */ 
		/* In location: 
		 * { 	
		 * 		l: {w,h,locationId,c:[[ground,forest,road,river,race,[objects]]xN]}, 
		 * 		p : [(0)characterId, (1)worldX, (2)worldY, (3)isLead, (4)name, (5)race, (6)class, 
		 *  		(7)maxHp, (8)maxMp, (9)maxEp, (10)hp, (11)mp, (12)ep, 
		 *  		(13)str, (14)dex, (15)wis, (16)itl, (17)items[], (18)equipment[], (19)spells[], (20)skills[],
		 *  		(21)ac, (22)ev, (23)resistances[]],
		 * 		online : [[characterId,x,y,name,maxHp,hp,effects,equipment(,cls,race)|(,type)]xM], 
		 * }
		 */
		StringBuilder answer = new StringBuilder();
		answer
			.append("{\"e\":\"loadContents\",")
			.append("\"l\":{")
			.append("},");
		// Player data
		answer
			.append("\"p\":[")
/* 0 */		.append(characterId).append(",")
/* 1 */		.append(x).append(",")
/* 2 */		.append(y).append(",")
/* 3 */		.append(true).append(",\"")
/* 4 */		.append(name).append("\",")
/* 5 */		.append(race.race2int()).append(",\"")
/* 6 */		.append(cls).append("\",")
/* 7 */		.append(maxHp).append(",")
/* 8 */		.append(maxMp).append(",")
/* 9 */		.append(maxEp).append(",")
/* 10*/		.append(hp).append(",")
/* 11*/		.append(mp).append(",")
/* 12*/		.append(ep).append(",")
/* 13*/ 	.append(str).append(",")
/* 14*/		.append(dex).append(",")
/* 15*/		.append(wis).append(",")
/* 16*/		.append(itl).append(",")
/* 17*/		.append(inventory.jsonGetContents()).append(",")
/* 18*/		.append(equipment.jsonGetEquipment()).append(",[");
/* 19*/	// Spells
		int i = 0;
		int iterations = spells.size() - 1;
		if (iterations > -1) {
			for (; i < iterations; i++) {
				answer.append(spells.get(i)).append(",");
			}
			answer.append(spells.get(i));
		}
		answer.append("],[");

/* 20*/	// Skills
		i = 0;
		for (iterations = skillNames.length - 1; i < iterations; i++) {
			answer.append(skills.get(skillNames[i])).append(",");
		}
		answer.append(skills.get(skillNames[i])).append("],");
		// Parameters
		answer
/* 21*/		.append(armor).append(",")
/* 22*/		.append(evasion).append(",")
		// Resistances
/* 23*/
		.append("[")
		.append(fireRes).append(",")
		.append(coldRes).append(",")
		.append(poisonRes).append(",")
		.append(acidRes).append("]")
		
		// Online characters
		.append("],\"online\":[");
		ArrayList<Character> onlinePlayers = new ArrayList<Character>();
		i = 0;
		iterations = onlinePlayers.size() - 1;

		if (iterations > -1) {
			for (; i < iterations; i++) {
				Character character = onlinePlayers.get(i);
				answer
					.append("[")
					.append(character.characterId).append(",")
					.append(character.x).append(",")
					.append(character.y).append(",\"")
					.append(character.name).append("\",")
					.append(character.fraction).append(",")
					.append(character.maxHp).append(",")
					.append(character.hp).append(",")
					.append(character.maxMp).append(",")
					.append(character.mp).append(",")
					.append(character.jsonGetEffects()).append(",")
					.append(character.jsonGetEquipment());
				if (character instanceof PlayerCharacter) {
					PlayerCharacter player = (PlayerCharacter) onlinePlayers
							.get(i);
					answer
						.append(",\"")
						.append(player.cls).append("\",")
						.append(player.race.race2int()).append("],");
				} else {
					answer
						.append(",\"")
						.append(character.type).append("\"],");
				}
			}
			Character character = onlinePlayers.get(i);
			answer
				.append("[")
				.append(character.characterId).append(",")
				.append(character.x).append(",")
				.append(character.y).append(",\"")
				.append(character.name).append("\",")
				.append(character.fraction).append(",")
				.append(character.maxHp).append(",")
				.append(character.hp).append(",")
				.append(character.maxMp).append(",")
				.append(character.mp).append(",")
				.append(character.jsonGetEffects()).append(",")
				.append(character.jsonGetEquipment());
			if (character instanceof PlayerCharacter) {
				PlayerCharacter player = (PlayerCharacter) onlinePlayers
						.get(i);
				answer
					.append(",\"")
					.append(player.cls).append("\",")
					.append(player.race.race2int()).append("]");
			} else {
				answer
					.append(",\"")
					.append(character.type).append("\"]");
			}
		}

		answer.append("],\"chat\":[");
		// Chat messages and invite
		Chat.Message[] messages = Chat.getMessagesAfter(System
				.currentTimeMillis() - 1000 * 60 * 10);

		if (messages.length > 0) {
			for (i = 0; i < messages.length - 1; i++) {
				answer
					.append("\"")
					.append(messages[i].player.name).append("\",\"")
					.append(messages[i].message).append("\",");
			}
			answer
			.append("\"")
			.append(messages[i].player.name).append("\",\"")
			.append(messages[i].message).append("\"");
		}
		answer.append("],");
		if (inviter != null) {
			answer
				.append("\"invite\":[")
				.append(inviter.characterId).append(",")
				.append(inviter.name).append("],");
		}

		// entering
		answer.append("\"en\":false}");
		return answer.toString();
	}
}
