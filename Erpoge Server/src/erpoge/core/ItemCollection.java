package erpoge.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

/**
 * ItemCollection stores a group of items, representing, for example,
 * character's inventory or a heap of items lying on the ground.
 */
public class ItemCollection implements GsonForStaticDataSerializable {
	// itemId => UniqueItem
	protected final HashMap<Integer, UniqueItem> uniqueItems = new HashMap<Integer, UniqueItem>();
	// typeId => ItemPile
	private final HashMap<Integer, ItemPile> itemPiles = new HashMap<Integer, ItemPile>();

	public ItemCollection() {

	}

	public void add(UniqueItem item) {
		uniqueItems.put(item.getId(), item);
	}

	public void add(int typeId, int amount) {
		if (itemPiles.containsKey(typeId)) {
			itemPiles.get(typeId).changeAmount(amount);
		} else {
			itemPiles.put(typeId, new ItemPile(typeId, amount));
		}
	}

	public void add(ItemPile pile) {
		int typeId = pile.getType().getId();
		if (itemPiles.containsKey(typeId)) {
			itemPiles.get(typeId).changeAmount(pile.getAmount());
		} else {
			itemPiles.put(typeId, pile);
		}
	}

	public void removePile(ItemPile pile) {
		ItemPile pileInMap = itemPiles.get(pile.getType().getId());
		if (pile == pileInMap || pileInMap.getAmount() == pile.getAmount()) {
			itemPiles.remove(pile.getType().getId());
		} else if (pile.getAmount() < pileInMap.getAmount()) {
			pileInMap.setAmount(pileInMap.getAmount() - pile.getAmount());
		} else {
			throw new Error("Incorrect pile removing: type "
					+ pile.getType().getId() + ", removing " + pile.getAmount()
					+ ", has " + pileInMap.getAmount());
		}
	}

	public void removeUnique(UniqueItem item) {
		uniqueItems.remove(item.getId());
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


	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		for (UniqueItem item : uniqueItems.values()) {
			jArray.add(item.serialize(context));
		}
		for (ItemPile pile : itemPiles.values()) {
			jArray.add(pile.serialize(context));
		}
		return jArray;
	}
}
