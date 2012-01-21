onLoadEvents['gui'] = function _() {
	document.getElementById("intfWindowWrap").style.width = localStorage.getItem(3)+"px";
	document.getElementById("intfWindowWrap").style.height = localStorage.getItem(4)+"px";
	recountWindowSize();
	gameField = document.getElementById("gameField");
	gameField.cellInfo = document.getElementById("cellInfo");
	gameField.style.top = "0px";
	gameField.style.left = "0px";
	floorCanvas = document.getElementById("gameFieldFloor");
	document.onkeydown=handlers.document.keydown;
	document.oncontextmenu=handlers.document.contextmenu;
	
	// Mouse events on game field
	gameField.oncontextmenu = handlers['gameField'].contextmenu;
	gameField.onclick = handlers['gameField'].click;
	gameField.onmouseover = handlers['gameField'].mouseover;
	gameField.onmouseout = handlers['gameField'].mouseout;
	gameField.onmousemove = handlers['gameField'].mousemove;
	gameField.cellInfo.onmouseover = handlers['cellInfo'].mouseover;
	
	CellCursor.init();
	handlers.initInterface();	
};
var CellCursor = {
	x:-1,
	y:-1,
	callback: null,
	context: null,
	zoneCenter: null,
	isSelectionMode: false,
	maximumDistance: 9000,
	currentStyle: "Main",
	shadedCells: [],
	availableCells: [],
	move: function _(x,y) {
		if (this.isSelectionMode && this.availableCells.indexOf(Terrain.cells[x][y])==-1) {
			this.changeStyle("Unavailable");
		} else if (this.isSelectionMode) {
			this.changeStyle("CellAction");
		}
		var viewIndent = Terrain.getViewIndentation(x,y,1);
		mapCursorX=x;
		mapCursorY=y;
		this.elem.style.left = viewIndent.left*32+"px";
		this.elem.style.top = viewIndent.top*32+"px";
		this.x=x;
		this.y=y;
	},
	hide: function _() {
		this.elem.style.display="none";
	},
	show: function _() {
		this.elem.style.display="block";
	},
	init: function _() {
		this.elem = document.createElement("div");
		this.elem.addClass("wrap");
		this.elem.style.zIndex = "2";
		this.bg = document.createElement("div");
		this.elem.appendChild(this.bg);
		gameField.appendChild(this.elem);
		this.changeStyle("Main");
		this.move(0,0);
	},
	invoke: function _(x,y) {
		if (x==undefined) {
			if (this.character) {
				x=this.character.x;
				y=this.character.y;
			} else {
				x=mapCursorX || player.x;
				y=mapCursorY || player.y;
			}
		}
		UI.setMode(UI.MODE_CURSOR_ACTION);
		this.move(x,y);
		this.show();
	},
	withdraw: function _() {
		this.hide();
		UI.setMode(UI.MODE_DEFAULT);
	},
	changeStyle: function _(className) {
		this.currentStyle = className;
		this.bg.className = "cellCursor"+className;
	},
	enterSelectionMode: function _(callback, context, maximumDistance, zoneCenter) {
	/**
	 * Enters cell selection mode. Exit from selection mode and callback call
	 * are in handlers object. Also shades cells if needed, so player can see 
	 * the maximum range of his action.
	 * 
	 * @param {Function} callback Function to call after cell was chosen
	 * @param {Object} context Context of that function (or leave undefined 
	 * so the window object will be the context)
	 * @param {Number} maximumDistance Radius of selection area (or leave 
	 * undefined so distance will be unlimited)
	 * @param {Object} zoneCenter Object with fields object.x and object.y; 
	 * this may be a Character, GameObject or simply {x:int,y:int}. Area of 
	 * applicable cells will be made around th zoneCenter (or leave zone center 
	 * undefined so it will be the player)
	 */
		if (context === undefined) {
			context = window;
		}
		this.context = context;
		if (maximumDistance === undefined) {
			maximumDistance = 9000;
		}
		this.maximumDistance = maximumDistance;
		if (zoneCenter === undefined) {
			zoneCenter = player;
		}
		this.zoneCenter = zoneCenter;
		this.isSelectionMode = true;
		this.callback = callback;
		
		
		UI.setMode(UI.MODE_CURSOR_ACTION);
		this.changeStyle("CellAction");
		// Shade cells
		this.shadedCells = [];
		var startX, startY, endX, endY;
		if (Terrain.isPeaceful) {
			var bounds = UI.getViewCellBounds();
			startX = bounds[0];
			startY = bounds[1];
			endX = startX+bounds[2];
			endY = startY+bounds[3];
		} else {
			startX = zoneCenter.x-player.VISION_RANGE;
			startY = zoneCenter.y-player.VISION_RANGE;
			endX = zoneCenter.x+player.VISION_RANGE;
			endY = zoneCenter.y+player.VISION_RANGE;
		}
		for (var x = startX; x<=endX; x++) {
			for (var y = startY; y<=endY; y++) {
				if (
					player.visibleCells[x][y] 
					&& (Math.floor(distance(zoneCenter.x,zoneCenter.y,x,y)) > maximumDistance
					|| (!Terrain.isPeaceful || !player.canSee(x,y,false,true)))
				) {
					this.shadedCells.push(Terrain.cells[x][y]);
					Terrain.cells[x][y].shade();
				} else {
					this.availableCells.push(Terrain.cells[x][y]);
				}
			}
		}
	},
	chooseCurrentCell: function _() {
	/**
	 * Exits selection mode and calls the callback
	 */
		if (this.callback === null) {
			throw new Error("No callback assigned");
		}		
		this.exitSelectionMode();
		this.callback.apply(this.context, [CellCursor.x, CellCursor.y]);
	},
	exitSelectionMode: function _() {
	/**
	 * Exits selection mode without calling the callback
	 */
		this.isSelectionMode = false;
		UI.setMode(UI.MODE_DEFAULT);
		this.changeStyle("Main");
		for (var i in this.shadedCells) {
			this.shadedCells[i].unshade();
		}
		this.shadedCells = [];
	}
};
function showLoadingScreen() {
	document.getElementById("stLoad").style.display="block";
}
function hideLoadingScreen() {
	document.getElementById("stLoad").style.display="none";
}
function winkElement(elem,text) {
	elem.style.opacity="0";
	elem.innerHTML=text || elem.innerHTML;
	qoanimate(elem,1,300);
}