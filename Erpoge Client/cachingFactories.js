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
 * @param {Object} object Value object.
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
 * returns it.
 * @return
 */
CachingMonoFactory.prototype.get = function get() {
	for (var i=0; i<this.cache.length; i++) {
		if (this.isValueFree(this.cache[i])) {
			return this.cache[i];
		}
	}
	// If there's no free element
	var newValue = this.createNewValue();
	this.cache.push(newValue);
	return newValue;
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
/**
 * Returns an HTMLDivElement containing particular letter as inner text.
 * 
 * @memberOf CachingLetterFactory
 * @param {String}
 *            className Name of CSS class of div element.
 * @param {String}
 *            letter A string of 1 character to put inside HTMLDivElement.
 * @return {HTMLDivElement}
 */
CachingLetterFactory.get = function get(className, letter) {
	var nDiv = CachingMonoFactory.prototype.get.apply(this);
	nDiv.className = className;
	nDiv.firstChild.nodeValue = letter;
	return nDiv;
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
 * @param {String}
 *            bg Standard item background class name. See style.css, they start
 *            with div.itemBg*. E.g. for class itemBgEquipment this argument
 *            should be "Equipment".
 */
NullCachingItemViewFactory.createNewValue = function createNewValue(background) {
	return new ItemView(null);
};
NullCachingItemViewFactory.get = function get(bg) {
	var itemView = CachingMonoFactory.prototype.get.apply(this);
	itemView.rootElement.setData("typeId", -1);
	itemView.rootElement.setData("param", -1);
	itemView.getImg().setAttribute("src", "./images/intf/nothing.png");
	// This is because of the next line: otherwise element will have all the 
	// classes set by .addClass().
	itemView.getNode().className = "itemView";
	itemView.getNode().addClass("itemView"+bg);
	itemView.changeAmount(1);
	return itemView;
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
/** @public @type String */
};
CachingItemViewFactory.__proto__ = new CachingMonoFactory();
/**
 * Gets {@link ItemView} from chace and modifies cached ItemView so it 
 * represents UniqueItem|ItemPile item with background CSS class "itemView"+bg.
 * 
 * @param {UniqueItem|ItemPile} item
 * @param {String} bg Postfix for background node CSS class.
 * @return {ItemView} Created ItemView
 */
CachingItemViewFactory.get = function get(item, bg) {
	if (bg === undefined) {
		throw new Error("You must provide bg!");
	}
	var itemView = CachingMonoFactory.prototype.get.apply(this);
	itemView.item = item;
	// This is because of the next line: otherwise element will have all the 
	// classes set by .addClass().
	itemView.getNode().className = "itemView"; 
	itemView.getNode().addClass("itemView"+bg);
	if (item instanceof ItemPile) {
		itemView.changeAmount(item.amount);
	} else {
		itemView.changeAmount(1);
	}
	itemView.getImg().setAttribute("src", "./images/items/"+item.typeId+".png");
	itemView.rootElement.setData("typeId", item.typeId);
	if (item instanceof UniqueItem) {
		itemView.rootElement.setData("param", item.itemId);
		itemView.changeAmount(1);
	} else {
		itemView.rootElement.setData("param", item.amount);
		itemView.changeAmount(item.amount);
	}
	return itemView;
};
/**
 * @private
 * @param item
 * @return {ItemView}
 */
CachingItemViewFactory.createNewValue = function createNewValue(item) {
	return new ItemView(item, this.bg);
};

CachingItemViewFactory.isValueFree = function isValueFree(element) {
	return element.parentNode === null;
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
/**
 * Returns an ItemView ot an item that is already created or throws an
 * Error if ItemView for item is not created in this cache.
 * 
 * @param {UniqueItem|ItemPile} item Key.
 * @return {ItemView} Value.
 */
CachingItemViewFactory.getCreatedItem = function getCreatedItem(item) {
	return this.cache[item.__proto__.constructor.name][item.hashCode()]
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
		func.apply(context, [this._contents[i]]);
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
HashMap.prototype.isEmpty = function () {
	for (var i in this._contents) {
		return false;
	}
	return true;
};
