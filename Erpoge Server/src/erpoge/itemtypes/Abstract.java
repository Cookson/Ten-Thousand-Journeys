package erpoge.itemtypes;

import erpoge.inventory.ItemsTypology;

public class Abstract extends ItemType {
	public Abstract(String n) {
		super(n, ItemType.CLASS_ABSTRACT, 0, 0, ItemsTypology.MISSING);
	}

	public String jsonPartTypology() {
		// Get a json string that describes this item type
		// This method of this subclass is not used (need it here because
		// there's an abstract method in ItemType
		return "";
	}
}
