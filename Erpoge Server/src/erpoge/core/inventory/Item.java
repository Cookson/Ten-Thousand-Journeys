package erpoge.core.inventory;

import erpoge.core.StaticData;
import erpoge.core.itemtypes.ItemSystemMetaInfo;
import erpoge.core.itemtypes.ItemType;

public abstract class Item extends ItemSystemMetaInfo {
	protected ItemType type;
	
	public Item(int typeId) {
		type = StaticData.getItemType(typeId);
	}
	
	public ItemType getType() {
		return type;
	}
	public abstract int getParam();
	public abstract String toJson();
	public abstract String toString();
}
