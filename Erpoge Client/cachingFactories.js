/*
 * cachingFactories.js: Contains classes to create different factories that 
 * cache created objects.
 */
/* Abstract classes */
/**
 * Caching factory is a hash map, where key is any object, and from getting
 * value under one key we can get several instances of desired objects.
 * Value objects are built by caching factory. In CachingFactory objects may
 * become "free", which means we can take previously created instance of object 
 * and return it instead of creating a new one. So in caching factory we 
 * consider two types of objects: key objects (which we pass as an argument) 
 * and value objects (which are created and stored by CachingFactory).
 * 
 * @contructor
 */
function CachingFactory() {
	this.cache = {};
}
/**
 * Returns a stored or new value object corresponding to key object.
 * 
 * @param {Object} object Key object.
 * @return {Object} Value object.
 */
CachingFactory.prototype.get = function get(object) {
	Interface.check(object, "Hashable");
	var hashCode = object.hashCode();
	if (!(hashCode in this.cache)) {
	// If no objects equal to argument object were stored,
	// return new value object.
		var newValue = this.createNewValue(object);
		this.cache[hashCode] = [newValue];
	}
	for (var i in this.cache[hashCode]) {
		if (this.isValueFree(this.cache[hashCode][i])) {
		// If has free value object, return cached value object.
			return this.cache[hashCode][i];
		}
	}
	// If has no free objects, return new object.
	var newValue = this.createNewValue(object);
	this.cache[hashCode] = [newValue];
	return newValue;
};
/**
 * Abstract method that must describe which values are created and stored in
 * CachingFactory. Throws an Error if not overriden in subclass.
 * 
 * @private
 * @param {Object} object Key in map of 
 * @return Object
 */
CachingFactory.prototype.createNewValue = function createNewValue(object) {
	throw new Error("Method createNewValue must be overriden in CachingFactory descendants!");
};
/**
 * Abstract method that must tell if the object is free for reusing or not.
 * 
 * @param {Object} value Value object.
 * @return {Boolean}
 */
CachingFactory.prototype.isValueFree = function isValueFree(value) {
	throw new Error("Method isValueFree must be overriden in CachingFactory descendants!");
};


/**
 * CachingMonoFactory creates identical instances of value objects.
 * CachingMonoFactory is not a hash map, it is a set. It is a simplified form
 * of {@link CachingFactory} that does not get value object by key object,
 * but any free stored object may be returned.
 */
function CachingMonoFactory() {
	CachingFactory.apply(this);
	this.cache = [];
}
CachingMonoFactory.prototype = new CachingFactory();
/**
 * Returns cached value object if has any, or creates a new value object and 
 * returns it. Provided arguments must describe how to modify the value.
 * 
 * @return {object}
 */
CachingMonoFactory.prototype.get = function get() {
	for (var i=0; i<this.cache.length; i++) {
		if (this.isValueFree(this.cache[i])) {
			this.modifyCached.apply(this.cache[i], arguments);
			return this.cache[i];
		}
	}
	// If there's no free element
	
	var newValue = this.createNewValue();
	this.cache.push(newValue);
	this.modifyCached.apply(this.cache[i], arguments);
	return newValue;
};
CachingMonoFactory.prototype.modifyCached = function() {
	throw new Error("modifyCached must be overridden by descendant!");
};

/**
 * MultitypeCachingFactory is an abstract CachingFactory that can take objects 
 * of any number of different types as keys.
 * 
 * @extends CachingFactory
 */
function MultitypeCachingFactory() {
	CachingFactory.apply(this);
}
MultitypeCachingFactory.prototype = new CachingFactory();
/**
 * @see CachingFactory#get
 * @param {Object} object Key object.
 * @return {Object} Value object.
 */
MultitypeCachingFactory.prototype.get = function get(object) {
	Interface.check(object, "Hashable");
	var hashCode = object.hashCode();
	var type = object.__proto__.constructor.name;
	if (!(type in this.cache)) {
	// If no objects of that type were created, return and store a new object.
		var newValue = this.createNewValue(object);
		this.cache[type] = {};
		this.cache[type][hashCode] = newValue;
		return newValue;
	}
	if (!(hashCode in this.cache[type])) {
	// If no objects that are equal to argument object are saved as keys,
	// also create a new object.
		var newValue = this.createNewValue(object);
		this.cache[type][hashCode] = newValue;
		return newValue;
	}
	// Create a new value object and store it.
	var newValue = this.createNewValue(object);
	this.cache[type][hashCode] = newValue;
	return newValue;
};
MultitypeCachingFactory.prototype.isValueFree = function isValueFree(value) {
	return value.parentNode === null;
};

/* Non-abstract classes */
/**
 * This singleton returns HTMLDivElements with letters. It does not create a new
 * HTMLDivElement all the time, but tries to cache them, so using it is more
 * productive than creating divs with letters on the spot.
 * 
 * @see LetterAssigner
 * @class
 */
var CachingLetterFactory = new (function CachingLetterFactory() {
	CachingMonoFactory.apply(this);
});
CachingLetterFactory.__proto__ = new CachingMonoFactory();
CachingLetterFactory.modifyCached = function(className, letter) {
	this.className = className;
	this.firstChild.nodeValue = letter;
};
CachingLetterFactory.createNewValue = function createNewValue() {
	var nDiv = document.createElement("div");
	var nText = document.createTextNode("");
	nDiv.appendChild(nText);
	return nDiv;
};
CachingLetterFactory.isValueFree = function isValueFree(value) {
	return value.parentNode === null;
};


/**
 * Singleton that creates and stores {@link ItemView}s with null items, i.e.
 * plain backgrounds. It lets removed ItemViews not be destroyed, but used later
 * if needed, so in the whole life of browser game window we will only need to
 * have as much null ItemViews as it was maximally created at the same time.
 * 
 * @class
 */
var NullCachingItemViewFactory = new CachingMonoFactory();
NullCachingItemViewFactory.isValueFree = function(value) {
	return value.rootElement.parentNode === null;
};
/**
 * 
 * 
 * @param {String} background Standard item background class name. See 
 * style.css for div.itemBg*. E.g. for class itemBgEquipment this argument
 * should be "Equipment".
 * @return {ItemView} A new ItemView 
 */
NullCachingItemViewFactory.createNewValue = function createNewValue(background) {
	return new ItemView(null);
};
NullCachingItemViewFactory.modifyCached = function(bg) {
	this.rootElement.setData("typeId", -1);
	this.rootElement.setData("param", -1);
	this.getImg().setAttribute("src", "./images/intf/nothing.png");
	// This is because of the next line: otherwise element will have all the 
	// classes set by .addClass().
	this.getNode().className = "iconWrap";
	this.getNode().addClass("itemView"+bg);
	this.changeAmount(1);
};

/**
 * Data structure that creates and caches {@link ItemView}s. Cached ItemViews
 * don't have image, .item or amount: they get these when you 
 * CachingItemViewFactory.get(item, bg) them
 * 
 * @see CachingItemViewFactory#get
 * @constructor
 * @extends CachingMonoFactory 
 * @param {String} bg Postfix pointing to a standard (from style.css) CSS 
 * class. See {@link CachingItemViewFactory#get}
 */
CachingItemViewFactory = {
	
};
CachingItemViewFactory.__proto__ = new CachingMonoFactory();
CachingItemViewFactory.modifyCached = function(item, bg) {
	if (bg === undefined) {
		throw new Error("You must provide bg!");
	}
	this.item = item;
	// This is because of the next line: otherwise element will have all the 
	// classes set by .addClass().
	this.getNode().className = "iconWrap"; 
	this.getNode().addClass("itemView"+bg);
	if (item instanceof ItemPile) {
		this.changeAmount(item.amount);
	} else {
		this.changeAmount(1);
	}
	this.getImg().setAttribute("src", "./images/items/"+item.typeId+".png");
	this.rootElement.setData("typeId", item.typeId);
	if (item instanceof UniqueItem) {
		this.rootElement.setData("param", item.itemId);
		this.changeAmount(1);
	} else {
		this.rootElement.setData("param", item.amount);
		this.changeAmount(item.amount);
	}
	this.getNode().clearEventListeners();
};
/**
 * @private
 * @param item
 * @return {ItemView}
 */
CachingItemViewFactory.createNewValue = function createNewValue(item) {
	return new ItemView(item, this.bg);
};
CachingItemViewFactory.isValueFree = function isValueFree(itemView) {
	return itemView.getNode().parentNode === null;
};
/**
 * Get array of all the item objects to which ItemViews are associated.
 * 
 * @return Object[] Array of {@link UniqueItem}s and {@link ItemPiles}.
 *          Unique items, if the CachingItemViewFactory has any, go first.
 */
CachingItemViewFactory.getValues = function getValues() {
	var answer = [];
	for (var i in this.cache) {
		for (var j in this.cache[i]) {
			for (var k in this.cache[i][j]) {
				answer.push(this.cache[i][j][k].item);
			}
		}
	}
	return answer;
};

var CachingSpellFactory = {};
CachingSpellFactory.__proto__ = new CachingMonoFactory();
CachingSpellFactory.createNewValue = function createNewValue() {
	var nDiv = document.createElement("div");
	var nImg = document.createElement("img");
	nDiv.className = "iconWrap";
	nDiv.addClass("spellView");
	nDiv.appendChild(nImg);
	return nDiv;
};
CachingSpellFactory.isValueFree = function(div) {
	return div.parentode === null;
};
CachingSpellFactory.modifyCached = function(spellId) {
	if (spellId === null) {
		this.children[0].setData("spellId", -1);
		this.children[0].setAttribute("src", "");
	} else {
		this.children[0].setData("spellId", spellId);
		this.children[0].setAttribute("src", "./images/spells/"+spellId+".png");
	}
	this.clearEventListeners();
	this.children[0].clearEventListeners();
};

/**
 * MultitypeSet is a data structure for storing hashable objects of different
 * types.
 */
function MultitypeSet() {
	this.elements = {};
}
/**
 * Stores an object in this set. If object is already stored, throws an error.
 * 
 * @param {Object} object An object to store.
 */
MultitypeSet.prototype.add = function add(object) {
	var type = object.__proto__.constructor.name;
	if (!(type in this.elements)) {
		this.elements[type] = {};
	} else if (object.hashCode() in this.elements[type]) {
		throw new Error("Object "+type+" "+object.hashCode()+" is already in set");
	}
	this.elements[type][object.hashCode()] = object;
};
/**
 * Removes object from set. Throws an error if the set doesn't have that object.
 * 
 * @param {Object} object Object we remove.
 */
MultitypeSet.prototype.remove = function remove(object) {
	var type = object.__proto__.constructor.name;
	var hashCode = object.hashCode();
	if (!(type in this.elements)) {
		throw new Error("There are no objects of type "+type+" in set!");
	}
	if (!(hashCode in this.elements[type])) {
		throw new Error("There's no object "+type+" "+hashCode+" in set!");
	}
	delete this.elements[type][object.hashCode()];
};
/**
 * Checks if set has object of certain type with certain hash code or not.
 * 
 * @param {Object} object Object we are checking for.
 * @return {Boolean}
 */
MultitypeSet.prototype.has = function has(object) {
	var type = object.__proto__.constructor.name;
	if (!(type in this.elements)) {
		return false;
	}
	return object.hashCode() in this.elements[type];
};
/**
 * Remove all the objects from this set.
 */
MultitypeSet.prototype.empty = function empty() {
	for (var i in this.elements) {
		for (var j in this.elements[i]) {
			delete this.elements[i][j];
		}
		delete this.elements[i];
	}
};
/**
 * Returns an array of all the objects stored in this set.
 * 
 * @return {Object[]} Arrays of all the objects in MultitypeSet
 */
MultitypeSet.prototype.getValues = function getValues() {
	var answer = [];
	for (var i in this.elements) {
		for (var j in this.elements[i]) {
			answer.push(this.elements[i][j]);
		}
	}
	return answer;
};
function HashSet() {
	this._contents = {};
};
HashSet.prototype.BREAK = {};
HashSet.prototype.add = function add(object) {
	Interface.check(object, "Hashable");
	var hash = object.hashCode();
	if (hash in this._contents) {
		throw new Error("Object "+object.__proto__.constructor.name+" "+hash+" is already in set!");
	}
	this._contents[hash] = object;
};
HashSet.prototype.remove = function remove(object) {
	Interface.check(object, "Hashable");
	var hash = object.hashCode();
	if (!(hash in this._contents)) {
		throw new Error("Object "+object.__proto__.constructor.name+" "+hash+" is not in set!");
	}
	delete this._contents[hash];
};
HashSet.prototype.contains = function contains(object) {
	Interface.check(object, "Hashable");
	return object.hashCode() in this._contents;
};
/**
 * A synonym for HashSet.contains
 */
HashSet.prototype.containsValue = HashSet.prototype.contains;
HashSet.prototype.getValues = function getValues() {
	var answer = [];
	for (var i in this._contents) {
		answer.push(this._contents[i]);
	}
	return answer;
};
HashSet.prototype.forEach = function forEach(func, context) {
	for (var i in this._contents) {
		func.apply(context, [this._contents[i]]);
	}
};
HashSet.prototype.getEqual = function getEqual(object) {
	for (var i in this._contents) {
		if (this._contents[i].equals(object)) {
			return this._contents[i];
		}
	}
	return null;
};
HashSet.prototype.empty = function empty() {
	for (var i in this._contents) {
		delete this._contents[i];
	}
};

function HashMap() {
	this._contents = {};
}
HashMap.prototype.get = function get(key) {
	var hash = (typeof key != "object") ? key : key.hashCode();
	if (hash in this._contents) {
		return this._contents[hash];
	}
	return null;
};
HashMap.prototype.put = function put(key, value) {
	return this._contents[(typeof key != "object") ? key : key.hashCode()] = value;
};
HashMap.prototype.containsKey = function containsKey(key) {
	return ((typeof key != "object") ? key : key.hashCode()) in this._contents;
};
HashMap.prototype.contains = function() {
	throw new Error("HashMap has no method HashMap.contains; did you mean .containsValue or .containsKey?");
};
HashMap.prototype.containsValue = function containsValue(value) {
	for (var i in this._contetns) {
		if (this._contents[i].equals(value)) {
			return true;
		}
	}
};
HashMap.prototype.empty = function empty() {
	for (var i in this._contents) {
		delete this._contents[i];
	}
};
HashMap.prototype.forEach = function forEach(func, context) {
	if (context === undefined) {
		context = window;
	}
	for (var i in this._contents) {
		if (func.apply(context, [this._contents[i]]) == this.BREAK) {
			break;
		}
	}
};
HashMap.prototype.remove = function remove(key) {
	key = ((typeof key !== "object") ? key : key.hashCode());
	if (!(key in this._contents)) {
		throw new Error("HashMap doesn't have key "+key.__proto__.constructor.name+" "+key.hashCode());
	}
	delete this._contents[key];
};
/**
 * Removes value under a key with certain hashCode.
 * 
 * @param {string} hashCode Hash code of key object.
 */
HashMap.prototype.removeByHashCode = function(hashCode) {
	if (!(hashCode in this._contents)) {
		throw new Error("HashMap has no object with hash code "+hashCode);
	}
	delete this._contents[hashCode];
};
HashMap.prototype.getValues = function getValues() {
	var answer = [];
	for (var i in this._contents) {
		answer.push(this._contents[i]);
	}
	return answer;
};
HashMap.prototype.getKeys = function() {
	var answer = [];
	for (var i in this._contents) {
		
	}
};
HashMap.prototype.isEmpty = function () {
	for (var i in this._contents) {
		return false;
	}
	return true;
};

/**
 * Matrix is a 2-dimensional map, each element of which has two indexes ("x" 
 * and "y"). The advantage over a simple Array of Arrays is that Matrix has 
 * smarter automatic memory management and is generally easier to use.
 * 
 * @constructor
 */
function Matrix() {
	this._c = {};
}
/**
 * Put an element to Matrix under two indexes.
 * @param {number} x
 * @param {number} y
 * @param {Object} object
 * @return {Object} The object we've just put to the Matrix.
 */
Matrix.prototype.put = function(x,y,object) {
	if (this._c[x] === undefined) {
		this._c[x] = {};
	}
	this._c[x][y] = object;
};
/**
 * @param {number} x
 * @param {number} y
 * @return {Object} An object at these indexes.
 */
Matrix.prototype.get = function(x,y) {
	if (this._c[x] === undefined) {
		return null;
	}
	if (this._c[x][y] === undefined) {
		return null;
	}
	return this._c[x][y];
};
Matrix.prototype.getValues = function() {
	var answer = [];
	for (var i in this._c) {
		for (var j in this._c[i]) {
			answer.push(this._c[i][j]);
		}
	}
	return answer;
};

function Tree(object) {
	this._contents = object || {};
}
/**
 * 
 * The last argumant is value, all the arguments before are indexes. For 
 * example, 
 * @example
 * var t = new Tree({
 * 	1:"a",
 *  2: {
 *    1: "b",
 *    2: "c"
 *  }
 * });
 * t.set(2, 3, "d");
 * console.log(t.show());
 * // This outputs the following to console: 
 * {
 * 	1:"a",
 *  2: {
 *    1: "b",
 *    2: "c",
 *    3: "d"
 *  }
 * }
 * 
 * @returns {Boolean}
 */
Tree.prototype.set = function() {
	var lvl = this._contents;
	var i=0;
	var undef = false;
	// In the loop we iterate through elements of tree from [0] to [length-2]
	for (var l=arguments.length; i<l-2; i++) {
		if (lvl[arguments[i]] === undefined) {
			undef = true; 
			lvl[arguments[i]] = {};
		} else {
			lvl = lvl[arguments[i]];
		}
	}
	lvl[arguments[i]] = arguments[i+1];
};
Tree.prototype.get = function() {
	var lvl = this._contents;
	for (var i=0, l=arguments.length; i<l; i++) {
		lvl = lvl[arguments[i]];
	}
	return lvl;
};
Tree.prototype.show = function() {
	return this._contents;
};
/**
 * @constructor
 * Map2D is used to store any data by associating it with two indexes: "x
 * coordinate" and "y coordinate". Unlike simple 2-dimensional arrays,
 * this data structure allows not only integer indexes greater or equal to zero,
 * but also negative integers. This data structure is handy for storing data
 * associated with cells on game field.
 * 
 * @example
 * var m = new Map2D();
 * m.set(-1, 2, "hello");
 * m.get(-1, 2); // Returns "hello"
 * m.set(3.14, "12", "hey!"); // Throws an error (indexes must be integers)
 */
function Map2D() {
	this._contents = {};
}
/**
 * Associates a value with a couple of indexes.
 * 
 * @param {number} x First index
 * @param {number} y Second index
 * @param {mixed} value
 *
 */
Map2D.prototype.set = function(x, y, value) {
	if (typeof x !== "number" || Math.floor(x) !== x) {
		throw new TypeEror("First argument must be an integer, but it is "+x)
	}
	if (typeof y !== "number" || Math.floor(y) !== y) {
		throw new TypeEror("Second argument must be an integer, but it is "+y)
	}
	if (this._contents[x] === undefined) {
		this._contents[x] = {};
	}
	if (value === null) {
		delete this._contents[x][y];
	} else {
		this._contents[x][y] = value;
	}
};
/**
 * Returns a value stored under two indexes. If there is no value stored
 * under these indexes, returns null
 * @param {number} x First index
 * @param {number} y Second index
 * @returns {mixed|null}
 */
Map2D.prototype.get = function(x, y) {
	if (typeof x !== "number" || Math.floor(x) !== x) {
		throw new Error("First argument must be an integer, but it is "+x)
	}
	if (typeof y !== "number" || Math.floor(y) !== y) {
		throw new TypeEror("Second argument must be an integer, but it is "+y)
	}
	if (this._contents[x] === undefined || this._contents[x][y] === undefined) {
		return null;
	}
	return this._contents[x][y];
};
/**
 * Removes an object under certain index. If there is no object under such 
 * index, returns false (this is considered correct and throws no
 * exception)
 * 
 * @param {number} x Index 1
 * @param {number} y Index 2
 * 
 * @returns {boolean} false if there is no object under such indexes,
 * 		true is object is successfully removed
 */
Map2D.prototype.remove = function(x, y) {
	if (!(x in this._contents)) {
		return false;
	}
	if (!(y in this._contents[x])) {
		return false;
	}
	delete this._contents[x][y];
	return true;
};
