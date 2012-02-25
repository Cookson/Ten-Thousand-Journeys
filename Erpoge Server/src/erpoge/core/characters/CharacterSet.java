package erpoge.core.characters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import erpoge.core.Character;
import erpoge.core.Main;


public class CharacterSet {
	private HashMap<Integer, Character> characters;
	public CharacterSet(List<Character> pls) {
		for (Character p : pls) {
			characters.put(p.characterId, p);
		}
	}
	public CharacterSet() {
		characters = new HashMap<Integer, Character>();
	}
	public Character get(int characterId) {
		return characters.get(characterId);
	}
//	@SuppressWarnings("unchecked")
	public <T extends Character> T getByName(String name) {
		Collection<Character> values = characters.values();
		for (Character ch : values) {
			if (ch.name.equals(name)) {
				return (T) ch;
			}
		}
		throw new Error("Character map has no character named "+name);
	}
	public void add(Character character) {
		/*
		 * Adds a characted to the set and sets his characterId
		 */
		characters.put(character.characterId, character);
	}
	public void remove(Character character) {
		characters.remove(character.characterId);
	}
	public Collection<PlayerCharacter> getPlayers() {
		Collection<PlayerCharacter> players = new ArrayList<PlayerCharacter>();
		for (Character ch : characters.values()) {
			if (ch instanceof PlayerCharacter) {
				players.add((PlayerCharacter)ch);
			}
		}
		return players;
	}
	public Collection<Character> getCharacters() {
		Collection<Character> chars = new ArrayList<Character>();
		for (Character ch : characters.values()) {
			chars.add(ch);
		}
		return chars;
	}
}
