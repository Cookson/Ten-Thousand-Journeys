package erpoge.terrain.settlements;
import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.RectangleArea;
import erpoge.Side;
import erpoge.terrain.settlements.Settlement.QuarterSystem.Quarter;
import erpoge.terrain.settlements.Settlement.RoadSystem.Road;

public class BuildingPlace extends RectangleArea {
/**
 * Space for placing a building. Each quarter will be divided into 
 * several of those after roads' and quarters' generation.
 */
	public final ArrayList<Road> closeRoads = new ArrayList<Road>();
	public BuildingPlace(Rectangle rectangle, Quarter quarter) {
		super(rectangle);
		for (Road road : quarter.closeRoads) {
			if (road.isRectangleNearRoad(this)) {
				closeRoads.add(road);
			}
		}
	}
	public BuildingPlace(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
}