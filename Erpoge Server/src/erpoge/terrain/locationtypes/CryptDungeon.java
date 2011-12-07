package erpoge.terrain.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import erpoge.Coordinate;
import erpoge.Main;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Location;
import erpoge.terrain.LocationGenerator;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.Building.BasisBuildingSetup;

public class CryptDungeon extends LocationGenerator {
	public CryptDungeon(Location location) {
		super(location);
		fillWithCells(GameObjects.FLOOR_STONE, GameObjects.OBJ_WALL_GREY_STONE);
		CustomRectangleSystem mainCRS = new CustomRectangleSystem(this, 3, 3,
				width - 6, height - 6, 0);
		// Draw hall

		mainCRS.splitRectangle(0, false, 14);
		mainCRS.splitRectangle(mainCRS.size() - 1, false, 4);
		// So hallRec after two splits is under index 1

		Rectangle hallRec = mainCRS.rectangles.get(1);
		square(hallRec.x, hallRec.y, hallRec.width, hallRec.height,
				ELEMENT_OBJECT, GameObjects.OBJ_VOID, true);
		RectangleSystem mainRS = getGraph(mainCRS);
		for (int i = 0; i < 3; i++) {
			// For each of other rectangles in mainRS
			if (i == 1) {
				// If it is hall rectangle - continue
				continue;
			}
			Rectangle sideRec = mainRS.rectangles.get(i);
			Set<Coordinate> doorsNotOnBorder = new HashSet<Coordinate>();
			Building sideRooms;
			int numberOfAttempts = 0;
			do {
			// Generate rectangleSystems until we get a connected one
				sideRooms = new Building(this, sideRec.x, sideRec.y,
						sideRec.width, sideRec.height, 2);
				sideRooms.rectangleSystem.nibbleSystem(1, 50);
				sideRooms.rectangleSystem.excludeIsolatedVertexes();
				if (++numberOfAttempts > 20) {
					throw new Error("Generator has trouble generating level");
				}
			} while (!sideRooms.rectangleSystem.isConnected());
			
			// Draw rooms
			sideRooms.buildBasis(GameObjects.OBJ_WALL_GREY_STONE, BasisBuildingSetup.CONVERT_TO_DIRECTED_TREE);
			sideRooms.clearBasisInside();
			for (int side = RectangleSystem.SIDE_N; side < RectangleSystem.SIDE_W; side++) {
			// Find inner side of this rectangle (this is a one-iteration cycle where we find approptiate side)
				if (!mainRS.outerSides.get(i).contains(side)) {
					square(sideRec.x, sideRec.y, sideRec.width, sideRec.height,
							ELEMENT_OBJECT, GameObjects.OBJ_WALL_GREY_STONE,
							false);
					for (int k = 0; k < 2; k++) {
						// Place doors from side rooms to the hall and save
						// their numbers if doors are not leading right to the
						// hall
						Coordinate c = sideRooms.placeFrontDoor(side);
						if (!isCellOnRectangleBorder(c.x, c.y, sideRec)) {
							doorsNotOnBorder.add(c);
						}
					}

					for (Coordinate c : doorsNotOnBorder) {
						// Draw paths from doors to the hall
						c.moveToSide(side);
						lineToRectangleBorded(c.x, c.y, side, sideRec,
								ELEMENT_OBJECT, GameObjects.OBJ_VOID);
					}
					break;
				}
			}
		}
	}
}
