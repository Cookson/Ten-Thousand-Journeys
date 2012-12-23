package erpoge.core.net.clientmessages;

public class ServerMessageCharacterAuthInfo {
	public int characterId;
	public String name;
	public int race;
	public String cls;
	public int level;
	public int[] equipment;

	public ServerMessageCharacterAuthInfo() {

	}

	public ServerMessageCharacterAuthInfo(int characterId, String name,
			String cls, int race, int level, int[] equipment) {
		this.characterId = characterId;
		this.name = name;
		this.cls = cls;
		this.race = race;
		this.level = level;
		this.equipment = equipment;
	}
}
