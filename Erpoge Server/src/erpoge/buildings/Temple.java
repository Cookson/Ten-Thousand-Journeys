package erpoge.buildings;

import java.awt.Rectangle;

import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.net.RectangleArea;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.TerrainBasics;
import erpoge.core.terrain.settlements.Building;
import erpoge.core.terrain.settlements.BuildingPlace;

public class Temple extends Building {
	public static final long serialVersionUID = 801812251L;
	public void draw() {
		// Direction dir;
		Side side = Side.N;
		// int lobbyWidth = 6;
		// if (side == Side.N || side == Side.S) {
		// 	dir = Direction.H;
		// } else {
			// dir = Direction.V;
		// }
		
		// For two of four sides we should revert width of cut rectangle
		CustomRectangleSystem crs = new CustomRectangleSystem(x, y, width, height, 1);

		crs.cutRectangleFromSide(0, side.opposite(), 4);
		
		// 0 - area behind altar, 1 - main area
		int treesRec = crs.cutRectangleFromSide(0, side.counterClockwise(), 4);
		crs.excludeRectangle(treesRec);
		
		rectangleSystem = settlement.getGraph(crs);
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE);
		setLobby(1);
		
		/* CONTENT */
		// Althar and cathedra
		RectangleArea mainRec = rectangleSystem.content.get(1);
		RectangleArea backRec = rectangleSystem.content.get(0);
		Coordinate c = mainRec.getMiddleOfSide(side.opposite());
		Coordinate c2 = new Coordinate(c);
		c2.moveToSide(side,1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_HUMAN_ALTAR);
		settlement.setObject(c2.x, c2.y, GameObjects.OBJ_HUMAN_TRIBUNE);
		
		// Benches
		c = mainRec.getCellFromSide(side, side.clockwise(), 0);
		c2 = mainRec.getCellFromSide(side, side.counterClockwise(), 0);
		int limit = (side.isVertical() ? mainRec.height : mainRec.width)-5;
		for (int i=0; i<limit; i++) {
			settlement.line(c.x, c.y, c2.x, c2.y, TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_BENCH);
			c.moveToSide(side.opposite(), 2);
			c2.moveToSide(side.opposite(), 2);
		}
		
		// Passage from door to tribune
		c2 = mainRec.getMiddleOfSide(side.opposite());
		c2.moveToSide(side, 2);
		c = mainRec.getMiddleOfSide(side);
		settlement.line(c.x, c.y, c2.x, c2.y, TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_VOID);
		
		// Door to back room
		c = mainRec
			.getCellFromSide(side.opposite(), side.counterClockwise(), 1);
		c.moveToSide(side.opposite(), 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_DOOR_BLUE);
		
		// Front door
		c = mainRec.getMiddleOfSide(side);
		c.moveToSide(side, 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_DOOR_BLUE);
		
		// Statues
		c.moveToSide(side, 1);
		c.moveToSide(side.clockwise(), 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_STATUE_DEFENDER_1);
		c.moveToSide(side.counterClockwise(), 2);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_STATUE_FEMALE_ELF_3);
		
		// Trees behind temple
		Rectangle r = crs.excluded.get(treesRec);
		for (int rx = r.x; rx<r.x+r.width; rx += 2) {
			for (int ry = r.y; ry<r.y+r.height; ry += 2) {
				settlement.setObject(rx, ry, GameObjects.OBJ_TREE_1);
			}
		}
		
		// Back door
		c = backRec.getCellFromSide(side.clockwise(), side.opposite(), 1);
		c.moveToSide(side.clockwise(), 1);
		settlement.setObject(c.x, c.y, GameObjects.OBJ_DOOR_BLUE);
		
		// Chests
		c = backRec.getCellFromSide(side.counterClockwise(), side.opposite(), 0);
		while (backRec.contains(c)) {
			settlement.setObject(c.x, c.y, GameObjects.OBJ_CHEST_1);
			c.moveToSide(side, 1);
		}
		
		// Ceilings
		for (Rectangle ceiling : rectangleSystem.content.values()) {
			settlement.createCeiling(ceiling, 1);
		}
	}
	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		return place.width > 14 || place.height > 14;
	}
}
