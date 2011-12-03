package erpoge.terrain.locationtypes;

import java.awt.Rectangle;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.characters.GeneratorCharacterGroup;
import erpoge.graphs.RectangleSystem;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.CellCollection;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.World;

import java.util.ArrayList;

public class Forest extends TerrainGenerator {
	public Forest(Location location) {
		super(location);
		fillWithCells(1,0);
		Chance ch50 = new Chance(50);
		// Choose starting position
		int x;
		int y;
		if (ch50.roll()) {
			y = (ch50.roll()) ? 1 : width - 2;
			x = Chance.rand(1, width - 10);
			startArea = new Rectangle(x, y, 8, 1);
		} else {
			x = (ch50.roll()) ? 1 : height - 2;
			y = Chance.rand(1, height - 10);
			startArea = new Rectangle(x, y, 1, 8);
		}

		// Objects creation
		// num=(w*h)/50;
		// for (i=0;i<num;i++) {
		// x=rand(0,w-1);
		// y=rand(0,h-1);
		// if (contents[x][y]["object"]) {
		// continue;
		// }
		// setObject(x,y,GameObjects.OBJ_TREE_2);
		// }

		// Mods creation
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		coords.add(new Coordinate(0, 0));
		coords.add(new Coordinate(width - 1, 0));
		coords.add(new Coordinate(width - 1, height - 1));
		coords.add(new Coordinate(0, height - 1));
		CellCollection mobsSpace = getCellCollection(polygon(coords));
		
		// foreach (mobsSpace->cells as v) {
		// write(v[0]." ".v[1]);
		// }
		
		ArrayList<GeneratorCharacterGroup> listcs = new ArrayList<GeneratorCharacterGroup>();
		listcs.add(new GeneratorCharacterGroup("bear", "Мишка", width * height / 150, 0));
		mobsSpace.placeCharacters(listcs);
		
		mobsSpace.forest(Chance.rand(5, 12));
		ArrayList<Integer> objtypes = new ArrayList<Integer>();
		objtypes.add(GameObjects.OBJ_DOOR_BLUE);
//		objtypes.add(GameObjects.OBJ_CHEST_1);
//		objtypes.add(GameObjects.OBJ_CHEST_2);
//		objtypes.add(GameObjects.OBJ_CHEST_3);
		mobsSpace.setObjects(objtypes, 40);
		objtypes.clear();
		objtypes.add(GameObjects.OBJ_ROCK_1);
		mobsSpace.setObjects(objtypes, 13);
		// Draw a road, if needed
//		 if (false) {
//			 if (ch50.roll()) {
//			 // Горизонтальная дорога
//			 int roadY=(int)Math.floor(height/2)+Chance.rand(0,3)-3;
//			 boldLine(0,roadY,width,roadY,ELEMENT_REMOVE,1,5);
//			 boldLine(0,roadY,width,roadY,ELEMENT_FLOOR,5);
//			 } else {
//			 // Вертикальная дорога
//			 int roadX=(int)Math.floor(width/2)+Chance.rand(0,3)-3;
//			 boldLine(roadX,0,roadX,height,ELEMENT_REMOVE,1,5);
//			 boldLine(roadX,0,roadX,height,ELEMENT_FLOOR,5);
//			 }
//		
//		 }
		
		// Cave
//		int caveW = Math.min(Chance.rand(6, 14), width - 2);
//		int caveH = Math.min(Chance.rand(6, 14), height - 2);
//		int caveX = Chance.rand(0, width - caveW);
//		int caveY = Chance.rand(0, width - caveH);
//		RectangleSystem caveRS = location.getGraph(caveX, caveY, caveW, caveH, 1, 0);
//		caveRS.nibbleSystem(1, 90);
//		caveRS.drawBorders(ELEMENT_OBJECT, GameObjects.OBJ_WALL_CAVE, false);
		
	}
}
