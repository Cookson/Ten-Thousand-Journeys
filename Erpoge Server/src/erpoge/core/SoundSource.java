package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * Represents a prolonged sound that, unlike {@link Sound}, has its duration, or
 * even may be coming out infinitely.
 * @see {@link Sound}
 */
public class SoundSource extends Sound implements GsonForStaticDataSerializable {
	public int lifetime;

	public SoundSource(int x, int y, SoundType type, int lifetime) {
		super(x, y, type);
		this.lifetime = lifetime;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = (JsonArray) super.serialize(context);
		jArray.add(new JsonPrimitive(lifetime));
		return jArray;
	}
}
