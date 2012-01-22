function Benchmark() {
	this.minimapTime = 150;
	this.dollTime = 350;
	this.arithmeticsTime = 110;
	this.testMinimap = function(times) {
		var t = new Date().getTime();
		for ( var i = 0; i < times; i++) {
			// Миникарта
			minimap.draw();
		}
		t = new Date().getTime() - t;
		this.logger.log("Minimap test: "
				+ t
				+ " ms ("
				+ this.percentageClassName(Math.round(this.minimapTime / t
						* 100)) + ")");
	};
	this.testDoll = function(times) {
		var t = new Date().getTime();
		var character = {};
		character.equipment = [ 71, 301, 301, 322, 313, 304, 152 ];
		character.race = 1;
		character.doll = new Doll(character);
		for ( var i = 0; i < times; i++) {
			character.doll.draw();
		}
		t = new Date().getTime() - t;
		this.logger.log("Doll test: " + t + " ms ("
				+ this.percentageClassName(Math.round(this.dollTime / t * 100))
				+ ")");
	};
	this.testArithmetics = function(times) {
		// Тест скорости выполнения арифметических действий
		var t = new Date().getTime();
		for ( var i = 0; i < times; i++) {
			9000 / 9001 + 9002 - 9003 * 9004 % 9006;
		}
		t = new Date().getTime() - t;
		this.logger.log("Arithmetics test: "
				+ t
				+ " ms ("
				+ this.percentageClassName(Math.round(this.arithmeticsTime / t
						* 100)) + ")");
	};
	this.animationTest = function(time) {
		var b = fps;
		fps = 60;
		var t = new Date().getTime();
		var bench = this;
		qanimate(
				Player.cellWrap,
				[ -100, 0 ],
				time / 6,
				function() {
					qanimate(
							Player.cellWrap,
							[ 100, 100 ],
							time / 6,
							function() {
								qanimate(
										Player.cellWrap,
										[ 100, -100 ],
										time / 6,
										function() {
											qanimate(
													Player.cellWrap,
													[ -100, -100 ],
													time / 6,
													function() {
														qanimate(
																Player.cellWrap,
																[ -100, 100 ],
																time / 6,
																function() {
																	qanimate(
																			Player.cellWrap,
																			[
																					100,
																					0 ],
																			time / 6,
																			function() {
																				fps = b;
																				t = new Date()
																						.getTime()
																						- t;
																				bench.logger
																						.log("Animation test: "
																								+ t
																								+ " ms ("
																								+ bench
																										.percentageClassName(Math
																												.round(time
																														/ t
																														* 100))
																								+ ")");
																			});
																});
													});
										});
							});
				});
	};
	this.percentageClassName = function(percentage) {
		// Возвращает строку названием класса элемента для отображения
		// производительности, выраженной в процентах
		var cls = "benchmark";
		if (percentage < 30) {
			cls += "VeryBad";
		} else if (percentage < 70) {
			cls += "Bad";
		} else if (percentage < 130) {
			cls += "Good";
		} else {
			cls += "VeryGood";
		}
		return "<span class='" + cls + "'/>" + percentage + "%</span>";
	};
	this.createLogWindow = function() {
		this.logWindow = new GameWindow("GAlert", {
			width : "400px",
			height : "300px"
		});
		this.logElem = document.createElement("div");
		this.logElem.id = "benchmarkLog";
		this.logWindow.element.insertBefore(this.logElem,
				this.logWindow.element.children[0]);
		this.logWindow.show();
	};
	this.log = function(text) {
		this.logElem.innerHTML += text + "<br />";
	};
	this.test = function() {
		this.logger = this; // Объект, который ведёд лог объекта. console или
							// this
		this.createLogWindow();
		this.testMinimap(100);
		this.testDoll(10000);
		this.testArithmetics(9000000);
		this.animationTest(1000);
	};
}
benchmark = new Benchmark();