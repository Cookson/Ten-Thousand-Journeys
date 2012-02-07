package erpoge.buildings;

import erpoge.core.Main;
import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.settlements.Building;
import erpoge.core.terrain.settlements.Building.BasisBuildingSetup;

public class House extends Building {
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(x,y,width,height,1);
		
		int mainRoom = 0;
		int exFront = crs.cutRectangleFromSide(0, frontSide, 3);
		int lobby = crs.cutRectangleFromSide(1, leftSide, 6);
		int workshop = crs.cutRectangleFromSide(1, rightSide, 6);
		int farRoom = crs.cutRectangleFromSide(0, rightSide, 7);
		
		crs.excludeRectangle(exFront);
		
		rectangleSystem = settlement.getGraph(crs);
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
		
		placeDoor(crs.content.get(lobby), rightSide, GameObjects.OBJ_DOOR_BLUE);
		placeDoor(crs.content.get(lobby), backSide, GameObjects.OBJ_DOOR_BLUE);
		placeDoor(crs.content.get(mainRoom), rightSide, GameObjects.OBJ_DOOR_BLUE);
		placeDoor(crs.content.get(workshop), backSide, GameObjects.OBJ_DOOR_BLUE);
		
		Coordinate c = crs.content.get(mainRoom).getCellFromSide(frontSide, leftSide, 0);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_LADDER_UP);
		
		/* SECOND FLOOR */
//		settlement.touchLevel(1, settlement.getWidth(), settlement.getHeight());
//		settlement.linkWithPortals(c, c, 1);
//		settlement.selectLevel(1);
//		crs = new CustomRectangleSystem(settlement,x,y,width,height,1);
//		settlement.setObject(c.x, c.y, GameObjects.OBJ_LADDER_DOWN);
//		rectangleSystem = settlement.getGraph(crs);
//		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
	}
}
