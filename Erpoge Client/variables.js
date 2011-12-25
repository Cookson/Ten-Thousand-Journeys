/* Основные глобальные игровые переменные */
// Системные константы
Global = {
	container: {
		items: new ItemMap(),
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
Terrain = {
// Passability constants
	PASS_FREE    : -1,
	PASS_BLOCKED :  1,
	PASS_SEE     :  3,
// Cardinal directions
	SIDE_N       :  1,
	SIDE_E       :  2,
	SIDE_S       :  3,
	SIDE_W       :  4,
	
	cells : null,
	ceilings: [],
	cameraOrientation : 1,
	cssSideX : "left",
	cssSideY : "top",
	
	getViewIndentation: function _(x,y,scale) {
		if (Terrain.cameraOrientation == Terrain.SIDE_N) {
			return {left: x*scale, top: y*scale};
		} else if (Terrain.cameraOrientation == Terrain.SIDE_E) {
			return {left: (height-y-1)*scale, top: x*scale};
		} else if (Terrain.cameraOrientation == Terrain.SIDE_S) {
			return {left: (width-x-1)*scale, top: (height-y-1)*scale};
		} else if (Terrain.cameraOrientation == Terrain.SIDE_W) {
			return {left: y*scale, top: (width-x-1)*scale};
		}
	},
	getNormalView: function (x,y) {
		if (Terrain.cameraOrientation == Terrain.SIDE_N) {
			return {x: x, y: y};
		} else if (Terrain.cameraOrientation == Terrain.SIDE_E) {
			return {x: y, y: (height-x-1)};
		} else if (Terrain.cameraOrientation == Terrain.SIDE_S) {
			return {x: (width-x-1), y: (height-y-1)};
		} else if (Terrain.cameraOrientation == Terrain.SIDE_W) {
			return {x: (width-y-1), y: x};
		}
	},
	isOrientationVertical: function _() {
		return this.cameraOrientation == this.SIDE_N || this.cameraOrientation == this.SIDE_S;
	},
	getHorizontalDimension: function _() {
		if (this.cameraOrientation == this.SIDE_N || this.cameraOrientation == this.SIDE_S) {
			return width;
		} else {
			return height;
		}
	},
	getVerticalDimension: function _() {
		if (this.cameraOrientation == this.SIDE_N || this.cameraOrientation == this.SIDE_S) {
			return height;
		} else {
			return width;
		}
	}	
};
// Переменные соединения
var servers=[[]]; // Список всех серверов с логинами и паролями к персонажу на них, берётся из localStorage
var session="";
var currentCharacterId=0; // Id текущего персонажа. Используется в функциях чтения ответа сервера, в которых не указан действующий персонаж

var animationsLeft=0; // Ответ отсылается серверу только тогда, когда закончатся все анимации. Переменная увеличивается при начале любой анимации и уменьшается при окончании
var canAct=false; // Игрок ходит (true) или принимает данные о ходах других (false)
var hasQueuedAction=false; // Есть ли у игрока действия, поставленные в очередь 
					   //(например, автоматическое движение, когда персонаж отправлен на несколько клеток, а не на соседнюю)
var turns=null; // Очерёдность ходов (дублируется из каждого запроса в эту переменную)
// Переменные интерфейса
var BARwidth=20;
var keyPressForbidden=0;
var fps=60; // Так как в javascript нельзя организовать паузу в том же "потоке", то понятие FPS является относительным 
		// и обозначает коэффициент для рассчёта количества кадров. При нормальной производительности реальный fps анимации стремится к переменной.
var gameField=null;
var lastEffectId=0;
var effectData=[]; // Сюда функции эффектов сохраняют временные данные, в основном объекты jQuery
// Переменные для проверки необходимости скрытия интерфейса при диалогах/alert/confirm

// Переменные путей
var width=0;
var height=0;
var areaId=null;
var rendW=0;
var rendH=0;
var rendCX=-1;
var rendCY=-1;
var prevRendCX=-1;
var prevRendCY=-1;
var onGlobalMap=false;
// var rendcharacters=[];
// var maxReports=6;
// Разное
var characters={};
var world=null;
var area={
	floor:[],
	objects:[],
	items:[],
	characters:[],
	paths:[]
};
var onLoadEvents=[]; // Массив для функций, которые выполняются при загрузке
// var quickAccessSlots=[null,null,null,null,null,null,null,null,null];
// var hasSeenStartNewWindowOffer=false;
// var enableVisibility=true;
var dialOrder={
// Порядок расстановки персонажей в окне диалога
	main:[11,6,16,10,1,21,5,15,0,20],
	second:[13,8,18,14,3,23,9,19,4,24]
};
// Переменные персонажей
// var liveCharacters=[]; // Живые персонажи (для readCharacters)
var deadCharacters=[]; // Мёртвые (оттуда же)
var killedByAttackCharacters=[]; // Персонажи, убитые какой-либо прямой атакой, а не эффектом или чем-нибудь ещё

// Здесь хранятся изученные создаваемым игроком навыки перед передачей серверу
var newPlayerLearnedSkills=[];
var newPlayerClass="";
var onlinePlayers=[];
var inviterPlayerId=0;
var mapCursorX=0; // dfffffffff
var mapCursorY=0;
var gAlertTimeout=null;
var actionsList=null;
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
var keysMode=0; 	/*	Режим, в котором работают клавиши. 
						0 - обычный, 
						1 - выбор предметов из рюкзака, 
						2 - выбор амуниции
						3 - выбор заклинаний
						4 - выбор предметов с пола 
						5 - чат
						6 - выбор клетки, дальняя атака	*/
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
var isLocationPeaceful;
var tiles = [];
var serverAnswer;
var serverAnswerIterator;
var playerCreation = {skills:{}};
var chat = [];