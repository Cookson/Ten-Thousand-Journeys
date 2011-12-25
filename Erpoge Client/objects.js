function Cell(x, y) {
	// Ячейка массива клеток, содержащего модели объектов, предметов и стен
	// Свойства ячейки области
	this.wall;
	this.object;
	this.ceiling;
	this.items = new ItemMap();
	this.floor;
	this.character;
	this.path;
	this.x = x;
	this.y = y;
	this.visible = false;
	this.passability = Terrain.PASS_FREE;
	if (onGlobalMap) {
		this.worldPlayers = [];
	}
	// Свойства ячейки мира
	this.size = null;
	this.objects = [];
	this.difficulty = null;
	this.river = null;

	this.forest;
	this.show = function() {
		if (!onGlobalMap) {
			this.floor.show();
		}
		if (this.object!=null) {
			this.object.show();
		}
		if (this.forest!=null) {
			this.forest.show();
		}
		if (this.character) {
			this.character.showModel();
		}
		if (this.path!=null) {
			this.path.show();
		}
		if (this.soundSource) {
			this.soundSource.show();
		}
		if (this.ceiling) {
			this.ceiling.show();
		}
		this.showItems();
		this.visible = true;
	};
	this.hide = function() {
		if (!onGlobalMap) {
			this.floor.hide();
		}
		if (this.object!=null) {
			this.object.hide();
		}
		if (this.forest!=null) {
			this.forest.hide();
		}
		if (this.path!=null) {
			this.path.hide();
		}
		if (this.character) {
			this.character.hideModel();
		}
		if (this.soundSource) {
			this.soundSource.hide();
		}
		if (this.ceiling) {
			this.ceiling.hide();
		}
		this.hideItems();
		this.visible = false;
	};
	this.shade = function() {
		this.floor.shade();
		if (this.object!=null) {
			this.object.shade();
		}
		if (this.forest!=null) {
			this.forest.shade();
		}
		if (this.path!=null) {
			this.path.shade();
		}
		if (this.character) {
			this.character.hideModel();
		}
		if (this.soundSource) {
			this.soundSource.hide();
		}
		if (this.ceiling) {
			this.ceiling.shade();
		}
		this.shadeItems();
	};
	this.unshade = function() {
		this.floor.unshade();
		if (this.object!=null) {
			this.object.unshade();
		}
		if (this.forest!=null) {
			this.forest.unshade();
		}
		if (this.path!=null) {
			this.path.unshade();
		}
		if (this.character) {
			this.character.showModel();
		}
		this.unshadeItems();
		if (this.soundSource) {
			this.soundSource.show();
		}
		if (this.ceiling) {
			this.ceiling.unshade();
		}
	};
	this.showItems = function() {
		// Показать появившиеся предметы
		var values = this.items.getValues();
		for (var i in values) {
			values[i].itemImage.show();
		}
	};
	this.hideItems = function() {
		// Показать появившиеся предметы
		var values = this.items.getValues();
		for (var i in values) {
			values[i].itemImage.hide();
		}
	};
	this.unshadeItems = function() {
		var values = this.items.getValues();
		for (var i in values) {
			values[i].itemImage.unshade();
		}
	};
	this.shadeItems = function() {
		var values = this.items.getValues();
		for (var i in values) {
			values[i].itemImage.shade();
		}
	};
	this.hasItem = function(typeId, param) {
		if (isUnique(typeId)) {
			return this.items.hasUnique(param);
		} else {
			if (this.items.hasPile(typeId)) {
				return this.items.getPile(typeId).amount >= param;
			}
		}
		return false;
	};
	this.addItem = function(item) {
		if (item.isUnique || !this.items.hasPile(item.typeId, item.amount)) {
			(this.items.addItem(item).itemImage = new CellItemImage(this, item)).show();
		} else {
			this.items.addItem(item);
		}
	};
	this.addItemWithoutShowing = function(item) {
		this.items.addItem(item).itemImage = new CellItemImage(this, item);
	};
	this.removeItem = function(typeId, param) {
		var item = this.items.getItem(typeId, param);
		this.items.remove(typeId, param);
		if (!this.items.hasItem(item)) {
			item.itemImage.remove();
		}
	};
}
function CellItemImage(cell, item) {
	this.item = item;
	this.cell = Terrain.cells[cell.x][cell.y];
	this.image = null;
	this.show = function() {
		var viewIndent = Terrain.getViewIndentation(this.cell.x,this.cell.y,32);
		this.image = document.createElement("img");
		this.image.style.position = "absolute";
		this.image.src = "./images/items/"+item.typeId+".png";
		this.image.style.left = viewIndent.left + "px";
		this.image.style.top = viewIndent.top + "px";
		// У всех предметов на полу z-index:2
		this.image.style.zIndex = 2;
		gameField.appendChild(this.image);
	};
	this.hide = function() {
		this.image.parentNode.removeChild(this.image);
		this.image = null;
	};
	this.shade = function() {
		this.image.style.opacity = "0.5";
	};
	this.unshade = function() {
		this.image.style.opacity = "1";
	};
	this.remove = function() {
		if (this.image !== null) {
			this.hide();
		}
	};
}
function Wall(x, y, type) {
	this.type = type;
	this.x = x;
	this.y = y;
	this.image = null;
	this.doorSides = []; // Когда стена граничит с дверью, создаются
	// дополнительные изображения для сторон стены, с
	// которых находятся двери
	Terrain.cells[x][y].passability = objectProperties[type][2];
	this.show = function() {
		var postfix = "";
		var x = this.x;
		var y = this.y;
		if (y>0&&Terrain.cells[x][y-1].object&&isDoor(Terrain.cells[x][y-1].object.type)
				&&( !Terrain.cells[x][y-2].floor||Terrain.cells[x][y-2].wall)) {
			this.newDoorSide(0);
		}
		if (x<width-1&&Terrain.cells[x+1][y].object
				&&isDoor(Terrain.cells[x+1][y].object.type)
				&&( !Terrain.cells[x+2][y].floor||Terrain.cells[x+2][y].wall)) {
			this.newDoorSide(1);
		}
		if (y<height-1&&Terrain.cells[x][y+1].object
				&&isDoor(Terrain.cells[x][y+1].object.type)
				&&( !Terrain.cells[x][y+2].floor||Terrain.cells[x][y+2].wall)) {
			this.newDoorSide(2);
		}
		if (x>0&&Terrain.cells[x-1][y].object&&isDoor(Terrain.cells[x-1][y].object.type)
				&&( !Terrain.cells[x-2][y].floor||Terrain.cells[x-2][y].wall)) {
			this.newDoorSide(3);
		}
		
		var postfixN = (y>0&&(Terrain.cells[x][y-1].wall && 
				(!wallConnectsOnlyWithItself(Terrain.cells[x][y-1].wall.type) &&
				!wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) || 
					wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) && 
					Terrain.cells[x][y-1].wall.type==Terrain.cells[x][y].wall.type) ||
				this.doorSides[0])
				// !player.canSee(x,y-1)
				) ? "1" : "0";
		
		var postfixE = (x<width-1&&(Terrain.cells[x+1][y].wall
				&& (!wallConnectsOnlyWithItself(Terrain.cells[x+1][y].wall.type) &&
				!wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type)	|| 
					wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) && 
					Terrain.cells[x+1][y].wall.type==Terrain.cells[x][y].wall.type) ||
				this.doorSides[1]) 
				//!player.canSee(x+1,y)
				) ? "1" : "0";
		var postfixS = (y<height-1&&(Terrain.cells[x][y+1].wall
				&& (!wallConnectsOnlyWithItself(Terrain.cells[x][y+1].wall.type) &&
				!wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) || 
					wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) && 
					Terrain.cells[x][y+1].wall.type == Terrain.cells[x][y].wall.type)||
				this.doorSides[2])
				// !player.canSee(x,y+1)
				) ? "1" : "0";
		var postfixW = (x>0&&(Terrain.cells[x-1][y].wall
				&& (!wallConnectsOnlyWithItself(Terrain.cells[x-1][y].wall.type) &&
					!wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) || 
					wallConnectsOnlyWithItself(Terrain.cells[x][y].wall.type) && 
					Terrain.cells[x-1][y].wall.type == Terrain.cells[x][y].wall.type) ||
				this.doorSides[3])
				// !player.canSee(x-1,y)
				) ? "1" : "0";
		var postfix;
		if (Terrain.cameraOrientation == Terrain.SIDE_N) {
			postfix = postfixN+postfixE+postfixS+postfixW;
		} else if (Terrain.cameraOrientation == Terrain.SIDE_E) {
			postfix = postfixW+postfixN+postfixE+postfixS;
		} else if (Terrain.cameraOrientation == Terrain.SIDE_S) {
			postfix = postfixS+postfixW+postfixN+postfixE;
		} else if (Terrain.cameraOrientation == Terrain.SIDE_W) {
			postfix = postfixE+postfixS+postfixW+postfixN;
		}
		var viewIndent = Terrain.getViewIndentation(x,y,1);
		this.image = document.createElement('img');
		this.image.src = "./images/walls/"+wallNames[this.type]+"_"+postfix
				+".png";
		this.image.style.position = "absolute";
		this.image.style.zIndex = viewIndent.top*2+1;
		gameField.appendChild(this.image);
		this.image.style.top = viewIndent.top*32
				+( -parseInt(objectProperties[this.type][1])+32)+"px";
		this.image.style.left = viewIndent.left*32
				+(( -parseInt(objectProperties[this.type][0])+32)/2)+"px";

	};
	this.newDoorSide = function(side) {
		// Создать изображение стороны стены, обёрнутое во wrap
		// side - сторона стены (0-3 по часовой стрелке)
		var initialSide = side;
		if (Terrain.cameraOrientation == Terrain.SIDE_E) {
			side = (side+1)%4;
		} else if (Terrain.cameraOrientation == Terrain.SIDE_S) {
			side = (side+2)%4;
		} else if (Terrain.cameraOrientation == Terrain.SIDE_W) {
			side = (side+3)%4;
		}
		var viewIndent = Terrain.getViewIndentation(this.x,this.y,1);
		var img = document.createElement('img');
		img.style.position = "absolute";
		img.src = "./images/walls/"+wallNames[this.type]+"_d"+side+".png";
		gameField.appendChild(img);
		img.style.top = viewIndent.top*32-parseInt(objectProperties[this.type][1])+32
				+"px";
		img.style.left = viewIndent.left*32
				+( -parseInt(objectProperties[this.type][0])+32)/2+"px";
		img.style.zIndex = viewIndent.top*2+2;
		this.doorSides[initialSide] = img;
	};
	this.hide = function() {
		this.image.parentNode.removeChild(this.image);
		this.image = null;
		for ( var i = 0; i<4; i++ ) {
			if (this.doorSides[i] === undefined) {
				continue;
			}
			this.doorSides[i].parentNode.removeChild(this.doorSides[i]);
			delete this.doorSides[i];
		}
	};
	this.remove = function() {
		this.hide();
		Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
		delete Terrain.cells[this.x][this.y].wall;
		delete Terrain.cells[this.x][this.y].object;
		var sides = [0, -1, 1, 0, 0, 1, -1, 0];
		for ( var i = 0; i<sides.length/2; i++ ) {
			var nx = this.x+sides[i*2];
			var ny = this.y+sides[i*2+1];
			if (Terrain.cells[nx][ny].wall&&Terrain.cells[nx][ny].visible) {
				Terrain.cells[nx][ny].wall.hide();
				Terrain.cells[nx][ny].wall.show();
			}
		}
	};
	this.shade = function() {
		this.image.style.opacity = "0.5";
		for ( var i = 0; i<this.doorSides.length; i++ ) {
			if ( !this.doorSides[i]) {
				continue;
			}
			this.doorSides[i].style.opacity = "0.5";
		}
	};
	this.cursorShade = function() {
		this.image.style.opacity = "0.2";
		for ( var i = 0; i<this.doorSides.length; i++ ) {
			if ( !this.doorSides[i]) {
				continue;
			}
			this.doorSides[i].style.opacity = "0.2";
		}
	};
	this.unshade = function() {
		this.image.style.opacity = "1";
		for ( var i = 0; i<this.doorSides.length; i++ ) {
			if ( !this.doorSides[i]) {
				continue;
			}
			this.doorSides[i].style.opacity = "1";
		}
	};
	Terrain.cells[x][y].wall = this;
	Terrain.cells[x][y].object = this;
}
function Floor(x, y, type) {
	this.type = type;
	this.x = x;
	this.y = y;
	this.isCanvas = false;
	this.show = function(noForceNeighbourReshow) {
		// Отобразить изображение тайла
		// Аргумент noForceNeighbourReshow ставится в true, когда метод show()
		// вызван из метода show() объекта,
		// изображение которого рисуется на канве. Аргумент используется во
		// избежание рекурсии.
		if ((visToNum(this.x-1, this.y)+visToNum(this.x+1, this.y))
				*(visToNum(this.x, this.y-1)+visToNum(this.x, this.y+1))<=1
				&&(wallToNum(this.x-1, this.y)+wallToNum(this.x+1, this.y))
						*(wallToNum(this.x, this.y-1)+wallToNum(this.x,
								this.y+1))==1
				&&seenToNum(this.x-1, this.y)+seenToNum(this.x, this.y-1)
						+seenToNum(this.x+1, this.y)
						+seenToNum(this.x, this.y+1)==2) {
			// Исключение для "уголков" за угловыми стенами
			return false;
		} else {

		}
		var viewIndent = Terrain.getViewIndentation(this.x,this.y,32);
		if (x+1<width&&Terrain.cells[x+1][y].floor.type!=this.type||x-1>0
				&&Terrain.cells[x-1][y].floor.type!=this.type||y+1<height
				&&Terrain.cells[x][y+1].floor.type!=this.type||y-1>0
				&&Terrain.cells[x][y-1].floor.type!=this.type) {
			// Если этот тайл граничит с тайлом другого типа

			// Получаем типы соседних тайлов или тип этого тайла, если такого
			// соседнего тайла нет (если этот тайл на границе)
			var up = (y==0) ? this.type
					: (Terrain.cells[x][y-1].floor==null||Terrain.cells[x][y-1].wall) ? this.type
							: Terrain.cells[x][y-1].floor.type;
			var right = (x==width-1) ? this.type
					: (Terrain.cells[x+1][y].floor==null||Terrain.cells[x+1][y].wall) ? this.type
							: Terrain.cells[x+1][y].floor.type;
			var down = (y==height-1) ? this.type
					: (Terrain.cells[x][y+1].floor==null||Terrain.cells[x][y+1].wall) ? this.type
							: Terrain.cells[x][y+1].floor.type;
			var left = (x==0) ? this.type
					: (Terrain.cells[x-1][y].floor==null||Terrain.cells[x-1][y].wall) ? this.type
							: Terrain.cells[x-1][y].floor.type;
			
			if (Terrain.cameraOrientation == Terrain.SIDE_E) {
				var leftBuf = left;
				left = down;
				down = right;
				right = up;
				up = leftBuf;
			} else if (Terrain.cameraOrientation == Terrain.SIDE_S) {
				var upBuf = up;
				var leftBuf = left;
				up = down;
				left = right;
				right = leftBuf;
				down = upBuf;
			} else if (Terrain.cameraOrientation == Terrain.SIDE_W) {
				var upBuf = up;
				up = right;
				right = down;
				down = left;
				left = upBuf;
			}
			var tileType= "t"+this.type+","+up+","+right+","+down+","+left;
			
			
			if (floorImages[tileType]!==undefined) {
				// Если изображение такого тайла уже использовалось, и поэтому
				// сгенерировано
				// Отрисовка основного изображения
				var ctx = floorCanvas.getContext("2d");
				if ( +localStorage.getItem(2)) {
					var imageData = floorImages[tileType];
					// Отрисовка сетки
					for ( var i = 0; i<32; i++ ) {
						var pix = getPixel(imageData, i, 0);
						pix[3] = 200;
						setPixel(imageData, i, 0, pix);
					}
					for ( var j = 0; j<32; j++ ) {
						var pix = getPixel(imageData, 0, j);
						pix[3] = 200;
						setPixel(imageData, 0, j, pix);
					}
					ctx.putImageData(imageData, viewIndent.left, viewIndent.top);
				} else {
					ctx.putImageData(floorImages[tileType], viewIndent.left,
							viewIndent.top);
				}
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
			} else {
				// Если изображение такого тайла ещё не сгенерировано, то
				// создать его
				// Отрисовка основного изображения
				var ctx = floorCanvas.getContext("2d");
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
			var ctx = floorCanvas.getContext("2d");
			ctx.drawImage(tiles[this.type][getNumFromSeed(x, y,
					NUM_OF_TILES[floorNames[this.type]])], viewIndent.left, viewIndent.top);
			if ( +localStorage.getItem(2)) {
				var imageData = ctx.getImageData(viewIndent.left, viewIndent.top, 32, 32);
				// Отрисовка сетки
				for ( var i = 0; i<32; i++ ) {
					var pix = getPixel(imageData, i, 0);
					pix[3] = 200;
					setPixel(imageData, i, 0, pix);
				}
				for ( var j = 0; j<32; j++ ) {
					var pix = getPixel(imageData, 0, j);
					pix[3] = 200;
					setPixel(imageData, 0, j, pix);
				}
				ctx.putImageData(imageData, viewIndent.left, viewIndent.top);
			}
		}
		if ((visToNum(this.x-1, this.y)+visToNum(this.x+1, this.y))
				*(visToNum(this.x, this.y-1)+visToNum(this.x, this.y+1))==1
				&&(wallToNum(this.x-1, this.y)+wallToNum(this.x+1, this.y))
						*(wallToNum(this.x, this.y-1)+wallToNum(this.x,
								this.y+1))==1
				&&(seenToNum(this.x-1, this.y)+seenToNum(this.x, this.y-1)
						+seenToNum(this.x+1, this.y)
						+seenToNum(this.x, this.y+1)==4||seenToNum(this.x-1,
						this.y)
						+seenToNum(this.x, this.y-1)
						+seenToNum(this.x+1, this.y)
						+seenToNum(this.x, this.y+1)==2)) {
			// Случай затенения уголков у стен
			this.shade();
		}
	};
	this.hide = function() {
		var ctx = floorCanvas.getContext("2d");
		ctx.clearRect(this.x*32, this.y*32, 32, 32);
		// if (this.image!=null) {
		// this.image.parentNode.removeChild(this.image);
		// this.image=null;
		// }
	};
	this.shade = function() {
		var ctx = floorCanvas.getContext("2d");
		var imgData = ctx.getImageData(this.x*32, this.y*32, 32, 32);
		var len = imgData.width*imgData.height; // Количество пикселей в
		// выделенной зоне
		// (количество элементов в массиве - в 4 раза больше (RGBA))
		for ( var i = 0; i<len; i++ ) {
			imgData.data[i*4+3] = 128;
		}
		ctx.putImageData(imgData, this.x*32, this.y*32);
		if ( +localStorage.getItem(2)) {
			var imageData = ctx.getImageData(this.x*32, this.y*32, 32, 32);
			// Отрисовка сетки
			for ( var i = 0; i<32; i++ ) {
				var pix = getPixel(imageData, i, 0);
				pix[3] = 140;
				setPixel(imageData, i, 0, pix);
			}
			for ( var j = 0; j<32; j++ ) {
				var pix = getPixel(imageData, 0, j);
				pix[3] = 140;
				setPixel(imageData, 0, j, pix);
			}
			ctx.putImageData(imageData, this.x*32, this.y*32);
		}
	};
	this.unshade = function() {
		var ctx = floorCanvas.getContext("2d");
		var imgData = ctx.getImageData(this.x*32, this.y*32, 32, 32);
		var len = imgData.width*imgData.height; // Количество пикселей в
		// выделенной зоне
		// (количество элементов в массиве - в 4 раза больше (RGBA))
		for ( var i = 0; i<len; i++ ) {
			imgData.data[i*4+3] = 255;
		}
		ctx.putImageData(imgData, this.x*32, this.y*32);
	};
}
function GameObject(x, y, type) {
	this.type = type;
	this.x = x;
	this.y = y;
	this.image = null;
	this.mod = -1;
	Terrain.cells[this.x][this.y].passability = objectProperties[type][2];
	this.show = function() {
		if (this.image==null) {
			this.image = document.createElement("img");
			this.y *= 1; /* */// Здесь this.y оказывается сторокой,
			var viewIndent = Terrain.getViewIndentation(this.x, this.y, 1);
			// разобраться
			var vertical = isDoor(this.type)
					&&(1+this.y<width&&Terrain.cells[this.x][1+this.y].wall
							&&this.y-1>0&&Terrain.cells[this.x][this.y-1].wall);
			if (isDoor(this.type) && (Terrain.cameraOrientation == Terrain.SIDE_E || Terrain.cameraOrientation == Terrain.SIDE_W)) {
				vertical = !vertical;
			}
			this.image.style.position = "absolute";
			this.image.style.top = +"px";
			this.image.style.left = +"px";
			this.image.setAttribute("src", "./images/objects/"+this.type
					+(vertical ? "_v" : "")+".png");
			this.image.style.top = viewIndent.top*32
					+( -parseInt(objectProperties[this.type][1])+32-(vertical ? 10
							: 0))+"px";
			this.image.style.left = viewIndent.left*32
					+( -parseInt(objectProperties[this.type][0])+32)/2+"px";
			this.image.style.zIndex = viewIndent.top*2+1;
			gameField.appendChild(this.image);
			// this.image.getElementByTagName("img");
		}
	};
	this.hide = function() {
		if (this.image!=null) {
			this.image.parentNode.removeChild(this.image);
			this.image = null;
		}
	};
	this.remove = function() {
		this.hide();
		Terrain.cells[this.x][this.y].object = null;
		Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
	};
	this.shade = function() {
		this.image.style.opacity = "0.5";
	};
	this.cursorShade = function() {
		this.image.style.opacity = "0.2";
	};
	this.unshade = function() {
		this.image.style.opacity = "1";
	};
	Terrain.cells[x][y].object = this;
}
function Forest(x, y, type) {
	this.type = type;
	this.x = x;
	this.y = y;
	this.image = null;
	this.show = function() {
		var postfix = "";
		var x = this.x;
		var y = this.y;
		if (Terrain.cells[x][y].path) {
			// Особый спрайт леса выбирается в том случае, если на той же клетке
			// есть река
			postfix += (y>0&&Terrain.cells[x][y-1].path) ? "1" : "0";
			postfix += (x<width-1&&Terrain.cells[x+1][y].path) ? "1" : "0";
			postfix += (y<height-1&&Terrain.cells[x][y+1].path) ? "1" : "0";
			postfix += (x>0&&Terrain.cells[x-1][y].path) ? "1" : "0";
		} else {
			// Иначе используется обычный спрайт
			postfix = "0000";
		}
		this.image = document.createElement('div');
		this.image.className = "wrap";
		var img = document.createElement('img');
		img.src = "./images/walls/"+wallNames[this.type]+"_"+postfix+".png";
		this.image.appendChild(img);
		this.image.style.top = y*32+"px";
		this.image.style.left = x*32+"px";
		this.image.style.zIndex = y+2;
		gameField.appendChild(this.image);
	};
	this.hide = function() {
		if (this.image!=null) {
			this.image.parentNode.removeChild(this.image);
			this.image = null;
		}
	};
	this.remove = function() {
		this.hide();
		Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
		Terrain.cells[this.x][this.y].wall = null;
	};
	this.shade = function() {
		this.image.getElementsByTagName("img")[0].style.opacity = "0.5";
		// this.image.getElementsByTagName("img")[0].style.display="none";
	};
	this.unshade = function() {
		this.image.getElementsByTagName("img")[0].style.opacity = "1";
		// this.image.getElementsByTagName("img")[0].style.display="inline-block";
	};
}
function Path(x, y, type) {
	// Пусть - лес или река
	this.type = type;
	this.x = x;
	this.y = y;
	this.image = null;
	this.show = function() {
		var postfix = "";
		var x = this.x;
		var y = this.y;
		postfix += (y>0&&Terrain.cells[x][y-1].path&&Terrain.cells[x][y-1].path.type==this.type) ? "1"
				: "0";
		postfix += (x<width-1&&Terrain.cells[x+1][y].path&&Terrain.cells[x+1][y].path.type==this.type) ? "1"
				: "0";
		postfix += (y<height-1&&Terrain.cells[x][y+1].path&&Terrain.cells[x][y+1].path.type==this.type) ? "1"
				: "0";
		postfix += (x>0&&Terrain.cells[x-1][y].path&&Terrain.cells[x-1][y].path.type==this.type) ? "1"
				: "0";
		this.image = document.createElement('div');
		this.image.className = "wrap";
		var img = document.createElement('img');
		img.src = "./images/walls/"+wallNames[this.type]+"_"+postfix+".png";
		this.image.appendChild(img);
		this.image.style.top = y*32+"px";
		this.image.style.left = x*32+"px";
		this.image.style.zIndex = 2;
		gameField.appendChild(this.image);
	};
	this.hide = function() {
		if (this.image!=null) {
			this.image.parentNode.removeChild(this.image);
			this.image = null;
		}
	};
	this.remove = function() {
		this.hide();
		Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
		Terrain.cells[this.x][this.y].wall = null;
	};
	this.shade = function() {
		this.image.getElementsByTagName("img")[0].style.opacity = "0.5";
		// this.image.getElementsByTagName("img")[0].style.display="none";
	};
	this.unshade = function() {
		this.image.getElementsByTagName("img")[0].style.opacity = "1";
		// this.image.getElementsByTagName("img")[0].style.display="inline-block";
	};
}
function WorldPlayer(x, y, character) {
	this.image = null;
	this.x = x;
	this.y = y;
	this.name = name;
	this.characterId = character.characterId;
	this.race = character.race;
	this.visible = false;
	this.character = character;
	this.doll = new Doll(character);
	character.doll = this.doll;
	this.doll.draw();
	// Индекс объекта в массиве с игроками в его ячейке
	this.worldPlayersPos = Terrain.cells[x][y].worldPlayers.length;
	this.image = null;
	this.hide = function() {
		if (this.visible) {
			this.image.style.visibility = "hidden";
			this.visible = false;
		}

	};
	this.show = function() {
		var cell = Terrain.cells[this.x][this.y];
		if (cell.worldPlayers.length==2) {
			// Отобразить группу (если этот игрок - второй присоединяемый к
			// группе).
			// Когда на клетке появляется третий и более игрок, изображение
			// группы уже есть (после второго игрока)
			this.hide();
			cell.groupImage = document.createElement("div");
			cell.groupImage.className = "wrap";
			var nImg = document.createElement("img");
			nImg.style.position = "relative";
			nImg.setAttribute("src", "./images/intf/party.png");
			cell.groupImage.appendChild(nImg);
			cell.groupImage.style.top = (this.y*32)+"px";
			cell.groupImage.style.left = (this.x*32)+"px";
			cell.groupImage.style.zIndex = this.y*2+3;
			gameField.appendChild(cell.groupImage);
			cell.groupImage.getElementsByTagName("img");
			// Спрятать только первого персонажа в клетке:
			// этот (второй) и так не отобразится,
			// а для новых отображение вызываться не будет.
			cell.worldPlayers[0].hide();
		} else if (cell.worldPlayers.length==1) {
			// Отобразить одного игрока
			// Если этот игрок - единственный в группе
			this.image = document.createElement("div");
			this.image.className = "wrap";
			this.image.appendChild(document.createElement("img"));
			gameField.appendChild(this.image);
			this.image.style.top = (this.y*32)+"px";
			this.image.style.left = (this.x*32)+"px";
			this.image.style.zIndex = this.y*2+3;
			this.image.style.visibility = "visible";
			this.image.appendChild(this.doll.DOMNode);
			this.visible = true;
		}
	};
	this.move = function(x, y) {
		if (Terrain.cells[this.x][this.y].worldPlayers.length>1) {
			// Изменить свойство wolrdPlayersPos у всех игроков, имеющих больший
			// индекс в массиве игроков
			for ( var i = this.worldPlayersPos+1; i<Terrain.cells[this.x][this.y].worldPlayers.length; i++ ) {
				Terrain.cells[this.x][this.y].worldPlayers[i].worldPlayersPos-- ;
			}

		}
		Terrain.cells[this.x][this.y].worldPlayers.splice(this.worldPlayersPos, 1);
		this.redrawCellGroup(); // Перерисовать старую клетку
		this.image.style.top = (y*32)+"px";
		this.image.style.left = (x*32)+"px";
		this.x = x;
		this.y = y;
		Terrain.cells[this.x][this.y].worldPlayers.push(this);
		this.worldPlayersPos = Terrain.cells[this.x][this.y].worldPlayers.length-1;
		this.redrawCellGroup(); // Перерировать новую клетку
	};
	this.redrawCellGroup = function() {
		// Перерисовать группу в клетке после того, как один из игроков ушёл из
		// клетки или исчез.
		// Рассматриваются три случая:
		// 1) когда в клетке этот и ещё один игрок;
		// 2) когда этот игрок - единственный в клетке;
		// 3) все остальные случаи
		var cell = Terrain.cells[this.x][this.y];
		if (cell.worldPlayers.length>2&&this.worldPlayersPos<2) {
			// Если этот игрок - второй в массиве, и в массиве больше двух
			// игроков
			cell.worldPlayers[2].image = cell.worldPlayers[1].image;
			cell.worldPlayers[1].image = null;
		}
		if (cell.worldPlayers.length==1) {
			// 1)
			// Удалить изображение группы
			if (cell.groupImage) {
				cell.groupImage.parentNode.removeChild(cell.groupImage);
				delete cell.groupImage;
			}
			// Отобразить изображение оставшегося игрока
			cell.worldPlayers[0].show();
		} else if (cell.worldPlayers.length==2) {
			// 2)
			// Иначе никаких изображений не удаляется - только объект
			// Удаление
			Terrain.cells[this.x][this.y].worldPlayers[1].show();
		}
	};
	this.remove = function() {
		// Удаление игрока из клетки на глобальной карте.
		Terrain.cells[this.x][this.y].worldPlayers.splice(this.worldPlayersPos, 1);
		this.image.parentNode.removeChild(this.image);

		// Изменить свойство wolrdPlayersPos у всех игроков, имеющих больший
		// индекс в массиве игроков
		for ( var i = this.worldPlayersPos; i<Terrain.cells[this.x][this.y].worldPlayers.length; i++ ) {
			Terrain.cells[this.x][this.y].worldPlayers[i].worldPlayersPos-- ;
		}
		this.redrawCellGroup();
		delete worldPlayers[this.characterId];
	};
	Terrain.cells[x][y].worldPlayers.push(this);
	worldPlayers[this.characterId] = this;
	if (Terrain.cells[x][y].worldPlayers.length<3) {
		// Вызывать отображние только для первого и второго (отображние группы)
		// игроков в клетке
		this.show();
	}
}
function WorldObject(x, y, type) {
	this.type = type;
	this.x = x;
	this.y = y;
	this.image = null;
	Terrain.cells[this.x][this.y].passability = worldObjectProperties[type][2];
	this.show = function() {
		if (this.image==null) {
			this.image = document.createElement("img");
			this.y *= 1; /* */// Здесь this.y оказывается сторокой,
			// разобраться
			var vertical = isDoor(this.type)
					&&(Terrain.cells[this.x][1+this.y].wall||Terrain.cells[this.x][this.y-1].wall);
			this.image.style.position = "absolute";
			this.image.style.top = +"px";
			this.image.style.left = +"px";
			this.image.setAttribute("src", "./images/worldobjects/"+this.type
					+(vertical ? "_v" : "")+".png");
			this.image.style.top = this.y
					*32
					+( -parseInt(worldObjectProperties[this.type][1])+32-(vertical ? 10
							: 0))+"px";
			this.image.style.left = this.x*32
					+( -parseInt(worldObjectProperties[this.type][0])+32)/2
					+"px";
			this.image.style.zIndex = this.y*2;
			gameField.appendChild(this.image);
			// this.image.getElementByTagName("img");
		}
	};
	this.hide = function() {
		if (this.image!=null) {
			this.image.parentNode.removeChild(this.image);
			this.image = null;
		}
	};
	this.remove = function() {
		this.hide();
		Terrain.cells[this.x][this.y].object = null;
	};
	this.shade = function() {
		this.image.style.opacity = "0.5";
	};
	this.unshade = function() {
		this.image.style.opacity = "1";
	};
	Terrain.cells[x][y].object = this;
}
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
	this.show = function() {
		this.wrap.style.display = "inline-block";
	};
	this.hide = function() {
		this.wrap.style.display = "none";
	};
	this.remove = function() {
		gameField.removeChild(this.wrap);
	};
}
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
	this.show = function _() {
		this.image.style.display = "inline-block";
	};
	this.hide = function _() {
		this.image.style.display = "none";
	};
	this.shade = function _() {
		this.image.style.opacity = "0.5";
	};
	this.unshade = function _() {
		this.image.style.opacity = "1";
	};
	Terrain.cells[x][y].ceiling = this;
}
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
	this.hide = function _() {
		for (var i = 0; i<w; i++) {
			for (var j=0; j<h; j++) {
				Terrain.cells[this.x+i][this.y+j].ceiling.hide();
			}
		}
	};
	this.show = function _() {
		for (var i = 0; i<w; i++) {
			for (var j=0; j<h; j++) {
				Terrain.cells[this.x+i][this.y+j].ceiling.show();
			}
		}
	};
	Terrain.ceilings.push(this);	
}
// Специальные функции для работы с объектами
function visToNum(x, y) {
	// Возвращает 1, если клетка видима, и 0, если клетка невидима или находится
	// за пределами карты
	return +(x>=0&&x<width&&y>0&&y<height /*&& !!player.visibleCells[x][y]*/);
}
function seenToNum(x, y) {
	// Возвращает 1, если клетка уже была увидена, и 0, если клетка не была
	// увидена или находится за пределами карты
	return +(x>=0&&x<width&&y>0&&y<height&& ! !player.seenCells[x][y]);
}
function wallToNum(x, y) {
	// Возвращает 1, если на клетке есть стена, и 0, если на клетке нет стены
	// или нет такой клетки
	return +(x>=0&&x<width&&y>0&&y<height&&( ! !Terrain.cells[x][y].wall|| ! !(Terrain.cells[x][y].object&&isDoor(Terrain.cells[x][y].object.type))));
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
	return Terrain.cells[x]&&Terrain.cells[x][y]&&(Terrain.cells[x][y].wall||Terrain.cells[x][y].object);
}
// Функции типов объектов
function isDoor(type) {
	return (type>=40&&type<50);
}
function isOpenDoor(type) {
	return isDoor(type)&&(type%2==1);
}
function isWall(type) {
	return type<40&&type>0;
}
function isContainer(type) {
	return type>=60&&type<70;
}
function wallConnectsOnlyWithItself(wallId) {
	return wallId==6;
}
