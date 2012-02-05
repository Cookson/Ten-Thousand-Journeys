package erpoge.graphs;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import erpoge.Coordinate;
import erpoge.RectangleArea;
import erpoge.Side;
import erpoge.Utils;
import erpoge.terrain.Location;
/**
 * RectangleSystem that can modify contents of the location where it is built.
 */
public class TerrainRectangleSystem extends RectangleSystem {
	private Location location;
	public TerrainRectangleSystem(Location locaction, int x, int y, int width, int height, int minRectangleWidth, int borderWidth) {
		super(x, y, width, height, minRectangleWidth, borderWidth);
		this.location = location;
	}
	public TerrainRectangleSystem(Location location, CustomRectangleSystem crs) {
		super(crs);
		this.location = location;
	}
	public void excludeRectanglesHaving(int type, int val) {
		Set<Integer> keys = new HashSet<Integer>(content.keySet());
		keyLoop: for (int i : keys) {
			Rectangle r = content.get(i);
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
	public void drawBorders(int type, int name, boolean onlyInner) {
		// Заполнить границы графа объектами type вида name
		// Если onlyInner=true, то заполняется вся внутренняя область
		// прямоугольника, очерчивающего систему прямоугольников,
		// кроме внутренней области самих прямоугольников, составляющих
		// системму.
		// Иначе, если onlyInner=false, то каждый прямоугольник системы
		// очерчивается квадратом из элементов type,name
		// В этих двух случаях используются разные алгоритмы с разной
		// производительностью (onlyInner=false быстрее)
		// Следует учитывать, что при onlyInner=true заполнится и внутренняя
		// область прямоугольников,
		// которые были исключены из системы с помощью
		// RectangleSystem::excludeRectangle.

		if (onlyInner) {
			if (borderWidth == 0) {
				throw new Error(
						"An attempt to draw inner borders of a rectangleSystem with borderWidth = 0");
			}
			// Очерчивание только внутренних границ
//			// Буферизуем содержимое области, которую покрывает граф
//			// Cell[][] bufContents = new Cell[width][height];
			//
			// int endX = startX + width - 1;
			// int endY = startY + height - 1;
			// for (int x = startX; x <= endX; x++) {
			// for (int y = startY; y <= endY; y++) {
			// Копируем содержимое каждой ячейки, передавая будущим
//			// новым ячейкам в качестве параметра старые
//			// bufContents[x - startX][y - startY] = new Cell(
			// location.cells[x][y]);
			//
			// }
			// }
			// Заполняем всю площадь графа объектами type,name
//			// Список типов type - в TerrainBasics
			// location.square(startX, startY, width, height, type, name, true);
			//
			// Ставим обратно объекты внутри прямоугольников графа - таким
//			// образом, заполнены новыми объектами будут только границы
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
		
			Set<Integer> keys = content.keySet();
			for (int k : keys) {
				Rectangle r = content.get(k);
				if (!outerSides.get(k).contains(Side.E)) {
					location.square(r.x + r.width, r.y, borderWidth, r.height,
							type, name, true);
				}
				if (!outerSides.get(k).contains(Side.S)) {
					location.square(r.x, r.y + r.height, r.width, borderWidth,
							type, name, true);
				}
				if (!outerSides.get(k).contains(Side.S)
						&& !outerSides.get(k).contains(Side.E)) {
					location.square(r.x + r.width, r.y + r.height, borderWidth,
							borderWidth, type, name, true);
				}
			}
		} else {
			// Очерчивание границ всех прямоугольников
			for (Map.Entry<Integer, RectangleArea> e : content.entrySet()) {
				Rectangle r = e.getValue();
				location.square(r.x - 1, r.y - 1, r.width + 2, r.height + 2,
						type, name);
			}
		}
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
			ArrayList<Side> sides = outerSides.get(i);
			Rectangle r = content.get(i);
			boolean n = sides.contains(Side.N);
			boolean e = sides.contains(Side.E);
			boolean s = sides.contains(Side.S);
			boolean w = sides.contains(Side.W);
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
	public void fillContents(int type, int value) {
		for (Rectangle r : content.values()) {
			location.square(r, type, value, false);
		}
	}
	private static class ccwlLastCellHolder {
		// A class that is used by connectCornersWithLines() method to store
		// lastCell variable for custom comparator.
		public static Coordinate center;;
	}
}
