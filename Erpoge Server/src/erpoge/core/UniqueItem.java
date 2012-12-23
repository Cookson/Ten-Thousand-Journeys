package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * A single non-stackable item. Its main property is its id inherited from
 * {@link UniqueObject}. Unlike {@link ItemPile}s, UniqueObjects are unique -  
 */
public class UniqueItem extends UniqueObject implements Item, GsonForStaticDataSerializable {
	private ItemType type;

	public UniqueItem(int typeId) {
		super();
		type = StaticData.getItemType(typeId);
	}

	public String toString() {
		return type.getName();
	}

	@Override
	public int getParam() {
		return id;
	}

	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(type.getId()));
		jArray.add(new JsonPrimitive(id));
		return jArray;
	}

	@Override
	public ItemType getType() {
		return type;
	}
}
