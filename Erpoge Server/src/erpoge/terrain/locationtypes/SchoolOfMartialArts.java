package erpoge.terrain.locationtypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import erpoge.Main;
import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Location;
import erpoge.terrain.LocationGenerator;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.Monastery;

public class SchoolOfMartialArts extends LocationGenerator {
	public SchoolOfMartialArts(Location location) {
		super(location);
		int groundType = GameObjects.FLOOR_GRASS;
		fillWithCells(groundType, GameObjects.OBJ_VOID);
		makePeaceful();
		Building monastery = new Monastery(this, 5, 5, width-10, height-10); 
	}
}
