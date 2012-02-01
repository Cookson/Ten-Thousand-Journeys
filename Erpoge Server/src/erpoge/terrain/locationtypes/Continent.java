//package erpoge.terrain.locationtypes;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
//
//import erpoge.Chance;
//import erpoge.Coordinate;
//import erpoge.Main;
//import erpoge.graphs.Graph;
//import erpoge.graphs.RectangleSystem;
//import erpoge.graphs.WeigherPathLength;
//import erpoge.objects.GameObjects;
//import erpoge.terrain.*;
//
//public class Continent extends Location {
//	public Continent(World world) {
//		super(world);
//		
//		int numOfIslands = 10;
//		int numOfCities = 8;
//		
//		RectangleSystem rs = getGraph(0,0,width,height,1,0);
//		fillWithCells(7, 0);
//		
//		rs.nibbleSystem(3, 30);
//		
//		for (Rectangle r : rs.rectangles.values()) {
//			square(r, ELEMENT_FLOOR, GameObjects.FLOOR_GRASS, true);
//		}
//		// Create islands
//		RectangleSystem islandsGraph = getGraph(2,2,width-4,height-4,7,2);
//		islandsGraph.excludeRectanglesHaving(ELEMENT_FLOOR, 1);
//		
//		numOfIslands = Math.min(numOfIslands, islandsGraph.size());
//		for (int i=0;i<numOfIslands;i++) {
//			int k = islandsGraph.getRandomRectangleNum();
//			Rectangle r = islandsGraph.rectangles.get(k);
//			islandsGraph.excludeRectangle(k);
//			RectangleSystem island = getGraph(r.x, r.y, r.width, r.height, 1, 0);
//			island.nibbleSystem(1, 30);
//			for (Rectangle r2 : island.rectangles.values()) {
//				square(r2, ELEMENT_FLOOR, 1, true);			
//			}
//		}
//		
//		// Smooth with cellular automata method
//		cellularAutomataSmooth(1, ELEMENT_FLOOR, GameObjects.FLOOR_GRASS, GameObjects.FLOOR_WATER);
//		// � ����� ������� rs ���������
//		
//		// Place forests
//		for (Rectangle r : getGraph(2,2,width-4,height-4,10,0).rectangles.values()) { 
//			for (int i=r.x;i<r.x+r.width-1;i++) {
//				for (int j=r.y;j<r.y+r.height-1;j++) {
//					if (cells[i][j].floor()!=7 && Chance.roll(60)) {
//						setObject(i,j,GameObjects.OBJ_WORLD_FOREST);
//					}
//				}
//			}
//		}
//		cellularAutomataSmooth(9, ELEMENT_OBJECT, GameObjects.OBJ_WORLD_FOREST, GameObjects.OBJ_VOID);
//		for (int i=0;i<width;i++) {
//			for (int j=0;j<height;j++) {
//				if (cells[i][j].floor()==7 && cells[i][j].object() != 0) {
//					setObject(i,j,GameObjects.OBJ_VOID);
//				}
//			}
//		}
//		
//		// Place cities
//		Graph<Coordinate> cities = new Graph<Coordinate>();
//		RectangleSystem citiesRS = getGraph(2,2,width-4,height-4,3,0);
//		
////		Main.console(citiesRS.size());
////		citiesRS.excludeRectanglesHaving(ELEMENT_FLOOR, GameObjects.FLOOR_WATER);
////		Main.console(citiesRS.size());
//
//		
//		Rectangle coastr = citiesRS.getRandomRectangle();
//		CellCollection coast = getCoast(coastr.x, coastr.y);
//		for (int i=0;i<numOfCities;i++) {
//			Coordinate c = coast.setElementAndReport(ELEMENT_OBJECT, GameObjects.OBJ_WORLD_CITY);
//			coast.removeCellsCloseTo(c.x, c.y, 5);
//			cities.addVertex(c);
//		}
////		line(37, 37, 23, 18, ELEMENT_OBJECT, GameObjects.OBJ_WORLD_CONUS_ROCK);
////		numOfCities = Math.min(numOfCities, citiesRS.size());
////		for (int i=0;i<numOfCities;i++) {
////			int num = citiesRS.getRandomRectangleNum();
////			Rectangle r = citiesRS.rectangles.get(num);
////			int cityX = r.x+Chance.rand(0,r.width-1);
////			int cityY = r.y+Chance.rand(0,r.height-1);
////			setObject(cityX, cityY, GameObject.OBJ_WORLD_CITY);
////			cities.addVertex(new Coordinate(cityX, cityY));
////		}
//		
//		// Link cities with roads
//		cities.getMinimumSpanningTree(new WeigherPathLength(this, cities));
//		for (int i=0;i<cities.edges.size();i++) {
//			int x1 = cities.content.get(i).x;
//			int y1 = cities.content.get(i).y;
//			for (int j : cities.edges.get(i)) {
//				drawPath(x1, y1, cities.content.get(j).x, cities.content.get(j).y, ELEMENT_ROAD, 1);
//			}
//		}
//	}
//}
