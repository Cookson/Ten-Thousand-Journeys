package erpoge.buildings;

import java.util.ArrayList;

import erpoge.core.Building;
import erpoge.core.Location;
import erpoge.core.RandomRectangleSystem;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.meta.Side;
import erpoge.core.terrain.settlements.BuildingPlace;

public class House extends Building {
	public static final long serialVersionUID = 23L;
	private RectangleSystem rs;
	public House(Location location, BuildingPlace place, Side frontSide) {
		super(location, place, frontSide);
	}
	public void draw() {
		int wall = StaticData.getObjectType("wall_grey_stone").getId();
		int objDoorBlue = StaticData.getObjectType("door_blue").getId();
		int objLadderUp = StaticData.getObjectType("ladder_up").getId();
		int water = StaticData.getFloorType("water").getId();
//		rs = new RectangleSystem(1);

//		RectangleArea mainRoom = rs.addRectangleArea(x, y, width, height);
//		RectangleArea exFront = rs.cutRectangleFromSide(mainRoom, frontSide, 3);
//		RectangleArea leftRoom = rs.cutRectangleFromSide(exFront, leftSide, 11);
//		RectangleArea workshop = rs.cutRectangleFromSide(exFront, rightSide, 6);
//		RectangleArea rightRoom = rs.cutRectangleFromSide(mainRoom, rightSide, 14);
//		rs.excludeRectangle(exFront);
//		terrainModifier = settlement.getTerrainModifier(rs);
//		rs.build();
//		terrainModifier.fillContents(Location.ELEMENT_FLOOR, water);
//		terrainModifier.drawInnerBorders(Location.ELEMENT_OBJECT, wallGreyStone);

//		rs = new RandomRectangleSystem(x, y, width, height, 2, 1);
		rs = new RectangleSystem(1);
		ArrayList<RectangleArea> ras = new ArrayList<RectangleArea>();
		
		ras.add(rs.addRectangleArea(x, y, width, height));
		ras.add(rs.cutRectangleFromSide(ras.get(0), frontSide, 5));
		ras.add(rs.cutRectangleFromSide(ras.get(0), frontSide, 5));
		ras.add(rs.cutRectangleFromSide(ras.get(2), leftSide, 3));
		ras.add(rs.cutRectangleFromSide(ras.get(2), leftSide, 3));
		ras.add(rs.cutRectangleFromSide(ras.get(2), leftSide, 3));
		ras.add(rs.cutRectangleFromSide(ras.get(1), leftSide, 3));
		
		rs.build();
		terrainModifier = settlement.getTerrainModifier(rs);
		terrainModifier.drawInnerBorders(Location.ELEMENT_OBJECT, wall);
		terrainModifier.drawOuterBorders(Location.ELEMENT_OBJECT, wall);
		
		/* SECOND FLOOR */
		// settlement.touchLevel(1, settlement.getWidth(),
		// settlement.getHeight());
		// settlement.linkWithPortals(c, c, 1);
		// settlement.selectLevel(1);
		// crs = new CustomRectangleSystem(settlement,x,y,width,height,1);
		// settlement.setObject(c.x, c.y, GameObjects.OBJ_LADDER_DOWN);
		// terrainModifier = settlement.getGraph(crs);
		// buildBasis(GameObjects.OBJ_wall_gray_stone);
	}
	public RectangleSystem getRs() {
		return rs;
	}

	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		return true;
	}
}
