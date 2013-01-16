package erpoge.locationtypes;
//package erpoge.terrain.locationtypes;
//
//import java.awt.Rectangle;
//import java.util.ArrayList;
//
//import erpoge.Chance;
//import erpoge.Coordinate;
//import erpoge.Main;
//import erpoge.RectangleArea;
//import erpoge.Side;
//import erpoge.graphs.RectangleSystem;
//import erpoge.objects.GameObjects;
//import erpoge.terrain.CellCollection;
//import erpoge.terrain.Location;
//import erpoge.terrain.LocationGenerator;
//import erpoge.terrain.settlements.Crypt;
//import erpoge.characters.Character;
//
//public class Graveyard extends LocationGenerator {
//	public Graveyard(Location location) {
//		super(location);
//		fillWithCells(GameObjects.FLOOR_DRY_GRASS, GameObjects.OBJ_VOID);
//		RectangleSystem mainRS = getGraph(3, 3, width - 6, height - 6, 8, 2);
//		int cryptRecNum = mainRS.getRandomRectangleNum();
//		Rectangle cryptRec = mainRS.rectangles.get(cryptRecNum);
//		square(mainRS.startX - 1, mainRS.startY - 1, mainRS.width + 2,
//				mainRS.height + 2, ELEMENT_OBJECT,
//				GameObjects.OBJ_WALL_LATTICE, false);
//		Crypt crypt = new Crypt(this, cryptRec.x, cryptRec.y, cryptRec.width,
//				cryptRec.height);
//		Location cryptDungeon = new Location(width, height, type, name, world);
////		new CryptDungeon(cryptDungeon);
//		createPortal(crypt.stairsCoord.x, crypt.stairsCoord.y, cryptDungeon);
////		makePeaceful();
//		mainRS.drawBorders(ELEMENT_FLOOR, GameObjects.FLOOR_GROUND, true);
//		ArrayList<Side> sides = new ArrayList<Side>();
//		ArrayList<Side> outerSides = mainRS.outerSides.get(cryptRecNum);
//		for (int i = 1; i < 5; i++) {
//			if (!outerSides.contains(Side.int2side(i))) {
//				sides.add(Side.int2side(i));
//			}
//		}
//		crypt.placeFrontDoor(sides.get(Chance.rand(0, sides.size()-1)));
//		mainRS.excludeRectangle(cryptRecNum);
//		mainRS.detectOuterRectangles();
//		setStartArea(mainRS.getRandomOuterRectangle());
//
//		
//
//		int[] objTypes = new int[]{GameObjects.OBJ_GRAVE_1,
//				GameObjects.OBJ_GRAVE_2, GameObjects.OBJ_GRAVE_3,
//				GameObjects.OBJ_TREE_DEAD_1, GameObjects.OBJ_TREE_DEAD_2};
//		int[] graveTypes = new int[]{GameObjects.OBJ_GRAVE_1,
//				GameObjects.OBJ_GRAVE_2, GameObjects.OBJ_GRAVE_3};
//		int[] treesIds = new int[]{GameObjects.OBJ_TREE_DEAD_1,
//				GameObjects.OBJ_TREE_DEAD_2};
//		for (Rectangle r1 : mainRS.rectangles.values()) {
//			RectangleArea r = new RectangleArea(r1);
//			if (Chance.roll(10)) {
//				// Place a single grave (10%)
//				CellCollection cs = newCellCollection(r.getCells());
//				for (int i = 0; i < 14; i++) {
//					cs.setObjects(
//							treesIds[Chance.rand(0, treesIds.length - 1)], 1);
//				}
//				int sx = Chance.rand(r.x, r.x + r.width - 6);
//				int sy = Chance.rand(r.y, r.y + r.height - 6);
//				square(sx, sy, 5, 5, ELEMENT_OBJECT, GameObjects.OBJ_VOID, true);
//				setObject(sx, sy, GameObjects.OBJ_wall_gray_stone);
//				setObject(sx + 4, sy, GameObjects.OBJ_wall_gray_stone);
//				setObject(sx + 4, sy + 4, GameObjects.OBJ_wall_gray_stone);
//				setObject(sx, sy + 4, GameObjects.OBJ_wall_gray_stone);
//				setObject(sx + 2, sy + 2, GameObjects.OBJ_GRAVE_1);
//				square(sx, sy, 5, 5, ELEMENT_FLOOR, GameObjects.FLOOR_STONE,
//						true);
//			} else {
//				// Place several graves and trees (90%)
//				CellCollection cs = newCellCollection(r.getCells());
//				int amount = r.width * r.height / 10;
//				for (int i = 0; i < amount; i++) {
//					if (Chance.roll(3)) {
//						// Place people around grave
//						Coordinate c = cs.setElementAndReport(ELEMENT_OBJECT,
//								graveTypes[Chance
//										.rand(0, graveTypes.length - 1)]);
//						CellCollection cryersCS = newCellCollection(getCellsAroundCell(
//								c.x, c.y));
//						int amountOfCryers = Chance.rand(1,
//								Math.min(4, cryersCS.size()));
//						for (int j = 0; j < amountOfCryers; j++) {
//							cryersCS.setCharacter("dwarvenHooker",
//									"�����������").setFraction(
//									Character.FRACTION_NEUTRAL);
//						}
//					} else {
//						cs.setObjects(
//								objTypes[Chance.rand(0, objTypes.length - 1)],
//								1);
//					}
//				}
//			}
//		}
//	}
//}
