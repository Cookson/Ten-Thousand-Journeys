package erpoge.terrain.settlements.buildings;

import erpoge.graphs.CustomRectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.BuildingPlace;
import erpoge.terrain.settlements.Settlement;

public class OneRoomHouse extends Building {
	public void draw() {
		getRectangleSystem(900);	
		buildBasis(GameObjects.OBJ_WALL_WOODEN);
		
		placeFrontDoor(getDoorSide());
	}
	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		// TODO Auto-generated method stub
		return place.width > 6 || place.height > 6;
	}
}
