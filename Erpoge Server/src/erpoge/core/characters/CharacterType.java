package erpoge.core.characters;

import java.util.ArrayList;

public class CharacterType {
	public int creatureType;
	public int size;
	public int hp;
	public int mp;
	public int speed;
	public int armor;
	public int evasion;
	public ArrayList<Integer> spells;
	public ArrayList<Integer> protections;
	public boolean isCaster;

	public CharacterType(int creatureType, int size, int hp, int mp, int speed, int armor, int evasion,
			ArrayList<Integer> spells, ArrayList<Integer> protections) {
		this.creatureType = creatureType;
		this.size = size;
		this.hp = hp;
		this.mp = mp;
		this.speed = speed;
		this.armor = armor;
		this.evasion = evasion;
		this.protections = protections;
		this.spells = spells;
		this.isCaster = spells.size() > 0;
	}
}
