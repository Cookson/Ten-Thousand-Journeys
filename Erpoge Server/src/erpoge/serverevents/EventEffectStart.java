package erpoge.serverevents;

public class EventEffectStart extends ServerEvent {
	public static final String e = "effectStart";
	public int characterId;
	public int effectId;
	public EventEffectStart(int characterId, int effectId) {
		this.characterId = characterId;
		this.effectId = effectId;
	}
}
