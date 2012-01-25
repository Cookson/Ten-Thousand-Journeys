package erpoge.itemtypes;

public enum Attribute {
	ARMOR, HP, MP, ENERGY, ACTION_POINTS, MAX_HP, MAX_MP, ATTACK_POWER, STEALTH, HEARING, EYESIGHT, EVASION, SPEED;
	public int attr2int() {
		switch (this) {
		case ARMOR:         return 1;
		case EVASION:       return 2;
		case HP:            return 3;
		case MP:            return 4;
		case MAX_HP:        return 5;
		case MAX_MP:        return 6;
		case ENERGY:        return 7;
		default:
			throw new Error("Unknown attribute");
		}
	}
}
