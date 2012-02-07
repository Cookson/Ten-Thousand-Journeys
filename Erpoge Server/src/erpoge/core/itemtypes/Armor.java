package erpoge.core.itemtypes;

import erpoge.core.characters.Character;

public class Armor extends ItemType {
	public final int armor;
	public final int evasion;

	public Armor(String name, int cls, int weight, int price, int material, int ac, int ev) {
		super(name, cls, weight, price, material);
		this.armor = ac;
		this.evasion = ev;
		this.unique = true;
	}
	public String jsonPartTypology() {
	// Get a json string that describes this item type
		// Out: [name,cls,weight,price, ac, ev]
		return new StringBuilder()
			.append("[\"")
			.append(name).append("\",")
			.append(cls).append(",")
			.append(weight).append(",")
			.append(price).append(",")
			.append(material).append(",")
			.append(armor).append(",")
			.append(evasion).append("]")
			.toString();
	}
	@Override
	public void addBonuses(Character character) {
		character.changeAttribute(Attribute.ARMOR, armor);
		character.changeAttribute(Attribute.EVASION, evasion);
	}
	@Override
	public void removeBonuses(Character character) {
		character.changeAttribute(Attribute.ARMOR, -armor);
		character.changeAttribute(Attribute.EVASION, -evasion);
	}
}
