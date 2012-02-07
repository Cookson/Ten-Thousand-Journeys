package erpoge.core.terrain;

import java.awt.Rectangle;

public class Ceiling extends Rectangle {
	public final int type;
	public Ceiling(Rectangle rectangle, int type) {
		super(rectangle);
		this.type = type;
	}
}
