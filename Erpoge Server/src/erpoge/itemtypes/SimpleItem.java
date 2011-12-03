package erpoge.itemtypes;

import erpoge.Main;
import erpoge.inventory.ItemsTypology;

public class SimpleItem {
	public int id = -1;
	public int amount = 1;
	public SimpleItem() {
		/**
		 * Constructor for Gson.fromJson();
		 */
	}
	public SimpleItem(int i, int a) {
		id = i;
		amount = a;
	}
	public void describe() {
		Main.outln(id+" "+amount);
		Main.outln(amount+" of "+ItemsTypology.item(id).name);
	}
}
