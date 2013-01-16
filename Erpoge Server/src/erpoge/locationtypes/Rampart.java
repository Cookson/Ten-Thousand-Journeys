package erpoge.locationtypes;
//package erpoge.terrain.locationtypes;
//
//import java.awt.Rectangle;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import erpoge.Chance;
//import erpoge.Main;
//import erpoge.characters.CustomCharacterAction;
//import erpoge.graphs.CustomRectangleSystem;
//import erpoge.graphs.RectangleSystem;
//import erpoge.objects.GameObjects;
//import erpoge.terrain.CellCollection;
//import erpoge.terrain.HorizontalPlane;
//import erpoge.terrain.Location;
//import erpoge.terrain.settlements.Settlement;
//
//public class Rampart extends Settlement {
//	public Rampart(HorizontalPlane plane, int x, int y, int width, int height) {
//		super(plane, x, y, width, height);
//		RectangleSystem mainRS = getGraph(3, 3, width - 6, height - 6, 4, 4);
//
//		// CustomRectangleSystem crs = new CustomRectangleSystem(this, 10, 20,
//		// 15, 15, 0);
//		// crs.splitRectangle(0, true, 7);
//		// crs.splitRectangle(1, false, 4);
//		// crs.splitRectangle(2, false, 3);
//		// crs.splitRectangle(0, false, 8);
//		// RectangleSystem mainRS = getGraph(crs);
//		// mainRS.drawBorders(ELEMENT_OBJECT, GameObjects.OBJ_wall_gray_stone,
//		// false);
//		// mainRS.rectangles.get(2)));
//
////		Set<Integer> keys = mainRS.getRectanglesKeys();
////		int cx = mainRS.startX + mainRS.width / 2;
////		int cy = mainRS.startY + mainRS.height / 2;
////		int radius = Math.min(width, height) / 2 - 5;
////		for (int i : keys) {
////			if (!mainRS.isRectangleInCircle(mainRS.rectangles.get(i), cx, cy,
////					radius)) {
////				mainRS.excludeRectangle(i);
////			}
////		}
////		mainRS.connectCornersWithLines(ELEMENT_OBJECT,
////				GameObjects.OBJ_wall_gray_stone, 0, true);
////		mainRS.connectCornersWithLines(ELEMENT_OBJECT,
////				GameObjects.OBJ_wall_gray_stone, 3, true);
////
////		int palaceRecNum = mainRS.getRandomRectangleNum();
////		Set<Rectangle> palaceRecs = new HashSet<Rectangle>();
////		palaceRecs.add(mainRS.rectangles.get(palaceRecNum));
////		ArrayList<Integer> edges = new ArrayList<Integer>(mainRS.edges
////				.get(palaceRecNum));
////		for (int e : edges) {
////			palaceRecs.add(mainRS.rectangles.get(e));
////			mainRS.excludeRectangle(e);
////			break;
////		}
////		mainRS.excludeRectangle(palaceRecNum);
////
////		RectangleSystem palaceRS = RectangleSystem
////				.createSystemFromRectangleSet(this, palaceRecs,
////						mainRS.borderWidth);
////		palaceRS.expandRectanglesToBorder(2);
//		
//		// palaceRS.drawBorders(ELEMENT_OBJECT, GameObjects.OBJ_wall_gray_stone,
//		// false);
////		palaceRS.fillContents(ELEMENT_FLOOR, GameObjects.FLOOR_WATER);
//		// palaceRS.connectCornersWithLines(ELEMENT_OBJECT,
//		// GameObjects.OBJ_wall_gray_stone, 0, false);
//		new CellCollection(polygon(RectangleSystem.getOuterPoints(mainRS
//				.getRectanglesCorners(), mainRS.getCenter().x, mainRS
//				.getCenter().y), true), this).fillWithElements(ELEMENT_OBJECT, GameObjects.OBJ_wall_gray_stone);
//		// Set<Integer> entries = new HashSet<Integer>
//		// (mainRS.rectangles.keySet());
//		// for (int i : entries) {
//		// if (!mainRS.rectangles.containsKey(i)) {
//		// continue;
//		// }
//		// Rectangle r = mainRS.rectangles.get(i);
//		// new Tavern(this, r.x, r.y, r.width, r.height);
//		// }
//	}
//}
