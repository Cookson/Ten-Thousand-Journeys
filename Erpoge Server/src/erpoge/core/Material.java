package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class Material extends UniqueObject implements GsonForStaticDataSerializable {
	private int durability;
	private int density;
	private String name;
	public Material(String name, int durability, int density) {
		super();
		this.name = name;
		this.durability = durability;
		this.density = density;
	}
	public String toString() {
		return name;
	}
	public String getName() {
		return name;
	}
	public int getDurability() {
		return durability;
	}
	public int getDensity() {
		return density;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(name));
		jArray.add(new JsonPrimitive(durability));
		jArray.add(new JsonPrimitive(density));
		return jArray;
	}
}
