package erpoge.core.characters;

public enum Race {
	HUMAN,
	ELF,
	DWARF,
	ORC,
	KOBAIAN;
	public int race2int() {
		switch (this) {
		case HUMAN:
			return 0;
		case ELF:
			return 1;
		case DWARF:
			return 2;
		case ORC:
			return 3;
		case KOBAIAN:
			return 4;
		default:
			throw new Error("Unknown race");
		}
	}
	public static Race int2race(int race) {
		switch (race) {
		case 0:
			return HUMAN;
		case 1:
			return ELF;
		case 2:
			return DWARF;
		case 3:
			return ORC;
		case 4:
			return KOBAIAN;
		default:
			throw new Error("Unknown race");
		}
	}
}
