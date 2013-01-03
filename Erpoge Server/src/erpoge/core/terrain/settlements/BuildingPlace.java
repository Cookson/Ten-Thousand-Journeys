package erpoge.core.terrain.settlements;
import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.core.EnhancedRectangle;
import erpoge.core.terrain.settlements.Settlement.QuarterSystem.Quarter;
import erpoge.core.terrain.settlements.Settlement.RoadSystem.Road;
/**
 * Space for placing a building. Each quarter will be divided into 
 * several of those after roads' and quarters' generation.
 */
public class BuildingPlace extends EnhancedRectangle {
	public static final long serialVersionUID = 83682932346L;

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
