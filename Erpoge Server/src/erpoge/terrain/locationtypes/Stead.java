package erpoge.terrain.locationtypes;

import java.awt.Rectangle;

import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Location;
import erpoge.terrain.LocationGenerator;
import erpoge.terrain.settlements.House;
import erpoge.characters.Character;

public class Stead extends LocationGenerator {
	public Stead(Location location) {
		super(location);
		fillWithCells(GameObjects.FLOOR_GRASS, GameObjects.OBJ_VOID);
		RectangleSystem mainRS = getGraph(3, 3, width-6, height-6, 8, 1);
		mainRS.detectOuterRectangles();
		
		// Fill ground
		square(mainRS.startX, mainRS.startY, mainRS.width, mainRS.height, ELEMENT_FLOOR, GameObjects.FLOOR_GROUND, true);
		
		// House
		int houseRecNum = mainRS.getRandomOuterRectangleNum();
		Rectangle houseRec = mainRS.rectangles.get(houseRecNum);
		mainRS.excludeRectangle(houseRecNum);
		new House(this, houseRec.x, houseRec.y, houseRec.width, houseRec.height, 4);

		// Barn
		int barnRecNum = mainRS.getRandomOuterRectangleNum();
		Rectangle barnRec = mainRS.rectangles.get(barnRecNum);
		mainRS.excludeRectangle(barnRecNum);
		new House(this, barnRec.x, barnRec.y, barnRec.width, barnRec.height, 4);
		square(mainRS.startX, mainRS.startY, mainRS.width, mainRS.height, ELEMENT_OBJECT, GameObjects.OBJ_WALL_LATTICE, false);
		
	}
}
