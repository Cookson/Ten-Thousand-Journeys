package erpoge;

import java.awt.Point;

import erpoge.terrain.TerrainBasics;


public class Coordinate extends Point{
	public Coordinate(int x, int y) {
		super(x,y);
	}
	public Coordinate(Coordinate c) {
		super(c.x,c.y);
	}
	public boolean equals(Object o) {
		if (!(o instanceof Coordinate)) {
			return false;
		}
		Coordinate coordinate = (Coordinate) o;
		return coordinate.x == x && coordinate.y == y;
	}
	public String toString() {
		return x+":"+y;
	}
	public boolean isNear(int x, int y) {
		int ableX=Math.abs(this.x-x);
		int ableY=Math.abs(this.y-y);
		if ((ableX==1 && ableY==0) || (ableY==1 && ableX==0) || (ableY==1 && ableX==1)) {
			return true;
		}
		return false;
	}
	public static boolean isNear(int startX, int startY, int endX, int endY) {
		int ableX=Math.abs(startX-endX);
		int ableY=Math.abs(startY-endY);
		if ((ableX==1 && ableY==0) || (ableY==1 && ableX==0) || (ableY==1 && ableX==1)) {
			return true;
		}
		return false;
	}
	public int distance(int x, int y) {
	// ���������� ����� ����� �������
		return (int)Math.sqrt(Math.pow(this.x-x, 2)+Math.pow(this.y-y, 2));
	}
	public int distance(Coordinate e) {
	// ���������� ����� ����� �������
		return (int)Math.sqrt(Math.pow(this.x-e.x, 2)+Math.pow(this.y-e.y, 2));
	}
	public void moveToSide(Side side, int distance) {
		if (side == Side.N) {
			this.y -= distance;
		} else if (side == Side.E) {
			this.x += distance;
		} else if (side == Side.S) {
			this.y += distance;
		} else if (side == Side.W) {
			this.x -= distance;
		} else {
			throw new Error("Inappropriate side "+side);
		}
	}
}
