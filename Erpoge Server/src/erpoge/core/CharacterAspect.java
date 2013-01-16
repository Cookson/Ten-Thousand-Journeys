package erpoge.core;

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * Enum-like static data structure that stores flags for {@link CharacterType} 
 * creation that determine CharacterType's nature, like `humanoid`, `animal` or
 * `robot`. Each aspect is identified by both its name and id — a human readable 
 * string and a generated integer.
 *
 */
public class CharacterAspect extends UniqueObject implements GsonForStaticDataSerializable {
	private static final HashMap<String, CharacterAspect> aspects = new HashMap<String, CharacterAspect>();
	private final String name;
	static {
		registerAspect("humanoid");
		registerAspect("animal");
	}
	/**
	 * 
	 * @param name
	 */
	private CharacterAspect(String name) {
		this.name = name;
	}
	public static CharacterAspect getByName(String name) {
		CharacterAspect aspect = aspects.get(name);
		if (aspect == null) {
			throw new RuntimeException("Aspect "+name+" has not been registered; did you mistype it?");
		}
		return aspect;
	}
	/**
	 * Adds a new available CharacterAspect aspect to game.
	 * 
	 * @param name Name of aspect.
	 */
	static void registerAspect(String name) {
		aspects.put(name, new CharacterAspect(name));
	}
	/**
	 * Serializes a {@link CharacterAspect} into JSON/
	 * 
	 * @return A single JsonElement — the id of the CharacterAspect.
	 */
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		return new JsonPrimitive(name);
	}
}
