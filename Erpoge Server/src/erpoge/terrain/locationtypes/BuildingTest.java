package erpoge.terrain.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Utils;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.Location;
import erpoge.terrain.settlements.BuildingPlace;
import erpoge.terrain.settlements.Settlement;
import erpoge.terrain.settlements.buildings.BuildingType;
import erpoge.terrain.settlements.buildings.House;
import erpoge.terrain.settlements.buildings.House2Rooms;
import erpoge.terrain.settlements.buildings.Inn;
import erpoge.terrain.settlements.buildings.OneRoomHouse;
import erpoge.terrain.settlements.buildings.Park;
import erpoge.terrain.settlements.buildings.Smithy;
import erpoge.terrain.settlements.buildings.Temple;
import erpoge.terrain.settlements.buildings.TestBuilding;
import erpoge.characters.Character;
import erpoge.graphs.RectangleSystem;

public class BuildingTest extends Settlement {
	public BuildingTest(Location location) {
		super(location);
		
		int buildingSizeX = 18;
		int buildingSizeY = 19;
		
		makePeaceful();
		fillWithCells(GameObjects.FLOOR_GRASS,GameObjects.OBJ_VOID);
		// -2 and +5 are beacuse of road.width
		Coordinate nw = new Coordinate((width-buildingSizeX)/2-2,(height-buildingSizeY)/2-2);
		Coordinate ne = new Coordinate(nw.x+buildingSizeX+5,nw.y);
		Coordinate se = new Coordinate(nw.x+buildingSizeX+5,nw.y+buildingSizeY+5);
		Coordinate sw = new Coordinate(nw.x,nw.y+buildingSizeY+5);
		roadSystem.createRoad(nw.x,nw.y,ne.x,ne.y);
		roadSystem.createRoad(ne.x,ne.y,se.x,se.y);
		roadSystem.createRoad(se.x,se.y,sw.x,sw.y);
		roadSystem.createRoad(sw.x,sw.y,nw.x,nw.y);
		roadSystem.drawRoads();
		quarterSystem.build(roadSystem.getReferencePoints());
		for (BuildingPlace place : quarterSystem.buildingPlaces) {
			if (place.contains(nw.x+buildingSizeX/2, nw.y+buildingSizeY/2)) {
				placeBuilding(place, House.class);
				break;
			}
		}
		setStartArea(5,5,5,5);
	}
}