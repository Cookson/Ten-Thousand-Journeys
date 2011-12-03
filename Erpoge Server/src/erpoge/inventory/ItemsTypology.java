package erpoge.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import erpoge.Main;
import erpoge.itemtypes.Abstract;
import erpoge.itemtypes.ItemSystemMetaInfo;
import erpoge.itemtypes.ItemType;
import erpoge.itemtypes.Armor;
import erpoge.itemtypes.Useable;
import erpoge.itemtypes.Weapon;

public final class ItemsTypology extends ItemSystemMetaInfo {
	
	/* type: 
	0-10 - ������
	11 - ���
	12 - ����
	13 - ������
	14 - ��������
	15 - �������
	16 - ����
	17 - ! ���������������
	18 - ! ���������������
	19 - ������
	20 - ! ��������������� ��� ������� ������
	21 - ������ */
	
	public static final ItemType ABSTRACT_TYPE = new Abstract("Abstract Item");
	private static final HashMap<Integer, ItemType> items = new HashMap<Integer, ItemType>();

	public static int generateId(ItemType item) {
		int newId = item.getCls() * CLASS_LENGTH;
		while (items.containsKey(newId)) {
			newId++;
		}
		return newId;
	}

	public static void put(String name, int cls, int weight, int price, int material,
			int param1, int param2, int param3) {
		ItemType ittype;
		if (ItemType.isWeapon(cls)) {
			ittype = new Weapon(name, cls, weight, price, material, param1, param2,
					param3);
		} else if (ItemType.isArmor(cls)) {
			ittype = new Armor(name, cls, weight, price, material, param1, param2);
		} else {
			ittype = new Useable(name, cls, weight, price, material);
		}
		int id = generateId(ittype);
		ittype.setId(id);
		items.put(id, ittype);
	}

	public static ItemType item(int typeId) {
		/**
		 * Get item type by it's id.
		 * 
		 * @return Item item
		 */
		if (items.containsKey(typeId)) {
			return items.get(typeId);
		} else {
			throw new Error("Items typology does not have the item type " + typeId);
		}
	}

	public static void showTypology() {
		Collection<ItemType> values = items.values();
		for (ItemType type : values) {
			Main.outln(type.getTypeId()+": "+type.getName());
		}
	}
	
	public static void jsonTypology() {
	// Output the whole typology to the console as json
	// Out: {"characterId":[name,cls,weight,price[,weaponDamage[,weaponSpeed]]]xN}
		String data = "{";
		int i=0;
		
		ItemType[] types = items.values().toArray(new ItemType[0]);
		int iterations = types.length - 1;
		for (;i<iterations;i++) {
			data +=types[i].getTypeId()+":"+types[i].jsonPartTypology()+",";
		}
		data +=types[i].getTypeId()+":"+types[i].jsonPartTypology()+"}";
	}
	
	public static int getSlotFromId(int itemId) {
		return ItemsTypology.item(itemId).getSlot();
	}
	public static void init() {
		/**
		 * �������� ������: [ 0:name, 1:type, 2:weight, 3:price ( ��� ������:
		 * [,4:weaponDamage [,5:weaponAccuracy [,6:weaponSpeed ]]] ) || ( ���
		 * ��������: [,4:AC [,5:EV ]] ) ] type: 0-10 - ������ 11 - ��� 12 - ����
		 * 13 - ������ 14 - �������� 15 - ������� 16 - ���� 17 - !
		 * ��������������� 18 - ! ��������������� 19 - ������ 20 - !
		 * ��������������� ��� ������� ������ 21 - ������, 22 - ����� ������
		 * ������: 1 - ��� 2 - ����� 3 - �������� 4 - ��������� 5 - ����� 6 -
		 * ������ 7 - ���
		 */
		// ����
		put("SYSTEM_ITEM", CLASS_ABSTRACT, 0, 0, 0, 0, 0, MISSING);
		put("�������� ���", CLASS_SWORD, 34, 100, MATERIAL_BRONZE, 6, 4, 11);
		put("���", CLASS_SWORD, 12, 100, MATERIAL_BRONZE, 7, 4, 13);
		put("�����", CLASS_SWORD, 10, 130, MATERIAL_BRONZE, 7, 4, 12);
		put("�������", CLASS_SWORD, 14, 140, MATERIAL_BRONZE, 8, 4, 14);
		put("�����", CLASS_SWORD, 10, 130, MATERIAL_BRONZE, 7, 4, 13);

		// ������
		put("�����", CLASS_AXE, 18, 100, MATERIAL_BRONZE, 7, 3, 13);
		put("������", CLASS_AXE, 22, 160, MATERIAL_BRONZE, 14, -2, 17);
		put("������ �����", CLASS_AXE, 28, 200, MATERIAL_BRONZE, 26, 13, 13);

		// ��������
		put("������", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 8, 3, 14);
		put("�����������", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 10, -1, 15);
		put("�����������", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 10, -1, 15);
		put("�����������", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 10, -1, 15);
		put("���", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 9, 2, 15);
		put("������� ������", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 9, 2, 15);

		// ���������
		put("�����", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 6, 3, 120);
		put("�����", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 6, 3, 120);
		put("��������", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 13, 3, 16);
		put("����", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 16, 6, 3);
		put("����", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 16, 6, 3);

		// ������
		put("�����", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("�����", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("�����", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("�����", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("�����", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);

		// �������
		put("���", CLASS_DAGGER, 10, 100, MATERIAL_BRONZE, 5, 3, 10);
		put("������", CLASS_DAGGER, 10, 100, MATERIAL_BRONZE, 6, 4, 10);

		// ����
		put("�������� ���", CLASS_BOW, 1, 100, MATERIAL_BRONZE, 7, 1, 11);

		// ����
		put("������� ���", CLASS_SHIELD, 1, 100, MATERIAL_BRONZE, 7, 1, 11);
		put("������� ���", CLASS_SHIELD, 1, 100, MATERIAL_BRONZE, 7, 1, 11);
		put("������� ���", CLASS_SHIELD, 1, 100, MATERIAL_BRONZE, 7, 1, 11);

		// �����
		put("����� ����", CLASS_CLOAK, 10, 100, ItemType.MATERIAL_BRONZE, 1, 0, MISSING);
		put("������� ����", CLASS_CLOAK, 10, 100, ItemType.MATERIAL_BRONZE, 1, 0, MISSING);
		put("������ ����", CLASS_CLOAK, 10, 100, ItemType.MATERIAL_BRONZE, 1, 0, MISSING);

		// ������
		put("������ ��������", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("������ ����", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("������ ������", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("����������� ������", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("������ ������", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);

		// �����
		// ��������
		put("���� ��������", CLASS_HEADGEAR, 1, 100, MATERIAL_BRONZE, 2, -1, MISSING);
		put("������ ��������", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 7, -5, MISSING);
		put("�������� ��������", CLASS_GLOVES, 1, 100, MATERIAL_BRONZE, 2, 0, MISSING);
		put("������� ��������", CLASS_BOOTS, 1, 100, MATERIAL_BRONZE, 2, -1, MISSING);

		// �������� ������
		put("�������� ����", CLASS_HEADGEAR, 1, 100, MATERIAL_BRONZE, 2, -1, MISSING);
		put("��������", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 6, -4, MISSING);
		put("�������� ��������", CLASS_GLOVES, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("�������� �������", CLASS_BOOTS, 1, 100, MATERIAL_BRONZE, 1, -1, MISSING);

		// ����
		put("������� ����", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("�������� ����", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 3, 0, MISSING);

		// �������
		put("������� ����", CLASS_HEADGEAR, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("������� ������", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 4, -2, MISSING);
		put("������� ��������", CLASS_GLOVES, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("������� ������", CLASS_BOOTS, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);

		// ��������� ������
		put("������� �� ������ �����", CLASS_BODY, 1, 10, MATERIAL_BRONZE, 1, -1, MISSING);
		put("������� �� ������ �����", CLASS_BOOTS, 1, 10, MATERIAL_BRONZE, 1, 0, MISSING);

		// �����
		put("����� ���������� \"�������� ���\"", CLASS_BOOK, 1, 1000, MATERIAL_BRONZE, 0, 0, MISSING);

		// ������
		put("������", CLASS_AMMO, 1, 1000, MATERIAL_BRONZE, 0, 0, MISSING);

		put("������", 9000, 0, 1, 0, MATERIAL_BRONZE, 0, MISSING);
		items.put(-1, ABSTRACT_TYPE);
	}
	public static int getMissileType(UniqueItem weapon) {
		if (weapon.getItemId() == 700) {
			return item(2300).getTypeId();
		} else {
			throw new Error("Unknown missile type");
		}
	}
}
