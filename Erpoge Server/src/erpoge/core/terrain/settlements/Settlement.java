package erpoge.core.terrain.settlements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import erpoge.buildings.BuildingType;
import erpoge.buildings.Inn;
import erpoge.buildings.OneRoomHouse;
import erpoge.buildings.Temple;
import erpoge.buildings.TestBuilding;
import erpoge.core.Main;
import erpoge.core.characters.*;
import erpoge.core.characters.Character;
import erpoge.core.graphs.CustomRectangleSystem;
import erpoge.core.graphs.RectangleSystem;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Direction;
import erpoge.core.meta.Side;
import erpoge.core.meta.Utils;
import erpoge.core.net.RectangleArea;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.*;
import erpoge.core.terrain.settlements.Settlement.RoadSystem.Road;
public class Settlement extends Location {
	public CustomRectangleSystem rectangleSystem;
	protected RoadSystem roadSystem = new RoadSystem();
	protected QuarterSystem quarterSystem;
	public HashMap<Integer, RectangleSystem> quarters = new HashMap<Integer, RectangleSystem>();
	public ArrayList<Building> buildings = new ArrayList<Building>();
	public HashMap<Integer, Character> dwellers = new HashMap<Integer, Character>();
	public ArrayList<Service> services = new ArrayList<Service>();
	
	public Settlement (HorizontalPlane plane, int x, int y, int width, int height) {
		super(plane, x, y, width, height, "Settlement");
		quarterSystem = new QuarterSystem(this);
	}
	public void placeBuilding(BuildingPlace place, Class<? extends Building> cls) {
			Building building;
			try {
				building = cls.newInstance();
				if (building.fitsToPlace(place)) {
					building.setProperties(this, place);
					building.draw();
					buildings.add(building);
				} else {
					Main.log("Coundn't place building "+cls.getSimpleName());
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
//		if (type == BuildingType.TEST) {
//			buildings.add(new TestBuilding(this, place));
//		} else if (type == BuildingType.INN) {
//			buildings.add(new Inn(this, place));
//		} else if (type == BuildingType.ONE_ROOM_HOUSE) {
//			buildings.add(new OneRoomHouse(this, place));
//		} else if (type == BuildingType.TEMPLE) {
//			buildings.add(new Temple(this, place));
//		}
	}
	public void createRandomRoadSystem() {
		for (int y = Chance.rand(0, 20); y<height; y+=Chance.rand(20,25)) {
			roadSystem.createRoad(Chance.rand(0,5), y, Chance.rand(width-5,width-1), y);
		}
		for (int x = Chance.rand(0, 20); x<width; x+=Chance.rand(20,25)) {
			roadSystem.createRoad(x, Chance.rand(0,5), x, Chance.rand(height-5,height-1));
		}
	}
	public void markQuarter(int key, int width/*=8*/, int border/*=2*/) {
	// Разметить квартал - создать систему прямоугольников и занести её в Settlement::quarters
	// in: индекс пярмоугольника, на котором строится квартал, в Settlement::rectangles
		Rectangle r = rectangleSystem.content.get(key);
		quarters.put(key, new RectangleSystem(r.x+1,r.y+1,r.width-2,r.height-2, width, border));
//		quarters.get(key).initialFindOuterSides();
	}
	public Service createService(Character dweller, int type, String name) {
		Service service = new Service(dweller,type,name);
		services.add(service);
		return service;
	}
	public class RoadSystem {
		private ArrayList<Road> roads = new ArrayList<Road>();
		private HashMap<Road, ArrayList<Intersection>> intersections = new HashMap<Road, ArrayList<Intersection>>();
		private HashMap<Road, ArrayList<Intersection>> branches = new HashMap<Road, ArrayList<Intersection>>();
		public RoadSystem() {
			
		}
		public Road createRoad(int startX, int startY, int endX, int endY) {
			if (startX > endX) {
				int buf = startX;
				startX = endX;
				endX = buf;
			}
			if (startY > endY) {
				int buf = startY;
				startY = endY;
				endY = buf;
			}
			Road newRoad = new Road(startX, startY, endX, endY);
			
			// If new road lies on the same cells as one of 
			// existing roads, then throw an error.
			if (newRoad.direction == Direction.V) {
				for (Road road : roads) {
					if (
						road.direction == Direction.V && 
						road.start.x == newRoad.start.x && 
						Utils.integersRangeIntersection(road.start.y, road.end.y, newRoad.start.y, newRoad.end.y) >= 1
					) {
						throw new Error("Two roads on the same vertical line: "+newRoad+" and "+road);
					}
				}
			} else {
				for (Road road : roads) {
					if (
						road.direction == Direction.H && 
						road.start.y == newRoad.start.y && 
						Utils.integersRangeIntersection(road.start.x, road.end.x, newRoad.start.x, newRoad.end.x) >= 1
					) {
						throw new Error("Two roads on the same horizontal line: "+newRoad+" and "+road);
					}
				}
			}
			findChanges(newRoad);
			roads.add(newRoad);
			intersections.put(newRoad, new ArrayList<Intersection>());
			branches.put(newRoad, new ArrayList<Intersection>());
			
			return newRoad;
		}
		public void findChanges(Road newRoad) {
		/**
		 * Find new branches and intersections after adding newRoad.
		 */
			for (Road oldRoad : roads) {
				if (!areParallel(newRoad, oldRoad)) {
				// If roads are parallel, then go to next road
					if (newRoad.direction == Direction.V) {
					// If new road is vertical
						if (
							(newRoad.start.y == oldRoad.start.y || newRoad.end.y == oldRoad.start.y) && 
							newRoad.start.x >= oldRoad.start.x && newRoad.start.x <= oldRoad.end.x
						) {
							branches.get(oldRoad).add(
									new Intersection(newRoad, new Coordinate(newRoad.start.x,oldRoad.start.y)));
						} else if (
							newRoad.start.x >= oldRoad.start.x && newRoad.start.x <= oldRoad.end.x &&
							oldRoad.start.y >= oldRoad.start.y && newRoad.start.y <= oldRoad.end.y
						) {
							intersections.get(oldRoad).add(
									new Intersection(newRoad, new Coordinate(newRoad.start.x,oldRoad.start.y)));
						}
					} else {
					// If new road is horizontal
						if (
							(newRoad.start.x == oldRoad.start.x || newRoad.end.x == oldRoad.start.x) && 
							newRoad.start.y >= oldRoad.start.y && newRoad.start.y <= oldRoad.end.y
						) {
							branches.get(oldRoad).add(
									new Intersection(newRoad, new Coordinate(oldRoad.start.x,newRoad.start.y)));
						} else if (
							newRoad.start.y >= oldRoad.start.y && newRoad.start.y <= oldRoad.end.y &&
							oldRoad.start.x >= oldRoad.start.x && newRoad.start.x <= oldRoad.end.x
						) {
							intersections.get(oldRoad).add(
									new Intersection(newRoad, new Coordinate(oldRoad.start.x,newRoad.start.y)));
						}
					}
				}
			}
		}
		public boolean areParallel(Road road1, Road road2) {
			return road1.direction == road2.direction;
		}
		public void drawRoads() {
			for (Road road : roads) {
				boldLine(road.start.x, road.start.y, road.end.x, road.end.y, 
						ELEMENT_FLOOR, GameObjects.FLOOR_GROUND, 5);
			}
		}
		public void printStatistics() {
			Main.log("Roads: "+roads.size());
			int count = 0;
			for (ArrayList<Intersection> entity : branches.values()) {
				count += entity.size();
			}
			Main.log("Branches: "+count);
			count = 0;
			for (ArrayList<Intersection> entity : intersections.values()) {
				count += entity.size();
			}
			Main.log("Intersections: "+count);
			
		}
		public ArrayList<Coordinate> getReferencePoints() {
		/**
		 * Get significant points of road systems: intersections, 
		 * starts of branches and ends of roads
		 */
			ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
			for (ArrayList<Intersection> list : intersections.values()) {
			// Intersections
				for (Intersection intersection : list) {
					answer.add(intersection.point);
				}
			}
			// Branches points will be added in roads loop (all branch points are road starts/ends)
			for (Road road : roads) {
			// Ends of roads
				answer.add(road.start);
				answer.add(road.end);
			}
			return answer;
		}
		public class Road {
			public final Coordinate start;
			protected final Coordinate end;
			protected final Direction direction;
			protected int width = 5;
			public Road(int startX, int startY, int endX, int endY) {
				if (startX != endX && startY != endY) {
					throw new Error("Inappropriate new road: "+startX+" "+startY+" "+endX+" "+endY);
				}
				start = new Coordinate(startX, startY);
				end = new Coordinate(endX, endY);
				direction = start.x == end.x ? Direction.V : Direction.H;
			}
			public String toString() {
				return "Road ["+start.x+", "+start.y+", "+end.x+", "+end.y+"];";
			}
			public Side getSideOfRectangle(Rectangle r) {
			// Get side of rectangle from which this road is located
				if (direction == Direction.V) {
					if (this.start.x < r.x) {
						return Side.W;
					} else if (this.start.x >= r.x+r.width) {
						return Side.E;
					} else {
						throw new Error("Vertical road "+this+" is inside rectangle "+r);
					}
				} else {
					if (this.start.y < r.y) {
						return Side.N;
					} else if (this.start.y >= r.y+r.height) {
						return Side.S;
					} else {
						throw new Error("Horizontal road "+this+" is inside rectangle "+r);
					}
				}
			}
			public boolean crossesRectangle(Rectangle r) {
				if (direction == Direction.V) {
					return start.x >= r.x && start.x < r.x+r.width;
				} else {
					return start.y >= r.y && start.y < r.y+r.height;
				}
			}
			public boolean isRectangleOverlapsRoad(Rectangle rectangle) {
				RectangleArea ra = new RectangleArea(rectangle);
				if (direction == Direction.V) {
					if (
						Utils.integersRangeIntersection(
							rectangle.y, rectangle.y+rectangle.height-1, 
							start.y, end.y) > 0 && 
						ra.distanceToLine(start, end) < width / 2
					) {
					// If road line and rectangle overlap in y-axis,
					// and road is close enough to rectangle
						return true;
					}
				} else {
					if (
						Utils.integersRangeIntersection(
							rectangle.x, rectangle.x+rectangle.width-1, 
							start.x, end.x) > 0 && 
						ra.distanceToLine(start, end) < width / 2
					) {
					// Same for x-axis
						return true;
					}
				}
				return false;
			}
			public boolean isRectangleNearRoad(Rectangle rectangle) {
				/**
				 * Checks if this road goes along one of the borders of a rectangle
				 */
					RectangleArea ra = new RectangleArea(rectangle);
					if (direction == Direction.V) {
						if (
							Utils.integersRangeIntersection(
								rectangle.y, rectangle.y+rectangle.height-1, 
								start.y, end.y) > 0 && 
							ra.distanceToLine(start, end) == width/2+1
						) {
						// If road line and rectangle overlap in y-axis,
						// and road is close enough to rectangle
							return true;
						}
					} else {
						if (
							Utils.integersRangeIntersection(
								rectangle.x, rectangle.x+rectangle.width-1, 
								start.x, end.x) > 0 && 
							ra.distanceToLine(start, end) == width/2+1
						) {
						// Same for x-axis
							return true;
						}
					}
					return false;
				}
		}
		public class Intersection {
		/**
		 * Intersection or branch
		 */
			protected Road road;
			protected Coordinate point;
			public Intersection(Road road, Coordinate point) {
				this.road = road;
				this.point = point;
			}
		}
		
	}
	public class QuarterSystem {
		private static final char EMPTY = '.';
		private static final char ROAD = '/';
		private static final char QUARTER = '#';
		private Settlement settlement;
		private char[][] grid;
		public ArrayList<Quarter> quarters = new ArrayList<Quarter>();
		public ArrayList<BuildingPlace> buildingPlaces = new ArrayList<BuildingPlace>();
		public QuarterSystem(Settlement settlement) {
			this.settlement = settlement;
		}
		public void showGrid() {
			for (int y=0; y<settlement.height; y++) {
				for (int x=0; x<settlement.height; x++) {
					Main.out(grid[x][y]);
				}
				Main.outln();
			}
		}
		public void build(ArrayList<Coordinate> points) {
		/**
		 * Builds quarter system from significant points of road system
		 */
			// Grid will be filled with values that show what is in this cell:
			// road, quarter or nothing			
			grid = new char[settlement.width][settlement.height];
			for (int i=0;i<settlement.width; i++) {
				for (int j=0;j<settlement.height; j++) {
					grid[i][j] = EMPTY;
				}
			}
			for (Road road : settlement.roadSystem.roads) {
				if (road.direction == Direction.V) {
					for (int y=road.start.y; y<=road.end.y; y++) {
						grid[road.start.x][y] = ROAD;
					}
				} else {
					for (int x=road.start.x; x<=road.end.x; x++) {
						grid[x][road.start.y] = ROAD;
					}
				}
			}
//			for (int i=0;i<settlement.height; i++) {
//				for (int j=0;j<settlement.width; j++) {
//					Main.out(grid[j][i]);
//				}
//				Main.outln();
//			}
			for (Coordinate point : points) {
				findQuarter(point, 1, 1);
				findQuarter(point, 1, -1);
				findQuarter(point, -1, 1);
				findQuarter(point, -1, -1);
			}
			for (Quarter quarter : quarters) {
				for (BuildingPlace place : quarter.getBuildingPlaces(25)) {
					buildingPlaces.add(place);
				}
			}
		}
		private void findQuarter(Coordinate point, int dx, int dy) {
		/**
		 * Find empty area from the particular side of point.
		 * Side is determined by dx and dy, both of which can be either 1 or -1.
		 * We expand the quarter until it stumbles upon a road, border of 
		 * location or another quarter. 
		 */			
			Coordinate cornerPoint = new Coordinate(point.x+dx, point.y+dy);
			if (cornerPoint.x < 0 || cornerPoint.y < 0 || cornerPoint.x >= settlement.width || cornerPoint.y >= settlement.height) {
				return;
			}
//			grid[cornerPoint.x][cornerPoint.y] = QUARTER;
			boolean xStop = false;
			boolean yStop = false;
			int quarterWidth = 0;
			int quarterHeight = 1; 	// This is necessary, because 
									// after width becomes 1, height also becomes 1
			for (int step = 0;!(xStop && yStop);step++) {
				if (!xStop) {
					int x = cornerPoint.x+dx*quarterWidth;
					if (x >= 0 && x < settlement.width) {
					// If x is inside location
						for (int y=cornerPoint.y; y!=cornerPoint.y+dy*quarterHeight; y+=dy) {
							if (grid[x][y] != EMPTY) {
								xStop = true;
								break;
							} else {
								grid[x][y] = QUARTER;
							}
						}
						if (!xStop) {
							quarterWidth++;
						}
					} else {
						xStop = true;
					}
				}
				if (!yStop) {
					int y = cornerPoint.y+dy*quarterHeight;
					if (y >= 0 && y < settlement.height) {
					// If y is inside location
						for (int x=cornerPoint.x; x!=cornerPoint.x+dx*quarterWidth; x+=dx) {
							if (x == settlement.width || x == -1) {
								yStop = true;
								break;
							} else if (grid[x][y] != EMPTY) {
								yStop = true;
								break;
							} else {
								grid[x][y] = QUARTER;
							}
						}
						if (!yStop) {
							quarterHeight++;
						}
					} else {
						yStop = true;
					}
				}
			}
			if (quarterWidth > 3 && quarterHeight > 3) {
				quarters.add(new Quarter(this, new Rectangle(
					dx == 1 ? cornerPoint.x : cornerPoint.x-quarterWidth+1,
					dy == 1 ? cornerPoint.y : cornerPoint.y-quarterHeight+1,
					quarterWidth, quarterHeight
				)));
			}
		}
		public class Quarter extends Rectangle {
			public final QuarterSystem system;
			public final ArrayList<Road> closeRoads = new ArrayList<Road>();
			public Quarter(QuarterSystem system, Rectangle rectangle) {
				super(rectangle);
				this.system = system;
				for (Road road : system.settlement.roadSystem.roads) {
					/* */ // May fail for roads with even width
					if (road.isRectangleOverlapsRoad(rectangle)) {
						closeRoads.add(road);
					}
				}
				
				// Now we have a rectangle with border near roads' center line. 
				// Then we narrow rectangle by roads, according to roads' width.
				
				for (Road road : system.settlement.roadSystem.roads) {
					if (!road.crossesRectangle(this)) {
						narrowRectangleByRoad(this, road);
					}
				}
			}
			public HashSet<BuildingPlace> getBuildingPlaces(int minWidth) {
				HashSet<BuildingPlace> answer = new HashSet<BuildingPlace>();
				RectangleSystem rs = settlement.getGraph(x, y, width, height, minWidth, 1);
				for (int k : rs.content.keySet()) {
					if (rs.isRectangleOuter(k)) {
						answer.add(new BuildingPlace(rs.content.get(k), this));
					}
				}
				return answer;
			}
			private void narrowRectangleByRoad(Rectangle rec, Road road) {
			/**
			 * Change rectangle start and dimensions as if road would "bite off" 
			 * a part of rectangle by road's width.
			 */
				Side side = road.getSideOfRectangle(rec);
				if (side == Side.N) {
					int newY = Math.max(road.start.y+road.width/2+1, rec.y);
					if (newY != rec.y) {
						rec.setBounds(rec.x, newY, rec.width, rec.height-(newY-road.start.y)+1);
					}
				} else if (side == Side.E) {
					int newEndX = Math.min(rec.x+rec.width-1, road.start.x-road.width/2-1);
					if (newEndX != rec.x+rec.width-1) {
						rec.setSize(newEndX-rec.x+1, rec.height);
					}
				} else if (side == Side.S) {
					int newEndY = Math.min(rec.y+rec.height-1, road.start.y-road.width/2-1);
					if (newEndY != rec.y+rec.height-1) {
						rec.setSize(rec.width, newEndY-rec.y+1);
					}
				} else if (side == Side.W) {
					int newX = Math.max(road.start.x+road.width/2+1, rec.x);
					if (newX != rec.x) {
						rec.setBounds(newX, rec.y, rec.width-(newX-road.start.x)+1, rec.height);
					}
				}
			}
		}
		
	}
}
