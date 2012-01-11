function handleNextEvent() {
	if (serverAnswerIterator >= serverAnswer.length) {
		if (!onGlobalMap) {
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
var serverAnswerHandlers = {
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
		if (!player.characterId) {
		// If the world is not loaded for this client (Character stub instead player Character, see variables2.js)
			return;
		}
		characters[value.characterId] = new Character(
			value.characterId, "player", value.x, value.y, 1, value.race, false
		);
		new WorldPlayer(value.worldX, value.worldY, characters[value.characterId]);
		handleNextEvent();
	},
	deauth: function _(value) {
		// Deauthorization
		delete characters[value.characterId];
		worldPlayers[value.characterId].remove();
		handleNextEvent();
	},
	chm: function _(value) {
		// Chat message
		if (onGlobalMap) {
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
		if (characters[value.characterId] == player && (player.destX != value.x || player.destY != value.y)) {
		// ������� ������ ���� ����� showMove - � showMove ���������� check out (��������, ��� ���������� ���������)
			player.addActionToQueue(player.sendMove);
		} else {
			CellCursor.show();
		}
		characters[value.characterId].showMove(value.x, value.y);	
		UI.notify("environmentChange");
		if (value.characterId == player.characterId) {
			UI.notify("lootChange");
		}
	},
	putOn: function _(value) {
		// Put on item
		if (onGlobalMap) {
			if (value.characterId == player.characterId) {
				player.putOn(value.itemId);
				UI.notify("inventoryChange");
				UI.notify("ammunitionChange");
			} else {
			// Someone else on world map put on an item
			}
		} else {
			characters[value.characterId].putOn(value.itemId);
			if (value.characterId == player.characterId) {
				UI.notify("inventoryChange");
				UI.notify("ammunitionChange");
			}
			handleNextEvent();
		}
	},
	takeOff: function _(value) {
		if (onGlobalMap) {
			if (value.characterId == player.characterId) {
				player.takeOff(value.itemId);
				UI.notify("inventoryChange");
				UI.notify("ammunitionChange");
			} else {
			// Someone else on world map puts an item on
			}
		} else {
			characters[value.characterId].takeOff(value.itemId);
			if (value.characterId == player.characterId) {
				UI.notify("inventoryChange");
				UI.notify("ammunitionChange");
			}
			handleNextEvent();
		}
	},
	drop: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" drops an item "+items[value.typeId][0]+" on global map!");
		} else {
			UI.notify("lootChange");
			UI.notify("inventoryChange");
			handleNextEvent();
		}
	},
	pickUp: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" picks up an item "+items[value.typeId][0]+" on global map!");
		} else {
			UI.notify("inventoryChange");
			UI.notify("lootChange");
			handleNextEvent();
		}
	},
	openContainer: function _(value) {
		UI.setMode(UI.MODE_CONTAINER);
		Global.container.items.empty();
		for (var i in value.items) {
			Global.container.items.addItem(value.items[i][0], value.items[i][1]);
		}
		UI.notify("containerOpen");
		handleNextEvent();
	},
	takeFromContainer: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" takes an item "+items[value.typeId][0]+" from container on global map!");
		} else {
			if (value.characterId == player.characterId) {
				Global.container.items.remove(value.typeId, value.paramparam);
				UI.notify("containerChange");
			}
			handleNextEvent();
		}
	},
	putToContainer: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" puts an item "+items[value.typeId][0]+" to container on global map!");
		} else {
			if (value.characterId == player.characterId) {
				Global.container.items.addItem(value.typeId, value.param);
				UI.notify("containerChange");
			}
			handleNextEvent();
		}
	},
	meleeAttack: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.attackerId+" attacks on global map!");
		} else {
			characters[value.attackerId].showAttack(value.aimId, false);
		}
	},
	damage: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" was damaged on global map!");
		} else {
		// handleNextEvent() is inside showDamage
			characters[value.characterId].showDamage(value.amount, value.type);
		}
	},
	death: function _(value) {
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" had an attempt to die on global map!");
		} else {
			characters[value.characterId].showDeath();
		}
	},
	itemAppear: function _(value) {
		if (onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			if (isUnique(value.typeId)) {
				Terrain.cells[value.x][value.y].addItem(new UniqueItem(value.typeId, value.param));
			} else {
				Terrain.cells[value.x][value.y].addItem(new ItemPile(value.typeId, value.param));
			}				
		}
		handleNextEvent();
	},
	itemDisappear: function _(value) {
		if (onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			Terrain.cells[value.x][value.y].removeItem(value.typeId, value.param);
			if (value.x == player.x && value.y == player.y) {
				UI.notify("lootChange");
			}
		}
		handleNextEvent();
	},
	castSpell: function _(value) {
		if (onGlobalMap) {
			throw new Error("Cast spell on global map!");
		} else {
			UI.notify("spellCast");
			
			if (value.characterId == player.characterId) {
				player.unselectSpell();
				player.spellAimId=-1;
				player.spellX=-1;
				player.spellY=-1;
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
		if (onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else {
			Character.prototype.showMissileFlight(value.fromX, value.fromY, value.toX, value.toY, value.missile);
		}
	},
	loseItem: function _(value) {
		if (onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == player.characterId) {
			player.loseItem(value.typeId, value.param);
		}
		handleNextEvent();
	},
	getItem: function _(value) {
		if (onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == player.characterId) {
			player.getItem(value.typeId, value.param);
		}
		handleNextEvent();
	},
	objectAppear: function _(value) {
		new GameObject(value.x, value.y, value.object).show();
		player.updateVisibility();
		handleNextEvent();
	},
	objectDisappear: function _(value) {
		Terrain.cells[value.x][value.y].object.remove();
		player.updateVisibility();
		handleNextEvent();
	},	
	characterAppear: function _(value) {
		characters[value.characterId] = new Character(value.characterId, value.type, value.x, value.y, value.fraction);
		characters[value.characterId].display();
		handleNextEvent();
	},	
	nextTurn: function _(value) {
		if (value.characterId == player.characterId) {
			if (player.actionQueue.length > 0) {
				player.doActionFromQueue();
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
		if (value.playerId == player.characterId) {
			UI.notify("dialoguePointRecieve", {phrase:value.phrase, answers:value.answers});
		}
		handleNextEvent();
	},
	dialogueEnd: function _(value) {
		if (value.characterId == player.characterId) {
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
	}
};