/*
 * File serverEvents.js represents the active part of MVC model. It uses raw 
 * data recieved from server to manipulate the model on both high and low 
 * levels; it also fires events corresponding to recieved data.
 */
(function() {
	function outGameHandleNextEvent() {
		if (serverAnswerIterator >= serverAnswer.length) {
			return;
		}
		var value = serverAnswer[serverAnswerIterator++];
		if (serverAnswerHandlers[value.e]) {
			// console["log"]("recieve", value.e, value);
			serverAnswerHandlers[value.e](value);
		} else {
			throw new Error("Unknown type of non-synchronized answer: "+value.e);
		}
	}
	function inGameHandleNextEvent() {
		if (serverAnswerIterator >= serverAnswer.length) {
			Net.send({a:Net.CHECK_OUT});
			return;
		}
		var value = serverAnswer[serverAnswerIterator++];
		if (serverAnswerHandlers[value.e]) {
			// console["log"]("recieve", value.e, value);
			serverAnswerHandlers[value.e](value);
		} else {
			throw new Error("Unknown type of non-synchronized answer: "+value.e);
		}
	}
	/*
	 * There are 2 event handling functions: in-game and out-game
	 */
	Net.handleNextEvent = outGameHandleNextEvent;
	var characters = Characters.getInstance();
	var worldData = World.getInstance();
	var serverAnswerHandlers = {
		login: function serverEventLogin(data) {
				/* 	in: {
					l: String login,
					p: String password,
				}
				out: {players:[{characterId, name, race, class, level, equipment}xN]}
			*/
			if (data.error !== undefined) {
			// If there is an error
				UI.notify("loginError", data.error);
			} else {
				Net.accountPlayers = data.players;
				UI.notify("accountPlayersRecieve", data.players);
				Net.setServerAdressesStorage(serverAddress, Global.playerLogin, Global.playerPassword);
			}
			Net.handleNextEvent();
		},
		authentificationSuccessful: function _(value) {
		// Preparing map after login
			if (characters[1]) {
				characters[1].cellWrap.parentNode.removeChild(characters[1].cellWrap);
				delete characters[1];
			}
	//		UI.showLoadingScreen();
			onlinePlayers = [];
			Net.handleNextEvent = inGameHandleNextEvent;
			Net.handleNextEvent();
		},
		playerData: function playerData(event) {
			characters.createPlayer(event.data);
			if (Player.cls == undefined) {
				Player.init(event.data);
			}
			
			Events.fire("playerDataLoaded", event);
			Events.fire("locationLoad");
			GameField.showCharacter(
				Characters.player("characterId"), 
				Characters.player("x"), 
				Characters.player("y")
			);
			/* z */ // UI.setKeyMapping("Default");
	//		UI.hideLoadingScreen();
			Player.initVisibility();
	//		Player.updateVisibility(0,0);
			GameField.setViewPort(characters.player.x, characters.player.y, 13, 9);
			// Display terrain (we can do it only now because only now we have
			// player's vision range, coordinates and other info)
			Net.handleNextEvent();
		},
		loadContents: function serverEventLoadContents(data) {
		/*
		 *  in location: {
		 *  	onGlobalMap: false,
		 *  	l : {w,h,locationId,c:[[ground,forest,road,river,race,[objects]]xN]},
		 *   	p : [
		 *  		characterId, name, race, class, level,
		 *  		maxHp, maxMp, 
		 *  		str, dex, wis, itl, 
		 *  		items, equipment, spells, skills,
		 *  		x, y
		 *  	],
		 *  	islead : boolean,
		 *		online : [[characterId,x,y,name,maxHp,hp,maxMp,mp,effects,equipment(,cls,race)|(,type)]xM],
		 *  }
		 */
			if (data.error === 0) {
				throw new Error("Login is empty");
			} else if (data.error == 1) {
				throw new Error("Login is empty");
			} else if (data.error == 2) {
				throw new Error("No such account");
			} else if (data.error == 4) {
				throw new Error("No such character on account");
			}
			UI.showLoadingScreen();
			// Area loading
			if (Player.cls == null) {
				Player.init(data.p);
			}
			characters[Player.characterId] = Player;
			Player.display();

			GameField.clearGameZone();
			GameFrame.moveGameField(Player.x, Player.y, true);
			Player.initVisibility();
			for (var i=0;i<data.length;i++) {
				Characters.createCharacter(data[i]);
			}
			UI.enterLocationMode();
			UI.notify("locationLoad");
			UI.setKeyMapping("Default");
			UI.hideLoadingScreen();
		},
		chunkContents : function serverEventChunkContents(data) {
			var chunk = new worldData.Chunk(data.x, data.y);
			worldData.chunks.set(data.x, data.y, chunk);
			chunk.loadData(data);
			Events.fire("chunkLoad", data);
			Net.handleNextEvent();
		},
		excludeChunk: function _(value) {
			// Chunks are specified by their left-top coordinate {x;y}
			if (!worldData.chunks.remove(value.x, value.y)) {
				throw new Error("Chunk "+value.x+";"+value.y+" has not been loaded yet, and can not be removed");
			}
			Events.fire("chunkUnload", {x:value.x, y:value.y});
			Net.handleNextEvent();
		},
		serverInfo: function serverEventServerInfo(data) {
			Net.serverName = data.serverName;
			Net.online = data.online;
			worldData.setChunkWidth(data.chunkWidth);
			UI.notify("serverInfoRecieve");
	//		Net.send({a:Net.LOAD_PASSIVE_CONTENTS});  
			UI.hideLoadingScreen();
		},
		deauth: function serverEventDeauthorization(value) {
			// Deauthorization
			delete characters[value.characterId];
			worldPlayers[value.characterId].remove();
			Net.handleNextEvent();
		},
		chm: function serverEventChatMessage(value) {
			// Chat message
			characters[value.characterId].showSpeech(value.text);
			chat.push([characters[value.characterId].name, value.text]);
//				addMessageToChat(characters[value.characterId].name, value.text);
			UI.notify("chatMessage");
			Net.handleNextEvent();
		},
		move: function serverEventMove(value) {
			var character = characters.characters[value.characterId];
			var prevX = character.x, prevY = character.y;
			var c = worldData.getCell(prevX, prevY);
			delete c.character;
			c.p = StaticData.PASSABILITY_FREE;
			character.x = value.x;
			character.y = value.y;
			worldData.getCell(character.x, character.y).p = StaticData.PASSABILITY_SEE;
			worldData.getCell(character.x, character.y).character = character;
			if (Player.sees(prevX, prevY) && !Player.sees(character.x, character.y)) {
				GameField.unshowCharacter(character.characterId);
			} else if (Player.sees(character.x, character.y) && !Player.sees(character.x, character.y)) {
				GameField.showCharacter(character.characterId, character.x, character.y);
			}
			if (value.characterId === Characters.player("characterId")) {
				Player.updateVisibility(character.x-prevX, character.y-prevY);
				UI.notify("lootChange");
			}
			GameField.moveCharacter(character.characterId, value.x, value.y);
			GameField.setViewPort(value.x, value.y);
			UI.notify("environmentChange");
			Net.handleNextEvent();
		},
		putOn: function serverEventPutOn(value) {
			// Put on item
			var character = characters[value.characterId];
			var item = character.items.getUnique(itemId);
			var slot = getSlotFromClass(items[item.typeId][1]);
			if (slot==9 && character.equipment.getItemInSlot(9)) {
				slot = 10;
			}
			character.equipment.putOn(item);
			Events.fire("loseItem", value);
			Events.fire("equipmentChange", value);
			Net.handleNextEvent();
		},
		takeOff: function serverEventTakeOff(value) {
			var character = characters[value.characterId];
			var slot = 0;
			// For list of slots search items.js
			for (; slot<10; slot++ ) {
				var item = character.equipment.getItemInSlot(slot);
				if (item && item.itemId==itemId) {
					break;
				}
			}
			if (slot==10) {
				throw new Error("Not found item "+itemId+" in equipment");
			}
			if ( !character.equipment.hasItemInSlot(slot)) {
				throw new Error("Character "+character.name
						+" is trying to take off an item that he doesn't wear");
				return false;
			}
			character.equipment.takeOffFromSlot(slot);
			Events.fire("getItem", value.characterId);
			Events.fire("equipmentChange", value.characterId);
			Net.handleNextEvent();
		},
		drop: function serverEventDrop(value) {
			throw new Error("Not implemented");
			Net.handleNextEvent();
		},
		pickUp: function serverEventPickUp(value) {
			throw new Error("Not impl");
			Net.handleNextEvent();
		},
		openContainer: function serverEventOpenContainer(value) {
			
			Global.container.items.empty();
			for (var i in value.items) {
				Global.container.items.addNewItem(value.items[i][0], value.items[i][1]);
			}
			UI.notify("containerOpen");
			Net.handleNextEvent();
		},
		takeFromContainer: function serverEventTakeFromContainer(value) {
			throw new Error("Not imple");
			if (value.characterId == Player.characterId) {
				Global.container.items.remove(value.typeId, value.param);
				UI.notify("containerChange");
			}
			Net.handleNextEvent();
		},
		putToContainer: function serverEventPutToContainer(value) {
			throw new Error("Not impl");
			if (value.characterId == Player.characterId) {
				Global.container.items.addNewItem(value.typeId, value.param);
				UI.notify("containerChange");
			}
			Net.handleNextEvent();
		},
		meleeAttack: function serverEventMeleeAttack(value) {
			Events.fire("meleeAttack", value);
			// Net.handleNextEvent();
			throw new Error("You have to handle it somehow!");
		},
		damage: function serverEventDamage(value) {
			characters[value.characterId].hp -= value.amount
			Event.fire("damageGot", value);
			// Net.handleNextEvent()
			throw new Error("You have to handle it somehow!");
		},
		changeMana: function serverEventChangeMana(value) {
			characters[value.characterId].mp = value.value;
			if (value.characterId == Player.characterId) {
				UI.notify("manaChange");
			}
			Net.handleNextEvent();
		},
		changeEnergy: function serverEventChangeEnergy(value) {
			characters[value.characterId].ep = value.value;
			if (value.characterId == Player.characterId) {
				UI.notify("energyChange");
			}
			Net.handleNextEvent();
		},
		death: function serverEventDeath(value) {
			throw new Error("Not impl");
			Characters.characterRemove(value.characterId);
			world.getCell(this.x, this.y).p = StaticData.PASSABILITY_FREE;
			Events.fire("characterDisappear");
		},
		itemAppear: function serverEventItemAppear(value) {
			throw new Error("Not implemented");
			var item;
			if (isUnique(value.typeId)) {
				item = new UniqueItem(value.typeId, value.param);
			} else {
				item = new ItemPile(value.typeId, value.param);
			}
			var cell = worldData.getCell(value.x, value.y);
			if (!("items" in cell)) {
				cell.items = new ItemSet();
			}
			cell.items.addItem(x,y,item);
			if (Player.x == value.x && Player.y == value.y) {
				Events.fire("lootChange");
			}
			Net.handleNextEvent();
		},
		itemDisappear: function serverEventItemDisappear(value) {
			throw new Error("Not implemented");
			// Create a copy if item and delete the original item using copy as a key.
			var item;
			if (isUnique(value.typeId)) {
				item = new UniqueItem(value.typeId, value.param);
			} else {
				item = new ItemPile(value.typeId, value.param);
			}
			worldData.getCell(value.x,value.y)
				.removeItem(value.typeId, value.param);
			
			var cell = worldData.getCell(x, y);
			if (!("items" in cell)) {
				throw new Error("Cannot remove item "+item+": cell "+x+":"+y+" has no items");
			}
			cell.items.remove(item);

			if (value.x == Player.x && value.y == Player.y) {
				UI.notify("lootChange");
			}
			Net.handleNextEvent();
		},
		castSpell: function serverEventCastSpell(value) {
			throw new Error("Not implemanted");
			UI.notify("spellCast");
			
			if (value.characterId == Player.characterId) {
				Player.spellAimId=-1;
				Player.spellX=-1;
				Player.spellY=-1;
			}
//				new effectTypes.confuse(this.x, this.y, this.x, this.y,  1000, 1000, 1000, 1000, function() {
//					Net.handleNextEvent();
//				});
			Net.handleNextEvent();
//				weatherEffect = new effectTypes.rain(
//					this.x, this.y, this.x, this.y, 
//					UI.width/2+100, UI.height/2+100, 
//					UI.width/2+100, UI.height/2+100);			
		},
		missileFlight: function serverEventMissileFlight(value) {
			Events.fire("missileFlight", value);
		},
		loseItem: function serverEventLoseItem(value) {
			throw new Error("Not implemanted");
			if (isUnique(typeId)) {
				characters[value.characterId].items.removeUnique(param);
			} else {
				characters[value.characterId].items.removePile(typeId, param);
			}
			Events.fire("loseItem", value);
			Net.handleNextEvent();
		},
		getItem: function serverEventGetItem(value) {
			characters[value.characterId].items.addNewItem(typeId, param);
			Events.fire("getItem", value);
			Net.handleNextEvent();
		},
		objectAppear: function serverEventObjectAppear(value) {
			console.log("create",value.object);
			var c = worldData.getCell(value.x, value.y);
			c.o = value.object;
			c.p = StaticData.object(value.object).passability;
			GameField.showObject(value.x, value.y, value.object);
			Player.updateVisibility(0, 0);
			Net.handleNextEvent();
		},
		objectDisappear: function serverEventObjectDisappear(value) {
			var object = worldData.getCell(value.x, value.y).o;
			console.log("remove", object);
			worldData.getCell(value.x, value.y).p = StaticData.PASSABILITY_FREE;
			GameField.removeObject(value.x, value.y);
			Events.fire("objectDisappear", {x:value.x, y:value.y, o:object});
			Player.updateVisibility(0, 0);
			Net.handleNextEvent();
		},	
		characterAppear: function serverEventCharacterAppear(value) {
			throw new Error("Not implemented");
			// characters[value.characterId] = new Character(value.characterId, value.type, value.x, value.y, value.fraction, value.hp, value.maxHp);
			// characters[value.characterId].display();
			var character = Characters.createCharacter(value);
			GameField.displayCharacter(value.x, value.y, character);
			Events.fire("characterAppear");
			Net.handleNextEvent();
		},	
		nextTurn: function serverEventNextTurn(value) {
			if (value.characterId == Player.characterId) {
				if (Player.actionQueue.length > 0) {
					Player.doActionFromQueue();
				}
			}
			Events.fire("nextTurn");
			// Here must be no Net.handleNextEvent()
		},	
		worldTravel: function serverEventWorldTravel(value) {
			worldPlayers[value.characterId].move(value.x, value.y);
			Net.handleNextEvent();
		},
		useObject: function serverEventUseObject(value) {
			Net.handleNextEvent();
		},
		sound: function serverEventSound(value) {
			showSound(value.x, value.y, value.type);
		},
		soundSourceAppear: function serverEventSoundSourceAppear(value) {
			new SoundSource(value.x, value.y, value.type);
			Net.handleNextEvent();
		},
		soundSourceDisappear: function serverEventSoundSourceDisappear(value) {
			throw new Error("Not implemented");
			World.cells[value.x][value.y].soundSource.remove();
			Net.handleNextEvent();
		},
		dialoguePoint: function serverEventDialoguePoint(value) {
			if (value.playerId == Player.characterId) {
				UI.notify("dialoguePointRecieve", {phrase:value.phrase, answers:value.answers});
			}
			Net.handleNextEvent();
		},
		dialogueEnd: function serverEventDialogueEnd(value) {
			if (value.characterId == Player.characterId) {
				UI.notify("dialogueEnd");
			}
			Net.handleNextEvent();
		},
		effectStart: function serverEventEffectStart(value) {
			GameField.showEffectStart(value.characterId, value.effectId);
			Net.handleNextEvent();
		},
		effectEnd: function serverEventEffectEnd(value) {
			GameField.showEffectEnd(value.characterId, value.effectId);
			Net.handleNextEvent();
		},
		attrChange: function serverEventAttrChange(value) {
			throw new Error("Not implemented!");
			UI.notify("attributeChange", [attrId, value]);
			Net.handleNextEvent();
		},
		changePlaces: function serverEventChangePlaces(value) {
			throw new Error("Not implemented!");
			var character1 = characters[value.character1Id];
			var character2 = characters[value.character2Id];
			World.cells[character1.x][character1.y].character = character1;
			World.cells[character2.x][character2.y].character = character2;
			Net.handleNextEvent();
		},
		jump: function serverEventJump(value) {
			var character = characters[value.characterId];
			character.destX = character.x;
			character.destY = character.y;
			Net.handleNextEvent();
		},
		enterState: function serverEventEnterState(value) {
			characters[value.characterId].stateId = value.stateId;
			if (Player.characterId === value.characterId) {
				UI.notify("stateEntered");
			}
			Net.handleNextEvent();
		}
	};
})();
