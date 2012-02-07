function handleNextEvent() {
	if (serverAnswerIterator >= serverAnswer.length) {
		if (Player.name !== null) {
		// If player is loaded, then check out after displaying all the events
			Net.send({a:Net.CHECK_OUT});
		}
		return;
	}
	var value = serverAnswer[serverAnswerIterator++];
	if (serverAnswerHandlers[value.e]) {
		console["log"](value.e, serverAnswerIterator);
		serverAnswerHandlers[value.e](value);
	} else {
		throw new Error("Unknown type of non-synchronized answer: "+value.e);
	}
	
}
var serverAnswerHandlers = {
	wt: function serverEventWorldTravel(value) {
	// serverAnswerHandlers.worldTravel ?!??! 
		// Character travels in the world
		var worldPlayer = worldPlayers[value.characterId];
		worldPlayer.move(value.x, value.y);
		if (characters[value.characterId].isClientPlayer) {
			centerWorldCamera(value.x, value.y);
		}
		handleNextEvent();
	},
	we: function serverEventWorldEnter(value) {
		// Character enters world (on global map)
		if (!Player.characterId) {
		// If the world is not loaded for this client (Character stub instead player Character, see variables2.js)
			return;
		}
		characters[value.characterId] = new Character(
			value.characterId, "player", value.x, value.y, 1, value.race, false
		);
		new WorldPlayer(value.worldX, value.worldY, characters[value.characterId]);
		handleNextEvent();
	},
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
			UI.notify("accountPlayersRecieve", data.players);
			Net.setServerAdressesStorage(serverAddress, Global.playerLogin, Global.playerPassword);
		}
		handleNextEvent();
	},
	authentificationSuccessful: function _(value) {
	// Preparing map after login
		UI.setClickHandler(Player.locationClickHandler, Player);
		if (characters[1]) {
			characters[1].cellWrap.parentNode.removeChild(characters[1].cellWrap);
			delete characters[1];
		}
		characters = {};
		showLoadingScreen();
		onlinePlayers = [];
		prepareArea();
		handleNextEvent();
	},
	playerData: function playerData(data) {
		if (Player.cls == undefined) {
			Player.init(data.data);
		}
		Player.display();
		Player.showModel();
		UI.notify("locationLoad");
		UI.setKeyMapping("Default");
		recountWindowSize();
		hideLoadingScreen();
		moveGameField(Player.x, Player.y, false);
		handleNextEvent();
	},
	loadContents: function serverEventLoadContents(data) {
	// Find out whether Player is on global map or in an area and
	// load contents of character's environment after authentification.
	// Server can send two different types of answers: when the Player is in location
	// and when he is on the world map. The type is determined by data.onGlobalMap value
	/* on world map: {
	 *  	w : {c:[[[ground,forest,road,river,race,[objects]]xN]xM]},
	 *  	p : [
	 *  		characterId, name, race, class, level, 
	 *  		maxHp, maxMp, 
	 *  		str, dex, wis, itl, 
	 *  		items, equipment, spells, skills, 
	 *  		worldX, worldY
	 *  	],
	 *  	islead : boolean,
	 *  	online : [[characterId,name,class,race,party,worldX,worldY]xM],
	 *  	chat : [name,message, name,message ...] || 0,
	 *  	invite : [inviterId,inviterName] || 0
	 *  },
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
		showLoadingScreen();
		if (data.onGlobalMap) {
		// World loading
			UI.setClickHandler(handlers.globalMapClickHandler, window);
			Terrain.isPeaceful = false;
			Terrain.onGlobalMap = true;
			prepareArea();
			readWorld(data.w);
			if (Player.cls == undefined) {
				Player.init(data.p);
			}
			readOnlinePlayers(data.online);
			centerWorldCamera(Player.worldX, Player.worldY,true);
			readChatMessages(data.chat);
			data.inv && readInvite(data.inv);
			readEntering(data.en);
			if (Net.callback) {
				Net.callback();
			}
			UI.enterGlobalMapMode();
			
		} else {
		// Area loading
			
			if (Player.cls == null) {
				Player.init(data.p);
			}
			characters[Player.characterId] = Player;
			Player.display();
			moveGameField(Player.x, Player.y, true);
			Player.initVisibility();
			readCharacters(data.online);
			UI.enterLocationMode();
			UI.notify("locationLoad");
			UI.setKeyMapping("Default");
		}
		recountWindowSize();
		hideLoadingScreen();
	},
	chunkContents : function serverEventChunkContents(data) {
		var chunk = Terrain.createChunk(data.x, data.y, data);
		console.log(chunk.x, chunk.y);
		chunk.loadData(data);
		chunk.show();
		recountWindowSize();
		moveGameField(-10,-10);
		hideLoadingScreen();
		handleNextEvent();
	},
	excludeChunk: function(value) {
		Terrain.removeChunk(value.x, value.y);
		handleNextEvent();
	},
	serverInfo: function serverEventServerInfo(data) {
		Net.serverName = data.serverName;
		Net.online = data.online;
		UI.notify("serverInfoRecieve");
//		Net.send({a:Net.LOAD_PASSIVE_CONTENTS});  
		hideLoadingScreen();
	},
	deauth: function serverEventDeauthorization(value) {
		// Deauthorization
		delete characters[value.characterId];
		worldPlayers[value.characterId].remove();
		handleNextEvent();
	},
	chm: function serverEventChatMessage(value) {
		// Chat message
		if (Terrain.onGlobalMap) {
//				addMessageToChat(worldPlayers[value.characterId].name, value.text);
			chat.push([characters[value.characterId].name, value.text]);
		} else {
			characters[value.characterId].showSpeech(value.text);
			chat.push([characters[value.characterId].name, value.text]);
//				addMessageToChat(characters[value.characterId].name, value.text);
		}
		UI.notify("chatMessage");
		handleNextEvent();
	},
	move: function serverEventMove(value) {
		if (characters[value.characterId] == Player && (Player.destX != value.x || Player.destY != value.y)) {
		// If player moves
			if (!(Player.x == Player.destX && Player.y == Player.destY)) {
			// If player moves by UIAction "move"
				Player.addActionToQueue("move");
			}
		} else {
			CellCursor.show();
		}
		characters[value.characterId].showMove(value.x, value.y);	
		UI.notify("environmentChange");
		if (value.characterId == Player.characterId) {
			if (!Player.lootLetterAssigner.isEmpty()) {
				Player.lootLetterAssigner.empty();
			}
			UI.notify("lootChange");
		}
	},
	putOn: function serverEventPutOn(value) {
		// Put on item
		if (Terrain.onGlobalMap) {
			if (value.characterId == Player.characterId) {
				Player.putOn(value.itemId);
				UI.notify("inventoryChange");
				UI.notify("equipmentChange");
			} else {
			// Someone else on world map put on an item
			}
		} else {
			characters[value.characterId].putOn(value.itemId);
			if (value.characterId == Player.characterId) {
				UI.notify("inventoryChange");
				UI.notify("equipmentChange");
			}
			handleNextEvent();
		}
	},
	takeOff: function serverEventTakeOff(value) {
		if (Terrain.onGlobalMap) {
			if (value.characterId == Player.characterId) {
				Player.takeOff(value.itemId);
				UI.notify("inventoryChange");
				UI.notify("equipmentChange");
			} else {
			// Someone else on world map puts an item on
			}
		} else {
			characters[value.characterId].takeOff(value.itemId);
			if (value.characterId == Player.characterId) {
				UI.notify("inventoryChange");
				UI.notify("equipmentChange");
			}
			handleNextEvent();
		}
	},
	drop: function serverEventDrop(value) {
//		if (Terrain.onGlobalMap) {
//			throw new Error("Character "+value.characterId+" drops an item "+items[value.typeId][0]+" on global map!");
//		} else {
//			UI.notify("lootChange");
//			UI.notify("inventoryChange");
			handleNextEvent();
//		}
	},
	pickUp: function serverEventPickUp(value) {
//		if (Terrain.onGlobalMap) {
//			throw new Error("Character "+value.characterId+" picks up an item "+items[value.typeId][0]+" on global map!");
//		} else {
//			UI.notify("inventoryChange");
//			UI.notify("lootChange");
			handleNextEvent();
//		}
	},
	openContainer: function serverEventOpenContainer(value) {
		
		Global.container.items.empty();
		for (var i in value.items) {
			Global.container.items.addNewItem(value.items[i][0], value.items[i][1]);
		}
		UI.notify("containerOpen");
		handleNextEvent();
	},
	takeFromContainer: function serverEventTakeFromContainer(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.characterId+" takes an item "+items[value.typeId][0]+" from container on global map!");
		} else {
			if (value.characterId == Player.characterId) {
				Global.container.items.remove(value.typeId, value.param);
				UI.notify("containerChange");
			}
			handleNextEvent();
		}
	},
	putToContainer: function serverEventPutToContainer(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.characterId+" puts an item "+items[value.typeId][0]+" to container on global map!");
		} else {
			if (value.characterId == Player.characterId) {
				Global.container.items.addNewItem(value.typeId, value.param);
				UI.notify("containerChange");
			}
			handleNextEvent();
		}
	},
	meleeAttack: function serverEventMeleeAttack(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.attackerId+" attacks on global map!");
		} else {
			characters[value.attackerId].showAttack(value.aimId, false);
		}
	},
	damage: function serverEventDamage(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.characterId+" was damaged on global map!");
		} else {
		// handleNextEvent() is inside showDamage
			characters[value.characterId].showDamage(value.amount, value.type);
		}
	},
	changeMana: function serverEventChangeMana(value) {
		characters[value.characterId].mp = value.value;
		if (value.characterId == Player.characterId) {
			UI.notify("manaChange");
		}
		handleNextEvent();
	},
	changeEnergy: function serverEventChangeEnergy(value) {
		characters[value.characterId].ep = value.value;
		if (value.characterId == Player.characterId) {
			UI.notify("energyChange");
		}
		handleNextEvent();
	},
	death: function serverEventDeath(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.characterId+" had an attempt to die on global map!");
		} else {
			characters[value.characterId].showDeath();
		}
	},
	itemAppear: function serverEventItemAppear(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			var item;
			if (isUnique(value.typeId)) {
				item = new UniqueItem(value.typeId, value.param);
				
			} else {
				item = new ItemPile(value.typeId, value.param);
			}
			Terrain.cells[value.x][value.y].addItem(item);
			if (Player.x == value.x && Player.y == value.y) {
				Player.lootLetterAssigner.addObject(item);
				UI.notify("lootChange");
			}
		}
		handleNextEvent();
	},
	itemDisappear: function serverEventItemDisappear(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			// Create a copy if item and delete the original item using copy as a key.
			var item;
			if (isUnique(value.typeId)) {
				item = new UniqueItem(value.typeId, value.param);
			} else {
				item = new ItemPile(value.typeId, value.param);
			}
			Terrain.cells[value.x][value.y].removeItem(value.typeId, value.param);
			if (value.x == Player.x && value.y == Player.y) {
				Player.lootLetterAssigner.removeObject(item);
				UI.notify("lootChange");
			}
		}
		handleNextEvent();
	},
	castSpell: function serverEventCastSpell(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Cast spell on global map!");
		} else {
			UI.notify("spellCast");
			
			if (value.characterId == Player.characterId) {
				Player.spellAimId=-1;
				Player.spellX=-1;
				Player.spellY=-1;
			}
//				new effectTypes.confuse(this.x, this.y, this.x, this.y,  1000, 1000, 1000, 1000, function() {
//					handleNextEvent();
//				});
			handleNextEvent();
//				weatherEffect = new effectTypes.rain(
//					this.x, this.y, this.x, this.y, 
//					UI.width/2+100, UI.height/2+100, 
//					UI.width/2+100, UI.height/2+100);			
		}
	},
	missileFlight: function serverEventMissileFlight(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else {
			Character.prototype.showMissileFlight(value.fromX, value.fromY, value.toX, value.toY, value.missile);
		}
	},
	loseItem: function serverEventLoseItem(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == Player.characterId) {
			Player.loseItem(value.typeId, value.param);
		}
		handleNextEvent();
	},
	getItem: function serverEventGetItem(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == Player.characterId) {
			Player.getItem(value.typeId, value.param);
		}
		handleNextEvent();
	},
	objectAppear: function serverEventObjectAppear(value) {
		Terrain.createObject(value.x, value.y, value.object);
		Terrain.displayObject(value.x, value.y);
		Player.updateVisibility();
		handleNextEvent();
	},
	objectDisappear: function serverEventObjectDisappear(value) {
		Terrain.getCell(value.x,value.y).object.remove(value.x,value.y);
		Player.updateVisibility();
		handleNextEvent();
	},	
	characterAppear: function serverEventCharacterAppear(value) {
		characters[value.characterId] = new Character(value.characterId, value.type, value.x, value.y, value.fraction, value.hp, value.maxHp);
		characters[value.characterId].display();
		handleNextEvent();
	},	
	nextTurn: function serverEventNextTurn(value) {
		if (value.characterId == Player.characterId) {
			if (Player.actionQueue.length > 0) {
				Player.doActionFromQueue();
			}
		}
		// Here must be no handleNextEvent()
	},	
	worldTravel: function serverEventWorldTravel(value) {
		worldPlayers[value.characterId].move(value.x, value.y);
		handleNextEvent();
	},
	useObject: function serverEventUseObject(value) {
		handleNextEvent();
	},
	sound: function serverEventSound(value) {
		showSound(value.x, value.y, value.type);
	},
	soundSourceAppear: function serverEventSoundSourceAppear(value) {
		new SoundSource(value.x, value.y, value.type);
		handleNextEvent();
	},
	soundSourceDisappear: function serverEventSoundSourceDisappear(value) {
		Terrain.cells[value.x][value.y].soundSource.remove();
		handleNextEvent();
	},
	dialoguePoint: function serverEventDialoguePoint(value) {
		if (value.playerId == Player.characterId) {
			UI.notify("dialoguePointRecieve", {phrase:value.phrase, answers:value.answers});
		}
		handleNextEvent();
	},
	dialogueEnd: function serverEventDialogueEnd(value) {
		if (value.characterId == Player.characterId) {
			UI.notify("dialogueEnd");
		}
		handleNextEvent();
	},
	effectStart: function serverEventEffectStart(value) {
		characters[value.characterId].showEffectStart(value.effectId);
		handleNextEvent();
	},
	effectEnd: function serverEventEffectEnd(value) {
		characters[value.characterId].showEffectEnd(value.effectId);
		handleNextEvent();
	},
	attrChange: function serverEventAttrChange(value) {
		characters[value.characterId].changeAttribute(value.attrId, value.value);
		handleNextEvent();
	},
	changePlaces: function serverEventChangePlaces(value) {
		var character1 = characters[value.character1Id];
		var character2 = characters[value.character2Id];
		Terrain.cells[character1.x][character1.y].character = character1;
		Terrain.cells[character2.x][character2.y].character = character2;
		handleNextEvent();
	},
	jump: function serverEventJump(value) {
		var character = characters[value.characterId];
		character.destX = character.x;
		character.destY = character.y;
		handleNextEvent();
	}
};