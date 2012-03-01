package erpoge.core.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import erpoge.core.Character;
import erpoge.core.TimeStream;
import erpoge.core.characters.NonPlayerCharacter;
import erpoge.core.characters.PlayerCharacter;
import erpoge.core.inventory.Item;
import erpoge.core.inventory.ItemMap;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.itemtypes.Attribute;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.net.serverevents.EventCharacterAppear;
import erpoge.core.net.serverevents.EventFloorChange;
import erpoge.core.net.serverevents.EventItemAppear;
import erpoge.core.net.serverevents.EventItemDisappear;
import erpoge.core.net.serverevents.EventObjectAppear;
import erpoge.core.net.serverevents.EventObjectDisappear;
import erpoge.core.net.serverevents.EventSound;
import erpoge.core.net.serverevents.EventSoundSourceAppear;
import erpoge.core.net.serverevents.EventSoundSourceDisappear;
import erpoge.core.objects.Sound;
import erpoge.core.objects.SoundSource;
import erpoge.core.objects.SoundType;

public class Chunk extends TerrainBasics {
	private HashSet<Character> characters = new HashSet<Character>();
	private HashSet<NonPlayerCharacter> nonPlayerCharacters = new HashSet<NonPlayerCharacter>();
	public static final byte WIDTH = 20;
	public TimeStream timeStream;
	public HorizontalPlane plane;
	private ArrayList<SoundSource> soundSources = new ArrayList<SoundSource>();
	public Chunk neighborN;
	public Chunk neighborE;
	public Chunk neighborS;
	public Chunk neighborW;
	public Chunk(HorizontalPlane plane, int x, int y) {
		super(x, y);
		this.plane = plane;
		this.cells = new Cell[Chunk.WIDTH][Chunk.WIDTH];
		for (byte i=0; i<WIDTH; i++) {
			for (byte j=0; j<WIDTH; j++) {
				cells[i][j] = new Cell();
			}
		}
	}
	public Cell getCell(int x, int y) {
		return cells[x-this.x][y-this.y];
	}
	public Chunk getNeighbor(Side side) {
		switch (side) {
		case N:
			return neighborN;
		case E:
			return neighborE;
		case S:
			return neighborE;
		case W:
			return neighborE;
		default:
			throw new Error("Srong side "+side+"!");
		}
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public String jsonPartGetContents() {
		/*
			Format: non-valid json data; 
				String "
				c:[[floor,object,[[itemId,amount]xN]]xM],ceilings:[[x,y,width,height]xL]";
		*/
		StringBuilder answer = new StringBuilder();
		
		answer.append("\"c\":[");
		for (int j = 0; j<WIDTH; j++) {
			for (int i=0; i<WIDTH; i++) {
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
				answer.append("]").append(i+j<WIDTH+WIDTH-2 ? "," : "");
			}
		}
		answer.append("]");
//				",\"ceilings\":[");
//		int ceilingsSize = ceilings.size();
//		int i=0;
//		for (Ceiling c : ceilings) {
//			answer
//				.append("[")
//				.append(c.x).append(",")
//				.append(c.y).append(",")
//				.append(c.width).append(",")
//				.append(c.height).append(",")
//				.append(c.type).append("]").append(++i<ceilingsSize ? "," : "");
//		}
//		answer.append("]");
		return answer.toString();
	}
	// From Location
	public String jsonGetContainerContents(int x, int y) {
		return getContainer(x,y).jsonGetContents();
	}
	protected NonPlayerCharacter createCharacter(int relX, int relY, String type, String name, int fraction) {
		NonPlayerCharacter character = new NonPlayerCharacter(plane, x+relX, y+relY, type, name);
		character.setTimeStream(timeStream);
		timeStream.addNonPlayerCharacter(character);
		character.setFraction(fraction);
		characters.add(character);
		nonPlayerCharacters.add(character);
		cells[relX][relY].character(character);
		timeStream.addEvent(new EventCharacterAppear(
				character.characterId, character.x, character.y, character.type, character.name,
				character.getAttribute(Attribute.MAX_HP), character.getAttribute(Attribute.HP),
				character.getAttribute(Attribute.MAX_MP), character.getAttribute(Attribute.MP),
				character.getEffects(), character.getEquipment(), character.getFraction()));
		character.notifyNeighborsVisiblilty();
		character.getVisibleEntities();
		return character;
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
				if (cells[spawn.x+dx][spawn.y+dy].getPassability() == PASSABILITY_FREE) {
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
		characters.add(ch);
	}	
	public void removeCharacter(Character character) {
		cells[character.x-x][character.y-y].setPassability(PASSABILITY_FREE);
		cells[character.x-x][character.y-y].character(false);
		characters.remove(character);
	}
	public void setFloor(int x, int y, int type) {
		super.setFloor(x, y, type);
		timeStream.addEvent(new EventFloorChange(type, this.x+x ,this.y+y));
	}
	public void setObject(int x, int y, int type) {
		super.setObject(x, y, type);
		timeStream.addEvent(new EventObjectAppear(type, this.x+x ,this.y+y));
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
		timeStream.addEvent(new EventObjectDisappear(this.x+x, this.y+y));
		for (NonPlayerCharacter ch : nonPlayerCharacters) {
			if (ch.initialCanSee(x,y)) {
				ch.getVisibleEntities();
			}
		}
	}
	
	public void removeItem(ItemPile pile, int x, int y) {
		super.removeItem(pile, x, y);
		timeStream.addEvent(new EventItemDisappear(pile.getType().getTypeId(), pile.getAmount(), this.x+x ,this.y+y));
	}
	public void removeItem(UniqueItem item, int x, int y) {
		super.removeItem(item, x, y);
		timeStream.addEvent(new EventItemDisappear(item.getType().getTypeId(), item.getItemId(), this.x+x ,this.y+y));
	}
	public void setCharacter(int x, int y, String t, int fraction) {
		createCharacter(x, y, t, "", 0);
	}
	public void createSoundSource(int x, int y, SoundType type) {
		soundSources.add(new SoundSource(x, y, type, 1000));
		timeStream.addEvent(new EventSoundSourceAppear(type.type2int(), this.x+x, this.y+y));
	}
	public void removeSoundSource(int x, int y) {
		int size = soundSources.size();
		for (int i=0; i<size; i++) {
			Sound s = soundSources.get(i);
			if (s.x == x && s.y==y) {
				soundSources.remove(i);
				timeStream.addEvent(new EventSoundSourceDisappear(1, this.x+x, this.y+y));
				return;
			}
		}		                            
		throw new Error("Sound source at "+x+":"+y+" not found");
	}
	/**
	 * @param x Relative coordinates
	 * @param y Relative coordinates
	 */
	public void addItem(UniqueItem item, int x, int y) {
		super.addItem(item, x, y);
		timeStream.addEvent(new EventItemAppear(item.getType().getTypeId(), item.getItemId(), this.x+x ,this.y+y));
	}
	public void addItem(ItemPile pile, int x, int y) {
		super.addItem(pile, x, y);
		timeStream.addEvent(new EventItemAppear(pile.getType().getTypeId(), pile.getAmount(), this.x+x, this.y+y));
	}

	public void setTimeStream(TimeStream timeStream) {
		this.timeStream = timeStream; 
	}
	public int getWidth() {
		return Chunk.WIDTH;
	}
	public int getHeight() {
		return Chunk.WIDTH;
	}
	public String toString() {
		return "Chunk "+x+" "+y;
	}
	public int[] getContentsAsIntegerArray() {
		int[] contents = new int[Chunk.WIDTH*Chunk.WIDTH*2];
		int u=0;
		for (int y=0; y<Chunk.WIDTH; y++) {
			for (int x=0; x<Chunk.WIDTH; x++) {
				contents[u++] = cells[x][y].floor;
				contents[u++] = cells[x][y].object;
			}
		}
		return contents;
	}
	public Integer[] getItemsAsIntegerArray() {
		// [x,y,typeId,param, x,y,typeId,param, ...]
		ArrayList<Integer> contents = new ArrayList<Integer>();
		for (int y=0; y<Chunk.WIDTH; y++) {
			for (int x=0; x<Chunk.WIDTH; x++) {
				ItemMap map = cells[x][y].items;
				if (map.size() > 0) {
					for (Item item : map.values()) {
						contents.add(this.x+x);
						contents.add(this.y+y);
						contents.add(item.getTypeId());
						contents.add(item.getParam());
					}
				}
			}
		}
		return contents.toArray(new Integer[] {});
	}
	public ArrayList[] getCharacters() {
		ArrayList[] answer = new ArrayList[characters.size()];
		int u = 0;
		for (Character character : characters) {
			ArrayList chdata = new ArrayList();
			/* 0 */chdata.add(character.characterId);
			/* 1 */chdata.add(character.x);
			/* 2 */chdata.add(character.y);
			/* 3 */chdata.add(character.type);
			/* 4 */chdata.add(character.name);
			/* 5 */chdata.add(character.maxHp);
			/* 6 */chdata.add(character.hp);
			/* 7 */chdata.add(character.maxMp);
			/* 8 */chdata.add(character.mp);
			/* 9 */chdata.add(character.getEffects());
			/*10 */chdata.add(character.getEquipment());
			/*11 */chdata.add(character.fraction);
			answer[u++] = chdata;
		}
		return answer;
	}
}
