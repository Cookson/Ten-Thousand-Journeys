package erpoge.terrain.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.characters.GeneratorCharacterGroup;
import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.CellCollection;
import erpoge.terrain.Container;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.settlements.Building;

public class Dungeon extends TerrainGenerator {
	public Dungeon (Location location) {
		super(location);
		RectangleSystem graph = this.getGraph(0,0,width,height,14,7);
		fillWithCells(2,4);
		graph.convertGraphToDirectedTree(3);
		int size=graph.size();
		int maxRoomSize=24;
		int minRoomSize=16;
		ArrayList<Rectangle> rooms=new ArrayList<Rectangle>();
		for (Rectangle r : graph.rectangles.values()) {
			int w=Math.min(r.width-2,Chance.rand(minRoomSize,maxRoomSize));
			int x=r.x+Chance.rand(1,r.width-w-1);
			int h=Math.min(r.height-2,Chance.rand(minRoomSize,maxRoomSize));
			int y=r.y+Chance.rand(1,r.height-h-1);
			rooms.add(new Rectangle(x,y,w,h));
			square(x,y,w,h,ELEMENT_REMOVE,0,true);
		}
		location.startArea=rooms.get(Chance.rand(0,rooms.size()-1));
		for (Rectangle room : rooms) {
		// Заполняем комнаты содержимым
			if (room.x==this.startArea.x && room.y==this.startArea.y) {
				continue;
			}
			if (Chance.roll(30)) {
				int x=room.x+Chance.rand(0,room.width-1);
				int y=room.y+Chance.rand(0,room.height-1);
				setObject(x, y, GameObjects.OBJ_CHEST_1);
				Container c = createContainer(x,y);
			} else if (Chance.roll(50)) {
//				if (true)
//				continue;
//				RectangleSystem house=house(room.x,room.y,room.width,room.height,4,4);
				Building b = new Building(this, room.x, room.y, room.width, room.height, 4);
				b.buildBasis(GameObjects.OBJ_WALL_GREY_STONE,false);
				RectangleSystem house = b.rectangleSystem;
				for (Rectangle hroom : house.rectangles.values()) {
					CellCollection roomCollection=newCellCollection(closeCells(hroom.x+1,hroom.y+1,7, PASSABILITY_FREE, true));
					if (Chance.roll(70)) {
						ArrayList<GeneratorCharacterGroup> hroomCharacters = new ArrayList<GeneratorCharacterGroup>();
						hroomCharacters.add(new GeneratorCharacterGroup("goblin","Гоблин",Chance.rand(1,3),0));
						hroomCharacters.add(new GeneratorCharacterGroup("goblinMage","Гоблин-маг",Chance.rand(0,1)*Chance.rand(0,2),0));
						hroomCharacters.add(new GeneratorCharacterGroup("ogre","Огр",Chance.rand(0,1)*Chance.rand(0,1),0));
						roomCollection.placeCharacters(hroomCharacters);
					} else {
//						Coordinate cell = roomCollection.cells.get(Chance.rand(0,roomCollection.cells.size()-1));
//						
//						int x=cell.x;
//						int y=cell.y;
//						setObject(x, y, GameObjects.OBJ_CHEST_1);
//						Container container = createContainer(x,y);
//						if (Chance.roll(20)) {
//							container.addItem(900000, Chance.rand(20,100));
//						}
//						int[] items=new int[] {100,1904,1503,400};
//						for (int item : items) {
//							if (Chance.roll(10)) {
//								container.addItem(item, 1);
//							}
//						}
					}
				}
			}
		}
		
		for (int k=0;k<rooms.size();k++) {
			Rectangle ri1 = rooms.get(k);
			for (int edge : graph.edges.get(k)) {
				Rectangle r2=rooms.get(edge);
				if (
					(graph.rectangles.get(k).x+graph.rectangles.get(k).width+graph.borderWidth==graph.rectangles.get(edge).x 
					|| graph.rectangles.get(edge).x+graph.rectangles.get(edge).width+graph.borderWidth==graph.rectangles.get(k).x)
				) {
				// Если граничат вертикальными сторонами, и если можно соединить жирной линией, не пересекая границы комнаты вдоль
					Rectangle r1 = ri1;
					if (r1.x>r2.x) {
					// Сделать первой комнатой ту, которая левее
						Rectangle buf = r1;
						r1 = r2;
						r2 = buf;
					}
					int xStart=r1.x+r1.width-1;
					int yStart=(int)(r1.y+Math.floor(r1.height/2));
					int xEnd=r2.x;
					int yEnd=(int)(r2.y+Math.floor(r2.height/2));
					if (xEnd-xStart<7) {
					// Если проход с перегибом не помещается, сделать просто линию
						boldLine(xStart, yStart, xEnd, yEnd, ELEMENT_REMOVE, 0);
					} else {
						int xBend=Chance.rand(xStart+3, xEnd-3);
						boldLine(xStart, yStart, xBend, yStart, ELEMENT_REMOVE, 0);
						boldLine(xBend, yStart, xBend, yEnd, ELEMENT_REMOVE, 0);
						boldLine(xBend, yEnd, xEnd, yEnd, ELEMENT_REMOVE, 0);
						if (Chance.roll(50)) {
						// Дверь на конце
							setObject(xStart+1,yStart+1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xStart+1,yStart-1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xStart+1,yStart,GameObjects.OBJ_DOOR_BLUE);
						}
						if (Chance.roll(50)) {
						// Дверь на другом конце
							setObject(xEnd-1,yEnd+1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xEnd-1,yEnd-1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xEnd-1,yEnd,GameObjects.OBJ_DOOR_BLUE);
						}
					}
					setObject(xEnd,yEnd,GameObjects.OBJ_VOID);
					setObject(xEnd,yEnd-1,GameObjects.OBJ_VOID);
					setObject(xEnd,yEnd+1,GameObjects.OBJ_VOID);
					setObject(xStart,yStart+1,GameObjects.OBJ_VOID);
					setObject(xStart,yStart-1,GameObjects.OBJ_VOID);
					setObject(xStart,yStart,GameObjects.OBJ_VOID);
				} else {
				// Если граничат горизонтальными
					Rectangle r1 = ri1;
					if (r1.y>r2.y) {
					// Сделать первой комнатой ту, которая выше
						Rectangle buf=r1;
						r1=r2;
						r2=buf;
					}
					int xStart=(int)(r1.x+Math.floor(r1.width/2));
					int yStart=r1.y+r1.height-1;
					int xEnd=(int)(r2.x+Math.floor(r2.width/2));
					int yEnd=r2.y;
					if (yEnd-yStart<7) {
					// Если проход с перегибом не помещается, сделать просто линию
						boldLine(xStart, yStart, xEnd, yEnd, ELEMENT_REMOVE, 0);
					} else {
						int yBend=Chance.rand(yStart+3, yEnd-3);
						boldLine(xStart, yStart, xStart, yBend, ELEMENT_REMOVE, 0);
						boldLine(xStart, yBend, xEnd, yBend, ELEMENT_REMOVE, 0);
						boldLine(xEnd, yBend, xEnd, yEnd, ELEMENT_REMOVE, 0);
						if (Chance.roll(50)) {
						// Дверь на конце
							setObject(xStart-1,yStart+1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xStart+1,yStart+1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xStart,yStart+1,GameObjects.OBJ_DOOR_BLUE);
							setObject(xEnd,yEnd,GameObjects.OBJ_VOID);
						}
						if (Chance.roll(50)) {
						// Дверь на другом конце
							setObject(xEnd-1,yEnd-1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xEnd+1,yEnd-1,GameObjects.OBJ_WALL_GREY_STONE);
							setObject(xEnd,yEnd-1,GameObjects.OBJ_DOOR_BLUE);
							setObject(xEnd,yEnd,GameObjects.OBJ_VOID);
						}
					}
					setObject(xEnd+1,yEnd,GameObjects.OBJ_VOID);
					setObject(xEnd-1,yEnd,GameObjects.OBJ_VOID);
					setObject(xEnd,yEnd,GameObjects.OBJ_VOID);
					setObject(xStart,yStart,GameObjects.OBJ_VOID);
					setObject(xStart+1,yStart,GameObjects.OBJ_VOID);
					setObject(xStart-1,yStart,GameObjects.OBJ_VOID);
				}
			}
		}
		showLocation();
	}
}
