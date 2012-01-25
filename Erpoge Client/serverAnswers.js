function handleNextEvent() {
	if (serverAnswerIterator >= serverAnswer.length) {
		if (!Terrain.onGlobalMap) {
			Net.send({a:Net.CHECK_OUT});
		}
		return;
	}
	var value = serverAnswer[serverAnswerIterator++];
	if (serverAnswerHandlers[value.e]) {
		serverAnswerHandlers[value.e](value);
	} else {
		throw new Error("Unknown type of non-synchronized answer: "+value.e);
	}
//	console["log"](serverAnswer);	
}
serverAnswerHandlers = {
	wt: function _(value) {
		// Character travels in the world
		var worldPlayer = worldPlayers[value.characterId];
		worldPlayer.move(value.x, value.y);
		if (characters[value.characterId].isClientPlayer) {
			centerWorldCamera(value.x, value.y);
		}
		handleNextEvent();
	},
	we: function _(value) {
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
	login: function _(data) {
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
	loadContents: function _(data) {
	// Find out whether Player is on global map or in an area and
	// load contents of character's environment after authentification.
	// Server can send two different types of answers: when the Player is in location
	// and when he is on the world map. The type is determined by data.onGlobalMap value
	/* on world map: {
	 * 		onGlobalMap: true,
	 *  	w : {w,h,c:[[ground,forest,road,river,race,[objects]]xN]},
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
			UI.notify("worldLoad");
			UI.setKeyMapping("Default");
		} else {
		// Area loading
			if (characters[1]) {
				characters[1].cellWrap.parentNode.removeChild(characters[1].cellWrap);
				delete characters[1];
			}
			characters = {};
			Terrain.onGlobalMap=false;
			showLoadingScreen();
			onlinePlayers = [];
			Terrain.width = data.l.w;
			height = data.l.h;
			Terrain.isPeaceful = data.l.p;
			onlinePlayers = [];
			prepareArea();
			readLocation(data.l);
			if (Player.cls == undefined) {
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
	loadPassiveContents : function _(data) {
		Terrain.onGlobalMap = true;
		readWorld(data);
		recountWindowSize();
		centerWorldCamera(Terrain.width/2, Terrain.height/2, true);
		hideLoadingScreen();
	},
	deauth: function _(value) {
		// Deauthorization
		delete characters[value.characterId];
		worldPlayers[value.characterId].remove();
		handleNextEvent();
	},
	chm: function _(value) {
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
	move: function _(value) {
		if (characters[value.characterId] == Player && (Player.destX != value.x || Player.destY != value.y)) {
		// ������� ������ ���� ����� showMove - � showMove ���������� check out (��������, ��� ���������� ���������)
			Player.addActionToQueue("move");
		} else {
			CellCursor.show();
		}
		characters[value.characterId].showMove(value.x, value.y);	
		UI.notify("environmentChange");
		if (value.characterId == Player.characterId) {
			UI.notify("lootChange");
		}
	},
	putOn: function _(value) {
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
	takeOff: function _(value) {
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
	drop: function _(value) {
//		if (Terrain.onGlobalMap) {
//			throw new Error("Character "+value.characterId+" drops an item "+items[value.typeId][0]+" on global map!");
//		} else {
//			UI.notify("lootChange");
//			UI.notify("inventoryChange");
			handleNextEvent();
//		}
	},
	pickUp: function _(value) {
//		if (Terrain.onGlobalMap) {
//			throw new Error("Character "+value.characterId+" picks up an item "+items[value.typeId][0]+" on global map!");
//		} else {
//			UI.notify("inventoryChange");
//			UI.notify("lootChange");
			handleNextEvent();
//		}
	},
	openContainer: function _(value) {
		
		Global.container.items.empty();
		for (var i in value.items) {
			Global.container.items.addNewItem(value.items[i][0], value.items[i][1]);
		}
		UI.notify("containerOpen");
		handleNextEvent();
	},
	takeFromContainer: function _(value) {
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
	putToContainer: function _(value) {
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
	meleeAttack: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.attackerId+" attacks on global map!");
		} else {
			characters[value.attackerId].showAttack(value.aimId, false);
		}
	},
	damage: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.characterId+" was damaged on global map!");
		} else {
		// handleNextEvent() is inside showDamage
			characters[value.characterId].showDamage(value.amount, value.type);
		}
	},
	changeMana: function _(value) {
		characters[value.characterId].mp = value.value;
		if (value.characterId == Player.characterId) {
			UI.notify("manaChange");
		}
		handleNextEvent();
	},
	changeEnergy: function _(value) {
		characters[value.characterId].ep = value.value;
		if (value.characterId == Player.characterId) {
			UI.notify("energyChange");
		}
		handleNextEvent();
	},
	death: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Character "+value.characterId+" had an attempt to die on global map!");
		} else {
			characters[value.characterId].showDeath();
		}
	},
	itemAppear: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			if (isUnique(value.typeId)) {
				Terrain.cells[value.x][value.y].addItem(new UniqueItem(value.typeId, value.param));
			} else {
				Terrain.cells[value.x][value.y].addItem(new ItemPile(value.typeId, value.param));
			}
			if (Player.x == value.x && Player.y == value.y) {
				UI.notify("lootChange");
			}
		}
		handleNextEvent();
	},
	itemDisappear: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			Terrain.cells[value.x][value.y].removeItem(value.typeId, value.param);
			if (value.x == Player.x && value.y == Player.y) {
				UI.notify("lootChange");
			}
		}
		handleNextEvent();
	},
	castSpell: function _(value) {
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
	missileFlight: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else {
			Character.prototype.showMissileFlight(value.fromX, value.fromY, value.toX, value.toY, value.missile);
		}
	},
	loseItem: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == Player.characterId) {
			Player.loseItem(value.typeId, value.param);
		}
		handleNextEvent();
	},
	getItem: function _(value) {
		if (Terrain.onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == Player.characterId) {
			Player.getItem(value.typeId, value.param);
		}
		handleNextEvent();
	},
	objectAppear: function _(value) {
		new GameObject(value.x, value.y, value.object).show();
		Player.updateVisibility();
		handleNextEvent();
	},
	objectDisappear: function _(value) {
		Terrain.cells[value.x][value.y].object.remove();
		Player.updateVisibility();
		handleNextEvent();
	},	
	characterAppear: function _(value) {
		characters[value.characterId] = new Character(value.characterId, value.type, value.x, value.y, value.fraction, value.hp, value.maxHp);
		characters[value.characterId].display();
		handleNextEvent();
	},	
	nextTurn: function _(value) {
		if (value.characterId == Player.characterId) {
			if (Player.actionQueue.length > 0) {
				Player.doActionFromQueue();
			}
		}
		// Here must be no handleNextEvent()
	},	
	worldTravel: function _(value) {
		worldPlayers[value.characterId].move(value.x, value.y);
		handleNextEvent();
	},
	useObject: function _(value) {
		handleNextEvent();
	},
	sound: function _(value) {
		showSound(value.x, value.y, value.type);
	},
	soundSourceAppear: function _(value) {
		new SoundSource(value.x, value.y, value.type);
		handleNextEvent();
	},
	soundSourceDisappear: function _(value) {
		Terrain.cells[value.x][value.y].soundSource.remove();
		handleNextEvent();
	},
	dialoguePoint: function _(value) {
		if (value.playerId == Player.characterId) {
			UI.notify("dialoguePointRecieve", {phrase:value.phrase, answers:value.answers});
		}
		handleNextEvent();
	},
	dialogueEnd: function _(value) {
		if (value.characterId == Player.characterId) {
			UI.notify("dialogueEnd");
		}
		handleNextEvent();
	},
	effectStart: function _(value) {
		characters[value.characterId].showEffectStart(value.effectId);
		handleNextEvent();
	},
	effectEnd: function _(value) {
		characters[value.characterId].showEffectEnd(value.effectId);
		handleNextEvent();
	},
	attrChange: function _(value) {
		characters[value.characterId].changeAttribute(value.attrId, value.value);
		handleNextEvent();
	},
	changePlaces: function _(value) {
		var character1 = characters[value.character1Id];
		var character2 = characters[value.character2Id];
		Terrain.cells[character1.x][character1.y].character = character1;
		Terrain.cells[character2.x][character2.y].character = character2;
		handleNextEvent();
	},
	jump: function _(value) {
		var character = characters[value.characterId];
		character.destX = character.x;
		character.destY = character.y;
		handleNextEvent();
	}
};