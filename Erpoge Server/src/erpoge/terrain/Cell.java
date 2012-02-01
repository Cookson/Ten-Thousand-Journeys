package erpoge.terrain;
import java.util.HashSet;

import erpoge.CellEvent;
import erpoge.Main;
import erpoge.inventory.Item;
import erpoge.inventory.ItemMap;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.objects.GameObjects;
import erpoge.characters.Character;

public class Cell {
	protected int floor = GameObjects.FLOOR_VOID;
	protected int object = GameObjects.OBJ_VOID;
	private int passability = TerrainBasics.PASSABILITY_FREE;
	private Character character = null;
	private HashSet<CellEvent> events;
	public final ItemMap items = new ItemMap();

	public Cell() {
		events = new HashSet<CellEvent>();
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
		events = new HashSet<CellEvent>(cell.events);
		floor = cell.floor;
		object = cell.object;
		character = cell.character;
	}

	protected void addItem(UniqueItem item) {
		items.add(item);
	}
	protected void addItem(ItemPile item) {
		items.add(item.getType().getTypeId(), item.getAmount());
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
