package erpoge.locationtypes;

import erpoge.buildings.House;
import erpoge.core.HorizontalPlane;
import erpoge.core.Location;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.StdTerrainViewer;
import erpoge.core.TerrainModifier;
import erpoge.core.meta.Side;
import erpoge.core.terrain.settlements.BuildingPlace;

public class Forest extends Location {
	int wall = StaticData.getObjectType("wall_grey_stone").getId();
	int grass = StaticData.getFloorType("grass").getId();
	int water = StaticData.getFloorType("water").getId();

	public Forest(HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height, "Forest");
		House house = new House(this, new BuildingPlace(3, 3, 22, 22), Side.E);
		placeBuilding(house);
		new StdTerrainViewer(this).printNumbers()
		.addRectangleSystem(house.getRs())
		.print();
	}
	private void recSysTest() {
		RectangleSystem rs = new RectangleSystem(3);
		// RectangleSystem rs = new RandomRectangleSystem(2, 2, 55, 22, 3, 2);
		TerrainModifier tm = new TerrainModifier(this, rs);
		rs.build();
		tm.fillContents(ELEMENT_FLOOR, water);
		tm.drawInnerBorders(ELEMENT_OBJECT, wall);
		tm.drawOuterBorders(ELEMENT_OBJECT, wall);
	}
}
