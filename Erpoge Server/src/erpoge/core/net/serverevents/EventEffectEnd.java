package erpoge.core.net.serverevents;

public class EventEffectEnd extends ServerEvent {
	public static final String e = "effectEnd";
	public int characterId;
	public int effectId;
	public EventEffectEnd(int characterId, int effectId) {
		this.characterId = characterId;
		this.effectId = effectId;
	}
}
