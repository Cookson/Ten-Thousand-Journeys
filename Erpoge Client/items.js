/* Предметы */
// Формат: [name,type,weight,price[,weaponDamage[,weaponSpeed]]]
/* type: 
	0-10 - оружие
	11 - щит
	12 - шлем
	13 - доспех
	14 - перчатки
	15 - ботинки
	16 - плащ
	17 - ! зарезервировано
	18 - ! зарезервировано
	19 - кольцо
	20 - ! зарезервировано для второго кольца
	21 - амулет
	9000 - деньги
	
Номера слотов для предмета определённого типа в массиве амуниции равны (номерТипа-10) (оружие - 0, щит - 1 и т.д.)

Классы оружия:
	1 - меч
	2 - топор
	3 - дробящее
	4 - древковое
	5 - посох
	6 - кинжал
	7 - лук
*/

// Формат: [type,one/two-handed]
// Этот блок комментариев не нужен, оставлен на всякий случай.
/* type:
	1: кинжал,
	2: короткий меч,
	3: длинный меч,
	4: топор,
	5: булава,
	6: древковое,
	7: посох,
	8: лук,
	9: арбалет,
	10: праща,
	11: духовая трубка
one/two-handed: 1 || 2;
*/
var items={2200:["Книга заклинания Огненный шар",22,1,1000,4],1100:["Шлем марунарх",11,1,100,4,2,-1],1101:["Железный шлем",11,1,100,4,2,-1],1102:["Кожаный шлем",11,1,100,4,1,0],402:["Алебарда", 4,10,100,4,13,3,16],1502:["Зелёный плащ",15,10,100,4,1,0],403:["Коса", 4,10,100,4,16,6,3],400:["Копьё", 4,10,100,4,6,3,120],1500:["Серый плащ",15,10,100,4,1,0],401:["Копьё", 4,10,100,4,6,3,120],1501:["Красный плащ",15,10,100,4,1,0],404:["Коса", 4,10,100,4,16,6,3],700:["Короткий лук", 7,1,100,4,7,1,11],305:["Золотая булава", 3,100,100,4,9,2,15],304:["Цеп", 3,100,100,4,9,2,15],1401:["Железные ботинки",14,1,100,4,1,-1],1400:["Ботинки марунарх",14,1,100,4,2,-1],1403:["Ботинки из грубой ткани",14,1,10,4,1,0],1402:["Кожаные сапоги",14,1,100,4,1,0],1600:["Кольцо мудрости",16,10,100,4,0,0],1601:["Кольцо силы",16,10,100,4,0,0],1602:["Кольцо смерти",16,10,100,4,0,0],1603:["Обручальное кольцо",16,10,100,4,0,0],1604:["Кольцо кольца",16,10,100,4,0,0],900000:["Золото",9000,0,1,0],300:["Булава", 3,100,100,4,8,3,14],301:["Моргенштерн", 3,100,100,4,10,-1,15],302:["Моргенштерн", 3,100,100,4,10,-1,15],303:["Моргенштерн", 3,100,100,4,10,-1,15],201:["Секира", 2,22,160,4,14,-2,17],200:["Топор", 2,18,100,4,7,3,13],202:["Боевой топор", 2,28,200,4,26,13,13],1302:["Кожаные перчатки",13,1,100,4,1,0],1301:["Железные перчатки",13,1,100,4,1,0],1300:["Перчатки марунарх",13,1,100,4,2,0],1001:["Круглый щит", 10,1,100,4,7,1,11],1000:["Круглый щит", 10,1,100,4,7,1,11],1002:["Круглый щит", 10,1,100,4,7,1,11],102:["Сабля", 1,10,130,4,7,4,12],103:["Гладиус", 1,14,140,4,8,4,14],2300:["Стрела",23,1,1000,4],100:["Короткий меч", 1,34,100,4,6,4,11],101:["Меч", 1,12,100,4,7,4,13],504:["Посох", 5,5,10,4,100,7,6],500:["Посох", 5,5,10,4,100,7,6],501:["Посох", 5,5,10,4,100,7,6],502:["Посох", 5,5,10,4,100,7,6],503:["Посох", 5,5,10,4,100,7,6],104:["Сабля", 1,10,130,4,7,4,13],1202:["Льняная роба",12,1,100,4,1,0],1203:["Огненная роба",12,1,100,4,3,0],1200:["Доспех марунарх",12,1,100,4,7,-5],1201:["Кольчуга",12,1,100,4,6,-4],1204:["Кожаный доспех",12,1,100,4,4,-2],1205:["Рубашка из грубой ткани",12,1,10,4,1,-1],601:["Кинжал", 6,10,100,4,6,4,10],600:["Нож", 6,10,100,4,5,3,10]};
// Функции оружия
function isWeapon(typeId) {
	return items[typeId][1]>=1 & items[typeId][1]<=10;
}
function isEquipment(typeId) {
	return items[typeId][1]>=1 & items[typeId][1]<=21;
}
function getSlotFromClass(type) {
// Получить слот предмета из его типа
	if (type>=1 && type<=9) {
	// Оружие
		return 0;
	} else if (type == 10) {
		return 1;
	} else if (type==11) {
		return 2;
	} else if (type==12) {
		return 3;
	} else if (type==13) {
		return 4;
	} else if (type==14) {
		return 5;
	} else if (type==15) {
		return 6;
	} else if (type==16) {
		return 7;
	} else if (type==17) {
		return 9;
	} else if (type==18) {
		return 9;
	}
}
function isUsable(typeId) {
// Можно ли использовать предмет (если его можно надеть - не значит, что его
// можно использовать)
	if (items[typeId][1]==23 || items[typeId][1] == 9000) {
		return true;
	}
	return false;
}
function isMissile(typeId) {
	return typeId == 2300;
}
function isUnique(typeId) {
	return !isUsable(typeId);
}
/**
 * Class represents a set it items that are in the same place. This may be
 * a character's inventory, container like chest, or items lying on the floor 
 * on the same cell.
 * 
 * @constructor
 */
function ItemSet() {
/**
 * @type Object
 * @private
 */
	this.uniqueItems = {};
/**
 * @type Object
 * @private
 */
	this.itemPiles = {};
}
/**
 * Add a UniqueItem or ItemPile to this ItemSet.
 * 
 * @param {UniqueItem|ItemPile}
 */
ItemSet.prototype.addItem = function(item) {
	if (item instanceof UniqueItem) {
		this.uniqueItems[item.itemId] = item;
	} else {
	// If item instanceof ItemPile
		if (this.itemPiles[item.typeId] !== undefined) {
			this.itemPiles[item.typeId].amount += item.amount;
		} else {
			this.itemPiles[item.typeId] = item;
		}
	}
};
/**
 * Creates a new {@link UniqueItem} or {@link ItemPile} object and adds it to 
 * this ItemSet. Note that you do not have to specify if you are adding an 
 * ItemPile or a UniqueItem — methods decides itself what kind if item you are
 * create based on typeId.
 * 
 * @param {Number} typeId Id of item's type.
 * @param {Number} param If type typeId is type of UniqueItem, then this must 
 * be itemId of this item, otherwise, if that is ItemPile type, this must be 
 * amount of items in pile.
 * @returns {mixed} New created {@link UniqueItem} or {@link ItemType}
 */
ItemSet.prototype.addNewItem = function (typeId, param) {
	if (isUnique(typeId)) {
		this.addItem(new UniqueItem(typeId, param));
	} else {
		this.addItem(new ItemPile(typeId, param));
	}
};
/**
 * Removes a {@link UniqueItem} with particular itemId. If ItemSet has no
 * UniqueItem with such itemId, then method throws an Error.
 * 
 * @see UniqueItem
 * @param {Number} itemId
 * @throws {Error} If ItemSet has no {@link UniqueItem} with such itemId.
 */
ItemSet.prototype.removeUnique = function(itemId) {
	if (itemId in this.uniqueItems) {
		delete this.uniqueItems[itemId];
	} else {
		throw new Error("No UniqueItem with itemId "+itemId);
	}
};
/**
 * Removes a particular amount if items from an {@link ItemPile} with 
 * particular typeId. If ItemSet has no ItemPile with such itemId, or doesn't
 * have as many items as you want to take away, then method throws an Error.
 * 
 * @see ItemPile
 * @param {Number} itemId
 * @throws {Error} If ItemSet has no {@link UniqueItem} with such itemId, or
 * if argument amount is bigger than ItemPile.amount.
 */
ItemSet.prototype.removePile = function(typeId, amount) {
	this.itemPiles[typeId].amount -= amount;
	if (this.itemPiles[typeId].amount == 0) {
		delete this.itemPiles[typeId];
	} else if (this.itemPiles[typeId].amount < 0) {
		throw new Error("Предметов отнято больше, чем есть");
	}
};
/**
 * This method works in two ways. If typeId is a type of {@link UniqueItem} 
 * type, it removes a UniqueItem with itemId that equals %param%. Else, if
 * typeId is a type of {@link ItemPile}, then if removes %param% items from 
 * that Pile. 
 * 
 * @see UniqueItem
 * @see ItemPile
 * @see ItemSet#removeUnique
 * @see ItemSet#removePile
 * @param {Number} typeId Id of item's type.
 * @param {Number} param If type typeId is type of UniqueItem, then this must 
 * be itemId of this item, otherwise, if that is ItemPile type, this must be 
 * amount of items in pile.
 */
ItemSet.prototype.remove = function(typeId, param) {
	if (isUnique(typeId)) {
		this.removeUnique(param);
	} else {
		this.removePile(typeId, param);
	}
};
/**
 * Get a {@link UniqueItem} object with particular itemId from this ItemSet.
 * Returns null if there's no UniqueItem with such itemId.
 * 
 * @see ItemSet#getPile
 * @see ItemSet#getItem
 * @param {Number} itemId
 * @returns {UniqueItem} Returns null if there's no UniqueItem with such 
 * itemId.
 */
ItemSet.prototype.getUnique = function(itemId) {
	return this.uniqueItems[itemId] || null;
};
/**
 * Get a {@link ItemPile} object with particular itemId from this ItemSet. 
 * Returns null if there's no ItemPile with such typeId.
 * @see ItemSet#getUnique
 * @see ItemSet#getItem
 * @param {Number} typeId
 * @returns {ItemPile} Returns null if there's no ItemPile with such 
 * typeId.
 */
ItemSet.prototype.getPile = function(typeId) {
	return this.itemPiles[typeId] || null;
};
/**
 * This method works in two ways. If typeId is a type of {@link UniqueItem} 
 * type, it returns a UniqueItem with itemId that equals %param%. Else, if
 * typeId is a type of {@link ItemPile}, then if removes %param% items from 
 * that Pile. Returns null if there's no UniqueItem with such itemId or 
 * ItemPile with such typeId. Try not to abuse this method, because if you know
 * exactly if typeId is type of UniqueItem/ItemType, you better use 
 * corresponding methods — they work much quicker.
 * 
 * @see ItemSet#getUnique
 * @see ItemSet#getPile
 * @param typeId
 * @returns {mixed} Returns UnqiueItem/ItemPile, or returns null if there's no 
 * UniqueItem with such itemId or ItemPile with such typeId.
 */
ItemSet.prototype.getItem = function(typeId, param) {
	if (isUnique(typeId)) {
		return this.uniqueItems[param];
	} else {
		return this.itemPiles[typeId];
	}
	return null;
};
/**
 * Checks if ItemSet has {@link UniqueItem} with particular itemId.
 * 
 * @param {Number} itemId
 * @returns {Boolean} True if has such UniqueItem, false otherwise.
 */
ItemSet.prototype.hasUnique = function(itemId) {
	return this.uniqueItems[itemId] != undefined;
};
/**
 * Checks if ItemSet has more that %amount% of items {@link ItemPile} with 
 * particular typeId, or does it have that ItemPile at all.
 * 
 * @param {Number} typeId
 * @param amount
 * @returns {Boolean} True if has and has enough of such ItemPile, false 
 * otherwise.
 */
ItemSet.prototype.hasPile = function(typeId, amount) {
	if (amount === undefined) {
		amount = 1;
	}
	return this.itemPiles[typeId] !== undefined && this.itemPiles[typeId].amount >= amount;
};
/**
 * Checks if ItemSet contains particular {@link UniqueItem}/{@link ItemPile} 
 * object.
 * 
 * @param {UniqueItem|ItemPile} item 
 * @returns {Boolean} True if it contains such object, false otherwise.
 */
ItemSet.prototype.hasItem = function(item) {
	if (item instanceof UniqueItem) {
		return this.hasUnique(item.itemId);
	} else {
		return this.hasPile(item.typeId, item.amount);
	}
};
/** @private @const */
ItemSet.prototype.PILE = 0xB00B1E5;
/** @private @const */
ItemSet.prototype.UNIQUE = 0xFA1105E5;
/**
 * Returns contents of item set as an array of UniqueItem and ItemPile objects.
 * First go {@link UniqueItem}s, then go {@link ItemPile}s
 * 
 * @returns {mixed[]}
 */
ItemSet.prototype.getValues = function() {
	var answer = [];
	for (var i in this.uniqueItems) {
		answer.push(this.uniqueItems[i]);
	}
	for (var i in this.itemPiles) {
		answer.push(this.itemPiles[i]);
	}
	return answer;
};
/**
 * Removes all contents of this ItemSet.
 */
ItemSet.prototype.empty = function _() {
	this.itemPiles = {};
	this.uniqueItems = {};
};
/**
 * Equipment is class for storing items worn on characters. Essentially it is 
 * a hash map where key is a slot number and value is a {@link UniqueItem}.
 * Equipment consists only of UniqueItems, no ItemPiles.
 * 
 * @constructor
 */
function Equipment() {
/** @private */
	this.items = {};
}
/**
 * Fills Equipment object with particular equipment.
 * 
 * @param {Array} data
 */
Equipment.prototype.getFromData = function(data) {
	for (var slot in data) {
		if (slot == 9 && this.hasItemInSlot(9)) {
			slot = 10;
		}
		this.putOnToSlot(slot, new UniqueItem(data[slot][0], data[slot][1]));
	}
};
/**
 * Put on an item to the slot it belongs to.
 * 
 * @see Equipment#putOnToSlot
 * @param {UniqueItem} item
 */
Equipment.prototype.putOn = function(item) {
	this.items[getSlotFromClass(items[item.typeId][1])] = item;
};
/**
 * Put item to particular slot.
 * 
 * @see Equipment#putOn
 * @param {Number} slot
 * @param {UniqueItem} item
 */
Equipment.prototype.putOnToSlot = function(slot, item) {
	this.items[slot] = item;
};
/**
 * Returns an {@link UniqueItem} object of item in particular slot.
 * @returns {UniqueItem}
 */
Equipment.prototype.getItemInSlot = function(slot) {
	return this.items[slot] || null;
};
/**
 * Searches for {@link UniqueItem} with particular item id.
 * 
 * @see UniqueItem
 * @param {Number} itemId
 * @returns {UniqueItem} Or null if item is not found.
 */
Equipment.prototype.getItemById = function(itemId) {
	for (var i in this.items) {
		if (this.items[i].itemId == itemId) {
			return this.items[i];
		}
	}
	return null;
};
/**
 * Removes particular item from the slot it is. If Equipment doesn't have this
 * item, throws an Error.
 * 
 * @throw {Error}
 */
Equipment.prototype.takeOffItem = function(item) {
	for (var i in this.items) {
		if (this.items[i] == item) {
			delete this.items[i];
			break;
		}
	}
	throw new Error("No item "+item.itemId+" in equipment");
};
/**
 * Checks if Equipment has any item in particular slot.
 * 
 * @returns {Boolean} True if Equipment has an item, false otherwise.
 */
Equipment.prototype.hasItemInSlot = function(slot) {
	return slot in this.items;
};
/**
 * Removes an item in particular slot of this Equipment or throws an Error if
 * this Equipment has no item in that slot.
 * 
 * @param {Number} slot
 * @throws {Error} If this Equipment has no item in that slot.
 */
Equipment.prototype.takeOffFromSlot = function(slot) {
	if (this.items[slot]) {
		delete this.items[slot];
	} else {
		throw new Error("No item in slot "+slot);
	}
};
/** 
 * How many equipment slots are there in game.
 * @public
 * @const
 */
Equipment.prototype.NUMBER_OF_SLOTS = 10;
//function createInventoryItem(typeId, param) {
//	if (isUnique(typeId)) {
//		return new UniqueItem(typeId, param);
//	} else {
//		return new ItemPile(typeId, param);
//	}
//}
/**
 * Class representing a single item that does not stack with other items. This 
 * may be, for example, a piece of equipment. Two UniqueItems of the same type
 * may have different properties (e.g. blessings), unlike {@link ItemPile}s.
 * 
 * @see ItemPile
 * @constructor
 */
function UniqueItem(typeId, itemId) {
/** 
 * A number describing to which type this item relates.
 * @type Number
 * @public 
 */
	this.typeId = typeId;
/** 
 * An identifier, unique for each UniqueItem.
 * @type Number
 * @public 
 */
	this.itemId = itemId;
}
/**
 * Checks if this UniqueItem is a melee weapon or not.
 * 
 * @returns {Boolean}
 */
UniqueItem.prototype.isMelee = function () {
	return !this.isRanged();
};
/**
 * Checks if this UniqueItem is a ranged weapon or not.
 * 
 * @returns {Boolean}
 */
UniqueItem.prototype.isRanged = function(typeId) {
	return items[this.typeId][1]==7;
};
/**
 * Returns a name of this item's type.
 * 
 * @returns {String}
 */
UniqueItem.prototype.toString = function () {
	return items[this.typeId][0];
};
/**
 * Comparator.
 * 
 * @see System#hasEqualObject
 * @param {UniqueItem} a
 * @param {UniqueItem} b
 * @return {Boolean}
 */
UniqueItem.prototype.equals = function(a,b) {
	return a.itemId == b.itemId;
};
UniqueItem.prototype.hashCode = function() {
	return this.itemId;
};
/**
 * Class representing a pile if similar items, none of which has properties
 * different from properties of other items in pile, unlike 
 * {@link UniqueItem}s. These may be arrows, bullets, potions, food, money etc.
 * 
 * @see UniqueItem
 * @param {Number} typeId
 * @param {Number} amount
 */
function ItemPile(typeId, amount) {
/**
 * A number describing to which type this pile relates.
 * @type Number
 * @public 
 */
	this.typeId = typeId;
/**
 * Amount of items in this pile.
 * 
 * @type Number
 * @public
 */
	this.amount = amount;
}
ItemPile.prototype.toString = function () {
	return this.amount+" of "+items[this.typeId];
};
/**
 * Comparator.
 * 
 * @see System#hasEqualObject
 * @param {ItemPile} a
 * @param {ItemPile} b
 * @return {Boolean}
 */
ItemPile.prototype.equals = function(a,b) {
	return a.typeId == b.typeId;
};
ItemPile.prototype.hashCode = function() {
	return this.typeId;
};
