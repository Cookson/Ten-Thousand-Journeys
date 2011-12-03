package erpoge.serverevents;

public class EventWorldEntering extends ServerEvent {
	public static final String e = "we";
	public int characterId;
	public String name;
	public String cls;
	public int race;
	public int party;
	public int worldX;
	public int worldY;
	public EventWorldEntering(int characterId, String name, String cls, int race, int worldX, int worldY) {
		this.characterId = characterId;
		this.name = name;
		this.cls = cls;
		this.race = race;
		this.worldX = worldX;
		this.worldY = worldY;
	}
}
