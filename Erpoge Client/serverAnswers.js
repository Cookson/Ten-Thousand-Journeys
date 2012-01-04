function handleNextEvent() {
	if (serverAnswerIterator >= serverAnswer.length) {
		if (!onGlobalMap) {
			Net.send({a:Net.CHECK_OUT});
		}
		return;
	}
	var value = serverAnswer[serverAnswerIterator++];
//	console["log"](serverAnswer);
	switch (value.e) {
	case "wt":
		// Character travels in the world
		var worldPlayer = worldPlayers[value.characterId];
		worldPlayer.move(value.x, value.y);
		if (characters[value.characterId].isClientPlayer) {
			centerWorldCamera(value.x, value.y);
		}
		handleNextEvent();
		break;
	case "we":
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
		break;
	case "deauth":
		// Deauthorization
		delete characters[value.characterId];
		worldPlayers[value.characterId].remove();
		handleNextEvent();
		break;
	case "chm":
		// Chat message
		if (onGlobalMap) {
//			addMessageToChat(worldPlayers[value.characterId].name, value.text);
			chat.push([characters[value.characterId].name, value.text]);
		} else {
			characters[value.characterId].showSpeech(value.text);
			chat.push([characters[value.characterId].name, value.text]);
//			addMessageToChat(characters[value.characterId].name, value.text);
		}
		UI.notify("chatMessage");
		handleNextEvent();
		break;
	case "move":
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
		break;
	case "putOn":
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
		break;
	case "takeOff":
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
		break;
	case "drop":
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" drops an item "+items[value.typeId][0]+" on global map!");
		} else {
			UI.notify("lootChange");
			UI.notify("inventoryChange");
			handleNextEvent();
		}
		break;
	case "pickUp":
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" picks up an item "+items[value.typeId][0]+" on global map!");
		} else {
			UI.notify("inventoryChange");
			UI.notify("lootChange");
			handleNextEvent();
		}
		break;
	case "openContainer":
		UI.setMode(UI.MODE_CONTAINER);
		Global.container.items.empty();
		for (var i in value.items) {
			Global.container.items.addItem(value.items[i][0], value.items[i][1]);
		}
		UI.notify("containerOpen");
		handleNextEvent();
		break;
	case "takeFromContainer":
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" takes an item "+items[value.typeId][0]+" from container on global map!");
		} else {
			if (value.characterId == player.characterId) {
				Global.container.items.remove(value.typeId, value.paramparam);
				UI.notify("containerChange");
			}
			handleNextEvent();
		}
		break;
	case "putToContainer":
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" puts an item "+items[value.typeId][0]+" to container on global map!");
		} else {
			if (value.characterId == player.characterId) {
				Global.container.items.addItem(value.typeId, value.param);
				UI.notify("containerChange");
			}
			handleNextEvent();
		}
		break;
	case "meleeAttack":
		if (onGlobalMap) {
			throw new Error("Character "+value.attackerId+" attacks on global map!");
		} else {
			characters[value.attackerId].showAttack(value.aimId, false);
		}
		break;
	case "damage":
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" was damaged on global map!");
		} else {
		// handleNextEvent() is inside showDamage
			characters[value.characterId].showDamage(value.amount, value.type);
		}
		break;
	case "death":
		if (onGlobalMap) {
			throw new Error("Character "+value.characterId+" had an attempt to die on global map!");
		} else {
			characters[value.characterId].showDeath();
		}
		break;
	case "itemAppear":
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
		break;
	case "itemDisappear":
		if (onGlobalMap) {
			throw new Error("Item appear on global map!");
		} else {
			Terrain.cells[value.x][value.y].removeItem(value.typeId, value.param);
			if (value.x == player.x && value.y == player.y) {
				UI.notify("lootChange");
			}
		}
		handleNextEvent();
		break;
	case "castSpell":
		if (onGlobalMap) {
			throw new Error("Cast spell on global map!");
		} else {
			characters[value.characterId].showCastSpell(value.spellId, value.x, value.y);
			console["log"](characters[value.characterId].name+" casts spell "+value.spellId+" to "+x+","+y);
			UI.notify("spellCast");
			
			if (value.characterId == player.characterId) {
				player.unselectSpell();
				player.spellAimId=-1;
				player.spellX=-1;
				player.spellY=-1;
			}
//			new effectTypes.confuse(this.x, this.y, this.x, this.y,  1000, 1000, 1000, 1000, function() {
//				handleNextEvent();
//			});
			handleNextEvent();
//			weatherEffect = new effectTypes.rain(
//				this.x, this.y, this.x, this.y, 
//				UI.width/2+100, UI.height/2+100, 
//				UI.width/2+100, UI.height/2+100);			
		}
		break;
	case "missileFlight":
		if (onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else {
			Character.prototype.showMissileFlight(value.fromX, value.fromY, value.toX, value.toY, value.missile);
		}
		break;
	case "loseItem":
		if (onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == player.characterId) {
			player.loseItem(value.typeId, value.param);
		}
		handleNextEvent();
		break;
	case "getItem":
		if (onGlobalMap) {
			throw new Error("Missile flight on global map!");
		} else if (value.characterId == player.characterId) {
			player.getItem(value.typeId, value.param);
		}
		handleNextEvent();
		break;
	case "objectAppear":
		new GameObject(value.x, value.y, value.object).show();
		player.updateVisibility();
		handleNextEvent();
		break;
	case "objectDisappear":
		Terrain.cells[value.x][value.y].object.remove();
		player.updateVisibility();
		handleNextEvent();
		break;	
	case "characterAppear":
		characters[value.characterId] = new Character(value.characterId, value.type, value.x, value.y, value.fraction);
		handleNextEvent();
		break;	
	case "nextTurn":
		if (value.characterId == player.characterId) {
			if (player.actionQueue.length > 0) {
				player.doActionFromQueue();
			}
		}
		// Here must be no handleNextEvent()
		break;	
	case "worldTravel":
		worldPlayers[value.characterId].move(value.x, value.y);
		handleNextEvent();
		break;
	case "useObject":
		handleNextEvent();
		break;
	case "sound":
		showSound(value.x, value.y, value.type);
		break;
	case "soundSourceAppear":
		new SoundSource(value.x, value.y, value.type);
		handleNextEvent();
		break;
	case "soundSourceDisappear":
		Terrain.cells[value.x][value.y].soundSource.remove();
		handleNextEvent();
		break;
	case "dialoguePoint":
		if (value.playerId == player.characterId) {
			UI.notify("dialoguePointRecieve", {phrase:value.phrase, answers:value.answers});
		}
		handleNextEvent();
		break;
	case "dialogueEnd":
		if (value.characterId == player.characterId) {
			UI.notify("dialogueEnd");
		}
		handleNextEvent();
		break;
	case "effectStart":
		characters[value.characterId].showEffectStart(value.effectId);
		handleNextEvent();
		break;
	case "effectEnd":
		characters[value.characterId].showEffectEnd(value.effectId);
		handleNextEvent();
		break;
	case "attrChange":
		characters[value.characterId].changeAttribute(value.attrId, value.value);
		handleNextEvent();
		break;
	default:
		throw new Error("Unknown type of non-synchronized answer: "+value.e);
	}
} 