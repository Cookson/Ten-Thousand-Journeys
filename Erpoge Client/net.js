onLoadEvents['ajax'] = function _() {
	// Обработчики событий ajax
	serverAddress = localStorage.getItem("serverAddress");
	Net.readStorageToServers();
	Net.init();
};
Net = {
	SERVER_INFO				: 0,
	ATTACK					: 1,
	MOVE					: 2,
	PUT_ON					: 3,
	TAKE_OFF				: 4,
	PICK_UP_PILE			: 5,
	LOGIN 					: 6,
	LOAD_CONTENTS			: 7,
	AUTH					: 8,
	APPEAR 					: 9,
	WORLD_TRAVEL			: 10,
	DEAUTH					: 11,
	CHAT_MESSAGE			: 12,
	DROP_PILE				: 13,
	OPEN_CONTAINER			: 14,
	PUT_TO_CONTAINER		: 15,
	TAKE_FROM_CONTAINER		: 16,
	CAST_SPELL				: 17,
	SHOOT_MISSILE			: 18,
	USE_OBJECT				: 19,
	CHECK_OUT				: 20,
	ENTER_LOCATION			: 21,
	LEAVE_LOCATION			: 22,
	ANSWER					: 23,
	START_CONVERSATION		: 24,
	DROP_UNIQUE				: 25,
	PICK_UP_UNIQUE			: 26,
	LOAD_PASSIVE_CONTENTS	: 27,
	ACCOUNT_REGISTER		: 28,
	PLAYER_CREATE			: 29,
	IDLE					: 30,
	/* Special actions */
	PUSH                    : 201,
	CHANGE_PLACES           : 202,
	MAKE_SOUND              : 203,
	JUMP                    : 204,
	SHIELD_BASH             : 205,
	serverAddress : "ws://"+window.location.host+":8787",
	websocket : null,
	accountPlayers : [],
	send : function _(data, onmessage, callback) {
		// Функция, отправляющая серверу данные, получающая ответ и
		// обрабатывающая его.
		onmessage && (this.onmessage = onmessage);
		callback && (this.callback = callback);
		this.websocket.send(JSON.stringify(data));
	},
	init : function _() {
		
		this.websocket = window.MozWebSocket ? new MozWebSocket(this.serverAddress) : new WebSocket(this.serverAddress);
		this.websocket.onopen = function() {
//			document.getElementById("stChooseServerForm").onsubmit = handlers.stChooseServerForm.submit;
			
			Net.send({a:Net.SERVER_INFO});
			if (!localStorage.getItem(101)) {
				// Автовыбор сервера из URL
//				document.getElementById("stChooseServer").value = window.location.href
//						.replace(/http:\/\/|\/$/g, "");
			} else if (localStorage.getItem(101) && localStorage.getItem(101) !== "0") {
				// Если страница загрузилась в результате "быстрой перезагрузки",
				// залогиниться под прошлым персонажем
				serverAddress = localStorage.getItem("serverAddress");
				var sData = localStorage.getItem(101).split(",");
				localStorage.removeItem(101);
				setTimeout(function() {
				/* */ // Здесь без таймаута почему-то не отрисовываются куклы после загрузки уровня.
					Net.logInForCharacter(sData[0].replace(/\`/, ""), sData[1], sData[2]);
					UI.showAlwaysShownElements();
				}, 100);
			} else if (localStorage.getItem("serverAddress") !== null) {
				// Automatically choose the last server, if it is available
				document.getElementById("stChooseServer").value = localStorage
						.getItem("serverAddress");
				document.getElementById("stChooseServerForm").onsubmit();
			} else {
				showStChooseServer();
			}
		};
		this.websocket.onclose = function() {
			console["log"]("Socket closed");
		};
		this.websocket.onmessage = function(data) {
			try {
				var parsedData = JSON.parse(data.data);
			} catch(e) {
				console["log"]("Incorrect json output from server: ",data.data);
				return;
			}
			if (parsedData.a === undefined) {
			// Non-synchronized data recieving
				serverAnswer = parsedData;
				serverAnswerIterator = 0;
				handleNextEvent();
			} else if (Net.onmessage) {
			// Synchronized data recieving
				Net.onmessage(parsedData);
			}
		};
	},
	readStorageToServers : function _() {
		// Read servers' cookies to servers array
		for (var i = 1; localStorage.getItem("server" + i) !== null; i++) {
			servers.push(localStorage.getItem("server" + i).split(" "));
		}
	},
	setServerAdressesStorage : function _(address, login, password) {
		// Установить для сервера значение в localStorage с адресом, логином и
		// паролем,
		// а также пишет в массив servers
		var i = 1;
		for (; i < servers.length; i++) {
			if (servers[i][0] == address) {
				// Если такой сервер уже есть, обновить для него значение с
				// новым
				// логином и паролем
				localStorage.setItem("server" + i, address + " " + login + " "
						+ password);
				servers[i][1] = login;
				servers[i][2] = password;
			}
			break;
		}
		if (i == servers.length) {
			// Если такого сервера нет, создать для него значение с логином и
			// паролем
			localStorage.setItem("server" + i, address + " " + login + " "
					+ password);
			servers.push([ serverAddress, login, password ]);
		}
	},
	getServerNum : function _(address) {
		// Получает номер сервера с данным адресом в куках
		for ( var i = 1; localStorage.getItem("server" + i); i++) {
			if (localStorage.getItem("server" + i)[0] == address) {
				return i;
			}
		}
		return false;
	},
	getServer : function _(address) {
		// Получает из кук данные о соединении с сервером (адрес, логин и
		// пароль) по
		// адресу сервера
		for ( var i = 1; localStorage.getItem("server" + i) !== null; i++) {
			if (localStorage.getItem("server" + i).split(" ")[0] == address) {
				return servers[i];
			}
		}
		return false;
	},
	quickRefresh: function _() {
		localStorage.setItem(101,Player.characterId+","+Global.playerLogin+","+Global.playerPassword);
		window.location.reload();
	},
	logInForCharacter: function _(characterId, login, password) {
		Net.send({
			a:Net.LOAD_CONTENTS,
			login:login,
			password:password,
			characterId:characterId
		});
		Global.playerLogin = login;
		Global.playerPassword = password;
		UI.notify("login");
	}
};

