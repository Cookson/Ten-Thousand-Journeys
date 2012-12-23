package erpoge.core;

public class Cell {
	protected int floor = StaticData.VOID;
	protected int object = StaticData.VOID;
	private int passability = TerrainBasics.PASSABILITY_FREE;
	private Character character = null;

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
		if (object != StaticData.VOID) {
			if (isDoor()) {
				Main.out("+");
			} else {
				Main.out("8");
			}
		} else if (character != null) {
			Main.out("!");
		} else if (floor == StaticData.getFloorType("water").getId()) {
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
