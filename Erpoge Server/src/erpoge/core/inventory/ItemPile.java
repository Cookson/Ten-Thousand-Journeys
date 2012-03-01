package erpoge.core.inventory;

public class ItemPile extends Item {
	private int amount;
	public ItemPile(int typeId, int amount) {
		super(typeId);
		this.amount = amount;
	}
	public int changeAmount(int amount) {
		if (this.amount + amount <= 0) {
			throw new Error("Item's amount decreased by more than this item contained ("+this.amount+" - "+amount+")");
		}
		this.amount += amount;
		return this.amount;
	}
	public int getAmount() {
		return amount;
	}
	public int setAmount(int amount) {
		this.amount = amount;
		return amount;
	}
	public int hashCode() {
		return type.getTypeId()*100000+amount;
	}
	public ItemPile separatePile(int amount) {
		return new ItemPile(type.getTypeId(), amount);
	}
	@Override
	public String toString() {
		return amount+" "+type.getName();
	}
	@Override
	public String toJson() {
			return "["+type.getTypeId()+","+amount+"]";
	}
	public static ItemPile createPileFromClass(int classId, int indexInClass, int amount) {
		return new ItemPile(classId*CLASS_LENGTH+indexInClass, amount);
	}
	@Override
	public int getParam() {
		return amount;
	}
}
