package erpoge.itemtypes;

public class Useable extends ItemType {
	public Useable(String name, int cls, int weight, int price, int material) {
		super(name, cls, weight, price, material);
		unique = false;
	}
	public String jsonPartTypology() {
	// Get a json string that describes this item type
	// Out: [name,cls,weight,price]
		return "[\""+name+"\", "+cls+", "+weight+","+price+","+material+"]";
	}
}
