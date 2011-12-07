package erpoge.terrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.awt.Rectangle;
import java.io.IOException;

import net.tootallnate.websocket.WebSocket;

import erpoge.Chance;
import erpoge.Chat;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.MainHandler;
import erpoge.characters.Character;
import erpoge.characters.CharacterSet;
import erpoge.characters.NonPlayerCharacter;
import erpoge.characters.PlayerCharacter;
import erpoge.characters.TurnQueue;
import erpoge.inventory.Item;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.objects.Sound;
import erpoge.serverevents.*;

public class Location extends TerrainBasics {
	public final static Location ABSTRACT_LOCATION = new Location(0,0,"ABSTRACT LOCATION");
	public final String type;
	public final String name;
	public final World world;
	public int worldX;
	public int worldY;
	
	private ArrayList<PlayerCharacter> players = new ArrayList<PlayerCharacter>();
	private ArrayList<Sound> soundSources = new ArrayList<Sound>();
	/**
	 * serverEvents - a core of the mechanism of asynchronous server-side data sending.
	 * A character does his actions, all the events like attacks, spells, item manipulations,
	 * environment changes and so on are being written into location's/world's serverEvents
	 * and then flushed (Location.flushEvents) as json to all the needed players (either near enough
	 * in location or in the world)
	 */
	public ArrayList<ServerEvent> serverEvents = new ArrayList<ServerEvent>();

	private TurnQueue turnQueue = new TurnQueue(characters);

	public boolean noMorePlayersInLocation = false;	
	
	public Location(int w, int h, String t, String n, World wo) {
		super(w,h);
		type = t;
		name = n;
		world = wo;
		Chat.initLocationChat(this);
	}
	protected Location(int width, int height, String name) {
		this(width, height, "", name, World.ABSTRACT_WORLD);
	}
	public void sendOutEvent(PlayerCharacter character, ServerEvent event) {
	// Send out an event to all the players who are on global map.
	// Used only for sending to global map, not to location.
		ArrayList<WebSocket> targetConnections = new ArrayList<WebSocket>();
		// Form the event in json
		String data = "["+MainHandler.gsonIncludesStatic.toJson(event,event.getClass())+"]";
		// Select the recipients
		for (WebSocket conn : MainHandler.instance.connections()) {
			if (conn.character != null && conn.character.isOnGlobalMap()) {
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
	public void flushEvents(int toWho, Character character) {
		ArrayList<WebSocket> targetConnections = new ArrayList<WebSocket>();
		// Get the list of target players
		if (toWho == TO_LOCATION) {
			// Send to those in location who are close enough to get these events
			for (WebSocket conn : MainHandler.instance.connections()) {
				if (conn.character != null && !conn.character.isOnGlobalMap() && conn.character.location == this) {
					targetConnections.add(conn);
				}
			}
		} else if (toWho == TO_WORLD) {
			for (WebSocket conn : MainHandler.instance.connections()) {
				if (conn.character != null && conn.character.isOnGlobalMap()) {
					targetConnections.add(conn);
				}
			}
		}
		
		// Form the json string
		String data = "[";
		int i=0;
		int iterations = serverEvents.size()-1;
		if (iterations>-1) {
			for (;i<iterations;i++) {
				ServerEvent event = serverEvents.get(i);
				data += MainHandler.gsonIncludesStatic.toJson(event,event.getClass())+",\n";
			}
			ServerEvent event = serverEvents.get(i);
			data += MainHandler.gsonIncludesStatic.toJson(event,event.getClass());
		}
		data += "]";
		// Send data to all players
		for (WebSocket conn : targetConnections) {
			try {
				conn.send(data);
			} catch (IOException e) {
				Main.outln("Data sending error");
			}
		}
		serverEvents.clear();
	}
	public void addEvent(ServerEvent event) {
		serverEvents.add(event);
	}
	
	public void setWorldCoordinate(int x, int y) {
		worldX = x;
		worldY = y;
	}
	public String jsonGetContainerContents(int x, int y) {
		return getContainer(x,y).jsonGetContents();
	}
	public String jsonPartGetLocationContents() {
		/*
			Format: non-valid json data; 
				String "w:xSize,h:ySize,p:boolean,c:[[floor,object,[[itemId,amount]xN]]xM]";
		*/
		String answer = "\"w\":"+width+",\"h\":"+height+",\"p\":"+isPeaceful+",";
		int sSize=soundSources.size();
		if (sSize > 0) {
			answer += "\"s\":[";
			for (int i=0;i<sSize-1;i++) {
				Sound s = soundSources.get(i);
				answer += "["+s.x+","+s.y+","+s.type+"],";
			}
			Sound s = soundSources.get(sSize-1);
			answer += "["+s.x+","+s.y+","+s.type+"]],";
		}
		answer += "\"c\":[";
		for (int j = 0;j<height;j++) {
			for (int i=0;i<width;i++) {
				Cell c = cells[i][j];
				answer += "["+c.floor()+","+c.object();
				int iSize = c.items.size();
				if (iSize > 0) {
					answer += ",[";
					int k=0;
					for (Item item : c.items.values()){
						answer += item.toJson()+((k<iSize-1) ? "," : "");
						k++;
					}
					answer += "]";
				}
				answer += "]"+(i+j<width+height-2 ? "," : "");
			}
		}
		answer += "]";
		return answer;
	}
	public Coordinate getStartCoord() {
		for (int i=startArea.x;i<startArea.x+startArea.width;i++) {
			for (int j=startArea.y;j<startArea.y+startArea.height;j++) {
				if (passability[i][j] == 0) {
					return new Coordinate(i,j);
				}
			}
		}
		throw new Error("Cannot get start coord");
	}	
	public PlayerCharacter createCharacter(String type, String name, int race,
			String cls, int sx, int sy) {
		PlayerCharacter ch = new PlayerCharacter(type, name, race, cls, this, sx, sy);
		cells[sx][sy].character(ch);
		characters.put(ch.characterId, ch);
		players.add(ch);
		return ch;
	}
	
	public Character createCharacter(String type, String name, int sx, int sy) {
		Character ch = new NonPlayerCharacter(type, name, this, sx, sy);
		characters.put(ch.characterId, ch);
		cells[sx][sy].character(ch);
		addEvent(new EventCharacterAppear(ch.characterId, ch.x, ch.y, ch.type, ch.name, ch.maxHp, ch.hp,
				ch.maxMp, ch.mp, ch.getEffects(), ch.getAmmunition()));
		ch.getVisibleEntities();
		return ch;
	}	
	
	public void addCharacter(Character ch) {
		Coordinate start = getStartCoord();
		cells[start.x][start.y].character(ch);
		ch.x = start.x;
		ch.y = start.y;
		characters.put(ch.characterId, ch);
		ch.location = this;
		if (ch instanceof PlayerCharacter) {
			players.add((PlayerCharacter)ch);
		}
	}
	
	public void removeCharacter(Character character) {
		passability[character.x][character.y] = 0;
		characters.remove(character.characterId);
	}
	
	public void removeCharacter(PlayerCharacter character) {
		passability[character.x][character.y] = 0;
		characters.remove(character);
		character.location = Location.ABSTRACT_LOCATION;
	}
	
	public void setFloor(int x, int y, int type) {
//		throw new Error("BAKA");
		super.setFloor(x, y, type);
		addEvent(new EventFloorChange(type, x ,y));
	}
	
	public void setObject(int x, int y, int type) {
		super.setObject(x, y, type);
		addEvent(new EventObjectAppear(type, x ,y));
		for (Character ch : characters.values()) {
			if (ch.initialCanSee(x,y)) {
				ch.getVisibleEntities();
			}
		}
	}
	
	public void removeObject(int x, int y) {
		super.removeObject(x, y);
		addEvent(new EventObjectDisappear(x, y));
		for (Character ch : characters.values()) {
			if (ch.initialCanSee(x,y)) {
				ch.getVisibleEntities();
			}
		}
	}
	
	public void addItem(UniqueItem item, int x, int y) {
		super.addItem(item, x, y);
		addEvent(new EventItemAppear(item.getType().getTypeId(), item.getItemId(), x ,y));
	}
	public void addItem(ItemPile pile, int x, int y) {
		super.addItem(pile, x, y);
		addEvent(new EventItemAppear(pile.getType().getTypeId(), pile.getAmount(), x ,y));
	}
	
	
	
	public void removeItem(ItemPile pile, int x, int y) {
		super.removeItem(pile, x, y);
		addEvent(new EventItemDisappear(pile.getType().getTypeId(), pile.getAmount(), x ,y));
	}
	public void removeItem(UniqueItem item, int x, int y) {
		super.removeItem(item, x, y);
		addEvent(new EventItemDisappear(item.getType().getTypeId(), item.getItemId(), x ,y));
	}
	public void setCharacter(int x, int y, String t, int fraction) {
		createCharacter(t, "", x, y);
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
				if (noMorePlayersInLocation ) {
					return;
				}
				nextCharacter = turnQueue.next();
			}
			addEvent(new EventNextTurn(nextCharacter.characterId));
			flushEvents(TO_LOCATION, player);
		}
	}
	
	public Collection<PlayerCharacter> getPlayers() {
		// TODO Auto-generated method stub
		return players;
	}
	public Collection<Character> getCharacters() {
		return characters.values();
	}
	public void openDoor(int x, int y) {
		int doorId = cells[x][y].object();
		removeObject(x,y);
		if (doorId % 2 == 0) { 
		// The door is closed, open the door
			setObject(x,y,doorId-1);	
		} else {
			setObject(x,y,doorId+1);
		}
	}
	public void fireSound(int x, int y, int type) {
		addEvent(new EventSound(type, x, y));
	}
	public void createSoundSource(int x, int y, int type) {
		soundSources.add(new Sound(x, y, type));
		addEvent(new EventSoundSourceAppear(type, x, y));
	}
	public void removeSoundSource(int x, int y) {
		int size = soundSources.size();
		for (int i=0; i<size; i++) {
			Sound s = soundSources.get(i);
			if (s.x == x && s.y==y) {
				soundSources.remove(i);
				addEvent(new EventSoundSourceDisappear(1, x, y));
				return;
			}
		}		                            
		throw new Error("Sound source at "+x+":"+y+" not found");
	}
}
