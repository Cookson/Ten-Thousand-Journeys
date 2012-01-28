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
		onLoadEvents['defaultUIActions']();
		onLoadEvents['gui']();
		onLoadEvents['storage']();
		onLoadEvents['ajax']();
		onLoadEvents['keys']();
	}, 1);
};
onLoadEvents['game'] = function _() {
	if (!document.head) {
		document.head = document.getElementsByTagName("head")[0];
	}
	cacheImages();
	saveParticlesImageData();
	Keys.formReverseKeyCodesTable();
};
function moveGameField(x,y,initiate) {
	var normal = Terrain.getViewIndentation(x,y,1);
	x = normal.left;
	y = normal.top;
	var xCells=x;
	var yCells=y;
	x=UI.visibleWidth/2-x*32;
	x-=(x%32==0)?0:16;
	x=(x<0)?((Terrain.getHorizontalDimension()-xCells-1)*32<UI.visibleWidth/2)?UI.visibleWidth-Terrain.getHorizontalDimension()*32:x:0;
	if (Terrain.getHorizontalDimension()*32<UI.visibleWidth) {
		x=0;
	}
	y=UI.visibleHeight/2-y*32;
	y-=(y%32==0)?0:16;
	y=(y<0)?((Terrain.getVerticalDimension()-yCells-1)*32<UI.visibleHeight/2)?UI.visibleHeight-Terrain.getVerticalDimension()*32:y:0;
	if (Terrain.getVerticalDimension()*32<UI.visibleHeight) {
		y=0;
	}
	// Here x and y contain indentations in pixels.
	if (+localStorage.getItem(1) && !initiate) {
		if (Terrain.onGlobalMap) {
			var dist=distance(xCells,yCells, Player.worldX, Player.worldY);
			var time=(dist<2) ? 200 : (dist<10) ? dist*30 : 150;
			qanimate(gameField, [x-parseInt(gameField.style.left),y-parseInt(gameField.style.top)], time, function() {
				// gameField.onmousemove(false);c
			});
		} else {
			qanimate(gameField, [x-parseInt(gameField.style.left),y-parseInt(gameField.style.top)], 90, function() {
				
			});
		}
	} else {
		gameField.style.left=x+"px";
		gameField.style.top=y+"px";		
		if (weatherEffect) {
			var wx = Player.x;
			var wy = Player.y;
			weatherEffect.move(Math.min(Math.max(wx, rendCX), Terrain.width-rendCX), Math.min(Math.max(wy, rendCY), Terrain.height-rendCY));
		}
	}
}
function centerWorldCamera(x,y,initiate) {
	moveGameField(x,y,initiate);
	rendCX=x;
	rendCY=y;
	renderView();
}
/**
 * @private
 * 
 * @param {Boolean} isWorld True is world loading, false if location loading.
 */
function prepareArea(isWorld) {
// Подготавливает область для загрузки в неё данных с сервера
// Если isWorld - загружется мир, иначе - область
	while (gameField.children.length>2) {
	// Remove all the children of #gameField, except of gameFieldFloor,
	// cellCursor
		gameField.removeChild(gameField.children[2]);
	}
	rendCX = -1;
	rendCY = -1;
	prevRendCX = -1;
	prevRendCY = -1;
	Terrain.cells = blank2dArray();
	for (var i=0;i<Terrain.width;i++) { 
		for (var j=0;j<Terrain.height;j++) { 
			Terrain.cells[i][j] = new Cell(i,j);
		}
	}
	floorCanvas.width=Terrain.getHorizontalDimension()*32;
	floorCanvas.height=Terrain.getVerticalDimension()*32;
	gameField.style.display = "inline-block";
	gameField.style.width = (32*Terrain.getHorizontalDimension())+"px";
	gameField.style.height = (32*Terrain.getVerticalDimension())+"px";	
}
function performAction(actionName, args) {
	if (!(actionName in UI.registeredActions)) {
		throw new Error("No action "+actionName+" registered");
	}
	if (args === undefined) {
		args = [];
	}
	UI.registeredActions[actionName]._handler
		.apply(UI.registeredActions[actionName]._context, args);
}
function renderView() {
// Прорисовка содержимого ячеек
	if (rendCX-(rendW-1)/2 < 0) {
	// Чтобы область прорисовки была полностью на игровом поле
	// По оси X
		rendCX = (rendW-1)/2;
	} else if (rendCX+(rendW-1)/2 >= Terrain.width) {
		rendCX = Terrain.width-1-(rendW-1)/2;
	}
	if (rendCY-(rendH-1)/2 < 0) {
	// По оси Y
		rendCY = (rendH-1)/2;
	} else if (rendCY+(rendH-1)/2 >= Terrain.height) {
		rendCY = Terrain.height-1-(rendH-1)/2;
	}
	if (prevRendCX==-1) {
		var startX = rendCX-(rendW-1)/2;
		var startY = rendCY-(rendH-1)/2;
		var maxX = rendCX+(rendW-1)/2;
		var maxY = rendCY+(rendH-1)/2;
	} else {
		var startX = Math.min(rendCX-(rendW-1)/2, prevRendCX-(rendW-1)/2);
		var startY = Math.min(rendCY-(rendH-1)/2, prevRendCY-(rendH-1)/2);
		var maxX = Math.max(rendCX+(rendW-1)/2, prevRendCX+(rendW-1)/2);
		var maxY = Math.max(rendCY+(rendH-1)/2, prevRendCY+(rendH-1)/2);
	}
	startX = (startX<0) ? 0 : startX;
	startY = (startY<0) ? 0 : startY;
	maxX = (maxX>=Terrain.width)?Terrain.width-1:maxX;
	maxY = (maxY>=Terrain.height)?Terrain.height-1:maxY;
	var newCells = 0;
	var shown = 0;
	var hidden= 0 ;
	var shaded = 0;
	var unshaded = 0;
	var shadedD = "";
	if (!Terrain.isPeaceful) {		
		for (var i=0; i<=maxX-startX; i++) {
			for (var j=0; j<=maxY-startY; j++) {
			// Переделать на циклы по x и y, как исправлю
				var x = startX+i;
				var y = startY+j;
				if (!Terrain.onGlobalMap && Player.seenCells[x][y]!=undefined) {
					if (isInPlayerVis(x,y) && !isInPlayerPrevVis(x,y)) {
						if (Terrain.cells[x][y].floor == null) {
							if (Terrain.cells[x][y].visible) {
							}
							Terrain.cells[x][y].show();
							shown++;
						}
						// Terrain.cells[x][y].unshade();
						unshaded++;
					} else if (!isInPlayerVis(x,y) && isInPlayerPrevVis(x,y)) {
						// shadedD+=x+", "+y+"(Player.visibleCells["+x+"]["+y+"]) -
						// "+Player.visibleCells[x][y]+"\n"
						Terrain.cells[x][y].shade();
						shaded++;
					} else if (isInRendRange(x,y) && !isInPrevRendRange(x,y) && prevRendCX!=-1) {
						Terrain.cells[x][y].show();
						shown++;
						if (Player.visibleCells[x][y] === undefined) {
							Terrain.cells[x][y].shade();
						}
					}
				} else if (Terrain.onGlobalMap && !isInPrevRendRange(x,y) && isInRendRange(x,y)) {
					// Показать новые
					Terrain.cells[x][y].show();
					shown++;
				}
				if (isInPrevRendRange(x,y) && !isInRendRange(x,y) && (Player.seenCells[x][y] || Terrain.onGlobalMap) && prevRendCX!=-1) {
					// Спрятать старые
					Terrain.cells[x][y].hide();
					hidden++;
				}
			}
		}
//		Player.prevVisibleCells = Player.visibleCells;
	}
	prevRendCX = rendCX;
	prevRendCY = rendCY;	
}
function rotateCamera(side) {
	if (!(side instanceof Side)) {
		throw new Error("This function must get instance of Side as parameter!");
	}
	Terrain.cameraOrientation = side;
	for (var y=0; y<Terrain.height; y++) {
		for (var x=0; x<Terrain.width; x++) {
			Terrain.cells[x][y].hide();
		}
	}
	floorCanvas.width=Terrain.getHorizontalDimension()*32;
	floorCanvas.height=Terrain.getVerticalDimension()*32;
	for (var y=0; y<Terrain.height; y++) {
		for (var x=0; x<Terrain.width; x++) {
			Terrain.cells[x][y].show();
		}
	}
	for (var ch in characters) {
		characters[ch].placeSprite();
	}
	moveGameField(Player.x, Player.y, true);
	UI.notify("cameraRotation");
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
10	equipment,
	(cls,race)
	|(type)
	]xM],
 */
	for (var i=0;i<data.length;i++) {
		if (data[i][3] == Player.name) {
			continue;
		}
		
		var isPlayer = data[i].length == 13;
		new Character(
			data[i][0],
			isPlayer ? "player" : data[i][11], 
			data[i][1], 
			data[i][2], 
			data[i][4],
			data[i][6],
			data[i][5]
		);
		var character = characters[data[i][0]];
		character.display();
	}
}
function worldMapRenderView() {
// Отображение вида глобальной карты
// Использует функцию renderView()
	if (prevRendCX == -1) {
		var startX = rendCX-(rendW-1)/2;
		var startY = rendCY-(rendH-1)/2;
		var maxX = rendCX+(rendW-1)/2;
		var maxY = rendCY+(rendH-1)/2;
	} else {
		var startX = Math.min(rendCX-(rendW-1)/2, prevRendCX-(rendW-1)/2);
		var startY = Math.min(rendCY-(rendH-1)/2, prevRendCY-(rendH-1)/2);
		var maxX = Math.max(rendCX+(rendW-1)/2, prevRendCX+(rendW-1)/2);
		var maxY = Math.max(rendCY+(rendH-1)/2, prevRendCY+(rendH-1)/2);
	}
	startX = (startX < 0) ? 0 : startX;
	startY = (startY < 0) ? 0 : startY;
	maxX = (maxX >= Terrain.width) ? Terrain.width-1 : maxX;
	maxY = (maxY >= Terrain.height) ? Terrain.height-1 : maxY;
//	Player.visibleCells = blank2dArray();
//	for (var i=startX; i<=maxX; i++) {
//		for (var j=startY; j<=maxY; j++) {
//			Player.visibleCells[i][j] = true;
//		}
//	}
	
	renderView();
//	Player.prevVisibleCells = Player.visibleCells;
}
function drawWorldMapFloor(ctx,x,y,floor) {
// Создаёт изображение поверхности глобальной карты (только для floor, объекты
// отрисовываются так же, как и на локальной карте)
	// Получаем типы соседних тайлов или тип этого тайла, если такого соседнего
	// тайла нет (если этот тайл на границе)
	var up = (y==0) ? floor : Terrain.cells[x][y-1].floor.type;
	var right = (x==Terrain.width-1) ? floor : Terrain.cells[x+1][y].floor.type;
	var down = (y==Terrain.height-1) ? floor : Terrain.cells[x][y+1].floor.type;
	var left = (x==0) ? floor : Terrain.cells[x-1][y].floor.type;
	
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
	Terrain.width = data.w;
	Terrain.height = data.h;
//	Player.seenCells = blank2dArray();
//	for (var x=0;x<Terrain.width;x++) {
//		Player.seenCells[x]=[];
//		for (var y=0;y<Terrain.height;y++) {
//			Player.seenCells[x][y]=true;
//		}
//	}
	prepareArea(true);
	var contents=data.c;
	var x=0, y=0;
	for (var i=0; i<contents.length; i++) {
		x++;
		if (x==Terrain.width) {
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
		worldMapFloorCanvas.width=Terrain.width*32;
		worldMapFloorCanvas.height=Terrain.height*32;
		var ctx=worldMapFloorCanvas.getContext("2d");
	} else {
		worldMapFloorCanvas.getContext("2d").clearRect(0,0,Terrain.width*32, Terrain.height*32);
	}
	
	for (var num=0; num<contents.length; num++) {
		Terrain.cells[x][y].floor = new Floor(x,y,contents[num][0]);
		x++;
		if (x==Terrain.width) {
			x=0;
			y++;
		}
	}
	
	x=0;
	y=0;
	for (var num=0;num<contents.length;num++) {
		// (пол создавался выше)
		Terrain.cells[x][y].floor.show();
		if (contents[x+y*Terrain.width][1]==901) {
			new WorldObject(x,y,901);
		} else if (contents[x+y*Terrain.width][1] != 0) {
		// Лес
			if (contents[x+y*Terrain.width][1]==900) {
				Terrain.cells[x][y].forest = new Forest(x,y,900);
			} else if (contents[x+y*Terrain.width][1]==903) {
				new Wall(x,y,903);
				Terrain.cells[x][y].wall.show();
			} else if (contents[x+y*Terrain.width][1]==904) {
				new WorldObject(x,y,904);
			}
		}
		var isRiver = contents[x+y*Terrain.width][3] > 0;
		var isRoad = contents[x+y*Terrain.width][2] > 0;
		if (isRiver) {
		// Река
			Terrain.cells[x][y].path = new Path(x,y,21);
		} else if (isRoad) {
		// Дорога
			Terrain.cells[x][y].path = new Path(x,y,31);
		}
		if (isRiver || isRoad) {
			if (x-1>=0 && Terrain.cells[x-1][y].wall) {
				Terrain.cells[x-1][y].wall.hide(); Terrain.cells[x-1][y].wall.show();
			}
			if (x+1<Terrain.width && Terrain.cells[x+1][y].wall) { 
				Terrain.cells[x+1][y].wall.hide(); Terrain.cells[x+1][y].wall.show(); 
			}
			if (y+1<Terrain.height && Terrain.cells[x][y+1].wall) { 
				Terrain.cells[x][y+1].wall.hide(); Terrain.cells[x][y+1].wall.show();
			}
			if (y-1>=0 && Terrain.cells[x][y-1].wall) { 
				Terrain.cells[x][y-1].wall.hide(); Terrain.cells[x][y-1].wall.show();
			}
		}
		if (contents[x+y*Terrain.width][3]!=0) {
			for (var i=0;i<contents[x+y*Terrain.width][3].length;i++) {
				new WorldObject(x,y,contents[x+y*Terrain.width][3][i]);
				Terrain.cells[x][y].object.show();
			}
		}
		x++;
		if (x == Terrain.width) {
			x = 0;
			y++;
		}
	}
	// Обернуть изображение поверхности и добавить к карте мира
	var worldGroundWrap=document.createElement("div");
	worldGroundWrap.className="wrap";
	worldGroundWrap.appendChild(worldMapFloorCanvas);
	document.getElementById("gameField").appendChild(worldGroundWrap);
	
	moveGameField(Math.floor(Terrain.width/2),Math.floor(Terrain.height/2), true);
	rendCX=Math.floor(Terrain.width/2);
	rendCY=Math.floor(Terrain.height/2);
	renderView();
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
	Terrain.width = data.w;
	Terrain.height = data.h;
//	for (var x=0;x<Terrain.width;x++) {
//		Player.seenCells[x]=[];
//		for (var y=0;y<Terrain.height;y++) {
//			Player.seenCells[x][y]=true;
//		}
//	}
	prepareArea(true);
	var contents=data.c;
	var x=0, y=0;
	for (var i=0;i<contents.length;i++) {
		x++;
		if (x == Terrain.width) {
			x=0;
			y++;
		}
	}
	x=0;
	y=0;
	var u=0;
	var cell;
	for (var num=0;num<contents.length;num++) {
		cell = contents[x+y*Terrain.width];
		Terrain.cells[x][y].floor = new Floor(x,y,contents[num][0]);
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
					Terrain.cells[x][y].addItemWithoutShowing(new UniqueItem(typeId, param));
				} else {
				// Здесь нужно именно так, чтобы не вызывалось отображение добавляемого предмета
					Terrain.cells[x][y].addItemWithoutShowing(new ItemPile(typeId, param));
				}
			}
		}
		x++;
		if (x == Terrain.width) {
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
	if (data.ceilings) {
		for (var i=0; i<data.ceilings.length; i++) {
			new Ceiling(data.ceilings[i][0],data.ceilings[i][1],data.ceilings[i][2],data.ceilings[i][3]);
		}
	}
}
function enterArea(callback) {
// Войти в область [Player.worldX,Player.worldY] и загрузить информацию о
// персонаже
	showLoadingScreen();
	Net.send({
		a:Net.ENTER_LOCATION,
		x:worldPlayers[Player.characterId].x,
		y:worldPlayers[Player.characterId].y,
		characterId:Player.characterId
	},handlers.net.loadContents);
//	Net.send({a:Net.APPEAR,n:Player.name,x:Player.worldX,y:Player.worldY,pid:Player.partyId,islead:Player.isPartyLeader},handlers.net.appear);
}
function leaveLocation(callback) {
// Выйти из области или загрузить мир при загрузке игры, в т. ч. загрузить
// информацию об игроке
	showLoadingScreen();
	Net.send({a:Net.LEAVE_LOCATION},handlers.net.loadContents);
}
function readOnlinePlayers(data) {
// Отобразить список игроков онлайн и сохранить его в переменной
// in: [[characterId,name,class,race,party,worldX,worldY]xN]
	for (var i in data) {
		new WorldPlayer(data[i][0], data[i][1], data[i][2], data[i][3], data[i][4], data[i][5], data[i][6]);
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
