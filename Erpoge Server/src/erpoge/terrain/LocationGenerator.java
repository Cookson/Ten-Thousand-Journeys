package erpoge.terrain;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Side;
import erpoge.characters.NonPlayerCharacter;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.BuildingPlace;

public class LocationGenerator extends TerrainGenerator {
	protected String type;
	protected String name;
	protected final World world;
	public final HashMap<Integer, Location> levels = new HashMap<Integer, Location>();
	public HashSet<NonPlayerCharacter> nonPlayerCharacters = new HashSet<NonPlayerCharacter>();

	public LocationGenerator(Location location) {
		super(location);

		this.levels.put(0, location);
		linkFieldsToLocationsFields(location);
		
		world = this.location.world;
	}
	private void linkFieldsToLocationsFields(Location location) {
		cells = location.cells;
		characters = location.characters;
		nonPlayerCharacters = location.nonPlayerCharacters;
		containers = location.containers;
		ceilings = location.ceilings;
		passability = location.passability;
		startArea = location.startArea;
		portals = location.portals;
		name = "Имя локации";
		type = location.type;
	}
	public void linkWithPortals(Coordinate from, Coordinate to, int levelNumber) {
	/**
	 * Create and link portals at Coordinate from in current level
	 * and at Coordinate to in level number levelNumber. 
	 * Note that you still need to create game objects for portals.
	 */
		Location destLocation = levels.get(levelNumber);
		Portal fromPortal = new Portal(from, destLocation);
		Portal destPortal = new Portal(to, location);
		fromPortal.linkWith(destPortal);
		portals.add(fromPortal);
		destLocation.portals.add(destPortal);
		
	}
	public void makePeaceful() {
		location.isPeaceful = true;
	}
	public NonPlayerCharacter createCharacter(String type, String name, int sx, int sy) {
		NonPlayerCharacter ch = new NonPlayerCharacter(type, name, (Location)location, sx, sy);
		characters.put(ch.characterId, ch);
		nonPlayerCharacters.add(ch);
		location.cells[sx][sy].character(ch);
		return ch;
	}
	public void selectLevel(int level) {
		location = levels.get(level);
		linkFieldsToLocationsFields(location);
	}
	public Location addLevel(int number, int width, int height) {
		Location level = new Location(width, height, "LevelType", "LevelName", world);
		levels.put(number, level);
		return level;
	}
	public boolean touchLevel(int number, int width, int height) {
	/**
	 * Creates level, if level doesn's exist. Returns false
	 * if level exists, true otherwise.
	 */
		if (hasLevel(number)) {
			return false;
		} else {
			addLevel(number, width, height);
			return true;
		}
	}
	public Location getLevel(int number) {
	/**
	 * Returns Location object of certain level.
	 */
		return levels.get(number);
	}
	public boolean hasLevel(int number) {
		return levels.containsKey(number);
	}
	public Building placeBuilding(Class<? extends Building> building, int x, int y, int width, int height, Side side) {
	/**
	 * Places building when current location is not Settlement.
	 * 
	 * @param side What side a building is rotated to.
	 */
		BuildingPlace place = new BuildingPlace(x,y,width,height);
		try {
			return building.newInstance().setProperties(this, place);
		} catch (Exception e) {
			throw new Error("Couldn't place building");
		}
	}
}
