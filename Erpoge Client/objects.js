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
	this.floor = null;
	this.soundSource = null;
	/** @private @type Character */
	this.character = null;
	this.visible = false;
	this.passability = Terrain.PASS_FREE;
}
/**
 * 
 * @param {Chunk} chunk
 * @param {number} x
 * @param {number} y
 */
Cell.prototype.show = function(chunk, x, y) {
	var floor = this.floor;
//	setTimeout(function() {
		floor.show(chunk, x, y);
//	},random(1,10));
	
	if (this.object !== null) {
		this.object.show(x, y);
	}
	if (this.character !== null) {
		this.character.showModel();
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
	this.floor.hide(chunk, x, y);
	if (this.object !== null) {
		this.object.hide(x, y);
	}
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
	this.floor.shade(chunk, x, y);
	if (this.object !== null) {
		this.object.shade(x, y);
	}
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
	this.floor.unshade(chunk, x, y);
	if (this.object !== null) {
		this.object.unshade();
	}
	if (this.character !== null) {
		this.character.showModel();
	}
	this.unshadeItems();
	if (this.soundSource !== null) {
		this.soundSource.show();
	}
	if (this.ceiling !== null) {
		this.ceiling.unshade();
	}
};
Cell.prototype.unshadeItems = function() {
	var values = this.items.getValues();
	for (var i in values) {
		values[i].itemImage.unshade();
	}
};
Cell.prototype.shadeItems = function() {
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
		this.items.add(item);
		var viewIndent = Terrain.getViewIndentation(x,y,32);
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
		this.items.add(item);
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
	item.__prototype.constructor.apply(this)
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

function GameObject(x, y, type) {
	this.type = type;
	this.image = document.createElement("img");
	this.mod = -1;
	var viewIndent = Terrain.getViewIndentation(x, y, 1);
	this.image.style.position = "absolute";
	if (!this instanceof Wall) {
		this.image.setAttribute("src", "./images/objects/"+type+".png");
	}
	this.image.style.top = viewIndent.top*32+(-parseInt(objectProperties[type][1])+32)+"px";
	this.image.style.left = viewIndent.left*32+(-parseInt(objectProperties[type][0])+32)/2+"px";
	this.image.style.zIndex = (100000+y)*2;
	this.image.style.display = "none";
}
GameObject.prototype.show = function() {
	this.image.style.display = "inline-block";
};
GameObject.prototype.hide = function() {
	this.image.style.display = "none";
};
GameObject.prototype.remove = function(x,y) {
	this.image.parentNode.removeChild(this.image);
	Terrain.getCell(x,y).object = null;
	Terrain.getCell(x,y).passability = Terrain.PASS_FREE;
};
GameObject.prototype.shade = function() {
	this.image.style.opacity = "0.5";
};
GameObject.prototype.cursorShade = function() {
	this.image.style.opacity = "0.2";
};
GameObject.prototype.unshade = function() {
	this.image.style.opacity = "1";
};
/**
 * Display functions are called after all the GameObjects, Walls, Doors etc are
 * created to append all these elements' HTMLElements to to game field.
 */
GameObject.prototype.display = function() {
	gameField.appendChild(this.image);
};

function Wall(x, y, type) {
	GameObject.apply(this, [x,y,type]);
	this._doorSides = [];
}
Wall.prototype = new GameObject(0,0,90);
Wall.prototype.show = function(x,y) {
	this.updateView(x,y);
	this.image.style.display = "inline-block";
};
Wall.prototype.updateView = function(x,y) {
	var postfix = "";
	var postfixN = (Terrain.getCell(x,y-1) && (Terrain.getCell(x,y-1).isWall() || Terrain.getCell(x,y-1).isDoor())) ? "1" : "0";
	var postfixE = (Terrain.getCell(x+1,y) && (Terrain.getCell(x+1,y).isWall() || Terrain.getCell(x+1,y).isDoor())) ? "1" : "0";
	var postfixS = (Terrain.getCell(x,y+1) && (Terrain.getCell(x,y+1).isWall() || Terrain.getCell(x,y+1).isDoor())) ? "1" : "0";
	var postfixW = (Terrain.getCell(x-1,y) && (Terrain.getCell(x-1,y).isWall() || Terrain.getCell(x-1,y).isDoor())) ? "1" : "0";
	if (Terrain.cameraOrientation == Side.N) {
		postfix = postfixN + postfixE + postfixS + postfixW;
	} else if (Terrain.cameraOrientation == Side.E) {
		postfix = postfixW + postfixN + postfixE + postfixS;
	} else if (Terrain.cameraOrientation == Side.S) {
		postfix = postfixS + postfixW + postfixN + postfixE;
	} else if (Terrain.cameraOrientation == Side.W) {
		postfix = postfixE + postfixS + postfixW + postfixN;
	}
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
	var cell = Terrain.getCell(x,y);
	cell.passability = Terrain.PASS_FREE;
	delete cell.wall;
	delete cell.object;
	var sides = [0, -1, 1, 0, 0, 1, -1, 0];
	for (var i = 0; i<sides.length/2; i++) {
		var nx = this.x+sides[i*2];
		var ny = this.y+sides[i*2+1];
		var neighbor = Terrain.getCell(nx,ny);
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
		this._doorSides[i].style.opacity = "0.5";
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
	for (var i = 0; i<this.doorSides.length; i++) {
		if (this._doorSides[i] === undefined) {
			continue;
		}
		this._doorSides[i].style.opacity = "1";
	}
};
function Floor(type) {
	this.type = type;
	this.isCanvas = false;
}
Floor.prototype.show = function(chunk, x, y, noForceNeighbourReshow) {
	// Отобразить изображение тайла
	// Аргумент noForceNeighbourReshow ставится в true, когда метод show()
	// вызван из метода show() объекта,
	// изображение которого рисуется на канве. Аргумент используется во
	// избежание рекурсии.
	if ((visToNum(x-1, y)+visToNum(x+1, y))
			*(visToNum(x, y-1)+visToNum(x, y+1))<=1
			&&(wallToNum(x-1, y)+wallToNum(x+1, y))
					*(wallToNum(x, y-1)+wallToNum(x,
							y+1))==1
			&&seenToNum(x-1, y)+seenToNum(x, y-1)
					+seenToNum(x+1, y)
					+seenToNum(x, y+1)==2) {
		// Исключение для "уголков" за угловыми стенами
		return false;
	} else {
		
	}
	var viewIndent = Terrain.getViewIndentation(x-chunk.x,y-chunk.y,32);
	if (
		Terrain.getCell(x+1,y) && Terrain.getCell(x+1,y).floor.type!=this.type
		|| Terrain.getCell(x-1,y) && Terrain.getCell(x-1,y).floor.type!=this.type
		|| Terrain.getCell(x,y+1) && Terrain.getCell(x,y+1).floor.type!=this.type
		|| Terrain.getCell(x,y-1) && Terrain.getCell(x,y-1).floor.type!=this.type
	) {
	// Если этот тайл граничит с тайлом другого типа
		// Получаем типы соседних тайлов или тип этого тайла, если такого
		// соседнего тайла нет (если этот тайл на границе)
		var up = Terrain.getCell(x,y-1) ? Terrain.getCell(x,y-1).floor.type : this.type;
		var right = Terrain.getCell(x+1,y) ? Terrain.getCell(x+1,y).floor.type : this.type;
		var down = Terrain.getCell(x,y+1) ? Terrain.getCell(x,y+1).floor.type : this.type;
		var left = Terrain.getCell(x-1,y) ? Terrain.getCell(x-1,y).floor.type : this.type;
		if (Terrain.cameraOrientation == Side.E) {
			var leftBuf = left;
			left = down;
			down = right;
			right = up;
			up = leftBuf;
		} else if (Terrain.cameraOrientation == Side.S) {
			var upBuf = up;
			var leftBuf = left;
			up = down;
			left = right;
			right = leftBuf;
			down = upBuf;
		} else if (Terrain.cameraOrientation == Side.W) {
			var upBuf = up;
			up = right;
			right = down;
			down = left;
			left = upBuf;
		}
		var tileType= "t"+this.type+","+up+","+right+","+down+","+left;
		
		if (floorImages[tileType] !== undefined) {
			// Если изображение такого тайла уже использовалось, и поэтому
			// сгенерировано
			// Отрисовка основного изображения
			var ctx = chunk.canvas.getContext("2d");
			if (+localStorage.getItem(2)) {
				var imageData = floorImages[tileType];
				// Отрисовка сетки
				for (var i = 0; i<32; i++) {
					var pix = getPixel(imageData, i, 0);
					pix[3] = 200;
					setPixel(imageData, i, 0, pix);
				}
				for (var j = 0; j<32; j++) {
					var pix = getPixel(imageData, 0, j);
					pix[3] = 200;
					setPixel(imageData, 0, j, pix);
				}
				ctx.putImageData(imageData, viewIndent.left, viewIndent.top);
			} else {
				ctx.putImageData(floorImages[tileType], viewIndent.left,
						viewIndent.top);
			}
			if (+localStorage.getItem(2)) {
				// Отрисовка сетки
				for ( var i = 0; i<32; i++ ) {
					var pix = getPixel(floorImages[tileType], i, 0);
					pix[3] = 127;
					setPixel(floorImages[tileType], i, 0, pix);
				}
				for ( var j = 0; j<32; j++ ) {
					var pix = getPixel(floorImages[tileType], 0, j);
					pix[3] = 127;
					setPixel(floorImages[tileType], 0, j, pix);
				}
			}
		} else {
			// Если изображение такого тайла ещё не сгенерировано, то
			// создать его
			// Отрисовка основного изображения
			var ctx = chunk.canvas.getContext("2d");
			ctx.drawImage(tiles[this.type][0], viewIndent.left, viewIndent.top);

			// Отрисовка переходов
			var neighbors = [up, right, down, left];
			for (var i=0; i<4; i++) {				
				if (neighbors[i]!=this.type) {
					ctx.getTransition(neighbors[i], i, true, viewIndent.left,
							viewIndent.top);
				}
			}
			floorImages[tileType] = ctx.getImageData(viewIndent.left, viewIndent.top,
					32, 32);
			if ( +localStorage.getItem(2)) {
				// Отрисовка сетки
				for ( var i = 0; i<32; i++ ) {
					var pix = getPixel(floorImages[tileType], i, 0);
					pix[3] = 127;
					setPixel(floorImages[tileType], i, 0, pix);
				}
				for ( var j = 0; j<32; j++ ) {
					var pix = getPixel(floorImages[tileType], 0, j);
					pix[3] = 127;
					setPixel(floorImages[tileType], 0, j, pix);
				}
			}
		}
	} else {
	// If tile does not bound with a tile of another type
		var ctx = chunk.canvas.getContext("2d");
		ctx.drawImage(tiles[this.type][getNumFromSeed(x, y,
				NUM_OF_TILES[floorNames[this.type]])], viewIndent.left, viewIndent.top);
		if (+localStorage.getItem(2)) {
			var imageData = ctx.getImageData(viewIndent.left, viewIndent.top, 32, 32);
			// Отрисовка сетки
			for (var i = 0; i<32; i++) {
				var pix = getPixel(imageData, i, 0);
				pix[3] = 200;
				setPixel(imageData, i, 0, pix);
			}
			for (var j = 0; j<32; j++) {
				var pix = getPixel(imageData, 0, j);
				pix[3] = 200;
				setPixel(imageData, 0, j, pix);
			}
			
			ctx.putImageData(imageData, viewIndent.left, viewIndent.top);
		}
	}
	if ((visToNum(x-1, y)+visToNum(x+1, y))
			*(visToNum(x, y-1)+visToNum(x, y+1))==1
			&&(wallToNum(x-1, y)+wallToNum(x+1, y))
					*(wallToNum(x, y-1)+wallToNum(x,
							y+1))==1
			&&(seenToNum(x-1, y)+seenToNum(x, y-1)
					+seenToNum(x+1, y)
					+seenToNum(x, y+1)==4||seenToNum(x-1,y)
					+seenToNum(x, y-1)
					+seenToNum(x+1, y)
					+seenToNum(x, y+1)==2)) {
		// Случай затенения уголков у стен
		this.shade();
	}
};
Floor.prototype.hide = function(chunk,x,y) {
	var ctx = chunk.canvas.getContext("2d");
	ctx.clearRect((x-chunk.x)*32, (y-chunk.y)*32, 32, 32);
	// if (this.image!=null) {
	// this.image.parentNode.removeChild(this.image);
	// this.image=null;
	// }
};
Floor.prototype.shade = function(chunk, x, y) {
	var ctx = chunk.canvas.getContext("2d");
	var imgData = ctx.getImageData(x*32, y*32, 32, 32);
	var len = imgData.width*imgData.height; // Количество пикселей в
	// выделенной зоне
	// (количество элементов в массиве - в 4 раза больше (RGBA))
	for ( var i = 0; i<len; i++ ) {
		imgData.data[i*4+3] = 128;
	}
	ctx.putImageData(imgData, x*32, y*32);
	if ( +localStorage.getItem(2)) {
		var imageData = ctx.getImageData(x*32, y*32, 32, 32);
		// Отрисовка сетки
		for (var i = 0; i<32; i++) {
			var pix = getPixel(imageData, i, 0);
			pix[3] = 140;
			setPixel(imageData, i, 0, pix);
		}
		for (var j = 0; j<32; j++) {
			var pix = getPixel(imageData, 0, j);
			pix[3] = 140;
			setPixel(imageData, 0, j, pix);
		}
		ctx.putImageData(imageData, x*32, y*32);
	}
};
Floor.prototype.unshade = function(chunk, x, y) {
	var ctx = chunk.canvas.getContext("2d");
	var imgData = ctx.getImageData(x*32, y*32, 32, 32);
	var len = imgData.width*imgData.height; // Количество пикселей в
	// выделенной зоне
	// (количество элементов в массиве - в 4 раза больше (RGBA))
	for ( var i = 0; i<len; i++ ) {
		imgData.data[i*4+3] = 255;
	}
	ctx.putImageData(imgData, x*32, y*32);
};
function Door(x,y,type) {
	GameObject.apply(this, [x, y, type]);
	this.image.style.top = parseInt(this.image.style.top)-10+"px";
}
Door.prototype = new GameObject(0,0,90);
Door.prototype.show = function(x,y) {
	var vertical = Terrain.getCell(x,y+1) && Terrain.getCell(x,y+1).isWall() && Terrain.getCell(x,y-1).wall !== null;
	if (Terrain.cameraOrientation == Side.E || Terrain.cameraOrientation == Side.W) {
		vertical = !vertical;
	}
	this.updateView(x,y);
	this.image.setAttribute("src", "./images/objects/"+this.type+(vertical ? "_v" : "")+".png");
	this.image.style.display = "inline-block";
	Side.cardinal.forEach(function() {
	// "this" here is a side object, the function is applied to each cardinal side.
		var d = this.side2d();
		var cell = Terrain.getCell(x+d[0], y+d[1]);
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
		var cell = Terrain.getCell(x+d[0], y+d[1]);
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
		var cell = Terrain.getCell(x+d[0], y+d[1]);
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
		var cell = Terrain.getCell(x+d[0], y+d[1]);
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
	if (Terrain.cameraOrientation == Side.E) {
		side = (side+1)%4;
	} else if (Terrain.cameraOrientation == Side.S) {
		side = (side+2)%4;
	} else if (Terrain.cameraOrientation == Side.W) {
		side = (side+3)%4;
	}
	var viewIndent = Terrain.getViewIndentation(x,y,32);
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
	
	var viewIndent = Terrain.getViewIndentation(x,y,1);
	this.image.style.left = viewIndent.left*32+"px";
	this.image.style.top = (viewIndent.top*32-20)+"px";
	this.image.setAttribute("src","./images/terrain/dryGrass_7.png");
	gameField.appendChild(this.image);
	Terrain.cells[x][y].ceiling = this;
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
	Terrain.ceilings.push(this);	
}
Ceiling.prototype.hide = function _() {
	for (var i = 0; i<w; i++) {
		for (var j=0; j<h; j++) {
			Terrain.cells[this.x+i][this.y+j].ceiling.hide();
		}
	}
};
Ceiling.prototype.show = function _() {
	for (var i = 0; i<w; i++) {
		for (var j=0; j<h; j++) {
			Terrain.cells[this.x+i][this.y+j].ceiling.show();
		}
	}
};

/**
 * Chunks represent a square part of terrain
 */
function Chunk(x, y, data) {
	this.x = x;
	this.y = y;
	this.cells = blank2dArray(Terrain.CHUNK_WIDTH, Terrain.CHUNK_WIDTH);
	this.canvas = document.createElement("canvas");
	this.canvas.setAttribute("width", 32*Terrain.CHUNK_WIDTH);
	this.canvas.setAttribute("height", 32*Terrain.CHUNK_WIDTH);
	this.canvas.style.position = "absolute";
	var viewIndent = Terrain.getViewIndentation(x, y, 32);
	this.canvas.style.top = viewIndent.top+"px";
	this.canvas.style.left = viewIndent.left+"px";
	this.canvas.style.zIndex = 0;
	document.getElementById("gameField").appendChild(this.canvas);
}
Chunk.prototype.loadData = function(data) {
	/*
	 * data: {x:int,y:int,c:[floor,object, floor,object...]}
	 */
	for (var i=0, x=0, y=0; i<data.c.length; i+=2) {
		this.cells[x][y] = new Cell();
		if (x === Terrain.CHUNK_WIDTH-1) {
			y++;
			x = 0;
		} else {
			x++;
		}
	}
	for (var i=0, x=0, y=0; i<data.c.length; i+=2) {
		var cell = this.cells[x][y];
		cell.floor = new Floor(data.c[i]);
		if (data.c[i+1] !== 0) {
			Terrain.createObject(this.x+x, this.y+y, data.c[i+1]);
		}
		if (x === Terrain.CHUNK_WIDTH-1) {
			y++;
			x = 0;
		} else {
			x++;
		}
	}
	for (var i=0, x=0, y=0; i<data.c.length; i+=2) {
		if (data.c[i+1] !== 0) {
			Terrain.displayObject(this.x+x, this.y+y);
		}
		if (x === Terrain.CHUNK_WIDTH-1) {
			y++;
			x = 0;
		} else {
			x++;
		}
	}
	// Items loading
	for (var i=0, x=0, y=0; i<data.i.length; i+=4) {
		var item;
		if (isUnique(data.i[i+2])) {
			item = new UniqueItem(data.i[i+2], data.i[i+3]);
		} else {
			item = new ItemPile(data.i[i+2], data.i[i+3]);
		}
		Terrain.createItem(data.i[i],data.i[i+1],item);
		if (x === Terrain.CHUNK_WIDTH-1) {
			y++;
			x = 0;
		} else {
			x++;
		}
	}
	// Characters loading
	for (var i=0; i<data.ch.length; i++) {
		
	}
};
Chunk.prototype.getAbsoluteCell = function(x, y) {
	return this.cells[x-this.x][y-this.y];
};
//Chunk.prototype.show = function() {
//	throw new Error("FUCK");
//	for (var y=0; y<Terrain.CHUNK_WIDTH; y++) {
//		for (var x=0; x<Terrain.CHUNK_WIDTH; x++) {
//			if (Player.sees(this.x+x, this.y+y)) {
//				this.cells[x][y].show(this, this.x+x, this.y+y);
//			}
//		}
//	}
//	// Redraw walls on border of neighbor chunks
//	var chunk;
//	// From top
//	if (chunk = Terrain.getChunkByCoord(this.x,this.y-Terrain.CHUNK_WIDTH)) {
//		for (var x=0; x<Terrain.CHUNK_WIDTH; x++) {
//			if (chunk.cells[x][Terrain.CHUNK_WIDTH-1].object.updateView !== undefined) { 
//				chunk.cells[x][Terrain.CHUNK_WIDTH-1].object
//					.updateView(this.x+x,this.y-1);
//			}
//		}
//	}
//	// From right
//	if (chunk = Terrain.getChunkByCoord(this.x+Terrain.CHUNK_WIDTH,this.y)) {
//		var col = chunk.cells[0];
//		for (var y=0; y<Terrain.CHUNK_WIDTH; y++) {
//			if (col[y].object.updateView != undefined) {
//				col[y].object.updateView(this.x+Terrain.CHUNK_WIDTH,this.y+y);
//			}
//		}
//	}
//	// From bottom
//	if (chunk = Terrain.getChunkByCoord(this.x,this.y+Terrain.CHUNK_WIDTH)) {
//		for (var x=0; x<Terrain.CHUNK_WIDTH; x++) {
//			if (chunk.cells[x][0].object.updateView !== undefined) {
//				chunk.cells[x][0].object.updateView(this.x+x, this.y+Terrain.CHUNK_WIDTH);
//			}
//		}
//	}
//	// From left
//	if (chunk = Terrain.getChunkByCoord(this.x-Terrain.CHUNK_WIDTH,this.y)) {
//		var col = chunk.cells[Terrain.CHUNK_WIDTH-1];
//		for (var y=0; y<Terrain.CHUNK_WIDTH; y++) {
//			if (col[y].object.updateView !== undefined) {
//				col[y].object.updateView(this.x-1,this.y+y);
//			}
//		}
//	}
//};

// Специальные функции для работы с объектами
function visToNum(x, y) {
	// Возвращает 1, если клетка видима, и 0, если клетка невидима или находится
	// за пределами карты
	return 1;
}
function seenToNum(x, y) {
	// Возвращает 1, если клетка уже была увидена, и 0, если клетка не была
	// увидена или находится за пределами карты
	return +(x>=0&&x<Terrain.width&&y>0&&y<Terrain.height&& !!Player.seenCells[x][y]);
}
function wallToNum(x, y) {
	// Возвращает 1, если на клетке есть стена, и 0, если на клетке нет стены
	// или нет такой клетки
	return +(x>=0&&x<Terrain.width&&y>0&&y<Terrain.height&&(!!Terrain.cells[x][y].wall|| ! !(Terrain.cells[x][y].object&&isDoor(Terrain.cells[x][y].object.type))));
}
function anyItemsInCell(x, y) {
	// Проверяет, есть ли в клетке предметы
	// На клетках, на которых не было и нет предметов, работает очень быстро
	for (var i in Terrain.cells[x][y].items.uniqueItems) {
		// Если в клетке нет ни одного предмета, то этот цикл не начинается
		return true;
	}
	for (var i in Terrain.cells[x][y].items.itemPiles) {
		// Если в клетке нет ни одного предмета, то этот цикл не начинается
		return true;
	}
	return false;
}
function getObject(x, y) {
	// Возвращает объект стены или объект GameObject, если один из них есть в
	// клетке x:y, иначе - false
	var cell = Terrain.getCell(x, y);
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
