var Interface = {
	Iterable: function(object) {
		return (object.forEach instanceof Function
			&& object.forEach.length == 2);
	},
	Hashable: function(object) {
		return object.hashCode instanceof Function;
	},
	Arrayable: function(object) {
		return object.getValues instanceof Function;
	}
};
Interface.objectImplements = function(object, interfaceName) {
	return Interface[interfaceName](object);
};
Interface.check = function(object, interfaceName) {
	if (!Interface[interfaceName](object)) {
		throw new Error("Object "+object.__proto__.constructor.name+
				" does not implement interface "+interfaceName);
	}
};