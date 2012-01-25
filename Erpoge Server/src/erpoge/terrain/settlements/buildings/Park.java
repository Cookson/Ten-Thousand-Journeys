package erpoge.terrain.settlements.buildings;

import java.awt.Rectangle;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.RectangleArea;
import erpoge.Side;
import erpoge.graphs.CustomRectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.settlements.Building;

public class Park extends Building {
	public void draw() {
		CustomRectangleSystem crs = new CustomRectangleSystem(settlement, x, y, width, height, 1);
		int rightBottom = crs.cutRectangleFromSide(0, frontSide, crs.rectangles.get(0).getDimensionBySide(frontSide)/2);
		int leftTop = crs.cutRectangleFromSide(0, leftSide, crs.rectangles.get(0).getDimensionBySide(leftSide)/2);
		int leftBottom = crs.cutRectangleFromSide(1, leftSide, crs.rectangles.get(1).getDimensionBySide(leftSide)/2);
		int rightTop = 0;
		
		// Stretch rectangles to have free place between them (for statues etc)s
		int centralPlaceSizeMod = 2; // Square of central place will be $centralPlaceSizeMod*2-1
		if (centralPlaceSizeMod > 0) {
			RectangleArea recRightTop = crs.rectangles.get(rightTop);
			recRightTop.stretch(leftSide, -centralPlaceSizeMod).stretch(frontSide, centralPlaceSizeMod);
			RectangleArea recRightBottom = crs.rectangles.get(rightBottom);
			recRightBottom.stretch(backSide, -centralPlaceSizeMod).stretch(leftSide, centralPlaceSizeMod);
			RectangleArea recLeftBottom = crs.rectangles.get(leftBottom);
			recLeftBottom.stretch(rightSide, -centralPlaceSizeMod).stretch(backSide, centralPlaceSizeMod);
			RectangleArea recLeftTop = crs.rectangles.get(leftTop);
			recLeftTop.stretch(frontSide,-centralPlaceSizeMod).stretch(rightSide,centralPlaceSizeMod);
			
			// Central rectangle
			Coordinate c1 = recLeftBottom.getCellFromSide(rightSide, backSide, 0).moveToSide(rightSide, 2);
			Coordinate c2 = recRightTop.getCellFromSide(leftSide, frontSide, 0).moveToSide(leftSide, 2);
			RectangleArea recCenter = RectangleArea.getRectangleFromTwoCorners(c1,c2);
			Main.console(recCenter);
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
			for (Rectangle r : crs.rectangles.values()) {
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
			crs.addRectangle(recCenter);
		}
		
		
		setRectangleSystem(crs);
	}
}
