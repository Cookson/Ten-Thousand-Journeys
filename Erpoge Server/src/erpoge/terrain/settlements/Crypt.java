package erpoge.terrain.settlements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.CellCollection;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.TerrainGenerator;

public class Crypt extends Building {
	public Coordinate stairsCoord;
	public Crypt(TerrainGenerator settlement, int x, int y, int width,
			int height) {
		super(settlement, x, y, width, height, 5);
		
		buildBasis(GameObjects.OBJ_WALL_GREY_STONE, false);
		settlement.square(x, y, width, height, TerrainBasics.ELEMENT_FLOOR,
				GameObjects.FLOOR_STONE, true);
		ArrayList<Rectangle> roomsValues = new ArrayList<Rectangle>(
				rooms.values());
		for (Rectangle r : rooms.values()) {
			ArrayList<Coordinate> doorCells = getCellsNearDoors(r);
			int amountOfStatuePairs = Chance.rand(0, (r.width + r.height) - 4);
			for (int i = 0; i < amountOfStatuePairs; i++) {
				if (Chance.roll(50)) {
					int dy = Chance.rand(0, r.height - 1);
					settlement.setObject(r.x, r.y + dy,
							GameObjects.OBJ_STATUE_GARGOYLE);
					settlement.setObject(r.x + r.width - 1, r.y + dy,
							GameObjects.OBJ_STATUE_GARGOYLE);
				} else {
					int dx = Chance.rand(0, r.width - 1);
					settlement.setObject(r.x + dx, r.y,
							GameObjects.OBJ_STATUE_GARGOYLE);
					settlement.setObject(r.x + dx, r.y + r.height - 1,
							GameObjects.OBJ_STATUE_GARGOYLE);
				}
			}
			for (Coordinate c : doorCells) {
				settlement.setObject(c.x, c.y, GameObjects.OBJ_VOID);
			}
		}
		Rectangle stairsRec = roomsValues.get(Chance.rand(0,
				roomsValues.size() - 1));
		CellCollection stairsRoomCS = settlement.newCellCollection(settlement
				.getRectangleCells(stairsRec));
		stairsCoord = stairsRoomCS.setElementAndReport(
				TerrainBasics.ELEMENT_OBJECT,
				GameObjects.OBJ_STAIRS_SONE_GREY_DOWN);
	}
}
