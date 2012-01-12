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
	
	// События мышки на игровом поле
	gameField.oncontextmenu = handlers['gameField'].contextmenu;
	gameField.onclick = handlers['gameField'].click;
	gameField.onmouseover = handlers['gameField'].mouseover;
	gameField.onmouseout = handlers['gameField'].mouseout;
	gameField.onmousemove = handlers['gameField'].mousemove;
	gameField.cellInfo.onmouseover = handlers['cellInfo'].mouseover;
		
	/*
	 * // Выбор расы
	var nlRaces=nStNewPlayer.getElementsByClassName("stNewPlayerRace");
	for (var i=0;i<nlRaces.length;i++) {
		nlRaces[i].onclick=handlers.stNewPlayerRace.click;
		nlRaces[i].setAttribute("race",i);
	}
	document
		.getElementById("stNewPlayerComplete")
		.onclick=handlers.stNewPlayerComplete.click;
		// Атрибуты
	var nlAttributes=nStNewPlayer.getElementsByClassName("stNewPlayerAttributeValue");
	for (var i=0;i<nlAttributes.length;i++) {
		nlAttributes[i].setAttribute("attribute",i);
	}
		// Изучение навыка
	var nlSkills=nStNewPlayer.getElementsByClassName("stNewPlayerSkill");
	for (var i=0;i<nlSkills.length;i++) {
		nlSkills[i].onclick=handlers.stNewPlayerSkill.click;
		nlSkills[i].setAttribute("skill",i);
	}
		// Выбор класса
	var nlClasses=nStNewPlayer.getElementsByClassName("stNewPlayerClass");
	for (var i=0;i<nlClasses.length;i++) {
		nlClasses[i].onclick=handlers.stNewPlayerClass.click;
	}
		// Выбор навыков
	var nlSkills=nStNewPlayer.getElementsByClassName("stNewPlayerLearnedSkill");
	for (var i=0;i<nlSkills.length;i++) {
		nlSkills[i].onclick=handlers.stNewPlayerLearnedSkill.click;
	}
	*/
	
	// Курсоры выбора клетки
	CellCursor.init();
	
	// Build interface
	
	
	handlers.initInterface();
	
};
// Классы интерфейса
var CellCursor = {
// Класс контролирует отображение различных курсоров - квадратных прямоугольников, которыми выделяется активная клетка
// Всего в игре два курсора: стандартный и для дальних атак/заклинаний
	x:-1,
	y:-1,
	callback: null,
	context: null,
	zoneCenter: null,
	isSelectionMode: false,
	maximumDistance: 9000,
	shadedCells: [],
	move: function _(x,y) {
		if (this.isSelectionMode && Math.floor(distance(this.zoneCenter.x,this.zoneCenter.y,x,y))>this.maximumDistance) {
			return;
		}
		var viewIndent = Terrain.getViewIndentation(x,y,1);
		
		mapCursorX=x;
		mapCursorY=y;
		this.elem.style.left=viewIndent.left*32+"px";
		this.elem.style.top=viewIndent.top*32+"px";
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
		this.bg.className = "cellCursor"+className;
	},
	enterSelectionMode: function _(callback, context, maximumDistance, zoneCenter) {
	/**
	 * Enters cell selection mode.
	 * Exit from selection mode and callback call
	 * are in handlers object.
	 * 
	 * Also shades cells if needed, so player can see the 
	 * maximum range of his action.
	 * 
	 * callback - function to call after cell was chosen
	 * context - context of that function (or leave undefined 
	 * 		so the window object will be the context)
	 * maximumDistance - radius of selection area (or leave 
	 * 		undefined so distance will be unlimited)
	 * zoneCenter - object with fields object.x and object.y; 
	 * 		this may be a Character, GameObject or simply {x:int,y:int}.
	 * 		Area of applicable cells will be made around th zoneCenter
	 * 		(or leave zone center undefined so it will be the player)
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
//				console.log(x,y)
				if (player.visibleCells[x][y] && Math.floor(distance(zoneCenter.x,zoneCenter.y,x,y)) > maximumDistance) {
					this.shadedCells.push(Terrain.cells[x][y]);
					Terrain.cells[x][y].shade();
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
	}
};

function showStConnection() {
	var nlStartScreenMenus=document.getElementById("startScreen").children;
	for (var i=0;i<nlStartScreenMenus.length;i++) {
		nlStartScreenMenus[i].style.display="none";
	}
	document.getElementById("stServer").style.display="inline-block";
	document.getElementById("stServerLogin").focus();
}

function showLoadingScreen() {
	document.getElementById("stLoad").style.display="block";
}
function hideLoadingScreen() {
	document.getElementById("stLoad").style.display="none";
}

function addLearnedSkill(skill) {
// Выучить навык
	// Добавить навык в глобальную переменную
	if (playerCreation.skills[skill] === undefined) {
		playerCreation.skills[skill] = 1;
	} else {
		playerCreation.skills[skill]++;
	}
	// Очистить список навыков
	var nStNewPlayerLearnedSkills=document.getElementById("stNewPlayerLearnedSkills");
	while (nStNewPlayerLearnedSkills.children.length>0) {
		nStNewPlayerLearnedSkills.removeChild(nStNewPlayerLearnedSkills.children[0]);
	}
	// Сформировать новый список навыков
	for (var i in playerCreation.skills) {
		var nNewSkill=document.createElement("div");
		nNewSkill.innerHTML=skillNamesUserLanguage[i]+" "+playerCreation.skills[i];
		nNewSkill.setAttribute("skill",i);
		nNewSkill.onclick=handlers.stNewPlayerLearnedSkill.click;
		document.getElementById("stNewPlayerLearnedSkills").appendChild(nNewSkill);
	}
}
function removeLearnedSkill(skill) {
	// Удалить навык из массива в глобальной переменной
	if (playerCreation.skills[skill]==1) {
		delete playerCreation.skills[skill];
	} else {
		playerCreation.skills[skill]--;
	}
	// Очистить список навыков
	var nStNewPlayerLearnedSkills=document.getElementById("stNewPlayerLearnedSkills");
	while (nStNewPlayerLearnedSkills.children.length>0) {
		nStNewPlayerLearnedSkills.removeChild(nStNewPlayerLearnedSkills.children[0]);
	}
	// Сформировать новый список навыков
	for (var i in playerCreation.skills) {
		var nNewSkill=document.createElement("div");
		nNewSkill.innerHTML=skillNamesUserLanguage[i]+" "+playerCreation.skills[i];
		nNewSkill.setAttribute("skill",i);
		nNewSkill.onclick=handlers.stNewPlayerLearnedSkill.click;
		document.getElementById("stNewPlayerLearnedSkills").appendChild(nNewSkill);
	}
}
function getUsedSkillPoints() {
// Получить количество использованных очков навыков при создании персонажа
	var usedSkillPoints=0;
	for (var i in playerCreation.skills) {
		usedSkillPoints+=playerCreation.skills[i]*baseSkillPoints;
		for (var j=1;j<playerCreation.skills[i];j++) {
			usedSkillPoints+=deltaSkillPoints*j;
		}
	}
	return usedSkillPoints;
}
function winkElement(elem,text) {
	elem.style.opacity="0";
	elem.innerHTML=text || elem.innerHTML;
	qoanimate(elem,1,300);
}
function gAlert(text,autoCloseOff,callback) {
// Отобразить сообщение в углу игрового поля
// autoCloseOff: если true, то окно не закроется само
// callback: функция. Вызывается после закрытия окна, если autoCloseOff==true
	throw new Error("GALERT "+text);
	if (windowGAlert.timeout!==null) {
		clearTimeout(windowGAlert.timeout);
	}
	windowGAlert.nText.innerHTML=text;
	windowGAlert.show();
	if (!autoCloseOff) {
		windowGAlert.timeout=setTimeout(function() {
			windowGAlert.hide();
			windowGAlert.timeout=null;
		},Math.max(text.length*40,1200));
	} else if (callback) {
		windowGAlert.wrap.onclick=function() {
			callback();
			if (windowGAlert.wrap.onclick==arguments.callee) {
			// Если callback не изменил onclick кнопки. 
			// Учитывая это условие, можно строить цепочки из вложенных в колбэки друг друга gAlert'ов
				windowGAlert.wrap.onclick=handlers.intfAlertOk.click;
			}
		};
	}
}

function showRightPanelKeys(mode) {
// Показать горячие клавиши для предметов и заклинаний на правой панели
/* То, к чему показываются клавиши, определяется аргументом-модификатором:
	1: рюкзак
	2: амуниция
	3: заклинания
	4: лут
 */
	keysMode=mode;
	if (mode==1) {
		var len=player.items.length;
		var rootElemId="invItems";
	} else if (mode==2) {
		var len=10; // Слотов с амуницией всегда одинаковое количество
		var rootElemId="invAmmunition";
	} else if (mode==3) {
		var len=player.spells.length;
		var rootElemId="spellsList";
	} else if (mode==4) {
		var len=0;
		for (var i in Terrain.cells[player.x][player.y].items) {
		// Находим количество предметов в клетке на полу
			len++;
		}
		var rootElemId="invLoot";
	}
	var nlItems=document.getElementById(rootElemId).children;
	keysCharElements=[];
	for (var i=0;i<len && nlItems[i].itemId!=-1;i++) {
	// Проставляем элементы с буквами 
		var nWrap=document.createElement("div");
		nWrap.className="wrap";
		nWrap.style.zIndex="2";
		var nChar=document.createElement("div");
		nChar.className="infoChar";
		nChar.innerHTML=(mode==3) ? spellsKeyChars[i] : keyChars[i];
		nWrap.appendChild(nChar);
		keysCharElements.push(nWrap);
		nlItems[i].insertBefore(nWrap,nlItems[i].children[0]);
	}
}