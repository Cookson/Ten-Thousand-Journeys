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
				// ќпределить, в каком квартале находитс€ это здание
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
	// function buildingTavern() { // “аверна
	// graph=buildBasis(x,y,w,h,4,5,true);
	// graph.findOuterSides();
	// // foreach (graph.rectangles as r) {
	// // location.placeSeveralObjects(array(73,74),Chance.Chance.rand(1,5),r);
	// // }
	// lobbyNum=lobby;
	// location.placeSeveralObjects(array(73,74),Chance.Chance.rand(1,5),rectangleSystem.rectangles[lobbyNum]);
	// return graph;
	// }

	public Coordinate placeFrontDoor(int side) {
		// –азместить входную дверь и определить, кака€ их комнат €вл€етс€
		// входной (Building::lobby)
		// ¬ходна€ дверь размещаетс€ на случайной клетке с указанной стороны
		// дома.
		// ƒверь размещаетс€ естественно: так, чтобы внутри дома сразу же за
		// дверью не было стены.
		// in: side - сторона. ≈сли не указана - сторона определитс€ на основе
		// информации о Settlement здани€ и квартале, в котором оно находитс€.
		// ≈сли Settlement нет - просто будет выбрана случайно
		// out: [x,y] - координаты двери
		if (side == -1) {
			if (hasSettlement() && quarter != null) {
				// ≈сли известно, в каком квартале находитс€ здание
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
				// »наче выбрать случайную сторону
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
	public boolean hasSettlement() {
		return hasSettlement;
	}
	public HashMap<Integer, Integer> findDoorAppropriateCells(int side) {
		/*
		 * Ќаходит в произвольной системе пр€моугольников rooms клетки со
		 * стороны side, на которых можно разместить двери.
		 */
		// outerSides=array();
		// size=sizeof(rooms);
		// for (i=0;i<size;i++) {
		// for (j=i+1;j<size;j++) {

		// }
		
		// }
		// —начала получаем стороны, на которых располагаютс€ искомые клетки
		// ≈сли мы ищем клетки с горизонтальных сторон (1 или 3),
		// то в sides индекс - x-координата, значение - y-координата, иначе
		// наоборот.
		HashMap<Integer, Integer> cells = new HashMap<Integer, Integer>();
		Set<Integer> keys;
		if (side == SIDE_N) {
			// ¬ерхн€€ сторона
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
			// ѕрава€ сторона
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

			// foreach (rooms as r) {
			// x=r[0]+r[2];
			// for (i=r[1];i<r[1]+r[3];i++) {
			// if (!isset(cells[i]) || cells[i]<x) {
			// cells[i]=x;
			// }
			// }
			// }
			// foreach (cells as y=>x) {
			// if (location.contents[x+1][y]['object'] ||
			// location.contents[x-1][y]['object']) {
			// unset(cells[y]);
			// }
			// }
		} else if (side == SIDE_S) {
			// Ќижн€€ сторона
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

			// foreach (rooms as r) {
			// y=r[1]+r[3];
			// for (i=r[0];i<r[0]+r[2];i++) {
			// if (!isset(cells[i]) || cells[i]<y) {
			// cells[i]=y;
			// }
			// }
			// }
			// foreach (cells as x=>y) {
			// if (location.contents[x][y+1]['object'] ||
			// location.contents[x][y-1]['object']) {
			// unset(cells[x]);
			// }
			// }
		} else if (side == SIDE_W) {
			// Ћева€ сторона
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

			// foreach (rooms as r) {
			// x=r[0]-1;
			// for (i=r[1];i<r[1]+r[3];i++) {
			// if (!isset(cells[i]) || cells[i]>x) {
			// cells[i]=x;
			// }
			// }
			// }
			// foreach (cells as y=>x) {
			// if (location.contents[x+1][y]['object'] ||
			// location.contents[x-1][y]['object']) {
			// unset(cells[y]);
			// }
			// }
		}
		return cells;
	}
	public RectangleSystem buildBasis(int wallType /* =4 */,
			boolean notSimpleForm/* =false */) {
		// –исует основу дома - вписанные в квадрат (x,y,w,h) комнаты,
		// соединЄнные в виде дерева без циклов
		// in: notSimpleForm - если true, то некоторые крайние пр€моугольники
		// системы дома будут исключены, что придаст дому более сложную форму
		// out: RectangleSystem - комнаты дома.
		// ѕосле вызова этой функции из Building::rooms можно достать
		// RectangleSystem::rectangles комнаты
		// x++; // ”меньшаем квадрат, так как комнаты будут обведены квадратами
		// // шириной в одну клетку
		// y++;
		// width -= 2;
		// height -= 2;
		RectangleSystem graph = rectangleSystem;
		if (notSimpleForm) {
			if (graph.rectangles.size() > 3) {
				// ≈сли дом состоит из более чем одного пр€моугольника, то
				// изменить его форму
				// graph.initialFindOuterSides();
				boolean formChanged = false;
				Set<Integer> keys = graph.outerSides.keySet();
				for (int k : keys) {
					ArrayList<Integer> sides = graph.outerSides.get(k);
					if (sides.size() == 0 || Chance.roll(70)) {
						continue;
					}
					if (!graph.isVertexExclusible(k)) {
						continue;
					} else {
						formChanged = true;
						graph.excludeRectangle(k);
					}
				}
				if (!formChanged) {
					graph.excludeRectangle(0);
				}
			}
		}
		rectangleSystem = graph;
		graph.convertGraphToDirectedTree();
		graph.drawBorders(1, wallType, false); // —тены дома
		int floorType = GameObjects.FLOOR_STONE;
		for (Rectangle r : graph.rectangles.values()) {
			// ѕол
			fillFloor(r, floorType);
		}

		Set<Integer> keys = graph.edges.keySet();
		for (int k : keys) {
			// —оедин€ем комнаты дверьми
			ArrayList<Integer> edge = graph.edges.get(k);
			Rectangle r1 = graph.rectangles.get(k);
			for (int vertex : edge) {
				Rectangle r2 = graph.rectangles.get(vertex);
				// —оедин€ем только там, где есть свободное место с обеих сторон
				// от потенциального местоположени€ двери
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
			y = Math.max(r1.y - 1, r2.y - 1); // ƒа, там x берЄтс€ max, а здесь
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

	public void clearBasisInside() {
		for (Rectangle r : rectangleSystem.rectangles.values()) {
			location.square(r.x, r.y, r.width, r.height,
					TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_VOID, true);
		}
	}
}
