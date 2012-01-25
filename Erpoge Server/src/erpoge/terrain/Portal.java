package erpoge.terrain;

import erpoge.Coordinate;

public class Portal extends Coordinate {
/**
 * Portal is a marker on particular cell which 
 * grants to haracters near it ability to go
 * to another location which is linked with current 
 * location by this portal. Ladders and teleporters 
 * work this way.
 * 
 * Portals work in pairs: portal points to another portal,
 * which points to the first one.
 */
	public final Location location;
	private Portal anotherEnd;
	public Portal(Coordinate coordinate, Location location) {
		super(coordinate);
		this.location = location;
	}
	public void linkWith(Portal portal) {
		portal.anotherEnd = this;
		this.anotherEnd = portal;
	}
	public Portal getAnotherEnd() {
		return anotherEnd;
	}

}
