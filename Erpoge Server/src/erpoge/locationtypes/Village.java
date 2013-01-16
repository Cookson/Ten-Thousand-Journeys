package erpoge.locationtypes;

import erpoge.buildings.Inn;
import erpoge.core.HorizontalPlane;
import erpoge.core.StaticData;
import erpoge.core.terrain.settlements.BuildingPlace;
import erpoge.core.terrain.settlements.Settlement;

public class Village extends Settlement {
	public Village(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height);
		int floorGrass = StaticData.getFloorType("grass").getId();
		fillWithCells(floorGrass, StaticData.VOID);
		createRandomRoadSystem();
		roadSystem.createRoad(width/2,0,width/2,height-1);
		roadSystem.drawRoads();
		quarterSystem.build(roadSystem.getReferencePoints());
//		for (BuildingPlace place : quarterSystem.buildingPlaces) {
//			placeBuilding(place, Inn.class);
//		}
	}
}
