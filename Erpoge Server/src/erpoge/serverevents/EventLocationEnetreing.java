package erpoge.serverevents;

public class EventLocationEnetreing extends ServerEvent {
	// characterId,name,class,race,party,worldX,worldY
	public static final String e = "we";
	public int characterId;
	public String name;
	public String cls;
	public int race;
	public int party;
	public int x;
	public int y;
	public EventLocationEnetreing(int characterId, String name, String cls, int race, int x, int y) {
		this.characterId = characterId;
		this.name = name;
		this.cls = cls;
		this.race = race;
		this.x = x;
		this.x = y;
	}
}
