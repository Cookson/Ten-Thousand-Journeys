// Графические эффекты
// Все эффекты - методы класса персонажа
var effectTypes = {};
function Effect(startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback) {
	if (startX != undefined) {
		this.padding1 = (padding1) ? padding1 : 0;
		this.padding2 = (padding2) ? padding2 : 0;
		this.padding3 = (padding3) ? padding3 : 0;
		this.padding4 = (padding4) ? padding4 : 0;
		this.callback = (callback) ? callback : undefined;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.width = Math.abs(startX-endX)*32+32;
		this.height = Math.abs(startY-endY)*32+32;
		this.overallWidth = this.width+this.padding2+this.padding4;
		this.overallHeight = this.height+this.padding1+this.padding3;
		
		this.canvas = document.createElement("canvas");
		this.canvas.width = this.overallWidth;
		this.canvas.height = this.overallHeight;
		
		
		this.wrap = document.createElement("div");
		this.wrap.className = "wrap";
		this.wrap.style.top = (this.startY*32-this.padding1-((endY>startY)? 0 : this.height-32)-16)+"px";
		this.wrap.style.left = (this.startX*32-this.padding4-((endX>startX)? 0 : this.width-32))+"px";
		this.wrap.style.zIndex=9000;
		this.wrap.appendChild(this.canvas);
		gameField.appendChild(this.wrap);
		
		this.ctx = this.canvas.getContext("2d");
		this.particles = [];
		this.frameEvents = {};
	}
};
Effect.prototype.move = function(x, y) {
	this.wrap.style.top = (y*32-this.padding1-((this.endY>this.startY)? 0 : this.height-32)-16)+"px";
	this.wrap.style.left = (x*32-this.padding4-((this.endX>this.startX)? 0 : this.width-32))+"px";
};
Effect.prototype.pause = function() {
// Places a fake function to _this particular_ object to pause the draw "loop"
	this.start = function() {};
};
Effect.prototype.clear = function() {
	this.ctx.clearRect(0,0,this.canvas.width,this.canvas.height);
};
Effect.prototype.resume = function() {
// Deletes the fake function from _this particular_ object 
// so the object subsequently calls to start() in its prototype.
	delete this.start;
	this.start();
};
Effect.prototype.addParticle = function(particle) {
	this.particles.push(particle);
};
Effect.prototype.drawParticle = function(particle) {
//	console["log"](particle.imageData, particle.x, this.padding4, particle.y, this.padding1);
	this.ctx.putImageData(particle.imageData, particle.x+this.padding4, particle.y+this.padding1);	
};
Effect.prototype.draw = function() {
	this.ctx.clearRect(0,0,this.overallWidth,this.overallHeight);
	var len = this.particles.length;
	for (var i=0;i<len;i++) {
		this.particles[i].step();
		if (this.particles[i].abandoned) {
			this.particles.splice(i,1);
			i--;
			len--;
		} else {
			this.drawParticle(this.particles[i]);
		}
	}
//	this.ctx.fillStyle="#fadebb";
//	this.ctx.fillRect(0,0,this.canvas.width,this.canvas.height);
//	this.ctx.fillStyle="#fa3e3b";
//	this.ctx.fillRect(this.padding4,this.padding1,this.width,this.height);
//	this.ctx.fillStyle="#111";
//	this.ctx.fillRect(this.padding4,this.padding1,3,3);
};
Effect.prototype.start = function() {
	this.framesLeft--;
	if (this.framesLeft < 0) {
		this.destroy();
		this.callback && this.callback();
		return true;
	}
	var startTime = new Date().getTime();
	this.frameEvents[this.frames-this.framesLeft] && this.frameEvents[this.frames-this.framesLeft].apply(this);
	this.draw();
	var effect = this;
	setTimeout(function() {
		effect.start();
	}, this.frameTime - new Date().getTime() + startTime);
};
Effect.prototype.markForDestruction = function() {
	this.framesLeft = -1;
};
Effect.prototype.destroy = function() {
	this.wrap.removeChild(this.canvas);
	gameField.removeChild(this.wrap);
};
Effect.prototype.getPixelStartX = function() {
	return (this.startX < this.endX) ? 16 : this.width - 16;
};
Effect.prototype.getPixelStartY = function() {
	return (this.startY < this.endY) ? 16 : this.height - 16;
};
Effect.prototype.getPixelEndX = function() {
	return (this.startX > this.endX) ? 16 : this.width - 16;
};
Effect.prototype.getPixelEndY = function() {
	return (this.startY > this.endY) ? 16 : this.height - 16;
};
Effect.prototype.addFrames = function(amount) {
	this.framesLeft += amount;
	this.frames += amount;
};
Effect.prototype.FPS = 60;
Effect.prototype.frameTime = 1000 / Effect.prototype.FPS;
Effect.prototype.frameTimeSec = Effect.prototype.frameTime / 1000;
Effect.prototype.makeInfinite = function() {
	this.framesLeft += 100000000;
	this.frames += 100000000;
};
function Particle(name, startX, startY, effect, initFunc, movementFunc) {
	this.imageData = particlesImageData[name] ;
	this.startX = startX;
	this.startY = startY;
	this.x = startX;
	this.y = startY;
	this.effect = effect;
	this.step = movementFunc;
	initFunc.apply(this);
}
Particle.prototype.enablePointKinematics = function() {
	this.vx = 0;
	this.vy = 0;
	this.ax = 0;
	this.ay = 0;
};
Particle.prototype.pointKinematicsMove = function() {
	this.vx += this.ax * Effect.prototype.frameTimeSec;
	this.vy += this.ay * Effect.prototype.frameTimeSec;
	this.x += this.vx * Effect.prototype.frameTimeSec;
	this.y += this.vy * Effect.prototype.frameTimeSec;
};
(function () {
	effectTypes.linear = function (startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback) {
		arguments.callee.prototype.constructor.apply(this, arguments);
		this.frames = Math.round(distance(startX, startY, endX, endY)*32*(this.FPS/500));
		this.framesLeft = this.frames;
		var lin = function() {
			this.x += this.dx;
			this.y += this.dy;
			if (Math.abs(this.x-this.endX) < 1) {
				this.abandoned = true;
			}
		};
		var init = function() {
			this.endX = this.effect.getPixelEndX()+(Math.random()*60-30)*Math.cos(Math.random()*Math.PI);
			this.endY = this.effect.getPixelEndY()+(Math.random()*60-30)*Math.sin(Math.random()*Math.PI);
			this.dx = (this.endX - this.startX) / this.effect.framesLeft;
			this.dy = (this.endY - this.startY) / this.effect.framesLeft;
		};
		var sX = this.getPixelStartX();
		var sY = this.getPixelStartY();
		var eventAdd = function() {
			this.addFrames(1);
			for (var i=0;i<20;i++) {
				this.addParticle(new Particle(
					"spark1",
					this.getPixelStartX()+Math.random()*10-5, 
					this.getPixelStartY()+Math.random()*10-5, 
					this, 
					init, 
					lin
				));
			}
		};
		for (var i=1;i<40;i++) {
			this.frameEvents[i*1] = eventAdd;
		}
//		for (var i=0;i<1000;i++) {
//			this.addParticle(new Particle(
//				"spark1", 
//				sX+(Math.random()*60-30)*Math.cos(Math.random()*Math.PI), 
//				sY+(Math.random()*60-30)*Math.sin(Math.random()*Math.PI), 
//				this, init, lin));
//		}
		this.start(); 
	};
	effectTypes.rain = function (startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback) {
		arguments.callee.prototype.constructor.call(this, startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback);
		this.frames = Math.round(this.overallHeight*(this.FPS/500));
		this.framesLeft = this.frames;
		
		var init = function() {
//			this.x = Math.random()*this.effect.overallWidth;
//			this.y = this.effect.overallHeight;
			this.endX = this.startX;
			this.endY = this.startY+200;
			this.dy = (this.effect.overallHeight) / this.effect.framesLeft;
		};
		var dropPath = function() {
			this.y += this.dy;
			if (this.y > this.endY) {
				this.abandoned = true;
			}
		};
		var addDrop = function() {
			this.frameEvents[this.frames-this.framesLeft+2] = arguments.callee;
			this.addFrames(2);
			for (var i=0;i<10;i++) {
				this.addParticle(new Particle(
						"shiver1", 
						Math.random()*this.overallWidth-this.padding4, 
						-this.padding1+Math.random()*(this.overallHeight-200), 
						this, init, dropPath));
			}
		};
		this.addFrames(10);
		this.frameEvents[2] = addDrop;
		this.start();
	};
	effectTypes.sinusoidal = function(startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback) {
		arguments.callee.prototype.constructor.call(this, startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback);
		var init = function() {
			this.endX = (this.effect.startX > this.effect.endX) ? 16 : this.effect.width-16;
			this.endY = (this.effect.startY > this.effect.endY) ? 16 : this.effect.height-16;
			this.dx = (this.endX - this.startX)/100;
			this.dy = (this.endY - this.startY)/100;
			this.cx = this.effect.getPixelStartX();
			this.cy = this.effect.getPixelStartY();
		};
		var tg = (endX-startX)/(endY-startY);
		var lin = function() {
//			var a = Math.PI*(Math.abs((this.cx-this.endX)/(this.endX-this.startX))*7)
//			var d = Math.sin(a);
//			var dx = Math.sin(a)*Math.cos(Math.PI/2 + a);
//			var dy = Math.cos(a)*Math.cos(Math.PI/2 + a);
			var dx=0;
			var dy=0;
			this.cx++;
			this.cy += tg;
			this.x += this.cx + dx;
			this.y += this.cy + dy;
		};
		var sX = (this.startX < this.endX) ? 16 : this.width - 16;
		var sY = (this.startY < this.endY) ? 16 : this.height - 16;
		for (var i=0;i<10000;i++) {
			this.addParticle(new Particle("spark1", sX+Math.random()*100-50, sY+Math.random()*100-50, this, init, lin));
		}
		this.start(); 
	};
	effectTypes.blood = function(startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback) {
		arguments.callee.prototype.constructor.call(this, startX, startY, endX, endY, padding1, padding2, padding3, padding4);
		this.frames = Math.round(this.FPS/3*2);
		this.framesLeft = this.frames;
		var init = function() {
			this.dx = Math.random()-0.5;
			this.dy = Math.random()-0.5;
			this.dx = Math.random()-0.5;
			this.dy = Math.random()-0.5;
		};
		var tg = (endX-startX)/(endY-startY);
		var lin = function() {
			this.x += this.dx;
			this.y += this.dy;
		};
		var sX = (this.startX < this.endX) ? 16 : this.width - 16;
		var sY = (this.startY < this.endY) ? 16 : this.height - 16;
		for (var i=0;i<10;i++) {
			this.addParticle(new Particle("blood1", 16, 16, this, init, lin));
		}
		this.start(); 
	};
	effectTypes.confuse = function(startX, startY, endX, endY, padding1, padding2, padding3, padding4, callback) {
		arguments.callee.prototype.constructor.apply(this, arguments);
		this.frames = Math.round(this.overallHeight*(this.FPS/500));
		this.framesLeft = this.frames;
		this.makeInfinite();
		var init = function() {
			this.angle = Math.random()*Math.PI*2;
			this.spd = Math.max(this.angle / 2, Math.PI/3);
			this.x = Math.cos(this.angle)*32 + 16;
			this.y = Math.sin(this.angle)*32 + 16;
			this.dr = Math.random()*0.3;
			this.r = Math.max(Math.random()*32, 20);
		};
		var mov = function() {
			this.angle += this.spd * Effect.prototype.frameTimeSec;
			this.x = Math.cos(this.angle)*this.r + 16;
			this.y = Math.sin(this.angle)*this.r + 16;
			this.r += this.dr;
			if (this.r > 40 || this.r < 20) {
				this.dr = -this.dr;
			}
		};
		for (var i=0; i<8; i++) {
			var p = this.addParticle(new Particle("shiver1", 0, 0, this, init, mov));
		}
		this.start();
	};
	effectTypes[4] = effectTypes.confuse;
})();
var proto = new Effect();
for (var i in effectTypes) {
	effectTypes[i].prototype = proto;
}