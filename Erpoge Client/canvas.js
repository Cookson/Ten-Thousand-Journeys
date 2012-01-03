


// Список изображений для кеширования

// Амуниция для куклы персонажа
imageNames = [ [] ];
for ( var i in NUM_OF_TILES) {
	var floorNum = getFloorNum(i);
	tiles[floorNum] = [];
	for ( var j = 0; j < NUM_OF_TILES[i]; j++) {
		tiles[floorNum][j] = new Image();
		tiles[floorNum][j].src = "./images/terrain/" + i + "_" + j + ".png";
	}
}
var imageNums = [ 2, 34, 58, 59, 60, 61, 600, 601,
		700, 1201, 1202, 1300, 1302, 100, 1304, 1402, 1505, 1501, 1504, 104, 1401, 403, 402, 1301];
bufCanvas = -1;
bufContext = -1;
bufImageData = -1;
var particleTypes = ["shiver1", "shiver2", "blood1", "spark1", "spark2", "yellow_spark1", "yellow_spark2"];
var particlesImageData = {};
// Установку и получение пикселя пришлось сделать функцией, а не методом
// объекта,
// потому что Firefox из getImageData возвращает стандартный объект, а не
// ImageData
function saveParticlesImageData() {
	var canvas = document.createElement("canvas");
	canvas.width = 100;
	canvas.height = 100;
	var ctx = canvas.getContext("2d");
	(function preloadParticles(num) {
		if (!num) {
			num = 0;
		}
		var image = new Image();
		
		image.onload = function() {
			ctx.drawImage(image,0,0);
			particlesImageData[particleTypes[num]] = ctx.getImageData(0,0,image.width,image.height);
			if (num+1 < particleTypes.length) {
				preloadParticles(num+1);
			}
		};
		
		image.src = "./images/effects/" + particleTypes[num] + ".png";
	})();
}

function cacheImages() {
	for (var i in imageNums) {
		preloadImage(imageNums[i], "./images/chardoll/");
	}
}
function preloadImage(num, folder) {
	images[num] = new Image();
	images[num].imgNum = num;
	images[num].src = folder + "" + num + ".png";
}
function setPixel(imageData, x, y, color) {
	// Установить цвет пикселя в ImageData
	// imageData - объект ImageData
	// color - [r,g,b,a]
	var r = color[0];
	var g = color[1];
	var b = color[2];
	var a = color[3];
	imageData.data[(x + y * imageData.width) * 4] = r;
	imageData.data[(x + y * imageData.width) * 4 + 1] = g;
	imageData.data[(x + y * imageData.width) * 4 + 2] = b;
	imageData.data[(x + y * imageData.width) * 4 + 3] = a;
}
function getPixel(imageData, x, y) {
	// Получить цвет пикселя
	// Формат: [r,g,b]
	return [ imageData.data[(x + y * imageData.width) * 4],
			imageData.data[(x + y * imageData.width) * 4 + 1],
			imageData.data[(x + y * imageData.width) * 4 + 2],
			imageData.data[(x + y * imageData.width) * 4 + 3] ];
}
CanvasRenderingContext2D.prototype.applyTransition = function(transition) {
	// Буферный canvas, из которого будет нарисовано изображение в this
	if (bufCanvas == -1) {
		bufCanvas = document.createElement("canvas");
		bufCanvas.setAttribute("width", "32");
		bufCanvas.setAttribute("height", "32");
		bufContext = bufCanvas.getContext("2d");
		bufImageData = bufContext.getImageData(0, 0, 32, 32);
	}
	for ( var i = 0; i < transition.width; i++) {
		for ( var j = 0; j < transition.height; j++) {
			setPixel(bufImageData, i, j, getPixel(transition, i, j));
		}
	}
	bufContext.putImageData(bufImageData, 0, 0);
	this.drawImage(bufCanvas, 0, 0);
};
CanvasRenderingContext2D.prototype.getTransition = function(tileType,
		direction, put, x, y) {
	// Возвращает "переход" к тайлу tiles[tileNum]
	// Возвращает imageData
	// put - отрисовывать ли сразу же на той же канве этот переход
	// x,y - координаты начальной точки в пикселяхы
	// Получаем данные изображения, от которого переходим, на том тайле, в
	// который размещаем переход
	if (x === undefined) {
		x = 0;
		y = 0;
	}
	var imageData = this.getImageData(x, y, 32, 32);
	
	// Получаем данные об изображении, к которому переходим
	var tileImageCtx = document.createElement("canvas").getContext("2d");
	tileImageCtx.drawImage(tiles[tileType][0], 0, 0);
	var tileImageData = tileImageCtx.getImageData(0, 0, 32, 32);
	var i;
	var j;
	if (direction == 0) {
		// Вверх
		iMin = 0;
		iMax = 32;
		jMin = 0;
		jMax = 4;
	} else if (direction == 1) {
		// Направо
		iMin = 28;
		iMax = 32;
		jMin = 0;
		jMax = 32;
	} else if (direction == 2) {
		// Вниз
		iMin = 0;
		iMax = 32;
		jMin = 28;
		jMax = 32;
	} else if (direction == 3) {
		// Направо
		iMin = 0;
		iMax = 4;
		jMin = 0;
		jMax = 32;
	}
	for ( var i = iMin; i < iMax; i++) {
		for ( var j = jMin; j < jMax; j++) {
			var chance;
			if (i == 0 || i == 32 || j == 0 || j == 32) {
				chance = 20;
			} else if (i == 1 || i == 31 || j == 1 || j == 31) {
				chance = 60;
			} else if (i == 2 || i == 30 || j == 2 || j == 30) {
				chance = 80;
			} else {
				chance = 90;
			}
			if (Math.floor(Math.random() * 100) < chance) {
				continue;
			}
			setPixel(imageData, i, j, getPixel(tileImageData, i, j));
		}
		j = 0;
	}
	if (put) {
		this.putImageData(imageData, x, y);
	} else {
		return imageData;
	}
};
CanvasRenderingContext2D.prototype.glowSprite = function(color) {
	
};
function pixelArrayToHex(arr) {
	// Принимает цвет в dec-формате массива [r,g,b], возвращает цвет в
	// hex-формате "#rrggbb"
	return "#" + dec2hex(arr[0], true) + dec2hex(arr[1], true)
			+ dec2hex(arr[2], true);
}
function dec2hex(dec, has2symblos) {
	var hex = "0123456789ABCDEF";
	var result = [];
	while (dec >= 16) {
		result[result.length] = hex.charAt(dec % 16);
		dec = Math.floor(dec / 16);
	}
	result[result.length] = hex.charAt(dec);
	if (has2symblos && result.length == 1) {
		result.push("0");
	}
	return result.reverse().join("");
}
function getTileSetOnCanvas() {
	// Загрузить изображения из tiles на отдельный canvas, чтобы постоянно иметь
	// их в памяти
	var tileSetContext = document.getElementById("tileSet").getContext("2d");
	var x = 0;
	var y = 0;
	for ( var i = 0; i < tiles.length; i++) {
		tileSetContext.drawImage(tiles[i], x, y);
		x += 32;
		if (x == 640) {
			y += 32;
			x = 0;
		}
	}
}
function Doll(character) {
	// Кукла персонажа
	// Использует глобальные переменные: charDollImages,
	// Аргумент character - объект персонажа, которому принадлежит эта кукла
	this.character = character;
	this.DOMNode = document.createElement("canvas");
	this.DOMNode.width = 32;
	this.DOMNode.height = 32;
	this.ctx = this.DOMNode.getContext("2d");
	this.hands = [ [ 6, 14 ] ];
	this.drawn = false; // Кукла уже была отрисована как минимум один раз
	this.items = {
		weapon : 50
	};
	this.drawBody = function() {
		this.ctx.drawImage(images[(this.character) ? 58 + this.character.race : 58], 0, 0);
	};
	this.drawAmmunition = function(typeId) {
		this.ctx.drawImage(images[typeId], 0, 0);
	};
	this.draw = function() {
		this.ctx.clearRect(0, 0, 32, 32);
		this.drawBody();
		if (
			this.character && this.character.ammunition &&
			this.character.ammunition.getItemInSlot(6)
		) {
			this.drawAmmunition(this.character.ammunition.getItemInSlot(6).typeId);
		}
		if (!onGlobalMap || this.character.characterId == player.characterId) {
			for ( var i = 0; i < Ammunition.prototype.NUMBER_OF_SLOTS; i++) {
				if (!this.character.ammunition.hasItemInSlot(i)) {
					continue;
				}
				if (!images[this.character.ammunition.getItemInSlot(i).typeId]) {
					continue;
				}
				if (i == 6) {
					continue;
				}
				if (
					this.character && this.character.ammunition &&
					this.character.ammunition.getItemInSlot(i)
				) {
					this.drawAmmunition(this.character.ammunition.getItemInSlot(i).typeId);
				}
			}
		} else if (onGlobalMap && !this.character.characterId == player.characterId) {
			this.drawAmmunition(342);
			this.drawAmmunition(344);
		}
		
		this.drawn = true;
	};
}
function Minimap(elem) {
	this.w = width;
	this.h = height;
	this.scale = 2; // Размер клетки на карте в пикселях
	this.ctx;
	this.elem = elem;
	this.cells;
	this.init();
}
Minimap.prototype.init = function() {
	this.elem = this.elem ? this.elem : document.createElement("canvas");
	this.elem.width = Terrain.getHorizontalDimension() * this.scale;
	this.elem.height = Terrain.getVerticalDimension() * this.scale;
	this.ctx = this.elem.getContext("2d");
	// this.elem.style.position="absolute";
	// this.elem.style.top="200px";
	// this.elem.style.left="200px";
	// this.elem.style.zIndex="9000";
	// document.body.appendChild(this.elem);

	this.cells = blank2dArray();
	for ( var x = 0; x < this.w; x++) {
		for ( var y = 0; y < this.h; y++) {
			this.cells[x][y] = 0;
		}
	}
	this.ctx.fillStyle = this.fillStyles["empty"];
	this.ctx.fillRect(0, 0, this.w * this.scale, this.h * this.scale);
	this.draw();
};
Minimap.prototype.draw = function() {
	// Отрисовка миникарты
	var charactersCoords = [];
	for ( var i in characters) {
		// Нахождение координат всех видимых мобов
		var x = characters[i].x;
		var y = characters[i].y;
		if (player.visibleCells[x][y]) {
			charactersCoords[i] = [ x, y ];
		}
	}
	var obj;
	for ( var x = 0; x < this.w; x++) {
		for ( var y = 0; y < this.h; y++) {
			if (!player.visibleCells[x][y]) {
				if (player.seenCells[x][y]) {
					if (getObject(x, y)) {
						this.drawCell(x, y, "seenWall");
					} else {
						this.drawCell(x, y, "seenFloor");
					}
				} else {
					this.drawCell(x, y, "empty");
				}
			} else if (anyItemsInCell(x, y)) {
				// Предмет
				this.drawCell(x, y, "item");
			} else if (obj = getObject(x, y)) {
				this.drawCell(x, y, "wall");
			} else {
				this.drawCell(x, y, "floor");
			}
		}
	}
	for ( var i in charactersCoords) {
		if (characters[i].isClientPlayer) {
			this.drawCell(charactersCoords[i][0], charactersCoords[i][1], "clientPlayer");
		} else if (!player.isEnemy(characters[i])) {
			this.drawCell(charactersCoords[i][0], charactersCoords[i][1], "ally");
		} else {
			this.drawCell(charactersCoords[i][0], charactersCoords[i][1], "enemy");
		}
	}
};
Minimap.prototype.drawCell = function(x, y, fillStyle) {
	// Отрисовывает ячейку миникарты. fillStyle - название стиля заполнения
	// клетки (из Minimap.prototype.fillStyles)
	if (this.cells[x][y] == this.cellTypes[fillStyle]) {
		return false;
	}
	this.ctx.fillStyle = this.fillStyles[fillStyle];
	var viewIndent = Terrain.getViewIndentation(x, y, this.scale);
	this.ctx.fillRect(viewIndent.left, viewIndent.top, this.scale, this.scale);
};
Minimap.prototype.fillStyles = {
	clientPlayer : "rgb(56,255,56)",
	ally : "rgb(70,180,100)",
	enemy : "rgb(150,80,100)",
	item : "rgb(80,100,120)",
	wall : "rgb(120,120,120)",
	floor : "rgb(80,80,80)",
	empty : "rgb(0,0,0)",
	seenFloor : "rgb(40,40,40)",
	seenWall : "rgb(100,100,100)"
};
Minimap.prototype.changeDimensions = function(newWidth, newHeight) {
	this.w = newWidth;
	this.h = newHeight;
	this.init();
};
Minimap.prototype.cellTypes = {
	empty : 0,
	floor : 1,
	wall : 2,
	player : 5,
	ally : 4,
	enemy : 3,
	item : 6
};
