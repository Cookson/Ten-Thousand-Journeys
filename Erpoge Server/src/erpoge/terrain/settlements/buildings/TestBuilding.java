package erpoge.terrain.settlements.buildings;

import erpoge.Main;
import erpoge.Side;
import erpoge.objects.GameObjects;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.BuildingPlace;
import erpoge.terrain.settlements.Settlement;
import erpoge.terrain.settlements.Building.BasisBuildingSetup;

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
