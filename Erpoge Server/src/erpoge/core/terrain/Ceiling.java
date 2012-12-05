package erpoge.core.terrain;

import java.awt.Rectangle;

public class Ceiling extends Rectangle {
	private static final long serialVersionUID = 5258255478966149057L;
	public final int type;
	public Ceiling(Rectangle rectangle, int type) {
		super(rectangle);
		this.type = type;
	}
}
