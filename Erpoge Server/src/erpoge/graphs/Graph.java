package erpoge.graphs;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import erpoge.Main;


public class Graph<ContentClass> implements Iterable<ContentClass> {
	public HashMap<Integer, ArrayList<Integer>> edges;
	public HashMap<Integer, ContentClass> excluded = new HashMap<Integer, ContentClass>();
	public int size;
	public HashMap<Integer, ContentClass> content;
	public Graph() {
		edges = new HashMap<Integer, ArrayList<Integer>>();
		size = 0;
		content = new HashMap<Integer, ContentClass>();
	}
	public int[][] getAdjacencyMatrix(EdgeWeigher weigher) {
		int size = edges.size();
		int[][] answer = new int[size][size];
		for (int i=0;i<size;i++) {
			for (int j=i+1;j<size;j++) {
				answer[i][j] = weigher.weigh(i,j);
				answer[j][i] = answer[i][j];
			}
		}
		return answer;
	}
	public ContentClass addVertex(ContentClass element) {
		edges.put(size, new ArrayList<Integer>());
		content.put(size, element);
		size++;
		return element;
	}
	public void showAdjacensyMatrix(int[][] matrix) {
		for (int i=0;i<size;i++) {
			for (int j=0;j<size;j++) {
				Main.out(matrix[i][j]+"\t");
			}
			Main.outln();
		}
	}
	public void link(int vertex1, int vertex2) {
		edges.get(vertex1).add(vertex2);
	}
	public void mutualLink(int vertex1, int vertex2) {
		edges.get(vertex1).add(vertex2);
		edges.get(vertex2).add(vertex1);
	}
	public void mutualUnlink(int vertex1, int vertex2) {
		edges.get(vertex1).remove(vertex2);
		edges.get(vertex2).remove(vertex1);
	}
	public void unlink(int vertex1, int vertex2) {
		edges.get(vertex1).remove(vertex2);
	}
	public void getMinimumSpanningTree(EdgeWeigher weigher) {
		int n = edges.size();
		int[][] g = getAdjacencyMatrix(weigher);
//		showAdjacensyMatrix(g);
		int e[] = new int[n];
		e[0] = 0;
		for (int i=0;i<n-1;i++) {
			int minStart = -1;
			int minEnd = -1;
			
			for (int j=0; j<=i; j++) {
				loopK: for (int k=0;k<n;k++) {
					for (int l=0;l<=i;l++) {
						if (e[l] == k) {
							continue loopK;
						}
					}
					if (minStart == -1 || g[e[j]][k]<g[minStart][minEnd]) {
						minStart = e[j];
						minEnd = k;
					}
				}					                               
			}
			e[i+1] = minEnd;
			edges.get(minEnd).add(minStart);
		}
	}
	public void excludeIsolatedVertexes() {
		Set<Integer> keys = edges.keySet();
		Set<Integer> forRemoval = new HashSet<Integer>();
		for (int k : keys) {
			if (edges.get(k).size()==0) {
				forRemoval.add(k);
				excluded.put(k, content.get(k));
				content.remove(k);
			}
		}
	}
	public boolean isConnected() {
		Set<Integer> keys = content.keySet();
		ArrayList<Integer> checkedEdges = new ArrayList<Integer>();
		checkedEdges.add(keys.iterator().next());
		int size1 = 1;
		int size2;
		do {
			size2 = size1;
			for (int k1=0; k1<checkedEdges.size();k1++) {
				int v = checkedEdges.get(k1);
				ArrayList<Integer> edgesV = edges.get(v);
				for (int k2 : keys) {
					if (checkedEdges.contains(k2)) {
						continue;
					}
					if (edgesV.contains(k2) || edges.get(k2).contains(v)) {
						checkedEdges.add(k2);
					}
				}
			}
			size1=checkedEdges.size();
		} while (size1 != size2);
		return size1 == edges.size();
	}
	@Override
	public Iterator<ContentClass> iterator() {
		return content.values().iterator();
	}
}
