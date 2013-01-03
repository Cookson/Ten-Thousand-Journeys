package erpoge.locationtypes;

import erpoge.core.HorizontalPlane;
import erpoge.core.Location;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.TerrainModifier;
import erpoge.core.meta.Side;

public class Forest extends Location {
	public Forest(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height, "Forest");
		
		int wall = StaticData.getObjectType("wall_grey_stone").getId();
		int grass = StaticData.getFloorType("grass").getId();
		int water = StaticData.getFloorType("water").getId();
		RectangleSystem rs = new RectangleSystem(1);
//		RectangleSystem rs = new RandomRectangleSystem(2, 2, 56, 22, 3, 2);
		TerrainModifier tm = new TerrainModifier(this, rs);
//		rs.build();
		
//		tm.drawInnerBorders(ELEMENT_OBJECT, wall);

		rs.addRectangleArea(12, 3, 7, 6);
		rs.addRectangleArea(27, 3, 3, 6);
		RectangleArea r = rs.addRectangleArea(5, 10, 29, 6);
		rs.build();
		tm.fillContents(ELEMENT_FLOOR, water);
		tm.drawSegments(rs.getSegmentsFreeFromNeighbors(r, Side.N));
	}
}
