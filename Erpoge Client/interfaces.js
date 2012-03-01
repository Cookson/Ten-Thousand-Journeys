var Interface = {
	Iterable: function(object) {
		return object.forEach instanceof Function;
	},
	IterableWithContext: function(object) {
		return (object.forEach instanceof Function
			&& object.forEach.length === 2);
	},
	Hashable: function(object) {
		return object.hashCode instanceof Function;
	},
	Arrayable: function(object) {
		return object.getValues instanceof Function;
	}
};
Interface.objectImplements = function(object, interfaceName) {
	if (typeof object !== "object") {
		throw new Error(object+" is not an object");
	}
	if (Interface[interfaceName] === undefined) {
		throw new Error(interfaceName+" is not a correct interface");
	}
	return Interface[interfaceName](object);
};
Interface.check = function(object, interfaceName) {
	if (!Interface[interfaceName](object)) {
		throw new Error("Object "+object.__proto__.constructor.name+
				" does not implement interface "+interfaceName);
	}
};