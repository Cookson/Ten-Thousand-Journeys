package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.meta.Coordinate;

public class House extends Building {
	public static final long serialVersionUID = 23L;

	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		int objDoorBlue = StaticData.getObjectType("door_blue").getId();
		int objLadderUp = StaticData.getObjectType("ladder_up").getId();

		RectangleSystem crs = new RectangleSystem(1);

		RectangleArea mainRoom = crs.addRectangleArea(x, y, width, height);
		RectangleArea exFront = crs.cutRectangleFromSide(mainRoom, frontSide, 3);
		RectangleArea lobby = crs.cutRectangleFromSide(exFront, leftSide, 6);
		RectangleArea workshop = crs.cutRectangleFromSide(exFront, rightSide, 6);
		// int farRoom = crs.cutRectangleFromSide(0, rightSide, 7);

		crs.excludeRectangle(exFront);

		terrainModifier = settlement.getTerrainModifier(crs);
		buildBasis(wallGreyStone);

		placeDoor(lobby, rightSide, objDoorBlue);
		placeDoor(lobby, backSide, objDoorBlue);
		placeDoor(mainRoom, rightSide, objDoorBlue);
		placeDoor(workshop, backSide, objDoorBlue);

		Coordinate c = mainRoom.getCellFromSide(frontSide, leftSide, 0);
		settlement.setObject(c.x, c.y, objLadderUp);

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
}
