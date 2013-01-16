package erpoge.core.net.serverevents;

public class EventMissileFlight extends ServerEvent {
	public static final String e = "missileFlight";
	public int fromX;
	public int fromY;
	public int toX;
	public int toY;
	public int missile;
	public EventMissileFlight(int fromX, int fromY, int toX, int toY, int missile) {
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
		this.missile = missile;
	}
}
