package erpoge.terrain;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.characters.Character;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.graphs.RectangleSystem;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.objects.GameObjects;

public abstract class TerrainBasics {
	public static final int 
		MAX_CHARACTERS_QUANTITY = 128,
		
		ELEMENT_FLOOR = 0,
		ELEMENT_OBJECT = 1,
		ELEMENT_GROUND = 2,
		ELEMENT_FOREST = 3,
		ELEMENT_ROAD = 4,
		ELEMENT_RIVER = 5,
		ELEMENT_RACE = 6,
		ELEMENT_CHARACTER = 7,
		ELEMENT_REMOVE = 8,
		ELEMENT_OBJECTS = 9,
		
		PASSABILITY_FREE = 0,
		PASSABILITY_SEE = 3,
		PASSABILITY_NO = 1,
		
		TO_LOCATION = 1,
		TO_WORLD = 2;

	public int width;
	public int height;
	public HashMap<Integer, Character> characters = new HashMap<Integer, Character>();
	public HashMap<Integer, Container> containers = new HashMap<Integer, Container>();
	public ArrayList<Ceiling> ceilings;
	public Cell[][] cells;
	public int[][] passability;
	public World world;
	public Rectangle startArea = new Rectangle(0, 0, 0, 0);
	public HashSet<Portal> portals = new HashSet<Portal>();
	public boolean isPeaceful = false;
	
	public TerrainBasics(int width, int height) {
		this.width = width;
		this.height = height;
		cells = new Cell[width][height];
		passability = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell();
				passability[i][j] = 0;
			}
		}

	}

	protected int getX(int num) {
		return num % width;
	}

	protected int getY(int num) {
		return (num - num % width) / width;
	}

	protected int getNum(int x, int y) {
		return y * width + x;
	}
	public void showLocation() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				showCell(j, i);
			}
			Main.outln();
		}
	}
	public void showCell(int x, int y) {
		Cell c = cells[x][y];
		if (c.object() != GameObjects.OBJ_VOID) {
			if (isDoor(x, y)) {
				Main.out("+");
			} else {
				Main.out("8");
			}
		} else if (c.character() != null) {
			Main.out("!");
		} else if (c.floor == GameObjects.FLOOR_WATER) {
			Main.out("~");
		} else {
			Main.out(".");
		}
	}
	public void setStartArea(int x, int y, int width, int height) {
		startArea.setBounds(x, y, width, height);
	}
	public void setStartArea(Rectangle r) {
		// TODO Auto-generated method stub
		startArea.x = r.x;
		startArea.y = r.y;
		startArea.width = r.width;
		startArea.height = r.height;
	}
	public void setFloor(int x, int y, int type) {
		cells[x][y].floor(type);
		computePassability(x, y);
	}
	public void setObject(int x, int y, int type) {
		cells[x][y].object(type);
		if (isContainer(type)) {
			createContainer(x, y);
		}
		computePassability(x, y);
	}
	public void setObject(Coordinate c, int type) {
		setObject(c.x, c.y, type);
	}
	
	protected boolean isContainer(int id) {
		return id >= 60 && id <= 63;
	}
	protected abstract Character createCharacter(String type, String name,
			int x, int y);
	protected void createContainer(int x, int y, int capacity) {
		containers.put(getNum(x, y), new Container(capacity));
	}
	public void createCeiling(Rectangle ceiling, int type) {
		ceilings.add(new Ceiling(ceiling, type));
	}
	public Container createContainer(int x, int y) {
		Container container = new Container();
		containers.put(getNum(x, y), container);
		return container;
	}
	public Container getContainer(int x, int y) {
		int num = getNum(x, y);
		if (containers.containsKey(num)) {
			return containers.get(num);
		} else {
			throw new Error("Контейнера на " + x + ":" + y + "не существует");
		}
	}
	public void addItem(UniqueItem item, int x, int y) {
		cells[x][y].addItem(item);
	}
	public void addItem(ItemPile pile, int x, int y) {
		cells[x][y].addItem(pile);
	}
	public void removeItem(UniqueItem item, int x, int y) {
		cells[x][y].removeItem(item);
	}
	public void removeItem(ItemPile pile, int x, int y) {
		cells[x][y].removeItem(pile);
	}
	public void setElement(int x, int y, int type, int val) {
		switch (type) {
			case ELEMENT_FLOOR :
				setFloor(x, y, val);
				break;
			case ELEMENT_OBJECT :
				setObject(x, y, val);
				break;
			case ELEMENT_REMOVE :
				setObject(x, y, GameObjects.OBJ_VOID);
				break;
			case ELEMENT_FOREST :
				setObject(x, y, val);
				break;
			case ELEMENT_ROAD :
				setRoad(x, y, val);
				break;
			default :
				throw new Error("Unknown type "
						+ type);
		}
	}
	private void setRoad(int x, int y, int val) {
		((WorldCell) cells[x][y]).setRoad(val);
	}
	public void setElement(Character ch) {
		characters.put(ch.characterId, ch);
	}
	public int getElement(int x, int y, int type) {
		switch (type) {
			case ELEMENT_FLOOR :
				return cells[x][y].floor();
			case ELEMENT_OBJECT :
				return cells[x][y].object();
			default :
				throw new Error("Not registered type " + type);
		}
	}
	public Coordinate[] vector(int startX, int startY, int endX, int endY) {
		int l = Math.round(Math.max(Math.abs(endX - startX),
				Math.abs(endY - startY)));
		float x[] = new float[l + 2];
		float y[] = new float[l + 2];
		Coordinate result[] = new Coordinate[l + 1];

		x[0] = startX;
		y[0] = startY;

		if (startX == endX && startY == endY) {
			result = new Coordinate[1];
			result[0] = new Coordinate(startX, startY);
			return result;
		}
		float dx = (endX - startX) / (float) l;
		float dy = (endY - startY) / (float) l;
		for (int i = 1; i <= l; i++) {
			x[i] = x[i - 1] + dx;
			y[i] = y[i - 1] + dy;
		}
		x[l + 1] = endX;
		y[l + 1] = endY;

		for (int i = 0; i <= l; i++) {
			result[i] = new Coordinate(Math.round(x[i]), Math.round(y[i]));
		}
		return result;
	}
	public void line(int startX, int startY, int endX, int endY, int type,
			int val, int chance) {
		if (startX == endX && startY == endY) {
			int x = startX;
			int y = startY;
			setElement(x, y, type, val);
			return;
		}
		/*
		 * if (!isset(type)) { type=1; } if (!isset(name)) { name=1; }
		 */
		Coordinate[] cells = vector(startX, startY, endX, endY);
		int size = cells.length;
		Chance cellChance = new Chance(chance);
		for (int i = 0; i < size - 1; i++) {
			int x = cells[i].x;
			int y = cells[i].y;
			int x2 = cells[i + 1].x;
			int y2 = cells[i + 1].y;
			if (chance != 100 && !cellChance.roll()) {
				continue;
			}

			setElement(x, y, type, val);
			if (i < cells.length - 1 && x != x2 && y != y2) {
				// ������
				setElement(x + ((x2 > x) ? 1 : -1), y, type, val);
			}
			if (i == size - 2) {
				setElement(x2, y2, type, val);
			}
		}
	}
	public void line(int startX, int startY, int endX, int endY, int type,
			int val) {
		line(startX, startY, endX, endY, type, val, 100);
	}
	public void square(int startX, int startY, int w, int h, int type, int name) {
		square(startX, startY, w, h, type, name, false);
	}
	public void square(Rectangle r, int type, int name, boolean fill) {
		square(r.x, r.y, r.width, r.height, type, name, fill);
	}
	public void square(int startX, int startY, int w, int h, int type,
			int name, boolean fill) {
		// ������ ������ � ������� ����� ����� � start ������� � width � �������
		// � height
		// �� ������ ���� type [0:���|1:�����|2:������|3:�������������] ������
		// name
		// ���� fill==true, ��������� ��������� ������� ��������
		if (startX + w > width || startY + h > height) {
			// ���� ������� ������� �� ������� ����� �� ����������� ��� ��
			// ���������
			/* */// ����� �������� ���, � ��������� ��� ��������, ����� �� ����
					// ����� ����������!
			throw new Error("Square " + startX + "," + startY + "," + width
					+ "," + height + " goes out of location borders");
		}
		// ��������� ����
		if (w == 1) {
			line(startX, startY, startX, startY + h - 1, type, name);
		} else if (h == 1) {
			line(startX, startY, startX + w - 1, startY, type, name);
		} else {
			line(startX, startY, startX + w - 2, startY, type, name);
			line(startX, startY, startX, startY + h - 2, type, name);
			line(startX + w - 1, startY, startX + w - 1, startY + h - 1, type,
					name);
			line(startX, startY + h - 1, startX + w - 2, startY + h - 1, type,
					name);
			if (fill) {
				// ���� ���� - ��������� ���������� �������
				for (int i = 1; i < h - 1; i++) {
					line(startX + 1, startY + i, startX + w - 1, startY + i,
							type, name);
				}
			}
		}
	}
	public float distance(int startX, int startY, int endX, int endY) {
		return (float) Math.sqrt(Math.pow(startX - endX, 2)
				+ Math.pow(startY - endY, 2));
	}
	public ArrayList<Coordinate> getCircle(int cx, int cy, int r) {
		// ������ ���� � ������� � cX;cY ������� r �� �������� type / name
		// ���� fill=true, ��������� ���������� �������
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		int d = -r / 2;
		int xCoord = 0;
		int yCoord = r;
		Hashtable<Integer, Integer> x = new Hashtable<Integer, Integer>();
		Hashtable<Integer, Integer> y = new Hashtable<Integer, Integer>();
		x.put(0, 0);
		y.put(0, r);
		do {
			// �������� ���� (�������� ����������)
			if (d < 0) {
				xCoord += 1;
				d += xCoord;
			} else {
				yCoord -= 1;
				d -= yCoord;
			}
			x.put(x.size(), xCoord);
			y.put(y.size(), yCoord);
		} while (yCoord > 0);
		int size = x.size();
		for (int i = 0; i < size; i++) {
			answer.add(new Coordinate(cx + x.get(i), cy + y.get(i)));
			answer.add(new Coordinate(cx - x.get(i), cy + y.get(i)));
			answer.add(new Coordinate(cx + x.get(i), cy - y.get(i)));
			answer.add(new Coordinate(cx - x.get(i), cy - y.get(i)));
		}
		return answer;
	}
	public void circle(int cX, int cY, int r, int type, int name) {
		circle(cX, cY, r, type, name, false);
	}
	public void circle(int cX, int cY, int r, int type, int name, boolean fill) {
		// ������ ���� � ������� � cX;cY ������� r �� �������� type / name
		// ���� fill=true, ��������� ���������� �������
		int d = -r / 2;
		int xCoord = 0;
		int yCoord = r;
		Hashtable<Integer, Integer> x = new Hashtable<Integer, Integer>();
		Hashtable<Integer, Integer> y = new Hashtable<Integer, Integer>();
		x.put(0, 0);
		y.put(0, r);
		do {
			// �������� ���� (�������� ����������)
			if (d < 0) {
				xCoord += 1;
				d += xCoord;
			} else {
				yCoord -= 1;
				d -= yCoord;
			}
			x.put(x.size(), xCoord);
			y.put(y.size(), yCoord);
		} while (yCoord > 0);
		int size = x.size();
		for (int i = 0; i < size; i++) {
			setElement(cX + x.get(i), cY + y.get(i), type, name);
			setElement(cX - x.get(i), cY + y.get(i), type, name);
			setElement(cX + x.get(i), cY - y.get(i), type, name);
			setElement(cX - x.get(i), cY - y.get(i), type, name);
		}
	}
	public RectangleSystem getGraph(int startX, int startY, int width,
			int height, int minRectangleWidth, int borderWidth) {
		return new RectangleSystem(this, startX, startY, width, height,
				minRectangleWidth, borderWidth);
	}
	public RectangleSystem getGraph(CustomRectangleSystem crs) {
		return new RectangleSystem(crs);
	}
	public CellCollection getCellCollection(ArrayList<Coordinate> cls) {
		return new CellCollection(cls, this);
	}
	public void removeObject(int x, int y) {
		cells[x][y].object(GameObjects.OBJ_VOID);
		computePassability(x, y);
	}
	public boolean isDoor(int x, int y) {
		return cells[x][y].object() > 40 && cells[x][y].object() < 51;
	}
	public void computePassability(int x, int y) {
		Cell c = cells[x][y];
		if (c.floor() == GameObjects.FLOOR_WATER) {
			passability[x][y] = 1;
		} else if (c.object() != GameObjects.OBJ_VOID) {
			passability[x][y] = GameObjects.getPassability(c.object());
		} else if (c.character() != null) {
			passability[x][y] = 1;
		} else {
			passability[x][y] = 0;
		}
	}
	public void showPassability() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Main.out(passability[j][i]);
			}
			Main.outln();
		}
	}
}
