package erpoge.core.terrain;
import java.util.HashSet;

import erpoge.core.Character;
import erpoge.core.Main;
import erpoge.core.inventory.Item;
import erpoge.core.inventory.ItemMap;
import erpoge.core.inventory.ItemPile;
import erpoge.core.inventory.UniqueItem;
import erpoge.core.objects.GameObjects;

public class Cell {
	protected int floor = GameObjects.FLOOR_VOID;
	protected int object = GameObjects.OBJ_VOID;
	private int passability = TerrainBasics.PASSABILITY_FREE;
	private Character character = null;
	public final ItemMap items = new ItemMap();

	public Cell() {
		
	}
	public Cell(int f, int o, Character ch) {
		this();
		floor = f;
		object = o;
		character = ch;
	}

	public Cell(int f, int o) {
		this();
		floor = f;
		object = o;
	}

	public Cell(Cell cell) {
		floor = cell.floor;
		object = cell.object;
		character = cell.character;
	}

	protected void addItem(UniqueItem item) {
		items.add(item);
	}
	protected void addItem(ItemPile item) {
		items.add(item.getType().getId(), item.getAmount());
	}
	public int getElement(int type) {
		if (type == TerrainBasics.ELEMENT_FLOOR) {
			return floor;
		} else if (type == TerrainBasics.ELEMENT_OBJECT) {
			return object;
		} else if (type == TerrainBasics.ELEMENT_FOREST) {
			return object;
		} else {
			throw new Error("Unknown type "+type);
		}
	}
	protected void addItem(int itemId, int itemNum) {
		items.add(itemId, itemNum);
	}
	
	public void removeItem(UniqueItem item) {
		items.removeUnique(item);
	}
	
	public void removeItem(ItemPile pile) {
		items.removePile(pile);
	}
	public Character character() {
		return character;
	}
	public boolean hasCharacter() {
		return character != null;
	}
	public void character(boolean f) {
		character = null;
	}
	public void character(Character ch) {
		character = ch;
	}
	public int floor() {
		return floor;
	}
	public void floor(int value) {
		floor = value;
	}
	public int object() {
		return object;
	}
	public void object(int value) {
		object = value;
	}
	public boolean isDoor() {
		return object > 40 && object < 51;
	}
	public void show() {
		if (object != GameObjects.OBJ_VOID) {
			if (isDoor()) {
				Main.out("+");
			} else {
				Main.out("8");
			}
		} else if (character != null) {
			Main.out("!");
		} else if (floor == GameObjects.FLOOR_WATER) {
			Main.out("~");
		} else {
			Main.out(".");
		}
	}
	public void setPassability(int passability) {
		this.passability = passability;
	}
	public int getPassability() {
		return passability;
	}
	
}
