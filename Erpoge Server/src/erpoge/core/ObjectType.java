package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * Describes a certain type of inanimate objects that are large enough to be
 * treated as {@link Item}s: trees, furniture, wall segments
 */
public class ObjectType extends UniqueObject implements GsonForStaticDataSerializable {
	private final String name;
	private final int passability;
	private final boolean isUsable;

	public ObjectType(String name, int passability, boolean isUsable) {
		super();
		this.name = name;
		this.passability = passability;
		this.isUsable = isUsable;
	}

	public int getPassability() {
		return passability;
	}

	public boolean isUsable() {
		return isUsable;
	}

	public String getName() {
		return name;
	}

	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(name));
		jArray.add(new JsonPrimitive(passability));
		return jArray;
	}

}
