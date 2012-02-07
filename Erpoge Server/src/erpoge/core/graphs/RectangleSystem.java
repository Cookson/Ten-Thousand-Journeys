package erpoge.core.graphs;

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

import erpoge.core.Main;
import erpoge.core.meta.Chance;
import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Direction;
import erpoge.core.meta.Side;
import erpoge.core.meta.Utils;
import erpoge.core.net.RectangleArea;
import erpoge.core.objects.GameObjects;
import erpoge.core.terrain.Cell;
import erpoge.core.terrain.Location;
import erpoge.core.terrain.TerrainBasics;

public class RectangleSystem extends Graph<RectangleArea> {
	public int width;
	public int height;
	public int startX;
	public int startY;
	public int minRectangleWidth;
	public int borderWidth;
	public HashMap<Integer, ArrayList<Side>> outerSides;
	private HashMap<Integer, RectangleArea> outerRectangles = new HashMap<Integer, RectangleArea>();

	/*
	 * Многоугольники, которые покрывают уровень при представлении уровня как
	 * графа из многоугольников, см. функцию getGraph
	 */

	public RectangleSystem(int startX, int startY, int width, int height,
			int minRectangleWidth, int borderWidth) {
		/*
		 * location - объект локации, на которой строится граф;
		 * startX,startY,width,height - квардат, в который вмещается граф;
		 * minRectangleWidth - минимальная ширина/высота прямоугольников, на
		 * которые разделется граф; borderWidth - ширина (в клетках) границы
		 * между прямоугольниками;
		 */
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
		this.minRectangleWidth = minRectangleWidth;
		this.borderWidth = borderWidth;
		excluded = new HashMap<Integer, RectangleArea>();
		addVertex(new RectangleArea(startX, startY, width, height));
		

		boolean noMoreRectangles = false;
		int splitableRecSizeLimit = minRectangleWidth * 2 + borderWidth + 1;
		Chance ch = new Chance(50);
		while (!noMoreRectangles) {
			noMoreRectangles = true;
			int size = content.size();
			for (int i = 0; i < size; i++) {
				if (content.get(i).width > splitableRecSizeLimit
						&& content.get(i).height > splitableRecSizeLimit) {
					noMoreRectangles = false;
					splitRectangle(i, ch.roll() ? Direction.V : Direction.H);
				} else if (content.get(i).width > splitableRecSizeLimit) {
					noMoreRectangles = false;
					splitRectangle(i, Direction.V);
				} else if (content.get(i).height > splitableRecSizeLimit) {
					noMoreRectangles = false;
					splitRectangle(i, Direction.H);
				}
			}
		}
		buildEdges();
		initialFindOuterSides();
	}

	public RectangleSystem(CustomRectangleSystem crs) {
		this.content = crs.content;
		this.edges = crs.edges;
		this.borderWidth = crs.borderWidth;
		this.startX = crs.startX;
		this.startY = crs.startY;
		this.width = crs.width;
		this.height = crs.height;
		this.edges = crs.edges;
		this.excluded = crs.excluded;
		this.outerSides = new HashMap<Integer, ArrayList<Side>>();
//		buildEdges();
		findOuterSidesOfComplexForm();
	}

	public int size() {
		return content.size();
	}

	public void splitRectangle(int i, Direction dir) {
		// Разделить прямоугольник на два, каждый из которых по ширине и по
		// высоте больше minRectangleWidth*2
		// i - индекс прямоугольника в массиве rectangles
		// dir - направление разделяющей стены - вертикальное (true) или
		// горизонтальное (false)
		Rectangle r = content.get(i);
		if (dir == Direction.V) {
			// Vertical
			int x = Chance.rand(r.x + minRectangleWidth, r.x + r.width
					- minRectangleWidth - 1 - borderWidth + 1);
			content.put(i, new RectangleArea(r.x, r.y, x - r.x, r.height));
			addVertex(new RectangleArea(x + 1 + borderWidth - 1, r.y, r.x + r.width
					- x - 1 - borderWidth + 1, r.height));
		} else {
			// Horizontal
			int y = Chance.rand(r.y + minRectangleWidth, r.y + r.height
					- minRectangleWidth - 1 - borderWidth + 1);
			content.put(i, new RectangleArea(r.x, r.y, r.width, y - r.y));
			addVertex(new RectangleArea(r.x, y + 1
					+ borderWidth - 1, r.width, r.y + r.height - y - 1
					- borderWidth + 1));
		}
	}

	public void buildEdges() {
	// Построить рёбра в графе
		int len = content.size();
		for (int i = 0; i < len; i++) {
			Rectangle r1 = content.get(i);
			if (r1 == null) {
				continue;
			}
			for (int j = i + 1; j < len; j++) {
				Rectangle r2 = content.get(j);
				if (r2 == null) {
					continue;
				}
				if (areRectanglesNear(r1, r2)) {
					mutualLink(i, j);
				}
			}
		}
	}
	
	public boolean areRectanglesNear(Rectangle r1, Rectangle r2) {
		// Соприкасаются ли прямоугольники горизонтальными или вертикальными
		// сторонами с учётом borderWidth
		// if (rec1.x > rec2.x) {
		// В rec1 должен храниться прямоугольник, находящийся левее
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
		
//		int x2 = rec1.x + rec1.width + borderWidth - 1;
//		int y2 = rec1.y + rec1.height + borderWidth - 1;
//		int x4 = rec2.x + rec2.width + borderWidth - 1;
//		int y4 = rec2.y + rec2.height + borderWidth - 1;
//		// (x1<=x4)&(x2>=x3) (y1<=y4)&(y2>=y3)
//		if (rec1.x == x4 && x2 == rec2.x && rec1.y == y4 && y2 == rec2.y) {
//			return true;
//		}
//		return false;
		if (r1.x + r1.width + borderWidth == r2.x || r2.x + r2.width + borderWidth == r1.x) {
			// Rectangles share vertical line
			int a1 = r1.y;
			int a2 = r1.y+r1.height-1;
			int b1 = r2.y;
			int b2 = r2.y+r2.height-1;
			int intersection = Utils.integersRangeIntersection(a1, a2, b1, b2);
			if (intersection >= 1) {
				return true;
			} else {
				return false;
			}
		} else if (r1.y + r1.height + borderWidth == r2.y || r2.y + r2.height + borderWidth == r1.y) {
			// Rectangles share horizontal line
			int a1 = r1.x;
			int a2 = r1.x+r1.width-1;
			int b1 = r2.x;
			int b2 = r2.x+r2.width-1;
			int intersection = Utils.integersRangeIntersection(a1, a2, b1, b2);
			if (intersection >= 1) {
				return true;
			} else {
				return false;
			}
		} else {
		// Rectangles dont' share horizontal or vertical lines
			return false;
		}
	}

	

	public void convertGraphToDirectedTree() {
		convertGraphToDirectedTree(-1);
	}

	public void addVertexToTree(int vertex) {
		addVertexToTree(vertex, -1);
	}

	public void addVertexToTree(int vertex, int previousVertex) {
		// Используется при построении ориентрированного дерева
		// Все ненаправленные рёбра этой вершины заменяет на направленные от
		// вершины (так же, как isolateVertex),
		// но, в отличие от isolateVertex, ничего не делает с направленными к
		// этой вершине рёбрами.
		for (int v : edges.get(vertex)) {
			// Среди всех вершин ищем вершины, соединённые с nextVertex,
			// и удаляем из них nextVertex
			if (v == previousVertex) {
				continue;
			}
			int k = edges.get(v).indexOf(vertex);
			edges.get(v).remove(k);
		}
	}

	public ArrayList<Integer> convertGraphToDirectedTree(int currVertex) {
		// Преобразовать ненаправленный связный граф в ориентированное дерево,
		// началом которого является вершина currVertex
		// (если currVertex не задана - она выбирается случайно)
		// Возвращает список из координат вершин-тупиков, 
		// где нулевой элемент - currVertex
		
		Set<Integer> keys = new HashSet<Integer>(edges.keySet());
		ArrayList<Integer> deadEnds = new ArrayList<Integer>();
		if (keys.size() == 1) {
			// Выйти из функции для системы из одного прямоугольника
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
		// Устанавливаем первую вершину
		addVertexToTree(currVertex);
		ArrayList<ArrayList<Integer>> chainBranches = new ArrayList<ArrayList<Integer>>();
		chainBranches.add(new ArrayList<Integer>());
		chainBranches.get(0).add(currVertex);
		// Ключевой массив алгоритма
		// Содержит все последовательности вершин, образующие ветви цепи
		// [0] начинается со startVertex
		// последующие ветки начинаются с вершины, которая уже присутствует в
		// одной
		// (реже, но тоже возможно и работает - в нескольких) ветках
		int currBranch = 0; // Индекс текущей ветви
		boolean nextVertexFound = true;
		// num=0; // Дебаг
		ArrayList<Integer> usedVertexes = new ArrayList<Integer>();
		usedVertexes.add(startVertex);
		Chance ch20 = new Chance(20);
		chooseRand: while (nextVertexFound) {
			// От этой вершины выбираем случайную из соединённых с ней и удаляем
			// эту случайную вершину изо всех edges, соединяя случайную с
			// текущей,
			// после чего случайная (nextCell) становится текущей
			if (edges.get(currVertex).size() == 0 || ch20.roll()
					&& edges.get(currVertex).size() > 1 && currBranch > 1 /* Дебажил полночи, 
					чтобы прийти к этому условию. currBranch>1 нужно, чтобы 
					случайная новая ветка (из ch20) не начиналась на первой вершине */) {
				// Если с текущей вершиной не соединено других вершин, выбрать
				// одну из предыдущих вершин
				currBranch++;
				nextVertexFound = false;
				for (int i = 0; i < currBranch; i++) {
					// Смотрим предыдущие ветви
					for (int j = 0; j < chainBranches.get(i).size(); j++) {
						// Смотрим все вершины ветви
						int vertexFromBranch = chainBranches.get(i).get(j);
						if (edges.get(vertexFromBranch).size() > 0) {
							// Если с этой вершиной из предыдущей ветви
							// соединены какие-то пока что свободные вершины,
							// начинать от этой вершины
							Collections.sort(edges.get(vertexFromBranch),
									Collections.reverseOrder());
							// Выбрать текущей вершиной случайную вершину, с
							// которой обоюдно (в обе стороны) соединена вершина
							// из предыдущей ветви

							// Сюда сохраняем номера вершин, с которыми
							// vertexFromBranch имеет обоюдную связь
							ArrayList<Integer> links = new ArrayList<Integer>();

							for (int vertex : edges.get(vertexFromBranch)) {
								// Ищем эти вершины
								if (!usedVertexes.contains(vertex)) {
									links.add(vertex);
								}
							}
							// И выбираем следующей случайную из них
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
				// Collections.reverseOrder()); // Перемещаем null-значения
				// в текущей вершине в
				// конец массива
				int nextVertex = edges.get(currVertex).get(
						Chance.rand(0, edges.get(currVertex).size() - 1));
				chainBranches.get(currBranch).add(nextVertex);
				addVertexToTree(nextVertex, currVertex);
				usedVertexes.add(nextVertex);
				currVertex = nextVertex;
			}
		}
		// Смыслом всех предыдущих действий было заполнение массива
		// chainBranches массивами веток вершин
		Set<Integer> c = edges.keySet();
		Iterator<Integer> it = c.iterator();
		while (it.hasNext()) {
			// Имея готовый массив chainBranches, мы обнуляем вершины...
			edges.put(it.next(), new ArrayList<Integer>());
		}
		int size1 = chainBranches.size();
		for (int i = 0; i < size1; i++) {
			// ...и последовательно соединяем вершины в каждой ветке
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
		 * Прописать для каждого прямоугольника, с какими из четырёх сторон
		 * прямоугольника, описывающего область графа, они граничат. Результаты
		 * записываются в массив Graph::outerSides, где индекс - номер
		 * прямоугольника, значение - массив номеров сторон, с которыми
		 * прямоугольник граничит. Номера сторон: 1 4 2 3 Для следующего графа
		 * результат работы метода будет:
		 * Graph::outerSides=array(array(1,4),array(1,2),array(2,3),array(3,4))
		 */
		outerSides = new HashMap<Integer, ArrayList<Side>>();
		int size = 0;

		Set<Integer> rkeys = content.keySet();
		Iterator<Integer> it = rkeys.iterator();
		while (it.hasNext()) {
			int key = it.next();
			Rectangle r = content.get(key);
			outerSides.put(key, new ArrayList<Side>());
			try {
				if (r.y == startY) {
					// Верхняя сторона
					outerSides.get(key).add(Side.N);
				}
				if (r.x + r.width == startX + width) {
					// Правая
					outerSides.get(key).add(Side.E);
				}
				if (r.y + r.height == startY + height) {
					// Нижняя
					outerSides.get(key).add(Side.S);
				}
				if (r.x == startX) {
					// Левая
					outerSides.get(key).add(Side.W);
				}
			} catch (Exception e) {
			}
			size++;
		}
	}

	public void findOuterSidesOfComplexForm() {
	// Initial search for outer sides in custom rectangle system of
	// non-squared form
		for (int i : content.keySet()) {
			outerSides.put(i, new ArrayList<Side>());
			Rectangle r = content.get(i);
			HashMap<Side, Integer> sides = new HashMap<Side, Integer>();
			sides.put(Side.N, r.width);
			sides.put(Side.E, r.height);
			sides.put(Side.S, r.width);
			sides.put(Side.W, r.height);
			for (int j : edges.get(i)) {
				Rectangle r2 = content.get(j);
				if (areRectanglesNear(r, r2)) {
					Side side = getNeighborSide(r, r2);
					sides.put(side, sides.get(side)
							- lengthOfAdjacenctZone(r, r2));
				} else {
					throw new Error("Rectangles are not close to each other! Error in logic of rectangles' splitting!");
				}
			}
			ArrayList<Side> thisOuterSides = outerSides.get(i);
			for (Map.Entry<Side, Integer> e : sides.entrySet()) {
				if (e.getValue() > 0) {
					thisOuterSides.add(e.getKey());
				}
			}
		}
	}

	public void excludeRectangle(int num) {
		// Исключить прямоугольник из системы, удалив его, его рёбра и записи о
		// граничащих с ним сторонах
		// Стоит учитывать, что после этого под индексом удалённого
		// прямоугольника не будет прямоугольника,
		// что необходимо будет учитывать при переборе массива прямоугольников в
		// цикле с итератором.
		if (!content.containsKey(num)) {
			throw new Error("The rectangle system already has no �" + num
					+ " rectangle");
		}
		excluded.put(num, content.get(num));

		// Add outer sides to nearby rectangles
		Rectangle r = content.get(num);
		for (int neighbor : edges.get(num)) {
			Rectangle nr = content.get(neighbor);
			Side side = getNeighborSide(nr, r);
			ArrayList<Side> neighborOuterSides = outerSides.get(neighbor);
			if (!neighborOuterSides.contains(side)) {
				neighborOuterSides.add(side);
			}
		}
		outerSides.remove(num);

		content.remove(num); // Удаляем прямоугольник...
		if (outerRectangles.containsKey(num)) {
			outerRectangles.remove(num);
		}
		edges.remove(num); // его рёбра...
		Set<Integer> keys = edges.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			// удаляем соединения этого прямоугольника с остальными...
			int k = it.next();
			ArrayList<Integer> e = edges.get(k);
			int pos = e.indexOf(num);
			if (pos != -1) {
				edges.get(k).remove(pos);
			}
		}
	}

	public boolean isVertexExclusible(int vertex) {
		// Проверяет, останется ли граф связным, если исключить вершину vertex
		// В ходе алгоритма в vertexes сохраняются значения для каждой вершины
		// (индекс - номер вершины, значение - значение)
		// Значение 0 значит, что вершина не посещена алгоритмом
		// Значение 1 значит, что вершина может быть достигнута из одной из
		// вершин со значением 2
		// Значение 2 значит, что вершину уже не нужно проверять
		// Алгоритм выполняется, пока не кончатся вершины со значением 0 или не
		// кончатся вершины со значением 1
		// В первом случае это будет значить, что граф связный, во втором - что
		// не связный
		HashMap<Integer, Integer> vertexes = new HashMap<Integer, Integer>();
		int v0 = -1; // Количество вершин со значением 0 (уменьшается по ходу
		// алгоритма) (-1, т.к. одна из вершин сразу будет со
		// значением 1)
		int v1 = 1; // Количество вершин со значением 1 (увеличивается по ходу
		// алгоритма)
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
			// Берём начальную вершину (любую, но нам неизвестно, вершины с
			// какими индексами есть в графе)
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
				// Ищем любую вершину со значением 1
				int k = it.next();
				int v = vertexes.get(k);
				if (v != 1 || k == vertex) {
					continue;
				}
				for (int ve : edges.get(k)) {
					// Проставляем всем её соседним вершинам со значением 0
					// значение 1
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
		// Найти в системе прямоугольник по данным координатам принадлежащей ему
		// ячейки
		// Возвращает индекс прямоугольника в rectangles.
		// Если клетка не относится ни к одному прямоугольнику, возвращает
		// false.
		// Внимание: прямоугольник в rectangles может иметь индекс 0,
		// поэтому сравнивать результат с false нужно со сравнением типов
		// (===/!==)
		Set<Integer> keys = content.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			// Ищем любую вершину со значением 1
			int k = it.next();
			Rectangle r = content.get(k);
			if (rectangleHasCell(r, x, y)) {
				return k;
			}
		}
		return -1;
	}

	public static boolean rectangleHasCell(Rectangle r, int cellX, int cellY) {
		// Принадлежит ли прямоугольнику точка [cellX;cellY]
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
		return content.containsKey(i);
	}

	public Set<Integer> getRectanglesKeys() {
		return new HashSet<Integer>(content.keySet());
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
		for (int k : content.keySet()) {
			Rectangle r = content.get(k);
			if (r.x == x && r.y == y) {
				return k;
			}
		}
		return -1;
	}

	public int getRandomRectangleNum() {
		ArrayList<Integer> keys = new ArrayList<Integer>(content.keySet());
		if (keys.size() == 0) {
			throw new Error("system has no rectangles");
		}
		return keys.get(Chance.rand(0, keys.size() - 1));
	}

	public Rectangle getRandomRectangle() {
		return content.get(getRandomRectangleNum());
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
				outerRectangles.put(k, content.get(k));
			}
		}
	}

	public Side getNeighborSide(Rectangle rectangle, Rectangle neighbor) {
		// Get side from which neighbor is located relatively to the first
		// rectangle
		if (rectangle.y == neighbor.y + neighbor.height + borderWidth) {
			return Side.N;
		}
		if (rectangle.x + rectangle.width + borderWidth == neighbor.x) {
			return Side.E;
		}
		if (rectangle.y + rectangle.height + borderWidth == neighbor.y) {
			return Side.S;
		}
		if (rectangle.x == neighbor.x + neighbor.width + borderWidth) {
			return Side.W;
		}
		throw new Error("Cannot find side of neighbor rectangle " + neighbor
				+ " for rectangle " + rectangle);
	}

	public static Side getOppositeSide(Side side) {
		if (side == Side.N) {
			return Side.S;
		}
		if (side == Side.E) {
			return Side.W;
		}
		if (side == Side.S) {
			return Side.N;
		}
		if (side == Side.W) {
			return Side.E;
		}
		throw new Error(side + " is an inappropriate side id");
	}

	public static RectangleSystem createSystemFromRectangleSet(
			Location terrain, Set<RectangleArea> set, int borderWidth) {
		CustomRectangleSystem newCRS = new CustomRectangleSystem(terrain);
		newCRS.setStartCoord(terrain.getWidth(), terrain.getHeight());
		int endX = 0, endY = 0;
		for (RectangleArea r : set) {
			newCRS.addVertex(r);
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
		Set<Integer> keys = new HashSet<Integer>(content.keySet());
		for (int k : keys) {
			Rectangle r = content.get(k);
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
		if (!areRectanglesNear(r1, r2)) {
			return -1;
		}
		Side side = getNeighborSide(r1, r2);
		if (side == Side.N || side == Side.S) {
			return Math.min(r1.x + r1.width, r2.x + r2.width)
					- Math.max(r1.x, r2.x);
		}
		if (side == Side.E || side == Side.W) {
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
		for (Rectangle r : content.values()) {
			r.x -= depth;
			r.y -= depth;
			r.width += depth * 2;
			r.height += depth * 2;
		}
		borderWidth -= depth * 2;
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
		for (Rectangle r : content.values()) {
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

	public boolean isRectangleOuter(int k) {
		return outerSides.get(k).size() > 0;
	}
}