package erpoge.core.inventory;

import erpoge.core.itemtypes.ItemSystemMetaInfo;
import erpoge.core.itemtypes.ItemType;
import erpoge.core.itemtypes.ItemsTypology;

public abstract class Item extends ItemSystemMetaInfo {
	protected ItemType type;
	
	public Item(int typeId) {
		type = ItemsTypology.item(typeId);
	}
	
	public int getTypeId() {
		return type.getTypeId();
	}
	public ItemType getType() {
		return type;
	}
	public abstract int getParam();
	public abstract String toJson();
	public abstract String toString();
}
