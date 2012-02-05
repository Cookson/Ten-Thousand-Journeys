function Character(id, type, x, y, fraction, hp, maxHp) {
	if (id== -1) {
		return;
	}
	characters[id] = this;
	this.x = x;
	this.y = y;
	this.isClientPlayer = false;
	console.log(arguments);
	Terrain.getCell(x,y).character = this;
	Terrain.getCell(x,y).passability = Terrain.PASS_SEE;
	this.characterId = id;
	this.destX = this.x;
	this.destY = this.y;
	this.type = (type!=undefined) ? type : "player";
	if (this.type!="player") {
		this.name = characterTypes[type][0];
	}

	this.fraction = (fraction==undefined) ? (this.characterId==0) ? 1 : 0
			: fraction;

	this.aimcharacter = -1;
	this.cls = null;
	if (this.type!="player") {
		this.hp = hp;
		this.maxHp = maxHp;
	}

	this.VISION_RANGE = 8;
	this.visible;

	this.cellWrap = document.createElement("div");
	this.cellWrap.className = "cellWrap";
	this.cellWrap.setAttribute("id", "character"+this.characterId);

	var viewIndent = Terrain.getViewIndentation(this.x, this.y, 32);
	this.cellWrap.style.top = viewIndent.top+"px";
	this.cellWrap.style.left = viewIndent.left+"px";
	this.cellWrap.style.opacity = "0";
	this.cellWrap.style.zIndex = this.y*2+2;

	this.equipment = new Equipment();
	this.effects = {};

	// Spell
	this.spellId = -1;
	this.spellAimId = -1;
	this.spellX = -1;
	this.spellY = -1;
}
/* View */
Character.prototype.display = function() {
	// Initiates character view
	// This method is overriden in Player class
	gameField.appendChild(this.cellWrap);
	var nCharacterImage = document.createElement("img");
	nCharacterImage
			.setAttribute("src", "./images/characters/"+this.type+".png");
	if (this.typeincharacterSpriteSizes) {
		// If this character's sprite has irregular size (not 32х32)
		nCharacterImage.style.left = ((32-characterSpriteSizes[this.type][0])/2)
				+"px";
		nCharacterImage.style.top = ((16-characterSpriteSizes[this.type][1]))
				+"px";
	}
	this.cellWrap.appendChild(nCharacterImage);
	if (this.fraction == 1) {
		// Friend marker
		var nWrap = document.createElement("div");
		nWrap.className = "wrap";
		var nFriendMarker = document.createElement("img");
		nFriendMarker.className = "cellFriendMarker";
		nFriendMarker.src = "./images/intf/friendMarker.png";
		nWrap.appendChild(nFriendMarker);
		this.cellWrap.insertBefore(nWrap, this.cellWrap.children[0]);
	}
	if (Player.canSee(this.x, this.y)) {
		this.showModel();
	}
	// HP strip
	this.initHpBar();
};
Character.prototype.placeSprite = function(x, y) {
	// Разместить спрайт персонажа на конкретной клетке
	if (x===undefined) {
		x = this.x;
		y = this.y;
	}
	var viewIndent = Terrain.getViewIndentation(x, y, 1);
	this.cellWrap.style.left = viewIndent.left*32+"px";
	this.cellWrap.style.top = viewIndent.top*32+"px";
	this.cellWrap.style.zIndex = viewIndent.top*2+1;
};
Character.prototype.showModel = function() {
	this.cellWrap.style.opacity = "1";
	this.visible = true;
	for ( var i in this.effects) {
		this.effects[i].resume();
	}
};
Character.prototype.hideModel = function() {
	this.cellWrap.style.opacity = "0";
	this.visible = false;
	this.hideHpBar();
	for ( var i in this.effects) {
		this.effects[i].pause();
		this.effects[i].clear();
	}
};
Character.prototype.showAttackResult = function(res) {
	handleNextEvent();
	// this.graphicEffect("blood",function() {
	//		
	// });
};
Character.prototype.redrawDoll = function() {
	// Отобразить амуницию
	// prevEquipment - предыдущее значение this.equipment
	if (Player.canSee(this.x, this.y)) {
		this.doll.draw();
	}
};
Character.prototype.initHpBar = function() {
	// Creates HP bar element
	this.nHpBar = document.createElement("div");
	this.nHpBar.className = "cellHp";
	this.cellWrap.appendChild(this.nHpBar);
	if (this.typeincharacterSpriteSizes) {
		this.nHpBar.style.top = ((16-characterSpriteSizes[this.type][1]))+"px";
	}
	this.nHpBar.className = "cellHp";
	this.refreshHpBar();
};
Character.prototype.hideHpBar = function() {
	this.nHpBar.display = "none";
};
Character.prototype.showHpBar = function() {
	this.nHpBar.display = "block";
};
Character.prototype.refreshHpBar = function() {
	// Adjusts HP bar length and color.
	// if (this.hp==this.maxHp) {
	// // If HP is max then hide bar
	// this.hideHpBar();
	// return false;
	// } else {
	// Else change it's length and color
	var w = ((BARwidth/this.maxHp*this.hp>=BARwidth) ? BARwidth : Math
			.ceil(BARwidth/this.maxHp*this.hp));
	this.nHpBar.style.width = w+"px";
	this.nHpBar.style.borderRight = (BARwidth-w)+"px solid #000";
	// Colors of HP bar
	if (w<=BARwidth/4) {
		// Very low hp
		this.nHpBar.style.backgroundColor = "#ff2400";
	} else if (w<=BARwidth/2) {
		// Low hp
		this.nHpBar.style.backgroundColor = "#e6dc0d";
	} else {
		// Normal hp
		this.nHpBar.style.backgroundColor = "#34c924";
	}
	// }
};
Character.prototype.refreshMpBar = function() {
	document.getElementById("barsMpValue").innerHTML = this.mp+"/"+this.maxMp;
	document.getElementById("barsMpStrip").style.width = (106*this.mp/this.maxMp)
			+"px";
};
Character.prototype.showAttack = function(aimId, ranged) {
	handleNextEvent();
};
Character.prototype.showDamage = function(amount, type) {
	this.showAttackResult();
	this.hp -= amount;
	this.refreshHpBar();
};
Character.prototype.showSpeech = function(message) {
	var bg = document.createElement("div");
	var text = document.createElement("div");
	var wrap = document.createElement("div");
	var wrap2 = document.createElement("div");
	wrap.className = "wrap";
	wrap2.className = "wrap";
	bg.className = "speechBubbleBg";
	text.className = "speechBubbleText";
	text.innerText = message;

	wrap.style.zIndex = 9000;
	wrap2.style.zIndex = 9000;
	text.style.zIndex = 1;

	wrap2.appendChild(text);
	wrap.appendChild(wrap2);
	wrap.appendChild(bg);

	gameField.appendChild(wrap);
	bg.style.height = text.clientHeight-8+"px";
	bg.style.width = text.clientWidth-8+"px";
	wrap.style.top = (32*this.y-text.clientHeight-12)+"px";
	wrap.style.left = (32*this.x-text.clientWidth/2+16)+"px";
	wrap.onclick = handlers.speechBubble.click;
	wrap.onmouseover = handlers.speechBubble.mouseover;
	wrap.onmouseout = handlers.speechBubble.mouseout;
	wrap.setAttribute("isMouseOver", "0");
	wrap.setAttribute("time", new Date().getTime());
	setTimeout(function() {
		if (wrap.getAttribute("isMouseOver")=="0") {
			gameField.removeChild(wrap);
			return false;
		}
	}, 2000);
};
Character.prototype.showEffectStart = function(effectId) {
	this.effects[effectId] = new effectTypes[effectId](this.x, this.y, this.x,
			this.y, 100, 100, 100, 100);
};
Character.prototype.showEffectEnd = function(effectId) {
	this.effects[effectId].markForDestruction();
	delete this.effects[effectId];
};
/* Calculations */
Character.prototype.canSee = function(x, y, setVisibleCells, forceCompute) {
	/**
	 * Checks if the cell is on line of sight
	 * 
	 * setVisibleCells - sets this.visibleCells[x][y] to true if can see, to
	 * false otherwise. forceCompute - compute visibility even if it is a
	 * peaceful location.
	 */
	// return true;
	if (this.isNear(x, y)||this.x==x&&this.y==y||Terrain.isPeaceful
			&& !forceCompute) {
		// Если клетка рядом или персонаж на ней стоит - то её точно видно
		if (setVisibleCells) {
			this.visibleCells[x][y] = true;
		}
		return true;
	}
	if (Math.floor(this.distance(x, y))>this.VISION_RANGE) {
		return false;
	}
	// Алгоритм рассматривает несколько случаев взаимного расположения начальной
	// и целевой клетки,
	// поскольку в значительной части случаев расчёт можно упростить. Алгоритм
	// для общего случая рассматривается последним.
	if (x==this.x||y==this.y) {
		// Для случая, когда тангенс прямой (угловой коэффициент) равен
		// бесконечности или 0
		// (т.е. когда в цикле в else может быть деление на ноль т.к. абсцисы
		// или ординаты конца и начала равны)
		// В этом случае придётся сделать только одну проверку по линии (не
		// таким методом, как в else для прямых с tg!=0 и tg!=1)
		if (x==this.x) {
			// Для вертикальных линий
			var dy = Math.abs(y-this.y)/(y-this.y);
			for ( var i = this.y+dy; i!=y; i += dy) {
				if (Terrain.cells[x][i].passability==Terrain.PASS_BLOCKED) {
					return false;
				}
			}
		} else {
			// Для горизонтальных линий
			var dx = Math.abs(x-this.x)/(x-this.x);
			for ( var i = this.x+dx; i!=x; i += dx) {
				if (Terrain.cells[i][y].passability==Terrain.PASS_BLOCKED) {
					return false;
				}
			}
		}
		if (setVisibleCells) {
			this.visibleCells[x][y] = true;
		}
		return true;
	} else if (Math.abs(x-this.x)==1) {
		// Для случая, когда координаты конца и начала находятся на двух
		// соседних вертикальных линиях
		var yMin = Math.min(y, this.y);
		var yMax = Math.max(y, this.y);
		for ( var i = yMin+1; i<yMax; i++ ) {
			if (Terrain.cells[x][i].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==yMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y] = true;
				}
				return true;
			}
		}
		for ( var i = yMin+1; i<yMax; i++ ) {
			if (Terrain.cells[this.x][i].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==yMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y] = true;
				}
				return true;
			}
		}
		return false;
	} else if (Math.abs(y-this.y)==1) {
		// Тот же случай, что и предыдущий, но для горизонтальных линий
		var xMin = Math.min(x, this.x);
		var xMax = Math.max(x, this.x);
		for ( var i = xMin+1; i<xMax; i++ ) {
			if (Terrain.cells[i][y].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==xMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y] = true;
				}
				return true;
			}
		}
		for ( var i = xMin+1; i<xMax; i++ ) {
			if (Terrain.cells[i][this.y].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==xMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y] = true;
				}
				return true;
			}
		}
		return false;
	} else if (Math.abs(x-this.x)==Math.abs(y-this.y)) {
		// Случай, когда линия образует с осями угол 45 градусов (abs(tg)==1)
		var dMax = Math.abs(x-this.x);
		var dx = x>this.x ? 1 : -1;
		var dy = y>this.y ? 1 : -1;
		var cx = this.x;
		var cy = this.y;
		for ( var i = 1; i<dMax; i++ ) {
			cx += dx;
			cy += dy;
			if (Terrain.cells[cx][cy].passability==Terrain.PASS_BLOCKED) {
				return false;
			}

		}
		if (setVisibleCells) {
			this.visibleCells[x][y] = true;
		}
		return true;
	} else {
		// Общий случай
		var start = [ [], []];
		var end = [];
		// x и y концов соответствуют x и y центра клетки или её ближайшего угла
		// (значения перебираются в цикле по k каждое с каждым)
		end[0] = (x>this.x) ? x-0.5 : x+0.5;
		end[1] = (y>this.y) ? y-0.5 : y+0.5;
		end[2] = x;
		end[3] = y;
		start[0][0] = (x>this.x) ? this.x+0.5 : this.x-0.5;
		start[0][1] = (y>this.y) ? this.y+0.5 : this.y-0.5;
		start[1][0] = (x>this.x) ? this.x+0.5 : this.x-0.5;
		// start[0][1]=this.y;
		// start[1][0]=this.x;
		start[1][1] = (y>this.y) ? this.y+0.5 : this.y-0.5;
		var rays = this.rays(this.x, this.y, x, y);
		jump: for ( var k = 0; k<3; k++ ) {
			var endNumX = (k==0||k==1) ? 0 : 2;
			var endNumY = (k==0||k==2) ? 1 : 3;
			for (j = 0; j<1; j++ ) {
				// Новый алгоритм расчёта видимости строится на том, есть ли
				// точки,
				// которые находятся ближе, чем на 0.5 клетки от прямой -
				// косвенный признак того, что прямая пересекает преграду.
				// Преграды в этом случае считаются окружностями с R=0.5
				// Это не мешает расчёту видимости на стыках клеток по
				// горизонтали.
				// В этом случае нужно сделать максимум шесть проверок (3 цикла
				// по k - точки конца - и два по j - точки начала)
				if (start[j][0]==this.x&&start[j][1]==this.y) {
					continue;
				}
				var xEnd = end[endNumX];
				var yEnd = end[endNumY];
				var xStart = start[j][0];
				var yStart = start[j][1];
				for ( var i in rays) {
					/* */// Здесь x|yPoint - глобальные переменные.
							// Пофиксить.
					xPoint = rays[i][0];
					yPoint = rays[i][1];
					if (Terrain.cells[xPoint][yPoint].passability==Terrain.PASS_BLOCKED) {
						// Проверяем каждую клетку
						if (xPoint==this.x&&yPoint==this.y||xPoint==x
								&&yPoint==y) {
							continue;
						}
						if (Math
								.abs(((yStart-yEnd)*xPoint+(xEnd-xStart)*yPoint+(xStart
										*yEnd-yStart*xEnd))
										/Math.sqrt(Math.abs((xEnd-xStart)
												*(xEnd-xStart)+(yEnd-yStart)
												*(yEnd-yStart))))<=0.5) {
							// Если расстояние до точки не больше 0.5, проверяем
							// следующую из 6 линий
							continue jump;
						}
					}
				}
				if (setVisibleCells) {
					this.visibleCells[x][y] = true;
				}
				return true;
			}
		}
		return false;
	}
};
Character.prototype.ray = function(startX, startY, endX, endY) {
	// Вспомогательная функция для this->canSee(). Возвращает клетки линии от
	// xStart:yStart до xEnd:yEnd
	if (startX==endX&&startY==endY) {
		return [startX, startY];
	}
	var x = [];
	var y = [];
	x[0] = startX;
	y[0] = startY;
	var L = Math.round(Math.max(Math.abs(endX-x[0]), Math.abs(endY-y[0])));
	var dX = (endX-x[0])/L;
	var dY = (endY-y[0])/L;
	for ( var i = 1; i<L; i++ ) {
		x[i] = x[i-1]+dX;
		y[i] = y[i-1]+dY;
	}
	x.push(endX);
	y.push(endY);
	var result = [];
	for (i = 0; i<=L; i++ ) {
		result.push( [Math.round(x[i]), Math.round(y[i])]);
	}
	return result;
};
Character.prototype.rays = function(startX, startY, endX, endY) {
	// Вспомогательная функция для this->canSee
	// Возвращает набор координат клеток, которые необходимо проверить для
	// проверки видимости
	return this.ray(startX, startY, endX, endY).concat(
			this.ray(startX, startY+(endY>startY ? 1 : -1), endX
					+(endX>startX ? -1 : 1), endY),
			this.ray(startX+(endX>startX ? 1 : -1), startY, endX, endY
					+(endY>startY ? -1 : 1)));
};
Character.prototype.getPathTable = function(ignorecharacters) {
	// Получает таблицу путей по волновому алгоритму
	// Отключено для возможности использования объектов
	// if
	// (Terrain.cells[this.destX][this.destY].passability==Terrain.PASS_BLOCKED)
	// {
	// this.destX=this.x;
	// this.destY=this.y;
	// }
	this.pathTable = blank2dArray();
	var destcharacterCoordX = (this.aimcharacter!= -1) ? this.aimcharacter.x
			: this.x;
	var destcharacterCoordY = (this.aimcharacter!= -1) ? this.aimcharacter.y
			: this.y;
	var isPathFound = false;
	var oldFront = [];
	var newFront = [];
	newFront[0] = {
		x : this.x,
		y : this.y
	}; // От какой клетки начинать отсчёт; было так: newFront[0]=[this.coord];
	for ( var i = 0; i<Terrain.height; i++ ) {
		this.pathTable[i] = [];
	}
	this.pathTable[this.x][this.y] = 0;
	var t = 0;
	do {
		oldFront = newFront;
		newFront = [];
		if (isPathFound===null) {
			isPathFound = true;
		}
		for ( var i = 0; i<oldFront.length; i++ ) {
			// Двигает фронт на восемь доступных сторон от каждой клетки
			var x = oldFront[i].x;
			var y = oldFront[i].y;
			var adjactentX = [x+1, x, x, x-1, x+1, x+1, x-1, x-1];
			var adjactentY = [y, y-1, y+1, y, y+1, y-1, y+1, y-1];
			for ( var j = 0; j<8; j++ ) {
				var thisNumX = adjactentX[j];
				var thisNumY = adjactentY[j];

				if (thisNumX<0||thisNumX>=Terrain.width||thisNumY<0
						||thisNumY>=Terrain.height
						||this.pathTable[thisNumX][thisNumY]!=undefined) {
					continue;
				}
				if (thisNumX==this.destX&&thisNumY==this.destY) {
					isPathFound = null;
				}
				if (Terrain.cells[thisNumX][thisNumY].passability!=Terrain.PASS_BLOCKED
						&&Terrain.cells[thisNumX][thisNumY].passability!=Terrain.PASS_SEE
						||Terrain.cells[thisNumX][thisNumY].object
						&&isDoor(Terrain.cells[thisNumX][thisNumY].object.type)/* *//*
																					 * &&
																					 * this.seenCells[thisNumX][thisNumY]!=undefined
																					 */) {
					this.pathTable[thisNumX][thisNumY] = t+1;
					newFront[newFront.length] = {
						x : thisNumX,
						y : thisNumY
					};
				}
			}
		}
		t++ ;
		if (t>900) {
			throw new Error("long get path table cycle");
		}
	} while (newFront.length>0&& !isPathFound&&t<900);
	return t;
};
Character.prototype.distance = function(x, y) {
	return Math.sqrt(Math.pow(this.x-x, 2)+Math.pow(this.y-y, 2));
};
Character.prototype.getVisibleCells = function() {
	// Получить видимые клетки
	if (Terrain.isPeaceful) {
		return;
	}
	this.prevVisibleCells = this.visibleCells;
	this.visibleCells = blank2dArray();
	this.visibleCells[this.x][this.y] = true;

	var minX = Math.max(this.x-this.VISION_RANGE, 0);
	var minY = Math.max(this.y-this.VISION_RANGE, 0);
	var maxX = Math.min(this.x+this.VISION_RANGE, Terrain.width-1);
	var maxY = Math.min(this.y+this.VISION_RANGE, Terrain.height-1);

	for ( var i = minX; i<=maxX; i++ ) {
		for ( var j = minY; j<=maxY; j++ ) {
			this.canSee(i, j, true);
		}
	}
};
Character.prototype.comeTo = function(x, y) {
	// Функция следования за character
	// Устанавливает номер клетки, в которую этот character должен идти, чтобы
	// прийти к aimcharacter
	if (this.isNear(x, y)) {
		// Стоять на месте, если цель на соседней клетке
		this.destX = this.x;
		this.destY = this.y;
		return false;
	}
	var dists = [x-1, y, x+1, y, x, y-1, x, y+1, x+1, y+1, x-1, y+1, x+1, y-1,
			x-1, y-1];
	var dist = 9000;
	var destX; // Конечное направление
	var destY; // Конечное направление
	var allPathsAreAvailable = false;
	var destXBuf = this.destX;
	var destYBuf = this.destY;
	for ( var i = 0; i<8&& !allPathsAreAvailable; i++ ) {
		this.destX = dists[i*2];
		this.destY = dists[i*2+1];
		if (Terrain.cells[this.destX][this.destY].passability!=Terrain.PASS_FREE) {
			continue;
		}
		var allPathsAreAvailable = true;
		// this.getPathTable();
		for ( var j = 0; j<8; j++ ) {
			if ( !this.pathTable[dists[i*2]][dists[i*2+1]]) {
				allPathsAreAvailable = false;
				break;
			}
		}
	}
	this.destX = destXBuf;
	this.destY = destYBuf;
	var atLeastOnePath = false; // Найден ли хотя бы один путь. Функция
								// возвращает значение этой переменной
	for ( var i = 0; i<8; i++ ) {
		var steps = this.pathTable[dists[i*2]][dists[i*2+1]];
		if (steps&&steps<dist&&steps>0
				&& !(dists[i*2]==this.x&&dists[i*2+1]==this.y)) {
			atLeastOnePath = true;
			dist = steps;
			destX = dists[i*2];
			destY = dists[i*2+1];
		}
	}
	if (atLeastOnePath) {
		this.destX = destX;
		this.destY = destY;
	} else {
		this.destX = this.x;
		this.destY = this.y;
	}
	return atLeastOnePath;
};
Character.prototype.getPath = function(destX, destY) {
	// Получить путь до клетки в виде массива координат (0 - первый шаг и т. д.)
	if (destX==undefined||destY==undefined) {
		destX = this.destX;
		destY = this.destY;
	}
	if (destX==this.x&&destY==this.y) {
		throw new Error("Gets path to its own x:y");
	}
	if (this.isNear(destX, destY)) {
		return [{
			x : destX,
			y : destY
		}];
	}

	var bufX = this.destX;
	var bufY = this.destY;
	// this.destX=destX;
	// this.destY=destY;
	var path = [];
	// Нахождение пути
	var currentNumX = destX;
	var currentNumY = destY;
	var x = currentNumX;
	var y = currentNumY;
	/* */var diff = [ -Terrain.width, 1, Terrain.width, -1];
	var t = 0;
	for ( var j = this.pathTable[currentNumX][currentNumY]; j>0; j = this.pathTable[currentNumX][currentNumY]) {
		// Счётчик: от кол-ва шагов до клетки dest до начальной клетки (шаг 1)
		path.push( {
			x : currentNumX,
			y : currentNumY
		});
		var diff = [ -Terrain.width, 1, Terrain.width, -1, Terrain.width+1,
				-Terrain.width+1, Terrain.width-1, -Terrain.width-1];
		var adjactentX = [x, x+1, x, x-1, x+1, x+1, x-1, x-1];
		var adjactentY = [y-1, y, y+1, y, y+1, y-1, y+1, y-1];
		for ( var i = 0; i<8; i++ ) {
			// Для каждой из доступных сторон (С, Ю, З, В)
			var thisNumX = adjactentX[i];
			if (thisNumX<0||thisNumX>=Terrain.width) {
				continue;
			}
			var thisNumY = adjactentY[i];
			if (thisNumY<0||thisNumY>=Terrain.height) {
				continue;
			}
			if (this.pathTable[thisNumX][thisNumY]==j-1
					&& !(currentNumX==0&&thisNumX==currentNumX-1||currentNumX==Terrain.width-1
							&&thisNumX==currentNumX+1)) {
				// Если клетка в этой стороне является предыдущим шагом, перейти
				// на неё
				currentNumX = adjactentX[i];
				currentNumY = adjactentY[i];
				x = currentNumX;
				y = currentNumY;
				break;
			}
		}
		t++ ;
		if (t==900) {
			console["log"]("get path: exit with error");
			break;
		}
	}
	return path.reverse();
};
Character.prototype.updateVisibility = function() {
	if (Terrain.isPeaceful) {
		return;
	}
	this.getVisibleCells();
	for ( var i = 0; i<Terrain.width; i++ ) {
		for ( var j = 0; j<Terrain.height; j++ ) {
			if (this.visibleCells[i][j]&& !this.prevVisibleCells[i][j]) {
				if (this.seenCells[i][j]) {
					Terrain.cells[i][j].unshade();
				} else {
					this.seenCells[i][j] = true;
					Terrain.cells[i][j].show();
				}
			} else if (this.prevVisibleCells[i][j]&& !this.visibleCells[i][j]) {
				Terrain.cells[i][j].shade();
			}
		}
	}
};
Character.prototype.initVisibility = function() {
	if (this.visibleCells.length!=Terrain.height
			||this.visibleCells[0].length!=Terrain.width) {
		// Init arrays if needed
		this.visibleCells = blank2dArray();
		this.prevVisibleCells = blank2dArray();
		this.seenCells = blank2dArray();
		this.pathTable = blank2dArray();
	}
	this.getVisibleCells();
	for ( var i = 0; i<Terrain.width; i++ ) {
		for ( var j = 0; j<Terrain.height; j++ ) {
			if (this.visibleCells[i][j]) {
				Terrain.cells[i][j].show();
				this.seenCells[i][j] = true;
			}
		}
	}
	if (Terrain.isPeaceful) {
		for ( var i = 0; i<Terrain.width; i++ ) {
			for ( var j = 0; j<Terrain.height; j++ ) {
				if ( !Terrain.cells[i][j].visible) {
					Player.seenCells[i][j] = true;
					Player.visibleCells[i][j] = true;
					Terrain.cells[i][j].show();
				}
			}
		}
	} else {

	}
};
Character.prototype.findEnemy = function(r) {
	// Находит ближайшего противника
	var enemy = false;
	// for (var i in characters) {
	// if (this.isEnemy(characters[i]) && this.canSee(characters[i].x,
	// characters[i].y) && (this.aimcharacter==-1 || this.distance(enemy.x,
	// enemy.y)>this.distance(characters[i].x, characters[i].y))) {
	// enemy=characters[i];
	// }
	// }
	return enemy;
};
Character.prototype.findCharacterByCoords = function(x, y) {
	for ( var i in characters) {
		if (characters[i].x==x&&characters[i].y==y) {
			return characters[i];
		}
	}
	return false;
};
/* Checks */
Character.prototype.isBareHanded = function() {
	return this.equipment.getItemInSlot(0)===null;
};
Character.prototype.hasItem = function(typeId, param) {
	// Имеет пресонаж предмет или заданное кол-во предметов
	if (isUnique(typeId)) {

	} else {
		if (param==undefined) {
			var param = 1;
		}
		for ( var i = 0; i<this.itemPiles.length; i++ ) {
			if (this.itemPiles[i][0]==item&&this.itemPiles[i][1]>=num) {
				return true;
			}
		}
		return false;
	}
};
Character.prototype.hasEffect = function(effectId) {
	// Проверка, имеет ли персонаж определённый эффект
	for ( var i in this.effects) {
		if (this.effects[i]==effectId) {
			return true;
		}
	}
	return false;
};
Character.prototype.isEnemy = function(aim) {
	var isAlly = false;
	if (aim.fraction!=this.fraction&&this.fraction!= -1&&aim.fraction!= -1
			&& !isAlly) {
		// Если найденный character не союзник этому character и если они оба —
		// не нейтральные монстры, то они враги
		return true;
	}
	return false;
};
Character.prototype.isNear = function(x, y) {
	var ableX = Math.abs(this.x-x);
	var ableY = Math.abs(this.y-y);
	if ((ableX==1&&ableY==0)||(ableY==1&&ableX==0)||(ableY==1&&ableX==1)) {
		return true;
	}
	return false;
};
/* Animations */
// Methods that show actions of characters and/or change their internal states
Character.prototype.animateSpell = function(spellId, aimId, spellX, spellY,
		callback) {
	spells[spellId].effect(this, (aimId== -1) ? -1 : characters[aimId], spellX,
			spellY, callback);
};
Character.prototype.showDeath = function() {
	this.cellWrap.parentNode.removeChild(this.cellWrap);
	Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
	if (this===Player) {
		UI.notify("death");
		var nlEffects = document.getElementById("effectsList").children;
		while (nlEffects.length>0) {
			nlEffects[0].parentNode.removeChild(nlEffects[0]);
		}
	}
	Terrain.cells[this.x][this.y].character = undefined;
	delete characters[this.characterId];
	handleNextEvent();
};
Character.prototype.dodge = function(attacker) {
	var tg = (this.x-attacker.x)/(this.y-attacker.y); // Тангенс
	var character = this;
	var top, left;
	if (this.x>attacker.x) {
		left = 10;
	} else if (this.x<attacker.x) {
		left = -10;
	}
	if (this.y>attacker.y) {
		top = 10;
	} else if (this.y<attacker.y) {
		top = -10;
	}
	qanimate(this.cellWrap, [left, top], 70, function() {
		qanimate(character.cellWrap, [ -left, -top], 120, function() {
			tryRefreshingInterval();
		});
	});
};
Character.prototype.meleeAttack = function(x, y) {
	var sideX = x-this.x;
	var sideY = y-this.y;
	var left = sideX*8;
	var top = sideY*8;
	var cellWrap = this.cellWrap;
	var character = this;
	// Анимация атаки
	qanimate(cellWrap, [left, top], 100, function() {
		qanimate(cellWrap, [ -left, -top], 100, function() {
			tryRefreshingInterval();
		});
	});
};
Character.prototype.showMove = function(nextCellX, nextCellY) {
	if (this.destX==this.x && this.destY==this.y) {
		// If Player moves not by his will, then destX/Y may remain the same,
		// so destX/Y should be changed
		this.destX = nextCellX;
		this.destY = nextCellY;
	}
	var top = 0;
	var left = 0;
	Terrain.cells[this.x][this.y].character = undefined;
	Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;

	var viewIndent = Terrain.getViewIndentation(nextCellX, nextCellY, 1);

	this.cellWrap.style.top = viewIndent.top*32+"px";
	this.cellWrap.style.left = viewIndent.left*32+"px";
	this.cellWrap.style.zIndex = viewIndent.top*2+2;
	this.x = nextCellX;
	this.y = nextCellY;
	Terrain.cells[this.x][this.y].passability = Terrain.PASS_SEE;
	Terrain.cells[this.x][this.y].character = this;
	if (this.visible&& !Player.visibleCells[this.x][this.y]) {
		this.hideModel();
	} else if ( !this.visible&&Player.visibleCells[this.x][this.y]) {
		this.showModel();
	}
	if (this.isClientPlayer) {
		moveGameField(this.x, this.y);
		this.updateVisibility();
		renderView();
	}
	for ( var i in this.effects) {
		var viewIndent = Terrain.getViewIndentation(this.x, this.y, 1);
		this.effects[i].move(viewIndent.left, viewIndent.top);
	}
	handleNextEvent();
};
Character.prototype.graphicEffect = function(name, callback) {
	// Графический эффект
	// if (!+localStorage.getItem(1)) {
	// // Если графические эффекты отключены, ничего не делать
	// return false;
	// }
	new effectTypes[name](this.x, this.y, this.x, this.y, 100, 100, 100, 100,
			callback);
	// graphicEffects[name].call(this, callback);
};
Character.prototype.showMissileFlight = function(fromX, fromY, toX, toY,
		missile) {
	var startViewIndent = Terrain.getViewIndentation(fromX, fromY, 1);
	var endViewIndent = Terrain.getViewIndentation(toX, toY, 1);
	fromX = startViewIndent.left;
	fromY = startViewIndent.top;
	toX = endViewIndent.left;
	toY = endViewIndent.top;
	var character = this;
	var a = {
		x : fromX,
		y : fromY
	};
	var b = {
		x : toX,
		y : toY
	};
	var mult = (b.x-a.x!=0) ? (b.y-a.y)/(b.x-a.x) : "zero";
	var top = 0;
	var left = 0;
	if (mult<= -4||mult>=4||mult=="zero") {
		top = (b.y>a.y) ? 8 : -8;
		var arrowDest = (b.y>a.y) ? 5 : 1;
	} else if (mult<=4&&mult>=0.25) {
		top = (b.y>a.y) ? 8 : -8;
		left = (b.x>a.x) ? 8 : -8;
		var arrowDest = (b.y>a.y) ? 4 : 8;
	} else if (mult<=0.25&&mult>= -0.25) {
		left = (b.x>a.x) ? 8 : -8;
		var arrowDest = (b.x>a.x) ? 3 : 7;
	} else if (mult>= -4&&mult<= -0.25) {
		top = (b.y>a.y) ? 8 : -8;
		left = (b.x>a.x) ? 8 : -8;
		var arrowDest = (b.y>a.y) ? 6 : 2;
	}
	var num = this.characterId;

	var thischaracter = this;
	// qanimate(thischaracter.cellWrap,[left,top],100,function() {
	// // Анимируем персонажа
	// qanimate(thischaracter.cellWrap,[-left,-top],100,function() {
	// tryRefreshingInterval();
	// });
	// });

	var nWrap = document.createElement("div");
	nWrap.setAttribute("characterId", this.characterId);
	nWrap.className = "wrap";

	var nImg = document.createElement("img");

	nImg.setAttribute("src", "./images/ranged/arrow.png");
	nImg.className = "arrow";
	nImg.style.top = "0px";
	nImg.style.left = "0px";
	var tan = ((toY-fromY)/(toX-fromX)).toFixed(3);
	var sideMod = (toX>=fromX) ? 0 : 1;
	nImg.style.webkitTransform = "rotate("
			+((Math.atan(tan)+Math.PI/2)+sideMod*Math.PI)+"rad)";

	nWrap.appendChild(nImg);
	nWrap.style.top = (fromY*32-16)+"px";
	nWrap.style.left = (fromX*32)+"px";
	nWrap.style.zIndex = fromY+2;
	gameField.appendChild(nWrap);
	// Missile animation
	qanimate(nImg, [(b.x-a.x)*32, (b.y-a.y)*32], distance(fromX, fromY, toX,
			toY)*70, function() {
		nImg.parentNode.removeChild(nImg);
		handleNextEvent();
	});
};
/* Internal state changing */
Character.prototype.loseItem = function(typeId, param) {
	if (isUnique(typeId)) {
		this.items.removeUnique(param);
	} else {
		this.items.removePile(typeId, param);
	}
	UI.notify("inventoryChange");
};
Character.prototype.getItem = function(typeId, param) {
	this.items.addNewItem(typeId, param);
	UI.notify("inventoryChange");
};
Character.prototype.putOn = function(itemId) {
	// Process putting on data sent from server and show it in inventory
	// if (!this.items[itemId]) {
	// throw new Error("Player is trying to put on item "+itemId+" that he
	// doesn't have");
	// }
	// Determine the slot in equipment for the item
	var item = this.items.getUnique(itemId);
	var slot = getSlotFromClass(items[item.typeId][1]);
	if (slot==9&&this.equipment.getItemInSlot(9)) {
		slot = 10;
	}
	this.equipment.putOn(item);
	this.redrawDoll();
};
Character.prototype.takeOff = function(itemId) {
	var slot = 0;
	// For list of slots search items.js
	for (; slot<10; slot++ ) {
		var item = this.equipment.getItemInSlot(slot);
		if (item&&item.itemId==itemId) {
			break;
		}
	}
	if (slot==10) {
		throw new Error("Not found item "+itemId+" in equipment");
	}
	if ( !this.equipment.hasItemInSlot(slot)) {
		throw new Error("Character "+this.name
				+" is trying to take off an item that he doesn't wear");
		return false;
	}
	this.equipment.takeOffFromSlot(slot);
	this.redrawDoll();
};
Character.prototype.changeAttribute = function _(attrId, value) {
	this.attributes[attrId] = value;
	UI.notify("attributeChange", [attrId, value]);
};

/**
 * @class
 * @extends Character
 * @param {mixed[]} data
 */
function ClientPlayer() {
	/** @public @type LetterAssigner */
	this.itemsLetterAssigner = new LetterAssigner("Inventory");
	/** @public @type LetterAssigner */
	this.spellsLetterAssigner = new LetterAssigner("Spells");
	/** @public @type LetterAssigner */
	this.lootLetterAssigner = new LetterAssigner("Loot");
	/** @public @type ItemSet */
	this.items = new ItemSet();
	this.spells = [];
	this.isClientPlayer = true;
	
	// These three will be initiated by blank2dArray in
	// Character.initVisiblilty() on location/global map enter.
	/** @public @type Array[] */
	this.visibleCells = [[]];
	/** @public @type Array[] */
	this.previousVisibleCells = [[]];
	/** @public @type Array[] */
	this.seenCells = [[]];
	/** @public @type string[] */
	this.actionQueue = [];
	/** @public @type Array[] */
	this.actionQueueParams = [];
	/** @public @type number[] */
	this.skills = [];
	/** @public @type string */
	this.name = null;
	/** @public @type number */
	this.race = null;
	/** @public @type string */
	this.cls = null;
	/** @public @type number */
	this.maxHp = null;
	/** @public @type number */
	this.maxMp = null;
	/** @public @type number */
	this.maxEp = null;
	/** @public @type number */
	this.hp = null;
	/** @public @type number */
	this.mp = null;
	/** @public @type number */
	this.ep = null;
	/** @public @type Object */
	this.attributes = {};
	/** @public @type number */
	this.attributes.str = 0;
	/** @public @type number */
	this.attributes.dex = 0;
	/** @public @type number */
	this.attributes.wis = 0;
	/** @public @type number */
	this.attributes.itl = 0;
	/** @public @type number */
	this.attributes.armor = 0;
	/** @public @type number */
	this.attributes.evasion = 0;
	/** @public @type number */
	this.attributes.fireRes = 0;
	/** @public @type number */
	this.attributes.coldRes = 0;
	/** @public @type number */
	this.attributes.poisonRes = 0;
	/** @public @type Doll */
	this.doll = null;
}
ClientPlayer.prototype = new Character(-1);
/** @name Player @type ClientPlayer */

ClientPlayer.prototype.init = function init(data) {
	/*
	 * data : [(0)x, (1)y, (2)characterId, (3)name,
	 * (4)race, (5)class, (6)maxHp, (7)maxMp, (8)maxEp, (9)hp, (10)mp, (11)ep,
	 * (12)str, (13)dex, (14)wis, (15)itl, (16)items[], (17)equipment[],
	 * (18)spells[], (19)skills[], (20)ac, (21)ev, (22)resistances[]]
	 */
	console.log(data);
	Character.apply(this, [data[2], "player", data[0], data[1], 1]);
	characters[data[2]] = this;
	this.x = data[0];
	this.y = data[1];
	UI.notify("titleChange");
	this.name = data[3];
	this.race = data[4];
	this.cls = data[5];
	this.maxHp = data[6];
	this.maxMp = data[7];
	this.maxEp = data[8];
	this.hp = data[9];
	this.mp = data[10];
	this.ep = data[11];
	this.attributes.str = data[12];
	this.attributes.dex = data[13];
	this.attributes.wis = data[14];
	this.attributes.itl = data[15];
	this.attributes.armor = data[20];
	this.attributes.evasion = data[21];
	this.attributes.fireRes = data[22][0];
	this.attributes.coldRes = data[22][1];
	this.attributes.poisonRes = data[22][2];

	// Inventory
	for (var i = 0; i<data[16].length; i+=2) {
		var typeId = data[16][i];
		var param = data[16][i+1];
		var item;
		if (isUnique(typeId)) {
			item = new UniqueItem(typeId, param);
			this.items.add(item);
		} else {
			item = new ItemPile(typeId, param);
			this.items.add(item);
		}
		this.itemsLetterAssigner.addObject(item);
	}

	this.equipment.getFromData(data[17]);
	this.equipment.forEach(function(item) {
		Player.itemsLetterAssigner.addObject(item);
	});

	// Spells
	this.spells = data[18];
	// Skills
//	var len = data[19].length/2;
//	this.skills = [];
//	for (var i = 0; i<len; i++) {
//		Player.skills.push(data[20][i*2]);
//		Player.skills.push(data[20][i*2+1]);
//	}	

	this.autoSetMissileType();

	this.doll = new Doll(this);
	this.cellWrap.appendChild(this.doll.DOMNode);
	this.initHpBar();
	UI.notify("attributesInit");
};
ClientPlayer.prototype.actions = ["push", "changePlaces", "makeSound", "shieldBash", "jump"];
ClientPlayer.prototype.display = function _() {
	gameField.appendChild(this.cellWrap);
	this.doll.draw();
};
ClientPlayer.prototype.showDamage = function _(amount, type) {
	Character.prototype.showDamage.apply(this, arguments);
	UI.notify("healthChange");
};
ClientPlayer.prototype.sendTakeOff = function _(item) {
	Net.send( {
		a : Net.TAKE_OFF,
		itemId : item.itemId
	});
};
ClientPlayer.prototype.putOn = function _(itemId) {
	Character.prototype.putOn.apply(this, [itemId]);
	this.items.removeUnique(itemId);
};
ClientPlayer.prototype.takeOff = function _(itemId) {
	this.items.add(this.equipment.getItemById(itemId));
	Character.prototype.takeOff.apply(this, [itemId]);
	// This code is also in Character.prototype.takeOff;
	// consider changing server output for takeOff event
	// to slot information, not itemId information.
};
ClientPlayer.prototype.autoSetMissileType = function _() {
	if (this.missileType!=null) {
		return;
	}
	for ( var i in this.items.itemPiles) {
		if (isMissile(this.items.itemPiles[i].typeId)) {
			this.setMissileType(this.items.itemPiles[i].typeId);
			return;
		}
	}
	for ( var i in this.items.uniqueItems) {
		if (isMissile(this.items.uniqueItems[i].typeId)) {
			this.setMissileType(this.items.uniqueItems[i].typeId);
			return;
		}
	}
};
ClientPlayer.prototype.setMissileType = function _(type) {
	this.missileType = type;
	UI.notify("missileTypeChange");
};
/* Setters */
ClientPlayer.prototype.getItem = function _(typeId, param) {
	Character.prototype.getItem.apply(this, arguments);
	var item = this.items.getItem(typeId, param);
	if (!this.itemsLetterAssigner.hasLetterForObject(item)) {
		this.itemsLetterAssigner.addObject(item);
	}
};
ClientPlayer.prototype.loseItem = function _(typeId, param) {
	var item = this.items.getItem(typeId, param);
	Character.prototype.loseItem.apply(this, arguments);
	this.itemsLetterAssigner.removeObject(item);
};

/* Interface methods */
ClientPlayer.prototype.selectMissileType = function _(type) {
	this.missileType = type||null;
};
ClientPlayer.prototype.selectMissile = function _() {
	// Enter missile mode
	if (this.equipment.getItemInSlot(0)
			&&this.equipment.getItemInSlot(0).isRanged()) {
		var aimcharacter;
		if (aimcharacter = this.findEnemy()) {
			CellCursor.move(aimcharacter.x, aimcharacter.y);
		} else {
			CellCursor.move(this.x, this.y);
		}

		UI.notify("missileSelect");

	} else {
		UI.notify("alert", "Игрок не держит в руках оружия дальнего боя!");
	}
};
ClientPlayer.prototype.selectSpell = function _(spellId) {
	// Enter spell casting mode
	this.spellId = spellId;
	CellCursor.enterSelectionMode("castSpell");
	UI.notify("spellSelect");
};
ClientPlayer.prototype.cellCursorSpellSelectCallback = function _() {
	this.sendCastSpell(CellCursor.x, CellCursor.y);
	this.unselectSpell();
};
ClientPlayer.prototype.unselectSpell = function _() {
	// Exit spell casting mode
	UI.notify("spellUnselect");
	this.spellId = -1;
	UI.setKeyMapping("Default");
	CellCursor.exitSelectionMode();
};
ClientPlayer.prototype.cellChooseAction = function _() {
	// Совершить действие на координате под курсором
	var x = CellCursor.x;
	var y = CellCursor.y;
	if ( !this.canSee(x, y)) {
		UI.notify("alert", "Игрок не видит целевой клетки!");
	} else {
		if (this.spellId!= -1) {
			// Spell
			this.sendCastSpell(x, y);
		} else {
			// Ranged attack
			var aim = Character.prototype.findCharacterByCoords(x, y);
			if (aim) {
				// On character
				this.sendAttack(aim.characterId, !this.equipment.getItemInSlot(
						0).isMelee());
			} else {
				// On cell
				this.sendShootMissile(x, y, 2300);
			}
		}
		CellCursor.character = Character.prototype.findCharacterByCoords(x, y);
		UI.setKeyMapping("Default");
	}
};
ClientPlayer.prototype.addActionToQueue = function _(actionName, params) {
	if (!(params instanceof Array)) {
		if (params === undefined) {
			params = [];
		} else {
			throw new Error("Incorrect params for queued action: "+params
					+" , queued action:", func);
		}
	}
	if (params === undefined) {
		params = [];
	}
	this.actionQueue.push(actionName);
	this.actionQueueParams.push(params);
};
ClientPlayer.prototype.doActionFromQueue = function _() {
	performAction(this.actionQueue[0], this.actionQueueParams[0]);
	this.actionQueue.shift();
	this.actionQueueParams.shift();
};
ClientPlayer.prototype.locationClickHandler = function _(x, y, e) {
	UI.notify("alert",x+" "+y);
	moveGameField(x, y);
	return;
	if (this.x!=this.destX||this.y!=this.destY) {
		return;
	}
	var aim;
	// If we click behind a character who we can attack
	var dx = x-this.x;
	var shiftX = dx==0 ? this.x : this.x+dx/Math.abs(dx);
	var dy = y-this.y;
	var shiftY = dy==0 ? this.y : this.y+dy/Math.abs(dy);
	if (x==this.x && y==this.y) {
		performAction("idle");
	} else if (
		(aim = Terrain.cells[shiftX][shiftY].character)
		&& this.isEnemy(aim)
	) {
		// Attack
		performAction("attack", [aim]);
	} else if (
		(aim = Terrain.cells[x][y].character)
		&& this != Terrain.cells[x][y].character 
		&& !this.isEnemy(aim)
	) {
		this.sendStartConversation(aim.characterId);
	} else if (
		Terrain.cells[x][y].object
		&& isDoor(Terrain.cells[x][y].object.type)
		&& (!isOpenDoor(Terrain.cells[x][y].object.type) || shiftKey)
		&& this.isNear(x, y)
	) {
		// Open door
		this.sendUseObject(x, y);
	} else if (Terrain.cells[x][y].object && this.isNear(x, y)
			&& isContainer(Terrain.cells[x][y].object.type)) {
		// Open cotainer
		Global.container.x = x;
		Global.container.y = y;
		this.sendOpenContainer();
	} else if (
		Terrain.cells[x][y].passability == Terrain.PASS_BLOCKED
		|| Terrain.cells[x][y].passability == Terrain.PASS_SEE
			
	) {
		// Go to object
		this.aimcharacter = -1;
		this.destX = x;
		this.destY = y;
		this.getPathTable();
		if (this.comeTo(x, y)) {
			performAction("move");
		} else {
			return;
		}
	} else {
		// If Player goes to cell
		this.destX = x;
		this.destY = y;
		performAction("move", [x,y]);
	}
};
ClientPlayer.prototype.sawCell = function() {
	return true;
};
var Player = new ClientPlayer();