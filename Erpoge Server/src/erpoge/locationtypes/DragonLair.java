package erpoge.locationtypes;
//package erpoge.terrain.locationtypes;
//
//import java.awt.Rectangle;
//import java.util.ArrayList;
//import java.util.Set;
//
//import erpoge.Chance;
//import erpoge.Coordinate;
//import erpoge.Main;
//import erpoge.graphs.RectangleSystem;
//import erpoge.objects.GameObjects;
//import erpoge.terrain.CellCollection;
//import erpoge.terrain.HorizontalPlane;
//import erpoge.terrain.Location;
//import erpoge.terrain.LocationGenerator;
//import erpoge.terrain.TerrainGenerator;
//
//public class DragonLair extends Location {
//	public DragonLair(HorizontalPlane plane, int x, int y, int width, int height) {
//		super(plane, x, y, width, height);
//		fillWithCells(GameObjects.FLOOR_GROUND, GameObjects.OBJ_WALL_CAVE);
//		RectangleSystem mainRS = getGraph(1, 1, width - 2, height - 2, 5, 1);
//		mainRS.initialFindOuterSides();
//		ArrayList<Integer> outerRecs = mainRS.getOuterRectanglesNums();
//		
//		// ������ ������, ������������ � ������ �� ������� ���������������
//		ArrayList<Integer> deadEnds = mainRS.convertGraphToDirectedTree(outerRecs.get(Chance.rand(0,
//				outerRecs.size() - 1)));
//		setStartArea(mainRS.rectangles.get(deadEnds.get(0)));
//		
//		Set<Integer> keys = mainRS.edges.keySet();
//		for (int k : keys) {
//			Rectangle r1 = mainRS.rectangles.get(k);
//			for (int r : mainRS.edges.get(k)) {
//				Rectangle r2 = mainRS.rectangles.get(r);
//				boldLine(r1.x + (int) Math.floor(r1.width / 2), r1.y
//						+ (int) Math.floor(r1.height / 2),
//						r2.x + (int) Math.floor(r2.width / 2), r2.y
//								+ (int) Math.floor(r2.height / 2),
//						ELEMENT_REMOVE, GameObjects.OBJ_VOID, 3);
//			}
//		}
//		cellularAutomataSmooth(3, ELEMENT_OBJECT,
//				GameObjects.OBJ_WALL_CAVE, GameObjects.OBJ_VOID);
//
//		// Dead ends
//		// Find the furthest deadEnd from start
//		int dragonEnd = deadEnds.get(1);
//		Rectangle r1 = mainRS.rectangles.get(deadEnds.get(0));
//		Rectangle r2 = mainRS.rectangles.get(dragonEnd);
//		for (int i=2; i<deadEnds.size(); i++) {
//			Rectangle r3 = mainRS.rectangles.get(deadEnds.get(i));
//			if (distance(r3.x, r3.y, r1.x, r1.y) < distance(r2.x, r2.y, r1.x, r1.y)) {
//				dragonEnd = i;
//				r2 = r3;
//			}
//		}
//		
//		// Additional rooms
//		RectangleSystem additionalRS = getGraph(6, 6, width-12, height-12, 4, 0);
//		additionalRS.excludeRectanglesHaving(ELEMENT_OBJECT, GameObjects.OBJ_VOID);
//		while (additionalRS.size() > 0) {
//			Rectangle r = additionalRS.getRandomRectangle();
//			ArrayList<Coordinate> closeCells = closeCells(r.x, r.y, 6, PASSABILITY_NO, true);
//			ArrayList<Coordinate> borderCells = getElementsAreaBorder(r.x, r.y, ELEMENT_OBJECT, 
//					GameObjects.OBJ_WALL_GREY_STONE, 8, false);
//			
//			newCellCollection(closeCells)
//					.fillWithElements(ELEMENT_OBJECT, GameObjects.OBJ_VOID);
//			CellCollection collectionBorder = newCellCollection(borderCells);
//			collectionBorder.fillWithElements(ELEMENT_OBJECT, GameObjects.OBJ_WALL_GREY_STONE);
//			// Exit (entrance)
//			Coordinate entranceCell = collectionBorder.getRandomCell();
//			newCellCollection(
//					CellCollection.rectangleToCellsList(new Rectangle(entranceCell.x-1, entranceCell.y-1, 3, 3)))
//					.fillWithElements(ELEMENT_OBJECT, GameObjects.OBJ_VOID);
//			// Modify rectangle system
////			additionalRS.excludeRectanglesHaving(ELEMENT_OBJECT, GameObjects.OBJ_DOOR_BLUE);
//			additionalRS.excludeRectanglesHaving(ELEMENT_OBJECT, GameObjects.OBJ_VOID);
//		}
//	}
//}
