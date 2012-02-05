// Функции обработчиков событий элементов
handlers={
	keysActions: {
	/* Registered actions. Now there are universal action that are
	 * registered by default. UI elements save their actions and
	 * contexts there when constructed.
	 */
		
	},
	document: {
		keydown: Keys.universalKeyDownHandler,
		contextmenu: function _() {
			return false;
		}
	},
	gameField: {
		click:function _(e) {
			var elementCoord=getOffsetRect(gameField);
			var xPx = Math.floor((e.clientX-elementCoord.left)/32);
			var yPx = Math.floor((e.clientY-elementCoord.top)/32);
			var normal = Terrain.getNormalView(xPx, yPx);
			var x=normal.x;
			var y=normal.y;
			UI.gameFieldClick(x,y,e);
		},
		mousemove:function _(e) {
		// Передвижение указателя клетки
			if (e.clientX==prevClientX && e.clientY==prevClientY) {
				return;
			}
			prevClientX=e.clientX;
			prevClientY=e.clientY;
			var elementCoord=getOffsetRect(gameField);
			var normal = Terrain.getNormalView(
					Math.floor((e.clientX-elementCoord.left)/32),
					Math.floor((e.clientY-elementCoord.top)/32));
			var x=normal.x;
			var y=normal.y;
			if (x==mapCursorX && y==mapCursorY) {
				return;
			}
			// Hide ceiling, if there is one.
//			if (Terrain.getCell(x,y).ceiling !== null) {
//				Terrain.getCell(x,y).ceiling.parent.hide();
//			} else if (Terrain.getCell(mapCursorX,mapCursorY).ceiling !== null) {
//				Terrain.getCell(mapCursorX,mapCursorY).ceiling.parent.show();
//			} 
			// Распрозрачивание горизонтального ряда объектов, закрывающих обзор
//			for (var dy=1; !getObject(x,y+dy-1) && Terrain.cells[x][y+dy]; dy++) {
//				if (hiddenBotheringObjects.indexOf(getNum(x,y+dy))==-1) {
//				// Если при движении мыши скрывается один и тот же ряд объектов, то ничего не делать, 
//				// иначе распрозрачить предыдущие запрозраченные объекты
//					var obj;
//					for (var i=0;obj=getObject(
//						getX(hiddenBotheringObjects[i]),
//						getY(hiddenBotheringObjects[i])
//					);i++) {
//						if (obj.image) {
//						// Если объект не скрылся от того, что персонаж двигается, и вместе с ним двигается камера
//							if (!player.canSee(obj.x,obj.y)) {
//								obj.shade();
//							} else {
//								obj.unshade();
//							}
//						}
//					}
//					hiddenBotheringObjects=[];
//				}
//			}
			mapCursorX=x;
			mapCursorY=y;
			CellCursor.move(x,y);
//			if (Player.spellId != -1) {
//				cellCursorSec.move(x,y);
//			} else {
//				cellCursorPri.move(x,y);
////				this.cellInfo.style.display="none";
//				// var nCurrentCell=document.getElementById("cellCursorPri");
//				// nCurrentCell.style.top=y*32+"px";
//				// nCurrentCell.style.left=x*32+"px";
//				// if (Terrain.onGlobalMap || Player.seenCells[x][y]) {
//					// nCurrentCell.style.borderColor="#ff0";
//				// } else {
//					// nCurrentCell.style.borderColor="#f00";
//				// }
//			}
			// Запрозрачивание горизонтального ряда объектов, закрывающих обзор
//			var objUnderCursor=getObject(x,y);
//			for (var dy=1;!getObject(x,y+dy-1) && Terrain.cells[x][y+dy];dy++) {
//				if (hiddenBotheringObjects.indexOf(getNum(x,y+dy))==-1 && player.seenCells[x][y] && (!objUnderCursor || objectProperties[objUnderCursor.type][2]==1)) {
//				// Если при движении мыши скрывается один и тот же ряд объектов, то ничего не делать, 
//				// иначе запрозрачить предыдущие распрозраченные объекты
//					var objectLower=getObject(x,y+dy);
//					if (objectLower && objectProperties[objectLower.type][1]>dy*32) {
//					// Если есть мешающий объект и он закрывает своей высотой обзор
//						// var obj=Terrain.cells[mapCursorX][mapCursorY+1].wall || Terrain.cells[mapCursorX][mapCursorY+1].object;
//						// obj.hide();
//						// obj.show();
//						// if (!Player.canSee(mapCursorX,mapCursorY+1)) {
//							// obj.shade();
//						// }
//						// От мешающего объекта идём влево. Если объект слева тоже мешающий, то продолжаем, иначе останавливаемся
//						var leftestObject=objectLower;
//						var i=x;
//						while ((leftestObject=getObject(--i,y+dy)) &&  leftestObject.image && objectProperties[leftestObject.type][1]>32 && !objectProperties[leftestObject.type][2] && Player.seenCells[i][y] && (!getObject(i,y) || objectProperties[getObject(i,y).type][2]==1)) { 
//						// Получаем объект слева так же, как и objectLower
//						}
//						// Теперь в i мы получили x-координату левейшего мешающего объекта.
//						// Идём от этого объекта направо и скрываем все мешающие объекты
//						var obj;
//						while ((obj=getObject(++i,y+dy)) &&  obj.image && objectProperties[obj.type][1]>32 && !objectProperties[obj.type][2] && Player.seenCells[i][y] && (!getObject(i,y) || objectProperties[getObject(i,y).type][2]==1)) {
//							try {
//								obj.cursorShade();
//								// obj.image.style.opacity="0.3";
//							} catch (e) {
//								
//							}
//							hiddenBotheringObjects.push(getNum(i,y+dy));
//						}
//					}
//				}
//			}
		},
		contextmenu:function _(e) {
			if (e.shiftKey || !Terrain.onGlobalMap) {
				return;
			}
			var elementCoord=getOffsetRect(gameField);
			var x=Math.floor((e.clientX-elementCoord.left)/32);
			var y=Math.floor((e.clientY-elementCoord.top)/32);
			
			mapCursorX=x;
			mapCursorY=y;
			
			// Получаем информацию о локации
			var cellData=[];
			cellData.push((Terrain.cells[x][y].floor.type==1) ? "Трава" : (Terrain.cells[x][y].floor.type==3) ? "Снег" : "Какая-то поверхность");
			if (Terrain.cells[x][y].forest) {
				cellData.push("Лес");
			}
			if (Terrain.cells[x][y].path) {
				cellData.push((Terrain.cells[x][y].path.type==21) ? "Река" : "Дорога");
			}
			if (Terrain.cells[x][y].object) {
				cellData.push(worldObjectProperties[Terrain.cells[x][y].object.type][3]);
			} else if (Terrain.cells[x][y].object) {
				cellData.push(worldObjectProperties[Terrain.cells[x][y].object.type][3]);
			}
			var playersArrayPos=cellData.length; // Позиция массива с игроками
			
			for (var i=0,numOfGroups=onlinePlayers.length;i<numOfGroups;i++) {
				for (var j=0,numOfPlayersInGroup=onlinePlayers[i].length;j<numOfPlayersInGroup;j++) {
					if (onlinePlayers[i][j][3]==x && onlinePlayers[i][j][4]==y) {
						if (!cellData[playersArrayPos]) {
							cellData.push([]);
						}
						cellData[playersArrayPos].push(onlinePlayers[i][j][1]+" "+onlinePlayers[i][j][0]);
					}
				}
			}
			if (cellData[playersArrayPos]) {
				cellData[playersArrayPos]=cellData[playersArrayPos].join(",<br />");
			}
			itemInfo=x+":"+y+"<br />"+cellData.join(",<br />");
			// Выводим информацию о предмете
			this.cellInfo.style.display="block";
			this.cellInfo.innerHTML=itemInfo;
			this.cellInfo.style.top=(e.clientY-this.cellInfo.clientHeight-15)+"px";
			this.cellInfo.style.left=(e.clientX-this.cellInfo.offsetWidth/2)+"px";
			return false;
		},
		mouseout: function _(e) {
			// document.getElementById("cellCursorPri").style.display="none";
		},
		mouseover: function _() {
			// document.getElementById("cellCursorPri").style.display="inline-block";
		}
	},
	globalMapClickHandler: function _(x,y,e) {
		if (e.shiftKey) {
			centerWorldCamera(x,y);
		} else {
			if (Player.isPartyLeader) {
				performAction("worldTravel", [x,y]);
			} else {
				UI.notify("alert", "Когда вы в группе, только лидер группы может перемещать группу по карте")
			}
		}
	},
	cellInfo: {
		mouseover:function _() {
			this.style.display="none";
		}
	},
	speechBubble: {
		click: function _(e) {
			gameField.removeChild(this);
			event.stopPropagation();
			return false;
		},
		mouseover: function _(e) {
			this.setAttribute("isMouseOver", "1");
		},
		mouseout: function _(e) {
			this.setAttribute("isMouseOver", "0");
			if (this.getAttribute("time") < new Date().getTime()-2000) {
				gameField.removeChild(this);
			}
		},
		timeout: function _(e) {
			gameField.removeChild(this);
			event.stopPropagation();
			return false;
		}
	},
	net: {
		
	},
	initWindows: function _() {
	// Windows are initiated before other ui elements, but after panels
		UI.addWindow({
			type: "windowGameAlert",
			hAlign: "left",
			vAlign: "top",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowInfo",
			hAlign: "right",
			vAlign: "bottom",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowLogin",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowAccountCharacters",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowSkills",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowDeath",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowSettings",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowDialogue",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowContainer",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowAccountCreate",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowPlayerCreate",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
	},
	initInterface : function _() {
	// First panels are initiated, then windows,
	// then other elements.
		UI.addPanel({
			name: "main",
			width: 204,
			side: "right"
		});
		handlers.initWindows();
		UI.addElement({
			type:"chat",
			hAlign: "left",
			vAlign: "bottom",
			displayMode: "always",
			panel: null
		});
		UI.addElement({
			type: "hpBar",
			panel: "main"
		});
		UI.addElement({
			type: "mpBar",
			panel: "main"
		});
		UI.addElement({
			type: "epBar",
			panel: "main"
		});
		UI.addElement({
			type: "attributeList",
			panel: "main"
		});
		UI.addElement({
			type: "iconMissileType",
			panel: "main"
		});
		UI.addElement({
			type: "iconsSpells",
			panel: "main"
		});
		UI.addElement({
			type: "iconsEquipment",
			panel: "main"
		});
		UI.addElement({
			type: "iconsInventory",
			panel: "main"
		});
		UI.addElement({
			type: "iconsLoot",
			panel: "main"
		});
		UI.addElement({
			type: "minimap",
			hAlign: "left",
			vAlign: "top",
			displayMode: "in location",
			panel: null
		});
		UI.addElement({
			type: "actionsPanel",
			hAlign: "center",
			vAlign: "bottom",
			displayMode: "in location",
			panel: null
		});
	}
};
