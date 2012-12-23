package erpoge.core.net.serverevents;

import erpoge.core.characters.CharacterState;

public class EventEnterState extends ServerEvent {
	public static final String e = "enterState";
	public int characterId;
	public int stateId;
	public EventEnterState(int characterId, CharacterState state) {
		this.characterId = characterId;
		this.stateId = state.state2int();
	}
}
