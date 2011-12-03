package erpoge.clientmessages;

public class ServerMessageCharacterAuthInfo {
	public int characterId;
	public String name;
	public int race;
	public String cls;
	public int level;
	public int[] ammunition;

	public ServerMessageCharacterAuthInfo() {

	}

	public ServerMessageCharacterAuthInfo(int characterId, String name,
			String cls, int race, int level, int[] ammunition) {
		this.characterId = characterId;
		this.name = name;
		this.cls = cls;
		this.race = race;
		this.level = level;
		this.ammunition = ammunition;
	}
}
