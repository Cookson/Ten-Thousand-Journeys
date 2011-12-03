package erpoge.serverevents;

public class EventChatMessage extends ServerEvent {
	public static final String e = "chm";
	public int characterId;
	public String text;
	public EventChatMessage(int characterId, String text) {
		this.characterId = characterId;
		this.text = text;
	}
}
