/* keysCore.js: Control settings
 * Assigning actions to keys
 */
var Keys = {
// keyCode => keyName (generated by function Keys.formReverseKeyCodesTable())
	KEYNAMES : {},
// keyName => keyCode
	KEYCODES : {
		"Backspace"		: 8,
		"Tab"			: 9,
		"Enter"			: 13,
		"Pause"			: 19,
		"Esc"			: 27,
		"Space"         : 32,
		"PageUp"		: 33,
		"PageDown"		: 34,
		"End"			: 35,
		"Home"			: 36,
		"LeftArrow"		: 37,
		"UpArrow"		: 38,
		"RightArrow"	: 39,
		"DownArrow"		: 40,
		"Insert"		: 45,
		"Delete"		: 46,
		"0"				: 48,
		"1"				: 49,
		"2"				: 50,
		"3"				: 51,
		"4"				: 52,
		"5"				: 53,
		"6"				: 54,
		"7"				: 55,
		"8"				: 56,
		"9"				: 57,
		"a"				: 65,
		"b"				: 66,
		"c"				: 67,
		"d"				: 68,
		"e"				: 69,
		"f"				: 70,
		"g"				: 71,
		"h"				: 72,
		"i"				: 73,
		"j"				: 74,
		"k"				: 75,
		"l"				: 76,
		"m"				: 77,
		"n"				: 78,
		"o"				: 79,
		"p"				: 80,
		"q"				: 81,
		"r"				: 82,
		"s"				: 83,
		"t"				: 84,
		"u"				: 85,
		"v"				: 86,
		"w"				: 87,
		"x"				: 88,
		"y"				: 89,
		"z"				: 90,
		"Num 0"			: 96,
		"Num 1"			: 97,
		"Num 2"			: 98,
		"Num 3"			: 99,
		"Num 4"			: 100,
		"Num 5"			: 101,
		"Num 6"			: 102,
		"Num 7"			: 103,
		"Num 8"			: 104,
		"Num 9"			: 105,
		"Num *"			: 106,
		"Num +"			: 107,
		"Num -"			: 109,
		"Num ."			: 110,
		"Num /"			: 111,
		"F1"			: 112,
		"F2"			: 113,
		"F3"			: 114,
		"F4"			: 115,
		"F5"			: 116,
		"F6"			: 117,
		"F7"			: 118,
		"F8"			: 119,
		"F9"			: 120,
		"F10"			: 121,
		"F11"			: 122,
		"F12"			: 123,
		";"				: 186,
		"="				: 187,
		","				: 188,
		"-"				: 189,
		"."				: 190,
		"/"				: 191,
		"`"				: 192,
		"["				: 219,
		"\\"			: 220,
		"]"				: 221,
		"'"				: 222
	},
	SHIFTKEYCODES: {
		")": 48,
		"!": 49,
		"@": 50,
		"#": 51,
		"$": 52,
		"%": 53,
		"^": 54,
		"&": 55,
		"*": 56,
		"(": 57,
		"A": 65,
		"B": 66,
		"C": 67,
		"D": 68,
		"E": 69,
		"F": 70,
		"G": 71,
		"H": 72,
		"I": 73,
		"J": 74,
		"K": 75,
		"L": 76,
		"M": 77,
		"N": 78,
		"O": 79,
		"P": 80,
		"Q": 81,
		"R": 82,
		"S": 83,
		"T": 84,
		"U": 85,
		"V": 86,
		"W": 87,
		"X": 88,
		"Y": 89,
		"Z": 90,
		":": 186,
		"+": 187,
		"<": 188,
		"_": 189,
		">": 190,
		"?": 191,
		"~": 192,
		"{": 219,
		"|": 220,
		"}": 221,
		"\"": 222		
	},
	SHIFTKEYNAMES : {},
// Public changable properties
	mode 				: -1,
	keyMapping: null,
	keyMappings: {}
};
/**
 * Form array "keyCode => keyName" from constant KEYCODES (that contains 
 * reverse array "keyName => keyCode")
 */
Keys.formReverseKeyCodesTable = function _() {
	for (var i in this.KEYCODES) {
		this.KEYNAMES[this.KEYCODES[i]] = i;
	}
	for (var i in this.SHIFTKEYCODES) {
		this.SHIFTKEYNAMES[this.SHIFTKEYCODES[i]] = i;
	}
};
/**
 * Sets cuurent keymapping.
 * 
 * @see UIKeymapping
 */
Keys.setKeyMapping = function(keymapping) {
	this.keyMapping = keymapping;
};
Keys.universalKeyDownHandler = function _(e) {
// The function in document.addEventListener("keyDown")
// Handles all the key presses in game. 
	var ctrl = e.ctrlKey ? 1 : 0;
	var alt = e.altKey ? 1 : 0;
	var shift = e.shiftKey ? 1 : 0;
	var key = e.keyCode;
	var uiAction = Keys.keyMapping.handlers[ctrl][alt][shift][key];
	if (uiAction === undefined) {
		return;
	}
	uiAction.handler.apply(uiAction.context, uiAction.arguments);
	e.preventDefault();
};
/**
 * Returns DOM structure with text describing a key combination.
 * 
 * @param {Number} ctrl 0 or 1
 * @param {Number} alt 0 or 1
 * @param {Number} shift 0 or 1
 * @param {Number} keyCode
 * @returns {HTMLDivElement}
 */
Keys.getCombinationAsTextInDiv = function _(ctrl, alt, shift, keyCode) {
	var text = [];
	if (ctrl == 1) {
		text.push("Ctrl");
	}
	if (alt == 1) {
		text.push("Alt");
	}
	if ((ctrl==1 || alt==1) && shift==1) {
	// If this is a key with shift and other modifier keys are also pressed
		text.push("Shift");
		text.push(Keys.KEYNAMES[keyCode]);
	} else if (shift==1 && (keyCode in Keys.SHIFTKEYNAMES)) {
	// If this is a key with only shift and the key has a shifted character 
	// (i.e. it's not F1 or something)
		text.push(Keys.SHIFTKEYNAMES[keyCode]);
	} else {
	// If this is a key without shift or a combination without shift
		text.push(Keys.KEYNAMES[keyCode]);
	}
	var div = document.createElement("div");
	div.appendChild(document.createTextNode(text.join(" + ")));
	return div;
};
/**
 * UIKeymapping class represents link with key combinations and actions
 * performed by pressing them. First argument must be a name of keymapping, 
 * all the next arguments are arrays, in which the last element is the name of
 * action (String), and all the elements before are names of keys (also 
 * Strings). For example,
 * new UIKeymapping("Test",["k","kick"],["Alt","p","punch"]). When UIKeyMapping
 * object is created, it is automatically saved in Keys singletone, to engine
 * methods can access this key mapping by its name.
 * 
 * @see UI#setKeyMapping
 * @see UI#registerAction
 * @constructor
 * @param {String} name Just to indentify this keymapping.
 */
function UIKeymapping(name) {
	this.handlers = {
		0:{0:{0:{},1:{}},1:{0:{},1:{}}},
		1:{0:{0:{},1:{}},1:{0:{},1:{}}}
	};
	this.arguments = {
		0:{0:{0:{},1:{}},1:{0:{},1:{}}},
		1:{0:{0:{},1:{}},1:{0:{},1:{}}}
	};
	this.name = name;
	for (var i=1; i<arguments.length; i++) {
		this.addHandler.apply(this, arguments[i]);
	}
	Keys.keyMappings[this.name] = this;
	/** @private @boolean */
	this.listensToLetterAssigner = false;
}
/**
 * Adds action for a key combination. Last argument in a name of registered 
 * action, all the arguments before are names of keys. For example,
 * keymapping.addHandler("Ctrl","h","showHelp"); or
 * keymapping.addHandler("Num 8","move",[Side.N]); 
 * 
 * @see UI#registerAction
 */
UIKeymapping.prototype.addHandler = function() {
	var ctrl = 0, alt = 0, shift = 0, keyCode, actionName, args = [];
	var namePosition = arguments.length-1;
	if (arguments[namePosition] instanceof Array) {
	// If the last argument is not a UIAction's name, but an array, then this
	// is arguments array.
		args = arguments[namePosition];
		namePosition = arguments.length-2;
	} 
	actionName  = arguments[namePosition];
	for (var i=0; i<namePosition; i++) {
		if (arguments[i] in Keys.SHIFTKEYCODES) {
		// Shift
			if (shift) {
				var keys = arguments.pop();
				throw new Error("Incorrect keys because of extra shift (use uppercase letters here only when you don't use shift): "+keys);
			}
			shift = 1;
			keyCode = Keys.SHIFTKEYCODES[arguments[i]];
		} else if (arguments[i] == "Shift") {
		// Shift
			if (shift) {
				var keys = arguments.pop();
				throw new Error("Incorrect keys because of extra shift (use uppercase letters here only when you don't use shift): "+keys);
			}
			shift = 1;
		} else if (arguments[i] == "Ctrl") {
		// Ctrl
			if (ctrl) {
				var keys = arguments.pop();
				throw new Error("Incorrect keys because of extra ctrl: "+keys);
			}
			ctrl = 1;
		} else if (arguments[i] == "Alt") {
		// Alt
			if (alt) {
				var keys = arguments.pop();
				throw new Error("Incorrect keys because of extra alt: "+keys);
			}
			alt = 1;
		} else if (keyCode) {
		// Error: extra key.
			throw new Error("Incorrect keys because of extra non-modifier key: "+keys);
		} else {
			keyCode = Keys.KEYCODES[arguments[i]];
		}
	}
	this.handlers[ctrl][alt][shift][keyCode] = UI.registeredActions[actionName];
	this.arguments[ctrl][alt][shift][keyCode] = args;
};
/**
 * Same as addhandler, but usage of this is more strict. This is not for 
 * general use.
 * 
 * @see UIKeymapping#addHandler
 * @see UIAction#name
 * @private
 * @param {Boolean} ctrl
 * @param {Boolean} alt
 * @param {Boolean} shift
 * @param {Number} keyCode
 * @param {String} actionName Name of UIAction.
 * @param {Array} [args=[]] Arguments for UIAction. By default it is an empty 
 * array.
 */
UIKeymapping.prototype.strictAddHandler = function(ctrl, alt, shift, keyCode, actionName, args) {
	this.handlers[ctrl][alt][shift][keyCode] = UI.registeredActions[actionName];
	this.arguments[ctrl][alt][shift][keyCode] = args;
};
/**
 * Unbind action from certain key combination.
 * 
 * @private
 * @param ctrl
 * @param alt
 * @param shift
 * @param keyCode
 */
UIKeymapping.prototype.strictRemoveHandler = function(ctrl, alt, shift, keyCode) {
	delete this.handlers[ctrl][alt][shift][keyCode];
	delete this.arguments[ctrl][alt][shift][keyCode];
};
/**
 * Starts listening to {@link LetterAssigner}. Whenever letter assigner assigns
 * a letter for an object, this UIKeymapping adds handler for that letter 
 * 
 * @public
 * @param {LetterAssigner} assigner LetterAssigner to listen to.
 * @param {String} action What action to perform when that letter is pressed.
 */
UIKeymapping.prototype.listenToLetterAssigner = function(assigner, action) {
	if (!(assigner instanceof LetterAssigner) || action.__proto__.constructor != String) {
		throw new Error("Wrong arguments: "+assigner+", "+action);
	}
	if (this.listensToLetterAssigner) {
		throw new Error("UIKeymapping "+this.name+" already listens to a letter assigner.");
	}
	this.listensToLetterAssigner = true;
	assigner.listeners.push(this);
	assigner.listenerActions.push(action);
};
/**
 * 
 * UIAction class represents any action performed by game user. For example, 
 * "open a door", "open inventory window", "attack closest monster". Objects
 * of this class are not for modder usage, Keys object works with these.
 * 
 * @contsructor
 * @param {Function} handler Function that handles user's command.
 * @param {Object} context Context in which handler works. This may be the
 * window object (by default) or any other object.
 */
function UIAction(handler, context) {
	this.handler = handler;
	this.context = context || window;
}
/**
 * Perform action. This is simply this.handler.apply(this.context, arguments);
 * You can pass any amount arguments to this method — the handler will be
 * applied with them.
 */
UIAction.prototype.perform = function() {
	this.handler.apply(this.context, arguments);
};
/**
 * Class represents a key mapping which links certain objects with their keys,
 * for example item objects or spell objects. This class allows automatic
 * assigning and reassigning of keys. {@link UIKeymapping}s mey listen to
 * LetterAssigners and set handlers for letters that are added to this 
 * LetterAssigner.
 * 
 * @extends UIKeumapping
 * @param {String} name Just a name to identify this object.
 * @return
 */
function LetterAssigner(name) {
/** 
 * Keys — object.__proto__.constructor.name (names of objects' constructor 
 * functions), values — [[ objects, where keys — hash codes of objects, and 
 * values — reverse ASCII codes of letters ]].
 * 
 * @private 
 * @type Object
 */
	this.categories = {};
/** @private @type Boolean */
	this.allLowercaseOccupied = false;
/**
 * Keys — reverse ASCII codes, values: {c:{String} constructor function name, 
 * h:{Number} hashCode}.
 * 
 * @private 
 * @type Object 
 */
	this.bookedLetters = {};
/**
 * UIKeymappings that listen to this LetterAssigner
 * @see UIKeymapping#listenToLetterAssigner
 * @private
 * @type UIKeymapping[]
 */
	this.listeners = [];
/**
 * Each listener saves the action it wants to perform with keys from this
 * LetterAssigner.
 * @private type String[]
 */
	this.listenerActions = [];
/**
 * @private @type Object
 */
	this.occupiedLetters = {};
}
LetterAssigner.prototype = new UIKeymapping("Prototype keymapping");
/**
 * Links object with letter. Letter is chosen automatically, though may be
 * pre-assigned (see {@link LetterAssigner#bookLetterForObject}). 
 * Object must be hashable, i.e. implement method .hashCode() in prototype. If
 * any {@link UIKeymapping}s are listening to this LetterAssigner, they will
 * set handler for that letter.
 * 
 * @see UIAction#name
 * @public
 * @param {Object} object
 * @param {String} actionName Name of registered UIAction
 */
LetterAssigner.prototype.addObject = function(object) {
	if (!(object.hashCode instanceof Function)) {
		throw new Error(object.__proto__.constructor.name+" class is not hashable!");
	}
	var category = object.__proto__.constructor.name;
	if (!(category in this.categories)) {
		this.categories[category] = {};
	} else if (object.hashCode() in this.categories[category]) {
		throw new Error("Object "+object.__proto__.constructor.name+" with hash\
 code "+object.hashCode()+" already registered in keymapping"+this.name);
	}
	var newKeyCode = this.getBookedLetter(object);
	var objectToReassign = null;
	if (newKeyCode != -1) {
	// If the letter for this object is booked.
		if (newKeyCode in this.occupiedLetters) {
		// If booked letter is already assigned, reassign it.
			objectToReassign = this.getObjectAtLetter(newKeyCode);
			this.removeObject(objectToReassign);
		}		
	} else {
	// If the letter for this object is not booked.
		var newKeyCode = this.getUnoccupiedCharacter();
	}
	this.categories[category][object.hashCode()] = newKeyCode;
	this.occupiedLetters[newKeyCode] = true;
	if (objectToReassign !== null) {
		this.addObject(objectToReassign);
	}
	// Update all the listeners
	var shift = 0;
	if (newKeyCode > 90) {
	// If added letter is uppercase
		shift = 1;
		newKeyCode -= 32;
	} else if (!this.hasFreeLowercase()) {
	// If added letter is lowercase
		this.allLowercaseOccupied = false;
	}	
	for (var i=0; i<this.listeners.length; i++) {
		this.listeners[i].strictAddHandler(0, 0, shift, newKeyCode, this.listenerActions[i], [object]);
	}
};
/** 
 * Unlink object and its letter. If any {@link UIKeymapping}s are listening to 
 * this LetterAssigner, they will remove handler for that letter.
 * 
 * @public
 * @param {Object} object
 */
LetterAssigner.prototype.removeObject = function(object) {
	var keyCode = this.categories[object.__proto__.constructor.name][object.hashCode()];
	var shift = 0;
	delete this.occupiedLetters[keyCode];
	if (keyCode > 90) {
	// If removed letter is uppercase
		shift = 1;
		keyCode -= 32;
	} else {
	// If removed letter is lowercase
		this.allLowercaseOccupied = false;
	}
	delete this.categories[object.__proto__.constructor.name][object.hashCode()];
	
	for (var i=0; i<this.listeners.length; i++) {
		this.listeners[i].strictRemoveHandler(0, 0, shift, keyCode);
	}
};
/**
 * Dedicates certain letter for an object, so every time you add this object (
 * or identical object with the same hashCode) in this keymapping, that object
 * will be assigned for the same letter. Other objects still can occupy the
 * letter, but if this object will be added, they will be reassigned. 
 * 
 * @see LetterAssigner#unbookLetterForObject
 * @param {Number} keyCode Reverse ASCII code of letter (65—90 for lowercase,
 * 97—122 for uppercase).
 */
LetterAssigner.prototype.bookLetterForObject = function(keyCode, object) {
	this.bookedLetters[keyCode] = [object.__proto__.constructor.name][object.hashCode()];
};
/**
 * Undedicate a letter for the object.
 * 
 * @param {Object} object
 */
LetterAssigner.prototype.unbookLetterForObject = function(object) {
	var hashCode = object.hashCode();
	var constructorName = object.__proto__.constructor.name;
	for (var i in this.bookedLetters) {
		if (
			this.bookedLetters[i].h == hashCode 
			&& this.bookedLetters[i].c == constructorName
		) {
			delete this.bookedLetters[i];
			return;
		}
	}
	throw new Error("No booked letter for object "+hashCode+" of "+constructorName);
};
/**
 * 
 * @returns {Number} Reverse ASCII code, if the letter is booked, -1 otherwise.
 */
LetterAssigner.prototype.getBookedLetter = function(object) {
	var hashCode = object.hashCode();
	var constructorName = object.__proto__.constructor.name;
	for (var i in this.bookedLetters) {
		if (
			this.bookedLetters[i].h == hashCode 
			&& this.bookedLetters[i].c == constructorName
		) {
			return i;
		}
	}
	return -1;
};
/**
 * Checks if this key already has an action.
 * 
 * @param {Number} keyCode Reverse ASCII code of letter.
 * @returns {Boolean}
 */
LetterAssigner.prototype.isLetterOccupied = function(keyCode) {
	return keyCode in this.occupiedLetters;
};
/**
 * Checks if this keymapping has free lowercase letters to assign. We need to
 * track free lowercase letters, because it is easier for player to use 
 * lowercase letters, so uppercase letters shouldn't be assigned until all the
 * lowercase letters are assigned.
 * 
 * @returns {Boolean}
 */
LetterAssigner.prototype.hasFreeLowercase = function() {
	for (var i=65; i<=90; i++) {
		if (this.handlers[0][0][0][i] === undefined) {
			return true;
		}
	}
	return false;
};
/**
 * Returns a letter linked with object.
 * 
 * @param {Object} object
 * @returns {String} 1 character string — the letter for object.
 */
LetterAssigner.prototype.getLetter = function(object) {
	var keyCode = this.categories[object.__proto__.constructor.name][object.hashCode()];
	if (keyCode > 90) {
	// If the letter is uppercase
		return Keys.SHIFTKEYNAMES[keyCode-32];
	} else {
	// If the letter is lowercase
		return Keys.KEYNAMES[keyCode];
	}
};
/**
 * Returns ASCII code of the firse unoccupied character. Uppercase character
 * codes are lowwercase character codes plus 32 (just as in ASCII).
 * @private
 * @returns {Number} Key code of character for lowercase caharacter, keyCode*2
 * for uppercase character.
 */
LetterAssigner.prototype.getUnoccupiedCharacter = function() {
	if (!this.allLowercaseOccupied) {
	// Try to get lowercase character.
		for (var i=65; i<=90; i++) {
			if (!(i in this.occupiedLetters)) {
				return i;
			}
		}
	}
	for (var i=97; i<=122; i++) {
	// Try to get uppercase character.
		if (!(i in this.occupiedLetters)) {
			return i;
		}
	}
	throw new Error("All the characters, both lower- and uppercase, are occupied.");
};
