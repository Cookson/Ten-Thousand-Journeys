package erpoge.terrain.settlements;

import java.awt.Rectangle;

import erpoge.Chance;
import erpoge.Main;
import erpoge.characters.NonPlayerCharacter;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.locationtypes.Settlement.QuarterSystem.BuildingPlace;

public class Inn extends Building {
	public Inn(TerrainGenerator settlement, int x, int y, int width,
			int height, BuildingPlace place) {
		super(settlement, x, y, width, height, place);
		CustomRectangleSystem crs = new CustomRectangleSystem(settlement.location,x,y,width,height,1);
		/* BASIS */
		// Lobby
		boolean dir;
		int side = doorSides.get(0);
		int lobbyWidth = 6;
		if (side == SIDE_N || side == SIDE_S) {
			dir = false;
		} else {
			dir = true;
		}
		
		// For two of four sides we should revert width of cut rectangle
		int sideMod = (side == SIDE_N || side == SIDE_W) ? -1 : 1;
		
		crs.splitRectangle(0, !dir, ((((!dir)?width:height)-lobbyWidth)/2-1)*sideMod);
		// Separate middle rectangle (lobby) and right rectangle
		crs.splitRectangle(1, !dir, lobbyWidth*sideMod);
		// Separate rectangle above lobby
		crs.splitRectangle(1, dir, 4*sideMod);		
		// Left rooms
		crs.splitRectangle(0, !dir, -4*sideMod);
		// Right rooms
		crs.splitRectangle(2, !dir, 4*sideMod);
		// 4 - left rooms, 0 - left, 1 - above middle, 3 - middle, 2 - right, 5 - right rooms
		// Place left rooms
		settlement.createCharacter("innkeeper", "Christian", crs.rectangles.get(0).x, crs.rectangles.get(0).y);
		int newRecId = 4;
		while(!dir && crs.rectangles.get(newRecId).height > 4*2 || 
				dir && crs.rectangles.get(newRecId).width > 4*2) {
			crs.splitRectangle(newRecId, dir, -4*sideMod);
			crs.link(0, crs.rectangles.size()-1);
			newRecId = crs.rectangles.size()-1;
		}
		// Link them with hall
		crs.link(4, 0);
		// Place right rooms
		newRecId = 5;
		while(!dir && crs.rectangles.get(newRecId).height > 4*2 || 
				dir && crs.rectangles.get(newRecId).width > 4*2) {
			crs.splitRectangle(newRecId, dir, -4*sideMod);
			crs.link(2, crs.rectangles.size()-1);
			newRecId = crs.rectangles.size()-1;
		}
		// Link them with hall
		crs.link(5, 2);
		
		crs.link(3, 0);
		crs.link(3, 1);
		crs.link(3, 2);
		
		rectangleSystem = settlement.getGraph(crs);
		rectangleSystem.initialFindOuterSides();
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE, BasisBuildingSetup.NOT_BUILD_EDGES);
		Main.console(place);
		placeFrontDoor(3, side);
		
		/* CONTENTS */
		Rectangle lobbyRec = rectangleSystem.rectangles.get(3);
//		NonPlayerCharacter innkeeper = 
	}
}
