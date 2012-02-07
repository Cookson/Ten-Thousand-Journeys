package erpoge.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;

import erpoge.buildings.BuildingType;
import erpoge.buildings.House;
import erpoge.buildings.House2Rooms;
import erpoge.buildings.Inn;
import erpoge.buildings.OneRoomHouse;
import erpoge.buildings.Park;
import erpoge.buildings.Smithy;
import erpoge.buildings.Temple;
import erpoge.buildings.TestBuilding;
import erpoge.core.Main;
import erpoge.core.characters.Character;
import erpoge.core.graphs.RectangleSystem;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Utils;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.Cell;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.settlements.BuildingPlace;
import erpoge.core.terrain.settlements.Settlement;

public class BuildingTest extends Settlement {
	public BuildingTest(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height);
		
		int buildingSizeX = 18;
		int buildingSizeY = 19;
		
		fillWithCells(GameObjects.FLOOR_GRASS, GameObjects.OBJ_VOID);
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
	}
}