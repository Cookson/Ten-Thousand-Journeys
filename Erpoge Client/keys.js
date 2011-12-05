/* keys.js: default control settings
 * 
 */
onLoadEvents['keys'] = function _() {
	Keys.assign(
			"leaveLocation", 
			"Shift", 
			",", 
			UI.MODE_IN_LOCATION);
	Keys.assign(
			"enterLocation", 
			"Shift", 
			".", 
			UI.MODE_ON_GLOBAL_MAP);
	Keys.assign(
			"quickRefresh", 
			"R", 
			UI.MODE_ALWAYS);
	Keys.assign(
			"focusOnChat", 
			"T", 
			UI.MODE_ALWAYS);
	Keys.assign(
			"sendToChat", 
			"Enter", 
			UI.MODE_CHAT);
	Keys.assign(
			"closeChat", 
			"Esc", 
			UI.MODE_CHAT);
	Keys.assign(
			"toggleUI", 
			"Ctrl", 
			"F1", 
			UI.MODE_ALWAYS);
	Keys.assign(
			"toggleSkillsWindow", 
			"N",
			UI.MODE_ALWAYS);
	Keys.assign(
			"toggleSettingsWindow", 
			"F4",
			UI.MODE_ALWAYS);
	Keys.assign(
			"unselectSpell", 
			"Esc",
			UI.MODE_CURSOR_ACTION);
	Keys.assign(
			"selectMissile", 
			"F",
			UI.MODE_ALWAYS);
	Keys.assign(
			"shootMissile", 
			"Space",
			UI.MODE_CURSOR_ACTION);
	Keys.assign(
			"closeContainer", 
			"Esc",
			UI.MODE_CONTAINER);
	Keys.assign(
			"takeAllFromContainer", 
			"A",
			UI.MODE_CONTAINER);
};
//leaveParty: function _() {
//	if (player.partyId==0) {
//		gAlert("Вы не состоите в группе");
//		return;
//	}
//	Net.send({leave:player.partyId}, function(data) {
//		player.isPartyLeader=true;
//		readOnlinePlayers(data);
//	});
//},
//inviteAgree: function _() {
//	Net.send({agree:inviterPlayerId},function(data) {
//		readOnlinePlayers(data);
//		document.getElementById("infoInvite").style.visibility="hidden";
//		player.isPartyLeader=false;
//	});
//},
//inviteDisagree: function _() {
//	Net.send({refuse:1},function(data) {
//		document.getElementById("infoInvite").style.visibility="hidden";
//	});
//},
//alertOk: function _() {

//},
//:function(e) { // Все события нажатия клавиш
//	if (keyPressForbidden || inMenu) {
//		return;
//	}
//	var key=e.keyCode;
//	if (keysMode==0) {
//	// Обычный режим работы клавиш
//		var shiftKey=e.shiftKey;
//		if (key==83) { /* S */
//			if (!onGlobalMap) {
//				if (canAct) {
//					player.idles=true;
//					player.action();
//				} else {
//					player.destX=player.x;
//					player.destY=player.y;
//					CellCursor.prototype.useCursor("cellCursorPri");
//				}
//			}
//		}
//		if (key==53) { /* 5 */
//			if (!onGlobalMap) {
//				if (canAct) {
//					player.restorationIdles=true;
//					player.action();
//				} else {
//					player.destX=player.x;
//					player.destY=player.y;
//				}
//			}
//		}
//		if (key==72 && shiftKey) { /* Shift + H */ // Помошь
//			window.open("./static/help.html");
//		}
//		if (key==112) { /* F1 */ // Помошь
//			
//		}
//		if (key==113) { /* F2 */ // Меню
//			if (!serverAddress || inMenu) {
//			// Если ещё даже не выбран сервер, то нельзя переключаться по меню
//				return false;
//			}
//			windowLogin.show();
//		}
//		if (key==115) { /* F4 */ // Настройки
//			if (!Net.serverAddress || inMenu) {
//			// Если ещё даже не выбран сервер, то нельзя переключаться по меню
//				return false;
//			}
//			if (windowSettings.visible) {
//				windowSettings.hide();
//			} else {
//				windowSettings.show();
//			}
//		}
//		if (key==32) { /* Space */
//			var e=document.createEvent("MouseEvent");
//			var button=document.getElementById("intfAlertOk");
//			var rect = getOffsetRect(button);
//			e.initMouseEvent("click",true,true,window,1,0,0,rect.left,rect.top,false,false,false,false,0,null);
//			// evObj.initMouseEvent( 'click', true, true, window, 1, 12, 345, 7, 220, false, false, true, false, 0, null );
//			// initMouseEvent( 'type', bubbles, cancelable, windowObject, detail, screenX, screenY, clientX, clientY, ctrlKey, altKey, shiftKey, metaKey, button, relatedTarget )
//			button.dispatchEvent(e);
//		}
//		if (key==70) { /* F */
//		// Войти в режим курсора выбора клетки
//			if (isRanged(player.ammunition.getItemInSlot(0).typeId)) {
//				CellCursor.prototype.useCursor("cellCursorSec");
//				var aimcharacter;
//				if (aimcharacter=player.findEnemy()) {
//					cellCursorSec.move(aimcharacter.x,aimcharacter.y);
//				} else {
//					cellCursorSec.move(player.x,player.y);
//				}
//				keysMode=6;
//			} else {
//				gAlert("Игрок не держит в руках оружия дальнего боя!");
//			}
//		}
//		if (key==73) { /* I */
//		// Войти в режим выбора предметов из рюкзака
//			if (!keysMode) {
//				showRightPanelKeys(1);
//			} else {
//				useNormalKeysMode();
//			}
//		}
//		if (key==90) { /* Z */
//		// Войти в режим выбора заклинания
//			stoppedByKey=!stoppedByKey;
//			if (!canAct) {
//				return false;
//			}
//			if (!keysMode) {
//				showRightPanelKeys(3);
//			} else {
//				useNormalKeysMode();
//			}
//		}
//		if (key==79) { /* O */
//		// Войти в режим выбора снимаемой амуниции
//			if (!keysMode) {
//				showRightPanelKeys(2);
//			} else {
//				useNormalKeysMode();
//			}
//		}
//		if (key==76) { /* L */
//		// Войти в режим выбора предмета но полу
//			if (!keysMode) {
//				showRightPanelKeys(4);
//			} else {
//				useNormalKeysMode();
//			}
//		}
//		if (key==37 || key==38 || key==39 || key==40) { // Стрелки
//			if (inMenu || !canAct) {
//				return;
//			}
//			var dKey=pressedArrow-key;
//			var destX=player.x;
//			var destY=player.y;
//			(pressedArrow==37)?destX-=1:1;
//			(pressedArrow==38)?destY-=1:1;
//			(pressedArrow==39)?destX+=1:1;
//			(pressedArrow==40)?destY+=1:1;
//			(key==37)?destX-=1:1;
//			(key==38)?destY-=1:1;
//			(key==39)?destX+=1:1;
//			(key==40)?destY+=1:1;
//			// Следующее основывается на том, что если нажать две клавиши по диагонали, то игрок пойдёт по диагонали
//			if (arrowPressTimer) {
//			// Если одна из двух клавиш уже нажата
//				clearTimeout(arrowPressTimer);
//				arrowPressTimer=undefined;
//				pressedArrow=0;
//				playerClick(destX,destY);
//			} else {
//			// Если не нажато ни одной из двух клавиш
//				pressedArrow=key;
//				if (destX==player.x && destY==player.y) {
//				// Если нажаты стрелки в противоположные стороны - ничего не делать
//					arrowPressTimer=undefined;
//					pressedArrow=0;
//				}
//				arrowPressTimer=setTimeout(function () {
//					playerClick(destX,destY);
//					arrowPressTimer=undefined;
//					pressedArrow=0;
//				},80);
//			}
//			cellCursorPri.move(-1,-1);	// Передвигаем курсор за границу, чтобы его не было видно, когда игрок ходит стрелками
//										// Просто CellCursor.hide() в этом случае недостаточно, так как в character.move() курсор показывается в конце движения
//		}
//		if (key==88) { /* X */
//		
//			
//		}
//		if (key==82) { /* R */
//			if (inMenu) {
//				return;
//			}
//			quickRefresh();
//		}
//		if (key==65) { /* A */
//			// if (inMenu) {
//				// return;
//			// }
//			// if (document.getElementById("intfActions").style.display=="none") {
//				// document.getElementById("intfActions").style.display="inline-block";
//			// } else {
//				// document.getElementById("intfActions").style.display="none";
//			// }
//		}
//		if (key==84) { /* T */
//			document.getElementById("chatInput").focus();
//			return false;
//		}
//		if (key==13) { // Enter
//		
//		}
//		if (key==78) { /* N */
//			if (inMenu) {
//				return;
//			}
//			if (windowSkills.visible) {
//				windowSkills.hide();
//			} else {
//				windowSkills.show();
//			}
//		}
////		if (key==66) { /* B */
////			benchmark.test();
////		}
//	} 
//	else if (keysMode==1) { // Рюкзак
//	// Быстрые клавиши предметов
//		if (key==27) {
//			useNormalKeysMode();
//			return;
//		}
//		var e=document.createEvent("MouseEvent");
//		var len=keyChars.length;
//		var pos=0;
//		for (;pos<len;pos++) {
//			if (keyChars.charCodeAt(pos)==key+32) {
//			// Char code "a"=97, key code "a"=65, 97-65=32
//				break;
//			}
//		}
//		var elem=document.getElementById("invItems").children[pos];
//		if (!elem) {
//			return;
//		}
//		var rect=getOffsetRect(elem);
//		e.initMouseEvent("click",true,true,window,1,0,0,rect.left,rect.top,false,false,false,false,0,null);
//		elem.dispatchEvent(e);
//		useNormalKeysMode();
//	} 
//	else if (keysMode==2) { // Амуниция
//	// Быстрые клавиши выбора снятия амуниции
//		if (key==27) {
//			useNormalKeysMode();
//			return;
//		}
//		var e=document.createEvent("MouseEvent");
//		var len=keyChars.length;
//		var pos=0;
//		for (;pos<len;pos++) {
//			if (keyChars.charCodeAt(pos)==key+32) {
//			// Char code "a"=97, key code "a"=65, 97-65=32
//				break;
//			}
//		}
//		var elem=document.getElementById("invAmmunition").children[pos];
//		if (!elem) {
//			return;
//		}
//		elem=elem.children[1];
//		var rect=getOffsetRect(elem);
//		e.initMouseEvent("click",true,true,window,1,0,0,rect.left+17,rect.top+17,false,false,false,false,0,null);
//		elem.dispatchEvent(e);
//		useNormalKeysMode();
//	} 
//	else if (keysMode==3) { // Заклинания
//	// Быстрые клавиши заклинаний
//		if (key==27) {
//			useNormalKeysMode();
//			return;
//		}
//		var e=document.createEvent("MouseEvent");
//		var len=spellsKeyChars.length;
//		var pos=0;
//		for (;pos<len;pos++) {
//			if (spellsKeyChars.charCodeAt(pos)==key+32) {
//			// Char code "a"=97, key code "a"=65, 97-65=32
//				break;
//			}
//		}
//		var elem=document.getElementById("spellsList").children[pos];
//		if (!elem) {
//			return;
//		}
//		useNormalKeysMode();
//		elem=elem.children[0]; // Выбрать элемент img
//		var rect=getOffsetRect(elem);
//		e.initMouseEvent("click",true,true,window,1,0,0,rect.left,rect.top,false,false,false,false,0,null);
//		elem.dispatchEvent(e);
//	} 
//	else if (keysMode==4) { // Лут
//	// Быстрые клавиши подбора лута
//		if (key==27) {
//			useNormalKeysMode();
//			return;
//		}
//		var e=document.createEvent("MouseEvent");
//		var len=keyChars.length;
//		var pos=0;
//		for (;pos<len;pos++) {
//			if (keyChars.charCodeAt(pos)==key+32) {
//			// Char code "a"=97, key code "a"=65, 97-65=32
//				break;
//			}
//		}
//		var elem=document.getElementById("invLoot").children[pos];
//		if (!elem) {
//			return;
//		}
//		var rect=getOffsetRect(elem);
//		e.initMouseEvent("click",true,true,window,1,0,0,rect.left,rect.top,false,false,false,false,0,null);
//		elem.dispatchEvent(e);
//		useNormalKeysMode();
//	}
//	else if (keysMode==5) { // Чат
//	// Режим работы клавиш со включенным чатом
//		if (key==27) {
//			if (isChatOpen()) {
//				// handlers.chatInput.blur();
//				document.getElementById("chatInput").blur();
//			}
//		}
//	}
//	else if (keysMode==6) { // Выбор клетки, дальняя атака
//	// Режим выбора клетки (для дальней атаки, заклинания etc)
//		if (key==27) {
//		// Выход из режима
//			CellCursor.prototype.useCursor("cellCursorPri");
//			keysMode=0;
//			return;
//		}
//		if (key==70 || key==32) {
//		// F или Space
//			player.cellChooseAction();
//		}
//		if (key==37) { // Стрелки
//			if (cellCursorSec.x!=0) {
//				cellCursorSec.move(cellCursorSec.x-1, cellCursorSec.y);
//			}
//		} else if (key==38) {
//			if (cellCursorSec.y!=0) {
//				cellCursorSec.move(cellCursorSec.x,cellCursorSec.y-1);
//			}
//		} else if (key==39) {
//			if (cellCursorSec.x!=width-1) {
//				cellCursorSec.move(cellCursorSec.x+1,cellCursorSec.y);
//			}
//		} else if (key==40) {
//			if (cellCursorSec.y!=height-1) {
//				cellCursorSec.move(cellCursorSec.x, cellCursorSec.y+1);
//			}
//		}
//	}
//},