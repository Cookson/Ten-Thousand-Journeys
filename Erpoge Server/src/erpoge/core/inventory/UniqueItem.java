package erpoge.core.inventory;

import erpoge.core.itemtypes.ItemsTypology;

public class UniqueItem extends Item {
	private static int lastId = 1;
	protected int itemId;
	public UniqueItem(int typeId) {
		super(typeId);
		itemId = ++lastId;
	}	
	public int getItemId() {
		return itemId;
	}
	public String toString() {
		return type.getName();
	}
	@Override
	public String toJson() {
		return "["+type.getId()+","+itemId+"]";
	}
	public static UniqueItem createItemByClass(int classId, int indexInType) {
		return new UniqueItem(classId*ItemsTypology.CLASS_LENGTH + indexInType);
	}
	@Override
	public int getParam() {
		return itemId;
	}
}
