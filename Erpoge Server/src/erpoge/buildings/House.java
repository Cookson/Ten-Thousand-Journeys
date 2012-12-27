package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.StaticData;
import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.meta.Coordinate;

public class House extends Building {
	public static final long serialVersionUID = 23L;
	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		int objDoorBlue = StaticData.getObjectType("door_blue").getId();
		int objLadderUp = StaticData.getObjectType("ladder_up").getId();
		
		CustomRectangleSystem crs = new CustomRectangleSystem(x,y,width,height,1);
		
		int mainRoom = 0;
		int exFront = crs.cutRectangleFromSide(0, frontSide, 3);
		int lobby = crs.cutRectangleFromSide(1, leftSide, 6);
		int workshop = crs.cutRectangleFromSide(1, rightSide, 6);
		// int farRoom = crs.cutRectangleFromSide(0, rightSide, 7);
		
		crs.excludeRectangle(exFront);
		
		rectangleSystem = settlement.getGraph(crs);
		buildBasis(wallGreyStone);
		
		placeDoor(crs.content.get(lobby),    rightSide, objDoorBlue);
		placeDoor(crs.content.get(lobby),    backSide,  objDoorBlue);
		placeDoor(crs.content.get(mainRoom), rightSide, objDoorBlue);
		placeDoor(crs.content.get(workshop), backSide,  objDoorBlue);
		
		Coordinate c = crs.content.get(mainRoom).getCellFromSide(frontSide, leftSide, 0);
		settlement.setObject(c.x, c.y, objLadderUp);
		
		/* SECOND FLOOR */
//		settlement.touchLevel(1, settlement.getWidth(), settlement.getHeight());
//		settlement.linkWithPortals(c, c, 1);
//		settlement.selectLevel(1);
//		crs = new CustomRectangleSystem(settlement,x,y,width,height,1);
//		settlement.setObject(c.x, c.y, GameObjects.OBJ_LADDER_DOWN);
//		rectangleSystem = settlement.getGraph(crs);
//		buildBasis(GameObjects.OBJ_wall_gray_stone);
	}
}
