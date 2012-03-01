package erpoge.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


import net.tootallnate.websocket.WebSocket;

import erpoge.core.characters.NonPlayerCharacter;
import erpoge.core.characters.PlayerCharacter;
import erpoge.core.net.MainHandler;
import erpoge.core.net.PlayerHandler;
import erpoge.core.net.serverevents.EventChunkContents;
import erpoge.core.net.serverevents.EventExcludeChunk;
import erpoge.core.net.serverevents.EventNextTurn;
import erpoge.core.net.serverevents.EventSound;
import erpoge.core.net.serverevents.ServerEvent;
import erpoge.core.objects.SoundType;
import erpoge.core.terrain.Chunk;
import erpoge.core.terrain.HorizontalPlane;

public class TimeStream {
	/** How far from character should terrain be loaded (in chunks) */
	private static final byte LOADING_DEPTH = 1;
	public static int BASE_ENERGY = 500;
	public HashSet<Character> characters = new HashSet<Character>();
	private HashSet<PlayerHandler> players = new HashSet<PlayerHandler>();
	public HashSet<NonPlayerCharacter> nonPlayerCharacters = new HashSet<NonPlayerCharacter>();
	private ArrayList<ServerEvent> events = new ArrayList<ServerEvent>();
	public HashSet<Chunk> chunks = new HashSet<Chunk>();
	public TimeStream(PlayerHandler character) {
		addCharacter(character);
		events.clear();
	}
	public void sendOutEvent(PlayerCharacter character, ServerEvent event) {
	// Send out an event to all the players who are on global map.
	// Used only for sending to global map, not to location.
		ArrayList<WebSocket> targetConnections = new ArrayList<WebSocket>();
		// Form the event in json
		String data = "["+MainHandler.gsonIncludesStatic.toJson(event,event.getClass())+"]";
		// Select the recipients
		for (WebSocket conn : MainHandler.instance.connections()) {
			if (conn.character != null) {
				targetConnections.add(conn);
			}
		}
		
		// Send out event to whem
		for (WebSocket conn : targetConnections) {
			try {
				conn.send(data);
			} catch (Exception e) {
				throw new Error("Data sending error");
			}
		}
	}
	public void flushEvents() {
		ArrayList<WebSocket> targetConnections = new ArrayList<WebSocket>();
		// Get the list of target players
		for (WebSocket conn : MainHandler.instance.connections()) {
			if (conn.character != null && conn.character.inTimeStream(this)) {
				targetConnections.add(conn);
			}
		}
		
		// Form the json string
		String data = "[";
		int i=0;
		int iterations = events.size()-1;
		if (iterations>-1) {
			for (;i<iterations;i++) {
				ServerEvent event = events.get(i);
				data += MainHandler.gsonIncludesStatic.toJson(event,event.getClass())+",\n";
			}
			ServerEvent event = events.get(i);
			data += MainHandler.gsonIncludesStatic.toJson(event,event.getClass());
		}
		data += "]";
		// Send data to all players
		for (PlayerHandler player : players) {
			try {
				player.connection.send(data);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Error("Data sending error");
			}
		}
//		Main.log(data);
		events.clear();
	}
	public void addEvent(ServerEvent event) {
//		Main.console("add event "+event);
		events.add(event);
	}
	public void addCharacter(PlayerHandler character) {
		characters.add(character);
		players.add(character);
		loadApproachedChunks(character.plane, character.x, character.y);
	}
	public void removeCharacter(PlayerCharacter player) {
		if (!characters.contains(player)) {
			throw new Error("Player "+player+" is not in this time stream");
		}
		characters.remove(player);
		players.remove(player);
	}
	public void removeCharacter(NonPlayerCharacter character) {
		if (!characters.contains(character)) {
			throw new Error("Character "+character+" is not in this time stream");
		}
		characters.remove(character);
		nonPlayerCharacters.remove(character);
	}
	public void addNonPlayerCharacter(NonPlayerCharacter character) {
		if (!chunks.contains(character.chunk)) {
			throw new Error(character+" must be in a timeStream's chunk to be added to timeStream" +
					"His chunk is "+character.chunk);
		}
		nonPlayerCharacters.add(character);
		characters.add(character);
	}
	public void checkOut(PlayerCharacter player) {
		if (player.checkedOut) {
			throw new Error("Player "+player.name+" has already checked out!");
		} else {
			player.checkedOut = true;
			for (PlayerCharacter p : players) {
				if (!p.checkedOut) {
					return;
				}
			}
			for (PlayerCharacter p : players) {
				p.checkedOut = false;
			}
			Character nextCharacter = next();
			while (nextCharacter instanceof NonPlayerCharacter) {
				((NonPlayerCharacter)nextCharacter).action();
				nextCharacter = next();
			}
			addEvent(new EventNextTurn(nextCharacter.characterId));
			flushEvents();
		}
	}
	public void makeSound(int x, int y, SoundType type) {
		addEvent(new EventSound(type.type2int(), x, y));
	}
	public Character getCharacterById(int characterId) {
		for (Character character : characters) {
			if (character.characterId == characterId) {
				return character;
			}
		}
		throw new Error("No character with id "+characterId);
	}
	/**
	 * Get the next character in turn queue.
	 * @return Character
	 */
	public Character next() {
		Character nextCharacter = null;
		// Get the character with the greatest action points left
		for (Character ch : characters) {
			if (nextCharacter == null || ch.getActionPoints() > nextCharacter.getActionPoints()) {
				nextCharacter = ch;
			}
		}
		// If all the characters' energy is less than 0, then here goes the next turn
		if (nextCharacter.getActionPoints() <= 0) {
			for (Character ch : characters) {
				ch.increaseActionPoints(BASE_ENERGY);
			}
			return next();
		}
		return nextCharacter;
	}
	/**
	 * Add a chunk to the TimeStream, set chunk's timeStream pointer to this
	 * TimeStream and add an event for players that this chunk has been added 
	 * to the TimeStream.
	 * 
	 * @param chunk
	 */
	public void addChunk(Chunk chunk) {
		if (chunk.timeStream != null) {
			throw new Error(chunk+" is already in a time stream!");
		}
		chunk.timeStream = this;
		chunks.add(chunk);
		addEvent(new EventChunkContents(chunk));
	}
	public void excludeChunk(Chunk chunk) {
		if (chunk.timeStream!=this) {
			throw new Error(chunk+" is not in this time stream!");
		}
		chunks.remove(chunk);
		chunk.timeStream = null;
		addEvent(new EventExcludeChunk(chunk));
	}
	/**
	 * Add chunks near certain point to this time stream and send their 
	 * contents to characters. 
	 * 
	 * @param x
	 * @param y
	 */
	public void loadApproachedChunks(HorizontalPlane plane, int x, int y) {
		// A chunk where character came to
		Chunk chunk = plane.getChunkWithCell(x, y);
		// Bounds where we check for missing chunks
		int endX = chunk.getX()+Chunk.WIDTH*TimeStream.LOADING_DEPTH;
		int endY = chunk.getY()+Chunk.WIDTH*TimeStream.LOADING_DEPTH;
		for (
			int currY = chunk.getY()-Chunk.WIDTH*TimeStream.LOADING_DEPTH; 
			currY<=endY; 
			currY += Chunk.WIDTH
		) {
		// Find missing chunks and query them
			for (
				int currX = chunk.getX()-Chunk.WIDTH*TimeStream.LOADING_DEPTH;
				currX<=endX;
				currX += Chunk.WIDTH
			) {
				Chunk newChunk = plane.getChunkByCoord(currX, currY);
				if (newChunk.timeStream != this) {
					addChunk(newChunk);
				}
				
			}
		}
	};
	public void unloadUnusedChunks(HorizontalPlane plane) {
		Set<Chunk> chunksToExclude = new HashSet<Chunk>();
		for (Chunk chunk : chunks) {
			if (chunk.plane != plane) {
				continue;
			}
			for (PlayerCharacter player : players) {
				int playerChunkX = plane.getChunkRoundedCoord(player.x);
				int playerChunkY = plane.getChunkRoundedCoord(player.y);
				if (
					Math.abs(playerChunkX-chunk.getX()) > Chunk.WIDTH*TimeStream.LOADING_DEPTH
					|| Math.abs(playerChunkY-chunk.getY()) > Chunk.WIDTH*TimeStream.LOADING_DEPTH
				) {
					chunksToExclude.add(chunk);
				}
			}
		}
		for (Chunk chunk : chunksToExclude) {
			this.excludeChunk(chunk);
		}
	}
}
