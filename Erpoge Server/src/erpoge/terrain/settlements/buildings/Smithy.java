package erpoge.terrain.settlements.buildings;

import erpoge.Side;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.BuildingPlace;
import erpoge.terrain.settlements.Building.BasisBuildingSetup;


public class Smithy extends Building {
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(settlement,x,y,width,height,1);
		Side side = Side.S;
		crs.cutRectangleFromSide(0, side, 5);
		crs.cutRectangleFromSide(1, side.clockwise(), 5);
		crs.excludeRectangle(2);
		rectangleSystem = settlement.getGraph(crs);
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
	}

	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		// TODO Auto-generated method stub
		return true;
	}
}
