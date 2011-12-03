package erpoge.serverevents;

public class EventGetUniqueItem extends ServerEvent {
	public static final String e = "getItem";
	public int characterId;
	public int typeId;
	public int param;
	public EventGetUniqueItem(int characterId, int typeId, int param) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.param = param;
	}
}
