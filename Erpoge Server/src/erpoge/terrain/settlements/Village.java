package erpoge.terrain.settlements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;

import erpoge.Chance;
import erpoge.Main;
import erpoge.Utils;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.Location;
import erpoge.terrain.locationtypes.Settlement;
import erpoge.terrain.locationtypes.Settlement.QuarterSystem.BuildingPlace;
import erpoge.terrain.locationtypes.Settlement.QuarterSystem.Quarter;
import erpoge.terrain.locationtypes.Settlement.RoadSystem.Road;
import erpoge.characters.Character;
import erpoge.graphs.RectangleSystem;

public class Village extends Settlement {
	public Village(Location location) {
		super(location);
		makePeaceful();
		fillWithCells(GameObjects.FLOOR_GRASS,GameObjects.OBJ_VOID);
//		createRandomRoadSystem();
		roadSystem.createRoad(0,20,width-1,20);
		roadSystem.drawRoads();
		quarterSystem.build(roadSystem.getReferencePoints());
		for (BuildingPlace place : quarterSystem.buildingPlaces) {
			placeBuilding(place, BuildingType.INN);
		}
		setStartArea(0,0,10,10);
	}
}