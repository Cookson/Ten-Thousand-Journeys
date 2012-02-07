package erpoge.buildings;

import erpoge.core.Main;
import erpoge.core.meta.Side;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.settlements.Building;
import erpoge.core.terrain.settlements.BuildingPlace;
import erpoge.core.terrain.settlements.Settlement;
import erpoge.core.terrain.settlements.Building.BasisBuildingSetup;

public class TestBuilding extends Building {
	public void draw() {
		getRectangleSystem(4);
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
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
