package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.meta.Side;
import erpoge.core.terrain.settlements.BuildingPlace;

public class Smithy extends Building {
	public static final long serialVersionUID = 568456832L;

	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();

		RectangleSystem crs = new RectangleSystem(1);
		Side side = Side.S;
		RectangleArea initialRec = crs.addRectangleArea(x, y, width, height);
		RectangleArea r1 = crs.cutRectangleFromSide(initialRec, side, 5);
		RectangleArea r2 = crs.cutRectangleFromSide(r1, side.clockwise(), 5);
		crs.excludeRectangle(r2);
		terrainModifier = settlement.getTerrainModifier(crs);
		buildBasis(wallGreyStone);
	}

	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		// TODO Auto-generated method stub
		return true;
	}
}
