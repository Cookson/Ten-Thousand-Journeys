/**
 * Class that provides easy interface for creating simple DOM structuress.
 * The most of it's methods return this MarkupMaker object, so you can call 
 * methods in chain.
 * 
 * @constructor
 * @this {MarkupMaker}
 */
function MarkupMaker() {
	/** @private */ this.fragment = document.createDocumentFragment();
	/** @private */ this.lastChild = null;
}
/**
 * Adds new node to the end of the fragment
 * 
 * @param {String} tagName
 * @return {MarkupMaker}
 */
MarkupMaker.prototype.add = function(tagName) {

	this.lastChild = document.createElement(tagName);
	this.fragment.appendChild(this.lastChild);
	return this;
};
/**
 * Put text to the last element
 * 
 * @param {String} text
 * @return {MarkupMaker}
 */
MarkupMaker.prototype.text = function(text) {
	this.lastChild.appendChild(document.createTextNode(text));
	return this;
};
/**
 * Change style of the last element.
 * 
 * @params {Object} params - object, where keys are property names in JavaScript style 
 * (borderColor instead of border-color etc) and values are values of 
 * style properties, for example:
 * {color: "#fde", textAlign: "center"}
 * @return {MarkupMaker}
 */
MarkupMaker.prototype.style = function(params) {
	for (var i in params) {
		this.lastElement.style[i] = params[i];
	}
	return this;
};
/**
 * Set CSS class of the last element. Several calls in a row may set multiple
 * classes for one element, for example:
 * 
 * new MarkupMaker().add("div").cls("description").cls("item").complete();
 * @param {String} cls Class name
 * @return {MarkupMaker}
 */
MarkupMaker.prototype.cls = function(cls) {
	this.lastElement.addClass(cls);
};
/**
 * Complete HTML structure creation and return DocumentFragment object.
 * DocumentFragment object can be inserted to DOM simply with:
 * 
 * element.appendChild(documentFragment);
 * 
 * or any similar methods, so all the elements from that DocumentFragment will 
 * be placed to DOM at that particular place.
 * 
 * @return {DocumentFragment}
 */
MarkupMaker.prototype.getDocumentFragment = function() {
	return this.fragment;
};
/**
 * Returns a div with all contents inside.
 * 
 * @return {HTMLDivElement}
 */
MarkupMaker.prototype.getWrappedContents = function() {
	var div = document.createElement("div");
	div.appendChild(this.fragment);
	return div;
};