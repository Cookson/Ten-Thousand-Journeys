package erpoge.core;

import java.util.HashSet;
import java.util.Iterator;
public class Account {
	final String login;
	final String password;
	final HashSet<PlayerCharacter> characters = new HashSet<PlayerCharacter>();

	public Account(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public void addCharacter(PlayerCharacter character) {
		characters.add(character);
	}

	public void removeCharacter(PlayerCharacter character) {
		characters.remove(character);
	}
	
	public void accountStatistic() {
		Main.log("Characters on account "+login+" "+this);
		for (PlayerCharacter player : characters) {
			Main.log(player.name+" "+player.getCls());
		}
	}

	public PlayerCharacter character(int id) {
		Iterator<PlayerCharacter> it = characters.iterator();
		while (it.hasNext()) {
			PlayerCharacter next = it.next();
			if (next.getId() == id) {
				return next;
			}
		}
		throw new Error("Character with id " + id + " not found in account "
				+ login);
	}
	public boolean hasCharacterWithId(int characterId) {
		Iterator<PlayerCharacter> it = characters.iterator();
		while (it.hasNext()) {
			PlayerCharacter next = it.next();
			if (next.getId() == characterId) {
				return true;
			}
		}
		return false;
	}
	public String[] getCharacterNames() {
		String[] names = new String[characters.size()];
		int i=0;
		Iterator<PlayerCharacter> it = characters.iterator();
		while (it.hasNext()) {
			names[i++] = it.next().name;
		}
		return names;
	}
	
}
