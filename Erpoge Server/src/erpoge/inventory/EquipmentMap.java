package erpoge.inventory;
import java.util.ArrayList;
import java.util.Arrays;

import erpoge.Utils;
import erpoge.Main;
import erpoge.itemtypes.ItemsTypology;

public class EquipmentMap extends ItemMap {
	private static final UniqueItem FREE_PIECE = new UniqueItem(-1);
	
	private UniqueItem[] equipment = new UniqueItem[NUMBER_OF_SLOTS];

	public EquipmentMap() {
		super();
		for (int i = 0; i < NUMBER_OF_SLOTS; i++) {
			equipment[i] = FREE_PIECE;
		}
	}

	public void add(UniqueItem item) {
		super.add(item);
		equipment[item.getType().getSlot()] = item;
	}
	
	public void remove(UniqueItem item) {
		equipment[ItemsTypology.getSlotFromId(item.getType().getTypeId())] = FREE_PIECE;
		super.removeUnique(item);
	}
	
	public void removeSlot(int slot) {
		// TODO Auto-generated method stub
		super.removeUnique(equipment[slot]);
		equipment[slot] = FREE_PIECE;
	}

	public boolean hasPiece(int slot) {
		return equipment[slot] != FREE_PIECE;
	}

	public UniqueItem getItemInSlot(int slot) {
		return equipment[slot];
	}
	public int[] getIntArray() {
		int [] answer = new int[22];
		int i = 0;
		for (Item item : equipment) {
			answer[i] = equipment[i].getItemId();
		}
		return answer;
	}
	public String jsonGetEquipment() {
		String answer ="{";
		boolean atLeastOneItem = false;
		for (int i=0; i<NUMBER_OF_SLOTS; i++) {
			if (equipment[i] == FREE_PIECE) {
				continue;
			}
			answer += (atLeastOneItem ? "," : "")+"\""+i+"\":"+equipment[i].toJson();
			atLeastOneItem = true;
		}
		return answer+"}";
	}
}
