package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.StaticData;
import erpoge.core.meta.Side;
import erpoge.core.terrain.settlements.BuildingPlace;

public class TestBuilding extends Building {
	public static final long serialVersionUID = 346347;
	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		
		getTerrainModifier(4);
		buildBasis(wallGreyStone);
		for (Side side : doorSides) {
			placeFrontDoor(side);
		}
	}
	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		// TODO Auto-generated method stub
		return true;
	}
}
