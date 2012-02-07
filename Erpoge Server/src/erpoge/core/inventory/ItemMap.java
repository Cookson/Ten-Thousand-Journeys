package erpoge.core.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import erpoge.core.Main;
import erpoge.core.itemtypes.ItemSystemMetaInfo;
import erpoge.core.itemtypes.ItemType;
import erpoge.core.terrain.Container;

public class ItemMap extends ItemSystemMetaInfo {
	// itemId => UniqueItem
	protected final HashMap<Integer, UniqueItem> uniqueItems = new HashMap<Integer, UniqueItem>();
	// typeId => ItemPile
	private final HashMap<Integer, ItemPile> itemPiles = new HashMap<Integer, ItemPile>();
	public ItemMap() {
		
	}
	public void add(UniqueItem item) {
		uniqueItems.put(item.getItemId(), item);
	}
	public void add(int typeId, int amount) {
		if (itemPiles.containsKey(typeId)) {
			itemPiles.get(typeId).changeAmount(amount);
		} else {
			itemPiles.put(typeId, new ItemPile(typeId, amount));
		}
	}
	public void add(ItemPile pile) {
		int typeId = pile.getType().getTypeId();
		if (itemPiles.containsKey(typeId)) {
			itemPiles.get(typeId).changeAmount(pile.getAmount());
		} else {
			itemPiles.put(typeId, pile);
		}
	}
	public void removePile(ItemPile pile) {
		ItemPile pileInMap = itemPiles.get(pile.getType().getTypeId());
		if (pile == pileInMap || pileInMap.getAmount() == pile.getAmount()) {
			itemPiles.remove(pile.getType().getTypeId());
		} else if (pile.getAmount() < pileInMap.getAmount()) {
			pileInMap.setAmount(pileInMap.getAmount() - pile.getAmount());
		} else {
			throw new Error("Incorrect pile removing: type "
					+ pile.getType().getTypeId() + ", removing "
					+ pile.getAmount() + ", has " + pileInMap.getAmount());
		}
	}
	public void removeUnique(UniqueItem item) {
		uniqueItems.remove(item.itemId);
	}
	public boolean hasUnique(int itemId) {
		return uniqueItems.containsKey(itemId);
	}
	public boolean hasPile(int typeId, int amount) {
		return itemPiles.containsKey(typeId)
				&& itemPiles.get(typeId).getAmount() >= amount;
	}
	public UniqueItem getUnique(int itemId) {
		return uniqueItems.get(itemId);
	}
	public ItemPile getPile(int typeId) {
		return itemPiles.get(typeId);
	}
	public int size() {
		return uniqueItems.size() + itemPiles.size();
	}
	public Collection<Item> values() {
		Collection<Item> c = new HashSet<Item>();
		c.addAll(uniqueItems.values());
		c.addAll(itemPiles.values());
		return c;
	}
	public String toString() {
		String answer = "[";
		for (Item i : uniqueItems.values()) {
			answer += i.toString() + ", ";
		}
		for (Item i : itemPiles.values()) {
			answer += i.toString() + ", ";
		}
		return answer + "]";
	}
	public String jsonGetContents() {
		StringBuilder answer = new StringBuilder("[");
		int i = 1;
		int size = size();
		for (UniqueItem item : uniqueItems.values()) {
			answer.append(item.toJson()).append((i == size) ? "" : ",");
			i++;
		}
		for (ItemPile item : itemPiles.values()) {
			answer.append(item.toJson()).append((i == size) ? "" : ",");
			i++;
		}
		answer.append("]");
		return answer.toString();
	}
	public int[] getDataForSending() {
		int[] answer = new int[size()*2];
		int pos = 0;
		for (UniqueItem item : uniqueItems.values()) {
			answer[pos++] = item.type.getTypeId();
			answer[pos++] = item.itemId;
		}
		for (ItemPile item : itemPiles.values()) {
			answer[pos++] = item.type.getTypeId();
			answer[pos++] = item.getAmount();
		}
		return answer;
	}
}
