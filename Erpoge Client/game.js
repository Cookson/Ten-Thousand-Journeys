// По загрузке документа выполнить:
window.onload = function _() {
	// Чтобы надпись "Загрузка" в начале была выравнена по вертикали
	recountWindowSize();
	document.getElementById("stLoad").style.color="#fff";
	// Собственно, загрузка
	// Задержка нужна для того, чтобы отобразилась надпись "Загрузка"
	setTimeout(function _() {
		onLoadEvents['customStylesheets']();
		onLoadEvents['game']();
		onLoadEvents['uiWindowPrototype']();
		onLoadEvents['gui']();
		onLoadEvents['storage']();
		onLoadEvents['ajax']();
		onLoadEvents['keys']();
	},1);
};
onLoadEvents['game'] = function _() {
	if (!document.head) {
		document.head=document.getElementsByTagName("head")[0];
	}
	cacheImages();
	saveParticlesImageData();
};
function moveGameField(x,y,initiate) {
// Центрировать камеру на клетке x;y
	var xCells=x;
	var yCells=y;
	x=UI.width/2-x*32;
	x-=(x%32==0)?0:16;
	x=(x<0)?((width-xCells-1)*32<UI.width/2)?UI.width-width*32:x:0;
	if (width*32<UI.width) {
		x=0;
	}
	y=(UI.height-UI.height%32)/2-y*32;
	y-=(y%32==0)?0:16;
	y=(y<0)?((height-yCells-1)*32<UI.height/2)?UI.height-height*32:y:0;
	if (height*32<UI.height) {
		y=0;
	}
	// На этом месте переменные x и y обозначают отступы в пикселях
	if (+localStorage.getItem(1) && !initiate) {
		if (onGlobalMap) {
			var dist=distance(xCells,yCells, player.worldX, player.worldY);
			var time=(dist<2) ? 200 : (dist<10) ? dist*30 : 150;
			qanimate(gameField, [x-parseInt(gameField.style.left),y-parseInt(gameField.style.top)], time, function() {
				// gameField.onmousemove(false);c
			});
		} else {
			qanimate(gameField, [x-parseInt(gameField.style.left),y-parseInt(gameField.style.top)], 90, function() {
				
			});
		}
	} else {
		gameField.style.top=y+"px";
		gameField.style.left=x+"px";
		if (weatherEffect) {
			var wx = player.x;
			var wy = player.y;
			weatherEffect.move(Math.min(Math.max(wx, rendCX), width-rendCX), Math.min(Math.max(wy, rendCY), height-rendCY));
		}
	}
}
function centerWorldCamera(x,y,initiate) {
// Перемещение камеры на глобальной карте
	moveGameField(x,y,initiate);
	rendCX=x;
	rendCY=y;
	worldMapRenderView();
}
function prepareArea(isWorld) {
// Подготавливает область для загрузки в неё данных с сервера
// Если isWorld - загружется мир, иначе - область
	while (gameField.children.length>3) {
	// Удаляем всех детей #gameField, кроме div'ов c gameFieldFloor,
	// cellCursorPri и cellCursorSec
		gameField.removeChild(gameField.children[3]);
	}
	rendCX=-1;
	rendCY=-1;
	prevRendCX=-1;
	prevRendCY=-1;
	keyPressActive=false;
	matrix = blank2dArray();
	vertex = blank2dArray();
	for (var i=0;i<width;i++) { 
	// Установка вершин и создание ячеек
		for (var j=0;j<height;j++) { 
			matrix[i][j] = new MatrixCell(i,j);
			vertex[i][j] = -1;
		}
	}
	// Canvas с полом
	floorCanvas.width=width*32;
	floorCanvas.height=height*32;
	// Игровое поле
	gameField.style.display="inline-block";
	gameField.style.width=(32*width)+"px";
	gameField.style.height=(32*height)+"px";
	
	setTimeout(function() {
		keyPressActive=true;
	}, 500);
	
}
function playerClick(x, y, shiftKey) {
// Функция обработки клика игрока
	if (player.x != player.destX || player.y != player.destY) {
		return;
	}
	var aim;
	if (
		(aim = matrix[x][y].character) &&
		player.isEnemy(aim)
	) {
	// Если игрок атакует
		if (isMelee(player.ammunition.getItemInSlot(0).typeId) && !player.isNear(x,y)) {
			return false;
		}
		player.sendAttack(aim.characterId,!isMelee(player.ammunition.getItemInSlot(0).typeId));
	} else if (player.spellId!=-1) {
	// Если игрок произносит заклинание
		var spell=spells[player.spellId];
		if (!spell.onCell && vertex[x][y] != 3 || spell.onCell && !spell.onOccupiedCell && vertex[x][y]!=-1) {
			player.unselectSpell(player.spellId);
			return false;
		}
		if (spells[player.spellId].onCell && spells[player.spellId].onCharacter) {
			if (spells[player.spellId].onOccupiedCell) {
			
			}
		}
		if (spells[player.spellId].onCharacter && player.aimcharacter.x==x && player.aimcharacter.y==y) {
			player.spellX=-1;
			player.spellY=-1;
			player.spellAimId=player.aimcharacter.characterId;
			
		} else if (spells[player.spellId].onCell) {
			player.spellX=x;
			player.spellY=y;
			player.spellAimId=-1;
			
		}
	} else if ((aim = matrix[x][y].character) && !player.isEnemy(aim)) {
		player.sendStartConversation(aim.characterId);
	} else if (
		matrix[x][y].object && isDoor(matrix[x][y].object.type) && 
		(!isOpenDoor(matrix[x][y].object.type) || shiftKey) && 
		player.isNear(x,y)
	) {
	// Открыть дверь
		player.sendUseObject(x,y);
	} else if (matrix[x][y].object && player.isNear(x,y) && isContainer(matrix[x][y].object.type)) {
	// Открыть контейнер
		windowContainer.x = x;
		windowContainer.y = y;
		player.sendOpenContainer(x,y);
	} else if (vertex[x][y]==1 || vertex[x][y]==3) {
	// Если игрок идёт к объекту или мобу
		player.aimcharacter=-1; // Нужно, если игрок идёт
		player.destX = x;
		player.destY = y;
		player.getPathTable();
		if (player.comeTo(x,y)) {
			player.sendMove();
		}
	} else if (keysMode==6) {
		player.cellChooseAction();
	} else {
	// Если игрок идёт к клетке
		player.destX=x;
		player.destY=y;
		player.sendMove();
	} 
}
function renderView() {
// Прорисовка содержимого ячеек
	if (rendCX-(rendW-1)/2<0) {
	// Чтобы область прорисовки была полностью на игровом поле
	// По оси X
		rendCX=(rendW-1)/2;
	} else if (rendCX+(rendW-1)/2>=width) {
		rendCX=width-1-(rendW-1)/2;
	}
	if (rendCY-(rendH-1)/2<0) {
	// По оси Y
		rendCY=(rendH-1)/2;
	} else if (rendCY+(rendH-1)/2>=height) {
		rendCY=height-1-(rendH-1)/2;
	}
	if (prevRendCX==-1) {
		var startX = rendCX-(rendW-1)/2;
		var startY = rendCY-(rendH-1)/2;
		var maxX = rendCX+(rendW-1)/2;
		var maxY = rendCY+(rendH-1)/2;
	} else {
		var startX=Math.min(rendCX-(rendW-1)/2, prevRendCX-(rendW-1)/2);
		var startY=Math.min(rendCY-(rendH-1)/2, prevRendCY-(rendH-1)/2);
		var maxX=Math.max(rendCX+(rendW-1)/2, prevRendCX+(rendW-1)/2);
		var maxY=Math.max(rendCY+(rendH-1)/2, prevRendCY+(rendH-1)/2);
	}
	startX=(startX<0)?0:startX;
	startY=(startY<0)?0:startY;
	maxX=(maxX>=width)?width-1:maxX;
	maxY=(maxY>=height)?height-1:maxY;
	var newCells=0;
	var shown=0;
	var hidden=0;
	var shaded=0;
	var unshaded=0;
	var shadedD="";
	if (!isLocationPeaceful) {		
		for (var i=0;i<=maxX-startX;i++) {
			for (var j=0;j<=maxY-startY;j++) {
			// Переделать на циклы по x и y, как исправлю
				var x=startX+i;
				var y=startY+j;
				if (player.seenCells[x][y]!=undefined && !onGlobalMap) {
					if (isInPlayerVis(x,y) && !isInPlayerPrevVis(x,y)) {
						if (matrix[x][y].floor==null) {
							if (matrix[x][y].visible) {
							}
							matrix[x][y].show();
							shown++;
						}
						// matrix[x][y].unshade();
						unshaded++;
					} else if (!isInPlayerVis(x,y) && isInPlayerPrevVis(x,y)) {
						// shadedD+=x+", "+y+"(player.visibleCells["+x+"]["+y+"]) -
						// "+player.visibleCells[x][y]+"\n"
						matrix[x][y].shade();
						shaded++;
					} else if (isInRendRange(x,y) && !isInPrevRendRange(x,y) && prevRendCX!=-1) {
						matrix[x][y].show();
						shown++;
						if (player.visibleCells[x][y]==undefined) {
							matrix[x][y].shade();
						}
					}
				} else if (onGlobalMap && !isInPrevRendRange(x,y) && isInRendRange(x,y)) {
					// Показать новые
					matrix[x][y].show();
					shown++;
				}
				if (isInPrevRendRange(x,y) && !isInRendRange(x,y) && (player.seenCells[x][y] || onGlobalMap) && prevRendCX!=-1) {
					// Спрятать старые
					matrix[x][y].hide();
					hidden++;
				}
			}
		}
		player.prevVisibleCells=player.visibleCells;
	}
	prevRendCX=rendCX;
	prevRendCY=rendCY;
	
}
function showCell(x,y) {
	matrix[x][y].hide();
	setTimeout(function() { matrix[x][y].show(); },500);
}
function showSound(x, y, type) {
	var wrap = document.createElement("div");
	var text = document.createElement("div");
	wrap.className = "wrap";
	text.className = "speechBubbleText";
	text.innerText = soundTypes[type].name;
	wrap.style.zIndex = 9000;
	wrap.appendChild(text);
	gameField.appendChild(wrap);
	wrap.style.top = (32*y-text.clientHeight-12) + "px";
	wrap.style.left = (32*x-text.clientWidth/2+16) + "px"; 
	qanimate(wrap, [0,-32], 1000, function(obj) {
		gameField.removeChild(obj);
		handleNextEvent();
	});
}
function readCharacters(data) {
// Читает полученную от сервера информацию о персонажах в area и обрабатывает
// логику
/*
in : [[
0	characterId,
1	x,
2	y,
3	name,
4	fraction,
5	maxHp,
6	hp,
7	maxMp,
8	mp,
9	effects,
10	ammunition,
	(cls,race)
	|(type)
	]xM],
 */
	
	for (var i=0;i<data.length;i++) {
		if (data[i][3] == player.name) {
			continue;
		}
		var isPlayer = data[i].length == 13;
		if (characters[data[i][0]] == undefined) {
			// (id, type, x, y, fraction, race)
			characters[data[i][0]] = new Character(data[i][0], isPlayer ? undefined : data[i][11], data[i][1], data[i][2], data[i][4], data[i][12]);
			var character = characters[data[i][0]];
//			if (!player.canSee(characters[data[i][0]].x,characters[data[i][0]].y)) {
//				characters[data[i][0]].hide();
//			}
//			if (data[i][1]=="player") {
//				characters[data[i][0]].name=alliesNames[data[i][0]];
//			}
		} else {
			throw new Error("Персонаж "+data[3]+" уже создан!");
		}
		// Амуниция
//		currentCharacterId=data[i][0];
//		readAmmunition(data[i][10]);
		if (isPlayer) {		
			character.name = data[i][3];
			character.maxHp=data[i][71];
			character.mp=data[i][8];
			if (data[i][12]) {
				character.race = +data[i][12];
			}
			var ammunition = data[i][10];
			for (var i in ammunition) {
				character.ammunition.putOnToSlot(i, new UniqueItem(ammunition[i][0], ammunition[i][1]));
			}
			
			character.showAmmunition();
		}
	}
}
// Вспомогательные функции для readCharacters
	function readAttacks(attackers, index) {
		var attacker = attackers[index];
		var character = characters[attacker[0]];
		var isAttackMelee = isMelee(character.ammunition.getItemInSlot(0).typeId);
		var killedByAttackCopy=[];
		for (var i in killedByAttackCharacters) {
			killedByAttackCopy.push(killedByAttackCharacters[i]);
		}
		if (attacker[6][2]) {
		// Если атака приходится на клетку, а не на персонажа
			var projectileType=1;
			character.rangedAttack(attacker[6][0], attacker[6][1], projectileTypesNames[projectileType]);
		} else {
			var aimcharacter=characters[attacker[6][0]];
			if (!character.isClientPlayer) {
				character.aimcharacter=aimcharacter;
			}
			// Движение атакующего
			if (isAttackMelee) {
				character.meleeAttack(aimcharacter.x, aimcharacter.y);
			} else {
				var projectileType=1;
				character.rangedAttack(aimcharacter.x, aimcharacter.y, projectileTypesNames[projectileType],function() {
					aimcharacter.graphicEffect((attacker[6][1]>0 ? projectileEffectsNames[projectileType] : "none"),function() {
						if (attacker[6][1]!=-1) {
							var killedByAttackBuf=window.killedByAttackCharacters;
							window.killedByAttackCharacters=killedByAttackCopy;
							aimcharacter.showAttackResult();
							window.killedByAttackCharacters=killedByAttackBuf;
						}
					});
				});
			}
			if (attacker[6][1]==-1) {
			// Уворот
				aimcharacter.dodge(character);
			} else {
				if (isAttackMelee) {
					aimcharacter.graphicEffect(attacker[6][1]>0 ? "blood" : "none",function() {
						var killedByAttackBuf=window.killedByAttackCharacters;
						window.killedByAttackCharacters=killedByAttackCopy;
						aimcharacter.showAttackResult();
						window.killedByAttackCharacters=killedByAttackBuf;
					});
				}
				var characterIsDead=false;
				for (var j in killedByAttackCharacters) {
					if (killedByAttackCharacters[j]==aimcharacter.characterId) {
						killedByAttackCharacters[j]=undefined;
						characterIsDead=true;
					}
				}
				if (!characterIsDead) {
					aimcharacter.hp-=attacker[6][1];
					aimcharacter.showHpBar();
				}
			}
		}
		if (index<attackers.length-1) {
			// setTimeout здесь для того, чтобы не создавать рекурсию и начинать
			// каждую функцию в новом замыкании
			setTimeout(function() { 
				readAttacks(attackers, index+1); 
			},700);
		} else {
			checkStopped=false;
		}
		// Обработка смерти происходит в другом месте. Не помню, где. Но
		// происходит :3
	}
function readDeadCharacters(data) {
	if (!data) {
		return;
	}
	
	if (data===0) {
		return false;
	}
	deadCharacters=[];
	try {
		data.length;
	} catch (e) {
	}
	for (var i=0;i<data.length;i++) {
		if (characters[data[i]]!==undefined) {
			deadCharacters.push(data[i]);
		}
	}
}
function readTurns(data) {
// Получает порядок ходящих персонажей и отображает его в #intfQueue,
// А также устанавливает canAct, определяющую, может ли игрок сейчас отправлять
// данные о своих действиях
// Внимание: обрабатывает логику ходов персонажей функция readCharacters
	turns=data;
	
	if (!data || data.length==0) {
		return;
	}
	
	// Если следующий ход - ход игрока клиента, то игрок может действовать
	currentCharacterId=data[0];
	if (!characters[currentCharacterId]) {
		return;
	}
	if (characters[currentCharacterId].type=="player") {
	// Если это игрок, создать canvas с его изображением
		var canvas=document.getElementById("queueCharacter");
		var imageData=characters[currentCharacterId].doll.DOMNode.getContext("2d").getImageData(0,0,32,32);
		canvas.getContext("2d").putImageData(imageData,0,0);
		var nName=document.getElementById("queueCharacterName");
		nName.innerHTML=characters[currentCharacterId].name;
		if (characters[currentCharacterId].characterId==player.characterId) {
			// nName.style.color="#5d5";
			// nName.style.backgroundColor="#282";
		} else {
			nName.style.color="#fff";
			nName.style.backgroundColor="#aa7";
		}
	}
}
function readItems(data) {
// Обрабатывает принятые данные о предметах в рюкзаке
	var i;
	for (i=0;i<data.length;i++) {
		var typeId = data[i][0];
		var param = data[i][1];
		if (isUnique(typeId)) {
			player.items.addItem(new UniqueItem(typeId, param));
		} else {
			player.items.addItem(new ItemPile(typeId, param));
		}
	}
	UI.notify("inventoryChange");
	fitIntfInfo();
}
function readAmmunition(data, currentCharacterId) {
// Обрабатывает принятые данные об амуниции
// Формат: [(id)xN]
	// Копируем амуницию игрока для функции player.showAmmuniiton() (чтобы
	// проверить, нужно перерисовывать куклу, или нет)
	for (var slot in data) {
	// Записать в объект
		if (slot==9 && characters[currentCharacterId].ammunition.hasItemInSlot(9)) {
			slot=10;
		}
		characters[currentCharacterId].ammunition.putOnToSlot(slot, new UniqueItem(data[slot][0], data[slot][1]));
	}
	if (characters[currentCharacterId].type == "player") {
		characters[currentCharacterId].showAmmunition();
	}
}
function readSpells(data) {
// Отобразить полученные заклинания
// Format: [(spellId)xN]
	player.spells = data;
	
}
function readSkills(data) {
	var len=data.length/2;
	player.skills=[];
	for (var i=0;i<len;i++) {
		player.skills.push(data[i*2]);
		player.skills.push(data[i*2+1]);
	}
}
function sendPlayerQueuedAction() {
// Автоматически отправляет действие игрока), которое поставлено в очередь
// (например, когда игрок идёт на несколько ячеек
	if (player.destX!=player.x || player.destY!=player.y) {
		window.futter=new Date().getTime();
		player.action();
		
	}
}
function worldMapRenderView() {
// Отображение вида глобальной карты
// Использует функцию renderView()
	if (prevRendCX==-1) {
		var startX=rendCX-(rendW-1)/2;
		var startY=rendCY-(rendH-1)/2;
		var maxX=rendCX+(rendW-1)/2;
		var maxY=rendCY+(rendH-1)/2;
	} else {
		var startX=Math.min(rendCX-(rendW-1)/2, prevRendCX-(rendW-1)/2);
		var startY=Math.min(rendCY-(rendH-1)/2, prevRendCY-(rendH-1)/2);
		var maxX=Math.max(rendCX+(rendW-1)/2, prevRendCX+(rendW-1)/2);
		var maxY=Math.max(rendCY+(rendH-1)/2, prevRendCY+(rendH-1)/2);
	}
	startX=(startX<0)?0:startX;
	startY=(startY<0)?0:startY;
	maxX=(maxX>=width)?width-1:maxX;
	maxY=(maxY>=height)?height-1:maxY;
	player.visibleCells=blank2dArray();
	for (var i=startX;i<=maxX;i++) {
		for (var j=startY;j<=maxY;j++) {
			player.visibleCells[i][j] = true;
		}
	}
	
	renderView();
	player.prevVisibleCells=player.visibleCells;
}
function drawWorldMapFloor(ctx,x,y,floor) {
// Создаёт изображение поверхности глобальной карты (только для floor, объекты
// отрисовываются так же, как и на локальной карте)
	// Получаем типы соседних тайлов или тип этого тайла, если такого соседнего
	// тайла нет (если этот тайл на границе)
	var up=(y==0) ? floor : matrix[x][y-1].floor.type;
	var right=(x==width-1) ? floor : matrix[x+1][y].floor.type;
	var down=(y==height-1) ? floor : matrix[x][y+1].floor.type;
	var left=(x==0) ? floor : matrix[x-1][y].floor.type;
	
	var tileType="t"+floor+","+up+","+right+","+down+","+left;
	if (up!=floor || right!=floor || down!=floor || left!=floor) {
		if (floorImages[tileType]!==undefined) {
		// Если изображение такого тайла уже использовалось, и поэтому
		// сгенерировано
			ctx.putImageData(floorImages[tileType],x*32,y*32);
		} else {
		// Если изображение такого тайла ещё не сгенерировано, то создать его
			ctx.drawImage(tiles[floor][0],x*32,y*32);
			// Отрисовка переходов
			var neighbors=[up,right,down,left];
//			for (var i=0;i<4;i++) {
//				if (neighbors[i]!=floor) {
//					ctx.getTransition(neighbors[i],i,true,x*32,y*32);
//				}
//			}
//			try {
				floorImages[tileType]=ctx.getImageData(x*32,y*32,32,32);
//			} catch (e) {
//				alert("Необходимо включить возможность открытия локальных файлов в canvas");
//			}
		}
	} else {
		ctx.drawImage(tiles[floor][getNumFromSeed(x,y,NUM_OF_TILES[floorNames[floor]])], x*32, y*32);
	}
	if (+localStorage.getItem(2)) {
		var imageData=ctx.getImageData(x*32,y*32,32,32);
		// Отрисовка сетки
		for (var i=0;i<32;i++) {
			var pix=getPixel(imageData,i,0);
			pix[3]=200;
			setPixel(imageData,i,0,pix);
		}
		for (var j=0;j<32;j++) {
			var pix=getPixel(imageData,0,j);
			pix[3]=200;
			setPixel(imageData,0,j,pix);
		}
		ctx.putImageData(imageData,x*32,y*32);
	}
}
function readWorld(data) {
// Отобразить карту мира
/*
	Format: {
		w:xSize,
		h:ySize,
		c:[[ground,forest,road,river,race,[objects]]xN]
	}
*/
	width = data.w;
	height = data.h;
	player.seenCells = blank2dArray();
	for (var x=0;x<width;x++) {
		player.seenCells[x]=[];
		for (var y=0;y<height;y++) {
			player.seenCells[x][y]=true;
		}
	}
	prepareArea(true);
	var contents=data.c;
	var x=0, y=0;
	for (var i=0;i<contents.length;i++) {
		x++;
		if (x==width) {
			x=0;
			y++;
		}
	}
	x=0;
	y=0;
	var u=0;
	// Отрисовано ли уже изображение поверхности мира
	var worldFloorImageDrawn=!!worldMapFloorCanvas;
	if (!worldFloorImageDrawn) {
	// Если изображение поверхности мира ещё не отрисовано, то создать для него
	// канвас
		worldMapFloorCanvas=document.createElement("canvas");
		worldMapFloorCanvas.width=width*32;
		worldMapFloorCanvas.height=height*32;
		var ctx=worldMapFloorCanvas.getContext("2d");
	} else {
		worldMapFloorCanvas.getContext("2d").clearRect(0,0,width*32, height*32);
	}
	
	for (var num=0;num<contents.length;num++) {
		matrix[x][y].floor = new Floor(x,y,contents[num][0]);
		x++;
		if (x==width) {
			x=0;
			y++;
		}
	}
	
	x=0;
	y=0;
	for (var num=0;num<contents.length;num++) {
		// (пол создавался выше)
		matrix[x][y].floor.show();
		if (contents[x+y*width][1]==901) {
			new WorldObject(x,y,901);
		} else if (contents[x+y*width][1] != 0) {
		// Лес
			if (contents[x+y*width][1]==900) {
				matrix[x][y].forest = new Forest(x,y,900);
			} else if (contents[x+y*width][1]==903) {
				new Wall(x,y,903);
				matrix[x][y].wall.show();
			} else if (contents[x+y*width][1]==904) {
				new WorldObject(x,y,904);
			}
		}
		var isRiver = contents[x+y*width][3] > 0;
		var isRoad = contents[x+y*width][2] > 0;
		if (isRiver) {
		// Река
			matrix[x][y].path = new Path(x,y,21);
		} else if (isRoad) {
		// Дорога
			matrix[x][y].path = new Path(x,y,31);
		}
		if (isRiver || isRoad) {
			if (x-1>=0 && matrix[x-1][y].wall) {
				matrix[x-1][y].wall.hide(); matrix[x-1][y].wall.show();
			}
			if (x+1<width && matrix[x+1][y].wall) { 
				matrix[x+1][y].wall.hide(); matrix[x+1][y].wall.show(); 
			}
			if (y+1<height && matrix[x][y+1].wall) { 
				matrix[x][y+1].wall.hide(); matrix[x][y+1].wall.show();
			}
			if (y-1>=0 && matrix[x][y-1].wall) { 
				matrix[x][y-1].wall.hide(); matrix[x][y-1].wall.show();
			}
		}
		if (contents[x+y*width][3]!=0) {
			for (var i=0;i<contents[x+y*width][3].length;i++) {
				new WorldObject(x,y,contents[x+y*width][3][i]);
				matrix[x][y].object.show();
			}
		}
		x++;
		if (x==width) {
			x=0;
			y++;
		}
	}
	// Обернуть изображение поверхности и добавить к карте мира
	var worldGroundWrap=document.createElement("div");
	worldGroundWrap.className="wrap";
	worldGroundWrap.appendChild(worldMapFloorCanvas);
	document.getElementById("gameField").appendChild(worldGroundWrap);
	
	moveGameField(Math.floor(width/2),Math.floor(height/2), true);
	rendCX=Math.floor(width/2);
	rendCY=Math.floor(height/2);
	worldMapRenderView();
	document.getElementById("intfQueue").style.display="none";
	hideMenu();
}
function readLocation(data) {
// Reads location contents got from server
/*
	Format: {
		w:xSize,
		h:ySize,
		c:[[floor,object,items,river,race,[objects]]xN]
	}
*/
	
	width=data.w;
	height=data.h;
	player.seenCells=[];
	for (var x=0;x<width;x++) {
		player.seenCells[x]=[];
		for (var y=0;y<height;y++) {
			player.seenCells[x][y]=true;
		}
	}
	prepareArea(true);
	var contents=data.c;
	var x=0, y=0;
	for (var i=0;i<contents.length;i++) {
		x++;
		if (x==width) {
			x=0;
			y++;
		}
	}
	x=0;
	y=0;
	var u=0;
	var cell;
	for (var num=0;num<contents.length;num++) {
		cell = contents[x+y*width];
		matrix[x][y].floor = new Floor(x,y,contents[num][0]);
		if (cell[1]!=0) {
			if (isWall(cell[1])) {
				new Wall(x, y, cell[1]);					
			} else {
				new GameObject(x, y, cell[1]);
			}
		}
		if (cell[2]) {
			for (var i=0; i<cell[2].length; i++) {
				var typeId = cell[2][i][0];
				var param = cell[2][i][1];
				if (isUnique(typeId)) {
				// Здесь нужно именно так, чтобы не вызывалось отображение добавляемого предмета
					matrix[x][y].addItemWithoutShowing(new UniqueItem(typeId, param));
				} else {
				// Здесь нужно именно так, чтобы не вызывалось отображение добавляемого предмета
					matrix[x][y].addItemWithoutShowing(new ItemPile(typeId, param));
				}
			}
		}
		x++;
		if (x==width) {
			x=0;
			y++;
		}
	}
	// Read sounds
	if (data.s) {
		for (var i in data.s) {
			new SoundSource(data.s[i][0], data.s[i][1], data.s[i][2]);
		}
	}
}
function createPlayerFromData(data) {
	
// Создаёт игрока
/*
 * Формат данных: [ 0:id, 1:name, 2:race, 3:cls, 4:lvl, 5:maxHp,
 * 6:maxMp, 7:hp, 8:mp, 9:str, 10:dex, 11:wis, 12:int, 13:items,
 * 14:ammunition, 15:spells, 16:skills, 17:x, 18:y ]
 * 
 * 
 */
	
	player=new Character(data[0],"player",data[17],data[18],1,data[2],true);
	UI.notify("titleChange");
	
	characters[data[0]]=player;
	player.name=data[1];
	player.race=data[2];
	player.cls=data[3];
	player.lvl=data[4];
	player.maxHp=data[5];
	player.maxMp=data[6];
	player.hp=data[7];
	player.mp=data[8];
	player.str=data[9];
	player.dex=data[10];
	player.wis=data[11];
	player.itl=data[12];
	readItems(data[13]);
	readAmmunition(data[14], data[0]);
	readSpells(data[15]);
	readSkills(data[16]);
	player.showLoot();
	return true;
}
function readWorldPlayer(data) {
	// Получить и отобразить полную информацию об игроке на глобальной карте
	// Формат: [characterId, name, race, class, level, maxHp, maxMp, str, dex, wis, itl,
	// items, ammunition, spells, skills, worldX, worldY]
	// Похоже на createPlayerFromData, но для глобальной карты]
		player = characters[data[0]];
		UI.notify("titleChange");
		player.isClientPlayer = true;
		player.name = data[1];
		player.race = data[2];
		player.cls = data[3];
		player.level = data[4];
		player.hp = data[5];
		player.maxHp = data[5];
		player.mp = data[6];
		player.maxMp = data[6];
		player.str = data[7];
		player.dex = data[8];
		player.wis = data[9];
		player.itl = data[10];
		readItems(data[11]);
		readAmmunition(data[12], data[0]);
		player.worldX = data[15];
		player.worldY = data[16];
		UI.notify("inventoryChange");
		player.showAmmunition();
		return true;
	}
function test(x,y) {
	player={
		name:player.name,
		worldX:x,
		worldY:y,
		partyId:player.partyId
	};
	enterArea();
}
function enterArea(callback) {
// Войти в область [player.worldX,player.worldY] и загрузить информацию о
// персонаже
	showStLoad();
	Net.send({
		a:Net.ENTER_LOCATION,
		x:worldPlayers[player.characterId].x,
		y:worldPlayers[player.characterId].y,
		characterId:player.characterId
	},handlers.net.loadContents);
//	Net.send({a:Net.APPEAR,n:player.name,x:player.worldX,y:player.worldY,pid:player.partyId,islead:player.isPartyLeader},handlers.net.appear);
}
function worldTravel(x,y) {
// Путешествие из одной клетки глобальной карты в другую
	Net.send({a:Net.WORLD_TRAVEL,x:x,y:y},handlers.net.worldTravel);
}
function leaveLocation(callback) {
// Выйти из области или загрузить мир при загрузке игры, в т. ч. загрузить
// информацию об игроке
	showStLoad();
	Net.send({a:Net.LEAVE_LOCATION},handlers.net.loadContents);
}
function readOnlinePlayers(data) {
// Отобразить список игроков онлайн и сохранить его в переменной
// in: [[characterId,name,class,race,party,worldX,worldY]xN]
	for (var i in data) {
		characters[data[i][0]] = new Character(data[i][0], "player", data[i][5], data[i][6], 1, data[i][3], false);
		new WorldPlayer(data[i][5],data[i][6],characters[data[i][0]]);
	}
}
function readChatMessages(data) {
// Отобразить полученные сообщения в окне чата
// Формат: [name,message, name,message...]
	var len=data.length/2;
	for (var i=0;i<len;i++) {
		chat.push([data[i*2],data[i*2+1]]);
	}
	UI.notify("chatMessage");
}
function isChatOpen() {
	// Ищем #chatForm > div.wrap и выходим из выполнения функции, если у него
	// display!="none"
	var nlChatMessages=document.getElementById("chatForm").getElementsByTagName("div");
	for (var i=0;i<nlChatMessages.length;i++) {
		if (nlChatMessages[i].className=="wrap" && nlChatMessages[i].style.display!="none") {
			return true;
		}
	}
	return false;
}
function readInvite(data) {
// Отобразить приглашение
// in: [inviterId,inviterName]
	if (data===0) {
		return false;
	}
	inviterPlayerId=data[0];
	document.getElementById("inviteName").innerHTML=data[1];
	document.getElementById("infoInvite").style.visibility="visible";
}
function readEntering(data) {
	if (data) {
		enterArea();
	}
}
