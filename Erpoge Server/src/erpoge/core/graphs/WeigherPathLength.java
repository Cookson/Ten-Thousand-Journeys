package erpoge.core.graphs;

import java.util.ArrayList;
import java.util.HashMap;

import erpoge.core.TerrainBasics;
import erpoge.core.meta.Coordinate;

public class WeigherPathLength extends EdgeWeigher {
	public TerrainBasics terrain;
	public HashMap<Integer, int[][]> pathTables = new HashMap<Integer, int[][]>();
	public HashMap<Integer, Coordinate> vertexes = new HashMap<Integer, Coordinate>();
	public WeigherPathLength(TerrainBasics terrain, Graph<Coordinate> graph) {
		this.terrain = terrain;
		this.vertexes = graph.content;
	}
	@Override
	public int weigh(int vertex1, int vertex2) {
		return vertexes.get(vertex1).distance(vertexes.get(vertex2));
//		if (pathTables.containsKey(vertex1)) {
//			return pathTables.get(vertex1)[vertexes.get(vertex2).x][vertexes.get(vertex2).y];
//		} else if (pathTables.containsKey(vertex2)) {
//			return pathTables.get(vertex2)[vertexes.get(vertex1).x][vertexes.get(vertex1).y];
//		} else {
//			int[][] table = terrain.getPathTable(
//					vertexes.get(vertex1).x, vertexes.get(vertex1).y, 
//					vertexes.get(vertex2).x, vertexes.get(vertex2).y
//				);
//			pathTables.put(vertex1, table);
//			return table[vertexes.get(vertex2).x][vertexes.get(vertex2).y];
//		}
	}

}
