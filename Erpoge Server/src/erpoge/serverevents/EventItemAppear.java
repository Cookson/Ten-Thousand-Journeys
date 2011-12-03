package erpoge.serverevents;

public class EventItemAppear extends ServerEvent {
	public static final String e = "itemAppear";
	public int typeId;
	public int param;
	public int x;
	public int y;
	public EventItemAppear(int typeId, int param, int x, int y) {
		this.typeId = typeId;
		this.param = param;
		this.x = x;
		this.y = y;
	}
}
