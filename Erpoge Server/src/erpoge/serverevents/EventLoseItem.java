package erpoge.serverevents;

public class EventLoseItem extends ServerEvent {
	public static final String e = "loseItem";
	public int characterId;
	public int typeId;
	public int param;
	public EventLoseItem(int characterId, int typeId, int param) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.param = param;
	}
}
