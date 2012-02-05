package erpoge.terrain;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import erpoge.Coordinate;
import erpoge.Main;
import erpoge.RectangleArea;
import erpoge.graphs.Graph;
import erpoge.graphs.RectangleSystem;
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
	private int numberOfChunks = 0;
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
		if (chunks.get(x).containsKey(y)) {
			throw new Error("Chunk at "+x+":"+y+" already exists");
		}
		numberOfChunks++;
		return chunks.get(x).put(y, new Chunk(this, x, y));
	}
	public void touchChunk(int x, int y) {
		if (!hasChunk(x, y)) {
			createChunk(x,y);
		}
	}
	public Chunk getChunkWithCell(int x, int y) {
		int chX = (x < 0) ? x-((x%Chunk.WIDTH==0) ? 0 : Chunk.WIDTH)-x%Chunk.WIDTH : x-x%Chunk.WIDTH;
		int chY = (y < 0) ? y-((y%Chunk.WIDTH==0) ? 0 : Chunk.WIDTH)-y%Chunk.WIDTH : y-y%Chunk.WIDTH;
		try {
			return chunks.get(chX).get(chY);
		} catch (NullPointerException e) {
			Main.log("No chunk "+chX+":"+chY+" with cell "+x+":"+y);
			return null;
		}
	}
	public Chunk getChunkByCoord(int x, int y) {
		return chunks.get(x).get(y);
	}
	public boolean hasChunk(int x, int y) {
		return chunks.containsKey(x-x%Chunk.WIDTH) && chunks.get(x-x%Chunk.WIDTH).containsKey(y-y%Chunk.WIDTH);
	}
	public Location generateLocation(int x, int y, int width, int height, String type) {
		// Create new chunks
		for (int j=getChunkRoundedCoord(y); j<=y+height; j+=Chunk.WIDTH) {
			for (int i=getChunkRoundedCoord(x); i<=x+width; i+=Chunk.WIDTH) {
				touchChunk(i, j);
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
		int chunkX = getChunkRoundedCoord(x);
		int chunkY = getChunkRoundedCoord(y);
		// Difference between the start cell and the coordinate of a chunk it is in.
		int endX = x+width;
		int endY = y+height;
		for (int currX=x; currX<endX; chunkX+=Chunk.WIDTH, currX=chunkX) {	
			for (int currY=y; currY<endY; chunkY+=Chunk.WIDTH, currY=chunkY) {
			// For each chunk in the selected zone
				Chunk chunk = getChunkByCoord(chunkX,chunkY);
				int dxInResult = 0;
				for (int k=currX-chunkX; k<Chunk.WIDTH && chunkX+k!=endX; k++) {
				// Fill answer array with cells from chunk
					int dyInResult = 0;
					for (int l=currY-chunkY; l<Chunk.WIDTH && chunkY+l!=endY; l++) {
						answer[currX-x+dxInResult][currY-y+dyInResult++] = chunk.cells[k][l];
					}
					dxInResult++;
				}
			}
			// It IS neccessary!
			chunkY = getChunkRoundedCoord(y);
		}
		return answer;
	}
	/**
	 * Round a coordinate (x or y, works equal) down to the nearest value in 
	 * which may be a corner of a chunk.
	 * @param x x or y coordinate.
	 * @returns Rounded coordinate value.
	 */
	public int getChunkRoundedCoord(int coord) {
		return (coord < 0) ? coord-((coord%Chunk.WIDTH==0) ? 0 : Chunk.WIDTH)-coord%Chunk.WIDTH : coord-coord%Chunk.WIDTH;
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
//		HashSet<Chunk> chunks = new HashSet<Chunk>();
//		for (int j=y; j<y+height; j++) {
//			for (int i=x; i<x+width; i++) {
//				chunks.add(getChunk(x, y));
//			}
//		}
		return getChunkWithCell(x,y).jsonPartGetContents();
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
		return getChunkWithCell(x, y).getCell(x, y);
	}
	public void generateTerrain(int x, int y, int width, int height) {
		RectangleSystem graph = new RectangleSystem(x, y, width, height, 40, 0);
		for (RectangleArea r : graph) {
			generateLocation(r.x, r.y, r.width, r.height, "Forest");
		}
	}	
	
}
