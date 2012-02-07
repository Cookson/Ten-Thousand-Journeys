package erpoge.core.net.serverevents;

public class EventMove extends ServerEvent {
	public static final String e = "move";
	public int characterId;
	public int x;
	public int y;
	public EventMove(int characterId, int x, int y) {
		this.characterId = characterId;
		this.x = x;
		this.y = y;
	}
}
