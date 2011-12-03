package erpoge.inventory;
import java.util.ArrayList;
import java.util.Arrays;

import erpoge.Utils;
import erpoge.Main;

public class AmmunitionMap extends ItemMap {
	private static final UniqueItem FREE_PIECE = new UniqueItem(-1);
	
	private UniqueItem[] ammunition = new UniqueItem[NUMBER_OF_SLOTS];

	public AmmunitionMap() {
		super();
		for (int i = 0; i < NUMBER_OF_SLOTS; i++) {
			ammunition[i] = FREE_PIECE;
		}
	}

	public void add(UniqueItem item) {
		super.add(item);
		ammunition[item.getType().getSlot()] = item;
	}
	
	public void remove(UniqueItem item) {
		ammunition[ItemsTypology.getSlotFromId(item.getType().getTypeId())] = FREE_PIECE;
		super.removeUnique(item);
	}
	
	public void removeSlot(int slot) {
		// TODO Auto-generated method stub
		super.removeUnique(ammunition[slot]);
		ammunition[slot] = FREE_PIECE;
	}

	public boolean hasPiece(int slot) {
		return ammunition[slot] != FREE_PIECE;
	}

	public UniqueItem getItemInSlot(int slot) {
		return ammunition[slot];
	}
	public int[] getIntArray() {
		int [] answer = new int[22];
		int i = 0;
		for (Item item : ammunition) {
			answer[i] = ammunition[i].getItemId();
		}
		return answer;
	}
	public String jsonGetAmmunition() {
		String answer ="{";
		boolean atLeastOneItem = false;
		for (int i=0; i<NUMBER_OF_SLOTS; i++) {
			if (ammunition[i] == FREE_PIECE) {
				continue;
			}
			answer += (atLeastOneItem ? "," : "")+"\""+i+"\":"+ammunition[i].toJson();
			atLeastOneItem = true;
		}
		return answer+"}";
	}
}
