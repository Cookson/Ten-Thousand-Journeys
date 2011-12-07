package erpoge.terrain.settlements;

import java.awt.Rectangle;

import erpoge.Chance;
import erpoge.inventory.ItemPile;
import erpoge.objects.GameObjects;
import erpoge.terrain.CellCollection;
import erpoge.terrain.TerrainBasics;
import erpoge.terrain.TerrainGenerator;
import erpoge.terrain.locationtypes.Settlement;
import erpoge.terrain.settlements.Building.BasisBuildingSetup;
import erpoge.characters.Character;
import erpoge.characters.CharacterCondition;
import erpoge.characters.CustomCharacterAction;
import erpoge.characters.Dialogue;
import erpoge.characters.DialoguePoint;
import erpoge.characters.NonPlayerCharacter;
import erpoge.characters.PlayerCharacter;

public class Tavern extends Building {
	public Tavern(TerrainGenerator settlement, int x, int y, int width, int height) {
		super(settlement, x, y, width, height, 4);
		buildBasis(4, BasisBuildingSetup.CONVERT_TO_DIRECTED_TREE);
		placeFrontDoor(-1);
		Rectangle lobbyRec = rectangleSystem.rectangles.get(lobby);
		for (int sx=lobbyRec.x; sx<lobbyRec.x+lobbyRec.width; sx++) {
			if (!settlement.isDoor(sx, lobbyRec.y-1)) {
				settlement.setObject(sx, lobbyRec.y, GameObjects.OBJ_VINESHELF);
			}
		}
		
		NonPlayerCharacter innkeeper = settlement.createCharacter("innkeeper", "Christian", lobbyRec.x, lobbyRec.y+1);
		innkeeper.setFraction(1);
		// Dialogue
		Dialogue dialogue = new Dialogue();
		CustomCharacterAction aGiveMoney = new CustomCharacterAction() {
			public void perform(Character actor, Character reciever) {
				reciever.getItem(new ItemPile(900000,100));
			}
		};
		DialoguePoint hello = dialogue.addPoint("Привет, странник!");
		DialoguePoint giveMoney = dialogue.addPoint("Вот тебе сто денег.", aGiveMoney);
		DialoguePoint youAreHuman = dialogue.addPoint("Ты человек!");
		DialoguePoint youAreElf = dialogue.addPoint("Ты эльф!");
		DialoguePoint youAreAnother = dialogue
				.addPoint("Ты ни человек, ни эльф!");
		DialoguePoint condWhoAmI = dialogue
				.addPoint(new CharacterCondition<DialoguePoint>(youAreHuman,
						youAreElf, youAreAnother) {
					public DialoguePoint test(Character opponent) {
						if (((PlayerCharacter) opponent).race == Character.RACE_HUMAN) {
							return results[0];
						} else if (((PlayerCharacter) opponent).race == Character.RACE_ELF) {
							return results[1];
						} else {
							return results[2];
						}
					}
				});
		dialogue.setRoot(hello);
		hello.addAnswer("Дай мне сто денег", giveMoney);
		hello.addAnswer("Скажи, добрый трактирщик, я эльф или человек?", condWhoAmI);
		giveMoney.addAnswer("Спасибо, пока!", hello, true);
		youAreHuman.addAnswer("Спасибо, пока!", hello, true);
		youAreElf.addAnswer("Спасибо, пока!", hello, true);
		youAreAnother.addAnswer("Спасибо, пока!", hello, true);
		innkeeper.setDialogue(dialogue);
		Rectangle tablesArea = new Rectangle(
				lobbyRec.x+1, lobbyRec.y+1, 
				lobbyRec.width-2, lobbyRec.height-2);
		for (int i=tablesArea.x+Chance.rand(0,1); i<tablesArea.x+tablesArea.width-1;i+=Chance.rand(1,2)) {
			for (int j=tablesArea.y+Chance.rand(0,1); j<tablesArea.y+tablesArea.height-1;j+=Chance.rand(1,2)) {
				settlement.setElement(i, j, TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TABLE_CHAIR_1);
			}
		}
//		CellCollection tablesArea = settlement.newCellCollection(CellCollection.rectangleToCellsList());
//		tablesArea.setElements(TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TABLE_CHAIR_1, 4);
//		tablesArea.setElements(TerrainBasics.ELEMENT_OBJECT, GameObjects.OBJ_TABLE_CHAIR_2, 4);
	}
}
