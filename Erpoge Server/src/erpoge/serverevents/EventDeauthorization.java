package erpoge.serverevents;

public class EventDeauthorization extends ServerEvent {
	public static final String e = "deauth";
	public int characterId;
	public EventDeauthorization(int characterId) {
		this.characterId = characterId;
	}
}
