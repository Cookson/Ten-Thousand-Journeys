package erpoge.buildings;

import erpoge.core.Building;
import erpoge.core.EnhancedRectangle;
import erpoge.core.RectangleArea;
import erpoge.core.RectangleSystem;
import erpoge.core.StaticData;
import erpoge.core.TerrainBasics;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;

public class Park extends Building {
	public static final long serialVersionUID = 734683L;
	public void draw() {
		int objStatueDefender1 = StaticData.getObjectType("statue_defender_1").getId();
		int objBench = StaticData.getObjectType("bench").getId();
		int floorWater = StaticData.getFloorType("water").getId();
		int floorGrass = StaticData.getFloorType("grass").getId();
		int floorStone = StaticData.getFloorType("stone").getId();
		int floorDryGrass = StaticData.getFloorType("dry_grass").getId();
		int objTree1 = StaticData.getFloorType("tree1").getId();
		int objTree2 = StaticData.getFloorType("tree2").getId();
		
		RectangleSystem crs = new RectangleSystem(1);
		RectangleArea rightTop = crs.addRectangleArea(x, y, width, height);
		RectangleArea rightBottom = crs.cutRectangleFromSide(rightTop, frontSide, rightTop.getDimensionBySide(frontSide)/2);
		RectangleArea leftTop = crs.cutRectangleFromSide(rightTop, leftSide, rightTop.getDimensionBySide(leftSide)/2);
		RectangleArea leftBottom = crs.cutRectangleFromSide(rightBottom, leftSide, rightBottom.getDimensionBySide(leftSide)/2);
		
		
		// Stretch rectangles to have free place between them (for statues etc)s
		int centralPlaceSizeMod = 2; // Square of central place will be $centralPlaceSizeMod*2-1
		if (centralPlaceSizeMod > 0) {
			rightTop.stretch(leftSide, -centralPlaceSizeMod).stretch(frontSide, centralPlaceSizeMod);
			rightBottom.stretch(backSide, -centralPlaceSizeMod).stretch(leftSide, centralPlaceSizeMod);
			leftBottom.stretch(rightSide, -centralPlaceSizeMod).stretch(backSide, centralPlaceSizeMod);
			leftTop.stretch(frontSide,-centralPlaceSizeMod).stretch(rightSide,centralPlaceSizeMod);
			
			// Central rectangle
			Coordinate c1 = leftBottom.getCellFromSide(rightSide, backSide, 0).moveToSide(rightSide, 2);
			Coordinate c2 = rightTop.getCellFromSide(leftSide, frontSide, 0).moveToSide(leftSide, 2);
			EnhancedRectangle recCenter = EnhancedRectangle.getRectangleFromTwoCorners(c1, c2);
			if (recCenter.width == 1) {
				settlement.setObject(recCenter.x, recCenter.y, objStatueDefender1);
			} else if (recCenter.width == 3) {
				int chance = Chance.rand(1,100);
				if (chance < 20) {
				// Just a statue in middle
					settlement.setObject(recCenter.x+1, recCenter.y+1, objStatueDefender1);
				} else if (chance < 40) {
				// A statue surrounded by water
					settlement.setObject(recCenter.x+1, recCenter.y+1, objStatueDefender1);
					settlement.square(recCenter, TerrainBasics.ELEMENT_FLOOR, floorWater, false);
				} else if (chance < 60) {
				// A statue and 4 benches
					settlement.setObject(recCenter.x+1, recCenter.y+1, objStatueDefender1);
					settlement.setObject(recCenter.x, recCenter.y+1, objBench);
					settlement.setObject(recCenter.x+1, recCenter.y, objBench);
					settlement.setObject(recCenter.x+2, recCenter.y+1, objBench);
					settlement.setObject(recCenter.x+1, recCenter.y+2, objBench);
				}
			} else {
			// A statue on the middle of pond
				settlement.square(recCenter, TerrainBasics.ELEMENT_FLOOR, floorWater, true);
				settlement.setObject(recCenter.x+recCenter.width/2, recCenter.y+recCenter.height/2, objStatueDefender1);
				settlement.setFloor(recCenter.x+recCenter.width/2, recCenter.y+recCenter.height/2, floorGrass);
			}
			for (EnhancedRectangle r : crs.rectangleSet()) {
				settlement.fillRectangle(r, TerrainBasics.ELEMENT_OBJECT, objTree1, 10);
				settlement.fillRectangle(r, TerrainBasics.ELEMENT_OBJECT, objTree2, 10);
				settlement.fillRectangle(r, TerrainBasics.ELEMENT_FLOOR, floorDryGrass, 5);
			}
			// Get coordinates for roads and draw roads
			c1 = leftTop
					.getCellFromSide(frontSide, leftSide, 0)
					.moveToSide(frontSide, 1)
					.moveToSide(leftSide, 1);
			c2 = leftTop
				.getCellFromSide(frontSide, rightSide, 0)
				.moveToSide(frontSide, 1)
				.moveToSide(rightSide, 1);
			Coordinate c3 = rightTop
				.getCellFromSide(leftSide, backSide, 0)
				.moveToSide(leftSide, 1)
				.moveToSide(backSide, 1);
			Coordinate c4 = rightTop
				.getCellFromSide(leftSide, frontSide, 0)
				.moveToSide(leftSide, 1)
				.moveToSide(frontSide, 1);
			Coordinate c5 = rightBottom
				.getCellFromSide(backSide, leftSide, 0)
				.moveToSide(backSide, 1)
				.moveToSide(leftSide, 1);
			Coordinate c6 = rightBottom
				.getCellFromSide(backSide, rightSide, 0)
				.moveToSide(backSide, 1)
				.moveToSide(rightSide, 1);
			Coordinate c7 = leftBottom
				.getCellFromSide(rightSide, backSide, 0)
				.moveToSide(rightSide, 1)
				.moveToSide(backSide, 1);
			Coordinate c8 = leftBottom
				.getCellFromSide(rightSide, frontSide, 0)
				.moveToSide(rightSide, 1)
				.moveToSide(frontSide, 1);
			settlement.line(c1.x, c1.y, c2.x, c2.y, TerrainBasics.ELEMENT_FLOOR, floorStone);
			settlement.line(c3.x, c3.y, c4.x, c4.y, TerrainBasics.ELEMENT_FLOOR, floorStone);
			settlement.line(c5.x, c5.y, c6.x, c6.y, TerrainBasics.ELEMENT_FLOOR, floorStone);
			settlement.line(c7.x, c7.y, c8.x, c8.y, TerrainBasics.ELEMENT_FLOOR, floorStone);
//			crs.addRectangleArea(recCenter);
		}
		
		
		setTerrainModifier(crs);
	}
}
