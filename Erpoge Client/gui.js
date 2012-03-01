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
	UI.setClickHandler(Player.locationClickHandler, Player);
};
var CellCursor = {
	x:-1,
	y:-1,
	callbackAction: null,
	argsFunction: null,
	zoneCenter: null,
	isSelectionMode: false,
	maximumDistance: 9000,
	currentStyle: "Main",
	shadedCells: [],
	availableCells: [],
	move: function _(x,y) {
		if (this.isSelectionMode && this.availableCells.indexOf(x+";"+y)==-1) {
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
		this.elem.style.display = "none";
	},
	show: function _() {
		this.elem.style.display = "block";
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
	withdraw: function _() {
		this.hide();
		UI.setKeyMapping("Default");
	},
	changeStyle: function _(className) {
		this.currentStyle = className;
		this.bg.className = "cellCursor"+className;
	},
	enterSelectionMode: function enterSelectionMode(callbackAction, maximumDistance, argsFunction, zoneCenter) {
	/**
	 * Enters cell selection mode. Exit from selection mode and callback call
	 * are in handlers object. Also shades cells if needed, so Player can see 
	 * the maximum range of his action.
	 * 
	 * @param {String} callback name of registered UIAction that is performed 
	 * after cell is chosen.
	 * @param {Number} maximumDistance Radius of selection area (or leave 
	 * undefined so distance will be unlimited)
	 * @param {function} argsFunction Normally the action will be performed 
	 * with arguments [CellCursor.x, CellCursor.y], but if you need other 
	 * arguments, you may specify this function that takes x and y and returns
	 * needed array of arguments.
	 * @param {Object} zoneCenter Object with fields object.x and object.y; 
	 * this may be a Character, GameObject or simply {x:int,y:int}. Area of 
	 * applicable cells will be made around th zoneCenter (or leave zone center 
	 * undefined so it will be the Player)
	 */
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
				if (Terrain.getCell(x, y).visible) {
					if (Math.floor(distance(zoneCenter.x,zoneCenter.y,x,y)) > maximumDistance) {
						this.shadedCells.push(x);
						this.shadedCells.push(y);
						Terrain.shadeCell(x,y);
					} else {
						this.availableCells.push(x+";"+y);
					}
				}
			}
		}
		this.move(this.x, this.y);
	},
	chooseCurrentCell: function _() {
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
	},
	exitSelectionMode: function _() {
	/**
	 * Exits selection mode without calling the callback
	 */
		this.isSelectionMode = false;
		UI.setKeyMapping("Default");
		UI.setClickHandler(Player.locationClickHandler, Player);
		this.changeStyle("Main");
		for (var i=0; i<this.shadedCells.length; i+=2) {
			Terrain.unshadeCell(this.shadedCells[i], this.shadedCells[i+1]);
		}
		this.shadedCells = [];
		this.availableCells = [];
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