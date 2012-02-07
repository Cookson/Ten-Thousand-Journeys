package erpoge.core.net.serverevents;

public class EventDialogueEnd extends ServerEvent {
	public static final String e = "dialogueEnd";
	public int characterId;
	public EventDialogueEnd(int characterId) {
		this.characterId = characterId;
	}
}
