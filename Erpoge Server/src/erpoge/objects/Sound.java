package erpoge.objects;

public class Sound {
	public static final int
		ROAR = 1,
		SCREAM = 2,
		STEPS = 3,
		WATER_FLOW = 4,
		CRASH = 5,
		LIGHTNING = 6,
		SPEECH = 7,
		WORKING_MECHANISMS = 8,
		BELL = 9;
	public int type;
	public int x;
	public int y;
	public Sound(int x, int y, int type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}
}
