package erpoge.characters;

import erpoge.Coordinate;
import erpoge.terrain.Location;

public class Entity extends Coordinate {
	public Entity(int x, int y) {
		super(x,y);
	}
	
	public Coordinate coordinate() {
		return new Coordinate(x,y);
	}
}
