package erpoge.serverevents;

public class EventSound extends ServerEvent {
	public static final String e = "sound";
	public int x;
	public int y;
	public int type;
	public EventSound(int type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}
}
