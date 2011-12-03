package erpoge.itemtypes;

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
		return "[\""+name+"\", "+cls+", "+weight+","+price+","+material+","+damage+","+accuracy+","+speed+"]";
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
}
