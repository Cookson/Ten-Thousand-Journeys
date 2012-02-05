﻿/** @class */
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
var Terrain = {
// Passability constants
	PASS_FREE    : -1,
	PASS_BLOCKED :  1,
	PASS_SEE     :  3,
	CHUNK_WIDTH  : 20,
	
	ceilings: [],
	cameraOrientation : Side.N,
	cssSideX : "left",
	cssSideY : "top",
	/** @private @type HashMap */
	chunks: new HashMap(),
	
	getViewIndentation: function _(x, y, scale) {
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
	},
	getNormalView: function (x, y) {
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
	},
	isOrientationVertical: function _() {
		return this.cameraOrientation == Side.N || this.cameraOrientation == Side.S;
	},
	getHorizontalDimension: function _() {
		if (this.cameraOrientation == Side.N || this.cameraOrientation == Side.S) {
			return Terrain.width;
		} else {
			return Terrain.height;
		}
	},
	getVerticalDimension: function _() {
		if (this.cameraOrientation == Side.N || this.cameraOrientation == Side.S) {
			return Terrain.height;
		} else {
			return Terrain.width;
		}
	},
	getCell: function _(x, y) {
		if (!this.getChunk(x,y)) {
			return null;
		}
		return this.getChunk(x,y).getAbsoluteCell(x,y);
	},
	/**
	 * 
	 * @param x
	 * @param y
	 * @returns {Chunk} A chunk containing cell x:y.
	 */
	getChunk: function _(x, y) {
		x = Math.floor(x/this.CHUNK_WIDTH)*this.CHUNK_WIDTH;
		y = Math.floor(y/this.CHUNK_WIDTH)*this.CHUNK_WIDTH;
		if (this.chunks.containsKey(x)) {
			if (this.chunks.get(x).containsKey(y)) {
				return this.chunks.get(x).get(y);
			}
			return null;
			throw new Error("No chunk "+x+" "+y);
		}
		return null;
		throw new Error("No chunk column "+x);
	},
	createChunk: function(x, y, data) {
		var column;
		if (!this.chunks.containsKey(x)) {
			column = this.chunks.put(x, new HashMap());
		} else {
			column = this.chunks.get(x);
		}
		return column.put(y, new Chunk(x, y, data));
	},
	setPassability: function(x, y, value) {
		this.getChunk(x, y).getAbsoluteCell(x,y).passability = value;
	},
	createObject: function(x, y, type) {
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
		
	},
	displayObject: function(x, y) {
		this.getCell(x, y).object.display(x, y);
	},
	removeObject: function(x, y) {
		var cell = this.getCell(x, y);
		cell.object.remove();
		cell.object = null;
		this.setPassability(x, y, this.PASS_FREE);
	}
};

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