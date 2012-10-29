function Character(id, type, x, y, fraction, hp, maxHp) {
	this.visible;

	this.cellWrap = document.createElement("div");

	var viewIndent = GameField.getViewIndentation(playerData.x, playerData.y, 32);
	this.cellWrap.style.top = viewIndent.top+"px";
	this.cellWrap.style.left = viewIndent.left+"px";
	this.cellWrap.style.opacity = "0";
	this.cellWrap.style.zIndex = (100000+playerData.y)*2+1;


	// Spell
	this.spellId = -1;
	this.spellAimId = -1;
	this.spellX = -1;
	this.spellY = -1;
}
/* View */
/* Calculations */
/* Checks */
/* Animations */
// Methods that show actions of characters and/or change their internal states
/* Internal state changing */

(function() {
	var instance
	/**
	 * @singleton
	 */
	var characters = Characters.getInstance();
	var playerData;
	var isSelectingCell
	var visionRange = 20;
	var pathTableWidth = 41;
	var pathTable = blank2dArray();
	var destX, destY;
	var worldData = World.getInstance();
	var pathTableGot = false; // Tells if pathTable was already built on current turn
	Events.addListener("nextTurn", this, function() {
		pathTableGot = false;
		console.timeEnd("turn");
		console.time("turn");
	});
	function ClientPlayer() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		instance = this;
		/** @public @type ItemSet */
		this.items = new ItemSet();
		this.spells = [];
		/** @public @type LetterAssigner */
		this.itemsLetterAssigner = new LetterAssigner("Inventory");
		/** @public @type LetterAssigner */
		this.spellsLetterAssigner = new LetterAssigner("Spells");
		/** @public @type LetterAssigner */
		this.lootLetterAssigner = new LetterAssigner("Loot");
		
		var visibleCells = blank2dArray();
		var prevVisibleCells = blank2dArray();
		
		/**
		 * @private
		 * Calculates the distance from a point to a line (i.e. the length of the normal
		 * to line from that point).
		 * 
		 * @param {number} xStart Coordinates of a line
		 * @param {number} yStart
		 * @param {number} xEnd
		 * @param {number} yEnd
		 * @param {number} xPoint Coordinates of a point
		 * @param {number} yPoint
		 * @return
		 */
		function distanceToLine(xStart, yStart, xEnd, yEnd, xPoint, yPoint) {
			return Math.abs(((yStart-yEnd)*xPoint+(xEnd-xStart)*yPoint+(xStart
					*yEnd-yStart*xEnd))
					/Math.sqrt(Math.abs((xEnd-xStart)
							*(xEnd-xStart)+(yEnd-yStart)
							*(yEnd-yStart))));
		}
		Object.defineProperty(this, "x", {
			get: function() {
				return playerData.x;
			}
		});
		Object.defineProperty(this, "y", {
			get: function() {
				return playerData.y;
			}
		});
		Object.defineProperty(this, "destX", {
			get: function() {
				return destX;
			}
		});
		Object.defineProperty(this, "destY", {
			get: function() {
				return destY;
			}
		});
		/**
		 * Checks if the cell is on line of sight
		 * 
		 * @param {boolean} setVisibleCells - sets this.visibleCells[x][y] to true if can see, to
		 * false otherwise.
		 * @return {boolean} true if it is, false if it isn't
		 */
		this.canSee = function(x, y, setVisibleCells) {
			// return true;
			var absDx = playerData.x-visionRange;
			var absDy = playerData.y-visionRange;
			if (isNear(playerData.x, playerData.y, x, y) || playerData.x==x && playerData.y==y) {
				// Если клетка рядом или персонаж на ней стоит - то её точно видно
				if (setVisibleCells) {
					visibleCells[x-absDx][y-absDy] = true;
				}
				return true;
			}
			if (Math.floor(this.distance(x, y))>visionRange) {
				return false;
			}
			// Алгоритм рассматривает несколько случаев взаимного расположения начальной
			// и целевой клетки,
			// поскольку в значительной части случаев расчёт можно упростить. Алгоритм
			// для общего случая рассматривается последним.
			if (x==playerData.x || y==playerData.y) {
				// Для случая, когда тангенс прямой (угловой коэффициент) равен
				// бесконечности или 0
				// (т.е. когда в цикле в else может быть деление на ноль т.к. абсцисы
				// или ординаты конца и начала равны)
				// В этом случае придётся сделать только одну проверку по линии (не
				// таким методом, как в else для прямых с tg!=0 и tg!=1)
				if (x == playerData.x) {
					// Для вертикальных линий
					var dy = Math.abs(y-playerData.y)/(y-playerData.y);
					for (var i=playerData.y+dy; i!=y; i+=dy) {
						if (World.passability(x, i) === StaticData.PASSABILITY_BLOCKED) {
							return false;
						}
					}
				} else {
					// Для горизонтальных линий
					var dx = Math.abs(x-playerData.x)/(x-playerData.x);
					for (var i=playerData.x+dx; i!=x; i += dx) {
						if (World.pasability(i,y) === StaticData.PASSABILITY_BLOCKED) {
							return false;
						}
					}
				}
				if (setVisibleCells) {
					visibleCells[x-absDx][y-absDy] = true;
				}
				return true;
			} else if (Math.abs(x-playerData.x)==1) {
				// Для случая, когда координаты конца и начала находятся на двух
				// соседних вертикальных линиях
				var yMin = Math.min(y, playerData.y);
				var yMax = Math.max(y, playerData.y);
				for (var i=yMin+1; i<yMax; i++) {
					if (World.passability(x, i) === StaticData.PASSABILITY_BLOCKED) {
						break;
					}
					if (i == yMax-1) {
						if (setVisibleCells) {
							visibleCells[x-absDx][y-absDy] = true;
						}
						return true;
					}
				}
				for (var i=yMin+1; i<yMax; i++) {
					if (World.passability(playerData.x, i) === StaticData.PASSABILITY_BLOCKED) {
						break;
					}
					if (i == yMax-1) {
						if (setVisibleCells) {
							visibleCells[x-absDx][y-absDy] = true;
						}
						return true;
					}
				}
				return false;
			} else if (Math.abs(y-playerData.y) == 1) {
				// Тот же случай, что и предыдущий, но для горизонтальных линий
				var xMin = Math.min(x, playerData.x);
				var xMax = Math.max(x, playerData.x);
				for (var i=xMin+1; i<xMax; i++) {
					if (World.passability(i, y) === StaticData.PASSABILITY_BLOCKED) {
						break;
					}
					if (i==xMax-1) {
						if (setVisibleCells) {
							visibleCells[x-absDx][y-absDy] = true;
						}
						return true;
					}
				}
				for (var i=xMin+1; i<xMax; i++) {
					if (World.passability(i, playerData.y) === StaticData.PASSABILITY_BLOCKED) {
						break;
					}
					if (i == xMax-1) {
						if (setVisibleCells) {
							visibleCells[x-absDx][y-absDy] = true;
						}
						return true;
					}
				}
				return false;
			} else if (Math.abs(x-playerData.x) == Math.abs(y-playerData.y)) {
				// Случай, когда линия образует с осями угол 45 градусов (abs(tg)==1)
				var dMax = Math.abs(x-playerData.x);
				var dx = (x>playerData.x) ? 1 : -1;
				var dy = (y>playerData.y) ? 1 : -1;
				var cx = playerData.x;
				var cy = playerData.y;
				for (var i=1; i<dMax; i++) {
					cx += dx;
					cy += dy;
					if (World.passability(cx, cy) === StaticData.PASSABILITY_BLOCKED) {
						return false;
					}
				}
				if (setVisibleCells) {
					visibleCells[x-absDx][y-absDy] = true;
				}
				return true;
			} else {
			// Общий случай
				var start = [[], []];
				var end = [];
				// x и y концов соответствуют x и y центра клетки или её ближайшего угла
				// (значения перебираются в цикле по k каждое с каждым)
				end[0] = (x>playerData.x) ? x-0.5 : x+0.5;
				end[1] = (y>playerData.y) ? y-0.5 : y+0.5;
				end[2] = x;
				end[3] = y;
				start[0][0] = (x>playerData.x) ? playerData.x+0.5 : this.x-0.5;
				start[0][1] = (y>playerData.y) ? playerData.y+0.5 : this.y-0.5;
				start[1][0] = (x>playerData.x) ? playerData.x+0.5 : this.x-0.5;
				// start[0][1]=playerData.y;
				// start[1][0]=playerData.x;
				start[1][1] = (y>playerData.y) ? playerData.y+0.5 : this.y-0.5;
				var rays = this.rays(playerData.x, playerData.y, x, y);
				jump: for (var k = 0; k<3; k++ ) {
					var endNumX = (k==0||k==1) ? 0 : 2;
					var endNumY = (k==0||k==2) ? 1 : 3;
					for (j = 0; j<1; j++) {
						// Новый алгоритм расчёта видимости строится на том, есть ли
						// точки,
						// которые находятся ближе, чем на 0.5 клетки от прямой -
						// косвенный признак того, что прямая пересекает преграду.
						// Преграды в этом случае считаются окружностями с R=0.5
						// Это не мешает расчёту видимости на стыках клеток по
						// горизонтали.
						// В этом случае нужно сделать максимум шесть проверок (3 цикла
						// по k - точки конца - и два по j - точки начала)
						if (start[j][0]==playerData.x && start[j][1]==playerData.y) {
							continue;
						}
						var xEnd = end[endNumX];
						var yEnd = end[endNumY];
						var xStart = start[j][0];
						var yStart = start[j][1];
						for (var i in rays) {
							/* */// Здесь x|yPoint - глобальные переменные.
									// Пофиксить.
							xPoint = rays[i][0];
							yPoint = rays[i][1];
							if (World.passability(xPoint, yPoint) === StaticData.PASSABILITY_BLOCKED) {
								// Проверяем каждую клетку
								if (
									xPoint==playerData.x && yPoint==playerData.y 
									|| xPoint==x && yPoint==y
								) {
									continue;
								}
								if (
									Math.abs(((yStart-yEnd)*xPoint+(xEnd-xStart)*yPoint+(xStart
												*yEnd-yStart*xEnd))
												/Math.sqrt(Math.abs((xEnd-xStart)
														*(xEnd-xStart)+(yEnd-yStart)
														*(yEnd-yStart))))<=0.5
								) {
									// Если расстояние до точки не больше 0.5, проверяем
									// следующую из 6 линий
									continue jump;
								}
							}
						}
						if (setVisibleCells) {
							visibleCells[x-absDx][y-absDy] = true;
						}
						return true;
					}
				}
				return false;
			}
		};
		/**
		 * Cast a ray and remember visible cells on its way.
		 * 
		 * @param {number} x
		 * @param {number} y
		 */
		this.castVisibilityRay = function(x, y) {
			var absDx = playerData.x-visionRange;
			var absDy = playerData.y-visionRange;
			if (isNear(playerData.x, playerData.y, x, y) || playerData.x==x && playerData.y==y) {
			// If the target cell is close to player (or he stays on it) — it is definitely visible
				visibleCells[x-absDx][y-absDy] = true;
				return true;
			}
			// Алгоритм рассматривает несколько случаев взаимного расположения начальной
			// и целевой клетки,
			// поскольку в значительной части случаев расчёт можно упростить. Алгоритм
			// для общего случая рассматривается последним.
			if (x === playerData.x || y === playerData.y) {
				// Для случая, когда тангенс прямой (угловой коэффициент) равен
				// бесконечности или 0
				// (т.е. когда в цикле в else может быть деление на ноль т.к. абсцисы
				// или ординаты конца и начала равны)
				// В этом случае придётся сделать только одну проверку по линии (не
				// таким методом, как в else для прямых с tg!=0 и tg!=1)
				if (x === playerData.x) {
					// Для вертикальных линий
					var dy = Math.abs(y-playerData.y)/(y-playerData.y);
					for (var i=playerData.y+dy; i!=y+dy; i+=dy) {
						visibleCells[x-absDx][i-absDy] = true;
						if (World.passability(x, i) === StaticData.PASSABILITY_BLOCKED) {
							break;
						}
					}
				} else {
					// Для горизонтальных линий
					var dx = Math.abs(x-playerData.x)/(x-playerData.x);
					for (var i=playerData.x+dx; i!=x+dx; i += dx) {
						visibleCells[i-absDx][y-absDy] = true;
						if (World.passability(i, y) === StaticData.PASSABILITY_BLOCKED) {
							break;
						}
					}
				}
			} else if (Math.abs(x-playerData.x) === 1) {
			// Для случая, когда координаты конца и начала находятся на двух
			// соседних вертикальных линиях
				var dy = y>playerData.y ? 1 : -1;
				for (var i=playerData.y+dy; i!=y+dy; i+=dy) {
					visibleCells[x-absDx][i-absDy] = true;
					if (
						World.passability(x, i) === StaticData.PASSABILITY_BLOCKED
						&& World.passability(playerData.x, i) === StaticData.PASSABILITY_BLOCKED
					) {
						return false;
					}
				}
			} else if (Math.abs(y-playerData.y) === 1) {
			// The same case as the previous one, but for horizontal lines
				var dx = x>playerData.x ? 1 : -1;
				for (var i=playerData.x+dx; i!=x+dx; i+=dx) {
					visibleCells[i-absDx][y-absDy] = true;
					if (
						World.passability(i, y) === StaticData.PASSABILITY_BLOCKED
						&& World.passability(i, playerData.y) === StaticData.PASSABILITY_BLOCKED
					) {
						return false;
					}
				}
			} else if (Math.abs(x-playerData.x) === Math.abs(y-playerData.y)) {
			// A case when line of sight makes 45 degrees angle with coordinate axises
				var dMax = Math.abs(x-playerData.x);
				var dx = (x>playerData.x) ? 1 : -1;
				var dy = (y>playerData.y) ? 1 : -1;
				var cx = playerData.x;
				var cy = playerData.y;
				for (var i=1; i<dMax; i++) {
					cx += dx;
					cy += dy;
					if (World.passability(cx, cy) === StaticData.PASSABILITY_BLOCKED) {
						return false;
					}
					visibleCells[cx-absDx][cy-absDy] = true;
				}
				visibleCells[x-absDx][y-absDy] = true;
				return true;
			} else {
			// A "common" case. Note that this algorithm will not work with rays on 
			// positions described in previous sections of current if-else block
				// dx/dy denote the quarter of world plane in which the line of sight is built
				// For example, dx = 1, dy = -1 denotes 1st quarter of game's 2-dimensional Euclidean space
				var dx = (playerData.x-x)/Math.abs(playerData.x-x); 
				var dy = (playerData.y-y)/Math.abs(playerData.y-y); 
				var currentX = playerData.x; // Current cell (moves throughout the algorithm). This is not the same as player's coordinate.
				var currentY = playerData.y;
				var availableCells = {0:[]}; // Index — distance in steps from player to a cell, value — array of cell coordinates that may belong to the trail
				var trail = [{x:playerData.x, y:playerData.y, d: 2}]; /* A path of the visibility ray. 
				The array consists of objects of the following structure:
					{
						x: {number},
						y: {number},
						[d: {number} (
							0 means that from previous cell this one has x = xPrev-dx, 
							1 means x = yPrev-dy, 
							2 means x = xPrev-dx, y=yPrev-dy (this is considered "common case", so player's cell has d = 3)
						)
					}
				*/
				var deletedCells = {}; // A hash map of hash maps. The first index is x coordinate, the second is y coordinate.
				for (var delx=Math.min(playerData.x, x), delmax=Math.max(playerData.x, x); delx<=delmax; delx++) {
					deletedCells[delx] = {};
				}
				var t = 1; // Index of current cell in trail
				var u = 0;
				while (true) {
					/* 
					   On each step of the algorithm three cells are taken:
					   one vertically near current cell,
					   one horizontally near a current cell,
					   and one diagonally between them. So selected cells
					   form one of these patterns (@ is current cell, stars are
					   selected cells).
					   .....................
					   ..**...**...*@...@*..
					   ..@*...*@...**...**..
					   .....................
					*/
					var trailCandidates = []; // Cells that may be in trail. The array has the same structure as trail, only here can be <= 3 cells and the order in not important
					var cache = trail[trail.length-1].d;
					var l; // Length of nextCells array
					if (cache === 0) {
					// If previous cell was 
						var nextCells = [
							{x:currentX-dx, y:currentY, d: 0}, 
							{x:currentX-dx, y:currentY-dy, d: 0}
						];
						l = 2;
					} else if (cache === 1) {
						var nextCells = [
							{x:currentX, y:currentY-dy, d: 1}, 
							{x:currentX-dx, y:currentY-dy, d: 1}
						];
						l = 2;
					} else {
						var nextCells =  [
							{x:currentX-dx, y:currentY, d: 2}, 
							{x:currentX, y:currentY-dy, d: 2}, 
							{x:currentX-dx, y:currentY-dy, d: 2}
						];
						l = 3;
					}
					for (var i=0; i<l; i++) {
					// In this loop we form trailCandidates array and also fill visibleCells
						var cellX = nextCells[i].x, 
						    cellY = nextCells[i].y;
						if (typeof deletedCells[cellX] === "undefined") {
							deletedCells[cellX] = {};
						}
						if (
							typeof deletedCells[cellX][cellY] === "undefined"
							&& distanceToLine(playerData.x, playerData.y, x, y, cellX, cellY)<=
							// Without this condition characters won't be able to 
							// properly look around a corner staying near a wall, 
							// beacuse a wall will be too close to character
								(Math.abs(playerData.x-x)===2 && Math.abs(playerData.y-y)>6 
								|| Math.abs(playerData.y-y)===2 && Math.abs(playerData.x-x)>6 ? 1 : 0.71)
						) {
						// If cell is visible and close enough to line to be a part of 
						// trail, add it to trail candidates
							visibleCells[cellX-absDx][cellY-absDy] = true;
							if (World.passability(cellX, cellY) !== StaticData.PASSABILITY_BLOCKED) {
								var cdx = nextCells[i].x-trail[trail.length-1].x;
								var cdy = nextCells[i].y-trail[trail.length-1].y;
								if (
									nextCells[i].d === 2 
									&& cdx*cdy === 0 // If one of cd[xy] is 0 (both can't appear to be 0, so we don't check for that too)
								) {
								// If previous cell doesn't restrict the line of sight on using explictly
								// -dx or -dy cells, but current should, set current cell's .d property
									if (cdy === 0 /** meaning that (cdx === 1 || cdx === -1) */) {
										nextCells[i].d = 0;
									} else /* if (cdx === 0 , meaning that (cdy === 1 || cdy === -1)) */ {
										nextCells[i].d = 1;
									}
								}
								trailCandidates.push(nextCells[i]);
							}
						}
						if (cellX === x && cellY === y) {
							return true;
						}
					}
					if (trailCandidates.length === 0) {
					// If we can't go anywhere from current cell, step back until we 
					// can find a step with available cells
						while (availableCells[t-1].length === 0) {
							t--;
							if (t === 0) {
							// If we return to the first cell, then there's no line of 
							// sight to x:y
								return false;
							}
							delete availableCells[t];
							var deletedCell = trail.pop();
							// Now there is definitely no line to destination cell
							// through this cell, so we can mark it as deleted and 
							// treat it like it blocks the line of sight, so we don't
							// need to compute extra iterations later.
							deletedCells[deletedCell.x][deletedCell.y] = true;
						}
						var cellToStartAgain = availableCells[t-1].pop();
						currentX = cellToStartAgain.x;
						currentY = cellToStartAgain.y;
					} else {
					// Else take one cell of the trail candidates that is closest to actual line
					// from cell 1 to cell 2, and put other trail candidates in availableCells array
						var bestCellIndex = 0;
						var minDistanceToLine = distanceToLine(playerData.x, playerData.y, x, y, trailCandidates[0].x, trailCandidates[0].y);
						for (var i=1; i<trailCandidates.length; i++) {
						// Find a cell that is the closest to default line from start coord 
						// to end coord.
							var curDistanceToLine = distanceToLine(playerData.x, playerData.y, x, y, trailCandidates[i].x, trailCandidates[i].y);
							if (curDistanceToLine < minDistanceToLine) {
								bestCellIndex = i;
								minDistanceToLine = curDistanceToLine;
							}
						}
						availableCells[t] = [];
						for (var i=0; i<trailCandidates.length; i++) {
						// Add other trail candidates to available cells list
							if (i !== bestCellIndex) {
								availableCells[t].push(trailCandidates[i]);
							}
						}
						trail.push(trailCandidates[bestCellIndex]);
						// So trail candidates are saved in availableCells' last element, and best cell is saved in trail last element
						currentX = trail[t].x;
						currentY = trail[t].y;
						t++;
					}
					// if (++u === 10000) {
					// 	throw new Error("Too much");
					// }
				}
			}
		};
		this.ray = function(startX, startY, endX, endY) {
			// Вспомогательная функция для this->canSee(). Возвращает клетки линии от
			// xStart:yStart до xEnd:yEnd
			if (startX==endX && startY==endY) {
				return [startX, startY];
			}
			var x = [];
			var y = [];
			x[0] = startX;
			y[0] = startY;
			var L = Math.round(Math.max(Math.abs(endX-x[0]), Math.abs(endY-y[0])));
			var dX = (endX-x[0])/L;
			var dY = (endY-y[0])/L;
			for (var i=1; i<L; i++ ) {
				x[i] = x[i-1]+dX;
				y[i] = y[i-1]+dY;
			}
			x.push(endX);
			y.push(endY);
			var result = [];
			for (i = 0; i<=L; i++ ) {
				result.push([Math.round(x[i]), Math.round(y[i])]);
			}
			return result;
		};
		this.rays = function(startX, startY, endX, endY) {
			// Вспомогательная функция для this->canSee
			// Возвращает набор координат клеток, которые необходимо проверить для
			// проверки видимости
			return this.ray(startX, startY, endX, endY).concat(
					this.ray(startX, startY+(endY>startY ? 1 : -1), endX
							+(endX>startX ? -1 : 1), endY),
					this.ray(startX+(endX>startX ? 1 : -1), startY, endX, endY
							+(endY>startY ? -1 : 1)));
		};
		this.distance = function(x, y) {
			return Math.sqrt(Math.pow(playerData.x-x, 2)+Math.pow(playerData.y-y, 2));
		};
		this.sees = function(x, y) {
			return visibleCells[x-playerData.x+visionRange][y-playerData.y+visionRange];
		};
		this.getVisibleCells = function() {
			prevVisibleCells = visibleCells;
			visibleCells = blank2dArray(visionRange*2+1, visionRange*2+1);
			var dx = playerData.x-visionRange;
			var dy = playerData.y-visionRange;
			var minX = playerData.x-visionRange;
			var minY = playerData.y-visionRange;
			var maxX = playerData.x+visionRange;
			var maxY = playerData.y+visionRange;
			getVis1:
			for (var x=playerData.x, y=minY; x<=maxX; x++) {
				do {
					this.castVisibilityRay(x,y);
					if (Math.floor(this.distance(x+1,y))>visionRange) {
						y++;
					} else {
						continue getVis1;
					}
					if (y > playerData.y) {
						break getVis1;
					}
				} while(true);
			}
			getVis2:
			for (var x=playerData.x, y=maxY; x<=maxX; x++) {
				do {
					this.castVisibilityRay(x,y);
					if (Math.floor(this.distance(x+1,y))>visionRange) {
						y--;
					} else {
						continue getVis2;
					}
					if (y <= playerData.y) {
						break getVis2;
					}
				} while(true);
			}
			getVis3:
			for (var x=playerData.x, y=maxY; x>=minX; x--) {
				do {
					this.castVisibilityRay(x,y);
					if (Math.floor(this.distance(x-1,y))>visionRange) {
						y--;
					} else {
						continue getVis3;
					}
					if (y <= playerData.y) {
						break getVis3;
					}
				} while(true);
			}
			getVis4:
			for (var x=playerData.x, y=minY; x>=minX; x--) {
				do {
					this.castVisibilityRay(x,y);
					if (Math.floor(this.distance(x-1,y))>visionRange) {
						y++;
					} else {
						continue getVis4;
					}
					if (y > playerData.y) {
						break getVis4;
					}
				} while(true);
			}
			visibleCells[visionRange][visionRange] = true;
		};
		this.isBareHanded = function() {
			return this.equipment.getItemInSlot(0)===null;
		};
		this.findEnemy = function(r) {
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
		/**
		 * Returns the cell that is chosen by player to come to.
		 * 
		 * @returns {Object} x - destX, y - destY
		 */
		this.getDestination = function() {
			return {x: destX, y:destY};
		};
		/**
		 * Sets a cell where player is aimed to go. This value can be used in
		 * some Actions
		 * 
		 * @param {number} x
		 * @param {number} y
		 * @returns {Object} x - destX, y - destY
		 */
		this.setDestination = function(x, y) {
			destX = x;
			destY = y;
			return {x: destX, y:destY};
		};
		/**
		 * PathTable is a 2-dimensional array NxN that represents cells on the game 
		 * field. The center cell of it is where player stays. Each cell of PathTable
		 * contains the number of steps from player to the corresponding cell on
		 * the game field. Building PathTable considers all the obstacles on the way.
		 * So each cell of PathTable shows how many steps does player need to perform
		 * to come to that cell.
		 * 
		 * @param {boolean} ignoreCharacters If true, then PathTable will be built
		 * as if player can step on cells occupied by other players.
		 */
		this.getPathTable = function(ignorecharacters) {
			/* Path table has relative indexes of cells: pathTable itself is a
			 * pathTableWidth * this.PATH_TABLE_WIDTH square, character's coord
			 * in it is {pathTableWidth/2,this.PATH_TABLE_WIDTH/2} rounded down.
			 * But cell objects in newFront and oldFront contain absolute coordinates!
			 */
			pathTableGot = true;
			pathTable = blank2dArray(pathTableWidth, pathTableWidth);
			// Difference between cell's absolute coordinate and its coordinate in
			// the pathTable.
			var dX = playerData.x-(pathTableWidth-1)/2;
			var dY = playerData.y-(pathTableWidth-1)/2;
			var isPathFound = false;
			var oldFront = [];
			var newFront = [];
			newFront[0] = {
				x : playerData.x,
				y : playerData.y
			};
			pathTable[playerData.x-dX][playerData.y-dY] = 0;
			var t = 0;
			do {
				oldFront = newFront;
				newFront = [];
				if (isPathFound === null) {
					isPathFound = true;
				}
				for (var i=0; i<oldFront.length; i++) {
				// Moves front to 8 available sides from each cell
					// These four have absolute coordinates
					var x = oldFront[i].x;
					var y = oldFront[i].y;
					var adjactentX = [x+1, x, x, x-1, x+1, x+1, x-1, x-1];
					var adjactentY = [y, y-1, y+1, y, y+1, y-1, y+1, y-1];
					for (var j=0; j<8; j++) {
					// Index of cell in pathTable (relative coordinates)
						var thisNumX = adjactentX[j]-dX;
						var thisNumY = adjactentY[j]-dY;
						if (
							thisNumX<0 
							|| thisNumX >= pathTableWidth 
							|| thisNumY < 0
							|| thisNumY >= pathTableWidth
							|| typeof pathTable[thisNumX][thisNumY] !== "undefined"
						) {
						// If the new cell is not in pathTable or it is already in pathTable
							continue;
						}
						if (thisNumX+dX==destX && thisNumY+dY==destY) {
						// So in the next iteration the function will know that path is found
							isPathFound = null;
						}
						if (
							World.passability(thisNumX+dX, thisNumY+dY) !== StaticData.PASSABILITY_BLOCKED
							&& World.passability(thisNumX+dX, thisNumY+dY) !== StaticData.PASSABILITY_SEE
							|| worldData.getCell(thisNumX+dX,thisNumY+dY).object
							&& isDoor(worldData.getCell(thisNumX+dX,thisNumY+dY).object.type)
						) {
						// If character can step to a cell, add it to the pathTable
							pathTable[thisNumX][thisNumY] = t+1;
							newFront[newFront.length] = {
								x : adjactentX[j],
								y : adjactentY[j]
							};
						}
					}
				}
				t++ ;
				if (t>900) {
					throw new Error("Too long getPathTable cycle");
				}
			} while (newFront.length>0 && !isPathFound && t<900);
			return pathTable;
		};
		this.getPath = function(x, y) {
			if (typeof x === "undefined" || typeof y === "undefined") {
				throw new Error("Wrong arguments");
			}
			if (x === playerData.x && y === playerData.y) {
				throw new Error("Gets path to its own x:y");
			}
			if (isNear(playerData.x, playerData.y, x, y)) {
			// If destination is right near the character, then return an array 
			// of just one cell — the destination cell.
				return [{
					x : x,
					y : y
				}];
			}
			var path = [];
			var currentNumX = x;
			var currentNumY = y;
			var cx = currentNumX;
			var cy = currentNumY;
			// Half of the width of the filled part of the pathTable (its filled 
			// part is a rectangle)
			var dX = playerData.x-(pathTableWidth-1)/2;
			// Half of the height of the same thing
			var dY = playerData.y-(pathTableWidth-1)/2;
			// Error counter
			var t = 0;
			for (var j=pathTable[currentNumX-dX][currentNumY-dY]; j>0; j=pathTable[currentNumX-dX][currentNumY-dY]) {
			// var j is a number of steps from the player's coordinate to the "target cell";
			// target cell is the destination cell (at the start of the loop), and it moves one step
			// towards the player with each iteration.
				path.push( {
					x : currentNumX,
					y : currentNumY
				});
				var adjactentX = [cx, cx+1, cx, cx-1, cx+1, cx+1, cx-1, cx-1];
				var adjactentY = [cy-1, cy, cy+1, cy, cy+1, cy-1, cy+1, cy-1];
				for (var i = 0; i<8; i++) {
				// From the target cell, check all the 8 sides for a cell that is 1 step closer
				// to player's coordinate than the current cell.
					var thisNumX = adjactentX[i]-dX; // The x coordinate of the cell that is checked
					if (thisNumX<0 || thisNumX>=pathTableWidth) {
						continue;
					}
					var thisNumY = adjactentY[i]-dY; // The y coordinate of the same cell
					if (thisNumY<0 || thisNumY>=pathTableWidth) {
						continue;
					}
					if (
						pathTable[thisNumX][thisNumY]==j-1
					) {
					// If the cell from this side is a previous step, go on it.
						currentNumX = adjactentX[i];
						currentNumY = adjactentY[i];
						cx = currentNumX;
						cy = currentNumY;
						break;
					}
				}
				t++;
				if (t==900) {
					throw new Error("get path: exit with error");
					break;
				}
			}
			return path.reverse();
		};
		/**
		 * Returns the cell near an occupied cell in which player should come if he
		 * wants to come _near_ that occupied cell. This isused, for example, to 
		 * come to an object or a character.
		 * 
		 * @param {number} x X coordinate of the occupied cell
		 * @param {number} y Y coordinate of the occupied cell
		 * @returns {Object} {x: number, y: number} coordinates of the cell to come to
		 * 		or false if there is no path or player stays right next to target cell
		 */
		this.getComeToDest = function(x, y) {
			if (isNear(playerData.x, playerData.y, x, y)) {
			// Стоять на месте, если цель на соседней клетке
				return false;
			}
			if (!pathTableGot) {
				this.getPathTable();
			}
			var dists = [x-1, y, x+1, y, x, y-1, x, y+1, x+1, y+1, x-1, y+1, x+1, y-1, x-1, y-1];
			var dX = playerData.x-(pathTableWidth-1)/2;
			var dY = playerData.y-(pathTableWidth-1)/2;
			var furthestPathSteps = 99999999;  // Amount of steps on the closest found path (initially - just a very large number)
			var answer = {}; // This object will be returned
			var atLeastOnePath = false; // Has at least one path been found
			for (var i=0; i<8; i++) {
				var steps = pathTable[dists[i*2]-dX][dists[i*2+1]-dY];
				if (typeof steps !== "undefined" && steps < furthestPathSteps && steps > 0) {
					furthestPathSteps = steps;
					answer.x = dists[i*2];
					answer.y = dists[i*2+1];
				}
			}
			if (furthestPathSteps === 99999999) {
			// If no path is found
				return false;
			}
			return answer;
		};
		this.init = function init(data) {
			// Character.apply(this, [data[2], "player", data[0], data[1], 1]);
			// characters[data[2]] = this;
			// playerData.x = data[0];
			// playerData.y = data[1];
			// UI.notify("titleChange");
			// this.name = data[3];
			// this.race = data[4];
			// this.cls = data[5];
			// this.maxHp = data[6];
			// this.maxMp = data[7];
			// this.maxEp = data[8];
			// this.hp = data[9];
			// this.mp = data[10];
			// this.ep = data[11];
			// this.attributes.str = data[12];
			// this.attributes.dex = data[13];
			// this.attributes.wis = data[14];
			// this.attributes.itl = data[15];
			// this.attributes.armor = data[20];
			// this.attributes.evasion = data[21];
			// this.attributes.fireRes = data[22][0];
			// this.attributes.coldRes = data[22][1];
			// this.attributes.poisonRes = data[22][2];
			// this.isClientPlayer = true;
			playerData = characters.characters[data[2]];
			playerData.equipment.getFromData(data[17]);
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

			destX = playerData.x;
			destY = playerData.y;
			// this.initHpBar();
			this.itemsLetterAssigner._setSources(playerData.items, playerData.equipment);
			this.spellsLetterAssigner._setSources(this.spells);
		};
		this.actions = ["push", "changePlaces", "makeSound", "shieldBash", "jump"];
		this.states = ["default", "run", "sneak", "sleep", "aim"];
		this.putOn = function _(itemId) {
			Character.prototype.putOn.apply(this, [itemId]);
			this.items.removeUnique(itemId);
		};
		this.takeOff = function _(itemId) {
			this.items.add(this.equipment.getItemById(itemId));
			Character.prototype.takeOff.apply(this, [itemId]);
			// This code is also in Character.prototype.takeOff;
			// consider changing server output for takeOff event
			// to slot information, not itemId information.
		};
		this.autoSetMissileType = function _() {
			if (this.selectedMissile != null) {
				return;
			}
			this.items.forEach(function(item) {
				if (isMissile(item.typeId)) {
					performAction("selectMissile", [item]);
					return HashSet.BREAK;
				}
			}, this);
		};
		/* Setters */
		this.loseItem = function (typeId, param) {
			var item = this.items.getItem(typeId, param);
			Character.prototype.loseItem.apply(this, arguments);
			UI.notify("inventoryChange");
			if (this.selectedMissile == item && !this.items.hasItem(item)) {
				performAction("selectMissile", [null]);
				this.autoSetMissileType();
			}
		};
		/* Interface methods */
		//ClientPlayer.prototype.selectMissile = function _() {
		//	// Enter missile mode
		//	if (this.equipment.getItemInSlot(0)
		//			&&this.equipment.getItemInSlot(0).isRanged()) {
		//		var aimcharacter;
		//		if (aimcharacter = this.findEnemy()) {
		//			CellCursor.move(aimcharacter.x, aimcharacter.y);
		//		} else {
		//			CellCursor.move(playerData.x, playerData.y);
		//		}
		//
		//		UI.notify("missileSelect");
		//
		//	} else {
		//		UI.notify("alert", "Игрок не держит в руках оружия дальнего боя!");
		//	}
		//};
		this.addActionToQueue = function _(actionName, params) {
			if (!(params instanceof Array)) {
				if (typeof params === "undefined") {
					params = [];
				} else {
					throw new Error("Incorrect params for queued action: "+params+" , queued action:", func);
				}
			}
			if (typeof params === "undefined") {
				params = [];
			}
			this.actionQueue.push(actionName);
			this.actionQueueParams.push(params);
		};
		this.doActionFromQueue = function _() {
			performAction(this.actionQueue[0], this.actionQueueParams[0]);
			this.actionQueue.shift();
			this.actionQueueParams.shift();
		};
		this.locationClickHandler = function _(x, y, e) {
		//	moveGameField(x, y);
		//	performAction("move", [x, y]);
		//	return;
		};
		this.sawCell = function() {
			return true;
		};
		/**
		 * Redraws the game field after player's field of view is changed 
		 * (player moved or his vision radius changed or something 
		 * started blocking the view)
		 * 
		 * @param {number} dx How far on x-axis player moved since his last 
		 * 		{@link ClientPlayer#updateVisibility} or 
		 * 		{@link ClientPlayer#initVisiblilty}
		 * @param {number} dy The same on y-axis
		 */
		this.updateVisibility = function(dx, dy) {
			this.getVisibleCells();
			var range = visionRange*2+1;
			var seenThings = { // Lists of things that are seen (or unseen, se below) by player.
			// This is being filled and sent to the game field drawing singletone
				s: { // Seen cells
					o: [], // Object
					f: [], // Floors
					c: [], // Characters
					i: []  // Items
				},
				u: { // Unseen cells
					o: [],
					f: [],
					c: [],
					i: []
				}
			};
			for (var i=0; i<range; i++) {
				for (var j=0; j<range; j++) {
					// Through the loop x and y variables look at square area around player
					// where player's field of view is.
					// Player's field of view on the previous turn is inside the square
					// shifted by dx/dy (there are provided in arguments) 
					var x = playerData.x-visionRange+i;
					var y = playerData.y-visionRange+j;
					// The next two if statements operate on different cells:
					// the first looks at cells in _current_ vision range and shows
					// contents of cells that are visible now;
					// the second looks at cells in vision range from the previous
					// place the player stood _before moving_, and _unshows_ contents
					// of cells that are invisible now.
					if (
						visibleCells[i][j] 
						&& (i+dx<0 || i+dx>=range
						|| j+dy<0 || j+dy>=range
						|| !prevVisibleCells[i+dx][j+dy])
					) {
					// This condition iterates through the newly seen cells
						var c = worldData.getCell(x, y);
						seenThings.s.f.push({x:x, y:y, f: c.f}); // Add cell to list of seen
						if (typeof c.o !== "undefined") {
							seenThings.s.o.push({x:x, y:y, o: c.o}); // Add object to list of seen
						}
						if (typeof c.character !== "undefined") {
							seenThings.s.c.push({x:x, y:y, c: c.character}); // Add character to list of seen
						}
					}
					if (
						prevVisibleCells[i][j] 
						&& (i-dx<0 || i-dx>=range
						|| j-dy<0 || j-dy>=range
						|| !visibleCells[i-dx][j-dy])
					) {
					// This condition iterates through the previously seen cells.
						var prevX = x-dx;
						var prevY = y-dy;
						var c = worldData.getCell(prevX, prevY);
						seenThings.u.f.push({x:prevX, y:prevY, f: c.f}); // Add floor to list of seen
						if (typeof c.o !== "undefined") {
							seenThings.u.o.push({x:prevX, y:prevY, o: c.o}); // Add object to list of seen
						}
						if (typeof c.character !== "undefined") {
							seenThings.u.c.push({x:prevX, y:prevY, c: c.character}); // Add character to list of seen
						}

					}
				}
			}
			Events.fire("environmentExplored", seenThings);
		};
		/**
		 * Hides and shows cells according to player's vision
		 * 
		 * @param {number} dx Difference by x-axis between previous player's position
		 * and current position.
		 * @param {number} dy Same for y-axis.
		 */
		this.initVisibility = function() {
			this.getVisibleCells();
			var seenThings = { // Lists of things that are seen (or unseen, se below) by player.
			// This is being filled and sent to the game field drawing singletone
				s: { // Seen cells
					o: [], // Object
					f: [], // Floors
					c: [], // Characters
					i: []  // Items
				}
				/* 
				, u: { // Unseen cells (is not used in this method, but used in 
					   // ClientPlayer.updateVisibility); has the same structure 
					   // as seenThings.s
					o: [],
					f: [],
					c: [],
					i: []
				}
				*/
			};
			for (var i=0; i<visionRange*2+1; i++) {
				for (var j=0; j<visionRange*2+1; j++) {
					if (visibleCells[i][j]) {
						var x = playerData.x-visionRange+i;
						var y = playerData.y-visionRange+j;
						var c = worldData.getCell(x, y);
						seenThings.s.f.push({x:x, y:y, f:c.f});
						var objId;
						if (typeof (objId = c.o) !== "undefined") {
							seenThings.s.o.push({x:x, y:y, o:objId});
						}
					}
				}
			}
			Events.fire("environmentExplored", seenThings);
		};
	}
	/** @name Player @type ClientPlayer */
	window.Player = new ClientPlayer();
})();
