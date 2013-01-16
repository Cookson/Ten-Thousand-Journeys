if (!document.head) {
	document.head = document.getElementsByTagName("head")[0];
}
HTMLElement.prototype.applyStyle = function _(style) {
	for (var i in style) {
		this.style[i] = style[i];
	}
};
/**
 * Adds a CSS class to this element. This method may ne used to make multiclass
 * HTMLElements
 * @param {String} className
 */
HTMLElement.prototype.addClass = function(className) {
	if (this.className == "") {
		this.className = className;
	} else {
		this.className += " "+className;
	}
};
/**
 * Use HTMLElement as hash map to bind some data to it.
 * 
 * @see HTMLElement#getData
 * @param {String} key String or number; unlike java, for example, objects 
 * can't be keys.
 * 
 * @param {mixed} value
 * @return {mixed} Returns parameter value.
 */
HTMLElement.prototype.setData = function _(key, value) {
	if (typeof this.data === "undefined") {
		this.data = {};
	}
	return this.data[key] = value;
};
/**
 * Use HTMLElement as hash map to get some previouslu added data.
 * 
 * @see HTMLElement#setData
 * @param {String} key
 * @return {mixed} Value at that key
 */
HTMLElement.prototype.getData = function _(key) {
	if (typeof this.data === "undefined" || typeof this.data[key] === "undefined") {
		throw new Error("No data at key "+key+" in element");
	}
	return this.data[key];
};
/**
 * Removes all the event listeners that were added to an element using 
 * UIElement.addEventListener().
 */
HTMLElement.prototype.clearEventListeners = function() {
	if (!("eventTypes" in this)) {
		return;
	}
	for (var i=0; i<this.eventTypes.length; i++) {
		this.removeEventListener(this.eventTypes[i], this.eventHandlers[i], false);
	}
	this.eventTypes = undefined;
	this.eventHandlers = undefined;
};
/*
 * Make Strings hahsable
 */
String.prototype.hashCode = function() {
	return this.valueOf();
};
String.prototype.equals = function(object) {
	return this.valueOf() === object.hashCode();
};
/**
 * Similar to Array.indexOf, but searches not for "javascriptly-equal" element, 
 * but for element with equal .hashCode() ("java-equal", lolz). Array must
 * contain only hashable objects of the same type as the sought-for object.
 * 
 * @param {Array} array
 * @param {Object} object A hashable object.
 * @return {Number} Index of object in array if has one, -1 otherwise.
 */
function indexOfByHash(array, object) {
	for (var i=0; i<array.length; i++) {
		if (array[i].equals(object)) {
			return i;
		}
	}
	return -1;
}
/**
 * Checks if there is any object in an array that is equal to %object% 
 * we pass in the second argument. Objects must be comparable, i.e. %object%
 * and all objects in %array% must implement method .equals() in their 
 * prototypes.
 * 
 * @param {Object[]} array Array of objects where we search for an object
 * equal to another particular object.
 * @param {Object} object Objects that we compare to each member of array.
 * @param {Function} comparator 
 * @throws {Error} If object is not comparable, i.e. its prototype doesn't
 * implement method .compare();
 */
var System = {};
System.hasEqualObject = function (array, object, comparator) {
	var c = comparator || object.equals;
	if (!(c instanceof Function)) {
		if (comparator === undefined) {
			throw new Error("Objects of type "+object.__proto__.constructor.name+" are not comparable");
		} else {
			throw new Error("Comparator must be function!");
		}
	}
	for (var i=0; i<array.length; i++) {
		if (c(array[i], object)) {
			return true;
		}
	}
	return false;
};
function recountWindowSize() {
	var nGameZone = document.getElementById("intfGameZone");
	var nWindowWrap = document.getElementById("intfWindowWrap");
	// document.getElementById("stLoad").style.lineHeight = document.body.clientHeight+"px";
	
	// Центрировать игровое окно по вертикали
	nWindowWrap.style.marginTop = (document.body.clientHeight-nWindowWrap.clientHeight)/2+"px";
	nWindowWrap.style.marginLeft = (document.body.clientWidth-nWindowWrap.clientWidth)/2+"px";
	UI.width = nWindowWrap.clientWidth;
	UI.width = UI.width-UI.width%32;
	UI.height = nWindowWrap.clientHeight;

	var w = Math.floor((UI.visibleWidth)/32);
	var h = Math.floor((UI.visibleHeight)/32);
	var rendW = ((w%2==1)?w:w+1)+2;
	var rendH = ((h%2==1)?h:h+1)+2;
}
function blank2dArray(i) {
	var w = i;
	var arr = [];
	for (var i=0; i<w; i++) {
		arr[i] = [];
	}
	return arr;
}
function distance(startX, startY, endX, endY) {
	return Math.sqrt(Math.pow(startX-endX, 2)+Math.pow(startY-endY, 2));
}
function isNear(startX, startY, endX, endY) {
	var ableX = Math.abs(startX-endX);
	var ableY = Math.abs(startY-endY);
	if ((ableX==1 && ableY==0) || (ableY==1 && ableX==0) || (ableY==1 && ableX==1)) {
		return true;
	}
	return false;
};
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
	return Math.round(Math.abs(Math.sin(Math.pow(Math.abs(a%20+0.2),Math.abs(b%20+0.2))+Math.cos(a))*max))%max;
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
    var box = elem.getBoundingClientRect();
    var body = document.body;
    var docElem = document.documentElement;
    var scrollTop = window.pageYOffset || docElem.scrollTop || body.scrollTop;
    var scrollLeft = window.pageXOffset || docElem.scrollLeft || body.scrollLeft;
    var clientTop = docElem.clientTop || body.clientTop || 0;
    var clientLeft = docElem.clientLeft || body.clientLeft || 0;
    var top = box.top+scrollTop-clientTop;
    var left = box.left+scrollLeft-clientLeft;
    return { top: Math.round(top), left: Math.round(left) };
}
/**
 * Returns array of three arrays: array[0] - elements that are only in set1;
 * array[1] - elements that are only in set2; array[2] - elements that are in
 * both set1 and set2. If set1 or set2 are objects, then their prototypes must 
 * have method .getValues() that returns an array of values to compare, and all 
 * the returned elements' prototypes must have method .equals() to check if one
 * object satisfies the conditions to be equal to another. 
 * 
 * @param {Object[]} set1 Array or Object that implements method .getValues. 
 * This array may contain any values: both objects and primitives.
 * @param {Object[]} set2 Same as set1. Set1 and set2 may be even of different
 * prototypes, but if their .getValues() returns objects of the same type,
 * it will work fine.
 * @return {Object[3][]}
 */
function getSetsDifferences(set1, set2) {
	if (Interface.objectImplements(set1, "Arrayable")) {
	// If set1 implements interface Set
		set1 = set1.getValues();
	}
	if (Interface.objectImplements(set2, "Arrayable")) {
	// If set2 implements interface Set
		set2 = set2.getValues();
	}
	var inFirst = [];
	var inBoth = [];
	var inSecond = [];
	for (var i=0; i<set1.length; i++) {
		if (!System.hasEqualObject(set2, set1[i])) {
			inFirst.push(set1[i]);
		} else {
			inBoth.push(set1[i]);
		}
	}
	for (var i=0; i<set2.length; i++) {
		if (!System.hasEqualObject(set1, set2[i])) {
			inSecond.push(set2[i]);
		}
	}
	return [inFirst, inSecond, inBoth];
}
var FunctionQueue;
(function() {
	/**
	 * FunctionQueue is a manager that allows serial (i.e. not parallel) 
	 * performing of asynchronous actions whose start and end points are in
	 * different coroutines ("threads"). To put it simply, it lets you call
	 * several functions consequentially even if one of them finishes its work
	 * after it actually ends (for example, in setTimeout inside that function).
	 * 
	 * @example
	 * // This way we can call three animation functions on an object, 
	 * // and they would execute parallelly, at the same time
	 * animate1.call(entity);
	 * animate2.call(entity);
	 * animate3.call(entity);
	 * 
	 * // Animations, as asynchronous functions that use setTimeout, will be
	 * // performed at the same time in that example.
	 * // But with FunctionQueue we can start the next function right after the
	 * // previous one was executed
	 * 
	 * var q = new FunctionQueue();
	 * // Queue three asynchronous animation functions that use setTimeouts
	 * q.add(animate1, entity); // Animates entity for 100ms
	 * q.add(animate2, entity); // ...and for another 50ms
	 * q.add(animate3, entity); // ...and for 70ms more
	 * 
	 * @constructor
	 */
	FunctionQueue = function() {
		this._functions = [];
		this._contexts = [];
		this._args = [];
	};
	/**
	 * Adds a function to a queue. If the queue has no queued functions, queue
	 * runs that function immediately. Else, if there is a function being 
	 * evaluated in this queue right now, the new function will be called after
	 * all the previously added functions have done their work.
	 * 
	 * @param {function} func A function to be executed when its time comes.
	 * @param {object} [context] An object in the context of which the function 
	 * will be executed (optional);
	 * @return
	 */
	FunctionQueue.prototype.add = function(func, context, args) {
		if (typeof func !== "function" && !(func instanceof Function)) {
			throw new Error("The first argument must be a function or Function object, now it is "
					+(typeof func === "object" ? func.constructor.name : typeof func));
		}
		if (typeof context !== "object") {
			throw new Error("The second argument must be an object, now it is "+(typeof context));
		}
		if (!(args instanceof Array)) {
			throw new Error("The third argument must be an array, now it is "+(typeof args === object ? args.constructor.name : typeof args));
		}
		this._functions.unshift(func);
		this._contexts.unshift(context);
		this._args.unshift(args);
		if (this._functions.length === 1) {
		// If this is the first and only function in queue — run it immediately
			
		}
	};
	/**
	 * 
	 * @param {function} func
	 * @return
	 * 
	 * @example
	 * var q = new FunctionQueue();
	 * function animate1() {
	 * 	
	 * }
	 * function animate2() {
	 * 	if (
	 * }
	 */
	FunctionQueue.prototype.done = function(func) {
		if (this._functions[this._functions.length-1] != func) {
		// If the function is not the function that is being executed in this FunctionQueue
			throw new Error("Function", Object.create(func), "said to queue that it's done, but this function is not on the top of the queue");
		}
		var lastIndex = this._queue.length-1;
		this._queue[lastIndex].apply(this._contexts[lastIndex], this._args[lastIndex]);
		this._functions.pop();
	};
})();
function extend(object, mixin) {
	for (var i in mixin) {
		if (object[i]) {
			throw new Error("Object already has property "+i+"!");
		}
		object[i] = mixin[i];
	}
}
