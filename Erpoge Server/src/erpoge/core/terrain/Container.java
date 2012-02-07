package erpoge.core.terrain;
import java.util.Hashtable;

import erpoge.core.inventory.ItemMap;

public class Container extends ItemMap {
	static int DEFAULT_CAPACITY = 16;
	public static int lastContainerId = 1;
	public int capacity;
	public int x;
	public int y;
	public int id;
	public Container(int capacity) {
		this.capacity = capacity;
		this.id = ++lastContainerId;
	}
	public Container() {
		this.capacity = DEFAULT_CAPACITY;
		this.id = ++lastContainerId;
	}
}
