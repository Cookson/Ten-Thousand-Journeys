package erpoge.core.net.serverevents;

public class EventSoundSourceAppear extends ServerEvent {
	public static final String e = "soundSourceAppear";
	public int x;
	public int y;
	public int type;
	public EventSoundSourceAppear(int type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}
}
