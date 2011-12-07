package erpoge.terrain.settlements;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;

import erpoge.Chance;
import erpoge.Main;
import erpoge.Utils;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.Location;
import erpoge.terrain.locationtypes.Settlement;
import erpoge.characters.Character;
import erpoge.graphs.RectangleSystem;

public class Village extends Settlement {
	public Village(Location location) {
		super(location);
		fillWithCells(1,0);
//		makePeaceful();
		rectangleSystem = new RectangleSystem(this,0,0,location.width,location.height,9,7); // Граф под дорогу, разделяющую город на части
		// Roads
		rectangleSystem.drawBorders(ELEMENT_FLOOR,GameObjects.FLOOR_GROUND,true);
//		rectangleSystem.initialFindOuterSides();
		Set<Integer> keys = rectangleSystem.rectangles.keySet();
		for (int k1 : keys) {
			// Строим кварталы
			Rectangle rec = rectangleSystem.rectangles.get(k1);
			markQuarter(k1, 8, 2);
			// objs will be used in upcoming loop
			ArrayList<Integer> objs=new ArrayList<Integer> (); 
			objs.add(51);
			objs.add(52);
			objs.add(53);
			RectangleSystem quarter = quarters.get(k1);
			Set<Integer> keys2 = quarter.rectangles.keySet();
			for (int k : keys2) {
			// Строим здания
				Rectangle r2 = quarter.rectangles.get(k);
				if (
					Utils.intersectArrays(
						quarter.outerSides.get(k),
						rectangleSystem.outerSides.get(k1)
					).size() < quarter.outerSides.get(k).size()
				) {
					// Строим здания только на тех местах, которые граничат с дорогой
					Building house = new Tavern(this, r2.x, r2.y, r2.width, r2.height);
					// Заполняем исключённые прямоугольники разными объектами
					for (Rectangle r : house.rectangleSystem.excluded.values()) {
						int numOfObjs = Math.round(r.width*r.height/7);
						placeSeveralObjects(objs, Chance.rand((int)Math.floor(numOfObjs/3*2),numOfObjs),r);
					}
					int  xs, ys, xe, ye;
					// Строим дорожку от двери
					if (house.doorSide==1) {
						xs=house.frontDoor.x;
						ys=house.frontDoor.y-1;
						xe=house.frontDoor.x;
						ye=r2.y-1;
					} else if (house.doorSide==2) {
						xs=house.frontDoor.x+1;
						ys=house.frontDoor.y;
						xe=r2.x+r2.width;
						ye=house.frontDoor.y;
					} else if (house.doorSide==3) {
						xs=house.frontDoor.x;
						ys=house.frontDoor.y+1;
						xe=house.frontDoor.x;
						ye=r2.y+r2.height;
					} else {
						xs=house.frontDoor.x-1;
						ys=house.frontDoor.y;
						xe=r2.x-1;
						ye=house.frontDoor.y;
					}
					line(xs,ys,xe,ye,ELEMENT_FLOOR,5);
					line(xs,ys,xe,ye,ELEMENT_REMOVE,0);
				} else {
					// Если же место для дома не граничит с дорогой, обрабатываем его отдельно
					int numOfObjs=Math.round(r2.width*r2.height/7);
					placeSeveralObjects(objs,Chance.rand((int)Math.floor(numOfObjs/3*2),numOfObjs),r2);
				}
			}
		}
		int iB = Chance.rand(0,buildings.size()-1);
		ArrayList<Integer> keys3 = new ArrayList<Integer> (buildings.get(iB).rectangleSystem.rectangles.keySet());
		int iR = keys3.get(Chance.rand(0, keys3.size()-1));
		
		location.startArea = buildings.get(iB).rectangleSystem.rectangles.get(iR);
//		Character trader = new Trader("human", "Steve", location, location.startArea.x, location.startArea.y);
		setObject(location.startArea.x+1, location.startArea.y+1, GameObjects.OBJ_ROCK_1);
		createPortal(location.startArea.x+1, location.startArea.y+1, location.world.createLocation(0, 0, 40, 40, "Empty", "Test"));
	}
}