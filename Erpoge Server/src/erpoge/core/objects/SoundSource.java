package erpoge.core.objects;

public class SoundSource extends Sound {
	public int lifetime;
	public SoundSource(int x, int y, SoundType type, int lifetime) {
		super(x, y, type);
		this.lifetime = lifetime;
	}
}
