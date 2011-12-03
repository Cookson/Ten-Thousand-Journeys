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
items={2200:["Книга заклинания \"Огненный шар\"", 22, 1,1000],1100:["Круглый щит", 11, 1,100,7,1],1101:["Круглый щит", 11, 1,100,7,1],1102:["Круглый щит", 11, 1,100,7,1],402:["Алебарда", 4, 10,100,13,3,16],1502:["Зелёный плащ", 15, 10,100,1,0],403:["Коса", 4, 10,100,16,6,3],1904:["Кольцо кольца", 19, 10,100,0,0],1503:["Ботинки марунарх", 15, 1,100,2,-1],400:["Копьё", 4, 10,100,6,3,120],1500:["Серый плащ", 15, 10,100,1,0],401:["Копьё", 4, 10,100,6,3,120],1501:["Красный плащ", 15, 10,100,1,0],404:["Коса", 4, 10,100,16,6,3],700:["Короткий лук", 7, 1,100,7,1,11],1900:["Кольцо мудрости", 19, 10,100,0,0],1901:["Кольцо силы", 19, 10,100,0,0],1902:["Кольцо смерти", 19, 10,100,0,0],1903:["Обручальное кольцо", 19, 10,100,0,0],305:["Золотая булава", 3, 100,100,9,2,15],304:["Цеп", 3, 100,100,9,2,15],1401:["Железные перчатки", 14, 1,100,1,0],1400:["Перчатки марунарх", 14, 1,100,2,0],1402:["Кожаные перчатки", 14, 1,100,1,0],1505:["Кожаные сапоги", 15, 1,100,1,0],1504:["Железные ботинки", 15, 1,100,1,-1],1506:["Ботинки из грубой ткани", 15, 1,10,1,0],900000:["Золото", 9000, 0,1],300:["Булава", 3, 100,100,8,3,14],301:["Моргенштерн", 3, 100,100,10,-1,15],302:["Моргенштерн", 3, 100,100,10,-1,15],303:["Моргенштерн", 3, 100,100,10,-1,15],1305:["Рубашка из грубой ткани", 13, 1,10,1,-1],1304:["Кожаный доспех", 13, 1,100,4,-2],201:["Секира", 2, 22,160,14,-2,17],200:["Топор", 2, 18,100,7,3,13],202:["Боевой топор", 2, 28,200,26,13,13],1303:["Огненная роба", 13, 1,100,3,0],1302:["Льняная роба", 13, 1,100,1,0],1301:["Кольчуга", 13, 1,100,6,-4],1300:["Доспех марунарх", 13, 1,100,7,-5],102:["Сабля", 1, 10,130,7,4,12],103:["Гладиус", 1, 14,140,8,4,14],2300:["Стрела", 23, 1,1000],100:["Короткий меч", 1, 34,100,6,4,11],101:["Меч", 1, 12,100,7,4,13],504:["Посох", 5, 5,10,100,7,6],500:["Посох", 5, 5,10,100,7,6],501:["Посох", 5, 5,10,100,7,6],502:["Посох", 5, 5,10,100,7,6],503:["Посох", 5, 5,10,100,7,6],104:["Сабля", 1, 10,130,7,4,13],1202:["Кожаный шлем", 12, 1,100,1,0],1200:["Шлем марунарх", 12, 1,100,2,-1],1201:["Железный шлем", 12, 1,100,2,-1],601:["Кинжал", 6, 10,100,6,4,10],600:["Нож", 6, 10,100,5,3,10]};
// Функции оружия
function isWeapon(typeId) {
	return items[typeId][1]>=1 & items[typeId][1]<=10;
}
function isEquipment(typeId) {
	return items[typeId][1]>=1 & items[typeId][1]<=21;
}
function isRanged(typeId) {
// Оружие дальнего боя или нет
	if (typeId==undefined) {
		return false;
	}
	if (items[typeId][1]==7) {
		return true;
	}
	return false;
}
function isMelee(typeId) {
// Оружие ближнего боя или нет
	if (typeId==undefined) {
		return true;
	}
	return !isRanged(typeId);
}
function getSlotFromClass(type) {
// Получить слот предмета из его типа
	if (type>=1 && type<=10) {
	// Оружие
		return 0;
	} else if (type==11) {
		return 1;
	} else if (type==12) {
		return 2;
	} else if (type==13) {
		return 3;
	} else if (type==14) {
		return 4;
	} else if (type==15) {
		return 5;
	} else if (type==16) {
		return 6;
	} else if (type==17) {
		return 7;
	} else if (type==18) {
		return 8;
	} else if (type==19) {
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
function isUnique(typeId) {
	return !isUsable(typeId);
}
function ItemMap() {
	this.uniqueItems = {};
	this.itemPiles = {};
}
ItemMap.prototype.addItem = function(item) {
	if (item.isUnique) {
		this.uniqueItems[item.itemId] = item;
	} else {
		if (this.itemPiles[item.typeId] !== undefined) {
			this.itemPiles[item.typeId].amount += item.amount;
		} else {
			this.itemPiles[item.typeId] = item;
		}
	}
	return item;
};
ItemMap.prototype.removeUnique = function(itemId) {
	delete this.uniqueItems[itemId];
};
ItemMap.prototype.removePile = function(typeId, amount) {
	this.itemPiles[typeId].amount -= amount;
	if (this.itemPiles[typeId].amount == 0) {
		delete this.itemPiles[typeId];
	} else if (this.itemPiles[typeId].amount <= 0) {
		throw new Error("Предметов отнято больше, чем есть");
	}
};
ItemMap.prototype.remove = function(typeId, param) {
	if (isUnique(typeId)) {
		this.removeUnique(param);
	} else {
		this.removePile(typeId, param);
	}
};
ItemMap.prototype.getUnique = function(itemId) {
	return this.uniqueItems[itemId];
};
ItemMap.prototype.getPile = function(typeId) {
	return this.itemPiles[typeId];
};
ItemMap.prototype.getItem = function(typeId, param) {
	if (isUnique(typeId)) {
		return this.uniqueItems[param];
	} else {
		return this.itemPiles[typeId];
	}
};
ItemMap.prototype.hasUnique = function(itemId) {
	return this.uniqueItems[itemId] != undefined;
};
ItemMap.prototype.hasPile = function(typeId, amount) {
	if (amount === undefined) {
		amount = 1;
	}
	return this.itemPiles[typeId] !== undefined && this.itemPiles[typeId].amount >= amount;
};
ItemMap.prototype.hasItem = function(item) {
	if (item.isUnique) {
		return this.hasUnique(item.itemId);
	} else {
		return this.hasPile(item.typeId, item.amount);
	}
	
};
ItemMap.prototype.PILE = 90834579;
ItemMap.prototype.UNIQUE = 158752563;
ItemMap.prototype.getValues = function() {
	var answer = [];
	for (var i in this.uniqueItems) {
		answer.push(this.uniqueItems[i]);
	}
	for (var i in this.itemPiles) {
		answer.push(this.itemPiles[i]);
	}
	return answer;
};
function Ammunition() {
	this.items = {};
	
}
Ammunition.prototype.putOn = function(item) {
	this.items[getSlotFromClass(items[item.typeId][1])] = item;
};
Ammunition.prototype.putOnToSlot = function(slot, item) {
	this.items[slot] = item;
};
Ammunition.prototype.getItemInSlot = function(slot) {
	return this.items[slot];
};
Ammunition.prototype.getItemById = function(itemId) {
	for (var i in this.items) {
		if (this.items[i].itemId == itemId) {
			return this.items[i];
		}
	}
};
Ammunition.prototype.takeOffItem = function(item) {
	for (var i in this.items) {
		if (this.items[i] == item) {
			delete this.items[i];
			return true;
		}
	}
	throw new Error("No item "+item.itemId+" in ammunition");
};
Ammunition.prototype.hasItemInSlot = function(slot) {
	return !!this.items[slot];
};
Ammunition.prototype.takeOffFromSlot = function(slot) {
	if (this.items[slot]) {
		delete this.items[slot];
	} else {
		throw new Error("No item in slot "+slot);
	}
};
Ammunition.prototype.NUMBER_OF_SLOTS = 10;
function createInventoryItem(typeId, param) {
	if (isUnique(typeId)) {
		return UniqueItem(typeId, param);
	} else {
		return ItemPile(typeId, param);
	}
}
function UniqueItem(typeId, itemId) {
	this.typeId = typeId;
	this.itemId = itemId;
	this.isUnique = true;
}
function ItemPile(typeId, amount) {
	this.typeId = typeId;
	this.amount = amount;
	this.isUnique = false;
}