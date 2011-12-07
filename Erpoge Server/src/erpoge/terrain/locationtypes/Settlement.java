package erpoge.terrain.locationtypes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import erpoge.Coordinate;
import erpoge.Main;
import erpoge.characters.*;
import erpoge.characters.Character;
import erpoge.graphs.RectangleSystem;
import erpoge.terrain.*;
import erpoge.terrain.settlements.Building;
import erpoge.terrain.settlements.Service;
public class Settlement extends LocationGenerator {
	public RectangleSystem rectangleSystem;
	public HashMap<Integer, RectangleSystem> quarters = new HashMap<Integer, RectangleSystem>();
	public ArrayList<Building> buildings = new ArrayList<Building>();
	public HashMap<Integer, Character> dwellers = new HashMap<Integer, Character>();
	public ArrayList<Service> services = new ArrayList<Service>();
	public Settlement (Location location) {
		super(location);
	}
	
	public void markQuarter(int key, int width/*=8*/, int border/*=2*/) {
	// Разметить квартал - создать систему прямоугольников и занести её в Settlement::quarters
	// in: индекс пярмоугольника, на котором строится квартал, в Settlement::rectangles
		Rectangle r = rectangleSystem.rectangles.get(key);
		quarters.put(key, new RectangleSystem(this,r.x+1,r.y+1,r.width-2,r.height-2, width, border));
//		quarters.get(key).initialFindOuterSides();
	}
	public Service createService(Character dweller, int type, String name) {
		Service service = new Service(dweller,type,name);
		services.add(service);
		return service;
	}
	public class Road {
		private Coordinate start;
		private Coordinate end;
	}
}
