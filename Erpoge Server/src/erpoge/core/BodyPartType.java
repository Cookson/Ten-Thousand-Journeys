package erpoge.core;

enum BodyPartType {
	HEAD(1),
	TORSO(2),
	HAND(3),
	GRIP(4),
	LEG(5),
	ARM(6),
	EAR(7),
	FOOT(8),
	FINGER(9),
	NECK(10),
	TENTACLE(11),
	TAIL(12),
	EYE(13),
	MOUTH(14),
	NOSE(15),
	TEETH(16),
	TONGUE(17);
	/**
	 * An exclusive identivication number which is equal in client and server
	 * used to identify a type of body part (but int a particular body part
	 * af a particular character!).
	 */
	final int id;
	BodyPartType(int id) {
		this.id = id;
	}
	public static BodyPartType string2BodyPart(String value) {
		if (value.equals("head")) {
			return BodyPartType.HEAD;
		} else if (value.equals("torso")) {
			return BodyPartType.TORSO;
		} else if (value.equals("hand")) {
			return BodyPartType.HAND;
		} else if (value.equals("grip")) {
			return BodyPartType.GRIP;
		} else if (value.equals("leg")) {
			return BodyPartType.LEG;
		} else if (value.equals("arm")) {
			return BodyPartType.ARM;
		} else if (value.equals("ear")) {
			return BodyPartType.EAR;
		} else if (value.equals("foot")) {
			return BodyPartType.FOOT;
		} else if (value.equals("finger")) {
			return BodyPartType.FINGER;
		} else if (value.equals("neck")) {
			return BodyPartType.NECK;
		} else if (value.equals("tentacle")) {
			return BodyPartType.TENTACLE;
		} else if (value.equals("tail")) {
			return BodyPartType.TAIL;
		} else if (value.equals("eye")) {
			return BodyPartType.EYE;
		} else if (value.equals("mouth")) {
			return BodyPartType.MOUTH;
		} else if (value.equals("nose")) {
			return BodyPartType.NOSE;
		} else if (value.equals("teeth")) {
			return BodyPartType.TEETH;
		} else if (value.equals("tongue")) {
			return BodyPartType.TONGUE;
		} else {
			throw new Error("Wrong body part name `"+value+"`");
		}
	}
	public String toString() {
		return super.toString().toLowerCase();
	}
	/**
	 * Returns an id of this body part type.
	 * 
	 * @return 
	 */
	public int getId() {
		return id;
	}
	/**
	 * Gets body tree from XML description of that tree.
	 */
}
