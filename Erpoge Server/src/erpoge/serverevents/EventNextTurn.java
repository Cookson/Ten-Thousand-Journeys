package erpoge.serverevents;

public class EventNextTurn extends ServerEvent {
	public static final String e = "nextTurn";
	public int characterId;
	public EventNextTurn(int characterId) {
		this.characterId = characterId;
	}
}
