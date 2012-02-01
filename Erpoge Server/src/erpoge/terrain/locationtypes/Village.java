package erpoge.terrain.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;

import erpoge.Chance;
import erpoge.Main;
import erpoge.Utils;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.HorizontalPlane;
import erpoge.terrain.Location;
import erpoge.terrain.settlements.BuildingPlace;
import erpoge.terrain.settlements.Settlement;
import erpoge.terrain.settlements.buildings.BuildingType;
import erpoge.terrain.settlements.buildings.Inn;
import erpoge.characters.Character;
import erpoge.graphs.RectangleSystem;

public class Village extends Settlement {
	public Village(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height);
		fillWithCells(GameObjects.FLOOR_GRASS,GameObjects.OBJ_VOID);
//		createRandomRoadSystem();
		roadSystem.createRoad(width/2,0,width/2,height-1);
		roadSystem.drawRoads();
		quarterSystem.build(roadSystem.getReferencePoints());
		for (BuildingPlace place : quarterSystem.buildingPlaces) {
			placeBuilding(place, Inn.class);
		}
	}
}