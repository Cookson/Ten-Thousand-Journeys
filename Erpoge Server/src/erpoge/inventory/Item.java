package erpoge.inventory;

import erpoge.itemtypes.ItemSystemMetaInfo;
import erpoge.itemtypes.ItemType;
import erpoge.itemtypes.ItemsTypology;

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
	public abstract String toJson();
	public abstract String toString();
}
