package erpoge.core.net.serverevents;

public class EventChangePlaces extends ServerEvent {
	public static final String e = "changePlaces";
	public int character1Id;
	public int character2Id;
	public EventChangePlaces(int character1Id, int character2Id) {
		this.character1Id = character1Id;
		this.character2Id = character2Id;
	}
}
