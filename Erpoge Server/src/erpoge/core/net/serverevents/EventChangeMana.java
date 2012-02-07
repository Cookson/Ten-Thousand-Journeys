package erpoge.core.net.serverevents;

public class EventChangeMana extends ServerEvent {
	public static final String e = "changeMana";
	public int characterId;
	public int value;
	public EventChangeMana(int characterId, int value) {
		this.characterId = characterId;
		this.value = value;
	}
}