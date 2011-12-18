package erpoge.terrain;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Side;
import erpoge.characters.Character;
import erpoge.characters.CharacterSet;
import erpoge.characters.NonPlayerCharacter;
import erpoge.graphs.RectangleSystem;
import erpoge.inventory.Item;
import erpoge.objects.GameObjects;
import erpoge.serverevents.EventFloorChange;
import erpoge.serverevents.EventObjectAppear;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.BuildingPlace;

public class LocationGenerator extends TerrainGenerator {
	protected final String type;
	protected final String name;
	protected final World world;

	public LocationGenerator(Location location) {
		super(location);
		
		cells = location.cells;
		characters = location.characters;
		containers = location.containers;
		passability = location.passability;
		startArea = location.startArea;
		locationPortals = location.locationPortals;

		type = location.type;
		name = "��� ����������";
		world = this.location.world;
	}
	public void makePeaceful() {
		location.isPeaceful = true;
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
