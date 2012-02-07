package erpoge.core.itemtypes;

import erpoge.core.characters.Character;

public class Useable extends ItemType {
	public Useable(String name, int cls, int weight, int price, int material) {
		super(name, cls, weight, price, material);
		unique = false;
	}
	public String jsonPartTypology() {
	// Get a json string that describes this item type
	// Out: [name,cls,weight,price]
		return new StringBuilder()
			.append("[\"")
			.append(name).append("\",")
			.append(cls).append(",")
			.append(weight).append(",")
			.append(price).append(",")
			.append(material).append("]")
			.toString();
	}
	@Override
	public void addBonuses(Character character) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeBonuses(Character character) {
		// TODO Auto-generated method stub
		
	}
}
