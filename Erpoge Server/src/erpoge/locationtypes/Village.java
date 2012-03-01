package erpoge.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;

import erpoge.buildings.BuildingType;
import erpoge.buildings.Inn;
import erpoge.core.Character;
import erpoge.core.Main;
import erpoge.core.graphs.RectangleSystem;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Utils;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.Cell;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
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