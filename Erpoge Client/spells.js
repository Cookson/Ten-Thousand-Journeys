// Заклинания
/*
name - отображающееся имя на родном языке пользователя
description - описание
mana - расход маны на заклинание
onlyOnSelf - колдующий может использовать заклинание только на себя
onCharacter - целью заклинания может быть персонаж
onCell - целью заклинания может быть клетка
effect - лиентский код самого заклинания 
*/
spells=[];
spells[1]={ 
	name:"Лечение",
	description:"Лечит слабые раны",
	mana:15,
	onlyOnSelf:false,
	onCharacter:true,
	onCell:false,
	effect:
		function(self, aim, x, y, callback) {
			aim.graphicEffect("blueSparkles", callback);
		}
};
spells[2]={ name:"Огненный шар",
	description:"Посылает в противника огненный шар",
	mana:10,
	onlyOnSelf:false,
	onCharacter:true,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			// if (aim!=-1) {
				// aim.graphicEffect("firesparks",callback);
			// }
			self.rangedAttack(((aim==-1)?x:aim.x),((aim==-1)?y:aim.y), "fireball", function() {
				if (aim!=-1) {
					aim.graphicEffect("firesparks",callback);
				}
			});
		},
	onOccupiedCell:true
};
spells[3]={ 
	name:"Яд",
	description:"Отравляет противника",
	mana:10,
	onlyOnSelf:false,
	onCharacter:true,
	onCell:false,
	effect:
		function(self, aim, x, y, callback) {
			if (aim!=-1) {
				aim.graphicEffect("poison");
			}
		}
};
spells[4]={ 
	name:"Вызвать монстра",
	description:"Вызывает монстра",
	mana:10,
	onlyOnSelf:false,
	onCharacter:false,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			
		},
	onOccupiedCell:false
};
spells[5]={ name:"Изменить пол",
	description:"Изменяет пол",
	mana:10,
	onlyOnSelf:false,
	onCharacter:false,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			
		},
	onOccupiedCell:true
};
spells[6]={ name:"Открыть дверь",
	description:"Открывает дверь",
	mana:10,
	onlyOnSelf:false,
	onCharacter:false,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			
		},
	onOccupiedCell:true
};
spells[7]={ name:"Удалить объект",
	description:"Удаляет объект",
	mana:10,
	onlyOnSelf:false,
	onCharacter:false,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			
		},
	onOccupiedCell:true
};
spells[8]={ name:"Создать объект",
	description:"Создаёт объект",
	mana:10,
	onlyOnSelf:false,
	onCharacter:false,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			
		},
	onOccupiedCell:false
};
spells[9]={ name:"Создать предмет",
	description:"Создаёт предмет",
	mana:10,
	onlyOnSelf:false,
	onCharacter:false,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			
		},
	onOccupiedCell:true
};
spells[10]={ name:"Ледяной болт",
	description:"Посылает в противника ледяной болт",
	mana:10,
	onlyOnSelf:false,
	onCharacter:true,
	onCell:true,
	effect:
		function(self, aim, x, y, callback) {
			// if (aim!=-1) {
				// aim.graphicEffect("firesparks",callback);
			// }
			self.rangedAttack(((aim==-1)?x:aim.x),((aim==-1)?y:aim.y), "icebolt", function() {
				if (aim!=-1) {
					aim.graphicEffect("iceshivers",callback);
				}
			});
		},
	onOccupiedCell:true
};