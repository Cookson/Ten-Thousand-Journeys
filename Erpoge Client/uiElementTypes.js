
var UIElementTypes = {};
/*
 * UIElementTypes object contains description objects for 
 * UI elements that can be created in game. Each type has 4
 * properties:
 * onInit - function that is called when UI element is constructed
 * onRefresh - function that determines the behavior of UI element:
 * 		how it changes when something happens in game. 
 * keysActions - object containing functions that may be bind
 * 		to keypad keys (see UI.registerAction
 * 		for information how exactly this works).
 * handlers - object containing functions that are bound to 
 * 		this UI element's HTML nodes when object is constructed
 * 		(all these bingings are commonly in this element type's onInit).
 */
UIElementTypes.uiWindowPrototype = {
	onInit: function _() {},
	listeners: {},
	keysActions: {},
	handlers: {},
	cssRules: function _() {
		
	}
};
UIElementTypes.panel = {
	onInit: function _() {},
	listeners: {},
	keysActions: {},
	handlers: {},
	cssRules: function _() {
		
	}
};
UIElementTypes.windowInfo = {
	onInit: function _() {
		
	},
	listeners: {
		infoShow: function _(structure) {
		/**
		 * %structure% is either a div containing a single-level sequence of 
		 * DOM nodes, or DocumentFragment.
		 */
			while (this.rootElement.children.length > 0) {
				this.rootElement.removeChild(this.rootElement.children[0]);
			}
			this.rootElement.appendChild(structure);
			this.show();
		},
		infoHide: function _() {
			this.hide();
		}
	},
	handlers: {},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			width: 120px;			\
			padding: 5px;			\
			border-radius: 6px;		\
			background-color: #333;	\
		}							\
		";
	}
};
UIElementTypes.windowGameAlert = {
	onInit: function _() {
		var nText = document.createElement("div");
		var nStructure = document.createElement("div");
		var nOkWrap = document.createElement("div");
		var nOk = document.createElement("div");
		var nTextText = document.createTextNode("");
		
		this.addCustomClass(nText, "Text");
		this.addCustomClass(nOkWrap, "OkWrap");
		this.addCustomClass(nOk, "Ok");
				
		this.chooseElementAsCloseButton(nOk);
		nText.appendChild(nTextText);
		nOk.appendChild(document.createTextNode("Ok"));
		nOkWrap.appendChild(nOk);
		this.rootElement.appendChild(nText);
		this.rootElement.appendChild(nStructure);
		this.rootElement.appendChild(nOkWrap);
		this.rootElement.appendChild(nOk);
		this.rootElement.appendChild(document.createElement("br"));
		
		this.setData("textNodeText", nTextText);
		this.setData("structureNode", nStructure);
	},
	listeners: {
		alert: function _(data) {
			var nStructure = this.getData("structureNode");
			while (nStructure.children.length > 0) {
			// In any case, we need to remove contents of this
				nStructure.removeChild(nStructure.children[0]);
			}
			if (data instanceof DocumentFragment) {
			// Show DOM nodes structures
				this.getData("textNodeText").nodeValue = "";
				nStructure.appendChild(data);
				nStructure.appendChild(data);				
			} else {
			// Show plain text	
				this.getData("textNodeText").nodeValue = data;
			}
			
			this.show();
		}
	},
	keysActions: {},
	handlers: {},
	cssRules: function _() {
		return "						\
		div.$type$ {					\
			width: 300px;				\
			top: 5px;					\
			left: 5px;					\
			text-align: left;			\
			padding: 20px;				\
			border-radius: 20px;		\
			margin: 10px;				\
		}								\
		div.$type$Text {				\
			display: table-row;			\
			text-align: left;			\
			padding: 10px 10px 4px 10px;\
			line-height: 20px;			\
			font-family: sans-serif;	\
			color: #fff;				\
			font-size: 13px;			\
		}								\
		div.$type$OkWrap {				\
			display: table-row;			\
		}								\
		div.$type$Ok {					\
			display: inline-block;		\
			width: 50px;				\
			line-height: 12px;			\
			text-align: center;			\
			background-color: #cec996;	\
			border: 1px solid #b9b885;	\
			padding: 4px;				\
			margin: 6px;				\
			color: #979663;				\
			cursor: pointer;			\
			border-radius: 6px;			\
		}								\
		";		
	}
};
UIElementTypes.windowAccountCharacters = {
	onInit: function _() {		
		var nHeaderDiv = document.createElement("div");
		var nPlayersList = document.createElement("div");
		var nButtonNewCharacter = document.createElement("div");
		var nButtonBack = document.createElement("div");
		
		this.addCustomClass(nHeaderDiv, "Header");
		nButtonBack.addClass("buttonBlack");
		nButtonBack.style.float = "right";
		nButtonNewCharacter.addClass("buttonBlack");
		nButtonNewCharacter.style.float = "left";
		this.addCustomClass(nPlayersList, "PlayersList");
		
		this.bindHandlerToUIElementContext(nButtonBack, "click", "backClick");
		this.bindHandlerToUIElementContext(nButtonNewCharacter, "click", "newPlayerClick");
		
		nButtonBack.appendChild(document.createTextNode("Назад"));
		nButtonNewCharacter.appendChild(document.createTextNode("Создать нового"));
		nHeaderDiv.appendChild(document.createTextNode("Персонажи на вашем аккаунте"));
		this.rootElement.appendChild(nHeaderDiv);
		this.rootElement.appendChild(nPlayersList);
		this.rootElement.appendChild(nButtonNewCharacter);
		this.rootElement.appendChild(nButtonBack);
		
		this.setData("playersListNode", nPlayersList);
	},
	listeners: {
		accountPlayersRecieve: function _(data) {
			// Clear players list
			// data: [[characterId, name, class, race, equipment]xN]
			Net.accountPlayers = data;
			var nPlayersList = this.getData("playersListNode");
			while (nPlayersList.children.length>0) {
				nPlayersList.children[0].parentNode.removeChild(nPlayersList.children[0]);
			}
			// Формируем новый список игроков
			var nDivPrototype = document.createElement("div");
			for (var i=0; i<data.length; i++) {
				var nDiv = document.createElement("div");
				this.addCustomClass(nDiv, "Player");
				this.addEventListener(nDiv, "click", "playerClick");
				
				nDiv.appendChild(document.createTextNode(
					data[i][1]+" - "+Global.races[data[i][3]]+" " +
					data[i][2]
				));
				nPlayersList.appendChild(nDiv);
				nDiv.setData("characterId", data[i][0]);
				nDiv.setData("name", data[i][1]);
				nDiv.setData("uiElement", this);
			}
			this.show();
		},
		accountPlayersListCall: function _() {
			this.show();
		},
		locationLoad: function() {
			this.hide();
		},
		worldLoad: "locationLoad"
	},
	keysActions: {},
	handlers: {
		playerClick: function _() {
			Net.logInForCharacter(this.getData("characterId"), Global.playerLogin, Global.playerPassword);
			UI.showAlwaysShownElements();
			this.getData("uiElement").hide();
			showLoadingScreen();
		},
		backClick: function _() {
			this.hide();
			UI.notify("serverInfoRecieve");
		},
		newPlayerClick: function _() {
			this.hide();
			UI.notify("playerCreateStart");
		}
	},
	cssRules: function _() {
		return "			  			\
		div.$type$ {					\
			width: 400px;				\
			height: 300px;				\
			padding: 20px;				\
			box-sizing: border-box;		\
			border-radius: 20px;		\
		}								\
		div.$type$PlayersList {			\
			height: 182px;				\
		}								\
		div.$type$Player {				\
			font-size: 18px;			\
			color: #fff;				\
			text-align: center;			\
			border: 1px solid #777;		\
			background-color: #000;		\
			padding: 10px 0px 10px 0px;	\
			cursor: pointer;			\
			border-radius: 5px;			\
			margin: 0px 0px 4px 0px;	\
		}								\
		div.$type$Player:hover {		\
			border: 1px solid #377;		\
		}								\
		div.$type$Header {				\
			text-align: center;			\
			color: #fff;				\
			font-size: 22px;			\
			margin-bottom: 10px;		\
		}								\
		";
	}
};
UIElementTypes.windowLogin = {
	onInit: function _() {
		var nServerName = document.createElement("div");
		var nServerAddress = document.createElement("div");
		var nServerOnline = document.createElement("div");		
		var nForm = document.createElement("div");	
		var nServerNameText = document.createTextNode("");
		var nServerAddressText = document.createTextNode("");
		var nServerOnlineText = document.createTextNode("");
		
		var nInputLogin = document.createElement("input");
		nInputLogin.setAttribute("type", "text");
		nInputLogin.setAttribute("name", "login");
		var nInputPassword = document.createElement("input");
		nInputPassword.setAttribute("type", "text");
		nInputPassword.setAttribute("name", "password");
		
		var nInputSubmit = document.createElement("div");
		nInputSubmit.appendChild(document.createTextNode("Войти"));
	
		var nInputRegister = document.createElement("div");
		nInputRegister.appendChild(document.createTextNode("Создать аккаунт"));
		
		var nLoginError = document.createElement("div");
		
		nInputSubmit.addClass("buttonBlack");
		nInputRegister.addClass("buttonBlack");
		this.addCustomClass(nForm, "Form");
		
		nServerName.addClass("windowHeader");
		this.addCustomClass(nServerAddress, "ServerAddress");
		this.addCustomClass(nServerName, "ServerName");
		this.addCustomClass(nServerOnline, "ServerOnline");
		this.addCustomClass(nInputLogin, "Login");
		this.addCustomClass(nInputPassword, "Password");
		this.addCustomClass(nInputRegister, "Register");
		this.addCustomClass(nInputSubmit, "Submit");
		this.addCustomClass(nLoginError, "LoginError");
		
		nServerOnline.appendChild(document.createTextNode("Онлайн: "));
		nServerOnline.appendChild(document.createElement("span"));
		
		nForm.addEventListener("submit", function(e){e.preventDefault();}, false);
		this.bindHandlerToUIElementContext(nInputSubmit, "click", "loginClick");
		this.bindHandlerToUIElementContext(nInputRegister, "click", "createAccountClick");
		
		nServerAddress.appendChild(nServerAddressText);
		nServerName.appendChild(nServerNameText);
		nServerOnline.appendChild(nServerOnlineText);
		nForm.appendChild(nServerName);
		nForm.appendChild(nServerAddress);
		nForm.appendChild(nServerOnline);
		nForm.appendChild(nInputLogin);
		nForm.appendChild(document.createElement("br"));
		nForm.appendChild(nInputPassword);
		nForm.appendChild(document.createElement("br"));
		nForm.appendChild(nLoginError);
		this.rootElement.appendChild(nForm);
		this.rootElement.appendChild(nInputRegister);
		this.rootElement.appendChild(nInputSubmit);		
		
		this.setData("loginInputNode", nInputLogin);
		this.setData("passwordInputNode", nInputPassword);
		this.setData("serverNameTextNode", nServerNameText);
		this.setData("serverAddressTextNode", nServerAddressText);
		this.setData("serverOnlineTextNode", nServerOnlineText);
		this.setData("loginErrorNode", nLoginError);
	},
	listeners: {
		serverInfoRecieve: function _(notifier) {
			window.currentServer = Net.getServer(serverAddress);
			if (window.currentServer) {
			// Если удалось получить данные о предыдущем посещённом сервере 
			// (getServer возвращает false в случае неудачи)
				this.getData("loginInputNode").value = currentServer[1];
				this.getData("passwordInputNode").value = currentServer[2];
			}
			this.getData("serverNameTextNode").nodeValue = Net.serverName;
			this.getData("serverAddressTextNode").nodeValue = "http://"+window.location.host;
			this.getData("serverOnlineTextNode").nodeValue = Net.online;
			this.show();
			setTimeout(function() {
				performAction("login",["1","1"]);
				setTimeout(function() {
					performAction("logInForCharacter", ["Alvoi"]);
				}, 30);
			},30);
		},
		accountPlayersRecieve: function _() {
			this.hide();
		},
		loginError: function _(data) {
			if (data === 0) {
				winkElement(this.getData("loginErrorNode"),"Сервер переполнен");
			} else if (data == 1) {
				winkElement(this.getData("loginErrorNode"),"Пустой логин!");
			} else if (data == 2) {
				winkElement(this.getData("loginErrorNode"),"Пустой пароль!");
			} else if (data == 3) {
				winkElement(this.getData("loginErrorNode"),"Введён неверный пароль или такого аккаунта не существует");
			} else {
				winkElement(this.getData("loginErrorNode"),"Неизвестная ошибка при заходе в игру");
			}
		},
		login: function _() {
			this.hide();
		}
	},
	keysActions: {},
	handlers: {
		loginClick: function _(e) {
		// Отправка логина и пароля
		// callback используется ровно в одном месте: вызов stLoginForm.onsubmit() из stChooseServerForm.onsubmit(), 
		// это требуется для реализации "быстрой перезагрузки".
			var login = this.getData("loginInputNode").value;
			var password = this.getData("passwordInputNode").value;
			if (login == "") {
				winkElement(this.getData("loginErrorNode"), "Empty login!");
				return;
			}
			Global.playerLogin = login;
			Global.playerPassword = password;
			performAction("login", [login, password]);
			e.preventDefault();
		},
		createAccountClick: function _() {
			this.hide();
			UI.notify("accountCreateStart");
		}
	},
	cssRules: function _() {
		return "						\
		div.$type$ {					\
			width: 400px;				\
			height: 300px;				\
			border-radius: 20px;		\
			padding: 20px 20px 58px 20px;	\
			box-sizing: border-box;		\
		}								\
		div.$type$Form {				\
			text-align: center;			\
			height: 100%;				\
		}								\
		div.$type$ServerName {			\
			font-variant: small-caps;	\
		}								\
		div.$type$ServerAddress {		\
			font-family: sans-serif;	\
			font-size: 12px;			\
			-moz-user-select: -moz-text;\
			-webkit-user-select: text;	\
			user-select: text;			\
			text-decoration: underline;	\
			color: #889 !important;		\
			padding: 4px;				\
			text-align: center;			\
		}								\
		div.$type$ServerOnline {		\
			font-sze: 16px;				\
			color: #fff;				\
			text-align: center;			\
			margin-bottom: 20px;		\
		}								\
		div.$type$Register {			\
			display: block;				\
			float: left;				\
		}								\
		div.$type$Submit {				\
			display: block;				\
			float: right;				\
		}								\
		div.$type$LoginError {			\
			height: 20px;				\
			line-height: 20px;			\
			color: #f55;				\
			text-align: center;			\
			font-size: 13px;			\
		}								\
		input.$type$Login, input.$type$Password	{	\
			margin: 2px;				\
			border-radius: 6px;			\
		}								\
		";
//		var textInputStyle = {
//			borderRadius: "5px",
//			padding: "5px",
//			margin: "5px 0px 0px 0px"
//		};
	}
};
UIElementTypes.iconsInventory = {
 	onInit: function iconsInventoryOnInit() {
 		this.setData("displayedNullViews",[]);
 		this.setData("itemLetters", []);
 		this.setData("keymappingPutOn", new UIKeymapping(
 				"Icons inventory put on",
 				["Esc", "quitSelection"],
 				["?", "showCurrentKeymapping"]
 		));
 		this.getData("keymappingPutOn").listenToLetterAssigner(Player.itemsLetterAssigner, "putOn");
 		this.setData("keymappingDrop", new UIKeymapping(
 				"Icons inventory drop",
 				["Esc", "quitSelection"]
 		));
 		this.getData("keymappingDrop").listenToLetterAssigner(Player.itemsLetterAssigner, "drop");
 		this.setData("keymappingPutToContainer", new UIKeymapping(
 				"Icons inventory put to container",
 				["Esc", "quitSelection"]
 		));
 		this.getData("keymappingPutToContainer").listenToLetterAssigner(Player.itemsLetterAssigner, "putToContainer");
 		this.setData("keymappingSelectMissile", new UIKeymapping(
 				"Icons inventory select missile",
 				["Esc", "quitSelection"],
 				["?", "showCurrentKeymapping"]
 		));
 		this.getData("keymappingSelectMissile").listenToLetterAssigner(Player.itemsLetterAssigner, "selectMissile");
 		this.setData("displayedItemViews", new HashSet());
 	},
 	listeners: {
 		inventoryChange: function _(notifier) {
	 		var count = 0;
	 		var items = Player.items.getValues();
	 		/** @type HashSet */
	 		var displayedItemViews = this.getData("displayedItemViews");
	 		var displayedNullViews = this.getData("displayedNullViews");
	 		while (displayedNullViews.length > 0) {
	 			this.rootElement.removeChild(displayedNullViews[0]);
	 			displayedNullViews.shift();
	 		}
	 		for (var i=0; i<items.length; i++) {
	 			count++;
	 			if (displayedItemViews.contains(items[i])) {
	 			// If item is already displayed, check if it is an ItemPile 
	 			// that changed its amount, otherwise just leave it as it is.
	 				if (items[i].amount != displayedItemViews.getEqual(items[i]).amount) {
	 					displayedItemViews.getEqual(items[i]).changeAmount(items[i].amount);
	 				}
	 				continue;
	 			}
	 			// If item is not displayed yet, display it.
	 			var newItemView = CachingItemViewFactory.get(items[i], "Inventory");
	 			displayedItemViews.add(newItemView);
	 			this.rootElement.appendChild(newItemView.rootElement);
	 			this.addEventListener(newItemView.rootElement, "click", "click");
	 			this.addEventListener(newItemView.rootElement, "contextmenu", "contextmenu");
	 			this.addEventListener(newItemView.rootElement, "mouseout", "mouseout");
	 		}
	 		var uiElement = this;
	 		displayedItemViews.forEach(function(view) {
	 		// Undisplay removed items
	 			if (!Player.items.contains(view.item)) {
	 				uiElement.rootElement.removeChild(view.rootElement);
	 				displayedItemViews.remove(view);
	 			}
	 		});
	 		while (count%6 != 0) {
	 			var nullView = NullCachingItemViewFactory.get("Inventory").getNode();
	 			this.rootElement.appendChild(nullView);
	 			displayedNullViews.push(nullView);
	 			count++;
	 		}
 		},
 		quitSelection: function quitSelection() {
			var nlLetters = this.getData("itemLetters");
			for (var i=0; i<nlLetters.length; i++) {
				nlLetters[i].parentNode.removeChild(nlLetters[i]);
			}
			this.setData("itemLetters", []);
			UI.setKeyMapping("Default");
		},
 		locationLoad: "inventoryChange",
 		worldLoad: "inventoryChange"
 	},
 	keysActions: {
 		iconsInventoryShowKeysToPutOn: function _() {
 			Player.itemsLetterAssigner.synchronizeWithSource();
			var itemViews = this.getData("displayedItemViews");
			var uiElement = this;
			var keymapping = this.getData("keymappingPutOn");
			var itemLetters = this.getData("itemLetters");
			itemViews.forEach(function(itemView) {
				var nLetter = CachingLetterFactory.get(
					"defaultLetter", 
					keymapping.assignedLetters.get(itemView)
				);
				itemView.getNode().appendChild(nLetter);
				itemLetters.push(nLetter);
			});
			UI.setKeyMapping("Icons inventory put on");
		},
		iconsInventoryShowKeysToDrop: function _() {
			Player.itemsLetterAssigner.synchronizeWithSource();
			var itemViews = this.getData("displayedItemViews");
			var uiElement = this;
			itemViews.forEach(function(itemView) {
				var nLetter = CachingLetterFactory.get(
					"defaultLetter", 
					Player.itemsLetterAssigner.getLetter(itemView.item)
				);
				itemView.getNode().appendChild(nLetter);
				uiElement.getData("itemLetters").push(nLetter);
			});
			UI.setKeyMapping("Icons inventory drop");
		},
		iconsInventoryShowKeysToSelectMissile: function _() {
			Player.itemsLetterAssigner.synchronizeWithSource();
			var itemViews = this.getData("displayedItemViews");
			var uiElement = this;
			var keymapping = this.getData("keymappingSelectMissile");
			var itemLetters = this.getData("itemLetters");
			itemViews.forEach(function(itemView) {
				var nLetter = CachingLetterFactory.get(
					"defaultLetter", 
					keymapping.assignedLetters.get(itemView)
				);
				itemView.getNode().appendChild(nLetter);
				itemLetters.push(nLetter);
			});
			UI.setKeyMapping("Icons inventory select missile");
		}
 	},
 	handlers: {
		click: function _(e) {
			var typeId = this.getData("typeId");
			var param = this.getData("param");
			if (Terrain.onGlobalMap && isEquipment(typeId)) {
			// На глобальной карте
				var slot = getSlotFromClass(items[typeId][1]);
				if (!Player.equipment.hasItemInSlot(slot)) {
					Player.sendPutOn(param);
				} else {
					performAction("takeOff",[Player.equipment.getItemInSlot(slot)]);
				}
			} else if (UI.mode == UI.MODE_CONTAINER) {
			// Положить в контейнер
				Player.sendPutToContainer(typeId, (e.shiftKey ? 1 : param));
			} else if (!Terrain.onGlobalMap && e.shiftKey) {
			// Выкинуть предмет (шифт-клик)
				if (isUnique(typeId)) {
					performAction("drop", [Player.items.getUnique(param)]);
				} else {
					performAction("drop", [Player.items.getPile(typeId)]);
				}
			} else if (isUsable(typeId)) {
			// Если предмет можно использовать (на локальной карте), то использовать его
				performAction("useItem", [Player.items.getItem(typeId, param)]);
			} else if (isEquipment(typeId)) {
			// Если предмет можно надеть, то надеть его
				var slot = getSlotFromClass(items[typeId][1]);
				performAction("putOn", [Player.items.getItem(typeId, param)]);
			}
		},
		contextmenu: function _(e) {
			var typeId = this.getData("typeId");
			if (typeId == -1) {
				return;
			}
			
			// Get item information
			if (isWeapon(typeId)) {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+typeId, function () {
					return new MarkupMaker()
						.add("b").text(items[typeId][0])
						.add("div").text("Урон: "+items[typeId][4])
						.add("div").text("Скорость: "+items[typeId][6])
						.getWrappedContents();
				}));
			} else if (isEquipment(typeId)) {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+typeId, function () {
					return new MarkupMaker()
						.add("b").text(items[typeId][0])
						.add("div").text("Защита: "+items[typeId][4])
						.add("div").text("Тяжесть: "+items[typeId][5])
						.getWrappedContents();
				}));
			} else {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+typeId, function () {
					return new MarkupMaker()
						.add("b").text(items[typeId][0])
						.getWrappedContents();
				}));
			}
			return false;
		},
		mouseout: function _(e) {
			UI.notify("infoHide");
		}
	},
	cssRules: function _() {
		return "						\
		div.$type$ {					\
			max-width: 204px;			\
			width: 204px;				\
		}								\
 		";		
	}
};
UIElementTypes.minimap = {
	onInit: function _() {
		 var nMap = document.createElement("canvas");
		 var minimap = new Minimap(nMap);
		 
		 this.bindHandlerToUIElementContext(minimap.elem, "mousemove", "mousemove");
		 this.bindHandlerToUIElementContext(minimap.elem, "click", "click");
		 
		 this.rootElement.appendChild(nMap);
		 
		 this.setData("minimap", minimap);
	},
	listeners: {
		environmentChange: function _(notifier) {
		 	this.getData("minimap").draw();
	 	},
	 	cameraRotation: function _() {
	 		this.getData("minimap").init();		
	 	},
	 	locationLoad: function _() {
	 		this.getData("minimap").changeDimensions(Terrain.width, Terrain.height);
	 	}
	},
	keysActions: {},
 	handlers: {
		click: function _(e) {
			var minimap = this.getData("minimap");
			var rect = getOffsetRect(minimap.elem);
			var normal = Terrain.getNormalView(
				Math.floor((e.clientX-rect.left)/minimap.scale), 
				Math.floor((e.clientY-rect.top)/minimap.scale)
			);
			UI.gameFieldClick(normal.x, normal.y, e);
		},
		mousemove: function _(e) {
			var minimap = this.getData("minimap");
			var rect=getOffsetRect(minimap.elem);
			CellCursor.move(Math.floor((e.clientX-rect.left)/minimap.scale),Math.floor((e.clientY-rect.top)/minimap.scale));
		}
	}
};
UIElementTypes.chat = {
	onInit: function _() {
		var nForm = document.createElement("form");
		nForm.setAttribute("name", "chatForm");
		nForm.setAttribute("method", "");
		nForm.setAttribute("action", "");
		var nWrap1 = document.createElement("div");
		var nWrap2 = document.createElement("div");
		var nBg = document.createElement("div");
		var nWindow = document.createElement("div");
		var nInput = document.createElement("input");
		nInput.setAttribute("type", "text");
		nInput.setAttribute("name", "chat");
		nInput.setAttribute("autocomplete", "off");
		
		nWrap1.addClass("wrap");
		nWrap2.addClass("wrap");
		this.addCustomClass(nWrap1, "Wrap");
		this.addCustomClass(nWrap2, "Wrap");
		this.addCustomClass(nInput, "Input");
		this.addCustomClass(nForm, "Form");
		this.addCustomClass(nBg, "Bg");
		this.addCustomClass(nWindow, "Window");
		
		this.onTopLayer();
		
		nWrap1.appendChild(nBg);
		nWrap2.appendChild(nWindow);
		nForm.appendChild(nWrap1);
		nForm.appendChild(nWrap2);
		nForm.appendChild(nInput);
		this.rootElement.appendChild(nForm);
		
		this.addEventListener(nInput, "focus", "inputOnFocus");
		this.addEventListener(nInput, "blur", "inputOnBlur");
		
		this.setData("inputNode", nInput);
		this.setData("formNode", nForm);
		this.setData("windowNode", nWindow);
	},
	listeners: {
		chatMessage: function _() {
			for (var i=0;i<chat.length; i++) {
				var sender = chat[i][0];
				var message = chat[i][1];
				if (sender === undefined) {
					sender = Player.name;
				}
				
				var nB=document.createElement("b");
				var nSpan = document.createElement("span");
				var nWindow = this.getData("windowNode");
				
				nB.appendChild(document.createTextNode(sender+": "));
				nWindow.appendChild(nB);
				nSpan.appendChild(document.createTextNode(message));
				nWindow.appendChild(nSpan);
				nWindow.appendChild(document.createElement("br"));
			}
			chat = [];
		},
		locationLoad: "chatMessage",
		worldLoad: "chatMessage"
	},
	keysActions: {
 		focusOnChat: function _() {
			this.getData("inputNode").focus();
		},
		closeChat: function _() {
			this.getData("inputNode").blur();
		},
		sendToChat: function _() {
			if (this.getData("inputNode").value == "") {
				return;
			}
			Net.send({a:Net.CHAT_MESSAGE,text:this.getData("inputNode").value},function() {});
			this.getData("inputNode").value="";
		}
	},
 	handlers: {
		inputOnFocus: function _() {
			UI.setKeyMapping("Chat");
			var nlWraps=this.parentNode.children;
			// First two children are wraps with background and text area
			for (var i=0;i<2;i++) {
				nlWraps[i].style.display="block";
			}
			this.applyStyle({
				borderTopLeftRadius: "0px",
				borderTopRightRadius: "0px"				
			});
		},
		inputOnBlur: function _() {
			UI.setKeyMapping("Default");
			var nlWraps=this.parentNode.children;
			// First two children are wraps with background and text area
			for (var i=0;i<2;i++) {
				nlWraps[i].style.display="none";
			}
			this.applyStyle({
				borderTopLeftRadius: "6px",
				borderTopRightRadius: "6px"				
			});
		}	
	},
	cssRules: function _() {
		return "						\
		form.$type$Form {				\
			display: block;				\
			width: 200px;				\
		}								\
		div.$type$Bg {					\
			z-index: 1;					\
			height: 200px;				\
			width: 100%;				\
			background-color: #3f3f41;	\
			opacity: 0.8;				\
			position: absolute;			\
			top: -200px;				\
			border-top-left-radius: 6px;	\
			border-top-right-radius: 6px;	\
			vertical-align: top;		\
		}								\
		div.$type$Window {				\
			z-index: 3;					\
			box-sizing: border-box;		\
			padding: 4px;				\
			height: 200px;				\
			width: 100%;				\
			color: #fff;				\
			position: absolute;			\
			top: -200px;				\
			font-family: sans-serif;	\
			border: 2px solid #3f3f4f;	\
			font-size: 11px;			\
			border-top-left-radius: 6px;	\
			border-top-right-radius: 6px;	\
			overflow-y: scroll;			\
			overflow-x: hidden;			\
			text-overflow: ellipsis;	\
			vertical-align: top;		\
		}								\
		input.$type$Input {				\
			background-color: #3f3f4f;	\
			width: 100%;				\
			max-width: 100%;			\
			border: 2px solid #3f3f4f;	\
			color: #fff;				\
			box-sizing: border-box;		\
			border-radius: 6px;			\
		}								\
		div.$type$Wrap {				\
			display: none;				\
			box-sizing: border-box;		\
			top: 0px;					\
		}								\
		";
	}
};
UIElementTypes.iconMissileType = {
	onInit: function () {
		var itemView = CachingItemViewFactory.get(new UniqueItem(2300,1), "Equipment");
		this.rootElement.appendChild(itemView.getNode());
		this.setData("imgNode", itemView.getImg());
	},
	listeners: {
		missileSelect: function _() {
			if (Player.selectedMissile === null) {
				this.getData("imgNode").setAttribute("src", "./images/intf/nothing.png");
			} else {
				this.getData("imgNode").setAttribute("src", "./images/items/"+Player.selectedMissile.typeId+".png");
			}
		}
	},
	keysAction: {
		iconMissileTypeSelect: function _() {
			
		}
	},
	handlers: {},
	cssRules: function _() {
		
	}
};
UIElementTypes.iconsEquipment = {
	onInit: function _() {	
		this.setData("itemViews", []);
		this.setData("keymappingTakeOff", new UIKeymapping(
				"IconsEquipmentTakeOff",
				["Esc", "quitSelection"]
		));
		this.getData("keymappingTakeOff").listenToLetterAssigner(Player.itemsLetterAssigner, "takeOff");
		var itemViews = this.getData("itemViews");
		for (var i=0; i<10; i++) {
			var itemView = NullCachingItemViewFactory.get("Equipment");
			itemViews.push(itemView);
			var nImg = itemView.getImg();
			this.addEventListener(nImg, "click", "click");
			this.addEventListener(nImg, "contextmenu", "contextmenu");
			this.addEventListener(nImg, "mouseout", "mouseout");
			this.rootElement.appendChild(itemView.getNode());
		}
		this.setData("displayedLetters", []);
	}, 
	listeners: {
		equipmentChange: function _() {
			var itemViews = this.getData("itemViews");
			for (var i=0; i<itemViews.length; i++) {
				var nImg = itemViews[i].getImg();
				var src, typeId, itemId;
				var item = Player.equipment.getItemInSlot(i);
				if (item === null) {
					src = "./images/intf/nothing.png";
					typeId = -1;
					itemId = -1;
				} else {
					src = "./images/items/"+item.typeId+".png";
					typeId = item.typeId;
					itemId = item.itemId;
				}
				nImg.setAttribute("src", src);
				nImg.setData("typeId", typeId);
				nImg.setData("itemId", itemId);
				nImg.parentNode.setData("item", item);
			}
		},
		quitSelection: function quitSelection() {
			var displayedLetters = this.getData("displayedLetters");
			for (var i=0; i<displayedLetters.length; i++) {
				displayedLetters[i].parentNode.removeChild(displayedLetters[i]);
			}
			UI.setKeyMapping("Default");
			this.setData("displayedLetters", []);
		},
		locationLoad: "equipmentChange",
		worldLoad: "equipmentChange"
	},
	keysActions: {
		iconsEquipmentTakeOff: function _(item) {
			var displayedLetters = this.setData("displayedLetters", []);
			var keymapping = this.getData("keymappingTakeOff");
			for (var i=0; i<this.rootElement.children.length; i++) {
				var item = this.rootElement.children[i].getData("item");
				if (item === null) {
					continue;
				}
				var nLetter = CachingLetterFactory
					.get("defaultLetter", keymapping.assignedLetters.get(item));
				this.rootElement.children[i].appendChild(nLetter);
				displayedLetters.push(nLetter);
			}
			UI.setKeyMapping("IconsEquipmentTakeOff");
		}
	},
 	handlers: {
		click: function _() {
			var itemId = this.getData("itemId");
			if (itemId != -1) {
			// If a piece of armor is put on, take if off.
				performAction("takeOff",[Player.equipment.getItemById(itemId)]);
			}
		},
		contextmenu: function _(e) {
			var itemId = this.getData("typeId");
			if (itemId == -1) {
				return;
			}
			var slot; // Номер слота амуниции (см. items.js/items.php)
			var itemInfo; // Информация о предмете
			var itemNum=this.getAttribute("param");
			var nItemInfo=document.getElementById("itemInfo");
			var itemArr=items[itemId];
			if (nItemInfo.style.display == "block") {
			// Повторный правый клик
				nItemInfo.style.display = "none";
				return false;
			}
			
			// Get item information
			if (isWeapon(itemId)) {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+itemId, function () {
					return new MarkupMaker()
						.add("b").text(items[itemId][0])
						.add("div").text("Урон: "+items[itemId][4])
						.add("div").text("Скорость: "+items[itemId][6])
						.getWrappedContents();
				}));
			} else if (isEquipment(itemId)) {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+itemId, function () {
					return new MarkupMaker()
						.add("b").text(items[itemId][0])
						.add("div").text("Защита: "+items[itemId][4])
						.add("div").text("Тяжесть: "+items[itemId][5])
						.getWrappedContents();
				}));
			} else {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+itemId, function () {
					return new MarkupMaker()
						.add("b").text(items[itemId][0])
						.getWrappedContents();
				}));
			}
			return false;
		},
		mouseout: function _(e) {
			UI.notify("infoHide");
		}
	},
 	cssRules: function _() {
 		return "						\
 		div.$type$ {					\
 			width: 204px;				\
 			font-size: 0px;				\
 			text-align: center;			\
 		}								\
 		";
 	}
};
UIElementTypes.iconsLoot = {
	onInit: function _() {
		this.setData("displayedNullViews",[]);
		this.setData("itemLetters", []);
		this.setData("displayedItemViews", new HashSet());
		this.setData("keymappingPickUp", new UIKeymapping(
				"IconsLootPickUp",
				["Esc", "quitSelection"]
		));
		this.getData("keymappingPickUp").listenToLetterAssigner(Player.lootLetterAssigner, "pickUp");
	},
	listeners: {
		lootChange: function _() {
	 		var count = 0;
	 		var items = Terrain.getItemsOnCell(Player.x,Player.y);
	 		var displayedItemViews = this.getData("displayedItemViews");
	 		displayedItemViews.empty();
	 		while (this.rootElement.children.length > 0) {
	 		// Remove all the children.
	 			this.rootElement.removeChild(this.rootElement.children[0]);
	 		}
	 		for (var i in items) {
	 			var itemView = CachingItemViewFactory.get(items[i], "Loot");
	 			var nItem = itemView.rootElement;
	 			displayedItemViews.add(itemView);
				this.addEventListener(nItem, "click", "click");
	 			this.addEventListener(nItem, "contextmenu", "contextmenu");
	 			this.addEventListener(nItem, "mouseout", "mouseout");
	 			this.rootElement.appendChild(nItem);
	 			count++;
	 		}
	 		while (count%6 != 0) {
	 			var nullView = NullCachingItemViewFactory.get("Loot").getNode();
	 			this.rootElement.appendChild(nullView);
	 			count++;
	 		}
		},
		quitSelection: function quitSelection() {
			var nlLetters = this.getData("itemLetters");
			for (var i in nlLetters) {
				nlLetters[i].parentNode.removeChild(nlLetters[i]);
			}
			this.setData("itemLetters", []);
			UI.setKeyMapping("Default");
		},
		locationLoad: "lootChange"
	},
	keysActions: {
		iconsLootShowKeysToPickUp: function _() {
			Player.lootLetterAssigner
				.synchronizeWithSource(Terrain.getCell(Player.x, Player.y).items);
			var itemViews = this.getData("displayedItemViews");
			var uiElement = this;
			var itemsLetters = this.getData("itemLetters");
			var keymapping = this.getData("keymappingPickUp");
			itemViews.forEach(function(itemView) {
				var nLetter = CachingLetterFactory.get(
					"defaultLetter", 
					keymapping.assignedLetters.get(itemView)
				);
				itemView.getNode().appendChild(nLetter);
				itemsLetters.push(nLetter);
			});
			UI.setKeyMapping("IconsLootPickUp");
		}
	},
 	handlers: {
		click : function _() {
			document.getElementById("itemInfo").style.display="none";
			var typeId = this.getData("typeId");
			var item;
			if (isUnique(typeId)) {
				var param = this.getData("param");
				item = Terrain.getCell(Player.x,Player.y).items.getUnique(param);
			} else {
				item =  Terrain.getCell(Player.x,Player.y).items.getPile(typeId);
			}
			performAction("pickUp", [item]);
		},
		contextmenu:function _(e) {
			var typeId = this.getData("typeId");
			var itemNum = this.getData("param");
			var itemArr = items[typeId];
			// Get item information
			if (isWeapon(typeId)) {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+typeId, function () {
					return new MarkupMaker()
						.add("b").text(items[typeId][0])
						.add("div").text("Урон: "+items[typeId][4])
						.add("div").text("Скорость: "+items[typeId][6])
						.getWrappedContents();
				}));
			} else if (isEquipment(itemId)) {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+itemId, function () {
					return new MarkupMaker()
						.add("b").text(items[typeId][0])
						.add("div").text("Защита: "+items[typeId][4])
						.add("div").text("Тяжесть: "+items[typeId][5])
						.getWrappedContents();
				}));
			} else {
				UI.notify("infoShow", UI.getStaticDOMStructure("item"+itemId, function () {
					return new MarkupMaker()
						.add("b").text(items[typeId][0])
						.getWrappedContents();
				}));
			}
			return false;
		},
		mouseout:function _(e) {
			UI.notify("infoHide");
		}
	},
 	cssRules: function _() {
 		return "						\
 		div.$type$ {					\
 			width:204px;				\
 		}								\
 		";
 	}
};
UIElementTypes.iconsSpells = {
	onInit: function _() {
    	
    },
    listeners: {
    	unselectCellAction: function _(notifier) {
    	// Refresh highlighted spell icon
	    	var nlSpells = this.rootElement.getElementsByTagName("img");
	    	for (var i=0;i<nlSpells.length;i++) {
	    		if (nlSpells[i].getData("spellId") == Player.spellId) {
	    			nlSpells[i].style.opacity = "1";
	    			break;
	    		} else if (i == nlSpells.length-1) {
	    			throw new Error("Highlighted spell icon not found!");
	    		}
	    	}
		},
		locationLoad: function _() {
    	// Build UIElement's contents from scratch
    		while (this.rootElement.children.length > 0) {
        		this.rootElement.removeChild(this.rootElement.children[0]);
        	}
        	for (var i=0; (i<Player.spells.length || i%6!=0); i++) {
        		var nSpellWrap = CachingSpellFactory.get(i<Player.spells.length ? Player.spells[i] : null);
        		this.addEventListener(nSpellWrap.children[0], "click", "click");
        		this.addEventListener(nSpellWrap.children[0], "contextmenu", "contextmenu");
        		this.addEventListener(nSpellWrap.children[0], "mouseout",  "mouseout");
        		
        		this.rootElement.appendChild(nSpellWrap);
        	}
    	}
    },
    keysActions: {},
 	handlers: {
    	click: function _() {
			document.getElementById("itemInfo").style.display = "none";
			var spellId = this.getData("spellId");
			if (Player.spellId == spellId) {
			// If this spell is already chosen, unchoose it
				performAction("unselectCellAction");
				this.applyStyle({
					opacity: "1"
				});
			} else {
				performAction("selectSpell", [spellId]);
				if (spells[spellId].onlyOnSelf) {
					Player.sendCastSpell(Player.x, Player.y);
				} else {
					this.applyStyle({
						opacity: "0.5"
					});
					performAction("selectSpell", [spellId]);
					// Spell cursor positioning
					var spell = spells[Player.spellId];
					var aimcharacter;
					if (spell.onlyOnSelf) {
						CellCursor.move(Player.x, Player.y);
					} else if (spell.onCharacter && (aimcharacter = Player.findEnemy())) {
						CellCursor.move(aimcharacter.x, aimcharacter.y);
					} else {
						CellCursor.move(Player.x, Player.y);
					}
				}
			}
		},
		contextmenu:function _(e) {
			var spellId=this.getData("spellId");
			UI.notify("infoShow", UI.getStaticDOMStructure("spell"+spellId, function () {
				return new MarkupMaker()
					.add("b").text(spells[spellId].name)
					.getWrappedContents();
			}));
			return false;
		},
		mouseout:function _() {
			UI.notify("infoHide");
		}
    },
    cssRules: function _() {
    	return "						\
    	div.$type$ {					\
    		-webkit-box-sizing: border-box;	\
    		-moz-box-sizing: border-box;	\
    		padding: 0px 0px 0px 0px;	\
    		width: 204px;				\
    		max-width: 204px;			\
    	}								\
    	";
    }
};
UIElementTypes.textTitle = {
	onInit: function _() {
    	var nName = document.createElement("div");
    	var nTitle = document.createElement("div");
    	var nNameTextNode = document.createTextNode("");
    	var nTitleTextNode = document.createTextNode("");
    	
    	this.addCustomClass(nName, "Name");
    	this.addCustomClass(nTitle, "Title");
    	
    	nName.appendChild(nNameTextNode);
    	nTitle.appendChild(nTitleTextNode);
    	this.rootElement.appendChild(nName);
    	this.rootElement.appendChild(nTitle);
    	
    	this.setData("nameTextNode", nNameTextNode);
    	this.setData("titleTextNode", nTitleTextNode);
    },
    listeners: {
    	titleChange: function _() {
	    	this.getData("nameTextNode").nodeValue = Player.name;
	    	this.getData("titleTextNode").nodeValue = Global.races[Player.race]+" "+Player.cls+" "+Player.level+" уровня";
    	},
    	locationLoad: "titleChange",
    	worldLoad: "titleChange"
    },
    keysActions: {
 		
	},
 	handlers: {},
 	cssRules: function _() {
 		return "						\
 		div.$type$ {					\
 			display: inline-block;		\
 			width: 200px;				\
 			text-align: center;			\
 			background-color: #000;		\
 		}								\
 		div.$type$Name {				\
 			width: 100%;				\
 			color: #ff3;				\
 			font-size: 18px;			\
 		}								\
 		div.$type$Title {				\
 			font-size: 15px;			\
 			color: #fff;				\
 		}								\
 		";
 	}
};
UIElementTypes.hpBar = {
    onInit: function _() {
    	var nMainWrap = document.createElement("div");
    	var nWrap1 = document.createElement("div");
    	var nWrap2 = document.createElement("div");
    	var nBg = document.createElement("div");
    	var nStrip = document.createElement("div");
    	var nValue = document.createElement("div");
    	var nValueTextNode = document.createTextNode("");
    	
    
    	this.addCustomClass(nBg, "Bg");
    	this.addCustomClass(nStrip, "Strip");
    	this.addCustomClass(nValue, "Value");
    	
    	nWrap1.addClass("wrap");
    	nWrap2.addClass("wrap");
    	
    	nValue.appendChild(nValueTextNode);
    	this.rootElement.appendChild(nBg);
    	this.rootElement.appendChild(nStrip);
    	nWrap2.appendChild(nValue);
    	nValueTextNode.nodeValue = "blablabla";
    	this.rootElement.appendChild(nWrap2);
    	
    	this.setData("stripNode", nStrip);
    	this.setData("valueTextNode", nValueTextNode);    	
    }, 
    listeners: {
    	healthChange: function _() {
	    	this.getData("valueTextNode").nodeValue = Player.hp+"/"+Player.maxHp;
	    	this.getData("stripNode").style.width = (204*Player.hp/Player.maxHp)+"px";
    	},
    	locationLoad: "healthChange"
    },
    keysActions: {},
 	handlers: {},
 	cssRules: function _() {
 		return "						\
 		div.$type$ {					\
 			height: 20px;				\
 		}								\
 		div.$type$Bg {					\
 			display: inline-block;		\
 			background-color: #272;		\
 			width: 204px;				\
 			height: 20px;				\
 			text-align: left;			\
 			border-radius: 4px;			\
 		}								\
 		div.$type$Strip {				\
 			background-color: #4a4;		\
 			height: 20px;				\
 			position: absolute;			\
 			top: 0px;					\
 			left: 0px;					\
 			border-radius: 4px;			\
 		}								\
 		div.$type$Value {				\
 			font-size: 13px;			\
 			color: #fff;				\
 			text-align: center;			\
 			vertical-align: middle;		\
 			line-height: 20px;			\
 			height: 20px;				\
 			width: 204px;				\
 		}								\
 		div.$type$ > div.wrap {			\
 			position: absolute;			\
 			top: 0px;					\
 			left: 0px;					\
 		}								\
 		";
 	}
};
UIElementTypes.epBar = {
    onInit: function _() {
    	var nMainWrap = document.createElement("div");
    	var nWrap1 = document.createElement("div");
    	var nWrap2 = document.createElement("div");
    	var nBg = document.createElement("div");
    	var nStrip = document.createElement("div");
    	var nValue = document.createElement("div");
    	var nValueTextNode = document.createTextNode("");
    	
    
    	this.addCustomClass(nBg, "Bg");
    	this.addCustomClass(nStrip, "Strip");
    	this.addCustomClass(nValue, "Value");
    	
    	nWrap1.addClass("wrap");
    	nWrap2.addClass("wrap");
    	
    	nValue.appendChild(nValueTextNode);
    	this.rootElement.appendChild(nBg);
    	this.rootElement.appendChild(nStrip);
    	nWrap2.appendChild(nValue);
    	nValueTextNode.nodeValue = "blablabla";
    	this.rootElement.appendChild(nWrap2);
    	
    	this.setData("stripNode", nStrip);
    	this.setData("valueTextNode", nValueTextNode);    	
    }, 
    listeners: {
    	energyChange: function _() {
    	
	    	this.getData("valueTextNode").nodeValue = Player.ep+"/"+Player.maxEp;
	    	this.getData("stripNode").style.width = (204*Player.ep/Player.maxEp)+"px";
    	},
    	locationLoad: "energyChange"
    },
    keysActions: {},
 	handlers: {},
 	cssRules: function _() {
 		return "						\
 		div.$type$ {					\
 			height: 20px;				\
 		}								\
 		div.$type$Bg {					\
 			display: inline-block;		\
 			background-color: #722;		\
 			width: 204px;				\
 			height: 20px;				\
 			text-align: left;			\
 			border-radius: 4px;			\
 		}								\
 		div.$type$Strip {				\
 			background-color: #a44;		\
 			height: 20px;				\
 			position: absolute;			\
 			top: 0px;					\
 			left: 0px;					\
 			border-radius: 4px;			\
 		}								\
 		div.$type$Value {				\
 			font-size: 13px;			\
 			color: #fff;				\
 			text-align: center;			\
 			vertical-align: middle;		\
 			line-height: 20px;			\
 			height: 20px;				\
 			width: 204px;				\
 		}								\
 		div.$type$ > div.wrap {			\
 			position: absolute;			\
 			top: 0px;					\
 			left: 0px;					\
 		}								\
 		";
 	}
};
UIElementTypes.mpBar = {
    onInit: function _() {
    	var nMainWrap = document.createElement("div");
    	var nWrap1 = document.createElement("div");
    	var nWrap2 = document.createElement("div");
    	var nBg = document.createElement("div");
    	var nStrip = document.createElement("div");
    	var nValue = document.createElement("div");
    	var nValueTextNode = document.createTextNode("");
    	
    
    	this.addCustomClass(nBg, "Bg");
    	this.addCustomClass(nStrip, "Strip");
    	this.addCustomClass(nValue, "Value");
    	
    	nWrap1.addClass("wrap");
    	nWrap2.addClass("wrap");
    	
    	nValue.appendChild(nValueTextNode);
    	this.rootElement.appendChild(nBg);
    	this.rootElement.appendChild(nStrip);
    	nWrap2.appendChild(nValue);
    	nValueTextNode.nodeValue = "blablabla";
    	this.rootElement.appendChild(nWrap2);
    	
    	this.setData("stripNode", nStrip);
    	this.setData("valueTextNode", nValueTextNode);    	
    }, 
    listeners: {
    	manaChange: function _() {
	    	this.getData("valueTextNode").nodeValue = Player.mp+"/"+Player.maxMp;
	    	this.getData("stripNode").style.width = (204*Player.mp/Player.maxMp)+"px";
    	},
    	locationLoad: "manaChange"
    },
    keysActions: {},
 	handlers: {},
 	cssRules: function _() {
 		return "						\
 		div.$type$ {					\
 			height: 20px;				\
 			position: relative;			\
 		}								\
 		div.$type$Bg {					\
 			display: inline-block;		\
 			background-color: #237;		\
 			width: 204px;				\
 			height: 20px;				\
 			text-align: left;			\
 			border-radius: 4px;			\
 		}								\
 		div.$type$Strip {				\
 			background-color: #44a;		\
 			height: 20px;				\
 			position: absolute;			\
 			top: 0px;					\
 			left: 0px;					\
 			border-radius: 4px;			\
 		}								\
 		div.$type$Value {				\
 			font-size: 13px;			\
 			color: #fff;				\
 			text-align: center;			\
 			vertical-align: middle;		\
 			line-height: 20px;			\
 			height: 20px;				\
 			width: 204px;				\
 		}								\
 		div.$type$ > div.wrap {			\
 			position: absolute;			\
 			top: 0px;					\
 			left: 0px;					\
 		}								\
 		";
 	}
};
UIElementTypes.windowSkills = {
	onInit: function _() {
		var nSkills = document.createElement("div"); // Замыкается на onShow()
		var nClose = document.createElement("div");
		
		nClose.addClass("containerClose");
		this.chooseElementAsCloseButton(nClose);
		
		nClose.appendChild(document.createTextNode("Закрыть"));
		this.rootElement.appendChild(nClose);
		this.rootElement.appendChild(document.createElement("br"));
		this.rootElement.appendChild(nSkills);
		
		this.setData("skillsNode", nSkills);
	},
	listeners: {
		skillChange: function _() {
			var nSkills = this.getData("skillsNode");
			while (nSkills.children.length > 0) {
				nSkills.removeChild(nSkills.children[0]);
			}
			for (var i=0;i<Player.skills.length;i+=2) {
				var nWrap = document.createElement("div");
				var nSkillName = document.createElement("div");
				var nSkillValue = document.createElement("div");
				
				this.addCustomClass(nSkillName, "Name");
				this.addCustomClass(nSkillValue, "Value");
				
				nSkillName.appendChild(document.createTextNode(Global.skillNames[Player.skills[i]]));
				nSkillValue.appendChild(document.createTextNode(Player.skills[i+1]));
				nWrap.appendChild(nSkillName);
				nWrap.appendChild(nSkillValue);
				nSkills.appendChild(nWrap);
			}
		},
		locationLoad: "skillChange",
		worldLoad: "skillChange"
	},
	keysActions: {
		toggleSkillsWindow: function _() {
			if (this.hidden) {
				this.show();
			} else {
				this.hide();
			}
		}
	},
	cssRules: function _() {
		return "						\
		div.$type$ {					\
			background-color: #333;		\
			width: 400px;				\
			height: 300px;				\
			border: 2px solid #888;		\
		}								\
		div.$type$Name {				\
			display: inline-block;		\
			width: 100px;				\
			font-size: 16px;			\
			color: #fff;				\
			text-align: left;			\
		}								\
		div.$type$Value {				\
			display: inline-block;		\
			width: 50px;				\
			font-size: 18px;			\
			color: #fff;				\
		}								\
		";
	}
};
UIElementTypes.windowDeath = {
	onInit: function _() {
		var nDiv = document.createElement("div");
		var nClose = document.createElement("div");
		this.onClose=function() {
			showLoadingScreen();
			setTimeout(leaveLocation,100);
		};
		
		nClose.addClass("containerClose");
		this.addCustomClass(nDiv, "Message");
		
		this.chooseElementAsCloseButton(nClose);
		
		nDiv.appendChild(document.createTextNode("Вы умерли"));
		nClose.appendChild(document.createTextNode("Закрыть"));
		this.rootElement.appendChild(nDiv);			
		
		this.rootElement.appendChild(nClose);
	},
	listeners: {
		death: function _() {
			this.show();
		}
	},
	keysActions: {},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			background-color: #333;	\
			text-align: center;		\
			width: 100px;			\
			border: 2px; solid #888;\
			border-radius: 6px;		\
		}							\
		div.$type$Message {			\
			font-size: 16px;		\
			color: #fff;			\
			padding: 4px;			\
		}							\
		";
	}
};
UIElementTypes.windowSettings = {
	onInit: function _() {
		var nAttr, nValue, nCkeckBox, nInput;		
		var nlAttrsAndValues = [];
		
		var nForm = document.createElement("form");
		nForm.setAttribute("method","");
		nForm.setAttribute("action","");
		var nAttrNameProto = document.createElement("div");
		var nAttrValueProto = document.createElement("div");
		var nCheckBoxProto = document.createElement("input");
		nCheckBoxProto.setAttribute("type", "checkbox");
		var nInputProto = document.createElement("input");
		nInputProto.setAttribute("type", "text");
		var nOk = document.createElement("input");
		nOk.setAttribute("type","button");
		nOk.setAttribute("value","Принять");
		
		nOk.addClass("buttonBlack");
		this.addCustomClass(nForm, "Form");
		this.addCustomClass(nAttrNameProto, "AttrName");
		this.addCustomClass(nAttrValueProto, "AttrValue");
		this.addCustomClass(nInputProto, "InputSize");
		
		// Field animation on move
		nAttr = nAttrNameProto.cloneNode(true);
		nValue = nAttrValueProto.cloneNode(true);
		nCheckBox = nCheckBoxProto.cloneNode(true);
		this.setData("animateMovingNode",nCheckBox);
		nAttr.appendChild(document.createTextNode("Анимация поля при движении"));
		nValue.appendChild(nCheckBox);
		nlAttrsAndValues.push(nAttr, nValue);
		
		// Show grid
		nAttr = nAttrNameProto.cloneNode(true);
		nValue = nAttrValueProto.cloneNode(true);
		nCheckBox = nCheckBoxProto.cloneNode(true);
		this.setData("showGridNode",nCheckBox);
		nAttr.appendChild(document.createTextNode("Отображение сетки"));
		nValue.appendChild(nCheckBox);
		nlAttrsAndValues.push(nAttr, nValue);
		
		// Game field size
		nAttr = nAttrNameProto.cloneNode(true);
		nValue = nAttrValueProto.cloneNode(true);
		nInput1 = nInputProto.cloneNode(true);
		this.setData("gameFieldSizeXNode",nInput1);
		nInput2 = nInputProto.cloneNode(true);
		this.setData("gameFieldSizeYNode",nInput2);
		nAttr.appendChild(document.createTextNode("Размер игровой области"));
		nValue.appendChild(nInput1);
		nValue.appendChild(document.createTextNode("x"));
		nValue.appendChild(nInput2);
		nlAttrsAndValues.push(nAttr, nValue);
		
		this.bindHandlerToUIElementContext(nOk, "click", "okClick");
		
		for (var i=0; i<nlAttrsAndValues.length; i++) {
			this.rootElement.appendChild(nlAttrsAndValues[i]);
		}
		this.rootElement.appendChild(nOk);
		
		// По загрузке клиента установить всем input'ам в форме настроек значения, соответствующие значениям в localStorage
		// Внимание! Значенния true/false следует хранить как 1/0, т.к. в localStorage могут сохраняться только строки, а Boolean("false")==true
		this.getData("animateMovingNode").checked = +localStorage.getItem(1);
		this.getData("showGridNode").checked = +localStorage.getItem(2);
		
		this.getData("gameFieldSizeXNode").setAttribute("value", +localStorage.getItem(3));
		this.getData("gameFieldSizeYNode").setAttribute("value", localStorage.getItem(4));
//		this.element.innerHTML = "<form name='stSettingsForm' id='stSettingsForm' method='' action=''"
//			+"onSubmit='return false;'>"
//		+"<div id='stSettingsAttrWrap'>"
//		+"<div class='stAttr'>Анимация поля при движении</div>"
//		+"<div class='stVal'><input type='checkbox'"
//		+"	id='settingsAnimateMoving'></div>"
//		+"<div class='stAttr'>Отображение сетки</div>"
//		+"<div class='stVal'><input type='checkbox' id='settingsGrid'></div>"
//		+"<div class='stAttr'>Размер игровой области</div>"
//		+"<div class='stVal'><input type='text' id='settingsSizeX'>x<input"
//		+"	type='text' id='settingsSizeY'></div>"
//		+"</div><br />"
//		+"<input type='button' value='Принять' id='stSettingsAccept'"
//		+"	class='stSubmit'></form>";
	},
	listeners: {},
	keysActions: {
		toggleSettingsWindow: function _() {
			if (this.hidden) {
				this.show();
			} else {
				this.hide();
			}
		}
	},
	handlers: {
		okClick: function _() {
			localStorage.setItem(1, +this.getData("animateMovingNode").checked);
			localStorage.setItem(2, +this.getData("showGridNode").checked);
			localStorage.setItem(3, this.getData("gameFieldSizeXNode").value);
			localStorage.setItem(4, this.getData("gameFieldSizeYNode").value);
			if (Player) {
				Net.quickRefresh();
			} else {
				window.location.href=window.location.href;
			}
		}
	},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			width: 400px;			\
			height: 300px;			\
			color: #fff;			\
			text-align: center;		\
			border-radius: 20px;	\
			padding: 20px;			\
			box-sizing: border-box;	\
		}							\
		form.$type$Form {			\
			text-align: center;		\
			line-height: 20px;		\
		}							\
		input.$type$InputSize {		\
			width: 35px;			\
			border-radius: 3px;		\
			margin: 4px;			\
		}							\
		div.$type$AttrName {		\
			width: 200px;			\
			display: inline-block;	\
			text-align: right !important;	\
			font-size: 15px;		\
		}							\
		div.$type$AttrValue {		\
			width: 120px;			\
			display: inline-block;	\
		}							\
		";
	}
};
UIElementTypes.windowDialogue = {
	onInit: function _() {
		var nPhrase = document.createElement("div");
		var nPhraseTextNode = document.createTextNode("");
		var nAnswers = document.createElement("div");
		var nClose = document.createElement("div");
		
		nClose.addClass("containerClose");
		
		this.addCustomClass(nPhrase, "Phrase");
		this.addCustomClass(nAnswers, "Answers");
		
		for (var i=0; i<10; i++) {
			var nAnswer = document.createElement("div");
			this.addEventListener(nAnswer, "click", "answerClick");
			nAnswer.setData("answerId", i);
			nAnswer.appendChild(document.createTextNode(""));
			nAnswers.appendChild(nAnswer);
		}
		
		this.chooseElementAsCloseButton(nClose);
		
		nPhrase.appendChild(nPhraseTextNode);
		nClose.appendChild(document.createTextNode("Закрыть"));
		this.rootElement.appendChild(nPhrase);
		this.rootElement.appendChild(nAnswers);
		
		this.setData("phraseTextNode", nPhraseTextNode);
		this.setData("answersNode", nAnswers);
	},
	listeners: {
		dialoguePointRecieve: function _(point) {
		// point: {phrase: string, answers: [string]}
			var nAnswers = this.getData("answersNode");
			this.getData("phraseTextNode").nodeValue = point.phrase;
			var i=0;
			for (; i<point.answers.length; i++) {
				nAnswers.children[i].style.display = "block";
				nAnswers.children[i].firstChild.nodeValue = point.answers[i];
			}
			for (; i<10; i++) {
				nAnswers.children[i].style.display = "none";
			}
			this.show();
		},
		dialogueEnd: function _() {
			this.hide();
		}
	},
	keysActions: {},
	handlers: {
		answerClick: function _() {
			Player.sendAnswer(this.getData("answerId"));
		}
	},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			border-radius: 20px;	\
			padding: 20px;			\
			width: 300px;			\
			height: 200px;			\
			box-sizing: border-box;	\
		}							\
		div.$type$Phrase {			\
			display: block;			\
			color: #ff8;			\
			vertical-align:top;		\
			padding:4px;			\
		}							\
		div.$type$Answers {			\
			display: block;			\
			width: 200px;			\
			vertical-align:top;		\
			padding:4px;			\
		}							\
		div.$type$Answers > div {	\
			color: #fff;			\
			text-align:left;		\
			cursor: pointer;		\
		}							\
		div.$type$Answers > div:hover {	\
			color:#f00;				\
		}							\
		";
	}
};
UIElementTypes.windowContainer = {
	onInit: function _() {
		var nItems = document.createElement("div");
		var nClose = document.createElement("div");
		
		nItems.addClass("containerItems");
		nClose.addClass("containerClose");
		this.rootElement.appendChild(nItems);		
		
		this.chooseElementAsCloseButton(nClose);
		this.addEventListener(nClose, "click", "containerClose");
		
		nClose.appendChild(document.createTextNode("Закрыть"));
		this.rootElement.appendChild(nClose);
		
		this.setData("itemsNode", nItems);
	},
	listeners: {
		containerChange: function _(data) {
			var nItems = this.getData("itemsNode");
			while (nItems.children.length>0) {
				nItems.removeChild(nItems.children[0]);
			}
			var itemValues = Global.container.items.getValues();
			for (var i in itemValues) {
				var nSlot = document.createElement("div");
				var nImg=document.createElement("img");
				nImg.setAttribute("src","./images/items/"+itemValues[i].typeId+".png");
				nSlot.addClass("containerItem");
				
				if (itemValues[i] instanceof UniqueItem) {
					nSlot.setAttribute("typeId",itemValues[i].typeId);
					nSlot.setAttribute("param",itemValues[i].itemId);
				} else {
					nSlot.setAttribute("typeId",itemValues[i].typeId);
					nSlot.setAttribute("param",itemValues[i].amount);
					if (itemValues[i].amount>1) { 
					// Количество предметов ещё в одной обёртке и изображение
						var nWrap=document.createElement("div");
						var nNum=document.createElement("div");
						nWrap.addClass("wrap");
						nNum.addClass("itemAmount");
						nNum.appendChild(document.createTextNode(itemValues[i].amount));
						nWrap.appendChild(nNum);
						nSlot.appendChild(nWrap);
					}
				}
				
				this.addEventListener(nSlot, "click", "itemClick");
				
				nSlot.appendChild(nImg);
				nItems.appendChild(nSlot);
			}
		},
		containerOpen: function _() {
			UI.setKeyMapping("Container");
			this.UIElementType.listeners.containerChange.apply(this);
			this.show();
		}
	},
	keysActions: {
		closeContainer: function _() {
			this.hide();
			UI.setKeyMapping("Default");
		}
	},
	handlers: {
		itemClick: function _(e) {
			Player.sendTakeFromContainer(
				+this.getAttribute("typeId"), 
				(e.shiftKey ? 1 : +this.getAttribute("param")),
				Global.container.x,
				Global.container.y
			);
		},
		containerClose: function _() {
			UI.setKeyMapping("Default");
		}
	},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			text-align: center;		\
			width: 400px;			\
			height: 300px;			\
			padding: 20px;			\
			box-sizing: border-box;	\
			border-radius: 20px;	\
		}							\
		";
	}
};
UIElementTypes.windowPlayerCreate = {
	onInit: function _() {
		var nLeftSide = document.createElement("div");
		var nRightSide = document.createElement("div");
		
		var nSection1 = document.createElement("div");
		var nInputName = document.createElement("input");
		var nSection2 = document.createElement("div");
		var nRaceProto = document.createElement("div");
		var nRaces = document.createElement("div");
		var nSection3 = document.createElement("div");
		var nAttributes = document.createElement("div");
		var nAttrNameProto = document.createElement("div");
		var nAttrValueProto = document.createElement("div");
		var nSection4 = document.createElement("div");
		var nClasses = document.createElement("div");
		var nClassProto = document.createElement("div");
		var nSkillsHeader = document.createElement("div");
		var nSkillProto = document.createElement("div");
		var nColumnProto = document.createElement("div");
		var nColumnHeaderProto = document.createElement("div");
		var nLearnedSkills = document.createElement("div");
		var nLearnedSkillProto = document.createElement("div");
		var nBack = document.createElement("div");
		var nComplete = document.createElement("div");
		var nClass = document.createElement("div");
		
		
		this.addCustomClass(nLeftSide, "LeftSide");
		this.addCustomClass(nRightSide, "RightSide");
		this.addCustomClass(nSection1, "Section");
		this.addCustomClass(nSection2, "Section");
		this.addCustomClass(nSection3, "Section");
		this.addCustomClass(nSection4, "Section");
		this.addCustomClass(nInputName, "Input");
		this.addCustomClass(nRaceProto, "Race");
		this.addCustomClass(nClass, "Class");
		this.addCustomClass(nAttrNameProto, "AttrName");
		this.addCustomClass(nAttrValueProto, "AttrValue");
		this.addCustomClass(nClasses, "Classes");
		this.addCustomClass(nClassProto, "Class");
		this.addCustomClass(nSkillProto, "Skill");
		this.addCustomClass(nSkillsHeader, "SkillsHeader");
		this.addCustomClass(nColumnProto, "Column");
		this.addCustomClass(nColumnHeaderProto, "ColumnHeader");
		this.addCustomClass(nLearnedSkills, "LearnedSkills");
		nBack.addClass("buttonBlack");
		nComplete.addClass("buttonBlack");
		nBack.style.float = "left";
		nComplete.style.float = "right";
		
		this.bindHandlerToUIElementContext(nBack, "click", "backClick");
		this.bindHandlerToUIElementContext(nComplete, "click", "completeClick");
		
		// Left side
		for (var i=0; i<Global.races.length; i++) {
		// Races
			var nRace = nRaceProto.cloneNode(true);
			nRace.appendChild(document.createTextNode(Global.races[i]));
			nRaces.appendChild(nRace);
			nRace.setData("uiElement", this);
			this.addEventListener(nRace, "click", "raceClick");
			nRace.setData("race", i);
		}
		for (var i in Global.attributes) {
		// Race attributes
			var nAttr = nAttrNameProto.cloneNode(true);
			var nVal = nAttrValueProto.cloneNode(true);
			nAttr.appendChild(document.createTextNode(Global.attributes[i]));
			nAttributes.appendChild(nAttr);
			nVal.appendChild(document.createTextNode(""));
			nAttributes.appendChild(nVal);
		}
		for (var i=0;i<Global.classNames.length;i++) {
		// Classes
			var nClass = nClassProto.cloneNode(true);
			this.addEventListener(nClass, "click", "classClick");
			nClass.appendChild(document.createTextNode(Global.classNames[i]));
			nClasses.appendChild(nClass);
			nClass.setData("uiElement",this);
		}
		
		nSection1.appendChild(document.createTextNode("Имя"));
		nSection2.appendChild(document.createTextNode("Раса"));
		nSection3.appendChild(document.createTextNode("Атрибуты"));
		nSection4.appendChild(document.createTextNode("Класс"));
		nSkillsHeader.appendChild(document.createTextNode("Навыки"));
		nLeftSide.appendChild(nSection1);
		nLeftSide.appendChild(nInputName);
		nLeftSide.appendChild(nSection2);
		nLeftSide.appendChild(nRaces);
		nLeftSide.appendChild(nSection3);
		nLeftSide.appendChild(nAttributes);
		nLeftSide.appendChild(nSection4);
		nLeftSide.appendChild(nClasses);
		
		// Right side
		nRightSide.appendChild(nSkillsHeader);
		var skillLists = [Global.skillsStr, Global.skillsDex, Global.skillsWis, Global.skillsItl];
		var skill = 0;
		for (var i=0; i<4; i++) {
			var nColumn = nColumnProto.cloneNode(true);
			var nColumnHeader = nColumnHeaderProto.cloneNode(true);
			nColumnHeader.appendChild(document.createTextNode(Global.attributes[i]));
			nColumn.appendChild(nColumnHeader);
			for (var j=0; j< skillLists[i].length; j++) {
				var nSkill = nSkillProto.cloneNode(true);
				nSkill.appendChild(document.createTextNode(skillLists[i][j]));
				nColumn.appendChild(nSkill);
				nSkill.setData("uiElement",this);
				nSkill.setData("skill",skill);
				this.addEventListener(nSkill, "click", "skillClick");
				skill++;
			}
			nRightSide.appendChild(nColumn);
		}
		nRightSide.appendChild(nLearnedSkills);
		
		this.rootElement.appendChild(nLeftSide);
		this.rootElement.appendChild(nRightSide);
		nBack.appendChild(document.createTextNode("Назад"));
		nComplete.appendChild(document.createTextNode("Готово"));
		this.rootElement.appendChild(nBack);
		this.rootElement.appendChild(nComplete);
		
		this.setData("attributesNode",nAttributes);
		this.setData("nameNode",nInputName);
		this.setData("learnedSkillProtoNode",nLearnedSkillProto);
		this.setData("learnedSkillsNode",nLearnedSkills);
		this.setData("learnedSkills",{});
		this.setData("learnedSkillsNodes",{});	
		this.setData("cls",null);	
		
		fleXenv.fleXcrollMain(nClasses);
				
		this.UIElementType.handlers.raceClick.apply(nRaces.children[0]);
	},
	listeners:{
		playerCreateStart: function _() {
			this.show();
		}
	},
	keysActions: {},
	handlers: {
		raceClick: function _() {
			var nlRaces = this.parentNode.children;
			var uiElement = this.getData("uiElement");
			var nAttributes = uiElement.getData("attributesNode");
			var race = this.getData("race");
			uiElement.setData("chosenRace", race);
			// Highlight selected race
			for (var i in nlRaces) {
				uiElement.setCustomClass(nlRaces[i],"Race");
			}
			uiElement.addCustomClass(this, "SelectedRace");
			
			// Show racial attributes
			for (var i=0; i<Global.racialAttributes[race].length; i++) {
				nAttributes.children[i*2+1].firstChild.nodeValue = Global.racialAttributes[race][i];
			}			
		},
		classClick: function _() {
			var nlClasses = this.parentNode.children;
			var uiElement = this.getData("uiElement");
			uiElement.setData("chosenClass",this.firstChild.nodeValue);
			
			// Highlight selected race
			for (var i in nlClasses) {
				uiElement.setCustomClass(nlClasses[i],"Class");
			}
			uiElement.setCustomClass(this, "SelectedClass");
		},
		skillClick: function _() {
			var uiElement = this.getData("uiElement");
			var nLearnedSkills = uiElement.getData("learnedSkillsNode");
			var learnedSkills = uiElement.getData("learnedSkills");
			var learnedSkillsNodes = uiElement.getData("learnedSkillsNodes");
			var skill = this.getData("skill");
			
			if (learnedSkills[skill]) {
				learnedSkills[skill]++;
				learnedSkillsNodes[skill].firstChild.nodeValue = Global.skillNames[skill]+" "+learnedSkills[skill];
			} else {
				learnedSkills[skill] = 1;
				var nLearnedSkill = uiElement.getData("learnedSkillProtoNode").cloneNode(true);
				uiElement.addCustomClass(nLearnedSkill,"LearnedSkill");
				nLearnedSkill.appendChild(document.createTextNode(Global.skillNames[skill]+" 1"));
				nLearnedSkill.setData("skill", skill);
				nLearnedSkill.setData("uiElement", uiElement);
				learnedSkillsNodes[skill] = nLearnedSkill;
				nLearnedSkills.appendChild(nLearnedSkill);
				uiElement.addEventListener(nLearnedSkill, "click", "learnedSkillClick");
			}
		},
		learnedSkillClick: function _() {
			var uiElement = this.getData("uiElement");
			var learnedSkills = uiElement.getData("learnedSkills");
			var skill = this.getData("skill");
			
			learnedSkills[skill]--;
			if (learnedSkills[skill] == 0) {
				this.parentNode.removeChild(this);
			} else {
				this.firstChild.nodeValue = Global.skillNames[skill]+" "+learnedSkills[skill];
			}			
		},
		backClick: function _() {
			this.hide();
			UI.notify("accountPlayersListCall");
		},
		completeClick: function _() {
			var formattedSkills = [];
			var storedSkills = this.getData("learnedSkills");
			var thisWindow = this;
			for (var i in storedSkills) {
				formattedSkills.push(+i, storedSkills[i]);
			}
			Net.send({
				a: Net.PLAYER_CREATE,
				account: Global.playerLogin,
				name: this.getData("nameNode").value, 
				race: this.getData("chosenRace"),
				cls: this.getData("chosenClass"),
				skills: formattedSkills
			}, function (data) {
				thisWindow.hide();
				UI.notify("accountPlayersRecieve", data.players);
			});
		}
	},
	cssRules: function _() {
		return " 					\
		div.$type$ {				\
			width: 600px;			\
			height: 500px;			\
			border-radius: 20px;	\
			padding: 20px 20px 62px 20px;	\
			box-sizing: border-box;	\
		}							\
		div.$type$LeftSide {		\
			width: 140px;			\
			height: 100%;			\
			float: left;			\
			text-align: left;		\
			margin: 0px 0px 6px 0px;\
		}							\
		div.$type$RightSide {		\
			height: 100%;			\
			float: right;			\
			width: 400px;			\
			margin: 0px 0px 6px 0px;\
		}							\
		div.$type$LeftSide * {		\
			text-align: left;		\
			font-size: 13px;		\
		}							\
		input.$type$Input {			\
			width: 100%;			\
			border-radius: 6px;		\
			box-sizing: border-box;	\
		}							\
		div.$type$Race {			\
			color: #add;			\
			font-size: 14px;		\
			cursor: pointer;		\
		}							\
		div.$type$Race:hover {		\
			color: #9aa;			\
		}							\
		div.$type$SelectedRace {	\
			color: #699;			\
		}							\
		div.$type$SelectedRace {	\
			padding-left: 10px !important;	\
			color: #377 !important;	\
		}							\
		div.$type$Class {			\
			color: #add;			\
			padding: 4px;			\
			font-size: 14px;		\
			cursor: pointer;		\
		}							\
		div.$type$Class:hover {		\
			color: #9aa;			\
		}							\
		div.$type$SelectedClass {	\
			color: #799;			\
		}							\
		div.$type$Classes {			\
			overflow-y: scroll;		\
			height: 126px;			\
			background-color: #222;	\
		}							\
		div.$type$SelectedClass {	\
			padding: 4px 4px 4px 10px;	\
			color: #377;			\
			font-size: 14px;		\
			cursor: pointer;		\
		}							\
		div.$type$Error {			\
			color: #f55;			\
			height: 20px;			\
		}							\
		div.$type$Column {			\
			display: inline-block;	\
			width: 100px;			\
			height: 200px;			\
			vertical-align: top;	\
			margin: 4px 0px 0px 0px;\
		}							\
		div.$type$ColumnHeader {	\
			width: 100px;			\
			font-size: 16px;		\
			color: #ccc;			\
			text-align: center;		\
		}							\
		div.$type$Skill {			\
			color: #add;			\
			font-size: 14px;		\
			text-align: left;		\
			padding: 4px 4px 4px 4px;	\
			cursor: pointer;		\
		}							\
		div.$type$Skill:hover {		\
			color: #9aa;			\
		}							\
		div.$type$SkillAttribute {	\
			color: #fff !important;	\
			font-size: 16px !important;	\
			-webkit-box-sizing: border-box;	\
			text-align: center !important;	\
			font-variant: small-caps;	\
			width: 100%;			\
		}							\
		div.$type$SkillAttribute:hover {	\
			color: #fff !important;	\
		}							\
		div.$type$SkillsHeader {	\
			font-size: 22px;		\
			text-align: center;		\
			color: #fff;			\
			font-variant: smallcaps;\
		}							\
		div.$type$LearnedSkill {	\
			display: inline-block;	\
			cursor: pointer;		\
			background-color: #377;	\
			padding: 6px;			\
			border-radius: 6px;		\
			margin: 6px 6px 0px 0px;\
		}							\
		div.$type$LearnedSkills {	\
			display: inline-block;	\
			max-width: 432px;		\
		}							\
		div.$type$LearnedSkill:hover {	\
			color: #9aa;			\
		}							\
		div.$type$Attributes {		\
			max-width: 110px;		\
			padding: 4px;			\
			height: 70px;			\
		}							\
		div.$type$AttrName {		\
			display: inline-block;	\
			width: 80px;			\
			padding: 4px 0px 0px 4px;	\
			height: 16px;			\
			color: #fff;			\
			box-sizing: border-box;	\
		}							\
		div.$type$AttrValue {		\
			display: inline-block;	\
			padding: 4px 0px 0px 0px;	\
			text-align: center;		\
			color: #377;			\
			width: 40px;			\
			height: 16px;			\
			box-sizing: border-box;	\
		}							\
		div.$type$Section {			\
			font-size: 18px;		\
			color: #986;			\
			font-variant: small-caps;	\
			margin-top: 10px;		\
		}							\
		";
	}
};
UIElementTypes.windowAccountCreate = {
	onInit: function _() {
		var nLogin = document.createElement("input");
		var nPassword = document.createElement("input");
		var nPasswordAgain = document.createElement("input");
		var nComplete = document.createElement("div");
		var nBack = document.createElement("div");
		var nWrap = document.createElement("div");
		var nHeader = document.createElement("div");
		var nError = document.createElement("div");
		
		var nDiv1 = document.createElement("div");
		nDiv1.appendChild(document.createTextNode("Логин"));
		
		var nDiv2 = nDiv1.cloneNode(true);
		nDiv2.firstChild.nodeValue = "Пароль";
		
		var nDiv3 = nDiv1.cloneNode(true);
		nDiv3.firstChild.nodeValue = "Пароль ещё раз";
		
		this.addCustomClass(nDiv1, "Label");
		this.addCustomClass(nDiv2, "Label");
		this.addCustomClass(nDiv3, "Label");
		this.addCustomClass(nWrap, "Wrap");
		this.addCustomClass(nError, "Error");
		nComplete.addClass("buttonBlack");
		nHeader.addClass("windowHeader");
		nBack.addClass("buttonBlack");
		nComplete.style.float = "left";
		nBack.style.float = "right";
		
		nComplete.appendChild(document.createTextNode("Создать"));
		nBack.appendChild(document.createTextNode("Назад"));
		
		this.bindHandlerToUIElementContext(nBack, "click", "backClick");
		this.bindHandlerToUIElementContext(nComplete, "click", "completeClick");
		
		nHeader.appendChild(document.createTextNode("Новый аккаунт"));
		nWrap.appendChild(nHeader);
		nWrap.appendChild(nDiv1);
		nWrap.appendChild(nLogin);
		nWrap.appendChild(nDiv2);
		nWrap.appendChild(nPassword);
		nWrap.appendChild(nDiv3);
		nWrap.appendChild(nPasswordAgain);
		nWrap.appendChild(nError);
		this.rootElement.appendChild(nWrap);
		this.rootElement.appendChild(nComplete);
		this.rootElement.appendChild(nBack);
		
		this.setData("errorNode", nError);
		this.setData("loginNode", nLogin);
		this.setData("passwordNode", nPassword);
		this.setData("passwordAgainNode", nPasswordAgain);
	},
	listeners: {
		accountCreateStart: function _() {
			this.show();
		}
	},
	keysActions: {},
	handlers: {
		backClick: function _() {
			this.hide();
			UI.notify("serverInfoRecieve");
		},
		completeClick: function _() {
			var nError = this.getData("errorNode");
			var nLogin = this.getData("loginNode");
			var nPassword = this.getData("passwordNode");
			var nPasswordAgain = this.getData("passwordAgainNode");
			if (nLogin.value == "") {
				winkElement(nError, "Заполните логин!");
			} else if (nPassword.value == "" || nPasswordAgain.value == "") {
				winkElement(nError, "Заполните пароль и подтверждение!");
			} else if (nPassword.value != nPasswordAgain.value) {
				winkElement(nError, "Пароль и подтверждение пароля не совпадают!");
			} else {
				var thisWindow = this;
				Net.send({a:Net.ACCOUNT_REGISTER,l:nLogin.value,p:nPassword.value}, function (data) {
					if (data.error !== undefined) {
						winkElement(nError, "Логин уже занят!");
					} else {
						Global.playerLogin = nLogin.value;
						Global.playerPassword = nPassword.value;
						UI.notify("accountPlayersRecieve", data.players);
						thisWindow.hide();
					}
				});
			}
		}
	},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			width: 400px;			\
			height: 300px;			\
			border-radius: 20px;	\
			padding: 20px 20px 58px 20px;	\
			box-sizing: border-box;	\
			text-align: center;		\
		}							\
		div.$type$Wrap {			\
			height: 100%;			\
		}							\
		div.$type$Wrap > input {	\
			margin: 4px 50px 4px 4px;	\
			border-radius: 5px;		\
		}							\
		div.$type$Label {			\
			display: inline-block;	\
			margin-left:20px;		\
			width: 95px;			\
			text-align: right;		\
			color: #fff;			\
			padding-right: 5px;		\
			font-size: 14px;		\
			height: 30px;			\
			line-height: 30px;		\
		}							\
		div.$type$Error {			\
			height: 20px;			\
			line-height: 20px;		\
			color: #f55;			\
			text-align: center;		\
			font-size: 13px;		\
		}							\
		";
	}
};
UIElementTypes.attributeList = {
	onInit: function _() {
		var nArmorName = document.createElement("div");
		var nArmorValue = document.createElement("div");
		var nlAttributes = [];
		var nEvasionName = document.createElement("div");
		var nEvasionValue = document.createElement("div");
		var nArmorText = document.createTextNode("");
		var nEvasionText = document.createTextNode("");
		var attributeList = ["str", "dex", "wis", "itl", "armor", "evasion", "fireRes", "coldRes", "poisonRes"];
		
		for (var i in attributeList) {
			var nName = document.createElement("div");
			var nValue = document.createElement("div");
			var nValueText = document.createTextNode("");
			
			this.addCustomClass(nName, "AttrName");
			this.addCustomClass(nValue, "AttrValue");
			
			nName.appendChild(document.createTextNode(attributeList[i]));	
			nValue.appendChild(nValueText);
			this.rootElement.appendChild(nName);
			this.rootElement.appendChild(nValue);
			
			this.setData(attributeList[i]+"Text",nValueText);
		}
	},
	listeners: {
		attributeChange: function _(data) {
		/* data: [attrId, value] */
			switch (data[0]) {
			case 1:
				this.getData("armorText").nodeValue = data[1];
			case 2:
				this.getData("evasionText").nodeValue = data[1];
			}
		},
		attributesInit: function _() {
			this.getData("armorText").nodeValue = Player.attributes.armor;
			this.getData("evasionText").nodeValue = Player.attributes.evasion;
			this.getData("fireResText").nodeValue = Player.attributes.fireRes;
			this.getData("coldResText").nodeValue = Player.attributes.coldRes;
			this.getData("poisonResText").nodeValue = Player.attributes.poisonRes;
			this.getData("strText").nodeValue = Player.attributes.str;
			this.getData("dexText").nodeValue = Player.attributes.dex;
			this.getData("wisText").nodeValue = Player.attributes.wis;
			this.getData("itlText").nodeValue = Player.attributes.itl;
		}
	},
	keysActions: {},
	handlers: {},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			background-color: #333;	\
			width: 204px;			\
			color: #fff;			\
		}							\
		div.$type$Attr {			\
			width: 100%;			\
		}							\
		div.$type$AttrName {		\
			display: inline-block;	\
			width: 100px;			\
		}							\
		div.$type$AttrValue {		\
			display: inline-block;	\
			width: 50px;			\
		}							\
		";
	}
};
UIElementTypes.actionsPanel = {
	onInit: function _() {
		this.setData("activeState",0);
		for (var i in Player.actions) {
			var nDiv = document.createElement("div");
			this.addCustomClass(nDiv,"Action");
			nDiv.appendChild(document.createTextNode(Player.actions[i].substring(0,3)));
			this.addEventListener(nDiv,"click", Player.actions[i]);
			this.rootElement.appendChild(nDiv);
		}
		var states = this.setData("nlStates",{});
		for (var i=1; i<Player.states.length; i++) {
			var nDiv = document.createElement("div");
			nDiv.setData("stateId", i);
			this.addCustomClass(nDiv,"InactiveState");
			nDiv.appendChild(document.createTextNode(Player.states[i].substring(0,3)));
			this.addEventListener(nDiv, "click", "state");
			this.rootElement.appendChild(nDiv);
			states[i] = nDiv;
		}
	},
	listeners: {
		stateEntered: function _() {
			if (this.getData("activeState") !== 0) {
				this.setCustomClass(this.getData("nlStates")[this.getData("activeState")], "InactiveState");
			}
			if (Player.stateId !== 0) {
				this.setCustomClass(this.getData("nlStates")[Player.stateId], "ActiveState");
			}
			this.setData("activeState",Player.stateId);
		}
	},
	keysActions: {},
	handlers: {
		push: function _() {
			performAction("choosePushAim");
		},
		changePlaces: function _() {
			CellCursor.enterSelectionMode("changePlaces", 1, function(x,y){
				return [Terrain.getCell(x,y)];
			});
		},
		makeSound: function () {
			performAction("makeSound", [1]);
		},
		shieldBash: function() {
			CellCursor.enterSelectionMode("shieldBash", 1, function(x,y){
				return [Terrain.getCell(x,y)];
			});
		},
		jump: function _() {
			CellCursor.enterSelectionMode("jump", 2);
		},
		state: function () {
			if (this.getData("stateId") === Player.stateId) {
				performAction("enterState", [0]);
			} else {
				performAction("enterState", [this.getData("stateId")]);
			}
		}
	},
	cssRules: function _() {
		return "					\
		div.$type$ {				\
			background-color: #333;	\
		}							\
		div.$type$Action {			\
			display: inline-block;	\
			cursor: pointer;		\
			border: 1px solid #333;	\
			overflow: hidden;		\
			background-color: #000;	\
			color: #fff;			\
			width: 32px;			\
			height: 32px;			\
			text-align: center;		\
			line-height: 32px;		\
		}							\
		div.$type$ActiveState {			\
			display: inline-block;	\
			cursor: pointer;		\
			border: 1px solid #333;	\
			overflow: hidden;		\
			background-color: #444;	\
			color: #fff;			\
			width: 32px;			\
			height: 32px;			\
			text-align: center;		\
			line-height: 32px;		\
		}							\
		div.$type$InactiveState {			\
			display: inline-block;	\
			cursor: pointer;		\
			border: 1px solid #333;	\
			overflow: hidden;		\
			background-color: #4aa;	\
			color: #fff;			\
			width: 32px;			\
			height: 32px;			\
			text-align: center;		\
			line-height: 32px;		\
		}							\
		";
	}
};
