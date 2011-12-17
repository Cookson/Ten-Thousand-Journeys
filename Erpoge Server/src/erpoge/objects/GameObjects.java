package erpoge.objects;

import java.util.HashMap;
import java.util.Set;

import erpoge.Coordinate;
import erpoge.Main;

public class GameObjects {
	public final static int
		FLOOR_VOID = 0,
		FLOOR_GRASS = 1,
		FLOOR_STONE = 2,
		FLOOR_SNOW = 3,
		FLOOR_SOIL = 4,
		FLOOR_GROUND = 5,
		FLOOR_DRY_GRASS = 6,
		FLOOR_WATER = 7,
		FLOOR_WOODEN = 8,
		
		OBJ_VOID = 0,
		OBJ_WALL_RED_STONE = 1,
		OBJ_WALL_BUSH = 3,
		OBJ_WALL_GREY_STONE = 4,
		OBJ_WALL_WOODEN = 5,
		OBJ_WALL_LATTICE = 6,
		OBJ_WALL_CAVE = 7,
		OBJ_DOOR_BLUE_OPEN = 41,
		OBJ_DOOR_BLUE = 42,
		OBJ_DOOR_BROWN_OPEN= 43,
		OBJ_DOOR_BROWN = 44,
		OBJ_TREE_1 = 51,
		OBJ_TREE_2 = 52,
		OBJ_TREE_3 = 53,
		OBJ_GIANT_MUSHROOM_1 = 54,
		OBJ_GIANT_MUSHROOM_2 = 55,
		OBJ_TREE_DEAD_1 = 56,
		OBJ_TREE_DEAD_2 = 57,
		OBJ_CHEST_1 = 60,
		OBJ_CHEST_2 = 61,
		OBJ_CHEST_3 = 62,
		OBJ_CHEST_SKELETON = 63,
		OBJ_SHELF_EMPTY = 70,
		OBJ_BOOKSHELF_1 = 71,
		OBJ_BOOKSHELF_2 = 72,
		OBJ_TABLE_CHAIR_1 = 73,
		OBJ_TABLE_CHAIR_2 = 74,
		OBJ_BED = 75,
		OBJ_TORCH = 76,
		OBJ_VINESHELF = 77,
		OBJ_BARREL = 78,
		OBJ_STOVE = 79,
		OBJ_FURNACE = 80,
		OBJ_ANVIL = 81,
		OBJ_ARMOR_RACK = 82,
		OBJ_WARDROBE = 83,
		OBJ_ROCK_1 = 90,
		OBJ_SKELETON = 120,
		OBJ_TENT = 200,
		OBJ_GRAVE_1 = 300,
		OBJ_GRAVE_2 = 301,
		OBJ_GRAVE_3 = 302,
		OBJ_STATUE_GARGOYLE = 307,
		
		OBJ_STAIRS_SONE_GREY_DOWN = 400,
		OBJ_STAIRS_SONE_GREY_UP = 401,
		OBJ_LADDER_DOWN = 402,
		OBJ_LADDER_UP = 403,
		
		OBJ_WORLD_FOREST = 900,
		OBJ_WORLD_CITY = 901,
		OBJ_WORLD_CASTLE = 902,
		OBJ_WORLD_ROCK = 903,
		OBJ_WORLD_CONUS_ROCK = 904,
		OBJ_WORLD_CRYPT = 905,
		OBJ_WORLD_MOON_SHRINE = 906;
	
	
	public static final HashMap<Integer, Integer> passability = new HashMap<Integer, Integer>();
	public static final HashMap<Integer, Integer> usability = new HashMap<Integer, Integer>();
	public static final HashMap<Integer, Integer> spriteSizeX = new HashMap<Integer, Integer>();
	public static final HashMap<Integer, Integer> spriteSizeY = new HashMap<Integer, Integer>();
	public static void init() {
		initPassability();
		initSizes();
		initUsability();
	}
	public static void initPassability() {
		setPassability(OBJ_WORLD_CONUS_ROCK, 1);
		setPassability(OBJ_WORLD_CASTLE, 0);
		setPassability(OBJ_WORLD_ROCK, 1);
		setPassability(OBJ_WORLD_CITY, 0);
		setPassability(OBJ_WORLD_FOREST, 0);
		setPassability(OBJ_ROCK_1, 3);
		setPassability(OBJ_ARMOR_RACK, 1);
		setPassability(OBJ_ARMOR_RACK, 3);
		setPassability(OBJ_ANVIL, 3);
		setPassability(OBJ_STOVE, 3);
		setPassability(OBJ_BARREL, 3);
		setPassability(OBJ_VINESHELF, 0);
		setPassability(OBJ_TORCH, 0);
		setPassability(OBJ_BED, 3);
		setPassability(OBJ_WARDROBE, 0);
		setPassability(OBJ_WALL_RED_STONE, 1);
		setPassability(OBJ_WALL_BUSH, 1);
		setPassability(OBJ_WALL_GREY_STONE, 1);
		setPassability(OBJ_WALL_WOODEN, 1);
		setPassability(OBJ_WALL_LATTICE, 3);
		setPassability(OBJ_WALL_CAVE, 1);
		setPassability(OBJ_DOOR_BLUE_OPEN, 0);
		setPassability(OBJ_DOOR_BLUE, 1);
		setPassability(OBJ_DOOR_BROWN_OPEN, 0);
		setPassability(OBJ_DOOR_BROWN, 1);
		setPassability(OBJ_TREE_1, 1);
		setPassability(OBJ_TREE_2, 1);
		setPassability(OBJ_TREE_3, 1);
		setPassability(OBJ_GIANT_MUSHROOM_1, 1);
		setPassability(OBJ_GIANT_MUSHROOM_2, 1);
		setPassability(OBJ_TREE_DEAD_1, 1);
		setPassability(OBJ_TREE_DEAD_2, 1);
		setPassability(OBJ_CHEST_1, 3);
		setPassability(OBJ_CHEST_2, 3);
		setPassability(OBJ_CHEST_3, 3);
		setPassability(OBJ_CHEST_SKELETON, 3);
		setPassability(OBJ_SHELF_EMPTY, 0);
		setPassability(OBJ_BOOKSHELF_1, 0);
		setPassability(OBJ_BOOKSHELF_2, 0);
		setPassability(OBJ_TABLE_CHAIR_1, 3);
		setPassability(OBJ_TABLE_CHAIR_2, 3);
		setPassability(OBJ_TENT, 1);
		setPassability(OBJ_GRAVE_1, 1);
		setPassability(OBJ_GRAVE_2, 1);
		setPassability(OBJ_GRAVE_3, 1);
		setPassability(OBJ_STATUE_GARGOYLE, 1);
		setPassability(OBJ_STAIRS_SONE_GREY_DOWN, 0);
		setPassability(OBJ_STAIRS_SONE_GREY_UP, 1);
		setPassability(OBJ_LADDER_DOWN, 0);
		setPassability(OBJ_LADDER_UP, 0);
	}
	public static void initSizes() {
		setSpriteSize(OBJ_WORLD_CONUS_ROCK, 64, 64);
		setSpriteSize(OBJ_WORLD_ROCK, 32, 40);
		setSpriteSize(OBJ_ROCK_1, 32, 32);
		setSpriteSize(OBJ_WORLD_CASTLE, 64, 64);
		setSpriteSize(OBJ_WORLD_CITY, 64, 64);
		setSpriteSize(OBJ_WORLD_FOREST, 36, 36);
		setSpriteSize(OBJ_WALL_RED_STONE, 32, 52);
		setSpriteSize(OBJ_WALL_BUSH, 32, 52);
		setSpriteSize(OBJ_WALL_GREY_STONE, 32, 52);
		setSpriteSize(OBJ_WALL_WOODEN, 32, 52);
		setSpriteSize(OBJ_WALL_LATTICE, 32, 52);
		setSpriteSize(OBJ_WALL_CAVE, 32, 52);
		setSpriteSize(OBJ_DOOR_BLUE_OPEN, 32, 42);
		setSpriteSize(OBJ_DOOR_BLUE, 32, 42);
		setSpriteSize(OBJ_DOOR_BROWN_OPEN, 32, 42);
		setSpriteSize(OBJ_DOOR_BROWN, 32, 42);
		setSpriteSize(OBJ_TREE_1, 32, 64);
		setSpriteSize(OBJ_TREE_2, 64, 64);
		setSpriteSize(OBJ_TREE_3, 96, 96);
		setSpriteSize(OBJ_GIANT_MUSHROOM_1, 32, 64);
		setSpriteSize(OBJ_GIANT_MUSHROOM_2, 64, 64);
		setSpriteSize(OBJ_TREE_DEAD_1, 42, 64);
		setSpriteSize(OBJ_TREE_DEAD_2, 42, 64);
		setSpriteSize(OBJ_CHEST_1, 32, 32);
		setSpriteSize(OBJ_CHEST_2, 32, 32);
		setSpriteSize(OBJ_CHEST_3, 32, 32);
		setSpriteSize(OBJ_CHEST_SKELETON, 32, 32);
		setSpriteSize(OBJ_SHELF_EMPTY, 32, 58);
		setSpriteSize(OBJ_BOOKSHELF_1, 32, 50);
		setSpriteSize(OBJ_BOOKSHELF_2, 32, 50);
		setSpriteSize(OBJ_TABLE_CHAIR_1, 32, 32);
		setSpriteSize(OBJ_TABLE_CHAIR_2, 32, 32);
		setSpriteSize(OBJ_BED, 42, 32);
		setSpriteSize(OBJ_WARDROBE, 32, 58);
		setSpriteSize(OBJ_TORCH, 32, 58);
		setSpriteSize(OBJ_VINESHELF, 32, 58);
		setSpriteSize(OBJ_BARREL, 32, 32);
		setSpriteSize(OBJ_STOVE, 32, 32);
		setSpriteSize(OBJ_FURNACE, 64, 64);
		setSpriteSize(OBJ_ANVIL, 32, 32);
		setSpriteSize(OBJ_ARMOR_RACK, 32, 32);
		setSpriteSize(OBJ_ROCK_1, 32, 32);
		setSpriteSize(OBJ_TENT, 32, 32);
		setSpriteSize(OBJ_GRAVE_1, 32, 42);
		setSpriteSize(OBJ_GRAVE_2, 32, 42);
		setSpriteSize(OBJ_GRAVE_3, 24, 42);
		setSpriteSize(OBJ_STATUE_GARGOYLE, 28, 52);
		setSpriteSize(OBJ_STAIRS_SONE_GREY_DOWN, 32, 32);
		setSpriteSize(OBJ_STAIRS_SONE_GREY_UP, 32, 32);
		setSpriteSize(OBJ_LADDER_DOWN, 32, 32);
		setSpriteSize(OBJ_LADDER_UP, 32, 32);
	}
	public static void initUsability() {
		setUsability(OBJ_WORLD_CONUS_ROCK, 0);
		setUsability(OBJ_WORLD_ROCK, 0);
		setUsability(OBJ_WORLD_CASTLE, 0);
		setUsability(OBJ_WORLD_CITY, 0);
		setUsability(OBJ_WORLD_FOREST, 0);
		setUsability(OBJ_ROCK_1, 0);
		setUsability(OBJ_WALL_RED_STONE, 0);
		setUsability(OBJ_WALL_BUSH, 0);
		setUsability(OBJ_WALL_GREY_STONE, 0);
		setUsability(OBJ_WALL_WOODEN, 0);
		setUsability(OBJ_WALL_LATTICE, 0);
		setUsability(OBJ_WALL_CAVE, 0);
		setUsability(OBJ_DOOR_BLUE_OPEN, 1);
		setUsability(OBJ_DOOR_BLUE, 1);		
		setUsability(OBJ_DOOR_BROWN_OPEN, 1);
		setUsability(OBJ_DOOR_BROWN, 1);
		setUsability(OBJ_TREE_1, 0);
		setUsability(OBJ_TREE_2, 0);
		setUsability(OBJ_TREE_3, 0);
		setUsability(OBJ_GIANT_MUSHROOM_1, 0);
		setUsability(OBJ_GIANT_MUSHROOM_2, 0);
		setUsability(OBJ_TREE_DEAD_1, 0);
		setUsability(OBJ_TREE_DEAD_2, 0);
		setUsability(OBJ_CHEST_1, 1);
		setUsability(OBJ_CHEST_2, 1);
		setUsability(OBJ_CHEST_3, 1);
		setUsability(OBJ_CHEST_SKELETON, 1);
		setUsability(OBJ_SHELF_EMPTY, 0);
		setUsability(OBJ_BOOKSHELF_1, 0);
		setUsability(OBJ_BOOKSHELF_2, 0);
		setUsability(OBJ_TABLE_CHAIR_1, 0);
		setUsability(OBJ_TABLE_CHAIR_2, 0);
		setUsability(OBJ_BED, 0);
		setUsability(OBJ_WARDROBE, 0);
		setUsability(OBJ_TORCH, 0);
		setUsability(OBJ_VINESHELF, 0);
		setUsability(OBJ_BARREL, 0);
		setUsability(OBJ_STOVE, 0);
		setUsability(OBJ_FURNACE, 0);
		setUsability(OBJ_ANVIL, 0);
		setUsability(OBJ_ARMOR_RACK, 0);
		setUsability(OBJ_ROCK_1, 0);
		setUsability(OBJ_TENT, 0);
		setUsability(OBJ_GRAVE_1, 0);
		setUsability(OBJ_GRAVE_2, 0);
		setUsability(OBJ_GRAVE_3, 0);
		setUsability(OBJ_STATUE_GARGOYLE, 0);
		setUsability(OBJ_STAIRS_SONE_GREY_DOWN, 0);
		setUsability(OBJ_STAIRS_SONE_GREY_UP, 0);
		setUsability(OBJ_LADDER_DOWN, 0);
		setUsability(OBJ_LADDER_UP, 0);
		
	}
	public static String jsonGetObjectProperties() {
		StringBuffer buf = new StringBuffer();
		buf.append("objectProperties = {\n");
		Set<Integer> keys = passability.keySet();
		for (int k : keys) {
			int usability = getUsability(k);
			buf.append("\t"+k+"\t: ["+getSpriteSizeX(k)+","+getSpriteSizeY(k)+","+getPassability(k)+((usability==1) ? ",1" : "")+"],\n");
		}
		buf.append("};");
		return buf.toString();
	}
	public static void setPassability(int id, int val) {
		passability.put(id, val);
	}
	public static void setSpriteSize(int id, int width, int height) {
		spriteSizeX.put(id, width);
		spriteSizeY.put(id, height);
	}
	public static void setUsability(int id, int val) {
		usability.put(id, val);
	}
	public static int getSpriteSizeX(int id) {
		return spriteSizeX.get(id);
	}
	public static int getSpriteSizeY(int id) {
		return spriteSizeY.get(id);
	}
	public static int getPassability(int id) {
		return passability.get(id);
	}
	public static int getUsability(int id) {
		return usability.get(id);
	}
}
