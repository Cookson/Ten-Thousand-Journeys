package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;

public class House2Rooms extends Building {
	public static final long serialVersionUID = 82134511L;

	@Override
	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		int objDoorBlue = StaticData.getObjectType("door_blue").getId();

		RectangleSystem crs = new RectangleSystem(1);
		Side side = Side.S;
		RectangleArea hallToKitchen = crs.addRectangleArea(x, y, width, height);
		RectangleArea rightRoom = crs.cutRectangleFromSide(hallToKitchen, side.counterClockwise(), 5);
		RectangleArea hall = crs.cutRectangleFromSide(hallToKitchen, side, 2);
		RectangleArea middleRoom = crs.cutRectangleFromSide(hallToKitchen, side.counterClockwise(), 5);
		RectangleArea exPartOfStoreroom = crs.cutRectangleFromSide(hall, side.clockwise(), 2);
		// int kitchen = crs.cutRectangleFromSide(0, side.opposite(), 5);
		RectangleArea storeroom = crs.cutRectangleFromSide(hallToKitchen, side.clockwise(), 2);

		// Stretch storeroom
		storeroom.stretch(side, exPartOfStoreroom.getDimensionBySide(side) + 1);
		crs.excludeRectangle(exPartOfStoreroom);

		terrainModifier = settlement.getTerrainModifier(crs);
		buildBasis(wallGreyStone);

		// Remove walls of hall to kitchen
		removeWall(hallToKitchen, side);
		removeWall(hallToKitchen, side.opposite());

		// Doors
		// Storeroom
		Coordinate c = storeroom.getCellFromSide(side.counterClockwise(), side, 1).moveToSide(side.counterClockwise(), 1);
		settlement.setObject(c.x, c.y, objDoorBlue);
		// Middle room
		c = middleRoom.getMiddleOfSide(side).moveToSide(side, 1);
		settlement.setObject(c.x, c.y, objDoorBlue);
		// Right room
		c = rightRoom.getCellFromSide(side.clockwise(), side, 0).moveToSide(side.clockwise(), 1);
		settlement.setObject(c.x, c.y, objDoorBlue);
		// Front Door
		placeFrontDoor(hall, side);
	}
}
