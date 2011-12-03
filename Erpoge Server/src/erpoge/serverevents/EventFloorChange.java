package erpoge.serverevents;

public class EventFloorChange extends ServerEvent {
	public static final String e = "floorChange";
	public int floor;
	public int x;
	public int y;
	public EventFloorChange(int floor, int x, int y) {
		this.floor = floor;
		this.x = x;
		this.y = y;
	}
}
