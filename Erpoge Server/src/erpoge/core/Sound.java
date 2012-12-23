package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;


/**
 * Represents a single sound that is, unlike {@link SoundSource} heard by
 * characters at certain moment.
 * 
 * @see SoundSource.
 * @author suseika
 * 
 */
public class Sound implements GsonForStaticDataSerializable {
	public SoundType type;
	public int x;
	public int y;

	public Sound(int x, int y, SoundType type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}

	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(x));
		jArray.add(new JsonPrimitive(y));
		jArray.add(new JsonPrimitive(type.getId()));
		return jArray;
	}
}
