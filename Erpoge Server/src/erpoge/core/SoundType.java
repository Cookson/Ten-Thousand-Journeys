package erpoge.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class SoundType implements GsonForStaticDataSerializable {
	private int lastSoundId = 0;
	private String name;
	private int bass;
	private int mid;
	private int treble;
	private int id;
	public SoundType(String name, int bass, int mid, int treble) {
		this.id = ++lastSoundId;
		this.name = name;
		this.bass = bass;
		this.mid = mid;
		this.treble = treble;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the bass
	 */
	public int getBass() {
		return bass;
	}
	/**
	 * @return the mid
	 */
	public int getMid() {
		return mid;
	}
	/**
	 * @return the treble
	 */
	public int getTreble() {
		return treble;
	}
	public int getId() {
		return id;
	}
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		jArray.add(new JsonPrimitive(name));
		jArray.add(new JsonPrimitive(bass));
		jArray.add(new JsonPrimitive(mid));
		jArray.add(new JsonPrimitive(treble));
		return jArray;
	}
}
