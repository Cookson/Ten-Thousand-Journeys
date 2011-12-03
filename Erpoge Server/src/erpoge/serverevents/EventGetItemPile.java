package erpoge.serverevents;

public class EventGetItemPile extends ServerEvent {
	public static final String e = "getItem";
	public int characterId;
	public int typeId;
	public int param;
	public EventGetItemPile(int characterId, int typeId, int param) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.param = param;
	}
}
