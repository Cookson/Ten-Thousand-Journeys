package erpoge.core;

/**
 * Manages ids of obejcts and makes sure that none of two objects inheriting
 * from UniqueObject hasve the same id.
 */
public abstract class UniqueObject {
	private static int lastId = 0;
	protected final int id;

	UniqueObject() {
		id = ++lastId;
	}

	public int getId() {
		return id;
	}
	public int hashCode() {
		return id;
	}
}
