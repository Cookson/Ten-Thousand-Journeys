package erpoge.core.meta;
/**
 * Enum for representing horizontal or vertical direction
 */
public enum Direction {
	H,V;
	public Direction reverted() {
	/**
	 * Return perpendicular direction
	 */
		if (this == H) {
			return V;
		} else {
			return H;
		}
	}
	public boolean isH() {
		return this == H;
	}
	public boolean isV() {
		return this == V;
	}
	public static Direction bool2dir(boolean bool) {
	/**
	 * Returns Direction.V if bool == true, otherwise returns Direction.H.
	 */
		return bool ? V : H;
	}
}
