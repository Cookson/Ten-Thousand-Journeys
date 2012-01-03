if (window.console===undefined) {
	window.console={
		log:function() {
			return false;
		}
	};
}
HTMLElement.prototype.applyStyle = function _(style) {
	for (var i in style) {
		this.style[i] = style[i];
	}
};
HTMLElement.prototype.addClass = function(className) {
	if (this.className == "") {
		this.className = className;
	} else {
		this.className += " "+className;
	}
};
HTMLElement.prototype.setData = function _(key, value) {
	if (typeof this.data === "undefined") {
		this.data = {};
	}
	return this.data[key] = value;
};
HTMLElement.prototype.getData = function _(key) {
	if (typeof this.data === "undefined" || typeof this.data[key] === "undefined") {
		throw new Error("No data at key "+key+" in element");
	}
	return this.data[key];
};
window.onresize=recountWindowSize;
window.onunload=function () {
// Разлогинивание персонажа при перезагрузке, если перезагрузка - не quickRefresh
	if (localStorage.getItem(101)=="0") {
		Net.send({a:Net.DEAUTH});
	}
};
function recountWindowSize() {
	var nGameZone = document.getElementById("intfGameZone");
	var nWindowWrap = document.getElementById("intfWindowWrap");
	document.getElementById("stLoad").style.lineHeight = document.body.clientHeight+"px";
	
	// Центрировать игровое окно по вертикали
	nWindowWrap.style.marginTop=(document.body.clientHeight-nWindowWrap.clientHeight)/2+"px";
	nWindowWrap.style.marginLeft=(document.body.clientWidth-nWindowWrap.clientWidth)/2+"px";
	// nGameZone.style.height=(nWindowWrap.clientHeight)+"px";
	// nGameZone.style.width=(nWindowWrap.clientWidth-nIntfInfo.clientWidth)-(nWindowWrap.clientWidth-nIntfInfo.clientWidth%32)+"px";
	// document.getElementById("intfBorder").style.width=(nWindowWrap.clientWidth-nGameZone.clientWidth-nIntfInfo.clientWidth-4)+"px";
	var l=0;
	UI.width = nWindowWrap.clientWidth;
	UI.width = UI.width-UI.width%32;
	UI.height = nWindowWrap.clientHeight;
//	nGameZone.style.width=UI.width+"px";
//	nGameZone.style.height=UI.height+"px";
	
	var w=Math.floor((UI.visibleWidth)/32);
	var h=Math.floor((UI.visibleHeight)/32);
	rendW=((w%2==1)?w:w+1)+2;
	rendH=((h%2==1)?h:h+1)+2;
	prevRendCX=-1;
	prevRendCY=-1;
	
	if (!inMenu) {
	// Если в игру уже зашли
	// player - для случаев, когда окно ресайзится до самого первого хода в локации (когда rendCX|Y==-1)
		moveGameField(rendCX==-1 ? player.x : rendCX, rendCY==-1 ? player.y : rendCY, true);
	}
	resizeTimeout=null;		
	
	// Center centered windows
	for (var i=0; i<gameWindows.length; i++) {
		if (gameWindows[i].centered) {
			gameWindows[i].center();
		}
	}
}
function blank2dArray(i) {
	var w = (i==undefined) ? width : i;
	var arr = [];
	for (var i=0; i<w; i++) {
		arr[i] = [];
	}
	return arr;
}
function distance(startX, startY, endX, endY) {
	return Math.sqrt(Math.pow(startX-endX, 2)+Math.pow(startY-endY, 2));
}
function getNum(x, y) {
	return parseInt(y)*width+parseInt(x);
}
function getX(num) {
	return num%width;
}
function getY(num) {
	return (num-num%width)/width;
}
function startTimer() {
	clearConsole();
	timerStart=new Date();
	timerStart=timerStart.getMilliseconds();
}
function qanimate(obj, dest, spd, cbk, top, left) {
// Функция быстрой анимации: объект, направление (объект со значениями top и left в пикселях), скорость, callback, позиция элемента (служебные аргументы)
// ВНИМАНИЕ: у объекта должны быть заранее установлены css-параметры top и left (0 по умолчанию), иначе функция не работает правильно!
// ВАЖНО: Функции cbk передаётся объект obj, поэтому можно работать с тем же объектом в callback
/* Например: 
	qanimate(obj,[x,y],time,function(sameObj){
		qanimate(sameObj,[x,y],time2);
	})
*/	
	var start=new Date().getTime();
	if (top===undefined) {
		top=parseFloat(obj.style.top);
		left=parseFloat(obj.style.left);
	}
	var counter=Math.ceil(fps/1000*spd);
	top=top+dest[1]/counter;
	left=left+dest[0]/counter;
	obj.style.top=(top)+"px";
	obj.style.left=(left)+"px";
	dest[1]-=dest[1]/counter;
	dest[0]-=dest[0]/counter;
	spd-=spd/counter;
	counter--;
	if (counter>0) {
		setTimeout(function() { 
			qanimate(obj,dest,spd,((cbk==undefined)?undefined:cbk),top,left);
		}, spd/counter-(new Date().getTime()-start));
	} else if (cbk!=undefined) {
		cbk(obj);
	}
}
function qoanimate(obj, targetOpacity, spd, cbk) {
// Анимация прозрачности
	var start=new Date().getTime();
	if (counter===undefined) {
		var counter=Math.ceil(fps/1000*spd);
	}
	var	opacity=parseFloat(obj.style.opacity);
	obj.style.opacity=(opacity+(targetOpacity-opacity)/counter);
	spd-=spd/counter;
	counter--;
	if (counter>0) {
		setTimeout(function() { 
			qoanimate(obj,targetOpacity,spd,((cbk==undefined)?undefined:cbk));
		}, spd/counter-(new Date().getTime()-start));
	} else if (cbk!=undefined) {
		cbk(obj);
	}
}
function lastFreeElem(arr) {
	for (var x=0;x<arr.length;x++) {
		if (arr[x]==undefined) {
			return x;
		}
	}
	return arr.length;
}
function random(x, y) {
// Промежутковый рандом (возвращает случайное целое число между x и y)
	if (y==undefined) {
		y=0;
	}
	if (x>y) {
		return x+Math.round(Math.random()*(y-x));
	} else if (x<y) {
		return y+Math.round(Math.random()*(x-y));
	} else {
		return x;
	}
}
function chance(x) {
// Булевый корейский рандом :3 (Возвращает true с вероятностью x%)
	if (Math.random()*100<x) {
		return true;
	}
	return false;
}
function inArray(subj, arr) {
	for (var i in arr) {
		if (arr[i]==subj) {
			return true;
		}
	}
	return false;
}
function getNumFromSeed(a,b,max) { 
	// Получить число от нуля до max из двух заданых чисел и константы SEED
	return Math.round(Math.abs(Math.sin(Math.pow(a%20+0.2,b%20+0.2)+Math.cos(a))*max))%max;
}
function arrIndexOf(subj, array) {
	for (var i in array) {
		if (array[i] == subj) {
			return i;
		}
	}
	return -1;
}
function isInRendRange(x,y) {
	if (x>=rendCX-(rendW-1)/2 && x<=rendCX+(rendW-1)/2 
	&& x>=0 && x<width
	&& y>=rendCY-(rendH-1)/2 && y<=rendCY+(rendH-1)/2 
	&& y>=0 && y<height) {
		return true;
	}
	return false;
}
function isInPrevRendRange(x,y) {
	if (prevRendCX==-1) {
		return false;
	}
	if (x>=prevRendCX-(rendW-1)/2 && x<=prevRendCX+(rendW-1)/2 
	&& x>=0 && x<width
	&& y>=prevRendCY-(rendH-1)/2 && y<=prevRendCY+(rendH-1)/2 
	&& y>=0 && y<height) {
		return true;
	}
	return false;
	
}
function isInPlayerVis(x,y) {
	if (player.visibleCells[x][y]!==undefined) {
		return true;
	}
	return false;
}
function isInPlayerPrevVis(x,y) {
	if (prevRendCX==-1) {
		return false;
	}
	if (player.prevVisibleCells[x][y]!=undefined) {
		return true;
	}
	return false;
}
function getFloorNum(name) {
	for (var i in floorNames) {
		if (floorNames[i] == name) {
			return i;
		}
	}
}
function preventSelection(element){
  var preventSelection = false;

  function addHandler(element, event, handler){
    if (element.attachEvent) 
      element.attachEvent('on' + event, handler);
    else 
      if (element.addEventListener) 
        element.addEventListener(event, handler, false);
  }
  function removeSelection(){
    if (window.getSelection) { window.getSelection().removeAllRanges(); }
    else if (document.selection && document.selection.clear)
      document.selection.clear();
  }
  function killCtrlA(event){
    var event = event || window.event;
    var sender = event.target || event.srcElement;

    if (sender.tagName.match(/INPUT|TEXTAREA/i))
      return;

    var key = event.keyCode || event.which;
    if (event.ctrlKey && key == 'A'.charCodeAt(0))  // 'A'.charCodeAt(0) можно заменить на 65
    {
      removeSelection();

      if (event.preventDefault) 
        event.preventDefault();
      else
        event.returnValue = false;
    }
  }

  // не даем выделять текст мышкой
  addHandler(element, 'mousemove', function(){
    if(preventSelection)
      removeSelection();
  });
  addHandler(element, 'mousedown', function(event){
    var event = event || window.event;
    var sender = event.target || event.srcElement;
    preventSelection = !sender.tagName.match(/INPUT|TEXTAREA/i);
  });

  // борем dblclick
  // если вешать функцию не на событие dblclick, можно избежать
  // временное выделение текста в некоторых браузерах
  addHandler(element, 'mouseup', function(){
    if (preventSelection)
      removeSelection();
    preventSelection = false;
  });

  // борем ctrl+A
  // скорей всего это и не надо, к тому же есть подозрение
  // что в случае все же такой необходимости функцию нужно 
  // вешать один раз и на document, а не на элемент
  addHandler(element, 'keydown', killCtrlA);
  addHandler(element, 'keyup', killCtrlA);
}
function getOffsetRect(elem) {
    var box=elem.getBoundingClientRect();
    var body=document.body;
    var docElem=document.documentElement;
    var scrollTop=window.pageYOffset || docElem.scrollTop || body.scrollTop;
    var scrollLeft=window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
    var clientTop=docElem.clientTop || body.clientTop || 0;
    var clientLeft=docElem.clientLeft || body.clientLeft || 0;
    var top=box.top+scrollTop-clientTop;
    var left=box.left+scrollLeft-clientLeft;
    return { top: Math.round(top), left: Math.round(left) };
}