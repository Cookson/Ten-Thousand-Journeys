package erpoge.core.terrain.settlements;
import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.core.net.RectangleArea;
import erpoge.core.terrain.settlements.Settlement.QuarterSystem.Quarter;
import erpoge.core.terrain.settlements.Settlement.RoadSystem.Road;

public class BuildingPlace extends RectangleArea {
	public static final long serialVersionUID = 83682932346L;
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
