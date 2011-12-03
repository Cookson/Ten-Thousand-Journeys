onLoadEvents['gui'] = function _() {
	document.getElementById("intfWindowWrap").style.width = localStorage.getItem(3)+"px";
	document.getElementById("intfWindowWrap").style.height = localStorage.getItem(4)+"px";
	recountWindowSize();
	gameField = document.getElementById("gameField");
	gameField.cellInfo = document.getElementById("cellInfo");
	gameField.style.top = "0px";
	gameField.style.left = "0px";
	floorCanvas = document.getElementById("gameFieldFloor");
	// Отмена горячих клавиш, когда фокус передан одному из input'ов
	var nlInputs=document.getElementsByTagName("input");
	for (var i=0;i<nlInputs.length;i++) {
		nlInputs[i].onfocus=handlers['input'].focus;
		nlInputs[i].onblur=handlers['input'].blur;
	}
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
	
	// Слоты амуниции
	var invSlotNames=[
		"invWeaponImg", 
		"invShieldImg", 
		"invHeadgearImg", 
		"invBodyImg", 
		"invGlovesImg", 
		"invBootsImg", 
		"invCloakImg", 
		"invRing1Img", 
		"invRing2Img", 
		"invAmuletImg"
	];
	
	// Курсоры выбора клетки
	CellCursor.prototype.cursors.push(new CellCursor("cellCursorPri"));
	CellCursor.prototype.cursors.push(new CellCursor("cellCursorSec"));
	cellCursorPri=CellCursor.prototype.cursors[0];
	cellCursorSec=CellCursor.prototype.cursors[1];
	
	// Игровые окна
	windowContainer = new GameWindow("Container");
	
	// Build interface
	new UIElement(
		"iconsInventory", 
		UI.ALIGN_LEFT, 
		-30,
		[UI.notifiers.inventoryChange, UI.notifiers.locationLoad, UI.notifiers.worldLoad],
		UI.ALWAYS
	);
//	new UIElement(
//		"minimap", 
//		UI.ALIGN_LEFT, 
//		UI.ALIGN_TOP, 
//		[UI.notifiers.environmentChange, UI.notifiers.locationLoad],
//		UI.IN_LOCATION
//	);
	new UIWindow(
		"windowGameAlert", 
		UI.ALIGN_LEFT, 
		UI.ALIGN_TOP, 
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIWindow(
		"windowLogin", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_CENTER, 
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIWindow(
		"windowAccountCharacters", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_CENTER,
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIWindow(
		"windowSkills",
		UI.ALIGN_CENTER, 
		UI.ALIGN_CENTER,
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIWindow(
		"windowDeath", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_CENTER,
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIWindow(
		"windowSettings", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_CENTER,
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIWindow(
		"windowDialogue", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_CENTER,
		UI.ALWAYS,
		Keys.MODE_ALWAYS
	);
	new UIElement(
		"chat", 
		UI.ALIGN_LEFT, 
		UI.ALIGN_BOTTOM,
		UI.ALWAYS
	);
	new UIElement(
		"ammunition", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_BOTTOM,
		UI.ALWAYS
	);
	new UIElement(
		"iconsLoot", 
		UI.ALIGN_RIGHT, 
		UI.ALIGN_BOTTOM,
		UI.IN_LOCATION
	);
	new UIElement(
		"iconsSpells", 
		UI.ALIGN_CENTER, 
		UI.ALIGN_TOP,
		UI.IN_LOCATION
	);
	new UIElement(
		"textTitle", 
		UI.ALIGN_RIGHT, 
		UI.ALIGN_TOP,
		UI.ALWAYS
	);
	new UIElement(
		"hpBar", 
		UI.ALIGN_RIGHT, 
		UI.ALIGN_BOTTOM,
		UI.IN_LOCATION
	);	
};
// Классы интерфейса
function CellCursor(id) {
// Класс контролирует отображение различных курсоров - квадратных прямоугольников, которыми выделяется активная клетка
// Всего в игре два курсора: стандартный и для дальних атак/заклинаний
// Все объекты курсоров хранятся в прототипе класса курсоров в массиве cursors
	this.x=-1;
	this.y=-1;
	this.id=id;
	this.character=false;
	this.init(id);
}
CellCursor.prototype.cursors=[];
CellCursor.prototype.move=function(x,y) {
	mapCursorX=x;
	mapCursorY=y;
	this.elem.style.left=x*32+"px";
	this.elem.style.top=y*32+"px";
	this.x=x;
	this.y=y;
};
CellCursor.prototype.hide=function() {
	this.elem.style.display="none";
};
CellCursor.prototype.show=function() {
	this.elem.style.display="block";
};
CellCursor.prototype.init=function(id) {
	this.elem=document.createElement("div");
	this.elem.className="wrap";
	this.elem.style.zIndex="2";
	var nBg=document.createElement("div");
	this.elem.appendChild(nBg);
	this.elem.id=id;
	gameField.appendChild(this.elem);
	this.character=false;
	this.move(-1,-1); // Убираем курсор за границы видимости, чтобы не висел в углу изначально
};
CellCursor.prototype.invoke=function(x,y) {
	if (x==undefined) {
		if (this.character) {
			x=this.character.x;
			y=this.character.y;
		} else {
			x=mapCursorX || player.x;
			y=mapCursorY || player.y;
		}
	}
	Keys.setMode(Keys.MODE_CURSOR_ACTION);
	this.move(x,y);
	this.show();
};
CellCursor.prototype.withdraw=function() {
	this.hide();
	Keys.setMode(Keys.MODE_DEFAULT);
};
CellCursor.prototype.useCursor=function(cursorObjName) {
	for (var i in CellCursor.prototype.cursors) {
		if (CellCursor.prototype.cursors[i].id!=cursorObjName) {
			CellCursor.prototype.cursors[i].hide();
		} else {
			CellCursor.prototype.cursors[i].show();
			CellCursor.prototype.cursors[i].move(mapCursorX,mapCursorY);
		}
	}
};
CellCursor.prototype.hideAllCursors=function() {
	for (var i in CellCursor.prototype.cursors) {
		CellCursor.prototype.cursors[i].hide();
	}
};
function GameWindow(type) {
// Класс окна.
// styleObject - объект-аргумент для метода jQuery.css
// Конструктор класса вызывает метод, определяемый аргументом type, который создаёт содержимое окна.
// Затем конструктор оборачивает созданное окно в div.wrap, после чего окно отображается
// В вызываемом методе, формирующем окно, обязательно должен создаваться элемент this.nCloseButton
/*
	События:
		onHide - при скрытии окна
		onSHow - при показе окна
		onClose - при нажатии на кнопку закрытия окна (при этом окно не обязательно должно скрываться)
*/
	this.windowId = gameWindows.length;
	this.type = type;
	var wind = this; // Эта переменная замыкается на обработчик клика по кнопке закрытия
	gameWindows[this.windowId]=this;
	this.element = document.createElement("div");
	this.element.className = "gameWindow";
	this.element.id = "window"+this.windowId;
	this.centered = false;
	this.wrap = document.createElement("div");
	this.wrap.className = "wrap";
	this.wrap.style.display = "none"; // Изначально окно создаётся не развёрнутым
	this.wrap.appendChild(this.element);
	this.visible = false;
	this["window"+type](); // Создать содержимое окна
	// Создать обёртку и поместить в неё элемент окна, а саму обёртку - в главное игровое окно
	var nLeftSide=document.getElementById("intfLeftSide");
	nLeftSide.insertBefore(this.wrap, nLeftSide.children[0]);
	// Кнопка закрытия окна
	if (this.nCloseButton) {
		this.nCloseButton.onclick=function() {
			wind.hide();
			wind.close();
		};
	}
	if (this.element.style.length==0) {
	// Если вид окна не задан, задать вид по умолчанию и отцентрировать
		this.applyStyle({
			backgroundColor:"#333",
			width:"400px",
			height:"300px",
			border:"2px solid #888"
		});
		this.center();
	}
}
GameWindow.prototype.center = function() {
// Выровнять окно по центру экрана (горизонтально и вертикально)
	this.element.style.left = (UI.width/2-parseInt(this.element.style.width)/2)+"px";
	this.element.style.top = (UI.height/2-parseInt(this.element.style.height)/2)+"px";
	this.centered = true;
};
GameWindow.prototype.applyStyle=function(style) {
// Задать вид окна
// style - объект, из ключей и значений которого формируется CSS-стиль элемента this.element
	this.style=style;
	for (var property in style) {
		this.element.style[property]=style[property];
	}
};
GameWindow.prototype.hide=function() {
	this.visible = false;
	this.onHide && this.onHide();
	this.wrap.style.display="none";
};
GameWindow.prototype.show=function() {
	this.visible = true;
	this.onShow && this.onShow();
	this.wrap.style.display="block";
};
GameWindow.prototype.remove=function() {
	this.wrap.parentNode.removeChild(this.wrap);
	delete gameWindows[this.windowId];
};
GameWindow.prototype.close=function() {
	this.onClose && this.onClose();	
};

GameWindow.prototype.windowContainer=function() {
// Контейнер (содержимое сундука)
	this.items = [];
	this.x = -1;
	this.y = -1;
	this.showItems=function(itms) {
	// Show items in container's window
	// Format: [itemId, amount, itemId, amount ...]
		
		if (itms!==undefined) {
			this.items = [];
			for (var i=0;i<itms.length/2;i++) {
				this.items[itms[i*2]]=itms[i*2+1];
			}
		}
		var nlItems=this.element.getElementsByClassName("containerItem");
		while (nlItems.length>0) {
			nlItems[0].parentNode.removeChild(nlItems[0]);
		}
		for (var itemId in this.items) {
		// Создать обёртку
			var nSlot=document.createElement("div");
			nSlot.className="containerItem";
			nSlot.setAttribute("typeId",itemId);
			nSlot.setAttribute("itemNum",this.items[itemId]);
			nSlot.onclick=handlers.containerItem.click;
			if (this.items[itemId]>1) { 
				// Количество предметов ещё в одной обёртке и изображение
				var nWrap=document.createElement("div");
				nWrap.className="wrap";
				var nNum=document.createElement("div");
				nNum.className="containerItemNum";
				nNum.innerHTML=this.items[itemId];
			}
			var nImg=document.createElement("img");
			nImg.setAttribute("src","./images/items/"+itemId+".png");
			if (this.items[itemId]>1) { 
				nWrap.appendChild(nNum);
				nSlot.appendChild(nWrap);
			}
			nSlot.appendChild(nImg);
			this.nItems.appendChild(nSlot);
		}
	};
	this.removeItem = function(itemId, amount) {
		if (!this.items[itemId]) {
			throw new Error("An attempt to remove items from container that this container haven't");
		}
		if ((this.items[itemId] -= amount) == 0) {
			delete this.items[itemId];
		}		
	};
	this.addItem = function(itemId, amount) {
		if (this.items[itemId]) {
			this.items[itemId] += amount;
		} else {
			this.items[itemId] = amount;
		}
	};
	this.onClose = function() {
		this.x = -1;
		this.y = -1;
	};
	this.nItems=document.createElement("div");
	this.nItems.className="containerItems";
	this.element.appendChild(this.nItems);
	
	this.showItems();
	
	// Кнопка закрытия
	this.nCloseButton=document.createElement("div");
	this.nCloseButton.className="containerClose";
	this.nCloseButton.innerHTML="Закрыть";
	this.nCloseButton.setAttribute("window",this.windowId);
	this.element.appendChild(this.nCloseButton);
	this.center();
}; 

// Various UI functions

function hideMenu() {
	document.getElementById("startHelpWrapper").style.display="none";
}
function showMenu() {
	document.getElementById("startHelpWrapper").style.display="block";
}
function showStChooseServer() {
	var nlStartScreenMenus=document.getElementById("startScreen").children;
	for (var i=0;i<nlStartScreenMenus.length;i++) {
		nlStartScreenMenus[i].style.display="none";
	}
	document.getElementById("stScrMenu").style.display="inline-block";
}
function showStConnection() {
	var nlStartScreenMenus=document.getElementById("startScreen").children;
	for (var i=0;i<nlStartScreenMenus.length;i++) {
		nlStartScreenMenus[i].style.display="none";
	}
	document.getElementById("stServer").style.display="inline-block";
	document.getElementById("stServerLogin").focus();
}

function showStLoad() {
	if (!inMenu) {
		showMenu();
	}
	var nlStartScreenMenus=document.getElementById("startScreen").children;
	for (var i=0;i<nlStartScreenMenus.length;i++) {
		nlStartScreenMenus[i].style.display="none";
	}
	document.getElementById("stLoad").style.display="inline-block";
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
function showSkillPointsLeft() {
	document.getElementById("stNewPlayerSkillPointLeft").innerHTML="Очки навыков: "+(maxSkillPoints-getUsedSkillPoints());
}
function winkElement(elem,text) {
	elem.style.opacity="0";
	elem.innerHTML=text || elem.innerHTML;
	qoanimate(elem,1,300);
}

function getAvailableClassNames() {
	skillNamesUserLanguage=["Булавы", "Топоры", "Щиты", "Мечи", "Древковое оружие", "Скрытность", "Реакция", "Луки", "Кинжалы", "Рукопашный бой", "Посох", "Кинезис", "Огонь", "Холод", "Некрос", "Демонология", "Ментал", "Магические предметы", "Ремесло", "Ловушки", "Взлом", "Механика", "Алхимия"];
	var names=["Путешественник"];
	var hasSkill=function(skill) {
		return playerCreation.skills[skill]!==undefined;
	};
	if (hasSkill(0) || hasSkill(1) || hasSkill(3) || hasSkill(4)) {
		names.push("Воин");
		names.push("Воитель");
		names.push("Рыцарь");
	}
	if ((hasSkill(0) || hasSkill(1) || hasSkill(3) || hasSkill(4)) && hasSkill(17)) {
		names.push("Паладин");
	}
	if (hasSkill(3)) {
		names.push("Мечник");
	}
	if (hasSkill(7)) {
		names.push("Лучник");
	}
	if (hasSkill(5) || hasSkill(20)) {
		names.push("Вор");
	}
	if (hasSkill(5) && hasSkill(6)) {
		names.push("Шпион");
	}
	if (hasSkill(21)) {
		names.push("Механик");
	}
	if (hasSkill(22)) {
		names.push("Алхимик");
	}
	if (hasSkill(9) || hasSkill(10)) {
		names.push("Монах");
	}
	if (hasSkill(11) || hasSkill(12) || hasSkill(13) || hasSkill(14) || hasSkill(15) || hasSkill(16)) {
		names.push("Маг");
		names.push("Волшебник");
		names.push("Чародей");
	}
	if (hasSkill(12)) {
		names.push("Маг огня");
	}
	if (hasSkill(13)) {
		names.push("Маг холода");
	}
	if (hasSkill(14)) {
		names.push("Некромант");
	}
	if (hasSkill(15)) {
		names.push("Демонолог");
	}
	if (hasSkill(16)) {
		names.push("Псионик");
	}
	if (hasSkill(14) || hasSkill(15)) {
		names.push("Тёмный маг");
	}
	if (hasSkill(14) && (hasSkill(0) || hasSkill(1) || hasSkill(3) || hasSkill(4))) {
		names.push("Рыцарь смерти");
	}
	return names;
}
function showClasses() {
// Отобразить названия доступных классов
	var classNames=getAvailableClassNames();
	// Очистить список классов
	var nStNewPlayerClasses=document.getElementById("stNewPlayerClasses");
	while (nStNewPlayerClasses.children.length>0) {
		nStNewPlayerClasses.removeChild(nStNewPlayerClasses.children[0]);
	}
	for (var i=0;i<classNames.length;i++) {
	// Заполнить новый список классов
		var nClass=document.createElement("div");
		nClass.className="stNewPlayerClass";
		nClass.innerHTML=classNames[i];
		nClass.onclick=handlers.stNewPlayerClass.click;
		document.getElementById("stNewPlayerClasses").appendChild(nClass);
	}
	if (classNames.indexOf(newPlayerClass)!=-1) {
	// Если названия классов заново отобразились, и среди них есть текущее название класса, то имитировать выбор этого класса
		var nlClassNames=document
			.getElementById("stNewPlayerClasses")
			.getElementsByClassName("stNewPlayerClass");
		for (var i=0;i<nlClassNames.length;i++) {
			if (nlClassNames[i].innerHTML==newPlayerClass) {
				nlClassNames[i].onclick();
			}
		}
	}
}
function gAlert(text,autoCloseOff,callback) {
// Отобразить сообщение в углу игрового поля
// autoCloseOff: если true, то окно не закроется само
// callback: функция. Вызывается после закрытия окна, если autoCloseOff==true
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
function fitIntfInfo() {
//	var a=document.getElementById("infoTitle").clientHeight;
//	var b=document.getElementById("infoBars").clientHeight;
//	var c=document.getElementById("infoAttributes").clientHeight;
//	var d=document.getElementById("infoInventory").clientHeight;
//	var e=document.getElementById("infoSpells").clientHeight;
//	var f=document.getElementById("infoChat").clientHeight;
//	var g=document.getElementById("infoInvite").clientHeight;
//	var h=document.getElementById("infoEffects").clientHeight;
//	var i=document.getElementById("minimap").clientHeight;
//	if (onGlobalMap) {
//		var invHeight=0;
//		var nInfoInventory=document.getElementById("infoInventory");
//		for (var i=0;i<nInfoInventory.children.length;i++) {
//		// Формируем высоту #infoInventory (т.к. его высота динамическая)
//			invHeight+=nInfoInventory.children[i].clientHeight;
//		}
//		nInfoInventory.style.height=invHeight+"px";
//		document.getElementById("infoOnline").style.height=(localStorage.getItem(4)-a-b-c-d-e-f-g-h)+"px";
//	} else {
//		document.getElementById("infoInventory").style.height=(localStorage.getItem(4)-a-b-c-e-f-g-h-i)+"px";
//	}	
}
function restorePlayerCreationScreen() {
// Восстановить окно создания персонажа к первоначальному состоянию, если оно изменено
	// Имя
	document.getElementById("stNewPlayerName").value="";
	// Раса
	delete playerCreation.race;
	delete playerCreation.cls;
	delete playerCreation.name;
	for (var i in playerCreation.skills) {
		delete playerCreation.skills[i];
	}
	var nlRaces=document
				.getElementById("stNewPlayerRaces")
				.children;
			for (var i=0;i<nlRaces.length;i++) {
				nlRaces[i].className="";
			}
	// Атрибуты
	var nlAttributes=document
		.getElementById("stNewPlayerAttributes")
		.getElementsByClassName("stNewPlayerAttributeValue");
	for (var i=0;i<nlAttributes.length;i++) {
		nlAttributes[i].innerHTML="";
	}
	// Класс
	newPlayerClass="";
	var nlClasses=document.getElementById("stNewPlayerClasses").getElementsByTagName("*");
	while (nlClasses.length>0) {
		nlClasses[0].parentNode.removeChild(nlClasses[0]);
	}
	// Навыки
	var nStNewPlayerLearnedSkills=document.getElementById("stNewPlayerLearnedSkills");
	while (nStNewPlayerLearnedSkills.children.length>0) {
		nStNewPlayerLearnedSkills.removeChild(nStNewPlayerLearnedSkills.children[0]);
	}
}
// Доступ к интерфейсу с клавиатуры
function useNormalKeysMode() {
// Выйти из текущего режима работы клавиш, убрать все дополнительные обозначения.
	hideRightPanelKeys();
	keysMode=0;
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
		for (var i in matrix[player.x][player.y].items) {
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
function hideRightPanelKeys() {
// Убрать все дополнительные обозначения для клавиш на правой панели
	var len=keysCharElements.length;
	for (var i=0;i<len;i++) {
		keysCharElements[i].parentNode.removeChild(keysCharElements[i]);
	}
	keysCharElements=[];
}
