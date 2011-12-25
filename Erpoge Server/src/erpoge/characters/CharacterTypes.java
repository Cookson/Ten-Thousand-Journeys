package erpoge.characters;

import java.util.ArrayList;
import java.util.HashMap;

import erpoge.Main;
import erpoge.itemtypes.ItemType;
import erpoge.language.Language;
/**
 * Typology of all characters who are in game.
 *
 */
public final class CharacterTypes {
	private static final CharacterTypes instance = new CharacterTypes();
	public static final int 
		CREATURE_TYPE_HUMANOID = 1,
		CREATURE_TYPE_ANIMAL = 2,
		CREATURE_TYPE_REPTILE = 3,
		CREATURE_TYPE_ELEMENTAL = 4,
		CREATURE_TYPE_INSECT = 5,
		CREATURE_TYPE_MECHANISM = 6,
		CREATURE_TYPE_SLIME = 7,
		CREATURE_TYPE_DEMON = 8,
		CREATURE_TYPE_HOLY = 9,
		CREATURE_TYPE_UNDEAD = 10;
	public static final int 
		ATTACK_TYPE_CLAW = 1,
		ATTACK_TYPE_BITE = 2,
		ATTACK_TYPE_SMASH = 3,
		ATTACK_TYPE_SPIT = 4,
		ATTACK_TYPE_FIRE_BREATH = 5;
	public static final int 
		NUMBER_OF_PROTECTIONS = 6,
		PROT_FIRE = 1,
		PROT_FROST = 2,
		PROT_POISON = 3,
		PROT_ACID = 4,
		PROT_ELECTRICITY = 5;
	public static final int 
		SIZE_TINY = 0,
		SIZE_LITLE = 1,
		SIZE_SMALL = 2,
		SIZE_MEDIUM = 3,
		SIZE_LARGE = 4,
		SIZE_BIG = 5,
		SIZE_GIANT = 6,
		SIZE_HUGE = 7;
		
	private static final HashMap<String, CharacterType> types = new HashMap<String, CharacterType>();
	public static void init() {
		addType("dragon", CREATURE_TYPE_REPTILE, SIZE_HUGE, 300, 200, 10, 20, 19, spells(2),
				protections(PROT_FROST,-1,PROT_FIRE,4));
		addType("goblin", CREATURE_TYPE_HUMANOID, SIZE_MEDIUM, 30, 0, 10, 5, 9, spells(),
				protections());
		addType("goblinMage", CREATURE_TYPE_HUMANOID, SIZE_MEDIUM, 30, 0, 10, 5, 9, spells(2),
				protections());
		addType("ogre", CREATURE_TYPE_HUMANOID, SIZE_LARGE, 30, 0, 10, 5, 9, spells(),
				protections());
		addType("dwarvenHooker", CREATURE_TYPE_HUMANOID, SIZE_LARGE, 30, 0, 10, 5, 9, spells(),
				protections());
		addType("innkeeper", CREATURE_TYPE_HUMANOID, SIZE_MEDIUM, 30, 0, 10, 5, 9, spells(),
				protections());
		addType("bear", CREATURE_TYPE_ANIMAL, SIZE_LARGE, 30, 0, 10, 5, 9, spells(),
				protections());
	}
	public static void addType(String type, int creatureType, int size, int hp, int mp, int speed, int armor, int evasion,
			ArrayList<Integer> spells, ArrayList<Integer> protections) {
		types.put(type, new CharacterType(creatureType, size, hp, mp, speed, armor, evasion,
				spells, protections));
	}
	public static CharacterType getType(String type) {
		return types.get(type);
	}
	public static ArrayList<Integer> spells(int... ids) {
		ArrayList<Integer> answer = new ArrayList<Integer>();
		for (int spellId : ids) {
			answer.add(spellId);
		}
		return answer;
	}
	public static ArrayList<Integer> protections(int... args) {
		ArrayList<Integer> answer = new ArrayList<Integer>(NUMBER_OF_PROTECTIONS);
		int len = (int) Math.floor(args.length/2);
		for (int i=0; i<NUMBER_OF_PROTECTIONS; i++) {
			answer.add(0);
		}
		for (int i=0; i<len; i++) {
			answer.set(args[i*2], args[i*2+1]);
		}
		return answer;
	}
	public static void jsonTypes() {
		String data = "characterTypes = {\n";
		int i=0;
		
		String[] keys = types.keySet().toArray(new String[0]);
		int iterations = keys.length - 1;
		for (;i<iterations;i++) {
			String typeName = keys[i];
			CharacterType type = types.get(typeName);
			data += "\t\""+typeName+"\":[\""+Language.getCharacterName(typeName)+"\", "+type.hp+", "+type.mp+"], \n";
		}
		String typeName = keys[i];
		CharacterType type = types.get(typeName);
		data += "\t\""+typeName+"\":[\""+Language.getCharacterName(typeName)+"\", "+type.hp+", "+type.mp+"]\n};";
		
		Main.outln(data);
	}
	
}
