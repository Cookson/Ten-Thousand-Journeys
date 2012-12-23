package erpoge.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.java_websocket.WebSocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import erpoge.core.net.Chat;

public class PlayerCharacter extends Character implements GsonForStaticDataSerializable {
	public static final long serialVersionUID = 96186762L;
	protected final String cls;
	protected int str;
	protected int dex;
	protected int wis;
	protected int itl;
	
	
	protected HashMap<String, Integer> skills = new HashMap<String, Integer>();
	protected int party = 0;
	protected PlayerCharacter inviter;
	protected NonPlayerCharacter dialoguePartner;
	
	public WebSocket connection;
	public PlayerCharacter(HorizontalPlane plane, int x, int y, String name, CharacterType race, String cls) {
		super(plane, race, x, y, name);
		this.cls = cls;
		this.plane = plane;
		this.plane.getCell(x, y).setPassability(TerrainBasics.PASSABILITY_SEE);
		maxEp = 100;
		ep = 100;
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
		timeStream.addEvent(ServerEvents.create("chatMessage", "["+id+","+message+"]"));
		timeStream.flushEvents();
	}
	public void die() {
		super.die();
		timeStream.removeCharacter(this);
	}
	public void startConversation(int characterId) {
		dialoguePartner = (NonPlayerCharacter) timeStream.getCharacterById(characterId);
		if (dialoguePartner.hasDialogue()) {
			dialoguePartner.applyConversationStarting(this);
			timeStream.flushEvents();
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

	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = (JsonArray) super.serialize(context);
		jArray.add(new JsonPrimitive(cls));
		return jArray;
	}
}
