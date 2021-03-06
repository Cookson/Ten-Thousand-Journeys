package erpoge.core;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import erpoge.core.meta.Coordinate;
import erpoge.core.terrain.Container;

public abstract class TerrainBasics {
	public static final int 
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
		PASSABILITY_NO = 1;
	public final int x;
	public final int y;
	public HashMap<Integer, Character> characters = new HashMap<Integer, Character>();
	public HashMap<Integer, Container> containers = new HashMap<Integer, Container>();
	public ArrayList<Ceiling> ceilings;
	Cell[][] cells;
	protected HashMap<Integer, ItemCollection> items = new HashMap<Integer, ItemCollection>();
	public TerrainBasics(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public void setFloor(int x, int y, int type) {
		try {
			cells[x][y].floor(type);
		} catch(NullPointerException e) {
			throw new Error();
		}
		computePassability(x, y);
	}
	public void setObject(int x, int y, int type) {
		cells[x][y].object(type);
//		if (isContainer(type)) {
//			createContainer(x, y);
//		}
		computePassability(x, y);
	}
	public void setObject(Coordinate c, int type) {
		setObject(c.x, c.y, type);
	}
	public void addItem(UniqueItem item, int x, int y) {
		items.get(x*getWidth()+y).add(item);
	}
	public void addItem(ItemPile item, int x, int y) {
		items.get(x*getWidth()+y).add(item);
	}
	
	public boolean isContainer(int id) {
		throw new NotImplementedException();
	}
	public void createCeiling(Rectangle ceiling, int type) {
		ceilings.add(new Ceiling(ceiling, type));
	}
	public Container createContainer(int x, int y, AspectContainer aspect) {
		Container container = new Container(aspect);
		containers.put(x*getWidth()+y, container);
		return container;
	}
	public Container getContainer(int x, int y) {
		return containers.get(x*getWidth()+y);
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
				setObject(x, y, StaticData.VOID);
				break;
			default :
				throw new Error("Unknown type "+type);
		}
	}
	public void setElement(Character ch) {
		characters.put(ch.getId(), ch);
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
	public ItemCollection getItems(int x, int y) {
		return items.get(x*getWidth()+y);
	}
	
	public float distance(int startX, int startY, int endX, int endY) {
		return (float) Math.sqrt(Math.pow(startX - endX, 2)
				+ Math.pow(startY - endY, 2));
	}
	public void removeItem(UniqueItem item, int x, int y) {
		items.get(x*getWidth()+y).removeUnique(item);
	}
	public void removeItem(ItemPile item, int x, int y) {
		items.get(x*getWidth()+y).removePile(item);
	}
	public void removeObject(int x, int y) {
		cells[x][y].object(StaticData.VOID);
		computePassability(x, y);
	}
	public void computePassability(int x, int y) {
		Cell cell = cells[x][y];
		if (cell.floor() == StaticData.getFloorType("water").getId()) {
			cell.setPassability(1);
		} else if (cell.object() != StaticData.VOID) {
			cell.setPassability(StaticData.getObjectType(cell.object()).getPassability());
		} else if (cell.character() != null) {
			cell.setPassability(PASSABILITY_SEE);
		} else {
			cell.setPassability(PASSABILITY_FREE);
		}
	}
	public boolean isDoor(int x, int y) {
		return StaticData.getObjectType(this.cells[x][y].object).getObjectClass() == ObjectType.CLASS_DOOR;
	}
	public abstract int getWidth();
	public abstract int getHeight();

	public static Coordinate[] vector(int startX, int startY, int endX, int endY) {
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
}
