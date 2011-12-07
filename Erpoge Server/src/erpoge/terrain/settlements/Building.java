package erpoge.terrain.settlements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.CellCollection;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.locationtypes.Settlement;

public class Building {
	public static final int
		SIDE_N = 1,		
		SIDE_E = 2,
		SIDE_S = 3,
		SIDE_W = 4;
	public TerrainGenerator location;
	public Settlement settlement;
	public int type;
	public int x;
	public int y;
	public int width;
	public int height;
	public HashMap<Integer, Rectangle> rooms;
	public RectangleSystem rectangleSystem;
	public int lobby = -1;
	public int doorSide;
	public RectangleSystem quarter;
	public int keyInQuarter;
	public Coordinate frontDoor;
	private boolean hasSettlement;
	private ArrayList<Integer> hallways = new ArrayList<Integer>();
	public Building(TerrainGenerator settlement, int x, int y, int width,
			int height, int minRoomSize) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.location = settlement;
		hasSettlement = false;
		if (settlement instanceof Settlement) {
			Settlement s = (Settlement) settlement;
			this.settlement = s;
			s.buildings.add(this);
			if (s.quarters != null) {
				for (RectangleSystem q : s.quarters.values()) {
					if (RectangleSystem.rectangleHasCell(new Rectangle(
							q.startX, q.startY, q.width, q.height), x, y)) {
						quarter = q;
					}
				}
			}
			hasSettlement = true;
		}
		rectangleSystem = location.getGraph(x + 1, y + 1, width - 2,
				height - 2, minRoomSize, 1);
	}
	public Building(TerrainGenerator settlement, int x, int y, int width,
			int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.location = settlement;
		hasSettlement = false;
		if (settlement instanceof Settlement) {
			Settlement s = (Settlement) settlement;
			this.settlement = s;
			s.buildings.add(this);
			if (s.quarters != null) {
				// ����������, � ����� �������� ��������� ��� ������
				for (RectangleSystem q : s.quarters.values()) {
					if (RectangleSystem.rectangleHasCell(new Rectangle(
							q.startX, q.startY, q.width, q.height), x, y)) {
						quarter = q;
					}
				}
			}
			hasSettlement = true;
		}
	}

	public Coordinate placeFrontDoor(int side) {
		if (side == -1) {
			if (hasSettlement() && quarter != null) {
				keyInQuarter = quarter.findRectangleByCell(x, y);
				ArrayList<Integer> probableSides = quarter.outerSides
						.get(keyInQuarter);
				int keyInSettlement = settlement.rectangleSystem
						.findRectangleByCell(x, y);
				int sidesSize = probableSides.size();
				for (int sSide : settlement.rectangleSystem.outerSides
						.get(keyInSettlement)) {
					for (int i = 0; i < sidesSize; i++) {
						if (sSide == probableSides.get(i)) {
							probableSides.remove(i);
							i--;
							sidesSize--;
						}
					}
				}
				if (probableSides.size() == 0) {
					throw new Error("Building has no available door sides");
				}
				side = probableSides.get(Chance.rand(0,
						probableSides.size() - 1));
			} else {
				side = Chance.rand(1, 4);
			}
		}
		HashMap<Integer, Integer> cells = findDoorAppropriateCells(side);
		if (cells.size() == 0) {
			throw new Error("Nowhere to place the door from side " + side);
		}
		int dx, dy;
		if (side == TerrainBasics.SIDE_N || side == TerrainBasics.SIDE_S) {
			ArrayList<Integer> xes = new ArrayList<Integer>(cells.keySet());
			dx = xes.get(Chance.rand(0, xes.size() - 1));
			dy = cells.get(dx);
			location.setObject(dx, dy, GameObjects.OBJ_DOOR_BLUE);
		} else {
			ArrayList<Integer> yes = new ArrayList<Integer>(cells.keySet());
			dy = yes.get(Chance.rand(0, yes.size() - 1));
			dx = cells.get(dy);
			location.setObject(dx, dy, GameObjects.OBJ_DOOR_BLUE);
		}
		if (side == TerrainBasics.SIDE_N) {
			lobby = rectangleSystem.findRectangleByCell(dx, dy + 1);
		} else if (side == TerrainBasics.SIDE_E) {
			lobby = rectangleSystem.findRectangleByCell(dx - 1, dy);
		} else if (side == TerrainBasics.SIDE_S) {
			lobby = rectangleSystem.findRectangleByCell(dx, dy - 1);
		} else if (side == TerrainBasics.SIDE_W) {
			lobby = rectangleSystem.findRectangleByCell(dx + 1, dy);
		} else {
			throw new Error("Unappropriate side");
		}
		if (lobby == -1) {
			throw new Error(
					"Can't determine the lobby room because desired cell is not in this rectangle system");
		}
		doorSide = side;
		frontDoor = new Coordinate(dx, dy);
		return frontDoor;
	}
	public Coordinate placeFrontDoor(int rectangleId, int side) {
		Rectangle r = rectangleSystem.rectangles.get(rectangleId);
		if (side == -1) {
			side = rectangleSystem.outerSides.get(rectangleId).get(0);
		}
		HashMap<Integer, Integer> cells = findDoorAppropriateCells(r,side);
		if (cells.size() == 0) {
			throw new Error("Nowhere to place the door from side " + side);
		}
		int dx, dy;
		if (side == TerrainBasics.SIDE_N || side == TerrainBasics.SIDE_S) {
			ArrayList<Integer> xes = new ArrayList<Integer>(cells.keySet());
			dx = xes.get(Chance.rand(0, xes.size() - 1));
			dy = cells.get(dx);
			location.setObject(dx, dy, GameObjects.OBJ_DOOR_BLUE);
		} else {
			ArrayList<Integer> yes = new ArrayList<Integer>(cells.keySet());
			dy = yes.get(Chance.rand(0, yes.size() - 1));
			dx = cells.get(dy);
			location.setObject(dx, dy, GameObjects.OBJ_DOOR_BLUE);
		}
		if (side == TerrainBasics.SIDE_N) {
			lobby = rectangleSystem.findRectangleByCell(dx, dy + 1);
		} else if (side == TerrainBasics.SIDE_E) {
			lobby = rectangleSystem.findRectangleByCell(dx - 1, dy);
		} else if (side == TerrainBasics.SIDE_S) {
			lobby = rectangleSystem.findRectangleByCell(dx, dy - 1);
		} else if (side == TerrainBasics.SIDE_W) {
			lobby = rectangleSystem.findRectangleByCell(dx + 1, dy);
		} else {
			throw new Error("Unappropriate side");
		}
		if (lobby == -1) {
			throw new Error(
					"Can't determine the lobby room because desired cell is not in this rectangle system");
		}
		doorSide = side;
		frontDoor = new Coordinate(dx, dy);
		return frontDoor;
	}
	public boolean hasSettlement() {
		return hasSettlement;
	}
	public HashMap<Integer, Integer> findDoorAppropriateCells(int side) {
		HashMap<Integer, Integer> cells = new HashMap<Integer, Integer>();
		Set<Integer> keys;
		if (side == SIDE_N) {
			for (Rectangle r : rooms.values()) {
				int y = r.y - 1;
				for (int i = r.x; i < r.x + r.width; i++) {
					if (!cells.containsKey(i) || cells.get(i) > y) {
						cells.put(i, y);
					}
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int x : keys) {
				int y = cells.get(x);
				if (location.cells[x][y + 1].object() != 0) {
					cells.remove(x);
				}
			}
		} else if (side == SIDE_E) {
			for (Rectangle r : rooms.values()) {
				int x = r.x + r.width;
				for (int i = r.y; i < r.y + r.height; i++) {
					if (!cells.containsKey(i) || cells.get(i) < x) {
						cells.put(i, x);
					}
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int y : keys) {
				int x = cells.get(y);
				if (location.cells[x - 1][y].object() != 0) {
					cells.remove(y);
				}
			}
		} else if (side == SIDE_S) {
			for (Rectangle r : rooms.values()) {
				int y = r.y + r.height;
				for (int i = r.x; i < r.x + r.width; i++) {
					if (!cells.containsKey(i) || cells.get(i) < y) {
						cells.put(i, y);
					}
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int x : keys) {
				int y = cells.get(x);
				if (location.cells[x][y - 1].object() != 0) {
					cells.remove(x);
				}
			}
		} else if (side == SIDE_W) {
			for (Rectangle r : rooms.values()) {
				int x = r.x - 1;
				for (int i = r.y; i < r.y + r.height; i++) {
					if (!cells.containsKey(i) || cells.get(i) > x) {
						cells.put(i, x);
					}
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int y : keys) {
				int x = cells.get(y);
				if (location.cells[x + 1][y].object() != 0) {
					cells.remove(y);
				}
			}
		}
		return cells;
	}
	public HashMap<Integer, Integer> findDoorAppropriateCells(Rectangle r, int side) {
		HashMap<Integer, Integer> cells = new HashMap<Integer, Integer>();
		Set<Integer> keys;
		if (side == SIDE_N) {
			int y = r.y - 1;
			for (int i = r.x; i < r.x + r.width; i++) {
				if (!cells.containsKey(i) || cells.get(i) > y) {
					cells.put(i, y);
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int x : keys) {
				y = cells.get(x);
				if (location.cells[x][y + 1].object() != 0) {
					cells.remove(x);
				}
			}
		} else if (side == SIDE_E) {
			int x = r.x + r.width;
			for (int i = r.y; i < r.y + r.height; i++) {
				if (!cells.containsKey(i) || cells.get(i) < x) {
					cells.put(i, x);
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int y : keys) {
				x = cells.get(y);
				if (location.cells[x - 1][y].object() != 0) {
					cells.remove(y);
				}
			}
		} else if (side == SIDE_S) {
			int y = r.y + r.height;
			for (int i = r.x; i < r.x + r.width; i++) {
				if (!cells.containsKey(i) || cells.get(i) < y) {
					cells.put(i, y);
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int x : keys) {
				y = cells.get(x);
				if (location.cells[x][y - 1].object() != 0) {
					cells.remove(x);
				}
			}
		} else if (side == SIDE_W) {
			int x = r.x - 1;
			for (int i = r.y; i < r.y + r.height; i++) {
				if (!cells.containsKey(i) || cells.get(i) > x) {
					cells.put(i, x);
				}
			}
			keys = new HashSet<Integer>(cells.keySet());
			for (int y : keys) {
				x = cells.get(y);
				if (location.cells[x + 1][y].object() != 0) {
					cells.remove(y);
				}
			}
		}
		return cells;
	}
	public RectangleSystem buildBasis(int wallType /* =4 */,
			 BasisBuildingSetup setup/* =false */) {
		RectangleSystem graph = rectangleSystem;
//		if (notSimpleForm) {
//			if (graph.rectangles.size() > 3) {
//				// graph.initialFindOuterSides();
//				boolean formChanged = false;
//				Set<Integer> keys = graph.outerSides.keySet();
//				for (int k : keys) {
//					ArrayList<Integer> sides = graph.outerSides.get(k);
//					if (sides.size() == 0 || Chance.roll(70)) {
//						continue;
//					}
//					if (!graph.isVertexExclusible(k)) {
//						continue;
//					} else {
//						formChanged = true;
//						graph.excludeRectangle(k);
//					}
//				}
//				if (!formChanged) {
//					graph.excludeRectangle(0);
//				}
//			}
//		}
		if (setup == BasisBuildingSetup.CONVERT_TO_DIRECTED_TREE) {
			graph.convertGraphToDirectedTree();
		} else if (setup == BasisBuildingSetup.KEYPOINTS_BASED) {
			
		} else if (setup == BasisBuildingSetup.NOT_BUILD_EDGES) {
			
		}
		
		graph.drawBorders(1, wallType, false);
		int floorType = GameObjects.FLOOR_STONE;
		for (Rectangle r : graph.rectangles.values()) {
			fillFloor(r, floorType);
		}

		Set<Integer> keys = graph.edges.keySet();
		for (int k : keys) {
			ArrayList<Integer> edge = graph.edges.get(k);
			Rectangle r1 = graph.rectangles.get(k);
			for (int vertex : edge) {
				Rectangle r2 = graph.rectangles.get(vertex);
				Coordinate c = connectRoomsWithDoor(r1, r2,
						GameObjects.OBJ_DOOR_BLUE);
				location.setFloor(c.x, c.y, floorType);
			}
		}
		rooms = graph.rectangles;
		return graph;
	}
	public Coordinate connectRoomsWithDoor(Rectangle r1, Rectangle r2,
			int doorObjectId) {
		int x, y;
		if (r1.x + r1.width + 1 == r2.x || r2.x + r2.width + 1 == r1.x) {
			// Vertical
			x = Math.max(r1.x - 1, r2.x - 1);
			y = Chance.rand(Math.max(r1.y, r2.y),
					Math.min(r1.y + r1.height - 1, r2.y + r2.height - 1));
		} else {
			// Horizontal
			y = Math.max(r1.y - 1, r2.y - 1); // ��, ��� x ������ max, � �����
												// y - min.
			x = Chance.rand(Math.max(r1.x, r2.x),
					Math.min(r1.x + r1.width - 1, r2.x + r2.width - 1));
		}
		location.setObject(x, y, doorObjectId);
		return new Coordinate(x, y);
	}
	public void fillFloor(Rectangle r, int floorId) {
		location.square(r.x, r.y, r.width, r.height, 0, floorId, true);
	}
	public ArrayList<Coordinate> getCellsNearWalls(Rectangle r) {
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		for (int i = r.x + 1; i < r.x + r.width - 1; i++) {
			if (!location.isDoor(i, r.y - 1)) {
				answer.add(new Coordinate(i, r.y));
			}
			if (!location.isDoor(i, r.y + r.height)) {
				answer.add(new Coordinate(i, r.y + r.height - 1));
			}
		}
		for (int i = r.y + 1; i < r.y + r.height - 1; i++) {
			if (!location.isDoor(r.x - 1, i)) {
				answer.add(new Coordinate(r.x, i));
			}
			if (!location.isDoor(r.x + r.width, i)) {
				answer.add(new Coordinate(r.x + r.width - 1, i));
			}
		}
		// Checking cells in corners
		if (!location.isDoor(r.x, r.y - 1) && !location.isDoor(r.x - 1, r.y)) {
			answer.add(new Coordinate(r.x, r.y));
		}
		if (!location.isDoor(r.x + r.width - 1, r.y - 1)
				&& !location.isDoor(r.x + r.width, r.y)) {
			answer.add(new Coordinate(r.x + r.width - 1, r.y));
		}
		if (!location.isDoor(r.x + r.width, r.y + r.height - 1)
				&& !location.isDoor(r.x + r.width - 1, r.y + r.height)) {
			answer.add(new Coordinate(r.x + r.width - 1, r.y + r.height - 1));
		}
		if (!location.isDoor(r.x, r.y + r.height)
				&& !location.isDoor(r.x - 1, r.y + r.height - 1)) {
			answer.add(new Coordinate(r.x, r.y + r.height - 1));
		}
		return answer;
	}
	public ArrayList<Coordinate> getCellsNearDoors(Rectangle r) {
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		for (int i = r.x + 1; i < r.x + r.width - 1; i++) {
			if (location.isDoor(i, r.y - 1)) {
				answer.add(new Coordinate(i, r.y));
			}
			if (location.isDoor(i, r.y + r.height)) {
				answer.add(new Coordinate(i, r.y + r.height - 1));
			}
		}
		for (int i = r.y + 1; i < r.y + r.height - 1; i++) {
			if (location.isDoor(r.x - 1, i)) {
				answer.add(new Coordinate(r.x, i));
			}
			if (location.isDoor(r.x + r.width, i)) {
				answer.add(new Coordinate(r.x + r.width - 1, i));
			}
		}
		// Checking cells in corners
		if (location.isDoor(r.x, r.y - 1) || location.isDoor(r.x - 1, r.y)) {
			answer.add(new Coordinate(r.x, r.y));
		}
		if (location.isDoor(r.x + r.width - 1, r.y - 1)
				|| location.isDoor(r.x + r.width, r.y)) {
			answer.add(new Coordinate(r.x + r.width - 1, r.y));
		}
		if (location.isDoor(r.x + r.width, r.y + r.height - 1)
				|| location.isDoor(r.x + r.width - 1, r.y + r.height)) {
			answer.add(new Coordinate(r.x + r.width - 1, r.y + r.height - 1));
		}
		if (location.isDoor(r.x, r.y + r.height)
				|| location.isDoor(r.x - 1, r.y + r.height - 1)) {
			answer.add(new Coordinate(r.x, r.y + r.height - 1));
		}
		return answer;
	}
	public void markAsHallway(int rectangleId) {
	/**
	 * Mark room as hallway so other rooms will prefer to connect to this room
	 * when buildBasis is called
	 */
		hallways.add(rectangleId);
	}
	public void clearBasisInside() {
		for (Rectangle r : rectangleSystem.rectangles.values()) {
			location.square(r.x, r.y, r.width, r.height,
					TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_VOID, true);
		}
	}
	public enum BasisBuildingSetup {
	/**
	 * Describes which methods should buildBasis use to build edges of graph
	 */
		NOT_BUILD_EDGES, CONVERT_TO_DIRECTED_TREE, KEYPOINTS_BASED
	}
}
