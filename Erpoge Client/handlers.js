// Функции обработчиков событий элементов
handlers={
	keysActions: {
	/* Registered actions. Now there are universal action that are
	 * registered by default. UI elements save their actions and
	 * contexts there when constructed.
	 */	
		leaveLocation: {
			action: function _() {
				if (onGlobalMap) {
					gAlert("Вы уже на глобальной карте");
					return;
				}
				leaveLocation();
			},
			context: window
		},
		enterLocation: {
			action: function _() {
				if (!onGlobalMap) {
					gAlert("Вы уже в локации");
					return;
				}
				if (!player.isPartyLeader) {
				// Если игрок - не лидер группы (и состоит в группе, то он не может входить в локацию сам
					gAlert("Вы не лидер партии!");
				} else if (onGlobalMap) {
					enterArea();
				}
			},
			context: window
		},
		quickRefresh : {
			action: function _() {
				Net.quickRefresh();
			},
			context: window			
		},
		disableUI: {
			action:function _() {
				UI.disable();
			},
			context: window
		},
		enableUI: {
			action:function _() {
				UI.enable();
			},
			context: window
		},
		toggleUI: {
			action:function _() {
				if (UI.disabled) {
					UI.enable();
				} else {
					UI.disable();
				}
			},
			context: window
		}
	},
	stPlayersPlayer: {
		
	},
	cellWrap: {
		click : function _() {

		}
	},
	loot: {
		
	},
	containerItem: {
		click: function _(e) {
			player.sendTakeFromContainer(
					+this.getAttribute("itemId"), 
					(e.shiftKey ? 1 : +this.getAttribute("itemNum")),
					windowContainer.x,
					windowContainer.y
			);
		}
	},
	spell: {
		
	},
	onlinePlayer: {
		click:function _() {
		// Приглашение кликом по игроку в списке
			var name=this.getAttribute("name");
			if (name==player.name) {
			// Приглашение самого себя
				gAlert("Нельзя пригласить в группу самого себя<span style='display:none;'>, омич ж ты полуёбок!</span>");
				return;
			}
			for (var i in onlinePlayers) {
				for (var j=0;j<onlinePlayers[i].length;j++) {
					if (onlinePlayers[i][j][0]==name) {
						if (i!=0) {
						// Если приглашаемый игрок уже в группе
							gAlert("Этот игрок уже в группе!");
							return false;
						} else if (onlinePlayers[i][j][3]!=player.worldX || onlinePlayers[i][j][4]!=player.worldY) {
						// Если вы и приглашаемый игрок не на одной клетке на глобальной карте
							gAlert("Вы должны быть на одной клетке с приглашаемым игроком!");
							return false;
						} else {
						// Приглашение
							Net.send({invite:name},function(data) {
								if (data==0) {
								// Игрок уже в группе
									gAlert("Игрок уже в группе");
								} else if (data==1) {
								// Пригласить
									gAlert("Приглашение отправлено");
								} else if (data==2) {
								// Игрока сейчас кто-то приглашает
									gAlert("Игрока уже кто-то приглашает, подождите");
								}
							});
						}
						break;
					}
				}
			}
		}
	},
	input: {
		focus:function _() {
			keysMode=5;
		},
		blur:function _() {
			useNormalKeysMode();
		},
		contextmenu:function _(e){
			return false;
		}
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
			var x=Math.floor((e.clientX-elementCoord.left)/32);
			var y=Math.floor((e.clientY-elementCoord.top)/32);
			if (onGlobalMap) {
			// На глобальной карте
				if (e.shiftKey || inMenu) {
					centerWorldCamera(x,y);
				} else {
					if (player.isPartyLeader) {
						worldTravel(x,y);
					} else {
						gAlert("Когда вы в группе, только лидер группы может перемещать группу по карте");
					}
				}
			} else {
			// На карте области
				var shiftKey=e.shiftKey;
				if (keysMode == 6) {
					player.cellChooseAction();
				} else {
					playerClick(x, y, shiftKey);
				}
			}
		},
		mousemove:function _(e) {
		// Передвижение указателя клетки
			if (e.clientX==prevClientX && e.clientY==prevClientY) {
				return;
			}
			prevClientX=e.clientX;
			prevClientY=e.clientY;
			var elementCoord=getOffsetRect(gameField);
			var x=Math.floor((e.clientX-elementCoord.left)/32);
			var y=Math.floor((e.clientY-elementCoord.top)/32);
			if (x==mapCursorX && y==mapCursorY) {
				return;
			}
			// Распрозрачивание горизонтального ряда объектов, закрывающих обзор
			for (var dy=1; !getObject(x,y+dy-1) && matrix[x][y+dy]; dy++) {
				if (hiddenBotheringObjects.indexOf(getNum(x,y+dy))==-1) {
				// Если при движении мыши скрывается один и тот же ряд объектов, то ничего не делать, 
				// иначе распрозрачить предыдущие запрозраченные объекты
					var obj;
					for (var i=0;obj=getObject(
						getX(hiddenBotheringObjects[i]),
						getY(hiddenBotheringObjects[i])
					);i++) {
						if (obj.image) {
						// Если объект не скрылся от того, что персонаж двигается, и вместе с ним двигается камера
							if (!player.canSee(obj.x,obj.y)) {
								obj.shade();
							} else {
								obj.unshade();
							}
						}
					}
					hiddenBotheringObjects=[];
				}
			}
			mapCursorX=x;
			mapCursorY=y;
			if (keysMode==6) {
				cellCursorSec.move(x,y);
			} else {
				cellCursorPri.move(x,y);
				this.cellInfo.style.display="none";
				// var nCurrentCell=document.getElementById("cellCursorPri");
				// nCurrentCell.style.top=y*32+"px";
				// nCurrentCell.style.left=x*32+"px";
				// if (onGlobalMap || player.seenCells[x][y]) {
					// nCurrentCell.style.borderColor="#ff0";
				// } else {
					// nCurrentCell.style.borderColor="#f00";
				// }
			}
			// Запрозрачивание горизонтального ряда объектов, закрывающих обзор
			var objUnderCursor=getObject(x,y);
			for (var dy=1;!getObject(x,y+dy-1) && matrix[x][y+dy];dy++) {
				if (hiddenBotheringObjects.indexOf(getNum(x,y+dy))==-1 && player.seenCells[x][y] && (!objUnderCursor || objectProperties[objUnderCursor.type][2]==1)) {
				// Если при движении мыши скрывается один и тот же ряд объектов, то ничего не делать, 
				// иначе запрозрачить предыдущие распрозраченные объекты
					var objectLower=getObject(x,y+dy);
					if (objectLower && objectProperties[objectLower.type][1]>dy*32) {
					// Если есть мешающий объект и он закрывает своей высотой обзор
						// var obj=matrix[mapCursorX][mapCursorY+1].wall || matrix[mapCursorX][mapCursorY+1].object;
						// obj.hide();
						// obj.show();
						// if (!player.canSee(mapCursorX,mapCursorY+1)) {
							// obj.shade();
						// }
						// От мешающего объекта идём влево. Если объект слева тоже мешающий, то продолжаем, иначе останавливаемся
						var leftestObject=objectLower;
						var i=x;
						while ((leftestObject=getObject(--i,y+dy)) &&  leftestObject.image && objectProperties[leftestObject.type][1]>32 && !objectProperties[leftestObject.type][2] && player.seenCells[i][y] && (!getObject(i,y) || objectProperties[getObject(i,y).type][2]==1)) { 
						// Получаем объект слева так же, как и objectLower
						}
						// Теперь в i мы получили x-координату левейшего мешающего объекта.
						// Идём от этого объекта направо и скрываем все мешающие объекты
						var obj;
						while ((obj=getObject(++i,y+dy)) &&  obj.image && objectProperties[obj.type][1]>32 && !objectProperties[obj.type][2] && player.seenCells[i][y] && (!getObject(i,y) || objectProperties[getObject(i,y).type][2]==1)) {
							try {
								obj.cursorShade();
								// obj.image.style.opacity="0.3";
							} catch (e) {
								
							}
							hiddenBotheringObjects.push(getNum(i,y+dy));
						}
					}
				}
			}
		},
		contextmenu:function _(e) {
			if (e.shiftKey || !onGlobalMap) {
				return;
			}
			var elementCoord=getOffsetRect(gameField);
			var x=Math.floor((e.clientX-elementCoord.left)/32);
			var y=Math.floor((e.clientY-elementCoord.top)/32);
			
			mapCursorX=x;
			mapCursorY=y;
			
			// Получаем информацию о локации
			var cellData=[];
			cellData.push((matrix[x][y].floor.type==1) ? "Трава" : (matrix[x][y].floor.type==3) ? "Снег" : "Какая-то поверхность");
			if (matrix[x][y].forest) {
				cellData.push("Лес");
			}
			if (matrix[x][y].path) {
				cellData.push((matrix[x][y].path.type==21) ? "Река" : "Дорога");
			}
			if (matrix[x][y].object) {
				cellData.push(worldObjectProperties[matrix[x][y].object.type][3]);
			} else if (matrix[x][y].object) {
				cellData.push(worldObjectProperties[matrix[x][y].object.type][3]);
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
	stGoToChooseServerScreen: {
		click: function _() {
			showStChooseServer();
		}
	},
	stGoToConnectionScreen: {
		click: function _() {
			showStConnection();
		}
	},
	stServerRegisger: {
		click: function _() {
			
		}
	},
	stNewPlayerComplete: {
		click: function _() {
			var obj = {
					a:		Net.CREATE_CHARACTER,
					name: 	document.getElementById("stNewPlayerName").value,
					login: 	playerLogin,
					race:	playerCreation.race,
					cls:	playerCreation.cls,
					skills:	(function() {
						var a = [];
						for (var i in playerCreation.skills) {
							a.push(i);
							a.push(playerCreation.skills[i]);
						}
						return a;
					})()
				};
			return;
			Net.send(obj,function(data) {
				var nError=document.getElementById("stNewPlayerError");
				if (data===0) {
					winkElement(nError,"Игрок с таким именем уже существует!");
				} else if (data===1) {
					winkElement(nError,"Введите имя персонажа");
				} else if (data===2) {
					winkElement(nError,"Выберите расу персонажа");
				} else if (data===3) {
					winkElement(nError,"Выберите хотя бы один навык");
				} else if (data===4) {
					winkElement(nError,"Выберите класс персонажа");
				} else if (data===5) {
					winkElement(nError,"Имя должно состоять только из русских или только из английских букв, содержать первую заглавную и остальные строчные буквы и быть длиннее одного символа");
				} else {
				// Эмуляция ввода в форму логина и пароля и их отправки
					addLoginPlayerDescription([
						0,
						document.getElementById("stNewPlayerName").value,
						playerCreation.race,
						playerCreation.cls,
						1
					]);
					windowAccountCharacters.show();
				}
			});
		}
	},
	stAccountInputs: {
		focus: function _() {
			document.getElemenyById("stAccountAlert").innerHTML="";
		}
	},
	stAccountForm: {
		submit: function _() {
			var login=document.getElementById("stAccountLogin").value;
			var password=document.getElementById("stAccountPassword").value;
			var passwordConfirm=document.getElementById("stAccountPasswordConfirm").value;
			if (password!==passwordConfirm) {
			// Проверка пароля
				winkElement(document.getElementById("stAccountAlert"),"Пароль и подтверждение пароля не совпадают");
				return false;
			}
			Net.send({cra:[login,password]},function(data) {
				var nError=document.getElementById("stAccountAlert");
				if (data===1) {
					winkElement(nError,"Поля не могут быть пустыми");
				} else if (data===2) {
					winkElement(nError, "Логин может содержать только английские или только русские буквы, а также цифры, подчёркивания и дефисы");
				} else if (data===3) {
					winkElement(nError,"Аккаунт с таким именем уже существует");
				} else {
					document.getElementById("stServerLogin").value=login;
					document.getElementById("stServerPassword").value=password;
					document.getElementById("stLoginForm").onsubmit();
				}
			});
			return false;
		}
	},
	stGoToCreatePlayer: {
		click: function _() {
			
		}
	},
	stNewPlayerRace: {
		click: function _() {
		// Выбор расы игрока
			playerCreation.race = +this.getAttribute("race");
			// Ресет стиля всех дивов с расами на дефолтный
			var nlRaces=document
				.getElementById("stNewPlayerRaces")
				.children;
			for (var i=0;i<nlRaces.length;i++) {
				nlRaces[i].className="";
			}
			this.className="stNewPlayerSelectedRace";
			// Отображение атрибутов для этой расы
			var nlAttributes=document
				.getElementById("stNewPlayerAttributes")
				.getElementsByClassName("stNewPlayerAttributeValue");
			for (var i=0;i<nlAttributes.length;i++) {
				nlAttributes[i].innerHTML=racialAttributes[playerCreation.race][i];
			}
		}
	},
	stNewPlayerSkill: {
		click: function _() {
		// Изучить навык
			var skill = +this.getAttribute("skill");
			var usedSkillPoints = getUsedSkillPoints()+baseSkillPoints+(newPlayerLearnedSkills[skill] ? newPlayerLearnedSkills[skill]*deltaSkillPoints : 0);
			if (usedSkillPoints <= maxSkillPoints) {
				addLearnedSkill(skill);
				showClasses();
				showSkillPointsLeft();
			}
		}
	},
	stNewPlayerClass: {
		click: function _() {
			var nlClasses=document.getElementById("stNewPlayerClasses").getElementsByTagName("*");
			for (var i=0;i<nlClasses.length;i++) {
				nlClasses[i].className="stNewPlayerClass";
			}
			this.className="stNewPlayerSelectedClass";
			playerCreation.cls = this.innerHTML;
			
		}
	},
	stNewPlayerLearnedSkill: {
		click: function _() {
			removeLearnedSkill(this.getAttribute("skill"));
			showClasses();
			showSkillPointsLeft();
		}
	},
	
	intfLeaveLocation: {
		click: function _() {
			
		}
	},
	
	// Ajax
	stChooseServerForm: {
		submit: function _() {
			serverAddress=document.getElementById("stChooseServer").value;
			// Добавляем сервер в список серверов
			var i=0;
			for (i=0;i<servers.length;i++) {
				if (servers[i][0]==serverAddress) {
					break;
				}
			}
			if (i!=servers.length) {
				servers.push([serverAddress]);
			}
			localStorage.setItem("serverAddress",serverAddress);
			Net.send({a:Net.SERVER_INFO}, handlers.net.serverInfo);
			return false;
		}
	},
	stLoginForm: {
		
	},
	net: {
		serverInfo: function _(data) {
			// in: {serverName:string, online:integer]
			
		},
		login: function _(data) {
			/* 	in: {
					l: String login,
					p: String password,
				}
				out: {players:[{characterId, name, race, class, level, ammunition}xN]}
			*/
			if (data.error !== undefined) {
			// If there is an error
				if (data===0) {
					winkElement(nLoginError,"Сервер переполнен");
				} else if (data==1) {
					winkElement(nLoginError,"Пустой логин!");
				} else if (data==2) {
					winkElement(nLoginError,"Пустой пароль!");
				} else if (data==3) {
					winkElement(nLoginError,"Введён неверный пароль или такого аккаунта не существует");
				} else {
					winkElement(nLoginError,"Неизвестная ошибка при заходе в игру");
				}
			} else {
				UI.notify("accountPlayersRecieve", data.players);
				Net.setServerAdressesStorage(serverAddress, playerLogin, playerPassword);
			}
		},
		loadContents : function _(data) {
		// Find out whether player is on global map or in an area and
		// load contents of character's environment after authentification.
		// Server can send two different types of answers: when the player is in location
		// and when he is on the world map. The type is determined by data.onGlobalMap value
		/* on world map: {
		 * 		onGlobalMap: true,
		 *  	w : {w,h,c:[[ground,forest,road,river,race,[objects]]xN]},
		 *  	p : [
		 *  		characterId, name, race, class, level, 
		 *  		maxHp, maxMp, 
		 *  		str, dex, wis, itl, 
		 *  		items, ammunition, spells, skills, 
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
		 *  		items, ammunition, spells, skills,
		 *  		x, y
		 *  	],
		 *  	islead : boolean,
		 *		online : [[characterId,x,y,name,maxHp,hp,maxMp,mp,effects,ammunition(,cls,race)|(,type)]xM],
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
			player={
				name:data.name,
				characterId:data.characterId,
				isPartyLeader:((data.party == 0 || data.party == data.characterId) ? true : false),
				partyId:data.party,
				worldX:data.worldX,
				worldY:data.worldY				
			};	// Создадим неполный объект игрока 
				// (чтобы не создавать новую переменную для сохранения имени 
				// и брать её из естественного источника - объекта player)
			showMenu();
			showStLoad();
			if (data.onGlobalMap) {
			// World loading
				isLocationPeaceful = false;
				onGlobalMap = true;
				prepareArea();
				readWorld(data.w);
				readOnlinePlayers(data.online);
				readWorldPlayer(data.p);
				player.isPartyLeader = data.islead;
				centerWorldCamera(player.worldX,player.worldY,true);
				readChatMessages(data.chat);
				data.inv && readInvite(data.inv);
				readEntering(data.en);
				if (Net.callback) {
					Net.callback();
				}
				fitIntfInfo();
				document.getElementById("intfleaveLocation").style.display="none";
				document.getElementById("intfleaveLocationBg").style.display="none";
				document.getElementById("intfEnterArea").style.display="block";
				document.getElementById("intfEnterAreaBg").style.display="block";
				UI.enterGlobalMapMode();
				UI.notify("worldLoad");
				Keys.setMode(Keys.MODE_ON_GLOBAL_MAP);
			} else {
			// Area loading
				if (characters[1]) {
					characters[1].cellWrap.parentNode.removeChild(characters[1].cellWrap);
					delete characters[1];
				}
				characters = {};
				onGlobalMap=false;
				showStLoad();
				showMenu();
				
				document.getElementById("intfEnterArea").style.display="none";
				document.getElementById("intfEnterAreaBg").style.display="none";
				document.getElementById("intfleaveLocation").style.display="block";
				document.getElementById("intfleaveLocationBg").style.display="block";
				onlinePlayers=[];
				width=data.l.w;
				height=data.l.h;
				isLocationPeaceful = data.l.p;
				areaId=data.l.locationId;
				onlinePlayers=[];
				prepareArea();
				readLocation(data.l);
				createPlayerFromData(data.p);
				
				moveGameField(player.x, player.y, true);
				player.initVisibility();
//				document.getElementById("minimap").style.display="block";
				if (matrix[player.x][player.y].object && matrix[player.x][player.y].object.type==9000) {
				// Событие при появлении в локации
					events[matrix[player.x][player.y].object.mod](matrix[player.x][player.y].object);
				}
				readCharacters(data.online);
//				minimap=new Minimap(document.getElementById("minimap"));
				fitIntfInfo();
				hideMenu();
				UI.enterLocationMode();
				UI.notify("locationLoad");
				Keys.setMode(Keys.MODE_IN_LOCATION);
			}
			fitIntfInfo();
			recountWindowSize();
		},
		loadPassiveContents : function _(data) {
			onGlobalMap = true;
			readWorld(data);
			document.getElementById("intfInfo").style.display = "none";
			document.getElementById("intfActions").style.display = "none";
			
			fitIntfInfo();
			recountWindowSize();
			
			centerWorldCamera(width/2, height/2, true);
			hideMenu();
			inMenu = true;
		},
		authComplete : function _() {
		// Tell the server that content loading after authentification is complete
		// And that server may now send events to this client
			
		}
	}
};
