/**
 * @constructor
 */
function Cell() {
	// Ячейка массива клеток, содержащего модели объектов, предметов и стен
	// Свойства ячейки области
	this.wall = null;
	/** @private @type GameObject */
	this.object = null;
	/** @private @type Ceiling */
	this.ceiling = null;
	/** @private @type ItemSet */
	this.items = null;
	this.soundSource = null;
	/** @private @type Character */
	this.character = null;
	this.visible = false;
	this.passability = passfree;
}
/**
 * 
 * @param {Chunk} chunk
 * @param {number} x
 * @param {number} y
 */
Cell.prototype.show = function(chunk, x, y) {
	if (this.object !== null) {
		this.object.show(x, y);
	}
	if (this.ceiling !== null) {
		this.ceiling.show();
	}
	if (this.items !== null) {
		var values = this.items.getValues();
		for (var i=0; i<values.length; i++) {
			values[i]._img.style.display = "inline-block";
		}
	}
	this.visible = true;
};
/**
 * 
 * @param {Chunk} chunk
 * @param {number} x
 * @param {number} y
 * @return
 */
Cell.prototype.hide = function(chunk, x, y) {
	if (this.character !== null) {
		this.character.hideModel();
	}
	if (this.soundSource !== null) {
		this.soundSource.hide();
	}
	if (this.ceiling !== null) {
		this.ceiling.hide();
	}
	if (this.items !== null) {
		
		var values = this.items.getValues();
		for (var i=0; i<values.length; i++) {
			values[i]._img.style.display = "none";
		}
	}
	this.visible = false;
};
Cell.prototype.shade = function(chunk, x, y) {
	if (this.character !== null) {
		this.character.hideModel();
	}
	if (this.soundSource !== null) {
		this.soundSource.hide();
	}
	if (this.ceiling !== null) {
		this.ceiling.shade();
	}
	this.shadeItems();
};
Cell.prototype.unshade = function(chunk, x, y) {
	this.unshadeItems();
	if (this.soundSource !== null) {
		this.soundSource.show();
	}
	if (this.ceiling !== null) {
		this.ceiling.unshade();
	}
};
Cell.prototype.unshadeItems = function() {
	if (this.items === null) {
		return;
	}
	var values = this.items.getValues();
	for (var i in values) {
		values[i].itemImage.unshade();
	}
};
Cell.prototype.shadeItems = function() {
	if (this.items === null) {
		return;
	}
	var values = this.items.getValues();
	for (var i in values) {
		values[i].itemImage.shade();
	}
	
};
/**
 * Checks if cell has a certain item or not.
 * @see {ItemPile}
 * @see UniqueItem
 * @param {number} typeId
 * @param {number} param
 * @return {boolean}
 */
Cell.prototype.hasItem = function(typeId, param) {
	if (isUnique(typeId)) {
		return this.items.hasUnique(param);
	} else {
		if (this.items.hasPile(typeId)) {
			return this.items.getPile(typeId).amount >= param;
		}
	}
	return false;
};
Cell.prototype.addItem = function(x,y,item) {
	if (item instanceof UniqueItem || !this.items.hasPile(item.typeId, item.amount)) {
		this.items.addItem(item);
		var viewIndent = World.getViewIndentation(x,y,32);
		// Here we add to item's fields a new one, used only in lying items'
		// handling.
		item._img = document.createElement("img");
		item._img.style.position = "absolute";
		item._img.style.display = Player.canSee(x,y) ? "inline-block" : "none";
		item._img.src = "./images/items/"+item.typeId+".png";
		item._img.style.left = viewIndent.left + "px";
		item._img.style.top = viewIndent.top + "px";
		item._img.style.zIndex = 2;
		gameField.appendChild(item._img);
	} else {
		this.items.addItem(item);
	}
};
Cell.prototype.removeItem = function(typeId, param) {
	var item = this.items.getItem(typeId, param);
	this.items.remove(item);
	if (!this.items.contains(item)) {
		item._img.parentNode.removeChild(item._img);
	}
};
Cell.prototype.isWall = function() {
	return this.object instanceof Wall;
};
Cell.prototype.isDoor = function() {
	return this.object instanceof Door;
};

function CellItemImage(item) {
	item.__prototype.constructor.apply(this);
	this.item = item;
	this.image = null;
}
CellItemImage.prototype.show = function(x,y) {
	
};
CellItemImage.prototype.hide = function() {
	this.image.parentNode.removeChild(this.image);
	this.image = null;
};
CellItemImage.prototype.shade = function() {
	this.image.style.opacity = "0.5";
};
CellItemImage.prototype.unshade = function() {
	this.image.style.opacity = "1";
};
CellItemImage.prototype.remove = function() {
	if (this.image !== null) {
		this.hide();
	}
};


function Wall(x, y, type) {
	GameObject.apply(this, [x,y,type]);
	this._doorSides = [];
}
//Wall.prototype = new GameObject(0,0,90);
Wall.prototype = {};
Wall.prototype.show = function(x,y) {
	this.updateView(x,y);
	this.image.style.display = "inline-block";
};
Wall.prototype.updateView = function(x,y) {
	var postfix = "";
	this.image.src = "./images/walls/"+wallNames[this.type]+"_"+postfix+".png";

};
/**
 * 
 * @param {number} x
 * @param {number} y
 * @param {Side} side
 */
Wall.prototype.createDoorSide = function(x,y,side) {
	this._doorSides[side.getCardinalInt()] = new DoorSide(x,y,this,side);
};
/**
 * 
 * @param {Side} side
 */
Wall.prototype.removeDoorSide = function(side) {
	gameField.removeChild(this._doorSides[side.getCardinalInt()]._img);
	delete this._doorSides[side.getCardinalInt()];
};
Wall.prototype.hide = function() {
	this.image.style.display = "none";
	for (var i = 0; i<4; i++) {
		if (this._doorSides[i] === undefined) {
			continue;
		}
		this._doorSides[i].hide();
	}
};
Wall.prototype.remove = function(x,y) {
	this.image.parentNode.removeChild(this.image);
	delete cell.wall;
	delete cell.object;
	var sides = [0, -1, 1, 0, 0, 1, -1, 0];
	for (var i = 0; i<sides.length/2; i++) {
		var nx = this.x+sides[i*2];
		var ny = this.y+sides[i*2+1];
		if (neighbor!= null && neighbor.isWall() && neighbor.visible) {
			neighbor.object.hide();
			neighbor.object.show();
		}
	}
};
Wall.prototype.shade = function() {
	this.image.style.opacity = "0.5";
	for (var i = 0; i<4; i++) {
		if (this._doorSides[i] === undefined) {
			continue;
		}
		this._doorSides[i]._img.style.opacity = "0.5";
	}
};
Wall.prototype.cursorShade = function() {
	this.image.style.opacity = "0.2";
	for (var i=0; i<this._doorSides.length; i++) {
		if (this._doorSides[i] === undefined) {
			continue;
		}
		this._doorSides[i].style.opacity = "0.2";
	}
};
Wall.prototype.unshade = function() {
	this.image.style.opacity = "1";
	for (var i=0; i<this._doorSides.length; i++) {
		if (this._doorSides[i] === undefined) {
			continue;
		}
		this._doorSides[i]._img.style.opacity = "1";
	}
};

function Door(x,y,type) {
	GameObject.apply(this, [x, y, type]);
	this.image.style.top = parseInt(this.image.style.top)-10+"px";
}
//Door.prototype = new GameObject(0,0,90);
Door.prototype = {};
Door.prototype.show = function(x,y) {
	this.updateView(x,y);
	this.image.setAttribute("src", "./images/objects/"+this.type+(vertical ? "_v" : "")+".png");
	this.image.style.display = "inline-block";
	Side.cardinal.forEach(function() {
	// "this" here is a side object, the function is applied to each cardinal side.
		var d = this.side2d();
		if (
			cell !== null 
			&& (cell.object instanceof Wall) 
			&& cell.object._doorSides[this.opposite().getCardinalInt()] !== undefined
		) {
			cell.object._doorSides[this.opposite().getCardinalInt()].show();
		}
	}, []);
};
Door.prototype.display = function(x,y) {
	GameObject.prototype.display.apply(this);
	var self = this;
	Side.cardinal.forEach(function() {
	// "this" here is a side object, the function is applied to each cardinal side.
		var d = this.side2d();
		if (cell !== null && (cell.object instanceof Wall)) {
			cell.object.createDoorSide(x+d[0],y+d[1],this.opposite());
		}
	}, []);
};
Door.prototype.remove = function(x,y) {
	GameObject.prototype.remove.apply(this, arguments);
	var self = this;
	Side.cardinal.forEach(function() {
	// "this" here is a Side object, the function is applied to each cardinal side.
		var d = this.side2d();
		if (
			cell !== null 
			&& (cell.object instanceof Wall) 
			&& cell.object._doorSides[this.opposite().getCardinalInt()] !== undefined
		) {
			cell.object.removeDoorSide(this.opposite());
		}
	}, []);
};
Door.prototype.updateView = function(x,y) {
	var self = this;
	Side.cardinal.forEach(function() {
	// "this" here is a Side object, the function is applied to each cardinal side.
		var d = this.side2d();
		if (
			cell !== null 
			&& (cell.object instanceof Wall) 
			&& cell.object._doorSides[this.opposite().getCardinalInt()] === undefined
		) {
		// If there is a wall without a doorside, create a doorside.
			cell.object.createDoorSide(x+d[0],y+d[1],this.opposite());
			
		}
	}, []);
};
/**
 * Doorsides are special wall attributes. When wall's neighbour is a door, wall
 * must be displayed flat in that place. These flatinesses are the door sides.
 * Doorsides are only internally created by Door objects and shown by Wall
 * object.
 * 
 * @constructor
 * @see Wall
 * @see Door
 * @param {number} x
 * @param {number} y
 * @param {Wall} wall Wall on which door side "hangs".
 * @param {Side} side From which side from the wall are the door and the 
 * doorside.
 */
function DoorSide(x,y,wall,side) {
	this._s = side;
	this._img = document.createElement('img');
	var initialSide = side;
	if (GameField.cameraOrientation == Side.E) {
		side = (side+1)%4;
	} else if (GameField.cameraOrientation == Side.S) {
		side = (side+2)%4;
	} else if (GameField.cameraOrientation == Side.W) {
		side = (side+3)%4;
	}
	var viewIndent = GameField.getViewIndentation(x,y,32);
	this._img.style.position = "absolute";
	this._img.style.display = "none";
	this._img.setAttribute("src", "./images/walls/"+wallNames[wall.type]+"_d"+side.getCardinalInt()+".png");
	this._img.style.top = viewIndent.top-parseInt(objectProperties[wall.type][1])+32+"px";
	this._img.style.left = viewIndent.left+(-parseInt(objectProperties[wall.type][0])+32)/2+"px";
	this._img.style.zIndex = (100000+y)*2+1;
	gameField.appendChild(this._img);
}
DoorSide.prototype.show = function() {
	this._img.style.display = "block";
};
DoorSide.prototype.hide = function() {
	this._img.style.display = "none";
};
function SoundSource(x, y, type) {
	this.x = x;
	this.y = y;
	this.type = type;
	this.wrap = document.createElement("div");
	var text = document.createElement("div");
	
	this.wrap.className = "wrap";
	text.className = "speechBubbleText";
	text.innerText = soundTypes[this.type].name;
	this.wrap.style.zIndex = 9000;
	this.wrap.appendChild(text);
	gameField.appendChild(this.wrap);
	this.wrap.style.top = (32*this.y-text.clientHeight-12) + "px";
	this.wrap.style.left = (32*this.x-text.clientWidth/2+16) + "px"; 
}
SoundSource.prototype.show = function() {
	this.wrap.style.display = "inline-block";
};
SoundSource.prototype.hide = function() {
	this.wrap.style.display = "none";
};
SoundSource.prototype.remove = function() {
	gameField.removeChild(this.wrap);
};
function CeilingCell(x, y, parent) {
	this.parent = parent;
	this.image = document.createElement("img");
	this.image.style.width = "32px";
	this.image.style.height = "32px";
	this.image.style.zIndex = "9000";
	this.image.style.position = "absolute";
	
	var viewIndent = GameField.getViewIndentation(x,y,1);
	this.image.style.left = viewIndent.left*32+"px";
	this.image.style.top = (viewIndent.top*32-20)+"px";
	this.image.setAttribute("src","./images/floors/dryGrass_7.png");
	gameField.appendChild(this.image);
}
CeilingCell.prototype.show = function _() {
	this.image.style.display = "inline-block";
};
CeilingCell.prototype.hide = function _() {
	this.image.style.display = "none";
};
CeilingCell.prototype.shade = function _() {
	this.image.style.opacity = "0.5";
};
CeilingCell.prototype.unshade = function _() {
	this.image.style.opacity = "1";
};
function Ceiling(x, y, w, h, type) {
	this.x = x;
	this.y = y;
	this.width = w;
	this.height = h;
	for (var i = 0; i<w; i++) {
		for (var j=0; j<h; j++) {
			new CeilingCell(x+i,y+j,this);
		}
	}
}
Ceiling.prototype.hide = function _() {
	for (var i = 0; i<w; i++) {
		for (var j=0; j<h; j++) {
		}
	}
};
Ceiling.prototype.show = function _() {
	for (var i = 0; i<w; i++) {
		for (var j=0; j<h; j++) {
		}
	}
};


// Специальные функции для работы с объектами
function visToNum(x, y) {
	// Возвращает 1, если клетка видима, и 0, если клетка невидима или находится
	// за пределами карты
	return 1;
}
function seenToNum(x, y) {
	throw new Error("Used erlier, don't he how i need this now");
	// Возвращает 1, если клетка уже была увидена, и 0, если клетка не была
	// увидена или находится за пределами карты
	return +(x>=0&&x<Trr.width&&y>0&&y<Trr.height&& !!Player.seenCells[x][y]);
}
function wallToNum(x, y) {
	throw new Error("KEKEKE");
	// Возвращает 1, если на клетке есть стена, и 0, если на клетке нет стены
	// или нет такой клетки
	return +(x>=0&&x<Tr.width&&y>0&&y<Trr.height&&(!!Trr.cells[x][y].wall|| ! !(Trr.cells[x][y].object&&isDoor(Trr.cells[x][y].object.type))));
}
function anyItemsInCell(x, y) {
	throw new Error("KOKOKO");
	// Проверяет, есть ли в клетке предметы
	// На клетках, на которых не было и нет предметов, работает очень быстро
	for (var i in Trr.cells[x][y].items.uniqueItems) {
		// Если в клетке нет ни одного предмета, то этот цикл не начинается
		return true;
	}
	for (var i in Trr.cells[x][y].items.itemPiles) {
		// Если в клетке нет ни одного предмета, то этот цикл не начинается
		return true;
	}
	return false;
}
function getObject(x, y) {
	throw new Error("KAKAKA");
	// Возвращает объект стены или объект GameObject, если один из них есть в
	// клетке x:y, иначе - false
	var cell = Trr.getCell(x, y);
	return cell && (cell.wall || cell.object);
}
// Функции типов объектов
function isDoor(type) {
	return (type>=40&&type<50);
}
function isOpenDoor(type) {
	return isDoor(type) && (type%2==1);
}
function isWall(type) {
	return type<40 && type>0;
}
function isContainer(type) {
	return type>=60 && type<70;
}
function wallConnectsOnlyWithItself(wallId) {
	return wallId == 6;
}
