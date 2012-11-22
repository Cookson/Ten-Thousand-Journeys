var Objects;
/**
 * A marker that can be on a certain cell on the game field. Its general use
 * is to mark a cell under mouse cursor, but instances of CellCursor may be 
 * created on any cell at any time for any purpose, e.g. marking targets in
 * some plugins.
 * 
 * @constructor
 * @param {number} x Absolute x coordinate of a cell in the world (see 
 * {@link CellCursor#move})
 * @param {number} y Absolute y coordinate of a cell in the world (see 
 * {@link CellCursor#move})
 * @param {mixed} style As in  {@link CellCursor#changeStyle}
 */
function CellCursor(x, y, style) {
	this._x = undefined;
	this._y = undefined;
	this._elem = document.createElement("div");
	this._elem.addClass("wrap");
	this._elem.style.zIndex = "2";
	this._bg = document.createElement("div");
	this._elem.appendChild(this._bg);
	GameField.getGameFieldElement().appendChild(this._elem);
	this.changeStyle("cellCursorMain");
	this.move(x, y);
}
/**
 * Move the cursor to a particular cell on game field
 * 
 * @param {number} x Absolute x coordinate of a cell in the world
 * @param {number} y Absolute y coordinate of a cell in the world
 */
CellCursor.prototype.move = function _(x,y) {
	var viewIndent = GameField.getViewIndentation(x,y,1);
	this._elem.style.left = viewIndent.left*32+"px";
	this._elem.style.top = viewIndent.top*32+"px";
	this._x = x;
	this._y = y;
	this._elem.children[0].innerText = x+" "+y;
};
/**
 * Make the cursor hidden
 * @see CellCursor#show
 */
CellCursor.prototype.hide = function() {
	this._elem.style.display = "none";
};
/**
 * Make the cursor visible in the game field
 * @see CellCursor#hide
 */
CellCursor.prototype.show = function() {
	this._elem.style.display = "block";
};
/**
 * Change cursor's appearance. It can be done either by setting its id, or
 * class, or separate CSS attributes.
 * 
 * @param {mixed} style If it is a string that starts from "#" symbol, 
 * then cursor element gets id equal to this string. If it is any other string —
 * then className eual to that string. If it is an object — then 
 * 
 * @example
 * cursor.changeStyle({
 * 	backgroundColor: "#fadebb",
 *  border: "1px solid red",
 *  opacity: "0.8"
 * });
 */
CellCursor.prototype.changeStyle = function(style) {
	if (typeof style === "object") {
	// Separate css attributes values from object's key->value
		for (var i in style) {
			this._bg.style[i] = style[i];
		}
	} else if (typeof style === "string") {
		if (style[0] === "#") {
		// If string starts from #, set element's id to that string
			this._bg.setAttribute("id", style.slice(1));
		} else {
		// Else set element's class to that string
			this._bg.className = style;
		}
	} else {
		throw new Error("Whong style argument");
	}
};

var CellAvailability;
(function () {
	var /** @type CellAvailability */ instance;
	/**
	 * @singleton
	 * @constructor
	 */
	function CellAvailability() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		var availableCells = [];
		var shadedCells = [];
		/**
		 * Enters cell selection mode. Exit from selection mode and callback call
		 * are in handlers object. Also shades cells if needed, so Player can see 
		 * the maximum range of his action.
		 * 
		 * @param {String} callbackAction name of registered UIAction that is performed 
		 * after cell is chosen.
		 * @param {Number} maximumDistance Radius of selection area (or leave 
		 * undefined so distance will be unlimited)
		 * @param {Function} argsFunction Normally the action will be performed 
		 * with arguments [CellCursor.x, CellCursor.y], but if you need other 
		 * arguments, you may specify this function that takes x and y and returns
		 * needed array of arguments.
		 * @param {Object} zoneCenter Object with fields object.x and object.y; 
		 * this may be a Character, GameObject or simply {x:int,y:int}. Area of 
		 * applicable cells will be made around th zoneCenter (or leave zone center 
		 * undefined so it will be the Player)
		 */
		this.enterSelectionMode = function enterSelectionMode(callbackAction, maximumDistance, argsFunction, zoneCenter) {
			if (!(typeof callbackAction == "string")) {
				throw new Error("Callback action must be a string");
			}
			if (argsFunction === undefined) {
				argsFunction = null;
			}
			this.argsFunction = argsFunction;
			if (maximumDistance === undefined) {
				maximumDistance = 9000;
			}
			this.maximumDistance = maximumDistance;
			if (zoneCenter === undefined) {
				zoneCenter = Player;
			}
			this.zoneCenter = zoneCenter;
			this.isSelectionMode = true;
			this.callbackAction = callbackAction;
			
			
			UI.setKeyMapping("CellCursor");
			UI.setClickHandler(this.chooseCurrentCell, this);
			// Shade cells
			this.shadedCells = [];
			var startX, startY, endX, endY;
			startX = Player.x-Player.VISION_RANGE;
			startY = Player.y-Player.VISION_RANGE;
			endX = Player.x+Player.VISION_RANGE;
			endY = Player.y+Player.VISION_RANGE;
			// Shade cells that are unavailable
			for (var x=startX; x<=endX; x++) {
				for (var y=startY; y<=endY; y++) {
					throw new Error("Not implemented: .visible");
					if (World.getCell(x, y).visible) {
						if (Math.floor(distance(zoneCenter.x,zoneCenter.y,x,y)) > maximumDistance) {
							this.shadedCells.push(x);
							this.shadedCells.push(y);
							GameField.shadeCell(x,y);
						} else {
							this.availableCells.push(x+";"+y);
						}
					}
				}
			}
			this.move(this.x, this.y);
		};
		this.chooseCurrentCell = function _() {
		/**
		 * Exits selection mode and calls the callback
		 */
			if (this.callbackAction === null) {
				throw new Error("No callback assigned");
			}
			this.exitSelectionMode();
			if (!this.argsFunction) {
				performAction(this.callbackAction, [CellCursor.x, CellCursor.y]);
			} else {
				performAction(this.callbackAction, this.argsFunction(CellCursor.x, CellCursor.y));
			}
		};
		/**
		 * Exits selection mode without calling the callback
		 */
		this.exitSelectionMode = function ffdd() {
			this.isSelectionMode = false;
			UI.setKeyMapping("Default");
			UI.setClickHandler(Player.locationClickHandler, Player);
			this.changeStyle("Main");
			for (var i=0; i<this.shadedCells.length; i+=2) {
				GameField.unshadeCell(this.shadedCells[i], this.shadedCells[i+1]);
			}
			this.shadedCells = [];
			this.availableCells = [];
		};
		/**
		 * Makes all the cells in the viewport that don't meet the criteria of the 
		 * functinon unavailable (means you can't choose them as a destination for your 
		 * certain current action).
		 * 
		 * @param {Function} func
		 */
		this.applyUnavailability = function(func) {
			
		};
		/**
		 * Make all the cells to available
		 * @return
		 */
		this.reset = function() {
			
		};
		/**
		 * Checks if a particular cell is available or not.
		 * @param {number} x
		 * @param {number} y
		 * @return {boolean} true if available, false if not
		 */
		this.isAvailable = function(x,y) {
			return availableCells.indexOf(x+";"+y) !== -1;
		};
		return instance = this;
	}
	CellAvailability = new CellAvailability();
})();

var CellSelect;
(function CellSelect_() {
	var instance;
	var selectedCells = [];
	var cellNumber;
	var cellAmount;
	var evts;
	/**
	 * A singletone for passing coordinates of cells selected by user to 
	 * various functions. Selection starts with method {@link CellSelect#start}.
	 * More that one cell can be selected this way. Even undefined amount of 
	 * cells can be selected — until player himself ends selection.
	 * 
	 * @singleton
	 * 
	 */
	function CellSelect() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		CellSelect = new CellSelect();
		
		var isSelectionGoing = false;
		/**
		 * Start selection. GameFrame enters the special selection mode when all
		 * other actions are blocked. 
		 * 
		 * @param {number} amount How many cells to select. 0 to select undefined 
		 * amount of cells until the user wants to stop.
		 * @param {object} eventHandlers An object containing functions to execute
		 * whenever a cell is selected, selection is cancelled or selection is ended
		 * @example
		 * CellSelect.start(2, {
		 * 	onSelect: function(x,y) {
		 * 		console.log("A cell {"+x+";"+y"} is selected");
		 *  },
		 *  onUnselect: function(x,y) {
		 * 		console.log("A cell {"+x+";"+y"} is unselected");
		 *  },
		 * });
		 */
		this.start = function(amount, eventHandlers) {
			selectedCells.length = 0;
			cellNumber = 1;
			cellAmount = amount;
			evts = events;
			isSelectionGoing = true;
		};
		/**
		 * Select one cell.
		 */
		this.selectCell = function() {
			var c = [GameFrame.getCursorCellX(), GameFrame.getCursorCellY()];
			selectedCells.push(c);
			cellNumber++;
			evts.onSelect(c[0], c[1]);
			if (cellAmount !== 0 && cellAmount === cellNumber) {
				CellSelect.end();
			}
		};
		/**
		 * Unselects the last selected cell
		 * 
		 */
		this.unselect = function() {
			cellNumber--;
			var c = selectedCells.pop();
			evts.onUnselect(c[0], c[1]);
		};
		/**
		 * Ends cell selection and returns all the selected cells in
		 * [[x,y],[x,y],[x,y]...] array of arrays form.
		 * @return {Array[Array[number]]}
		 */
		this.end = function() {
			if (cellAmount !== 0 && cellNumber < cellAmount) {
				throw new Error("Wrong stop at cell selection: only "+cellNumber+" out of "+cellAmount+" cells are selected");
			}
			evts.onEnd();
			isSelectionGoing = false;
			return selectedCells;
		};
		/**
		 * Checks if GameFrame is now in cell selection mode
		 * @return {boolean} true if it is, false if it is not.
		 */
		this.isSelectionGoing = function() {
			
		};
		return instance = this;
	}
	
})();

var GameField;
(function() {
	var /** @type {GameField} */ instance;
	/**
	 * @type {HTMLElement}
	 */
	var gameField;
	/**
	 * @singleton
	 * @constructor
	 *
	 * A default implementation of everything that displays contents
	 * of the game field (drawing characters, objects, floor, animated effects etc).
	 * In MVC terminology, this is the main View component of the engine.
	 *
	 * To port the engine to a particular graphical output platform except of browser (e.g.
	 * terminal, Unity or anything where JavaScript support can be enabled)
	 * one only has to rewrite the GameField methods to define how the 
	 * engine displays everything.
	 */	 
	function GameField() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		var characters = Characters.getInstance().characters;
		var player; // Copy of link to the raw player's character object
		var chunks; // A container for graphical representations of cells' contents
		var worldData = World.getInstance();
		var cameraOrientation = Side.N;
		var cssSideX = "left";
		var cssSideY = "top";
		var removed = 0, created = 0;
		var storedImages = Graphics.getInstance();
		/**
		 * Data structure that contains visual representation of the contents
		 * of every cell on the game field
		 * 
		 * @param {number} x
		 * @param {number} y
		 */
		function Chunk(x, y) {
			this.objects = new Map2D();
			this.characters = new Map2D();
			this.items = new Map2D();
			this.canvas = document.createElement("canvas");
			this.canvas.setAttribute("width", 32*worldData.chunkWidth);
			this.canvas.setAttribute("height", 32*worldData.chunkWidth);
			this.canvas.style.position = "absolute";
			var viewIndent = instance.getViewIndentation(x, y, 32);
			this.canvas.style.top = viewIndent.top+"px";
			this.canvas.style.left = viewIndent.left+"px";
			this.canvas.style.zIndex = 0;
			this.ctx = this.canvas.getContext("2d");
		}
		window.stat = function() {
			return [created, removed];
		}
		Events.addListener("playerDataLoaded", GameField, function() {
			player = Characters.getInstance().player;
		});
		Events.addListener("chunkLoad", GameField, function(e) {
			// Here chunk's canvas is only being appended to the game field.
			// Initial filling the canvas with graphical content happens in 
			// ClientCharacter.initVisibility
			var chunk = new Chunk(e.x, e.y);
			gameField.appendChild(chunk.canvas);
			chunks.set(e.x, e.y, chunk)
		});
		Events.addListener("chunkUnload", GameField, function(e) {
			var chunk = chunks.get(e.x, e.y);
			chunk.canvas.parentNode.removeChild(chunk.canvas);
			chunks.remove(e.x, e.y);
		});
		Events.addListener("environmentExplored", GameField, function(e) {
		/* Each time player's field of view is changed, this code does the following:
		 * 1) Checks if it is needed to preload any images that will be used in rendering
		 * 2) Loads then if necessary
		 * 3) Renders the new field of view
		 */
		 // See ClientPlayer#initVisibility and ClientPlayer#updateVisibilitiy
		 // to see how event argument is formed.
			if (typeof e.s !== "undefined") {
			// If anything new is seen
				var imageList = {floors: [], chardoll: []};
				// Finding unstored floors
				for (var i=0, l=e.s.f.length; i<l; i++) {
					var floorId = e.s.f[i].f;
					if (
						imageList.floors.indexOf(floorId) === -1 
						&& typeof storedImages.floors[floorId] === "undefined"
					) {
						imageList.floors.push(floorId);
					}
				}
				// Finding unstored character doll images
				for (var i=0, l=e.s.c.length; i<l; i++) {
					var character = e.s.c[i].c;
					if (
						imageList.chardoll.indexOf(58) === -1
						&& typeof storedImages.chardoll[58]
					) {
						imageList.chardoll.push(58);
					}
				}
				Graphics.storeImages(imageList, function() {
				// This is called asynchronously after all the images are stored
					if (typeof e.u !== "undefined") {
						try {
							for (var i=0, l=e.u.f.length; i<l; i++) {
								var floor = e.u.f[i]; // Like event.unseen.floor[i]
								instance.unshowFloor(floor.x, floor.y, floor.f);
							}
						} catch (error) {
							console.log(e.u.f);
							throw new Error("KAKAKA");
						}
						for (var i=0, l=e.u.o.length; i<l; i++) {
							var object = e.u.o[i]; // Like event.unseen.objects[i]
							instance.unshowObject(object.x, object.y, object.o);
						}
						for (var i=0, l=e.u.c.length; i<l; i++) {
							var character = e.u.c[i]; // Like event.unseen.characters[i]
							instance.showCharacter(character.x, character.y, character.c);
						}
						/* Items displaying not implemented! */
					}
					if (typeof e.u !== "undefined") {
						for (var i=0, l=e.s.f.length; i<l; i++) {
							var floor = e.s.f[i]; // Like event.seen.floors[i]
							instance.showFloor(floor.x, floor.y, floor.f);
						}
						for (var i=0, l=e.s.o.length; i<l; i++) {
							var object = e.s.o[i]; // Like event.seen.objects[i]
							instance.showObject(object.x, object.y, object.o);
						}
						for (var i=0, l=e.s.c.length; i<l; i++) {
							var character = e.s.c[i]; // Like event.seen.characters[i]
							instance.showCharacter(character.x, character.y, character.c);
						}
						/* Items displaying not implemented! */
					}
				});
			}
		});
		var charactersViews = {}; // Objects that contain graphical data associated with certain characters
		/**
		 * Initiates the game field. This must be called before usage.
		 */
		this.init = function() {
			gameField = document.getElementById("gameField");
			gameField.style.top = "0px";
			gameField.style.left = "0px";
			this.clearGameField();
		};
		/**
		 * Focuses camera on certain cell}
		 * 
		 * @param {number} cx
		 * @param {number} cy
		 */
		this.setViewPort = function(cx, cy) {
			if (typeof cx !== "number" || typeof cy !== "number") {
				throw new Error("Wrong arguments for setViewPort: ", arguments); 
			}
			var w = GameFrame.getWidth(),
			    h = GameFrame.getHeight();
//			if (typeof w === "number") {
//				if (w % 2 !== 1) {
//					throw new Error("Viewport width and height must be odd numbers (now w = "+w+")");
//				}
//				rendW = w;
//			}
//			if (typeof h === "number") {
//				if (h % 2 !== 1) {
//					throw new Error("Viewport width and height must be odd numbers (now h = "+h+")");
//				}
//				rendH = h;
//			}
			var x=cx, y=cy;
			var normal = instance.getViewIndentation(x,y,32);
			x = normal.left;
			y = normal.top;
//			var xCells=x;
//			var yCells=y;
			x-= w/2;
//			x-=(x%32==0)?0:16;
//			x=(x<0)?((GameField.getHorizontalDimension()-xCells-1)*32<UI.visibleWidth/2)?UI.visibleWidth-GameField.getHorizontalDimension()*32:x:0;
//			if (GameField.getHorizontalDimension()*32<UI.visibleWidth) {
//				x=0;
//			}
			y-= h/2;
//			y-=(y%32==0)?0:16;
//			y=(y<0)?((GameField.getVerticalDimension()-yCells-1)*32<UI.visibleHeight/2)?UI.visibleHeight-GameField.getVerticalDimension()*32:y:0;
//			if (GameField.getVerticalDimension()*32<UI.visibleHeight) {
//				y=0;
//			}
			// Here x and y contain indentations in pixels.
			gameField.style.left = -x+"px";
			gameField.style.top = -y+"px";		
//			if (weatherEffect) {
//				var wx = Player.x;
//				var wy = Player.y;
// throw new Error("Not implemented weather!");
//				weatherEffect.move(Math.min(Math.max(wx, rendCX), GameField.width-rendCX), Math.min(Math.max(wy, rendCY), GameField.height-rendCY));
//			}
		};
		this.showSound = function(x, y, type) {
			throw new Error("Not implamanted");
			var wrap = document.createElement("div");
			var text = document.createElement("div");
			wrap.className = "wrap";
			text.className = "speechBubbleText";
			text.innerText = soundTypes[type].name;
			wrap.style.zIndex = 9000;
			wrap.appendChild(text);
			gameField.appendChild(wrap);
			wrap.style.top = (32*y-text.clientHeight-12) + "px";
			wrap.style.left = (32*x-text.clientWidth/2+16) + "px"; 
			qanimate(wrap, [0,-32], 1000, function(obj) {
				gameField.removeChild(obj);
				handleNextEvent();
			});
		};
		/**
		 * Removes everything from the game field
		 */
		this.clearGameField = function() {
			while (gameField.children.length>0) {
			// Remove all the children of #gameField, except of gameFieldFloor, 
			// cellCursor and UI._gameFieldElementsContainer
				gameField.removeChild(GameField.nGameField.children[0]);
			}
			chunks = new Map2D();
		};
		this.getViewIndentation = function (x, y, scale) {
			if (cameraOrientation == Side.N) {
				return {left: x*scale, top: y*scale};
			} else if (cameraOrientation == Side.E) {
				throw new Error("This is implemented only for North case");
				return {left: (worldData.height-y-1)*scale, top: x*scale};
			} else if (cameraOrientation == Side.S) {
				throw new Error("This is implemented only for North case");
				return {left: (worldData.width-x-1)*scale, top: (World.height-y-1)*scale};
			} else if (this.cameraOrientation == Side.W) {
				throw new Error("This is implemented only for North case");
				return {left: y*scale, top: (width-x-1)*scale};
			} else {
				throw new Error("Unknown camera orientation: "+this.cameraOrientation);
			}
		};
		this.getNormalView = function (x, y) {
			if (cameraOrientation == Side.N) {
				return {x: x, y: y};
			} else if (this.cameraOrientation == Side.E) {
				throw new Error("This is implemented only for North case");
				return {x: y, y: (World.height-x-1)};
			} else if (this.cameraOrientation == Side.S) {
				throw new Error("This is implemented only for North case");
				return {x: (World.width-x-1), y: (World.height-y-1)};
			} else if (this.cameraOrientation == Side.W) {
				throw new Error("This is implemented only for North case");
				return {x: (World.width-y-1), y: x};
			} else {
				throw new Error("Unknown camera orientation: "+this.cameraOrientation);
			}
		};
		/**
		 * Change the perspective from which player sees the game field.
		 * By default the top of the screen represents north, the right of the
		 * screen — east and so on. .rotateCamera rotates view so north
		 * appears from particular side.
		 * @param {Side} side Where north must be.
		 * @return
		 */
		this.rotateCamera = function (side) {
			if (!(side instanceof Side)) {
				throw new Error("This function must get instance of Side as parameter!");
			}
			cameraOrientation = side;
			for (var y=0; y<World.height; y++) {
				for (var x=0; x<World.width; x++) {
					World.cells[x][y].hide();
				}
			}
			floorCanvas.width=World.getHorizontalDimension()*32;
			floorCanvas.height=World.getVerticalDimension()*32;
			for (var y=0; y<World.height; y++) {
				for (var x=0; x<World.width; x++) {
					/* z */ World.cells[x][y].show();
				}
			}
			for (var ch in characters) {
				GameField.showCharacter(characters[ch]("characterId"), characters[ch]("x"),characters[ch]("y"));
			}
			this.moveGameField(Player.x, Player.y, true);
			UI.notify("cameraRotation");
		};
		this.isOrientationVertical = function () {
			return cameraOrientation == Side.N || cameraOrientation == Side.S;
		};
		this.getHorizontalDimension = function _() {
			throw new Error("Not implemented");
			if (cameraOrientation == Side.N || cameraOrientation == Side.S) {
				return World.width;
			} else {
				return World.height;
			}
		};
		this.getVerticalDimension = function _() {
			throw new Error("Not implemented");
			if (cameraOrientation == Side.N || cameraOrientation == Side.S) {
				return World.height;
			} else {
				return World.width;
			}
		};
		this.animateSpell = function(srartX, startY, endX, endY, spellId) {
			throw new Error("Not implemented");
			spells[spellId].effect(this, (aimId== -1) ? -1 : characters[aimId], spellX,
					spellY, callback);
		};
		/** 
		 * A {@link Map2D} containing HTMLElements of 
		 * @type {Map2D}
		 */
		var objects = Objects = new Map2D();
		/**
		 * Draw the floor on a particular cell. This also redraws joint between
		 * the floor on current cell and floors on neighbour cells.
		 * 
		 * @param {number} x x coordinate of cell
		 * @param {number} y y coordinate of cell
		 */
		this.showFloor = function(x, y, floorId) {
			/* */ // May be optimized - if chunk coords are passed in event
			var chunkX = worldData.getChunkRoundedCoord(x);
			var chunkY = worldData.getChunkRoundedCoord(y);
			var chunk = chunks.get(chunkX, chunkY);
			// Отобразить изображение тайла
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
			}
			var viewIndent = this.getViewIndentation(x-chunkX, y-chunkY, 32);
			if (
				   World.floor(x+1, y) !== floorId 
				|| World.floor(x, y+1) !== floorId
				|| World.floor(x-1, y) !== floorId
				|| World.floor(x, y-1) !== floorId
			) {
			// If this tile has neighbours with another floorId, we will also
			// need to draw transitions on tile borders (how the floor smoothly 
			// turns to another type of floor 
				var up = World.floor(x,y-1);
				var right = World.floor(x+1,y);
				var down = World.floor(x,y+1);
				var left = World.floor(x-1,y);
				if (instance.cameraOrientation === Side.E) {
				// It camera is oriented not to the north
					var leftBuf = left;
					left = down;
					down = right;
					right = up;
					up = leftBuf;
				} else if (instance.cameraOrientation === Side.S) {
				// It camera is oriented not to the north
					var upBuf = up;
					var leftBuf = left;
					up = down;
					left = right;
					right = leftBuf;
					down = upBuf;
				} else if (instance.cameraOrientation === Side.W) {
				// It camera is oriented not to the north
					var upBuf = up;
					up = right;
					right = down;
					down = left;
					left = upBuf;
				}
				var tileType= "t"+floorId+","+up+","+right+","+down+","+left;
				
				chunk.ctx.putImageData(
					Graphics.getTransition(
						chunk.ctx.getImageData(0, 0, 32, 32)
					), 
					viewIndent.left, 
					viewIndent.top
				);
			} else {
			// If tile does not bound with a tile of another type
				chunk.ctx.drawImage(tiles[floorId][getNumFromSeed(x, y, StaticData.floor(floorId).num)], viewIndent.left, viewIndent.top);
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
				/* */ // Случай затенения уголков у стен (wtf?)
				this.shade();
			}
		};
		this.unshowFloor = function(x, y) {
			var chunkX = worldData.getChunkRoundedCoord(x);
			var chunkY = worldData.getChunkRoundedCoord(y);
			var chunk = chunks.get(chunkX, chunkY);
			try {
				chunk.ctx.clearRect((x-chunkX)*32, (y-chunkY)*32, 32, 32);
			} catch (e) {
				console.log(chunks, chunk, chunkX, chunkY, x, y);
				throw new Error("KAKAKA");
			}
		};
		this.shadeFloor = function(x, y) {
			var chunkX = worldData.getChunkRoundedCoord(x);
			var chunkY = worldData.getChunkRoundedCoord(y);
			var chunk = chunks.get(chunkX, chunkY);
			var imgData = chunk.ctx.getImageData((x-chunkX)*32, (y-chunkY)*32, 32, 32);
			// Amount of pixels in the area of the floor
			var len = imgData.width*imgData.height; 
			for (var i=0; i<len; i++) {
			// Set alpha channel value of pixels to 128 out of 255
				imgData.data[i*4+3] = 128;
			}
			ctx.putImageData(imgData, (x-chunk.x)*32, (y-chunk.y)*32);
			// if ( +localStorage.getItem(2)) {
			// 	var imageData = ctx.getImageData(x*32, y*32, 32, 32);
			// 	// Отрисовка сетки
			// 	for (var i = 0; i<32; i++) {
			// 		var pix = getPixel(imageData, i, 0);
			// 		pix[3] = 140;
			// 		setPixel(imageData, i, 0, pix);
			// 	}
			// 	for (var j = 0; j<32; j++) {
			// 		var pix = getPixel(imageData, 0, j);
			// 		pix[3] = 140;
			// 		setPixel(imageData, 0, j, pix);
			// 	}
			// 	ctx.putImageData(imageData, x*32, y*32);
			// }
		};
		/**
		 * Displays a flying missile animation.
		 * @param {number} fromX
		 * @param {number} fromY
		 * @param {number} toX
		 * @param {number} toY
		 * @param {number} missile Missile type
		 * @see {StaticData}
		 */
		this.showMissileFlight = function(fromX, fromY, toX, toY, missile) {
			var startViewIndent = GameField.getViewIndentation(fromX, fromY, 1);
			var endViewIndent = GameField.getViewIndentation(toX, toY, 1);
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
			nWrap.style.zIndex = (100000+this.y)*2+2;
			gameField.appendChild(nWrap);
			// Missile animation
			qanimate(nImg, [(b.x-a.x)*32, (b.y-a.y)*32], distance(fromX, fromY, toX,
					toY)*70, function() {
				nImg.parentNode.removeChild(nImg);
				handleNextEvent();
			});
		};
		this.unshadeFloor = function(x, y) {
			var chunkX = worldData.getChunkRoundedCoord(x);
			var chunkY = worldData.getChunkRoundedCoord(y);
			var chunk = chunks.get(chunkX, chunkY);
			var imgData = ctx.getImageData((x-chunkX)*32, (y-chunkY)*32, 32, 32);
			// Amount of pixels in the area of the floor
			var len = imgData.width*imgData.height; 
			for (var i = 0; i < len; i++ ) {
			// Set alpha channel to "not opaque at all"
				imgData.data[i*4+3] = 255;
			}
			ctx.putImageData(imgData, (x-chunkX)*32, (y-chunkY)*32);
		};
		this.showObject = function(x, y, objectId) {
			var chunkX = worldData.getChunkRoundedCoord(x);
			var chunkY = worldData.getChunkRoundedCoord(y);
			var chunk = chunks.get(chunkX, chunkY);
			var nObject;
			if (arguments.length !== 3) {
				throw new Error("Wrong arguments");
			}
			if (!(nObject = chunk.objects.get(x, y))) {
				nObject = document.createElement("img");
				chunk.objects.set(x, y, nObject);
				created++;
				var viewIndent = instance.getViewIndentation(x, y, 1);
				nObject.style.position = "absolute";
				var c = worldData.getCell(x,y);
				var objectType = StaticData.object(World.object(x, y)).type;
				if (objectType === StaticData.OBJECT_TYPE_DOOR) {
				// If there is a door
					nObject.setAttribute("src", "./images/objects/"+objectId+".png");
				} else if (objectType === StaticData.OBJECT_TYPE_WALL) {
				// If there is a wall
					// Make a postfix string which determines the resulting wall image
					var postfix = "";
					// Save object types of objects from 4 sides
					var objectN = World.object(x, y-1);
					var objectE = World.object(x+1, y);
					var objectS = World.object(x, y+1);
					var objectW = World.object(x-1, y);

					// Postfix is a string of 4 numbers "1" or "0", where "1" means there is a 
					// wall, and "0" means there is not a wall
					// For example, "0110" means there is no wall from North and West, but 
					// there are walls from East and South (the order is clockwise, from North on 
					// the firts char to West on the last char)
					if (objectN !== null) {
						var typeN = StaticData.object(World.object(x, y-1)).type;
						var postfixN = (typeN === StaticData.OBJECT_TYPE_WALL || typeN === StaticData.OBJECT_TYPE_DOOR) ? "1" : "0";
					} else {
						var postfixN = "0";
					}
					if (objectE !== null) {
						var typeE = StaticData.object(World.object(x+1, y)).type;
						var postfixE = (typeE === StaticData.OBJECT_TYPE_WALL || typeE === StaticData.OBJECT_TYPE_DOOR) ? "1" : "0";
					} else {
						var postfixE = "0";
					}
					if (objectS !== null) {
						var typeS = StaticData.object(World.object(x, y+1)).type;
						var postfixS = (typeS === StaticData.OBJECT_TYPE_WALL || typeS === StaticData.OBJECT_TYPE_DOOR) ? "1" : "0";
					} else {
						var postfixS = "0";
					}
					if (objectW !== null) {
						var typeW = StaticData.object(World.object(x-1, y)).type;
						var postfixW = (typeW === StaticData.OBJECT_TYPE_WALL || typeW === StaticData.OBJECT_TYPE_DOOR) ? "1" : "0";
					} else {
						var postfixW = "0";
					}

					if (cameraOrientation == Side.N) {
						postfix = postfixN + postfixE + postfixS + postfixW;
					} else if (cameraOrientation == Side.E) {
						postfix = postfixW + postfixN + postfixE + postfixS;
					} else if (cameraOrientation == Side.S) {
						postfix = postfixS + postfixW + postfixN + postfixE;
					} else if (cameraOrientation == Side.W) {
						postfix = postfixE + postfixS + postfixW + postfixN;
					}
					nObject.setAttribute("src", "./images/walls/"+StaticData.object(objectId).wallName+"_"+postfix+".png");
				} else {
					nObject.setAttribute("src", "./images/objects/"+objectId+".png");
				}
				nObject.style.top = viewIndent.top*32+(-StaticData.object(objectId).imgh+32)+"px";
				nObject.style.left = viewIndent.left*32+(-StaticData.object(objectId).imgw+32)/2+"px";
				/* */ /* A very dirty thing to do that! Need zIndex manobject(objectId). ass zIndexes */
				nObject.style.zIndex = (100000+y)*2;
			}
			nObject.style.display = "inline-block";
			gameField.appendChild(nObject);
		};
		this.unshowObject = function(x, y) {
			chunks
				.get(worldData.getChunkRoundedCoord(x), worldData.getChunkRoundedCoord(y))
				.objects
				.get(x, y)
				.style.display = "none";
		};
		this.removeObject = function(x, y) {
			var objects = chunks
				.get(worldData.getChunkRoundedCoord(x), worldData.getChunkRoundedCoord(y))
				.objects
			var nObject = objects.get(x,y);
			nObject.parentNode.removeChild(nObject);
			objects.remove(x, y);
			removed++;
		};
		this.removeChunk = function(x, y) {
			var nCanvas = chunks.get(x, y).canvas;
			nCanvas.parentNode.removeChild(nCanvas);
			chunks.remove(x, y);
		};
		this.shadeObject = function(x,y) {
			chunks
				.get(worldData.getChunkRoundedCoord(x), worldData.getChunkRoundedCoord(y))
				.objects
				.get(x, y)
				.style.opacity = "0.5";
		};
		this.unshadeObject = function(x,y) {
			chunks
				.get(worldData.getChunkRoundedCoord(x), worldData.getChunkRoundedCoord(y))
				.objects
				.get(x, y)
				.style.opacity = "1";
		};
		this.showGraphicEffect = function(name, callback) {
			// Графический эффект
			// if (!+localStorage.getItem(1)) {
			// // Если графические эффекты отключены, ничего не делать
			// return false;
			// }
			new effectTypes[name](this.x, this.y, this.x, this.y, 100, 100, 100, 100,
					callback);
			// graphicEffects[name].call(this, callback);
		};
		this.showDodge = function(dodgerId, attackerId) {
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
		this.showMeleeAttack = function(attackerId, x, y) {
			var sideX = x-this.x;
			var sideY = y-this.y;
			var left = sideX*8;
			var top = sideY*8;
			var cellWrap = this.cellWrap;
			var character = this;
			// Анимация атаки
			qanimate(cellWrap, [left, top], 100, function() {
				qanimate(cellWrap, [-left, -top], 100, function() {
					tryRefreshingInterval();
				});
			});
		};
		this.showEffectStart = function(characterId, effectId) {
			charactersViews[characterId].effects[effectId] = new effectTypes[effectId](this.x, this.y, this.x,
					this.y, 100, 100, 100, 100);
		};
		this.showEffectEnd = function(characterId, effectId) {
			var character = charactersViews[characterId];
			character.effects[effectId].markForDestruction();
			delete character.effects[effectId];
		};
		this.showDeath = function(characterId) {
			throw new Error("Not implemented");
			var character = characterViews[characterId];
			character.cellWrap.parentNode.removeChild(character.cellWrap);
			if (this === Player) {
				var nlEffects = document.getElementById("effectsList").children;
				while (nlEffects.length>0) {
					nlEffects[0].parentNode.removeChild(nlEffects[0]);
				}
			}
			delete charactersViews[this.characterId];
			handleNextEvent();
		};
		/**
		 * Shows a character's model on game field. Creates the model if it doesn't 
		 * exist, unhides if it does
		 * 
		 * @param {number} characterId Id of the character.
		 * @param {number} x X coordinate.
		 * @param {number} y Y coordinate.
		 */
		this.showCharacter = function(characterId, x, y) {
			var character = characters[characterId];
			if (typeof charactersViews[characterId] === "undefined") {
				var characterView = charactersViews[characterId] = {};
				characterView.cellWrap = document.createElement("div");
				characterView.cellWrap.className = "cellWrap";
				characterView.cellWrap.setAttribute("id", "character"+character.characterId);
				if (player.characterId === characterId) {
				// If we are drawing a player (not necessarily the Client Player
					characterView.cellWrap.appendChild((characterView.doll = new Doll(character)).element);
					gameField.appendChild(characterView.cellWrap);
					characterView.cellWrap.style.top = (character.y*32)+"px";
					characterView.cellWrap.style.left = (character.x*32)+"px";
				} else {
				// If we are drawing an NPC
					characterView.image.setAttribute("src", "./images/characters/"+character.type+".png");
					if (this.typeincharacterSpriteSizes) {
						// If this character's sprite has irregular size (not 32х32)
						nCharacterImage.style.left = ((32-characterSpriteSizes[character.type][0])/2)+"px";
						nCharacterImage.style.top = ((16-characterSpriteSizes[character.type][1]))+"px";
					}
					if (character.fraction == 1) {
						// Friend marker
						var nWrap = document.createElement("div");
						nWrap.className = "wrap";
						var nFriendMarker = document.createElement("img");
						nFriendMarker.className = "cellFriendMarker";
						nFriendMarker.src = "./images/intf/friendMarker.png";
						nWrap.appendChild(nFriendMarker);
						characterView.cellWrap.insertBefore(nWrap, characterView.cellWrap.children[0]);
					}
					characterView.cellWrap.appendChild(nCharacterImage);
				}
			}
		};

		this.unshowCharacter = function(characterId) {
			if (characters[characterId].image !== undefined) {
				characters[characterId].image.style.display = "inline-block";
			} else {
				characters[characterId].doll.style.display = "block";
			}
			for (var i in characters[characterId].effects) {
				characters[characterId].effects[i].pause();
				characters[characterId].effects[i].clear();
			}
		};

		this.removeCharacter = function(characterId) {
			gameField.removeChild(characters[characterId].doll || characters[characterId].image);
			/* */ // Don't forget to remove effects!
		};

		this.moveCharacter = function(characterId, x, y) {
			var character = characters[characterId];
			var viewIndent = this.getViewIndentation(x, y, 1);
			var characterView = charactersViews[characterId];
			characterView.cellWrap.style.left = viewIndent.left*32+"px";
			characterView.cellWrap.style.top = viewIndent.top*32+"px";
			characterView.cellWrap.style.zIndex = (100000+viewIndent.top)*2+1;
			// for (var i in character.effects) {
			// 	var viewIndent = this.getViewIndentation(charactersData[characterId].x, charactersData[characterId].y,1);
			// 	character.effects[i].move(viewIndent.left, viewIndent.top);
			// }
		};

		this.showSpeech = function(characterId, message) {
			var bg = document.createElement("div");
			var text = document.createElement("div");
			var wrap = document.createElement("div");
			var wrap2 = document.createElement("div");
			wrap.className = "wrap";
			wrap2.className = "wrap";
			bg.className = "speechBubbleBg";
			text.className = "speechBubbleText";
			text.innerText = message;

			wrap.style.zIndex = 2147483647;
			wrap2.style.zIndex = 2147483647;
			text.style.zIndex = 1;

			wrap2.appendChild(text);
			wrap.appendChild(wrap2);
			wrap.appendChild(bg);

			gameField.appendChild(wrap);
			bg.style.height = text.clientHeight-8+"px";
			bg.style.width = text.clientWidth-8+"px";
			wrap.style.top = (32*charactersData[characterId].y-text.clientHeight-12)+"px";
			wrap.style.left = (32*charactersData[characterId].x-text.clientWidth/2+16)+"px";
			wrap.onclick = handlers.speechBubble.click;
			wrap.onmouseover = handlers.speechBubble.mouseover;
			wrap.onmouseout = handlers.speechBubble.mouseout;
			wrap.setAttribute("isMouseOver", "0");
			wrap.setAttribute("time", new Date().getTime());
			setTimeout(function() {
				if (wrap.getAttribute("isMouseOver") == "0") {
					gameField.removeChild(wrap);
					return false;
				}
			}, 2000);
		};
		this.getGameFieldElement = function() {
			return gameField;
		};
		Events.addListener("playerMove", window, function(e) {
			GameFrame.setViewPort(e.x, e.y);
		});
		return instance = this;
	};
	window.GameField = new GameField();
})();


function Doll(character) {
	// Кукла персонажа
	// Использует глобальные переменные: charDollImages,
	// Аргумент character - объект персонажа, которому принадлежит эта кукла
	this.character = character;
	this.element = document.createElement("canvas");
	this.element.style.position = "absolute";
	this.element.style.zIndex = 9000;
	this.element.width = 32;
	this.element.height = 32;
	this.ctx = this.element.getContext("2d");
	this.hands = [[6, 14]];
	this.drawn = false; // Кукла уже была отрисована как минимум один раз
	this.items = {
		weapon : 50
	};
	this.draw();
}
/**
 * @private
 * Draw body of character's race.
 */
Doll.prototype.drawBody = function() {
	var image = Graphics.getImage("chardoll", 58);
	this.ctx.drawImage(image, 0, 0);
};
/**
 * @private
 * Draw piece of equimpent on character.
 * @param {Number} typeId Item typeId
 */
Doll.prototype.drawEquipment = function(typeId) {
	this.ctx.drawImage(images[typeId], 0, 0);
};
/**
 * Redraws the doll: its body and equipment worn.
 */
Doll.prototype.draw = function() {
	this.ctx.clearRect(0, 0, 32, 32);
	this.drawBody();
	if (
		this.character && this.character.equipment &&
		this.character.equipment.getItemInSlot(6)
	) {
		this.drawEquipment(this.character.equipment.getItemInSlot(6).typeId);
	}
	if (this.character.characterId == Characters.player("characterId")) {
		for (var i=0; i < Equipment.prototype.NUMBER_OF_SLOTS; i++) {
			if (!this.character.equipment.hasItemInSlot(i)) {
				continue;
			}
			if (!images[this.character.equipment.getItemInSlot(i).typeId]) {
				continue;
			}
			if (i == 6) {
				continue;
			}
			if (
				this.character && this.character.equipment &&
				this.character.equipment.getItemInSlot(i)
			) {
				this.drawEquipment(this.character.equipment.getItemInSlot(i).typeId);
			}
		}
	} else if (!this.character.characterId == Player.characterId) {
		this.drawEquipment(342);
		this.drawEquipment(344);
	}
	
	this.drawn = true;
};
function winkElement(elem,text) {
	elem.style.opacity="0";
	elem.innerHTML=text || elem.innerHTML;
	qoanimate(elem,1,300);
}
