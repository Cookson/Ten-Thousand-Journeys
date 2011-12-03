package erpoge.serverevents;

public class EventOpenContainer extends ServerEvent {
	public static final String e = "openContainer";
	public int containerId;
	public int[][] items; 
	public EventOpenContainer(int containerId, int[][] items) {
		this.containerId = containerId;
		this.items = items;
	}
}
