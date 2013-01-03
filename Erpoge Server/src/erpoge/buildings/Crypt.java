package erpoge.buildings;

import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.core.Building;
import erpoge.core.EnhancedRectangle;
import erpoge.core.StaticData;
import erpoge.core.TerrainBasics;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.terrain.CellCollection;
import erpoge.core.terrain.settlements.BuildingPlace;

public class Crypt extends Building {
	public static final long serialVersionUID = 836362727L;
	public Coordinate stairsCoord;

	public void draw() {
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		int floorStone = StaticData.getFloorType("stone").getId();
		int objStatueGargoyle = StaticData.getObjectType("statue_gargoyle").getId();
		int objStairsDown = StaticData.getObjectType("stairs_down").getId();
		buildBasis(wallGreyStone);
		settlement.square(x, y, width, height, TerrainBasics.ELEMENT_FLOOR, floorStone, true);
		ArrayList<Rectangle> roomsValues = new ArrayList<Rectangle>(rooms);
		for (Rectangle r : rooms) {
			ArrayList<Coordinate> doorCells = getCellsNearDoors(r);
			int amountOfStatuePairs = Chance.rand(0, (r.width + r.height) - 4);
			for (int i = 0; i < amountOfStatuePairs; i++) {
				if (Chance.roll(50)) {
					int dy = Chance.rand(0, r.height - 1);
					settlement.setObject(r.x, r.y + dy, objStatueGargoyle);
					settlement.setObject(r.x + r.width - 1, r.y + dy, objStatueGargoyle);
				} else {
					int dx = Chance.rand(0, r.width - 1);
					settlement.setObject(r.x + dx, r.y, objStatueGargoyle);
					settlement.setObject(r.x + dx, r.y + r.height - 1, objStatueGargoyle);
				}
			}
			for (Coordinate c : doorCells) {
				settlement.setObject(c.x, c.y, StaticData.VOID);
			}
		}
		EnhancedRectangle stairsRec = new EnhancedRectangle(roomsValues.get(Chance.rand(0, roomsValues.size() - 1)));
		CellCollection stairsRoomCS = settlement.newCellCollection(stairsRec.getCells());
		stairsCoord = stairsRoomCS.setElementAndReport(TerrainBasics.ELEMENT_OBJECT, objStairsDown);
	}

	@Override
	public boolean fitsToPlace(BuildingPlace place) {
		return true;
	}
}
