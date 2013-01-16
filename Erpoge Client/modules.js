var Modules = {
		
};
function Module(name, author, version) {
	this._name = name;
	this._author = author;
	this._version = version;
	console["log"]("Loding module "+name);
}
/**
 * @param {string} name Unique name of element type. You can start it with your
 * name, so it won't conflict with other contributor's element types, for 
 * example "cooksonSpeechBubble". 
 * @param {Object} element 
 */
Module.prototype.registerGameFieldElementType = function(name, element) {
	UIGameFieldElementTypes[name] = element;
	element._module = this;
};
/**
 * @param {string} name Unique name of element type. You can start it with your
 * name, so it won't conflict with other contributor's element types, for 
 * example "cooksonIconsEquipment". 
 * @param {Object} element 
 */
Module.prototype.registerUIElementType = function(name, element) {
	element._module = this;
	UIElementTypes[name] = element;
};