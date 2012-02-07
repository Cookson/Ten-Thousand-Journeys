package erpoge.locationtypes;

import java.awt.Rectangle;

import erpoge.buildings.Inn;
import erpoge.core.Main;
import erpoge.core.characters.*;
import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.graphs.RectangleSystem;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.Cell;
import erpoge.core.terrain.CellCollection;
import erpoge.core.terrain.Container;
import erpoge.core.terrain.HorizontalPlane;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.TerrainBasics;

import java.util.ArrayList;

public class Empty extends Location {
	public Empty(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height, "");
		fillWithCells(1, 0);
		square(4,4,12,12,TerrainBasics.ELEMENT_OBJECT,GameObjects.OBJ_WALL_GREY_STONE,false);
		square(6,6,8,8,TerrainBasics.ELEMENT_OBJECT,GameObjects.OBJ_WALL_GREY_STONE,false);
		setObject(6,10,GameObjects.OBJ_VOID);
//		createCharacter("bear", "Миша", 12, 9);
	}
}
