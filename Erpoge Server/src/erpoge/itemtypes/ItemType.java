package erpoge.itemtypes;

import java.util.HashMap;

import erpoge.characters.Character;
import erpoge.itemtypes.Attribute;

public abstract class ItemType extends ItemSystemMetaInfo {
	protected String name;
	protected final int cls;
	protected final int price;
	protected final int weight;
	protected final int material;
	protected int id;
	protected boolean unique;
	protected HashMap<Attribute, Integer> specialAttributes = new HashMap<Attribute, Integer>();

	public ItemType(String name, int cls, int weight, int price, int material) {
		this.name = name;
		this.cls = cls;
		this.weight = weight;
		this.price = price;
		this.material = material;
	}
	public int getSlot() {
		if (cls <= CLASS_LAST_WEAPON_CLASS_NUMBER && cls >= CLASS_SWORD) {
			return SLOT_RIGHT_HAND;
		} else if (cls == CLASS_SHIELD) {
			return SLOT_LEFT_HAND;
		} else if (cls == CLASS_BODY) {
			return SLOT_BODY;
		} else if (cls == CLASS_GLOVES) {
			return SLOT_GLOVES;
		} else if (cls == CLASS_BOOTS) {
			return SLOT_BOOTS;
		} else if (cls == CLASS_CLOAK) {
			return SLOT_CLOAK;
		} else if (cls == CLASS_RING) {
			return SLOT_RING;
		} else if (cls == CLASS_AMULET) {
			return SLOT_AMULET;
		}  else if (cls == CLASS_HEADGEAR) {
			return SLOT_HEADGEAR;
		} else {
			throw new Error("Unknown slot");
		}
	}
	public boolean isArmor() {
		return cls >= 11 && cls <= 21;
	}

	public boolean isWeapon() {
		return cls >= 0 && cls <= 10;
	}

	public static boolean isArmor(int c) {
		return c >= 11 && c <= 21;
	}
	public void addSpecialAttribute(Attribute attribute, int value) {
	/**
	 * Add additional custom attribute bonus
	 */
		specialAttributes.put(attribute, value);
	}
	public static boolean isWeapon(int c) {
		return c >= 0 && c <= 10;
	}
	public boolean isRanged() {
		return cls == CLASS_BOW;
	}
	public void setId(int iid) {
		id = iid;
	}
	public abstract String jsonPartTypology();

	public int getTypeId() {
		// TODO Auto-generated method stub
		return id;
	}

	public int getCls() {
		// TODO Auto-generated method stub
		return cls;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	public boolean isUnique() {
		return unique;
	}
	public abstract void addBonuses(Character character);
	public abstract void removeBonuses(Character character);
}
