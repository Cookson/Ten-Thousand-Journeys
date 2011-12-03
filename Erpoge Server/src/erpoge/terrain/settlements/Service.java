package erpoge.terrain.settlements;

import java.util.HashMap;

import erpoge.terrain.locationtypes.Settlement;
import erpoge.characters.Character;

public class Service {
	public int id;
	public String name;
	public int type;
	public Character dweller;
	public HashMap<Integer, Integer> items = new HashMap<Integer, Integer>();

	public Service(Character dweller, int type, String name) {
		// dweller - объект жителя
		// type - тип услуги, строка латиницей
		// name - название услуги, отображаемой в клиенте
		this.dweller = dweller;
		this.type = type;
		this.name = name;
	}
	
	public void addItem(int itemId, int amount) {
		// Добавить предмет. Если такой предмет уже есть в услуге - увеличить
		// количество таких предметов.
		// itemId - предмет
		// itemNum - количество предметов
		if (items.containsKey(itemId)) {
			items.put(itemId, items.get(itemId) + amount);
		} else {
			items.put(itemId, amount);
		}
	}
}
