package erpoge.core.characters;

public enum DamageType {
	PLAIN,
	FIRE,
	COLD,
	POISON,
	MENTAL,
	ELECTRICITY,
	ACID;
	public int type2int() {
		switch (this) {
		case PLAIN:
			return 1;
		case FIRE:
			return 2;
		case COLD:
			return 3;
		case POISON:
			return 4;
		case MENTAL:
			return 5;
		case ELECTRICITY:
			return 6;
		case ACID:
			return 7;
		default:
			throw new Error("Unknown damage type");
		}
	}
	public static DamageType int2type(int type) {
		switch (type) {
		case 1:
			return PLAIN;
		case 2:
			return FIRE;
		case 3:
			return COLD;
		case 4:
			return POISON;
		case 5:
			return MENTAL;
		case 6:
			return ELECTRICITY;
		case 7:
			return ACID;
		default:
			throw new Error("Unknown damage type");
		}
	}
}
