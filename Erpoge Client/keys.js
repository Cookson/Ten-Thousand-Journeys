/* keys.js: Default control settings
 */
onLoadEvents['keys'] = function _() {
	new UIKeymapping(
			"Default",
			["<", "leaveLocation"],
			["r", "quickRefresh"],
			["t", "focusOnChat"],
			[">", "enterLocation"],
			["Ctrl", "F1", "toggleUI"],
			["n", "toggleSkillsWindow"],
			["F4", "toggleSettingsWindow"],
			["c", "rotateCamera"],
			["s", "idle"],
			["f", "aimMissile"],
			["w", "iconsInventoryShowKeysToPutOn"],
			["d", "iconsInventoryShowKeysToDrop"],
			["Q", "iconsInventoryShowKeysToSelectMissile"],
			["T", "iconsEquipmentTakeOff"],
			["g", "iconsLootShowKeysToPickUp"],
			[",", "iconsLootShowKeysToPickUp"],
			["?", "showCurrentKeymapping"]
	);
	new UIKeymapping(
			"CellCursor",
			["Esc", "unselectCellAction"],
			["Space", "chooseCell"],
			["F", "chooseCell"],
			["Num 5", "chooseCell"],
			["Num 8", "cursorMove", [Side.N]]
	);
	new UIKeymapping(
			"Chat",
			["Enter", "sendToChat"],
			["Esc", "closeChat"]
	);
	new UIKeymapping(
			"Container",
			["Esc", "closeContainer"],
			["=", "takeAllFromContainer"]
	);
	UI.setKeyMapping("Default");
};
