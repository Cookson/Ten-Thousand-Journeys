package erpoge.terrain;
import java.util.ArrayList;

import erpoge.Main;


public class WorldCell extends Cell {
	public int river = 0;
	public int road = 0;
	public ArrayList<Integer> objects = new ArrayList<Integer>();
	public int race = 1;
	public WorldCell() {
		
	}
	WorldCell(int ground) {
		this(ground, 0, 0, 0, 0, new ArrayList<Integer>());
	}
	WorldCell(int ground, int forest) {
		this(ground, forest, 0, 0, 0, new ArrayList<Integer>());
	}
	WorldCell(int ground, int forest, int river) {
		this(ground, forest, river, 0, 0, new ArrayList<Integer>());
	}
	WorldCell(int ground, int forest, int river, int road) {
		this(ground, forest, river, road, 0, new ArrayList<Integer>());
	}
	WorldCell(int ground, int forest, int river, int road, int race) {
		this(ground, forest, river, road, race, new ArrayList<Integer>());
	}
	WorldCell(int ground, int object, int river, int road, int race, ArrayList<Integer> objects) {
		this.floor = ground;
		this.object = object;
		this.river = river;
		this.road = road;
		this.race = race;
		this.objects = objects;
	}
	public void setGround(int val) {
		floor = val;
	}
	public void setForest(int val) {
		object = val;
	}
	public void setRiver(int val) {
		river = val;
	}
	public void setRoad(int val) {
		road = val;
	}
	public void addObject(int val) {
		objects.add(val);
	}
	public void removeObject(int val) {
		objects.remove(val);
	}
	public void setRace(int val) {
		race = val;
	}
}
