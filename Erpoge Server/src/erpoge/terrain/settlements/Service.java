package erpoge.terrain.settlements;

import java.util.HashMap;

import erpoge.characters.Character;

public class Service {
	public int id;
	public String name;
	public int type;
	public Character dweller;
	public HashMap<Integer, Integer> items = new HashMap<Integer, Integer>();

	public Service(Character dweller, int type, String name) {
		// dweller - ������ ������
		// type - ��� ������, ������ ���������
		// name - �������� ������, ������������ � �������
		this.dweller = dweller;
		this.type = type;
		this.name = name;
	}
	
	public void addItem(int itemId, int amount) {
		// �������� �������. ���� ����� ������� ��� ���� � ������ - ���������
		// ���������� ����� ���������.
		// itemId - �������
		// itemNum - ���������� ���������
		if (items.containsKey(itemId)) {
			items.put(itemId, items.get(itemId) + amount);
		} else {
			items.put(itemId, amount);
		}
	}
}
