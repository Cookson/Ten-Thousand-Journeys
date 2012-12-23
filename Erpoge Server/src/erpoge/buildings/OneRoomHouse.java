package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.StaticData;
import erpoge.core.terrain.settlements.BuildingPlace;

public class OneRoomHouse extends Building {
	public static final long serialVersionUID = 35681734L;
	public void draw() {
		int wallWoorden = StaticData.getObjectType("wall_wooden").getId();
		
		getRectangleSystem(900);	
		buildBasis(wallWoorden);
		
		placeFrontDoor(getDoorSide());
	}
	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		// TODO Auto-generated method stub
		return place.width > 6 || place.height > 6;
	}
}
