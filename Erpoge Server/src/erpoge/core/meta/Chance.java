package erpoge.core.meta;

public class Chance {
	/**
	 * An object used to return true with particular probability. For example,
	 * new Chance(30).roll() will return true with 30 percent probability.
	 */
	private final int value;

	public Chance(int val) {
		value = val;
	}

	/**
	 * Get true of false.
	 * 
	 * @return true with probability this.value%
	 */
	public boolean roll() {
		return Math.random() * 100 < value;
	}

	public static boolean roll(int value) {
		return Math.random() * 100 < value;
	}

	/**
	 * Returns a random int from between two ints inclusive.
	 * @param a
	 *            min value
	 * @param b
	 *            max value
	 * @return true with probability this.value%
	 */
	public static int rand(int a, int b) {
		if (a > b) {
			throw new IllegalArgumentException();
		}
		return Math.min(a, b) + (int) Math.round(Math.random() * Math.abs(b - a));
	}
	/**
	 * Returns a random long from between two longs inclusive.
	 * @param a
	 *            min value
	 * @param b
	 *            max value
	 * @return true with probability this.value%
	 */
	public static long rand(long a, long b) {
		return Math.min(a, b) + (long) Math.round(Math.random() * Math.abs(b - a));
	}
}
