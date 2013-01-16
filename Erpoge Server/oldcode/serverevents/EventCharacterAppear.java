package erpoge.core.net.serverevents;

public class EventCharacterAppear extends ServerEvent {
	public static final String e = "characterAppear";
	public int characterId;
	public int x;
	public int y;
	public String type;
	public String name;
	public int maxHp;
	public int hp;
	public int maxMp;
	public int mp;
	public int[] effects;
	public int[][] equipment;
	public int fraction;
	public EventCharacterAppear(int characterId, int x, int y, int characterTypeId, String name, 
			int[] effects, int[][] equipment, int fraction) {
		this.characterId = characterId;
		this.x = x;
		this.y = y;
		this.name = name;
		this.effects = effects;
		this.equipment = equipment;
		this.fraction = fraction;
	}
}
