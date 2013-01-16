package erpoge.core;

public class AspectContainer extends Aspect {
	double volume;
	boolean liquidAllowing;
	public AspectContainer(double volume, boolean liquidAllowing) {
		super(AspectName.CONTAINER);
		this.volume = volume;
		this.liquidAllowing = liquidAllowing;
	}
	public String toString() {
		return "As container:\n"
			+"Volume is "+volume+" cm3\n"
			+(liquidAllowing ? "liquid-allowing" : "non liquid-allowing");
	}
	public double getVolume() {
		return volume;
	}
}
