package erpoge.core;

/**
 * Represents an item or a pile of items occupying a single slot in character's
 * inventory.
 */
public interface Item {
	public ItemType getType();
	public int getParam();
}
