package erpoge.serverevents;

public class EventItemDisappear extends ServerEvent {
	public static final String e = "itemDisappear";
	public int typeId;
	public int param;
	public int x;
	public int y;
	public EventItemDisappear(int typeId, int param, int x, int y) {
		this.typeId = typeId;
		this.param = param;
		this.x = x;
		this.y = y;
	}
}
