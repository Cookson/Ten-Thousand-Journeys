function Character(id, type, x, y, fraction) {
	if (id == -1) {
	// For Player.prototype
		return;
	}
	characters[id] = this;
	this.x=x;
	this.y=y;
	this.isClientPlayer = false;
	Terrain.cells[x][y].character = this;
	this.characterId=id;
	this.destX=this.x;
	this.destY=this.y;
	this.type=(type!=undefined)?type:"player";
	if (this.type!="player") {
		this.name = characterTypes[type][0];
	}
	
	this.fraction=(fraction==undefined)?(this.characterId==0)?1:0:fraction;
	
	this.aimcharacter=-1;
	this.cls = null;
	if (this.type!="player") {
		this.maxHp=(this.type=="player")?30:characterTypes[this.type][1];
		this.maxMp=this.mp;
		this.maxEp=this.ep;
		this.hp=this.maxHp;
		this.mp=this.maxHp;
	}
	
	this.VISION_RANGE=8;
	this.visible;
	
	// Ячейка
	this.cellWrap=document.createElement("div");
	this.cellWrap.className="cellWrap";
	this.cellWrap.setAttribute("id", "character"+this.characterId);
	gameField.appendChild(this.cellWrap);
	var viewIndent = Terrain.getViewIndentation(x,y,32);
	this.cellWrap.style.top=viewIndent.top+"px";
	this.cellWrap.style.left=viewIndent.left+"px";
	this.cellWrap.style.opacity="0";
	this.cellWrap.style.zIndex=this.y*2+2;
	
	// Массивы	
	this.ammunition = new Ammunition();	
	this.effects={};
	
	// Заклинания
	this.spellId=-1;
	this.spellAimId=-1;
	this.spellX=-1;
	this.spellY=-1;
}
Character.prototype.display = function () { 
// Initiates character view
// This method is overriden in Player class
	var ncharacterImage=document.createElement("img");
	ncharacterImage.className="characterIcon";
	ncharacterImage.setAttribute("characterId",this.characterId);
	ncharacterImage.setAttribute("src","images/characters/"+this.type+".png");
	if (this.type in characterSpriteSizes) {
	// If this mod's sprite has irregular size (not 32х32)
		ncharacterImage.style.left=((32-characterSpriteSizes[this.type][0])/2)+"px";
		ncharacterImage.style.top=((16-characterSpriteSizes[this.type][1]))+"px";
	}
	this.cellWrap.appendChild(ncharacterImage);
	if (this.fraction == 1) {
	// Friend marker
		var nWrap=document.createElement("div");
		nWrap.className="wrap";
		var nFriendMarker=document.createElement("img");
		nFriendMarker.className="cellFriendMarker";
		nFriendMarker.src="./images/intf/friendMarker.png";
		nWrap.appendChild(nFriendMarker);
		this.cellWrap.insertBefore(nWrap,this.cellWrap.children[0]);
	}
	if (player.canSee(this.x, this.y)) {
		this.showModel();
	}
	// HP strip
	this.initHpBar();
};
Character.prototype.sendAttack = function(aimId, ranged) {
	Net.send({a:Net.ATTACK, aimId:aimId, ranged:ranged});
};
Character.prototype.showAttack = function(aimId, ranged) {
	handleNextEvent();
};
Character.prototype.showDamage = function(amount, type) {
	this.showAttackResult();
	this.hp -= amount;
	this.refreshHpBar();
};
Character.prototype.canSee = function (x, y, setVisibleCells, test) {
	// Проверяет, находится ли данная клетка на линии видимости
//	return true;
	if (this.isNear(x,y) || this.x==x && this.y==y || isLocationPeaceful) {
	// Если клетка рядом или персонаж на ней стоит - то её точно видно
		if (setVisibleCells) {
			this.visibleCells[x][y] = true;
		}
		return true;
	}
	if (Math.floor(this.distance(x, y))>this.VISION_RANGE) {
		return false;
	}
	// Алгоритм рассматривает несколько случаев взаимного расположения начальной и целевой клетки, 
	// поскольку в значительной части случаев расчёт можно упростить. Алгоритм для общего случая рассматривается последним.
	if (x == this.x || y == this.y) {
		// Для случая, когда тангенс прямой (угловой коэффициент) равен бесконечности или 0 
		// (т.е. когда в цикле в else может быть деление на ноль т.к. абсцисы или ординаты конца и начала равны)
		// В этом случае придётся сделать только одну проверку по линии (не таким методом, как в else для прямых с tg!=0 и tg!=1)
		if (x == this.x) {
		// Для вертикальных линий
			var dy=Math.abs(y-this.y)/(y-this.y);
			for (var i=this.y+dy;i!=y;i+=dy) {
				if (Terrain.cells[x][i].passability == Terrain.PASS_BLOCKED) {
					return false;
				}
			}
		} else {
		// Для горизонтальных линий
			var dx=Math.abs(x-this.x)/(x-this.x);
			for (var i=this.x+dx;i!=x;i+=dx) {
				if (Terrain.cells[i][y].passability == Terrain.PASS_BLOCKED) {
					return false;
				}
			}
		}
		if (setVisibleCells) {
			this.visibleCells[x][y] = true;
		}
		return true;
	} else if (Math.abs(x-this.x)==1) {
	// Для случая, когда координаты конца и начала находятся на двух соседних вертикальных линиях
		var yMin=Math.min(y,this.y);
		var yMax=Math.max(y,this.y);
		for (var i=yMin+1;i<yMax;i++) {
			if (Terrain.cells[x][i].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==yMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y]=true;
				}
				return true;
			}
		}
		for (var i=yMin+1;i<yMax;i++) {
			if (Terrain.cells[this.x][i].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==yMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y]=true;
				}
				return true;
			}
		}
		return false;
	} else if (Math.abs(y-this.y)==1) {
	// Тот же случай, что и предыдущий, но для горизонтальных линий
		var xMin=Math.min(x,this.x);
		var xMax=Math.max(x,this.x);
		for (var i=xMin+1;i<xMax;i++) {
			if (Terrain.cells[i][y].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==xMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y]=true;
				}
				return true;
			}
		}
		for (var i=xMin+1;i<xMax;i++) {
			if (Terrain.cells[i][this.y].passability==Terrain.PASS_BLOCKED) {
				break;
			}
			if (i==xMax-1) {
				if (setVisibleCells) {
					this.visibleCells[x][y]=true;
				}
				return true;
			}
		}
		return false;
	} 
	else if (Math.abs(x-this.x)==Math.abs(y-this.y)) {
	// Случай, когда линия образует с осями угол 45 градусов (abs(tg)==1)
		var dMax=Math.abs(x-this.x);
		var dx=x>this.x ? 1 : -1;
		var dy=y>this.y ? 1 : -1;
		var cx=this.x;
		var cy=this.y;
		for (var i=1;i<dMax;i++) {
			cx+=dx;
			cy+=dy;
			if (Terrain.cells[cx][cy].passability==Terrain.PASS_BLOCKED) {
				return false;
			}
			
		}
		if (setVisibleCells) {
			this.visibleCells[x][y]=true;
		}
		return true;
	} 
	else {
	// Общий случай
		var start=[[],[]];
		var end=[];
		// x и y концов соответствуют x и y центра клетки или её ближайшего угла (значения перебираются в цикле по k каждое с каждым)
		end[0]=(x>this.x)? x-0.5 : x+0.5;
		end[1]=(y>this.y)? y-0.5 : y+0.5;
		end[2]=x;
		end[3]=y;
		start[0][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
		start[0][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
		start[1][0]=(x>this.x)? this.x+0.5 : this.x-0.5;
		// start[0][1]=this.y;
		// start[1][0]=this.x;
		start[1][1]=(y>this.y)? this.y+0.5 : this.y-0.5;
		var rays=this.rays(this.x,this.y,x,y);
		jump:
		for (var k=0;k<3;k++) {
			var endNumX=(k==0 || k==1)?0:2;
			var endNumY=(k==0 || k==2)?1:3;
			for (j=0;j<1;j++) {
			// Новый алгоритм расчёта видимости строится на том, есть ли точки, 
			// которые находятся ближе, чем на 0.5 клетки от прямой - косвенный признак того, что прямая пересекает преграду.
			// Преграды в этом случае считаются окружностями с R=0.5 
			// Это не мешает расчёту видимости на стыках клеток по горизонтали.
			// В этом случае нужно сделать максимум шесть проверок (3 цикла по k - точки конца - и два по j - точки начала)
				if (start[j][0]==this.x && start[j][1]==this.y) {
					continue;
				}
				var xEnd=end[endNumX];
				var yEnd=end[endNumY];
				var xStart=start[j][0];
				var yStart=start[j][1];
				for (var i in rays) {
					/* */ // Здесь x|yPoint - глобальные переменные. Пофиксить.
					xPoint=rays[i][0];
					yPoint=rays[i][1];
					if (Terrain.cells[xPoint][yPoint].passability==Terrain.PASS_BLOCKED) {
					// Проверяем каждую клетку
						if (xPoint==this.x && yPoint==this.y || xPoint==x && yPoint==y) {
							continue;
						}
						if (Math.abs(((yStart-yEnd)*xPoint+(xEnd-xStart)*yPoint+(xStart*yEnd-yStart*xEnd))/Math.sqrt(Math.abs((xEnd-xStart)*(xEnd-xStart)+(yEnd-yStart)*(yEnd-yStart))))<=0.5) {
						// Если расстояние до точки не больше 0.5, проверяем следующую из 6 линий
							continue jump;
						}
					}
				}
				if (setVisibleCells) {
					this.visibleCells[x][y]=true;
				}
				return true;
			}
		}
		return false;
	}
};
Character.prototype.ray = function(startX,startY,endX,endY) {
// Вспомогательная функция для this->canSee(). Возвращает клетки линии от xStart:yStart до xEnd:yEnd
	if (startX==endX && startY==endY) {
		return [startX, startY];
	}
	var x = [];
	var y = [];
	x[0]=startX;
	y[0]=startY;
	var L = Math.round(Math.max(Math.abs(endX-x[0]), Math.abs(endY-y[0]))); 
	var dX=(endX-x[0])/L;
	var dY=(endY-y[0])/L;
	for (var i=1;i<L;i++) {
		x[i]=x[i-1]+dX;
		y[i]=y[i-1]+dY;
	}
	x.push(endX);
	y.push(endY);
	var result=[];
	for (i=0;i<=L;i++) {
		result.push([Math.round(x[i]),Math.round(y[i])]);
	}
	return result;
};
Character.prototype.rays = function(startX,startY,endX,endY) {
// Вспомогательная функция для this->canSee
// Возвращает набор координат клеток, которые необходимо проверить для проверки видимости
	return this.ray(startX,startY,endX,endY).concat(
		this.ray(startX,startY+(endY>startY ? 1 : -1),endX+(endX>startX ? -1 : 1),endY),
		this.ray(startX+(endX>startX ? 1 : -1),startY,endX,endY+(endY>startY ? -1 : 1))
	);
};
Character.prototype.showCastSpell = function(spellId, x, y) {
	console["log"](this.name+" casts spell "+spellId+" to "+x+","+y);
	UI.notify("spellCast");
	this.unselectSpell();
	player.spellAimId=-1;
	player.spellX=-1;
	player.spellY=-1;
//	new effectTypes.confuse(this.x, this.y, this.x, this.y,  1000, 1000, 1000, 1000, function() {
//		handleNextEvent();
//	});
	handleNextEvent();
//	weatherEffect = new effectTypes.rain(
//		this.x, this.y, this.x, this.y, 
//		UI.width/2+100, UI.height/2+100, 
//		UI.width/2+100, UI.height/2+100);
	
};
Character.prototype.sendCastSpell = function(spellId, x, y) {
	Net.send({a:Net.CAST_SPELL, spellId:spellId, x:x, y:y});
};
Character.prototype.animateSpell = function(spellId, aimId, spellX, spellY, callback) {
	spells[spellId].effect(this, (aimId==-1)?-1:characters[aimId], spellX, spellY, callback);
};
Character.prototype.showDeath = function() {
	this.cellWrap.parentNode.removeChild(this.cellWrap);
	Terrain.cells[this.x][this.y].passability=Terrain.PASS_FREE;
	if (this === player) {
		UI.notify("death");
		var nlEffects=document.getElementById("effectsList").children;
		while (nlEffects.length>0) {
			nlEffects[0].parentNode.removeChild(nlEffects[0]);
		}
	}
	Terrain.cells[this.x][this.y].character = undefined;
	delete characters[this.characterId];
	handleNextEvent();
};
Character.prototype.distance=function(x,y) {
	return Math.sqrt(Math.pow(this.x-x, 2)+Math.pow(this.y-y, 2));
};
Character.prototype.dodge=function(attacker) {
	var tg=(this.x-attacker.x)/(this.y-attacker.y); // Тангенс
	var character=this;
	var top, left;
	if (this.x>attacker.x) {
		left=10;
	} else if (this.x<attacker.x) {
		left=-10;
	}
	if (this.y>attacker.y) {
		top=10;
	} else if (this.y<attacker.y) {
		top=-10;
	}
	animationsLeft++;
	qanimate(this.cellWrap,[left,top],70,function() {
		qanimate(character.cellWrap,[-left,-top],120,function() {
			animationsLeft--;
			tryRefreshingInterval();
		});
	});
	
	// .animate({top:"+="+top+"px",left:"+="+left+"px"}, 100).animate({top:"+="+(-top)+"px",left:"+="+(-left)+"px"}, 100, false, function() {
		// animationsLeft--;
		// animationsLeft--;
		// tryRefreshingInterval();
	// });
	//this.showAttackResult("dodge");
};
Character.prototype.isBareHanded = function () {
	return this.ammunition.getItemInSlot(0) === undefined;
};
Character.prototype.findEnemy=function(r) {
// Находит ближайшего противника
	var enemy=false;
//	for (var i in characters) {
//		if (this.isEnemy(characters[i]) && this.canSee(characters[i].x, characters[i].y) && (this.aimcharacter==-1 || this.distance(enemy.x, enemy.y)>this.distance(characters[i].x, characters[i].y))) {
//			enemy=characters[i];
//		}
//	}
	return enemy;
};
Character.prototype.getVisibleCells=function() {
// Получить видимые клетки
	if (isLocationPeaceful) {
		return;
	}
	this.prevVisibleCells=this.visibleCells;
	this.visibleCells=blank2dArray();
	this.visibleCells[this.x][this.y]=true;
	
	var minX = Math.max(this.x-this.VISION_RANGE, 0);
	var minY = Math.max(this.y-this.VISION_RANGE, 0);
	var maxX = Math.min(this.x+this.VISION_RANGE, width-1);
	var maxY = Math.min(this.y+this.VISION_RANGE, height-1);
	
	for (var i=minX;i<=maxX;i++) {
		for (var j=minY;j<=maxY;j++) {
			this.canSee(i,j,true);
		}
	}
};
Character.prototype.getPathTable=function(ignorecharacters) {
// Получает таблицу путей по волновому алгоритму
	// Отключено для возможности использования объектов
	// if (Terrain.cells[this.destX][this.destY].passability==Terrain.PASS_BLOCKED) {
		// this.destX=this.x;
		// this.destY=this.y;
	// }
	this.pathTable = blank2dArray();
	var destcharacterCoordX=(this.aimcharacter!=-1)?this.aimcharacter.x:this.x;
	var destcharacterCoordY=(this.aimcharacter!=-1)?this.aimcharacter.y:this.y;
	var isPathFound=false;
	var oldFront=[];
	var newFront=[];
	newFront[0]={x:this.x,y:this.y}; // От какой  клетки начинать отсчёт; было так: newFront[0]=[this.coord];
	for (var i=0;i<height;i++) {
		this.pathTable[i]=[];
	}
	this.pathTable[this.x][this.y]=0;
	var t=0;
	do {
		oldFront=newFront;
		newFront=[];
		if (isPathFound===null) {
			isPathFound=true;
		}
		for (var i=0;i<oldFront.length;i++) {
			// Двигает фронт на восемь доступных сторон от каждой клетки
			var x=oldFront[i].x;
			var y=oldFront[i].y;
			var adjactentX=[x+1,x,  x, x-1, x+1, x+1, x-1, x-1];
			var adjactentY=[y, y-1, y+1, y, y+1, y-1, y+1, y-1];
			for (var j=0;j<8;j++) {
				var thisNumX=adjactentX[j];
				var thisNumY=adjactentY[j];
				
				if (thisNumX<0 || thisNumX>=width || thisNumY<0 || thisNumY>=height || this.pathTable[thisNumX][thisNumY]!=undefined) {
					continue;
				}
				if (thisNumX==this.destX && thisNumY==this.destY) {
					isPathFound = null;
				}
				if (Terrain.cells[thisNumX][thisNumY].passability!=Terrain.PASS_BLOCKED && Terrain.cells[thisNumX][thisNumY].passability!=Terrain.PASS_SEE || Terrain.cells[thisNumX][thisNumY].object && isDoor(Terrain.cells[thisNumX][thisNumY].object.type)/* */ /* && this.seenCells[thisNumX][thisNumY]!=undefined */) {
					this.pathTable[thisNumX][thisNumY]=t+1;
					newFront[newFront.length]={x:thisNumX,y:thisNumY};
				}
			}
		}
		t++;
		if (t>900) {
			throw new Error("long get path table cycle");
		}
	} while (newFront.length>0 && !isPathFound && t<900);
	return t;
};
Character.prototype.showPathTable=function() {
	for (var i=0;i<width;i++) {
		for (var j=0;j<height;j++) {
			if (!this.pathTable[i][j]) {
				continue;
			}
			var num = document.createElement("div");
			num.innerText = this.pathTable[i][j];
			var w = document.createElement("div");
			w.className = "wrap";
			w.style.zIndex=9000;
			
			num.style.backgroundColor="#f4d";
			num.style.opacity = 0.2;
			num.style.width="28px";
			num.style.height="28px";
			num.style.textAlign="center";
			num.style.lineHeight="32px";
			num.style.fontSize="18px";
			num.style.display="inline-block";
			num.style.border="1px solid black";
			var viewIndent = Terrain.getViewIntentation(i,j,32);
			w.style.left = viewIndent.left+"px";
			w.style.top = viewIndent.top+"px";
			w.appendChild(num);
			document.getElementById("gameField").appendChild(w);
			
			
		}
	}
};
Character.prototype.comeTo=function(x,y) {
// Функция следования за character
// Устанавливает номер клетки, в которую этот character должен идти, чтобы прийти к aimcharacter
	if (this.isNear(x, y)) {
		// Стоять на месте, если цель на соседней клетке
		this.destX=this.x;
		this.destY=this.y;
		return false;
	}
	var dists=[x-1, y, x+1, y, x, y-1, x, y+1, x+1, y+1, x-1, y+1, x+1, y-1, x-1, y-1];
	var dist = 9000;
	var destX; // Конечное направление
	var destY; // Конечное направление
	var allPathsAreAvailable=false;
	var destXBuf = this.destX;
	var destYBuf = this.destY;
	for (var i=0;i<8 && !allPathsAreAvailable;i++) {
		this.destX=dists[i*2];
		this.destY=dists[i*2+1];
		if (Terrain.cells[this.destX][this.destY].passability!=Terrain.PASS_FREE) {
			continue;
		}
		var allPathsAreAvailable=true;
//		this.getPathTable();
		for (var j=0;j<8;j++) {
			if (!this.pathTable[dists[i*2]][dists[i*2+1]]) {
				allPathsAreAvailable=false;
				break;
			}
		}
	}
	this.destX = destXBuf;
	this.destY = destYBuf;
	var atLeastOnePath = false; // Найден ли хотя бы один путь. Функция возвращает значение этой переменной
	for (var i=0;i<8;i++) {
		var steps = this.pathTable[dists[i*2]][dists[i*2+1]];
		if (steps && steps<dist && steps>0 && !(dists[i*2]==this.x && dists[i*2+1]==this.y)) {
			atLeastOnePath = true;
			dist = steps;
			destX = dists[i*2];
			destY = dists[i*2+1];
		}
	}
	if (atLeastOnePath) {
		this.destX = destX;		
		this.destY = destY;
	} else {
		this.destX = this.x;
		this.destY = this.y;
	}
	return atLeastOnePath;
};
Character.prototype.hasItem=function(typeId, param) {
// Имеет пресонаж предмет или заданное кол-во предметов
	if (isUnique(typeId)) {
		
	} else {
		if (param==undefined) {
			var param=1;
		}
		for (var i=0;i<this.itemPiles.length;i++) {
			if (this.itemPiles[i][0]==item && this.itemPiles[i][1]>=num) {
				return true;
			}
		}
		return false;
	}
};
Character.prototype.hasEffect=function(effectId) {
// Проверка, имеет ли персонаж определённый эффект
	for (var i in this.effects) {
		if (this.effects[i]==effectId) {
			return true;
		}
	}
	return false;
};
Character.prototype.info=function() {
	gAlert("characterId: "+this.characterId+"<br>Aimcharacter: "+this.aimcharacter+"<br>Координаты: "+this.coord+"<br>Направление: "+this.dest+"<br>Поведение: "+this.behavior+"<br>");
};
Character.prototype.isEnemy=function(aim) {
	var isAlly=false;
	for (var i in area[1]) {
		if (inArray(this.fraction, diplomacy) && inArray(aim.fraction, diplomacy)) {
			isAlly=true;
		}
	}
	if (aim.fraction!=this.fraction && this.fraction!=-1 && aim.fraction!=-1 && !isAlly) {
	// Если найденный character не союзник этому character и если они оба — не нейтральные монстры, то они враги
		return true;
	}
	return false;
};
Character.prototype.isNear = function(x,y) {
	var ableX=Math.abs(this.x-x);
	var ableY=Math.abs(this.y-y);
	if ((ableX==1 && ableY==0) || (ableY==1 && ableX==0) || (ableY==1 && ableX==1)) {
		return true;
	}
	return false;
};
Character.prototype.leaveLocation=function() {
	Net.send({leaveLocation:1},function(data) {
		onGlobalMap=true;
		leaveLocation();
	});
	this.leavesArea=false;
};
Character.prototype.loseItem = function(typeId, param) {
	if(isUnique(typeId)) {
		this.items.removeUnique(param);
	} else {
		this.items.removePile(typeId, param);
	}
	UI.notify("inventoryChange");
};
Character.prototype.getItem = function(typeId, param) {
	this.items.addItem(typeId, param);
	UI.notify("inventoryChange");
};
Character.prototype.meleeAttack=function(x,y) {
	animationsLeft++;
	var sideX=x-this.x;
	var sideY=y-this.y;
	var left=sideX*8;
	var top=sideY*8;
	keyPressForbidden+=1;
	var cellWrap=this.cellWrap;
	var character=this;
	// Анимация атаки
	qanimate(cellWrap,[left,top], 100, function() {
		qanimate(cellWrap,[-left,-top], 100, function() {
			keyPressForbidden-=1;
			animationsLeft--;
			tryRefreshingInterval();
		});
	});
};
Character.prototype.idle=function() {	
	Net.send({a:Net.IDLE});
};
Character.prototype.sendMove=function() {
// Графическая и логическая обработка движения
	if (this.x<0) {
		throw new Error("dest x < 0");
	}
	this.placeSprite();
	var num=this.characterId;
	if (this.x!=this.destX || this.y!=this.destY) {
		this.getPathTable();
//		if (
//			Terrain.cells[this.destX][this.destY].passability!=Terrain.PASS_FREE && 
//			!(Terrain.cells[this.destX][this.destY].passability==Terrain.PASS_SEE && !this.canSee(this.destX, this.destY))
//		) {
//		// Если клетка не свободна и там не находится персонаж, которого этот не может видеть
//			this.destX=this.x;
//			this.destY=this.y;
//		}
		Terrain.cells[this.x][this.y].passability=Terrain.PASS_FREE;
		if (this.isClientPlayer) {
		// Для игрока
//			CellCursor.prototype.hideAllCursors();
//			this.showPath(this.destX, this.destY);
//			return;
//			try {
				var path = this.getPath(this.destX, this.destY);
//			} catch(e) {
//				console("Get path error: "+this.characterId);
//			}
			if (path.length == 0) {
				this.destX = this.x;
				this.destY = this.y;
				return false;
			}
			var nextCellX=path[0].x; 
			var nextCellY=path[0].y;
			if (Terrain.cells[nextCellX][nextCellY].passability==Terrain.PASS_BLOCKED && Terrain.cells[nextCellX][nextCellY].object && isDoor(Terrain.cells[nextCellX][nextCellY].object.type)) {
				player.addActionToQueue(player.sendMove);
				this.sendUseObject(nextCellX, nextCellY);
				return true;
			}
			var animateDest=[];
			
			var dir;
			if (nextCellX-this.x==0 && nextCellY-this.y==-1) {
				dir=0;
			} else if (nextCellX-this.x==1 && nextCellY-this.y==-1) {
				dir=1;
			} else if (nextCellX-this.x==1 && nextCellY-this.y==0) {
				dir=2;
			} else if (nextCellX-this.x==1 && nextCellY-this.y==1) {
				dir=3;
			} else if (nextCellX-this.x==0 && nextCellY-this.y==1) {
				dir=4;
			} else if (nextCellX-this.x==-1 && nextCellY-this.y==1) {
				dir=5;
			} else if (nextCellX-this.x==-1 && nextCellY-this.y==0) {
				dir=6;
			} else if (nextCellX-this.x==-1 && nextCellY-this.y==-1) {
				dir=7;
			} else {
				throw new Error("error getting dir from "+player.x+":"+player.y+" to "+this.destX+":"+this.destY);
			}
			Net.send({a:Net.MOVE,dir:dir});
		} else {
		// Для мобов и других игроков
			var character=this;
			var animateDest=new Array();
			var top=0;
			var left=0;
			/* ? */ // var diff=this.coord-nextCell; 
			if (this.y-this.destY==1) {
				var top=-32;
			} else if (this.y-this.destY==-1) {
				var top=32;
			} 
			if (this.x-this.destX==1) {
				var left=-32;
			} else if (this.x-this.destX==-1) {
				var left=32;
			}
			var viewIndent = Terrain.getViewIntentation(x/32,y/32,32);
			if ((this.x!=this.destX || this.y!=this.destY) && !player.canSee(this.x,this.y) && player.canSee(this.destX,this.destY)) {
				this.cellWrap.style.display="block";
			}
			if (+localStorage.getItem(1) && player.canSee(this.x, this.y)) {
			// Анимировать...
				animationsLeft++;
				qanimate(character.cellWrap,[viewIndent.left,viewIndent.top], 100, function() {
				// Скрыть персонажа, если он ушёл из поля видимости
					// if ((character.x!=character.destX || character.y!=character.destY) && player.canSee(character.x,character.y) && !player.canSee(character.destX,character.destY)) {
						// character.cellWrap.style.display="none";
					// }
					animationsLeft--;
					character.x=character.destX;
					character.y=character.destY;
					Terrain.cells[character.x][character.y].passability=Terrain.PASS_SEE;
					setTimeout(function() {
						tryRefreshingInterval();
					},10);
				});
			} else {
			// Или просто переместить
				var style=this.cellWrap.style;
				style.top=parseInt(style.top)+top+"px";
				style.left=parseInt(style.left)+left+"px";
				// if ((this.x!=this.destX || this.y!=this.destY) && player.canSee(this.x,this.y) && !player.canSee(this.destX,this.destY)) {
					// this.cellWrap.style.display="none";
				// }
				this.x=this.destX;
				this.y=this.destY;
				Terrain.cells[this.x][this.y].passability=Terrain.PASS_SEE;
				setTimeout(function() {
					tryRefreshingInterval();
				},10);
			}
			this.cellWrap.style.zIndex=this.destY*2+2;
		}
	}
};
Character.prototype.showMove = function(nextCellX, nextCellY) {
	var top = 0;
	var left = 0;
	Terrain.cells[this.x][this.y].character = undefined;
	Terrain.cells[this.x][this.y].passability = Terrain.PASS_FREE;
	
	var viewIndent = Terrain.getViewIndentation(nextCellX, nextCellY, 1);
	
	if (+localStorage.getItem(1)) {
		animationsLeft++;
		var character = this;
		qanimate(this.cellWrap,[viewIndent.left,viewIndent.top], 75, function() {
			animationsLeft--;
			character.cellWrap.style.zIndex=character.y*2+2;
			moveGameField(this.x, this.y);
			this.x=nextCellX; 
			this.y=nextCellY;
			Terrain.cells[this.x][this.y].passability = Terrain.PASS_SEE;
			Terrain.cells[this.x][this.y].character = this;
			renderView();
		});
	} else {
		this.cellWrap.style.top=viewIndent.top*32+"px";
		this.cellWrap.style.left=viewIndent.left*32+"px";
		this.cellWrap.style.zIndex = viewIndent.top*2 + 2;
		this.x = nextCellX; 
		this.y = nextCellY;
		Terrain.cells[this.x][this.y].passability = Terrain.PASS_SEE;
		Terrain.cells[this.x][this.y].character = this;
		if (this.visible && !player.visibleCells[this.x][this.y]) {
			this.hideModel();
		} else if (!this.visible && player.visibleCells[this.x][this.y]) {
			this.showModel();
		}
		if (this.isClientPlayer) {
			moveGameField(this.x, this.y);
			this.updateVisibility();
			this.showLoot();
			renderView();
		}
		for (var i in this.effects) {
			var viewIndent = Terrain.getViewIndentation(this.x, this.y, 1);
			this.effects[i].move(viewIndent.left, viewIndent.top);
		}
		handleNextEvent();
	}	
};
Character.prototype.getPath=function(destX,destY) {
// Получить путь до клетки в виде массива координат (0 - первый шаг и т. д.)
	
	if (destX==undefined || destY==undefined) {
		destX=this.destX;
		destY=this.destY;
	}
	if (destX==this.x && destY==this.y) {
		throw new Error("Gets path to its own x:y");
	}
	if (this.isNear(destX,destY)) {
		return [{x:destX,y:destY}];
	}
	
	var bufX=this.destX;
	var bufY=this.destY;
//	this.destX=destX;
//	this.destY=destY;
	var path=[];
	// Нахождение пути
	var currentNumX=destX;
	var currentNumY=destY;
	var x=currentNumX;
	var y=currentNumY;
	/* */var diff=[-width, 1, width, -1];
	var t=0;
	for (var j=this.pathTable[currentNumX][currentNumY];j>0;j=this.pathTable[currentNumX][currentNumY]) {
	// Счётчик: от кол-ва шагов до клетки dest до начальной клетки (шаг 1)
		path.push({x:currentNumX,y:currentNumY});
		var diff=[-width, 1, width, -1, width+1, -width+1, width-1, -width-1];
		var adjactentX=[x, x+1, x, x-1, x+1, x+1, x-1, x-1];
		var adjactentY=[y-1, y, y+1, y, y+1, y-1, y+1, y-1];
		for (var i=0;i<8;i++) {
		// Для каждой из доступных сторон (С, Ю, З, В)
			var thisNumX=adjactentX[i];
			if (thisNumX<0 || thisNumX>=width) { continue; }
			var thisNumY=adjactentY[i];
			if (thisNumY<0 || thisNumY>=height) { continue; }
			if (this.pathTable[thisNumX][thisNumY]==j-1 && !(currentNumX==0 && thisNumX==currentNumX-1 || currentNumX==width-1 && thisNumX==currentNumX+1)) {
			// Если клетка в этой стороне является предыдущим шагом, перейти на неё
				currentNumX=adjactentX[i];
				currentNumY=adjactentY[i];
				x=currentNumX;
				y=currentNumY;
				break;
			}
		}
		t++;
		if (t==900) {
			console["log"]("get path: exit with error");
			break;
		}
	}
	return path.reverse();
};
Character.prototype.showPath = function(x,y) {
	throw new Error("NOT AVAILABLE METHOD");
	this.getPathTable();
	this.destX = x;
	this.destY = y;
	var path = this.getPath();
	for (var i in path) {
	}
};
Character.prototype.placeSprite = function(x,y) {
// Разместить спрайт персонажа на конкретной клетке
	if (x===undefined) {
		x=this.x;
		y=this.y;
	}
	var viewIndent = Terrain.getViewIndentation(x,y,1);
	this.cellWrap.style.left=viewIndent.left*32+"px";
	this.cellWrap.style.top=viewIndent.top*32+"px";
	this.cellWrap.style.zIndex=viewIndent.top*2+1;
};
Character.prototype.sendPickUp = function(item) {
	if (item.isUnique) {
		Net.send({a:Net.PICK_UP_UNIQUE, itemId:item.itemId});
	} else {
		Net.send({a:Net.PICK_UP_PILE, typeId:item.typeId, amount:item.amount});
	}
};
Character.prototype.showPickUp = function(typeId, param) {
	UI.notify("inventoryChange");
	this.showLoot();
	handleNextEvent();
};
Character.prototype.sendPutOn = function(itemId) {
// Send putting on data to server
	Net.send({a:Net.PUT_ON,itemId:itemId});
};
Character.prototype.showPutOn = function(itemId) {
// Process putting on data sent from server and show it in inventory
//	if (!this.items[itemId]) {
//		throw new Error("Player is trying to put on item "+itemId+" that he doesn't have");
//	}
	// Determine the slot in ammunition for the item
	var item = this.items.getUnique(itemId);
	var slot = getSlotFromClass(items[item.typeId][1]);
	if (slot == 9 && this.ammunition.getItemInSlot(9)) {
		slot = 10;
	}
	this.ammunition.putOn(item);
	
	// Change player's inventory contents and delete if he has no such items anymore.
	this.items.removeUnique(itemId);

	// Show player's items
	if (this.isClientPlayer) {
		UI.notify("inventoryChange");
	}
	this.showAmmunition();
	handleNextEvent();
};
Character.prototype.sendDrop = function(item) {
	if (this.items.hasItem(item)) {
		if (item.isUnique) {
			Net.send({a:Net.DROP_UNIQUE,typeId:item.typeId,itemId:item.itemId});
		} else {
			Net.send({a:Net.DROP_PILE,typeId:item.typeId,amount:item.amount});
		}
	} else {
		throw new Error("Player "+this.name+" has no items of type "+item.typeId);
	}
};
Character.prototype.showDrop = function(typeId, param) {
	this.showLoot();
	UI.notify("inventoryChange");
	handleNextEvent();
};
Character.prototype.sendShootMissile = function(x, y, missile) {
	Net.send({a:Net.SHOOT_MISSILE,x:x,y:y,missile:missile});
};
Character.prototype.sendOpenContainer = function() {
	Net.send({a:Net.OPEN_CONTAINER,x:Global.container.x,y:Global.container.y});
};
Character.prototype.sendTakeFromContainer = function(typeId, param) {
	Net.send({
		a:Net.TAKE_FROM_CONTAINER,
		typeId:typeId,
		param:param,
		x:Global.container.x,
		y:Global.container.y
	});
};
Character.prototype.showTakeFromContainer = function(typeId, param) {
	if (this.isClientPlayer) {
		Global.container.items.remove(typeId, param);
		UI.notify("containerChange");
	}
	handleNextEvent();
};
Character.prototype.sendPutToContainer = function(typeId, param) {
	Net.send({
		a:Net.PUT_TO_CONTAINER,
		typeId:typeId,
		param:param,
		x:Global.container.x,
		y:Global.container.y
	});
};
Character.prototype.showPutToContainer = function(typeId, param) {
	if (this.isClientPlayer) {
		Global.container.items.addItem(typeId, param);
		UI.notify("containerChange");
	}
	handleNextEvent();
};
Character.prototype.sendUseObject=function(x,y) {
// Использовать объект на глобальной карте (например, дверь)
	Net.send({a:Net.USE_OBJECT,x:x,y:y});
};
Character.prototype.showUseObject=function(x,y) {
// Использовать объект на глобальной карте (например, дверь)
	handleNextEvent();
};
Character.prototype.showAttackResult=function(res) {
	 this.graphicEffect("blood",function() {
		 handleNextEvent();
	 });
};
Character.prototype.showAmmunition=function() {
// Отобразить амуницию
// prevAmmunition - предыдущее значение this.ammunition
	if (this.isClientPlayer) {
		UI.notify("ammunitionChange");
	}
	// Отрисовка куклы
	if (player.canSee(this.x,this.y)) {
		this.doll.draw();
	}
};
Character.prototype.initHpBar=function() {
// Creates HP bar element
	this.nHpBar = document.createElement("div");
	this.nHpBar.className = "cellHp";
	document.getElementById("character"+this.characterId).appendChild(this.nHpBar);
	if (this.type in characterSpriteSizes) {
		this.nHpBar.style.top=((16-characterSpriteSizes[this.type][1]))+"px";
	}
	this.nHpBar.className = "cellHp";
	this.refreshHpBar();
};
Character.prototype.hideHpBar=function() {
	this.nHpBar.display="none";
};
Character.prototype.showHpBar=function() {
	this.nHpBar.display="block";
};
Character.prototype.refreshHpBar = function () {
// Adjusts HP bar length and color.
//	if (this.hp==this.maxHp) {
//	// If HP is max then hide bar
//		this.hideHpBar();
//		return false;
//	} else {
	// Else change it's length and color
		var w=((BARwidth/this.maxHp*this.hp>=BARwidth)? BARwidth : Math.ceil(BARwidth/this.maxHp*this.hp));
		this.nHpBar.style.width=w+"px";
		this.nHpBar.style.borderRight=(BARwidth-w)+"px solid #000";
		// Colors of HP bar
		if (w<=BARwidth/4) {
		// Very low hp
			this.nHpBar.style.backgroundColor="#ff2400";
		}  else if (w<=BARwidth/2) {
		// Low hp
			this.nHpBar.style.backgroundColor="#e6dc0d";
		} else {
		// Normal hp
			this.nHpBar.style.backgroundColor="#34c924";
		}
//	}
};
Character.prototype.refreshMpBar = function() {
	document.getElementById("barsMpValue").innerHTML=this.mp+"/"+this.maxMp;
	document.getElementById("barsMpStrip").style.width=(106*this.mp/this.maxMp)+"px";
};
Character.prototype.showTakeOff = function(itemId) {
// Снимает предмет в слоте slot
// Список слотов см. в items.js
	var slot = 0;
	for (; slot < 10; slot++) {
		var item = this.ammunition.getItemInSlot(slot);
		if (item && item.itemId == itemId) {
			break;
		}
	}
	if (slot == 10) {
		throw new Error("Not found item "+itemId+" in ammunition");
	}
	if (!this.ammunition.hasItemInSlot(slot)) {
		throw new Error("Character "+this.name+" is trying to take off an item that he doesn't wear");
		return false;
	}
	if (this.isClientPlayer) {
		this.items.addItem(this.ammunition.getItemInSlot(slot));
	}
	this.ammunition.takeOffFromSlot(slot);
	if (this.isClientPlayer) {
		UI.notify("inventoryChange");
	}
	this.showAmmunition();
	handleNextEvent();
};
Character.prototype.sendTakeOff = function(item) {
	Net.send({a:Net.TAKE_OFF,itemId:item.itemId});
};
Character.prototype.showModel=function() {
	this.cellWrap.style.opacity="1";
	this.visible = true;
	for (var i in this.effects) {
		this.effects[i].resume();
	}
};
Character.prototype.hideModel=function() {
	this.cellWrap.style.opacity="0";
	this.visible = false;
	this.hideHpBar();
	for (var i in this.effects) {
		this.effects[i].pause();
		this.effects[i].clear();
	}
};
Character.prototype.graphicEffect=function(name, callback) {
// Графический эффект
	// if (!+localStorage.getItem(1)) {
	// // Если графические эффекты отключены, ничего не делать
		// return false;
	// }
	new effectTypes[name](this.x,this.y,this.x,this.y,100,100,100,100, callback);
//	graphicEffects[name].call(this, callback);
};
Character.prototype.addEffect=function(effectId) {
	this.effects[lastFreeElem(this.effects)]=effectId;
	if (this.isClientPlayer) {
		var nEffect=document.createElement("div");
		nEffect.className="effect";
		nEffect.setAttribute("effectId",effectId);
		nEffect.style.color="#"+effects[effectId][1];
		nEffect.innerHTML=effects[effectId][0];
		document.getElementById("effectsList").appendChild(nEffect);
	}	
};
Character.prototype.removeEffect=function(effectId) {
	var nlEffects=document.getElementById("effectsList").children;
	for (var i=0;i<nlEffects.length;i++) {
		if (nlEffects[i].getAttribute("effectId")==effectId) {
			nlEffects[i].parentNode.removeChild(nlEffects[i]);
		}
	}
	for (var i in this.effects) {
		if (this.effects[i]==effectId) {
			this.effects[i]=undefined;
			break;
		}
	}
};
Character.prototype.showLoot=function() {
	// if (this.characterId!=0) {
		// return false;
	// }	
	UI.notify("lootChange");
};
Character.prototype.findCharacterByCoords=function(x,y) {
	for (var i in characters) {
		if (characters[i].x==x && characters[i].y==y) {
			return characters[i];
		}
	}
	return false;
};
Character.prototype.cellChooseAction = function() {
// Совершить действие на координате под курсором
	var x=CellCursor.x;
	var y=CellCursor.y;
	if (!player.canSee(x,y)) {
		gAlert("Игрок не видит целевой клетки!");
	} else {
		if (player.spellId!=-1) {
		// Spell
			player.sendCastSpell(player.spellId, x, y);
		} else {
		// Ranged attack
			var aim = Character.prototype.findCharacterByCoords(x,y);
			if (aim) {
			// On character
				player.sendAttack(aim.characterId, !player.ammunition.getItemInSlot(0).isMelee());
			} else {
			// On cell
				player.sendShootMissile(x, y, 2300);
			}
		}
		CellCursor.character=Character.prototype.findCharacterByCoords(x,y);
		UI.setMode(UI.MODE_DEFAULT);
	}
};
Character.prototype.showMissileFlight = function(fromX, fromY, toX, toY, missile) {
	this.unselectMissile();
	
	var startViewIndent = Terrain.getViewIndentation(fromX, fromY, 1);
	var endViewIndent = Terrain.getViewIndentation(toX, toY, 1);
	fromX = startViewIndent.left;
	fromY = startViewIndent.top;
	toX = endViewIndent.left;
	toY = endViewIndent.top;
	animationsLeft++;
	animationsLeft++; // Так надо: второй раз - для анимации снаряда
	var character=this;
	var a={x:fromX, y:fromY};
	var b={x:toX, y:toY};
	var mult=(b.x-a.x!=0)? (b.y-a.y)/(b.x-a.x) : "zero"; 
	var top=0;
	var left=0;
	if (mult<=-4 || mult>=4 || mult=="zero") {
		top=(b.y>a.y) ? 8 : -8;
		var arrowDest = (b.y>a.y) ? 5 : 1;
	} else if (mult<=4 && mult>=0.25) {
		top=(b.y>a.y) ? 8 : -8;
		left=(b.x>a.x) ? 8 : -8;
		var arrowDest = (b.y>a.y) ? 4 : 8;
	} else if (mult<=0.25 && mult>=-0.25) {
		left=(b.x>a.x) ? 8 : -8;
		var arrowDest=(b.x>a.x) ? 3 : 7;
	} else if (mult>=-4 && mult<=-0.25) {
		top=(b.y>a.y) ? 8 : -8;
		left=(b.x>a.x) ? 8 : -8;
		var arrowDest=(b.y>a.y) ? 6 : 2;
	}
	var num=this.characterId;
	
	var thischaracter=this;
//	qanimate(thischaracter.cellWrap,[left,top],100,function() {
//	// Анимируем персонажа
//		qanimate(thischaracter.cellWrap,[-left,-top],100,function() {
//			animationsLeft--;
//			tryRefreshingInterval();
//		});
//	});
	
	var nWrap=document.createElement("div");
	nWrap.setAttribute("characterId",this.characterId);
	nWrap.className="wrap";
	
	var nImg=document.createElement("img");
	
	
	nImg.setAttribute("src","./images/ranged/arrow.png");
	nImg.className="arrow";
	nImg.style.top="0px";
	nImg.style.left="0px";
	var tan=((toY-fromY)/(toX-fromX)).toFixed(3);
	var sideMod=(toX>=fromX)?0:1;
	nImg.style.webkitTransform="rotate("+((Math.atan(tan)+Math.PI/2)+sideMod*Math.PI)+"rad)";
	
	nWrap.appendChild(nImg);		
	nWrap.style.top=(fromY*32-16)+"px";
	nWrap.style.left=(fromX*32)+"px";
	nWrap.style.zIndex=fromY+2;
	gameField.appendChild(nWrap);
	// Анимируем снаряд
	qanimate(nImg,[(b.x-a.x)*32,(b.y-a.y)*32],distance(fromX,fromY,toX,toY)*70,function() {
		nImg.parentNode.removeChild(nImg);
//		if (callback) {
//			callback();
//		}
		animationsLeft--;
		handleNextEvent();
	});
};
Character.prototype.addActionToQueue=function(func, params) {
// Add a function to the queue. When the client is informed by server that it is client's turn,
// the client will do the first queued action, if it has any.
// params: array of arguments
	if (!(params instanceof Array)) {
		if (params === undefined) {
			params = [];
		} else {
			throw new Error("Incorrect params for queued action: "+params+" , queued action:",func);
		}
	}
	this.actionQueue.push(func);
	this.actionQueueParams.push(params);
};
Character.prototype.doActionFromQueue = function() {
	this.actionQueue[0].apply(player, this.actionQueueParams[0]);
	this.actionQueue.splice(0, 1);
	this.actionQueueParams.splice(0, 1);
};
Character.prototype.showVisibleCells = function() {
//	for (var i=0;i<width;i++) {
//		for (var j=0;j<height;j++) {
//			if (!this.visibleCells[i][j]) {
//				Terrain.cells[i][j].shade();
//			}
//		}
//	}
};
Character.prototype.updateVisibility = function() {
	if (isLocationPeaceful) {
		return;
	}
	this.getVisibleCells();
	for (var i=0;i<width;i++) {
		for (var j=0;j<height;j++) {
			if (this.visibleCells[i][j] && !this.prevVisibleCells[i][j]) {
				if (this.seenCells[i][j]) {
					Terrain.cells[i][j].unshade();
				} else {
					this.seenCells[i][j] = true;
					Terrain.cells[i][j].show();
				}
			} else if (this.prevVisibleCells[i][j] && !this.visibleCells[i][j]) {
				Terrain.cells[i][j].shade();
			}
		}
	}
};
Character.prototype.initVisibility = function() {
	this.getVisibleCells();
	for (var i=0;i<width;i++) {
		for (var j=0;j<height;j++) {
			if (this.visibleCells[i][j]) {
				Terrain.cells[i][j].show();
				this.seenCells[i][j] = true;
			}
		}
	}
	if (isLocationPeaceful) {
		for (var i=0;i<width;i++) {
			for (var j=0;j<height;j++) {
				if (!Terrain.cells[i][j].visible) {
					player.seenCells[i][j] = true;
					player.visibleCells[i][j] = true;
					Terrain.cells[i][j].show();
				}
			}
		}
	} else {
		
	}
};
Character.prototype.showSpeech = function (message) {
	var bg = document.createElement("div");
	var text = document.createElement("div");
	var wrap = document.createElement("div");
	var wrap2 = document.createElement("div");
	wrap.className = "wrap";
	wrap2.className = "wrap";
	bg.className = "speechBubbleBg";
	text.className = "speechBubbleText";
	text.innerText = message;	
	
	wrap.style.zIndex = 9000;
	wrap2.style.zIndex = 9000;
	text.style.zIndex = 1;
	
	wrap2.appendChild(text);
	wrap.appendChild(wrap2);
	wrap.appendChild(bg);	
	
	
	gameField.appendChild(wrap);
	bg.style.height = text.clientHeight - 8 + "px";
	bg.style.width = text.clientWidth - 8 + "px";
	wrap.style.top = (32*this.y-text.clientHeight-12) + "px";
	wrap.style.left = (32*this.x-text.clientWidth/2+16) + "px";
	wrap.onclick = handlers.speechBubble.click;
	wrap.onmouseover = handlers.speechBubble.mouseover;
	wrap.onmouseout = handlers.speechBubble.mouseout;
	wrap.setAttribute("isMouseOver", "0");
	wrap.setAttribute("time", new Date().getTime());
	setTimeout(function() {
		if (wrap.getAttribute("isMouseOver") == "0") {
			gameField.removeChild(wrap);
			return false;
		}
	}, 2000);
};
Character.prototype.showDialoguePoint = function(characterId, point) {
// point: {phrase: string, answers: [string]}
	UI.notify("dialoguePointRecieve", point);
};
Character.prototype.sendStartConversation = function(characterId) {
	Net.send({a:Net.START_CONVERSATION,characterId:characterId});
};
Character.prototype.sendAnswer = function(answerId) {
	Net.send({a:Net.ANSWER,answerId:answerId});
};
Character.prototype.showEffectStart = function(effectId) {
	this.effects[effectId] = new effectTypes[effectId](this.x, this.y, this.x, this.y,  100, 100, 100, 100);
};
Character.prototype.showEffectEnd = function(effectId) {
	this.effects[effectId].markForDestruction();
	delete this.effects[effectId];
};
Character.prototype.selectSpell = function(spellId) {
	this.spellId = spellId;
	UI.setMode(UI.MODE_CURSOR_ACTION);
	UI.notify("spellSelect");
	CellCursor.changeStyle("CellAction");
};
Character.prototype.unselectSpell = function() {
	UI.notify("spellUnselect");
	this.spellId = -1;
	UI.setMode(UI.MODE_DEFAULT);
	CellCursor.changeStyle("Main");
};
Character.prototype.selectMissile = function() {
	if (player.ammunition.getItemInSlot(0) && isRanged(player.ammunition.getItemInSlot(0).typeId)) {
		var aimcharacter;
		if (aimcharacter=player.findEnemy()) {
			CellCursor.move(aimcharacter.x,aimcharacter.y);
		} else {
			CellCursor.move(player.x,player.y);
		}
		UI.setMode(UI.MODE_CURSOR_ACTION);
		UI.notify("missileSelect");
		CellCursor.changeStyle("CellAction");
	} else {
		UI.notify("alert","Игрок не держит в руках оружия дальнего боя!");
	}
};
Character.prototype.unselectMissile = function() {
	UI.notify("missileUnselect");
	UI.setMode(UI.MODE_DEFAULT);
	CellCursor.changeStyle("Main");
};
Character.prototype.changeAttribute = function _(attrId, value) {
	this.attributes[attrId] = value;
	UI.notify("attributeChange", [attrId, value]);
};
function Player(data) {
/*	
	data : [(0)characterId, (1)worldX, (2)worldY, (3)isLead, (4)name, (5)race, (6)class, 
			(7)maxHp, (8)maxMp, (9)maxEp, (10)hp, (11)mp, (12)ep, 
			(13)str, (14)dex, (15)wis, (16)itl, (17)items[], (18)ammunition[], (19)spells[], (20)skills[],
			(21)ac, (22)ev, (23)resistances[]] 
*/
	Character.apply(this, [data[0],"player",data[1], data[2],1]);
	window.player = this;
	characters[data[0]] = this;
	this.isClientPlayer = true;
	if (onGlobalMap) {
		this.worldX = data[1];
		this.worldY = data[2];
	} else {
		this.x = data[1];
		this.y = data[2];
	}
	UI.notify("titleChange");
	this.isPartyLeader = data[3];
	this.name = data[4];
	this.race = data[5];
	this.cls = data[6];
	this.maxHp = data[7];
	this.maxMp = data[8];
	this.maxEp = data[9];
	this.hp = data[10];
	this.mp = data[11];
	this.ep = data[12];
	this.attributes={};
	this.attributes.str = data[13];
	this.attributes.dex = data[14];
	this.attributes.wis = data[15];
	this.attributes.itl = data[16];
	this.attributes.armor = data[21];
	this.attributes.evasion = data[22];
	this.attributes.fireRes = data[23][0];
	this.attributes.coldRes = data[23][1];
	this.attributes.poisonRes = data[23][2];
	

	
	this.pathTable=blank2dArray();
	this.items = new ItemMap();
	this.spells = [];
	this.visibleCells=blank2dArray();
	this.prevVisibleCells=blank2dArray();
	this.seenCells=blank2dArray();
	this.skills=[];
	this.actionQueue = [];
	this.actionQueueParams = [];
	
	// Inventory	
	for (var i=0;i<data[17].length;i++) {
		var typeId = data[17][i][0];
		var param = data[17][i][1];
		if (isUnique(typeId)) {
			this.items.addItem(new UniqueItem(typeId, param));
		} else {
			this.items.addItem(new ItemPile(typeId, param));
		}
	}
	UI.notify("inventoryChange");
	
	// Ammunition
	for (var slot in data[18]) {
		if (slot==9 && this.ammunition.hasItemInSlot(9)) {
			slot=10;
		}
		this.ammunition.putOnToSlot(slot, new UniqueItem(data[18][slot][0], data[18][slot][1]));
	}
	// Spells
	player.spells = data[19];
	// Skills
	var len=data[20].length/2;
	player.skills=[];
	for (var i=0;i<len;i++) {
		player.skills.push(data[20][i*2]);
		player.skills.push(data[20][i*2+1]);
	}
	UI.notify("attributesInit");
	this.showLoot();
	UI.notify("inventoryChange");
//	this.showAmmunition();
}
Player.prototype = new Character(-1);
Player.prototype.display = function () {
	this.doll = new Doll(this);
	this.doll.draw();
	document.getElementById("character"+this.characterId).appendChild(this.doll.DOMNode);
	this.initHpBar();
};
Player.prototype.showDamage = function (amount, type) {
	Character.prototype.showDamage.apply(this, arguments);
	UI.notify("healthChange");
};