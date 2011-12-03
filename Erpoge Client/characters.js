// Имя, сила, ловкость, мудрость, аммуниция
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
	"innkeeper":["дварфийская проститутка", 30, 0], 
	"goblin":["гоблин", 30, 0], 
	"ogre":["огр", 30, 0], 
	"dragon":["дракон", 300, 200], 
	"goblinMage":["гоблин-маг", 30, 0], 
	"dwarvenHooker":["дварфийская проститутка", 30, 0]
};


characterSpriteSizes= {
	"dragon":[96,64]
};