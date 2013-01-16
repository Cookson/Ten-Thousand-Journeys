package erpoge.core.net.serverevents;

public class EventUseObject extends ServerEvent {
	public static final String e = "useObject";
	public int characterId;
	public int x;
	public int y;
	public EventUseObject(int characterId, int x, int y) {
		this.characterId = characterId;
		this.x = x;
		this.y = y;
	}
}
