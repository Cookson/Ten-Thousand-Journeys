function Side(value) {
	this.value = value;
}
Side.int2side = function _(side) {
/**
 * Returns Side corresponding to number side.
 * Integer side must be from 1 to 8, 1 is for Side.N, 
 * each next is for the next side clockwise
 */
	switch (side) {
	case 0:
		return Side.N;
	case 1:
		return Side.NE;
	case 2: 
		return Side.E;
	case 3: 
		return Side.SE;
	case 4:
		return Side.S;
	case 5: 
		return Side.SW;
	case 6:
		return Side.W;
	case 7:
		return Side.NW;
	default:
		throw new Error("Not appropriate side int!");	
	}
};
Side.prototype.getInt = function () {
/**
 * Returns int corresponding to Side.
 * 1 is for Side.N, each next is for the next side clockwise
 */
	return this.value;
};
Side.prototype.clockwise = function() {
	switch (this.value) {
	case 0:
		return Side.E;
	case 2:
		return Side.S;
	case 4:
		return Side.W;
	case 6: 
		return Side.N;
	default:
		throw new Error("Incorrect side "+this);
	}
};
Side.prototype.counterClockwise = function() {
	switch (this.value) {
	case 0:
		return Side.W;
	case 2:
		return Side.S;
	case 4:
		return Side.E;
	case 6: 
		return Side.N;
	default:
		throw new Error("Incorrect side "+this);
	}
};
Side.prototype.opposite = function() {
	switch (this.value) {
	case 0:
		return Side.S;
	case 1:
		return Side.SW;
	case 2:
		return Side.W;
	case 3:
		return Side.NW;
	case 4:
		return Side.N;
	case 5:
		return Side.NE;
	case 6: 
		return Side.E;
	case 7:
		return Side.SE;
	default:
		throw new Error("Incorrect side "+this);
	}
};
Side.prototype.ordinalClockwise = function() {
	switch (this.value) {
	case 0:
		return Side.NE;
	case 1:
		return Side.E;
	case 2:
		return Side.SE;
	case 3: 
		return Side.S;
	case 4:
		return Side.SW;
	case 5:
		return Side.W;
	case 6: 
		return Side.NW;
	case 7:
		return Side.N;
	default:
		throw new Error("Incorrect side "+this);
	}
};
Side.prototype.ordinalCounterClockwise = function() {
	switch (this.value) {
	case 0:
		return Side.NW;
	case 1:
		return Side.N;
	case 2:
		return Side.NE;
	case 3: 
		return Side.E;
	case 4:
		return Side.SE;
	case 5:
		return Side.S;
	case 6: 
		return Side.SW;
	case 7:
		return Side.W;
	default:
		throw new Error("Incorrect side "+this);
	}
};
Side.prototype.toString = function() {
	switch (this.value) {
	case 0:
		return "N";
	case 1:
		return "NE";
	case 2:
		return "E";
	case 3:
		return "SE";
	case 4:
		return "S";
	case 5:
		return "SW";
	case 6:
		return "W";
	case 7:
		return "NW";
	default:
		throw new Error("Unknown side");
	}
};
Side.prototype.isVertical = function() {
	switch (this.value) {
	case 0:
	case 4:
		return true;
	case 2:
	case 6:
		return false;
	default:
		throw new Error("Cannot be horizontal or vertical");
	}
};
Side.prototype.isHorizontal = function() {
	switch (this) {
	case 0:
	case 4:
		return false;
	case 2:
	case 6:
		return true;
	default:
		throw new Error("Cannot be horizontal or vertical");
	}
};
Side.getOrdinalDirection = function _(side1, side2) {
/**
 * Returns an ordinal direction between two cardinal directions.
 */
	if (side1 == Side.N) {
		if (side2 == Side.E) {
			return Side.NE;
		} else if (side2 == Side.W) {
			return Side.NW;
		}
	} else if (side1 == Side.E) {
		if (side2 == Side.N) {
			return Side.NE;
		} else if (side2 == Side.S) {
			return Side.SE;
		}
	} else if (side1 == Side.S) {
		if (side2 == Side.E) {
			return Side.SE;
		} else if (side2 == Side.W) {
			return Side.SW;
		}
	} else if (side1 == Side.W) {
		if (side2 == Side.N) {
			return Side.NW;
		} else if (side2 == Side.S) {
			return Side.SW;
		}
	}
	throw new Error("Sides "+side1+" and "+side2+" are not close as cardinal directions");
};
Side.d2side = function _(dx, dy) {
	if (dx === 0 && dy === 0 || Math.abs(dx) > 1 || Math.abs(dy) > 1) {
		throw new Error("Incorrect numbers: dx="+dx+", dy="+dy);
	}
	var side1 = Side.ANY_SIDE, side2 = Side.ANY_SIDE;
	if (dx === -1) {
		side1 = Side.W;
	} else if (dx === 1) {
		side1 = Side.E;
	}
	if (dy === 1) {
		side2 = Side.S;
	} else if (dy === -1) {
		side2 = Side.N;
	}
	if (side1 === Side.ANY_SIDE) {
		return side2;
	}
	if (side2 === Side.ANY_SIDE) {
		return side1;
	}
	return Side.getOrdinalDirection(side1, side2);
};
Side.prototype.side2d = function() {
	switch (this.value) {
	case 0:
		return [0,-1];
	case 1:
		return [1,-1];
	case 2:
		return [1,0];
	case 3: 
		return [1,1];
	case 4:
		return [0,1];
	case 5:
		return [-1,1];
	case 6: 
		return [-1,0];
	case 7:
		return [-1,-1];
	default:
		throw new Error("Incorrect side "+this);
	}
};
/**
 * Returns an integer corresponding to cardinal side as if there were no ordinal
 * sides.
 * 
 * @return {number} 0 for N, 1 for E, 2 for S and 3 for W.
 */
Side.prototype.getCardinalInt = function() {
	switch (this.value) {
	case 0:
		return 0;
	case 2:
		return 1;
	case 4:
		return 2;
	case 6:
		return 3;
	default:
		throw new Error("Only cardinal directions may return cardinal int");
	}
};
Side.cardinal = {};
/**
 * Use this to iterate by cardinal directions.
 * 
 * @param func
 * @param args
 */
Side.cardinal.forEach = function(func, args) {
	func.apply(Side.N, args);
	func.apply(Side.E, args);
	func.apply(Side.S, args);
	func.apply(Side.W, args);
};
/**
 * Use this to iterate by ordinal directions.
 * 
 * @param func
 * @param args
 */
Side.forEach = function(func, args) {
	func.apply(this.N, args);
	func.apply(this.NE, args);
	func.apply(this.E, args);
	func.apply(this.SE, args);
	func.apply(this.S, args);
	func.apply(this.SW, args);
	func.apply(this.W, args);
	func.apply(this.NW, args);
};
Side.N  = new Side(0);
Side.NE = new Side(1);
Side.E  = new Side(2);
Side.SE = new Side(3);
Side.S  = new Side(4);
Side.SW = new Side(5);
Side.W  = new Side(6);
Side.NW = new Side(7);
Side.ANY_SIDE = new Side(9000);
