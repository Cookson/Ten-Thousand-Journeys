package erpoge.serverevents;

public class EventPickUp extends ServerEvent {
	public static final String e = "pickUp";
	public int characterId;
	public int typeId;
	public int param;
	public EventPickUp(int characterId, int typeId, int param) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.param = param;
	}
}
