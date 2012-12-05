package erpoge.locationtypes;

import erpoge.buildings.Inn;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.settlements.BuildingPlace;
import erpoge.core.terrain.settlements.Settlement;

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
