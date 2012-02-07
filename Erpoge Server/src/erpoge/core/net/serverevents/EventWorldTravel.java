package erpoge.core.net.serverevents;

public class EventWorldTravel extends ServerEvent {
	public static final String e = "wt";
	public int x;
	public int y;
	public int characterId;
	public EventWorldTravel(int x, int y, int id) {
		this.x = x;
		this.y = y;
		characterId = id;
	}
}
