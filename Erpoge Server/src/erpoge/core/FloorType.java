package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class FloorType extends UniqueObject implements GsonForStaticDataSerializable {
	private String name;
	FloorType(String name) {
		super();
		this.name = name;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(name));
		return jArray;
	}
	public String getName() {
		return name;
	}
	
}
