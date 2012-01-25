package erpoge.serverevents;

public class EventJump extends ServerEvent {
	public static final String e = "jump";
	public int characterId;
	public EventJump(int characterId) {
		this.characterId = characterId;
	}
}
