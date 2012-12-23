package erpoge.core;
/**
 * Aspects determine how {@link ItemType}s may be used in game. For example, aspect {@link AspectApparel}
 * allows characters to wear an item, and {@link AspectContainer} allows storing other item
 * inside this item. An ItemType may have several aspects at the same time or not have any aspects at all.
 * Other than just being a flag for allowing certain operations with item, Aspects
 * are containers for properties that describe the mechanics of this particular ItemType.
 * For example, two pieces of apparel may have different form (a jacket is worn on torso, and
 * a hat is worn on head), and each instance of Aspect 
 *
 */
public abstract class Aspect {
	protected AspectName name;
	public Aspect(AspectName name) {
		this.name = name;
	}
	public AspectName getName() {
		return name;
	}
}
