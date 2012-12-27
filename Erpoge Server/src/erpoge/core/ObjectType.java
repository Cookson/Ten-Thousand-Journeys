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
	public static final int CLASS_DEFAULT = 0;
	public static final int CLASS_WALL = 1;
	public static final int CLASS_DOOR = 2;
	public static final int CLASS_INTERLEVEL = 3;
	private final String name;
	private final int passability;
	private final boolean isUsable;
	private final int cls;

	public ObjectType(String name, int passability, boolean isUsable, int cls) {
		super();
		this.name = name;
		this.passability = passability;
		this.isUsable = isUsable;
		this.cls = cls;
	}
	
	public int getObjectClass() {
		return cls;
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
