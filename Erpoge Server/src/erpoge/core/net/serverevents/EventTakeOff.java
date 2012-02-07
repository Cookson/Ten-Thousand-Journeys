package erpoge.core.net.serverevents;

public class EventTakeOff extends ServerEvent {
	public static final String e = "takeOff";
	public int itemId;
	public int characterId;
	public EventTakeOff(int characterId, int itemId) {
		this.itemId = itemId;
		this.characterId = characterId;
	}
}
