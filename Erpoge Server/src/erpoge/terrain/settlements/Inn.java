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
		Main.console("HELLO");
		/* BASIS */
		// Lobby
		boolean dir;
		int lobbyWidth = 6;
		if (width > height) {
			dir = true;
		} else if (height < width) {
			dir = false;
		} else {
			dir = Chance.roll(50);
		}
		crs.splitRectangle(0, dir, (((dir)?width:height)-lobbyWidth)/2-1);
		// Separate middle rectangle (lobby) and right rectangle
		crs.splitRectangle(1, dir, lobbyWidth);
		// Separate rectangle above lobby
		crs.splitRectangle(1, !dir, 4);		
		// Left rooms
		crs.splitRectangle(0, dir, -4);
		// Right rooms
		crs.splitRectangle(2, dir, 4);
		// 0 - left rooms, 4 - left, 1 - above middle, 3 - middle, 2 - right, 5 - right rooms
		// Place left rooms
		while(dir && crs.rectangles.get(0).height > 4*2 || 
				!dir && crs.rectangles.get(0).width > 4*2) {
			crs.splitRectangle(0, !dir, -4);
			crs.link(4, crs.rectangles.size()-1);
		}
		// Link them with hall
		crs.link(4, 0);
		// Place right rooms
		while(dir && crs.rectangles.get(5).height > 4*2 || 
				!dir && crs.rectangles.get(5).width > 4*2) {
			crs.splitRectangle(5, !dir, -4);
			crs.link(2, crs.rectangles.size()-1);
		}
		// Link them with hall
		crs.link(5, 2);
		
		crs.link(3, 4);
		crs.link(3, 1);
		crs.link(3, 2);
		
		rectangleSystem = settlement.getGraph(crs);
		rectangleSystem.initialFindOuterSides();
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE, BasisBuildingSetup.NOT_BUILD_EDGES);
		
		placeFrontDoor(3, -1);
		
		/* CONTENTS */
		Rectangle lobbyRec = rectangleSystem.rectangles.get(3);
		NonPlayerCharacter innkeeper = settlement.createCharacter("innkeeper", "Christian", lobbyRec.x, lobbyRec.y+1);
		
	}
}
