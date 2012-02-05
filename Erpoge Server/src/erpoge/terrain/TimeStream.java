package erpoge.terrain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import net.tootallnate.websocket.WebSocket;

import erpoge.Main;
import erpoge.MainHandler;
import erpoge.PlayerHandler;
import erpoge.characters.Character;
import erpoge.characters.NonPlayerCharacter;
import erpoge.characters.PlayerCharacter;
import erpoge.characters.TurnQueue;
import erpoge.objects.SoundType;
import erpoge.serverevents.EventNextTurn;
import erpoge.serverevents.ServerEvent;

public class TimeStream {
	public HashSet<Character> characters = new HashSet<Character>();
	private HashSet<PlayerHandler> players = new HashSet<PlayerHandler>();
	public HashSet<NonPlayerCharacter> nonPlayerCharacters = new HashSet<NonPlayerCharacter>();
	private ArrayList<ServerEvent> events = new ArrayList<ServerEvent>();
	private TurnQueue turnQueue = new TurnQueue(characters);
	private HashSet<Chunk> chunks = new HashSet<Chunk>();
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
			} catch (IOException e) {
				Main.outln("Data sending error");
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
			} catch (IOException e) {
				Main.outln("Data sending error");
			}
		}
		events.clear();
	}
	public void addEvent(ServerEvent event) {
		events.add(event);
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
			Character nextCharacter = turnQueue.next();
			while (nextCharacter instanceof NonPlayerCharacter) {
				((NonPlayerCharacter)nextCharacter).action();
				nextCharacter = turnQueue.next();
			}
			addEvent(new EventNextTurn(nextCharacter.characterId));
			flushEvents();
		}
	}
	public void makeSound(int x, int y, SoundType type) {
		// TODO Auto-generated method stub
		
	}
	public Character getCharacterById(int characterId) {
		for (Character character : characters) {
			if (character.characterId == characterId) {
				return character;
			}
		}
		throw new Error("No character with id "+characterId);
	}
	public void addChunk(Chunk chunk) {
		if (chunk.timeStream != null) {
			throw new Error(chunk+" is already in a time stream!");
		}
		chunk.timeStream = this;
		chunks.add(chunk);
	}
}
