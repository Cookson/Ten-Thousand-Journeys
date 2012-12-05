package erpoge.buildings;

import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.meta.Side;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.settlements.Building;
import erpoge.core.terrain.settlements.BuildingPlace;


public class Smithy extends Building {
	public static final long serialVersionUID = 568456832L;
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(x,y,width,height,1);
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
