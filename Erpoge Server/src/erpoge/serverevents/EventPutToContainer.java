package erpoge.serverevents;

public class EventPutToContainer extends ServerEvent {
	public static final String e = "putToContainer";
	public int characterId;
	public int itemId;
	public int amount;
	public int x;
	public int y;
	public EventPutToContainer(int characterId, int itemId, int amount, int x, int y) {
		this.characterId = characterId;
		this.itemId = itemId;
		this.amount = amount;
		this.x = characterId;
		this.y = characterId;
	}
}
