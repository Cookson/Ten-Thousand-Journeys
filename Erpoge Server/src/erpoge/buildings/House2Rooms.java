package erpoge.buildings;

import java.awt.Rectangle;

import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.net.RectangleArea;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.settlements.Building;

public class House2Rooms extends Building {
	public static final long serialVersionUID = 82134511L;
	@Override
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(x,y,width,height,1);
		Side side = Side.S;
		
		int rightRoom = crs.cutRectangleFromSide(0, side.counterClockwise(), 5);	
		int hall = crs.cutRectangleFromSide(0, side, 2);
		int middleRoom = crs.cutRectangleFromSide(0, side.counterClockwise(), 5);
		int exPartOfStoreroom = crs.cutRectangleFromSide(hall, side.clockwise(), 2);
		// int kitchen = crs.cutRectangleFromSide(0, side.opposite(), 5);
		int storeroom = crs.cutRectangleFromSide(0, side.clockwise(), 2);
		int hallToKitchen = 0;
		
		// Stretch storeroom
		RectangleArea recExPartOfStoreroom = crs.content.get(exPartOfStoreroom);
		crs.content.get(storeroom)
			.stretch(side, recExPartOfStoreroom.getDimensionBySide(side)+1);
		crs.excludeRectangle(exPartOfStoreroom);
		
		rectangleSystem = settlement.getGraph(crs);
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
		
		// Remove walls of hall to kitchen
		Rectangle recHallToKitchen = crs.content.get(hallToKitchen);
		removeWall(recHallToKitchen, side);
		removeWall(recHallToKitchen, side.opposite());
		
		// Doors
		// Storeroom
		RectangleArea recStoreroom = crs.content.get(storeroom);
		Coordinate c = recStoreroom
			.getCellFromSide(side.counterClockwise(), side, 1)
			.moveToSide(side.counterClockwise(), 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_DOOR_BLUE);
		// Middle room
		RectangleArea recMiddleRoom = crs.content.get(middleRoom);
		c = recMiddleRoom
			.getMiddleOfSide(side)
			.moveToSide(side, 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_DOOR_BLUE);
		// Right room
		RectangleArea recRightRoom = crs.content.get(rightRoom);
		c = recRightRoom
			.getCellFromSide(side.clockwise(), side, 0)
			.moveToSide(side.clockwise(), 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_DOOR_BLUE);
		// Front Door
		placeFrontDoor(hall, side);
	}
}
