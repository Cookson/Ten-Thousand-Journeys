package erpoge.core.graphs;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import erpoge.core.Location;
import erpoge.core.meta.Direction;
import erpoge.core.meta.Side;
import erpoge.core.net.RectangleArea;

public class CustomRectangleSystem extends Graph<RectangleArea> {
	int startX, startY, width, height, borderWidth;
	
	public CustomRectangleSystem(int startX, int startY, int width, int height, int borderWidth) {
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.borderWidth = borderWidth;
		addVertex(new RectangleArea(startX, startY, width, height));
	}
	public CustomRectangleSystem(Location location) {
		this.borderWidth = 0;
		this.content = new HashMap<Integer, RectangleArea>();
		addVertex(new RectangleArea(startX, startY, width, height));
	}
	/**
	 * Splits rectangle into two rectangles. Rectangle under current number
	 * is the left one (if dir == Direction.V) or the top one (if dir == Direction.H).
	 * If width < 0, then a rectangle width width/height = -width 
	 * from right side/bottom will be cut off, and under current number still stay
	 * right/bottom rectangle.
	 * 
	 * @param i Number of rectangle in this.rectangleSystem.rectangles
	 * @param direction Horizontally or vertically
	 * @param width How much to cut
	 */
	public int splitRectangle(int i, Direction dir, int width) {
	/* */ // Optimize size() calls
		RectangleArea r = content.get(i);
		boolean negativeWidth = width < 0;
		int newRecId;
		if (dir == Direction.V) {
			// Vertically
			if (negativeWidth) {
				width = r.width+width-1;
			}
			if (width > r.width-2) {
				throw new Error("Width "+width+" in vertical splitting is too big");
			}
			int x = r.x + width;
			RectangleArea leftRec = new RectangleArea(r.x, r.y, x - r.x, r.height);
			RectangleArea rightRec = new RectangleArea(x+1+borderWidth-1, r.y, r.x+r.width-x-1-borderWidth+1, r.height);
			content.put(i, negativeWidth ? leftRec : rightRec);
			addVertex(negativeWidth ? rightRec : leftRec);
			newRecId = size-1;
		} else {
			// Horizontally
			if (negativeWidth) {
				width = r.height+width-1;
			}
			if (width > r.height-2) {
				throw new Error("Width "+width+" in horizontal splitting is too big");
			}
			int y = r.y + width;
			RectangleArea topRec = new RectangleArea(r.x, r.y, r.width, y - r.y);
			RectangleArea bottomRec = new RectangleArea(r.x, y+1+borderWidth-1, r.width, r.y+r.height-y-1-borderWidth+1);
			
			content.put(i, negativeWidth ? topRec : bottomRec);
			addVertex(negativeWidth ? bottomRec : topRec);
			newRecId = size-1;
		}
		// Add empty edges array for new rectangle
		return newRecId;
	}
	public void buildEdges() {
	/*
	 * Build edges between rectangles
	 */
		int len = content.size();
		edges = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < len; i++) {
			edges.put(i, new ArrayList<Integer>());
		}
		for (int i = 0; i < len; i++) {
			Rectangle r1 = content.get(i);
			for (int j = i + 1; j < len; j++) {
				Rectangle r2 = content.get(j);
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
		if (rec1.x > rec2.x) {
			// � rec1 ������ ��������� �������������, ����������� �����
			Rectangle buf = rec1;
			rec1 = rec2;
			rec2 = buf;
		}
		if (
		/* ������������� ��������������� ��������� */
		rec1.x <= rec2.x
				&& rec2.x <= rec1.x + rec1.width - 1
				&& (rec1.y == rec2.y + rec2.height + borderWidth || rec2.y == rec1.y
						+ rec1.height + borderWidth)
				||
				/* ������������� ������������� ��������� */
				(rec1.y <= rec2.y && rec2.y <= rec1.y + rec1.height - 1 || rec2.y <= rec1.y
						&& rec1.y <= rec2.y + rec2.height - 1)
				&& rec1.x + rec1.width + borderWidth == rec2.x) {
			// �������� ����� ����� ��������
			return true;
		}
		return false;
	}
	public int size() {
		return content.size();
	}
	public void setStartCoord(int startX, int startY) {
		this.startX = startX;
		this.startY = startY;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
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
			if (r.contains(x, y)) {
				return k;
			}
		}
		return -1;
	}
	public int cutRectangleFromSide(int recNum, Side side, int depth) {
		if (side == Side.N) {
			return splitRectangle(recNum, Direction.H, depth);
		} else if (side == Side.E) {
			return splitRectangle(recNum, Direction.V, -depth);
		} else if (side ==Side.S) {
			return splitRectangle(recNum, Direction.H, -depth);
		} else if (side == Side.W) {
			return splitRectangle(recNum, Direction.V, depth);
		} else {
			throw new Error("Unknown side "+side);
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
			throw new Error("The rectangle system already has no " + num
					+ " rectangle");
		}
		excluded.put(num, content.get(num));
		content.remove(num); // Удаляем прямоугольник...
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
}
