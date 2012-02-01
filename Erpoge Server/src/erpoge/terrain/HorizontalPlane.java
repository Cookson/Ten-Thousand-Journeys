package erpoge.terrain;

import java.util.HashMap;
import java.util.HashSet;

import erpoge.Coordinate;
import erpoge.Main;
import erpoge.inventory.ItemPile;
import erpoge.inventory.UniqueItem;
import erpoge.serverevents.EventItemAppear;
import erpoge.terrain.locationtypes.BuildingTest;
import erpoge.terrain.locationtypes.Empty;
import erpoge.terrain.locationtypes.Forest;
import erpoge.terrain.locationtypes.Village;

public class HorizontalPlane {
	public HorizontalPlane upperPlane;
	public HorizontalPlane lowerPlane;
	public HashMap<Integer, HashMap<Integer, Chunk>> chunks = new HashMap<Integer, HashMap<Integer, Chunk>>();
	public HorizontalPlane() {
		
	}
	public Chunk createChunk(int x, int y) {
		if (x%Chunk.WIDTH != 0) {
			throw new Error("Wrong x "+x);
		}
		if (y%Chunk.WIDTH != 0) {
			throw new Error("Wrong y "+y);
		}
		if (!chunks.containsKey(x)) {
			chunks.put(x, new HashMap<Integer, Chunk>());
		}
		return chunks.get(x).put(y, new Chunk(this, x, y));
	}
	public Chunk getChunk(int x, int y) {
		return chunks.get(x-x%Chunk.WIDTH).get(y-y%Chunk.WIDTH);
	}
	public Chunk getChunkByCoord(int x, int y) {
		return chunks.get(x).get(y);
	}
	public boolean hasChunk(int x, int y) {
		return chunks.containsKey(x-x%Chunk.WIDTH) && chunks.get(x-x%Chunk.WIDTH).containsKey(y-y%Chunk.WIDTH);
	}
	public Location generateLocation(int x, int y, int width, int height, String type) {
		// Create new chunks
		for (int j=y-y%Chunk.WIDTH; j<=y+height; j+=Chunk.WIDTH) {
			for (int i=x-x%Chunk.WIDTH; i<=x+width; i+=Chunk.WIDTH) {
				createChunk(i, j);
			}
		}
		if (type.equals("Forest")) {
			return new Forest(this, x, y, width, height);
		} else if (type.equals("Empty")) {
			return new Empty(this, x, y, width, height);
		} else if (type.equals("BuildingTest")) {
			return new BuildingTest(this, x, y, width, height);
		} else if (type.equals("Village")) {
			return new Village(this, x, y, width, height);
		} else {
			throw new Error("No such location type "+type);
		} 
	}
	public Cell[][] getCells(int x, int y, int width, int height) {
		Cell[][] answer = new Cell[width][height];
		for (int j=y; j<y+height; j+=Chunk.WIDTH) {
			for (int i=x; i<x+width; i+=Chunk.WIDTH) {
			// Get chunk
				Chunk chunk = getChunkByCoord(i,j);
				for (int l=0; l<Chunk.WIDTH; l++) {
				// Fill answer array with cells from chunk
					for (int k=0; k<Chunk.WIDTH; k++) {
						answer[i-x+l][j-y+k] = chunk.cells[l][k];
					}
				}
			}
		}
		return answer;
	}
	/**
	 * Get contents of chunks in a particular area.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return 
	 */
	public String jsonPartGetContents(int x, int y, int width, int height) {
		HashSet<Chunk> chunks = new HashSet<Chunk>();
		for (int j=y; j<y+height; j++) {
			for (int i=x; i<x+width; i++) {
				chunks.add(getChunk(x, y));
			}
		}
		return "";
	}
	public void showTerrain(int x, int y, int width, int height) {
		Cell[][] cells = getCells(x, y, width, height);
		for (int j=0; j<height; j++) {
			for (int i=0; i<width; i++) {
				cells[i][j].show();
			}
			Main.outln();
		}
	}

	public Cell getCell(int x, int y) {
		return getChunk(x, y).getCell(x, y);
	}	
	
}
