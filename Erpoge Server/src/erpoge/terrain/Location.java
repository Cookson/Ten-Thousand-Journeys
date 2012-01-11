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
import erpoge.characters.NonPlayerCharacter;
import erpoge.characters.PlayerCharacter;
import erpoge.characters.TurnQueue;
import erpoge.inventory.Item;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.itemtypes.Attribute;
import erpoge.objects.Sound;
import erpoge.objects.SoundType;
import erpoge.serverevents.*;

public class Location extends TerrainBasics {
	public final static Location ABSTRACT_LOCATION = new Location(0,0,"ABSTRACT LOCATION");
	public final String type;
	public final String name;
	public final World world;
	public int worldX;
	public int worldY;
	
	private ArrayList<PlayerCharacter> players = new ArrayList<PlayerCharacter>();
	protected ArrayList<Ceiling> ceilings = new ArrayList<Ceiling>();
	public HashSet<NonPlayerCharacter> nonPlayerCharacters = new HashSet<NonPlayerCharacter>();
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
				String "w:xSize,h:ySize,p:boolean,s:[[x,y,type]xJ]c:[[floor,object,[[itemId,amount]xN]]xM],ceilings:[[x,y,width,height]xL]";
		*/
		StringBuilder answer = new StringBuilder();
		answer
			.append("\"w\":").append(width)
			.append(",\"h\":").append(height)
			.append(",\"p\":").append(isPeaceful).append(",");
		int sSize=soundSources.size();
		if (sSize > 0) {
			answer.append("\"s\":[");
			for (int i=0;i<sSize-1;i++) {
				Sound s = soundSources.get(i);
				answer
					.append("[")
					.append(s.x).append(",")
					.append(s.y).append(",")
					.append(s.type).append("],");
			}
			Sound s = soundSources.get(sSize-1);
			answer
				.append("[")
				.append(s.x).append(",")
				.append(s.y).append(",")
				.append(s.type).append("]],");
		}
		answer.append("\"c\":[");
		for (int j = 0;j<height;j++) {
			for (int i=0;i<width;i++) {
				Cell c = cells[i][j];
				answer.append("["+c.floor()).append(",").append(c.object());
				int iSize = c.items.size();
				if (iSize > 0) {
					answer.append(",[");
					int k=0;
					for (Item item : c.items.values()){
						answer.append(item.toJson()).append((k<iSize-1) ? "," : "");
						k++;
					}
					answer.append("]");
				}
				answer.append("]").append(i+j<width+height-2 ? "," : "");
			}
		}
		answer.append("],\"ceilings\":[");
		int ceilingsSize = ceilings.size();
		int i=0;
		for (Ceiling c : ceilings) {
			answer
				.append("[")
				.append(c.x).append(",")
				.append(c.y).append(",")
				.append(c.width).append(",")
				.append(c.height).append(",")
				.append(c.type).append("]").append(++i<ceilingsSize ? "," : "");
		}
		answer.append("]");
		return answer.toString();
	}
	public Coordinate getStartCoord() {
		for (int i=startArea.x;i<startArea.x+startArea.width;i++) {
			for (int j=startArea.y;j<startArea.y+startArea.height;j++) {
				if (passability[i][j] == PASSABILITY_FREE) {
					return new Coordinate(i,j);
				}
			}
		}
		throw new Error("Cannot get start coord");
	}	
	public NonPlayerCharacter createCharacter(String type, String name, int sx, int sy, int fraction) {
		NonPlayerCharacter ch = new NonPlayerCharacter(type, name, this, sx, sy);
		ch.setFraction(fraction);
		characters.put(ch.characterId, ch);
		nonPlayerCharacters.add(ch);
		cells[sx][sy].character(ch);
		addEvent(new EventCharacterAppear(
				ch.characterId, ch.x, ch.y, ch.type, ch.name,
				ch.getAttribute(Attribute.MAX_HP), ch.getAttribute(Attribute.HP),
				ch.getAttribute(Attribute.MAX_MP), ch.getAttribute(Attribute.MP),
				ch.getEffects(), ch.getAmmunition(), ch.getFraction()));
		ch.getVisibleEntities();
		return ch;
	}	
	
	public void addCharacter(PlayerCharacter ch) {
		Coordinate spawn = getStartCoord();
		cells[spawn.x][spawn.y].character(ch);
		ch.x = spawn.x;
		ch.y = spawn.y;
		characters.put(ch.characterId, ch);
		ch.location = this;
		players.add(ch);
	}
	public void addCharacter(PlayerCharacter ch, Portal portal) {
	/**
	 * Adds character near portal. Portal is portal object
	 * not in this location, but in location character came from.
	 */
		Coordinate spawn = portal.getAnotherEnd();
		boolean freeSpaceFound = false;
		both:
		for (int dx = -1; dx<2; dx++) {
		/**
		 * Search for free space near portal
		 */
			for (int dy = -1; dy<2; dy++) {
				if (passability[spawn.x+dx][spawn.y+dy] == PASSABILITY_FREE) {
					spawn.move(spawn.x+dx, spawn.y+dy);
					freeSpaceFound = true;
					break both;
				}
			}
		}
		if (!freeSpaceFound) {
			throw new Error("Free space not found");
		}
		cells[spawn.x][spawn.y].character(ch);
		ch.x = spawn.x;
		ch.y = spawn.y;
		ch.location = this;
		characters.put(ch.characterId, ch);
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
		super.setFloor(x, y, type);
		addEvent(new EventFloorChange(type, x ,y));
	}
	public void setObject(int x, int y, int type) {
		super.setObject(x, y, type);
		addEvent(new EventObjectAppear(type, x ,y));
		for (NonPlayerCharacter ch : nonPlayerCharacters) {
			if (ch.initialCanSee(x,y)) {
				ch.getVisibleEntities();
			}
		}
	}
	public void setObject(Coordinate c, int type) {
		setObject(c.x, c.y, type);
	}
	
	public void removeObject(int x, int y) {
		super.removeObject(x, y);
		addEvent(new EventObjectDisappear(x, y));
		for (NonPlayerCharacter ch : nonPlayerCharacters) {
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
		createCharacter(t, "", x, y, 0);
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
	public void makeSound(int x, int y, SoundType type) {
		addEvent(new EventSound(type.type2int(), x, y));
	}
	public void createSoundSource(int x, int y, SoundType type) {
		soundSources.add(new Sound(x, y, type));
		addEvent(new EventSoundSourceAppear(type.type2int(), x, y));
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
