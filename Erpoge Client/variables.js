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
var ObjectType = {
	DOOR: 1,
	WALL: 2,
	TREE: 3,
	COMMON: 4
};
var Modules = {};
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

// var rendcharacters=[];
// var maxReports=6;
// Разное
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
