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
	gameZone        : null,
// Constants describing alignation of UI elements in game window
	ALIGN_LEFT 		: "LEFT",
	ALIGN_RIGHT 	: "RIGHT",
	ALIGN_TOP 		: "TOP",
	ALIGN_BOTTOM	: "BOTTOM",
	ALIGN_CENTER	: "CENTER",
// Constants describing when to show an element: on global map, in location or always
	IN_LOCATION		: "In location",
	ON_GLOBAL_MAP	: "On gobal map",
	ALWAYS			: "Always",
	// setting mode to MODE_DEFAULT actually sets mode to 
	// MODE_ON_GLOBAL_MAP or MODE_IN_LOCATION depending on 
	// where the player currently is.
	MODE_DEFAULT 			: 9000,
	MODE_ALWAYS 			: 0,
	MODE_ON_GLOBAL_MAP		: 1,
	MODE_IN_LOCATION		: 2,
	MODE_MISSILE			: 3,
	MODE_CHAT				: 4,
	MODE_CURSOR_ACTION		: 5,
	MODE_CONTAINER			: 6,
	MODE_MENU				: -1,
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
		manaChange: {},
		attributeChange: {},
		attributesInit: {},
		chatMessage: {},
		environmentChange: {},
		spellCast: {},
		titleChange: {},
		skillChange: {},
		death: {},
		dialoguePointRecieve: {},
		dialogueEnd: {},
		containerOpen: {},
		containerChange: {},
		cameraRotation: {},
		/* Interface events */
		spellSelect: {},
		spellUnselect: {},
		missileSelect: {},
		missileUnselect: {},
		accountCreateStart: {},
		accountPlayersListCall: {},
		playerCreateStart: {},
		alert: {},
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
	styleSheet: null,
	mode: -1,
// Placeholders for panels
	panels: {},
	topPanel: null,
	rightPanel: null,
	bottomPanel: null,
	leftPanel: null
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
			var uiElement = this.notifiers[i][j];
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
			UI.notifiers[i][j]._place();
		}
	}
};
UI.addListener = function _(notifier, func) {
// Add custom function to listen to notifier
	this.notifiers[notifier][UI._notifiersUniqueId++] = {update: func};
};
UI.setMode = function _(mode) {
	if (mode == UI.MODE_DEFAULT) {
		UI.mode = onGlobalMap ? UI.MODE_ON_GLOBAL_MAP : UI.MODE_IN_LOCATION;
	} else {
		UI.mode = mode;
	}
};
UI.addElement = function _(properties) {
// Creates new UIElement from description
	var uiElement = new UIElement(properties.type);
	if (properties.panel) {
	// Nothing else is needed for UIElements on panel
		uiElement.place(UI.panels[properties.panel]);
	} else {
		// Vertical align
		if (properties.vAlign !== undefined) {
			if (properties.vAlign == "top") {
				uiElement.vAlign = UI.ALIGN_TOP;
			} else if (properties.vAlign == "center") {
				uiElement.vAlign = UI.ALIGN_CENTER;
			} else if (properties.vAlign == "bottom") {
				uiElement.vAlign = UI.ALIGN_BOTTOM;
			} else {
				uiElement.vAlign = properties.vAlign;
			}
		} else {
			throw new Error("Vertical align must be given for UIElements not in panel");
		}
		// Horizontal align
		if (properties.hAlign !== undefined) {
			if (properties.hAlign == "left") {
				uiElement.hAlign = UI.ALIGN_LEFT;
			} else if (properties.hAlign == "center") {
				uiElement.hAlign = UI.ALIGN_CENTER;
			} else if (properties.hAlign == "right") {
				uiElement.hAlign = UI.ALIGN_RIGHT;
			} else {
				uiElement.hAlign = properties.vAlign;
			}
		} else {
			throw new Error("Horizontal align must be given for UIElements not in panel");
		}
		uiElement.place();
	}
};
UI._updateGameZoneBounds = function _() {
// Updates left, top, width and height of intfGameZone element
// according to panels' bounds
	var gzWidth = this.width;
	var gzHeight = this.height;
	var gzLeft = 0;
	var gzTop = 0;
	if (this.topPanel) {
		gzTop = this.topPanel.width;
		gzHeight -= this.topPanel.width;
	}
	if (this.leftPanel) {
		gzLeft = this.leftPanel.width;
		gzWidth -= this.leftPanel.width;
	}
	if (this.bottomPanel) {
		gzHeight -= this.bottomPanel.width;
	}
	if (this.rightPanel) {
		gzWidth -= this.rightPanel.width;
	}
	this.gameZone.style.width = gzWidth+"px";
	this.gameZone.style.height = gzHeight+"px";
	this.gameZone.style.left = gzLeft+"px";
	this.gameZone.style.top = gzTop+"px";
	this.visibleWidth = 
		this.width-this.getPanelWidth(Terrain.SIDE_W)
		-this.getPanelWidth(Terrain.SIDE_E);
	this.visibleHeight = 
		this.height-this.getPanelWidth(Terrain.SIDE_N)
		-this.getPanelWidth(Terrain.SIDE_S);
};
UI.setPanel = function _(panel, side) {
/* Places UIPanel from certain side.
 * If there is already a panel from that side,
 * hides that panel.
*/
	var previousPanel;
	// Remove existing panel (that panel still remains in UI.panels)
	if (side == Terrain.SIDE_N) {
		previousPanel = this.topPanel;
		this.topPanel = panel;
	} else if (side == Terrain.SIDE_E) {
		previousPanel = this.rightPanel;
		this.rightPanel = panel;
	} else if (side == Terrain.SIDE_S) {
		previousPanel = this.bottomPanel;
		this.bottomPanel = panel;
	} else if (side == Terrain.SIDE_W) {
		previousPanel = this.leftPanel;
		this.leftPanel = panel;
	}
	if (previousPanel) {
		previousPanel.rootElement.parentNode.removeChild(previousPanel.rootElement);
	}
	
	// Place new panel
	if (side == Terrain.SIDE_N) {
		UI.topPanel = panel;
		panel.rootElement.style.width = UI.width+"px";
		panel.rootElement.style.height = panel.width+"px";
		panel.rootElement.style.left = 0+"px";
		panel.rootElement.style.top = 0+"px";
	} else if (side == Terrain.SIDE_E) {
		UI.rightPanel = panel;
		panel.rootElement.style.width = panel.width+"px";
		panel.rootElement.style.height = UI.height+"px";
		panel.rootElement.style.left = UI.width-panel.width+"px";
		panel.rootElement.style.top = 0;
	} else if (side == Terrain.SIDE_S) {
		UI.bottomPanel = panel;
		panel.rootElement.style.width = UI.width+"px";
		panel.rootElement.style.height = panel.width+"px";
		panel.rootElement.style.left = 0+"px";
		panel.rootElement.style.top = UI.height-panel.width+"px";
	} else if (side == Terrain.SIDE_W) {
		UI.leftPanel = panel;
		panel.rootElement.style.width = panel.width+"px";
		panel.rootElement.style.height = UI.height+"px";
		panel.rootElement.style.left = 0+"px";
		panel.rootElement.style.top = 0+"px";
	} else {
		throw new Error("Unknown side "+side);
	}
	document.getElementById("intfLeftSide").appendChild(panel.rootElement);
	UI._updateGameZoneBounds();
};
UI.addPanel = function _(properties) {
/* Creates and places panel
	properties: {
		name - string, name of new panel in UI.panels
		width - integer, width of panel in pixels
		side - string, ("top"|"right"|"bottom"|"left")
	}
*/
	var panel = new UIPanel(properties.name, properties.width);
	if (properties.side) {
		var side;
		switch (properties.side) {
		case "top":
			side = Terrain.SIDE_N;
			break;
		case "right":
			side = Terrain.SIDE_E;
			break;
		case "bottom":
			side = Terrain.SIDE_S;
			break;
		case "left":
			side = Terrain.SIDE_W;
			break;
		default:
			throw new Error("Unknown side "+properties.side);
		}
		UI.setPanel(panel, side);
	}
};
UI.getPanelWidth = function _(side) {
	if (side == Terrain.SIDE_N) {
		return this.topPanel ? this.topPanel.width : 0;
	} else if (side == Terrain.SIDE_E) {
		return this.rightPanel ? this.rightPanel.width : 0;
	} else if (side == Terrain.SIDE_S) {
		return this.bottomPanel ? this.bottomPanel.width : 0;
	} else if (side == Terrain.SIDE_W) {
		return this.leftPanel ? this.leftPanel.width : 0;
	} else {
		throw new Error("Unknown side "+side);
	}
};
UI.addWindow = function _(properties) {
	properties.place = null;
	var uiWindow = new UIWindow(properties.type);
	// Vertical align
	if (properties.vAlign !== undefined) {
		if (properties.vAlign == "top") {
			uiWindow.vAlign = UI.ALIGN_TOP;
		} else if (properties.vAlign == "center") {
			uiWindow.vAlign = UI.ALIGN_CENTER;
		} else if (properties.vAlign == "bottom") {
			uiWindow.vAlign = UI.ALIGN_BOTTOM;
		} else {
			uiWindow.vAlign = properties.vAlign;
		}
	} else {
		throw new Error("Vertical align must be given for UIElements not in panel");
	}
	// Horizontal align
	if (properties.hAlign !== undefined) {
		if (properties.hAlign == "left") {
			uiWindow.hAlign = UI.ALIGN_LEFT;
		} else if (properties.vAlign == "center") {
			uiWindow.hAlign = UI.ALIGN_CENTER;
		} else if (properties.vAlign == "right") {
			uiWindow.hAlign = UI.ALIGN_RIGHT;
		} else {
			uiWindow.hAlign = properties.vAlign;
		}
	} else {
		throw new Error("Horizontal align must be given for UIElements not in panel");
	}
	uiWindow.place();
};
function UIElement(type) {
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
 *  panel - UIPanel to which this UIElement must be inserted (if needed)
 *  
 */
	if (type === -1) {
	// For prototype
		return;
	}
	
	this.type = type;
	this.UIElementType = UIElementTypes[type];
	this.data = {};
	this.permanentlyHidden = false;
	this.hidden = false;
	
	for (var actionName in this.UIElementType.keysActions) {
	// Register UIElementType's keysActions if it is still not registered
		Keys.registerKeyAction(this.UIElementType.keysActions[actionName], actionName, this);
	}
	if (this.UIElementType.listeners === undefined) {
		throw new Error("Listeners are not given in UI element "+type);
	}
	for (var notifierName in this.UIElementType.listeners) {
	// Add this element to all required notifiers
		if (typeof this.UIElementType.listeners[notifierName] === "string") {
			if (UI.notifiers[notifierName] === undefined || this.UIElementType.listeners[notifierName] === undefined) {
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
	
	
};
UIElement.prototype.setAlign = function(horizontal, vertical) {
	this.hAlign = horizontal;
	this.vAlign = vertical;
	return this;
};
UIElement.prototype.setDisplayMode = function (displayMode) {
	this.displayMode = displayMode;
	return this;
};
UIElement.prototype.place = function (panel) {
/* Places UIElement on it's place: on UIPanel, if
 * argument panel is given, above game field otherwise
 */
	// Root element gets className that equals to argument type
	this.rootElement = document.createElement("div");
	this.rootElement.addClass(this.type);
	if (panel !== undefined) {
		panel.addUIElement(this);
		this.rootElement.style.position = "relative";
	} else {
		var nContainer = document.getElementById("intfGameZone");
		nContainer.insertBefore(this.rootElement, nContainer.children[0]);
		this.rootElement.style.position = "absolute";
		this.rootElement.style.zIndex = 9000;
		this.onBottomLayer();
	}
	
	
	this.UIElementType.cssRules && this.UIElementType.cssRules() && this.addCSSRules();
	 
	this.UIElementType.onInit.apply(this);
	var a = this;
	setTimeout(function() {
		a._place();
	}, 1);
};
UIElement.prototype.update = function(notifier, data) {
	var listener = this.UIElementType.listeners[notifier];
	if (listener === undefined) {
		throw new Error("Cannot update element listening non-existent notifier "+notifier);
	}
	if (typeof listener === "string") {
		this.update(listener, data);
	} else {
		this.UIElementType.listeners[notifier].apply(this, [data]);
		this._place();
	}	
};
UIElement.prototype._place = function () {
// Place element to certain coordinates in game window zone
	if (this.panel !== undefined) {
		console["log"]("Place "+type+" which is on panel");
		return;
	}
	// Horizontally
	if (this.hAlign == UI.ALIGN_LEFT) {
		this.rootElement.style.left = UI.getPanelWidth(Terrain.SIDE_W)+"px";
	} else if (this.hAlign == UI.ALIGN_RIGHT) {
		this.rootElement.style.left = (UI.width-this.rootElement.clientWidth-UI.getPanelWidth(Terrain.SIDE_E))+"px";
	} else if (this.hAlign == UI.ALIGN_CENTER) {
		this.rootElement.style.left = (UI.width-this.rootElement.clientWidth+UI.getPanelWidth(Terrain.SIDE_W)-UI.getPanelWidth(Terrain.SIDE_E))/2+"px";
	} else if (this.hAlign < 0) {
		this.rootElement.style.left = (UI.width-this.rootElement.clientWidth-UI.getPanelWidth(Terrain.SIDE_E)+this.hAlign)+"px";
	} else {
		this.rootElement.style.left = UI.getPanelWidth(Terrain.SIDE_W)+this.hAlign+"px";
	}
	// Vertically
	if (this.vAlign == UI.ALIGN_TOP) {
		this.rootElement.style.top = UI.getPanelWidth(Terrain.SIDE_N)+"px";
	} else if (this.vAlign == UI.ALIGN_BOTTOM) {
		this.rootElement.style.top = (UI.height-this.rootElement.clientHeight-UI.getPanelWidth(Terrain.SIDE_S))+"px";
	} else if (this.vAlign == UI.ALIGN_CENTER) {
		this.rootElement.style.top = (UI.height-this.rootElement.clientHeight+UI.getPanelWidth(Terrain.SIDE_N)-UI.getPanelWidth(Terrain.SIDE_S))/2+"px";
	} else if (this.vAlign < 0) {
		this.rootElement.style.top = (UI.height-this.rootElement.clientHeight-UI.getPanelWidth(Terrain.SIDE_S)+this.vAlign)+"px";
	}  else {
		this.rootElement.style.top = UI.getPanelWidth(Terrain.SIDE_N)+this.vAlign+"px";
	}
};
UIElement.prototype.setData = function(name, data) {
// Bind any data (including objects) to this 
	return this.data[name] = data;
};
UIElement.prototype.getData = function(name) {
	if (this.data[name] === undefined) {
		return null;
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
		this._place();
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
/* Adds a class name to an element.
 * 
 * For example, if UIElement.gameAlert sets class name of 
 * div with postfix "Text", then this div will match 
 * selector "div.gameAlertText".
 * 
 * If element already has class name, then this element 
 * will be multiclass. For setting className to particular class
 * use UIElement.setCustomClass(); 
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
UIElement.prototype.setCustomClass = function _(element, className) {
	element.className = this.type+className;
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
			UI.styleSheet.insertRule(rule, 0);
			lastPoint = i+1;
		}
	}
};

function UIWindow(type) {
	UIElement.apply(this, [type]);
};

function UIPanel(name, width) {
	UIElement.apply(this, ["panel", UI.ALIGN_LEFT, UI.ALIGN_TOP, {}, UI.ALWAYS]);
	this.name = name;
	this.width = width;
	this.place();
	UI.panels[name] = this;
	UI._updateGameZoneBounds();
}
onLoadEvents['uiWindowPrototype'] = function _() {
	UIWindow.prototype = new UIElement("uiWindowPrototype", -1, -1, []);
	UIWindow.prototype.chooseElementAsCloseButton = function _(element, func) {
	// Makes certain element the button that closes this window
	// Also sets onClose callback
		var uiWindow = this;
		if (func !== undefined) {
			this._onClose = func;
		}
		this.closeButton = element;
		this.closeButton.addEventListener("click", function() {
			uiWindow.close();
		});
	};
	UIWindow.prototype.close = function _() {
		this.hide();
		this._onClose && this._onClose();
	};
	UIWindow.prototype.setKeyMode = function (mode) {
		this.keyMode = mode;
		return this;
	};
	UIWindow.prototype.place = function () {
		UIElement.prototype.place.apply(this);
		this.hidePermanently();
		this.rootElement.applyStyle({
			backgroundColor:"#333",
			border:"2px solid #888"
		});
	};
	UIPanel.prototype = new UIElement(-1);
	UIPanel.prototype.addUIElement = function (uiElement) {
		this.rootElement.appendChild(uiElement.rootElement);
	};
};
onLoadEvents['customStylesheets'] = function _() {
	document.head.appendChild(document.createElement("style"));
	UI.styleSheet = document.styleSheets[document.styleSheets.length-1];
	
	// This souldn't be here (but it works), move somewhere else
	UI.gameZone = document.getElementById("intfGameZone");
};
