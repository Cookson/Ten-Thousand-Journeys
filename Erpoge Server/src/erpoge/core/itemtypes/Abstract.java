package erpoge.core.itemtypes;

import erpoge.core.characters.Character;


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
	@Override
	public void addBonuses(Character character) {
		
	}
	@Override
	public void removeBonuses(Character character) {
		// TODO Auto-generated method stub
		
	}
}
