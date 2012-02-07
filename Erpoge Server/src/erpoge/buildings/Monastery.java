package erpoge.buildings;
//package erpoge.terrain.settlements.buildings;
//
//import java.awt.Rectangle;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Set;
//
//import erpoge.Chance;
//import erpoge.Main;
//import erpoge.objects.GameObjects;
//import erpoge.terrain.CellCollection;
//import erpoge.terrain.Location;
//import erpoge.terrain.TerrainBasics;
//import erpoge.terrain.settlements.Building;
//import erpoge.terrain.settlements.BuildingPlace;
//import erpoge.terrain.settlements.Settlement;
//import erpoge.characters.Character;
//import erpoge.graphs.RectangleSystem;
//
//public class Monastery extends Building {
//	public void draw() {
//		RectangleSystem mainRS = settlement.getGraph(x, y, width, height, 3, 1);
//		ArrayList<Integer> outerRecNums = mainRS.getOuterRectanglesNums();
//		ArrayList<Integer> entranceRecs = new ArrayList<Integer>();
//		ArrayList<Integer> excludedOuterRectangles = new ArrayList<Integer>();
//		for (int i : outerRecNums) {
//			if (mainRS.outerSides.get(i).size() == 1) {
//				entranceRecs.add(i);
//			}
//		}
//		
//		// Exclude outer rectangles to shape monastery
//		loopI : for (int i = 0; i < 3; i++) {
//			outerRecNums = mainRS.getOuterRectanglesNums();
//			loopJ : for (int j = 0; j < outerRecNums.size(); j++) {
//				int num = outerRecNums.get(j);
//				if (mainRS.outerSides.get(num).size() > 1) {
//					mainRS.excludeRectangle(num);
//					excludedOuterRectangles.add(num);
//					if (entranceRecs.contains(num)) {
//						entranceRecs.remove(entranceRecs.indexOf(num));
//					}
//					int numOf1Rec = 0;
//					for (int k : entranceRecs) {
//						if (mainRS.outerSides.get(k).size() == 1) {
//							numOf1Rec++;
//						}
//					}
//					if (numOf1Rec > 1) {
//						continue loopJ;
//					}
//					break loopI;
//				}
//			}
//		}
//
//		// Choose an entrance rectangle
//		int entranceRec = -1;
//		for (int i : entranceRecs) {
//			if (mainRS.outerSides.get(i).size() == 1) {
//				entranceRec = i;
//				break;
//			}
//		}
//		outerRecNums = mainRS.getOuterRectanglesNums();
//
//		// Exclude inner rectangles to make "training place"
//		Set<Integer> keys = new HashSet<Integer>(mainRS.rectangles.keySet());
//		ArrayList<Integer> innerSpace = new ArrayList<Integer>();
//		for (int i : keys) {
//			if (!outerRecNums.contains(i)) {
//				mainRS.excludeRectangle(i);
//				innerSpace.add(i);
//			}
//		}
//		if (entranceRec == -1) {
//			throw new Error("Outer rec with one outer side not found");
//		}
//
//		// Make entrance
//		mainRS.excludeRectangle(entranceRec);
//
//		// Place walls
//		mainRS.drawBorders(TerrainBasics.ELEMENT_OBJECT,
//				GameObjects.OBJ_WALL_WOODEN, false);
//
//		// Connect inner space and rooms with doors
//		// usedRooms - to connect rooms with only one inner space rectangle
//		ArrayList<Rectangle> usedRooms = new ArrayList<Rectangle>();
//		for (int i : innerSpace) {
//			Rectangle r1 = mainRS.excluded.get(i);
//			for (Rectangle r2 : mainRS.rectangles.values()) {
//				if (mainRS.areRectanglesNear(r1, r2) && !usedRooms.contains(r2)) {
//					usedRooms.add(r2);
//					connectRoomsWithDoor(r1, r2, GameObjects.OBJ_DOOR_BLUE);
//				}
//			}
//		}
//
//		// Connect not linked rooms with doors
//		mainRS.convertDoubleEdgesToSingle();
//		for (int i : mainRS.rectangles.keySet()) {
//			Rectangle r1 = mainRS.rectangles.get(i);
//			if (usedRooms.contains(r1)) {
//				continue;
//			}
//			for (int j : mainRS.edges.get(i)) {
//				Rectangle r2 = mainRS.rectangles.get(j);
//				connectRoomsWithDoor(r1, r2, GameObjects.OBJ_DOOR_BLUE);
//				break;
//			}
//		}
//
//		// Place objects and characters in rooms
//		for (Rectangle r : mainRS.rectangles.values()) {
//			CellCollection cellsNearWalls = settlement
//					.newCellCollection(getCellsNearWalls(r));
//			cellsNearWalls.setObjects(GameObjects.OBJ_BED, 1);
//			cellsNearWalls.setObjects(GameObjects.OBJ_BARREL, 1);
//			cellsNearWalls.setObjects(GameObjects.OBJ_CHEST_1, 1);
////			cellsNearWalls.setCharacter("dwarvenHooker", "���������-�����������").setFraction(Character.FRACTION_NEUTRAL);
//		}
//		
//		
//		// Check structure for connectivity of inner part and road part
//	}
//	@Override
//	public boolean fitsToPlace(BuildingPlace place) {
//		return false;
//	}
//}
