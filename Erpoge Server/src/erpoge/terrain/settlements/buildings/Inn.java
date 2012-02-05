package erpoge.terrain.settlements.buildings;

import java.awt.Rectangle;
import java.io.ObjectOutputStream.PutField;
import java.util.ArrayList;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Direction;
import erpoge.Main;
import erpoge.Side;
import erpoge.characters.NonPlayerCharacter;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.BuildingPlace;
import erpoge.terrain.settlements.Building.BasisBuildingSetup;

public class Inn extends Building {
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(x,y,width,height,1);
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
		
		// Separate middle rectangle (lobby) and left rectangle, get left rooms
		int leftRoomsId = crs.cutRectangleFromSide(0, side.clockwise(), ((((dir.isH())?width:height)-lobbyWidth)/2-1));
		// Separate middle rectangle (lobby) and right rectangle, get lobby
		int lobbyId = crs.cutRectangleFromSide(0, side.clockwise(), lobbyWidth);
		int rightRoomsId = 0;
		// Separate rectangle above lobby
		int aboveLobbyId = crs.cutRectangleFromSide(2, side.opposite(), 4);		
		// Left hall
		int leftHallId = crs.cutRectangleFromSide(1, side.counterClockwise(), 2);
		// Right hall
		int rightHallId = crs.cutRectangleFromSide(0, side.clockwise(), 2);
		// 1 - left rooms, 4 - left hall, 3 - above middle, 2 - middle, 5 - right hall, 0 - right rooms
//		crs.cutRectangleFromSide(rightRoomsId, side, 1);
		int firstSideRoom = 6;
		// Place left rooms and link them with left hall
		while(dir.isH() && crs.content.get(1).height > 5 || 
				dir.isV() && crs.content.get(1).width > 5) {
			crs.cutRectangleFromSide(leftRoomsId, side, 4);
			crs.link(leftHallId, crs.content.size()-1);
		}
		// Link last room
		crs.link(leftRoomsId, leftHallId);
		// Place right rooms and link them with right hall
		while(dir.isH() && crs.content.get(0).height > 5 || 
				dir.isV() && crs.content.get(0).width > 5) {
			crs.cutRectangleFromSide(rightRoomsId, side, 4);
			crs.link(rightHallId, crs.content.size()-1);
		}
		// Link last room
		crs.link(rightRoomsId, rightHallId);
		crs.link(rightHallId, lobbyId);
		crs.link(leftHallId, lobbyId);
		crs.link(aboveLobbyId, leftHallId);
		
		rectangleSystem = settlement.getGraph(crs);
		rectangleSystem.initialFindOuterSides();
		rectangleSystem.content.get(lobbyId).stretch(side, -1);
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
		
		placeFrontDoor(lobbyId, side);
		
		/* CONTENTS */
		Rectangle lobbyRec = rectangleSystem.content.get(lobbyId);
//		for (int i=firstSideRoom, size = crs.rectangles.size();i<size;i++) {
//			ArrayList<Coordinate> cells = getCellsNearWalls(crs.rectangles.get(i));
//		}
	}
	public boolean fitsToPlace(BuildingPlace place) {
		return (place.width > 23 || place.height > 23);
	}
}
