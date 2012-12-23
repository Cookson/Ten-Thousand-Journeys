/* uielements.js: Code for UI elements generation
 * (fixed blocks of images or text) such as inventory window, 
 * minimap and so on. UI elements are represented as widgets 
 * (UIElement) and windows (UIWindow).
 */
/** @class */
var UI = {
	// Constants describing alignation of UI elements in game window
	/**
	 * @constant
	 * @see UIElement#
	 * @type String;
	 */
	ALIGN_LEFT : "LEFT",
	/**
	 * @constant
	 * @type String;
	 */
	ALIGN_RIGHT : "RIGHT",
	/**
	 * @constant
	 * @type String;
	 */
	ALIGN_TOP : "TOP",
	/**
	 * @constant
	 * @type String;
	 */
	ALIGN_BOTTOM : "BOTTOM",
	/**
	 * @constant
	 * @type String;
	 */
	ALIGN_CENTER : "CENTER",
	/**
	 * @constant
	 * @type String;
	 */
	ALIGN_MIDDLE : "MIDDLE",
	// Constants describing when to show an element: on global map, in location
	// or always
	/**
	 * @constant
	 * @type String;
	 */
	IN_LOCATION : "In location",
	/**
	 * @constant
	 * @type String;
	 */
	ON_GLOBAL_MAP : "On gobal map",
	/**
	 * @constant
	 * @type String;
	 */
	ALWAYS : "Always",
	// 
	// 
	// 
	/**
	 * Setting mode to MODE_DEFAULT actually sets mode to MODE_ON_GLOBAL_MAP or
	 * MODE_IN_LOCATION depending on where the Player currently is.
	 * 
	 * @see UI#setMode
	 * @constant
	 */
	MODE_DEFAULT : 9000,
	/** @constant */
	MODE_ALWAYS : 0,
	/** @constant */
	MODE_ON_GLOBAL_MAP : 1,
	/** @constant */
	MODE_IN_LOCATION : 2,
	/** @constant */
	MODE_MISSILE : 3,
	/** @constant */
	MODE_CHAT : 4,
	/** @constant */
	MODE_CURSOR_ACTION : 5,
	/** @constant */
	MODE_CONTAINER : 6,
	/** @constant */
	MODE_MENU : -1,
	// System property for generating unique field names in notifiers' objects
	_notifiersUniqueId : 0,
	// zIndexes for sorting UI elements
	/** @private */
	_minLayerZIndex : 1000,
	/** @private */
	_maxLayerZIndex : 1000,
	// itemView properties
	/** @private */
	_itemViewImgProto : document.createElement("img"),
	/** @private */
	_itemViewWrapProto : document.createElement("div"),
	/** @private */
	_itemViewNumProto : document.createElement("div"),
	// Public properties
	// *****************
	// width and height of game window zone in pixels.
	// These are default values, they are overriden by
	// values saved in storage
	/**
	 * Width of UI (don't mix this up with width of game zone).
	 * 
	 * @see UI#height
	 * @type Number
	 * @public
	 */
	width : 1024,
	/**
	 * Height of UI (don't mix this up with height of game zone).
	 * 
	 * @see UI#width
	 * @type Number
	 * @public
	 */
	height : 600,
	/**
	 * @type HTMLElement
	 * @public
	 */
	gameZone : null,
	// 
	/**
	 * All UIElements save here when initiated
	 * 
	 * @public
	 */
	uiElements : [],
	// Lists of UI elements that wait for particular game events to redraw
	/** @private */
	_notifiers : {
		lootChange : {},
		equipmentChange : {},
		inventoryChange : {},
		healthChange : {},
		manaChange : {},
		energyChange : {},
		attributeChange : {},
		attributesInit : {},
		chatMessage : {},
		environmentChange : {},
		spellCast : {},
		titleChange : {},
		skillChange : {},
		death : {},
		dialoguePointRecieve : {},
		dialogueEnd : {},
		containerOpen : {},
		containerChange : {},
		stateEntered : {},
		/* Interface events */
		cameraRotation : {},
		quitSelection : {},
		unselectCellAction : {},
		spellSelect : {},
		spellUnselect : {},
		missileSelect : {},
		missileUnselect : {},
		accountCreateStart : {},
		accountPlayersListCall : {},
		playerCreateStart : {},
		alert : {},
		infoShow : {},
		infoHide : {},
		/* Game loading */
		locationLoad : {},
		worldLoad : {},
		serverInfoRecieve : {},
		login : {},
		loginError : {},
		accountPlayersRecieve : {}
	},
	/** @private */
	_savedDOMStructures : {},
	// Object CSSStyleSheet. A stylesheet for custom rules of UIElements
	// This field will be set in onLoadEvents['customStylesheets']
	styleSheet : null,
	mode : -1,
	// Placeholders for panels
	panels : {},
	/**
	 * @public
	 * @type UIPanel
	 */
	topPanel : null,
	/**
	 * @public
	 * @type UIPanel
	 */
	rightPanel : null,
	/**
	 * @public
	 * @type UIPanel
	 */
	bottomPanel : null,
	/**
	 * @public
	 * @type UIPanel
	 */
	leftPanel : null,
	registeredActions : {},
	
	/** @private @type HTMLDivElement */
	_gameFieldElementsContainer: document.createElement("div"),
	/** @private @type Function */
	_gameFieldClickHandler: null,
	_gameFieldClickContext: null,
	
	/**
	 * @public
	 * @type {number}
	 */
	visibleWidth: 0,
	/**
	 * @public
	 * @type {number}
	 */
	visibleHeight: 0
};
/**
 * Fires an event and notifies all the elements which listen to that event,
 * passing thd data to their handlers.
 * 
 * @param {String}
 *            groupName Name of event,
 * @param {mixed}
 *            data Any data object or primitive that will be passed to
 *            UIElements' event handlers
 * @return {Boolean} true if has any elements, otherwise false.
 * @memberOf UI
 */
UI.notify = function(groupName, data) {
	// console["log"](groupName);
	if ( !this._notifiers[groupName]) {
		console["log"]("Nonexistent notifier "+groupName);
		return false;
	}
	var hasElements = false;
	for ( var i in this._notifiers[groupName]) {
		// console["log"]("redraw", this._notifiers[groupName][i].rootElement);
		this._notifiers[groupName][i].getEvent(groupName, data);
		hasElements = true;
	}
	return hasElements;
};
/**
 * Show {@link UIElement}s that are only show in location, hide UIElements that
 * are only shown on global map.
 */
UI.enterLocationMode = function() {
	for ( var i in this.uiElements) {
		if (this.uiElements[i].displayMode==UI.IN_LOCATION) {
			this.uiElements[i].show();
		} else if (this.uiElements[i].displayMode==UI.ON_GLOBAL_MAP) {
			this.uiElements[i].hide();
		}
	}
};
/**
 * Show {@link UIElement}s that are only show on global map, hide UIElements
 * that are only shown in location map.
 */
UI.enterGlobalMapMode = function() {
	for ( var i in this.uiElements) {
		if (this.uiElements[i].displayMode==UI.ON_GLOBAL_MAP) {
			this.uiElements[i].show();
		} else if (this.uiElements[i].displayMode==UI.IN_LOCATION) {
			this.uiElements[i].hide();
		}
	}
};
/**
 * Show all the elements that are not permanently hidden.
 * 
 * @see UIElement#permanentlyHidden
 */
UI.showAlwaysShownElements = function() {
	for (var i in this.uiElements) {
		var uiElement = this.uiElements[i];
		if (uiElement.displayMode == UI.ALWAYS && !uiElement.permanentlyHidden) {
			this.uiElements[i].show();
		}
	}
};
/**
 * Hides all UIElements, UIWindows and UIPanels and disables their key press
 * reactions.
 * 
 * @see UI#enable
 * @throws {Error}
 *             If UI is already disabled.
 */
UI.disable = function() {
	if (this.disabled) {
		throw new Error("UI is already disabled!");
	}
	this.disabled = true;
	for ( var i = 0; i<this.uiElements.length; i++ ) {
		if (this.uiElements[i].permanentlyHidden) {
			continue;
		}
		this.uiElements[i].hide();
	}
	var panelsHidden = false;
	if (this.topPanel!==null) {
		panelsHidden = true;
		this.topPanel.hide();
	}
	if (this.rightPanel!==null) {
		panelsHidden = true;
		this.rightPanel.hide();
	}
	if (this.bottomPanel!==null) {
		panelsHidden = true;
		this.bottomPanel.hide();
	}
	if (this.leftPanel!==null) {
		panelsHidden = true;
		this.leftPanel.hide();
	}
	if (panelsHidden) {
		this._updateGameZoneBounds();
	}
};
/**
 * Shows all UIElements, UIWindows and UIPanels and enables their key press
 * reactions.
 * 
 * @see UI#disable
 * @throws {Error}
 *             If UI is already enabled.
 */
UI.enable = function() {
	// Show all UI elements that are not permanently hidden.
	// Use this to restore UI after using UI.disable()
	if (!this.disabled) {
		throw new Error("UI is already enabled!");
	}
	this.disabled = false;
	for (var i=0; i<this.uiElements.length; i++) {
		if (
			this.uiElements[i].permanentlyHidden
			|| this.uiElements[i].displayMode==UI.ON_GLOBAL_MAP
			|| this.uiElements[i].displayMode==UI.IN_LOCATION
		) {
			continue;
		}
		this.uiElements[i].show();
		this.uiElements[i].setPosition();
	}
	this.topPanel&&this.topPanel.show();
	this.rightPanel&&this.rightPanel.show();
	this.bottomPanel&&this.bottomPanel.show();
	this.leftPanel&&this.leftPanel.show();
};
/**
 * Make a custom function listen to a notifier. Usually only {@link UIElement}s
 * listen to notifiers, this method allows to bind any function on game event.
 * 
 * @param {String}
 *            notifier Event name.
 * @param {Function}
 *            func Event handler.
 */
UI.addListener = function(notifier, func) {
	// Add custom function to listen to notifier
	this._notifiers[notifier][UI._notifiersUniqueId++ ] = {
		update : func
	};
};
/**
 * Changes UI's mode. UI mode determines a set of handlers for keypresses.
 * 
 * @param {Number}
 *            mode Constan
 * @return
 */
UI.setKeyMapping = function(keyMappingName) {
	if (Keys.keyMappings[keyMappingName] === undefined) {
		throw new Error("No keymapping "+keyMappingName+" registered");
	}
	Keys.keyMapping = Keys.keyMappings[keyMappingName];
};
/**
 * Sets function that handles click on game field (not on UI elements). That
 * function must take 3 arguments: x and y of click cell and Event object.
 * 
 * @param {Function} func New handler.
 * @param {Function} context In thich context function will run. Leave undefined
 * to set context to window object.
 */
UI.setClickHandler = function(func, context) {
	this._gameFieldClickHandler = func;
	this._gameFieldClickContext = context || window;
};
/**
 * Transfers the management of click handling on game field to the handler
 * function.
 */
UI.gameFieldClick = function(x,y,e) {
	this._gameFieldClickHandler.apply(this._gameFieldClickContext, [x,y,e]);
};
/**
 * Creates a new UIElement from description.
 * 
 * @param {Object}
 *            properties
 * 
 * @param properties.type
 *            {Sring} Name of window type (as in uiElementTypes, with prefix
 *            "window")
 * 
 * @param properties.vAlign
 *            Vertical align: UI.ALIGN_(TOP|MIDDLE|BOTTOM) or a number value
 *            to set position in pixels, positive to set from top border,
 *            negative to set from bottom border. This parameter may not be
 *            omitted.
 * 
 * @param properties.hAlign
 *            Horizontal align: UI.ALIGN_(LEFT|CENTER|RIGHT) or a number
 *            value to set position in pixels, positive to set from left
 *            border, negative to set from right border. This parameter may
 *            not be omitted.
 */
UI.addElement = function _(properties) {
	var uiElement = new UIElement(properties.type);
	if (properties.panel) {
		// Nothing else is needed for UIElements on panel
		uiElement.place(UI.panels[properties.panel]);
	} else {
		// Vertical align
		if (properties.vAlign!==undefined) {
			if (properties.vAlign=="top") {
				uiElement.vAlign = UI.ALIGN_TOP;
			} else if (properties.vAlign=="center") {
				uiElement.vAlign = UI.ALIGN_CENTER;
			} else if (properties.vAlign=="bottom") {
				uiElement.vAlign = UI.ALIGN_BOTTOM;
			} else {
				uiElement.vAlign = properties.vAlign;
			}
		} else {
			throw new Error(
					"Vertical align must be given for UIElements not in panel");
		}
		// Horizontal align
		if (properties.hAlign!==undefined) {
			if (properties.hAlign=="left") {
				uiElement.hAlign = UI.ALIGN_LEFT;
			} else if (properties.hAlign=="center") {
				uiElement.hAlign = UI.ALIGN_CENTER;
			} else if (properties.hAlign=="right") {
				uiElement.hAlign = UI.ALIGN_RIGHT;
			} else {
				uiElement.hAlign = properties.vAlign;
			}
		} else {
			throw new Error(
					"Horizontal align must be given for UIElements not in panel");
		}
		uiElement.place();
	}
	this.uiElements.push(uiElement);
};
UI._updateGameZoneBounds = function _() {
	// Updates left, top, width and height of intfGameZone element
	// according to panels' bounds
	var gzWidth = this.width;
	var gzHeight = this.height;
	var gzLeft = 0;
	var gzTop = 0;
	if (this.topPanel) {
		gzTop = this.getPanelWidth(Side.N);
		gzHeight -= this.getPanelWidth(Side.N);
	}
	if (this.leftPanel) {
		gzLeft = this.getPanelWidth(Side.W);
		gzWidth -= this.getPanelWidth(Side.W);
	}
	if (this.bottomPanel) {
		gzHeight -= this.getPanelWidth(Side.S);
	}
	if (this.rightPanel) {
		gzWidth -= this.getPanelWidth(Side.E);
	}
	this.gameZone.style.width = gzWidth+"px";
	this.gameZone.style.height = gzHeight+"px";
	this.gameZone.style.left = gzLeft+"px";
	this.gameZone.style.top = gzTop+"px";
	this.visibleWidth = this.width-this.getPanelWidth(Side.W)
			-this.getPanelWidth(Side.E);
	this.visibleHeight = this.height-this.getPanelWidth(Side.N)
			-this.getPanelWidth(Side.S);
};
/**
 * Places UIPanel from particular side. If there is already a panel from that
 * side, hides that panel.
 * 
 * @param {UIPanel}
 *            panel Panel to place.
 * @param {Side}
 *            side Side to place. Side may be only Side.N, Side.E, Sdie.S,
 *            Side.W, i.e. no intercardinal directions
 */
UI.setPanel = function _(panel, side) {
	var previousPanel;
	// Remove existing panel (that panel still remains in UI.panels)
	if (side==Side.N) {
		previousPanel = this.topPanel;
		this.topPanel = panel;
	} else if (side==Side.E) {
		previousPanel = this.rightPanel;
		this.rightPanel = panel;
	} else if (side==Side.S) {
		previousPanel = this.bottomPanel;
		this.bottomPanel = panel;
	} else if (side==Side.W) {
		previousPanel = this.leftPanel;
		this.leftPanel = panel;
	}
	if (previousPanel) {
		previousPanel.rootElement.parentNode
				.removeChild(previousPanel.rootElement);
	}

	// Place new panel
	if (side==Side.N) {
		UI.topPanel = panel;
		panel.rootElement.style.width = UI.width+"px";
		panel.rootElement.style.height = panel.width+"px";
		panel.rootElement.style.left = 0+"px";
		panel.rootElement.style.top = 0+"px";
	} else if (side==Side.E) {
		UI.rightPanel = panel;
		panel.rootElement.style.width = panel.width+"px";
		panel.rootElement.style.height = UI.height+"px";
		panel.rootElement.style.left = UI.width-panel.width+"px";
		panel.rootElement.style.top = 0;
	} else if (side==Side.S) {
		UI.bottomPanel = panel;
		panel.rootElement.style.width = UI.width+"px";
		panel.rootElement.style.height = panel.width+"px";
		panel.rootElement.style.left = 0+"px";
		panel.rootElement.style.top = UI.height-panel.width+"px";
	} else if (side==Side.W) {
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
/**
 * Creates and displays a panel in user interface.
 * 
 * @param {Object}
 *            properties Key/value list of properties.
 * @param {String}
 *            properties.name Name at which this panel will be identified.
 * @param {Number}
 *            properties.width Width if panel in pixels
 * @param {String}
 *            properties.side Side from which to put the panel. May be "top",
 *            "right", "bottom" or "left".
 * @throws {Error}
 *             If properties.side is not an appropriate string.
 */
UI.addPanel = function _(properties) {
	var panel = new UIPanel(properties.name, properties.width);
	if (properties.side) {
		var side;
		switch (properties.side) {
		case "top":
			side = Side.N;
			break;
		case "right":
			side = Side.E;
			break;
		case "bottom":
			side = Side.S;
			break;
		case "left":
			side = Side.W;
			break;
		default:
			throw new Error("Unknown side "+properties.side);
		}
		UI.setPanel(panel, side);
	}
};
/**
 * Returns visible width of panel from particular side in pixels.
 * 
 * If there is no panel on that side or panel is hidden, returns 0;
 * 
 * @param
 */
UI.getPanelWidth = function _(side) {

	var panel;
	if (side==Side.N) {
		panel = this.topPanel;
	} else if (side==Side.E) {
		panel = this.rightPanel;
	} else if (side==Side.S) {
		panel = this.bottomPanel;
	} else if (side==Side.W) {
		panel = this.leftPanel;
	} else {
		throw new Error("Unknown side "+side);
	}
	if (panel&& !panel.hidden) {
		return panel.width;
	} else {
		return 0;
	}
};
UI.getViewCellBounds = function _() {
	/**
	 * Gets coordinate of left top cell in game window and game windows' width
	 * and height in cells
	 */
	return [Math.floor( -parseInt(gameField.style.left)/32),
			Math.floor( -parseInt(gameField.style.top)/32),
			Math.floor(parseInt(this.visibleWidth)/32),
			Math.floor(parseInt(this.visibleHeight)/32), ];
};
/**
 * Creates (see {@link UIWindow}) and places (@ee {@link UIElement}) a new
 * UIWindow object made by list of properties.
 * 
 * @param {Object}
 *            properties
 * @param properties.type
 *            {Sring} - name of window type (as in uiElementTypes, with prefix
 *            "window")
 * @param properties.vAlign -
 *            vertical align: UI.ALIGN_(TOP|MIDDLE|BOTTOM) or a number value to
 *            set position in pixels, positive to set from top border, negative
 *            to set from bottom border. This parameter may not be omitted.
 * @param properties.hAlign -
 *            horizontal align: UI.ALIGN_(LEFT|CENTER|RIGHT) or a number value
 *            to set position in pixels, positive to set from left border,
 *            negative to set from right border. This parameter may not be
 *            omitted.
 */
UI.addWindow = function _(properties) {
	properties.place = null;
	var uiWindow = new UIWindow(properties.type);
	// Vertical align
	if (properties.vAlign!==undefined) {
		if (properties.vAlign=="top") {
			uiWindow.vAlign = UI.ALIGN_TOP;
		} else if (properties.vAlign=="middle") {
			uiWindow.vAlign = UI.ALIGN_MIDDLE;
		} else if (properties.vAlign=="bottom") {
			uiWindow.vAlign = UI.ALIGN_BOTTOM;
		} else if (properties.vAlign.length!==undefined) {
			// If it is String
			throw new Error("Inappropriate vertical align of window "
					+properties.type);
		} else {
			uiWindow.vAlign = properties.vAlign;
		}
	} else {
		throw new Error(
				"Vertical align must be given for UIElements not in panel");
	}
	// Horizontal align
	if (properties.hAlign!==undefined) {
		if (properties.hAlign=="left") {
			uiWindow.hAlign = UI.ALIGN_LEFT;
		} else if (properties.hAlign=="center") {
			uiWindow.hAlign = UI.ALIGN_CENTER;
		} else if (properties.hAlign=="right") {
			uiWindow.hAlign = UI.ALIGN_RIGHT;
		} else if (properties.hAlign.length!==undefined) {
			// If it is String
			throw new Error("Inappropriate vertical align of window "
					+properties.type);
		} else {
			uiWindow.hAlign = properties.hAlign;
		}
	} else {
		throw new Error(
				"Horizontal align must be given for UIElements not in panel");
	}
	uiWindow.place();
};
/**
 * Returns DOM structure (single-level DOM elements sequence wrapped in a div),
 * saved under key %hash% in UI.savedDOMStructures, if it is saved. If there's
 * no key %hash% in UI.savedDOMStructures, then this structure will be generated
 * by function %func% and saved under key %hash%. Note that there is no method
 * UI.setDOMStructure, because DOM structures are saved only when
 * 
 * @param {String}
 *            hash Key where structure is saved/must be saved.
 * @param {Function}
 *            func Function that generates and returns the structure.
 * @return {HTMLDivElement}
 */
UI.getStaticDOMStructure = function _(hash, func) {
	if (!(hash in this._savedDOMStructures)) {
		this._savedDOMStructures[hash] = func();
	}
	return this._savedDOMStructures[hash];
};
/**
 * Add action to registered actions list under name %name%. Before performing
 * any UIAction or assigning a UIAction to a key we should register this action,
 * so action obtains name and context. Context determines which object function
 * action applies to (by default it is window)
 * 
 * @param {Function}
 *            handler Function that handles user's command.
 * @param {String}
 *            name Name at which we save the action.
 * @param {Object}
 *            [context=window] Context in which handler works.
 */
UI.registerAction = function _(name, context, handler) {
	if (name in UI.registeredActions) {
		throw new Error("Another action under name "+name
				+" is already registred");
	}
	if (context === undefined) {
		context = window;
	}
	UI.registeredActions[name] = new UIAction(handler, context);
};
UI.addGameFieldElement = function(type, x, y, params) {
	new UIGameFieldElement(type, x, y, params);
};
UI.showLoadingScreen = function() {
	
};
UI.hideLoadingScreen = function() {
	
};
/**
 * Creates a structure of dome nodes that represents an item image with
 * particular background. This class is used by ItemViewFactory
 * 
 * @private
 * @param {UniqueItem|ItemPile}
 *            item {@link UniqueItem}, or {@link ItemPile}, or the constructor
 *            function {@link UniqueItem} itself to not set src of image (is
 *            useful for preloading UI elements before Player is loaded).
 * 
 * @return {HTMLDivElement} A div (with set data) containing other nodes.
 */
function ItemView(item) {
/** @private */
	this.item = item;
/** @private @type HTMLDivElement */
	this.rootElement = this._itemViewWrapProto.cloneNode(true);
	this.numElement = this._itemViewNumProto.cloneNode(true);
	this.rootElement.appendChild(this._itemViewImgProto.cloneNode(true));
	this.rootElement.appendChild(this.numElement);
};
/**
 * img node for cloning
 * @private
 * @const
 */
ItemView.prototype._itemViewImgProto = document.createElement("img");
/**
 * div element for cloning. Has className "itemView"
 * @private
 * @const
 */
ItemView.prototype._itemViewWrapProto = document.createElement("div");
/**
 * div element for cloning. Has className "itemAmount"
 * @private
 * @const
 */
ItemView.prototype._itemViewNumProto = document.createElement("div");
/**
 * Get HTMLElement containing all the nodes.
 * 
 * @return {HTMLDivElement}
 */
ItemView.prototype.getNode = function() {
	return this.rootElement;
};
/**
 * Get HTMLImageElement of item image.
 * 
 * @return {HTMLImageElement}
 */
ItemView.prototype.getImg = function() {
	return this.rootElement.children[0];
};
/**
 * Changes displayed amount of items. If amount was undisplayable (1 item) and
 * became displayable (more than 1 item), display it.
 * 
 * @param {Number}
 *            amount
 * @throws {Error}
 *             If this is not an ItemView of {@link ItemPile} (i.e. it is an
 *             ItemView of {@link UniqueItem}).
 */
ItemView.prototype.changeAmount = function(amount) {
	if (amount == 1) {
		this.numElement.style.display = "none";
	} else if (this.numElement.firstChild.nodeValue==1 && amount>1) {
		this.numElement.style.display = "inline-block";
	}
	this.numElement.firstChild.nodeValue = amount;
};
/**
 * Checks if the ItemView is displayed or not (not append as a child to any
 * other elements).
 * 
 * @return {Boolean}
 */
ItemView.prototype.isDisplayed = function() {
	return this.rootElement.parentNode !== null;
};
ItemView.prototype.setBackground = function(bg) {
	this.rootElement.children[0].className = "iconWrap";
	this.rootElement.children[0].addClass("itemView"+bg);
};
/**
 * Display certain item in ItemView.
 * 
 * @param {UniqueItem|ItemPile|null} item If null, displays null view.
 */
ItemView.prototype.setItem = function(item) {
	this.item = item;
	if (item === null) {
		this.rootElement.children[0].setAttribute("src","./images/intf/nothing.png");
		this.changeAmount(1);
	} else {
		this.rootElement.children[0].setAttribute("src","./images/items/"+item.typeId+".png");
		if ("amount" in item) {
			this.changeAmount(item.amount);
		} else {
			this.changeAmount(1);
		}
	}
};
ItemView.prototype.hashCode = function() {
	return this.item.hashCode();
};
ItemView.prototype.equals = function(object) {
	return this.item.hashCode() == object.hashCode();
};

function UIGameFieldElement(type, x, y, params) {
	this.rootElement = document.createElement("div");
	UIGameFieldElementTypes[type].apply(this, params);
	UI._gameFieldElementsContainer.appendChild(this.rootElement);
	this.rootElement.style.position = "absolute";
	this.rootElement.style.top = y*32+"px";
	this.rootElement.style.left = x*32+"px";
};
UIGameFieldElement.prototype.remove = function() {
	UI._gameFieldElementsContainer.removeChild(this.rootElement);
};
/**
 * Class that represents a single element of user interface.
 * 
 * @constructor
 * @param {String}
 *            type String that is equal to desired element's type name in
 *            UIElementTypes.
 */
function UIElement(type) {
	if (type=== -1) {
		// For prototype
		return;
	}

	this.type = type;
	this.UIElementType = UIElementTypes[type];
	/** @private */
	this.data = {};
	this.permanentlyHidden = false;
	this.hidden = false;

	for ( var actionName in this.UIElementType.keysActions) {
		// Register UIElementType's keysActions if it is still not registered
		UI.registerAction(actionName, this,
				this.UIElementType.keysActions[actionName]);
	}
	if (this.UIElementType.listeners===undefined) {
		throw new Error("Listeners are not given in UI element "+type);
	}
	for ( var notifierName in this.UIElementType.listeners) {
		// Add this element to all required notifiers
		if ( typeof this.UIElementType.listeners[notifierName]==="string") {
			if (UI._notifiers[notifierName]===undefined
					||this.UIElementType.listeners[notifierName]===undefined) {
				throw new Error("Can't set listener of UI element "+this.type
						+" to nonexistent notifier "
						+this.UIElementType.listeners[notifierName]);
			}
			UI._notifiers[notifierName][UI._notifiersUniqueId++ ] = this;
		} else {
			if (UI._notifiers[notifierName]===undefined) {
				throw new Error("Can't set listener of UI element "+this.type
						+" to nonexistent notifier "+notifierName);
			}
			UI._notifiers[notifierName][UI._notifiersUniqueId++ ] = this;
		}
	}
};
UIElement.prototype.setDisplayMode = function(displayMode) {
	this.displayMode = displayMode;
	return this;
};
/**
 * Places UIElement on it's place: on UIPanel, if argument panel is given, above
 * game field otherwise. Calling this method is necessary to display a new
 * created UIElement.
 * 
 * @param {UIPanel}
 *            panel A UIPanel object, or leave it undefined.
 */
UIElement.prototype.place = function(panel) {
	// Root element gets className that equals to argument type
	this.rootElement = document.createElement("div");
	this.rootElement.addClass(this.type);
	if (panel!==undefined) {
		panel.addUIElement(this);
		this.rootElement.style.position = "relative";
	} else {
		var nContainer = document.getElementById("intfGameZone");
		nContainer.insertBefore(this.rootElement, nContainer.children[0]);
		this.rootElement.style.position = "absolute";
		this.rootElement.style.zIndex = 9000;
		this.onBottomLayer();
	}
	this.UIElementType.cssRules&&this.UIElementType.cssRules()
			&&this.addCSSRules();
	this.UIElementType.onInit.apply(this);
	var a = this;
	setTimeout(function() {
		a.setPosition();
	}, 1);
};
/**
 * Calls function corresponding to event name
 * 
 * @param {String}
 *            notifier Name of notifier, e.g. "alert" or "healthChange"
 * @param data
 *            Any data that this elements' handler of notifier can handle,
 * @see UI.notify
 * @return
 */
UIElement.prototype.getEvent = function(name, data) {
	var listener = this.UIElementType.listeners[name];
	if (listener===undefined) {
		throw new Error(
				"Cannot update element listening non-existent notifier "+name);
	}
	if ( typeof listener==="string") {
		// UIElementType listeners may contain just a string, this means
		// pointing
		// to a listener with such name.
		this.getEvent(listener, data);
	} else {
		this.UIElementType.listeners[name].apply(this, [data]);
		this.setPosition();
	}
};
UIElement.prototype.setPosition = function() {
	// Place element to particular coordinates in game window zone
	if (this.panel!==undefined) {
		throw new Error(
				"Elements on panels cannot be placed to particular coordinates!");
	}
	// Horizontally
	if (this.hAlign==UI.ALIGN_LEFT) {
		this.rootElement.style.left = UI.getPanelWidth(Side.W)+"px";
	} else if (this.hAlign==UI.ALIGN_RIGHT) {
		this.rootElement.style.left = (UI.width-this.rootElement.offsetWidth-UI
				.getPanelWidth(Side.E))
				+"px";
	} else if (this.hAlign==UI.ALIGN_CENTER) {
		this.rootElement.style.left = (UI.width-this.rootElement.offsetWidth
				+UI.getPanelWidth(Side.W)-UI.getPanelWidth(Side.E))
				/2+"px";
	} else if (this.hAlign<0) {
		this.rootElement.style.left = (UI.width-this.rootElement.offsetWidth
				-UI.getPanelWidth(Side.E)+this.hAlign)
				+"px";
	} else {
		this.rootElement.style.left = UI.getPanelWidth(Side.W)+this.hAlign+"px";
	}
	// Vertically
	if (this.vAlign==UI.ALIGN_TOP) {
		this.rootElement.style.top = UI.getPanelWidth(Side.N)+"px";
	} else if (this.vAlign==UI.ALIGN_BOTTOM) {
		this.rootElement.style.top = (UI.height-this.rootElement.offsetHeight-UI
				.getPanelWidth(Side.S))
				+"px";
	} else if (this.vAlign==UI.ALIGN_MIDDLE) {
		this.rootElement.style.top = (UI.height-this.rootElement.offsetHeight
				+UI.getPanelWidth(Side.N)-UI.getPanelWidth(Side.S))
				/2+"px";
	} else if (this.vAlign<0) {
		this.rootElement.style.top = (UI.height-this.rootElement.offsetHeight
				-UI.getPanelWidth(Side.S)+this.vAlign)
				+"px";
	} else {
		this.rootElement.style.top = UI.getPanelWidth(Side.N)+this.vAlign+"px";
	}
};
UIElement.prototype.setData = function(name, data) {
	// Bind any data (including objects) to this
	return this.data[name] = data;
};
UIElement.prototype.getData = function(name) {
	return this.data[name] === undefined ? null : this.data[name];
};
UIElement.prototype.hide = function() {
	this.rootElement.style.display = "none";
	this.hidden = true;
};
UIElement.prototype.show = function() {
	if ( !UI.disabled) {
		this.rootElement.style.display = "block";
		this.setPosition();
		this.hidden = false;
	}
};
/**
 * Mark element as "usually hidden" and hide it. This should be used for
 * elements that are not always visible and appear only when we call them (for
 * example, notification windows behave this way
 */
UIElement.prototype.hidePermanently = function() {
	this.permanentlyHidden = true;
	this.hide();
};
/**
 * Put UI element on top layer so it's zIndex is maximal
 */
UIElement.prototype.onTopLayer = function _() {
	this.rootElement.style.zIndex = ++UI._maxLayerZIndex;
};
/**
 * Put UI element on top layer so it's zIndex is minimal
 */
UIElement.prototype.onBottomLayer = function _() {
	this.rootElement.style.zIndex = --UI._minLayerZIndex;
};
/**
 * Bind handler to element so on event handler will be executed not in
 * HTMLElement's context, but in UIElement's context.
 * 
 * @see UIElement
 * @param {UIElement}
 *            element Element to whose context we bind the handler.
 * @param {String}
 *            eventName Name of DOM event, for example "click" or "focus".
 * @param {Function}
 *            handlerName
 */
UIElement.prototype.bindHandlerToUIElementContext = function _(element, eventName, handlerName) {
	var uiElement = this;
	var handler = this.UIElementType.handlers[handlerName];
	element.addEventListener(eventName, function(e) {
		handler.apply(uiElement, arguments);
	}, false);
};
/**
 * Adds an event listener from this UIElement's UIElementType to an elements.
 * If this element is reusable (is cached in CachingFactory), you will need
 * to remove his listeners using {@link UIElement#clearEventListeners}, or it
 * will have unnecessary event listeners afterwards.
 * 
 * @see UIElement#clearEventListeners
 * @param {HTMLElement} element Element to which we add an event listener.
 * @param {String} eventName Name of event, e.g. "click", "focus" etc.
 * @param handlerName Name of listener function in UIElementType.
 */
UIElement.prototype.addEventListener = function _(element, eventName,
		handlerName) {
	if (this.UIElementType.handlers[handlerName]===undefined) {
		console["log"]
				("No handler "+handlerName+" in UIElementType "+this.type);
	}
	element.addEventListener(eventName,
			this.UIElementType.handlers[handlerName]);
	if (element.eventTypes === undefined) {
		element.eventTypes = [];
		element.eventHandlers = [];
	}
	element.eventTypes.push(eventName);
	element.eventHandlers.push(this.UIElementType.handlers[handlerName]);
};
/**
 * Adds a class name, that will most likely be uniqe, to an element. For
 * example, if UIElement.gameAlert sets class name of div with postfix "Text",
 * then this div will match selector "div.gameAlertText". If element already has
 * class name, then this element will be multiclass. For setting className to
 * particular class use UIElement.setCustomClass();
 * 
 * Use this element so custom class names won't conflict with in-game class
 * names.
 * 
 * @see UIElement#setCustomClass
 * @see HTMLElement#addClass
 */
UIElement.prototype.addCustomClass = function _(element, postfix) {
	if (element.className=="") {
		element.className = this.type+postfix;
	} else {
		element.className += " "+this.type+postfix;
	}
};
UIElement.prototype.setCustomClass = function _(element, className) {
	element.className = this.type+className;
};
UIElement.prototype.addCSSRules = function _(ruleSet) {
	/*
	 * Adds custom rules of UIElements.
	 * 
	 * ruleset - (usually not given) string in format of .css file (see examples
	 * of ruleset in any UIElementType.cssRules section)
	 * 
	 * If ruleSet is not provided, it is taken from this.cssRules() (and this is
	 * default situation).
	 * 
	 * If ruleSet contains substring "$type$ in selectors, then this substring
	 * will be replaced by this UI element's type.
	 * 
	 * Rules may have several $type$ in selectors, for example:
	 * "div.$type$LeftSide, div.$type$RightSide { \ border: 1px solid black; \ }"
	 */

	if (typeof ruleSet === "undefined") {
		var ruleSet = this.UIElementType.cssRules();
	}
	var selector;
	var rule;
	var lastPoint = 0;
	for (var i=0; i<ruleSet.length; i++) {
		if (ruleSet[i]=="}") {
			rule = ruleSet.substring(lastPoint, i+1);
			var selectorLastPoint = 0;
			for (var j = 1; rule[j]!="{"; j++) {
				// Change substring "$type$" to this.type
				// Selector may have several $type$
				if (rule[j]=="$") {
					rule = rule.substr(0, j)+this.type+rule.substr(j+6);
				}
			}
			UI.styleSheet.insertRule(rule, 0);
			lastPoint = i+1;
		}
	}
};
/**
 * @constructor
 */
function UIWindow(type) {
	UIElement.apply(this, [type]);
};
/**
 * @constructor
 */
function UIPanel(name, width) {
	UIElement
			.apply(this, ["panel", UI.ALIGN_LEFT, UI.ALIGN_TOP, {}, UI.ALWAYS]);
	this.name = name;
	this.width = width;
	this.place();
	UI.panels[name] = this;
}

onLoadEvents['uiWindowPrototype'] = function _() {
	UIWindow.prototype = new UIElement("uiWindowPrototype", -1, -1, []);
	UIWindow.prototype.chooseElementAsCloseButton = function _(element, func) {
		// Makes particular element the button that closes this window
		// Also sets onClose callback
		var uiWindow = this;
		if (func!==undefined) {
			this._onClose = func;
		}
		this.closeButton = element;
		this.closeButton.addEventListener("click", function() {
			uiWindow.close();
		});
	};
	UIWindow.prototype.close = function _() {
		this.hide();
		this._onClose&&this._onClose();
	};
	UIWindow.prototype.setKeyMode = function(mode) {
		this.keyMode = mode;
		return this;
	};
	UIWindow.prototype.place = function() {
		UIElement.prototype.place.apply(this);
		this.hidePermanently();
		this.rootElement.applyStyle( {
			backgroundColor : "#333",
			border : "2px solid #888"
		});
	};
	UIPanel.prototype = new UIElement(-1);
	UIPanel.prototype.addUIElement = function(uiElement) {
		this.rootElement.appendChild(uiElement.rootElement);
	};

	UIGameFieldElement.prototype = new UIElement(-1);
	
	ItemView.prototype._itemViewImgProto.addClass("itemImg");
	ItemView.prototype._itemViewWrapProto.addClass("iconWrap");
	ItemView.prototype._itemViewNumProto.addClass("itemAmount");
	ItemView.prototype._itemViewNumProto.appendChild(document
			.createTextNode(""));
};
onLoadEvents['customStylesheets'] = function _() {
	document.head.appendChild(document.createElement("style"));
	UI.styleSheet = document.styleSheets[document.styleSheets.length-1];

	// This souldn't be here (but it works), move somewhere else
	UI.gameZone = document.getElementById("intfGameZone");
};
