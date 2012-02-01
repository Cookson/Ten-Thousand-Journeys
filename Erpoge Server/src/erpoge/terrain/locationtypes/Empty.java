package erpoge.terrain.locationtypes;

import java.awt.Rectangle;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.characters.*;
import erpoge.characters.Character;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.graphs.RectangleSystem;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.CellCollection;
import erpoge.terrain.Container;
import erpoge.terrain.HorizontalPlane;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.settlements.buildings.Inn;

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
