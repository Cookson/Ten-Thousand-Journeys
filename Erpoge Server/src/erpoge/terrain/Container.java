package erpoge.terrain;
import java.util.Hashtable;

import erpoge.inventory.ItemMap;

public class Container extends ItemMap {
	static int DEFAULT_CAPACITY = 16;
	public int capacity;
	public int x;
	public int y;
	public Container(int capacity) {
		this.capacity = capacity;
	}
	public Container() {
		this.capacity = DEFAULT_CAPACITY;
	}
}
