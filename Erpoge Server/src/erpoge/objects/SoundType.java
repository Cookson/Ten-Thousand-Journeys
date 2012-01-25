package erpoge.objects;

public enum SoundType {

	ROAR,
	SCREAM,
	STEPS,
	WATER_FLOW,
	CRASH,
	LIGHTNING,
	SPEECH,
	WORKING_MECHANISMS,
	BELL;
	public int type2int() {
		switch (this) {
		case ROAR:
			return 1;
		case SCREAM:
			return 2;
		case STEPS:
			return 3;
		case WATER_FLOW:
			return 4;
		case CRASH:
			return 5;
		case LIGHTNING:
			return 6;
		case SPEECH:
			return 7;
		case WORKING_MECHANISMS:
			return 8;
		case BELL:
			return 9;
		default:
			throw new Error("Unknown sound");
		}
	}

	public static SoundType int2type(int type) {
		// TODO Auto-generated method stub
		switch (type) {
		case 1:
			return ROAR;
		case 2:
			return SCREAM;
		case 3:
			return STEPS;
		case 4:
			return WATER_FLOW;
		case 5:
			return CRASH;
		case 6:
			return LIGHTNING;
		case 7:
			return SPEECH;
		case 8:
			return WORKING_MECHANISMS;
		case 9:
			return BELL;
		default:
			throw new Error("Unknown sound type "+type);
		}
	}
}
