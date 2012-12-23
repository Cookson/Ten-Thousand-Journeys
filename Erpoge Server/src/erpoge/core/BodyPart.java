package erpoge.core;
/**
 * Represents an abstract body part, either on a living character or seperate from him.
 * 
 * @see LivingBodyPart
 */
public class BodyPart {
	private static int lastId = 0;
	final BodyPartType type;
	final int id;
	BodyPart(BodyPartType type) {
		this.id = ++lastId;
		this.type = type;
	}
	public int getId() {
		return id;
	}
}
