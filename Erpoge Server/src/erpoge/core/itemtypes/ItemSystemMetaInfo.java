package erpoge.core.itemtypes;

public class ItemSystemMetaInfo {
	public final static int 
	// Number of elements in class
		CLASS_LENGTH = 100,
	// Constant to mark missing parameters in ItemType factory (ItemTypology.put())
		MISSING = -31337,
	// Item class which determines its slot
		// Weapondamage types (to be moved to another place)
		DAMAGE_TYPE_SLICE = 0,
		DAMAGE_TYPE_PIERCE = 1,
		DAMAGE_TYPE_BLUNT = 2,
		DAMAGE_TYPE_EXPLODE = 3,
		DAMAGE_TYPE_BURN = 4,
	// Slots in EquipmentMap
		SLOT_HEAD = 0,
		SLOT_TORSO = 1,
		SLOT_HAND = 2,
		SLOT_GRIP = 3,
		SLOT_LEGS = 4,
		SLOT_WAIST = 5,
		SLOT_EAR = 6,
		SLOT_FEET = 7,
		SLOT_FINGER = 8,
		SLOT_NECK = 9,
		SLOT_TENTACLE = 10,
		SLOT_TAIL = 11,
	// ���������
		MATERIAL_WOOD = 1,
		MATERIAL_STEEL = 2,
		MATERIAL_GOLD = 3,
		MATERIAL_BRONZE = 4,
		MATERIAL_MITHRIL = 5,
		MATERIAL_CLOTH = 6,
		MATERIAL_IRON = 7;
}
