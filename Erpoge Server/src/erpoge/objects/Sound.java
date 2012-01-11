package erpoge.objects;

public class Sound {
	public SoundType type;
	public int x;
	public int y;
	public Sound(int x, int y, SoundType type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}
}
