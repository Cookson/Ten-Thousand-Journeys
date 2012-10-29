onLoadEvents["defaultUIActions"] = function() {
	for (var name in UI.defaultUIActions) {
		UI.registerAction(name, window, UI.defaultUIActions[name])
	}
	UI.registerAction("idle", null, function () {
		Net.send({
			a : Net.IDLE
		});
	});
	UI.registerAction("answer", null, function (answerId) {
		Net.send({
			a : Net.ANSWER, 
			answerId : answerId
		});
	});
	UI.registerAction("startConversation", null, function (character) {
		Net.send({
			a : Net.START_CONVERSATION,
			characterId : character.characterId
		});
	});
	UI.registerAction("takeOff", null, function (item) {
		Net.send({
			a : Net.TAKE_OFF,
			itemId : item.itemId
		});
		UI.notify("quitSelection");
	});
	UI.registerAction("useObject", null, function (x, y) {
		Net.send({
			a : Net.USE_OBJECT,
			x : x,
			y : y
		});
	});
	UI.registerAction("takeFromContainer", null, function (item, amount) {
		Net.send({
			a : Net.TAKE_FROM_CONTAINER,
			typeId : typeId,
			param : amount === undefined ? item.itemId : amount,
					x : Global.container.x,
					y : Global.container.y
		});
	});
	UI.registerAction("shootMissile", null, function (x, y) {
		Net.send({
			a : Net.SHOOT_MISSILE,
			x : x,
			y : y,
			unique : this.selectedMissile instanceof UniqueItem ? true : false,
			missile : this.selectedMissile instanceof UniqueItem ? this.selectedMissile.itemId : this.selectedMissile.typeId
		});
	});
	UI.registerAction("openContainer", null, function (x, y) {
		Net.send({
			a : Net.OPEN_CONTAINER,
			x : Global.container.x,
			y : Global.container.y
		});
	});
	UI.registerAction("putToContainer", null, function (item, amount) {
		Net.send({
			a : Net.PUT_TO_CONTAINER,
			typeId : typeId,
			param : param,
			x : Global.container.x,
			y : Global.container.y
		});
	});
	UI.registerAction("attack", null, function (character) {
		Net.send({
			a : Net.ATTACK,
			aimId : character.characterId,
			ranged : false
		});
	});
	UI.registerAction("castSpell", null, function (x, y, spellId) {
		Net.send( {
			a : Net.CAST_SPELL,
			spellId : spellId || this.spellId,
			x : x,
			y : y
		});
		performAction("unselectCellAction");
	});
	UI.registerAction("drop", null, function (item, amount) {
		if (this.items.contains(item)) {
			if (item instanceof UniqueItem) {
				Net.send( {
					a : Net.DROP_UNIQUE,
					typeId : item.typeId,
					itemId : item.itemId
				});
			} else {
				Net.send( {
					a : Net.DROP_PILE,
					typeId : item.typeId,
					amount : amount === undefined ? item.amount : amount
				});
			}
			UI.notify("quitSelection");
		} else {
			throw new Error("Player "+this.name+" has no items of type "
					+item.typeId);
		}
	});
	UI.registerAction("putOn", null, function (item) {
		if (this.equipment.hasItemInSlot(item.getSlot())) {
			UI.notify("alert", "Сначала снимите "+window.items[this.equipment.getItemInSlot(item.getSlot()).typeId][0]);
			return;
		}
		Net.send({
			a : Net.PUT_ON,
			itemId : item.itemId
		});
		UI.notify("quitSelection");
	});
	UI.registerAction("choosePushAim", null, function (x, y) {
		// Chooses the object at cell x,y as the aim to push
		// This action calls action "choosePushDirection", which calls action
		// "push", which sends the query to server.
		CellCursor.enterSelectionMode("choosePushDirection", 1, function(x,y){
			return [World.getCell(x,y).character];
		});
	});
	UI.registerAction("choosePushDirection", null, function (entity) {
		// Chooses the direction
		CellCursor.enterSelectionMode("push", 1, function(x, y){
			return [entity, Side.d2side(x-entity.x, y-entity.y)];
		}, entity);
	});
	UI.registerAction("push", null, function (entity, direction) {
		// Direction is Side object
		Net.send({
			a : Net.PUSH,
			x : entity.x,
			y : entity.y,
			direction : direction.getInt()
		});
	});
	UI.registerAction("changePlaces", null, function (entity) {
		Net.send( {
			a : Net.CHANGE_PLACES,
			x : entity.x,
			y : entity.y
		});
	});
	UI.registerAction("makeSound", null, function (type) {
		/**
		 * @param {Number} type
		 */
		Net.send( {
			a : Net.MAKE_SOUND,
			type : type
		});
	});
	UI.registerAction("pickUp", null, function (item, amount) {
		/**
		 * @param {UniqueItem|ItemPile} item
		 * @param {Number} amount If item is ItemPile, you may provide amount to 
		 * pick up.
		 */
		if (item instanceof UniqueItem) {
			Net.send( {
				a : Net.PICK_UP_UNIQUE,
				itemId : item.itemId
			});
		} else {
			Net.send( {
				a : Net.PICK_UP_PILE,
				typeId : item.typeId,
				amount : amount === undefined ? item.amount : amount
			});
		}
		UI.notify("quitSelection");
	});
	UI.registerAction("shieldBash", null, function (entity) {
		/**
		 * @param {Object} entity Object with properties entity.x and entity.y
		 */
		if (Player.equipment.getItemInSlot(2) === null) {
			UI.notify("alert", "Вы либо галстук снимите, либо щит наденьте!")
			return;
		}
		Net.send({
			a : Net.SHIELD_BASH,
			x : entity.x,
			y : entity.y
		});
	});
	UI.registerAction("jump", null, function (x, y) {
		/**
		 * @param {Number} x
		 * @param {Number} y
		 */
		Net.send({
			a : Net.JUMP,
			x : x,
			y : y
		});
	});
	UI.registerAction("login", null, function (login, password) {
		/**
		 * @param {string} login
		 * @param {string} password
		 */
		Global.playerLogin = login;
		Global.playerPassword = password;
		Net.send({a:Net.LOGIN,l:login,p:password});
	});
	UI.registerAction("logInForCharacter", null, function logInForCharacter(name) {
		/**
		 * @param {String} name Name of character to log in.
		 */
		if (Global.playerLogin === null) {
			throw new Error("No login set");
		}
		for (var i=0; i<Net.accountPlayers.length; i++) {
			if (Net.accountPlayers[i][1] === name) {
				Net.logInForCharacter(Net.accountPlayers[i][0], Global.playerLogin, Global.playerPassword);
				return;
			}
		}
		throw new Error("No character "+name+" on account");
	});
	UI.registerAction("worldTravel", null, function (x, y) {
		/**
		 * @param {Number} x
		 * @param {Number} y
		 */
		Net.send({a:Net.WORLD_TRAVEL, x:x, y:y});
	});
	UI.registerAction("quitSelection", UI, function () {
		/**
		 * @param {Number} x
		 * @param {Number} y
		 */
		this.notify("quitSelection");
	});
	UI.registerAction("selectSpell", null, function(spellId) {
		Player.spellId = spellId;
		CellCursor.enterSelectionMode("castSpell");
		UI.notify("spellSelect");
	});
	UI.registerAction("selectMissile", null, function(item) {
		Player.selectedMissile = item;
		UI.notify("missileSelect");
		UI.notify("quitSelection");
	});
	UI.registerAction("enterState", null, function(stateId) {
		Net.send({
			a: Net.ENTER_STATE,
			stateId: stateId
		});
	});
	UI.registerAction("move", Player, function (x, y) {
		if (Characters.player("x") === x && Characters.player("y") === y) {
			throw new Error("Going to the same cell he is staying at");
		}
		this.getPathTable();
		var path = this.getPath(x, y);
		var nextCellX = path[0].x;
		var nextCellY = path[0].y;
		var objectId;
		if (
			World.passability(nextCellX,nextCellY) === StaticData.PASSABILITY_BLOCKED
			&& (objectId = World.object(nextCellX,nextCellY)) !== null
			&& StaticData.object(objectId).type === StaticData.OBJECT_TYPE_DOOR
		) {
		// Open door, if trying to move into a door
			performAction("useObject", [nextCellX, nextCellY]);
			return false;
		}
		Net.send({
			a: Net.MOVE,
			dir: Side.d2side(nextCellX-Characters.player("x"), nextCellY-Characters.player("y")).getInt()
		});
	});
	UI.registerAction("goTo", Player, function(x, y) {
	/**
	 * Repetitive action that uses action "move" until player reaches his
	 * destination
	 * @param {number} x
	 * @param {number} y
	 */
		Player.setDestination(x, y);
		var listener = Events.addListener("nextTurn", Player, function() {
		// Until player reaches destination, move one cell each time it's player's turn
			if (Characters.player("x") === x && Characters.player("y") === y) {
				Events.removeListener(listener);
			} else {
				// console.log(endX,endY);
				performAction("move", [x, y]);
			}
		});
		performAction("move", [x, y]);
	});
};
UI.defaultUIActions = {
	leaveLocation: function _() {
		throw new Error("Daguq");
	},
	enterLocation: function _() {
		throw new Error("DAFUQ");
	},
	quickRefresh: function _() {
		Net.quickRefresh();
	},
	disableUI: function _() {
		UI.disable();
	},
	enableUI: function _() {
		UI.enable();
	},
	toggleUI: function _() {
		if (UI.disabled) {
			UI.enable();
		} else {
			UI.disable();
		}
	},
	aimMissile: function _() {
		CellCursor.enterSelectionMode("shootMissile", 11);
	},
	unselectCellAction: function _() {
		CellCursor.exitSelectionMode();
		UI.notify("unselectCellAction");
		if (Player.spellId !== -1) {
			Player.spellId = -1;
		}
	},
	chooseCell: function _() {
		CellCursor.chooseCurrentCell();
	},
	takeAllFromContainer: function _() {
		var itemValues = Global.container.items.getValues();
		for (var i in itemValues) {
			Player.addActionToQueue(
				"takeFromContainer", 
				[
				 	itemValues[i].typeId, 
				 	itemValues[i][itemValues[i].amount ? "amount" : "itemId"]
				]
			);
		}
		Player.doActionFromQueue();
	},
	rotateCamera: function _() {
		if (GameField.cameraOrientation == Side.N) {
			rotateCamera(Side.E);
		} else if (GameField.cameraOrientation == Side.E) {
			rotateCamera(Side.S);
		} else if (GameField.cameraOrientation == Side.S) {
			rotateCamera(Side.W);
		} else if (GameField.cameraOrientation == Side.W) {
			rotateCamera(Side.N);
		}
	},
	cursorMove: function _(side) {
		var d = side.side2d();
		CellCursor.move(CellCursor.x+d[0], CellCursor.y+d[1]);
	},
	showCurrentKeymapping: function() {
		var fragment = document.createDocumentFragment();
		for (var i in Keys.keyMapping.handlers) {
			for (var j in Keys.keyMapping.handlers[i]) {
				for (var k in Keys.keyMapping.handlers[i][j]) {
					for (var l in Keys.keyMapping.handlers[i][j][k]) {
						var combination = Keys.getCombinationAsTextInDiv(i,j,k,l)
						var description = document.createElement();
						fragment.appendChild(combination);
					}
				}
			}
		}
		UI.notify("alert", fragment);
	}
};
