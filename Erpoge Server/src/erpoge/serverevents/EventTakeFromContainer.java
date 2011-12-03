package erpoge.serverevents;

public class EventTakeFromContainer extends ServerEvent {
	public static final String e = "takeFromContainer";
	public int characterId;
	public int typeId;
	public int amount;
	public int x;
	public int y;
	public EventTakeFromContainer(int characterId, int typeId, int amount, int x, int y) {
		this.characterId = characterId;
		this.typeId = typeId;
		this.amount = amount;
		this.x = characterId;
		this.y = characterId;
	}
}
