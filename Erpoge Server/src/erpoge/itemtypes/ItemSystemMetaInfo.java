package erpoge.itemtypes;

public class ItemSystemMetaInfo {
	public final static int 
	// Number of elements in class
		CLASS_LENGTH = 100,
	// Constant to mark missing parameters in ItemType factory (ItemTypology.put())
		MISSING = -31337,
	// Item class which determines its slot
		// Weapon types
		CLASS_ABSTRACT = -1,
		CLASS_SWORD = 1,
		CLASS_AXE = 2,
		CLASS_BLUNT = 3,
		CLASS_POLEARM = 4,
		CLASS_STAFF = 5,
		CLASS_DAGGER = 6,
		CLASS_BOW = 7,
		CLASS_LAST_WEAPON_CLASS_NUMBER = 7,
		// Armor types
		CLASS_SHIELD = 10,
		CLASS_HEADGEAR = 11,
		CLASS_BODY = 12,
		CLASS_GLOVES = 13,
		CLASS_BOOTS = 14,
		CLASS_CLOAK = 15,
		CLASS_RING = 16,
		CLASS_AMULET = 17,
		CLASS_BOOK = 22,
		CLASS_AMMO = 23,
	// Slots in AmmunitionMap
		NUMBER_OF_SLOTS = 10,
		SLOT_ANY_HAND = -1,
		SLOT_RIGHT_HAND = 0,
		SLOT_LEFT_HAND = 1,
		SLOT_HEADGEAR = 2,
		SLOT_BODY = 3,
		SLOT_GLOVES = 4,
		SLOT_BOOTS = 5,
		SLOT_CLOAK = 6,
		SLOT_RING = 7,
		SLOT_AMULET = 9,
	// Материалы
		MATERIAL_WOOD = 1,
		MATERIAL_STEEL = 2,
		MATERIAL_GOLD = 3,
		MATERIAL_BRONZE = 4,
		MATERIAL_MITHRIL = 5,
		MATERIAL_CLOTH = 6,
		MATERIAL_IRON = 7;
}
