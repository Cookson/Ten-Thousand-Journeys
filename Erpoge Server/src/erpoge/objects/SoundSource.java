package erpoge.objects;

public class SoundSource extends Sound {
	public int lifetime;
	public SoundSource(int x, int y, int type, int lifetime) {
		super(x, y, type);
		this.lifetime = lifetime;
	}
}
