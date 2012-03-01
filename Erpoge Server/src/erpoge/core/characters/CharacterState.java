package erpoge.core.characters;

public enum CharacterState {
	DEFAULT, RUNNING, SNEAKING, SLEEPING, AIMING;
	public int state2int() {
		switch (this) {
		case DEFAULT:
			return 0;
		case RUNNING:
			return 1;
		case SNEAKING:
			return 2;
		case SLEEPING:
			return 3;
		case AIMING:
			return 4;
		default:
			throw new Error("Unknown state");	
		}
	}
	public static CharacterState int2state(int stateId) {
		switch (stateId) {
		case 0:
			return DEFAULT;
		case 1:
			return RUNNING;
		case 2:
			return SNEAKING;
		case 3:
			return SLEEPING;
		case 4:
			return AIMING;
		default:
			throw new Error("Unknown state id "+stateId);
		}
	}
}
