package erpoge.core;

/**
 * Used to store BodyPartTypes in data structures that search for elements by
 * their hashCodes, for example in graphs. This type is used because
 * {@link BodyPartType} is enum and can't be instantiated
 */
public class BodyPartTypeInstance {
	private static int lastId = 0;
	final BodyPartType type;
	final int id;
	BodyPartTypeInstance(BodyPartType type) {
		this.type = type;
		this.id = ++lastId;
	}
	public int hashCode() {
		return id;
	}
	public String toString() {
		return ""+type+"("+id+")";
	}
	public BodyPartType getType() {
		return type;
	}
}
