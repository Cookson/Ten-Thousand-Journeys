package erpoge.graphs;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import erpoge.Chance;
import erpoge.Coordinate;
import erpoge.Main;
import erpoge.Utils;
import erpoge.objects.GameObjects;
import erpoge.terrain.Cell;
import erpoge.terrain.TerrainBasics;

public class RectangleSystem extends Graph<Rectangle> {
	// ����� ��� ��������� ����������� ������������� ������� ������� �� �������
	// ��������������
	// � ������������� ���� ������� � ���� �����,
	// ��� ������� - ��������������, � ���� - ������� �������������� � ���
	// �������
	
	public int width;
	public int height;
	public int startX;
	public int startY;
	public int minRectangleWidth;
	public int borderWidth;
	public TerrainBasics location;
	public HashMap<Integer, ArrayList<Integer>> outerSides;
	private HashMap<Integer, Rectangle> outerRectangles = new HashMap<Integer, Rectangle>();

	/*
	 * ��������������, ������� ��������� ������� ��� ������������� ������ ���
	 * ����� �� ���������������, ��. ������� getGraph
	 */
	public HashMap<Integer, Rectangle> rectangles;

	public RectangleSystem(TerrainBasics loc, int startX, int startY, int w,
			int h, int minRectangleWidth) {
		this(loc, startX, startY, w, h, minRectangleWidth, 1);
	}

	public RectangleSystem(TerrainBasics loc, int sx, int sy, int w, int h,
			int minrw, int bw) {
		/*
		 * location - ������ �������, �� ������� �������� ����;
		 * startX,startY,width,height - �������, � ������� ��������� ����;
		 * minRectangleWidth - ����������� ������/������ ���������������, ��
		 * ������� ���������� ����; borderWidth - ������ (� �������) �������
		 * ����� ����������������;
		 */
		width = w;
		height = h;
		location = loc;
		startX = sx;
		startY = sy;
		edges = new HashMap<Integer, ArrayList<Integer>>();
		excluded = new HashMap<Integer, Rectangle>();
		rectangles = content;
		rectangles.put(0, new Rectangle(sx, sy, w, h));
		minRectangleWidth = minrw;
		borderWidth = bw;

		boolean noMoreRectangles = false;
		int splitableRecSizeLimit = minRectangleWidth * 2 + borderWidth + 1;
		Chance ch = new Chance(50);
		while (!noMoreRectangles) {
			noMoreRectangles = true;
			int size = rectangles.size();
			for (int i = 0; i < size; i++) {
				if (rectangles.get(i).width > splitableRecSizeLimit
						&& rectangles.get(i).height > splitableRecSizeLimit) {
					noMoreRectangles = false;
					splitRectangle(i, ch.roll() ? true : false);
				} else if (rectangles.get(i).width > splitableRecSizeLimit) {
					noMoreRectangles = false;
					splitRectangle(i, true);
				} else if (rectangles.get(i).height > splitableRecSizeLimit) {
					noMoreRectangles = false;
					splitRectangle(i, false);
				}
			}
		}
		buildEdges();
		initialFindOuterSides();
	}

	public RectangleSystem(CustomRectangleSystem crs) {
		this.rectangles = crs.rectangles;
		this.edges = crs.edges;
		this.borderWidth = crs.borderWidth;
		this.startX = crs.startX;
		this.startY = crs.startY;
		this.width = crs.width;
		this.height = crs.height;
		this.location = crs.location;
		this.edges = new HashMap<Integer, ArrayList<Integer>>();
		this.excluded = new HashMap<Integer, Rectangle>();
		this.outerSides = new HashMap<Integer, ArrayList<Integer>>();
		buildEdges();
		findOuterSidesOfComplexForm();
	}

	public int size() {
		return rectangles.size();
	}

	public void splitRectangle(int i, boolean dir) {
		// ��������� ������������� �� ���, ������ �� ������� �� ������ � ��
		// ������ ������ minRectangleWidth*2
		// i - ������ �������������� � ������� rectangles
		// dir - ����������� ����������� ����� - ������������ (true) ���
		// �������������� (false)
		Rectangle r = rectangles.get(i);
		if (dir) {
			// ������������
			int x = Chance.rand(r.x + minRectangleWidth, r.x + r.width
					- minRectangleWidth - 1 - borderWidth + 1);
			rectangles.put(i, new Rectangle(r.x, r.y, x - r.x, r.height));
			rectangles.put(rectangles.size(),
					new Rectangle(x + 1 + borderWidth - 1, r.y, r.x + r.width
							- x - 1 - borderWidth + 1, r.height));
		} else {
			// ��������������
			int y = Chance.rand(r.y + minRectangleWidth, r.y + r.height
					- minRectangleWidth - 1 - borderWidth + 1);
			rectangles.put(i, new Rectangle(r.x, r.y, r.width, y - r.y));
			rectangles.put(rectangles.size(), new Rectangle(r.x, y + 1
					+ borderWidth - 1, r.width, r.y + r.height - y - 1
					- borderWidth + 1));
		}
	}

	public void buildEdges() {
		// ��������� ���� � �����
		int len = rectangles.size();
		edges = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < len; i++) {
			edges.put(i, new ArrayList<Integer>());
		}
		for (int i = 0; i < len; i++) {
			Rectangle r1 = rectangles.get(i);
			for (int j = i + 1; j < len; j++) {
				Rectangle r2 = rectangles.get(j);
				if (areRectanglesNear(r1, r2)) {
					edges.get(i).add(j);
					edges.get(j).add(i);
				}
			}
		}
	}

	public boolean areRectanglesNear(Rectangle rec1, Rectangle rec2) {
		// ������������� �� �������������� ��������������� ��� �������������
		// ��������� � ������ borderWidth
		// if (rec1.x > rec2.x) {
		// // � rec1 ������ ��������� �������������, ����������� �����
		// Rectangle buf = rec1;
		// rec1 = rec2;
		// rec2 = buf;
		// }
		// if (
		// rec1.x <= rec2.x
		// && rec2.x <= rec1.x + rec1.width - 1
		// && (rec1.y == rec2.y + rec2.height + borderWidth || rec2.y == rec1.y
		// + rec1.height + borderWidth)
		// ||
		// (rec1.y <= rec2.y && rec2.y <= rec1.y + rec1.height - 1 || rec2.y <=
		// rec1.y && rec1.y <= rec2.y + rec2.height - 1)
		// && rec1.x + rec1.width + borderWidth == rec2.x) {
		// return true;
		// }
		int x2 = rec1.x + rec1.width + borderWidth;
		int y2 = rec1.y + rec1.height + borderWidth;
		int x4 = rec2.x + rec2.width + borderWidth;
		int y4 = rec2.y + rec2.height + borderWidth;
		// (x1<=x4)&(x2>=x3) (y1<=y4)&(y2>=y3)
		if (rec1.x <= x4 && x2 >= rec2.x && rec1.y <= y4 && y2 >= rec2.y) {
			return true;
		}
		return false;
	}

	public void drawBorders(int type, int name, boolean onlyInner) {
		// ��������� ������� ����� ��������� type ���� name
		// ���� onlyInner=true, �� ����������� ��� ���������� �������
		// ��������������, ������������� ������� ���������������,
		// ����� ���������� ������� ����� ���������������, ������������
		// ��������.
		// �����, ���� onlyInner=false, �� ������ ������������� �������
		// ������������ ��������� �� ��������� type,name
		// � ���� ���� ������� ������������ ������ ��������� � ������
		// ������������������� (onlyInner=false �������)
		// ������� ���������, ��� ��� onlyInner=true ���������� � ����������
		// ������� ���������������,
		// ������� ���� ��������� �� ������� � �������
		// RectangleSystem::excludeRectangle.

		if (onlyInner) {
			if (borderWidth == 0) {
				throw new Error(
						"An attempt to draw inner borders of a rectangleSystem with borderWidth = 0");
			}
			// // ����������� ������ ���������� ������
			// // ���������� ���������� �������, ������� ��������� ����
			// Cell[][] bufContents = new Cell[width][height];
			//
			// int endX = startX + width - 1;
			// int endY = startY + height - 1;
			// for (int x = startX; x <= endX; x++) {
			// for (int y = startY; y <= endY; y++) {
			// // �������� ���������� ������ ������, ��������� �������
			// // ����� ������� � �������� ��������� ������
			// bufContents[x - startX][y - startY] = new Cell(
			// location.cells[x][y]);
			//
			// }
			// }
			// // ��������� ��� ������� ����� ��������� type,name
			// // ������ ����� type - � TerrainBasics
			//
			// location.square(startX, startY, width, height, type, name, true);
			//
			// // ������ ������� ������� ������ ��������������� ����� - �����
			// // �������, ��������� ������ ��������� ����� ������ �������
			// for (Map.Entry<Integer, Rectangle> e : rectangles.entrySet()) {
			// Rectangle r = e.getValue();
			// endX = r.x + r.width - 1;
			// endY = r.y + r.height - 1;
			// for (int x = r.x; x <= endX; x++) {
			// for (int y = r.y; y <= endY; y++) {
			// location.cells[x][y] = bufContents[x - startX][y
			// - startY];
			// }
			// }
			// }
			Set<Integer> keys = rectangles.keySet();
			for (int k : keys) {
				Rectangle r = rectangles.get(k);
				if (!outerSides.get(k).contains(SIDE_E)) {
					location.square(r.x + r.width, r.y, borderWidth, r.height,
							type, name, true);
				}
				if (!outerSides.get(k).contains(SIDE_S)) {
					location.square(r.x, r.y + r.height, r.width, borderWidth,
							type, name, true);
				}
				if (!outerSides.get(k).contains(SIDE_S)
						&& !outerSides.get(k).contains(SIDE_E)) {
					location.square(r.x + r.width, r.y + r.height, borderWidth,
							borderWidth, type, name, true);
				}
			}
		} else {
			// ����������� ������ ���� ���������������
			for (Map.Entry<Integer, Rectangle> e : rectangles.entrySet()) {
				Rectangle r = e.getValue();
				location.square(r.x - 1, r.y - 1, r.width + 2, r.height + 2,
						type, name);
			}
		}
	}

	public void convertGraphToDirectedTree() {
		convertGraphToDirectedTree(-1);
	}

	public void addVertexToTree(int vertex) {
		addVertexToTree(vertex, -1);
	}

	public void addVertexToTree(int vertex, int previousVertex) {
		// ������������ ��� ���������� ����������������� ������
		// ��� �������������� ���� ���� ������� �������� �� ������������ ��
		// ������� (��� ��, ��� isolateVertex),
		// ��, � ������� �� isolateVertex, ������ �� ������ � ������������� �
		// ���� ������� ������.
		for (int v : edges.get(vertex)) {
			// ����� ���� ������ ���� �������, ���������� � nextVertex,
			// � ������� �� ��� nextVertex
			if (v == previousVertex) {
				continue;
			}
			int k = edges.get(v).indexOf(vertex);
			edges.get(v).remove(k);
		}
	}

	public ArrayList<Integer> convertGraphToDirectedTree(int currVertex) {
		// ������������� �������������� ������� ���� � ��������������� ������,
		// ������� �������� �������� ������� currVertex
		// (���� currVertex �� ������ - ��� ���������� ��������)
		// ���������� ������ �� ��������� ������-�������,
		// ��� ������� ������� - currVertex

		Set<Integer> keys = new HashSet<Integer>(edges.keySet());
		ArrayList<Integer> deadEnds = new ArrayList<Integer>();
		if (keys.size() == 1) {
			// ����� �� ������� ��� ������� �� ������ ��������������
			return null;
		}
		if (currVertex == -1) {
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			int size = 0;
			for (int i : keys) {
				indexes.add(i);
				size++;
			}

			currVertex = indexes.get(Chance.rand(0, size - 1));
		}
		int startVertex = currVertex;
		// ������������� ������ �������
		addVertexToTree(currVertex);
		ArrayList<ArrayList<Integer>> chainBranches = new ArrayList<ArrayList<Integer>>();
		chainBranches.add(new ArrayList<Integer>());
		chainBranches.get(0).add(currVertex);
		// �������� ������ ���������
		// �������� ��� ������������������ ������, ���������� ����� ����
		// [0] ���������� �� startVertex
		// ����������� ����� ���������� � �������, ������� ��� ������������ �
		// �����
		// (����, �� ���� �������� � �������� - � ����������) ������
		int currBranch = 0; // ������ ������� �����
		boolean nextVertexFound = true;
		// num=0; // �����
		ArrayList<Integer> usedVertexes = new ArrayList<Integer>();
		usedVertexes.add(startVertex);
		Chance ch20 = new Chance(20);
		chooseRand: while (nextVertexFound) {
			// �� ���� ������� �������� ��������� �� ���������� � ��� � �������
			// ��� ��������� ������� ��� ���� edges, �������� ��������� �
			// �������,
			// ����� ���� ��������� (nextCell) ���������� �������
			if (edges.get(currVertex).size() == 0 || ch20.roll()
					&& edges.get(currVertex).size() > 1 && currBranch > 1 /*
																		 * ������
																		 * �
																		 * ����
																		 * ��� ,
																		 * �����
																		 * �
																		 * �����
																		 * �
																		 * �����
																		 * �
																		 * �����
																		 * � .
																		 * currBranch
																		 * >1
																		 * �����
																		 * ,
																		 * �����
																		 * �
																		 * �����
																		 * ���
																		 * �����
																		 * �����
																		 * (��
																		 * ch20)
																		 * ��
																		 * ���
																		 * ���
																		 * ����
																		 * ��
																		 * ���
																		 * ���
																		 * ��
																		 * �����
																		 */) {
				// ���� � ������� �������� �� ��������� ������ ������, �������
				// ���� �� ���������� ������
				currBranch++;
				nextVertexFound = false;
				for (int i = 0; i < currBranch; i++) {
					// ������� ���������� �����
					for (int j = 0; j < chainBranches.get(i).size(); j++) {
						// ������� ��� ������� �����
						int vertexFromBranch = chainBranches.get(i).get(j);
						if (edges.get(vertexFromBranch).size() > 0) {
							// ���� � ���� �������� �� ���������� �����
							// ��������� �����-�� ���� ��� ��������� �������,
							// �������� �� ���� �������
							Collections.sort(edges.get(vertexFromBranch),
									Collections.reverseOrder());
							// ������� ������� �������� ��������� �������, �
							// ������� ������� (� ��� �������) ��������� �������
							// �� ���������� �����

							// ���� ��������� ������ ������, � ��������
							// vertexFromBranch ����� �������� �����
							ArrayList<Integer> links = new ArrayList<Integer>();

							for (int vertex : edges.get(vertexFromBranch)) {
								// ���� ��� �������
								if (!usedVertexes.contains(vertex)) {
									links.add(vertex);
								}
							}
							// � �������� ��������� ��������� �� ���
							if (links.size() == 0) {
								continue;
							}
							currVertex = links.get(Chance.rand(0,
									links.size() - 1));
							nextVertexFound = true;
							int size = chainBranches.size();
							chainBranches.add(new ArrayList<Integer>());
							chainBranches.get(size).add(vertexFromBranch);
							chainBranches.get(size).add(currVertex);
							addVertexToTree(currVertex, vertexFromBranch);
							usedVertexes.add(currVertex);

							continue chooseRand;
						}
					}
					if (nextVertexFound) {
						break;
					}
				}
			}

			if (nextVertexFound) {
				// Collections.sort(edges.get(currVertex),
				// Collections.reverseOrder()); // ���������� null-��������
				// � ������� ������� �
				// ����� �������
				int nextVertex = edges.get(currVertex).get(
						Chance.rand(0, edges.get(currVertex).size() - 1));
				chainBranches.get(currBranch).add(nextVertex);
				addVertexToTree(nextVertex, currVertex);
				usedVertexes.add(nextVertex);
				currVertex = nextVertex;
			}
		}
		// ������� ���� ���������� �������� ���� ���������� �������
		// chainBranches ��������� ����� ������
		Set<Integer> c = edges.keySet();
		Iterator<Integer> it = c.iterator();
		while (it.hasNext()) {
			// ���� ������� ������ chainBranches, �� �������� �������...
			edges.put(it.next(), new ArrayList<Integer>());
		}
		int size1 = chainBranches.size();
		for (int i = 0; i < size1; i++) {
			// ...� ��������������� ��������� ������� � ������ �����
			currVertex = 0;
			int nextVertex = 0;
			int size2 = 0;
			try {
				nextVertex = chainBranches.get(i).get(0);
				size2 = chainBranches.get(i).size();
			} catch (IndexOutOfBoundsException e) {

			}
			for (int j = 1; j < size2; j++) {
				currVertex = nextVertex;
				nextVertex = chainBranches.get(i).get(j);
				edges.get(currVertex).add(nextVertex);
			}
			deadEnds.add(chainBranches.get(i).get(size2 - 1));
		}
		if (edges.get(startVertex).size() == 1) {
			deadEnds.add(startVertex);
		}
		return deadEnds;
	}

	public void convertDoubleEdgesToSingle() {
		for (int k1 : edges.keySet()) {
			ArrayList<Integer> edge1 = edges.get(k1);
			for (int k2 = 0; k2 < edge1.size(); k2++) {
				ArrayList<Integer> edge2 = edges.get(edge1.get(k2));
				if (edge2.contains(k1)) {
					edge2.remove(edge2.indexOf(k1));
				}
			}
		}
	}

	public void initialFindOuterSides() {
		/*
		 * ��������� ��� ������� ��������������, � ������ �� ������ ������
		 * ��������������, ������������ ������� �����, ��� ��������. ����������
		 * ������������ � ������ Graph::outerSides, ��� ������ - �����
		 * ��������������, �������� - ������ ������� ������, � ��������
		 * ������������� ��������.
		 */
		outerSides = new HashMap<Integer, ArrayList<Integer>>();
		int size = 0;

		Set<Integer> rkeys = rectangles.keySet();
		Iterator<Integer> it = rkeys.iterator();
		while (it.hasNext()) {
			int key = it.next();
			Rectangle r = rectangles.get(key);
			outerSides.put(key, new ArrayList<Integer>());
			try {
				if (r.y == startY) {
					// ������� �������
					outerSides.get(key).add(1);
				}
				if (r.x + r.width == startX + width) {
					// ������
					outerSides.get(key).add(2);
				}
				if (r.y + r.height == startY + height) {
					// ������
					outerSides.get(key).add(3);
				}
				if (r.x == startX) {
					// �����
					outerSides.get(key).add(4);
				}
			} catch (Exception e) {
			}
			size++;
		}
	}

	public void findOuterSidesOfComplexForm() {
		// Initial search for outer sides in custom rectangle system of
		// non-squared form
		for (int i : rectangles.keySet()) {
			outerSides.put(i, new ArrayList<Integer>());
			Rectangle r = rectangles.get(i);
			ArrayList<Integer> thisEdges = edges.get(i);
			HashMap<Integer, Integer> sides = new HashMap<Integer, Integer>();
			sides.put(SIDE_N, r.width);
			sides.put(SIDE_E, r.height);
			sides.put(SIDE_S, r.width);
			sides.put(SIDE_W, r.height);
			for (int j : thisEdges) {
				Rectangle r2 = rectangles.get(j);
				if (areRectanglesNear(r, r2)) {
					int side = getNeighborSide(r, r2);
					sides.put(side, sides.get(side)
							- lengthOfAdjacenctZone(r, r2));
				} else {
					throw new Error(":   |");
				}
			}
			ArrayList<Integer> thisOuterSides = outerSides.get(i);
			for (Map.Entry<Integer, Integer> e : sides.entrySet()) {
				if (e.getValue() > 0) {
					thisOuterSides.add(e.getKey());
				}
			}
		}
	}

	public void excludeRectangle(int num) {
		// ��������� ������������� �� �������, ������ ���, ��� ���� � ������ �
		// ���������� � ��� ��������
		// ����� ���������, ��� ����� ����� ��� �������� ���������
		// ��������������� �� ����� ��������������,
		// ��� ���������� ����� ��������� ��� �������� ������� ��������������� �
		// ����� � ����������
		if (!rectangles.containsKey(num)) {
			throw new Error("The rectangle system already has no �" + num
					+ " rectangle");
		}
		excluded.put(num, rectangles.get(num));

		// Add outer sides to nearby rectangles
		Rectangle r = rectangles.get(num);
		for (int neighbor : edges.get(num)) {
			Rectangle nr = rectangles.get(neighbor);

			// if (side == SIDE_N && nr.y == r.y + r.height + borderWidth
			// && !outerSides.get(neighbor).contains(1) || side == SIDE_E
			// && r.x == nr.x + nr.width + borderWidth
			// && !outerSides.get(neighbor).contains(2) || side == SIDE_S
			// && r.y == nr.y + nr.height + borderWidth
			// && !outerSides.get(neighbor).contains(3) || side == SIDE_W
			// && nr.x == r.x + r.width + borderWidth
			// && !outerSides.get(neighbor).contains(4)) {
			// outerSides.get(neighbor).add(side);
			// if (outerRectangles != null
			// && outerRectangles.containsKey(neighbor)) {
			// outerRectangles.put(neighbor,
			// outerRectangles.get(neighbor));
			// }
			// }
			int side = getNeighborSide(nr, r);
			ArrayList<Integer> neighborOuterSides = outerSides.get(neighbor);
			if (!neighborOuterSides.contains(side)) {
				neighborOuterSides.add(side);
			}
		}
		outerSides.remove(num);

		rectangles.remove(num); // ������� �������������...
		if (outerRectangles.containsKey(num)) {
			outerRectangles.remove(num);
		}
		edges.remove(num); // ��� ����...
		Set<Integer> keys = edges.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			// ������� ���������� ����� �������������� � ����������...
			int k = it.next();
			ArrayList<Integer> e = edges.get(k);
			int pos = e.indexOf(num);
			if (pos != -1) {
				edges.get(k).remove(pos);
			}
		}
	}

	public boolean isVertexExclusible(int vertex) {
		// ���������, ��������� �� ���� �������, ���� ��������� ������� vertex
		// � ���� ��������� � vertexes ����������� �������� ��� ������ �������
		// (������ - ����� �������, �������� - ��������)
		// �������� 0 ������, ��� ������� �� �������� ����������
		// �������� 1 ������, ��� ������� ����� ���� ���������� �� ����� ��
		// ������ �� ��������� 2
		// �������� 2 ������, ��� ������� ��� �� ����� ���������
		// �������� �����������, ���� �� �������� ������� �� ��������� 0 ��� ��
		// �������� ������� �� ��������� 1
		// � ������ ������ ��� ����� �������, ��� ���� �������, �� ������ - ���
		// �� �������
		HashMap<Integer, Integer> vertexes = new HashMap<Integer, Integer>();
		int v0 = -1; // ���������� ������ �� ��������� 0 (����������� �� ����
		// ���������) (-1, �.�. ���� �� ������ ����� ����� ��
		// ��������� 1)
		int v1 = 1; // ���������� ������ �� ��������� 1 (������������� �� ����
		// ���������)
		Set<Integer> keys = edges.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			int k = it.next();
			if (k == vertex) {
				continue;
			}
			vertexes.put(k, 0);
			v0++;
		}
		it = keys.iterator();
		while (it.hasNext()) {
			int k = it.next();
			// ���� ��������� ������� (�����, �� ��� ����������, ������� �
			// ������ ��������� ���� � �����)
			if (k == vertex) {
				continue;
			}
			vertexes.put(k, 1);
			break;
		}
		while (true) {
			keys = vertexes.keySet();
			it = keys.iterator();
			while (it.hasNext()) {
				// ���� ����� ������� �� ��������� 1
				int k = it.next();
				int v = vertexes.get(k);
				if (v != 1 || k == vertex) {
					continue;
				}
				for (int ve : edges.get(k)) {
					// ����������� ���� � �������� �������� �� ��������� 0
					// �������� 1
					if (ve == vertex) {
						continue;
					}
					if (vertexes.get(ve) == 0) {
						vertexes.put(ve, 1);
						v1++;
						v0--;
					}
				}
				vertexes.put(k, 2);
				v1--;
			}
			if (v0 == 0) {
				return true;
			} else if (v1 == 0) {
				return false;
			}
		}
	}

	public int findRectangleByCell(int x, int y) {
		// ����� � ������� ������������� �� ������ ����������� ������������� ���
		// ������
		// ���������� ������ �������������� � rectangles.
		// ���� ������ �� ��������� �� � ������ ��������������, ����������
		// false.
		// ��������: ������������� � rectangles ����� ����� ������ 0,
		// ������� ���������� ��������� � false ����� �� ���������� �����
		// (===/!==)
		Set<Integer> keys = rectangles.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			// ���� ����� ������� �� ��������� 1
			int k = it.next();
			Rectangle r = rectangles.get(k);
			if (rectangleHasCell(r, x, y)) {
				return k;
			}
		}
		return -1;
	}

	public static boolean rectangleHasCell(Rectangle r, int cellX, int cellY) {
		// ����������� �� �������������� ����� [cellX;cellY]
		return cellX <= r.x + r.width - 1 && cellX >= r.x
				&& cellY <= r.y + r.height - 1 && cellY >= r.y;
	}

	public static boolean isRectangleInCircle(Rectangle rectangle, int cx,
			int cy, int r) {
		Coordinate c = new Coordinate(cx, cy);
		if (c.distance(rectangle.x, rectangle.y) > r) {
			return false;
		}
		if (c.distance(rectangle.x + rectangle.width - 1, rectangle.y) > r) {
			return false;
		}
		if (c.distance(rectangle.x, rectangle.y + rectangle.height - 1) > r) {
			return false;
		}
		if (c.distance(rectangle.x + rectangle.width - 1, rectangle.y
				+ rectangle.height - 1) > r) {
			return false;
		}
		return true;
	}

	public boolean hasRectangleWithNum(int i) {
		return rectangles.containsKey(i);
	}

	public Set<Integer> getRectanglesKeys() {
		return new HashSet<Integer>(rectangles.keySet());
	}

	public void nibbleSystem(int depth, int chance) {
		// Gets some of the system's outer rectangles, removes them
		// with %chance% percent chance, gets new outer rectangles
		// and repeats this %depth% times.
		for (int k = 0; k < depth; k++) {
			Set<Integer> keys = new HashSet<Integer>();
			for (int i : outerSides.keySet()) {
				if (outerSides.get(i).size() > 0) {
					keys.add(i);
				}
			}
			for (int i : keys) {
				if (Chance.roll(chance)) {
					continue;
				}
				excludeRectangle(i);
			}
		}
	}

	public int getRectangleNumByCoord(int x, int y) {
		for (int k : rectangles.keySet()) {
			Rectangle r = rectangles.get(k);
			if (r.x == x && r.y == y) {
				return k;
			}
		}
		return -1;
	}

	public int getRandomRectangleNum() {
		ArrayList<Integer> keys = new ArrayList<Integer>(rectangles.keySet());
		if (keys.size() == 0) {
			throw new Error("system has no rectangles");
		}
		return keys.get(Chance.rand(0, keys.size() - 1));
	}

	public Rectangle getRandomRectangle() {
		return rectangles.get(getRandomRectangleNum());
	}

	public Rectangle getRandomOuterRectangle() {
		ArrayList<Integer> keys = new ArrayList<Integer>(outerRectangles
				.keySet());
		return outerRectangles.get(keys.get(Chance.rand(0, keys.size() - 1)));
	}

	public int getRandomOuterRectangleNum() {
		ArrayList<Integer> keys = new ArrayList<Integer>(outerRectangles
				.keySet());
		return keys.get(Chance.rand(0, outerRectangles.size() - 1));
	}

	public void excludeRectanglesHaving(int type, int val) {
		Set<Integer> keys = new HashSet<Integer>(rectangles.keySet());
		keyLoop: for (int i : keys) {
			Rectangle r = rectangles.get(i);
			for (int x = r.x; x < r.x + r.width; x++) {
				for (int y = r.y; y < r.y + r.height; y++) {
					if (location.cells[x][y].getElement(type) == val) {
						excludeRectangle(i);
						continue keyLoop;
					}
				}
			}
		}
	}

	public ArrayList<Integer> getOuterRectanglesNums() {
		ArrayList<Integer> answer = new ArrayList<Integer>();
		Set<Integer> keys = outerSides.keySet();
		for (int k : keys) {
			if (outerSides.get(k).size() > 0) {
				answer.add(k);
			}
		}
		return answer;
	}

	public void detectOuterRectangles() {
		Set<Integer> keys = outerSides.keySet();
		for (int k : keys) {
			if (outerSides.get(k).size() > 0) {
				outerRectangles.put(k, rectangles.get(k));
			}
		}
	}

	private static class ccwlLastCellHolder {
		// A class that is used by connectCornersWithLines() method to store
		// lastCell variable for custom comparator.
		public static Coordinate center;;
	}

	public void connectCornersWithLines(int type, int value, int padding,
			boolean considerBorderWidth) {
		ccwlLastCellHolder.center = new Coordinate(Math.round(startX + width
				/ 2), Math.round(startY + height / 2));
		ArrayList<Coordinate> corners = new ArrayList<Coordinate>();
		Comparator<Coordinate> comparator = new Comparator<Coordinate>() {
			@Override
			public int compare(Coordinate c1, Coordinate c2) {

				return new Double(Utils.getLineAngle(ccwlLastCellHolder.center,
						c1)).compareTo(Utils.getLineAngle(
						ccwlLastCellHolder.center, c2));
			}
		};
		for (int i : getRectanglesKeys()) {
			ArrayList<Integer> sides = outerSides.get(i);
			Rectangle r = rectangles.get(i);
			boolean n = sides.contains(SIDE_N);
			boolean e = sides.contains(SIDE_E);
			boolean s = sides.contains(SIDE_S);
			boolean w = sides.contains(SIDE_W);
			if (n && e) {
				corners.add(new Coordinate(r.x + r.width - 1
						+ (considerBorderWidth ? borderWidth : 0) + padding,
						r.y + (considerBorderWidth ? -borderWidth : 0)
								- padding));

			}
			if (e && s) {
				corners.add(new Coordinate(r.x + r.width - 1
						+ (considerBorderWidth ? borderWidth : 0) + padding,
						r.y + r.height - 1
								+ (considerBorderWidth ? borderWidth : 0)
								+ padding));
			}
			if (s && w) {
				corners.add(new Coordinate(r.x
						+ (considerBorderWidth ? -borderWidth : 0) - padding,
						r.y + r.height - 1
								+ (considerBorderWidth ? borderWidth : 0)
								+ padding));
			}
			if (w && n) {
				corners.add(new Coordinate(r.x
						+ (considerBorderWidth ? -borderWidth : 0) - padding,
						r.y + (considerBorderWidth ? -borderWidth : 0)
								- padding));
			}
		}
		Collections.sort(corners, comparator);
		Coordinate c1 = corners.get(0);
		int size = corners.size();
		for (int i = 1; i < size; i++) {
			Coordinate c2 = corners.get(i);
			location.line(c1.x, c1.y, c2.x, c2.y, type, value);
			c1 = c2;
			if (i == size - 1) {
				location.line(c2.x, c2.y, corners.get(0).x, corners.get(0).y,
						type, value);
			}

		}
	}

	public int getNeighborSide(Rectangle rectangle, Rectangle neighbor) {
		// Get side from which neighbor is located relatively to the first
		// rectangle
		if (rectangle.y == neighbor.y + neighbor.height + borderWidth) {
			return SIDE_N;
		}
		if (rectangle.x + rectangle.width + borderWidth == neighbor.x) {
			return SIDE_E;
		}
		if (rectangle.y + rectangle.height + borderWidth == neighbor.y) {
			return SIDE_S;
		}
		if (rectangle.x == neighbor.x + neighbor.width + borderWidth) {
			return SIDE_W;
		}
		throw new Error("Cannot find side of neighbor rectangle " + neighbor
				+ " for rectangle " + rectangle);
	}

	public static int getOppositeSide(int side) {
		if (side == SIDE_N) {
			return SIDE_S;
		}
		if (side == SIDE_E) {
			return SIDE_W;
		}
		if (side == SIDE_S) {
			return SIDE_N;
		}
		if (side == SIDE_W) {
			return SIDE_E;
		}
		throw new Error(side + " is an inappropriate side id");
	}

	public static RectangleSystem createSystemFromRectangleSet(
			TerrainBasics terrain, Set<Rectangle> set, int borderWidth) {
		int width, heght, startX, startY;
		CustomRectangleSystem newCRS = new CustomRectangleSystem(terrain);
		newCRS.setStartCoord(terrain.width, terrain.height);
		int endX = 0, endY = 0;
		for (Rectangle r : set) {
			newCRS.addRectangle(r);
			if (r.x < newCRS.startX) {
				newCRS.startX = r.x;
			}
			if (r.y < newCRS.startY) {
				newCRS.startY = r.y;
			}
			if (r.x + r.width > endX) {
				endX = r.x + r.width;
			}
			if (r.y + r.height > endY) {
				endY = r.y + r.height;
			}
		}

		newCRS.setWidth(endX - newCRS.startX);
		newCRS.setHeight(endY - newCRS.startY);
		newCRS.borderWidth = borderWidth;
		return new RectangleSystem(newCRS);
	}

	public Set<Rectangle> removeRectanglesInCircle(int x, int y, int radius) {
		// Remove rectangles that are partially or fully located in circle
		// {x,y,radius}
		Set<Rectangle> answer = new HashSet<Rectangle>();
		Set<Integer> keys = new HashSet<Integer>(rectangles.keySet());
		for (int k : keys) {
			Rectangle r = rectangles.get(k);
			if (isRectangleInCircle(r, x, y, radius)) {
				excludeRectangle(k);
				answer.add(r);
			}
		}
		return answer;
	}

	public int lengthOfAdjacenctZone(Rectangle r1, Rectangle r2) {
		// Returns length (in cells) of zone of contact between two adjactent
		// rectangles
		location.setFloor(r1.x + 1, r1.y + 1, GameObjects.FLOOR_WATER);
		location.setFloor(r2.x + 1, r2.y + 1, GameObjects.FLOOR_WATER);
		if (!areRectanglesNear(r1, r2)) {
			return -1;
		}
		int side = getNeighborSide(r1, r2);
		if (side == SIDE_N || side == SIDE_S) {
			return Math.min(r1.x + r1.width, r2.x + r2.width)
					- Math.max(r1.x, r2.x);
		}
		if (side == SIDE_E || side == SIDE_W) {
			return Math.min(r1.y + r1.height, r2.y + r2.height)
					- Math.max(r1.y, r2.y);
		}
		throw new Error(":  |");
	}

	public void expandRectanglesToBorder(int depth) {
		// Moves {x:y} of all rectangles to {x-depth:y-depth} and increases
		// width/height by depth*2
		// so that rectangles expand and make border thinner (borderWidth is
		// also decreased)
		if (borderWidth < depth * 2) {
			throw new Error("border width " + borderWidth
					+ " is too thin for expanding each rectangle by " + depth);
		}
		for (Rectangle r : rectangles.values()) {
			r.x -= depth;
			r.y -= depth;
			r.width += depth * 2;
			r.height += depth * 2;
		}
		borderWidth -= depth * 2;
	}

	public void fillContents(int type, int value) {
		for (Rectangle r : rectangles.values()) {
			location.square(r, type, value, false);
		}
	}

	public static ArrayList<Coordinate> getOuterPoints(
			ArrayList<Coordinate> points, int centerX, int centerY) {
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		Coordinate currentCoord = points.get(0);
		for (Coordinate c : points) {
			// Find first point: the one with least y-coordinate
			if (c.y < currentCoord.y) {
				currentCoord = c;
			}
		}
		Coordinate startCoord = currentCoord;
		double lastAngle = 0;

		do {
			Coordinate nextCoord = currentCoord; // Any Coordinate is ok - it
			// will change anyway
			double newAngle = Math.PI * 3; // Any value > Math.PI*2 is OK there
			for (Coordinate c : points) {
				if (c == currentCoord) {
					continue;
				}
				double angleC = Utils.getLineAngle(currentCoord, c);
				if (angleC >= lastAngle
						&& angleC <= newAngle
						&& (nextCoord == currentCoord || c.distance(
								currentCoord.x, currentCoord.y) < nextCoord
								.distance(currentCoord.x, currentCoord.y))) {
					newAngle = angleC;
					nextCoord = c;
				}
			}
			Main.console(newAngle);
			lastAngle = newAngle;
			if (nextCoord != null) {
				answer.add(nextCoord);
			} else {
				throw new Error();
			}
			currentCoord = nextCoord;
		} while (currentCoord != startCoord);
		return answer;
	}

	public ArrayList<Coordinate> getRectanglesCorners() {
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		for (Rectangle r : rectangles.values()) {
			answer.add(new Coordinate(r.x, r.y));
			answer.add(new Coordinate(r.x + r.width - 1, r.y));
			answer.add(new Coordinate(r.x, r.y + r.height - 1));
			answer.add(new Coordinate(r.x + r.width - 1, r.y + r.height - 1));
		}
		return answer;
	}

	public Coordinate getCenter() {
		return new Coordinate(startX + width / 2, startY + height / 2);
	}
}