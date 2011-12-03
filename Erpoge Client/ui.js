/* uielements.js: Code for UI elements generation
 * (fixed blocks of images or text) such as inventory window, 
 * minimap and so on. UI elements are represented as widgets 
 * (UIElement) and windows (UIWindow).
 */
var UI = {
// width and height of game window zone in pixels. 
// These are default values, they are overriden by 
// values saved in storage
	width			: 1024,
	height			: 600,
// Constants describint alignation of UI elements in game window
	ALIGN_LEFT 		: "LEFT",
	ALIGN_RIGHT 	: "RIGHT",
	ALIGN_TOP 		: "TOP",
	ALIGN_BOTTOM	: "BOTTOM",
	ALIGN_CENTER	: "CENTER",
// Constants describint when to show an element: on global map, in location or always
	IN_LOCATION		: "In location",
	ON_GLOBAL_MAP	: "On gobal map",
	ALWAYS			: "Always",
// System property for generating unique field names in notifiers' objects
	_notifiersUniqueId : 0,
// zIndexes for sorting UI elements
	_minLayerZIndex : 1000,
	_maxLayerZIndex : 1000,
// Public properties
	notifiers: {
	// Lists of UI elements that wait for certain game events to redraw
		lootChange: {},
		ammunitionChange: {},
		inventoryChange: {},
		healthChange: {},
		mana: {},
		chatMessage: {},
		environmentChange: {},
		spellCast: {},
		titleChange: {},
		skillChange: {},
		death: {},
		dialoguePointRecieve: {},
		/* Game loading */
		locationLoad: {},
		worldLoad: {},
		serverInfoRecieve: {},
		login: {},
		loginError: {},
		accountPlayersRecieve: {}
	},
	
// UI type automatically becomes registered when UIElement of this type is created
// Types need to be registered so their keysActions are registered 
// (see keysCore.js/Keys.registerKeyAction)
// If there is key in registeredUIElementTypes that is UIElementType's name,
// then UIElement is registered.
	registeredUIElementTypes: {
		
	},
// Object CSSStyleSheet. A stylesheet for custom rules of UIElements
// This field will be set in onLoadEvents['customStylesheets']
	styleSheet: null
};
UI.notify = function _(groupName, data) {
//	console["log"](groupName);
	if (!UI.notifiers[groupName]) {
		console["log"]("Nonexistent notifier "+groupName);
		return false;
	}
	var hasElements = false;
	for (var i in UI.notifiers[groupName]) {
//		console["log"]("redraw",UI.notifiers[groupName][i].rootElement);
		UI.notifiers[groupName][i].update(groupName, data);
		hasElements = true;
	}
	return hasElements;
};
UI.notifyEverything = function _() {
	for (var i in UI.notifiers) {
		for (var j in UI.notifiers[i]) {
			UI.notifiers[i][j].update(UI.notifiers[i]);
		}
	}
};
UI.enterLocationMode = function _() {
	for (var i in this.notifiers) {
		for (var j in this.notifiers[i]) {
			if (this.notifiers[i][j].displayMode == UI.IN_LOCATION) {
				this.notifiers[i][j].show();
			} else if (this.notifiers[i][j].displayMode == UI.ON_GLOBAL_MAP) {
				this.notifiers[i][j].hide();
			}
		}
	}
};
UI.enterGlobalMapMode = function _() {
	for (var i in this.notifiers) {
		for (var j in this.notifiers[i]) {
			if (this.notifiers[i][j].displayMode == UI.ON_GLOBAL_MAP) {
				this.notifiers[i][j].show();
			} else if (this.notifiers[i][j].displayMode == UI.IN_LOCATION) {
				this.notifiers[i][j].hide();
			}
		}
	}
};
UI.showAlwaysShownElements = function _() {
	for (var i in this.notifiers) {
		for (var j in this.notifiers[i]) {
			var uiElement = this.notifiers[i][j]
			if (uiElement.displayMode == UI.ALWAYS && !uiElement.permanentlyHidden) {
				this.notifiers[i][j].show();
			}
		}
	}
};
UI.disable = function _() {
// Hide all UI elements and disable their keypad reactions
	UI.disabled = true;
	for (var i in UI.notifiers) {
		for (var j in UI.notifiers[i]) {
			if (UI.notifiers[i][j].permanentlyHidden) {
				continue;
			}
			UI.notifiers[i][j].hide();
		}
	}
};
UI.enable = function _() {
// Show all UI elements that are not permanently hidden.
// Use this to restore UI after using UI.disable()
	UI.disabled = false;
	for (var i in UI.notifiers) {
		for (var j in UI.notifiers[i]) {
			if (
				UI.notifiers[i][j].permanentlyHidden || 
				UI.notifiers[i][j].displayMode == UI.ON_GLOBAL_MAP && !onGlobalMap ||
				UI.notifiers[i][j].displayMode == UI.IN_LOCATION && onGlobalMap
			) {
				continue;
			}
			UI.notifiers[i][j].show();
			UI.notifiers[i][j].place();
		}
	}
};

function UIElement(type, hAlign, vAlign, notifiers, displayMode) {
/* 
 * type - string that is equal to desired element's type name in UIElementTypes
 * hAlign - integer/enum value || UI.ALIGN_LEFT || UI.ALIGN_RIGHT || UI.ALIGN_CENTER
 * 		Horizontal align or distance to top/bottom border.
 * 		If integer is positive then it is distance to left border of game window
 * 		in pixels, else - to right.
 * vAlign - integer/enum value || UI.ALIGN_TOP || UI.ALIGN_BOTTOM || UI.ALIGN_CENTER
 * 		Vertical align or distance to top/bottom border.
 * 		If integer is positive then it is distance to top border of game window
 *  	in pixels, else - to bottom.
 *  notifers - array of event types (see UI.notifiers) that force this element to refresh
 *  displayMode - enum value UI.IN_LOCATION || UI.ON_GLOBAL_MAP || UI.ALWAYS
 *  	When this element must be displayed
 *  
 */
	this.hAlign = hAlign;
	this.vAlign = vAlign;
	this.type = type;
	this.UIElementType = UIElementTypes[type];
	this.data = {};
	this.displayMode = displayMode;
	this.notifiers = notifiers;
	this.permanentlyHidden = false;
	this.hidden = false;
	
	for (var actionName in this.UIElementType.keysActions) {
	// Register UIElementType's keysActions if it is still not registered
		Keys.registerKeyAction(this.UIElementType.keysActions[actionName], actionName, this);
	}
	
	for (var notifierName in this.UIElementType.listeners) {
	// Add this element to all required notifiers
		if (typeof this.UIElementType.listeners[notifierName] === "string") {
			if (this.UIElementType.listeners[notifierName] === undefined) {
				throw new Error("Can't set listener of UI element "
						+this.type+" to nonexistent notifier "+this.UIElementType.listeners[notifierName]);
			}
			UI.notifiers[notifierName][UI._notifiersUniqueId++] = this;
		} else {
			if (UI.notifiers[notifierName] === undefined) {
				throw new Error("Can't set listener of UI element "
						+this.type+" to nonexistent notifier "+notifierName);
			}
			UI.notifiers[notifierName][UI._notifiersUniqueId++] = this;
		}
	}
	
	// Root element gets className that equals to argument type
	this.rootElement = document.createElement("div");
	this.rootElement.addClass(type);
	var nLeftSide = document.getElementById("intfLeftSide");
	nLeftSide.insertBefore(this.rootElement, nLeftSide.children[0]);
	this.rootElement.style.position = "absolute";
	this.rootElement.style.zIndex = 9000;
	this.onBottomLayer();
	
	this.UIElementType.cssRules && this.UIElementType.cssRules() && this.addCSSRules();
	
	this.UIElementType.onInit.apply(this);
	var a = this;
	setTimeout(function() {
		a.place();
	}, 1);
}
UIElement.prototype.update = function(notifier, data) {
	var listener = this.UIElementType.listeners[notifier];
	if (listener === undefined) {
		throw new Error("Cannot update element listening non-existent notifier "+notifier);
	}
	if (typeof listener === "string") {
		this.update(listener, data);
	} else {
		this.UIElementType.listeners[notifier].apply(this, [data]);
		this.place();
	}	
};
UIElement.prototype.place = function () {
// Place element to certain coordinates in game window zone
	// Horizontally
	if (this.hAlign == UI.ALIGN_LEFT) {
		this.rootElement.style.left = "0px";
	} else if (this.hAlign == UI.ALIGN_RIGHT) {
		this.rootElement.style.left = (UI.width - this.rootElement.clientWidth)+"px";
	} else if (this.hAlign == UI.ALIGN_CENTER) {
		this.rootElement.style.left = (UI.width - this.rootElement.clientWidth)/2+"px";
	} else if (this.hAlign < 0) {
		this.rootElement.style.left = (UI.width - this.rootElement.clientWidth + this.hAlign)+"px";
	} else {
		this.rootElement.style.left = this.hAlign+"px";
	}
	// Vertically
	if (this.vAlign == UI.ALIGN_TOP) {
	} else if (this.vAlign == UI.ALIGN_BOTTOM) {
		this.rootElement.style.top = (UI.height - this.rootElement.clientHeight)+"px";
	} else if (this.vAlign == UI.ALIGN_CENTER) {
		this.rootElement.style.top = (UI.height - this.rootElement.clientHeight)/2+"px";
	} else if (this.vAlign < 0) {
		this.rootElement.style.top = (UI.height - this.rootElement.clientHeight + this.vAlign)+"px";
	}  else {
		this.rootElement.style.top = this.vAlign+"px";
	}
};
UIElement.prototype.setData = function(name, data) {
// Bind any data (including objects) to this 
	return this.data[name] = data;
};
UIElement.prototype.getData = function(name) {
	if (this.data[name] === undefined) {
		throw new Error("No data field with name "+name+" is assigned to element "+this.type);
	}
	return this.data[name];
};
UIElement.prototype.hide = function() {
	this.rootElement.style.display = "none";
	this.hidden = true;
};
UIElement.prototype.show = function() {
	if (!UI.disabled) {
		this.rootElement.style.display = "block";
		this.place();
		this.hidden = false;
	}
};
UIElement.prototype.hidePermanently = function() {
// Mark element as "usually hidden" and hide it.
// This should be used for elements that are not always visible
// and appear only when we call them (for example, notification 
// windows behave this way).
	this.permanentlyHidden = true;
	this.hide();
};
UIElement.prototype.onTopLayer = function _() {
// Put UI element on top layer so it's zIndex is maximal
	this.rootElement.style.zIndex = ++UI._maxLayerZIndex;
};
UIElement.prototype.onBottomLayer = function _() {
// Put UI element on bottom layer so it's zIndex is minimal
	this.rootElement.style.zIndex = --UI._minLayerZIndex;
};
UIElement.prototype.bindHandlerToUIElementContext = function _(element, eventName, handlerName) {
// Bind handler to element so on event handler will be executed 
// not in element's context, but in UIElement's context
	var uiElement = this;
	var handler = this.UIElementType.handlers[handlerName];
	element.addEventListener(eventName, function(e) {
		handler.apply(uiElement, arguments);
	}, false);
};
UIElement.prototype.addEventListener = function _(element, eventName, handlerName) {
	if (this.UIElementType.handlers[handlerName] === undefined) {
		console["log"]("No handler "+handlerName+" in UIElementType "+this.UIElementType);
	}
	element.addEventListener(eventName, this.UIElementType.handlers[handlerName]);
};
UIElement.prototype.addCustomClass = function _(element, postfix) {
/* Sets a class name to an element.
 * 
 * For example, if UIElement.gameAlert sets class name of 
 * div with postfix "Text", then this div will match 
 * selector "div.gameAlertText".
 * 
 * Use this element so custom class names won't conflict
 * with in-game class names.
 */
	if (element.className == "") {
		element.className = this.type+postfix;
	} else {
		element.className += " "+this.type+postfix;
	}
};
UIElement.prototype.addCSSRules = function _(ruleSet) {
/* Adds custom rules of UIElements.
 * 
 * ruleset - (usually not set) string in format of .css file
 * (see examples of ruleset in any UIElementType.cssRules section)
 * 
 * If ruleSet is not provided, it is taken from this.cssRules()
 * (and this is default situation).
 * 
 * If ruleSet contains substring "$type$ in selectors,
 * then this substring will be replaced by this UI element's type.
 * 
 * Rules may have several $type$ in selectors, for example:
 * "div.$type$LeftSide, div.$type$RightSide {	\
 * 		border: 1px solid black;				\
 * }"
 */
	
	if (typeof ruleSet === "undefined") {
		var ruleSet = this.UIElementType.cssRules();
	}
	var selector;
	var rule;
	var lastPoint = 0;
	for (var i=0; i<ruleSet.length; i++) {
		if (ruleSet[i] == "}") {
			rule = ruleSet.substring(lastPoint, i+1);
			var selectorLastPoint = 0;
			for (var j=1; rule[j] != "{"; j++) {
			// Change substring "$type$" to this.type
			// Selector may have several $type$
				if (rule[j] == "$") {
					rule = rule.substr(0,j)+this.type+rule.substr(j+6);
				}
			}
			UI.styleSheet.insertRule(rule);
			lastPoint = i+1;
		}
	}
};

function UIWindow(type, hAlign, vAlign, notifiers, displayMode, keyMode) {
	this.keyMode = keyMode;
	UIElement.apply(this, [type,hAlign,vAlign,notifiers,displayMode,keyMode]);
	this.hidePermanently();
	this.rootElement.applyStyle({
		backgroundColor:"#333",
		border:"2px solid #888"
	});
};
onLoadEvents['uiWindowPrototype'] = function _() {
	UIWindow.prototype = new UIElement("uiWindowPrototype", -1, -1, []);
	UIWindow.prototype.chooseElementAsCloseButton = function _(element) {
	// Makes certain element the button that closes this window
		var uiWindow = this;
		this.closeButton = element;
		this.closeButton.addEventListener("click", function() {
			uiWindow.close();
		});
	};
	UIWindow.prototype.close = function _() {
		this.hide();
	};
};
onLoadEvents['customStylesheets'] = function _() {
	document.head.appendChild(document.createElement("style"));
	UI.styleSheet = document.styleSheets[document.styleSheets.length-1];
	UI.styleSheet.addRule("   img.lala ", "{color:#fff;}");
};
