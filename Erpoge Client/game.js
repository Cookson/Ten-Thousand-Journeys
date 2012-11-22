window.onunload = function () {
// Разлогинивание персонажа при перезагрузке, если перезагрузка - не quickRefresh
	if (localStorage.getItem(101) == "0") {
		Net.send({a:Net.DEAUTH});
	}
};
window.onload = function _() {
	setTimeout(function _() {
		onLoadEvents['customStylesheets']();
		onLoadEvents['game']();
		onLoadEvents['uiWindowPrototype']();
		onLoadEvents['defaultUIActions']();
		onLoadEvents['storage']();


		// onLoadEvents['keys']();
		// setTimeout(function() {
		// 	var head = document.getElementsByTagName("head")[0];
		// 	for (var i=0; i<EnabledModules.length; i++) {
		// 		var script = document.createElement("script");
		// 		script.setAttribute("src", "./modules/"+EnabledModules[i]+"/module.js");
		// 		head.appendChild(script);
		// 	}
		// },1);
		
		GameField.init();
		GameFrame.init(1024, 600);
		serverAddress = localStorage.getItem("serverAddress");
		Net.readStorageToServers();
		Net.init();
		Events.fire("engineLoad");
	}, 1);
	document.getElementById("intfWindowWrap").style.width = localStorage.getItem(3)+"px";
	document.getElementById("intfWindowWrap").style.height = localStorage.getItem(4)+"px";
	(function() {
		var /** @type {GameFrame} */ instance;
		/**
		 * Represents the viewport (the rectangle-shaped "window", or "camera")
		 * through which player looks at the world. Not to be mixed up with
		 * {@link GameField}
		 * 
		 * @see GameField 
		 * @constructor
		 * @singleton
		 * @return {GameFrame}
		 */
		function GameFrame() {
			var inited = false,
			    w = undefined,
			    h = undefined,
			    events = {};
			if (typeof instance !== 'undefined') {
				return instance;
			}
			instance = this;
			
			/**
			 * Returns GameFrame's main element width in pixels (the visible width 
			 * of game frame).
			 * 
			 * @return {number} Width of #intfGameZone in pixels
			 */
			this.getWidth = function() {
				return w;
			};
			/**
			 * Returns GameFrame's main element height in pixels (the visible 
			 * height of game frame).
			 * 
			 * @return {number} Height of #intfGameZone in pixels
			 */
			this.getHeight = function() {
				return h;
			};
			/**
			 * This method should be called to start working with the GameFrame
			 * 
			 * @param {number} width Width of GameFrame in pixels
			 * @param {number} height Width of GameFrame in pixels
			 */
			this.init = function(width, height) {
				if (inited) {
					throw new Error("GameFrame is already init!");
				}
				w = width;
				h = height;
				inited = true;
				document.getElementById("intfGameZone").style.width = w+"px";
				document.getElementById("intfGameZone").style.height = h+"px";
			};
			/**
			 * MouseHandler handles mouse actions (clicks, moves etc) on GameFrame
			 * element. It controls different in-game types of cursors.
			 * 
			 * @constructor
			 * @singleton
			 */
			
			return this;
		};
		window.GameFrame = new GameFrame();
	})();
	
	(function() {
		var /** @type MouseHandler */ instance;
		var player;
		Events.addListener("playerDataLoaded", this, function() {
			player = Characters.getInstance().player;
		});
		function MouseHandler(element) {
			if (typeof instance !== "undefined") {
				return instance;
			}
			instance = this;
			var currentCursor,
			    prevClientX, 
			    prevClientY,
			    mouseCursorAbsCellX,
			    mouseCursorAbsCellY,
			    mouseCursor = new CellCursor();
			element.addEventListener("click", function mouseHandlerClick(e) {
				var elementCoord = getOffsetRect(GameField.getGameFieldElement());
				var xPx = Math.floor((e.clientX-elementCoord.left)/32);
				var yPx = Math.floor((e.clientY-elementCoord.top)/32);
				var normal = GameField.getNormalView(xPx, yPx);
				var x = normal.x;
				var y = normal.y;
				var dest = Player.getDestination();
				if (
					player.x != dest.x
					|| player.y != dest.y
				) {
					return;
				}
				var aim;
				// If we click behind a character who we can attack
				var dx = x-player.x;
				var shiftX = dx==0 ? player.x : player.x+dx/Math.abs(dx);
				var dy = y-player.y;
				var shiftY = dy==0 ? player.y : player.y+dy/Math.abs(dy);
				var cache; // User in several conditions below
				if (x === Characters.player("x") && y === Characters.player("y")) {
					performAction("idle");
				} else if (
					(aim = World.character(shiftX, shiftY))
					&& player.isEnemy(aim)
				) {
					// Attack
					performAction("attack", [aim]);
				} else if (
					(aim = World.character(x,y))
					&& Character.player !== World.character(x, y)
					&& !Player.isEnemy(aim)
				) {
					player.sendStartConversation(aim.characterId);
				} else if (
					(cache = World.object(x, y)) !== null
					&& StaticData.object(cache).type === StaticData.OBJECT_TYPE_DOOR
					&& (!isOpenDoor(cache) || e.shiftKey)
					&& isNear(Characters.player("x"), Characters.player("y"), x, y)
				) {
				// Open or close door
					performAction("useObject",[x, y]);
				} else if (World.object(x,y) && isNear(x, y, Characters.player("x"), Characters.player("y")) && isContainer(0)) {
					throw new Error("Not implemented!");
				// Open cotainer
					Global.container.x = x;
					Global.container.y = y;
					player.sendOpenContainer();
				} else if (
					World.passability(x, y) === StaticData.PASSABILITY_BLOCKED
					|| World.passability(x, y) === StaticData.PASSABILITY_SEE
				) {
				// Come to an object
					if (isNear(x, y, Characters.player("x"), Characters.player("y"))) {
						return false;
					}
					cache = Player.getComeToDest(x, y);
					performAction("goTo", [cache.x, cache.y]);
				} else {
					// If Player goes to cell
					Player.destX = x;
					Player.destY = y;
					performAction("goTo", [x, y]);
				}
			});
			element.addEventListener("contextmenu", function mouseHandlerContextmenu(e) {
				if (e.shiftKey) {
					return;
				}
				var elementCoord=getOffsetRect(gameField);
				var x=Math.floor((e.clientX-elementCoord.left)/32);
				var y=Math.floor((e.clientY-elementCoord.top)/32);
				
				mapCursorX = x;
				mapCursorY = y;
				
				return false;
			});
			element.addEventListener("mousemove", function mouseHandlerMove(e) {
				// Передвижение указателя клетки
				if (e.clientX==prevClientX && e.clientY==prevClientY) {
					return;
				}
				prevClientX = e.clientX;
				prevClientY = e.clientY;
				var elementCoord = getOffsetRect(GameField.getGameFieldElement());
				var normal = GameField.getNormalView(
						Math.floor((e.clientX-elementCoord.left)/32),
						Math.floor((e.clientY-elementCoord.top)/32));
				var x=normal.x;
				var y=normal.y;
				if (x==mapCursorX && y==mapCursorY) {
					return;
				}
				// Распрозрачивание горизонтального ряда объектов, закрывающих обзор
	//			for (var dy=1; !getObject(x,y+dy-1) && World.cells[x][y+dy]; dy++) {
	//				if (hiddenBotheringObjects.indexOf(getNum(x,y+dy))==-1) {
	//				// Если при движении мыши скрывается один и тот же ряд объектов, то ничего не делать, 
	//				// иначе распрозрачить предыдущие запрозраченные объекты
	//					var obj;
	//					for (var i=0;obj=getObject(
	//						getX(hiddenBotheringObjects[i]),
	//						getY(hiddenBotheringObjects[i])
	//					);i++) {
	//						if (obj.image) {
	//						// Если объект не скрылся от того, что персонаж двигается, и вместе с ним двигается камера
	//							if (!player.canSee(obj.x,obj.y)) {
	//								obj.shade();
	//							} else {
	//								obj.unshade();
	//							}
	//						}
	//					}
	//					hiddenBotheringObjects=[];
	//				}
	//			}
				mouseCursorAbsCellX = x;
				mouseCursorAbsCellY = y;
				mouseCursor.move(x,y);
	//			if (Player.spellId != -1) {
	//				cellCursorSec.move(x,y);
	//			} else {
	//				cellCursorPri.move(x,y);
	////				this.cellInfo.style.display="none";
	//				// var nCurrentCell=document.getElementById("cellCursorPri");
	//				// nCurrentCell.style.top=y*32+"px";
	//				// nCurrentCell.style.left=x*32+"px";
	//				// if (Player.seenCells[x][y]) {
	//					// nCurrentCell.style.borderColor="#ff0";
	//				// } else {
	//					// nCurrentCell.style.borderColor="#f00";
	//				// }
	//			}
				// Запрозрачивание горизонтального ряда объектов, закрывающих обзор
	//			var objUnderCursor=getObject(x,y);
	//			for (var dy=1;!getObject(x,y+dy-1) && World.cells[x][y+dy];dy++) {
	//				if (hiddenBotheringObjects.indexOf(getNum(x,y+dy))==-1 && player.seenCells[x][y] && (!objUnderCursor || objectProperties[objUnderCursor.type][2]==1)) {
	//				// Если при движении мыши скрывается один и тот же ряд объектов, то ничего не делать, 
	//				// иначе запрозрачить предыдущие распрозраченные объекты
	//					var objectLower=getObject(x,y+dy);
	//					if (objectLower && objectProperties[objectLower.type][1]>dy*32) {
	//					// Если есть мешающий объект и он закрывает своей высотой обзор
	//						// var obj=World.cells[mapCursorX][mapCursorY+1].wall || World.cells[mapCursorX][mapCursorY+1].object;
	//						// obj.hide();
	//						// obj.show();
	//						// if (!Player.canSee(mapCursorX,mapCursorY+1)) {
	//							// obj.shade();
	//						// }
	//						// От мешающего объекта идём влево. Если объект слева тоже мешающий, то продолжаем, иначе останавливаемся
	//						var leftestObject=objectLower;
	//						var i=x;
	//						while ((leftestObject=getObject(--i,y+dy)) &&  leftestObject.image && objectProperties[leftestObject.type][1]>32 && !objectProperties[leftestObject.type][2] && Player.seenCells[i][y] && (!getObject(i,y) || objectProperties[getObject(i,y).type][2]==1)) { 
	//						// Получаем объект слева так же, как и objectLower
	//						}
	//						// Теперь в i мы получили x-координату левейшего мешающего объекта.
	//						// Идём от этого объекта направо и скрываем все мешающие объекты
	//						var obj;
	//						while ((obj=getObject(++i,y+dy)) &&  obj.image && objectProperties[obj.type][1]>32 && !objectProperties[obj.type][2] && Player.seenCells[i][y] && (!getObject(i,y) || objectProperties[getObject(i,y).type][2]==1)) {
	//							try {
	//								obj.cursorShade();
	//								// obj.image.style.opacity="0.3";
	//							} catch (e) {
	//								
	//							}
	//							hiddenBotheringObjects.push(getNum(i,y+dy));
	//						}
	//					}
	//				}
	//			}
			});
			/**
			 * Returns the {@Link CellCursor} object that is used to display the
			 * mouse cursor.
			 * 
			 * @see CellCursor 
			 * @return {CellCursor}
			 */
			this.getMouseCursor = function() {
				return mouseCursor;
			};
		};
		Events.addListener("engineLoad", window, function() {
			window.MouseHandler = new MouseHandler(GameField.getGameFieldElement());
		});
	})();
	Events.addListener("connectionEstablished", window, function() {
		performAction("login",["1","1"]);
		setTimeout(function() {
			performAction("logInForCharacter", ["Alvoi"]);
		}, 30);
	});
};
(function() {
	var /** @type Events */ instance;
	/**
	 * @singleton
	 * @constructor
	 */
	function Events() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		instance = this;
		var events = {};
		var lastListenerId = 0; // Used to assign unique ids to event listeners
		var listenerIndexes = {}; // Links a listener id to its index in events[eventName] array
		// Default events
		/**
		 * Fires an event. It results in calling all the functions that were
		 * assigned to this event.
		 * 
		 * @param {string} name Name of event.
		 * @param {mixed} data Some custom data to pass to the functions.
		 */
		this.fire = function(eventName, data) {
			if (!(eventName in events)) {
				throw new Error("Attempting to fire an undefined event \""+eventName+"\"");
			}
			var listeners = events[eventName];
			for (var i=0; i<listeners.length; i++) {
				var e = listeners[i];
				e.func.apply(e.context, [data]);
			}
		};
		/**
		 * 
		 * @param {string} eventName
		 * @param {mixed} func May be any custom function or a string — a name
		 * of {@link PlayerAction}
		 * @param {object} context
		 * @returns {object} A unique object identifying this listener function,
		 * @see Events#removeListener
		 */
		this.addListener = function(eventName, context, func) {
			if (eventName in events) {
				listenerIndexes[lastListenerId] = events[eventName].length;
				events[eventName].push({func:func,context:context});
			} else {
				throw new Error("Attempting to add a listener to an undefined event \""+eventName+"\"");
			}
			return {n: eventName, i: lastListenerId++};
		};
		/**
		 * Remove a function added by {@link Events#addListener} that listens 
		 * for a certain event.
		 * @param {object} listenerDescriptor An object that is returned by
		 * {@link Events#addListener}
		 */
		this.removeListener = function(listenerDescriptor) {
			events[listenerDescriptor.n].splice([listenerIndexes[listenerDescriptor.i]], 1);
		};
		/**
		 * Makes a new event available for firing.
		 * @param {string} name
		 */
		this.registerEvent = function(name) {
			if (name in events) {
				throw new Error("Event \""+name+"\" is already registered!");
			}
			events[name] = [];
		};
		/**
		 * @inner
		 * @param {Array[string]} eventNames
		 */
		function init(eventNames) {
			for (var i=0,l=eventNames.length; i<l; i++) {
				instance.registerEvent(eventNames[i]);
			}
		}
		init([
			// Cell selection
			"exitCellSelectionMode",
			"enterCellSelectionMode",
			"chooseCell",
			"objectDisappear",
			"characterDisappear",
			"characterAppear",
			"playerDataLoaded",
			"locationLoad",
			"engineLoad",
			"connectionEstablished",
			"connectionAborted",
			"playerMove",
			"nextTurn",
			"chunkLoad",
			"chunkUnload",
			"environmentExplored"
		]);
	};
	window.Events = new Events();
})();
onLoadEvents['game'] = function _() {
	Graphics.buildImageCache();
	Keys.formReverseKeyCodesTable();
};
function performAction(actionName, args) {
	if (!(actionName in UI.registeredActions)) {
		throw new Error("No action "+actionName+" registered");
	}
	if (args === undefined) {
		args = [];
	}
	UI.registeredActions[actionName]._handler
		.apply(UI.registeredActions[actionName]._context, args);
}
function readCharacters(data) {
	/*
	in : [[
	0	characterId,
	1	x,
	2	y,
	3	name,
	4	fraction,
	5	maxHp,
	6	hp,
	7	maxMp,
	8	mp,
	9	effects,
	10	equipment,
		(cls,race)
		|(type)
		]xM],
	 */
}
 
