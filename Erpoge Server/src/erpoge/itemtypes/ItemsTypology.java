package erpoge.itemtypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import erpoge.Main;
import erpoge.inventory.UniqueItem;

public final class ItemsTypology extends ItemSystemMetaInfo {
	
	/* type: 
	0-10 - оружие
	11 - щит
	12 - шлем
	13 - доспех
	14 - перчатки
	15 - ботинки
	16 - плащ
	17 - ! зарезервировано
	18 - ! зарезервировано
	19 - кольцо
	20 - ! зарезервировано для второго кольца
	21 - амулет */
	
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
			if (types[i].getTypeId() <= 0) {
				continue;
			}
			data +=types[i].getTypeId()+":"+types[i].jsonPartTypology()+",";
		}
		if (types[i].getTypeId() > 0) { 
			data +=types[i].getTypeId()+":"+types[i].jsonPartTypology()+"}";
		}
		Main.log(data);
	}
	
	public static int getSlotFromId(int itemId) {
		return ItemsTypology.item(itemId).getSlot();
	}
	public static void init() {
		/**
		 * Предметы Формат: [ 0:name, 1:type, 2:weight, 3:price ( Для оружия:
		 * [,4:weaponDamage [,5:weaponAccuracy [,6:weaponSpeed ]]] ) || ( Для
		 * доспехов: [,4:AC [,5:EV ]] ) ] type: 0-10 - оружие 11 - щит 12 - шлем
		 * 13 - доспех 14 - перчатки 15 - ботинки 16 - плащ 17 - !
		 * зарезервировано 18 - ! зарезервировано 19 - кольцо 20 - !
		 * зарезервировано для второго кольца 21 - амулет, 22 - книга Классы
		 * оружия: 1 - меч 2 - топор 3 - дробящее 4 - древковое 5 - посох 6 -
		 * кинжал 7 - лук
		 */
		// Мечи
		put("SYSTEM_ITEM", CLASS_ABSTRACT, 0, 0, 0, 0, 0, MISSING);
		put("Короткий меч", CLASS_SWORD, 34, 100, MATERIAL_BRONZE, 6, 4, 11);
		put("Меч", CLASS_SWORD, 12, 100, MATERIAL_BRONZE, 7, 4, 13);
		put("Сабля", CLASS_SWORD, 10, 130, MATERIAL_BRONZE, 7, 4, 12);
		put("Гладиус", CLASS_SWORD, 14, 140, MATERIAL_BRONZE, 8, 4, 14);
		put("Сабля", CLASS_SWORD, 10, 130, MATERIAL_BRONZE, 7, 4, 13);

		// Топоры
		put("Топор", CLASS_AXE, 18, 100, MATERIAL_BRONZE, 7, 3, 13);
		put("Секира", CLASS_AXE, 22, 160, MATERIAL_BRONZE, 14, -2, 17);
		put("Боевой топор", CLASS_AXE, 28, 200, MATERIAL_BRONZE, 26, 13, 13);

		// Дробящее
		put("Булава", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 8, 3, 14);
		put("Моргенштерн", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 10, -1, 15);
		put("Моргенштерн", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 10, -1, 15);
		put("Моргенштерн", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 10, -1, 15);
		put("Цеп", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 9, 2, 15);
		put("Золотая булава", CLASS_BLUNT, 100, 100, MATERIAL_BRONZE, 9, 2, 15);

		// Древковое
		put("Копьё", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 6, 3, 120);
		put("Копьё", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 6, 3, 120);
		put("Алебарда", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 13, 3, 16);
		put("Коса", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 16, 6, 3);
		put("Коса", CLASS_POLEARM, 10, 100, MATERIAL_BRONZE, 16, 6, 3);

		// Посохи
		put("Посох", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("Посох", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("Посох", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("Посох", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);
		put("Посох", CLASS_STAFF, 5, 10, MATERIAL_BRONZE, 100, 7, 6);

		// Кинжалы
		put("Нож", CLASS_DAGGER, 10, 100, MATERIAL_BRONZE, 5, 3, 10);
		put("Кинжал", CLASS_DAGGER, 10, 100, MATERIAL_BRONZE, 6, 4, 10);

		// Луки
		put("Короткий лук", CLASS_BOW, 1, 100, MATERIAL_BRONZE, 7, 1, 11);

		// Щиты
		put("Круглый щит", CLASS_SHIELD, 1, 100, MATERIAL_BRONZE, 7, 1, 11);
		put("Круглый щит", CLASS_SHIELD, 1, 100, MATERIAL_BRONZE, 7, 1, 11);
		put("Круглый щит", CLASS_SHIELD, 1, 100, MATERIAL_BRONZE, 7, 1, 11);

		// Плащи
		put("Серый плащ", CLASS_CLOAK, 10, 100, ItemType.MATERIAL_BRONZE, 1, 0, MISSING);
		put("Красный плащ", CLASS_CLOAK, 10, 100, ItemType.MATERIAL_BRONZE, 1, 0, MISSING);
		put("Зелёный плащ", CLASS_CLOAK, 10, 100, ItemType.MATERIAL_BRONZE, 1, 0, MISSING);

		// Кольца
		put("Кольцо мудрости", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("Кольцо силы", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("Кольцо смерти", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("Обручальное кольцо", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);
		put("Кольцо кольца", CLASS_RING, 10, 100, MATERIAL_BRONZE, 0, 0, MISSING);

		// Броня
		// Марунарх
		put("Шлем марунарх", CLASS_HEADGEAR, 1, 100, MATERIAL_BRONZE, 2, -1, MISSING);
		put("Доспех марунарх", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 7, -5, MISSING);
		put("Перчатки марунарх", CLASS_GLOVES, 1, 100, MATERIAL_BRONZE, 2, 0, MISSING);
		put("Ботинки марунарх", CLASS_BOOTS, 1, 100, MATERIAL_BRONZE, 2, -1, MISSING);

		// Железный доспех
		put("Железный шлем", CLASS_HEADGEAR, 1, 100, MATERIAL_BRONZE, 2, -1, MISSING);
		put("Кольчуга", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 6, -4, MISSING);
		put("Железные перчатки", CLASS_GLOVES, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("Железные ботинки", CLASS_BOOTS, 1, 100, MATERIAL_BRONZE, 1, -1, MISSING);

		// Робы
		put("Льняная роба", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("Огненная роба", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 3, 0, MISSING);

		// Кожанка
		put("Кожаный шлем", CLASS_HEADGEAR, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("Кожаный доспех", CLASS_BODY, 1, 100, MATERIAL_BRONZE, 4, -2, MISSING);
		put("Кожаные перчатки", CLASS_GLOVES, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);
		put("Кожаные сапоги", CLASS_BOOTS, 1, 100, MATERIAL_BRONZE, 1, 0, MISSING);

		// Стартовая одежда
		put("Рубашка из грубой ткани", CLASS_BODY, 1, 10, MATERIAL_BRONZE, 1, -1, MISSING);
		put("Ботинки из грубой ткани", CLASS_BOOTS, 1, 10, MATERIAL_BRONZE, 1, 0, MISSING);

		// Книги
		put("Книга заклинания Огненный шар", CLASS_BOOK, 1, 1000, MATERIAL_BRONZE, 0, 0, MISSING);

		// Стрелы
		put("Стрела", CLASS_AMMO, 1, 1000, MATERIAL_BRONZE, 0, 0, MISSING);

		put("Золото", 9000, 0, 1, 0, MATERIAL_BRONZE, 0, MISSING);
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
