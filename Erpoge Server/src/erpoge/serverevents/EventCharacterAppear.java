package erpoge.serverevents;

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
	public int[][] ammunition;
	public int fraction;
	public EventCharacterAppear(int characterId, int x, int y, String type, String name, int maxHp,
			int hp, int maxMp, int mp, int[] effects, int[][] ammunition, int fraction) {
		this.characterId = characterId;
		this.x = x;
		this.y = y;
		this.type = type;
		this.name = name;
		this.maxHp = maxHp;
		this.hp = hp;
		this.maxMp = maxMp;
		this.mp = mp;
		this.effects = effects;
		this.ammunition = ammunition;
		this.fraction = fraction;
	}
}
