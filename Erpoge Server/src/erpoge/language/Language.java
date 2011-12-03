package erpoge.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import erpoge.Main;

public abstract class Language {
	public static Language instance = new Russian();
	public final HashMap<String, ArrayList<String>> characterNames = new HashMap<String, ArrayList<String>>();
	public final HashMap<String, ArrayList<String>> itemNames = new HashMap<String, ArrayList<String>>();
	public final HashMap<String, ArrayList<String>> objectNames = new HashMap<String, ArrayList<String>>();
	public final HashMap<String, String> books = new HashMap<String, String>();
	protected final HashMap<Integer, ArrayList<String>> cityNames = new HashMap<Integer, ArrayList<String>>();
	public int NUMBER_OF_CASES;
	public String LANGUAGE_NAME;
	
	public void addCharacterName(String type, String... names) {
		characterNames.put(type, new ArrayList<String>(Arrays.asList(names)));
	}
	public static String getCharacterName(String type) {
		return instance.characterNames.get(type).get(0);
	}
	public void addItemName(String type, String... names) {
		itemNames.put(type, new ArrayList<String>(Arrays.asList(names)));
	}
	public String getItemName(String type) {
		return instance.itemNames.get(type).get(0);
	}
	public void addObjectName(String type, String... names) {
		objectNames.put(type, new ArrayList<String>(Arrays.asList(names)));
	}
	public String getObjectName(String type) {
		return instance.objectNames.get(type).get(0);
	}
	public void addCityName(int race, String name) {
		cityNames.get(race).add(name);
	}
	public static void checkLanguage(Language language) {
		Language english = new English();
		Main.outln("Checking language "+language.LANGUAGE_NAME);
		for (String name : english.characterNames.keySet()) {
			if (!language.characterNames.containsKey(name)) {
				Main.outln("No translation for character name "+name);
			}
		}
		if (english.characterNames.keySet().size() < language.characterNames.keySet().size()) {
			for (String name : language.characterNames.keySet()) {
				if (!english.characterNames.containsKey(name)) {
					Main.outln(language.LANGUAGE_NAME+" character names contain odd names for type "+name);
				}
			}
		}
	}
}
