/* keys.js: Default control settings
 */
onLoadEvents['keys'] = function _() {
	var defaultKeymapping = new UIKeymapping(
			"Default",
			["<", "leaveLocation"],
			["r", "quickRefresh"],
			["t", "focusOnChat"],
			[">", "enterLocation"],
			["Ctrl", "F1", "toggleUi"],
			["n", "toggleSkillsWindow"],
			["F4", "toggleSettingsWindow"],
			["c", "rotateCamera"],
			["s", "idle"],
			["f", "selectMissile"]
	);
	var cellCursorKeymapping =  new UIKeymapping(
			"Cell cursor",
			["Esc", "unselectCellAction"],
			["Space", "chooseCell"],
			["F", "chooseCell"],
			["Num 5", "chooseCell"],
			["Num 8", "cursorMove", [Side.N]]
	);
	var chatKeymapping = new UIKeymapping(
			"Chat",
			["Enter", "sendToChat"],
			["Esc", "closeChat"]
	);
	var containerKeymapping = new UIKeymapping(
			"Container",
			["Esc", "closeContainer"],
			["=", "takeAllFromContainer"]
	);
	Keys.keyMapping = defaultKeymapping;
};
