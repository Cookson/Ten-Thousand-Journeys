package erpoge.buildings;

import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.settlements.Building;
import erpoge.core.terrain.settlements.BuildingPlace;
import erpoge.core.terrain.settlements.Settlement;

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
