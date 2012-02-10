/** @class */
var Global = {
	container: {
		items: new ItemSet(),
		x: -1,
		y: -1
	},
	skillNames: ["mace", "axe", "shield", "sword", "polearm", "stealth", "reaction", "bow", "dagger", "unarmed", "staff", "kinesis", "fire", "cold", "necros", "demonology", "mental", "magicItems", "craft", "traps", "effraction", "mechanics", "alchemy"],
	skillNamesUserLanguage: ["Булавы", "Топоры", "Щиты", "Мечи", "Древковое оружие", "Скрытность", "Реакция", "Луки", "Кинжалы", "Рукопашный бой", "Посох", "Кинезис", "Огонь", "Холод", "Некрос", "Демонология", "Ментал", "Магические предметы", "Ремесло", "Ловушки", "Взлом", "Механика", "Алхимия"],
	skillsStr: ["Булавы", "Топоры", "Щиты", "Мечи", "Древковое оружие"],
	skillsDex: ["Скрытность", "Реакция", "Луки", "Кинжалы", "Рукопашный бой", "Посох"],
	skillsWis: ["Кинезис", "Огонь", "Холод", "Некрос", "Демонология", "Ментал"],
	skillsItl: ["Магические предметы", "Ремесло", "Ловушки", "Взлом", "Механика", "Алхимия"],
	
	races: ["человек","эльф","дварф","орк","кобайянин"],
	attributes: ["Сила", "Ловкость", "Мудрость", "Интеллект"],
	classNames: ["Путешественник","Воин","Воитель","Рыцарь","Паладин","Мечник","Лучник","Вор","Шпион","Механик",
		           "Алхимик","Монах","Маг","Волшебник","Чародей","Маг огня","Маг холода","Некромант","Демонолог",
		           "Псионик","Тёмный маг","Рыцарь смерти"],
	racialAttributes: [
	/* Базовые расовые атрибуты 
		Индекс:
			0 - человек
			1 - эльф
			2 - дварф
			3 - орк
		Значение: массив [str,dex,wis,itl]
	*/
		[10,10,10,10],
		[6,15,12,10],
		[11,7,8,13],
		[18,11,8,6],
		[8,11,15,15]
	],
	playerLogin: null,
	playerPassword: null	
};
/** @class */
function Terrain(){
// Passability constants
	/** @public @type number */
	this.PASS_FREE    = -1;
	/** @public @type number */
	this.PASS_BLOCKED =  1;
	/** @public @type number */
	this.PASS_SEE     =  3;
	/** @public @type number */
	this.CHUNK_WIDTH  = 20;
	/** @private @type Ceiling[] */
	this.ceilings = [];
	/** @public @type number */
	this.cameraOrientation = Side.N;
	this.cssSideX = "left";
	this.cssSideY = "top";
	/** @private @type HashMap */
	this.chunks = new HashMap();
}
Terrain.prototype.getViewIndentation = function (x, y, scale) {
	if (this.cameraOrientation == Side.N) {
		return {left: x*scale, top: y*scale};
	} else if (this.cameraOrientation == Side.E) {
		return {left: (Terrain.height-y-1)*scale, top: x*scale};
	} else if (this.cameraOrientation == Side.S) {
		return {left: (Terrain.width-x-1)*scale, top: (Terrain.height-y-1)*scale};
	} else if (this.cameraOrientation == Side.W) {
		return {left: y*scale, top: (width-x-1)*scale};
	} else {
		throw new Error("Unknown camera orientation: "+this.cameraOrientation);
	}
};
Terrain.prototype.getNormalView = function (x, y) {
	if (this.cameraOrientation == Side.N) {
		return {x: x, y: y};
	} else if (this.cameraOrientation == Side.E) {
		return {x: y, y: (Terrain.height-x-1)};
	} else if (this.cameraOrientation == Side.S) {
		return {x: (Terrain.width-x-1), y: (Terrain.height-y-1)};
	} else if (this.cameraOrientation == Side.W) {
		return {x: (Terrain.width-y-1), y: x};
	} else {
		throw new Error("Unknown camera orientation: "+this.cameraOrientation);
	}
};
Terrain.prototype.isOrientationVertical = function () {
	return this.cameraOrientation == Side.N || this.cameraOrientation == Side.S;
};
Terrain.prototype.getHorizontalDimension = function _() {
	if (this.cameraOrientation == Side.N || this.cameraOrientation == Side.S) {
		return Terrain.width;
	} else {
		return Terrain.height;
	}
};
Terrain.prototype.getVerticalDimension = function _() {
	if (this.cameraOrientation == Side.N || this.cameraOrientation == Side.S) {
		return Terrain.height;
	} else {
		return Terrain.width;
	}
};
/**
 * Returns a cell by its absolute coordinates.
 * 
 * @param {number} x
 * @param {number} y
 * @return {Cell}
 */
Terrain.prototype.getCell = function(x, y) {
	if (!this.getChunkWithCell(x,y)) {
		return null;
	}
	return this.getChunkWithCell(x,y).getAbsoluteCell(x,y);
};
Terrain.prototype.getChunkRoundedCoord = function(coord) {
	return (coord < 0) ? coord-((coord%this.CHUNK_WIDTH==0) ? 0 : this.CHUNK_WIDTH)-coord%this.CHUNK_WIDTH : coord-coord%this.CHUNK_WIDTH;
};
/**
 * 
 * @param x
 * @param y
 * @return {Chunk} A chunk containing cell x:y.
 */
Terrain.prototype.getChunkWithCell = function (x, y) {
	return this.getChunkByCoord(
		this.getChunkRoundedCoord(x), 
		this.getChunkRoundedCoord(y)
	);
};
/**
 * 
 * @param x
 * @param y
 * @return {Chunk} A chunk containing cell x:y.
 */
Terrain.prototype.getChunkByCoord = function _(x, y) {
	if (this.chunks.containsKey(x)) {
		if (this.chunks.get(x).containsKey(y)) {
			return this.chunks.get(x).get(y);
		}
		return null;
//			throw new Error("No chunk "+x+" "+y);
	}
	return null;
//		throw new Error("No chunk column "+x);
};
/**
 * Creates a new chunk of terrain where you can put loaded objects. Do not use 
 * new Chunk() directly to create chunks — those are only containers!
 * 
 * @see Terrain#PASS_FREE
 * @param {number} x
 * @param {number} y
 * @param {number} type Type of object
 * @return {Chunk}
 */
Terrain.prototype.createChunk = function(x, y, data) {
	var column;
	if (!this.chunks.containsKey(x)) {
		column = this.chunks.put(x, new HashMap());
	} else {
		column = this.chunks.get(x);
	}
	return column.put(y, new Chunk(x, y, data));
};
/**
 * Sets passability value for a cell. 
 * 
 * @see Terrain#PASS_FREE
 * @param {number} x
 * @param {number} y
 * @param {number} type Type of object
 */
Terrain.prototype.setPassability = function(x, y, value) {
	this.getChunkWithCell(x, y).getAbsoluteCell(x,y).passability = value;
};
/**
 * Creates an object . Do not use new GameObject() directly to create objects —
 * those are only containers!
 * 
 * @param {number} x
 * @param {number} y
 * @param {number} type Type of object
 */
Terrain.prototype.createObject = function(x, y, type) {
	var cell = this.getCell(x, y);
	if (isWall(type)) {
	// Wall
		cell.object = cell.wall = new Wall(x, y, type);
	} else if (isDoor(type)) {
	// Door
		cell.object = new Door(x, y, type);
	} else {
	// Common object
		cell.object = new GameObject(x, y, type);
	}
	Terrain.setPassability(x, y, objectProperties[type][2]);
};
Terrain.prototype.displayObject = function(x, y) {
	this.getCell(x, y).object.display(x, y);
};
Terrain.prototype.showObject = function(x,y) {
	this.getCell(x,y).object.show(x,y);
};
Terrain.prototype.removeObject = function(x, y) {
	var cell = this.getCell(x, y);
	cell.object.remove();
	cell.object = null;
	this.setPassability(x, y, this.PASS_FREE);
};
Terrain.prototype.shadeCell = function(x,y) {
	var chunk = this.getChunkWithCell(x, y);
	chunk.getAbsoluteCell(x,y).shade(chunk,x,y);
};
Terrain.prototype.unshadeCell = function(x,y) {
	var chunk = this.getChunkWithCell(x, y);
	chunk.getAbsoluteCell(x,y).unshade(chunk,x,y);
};
/**
 * Creates a view of an item or item pile lying on the floor, or just adds 
 * items to a corresponding ItemPile if there is one in cell x:y.
 * 
 * @param {number} x
 * @param {number} y
 * @param {UniqueItem|ItemPile} item
 */
Terrain.prototype.createItem = function(x,y,item) {
	var cell = this.getCell(x, y);
	if (cell.items === null) {
		cell.items = new ItemSet();
	}
	cell.addItem(x,y,item);
};
/**
 * Removes a view of an item or item pile lying on the floor, or just removes 
 * items from a corresponding ItemPile if there is one in cell x:y.
 * 
 * @param {number} x
 * @param {number} y
 * @param {UniqueItem|ItemPile} item
 */
Terrain.prototype.removeItem = function(x,y,item) {
	var cell = this.getCell(x, y);
	if (cell.items === null) {
		throw new Error("Cannot remove item "+item+": cell "+x+":"+y+" has no items");
	}
	cell.items.remove(item);
};
/**
 * 
 */
Terrain.prototype.getItemsOnCell = function(x,y) {
	var cell = this.getCell(x, y);
	if (cell.items === null) {
		return [];
	}
	return cell.items.getValues();
};
/**
 * Checks whether chunk in certain cell already exists or not.
 * 
 * @param x
 * @param y
 * @return {boolean}
 */
Terrain.prototype.chunkExists = function(x, y) {
	return this.chunks.containsKey(x) && this.chunks.get(x).containsKey(y);
};
/**
 * Remove a chunk and undisplay its contents
 * @param x
 * @param y
 */
Terrain.prototype.removeChunk = function(x,y) {
	if (!this.chunkExists(x, y)) {
		throw new Error("Trying to remove chunk "+x+":"+y+" that does not exist");
	}
	var chunk = this.getChunkByCoord(x, y);
	for (var i=0; i<this.CHUNK_WIDTH; i++) {
	// Remove objects
		for (var j=0; j<this.CHUNK_WIDTH; j++) {
			if (chunk.cells[i][j].object !== null) {
				chunk.cells[i][j].object.remove(chunk.x+i, chunk.y+j);
			}
		}
	}
	// Remove canvas
	chunk.canvas.parentNode.removeChild(chunk.canvas);
	this.chunks.get(x).remove(y);
};


var Terrain = new Terrain();
// Переменные соединения
var servers=[[]]; // Список всех серверов с логинами и паролями к персонажу на них, берётся из localStorage
var session="";

var turns=null; // Очерёдность ходов (дублируется из каждого запроса в эту переменную)
var BARwidth=20;
var fps=60; // Так как в javascript нельзя организовать паузу в том же "потоке", то понятие FPS является относительным 
		// и обозначает коэффициент для рассчёта количества кадров. При нормальной производительности реальный fps анимации стремится к переменной.
var gameField=null;
var effectData=[]; // Сюда функции эффектов сохраняют временные данные, в основном объекты jQuery
// Переменные для проверки необходимости скрытия интерфейса при диалогах/alert/confirm

// Переменные путей
var rendW=0;
var rendH=0;
var rendCX=-1;
var rendCY=-1;
var prevRendCX=-1;
var prevRendCY=-1;
// var rendcharacters=[];
// var maxReports=6;
// Разное
var characters={};
var onLoadEvents = []; // Массив для функций, которые выполняются при загрузке

var newPlayerClass="";
var onlinePlayers=[];
var inviterPlayerId=0;
var mapCursorX=0; // dfffffffff
var mapCursorY=0;
// var charDollItemsIndents={
	// 50:[2,13]
// }; 
// Массив с объектами Image с изображениями для надетой на персонажа амуниции
// var charDollImages=[];
var floorImages={};
var worldMapFloorCanvas=null;
// var oneOfInputsIsUnderFocus=false; // Отвечает за запрет горячих клавиш, когда на каком-то input стоит фокус
var useDB=false; // Использовать ли базу данных
var DB;
var settings=[ // Здесь хранятся настройки клиента
// Здесь написаны настройки по умолчанию (которые заменяются на установленные, когда клиент загружается)
	true // Анимация передвижения
]; 
var images=[];
var inMenu=true; // Включено ли главное меню (если false - игрок в игровом окне)
var floorCanvas;	// Элемент канваса, в которой рисуется пол
var alliesNames;	// Массив, индекс - playerId, значение - имя
window.jsonp=[];	// Массив для сохранения функций jsonp
					// Функции сохраняются в массив, так как с каждым вызовом send создаётся новая функция и ненужные функции следует удалять

var hiddenBotheringObjects=[]; // Скрытые мешающие объекты (из обработчика движения мыши)
var maxSkillPoints=300;
var baseSkillPoints=50;
var deltaSkillPoints=50;
var projectileTypesNames=["","arrow","bolt","fireball","icebolt"];
var projectileEffectsNames=["","blood","blood","firesparks","iceshivers"];
var keyChars="qwertyasdfghzxcvbn";	// Порядок присвоения горячих клавиш для предметов в рюкзаке, лута
var spellsKeyChars="asdfghzxcvbn";	// Порядок присвоения горячих клавиш для заклинаний
var keysCharElements=[];	// Сюда сохраняются элементы с буквами для клавиш на иконкахs
var pressedArrow=0; // keyCode последней нажатой стрелки, для диагонального движения
var arrowPressTimer;// Таймер для обработки нажатия двух стрелок для диагонального движения
var prevClientX;	// Координаты последнего положения мыши, изменяются в gameField.onmousemove; 
var prevClientY;	// Необходимо сохранять здесь из-за бага с mousemove в Chrome, когда mousemove выполняется даже тогда, когда мышь не двигается
var minimap;
var gameWindows=[];	// Здесь хранятся объекты внутриигровых окон
var	windowSkills, 	// Конкретные окна: навыки
	windowServices, // Список услуг в городе
	windowGAlert, 	// Игровой Alert
	windowDeath, 	// Сообщение о смерти
	windowContainer,// Содержимое контейнеров
	windowSettings,// Содержимое контейнеров
	windowLogin;	// Содержимое контейнеров
var worldPlayers = [];	// Объекты для отображения игроков на карте мира.
var weatherEffect;
var tiles = [];
var serverAnswer;
var serverAnswerIterator;
var playerCreation = {skills:{}};
var chat = [];