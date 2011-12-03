package erpoge.terrain;

import java.util.ArrayList;

import erpoge.Coordinate;
import erpoge.characters.Character;

public class WorldGenerator extends TerrainGenerator {
	protected final String name;
	protected final World world;
	
	public WorldGenerator(World world) {
		super(world);
		this.world = world;
		cells = world.cells;
		characters = world.characters;
		name = "Лес генератора";
		passability = world.passability;
	}
}
