package erpoge.itemtypes;

public class Armor extends ItemType {
	public final int ac;
	public final int ev;

	public Armor(String name, int cls, int weight, int price, int material, int ac, int ev) {
		super(name, cls, weight, price, material);
		this.ac = ac;
		this.ev = ev;
		this.unique = true;
	}
	public String jsonPartTypology() {
	// Get a json string that describes this item type
		// Out: [name,cls,weight,price, ac, ev]
		return "[\""+name+"\", "+cls+", "+weight+","+price+","+material+","+ac+","+ev+"]";
	}
}
