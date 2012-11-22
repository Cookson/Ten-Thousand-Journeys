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
	for ( var i=0; i<tiles.length; i++) {
		tileSetContext.drawImage(tiles[i], x, y);
		x += 32;
		if (x == 640) {
			y += 32;
			x = 0;
		}
	}
}
function Minimap(elem) {
	throw new Error("Not reimplemented");
	this.w = FUCK;
	this.h = FUCK;
	this.scale = 2; // Размер клетки на карте в пикселях
	this.ctx;
	this.elem = elem;
	this.cells;
	this.init();
}
Minimap.prototype.init = function() {
	this.elem = this.elem ? this.elem : document.createElement("canvas");
	this.elem.width = GameField.getHorizontalDimension() * this.scale;
	this.elem.height = GameField.getVerticalDimension() * this.scale;
	this.ctx = this.elem.getContext("2d");
	// this.elem.style.position="absolute";
	// this.elem.style.top="200px";
	// this.elem.style.left="200px";
	// this.elem.style.zIndex="9000";
	// document.body.appendChild(this.elem);

	this.cells = blank2dArray();
	for (var x = 0; x < this.w; x++) {
		for (var y = 0; y < this.h; y++) {
			this.cells[x][y] = 0;
		}
	}
	this.ctx.fillStyle = this.fillStyles["empty"];
	this.ctx.fillRect(0, 0, this.w * this.scale, this.h * this.scale);
	this.draw();
};
/**
 * Redraw minimap.
 */
Minimap.prototype.draw = function() {
	// Отрисовка миникарты
	var charactersCoords = {};
	for (var i in characters) {
		// Нахождение координат всех видимых мобов
		var x = characters[i].x;
		var y = characters[i].y;
		if (Player.visibleCells[x][y]) {
			charactersCoords[i] = [x, y];
		}
	}
	var obj;
	for (var x = 0; x < this.w; x++) {
		for ( var y = 0; y < this.h; y++) {
			if (!Player.visibleCells[x][y]) {
				if (Player.seenCells[x][y]) {
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
	for (var i in charactersCoords) {
		if (characters[i].isClientPlayer) {
			this.drawCell(charactersCoords[i][0], charactersCoords[i][1], "clientPlayer");
		} else if (!Player.isEnemy(characters[i])) {
			this.drawCell(charactersCoords[i][0], charactersCoords[i][1], "ally");
		} else {
			this.drawCell(charactersCoords[i][0], charactersCoords[i][1], "enemy");
		}
	}
};
/**
 * @private
 * Draw a single cell on minimap.
 * 
 * @param {Number} x
 * @param {Number} y
 * @param fillStyle
 */
Minimap.prototype.drawCell = function(x, y, fillStyle) {
	// Отрисовывает ячейку миникарты. fillStyle - название стиля заполнения
	// клетки (из Minimap.prototype.fillStyles)
	if (this.cells[x][y] == this.cellTypes[fillStyle]) {
		return;
	}
	this.ctx.fillStyle = this.fillStyles[fillStyle];
	var viewIndent = GameField.getViewIndentation(x, y, this.scale);
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
/**
 * Changes width and height of Minimap (in cells, not in pixels).
 * @param {Number} width
 * @param {Number} height
 */
Minimap.prototype.changeDimensions = function(width, height) {
	this.w = width;
	this.h = height;
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
