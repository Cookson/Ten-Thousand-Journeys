﻿// Имя, сила, ловкость, мудрость, аммуниция
/*
Типы character.
Формат:
"type"=>array(
	0:name,
	1:maxHp,
	2:maxMp,
	3:str,
	4:dex,
	5:wis,
	6:int,
	7:humanoid (true|false),
	8:hasShootingAbility (true|false)
)
*/
// Имя, сила, ловкость, мудрость, аммуниция
characterTypes = {
	"bear":["bear", 30, 0], 
	"innkeeper":["innkeeper", 30, 0], 
	"goblin":["goblin", 30, 0], 
	"ogre":["ogre", 30, 0], 
	"dragon":["dragon", 300, 200], 
	"goblinMage":["goblin-mage", 30, 0], 
	"dwarvenHooker":["dwarven hooker", 30, 0]
};


characterSpriteSizes= {
	"dragon":[96,64]
};