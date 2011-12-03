package erpoge.characters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import erpoge.Main;
import erpoge.characters.Character;

public class TurnQueue {
	public static final int BASE_ENERGY = 500;
	public HashMap<Integer, Character> characters;
	public TurnQueue(HashMap<Integer, Character> characters) {
		this.characters = characters;
	}
	public Character next() {
		ArrayList<Character> values = new ArrayList<Character>(characters.values());
		Character nextCharacter = values.get(0);
		for (Character ch : values) {
			if (ch.energy > nextCharacter.energy) {
				nextCharacter = ch;
			}
		}
		if (nextCharacter.energy <= 0) {
			for (Character ch : values) {
				ch.energy += BASE_ENERGY;
			}
			return next();
		}
		return nextCharacter;
	}
}
