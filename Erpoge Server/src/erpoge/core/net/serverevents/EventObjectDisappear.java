package erpoge.core.net.serverevents;

public class EventObjectDisappear extends ServerEvent {
	public static final String e = "objectDisappear";
	public int x;
	public int y;
	public EventObjectDisappear(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
