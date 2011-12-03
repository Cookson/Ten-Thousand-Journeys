package erpoge.serverevents;

public class EventSoundSourceDisappear extends ServerEvent {
	public static final String e = "soundSourceDisappear";
	public int x;
	public int y;
	public int type;
	public EventSoundSourceDisappear(int type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}
}
