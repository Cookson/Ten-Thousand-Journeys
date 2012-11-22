(function() {
	var instance = {};
	var Data = instance;

	/** @public @type number */
	Object.defineProperty(instance, "PASSABILITY_FREE", {
		writable: false,
		configurable: false,
		enumerable: true,
		value: -1
	});
	/** @public @type number */
	Object.defineProperty(instance, "PASSABILITY_SEE", {
		writable: false,
		configurable: false,
		enumerable: true,
		value: 3
	});
	/** @public @type number */
	Object.defineProperty(instance, "PASSABILITY_BLOCKED", {
		writable: false,
		configurable: false,
		enumerable: true,
		value: 1
	});
	/** @public @type number */
	Object.defineProperty(instance, "OBJECT_TYPE_COMMON", {
		writable: false,
		configurable: false,
		enumerable: true,
		value: 1
	});
	/** @public @type number */
	Object.defineProperty(instance, "OBJECT_TYPE_WALL", {
		writable: false,
		configurable: false,
		enumerable: true,
		value: 2
	});
	/** @public @type number */
	Object.defineProperty(instance, "OBJECT_TYPE_DOOR", {
		writable: false,
		configurable: false,
		enumerable: true,
		value: 3
	});
	/**
	 * @singleton
	 *
	 * Holds all the static information about various possible game 
	 * entity types: items, objects, floors, characters etc.
	 */
	var data = {
		/**
		 * floorTypeId: floorTilesNamePrefix
		 */
		/**
		 * wallTypeId: wallImageNamePrefix
		 */
		objects: {
			1	: [32,52,1],
			3	: [32,52,1],
			4	: {
					imgw: 32,
					imgh: 52,
					passability: Data.PASSABILITY_BLOCKED,
					type: Data.OBJECT_TYPE_WALL,
					wallName: "greyStoneWall"
				},
			5	: [32,52,1],
			6	: [32,52,3],
			7	: [32,52,1],
			402	: [32,32,0],
			403	: {
				imgw:32,
				imgh:32,
				passability: Data.PASSABILITY_FREE,
				type: Data.OBJECT_TYPE_COMMON
			},
			400	: [32,32,0],
			401	: [32,32,1],
			307	: [28,52,1],
			309	: [28,52,1],
			308	: [28,52,1],
			311	: [28,52,1],
			310	: [28,52,1],
			313	: [28,52,1],
			42	: {
				imgw:32,
				imgh:42,
				passability: Data.PASSABILITY_BLOCKED,
				useability:1,
				type: Data.OBJECT_TYPE_DOOR
			},
			312	: [28,52,1],
			43	: [32,42,0,1],
			315	: [28,52,1],
			314	: [28,52,1],
			41	: {
				imgw: 32,
				imgh: 42,
				passability: Data.PASSABILITY_FREE,
				useability: 1,
				type: Data.OBJECT_TYPE_DOOR
			},
			316	: [28,52,1],
			44	: [32,42,1,1],
			51	: [32,64,1],
			55	: [64,64,1],
			54	: [32,64,1],
			53	: [96,96,1],
			904	: [64,64,1],
			52	: [64,64,1],
			903	: [32,40,1],
			902	: [64,64,0],
			901	: [64,64,0],
			57	: [42,64,1],
			900	: [36,36,0],
			56	: [42,64,1],
			63	: [32,32,3,1],
			300	: [32,42,1],
			62	: [32,32,3,1],
			301	: [32,42,1],
			61	: [32,32,3,1],
			302	: [24,42,1],
			60	: [32,32,3,1],
			70	: [32,58,0],
			71	: [32,50,0],
			201	: [32,58,0],
			200	: [70,70,1],
			203	: [32,34,3],
			202	: [32,32,3],
			76	: [32,58,0],
			77	: [32,58,0],
			78	: [32,32,3],
			79	: [32,32,3],
			72	: [32,50,0],
			73	: [32,32,3],
			74	: [32,32,3],
			75	: [42,32,3],
			81	: [32,32,3],
			80	: [32,32,3],
			83	: [32,58,0],
			82	: [32,32,1],
			90	: [32,32,3]
		},
		soundTypes: {
			1 : {name : "roar"},
			2 : {name : "scream"},
			3 : {name : "steps"},
			4 : {name : "waterFlow"},
			5 : {name : "crash"},
			6 : {name : "lightning"},
			7 : {name : "speech"},
			8 : {name : "workingMechanisms"},
			9 : {name : "bell"}
		},
		images: {
			chardoll: [2, 34, 58, 59, 60, 61, 600, 601, 700, 1201, 1202, 1300, 1302, 100, 1304, 1402, 1505, 1501, 1504, 104, 1401, 403, 402, 1301],
			particles: ["shiver1", "shiver2", "blood1", "spark1", "spark2", "yellow_spark1", "yellow_spark2"],
			// Index — id of wall, value — 
			walls: 7,
			objects: [],
			floors: [1, 2, 23, 5, 7, 1, 11, 2, 2]
		}
	};
	/**
	 * Returns information about various in-game entity types.
	 * It works close to the SQL style: you select values by three things:
	 * entity class (floors, objects, characters etc.), param name (width, 
	 * passability, name) and id (always a number).
	 * So basically data is stored in 3-dimensional array as
	 * data[entityType][entityId][param].
	 * 
	 * @param {object} what An object describing which 
	 * @param {string} what.cls The class of entity (floors, objects, characters etc.)
	 * @param {string} what.param The name of parameter you are looking for.
	 * @param {number} what.id The id of the object type.
	 * @returns {mixed} Any value that is stored for this type.
	 * @example
	 * StaticData.get({
	 * 	param: "imgw",
	 * 	cls: "object",
	 * 	id: 4
	 * }); // This returns the width of the image of an object type
	 * // With id 4 (a gray stone wall).
	 */
	instance.get = function(what) {
		if (
			!("cls" in what) ||
			!("param" in what) ||
			!("id" in what)
		) {
			throw new Error("To get params of entity types you should pass objects of this form: {param:'string',cls:'string',id:number}");
		}
		if (!(what.cls in data)) {
			throw new Error("No entity class '"+what.cls+"'!");
		}
		if (!(what.param in data[what.cls][what.id])) {
			throw new Error("No param '"+what.param+"' at id "+what.id+" of class '"+what.cls+"'!");
		}
		if (!(what.id in data[what.cls])) {
			throw new Error("No id "+what.id+" in entity class '"+what.cls+"'!");
		}
		return data[what.cls][what.id][what.param];
		
	};
	/**
	 * @name StaticData#getRawClass
	 * Returns an object containing all the data about entities of a certain class.
	 *
	 * @param {string} cls A class of game entities (floors, objects, characters,
	 * 	sounds etc.)
	 * @returns {object}
	 */
	instance.getRawClass = function(cls) {
		if (!(cls in data)) {
			throw new Error("There is no class of game entities named '"+cls+"'");
		}
		return data[cls];
	}
	/**
	 * @name StaticData#object
	 * 
	 * Returns and object describing game objects of a particular id. This is a 
	 * efficient, specific and easy-to-use analogue for {@link StaticData#get}
	 * 
	 * @param {number} objectId Id of game object
	 * @return {object}
	 */
	instance.object = function(objectId) {
		return data.objects[objectId];
	};
	/**
	 * @name StaticData#floor
	 * 
	 * Returns and object describing floor of a particular id This is a 
	 * efficient, specific and easy-to-use analogue for {@link StaticData#get}
	 * 
	 * @param {number} floorId Id of floor
	 * @return {object}
	 */
	instance.floor = function(floorId) {
		return data.floors[floorId];
	};
	window.StaticData = instance;
})();
