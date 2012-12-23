package erpoge.core;
public enum AspectName {
	RANGED_WEAPON(1),
	CONTAINER(2),
	FOOD(3),
	APPAREL(4);
	private final int id;
	AspectName(int id) {
		this.id = id;
	}
	public static AspectName string2AspectName(String name) {
		if (name.equals("rangedWeapon")) {
			return RANGED_WEAPON;
		} else if (name.equals("container")) {
			return CONTAINER;
		} else if (name.equals("food")) {
			return FOOD;
		} else if (name.equals("apparel")) {
			return APPAREL;
		} else {
			throw new Error("Wrong aspect name "+name);
		}
	}
	public int getId() {
		return id;
	}
}
