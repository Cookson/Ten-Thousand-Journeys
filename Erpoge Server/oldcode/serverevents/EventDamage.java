package erpoge.core.net.serverevents;

public class EventDamage extends ServerEvent {
	public static final String e = "damage";
	public int characterId;
	public int amount;
	public int type;
	public EventDamage(int characterId, int amount, int type) {
		this.characterId = characterId;
		this.amount = amount;
		this.type = type;
	}
}
