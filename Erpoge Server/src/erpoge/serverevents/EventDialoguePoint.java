package erpoge.serverevents;

public class EventDialoguePoint extends ServerEvent {
	public static final String e = "dialoguePoint";
	public int npcId;
	public int playerId;
	public String phrase;
	public String[] answers;
	public EventDialoguePoint(int npcId, int playerId, String phrase, String[] answers) {
		this.npcId = npcId;
		this.playerId = playerId;
		this.phrase = phrase;
		this.answers = answers;
	}
}
