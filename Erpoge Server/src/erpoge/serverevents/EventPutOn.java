package erpoge.serverevents;

public class EventPutOn extends ServerEvent {
	public static final String e = "putOn";
	public int itemId;
	public int characterId;
	public EventPutOn(int characterId, int itemId) {
		this.itemId = itemId;
		this.characterId = characterId;
	}
}
