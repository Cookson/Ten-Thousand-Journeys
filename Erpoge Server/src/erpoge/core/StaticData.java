package erpoge.core;

import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;


/**
 * Singleton that stores all the data about various types of in-game objects,
 * such as characters, objects or items. Provides access to information about
 * such types.
 */
public class StaticData implements GsonForStaticDataSerializable {
	/**
	 * Constant that means absence of an object or a floor tile on a cell. Used
	 * by methods such as {@link TerrainBasics#setFloor(int, int, int)} or
	 * {@link TerrainBasics#setObject(int, int, int)}.
	 */
	public static final int VOID = -1;
	final HashMap<Integer, Material> materials = new HashMap<Integer, Material>();
	final HashMap<Integer, SoundType> soundTypes = new HashMap<Integer, SoundType>();
	final HashMap<Integer, ItemType> itemTypes = new HashMap<Integer, ItemType>();
	final HashMap<Integer, CharacterType> characterTypes = new HashMap<Integer, CharacterType>();
	final HashMap<Integer, FloorType> floorTypes = new HashMap<Integer, FloorType>();
	final HashMap<Integer, ObjectType> objectTypes = new HashMap<Integer, ObjectType>();
	private final HashMap<String, Material> materialsByName = new HashMap<String, Material>();
	final HashMap<String, FloorType> floorTypesByName = new HashMap<String, FloorType>();
	final HashMap<String, ObjectType> objectTypesByName = new HashMap<String, ObjectType>();
	final HashMap<Integer, SoundType> soundTypesByName = new HashMap<Integer, SoundType>();
	public static final StaticData instance = new StaticData();
	/*
	 * Types of ObjectTypes passabilities. Complex types like "visual and 
	 * penetrable, but not walkable" are made with simple binary logic like
	 * PASSABILITY_VISUAL+PASSABILITY_PENETRABLE. We can check whether a certain
	 * passability value contains a passability type with, for example visual,
	 * with:
	 * passability & PASSABILITY_VISUAL
	 */
	
	public static final int PASSABILITY_NONE = 0;
	/**
	 * You can see through the object
	 */
	public static final int PASSABILITY_VISUAL = 1;
	/**
	 * You can see through the object
	 */
	public static final int PASSABILITY_WALKABLE = 2;
	/**
	 * You can shoot through the object
	 */
	public static final int PASSABILITY_PENETRABLE = 4;

	public StaticData() {
		
	}

	public static void showData() {
		System.out.println(GsonArbitraryArrays.getGson().toJson(instance));
	}
	public static CharacterType getCharacterType(int characterTypeId) {
		return instance.characterTypes.get(characterTypeId);
	}

	public static ItemType getItemType(int itemTypeId) {
		return instance.itemTypes.get(itemTypeId);
	}

	public static ItemType getItemType(String name) {
		for (ItemType type : instance.itemTypes.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	public static CharacterType getCharacterType(String name) {
		for (CharacterType type : instance.characterTypes.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}
	/**
	 * Registers a SoundType to be used in user code.
	 * 
	 * @param soundType Type description built from game data file.
	 */
	public static void add(SoundType soundType) {
		instance.soundTypes.put(soundType.getId(), soundType);
	}
	/**
	 * Registers a CharacterType to be used in user code.
	 * 
	 * @param characterType Type description built from .xml game data.
	 */
	public static void add(CharacterType characterType) {
		instance.characterTypes.put(characterType.getId(), characterType);
	}
	/**
	 * Registers a Material to be used in user code.
	 * 
	 * @param material Type description built from .xml game data.
	 */
	public static void add(Material material) {
		instance.materials.put(material.getId(), material);
		instance.materialsByName.put(material.getName(), material);
	}

	public static void add(ItemType itemType) {
		instance.itemTypes.put(itemType.getId(), itemType);
	}
	
	public static void add(FloorType floorType) {
		instance.floorTypes.put(floorType.getId(), floorType);
		instance.floorTypesByName.put(floorType.getName(), floorType);
	}
	
	public static void add(ObjectType objectType) {
		instance.objectTypes.put(objectType.getId(), objectType);
		instance.objectTypesByName.put(objectType.getName(), objectType);
	}
	/**
	 * Get Material by its string name.
	 * @see StaticData#getMaterial(int)
	 * @param name Name of material as it is declared in game data xml.
	 * @return
	 */
	public static Material getMaterialByName(String name) {
		Material type = instance.materialsByName.get(name);
		if (type == null) {
			throw new NullPointerException("Material "+name+" has not been registered. Did you misspell it?");
		}
		return type;
	}
	/**
	 * Get FloorType by its string name.
	 * @see StaticData#getFloorType(int)
	 * @param name Name of floor as it is declared in game data xml.
	 * @return
	 */
	public static FloorType getFloorType(String name) {
		FloorType type = instance.floorTypesByName.get(name);
		if (type == null) {
			throw new NullPointerException("Floor type "+name+" has not been registered. Did you misspell it?");
		}
		return type;
	}
	/**
	 * Get ObjectType by its id.
	 * @see StaticData#getObjectType(String)
	 * @see UniqueObject#getId()
	 * @param id Id of an ObjectType, generated by UniqueObject constructor.
	 * @return
	 */
	public static ObjectType getObjectType(int id) {
		return instance.objectTypes.get(id);
	}
	/**
	 * Get ObjectType by its string name.
	 * @see StaticData#getObjectType(int)
	 * @param name Name of an object as it is declared in game data xml.
	 * @return
	 */
	public static ObjectType getObjectType(String name) {
		ObjectType type = instance.objectTypesByName.get(name);
		if (type == null) {
			throw new NullPointerException("Object type "+name+" has not been registered. Did you misspell it?");
		}
		return type;
	}

	public static SoundType getSoundType(String name) {
		return instance.soundTypesByName .get(name);
	}

	public static SoundType getSoundType(int id) {
		return instance.soundTypes.get(id);
	}
	/**
	 * Serializes all the known static data to build a client static data file.
	 * @param typeOfSrc
	 * @param context
	 * @return
	 */
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		JsonArray jArray = new JsonArray();
		JsonArray jItemsArray = new JsonArray();
		JsonArray jCharactersArray = new JsonArray();
		JsonArray jMaterialsArray = new JsonArray();
		JsonArray jSoundsArray = new JsonArray();
		JsonArray jObjectsArray = new JsonArray();
		JsonArray jFloorsArray = new JsonArray();
		for (ItemType type : itemTypes.values()) {
			jItemsArray.add(type.serialize(context));
		}
		for (CharacterType type : characterTypes.values()) {
			jCharactersArray.add(type.serialize(context));
		}
		for (ObjectType type : objectTypes.values()) {
			jObjectsArray.add(type.serialize(context));
		}
		for (FloorType type : floorTypes.values()) {
			jFloorsArray.add(type.serialize(context));
		}
		for (SoundType type : soundTypes.values()) {
			jSoundsArray.add(type.serialize(context));
		}
		for (Material type : materials.values()) {
			jMaterialsArray.add(type.serialize(context));
		}
//		jArray.add(jItemsArray);
		jArray.add(jCharactersArray);
//		jArray.add(jMaterialsArray);
//		jArray.add(jSoundsArray);
//		jArray.add(jObjectsArray);
//		jArray.add(jFloorsArray);
		return jArray;
	}
}
