package erpoge;

import erpoge.terrain.TerrainBasics;


public class Coordinate {
	public int x;
	public int y;
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public int hashCode() {
		return x*10000+y;
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
	// Расстояние между двумя точками
		return (int)Math.sqrt(Math.pow(this.x-x, 2)+Math.pow(this.y-y, 2));
	}
	public int distance(Coordinate e) {
	// Расстояние между двумя точками
		return (int)Math.sqrt(Math.pow(this.x-e.x, 2)+Math.pow(this.y-e.y, 2));
	}
	public void moveToSide(int side) {
		if (side == TerrainBasics.SIDE_N) {
			this.y--;
		} else if (side == TerrainBasics.SIDE_E) {
			this.x++;
		} else if (side == TerrainBasics.SIDE_S) {
			this.y++;
		} else if (side == TerrainBasics.SIDE_W) {
			this.x--;
		} else {
			throw new Error("Inappropriate side "+side);
		}
	}
}
