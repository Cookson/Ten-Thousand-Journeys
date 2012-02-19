package erpoge.core.net;

import java.awt.Rectangle;
import java.util.ArrayList;

import erpoge.core.meta.Coordinate;
import erpoge.core.meta.Direction;
import erpoge.core.meta.Side;
/**
 * 
 * Same as java.awt.Rectangle, but has some more advanced functionality.
 *
 */
public class RectangleArea extends Rectangle {
	public RectangleArea(Rectangle r) {
		super(r);
	}
	public RectangleArea(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	public ArrayList<Coordinate> getCells() {
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		for (int i=x; i<x+width; i++) {
			for (int j=y; j<y+height; j++) {
				answer.add(new Coordinate(i,j));
			}
		}
		return answer;
	}
	public boolean isCellOnRectangleBorder(int x, int y, Rectangle r) {
		return x == this.x || y == this.y || x == this.x+this.width-1 || y == this.y+this.height-1;
	}
	public int distanceToLine(Coordinate start, Coordinate end) {
	/**
	 * Finds distance from line to rectangle's nearest border parallel to that line
	 */
		Direction dir;
		if (start.x == end.x) {
			dir = Direction.V;
		} else if (start.y == end.y) {
			dir = Direction.H;
		} else {
			throw new Error(start+" and "+end+" are not on the same line");
		}
		if (dir.isV() && start.x >= x && start.x <= x+width-1) {
			throw new Error("Vertical line inside rectangle");
		} else if (dir.isH() && start.y >= y && start.y <= y+height-1) {
			throw new Error("Horizontal line inside rectangle");
		}
		if (dir.isV()) {
			return start.x > x ? start.x-x-width+1 : x - start.x;
		} else {
			return start.y > y ? start.y-y-height+1 : y - start.y;
		}
	}
	public Coordinate getMiddleOfSide(Side side) {
		switch (side) {
		case N:
			return new Coordinate(x+width/2,y);
		case E:
			return new Coordinate(x+width-1,y+height/2);
		case S:
			return new Coordinate(x+width/2,y+height-1);
		case W:
			return new Coordinate(x,y+height/2);
		default:
			throw new Error("Incorrect side "+side.side2int());
		}
	}
	public Coordinate getCellFromSide(Side side, Side sideOfSide, int depth) {
	/**
	 * Get cell on border. 
	 * 
	 * @param side   Which border;
	 * 
	 * @param sideOf Side Determines one of the ends of border;
	 * 
	 * @param depth  How far is the cell from the end of the border.
	 * 0 is the first cell near end of border. Depth may be even
	 * more than width or height, so the cell will be outside
	 * the rectangle.
	 */
		switch (side) {
		case N:
			switch (sideOfSide) {
			case E:
				return new Coordinate(x+width-1-depth,y);
			case W:
				return new Coordinate(x+depth,y);
			default:
				throw new Error("sideOfSide ("+sideOfSide+") must be clockwise or counter-clockwise from side ("+side+")");
			}
		case E:
			switch (sideOfSide) {
			case N:
				return new Coordinate(x+width-1,y+depth);
			case S:
				return new Coordinate(x+width-1,y+height-1-depth);
			default:
				throw new Error("sideOfSide ("+sideOfSide+") must be clockwise or counter-clockwise from side ("+side+")");
			}
		case S:
			switch (sideOfSide) {
			case E:
				return new Coordinate(x+width-1-depth,y+height-1);
			case W:
				return new Coordinate(x+depth,y+height-1);
			default:
				throw new Error("sideOfSide ("+sideOfSide+") must be clockwise or counter-clockwise from side ("+side+")");
			}
		case W:
			switch (sideOfSide) {
			case N:
				return new Coordinate(x,y+depth);
			case S:
				return new Coordinate(x,y+height-1-depth);
			default:
				throw new Error("sideOfSide ("+sideOfSide+") must be clockwise or counter-clockwise from side ("+side+")");
			}
		default:
			throw new Error("Incorrect side "+side.side2int());
		}
	}
	public RectangleArea stretch(Side side, int amount) {
	/**
	 * Stretch rectangle
	 * 
	 * @param side Side where rectangle strecthes
	 * 
	 * @param amount Amount of cells to stretch. If depth > 0,
	 * then rectangle will grow, if depth < 0, then rectangle will
	 * shrink. Notice that if Side == N or W, rectangle.x and rectangle.y 
	 * will move. If depth == 0 then rectangle stays the same.
	 */
		switch (side) {
		case N:
			this.setBounds(this.x, this.y-amount, this.width, this.height+amount);
			break;
		case E:	
			this.setSize(this.width+amount, this.height);
			break;
		case S:
			this.setSize(this.width, this.height+amount);
			break;
		case W:
			this.setBounds(this.x-amount, this.y, this.width+amount, this.height);
			break;
		default:
			throw new Error("Incorrect side "+side);		
		}
		return this;
	}
	public int getDimensionBySide(Side side) {
	/**
	 * Returns this.height if side is N or S, 
	 * returns this.width if side is W or E
	 */
		switch (side) {
		case N:
		case S:
			return this.height;
		case E:
		case W:
			return this.width;
		default:
			throw new Error("Side "+side+" is incorrect");
		}
	}
	public Coordinate getCorner(Side side) {
	/**
	 * Returns Coordinate of particular rectangle's corner.s
	 */
		switch (side) {
		case NE:
			return new Coordinate(x+width-1, y);
		case SE:
			return new Coordinate(x+width-1, y+height-1);
		case SW:
			return new Coordinate(x, y+height-1);
		case NW:
			return new Coordinate(x,y);
		default:
			throw new Error("Incorrect side "+side);
		}
	}
	public static RectangleArea getRectangleFromTwoCorners(Coordinate c1, Coordinate c2) {
	/**
	 * Returns rectangle defined by two corner points
	 */
		int startX = Math.min(c1.x, c2.x);
		int startY = Math.min(c1.y, c2.y);
		int recWidth = Math.max(c1.x,c2.x)-startX+1;
		int recHeight = Math.max(c1.y,c2.y)-startY+1;
		return new RectangleArea(startX, startY, recWidth, recHeight);
	}
}
