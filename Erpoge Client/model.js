(function() { // World
	var /** @type Data */ instance;
	var chunkWidth = 20;
	/** @private @type Map2D */
	var chunks = new Map2D();
	/**
	 * @constructor
	 * @singleon
	 */
	function World() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		// sharedInstance is an object that can be used by other code
		// to access private data of Data singletone
		var sharedInstance = {};
		Object.defineProperty(sharedInstance, "chunkWidth", {
			get: function() {
				return chunkWidth;
			}
		});
		Object.defineProperty(sharedInstance, "chunks", {
			writable: false,
			enumerable: true,
			configurable: false,
			value: chunks
		});
		Object.defineProperty(sharedInstance, "removeObject", {
			writable: false,
			enumerable: true,
			configurable: false,
			value: removeObject
		});
		/**
		 * Returns a cell by its absolute coordinates.
		 * 
		 * @param {number} x
		 * @param {number} y
		 * @return {Cell}
		 */
		Object.defineProperty(sharedInstance, "getCell", {
			writable: false,
			configurable: false,
			enumerable: true,
			value: getCell
		});
		Object.defineProperty(sharedInstance, "Chunk", {
			writable: false,
			configurable: false,
			enumerable: true,
			value: Chunk
		});
		Object.defineProperty(sharedInstance, "setChunkWidth", {
			writable: false,
			configurable: false,
			enumerable: true,
			value: setChunkWidth
		});
		Object.defineProperty(sharedInstance, "getChunkRoundedCoord", {
			writable: false,
			configurable: false,
			enumerable: true,
			value: getChunkRoundedCoord
		});
		function setChunkWidth(width) {
			chunkWidth = width;
		}
		/**
		 * Chunks represent a square part of terrain
		 * @constructor
		 * @param {number} x x coordinate of the top left corner of a chunk
		 * @param {number} y y coordinate of the top left corner of a chunk
		 */
		function Chunk(x, y) {
			this.x = x;
			this.y = y;
			this.cells = blank2dArray(chunkWidth, chunkWidth);
			this.canvas = document.createElement("canvas");
			this.canvas.setAttribute("width", 32*chunkWidth);
			this.canvas.setAttribute("height", 32*chunkWidth);
			this.canvas.style.position = "absolute";
			var viewIndent = GameField.getViewIndentation(x, y, 32);
			this.canvas.style.top = viewIndent.top+"px";
			this.canvas.style.left = viewIndent.left+"px";
			this.canvas.style.zIndex = 0;
			document.getElementById("gameField").appendChild(this.canvas);
		}
		Chunk.prototype.loadData = function(data) {
		/*
		 * data: {x:int,y:int,c:[floor,object, floor,object...],ch:[]}
		 */
			for (var i=0, x=0, y=0; i<data.c.length; i+=2) {
				this.cells[x][y] = {};
				if (x === chunkWidth-1) {
					y++;
					x = 0;
				} else {
					x++;
				}
			}
			for (var i=0, x=0, y=0; i<data.c.length; i+=2) {
			// Each cell has floor — set its floor
				var cell = this.cells[x][y];
				cell.f = data.c[i];
				if ((id = data.c[i+1]) !== 0) {
				// If cell has object, set its object
					cell.o = id;
					cell.p = StaticData.object(id).passability
				}
				if (x === chunkWidth-1) {
					y++;
					x = 0;
				} else {
					x++;
				}
			}
			// Items loading
			for (var i=0, x=0, y=0; i<data.i.length; i+=4) {
				var item;
				if (isUnique(data.i[i+2])) {
					item = new UniqueItem(data.i[i+2], data.i[i+3]);
				} else {
					item = new ItemPile(data.i[i+2], data.i[i+3]);
				}
				World.createItem(data.i[i],data.i[i+1],item);
				if (x === chunkWidth-1) {
					y++;
					x = 0;
				} else {
					x++;
				}
			}
			// Characters loading
			
			for (var i=0; i<data.ch.length; i++) {
				characters[data.ch[i][0]] = new Character(data.ch[i][0], 
						data.ch[i][3], data.ch[i][1]+data.x, data.ch[i][2]+data.y,
						data.ch[i][11], data.ch[i][6], data.ch[i][7]);
				characters[data.ch[i][0]].display();
			}
		};
		Chunk.prototype.getAbsoluteCell = function(x, y) {
			return this.cells[x-this.x][y-this.y];
		};
		function getCell(x, y) {
			var rx = getChunkRoundedCoord(x);
			var ry = getChunkRoundedCoord(y);
			return chunks.get(rx, ry).cells[x-rx][y-ry];
		}
		function getChunkRoundedCoord(coord) {
			return (coord < 0) ? coord-((coord%chunkWidth==0) ? 0 : chunkWidth)-coord%chunkWidth : coord-coord%chunkWidth;
		}
		function removeObject(x, y) {
			var cell = getCell(x, y);
			delete cell.object;
			cell.p = StaticData.PASSABILITY_FREE;
		}
		function createChunk(x, y, data) {
		}
		/**
		 * Get id of floor on a particular cell
		 * 
		 * @param {number} x
		 * @param {number} y
		 * 
		 * @returns {number}
		 */
		this.floor = function (x, y) {
			return getCell(x, y).f;
		};
		/**
		 * Get the character on a particular cell
		 * 
		 * @param {number} x
		 * @param {number} y
		 * 
		 * @returns {function} Character function of null if there is no character
		 */
		this.character = function (x, y) {
			return getCell(x, y).character || null
		};
		/**
		 * Get id of object on a particular cell
		 * 
		 * @param {number} x
		 * @param {number} y
		 * 
		 * @returns {mixed} Id of object or null if there is no object
		 */
		this.object = function(x, y) {
			return getCell(x, y).o || null;
		};
		/**
		 * Get the type of passability of object on a particular cell
		 * 
		 * @param {number} x
		 * @param {number} y
		 * 
		 * @returns {number} Code of the passability type
		 */
		this.passability = function(x, y) {
			return getCell(x, y).p;
		};
		/**
		 * Returns an object with methods that allow access to some otherwise
		 * private data
		 * 
		 * @returns {Object}
		 */
		this.getInstance = function() {
			return sharedInstance;
		};
		return instance = this;
	}
	
	window.World = new World();
})();

(function() { // Characters
	var instance;
	var characters = {};
	var init;
	var worldData = World.getInstance();
	/** 
	 * @singleton
	 * Holds all the data about loaded characters.
	 * 
	 * This object provides methods for Model-level data manipulation, but not
	 * in usual way and only for engine parts, not for modules: an engine part
	 * may call {@link Characters#getInstance} go get an objects that contains
	 * methods for data manipulation. This object is meant not to be disposed
	 * by engine parts of code to module parts of code.
	 * 
	 */
	function CharactersConstructor() {
		if (typeof instance !== "undefined") {
			return instance;
		}
		/**
		 * Contains character objects as {characterId: characterObject}.
		 * Contains the {@link ClientPlayer} character at characters.player
		 * (after the ClientPlayer character is loaded from server).
		 */
		var characters = {};
		/**
		 * Creates a data piece that represents a character.
		 * This method must only be used by the engine, not by modules.
		 * 
		 * @param {object} params An object describing a character
		 */
		var createCharacter = function(params) {
			var character = {
				x: params.x,
				y: params.y,
				name: params.name,
				characterId: params.characterId,
				hp: params.hp,
				maxHp: params.maxHp,
				mp: params.mp,
				maxMp: params.maxMp,
				fraction: params.fraction,
				items: new ItemSet(),
				equipment: new Equipment(),
				effects: {},
				isPlayer: params.cls ? true : false
			};
			var inventory = params.inventory;
			// Inventory
			for (var i=0; i<inventory.length; i+=2) {
				var typeId = inventory[i];
				var param = inventory[i+1];
				var item;
				if (isUnique(typeId)) {
					item = new UniqueItem(typeId, param);
					character.items.add(item);
				} else {
					item = new ItemPile(typeId, param);
					character.items.add(item);
				}
			}
			worldData.getCell(character.x, character.y).character = character;
			worldData.getCell(character.x, character.y).p = StaticData.PASSABILITY_SEE;
			characters[params.characterId] = character;
			Object.defineProperty(Characters, params.characterId, {
			/* Create a special property in Characters singletone:
			 * non-writable and non-configurable, but enumerable (so one
			 * can iterate throughall the characters by 
			 * for (var i in Characters) { ...
			 */
				value: function(property) {
					if (character[property] === undefined) {
						throw new Error("Character object doesn't have property "+property);
					}
					return character[property];
				},
				writable: false,
				configurable: false,
				enumerable: true
			});
			return Characters[params.charactersId];
		};
		/**
		 * Create a player's character daatobject and assign it to two 
		 * properties of {@link Characters} object: 
		 * Characters[player's_characterId] and Characters.player
		 */
		var createPlayer = function(params) {
		/*
		 * data : [(0)x, (1)y, (2)characterId, (3)name,
		 * (4)race, (5)class, (6)maxHp, (7)maxMp, (8)maxEp, (9)hp, (10)mp, (11)ep,
		 * (12)str, (13)dex, (14)wis, (15)itl, (16)items[], (17)equipment[],
		 * (18)spells[], (19)skills[], (20)ac, (21)ev, (22)resistances[]]
		 */
			var player = {
				x: params[0],
				y: params[1],
				characterId: params[2],
				name: params[3],
				race: params[4],
				cls: params[5],
				maxHp: params[6],
				maxMp: params[7],
				maxEp: params[8],
				hp: params[9],
				mp: params[10],
				ep: params[11],
				str: params[12],
				dex: params[13],
				wis: params[14],
				itl: params[15],
				items: new ItemSet(params[16]),
				equipment: new Equipment(params[17]),
				spells: params[18],
				skills: params[19],
				ac: params[20],
				ev: params[21],
				resistances: params[22],
				fraction: 1,
			};
			worldData.getCell(player.x, player.y).character = player;
			worldData.getCell(player.x, player.y).passability = StaticData.PASSABILITY_SEE;
			Object.defineProperty(window.Characters, params[2], {
			/* Create a special property in Characters singletone:
			 * non-writable and non-configurable, but enumerable (so one
			 * can iterate throughall the characters by 
			 * for (var i in Characters) { ...
			 */
				value: function(property) {
					if (player[property] === undefined) {
						throw new Error("Character object doesn't have property "+property);
					}
					return player[property];
				},
				writable: false,
				configurable: false,
				enumerable: true
			});
			Object.defineProperty(window.Characters, "player", {
				value: window.Characters[player.characterId],
				writable: false,
				configurable: false,
				enumerable: false
			});
			characters[player.characterId] = player;
			/**
			 * A property containing the player's character data object
			 * @memberOf Character
			 * @name player
			 * @see Character
			 */
			sharedInstance.player = player;
			return Characters[params.charactersId];
		};
		var removeCharacter = function(characterId) {
			var character = characters[characterId];
			delete worldData.getCell(character.x,character.y).character;
			delete characters[characterId];
		};
		
		this.hasItem = function(characterId, typeId, param) {
			// Имеет пресонаж предмет или заданное кол-во предметов
			throw new Error("Not implemented!");
			if (isUnique(typeId)) {

			} else {
				if (param==undefined) {
					var param = 1;
				}
				for ( var i = 0; i<this.itemPiles.length; i++ ) {
					if (this.itemPiles[i][0]==item && this.itemPiles[i][1]>=num) {
						return true;
					}
				}
				return false;
			}
		};
		this.isEnemy = function(characterId, aimId) {
			if (
				characters[characterId].fraction != characters[aimId].fraction
				&& characters[characterId].fraction != -1 
				&& characters[aimId].fraction != -1
			) {
				// Если найденный character не союзник этому character и если они оба —
				// не нейтральные монстры, то они враги
				return true;
			}
			return false;
		};
		this.hasEffect = function(characterId, effectId) {
			// Проверка, имеет ли персонаж определённый эффект
			throw new Error("Not implemented!");
			return false;
		};
		this.findCharacterByCoords = function(x, y) {
			for (var i in characters) {
				if (characters[i].x==x && characters[i].y==y) {
					return characters[i];
				}
			}
			return null;
		};
		/**
		 * Returns a copy of an object that provides manipulation of characters
		 * data. This should be called before the engine is init.
		 */
		var sharedInstance = {
			createCharacter: createCharacter,
			removeCharacter: removeCharacter,
			characters: characters,
			createPlayer: createPlayer,
			player: null // This will be substituted with the player's object
							// as soon as player is loaded.
		};
		this.getInstance = function() {
			return sharedInstance;
		};
		return instance = this;
	}
	window.Characters = new CharactersConstructor();
})();

