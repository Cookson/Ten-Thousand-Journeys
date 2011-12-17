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
import erpoge.terrain.Location;
import erpoge.terrain.LocationGenerator;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.World;
import erpoge.terrain.settlements.buildings.Inn;

import java.util.ArrayList;

public class Empty extends LocationGenerator {
	public Empty(Location location) {
		super(location);
		fillWithCells(1, 0);
		setStartArea(5, 10, 5, 6);
		makePeaceful();
//		new Inn(this, 6,6,14,28);
	}
}
