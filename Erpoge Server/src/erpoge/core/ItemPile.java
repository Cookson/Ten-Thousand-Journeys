package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;


public class ItemPile implements Item, GsonForStaticDataSerializable {
	private int amount;
	private ItemType type;
	public ItemPile(int typeId, int amount) {
		type = StaticData.getItemType(typeId);
		this.amount = amount;
	}
	public int changeAmount(int amount) {
		if (this.amount + amount <= 0) {
			throw new Error("Item's amount decreased by more than this item contained ("+this.amount+" - "+amount+")");
		}
		this.amount += amount;
		return this.amount;
	}
	public int getAmount() {
		return amount;
	}
	public int setAmount(int amount) {
		this.amount = amount;
		return amount;
	}
	public int hashCode() {
		return type.getId()*100000+amount;
	}
	public ItemPile separatePile(int amount) {
		return new ItemPile(type.getId(), amount);
	}
	@Override
	public String toString() {
		return amount+" "+type.getName();
	}
	@Override
	public int getParam() {
		return amount;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(type.getId()));
		jArray.add(new JsonPrimitive(amount));
		return jArray;
	}
	@Override
	public ItemType getType() {
		return type;
	}
}
