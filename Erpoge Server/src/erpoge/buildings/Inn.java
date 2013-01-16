package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.meta.Direction;
import erpoge.core.meta.Side;
import erpoge.core.terrain.settlements.BuildingPlace;

public class Inn extends Building {
	public static final long serialVersionUID = 11672547L;

	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();

		RectangleSystem crs = new RectangleSystem(1);
		/* BASIS */
		// Lobby
		Direction dir;
		Side side = Side.S;
		int lobbyWidth = 5;
		if (side == Side.N || side == Side.S) {
			dir = Direction.H;
		} else {
			dir = Direction.V;
		}
		RectangleArea rightRooms = crs.addRectangleArea(x, y, width, height);
		// Separate middle rectangle (lobby) and left rectangle, get left rooms
		RectangleArea leftRooms = crs.cutRectangleFromSide(rightRooms, side.clockwise(), ((((dir.isH()) ? width
			: height) - lobbyWidth) / 2 - 1));
		// Separate middle rectangle (lobby) and right rectangle, get lobby
		RectangleArea lobby = crs.cutRectangleFromSide(rightRooms, side.clockwise(), lobbyWidth);
		
		// Separate rectangle above lobby
		RectangleArea aboveLobby = crs.cutRectangleFromSide(lobby, side.opposite(), 4);
		// Left hall
		RectangleArea leftHall = crs.cutRectangleFromSide(leftRooms, side.counterClockwise(), 2);
		// Right hall
		RectangleArea rightHall = crs.cutRectangleFromSide(rightRooms, side.clockwise(), 2);
		// 1 - left rooms, 4 - left hall, 3 - above middle, 2 - middle, 5 -
		// right hall, 0 - right rooms
		// crs.cutRectangleFromSide(rightRoomsId, side, 1);
		// int firstSideRoom = 6;
		// Place left rooms and link them with left hall
		while (dir.isH() && leftRooms.height > 5 || dir.isV() && leftRooms.width > 5) {
			crs.cutRectangleFromSide(leftRooms, side, 4);
		}
		// Place right rooms and link them with right hall
		while (dir.isH() && rightRooms.height > 5 || dir.isV() && rightRooms.width > 5) {
			crs.cutRectangleFromSide(rightRooms, side, 4);
		}
		// Link last room

		terrainModifier = settlement.getTerrainModifier(crs);
		terrainModifier.getRectangleSystem().findOuterRectangles();
		lobby.stretch(side, -1);
		buildBasis(wallGreyStone);

		placeFrontDoor(lobby, side);

		/* CONTENTS */
		// Rectangle lobbyRec = terrainModifier.content.get(lobbyId);
		// for (int i=firstSideRoom, size = crs.rectangles.size();i<size;i++) {
		// ArrayList<Coordinate> cells =
		// getCellsNearWalls(crs.rectangles.get(i));
		// }
	}

	public boolean fitsToPlace(BuildingPlace place) {
		return (place.width > 23 || place.height > 23);
	}
}
