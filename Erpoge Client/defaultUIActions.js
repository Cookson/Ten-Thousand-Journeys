onLoadEvents["defaultUIActions"] = function() {
	for (var name in UI.defaultUIActions) {
		UI.registerAction(name, window, UI.defaultUIActions[name])
	}
	UI.registerAction("idle", Player, function () {
		Net.send({
			a : Net.IDLE
		});
	});
	UI.registerAction("answer", Player, function (answerId) {
		Net.send({
			a : Net.ANSWER, 
			answerId : answerId
		});
	});
	UI.registerAction("startConversation", Player, function (character) {
		Net.send({
			a : Net.START_CONVERSATION,
			characterId : character.characterId
		});
	});
	UI.registerAction("takeOff", Player, function (item) {
		Net.send({
			a : Net.TAKE_OFF,
			itemId : item.itemId
		});
		UI.notify("quitSelection");
	});
	UI.registerAction("useObject", Player, function (x, y) {
		Net.send({
			a : Net.USE_OBJECT,
			x : x,
			y : y
		});
	});
	UI.registerAction("takeFromContainer", Player, function (item, amount) {
		Net.send({
			a : Net.TAKE_FROM_CONTAINER,
			typeId : typeId,
			param : amount === undefined ? item.itemId : amount,
					x : Global.container.x,
					y : Global.container.y
		});
	});
	UI.registerAction("shootMissile", Player, function (x, y) {
		Net.send({
			a : Net.SHOOT_MISSILE,
			x : x,
			y : y,
			missile : this.missileType
		});
	});
	UI.registerAction("openContainer", Player, function (x, y) {
		Net.send({
			a : Net.OPEN_CONTAINER,
			x : Global.container.x,
			y : Global.container.y
		});
	});
	UI.registerAction("putToContainer", Player, function (item, amount) {
		Net.send({
			a : Net.PUT_TO_CONTAINER,
			typeId : typeId,
			param : param,
			x : Global.container.x,
			y : Global.container.y
		});
	});
	UI.registerAction("attack", Player, function (character) {
		Net.send({
			a : Net.ATTACK,
			aimId : character.characterId,
			ranged : false
		});
	});
	UI.registerAction("castSpell", Player, function (x, y, spellId) {
		Net.send( {
			a : Net.CAST_SPELL,
			spellId : spellId || this.spellId,
			x : x,
			y : y
		});
	});
	UI.registerAction("drop", Player, function (item, amount) {
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
	UI.registerAction("putOn", Player, function (item) {
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
	UI.registerAction("choosePushAim", Player, function (x, y) {
		// Chooses the object at cell x,y as the aim to push
		// This action calls action "choosePushDirection", which calls action
		// "push", which sends the query to server.
		CellCursor.enterSelectionMode("choosePushDirection", 1, function(x,y){
			return [Terrain.cells[x][y]];
		});
	});
	UI.registerAction("choosePushDirection", Player, function (entity) {
		// Chooses the direction 
		CellCursor.enterSelectionMode("push", 1, function(x, y){
			return [entity, Side.d2side(x-entity.x, y-entity.y)];
		}, entity);
	});
	UI.registerAction("push", Player, function (entity, direction) {
		// Direction is Side object
		Net.send({
			a : Net.PUSH,
			x : entity.x,
			y : entity.y,
			direction : direction.getInt()
		});
	});
	UI.registerAction("changePlaces", Player, function (entity) {
		Net.send( {
			a : Net.CHANGE_PLACES,
			x : entity.x,
			y : entity.y
		});
	});
	UI.registerAction("makeSound", Player, function (type) {
		/**
		 * @param {Number} type
		 */
		Net.send( {
			a : Net.MAKE_SOUND,
			type : type
		});
	});
	UI.registerAction("pickUp", Player, function (item, amount) {
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
	UI.registerAction("shieldBash", Player, function (entity) {
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
	UI.registerAction("jump", Player, function (x, y) {
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
	UI.registerAction("login", Player, function (login, password) {
		/**
		 * @param {String} x
		 * @param {Pasword} y
		 */
		Global.playerLogin = login;
		Global.playerPassword = password;
		Net.send({a:Net.LOGIN,l:login,p:password}, handlers.net.login);
	});
	UI.registerAction("logInForCharacter", Player, function (name) {
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
	UI.registerAction("move", Player, function (x, y) {
	/**
	 * @param {String} name Name of character to log in.
	 */
		if (x !== undefined) {
			this.destX = x;
			this.destY = y;
		}
		this.placeSprite();
		var num = this.characterId;
		if (this.x==this.destX && this.y==this.destY) {
			throw new Error("Going to the same cell he is staying at");
		}
		this.getPathTable();
		Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
		var path = this.getPath(this.destX, this.destY);
		if (path.length == 0) {
			this.destX = this.x;
			this.destY = this.y;
			return;
		}
		var nextCellX = path[0].x;
		var nextCellY = path[0].y;
		if (
			Terrain.cells[nextCellX][nextCellY].passability==Terrain.PASS_BLOCKED
			&& Terrain.cells[nextCellX][nextCellY].object
			&& isDoor(Terrain.cells[nextCellX][nextCellY].object.type)
		) {
		// Open door
			Player.addActionToQueue("move");
			this.sendUseObject(nextCellX, nextCellY);
		}
		Net.send({
			a : Net.MOVE,
			dir : Side.d2side(nextCellX-this.x, nextCellY-this.y).getInt()
		});
	});
	UI.registerAction("worldTravel", Player, function (x, y) {
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
	
};
UI.defaultUIActions = {
	leaveLocation: function _() {
		if (Terrain.onGlobalMap) {
			UI.notify("alert", "Вы уже на глобальной карте");
			return;
		}
		leaveLocation();
	},
	enterLocation: function _() {
		if (!Terrain.onGlobalMap) {
			UI.notify("alert", "Вы уже в локации");
			return;
		}
		if (!Player.isPartyLeader) {
		// Если игрок - не лидер группы (и состоит в группе, то он не может входить в локацию сам
			UI.notify("alert", "Вы не лидер партии!");
		} else if (Terrain.onGlobalMap) {
			enterArea();
		}
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
	selectMissile: function _() {
		CellCursor.enterSelectionMode("shootMissile", 11);
	},
	unselectCellAction: function _() {
		CellCursor.exitSelectionMode();
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
		if (Terrain.cameraOrientation == Side.N) {
			rotateCamera(Side.E);
		} else if (Terrain.cameraOrientation == Side.E) {
			rotateCamera(Side.S);
		} else if (Terrain.cameraOrientation == Side.S) {
			rotateCamera(Side.W);
		} else if (Terrain.cameraOrientation == Side.W) {
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