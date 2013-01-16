package erpoge.locationtypes;

import erpoge.core.HorizontalPlane;
import erpoge.core.Location;
import erpoge.core.StaticData;
import erpoge.core.TerrainBasics;

public class Empty extends Location {
	public Empty(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height, "");
		int wallGreyStone = StaticData.getObjectType("wall_gray_stone").getId();
		fillWithCells(1, 0);
		square(4, 4, 12, 12, TerrainBasics.ELEMENT_OBJECT, wallGreyStone, false);
		square(6, 6, 8, 8, TerrainBasics.ELEMENT_OBJECT, wallGreyStone, false);
		setObject(6, 10, StaticData.VOID);
		// createCharacter("bear", "Миша", 12, 9);
	}
}
