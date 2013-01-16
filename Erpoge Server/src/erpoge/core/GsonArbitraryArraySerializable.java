package erpoge.core;
/**
 * Implementing this interface allows {@link GsonArbitraryArrays} serialize 
 * objects into arrays of arbitrary types, like in example below. Fields of 
 * the serialized object don't have to be primitive types — they may also be any 
 * type that implements GsonArbitraryArraySerializable.
 * 
 * @example
 * // Imagine we need to serialize an instance of this class 
 * class Warrior implements GsonArbitraryArraySerializable {
 *   String weapon;
 *   int age;
 *   boolean isAlive;
 *   Warrior(String weapon, int age, boolean isAlive) {
 *   	this.weapon = weapon;
 *   	this.age = age;
 *   	this.isAlive = isAlive;
 *   }
 *   String[] getFieldOrder() {
 *   	return new String[] { "weapon", "age", "isAlive" };
 *   }
 * }
 * 
 * // And this is how we serialize such object:
 * System.out.println(GsonArbitraryArrays.toJson(new Warrior("sword", 36, true)));
 * // prints ["sword", 36, true]
 */
interface GsonArbitraryArraySerializable {
	/**
	 * Returns an array containing names of fields of a class implementing 
	 * this interface. The order of names in array determines the order of 
	 * values in the serialized array.
	 * @return Names of fields to be serialized into an array.
	 */
	String[] getFieldOrder();
}
