// Функции обработчиков событий элементов
handlers={
	keysActions: {
	/* Registered actions. Now there are universal action that are
	 * registered by default. UI elements save their actions and
	 * contexts there when constructed.
	 */
		
	},
	
	globalMapClickHandler: function _(x,y,e) {
		if (e.shiftKey) {
			centerWorldCamera(x,y);
		} else {
			if (Player.isPartyLeader) {
				performAction("worldTravel", [x,y]);
			} else {
				UI.notify("alert", "Когда вы в группе, только лидер группы может перемещать группу по карте")
			}
		}
	},
	cellInfo: {
		mouseover:function _() {
			this.style.display="none";
		}
	},
	speechBubble: {
		click: function _(e) {
			gameField.removeChild(this);
			event.stopPropagation();
			return false;
		},
		mouseover: function _(e) {
			this.setAttribute("isMouseOver", "1");
		},
		mouseout: function _(e) {
			this.setAttribute("isMouseOver", "0");
			if (this.getAttribute("time") < new Date().getTime()-2000) {
				gameField.removeChild(this);
			}
		},
		timeout: function _(e) {
			gameField.removeChild(this);
			event.stopPropagation();
			return false;
		}
	},
	initWindows: function _() {
	// Windows are initiated before other ui elements, but after panels
		UI.addWindow({
			type: "windowGameAlert",
			hAlign: "left",
			vAlign: "top",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowInfo",
			hAlign: "right",
			vAlign: "bottom",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowLogin",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowAccountCharacters",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowSkills",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowDeath",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowSettings",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowDialogue",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowContainer",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowAccountCreate",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
		UI.addWindow({
			type: "windowPlayerCreate",
			hAlign: "center",
			vAlign: "middle",
			displayMode: "always",
			keyMode: "always"
		});
	},
	initInterface : function _() {
	// First panels are initiated, then windows,
	// then other elements.
		UI.addPanel({
			name: "main",
			width: 204,
			side: "right"
		});
		handlers.initWindows();
		UI.addElement({
			type:"chat",
			hAlign: "left",
			vAlign: "bottom",
			displayMode: "always",
			panel: null
		});
		UI.addElement({
			type: "hpBar",
			panel: "main"
		});
		UI.addElement({
			type: "mpBar",
			panel: "main"
		});
		UI.addElement({
			type: "epBar",
			panel: "main"
		});
		UI.addElement({
			type: "attributeList",
			panel: "main"
		});
		UI.addElement({
			type: "iconMissileType",
			panel: "main"
		});
		UI.addElement({
			type: "iconsSpells",
			panel: "main"
		});
		UI.addElement({
			type: "iconsEquipment",
			panel: "main"
		});
		UI.addElement({
			type: "iconsInventory",
			panel: "main"
		});
		UI.addElement({
			type: "iconsLoot",
			panel: "main"
		});
//		UI.addElement({
//			type: "minimap",
//			hAlign: "left",
//			vAlign: "top",
//			displayMode: "in location",
//			panel: null
//		});
		UI.addElement({
			type: "actionsPanel",
			hAlign: "center",
			vAlign: "bottom",
			displayMode: "in location",
			panel: null
		});
	}
};
