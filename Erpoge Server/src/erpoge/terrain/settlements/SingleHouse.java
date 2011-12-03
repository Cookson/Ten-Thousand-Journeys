package erpoge.terrain.settlements;

import java.util.ArrayList;

import erpoge.Chance;
import erpoge.terrain.Cell;
import erpoge.terrain.Location;
import erpoge.terrain.locationtypes.Settlement;

public class SingleHouse extends Settlement {
	public SingleHouse(Location location) {
		super(location);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				cells[i][j] = new Cell(1, 0);
			}
		}
		Building t = new Tavern(this, 10, 12, 14, 18);
		
		int iB = Chance.rand(0,buildings.size()-1);
		ArrayList<Integer> keys3 = new ArrayList<Integer> (buildings.get(iB).rectangleSystem.rectangles.keySet());
		int iR = keys3.get(Chance.rand(0, keys3.size()-1));
		
		location.startArea = buildings.get(iB).rectangleSystem.rectangles.get(iR);
	}
}
