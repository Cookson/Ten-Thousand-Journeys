package erpoge;

import java.util.HashSet;
import java.util.Iterator;
import erpoge.characters.Character;
import erpoge.characters.PlayerCharacter;
import erpoge.clientmessages.ServerMessageCharacterAuthInfo;
public class Account {
	public final String login;
	public final String password;
	public final HashSet<PlayerCharacter> characters = new HashSet<PlayerCharacter>();

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
		Main.console("Characters on account "+login+" "+this);
		for (PlayerCharacter ch : characters) {
			Main.console(ch.name+" "+ch.cls());
		}
	}

	public PlayerCharacter character(int id) {
		Iterator<PlayerCharacter> it = characters.iterator();
		while (it.hasNext()) {
			PlayerCharacter next = it.next();
			if (next.characterId == id) {
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
			if (next.characterId == characterId) {
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
	public String jsonPartGetCharactersAuthInfo() {
	// Get json data for each character on this account to show these characters on login.
	/*
	 * out: {
	 * 	"players:[{characterId, name, race, class, level, ammunition}xN]"
	 * }
	 */
		String answer = "\"players\":[";
		int i=0;
		PlayerCharacter[] values = characters.toArray(new PlayerCharacter[0]);
		int iterations = values.length - 1;
		for (;i <= iterations;i++) {
			answer += 	"["+values[i].characterId+
						",\""+values[i].name+
						"\",\""+values[i].cls()+
						"\","+values[i].race+
						","+values[i].level()+",[";
			int[] ammunition = values[i].ammunition.getIntArray();
			int j=ammunition.length-1;
			for (;j>=0;j--) {
				if (ammunition[j] != 0) {
					break;
				}
			}
			int k=0;
			for (;k<j;k++) {
				answer += ammunition[k]+",";
			}
			answer += ammunition[k]+"]";
			answer += (i == iterations) ? "]" : "],";
		}
		answer += "]";
		return answer;
	}
}
