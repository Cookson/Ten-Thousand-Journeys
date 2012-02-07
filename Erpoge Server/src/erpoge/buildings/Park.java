package erpoge.buildings;

import java.awt.Rectangle;

import erpoge.core.Main;
import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Side;
import erpoge.core.net.RectangleArea;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.TerrainBasics;
import erpoge.core.terrain.settlements.Building;

public class Park extends Building {
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(x, y, width, height, 1);
		int rightBottom = crs.cutRectangleFromSide(0, frontSide, crs.content.get(0).getDimensionBySide(frontSide)/2);
		int leftTop = crs.cutRectangleFromSide(0, leftSide, crs.content.get(0).getDimensionBySide(leftSide)/2);
		int leftBottom = crs.cutRectangleFromSide(1, leftSide, crs.content.get(1).getDimensionBySide(leftSide)/2);
		int rightTop = 0;
		
		// Stretch rectangles to have free place between them (for statues etc)s
		int centralPlaceSizeMod = 2; // Square of central place will be $centralPlaceSizeMod*2-1
		if (centralPlaceSizeMod > 0) {
			RectangleArea recRightTop = crs.content.get(rightTop);
			recRightTop.stretch(leftSide, -centralPlaceSizeMod).stretch(frontSide, centralPlaceSizeMod);
			RectangleArea recRightBottom = crs.content.get(rightBottom);
			recRightBottom.stretch(backSide, -centralPlaceSizeMod).stretch(leftSide, centralPlaceSizeMod);
			RectangleArea recLeftBottom = crs.content.get(leftBottom);
			recLeftBottom.stretch(rightSide, -centralPlaceSizeMod).stretch(backSide, centralPlaceSizeMod);
			RectangleArea recLeftTop = crs.content.get(leftTop);
			recLeftTop.stretch(frontSide,-centralPlaceSizeMod).stretch(rightSide,centralPlaceSizeMod);
			
			// Central rectangle
			Coordinate c1 = recLeftBottom.getCellFromSide(rightSide, backSide, 0).moveToSide(rightSide, 2);
			Coordinate c2 = recRightTop.getCellFromSide(leftSide, frontSide, 0).moveToSide(leftSide, 2);
			RectangleArea recCenter = RectangleArea.getRectangleFromTwoCorners(c1,c2);
			if (recCenter.width == 1) {
				settlement.setObject(recCenter.x, recCenter.y, GameObjects.OBJ_STATUE_DEFENDER_1);
			} else if (recCenter.width == 3) {
				int chance = Chance.rand(1,100);
				if (chance < 20) {
				// Just a statue in middle
					settlement.setObject(recCenter.x+1, recCenter.y+1, GameObjects.OBJ_STATUE_DEFENDER_1);
				} else if (chance < 40) {
				// A statue surrounded by water
					settlement.setObject(recCenter.x+1, recCenter.y+1, GameObjects.OBJ_STATUE_DEFENDER_1);
					settlement.square(recCenter, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_WATER, false);
				} else if (chance < 60) {
				// A statue and 4 benches
					settlement.setObject(recCenter.x+1, recCenter.y+1, GameObjects.OBJ_STATUE_DEFENDER_1);
					settlement.setObject(recCenter.x, recCenter.y+1, GameObjects.OBJ_BENCH);
					settlement.setObject(recCenter.x+1, recCenter.y, GameObjects.OBJ_BENCH);
					settlement.setObject(recCenter.x+2, recCenter.y+1, GameObjects.OBJ_BENCH);
					settlement.setObject(recCenter.x+1, recCenter.y+2, GameObjects.OBJ_BENCH);
				}
			} else {
			// A statue on the middle of pond
				settlement.square(recCenter, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_WATER, true);
				settlement.setObject(recCenter.x+recCenter.width/2, recCenter.y+recCenter.height/2, GameObjects.OBJ_STATUE_DEFENDER_1);
				settlement.setFloor(recCenter.x+recCenter.width/2, recCenter.y+recCenter.height/2, GameObjects.FLOOR_GRASS);
			}
			for (Rectangle r : crs.content.values()) {
				settlement.fillRectangle(r, TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TREE_1, 10);
				settlement.fillRectangle(r, TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TREE_2, 10);
				settlement.fillRectangle(r, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_DRY_GRASS, 5);
			}
			// Get coordinates for roads and draw roads
			c1 = recLeftTop
					.getCellFromSide(frontSide, leftSide, 0)
					.moveToSide(frontSide, 1)
					.moveToSide(leftSide, 1);
			c2 = recLeftTop
				.getCellFromSide(frontSide, rightSide, 0)
				.moveToSide(frontSide, 1)
				.moveToSide(rightSide, 1);
			Coordinate c3 = recRightTop
				.getCellFromSide(leftSide, backSide, 0)
				.moveToSide(leftSide, 1)
				.moveToSide(backSide, 1);
			Coordinate c4 = recRightTop
				.getCellFromSide(leftSide, frontSide, 0)
				.moveToSide(leftSide, 1)
				.moveToSide(frontSide, 1);
			Coordinate c5 = recRightBottom
				.getCellFromSide(backSide, leftSide, 0)
				.moveToSide(backSide, 1)
				.moveToSide(leftSide, 1);
			Coordinate c6 = recRightBottom
				.getCellFromSide(backSide, rightSide, 0)
				.moveToSide(backSide, 1)
				.moveToSide(rightSide, 1);
			Coordinate c7 = recLeftBottom
				.getCellFromSide(rightSide, backSide, 0)
				.moveToSide(rightSide, 1)
				.moveToSide(backSide, 1);
			Coordinate c8 = recLeftBottom
				.getCellFromSide(rightSide, frontSide, 0)
				.moveToSide(rightSide, 1)
				.moveToSide(frontSide, 1);
			settlement.line(c1.x, c1.y, c2.x, c2.y, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_STONE);
			settlement.line(c3.x, c3.y, c4.x, c4.y, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_STONE);
			settlement.line(c5.x, c5.y, c6.x, c6.y, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_STONE);
			settlement.line(c7.x, c7.y, c8.x, c8.y, TerrainBasics.ELEMENT_FLOOR, GameObjects.FLOOR_STONE);
			crs.addVertex(recCenter);
		}
		
		
		setRectangleSystem(crs);
	}
}
