package erpoge.terrain.settlements;

import erpoge.objects.GameObjects;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.settlements.Building.BasisBuildingSetup;

public class House extends Building {
	public House(TerrainGenerator settlement, int x, int y, int width,
			int height, int minRoomSize) {
		super(settlement, x, y, width, height, minRoomSize);
		buildBasis(GameObjects.OBJ_WALL_WOODEN, BasisBuildingSetup.CONVERT_TO_DIRECTED_TREE);
		
	}
}
