/* keys.js: Default control settings
 */
onLoadEvents['keys'] = function _() {
	Keys.assign({
		action: "leaveLocation", 
		keys: ["Shift", ","],
		mode: UI.MODE_IN_LOCATION
	});
	Keys.assign({
		action: "enterLocation", 
		keys: ["Shift", "."], 
		mode: UI.MODE_ON_GLOBAL_MAP
	});
	Keys.assign({
		action: "quickRefresh", 
		keys: ["R"], 
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "focusOnChat", 
		keys: ["T"], 
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "sendToChat", 
		keys: ["Enter"], 
		mode: UI.MODE_CHAT
	});
	Keys.assign({
		action: "closeChat", 
		keys: ["Esc"], 
		mode: UI.MODE_CHAT
	});
	Keys.assign({
		action: "toggleUI", 
		keys: ["Ctrl", "F1"], 
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "toggleSkillsWindow", 
		keys: ["N"],
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "toggleSettingsWindow", 
		keys: ["F4"],
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "closeContainer", 
		keys: ["Esc"],
		mode: UI.MODE_CONTAINER
	});
	Keys.assign({
		action: "takeAllFromContainer", 
		keys: ["A"],
		mode: UI.MODE_CONTAINER
	});
	Keys.assign({
		action: "rotateCamera", 
		keys: ["C"],
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "idle", 
		keys: ["S"],
		mode: UI.MODE_ALWAYS
	});
	/* Cursor actions */
	Keys.assign({
		action: "selectMissile", 
		keys: ["F"],
		mode: UI.MODE_ALWAYS
	});
	Keys.assign({
		action: "unselectCellAction", 
		keys: ["Esc"],
		mode: UI.MODE_CURSOR_ACTION
	});
	Keys.assign({
		action: "chooseCell", 
		keys: ["Space"],
		mode: UI.MODE_CURSOR_ACTION
	});
	Keys.assign({
		action: "chooseCell", 
		keys: ["F"],
		mode: UI.MODE_CURSOR_ACTION
	});
	Keys.assign({
		action:"cursorMove", 
		keys: ["Num 8"],
		mode: UI.MODE_CURSOR_ACTION,
		arguments: [Side.N]
	});
	
};
