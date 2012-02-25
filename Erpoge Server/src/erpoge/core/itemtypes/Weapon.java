package erpoge.core.itemtypes;

import erpoge.core.Character;
import erpoge.core.characters.CustomCharacterAction;

public class Weapon extends ItemType {
	private final int damage;
	private final int accuracy;
	private final int speed;

	public Weapon(String n, int c, int w, int pr, int material, int damage, int accuracy, int speed) {
		super(n, c, w, pr, material);
		this.damage = damage;
		this.accuracy = accuracy;
		this.speed = speed;
		this.unique = true;
	}
	public String jsonPartTypology() {
	// Get a json string that describes this item type
	// Out: [name,cls,weight,price, damage, accuracy, speed]
		return new StringBuilder()
			.append("[\"")
			.append(name).append("\", ")
			.append(cls).append(",")
			.append(weight).append(",")
			.append(price).append(",")
			.append(material).append(",")
			.append(damage).append(",")
			.append(accuracy).append(",")
			.append(speed).append("]")
			.toString();
	}
	public int getDamage() {
		return damage;
	}
	public int getAccuracy() {
		return accuracy;
	}
	public int getSpeed() {
		return speed;
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
