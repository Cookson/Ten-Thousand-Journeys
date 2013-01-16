package erpoge.core.net.serverevents;

public class EventCastSpell extends ServerEvent {
	public static final String e = "castSpell";
	public int characterId;
	public int spellId;
	public int x;
	public int y;
	public EventCastSpell(int characterId, int spellId, int x, int y) {
		this.characterId = characterId;
		this.spellId = spellId;
		this.x = x;
		this.y = y;
	}
}
