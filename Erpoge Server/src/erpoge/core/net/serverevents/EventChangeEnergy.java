package erpoge.core.net.serverevents;

public class EventChangeEnergy extends ServerEvent {
	public static final String e = "changeEnergy";
	public int characterId;
	public int value;
	public EventChangeEnergy(int characterId, int value) {
		this.characterId = characterId;
		this.value = value;
	}
}