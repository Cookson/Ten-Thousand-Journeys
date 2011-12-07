package erpoge.graphs;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import erpoge.Chance;
import erpoge.Main;
import erpoge.terrain.Location;
import erpoge.terrain.TerrainBasics;

public class CustomRectangleSystem extends Graph<Rectangle> {
	int startX, startY, width, height, borderWidth;
	public HashMap<Integer, Rectangle> rectangles;
	public TerrainBasics location;
	
	public CustomRectangleSystem(TerrainBasics location, int startX, int startY, int width, int height, int borderWidth) {
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.borderWidth = borderWidth;
		this.rectangles = new HashMap<Integer, Rectangle>();
		this.rectangles.put(0, new Rectangle(startX, startY, width, height));
		this.edges.put(0, new ArrayList<Integer>());
		this.location = location;
	}
	public CustomRectangleSystem(TerrainBasics location) {
		this.borderWidth = 0;
		this.rectangles = new HashMap<Integer, Rectangle>();
		this.edges.put(0, new ArrayList<Integer>());
		this.location = location;
	}
	/**
	 * Splits rectangle into two rectangles. Rectangle under current number
	 * is the left one (if dir == true) or the top one (if dir == false).
	 * If width < 0, then a rectangle width width/height = -width 
	 * from right side/bottom will be cut off, but under current number still stays
	 * left/top rectangle.
	 * 
	 * @param i Number of rectangle in this.rectangleSystem.rectangles
	 * @param dir true - vertically, false - horizontally
	 * @param width How much to cut
	 */
	public Rectangle splitRectangle(int i, boolean dir, int width) {
		Rectangle r = rectangles.get(i);
		Rectangle newRec;
		if (dir) {
			// Vertically
			if (width < 0) {
				width = r.width+width-1;
			}
			if (width > r.width-2) {
				throw new Error("Width "+width+" in vertical splitting is too big");
			}
			int x = r.x + width;
			newRec = new Rectangle(x+1+borderWidth-1, r.y, r.x + r.width-x-1-borderWidth+1, r.height);
			rectangles.put(i, new Rectangle(r.x, r.y, x - r.x, r.height));
			rectangles.put(rectangles.size(), newRec);
		} else {
			// Horizontally
			if (width < 0) {
				width = r.height+width-1;
			}
			if (width > r.height-2) {
				throw new Error("Width "+width+" in horizontal splitting is too big");
			}
			int y = r.y + width;
			newRec = new Rectangle(r.x, y+1+borderWidth-1, r.width, r.y+r.height-y-1-borderWidth+1);
			rectangles.put(i, new Rectangle(r.x, r.y, r.width, y - r.y));
			rectangles.put(rectangles.size(), newRec);
		}
		// Add empty edges array for new rectangle
		edges.put(size()-1, new ArrayList<Integer>());
		return newRec;
	}
	public void buildEdges() {
	/*
	 * Build edges between rectangles
	 */
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
	public void addRectangle(Rectangle r) {
		rectangles.put(rectangles.values().size(), r);
	}
	public int size() {
		return rectangles.size();
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
	public void cutRectangleFromSide(int recNum, int side, int depth) {
		if (side == SIDE_N) {
			splitRectangle(recNum, false, depth);
		} else if (side == SIDE_E) {
			splitRectangle(recNum, true, rectangles.get(recNum).width-depth);
		} else if (side == SIDE_S) {
			splitRectangle(recNum, false, rectangles.get(recNum).height-depth);
		} else if (side == SIDE_W) {
			splitRectangle(recNum, true, depth);
		} else {
			throw new Error("Unknown side "+side);
		}
	}
}
