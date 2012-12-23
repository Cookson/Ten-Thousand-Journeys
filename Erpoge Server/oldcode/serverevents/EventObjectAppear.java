package erpoge.core.net.serverevents;

public class EventObjectAppear extends ServerEvent {
	public static final String e = "objectAppear";
	public int object;
	public int x;
	public int y;
	public EventObjectAppear(int object, int x, int y) {
		this.object = object;
		this.x = x;
		this.y = y;
	}
}
