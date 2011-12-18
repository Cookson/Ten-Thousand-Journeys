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
		OBJ_HUMAN_ALTAR = 201,
		OBJ_HUMAN_TRIBUNE = 202,
		OBJ_BENCH = 203,
		OBJ_GRAVE_1 = 300,
		OBJ_GRAVE_2 = 301,
		OBJ_GRAVE_3 = 302,
		OBJ_STATUE_GARGOYLE = 307,
		OBJ_STATUE_DEFENDER_1 = 308,
		OBJ_STATUE_DEFENDER_2 = 309,
		OBJ_STATUE_DEFENDER_3 = 310,
		OBJ_STATUE_SPEAR_ELF_1 = 311,
		OBJ_STATUE_SPEAR_ELF_2 = 312,
		OBJ_STATUE_SPEAR_ELF_3 = 313,
		OBJ_STATUE_FEMALE_ELF_1 = 314,
		OBJ_STATUE_FEMALE_ELF_2 = 315,
		OBJ_STATUE_FEMALE_ELF_3 = 316,
		
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
		//                  Object Id                      Passability Width Height Usability
		setObjectProperties(OBJ_WORLD_CONUS_ROCK,          1,          64,   64,    0);
		setObjectProperties(OBJ_WORLD_CASTLE,              0,          64,   64,    0);
		setObjectProperties(OBJ_WORLD_ROCK,                1,          32,   40,    0);
		setObjectProperties(OBJ_WORLD_CITY,                0,          64,   64,    0);
		setObjectProperties(OBJ_WORLD_FOREST,              0,          36,   36,    0);
		setObjectProperties(OBJ_ROCK_1,                    3,          32,   32,    0);
		setObjectProperties(OBJ_ARMOR_RACK,                1,          32,   32,    0);
		setObjectProperties(OBJ_FURNACE		,              3,          32,   32,    0);
		setObjectProperties(OBJ_ANVIL,                     3,          32,   32,    0);
		setObjectProperties(OBJ_STOVE,                     3,          32,   32,    0);
		setObjectProperties(OBJ_BARREL,                    3,          32,   32,    0);
		setObjectProperties(OBJ_VINESHELF,                 0,          32,   58,    0);
		setObjectProperties(OBJ_TORCH,                     0,          32,   58,    0);
		setObjectProperties(OBJ_BED,                       3,          42,   32,    0);
		setObjectProperties(OBJ_WARDROBE,                  0,          32,   58,    0);
		setObjectProperties(OBJ_WALL_RED_STONE,            1,          32,   52,    0);
		setObjectProperties(OBJ_WALL_BUSH,                 1,          32,   52,    0);
		setObjectProperties(OBJ_WALL_GREY_STONE,           1,          32,   52,    0);
		setObjectProperties(OBJ_WALL_WOODEN,               1,          32,   52,    0);
		setObjectProperties(OBJ_WALL_LATTICE,              3,          32,   52,    0);
		setObjectProperties(OBJ_WALL_CAVE,                 1,          32,   52,    0);
		setObjectProperties(OBJ_DOOR_BLUE_OPEN,            0,          32,   42,    1);
		setObjectProperties(OBJ_DOOR_BLUE,                 1,          32,   42,    1);
		setObjectProperties(OBJ_DOOR_BROWN_OPEN,           0,          32,   42,    1);
		setObjectProperties(OBJ_DOOR_BROWN,                1,          32,   42,    1);
		setObjectProperties(OBJ_TREE_1,                    1,          32,   64,    0);
		setObjectProperties(OBJ_TREE_2,                    1,          64,   64,    0);
		setObjectProperties(OBJ_TREE_3,                    1,          96,   96,    0);
		setObjectProperties(OBJ_GIANT_MUSHROOM_1,          1,          32,   64,    0);
		setObjectProperties(OBJ_GIANT_MUSHROOM_2,          1,          64,   64,    0);
		setObjectProperties(OBJ_TREE_DEAD_1,               1,          42,   64,    0);
		setObjectProperties(OBJ_TREE_DEAD_2,               1,          42,   64,    0);
		setObjectProperties(OBJ_CHEST_1,                   3,          32,   32,    1);
		setObjectProperties(OBJ_CHEST_2,                   3,          32,   32,    1);
		setObjectProperties(OBJ_CHEST_3,                   3,          32,   32,    1);
		setObjectProperties(OBJ_CHEST_SKELETON,            3,          32,   32,    1);
		setObjectProperties(OBJ_SHELF_EMPTY,               0,          32,   58,    0);
		setObjectProperties(OBJ_BOOKSHELF_1,               0,          32,   50,    0);
		setObjectProperties(OBJ_BOOKSHELF_2,               0,          32,   50,    0);
		setObjectProperties(OBJ_TABLE_CHAIR_1,             3,          32,   32,    0);
		setObjectProperties(OBJ_TABLE_CHAIR_2,             3,          32,   32,    0);
		setObjectProperties(OBJ_TENT,                      1,          70,   70,    0);
		setObjectProperties(OBJ_HUMAN_ALTAR,               0,          32,   58,    0);
		setObjectProperties(OBJ_HUMAN_TRIBUNE,             3,          32,   32,    0);
		setObjectProperties(OBJ_BENCH,                     3,          32,   34,    0);
		setObjectProperties(OBJ_GRAVE_1,                   1,          32,   42,    0);
		setObjectProperties(OBJ_GRAVE_2,                   1,          32,   42,    0);
		setObjectProperties(OBJ_GRAVE_3,                   1,          24,   42,    0);
		setObjectProperties(OBJ_STATUE_GARGOYLE,           1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_DEFENDER_1,         1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_DEFENDER_2,         1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_DEFENDER_3,         1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_SPEAR_ELF_1,        1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_SPEAR_ELF_2,        1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_SPEAR_ELF_3,        1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_FEMALE_ELF_1,       1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_FEMALE_ELF_2,       1,          28,   52,    0);
		setObjectProperties(OBJ_STATUE_FEMALE_ELF_3,       1,          28,   52,    0);
		setObjectProperties(OBJ_STAIRS_SONE_GREY_DOWN,     0,          32,   32,    0);
		setObjectProperties(OBJ_STAIRS_SONE_GREY_UP,       1,          32,   32,    0);
		setObjectProperties(OBJ_LADDER_DOWN,               0,          32,   32,    0);
		setObjectProperties(OBJ_LADDER_UP,                 0,          32,   32,    0);
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
	public static void setObjectProperties(int id, int passability, int width, int height, int usability) {
		GameObjects.passability.put(id, passability);
		GameObjects.spriteSizeX.put(id, width);
		GameObjects.spriteSizeY.put(id, height);
		GameObjects.usability.put(id, usability);
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
