package erpoge.serverevents;

public class EventDeath extends ServerEvent {
	public static final String e = "death";
	public int characterId;
	public EventDeath(int characterId) {
		this.characterId = characterId;
	}
}
